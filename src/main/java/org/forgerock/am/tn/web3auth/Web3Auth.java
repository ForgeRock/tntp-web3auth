/*
 * This code is to be used exclusively in connection with ForgeRock’s software or services. 
 * ForgeRock only offers ForgeRock software or services to legal entities who have entered 
 * into a binding license agreement with ForgeRock.  
 */


package org.forgerock.am.tn.web3auth;

import static org.forgerock.openam.auth.node.api.Action.send;

import java.io.IOException;
import java.net.URL;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.ConfirmationCallback;

import org.apache.commons.lang.text.StrSubstitutor;
import org.forgerock.json.jose.builders.SignedJwtBuilderImpl;
import org.forgerock.json.jose.jwk.RsaJWK;
import org.forgerock.json.jose.jws.JwsAlgorithm;
import org.forgerock.json.jose.jws.handlers.SecretRSASigningHandler;
import org.forgerock.json.jose.jwt.JwtClaimsSet;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.AbstractDecisionNode;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.NodeState;
import org.forgerock.openam.auth.node.api.StaticOutcomeProvider;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.secrets.SecretBuilder;
import org.forgerock.secrets.keys.SigningKey;
import org.forgerock.util.i18n.PreferredLocales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.google.inject.assistedinject.Assisted;
import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import com.sun.identity.sm.RequiredValueValidator;


@Node.Metadata(outcomeProvider  = Web3Auth.OutcomeProvider.class,
               configClass      = Web3Auth.Config.class,
               tags				= {"marketplace", "trustnetwork" })
public class Web3Auth extends AbstractDecisionNode {
	private final Config config;
    private final Logger logger = LoggerFactory.getLogger(Web3Auth.class);
    private String loggerPrefix = "[Web3Auth Node]" + Web3AuthPlugin.logAppender;
	private static final String NEXT = "NEXT";
	private static final String ERROR = "ERROR";
    private static final String BUNDLE = Web3Auth.class.getName();
    private static final String sdkJsPathTemplate = "org/forgerock/am/tn/web3auth/client.js";
    private final String clientScript;


    /**
     * Configuration for the node.
     */
    public interface Config {
        @Attribute(order = 100, validators = { RequiredValueValidator.class })
        default String usernameField() {
        	return "username";
        };
        
        @Attribute(order = 200, validators = { RequiredValueValidator.class })
    	String clientID();

        @Attribute(order = 300, validators = { RequiredValueValidator.class })
    	String privateKey();
        
        @Attribute(order = 400, validators = { RequiredValueValidator.class })
    	default String web3AuthNetwork() {
        	return "sapphire_mainnet";
        };
        
        @Attribute(order = 500, validators = { RequiredValueValidator.class })
    	default long ttl() {
        	return 2000;
        };
        
        @Attribute(order = 600, validators = { RequiredValueValidator.class })
    	default String chainNamespace() {
        	return "eip155";
        };        
        
        @Attribute(order = 700, validators = { RequiredValueValidator.class })
    	default String chainId() {
        	return "0x1";
        };        
        
        @Attribute(order = 800, validators = { RequiredValueValidator.class })
    	default String rpcTarget() {
        	return "https://rpc.ankr.com/eth";
        }; 
        
        @Attribute(order = 900, validators = { RequiredValueValidator.class })
    	default String displayName() {
        	return "Ethereum Mainnet";
        }; 
        
        @Attribute(order = 1000, validators = { RequiredValueValidator.class })
    	default String blockExplorer() {
        	return "https://etherscan.io/";
        };         
        
        @Attribute(order = 1100, validators = { RequiredValueValidator.class })
    	default String ticker() {
        	return "ETH";
        };        

        @Attribute(order = 1200, validators = { RequiredValueValidator.class })
    	default String tickerName() {
        	return "Ethereum";
        };  
        
        @Attribute(order = 1300, validators = { RequiredValueValidator.class })
    	String verifier();
    }

    /**
     * Guice constructor.
     * @param config The node configuration.
     * @throws NodeProcessException If there is an error reading the configuration.
     */
    @Inject
    public Web3Auth(@Assisted Config config) throws NodeProcessException {
        this.config = config;
        this.clientScript = readJS();
    }

