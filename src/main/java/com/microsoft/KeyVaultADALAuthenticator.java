package com.microsoft;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;
import com.microsoft.rest.credentials.ServiceClientCredentials;


/**
 * Authenticates to Azure Key Vault by providing a callback to authenticate
 * using adal.
 * 
 * @author tifchen
 *
 */
public class KeyVaultADALAuthenticator {

	public static KeyVaultClient getAuthenticatedClient() {
		//Creates the KeyVaultClient using the created credentials.
		return new KeyVaultClient(createCredentials());
	}

	/**
	 * Creates a new KeyVaultCredential based on the access token obtained.
	 * @return
	 */
	private static ServiceClientCredentials createCredentials() {
		return new KeyVaultCredentials() {

			//Callback that supplies the token type and access token on request.
			@Override
			public String doAuthenticate(String authorization, String resource, String scope) {
					
					AuthenticationResult authResult;
					try {
						authResult = getAccessToken(authorization, resource);
						return authResult.getAccessToken();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return "";
			}
			
		};
	}
	
	/**
	 * Private helper method that gets the access token for the authorization and resource depending on which variables are supplied in the environment.
	 * 
	 * @param authorization
	 * @param resource
	 * @return
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws MalformedURLException 
	 * @throws Exception
	 */
	private static AuthenticationResult getAccessToken(String authorization, String resource) throws InterruptedException, ExecutionException, MalformedURLException {

		String clientId = System.getProperty("AZURE_CLIENT_ID");
		String clientKey = System.getProperty("AZURE_CLIENT_SECRET");

		AuthenticationResult result = null;
		
		//Starts a service to fetch access token.
		ExecutorService service = null;
		try {
			service = Executors.newFixedThreadPool(1);
			AuthenticationContext context = new AuthenticationContext(authorization, false, service);

			Future<AuthenticationResult> future = null;

			//Acquires token based on client ID and client secret.
			if (clientKey != null && clientKey != null) {
				ClientCredential credentials = new ClientCredential(clientId, clientKey);
				future = context.acquireToken(resource, credentials, null);
			}
			
			result = future.get();
		} finally {
			service.shutdown();
		}

		if (result == null) {
			throw new RuntimeException("Authentication results were null.");
		}
		return result;
	}
}
