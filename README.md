<!--
 * This code is to be used exclusively in connection with Ping Identity Corporation software or services.
 * Ping Identity Corporation only offers such software or services to legal entities who have entered into
 * a binding license agreement with Ping Identity Corporation.
 *
 * Copyright 2024 Ping Identity Corporation. All Rights Reserved
-->
# Web3Auth

A simple authentication node for Access Management 7.4.0 and above. This node uses <strong>Web3Auth </strong>which is a pluggable wallet infrastructure for
Web3 wallets and applications.

The goal of this node is to log a user into Web3Auth, and retrieve the users wallet to add to the session.

This node uses the [Web3Auth Core Kit Single Factor Auth Web SDK](https://web3auth.io/docs/sdk/core-kit/sfa-web).

Copy the .jar file from the ../target directory into the ../web-container/webapps/openam/WEB-INF/lib directory where AM is deployed.  Restart the web container to pick up the new node.  The node will then appear in the authentication trees components palette.


## Dependencies

To use this node, you must:

<ul>
    <li>Setup a Web Application in Web3Auth with a Custom Authentication </li>
</ul>

## Inputs

The node expects a username to be passed in that will be used for wallet retrieval.

## Configuration
<table>
	<tr>
		<th>Property</th>
		<th>Usage</th>
        <th>Default</th>
	</tr>
    <tr>
		<td>* Username Attribute</td>
        <td>The attribute name that contains the unique username that will be provided to Web3Auth</td>
	    <td>username</td>
    </tr>
	<tr>
		<td>* Client ID </td>
        <td>Client ID from the Web3Auth Dashboard</td>
        <td>N/A</td>
	</tr>
    <tr>
		<td>* Private JWK</td>
        <td>The private portion of the JWK used to sign the JWT sent to Web3Auth</td>
	    <td>N/A</td>
     </tr>
     <tr>
		<td>* Web3Auth Network</td>
        <td>Get your Network from Web3Auth Dashboard</td>
        <td>sapphire_mainnet</td>
	</tr>
     <tr>
		<td>* JWT Time-to-live</td>
        <td>Time-to-live in milliseconds</td>
        <td>2000</td>
	</tr>
    <tr>
		<td>* Chain Namespace</td>
        <td>The Compatible Chain to use</td>
        <td>eip155</td>
	</tr>
    <tr>
		<td>* Chain ID</td>
		<td>The chain id of the chain</td>
        <td>0x1</td>
	</tr>
    <tr>
		<td>* RPC Target</td>
		<td>RPC target Url for the chain</td>
        <td>https://rpc.ankr.com/eth</td>
	</tr>
    <tr>
		<td>* Display Name</td>
		<td>* Display Name for the chain</td>
        <td>Ethereum Mainnet</td>
	</tr>
    <tr>
		<td>* Block Explorer</td>
		<td>Url of the block explorer</td>
        <td>https://etherscan.io/</td>
	</tr>
    <tr>
		<td>* Ticker</td>
		<td>Default currency ticker of the network (e.g: ETH)</td>
        <td>ETH</td>
	</tr>
    <tr>
		<td>* Ticker Name</td>
		<td>Name for currency ticker (e.g: Ethereum)</td>
        <td>Ethereum</td>
	</tr>
  <tr>
		<td>* Verifier Identifier</td>
		<td>The unique identifier for your custom authentication registration on the auth network</td>
        <td>N/A</td>
	</tr>
  <tr>
		<td>* Issuer</td>
		<td>Issuer used in "Select JWT validation" in Web3Auth Custom Authentication configuration</td>
        <td>N/A</td>
	</tr>
  <tr>
		<td>* Audience</td>
		<td>Audience used in "Select JWT validation" in Web3Auth Custom Authentication configuration</td>
        <td>N/A</td>
	</tr>
  <tr>
		<td>* Message while waiting</td>
		<td>Message while waiting</td>
        <td>Please wait while we retrieve your Web3Auth Session ID</td>
	</tr>
  <tr>
		<td>* Session ID to Session</td>
		<td>Save the Web3Auth Session ID to the Access Management Session of the user</td>
        <td>True</td>
	</tr>
  <tr>
		<td>* Session ID to Shared State</td>
		<td>Save the Web3Auth Session ID to the Journey Shared State</td>
        <td>True</td>
	</tr>


</table>
* = Required


## Outputs

The Web3Auth SessionID can be saved to the Shared State and the users Access Management Session.

## Outcomes

<code>Next</code>
<br>Successfully retrieved the wallet and stores the sessionID in the FR secure session<br>

<code>Error</code>
<br>   Could not retrieve the wallet and store the sessionID in the FR secure session
## Troubleshooting

Review the log messages to find the reason for the error and address the issue appropriately.

Click here for <a href="https://web3auth.io/docs/what-is-web3auth">Documentation</a>

## Example Journey


![ScreenShot](./web3auth.png)


