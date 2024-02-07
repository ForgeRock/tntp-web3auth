/*
 * This code is to be used exclusively in connection with ForgeRock’s software or services. 
 * ForgeRock only offers ForgeRock software or services to legal entities who have entered 
 * into a binding license agreement with ForgeRock.  
 */

let web3auth = null;

(async function init() {

  const loadScript = (FILE_URL, async = true, type = 'text/javascript') => {
    return new Promise((resolve, reject) => {
      try {
        const scriptEle = document.createElement('script');
        scriptEle.type = type;
        scriptEle.async = async;
        scriptEle.src = FILE_URL;

        scriptEle.addEventListener('load', (ev) => {
          resolve({status: true});
        });

        scriptEle.addEventListener('error', (ev) => {
          reject({
            status: false,
            message: `Failed to load the script ${FILE_URL}`
          });
        });

        document.body.appendChild(scriptEle);
      } catch (error) {
        console.error(error);
        reject(error);
      }
    });
  };

  await loadScript('https://cdn.jsdelivr.net/npm/jquery@3/dist/jquery.min.js')
    .then(data => {
      console.log('Script loaded successfully', data);
    })
    .catch(err => {
      console.error(err);
    });

  await loadScript('https://cdn.jsdelivr.net/npm/buffer@6')
    .then(data => {
      console.log('Script loaded successfully', data);
    })
    .catch(err => {
      console.error(err);
    });

  await loadScript('https://cdn.jsdelivr.net/npm/@web3auth/single-factor-auth')
    .then(data => {
      console.log('Script loaded successfully', data);
    })
    .catch(err => {
      console.error(err);
    });

  await loadScript('https://cdn.jsdelivr.net/npm/@web3auth/ethereum-provider')
    .then(data => {
      console.log('Script loaded successfully', data);
    })
    .catch(err => {
      console.error(err);
    });

  await loadScript('https://cdn.jsdelivr.net/npm/web3@4.1.1/dist/web3.min.js')
    .then(data => {
      console.log('Script loaded successfully', data);
    })
    .catch(err => {
      console.error(err);
    });

  const clientId = "${subclientID}";

  const chainConfig = {
    chainNamespace: "${subchainNamespace}",
    chainId: "${subchainId}", // Please use 0x1 for Mainnet
    rpcTarget: "${subrpcTarget}",
    displayName: "${subdisplayName}",
    blockExplorer: "${subblockExplorer}",
    ticker: "${subticker}",
    tickerName: "${subtickerName}",
  };

  web3auth = new window.SingleFactorAuth.Web3Auth({
    clientId,
    web3AuthNetwork: "${subweb3AuthNetwork}",
  });

  const ethereumPrivateKeyProvider = new window.EthereumProvider.EthereumPrivateKeyProvider({
    config: {chainConfig},
  });

  await web3auth.init(ethereumPrivateKeyProvider);
  // IMP END - SDK Initialization

  if (web3auth.status === 'connected') {
    console.log('Connected!');
  } else {
    console.log('Not connected yet!');
  }

  const verifier = "${subverifier}";
  try {
    idToken = "${subidToken}";
    userID = "${subuserID}";
    const web3authSfaprovider = await web3auth.connect({
      verifier,
      verifierId: userID,
      idToken: idToken,
    });
    // IMP END - Login

    if (web3auth.status === 'connected') {
      console.log('Fully connected to Web3Auth!');

      /*const ethPrivateKey = await web3authSfaprovider.request({ method: "eth_private_key" });
      console.log("ETH Private Key", ethPrivateKey);

      const user = await web3auth.getUserInfo();
      console.log("User", user);

      const web3 = new Web3(web3auth.provider);
      const address = await web3.eth.getAccounts();
      console.log("Address", address);

      const balance = web3.utils.fromWei(
        await web3.eth.getBalance(address[0]), // Balance is in wei
        "ether"
      );
      console.log("Balance", balance);*/
      console.log("Here is the sessionID", web3auth.sessionId);

    } else {
      console.log('Still not connected JC!');
    }
  } catch (error) {
    alert(error);
  }
})();