    @Override
    public Action process(TreeContext context) {
    	
    	try {	

            logger.debug(loggerPrefix + "Started");
            
            String signedJWT = getSignedJWT(context);
            
            //ScriptTextOutputCallback scb = new ScriptTextOutputCallback(getStartScript(context.getStateFor(this).get(config.usernameField()).asString(), signedJWT));
            ScriptTextOutputCallback scb = getScriptedCallback(context.getStateFor(this).get(config.usernameField()).asString(), signedJWT);
            
            HiddenValueCallback hc = new HiddenValueCallback("web3authResponse");
            String[] callbackoptions = {"Next","Cancel"};
            ConfirmationCallback cc = new ConfirmationCallback(ConfirmationCallback.INFORMATION,callbackoptions, 0);
            Callback[] callbacks = new Callback[]{scb, hc, cc};
            return send(callbacks).build();
    		
    	}
    	catch (Exception ex) {
            String stackTrace = org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(ex);
            logger.error(loggerPrefix + "Exception occurred: " + stackTrace);
            System.out.println(stackTrace);
            context.getStateFor(this).putShared(loggerPrefix + "Exception", ex.getMessage());
            context.getStateFor(this).putShared(loggerPrefix + "StackTrace", stackTrace);
            return Action.goTo(ERROR).build();
        }
    	
    }
    
    private ScriptTextOutputCallback getScriptedCallback(String userId, String token) {
        Map<String, String> sdkConfigMap = new HashMap<>();
        sdkConfigMap.put("subclientID", config.clientID());
        sdkConfigMap.put("subchainNamespace", config.chainNamespace());
        sdkConfigMap.put("subrpcTarget", config.rpcTarget());
        sdkConfigMap.put("subchainId", config.chainId());
        sdkConfigMap.put("subdisplayName", config.displayName());
        sdkConfigMap.put("subblockExplorer", config.blockExplorer());
        sdkConfigMap.put("subticker", config.ticker());
        sdkConfigMap.put("subtickerName", config.tickerName());
        sdkConfigMap.put("subweb3AuthNetwork", config.web3AuthNetwork());
        sdkConfigMap.put("subverifier", config.verifier());
        sdkConfigMap.put("subidToken", token);
        sdkConfigMap.put("subuserID", userId);
        String sdkJs = new StrSubstitutor(sdkConfigMap).replace(this.clientScript);
        ScriptTextOutputCallback callback = new ScriptTextOutputCallback(sdkJs);
        return callback;
    }
    
    private String readJS() throws NodeProcessException {
        URL resource = Resources.getResource(sdkJsPathTemplate);
        try {
            return Resources.toString(resource, Charsets.UTF_8);
        } catch (IOException ex) {
            logger.error(String.format("%s %s: %s", loggerPrefix, ex.getClass().getCanonicalName(),
                            Arrays.toString(ex.getStackTrace())));
            throw new NodeProcessException(ex);
        }
    }
    
	private String getSignedJWT(TreeContext context) throws Exception{
		SecureRandom sr = new SecureRandom();
		long randomLong = Math.abs(sr.nextLong());
		
		RsaJWK privateRSAJWK = RsaJWK.parse(config.privateKey());		
		
		// sign setup
		SecretBuilder ssb = new SecretBuilder();
		ssb.expiresAt(Instant.now().plusMillis(config.ttl()));
		ssb.stableId(Long.toString(randomLong));
		ssb.secretKey(privateRSAJWK.toRSAPrivateKey());
		SigningKey sk = new SigningKey(ssb);
		SecretRSASigningHandler srsh = new SecretRSASigningHandler(sk);
		
		SignedJwtBuilderImpl jwtBuilder = new SignedJwtBuilderImpl(srsh)
				.headers().alg(JwsAlgorithm.RS256).done()
				.claims(getClaimSetToIG(context));

		String ets = jwtBuilder.build();
		return ets;
	}
	
	
	private JwtClaimsSet getClaimSetToIG(TreeContext context)throws Exception{
		JwtClaimsSet jwtClaims = new JwtClaimsSet();	
		NodeState ns = context.getStateFor(this);
		
		jwtClaims.setIssuer(context.request.serverUrl); //TODO make this a config input parm
		jwtClaims.setSubject(ns.get(config.usernameField()).asString());
		jwtClaims.addAudience("urn:" + context.request.hostName); //TODO make this a config input parm
		
		Date now = new Date();
		Date expireDate = new Date(now.getTime() + config.ttl());
		jwtClaims.setExpirationTime(expireDate);
		jwtClaims.setNotBeforeTime(now);
		jwtClaims.setIssuedAtTime(now);		
		return jwtClaims;
	}
    

    public static class OutcomeProvider implements StaticOutcomeProvider {
        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales) {
            ResourceBundle bundle = locales.getBundleInPreferredLocale(Web3Auth.BUNDLE,
                    OutcomeProvider.class.getClassLoader());

            return ImmutableList.of(
                new Outcome(NEXT, bundle.getString("nextOutcome")),
                new Outcome(ERROR, bundle.getString("errorOutcome"))
            );
        }
    }
    
}
