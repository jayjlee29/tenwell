package org.tenwell.identity.core;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.tenwell.identity.core.exception.IdentityException;
import org.tenwell.identity.core.saml2.SAMLSSOConstants;

public class IdentityProviderConfig {
	private Properties properties = new Properties();
	private IdentityProviderKeyStoreManager keyStoreManager;
	
	private IdentityProviderConfig(){
		
	}
	
	public static IdentityProviderConfig getConfig(HttpServletRequest req) throws IdentityException{
		
		return getConfig(req.getSession().getServletContext());
		
	}
	
	public static IdentityProviderConfig getConfig(ServletContext context) throws IdentityException{
		
		if(context.getAttribute(SAMLSSOConstants.CONFIG_BEAN_NAME)==null){
			return new IdentityProviderConfig();
		} 
		
		return (IdentityProviderConfig)context.getAttribute(SAMLSSOConstants.CONFIG_BEAN_NAME);
		
	}
	
	public void initConfig(ServletContext context) throws IOException, IdentityException{
		
		properties.load(context.getResourceAsStream("/WEB-INF/classes/saml/idp.properties"));
    	InputStream keyStoreInputStream = context.getResourceAsStream(properties.getProperty("KeyStore"));
        
    	keyStoreManager = new IdentityProviderKeyStoreManager(keyStoreInputStream,
                         properties.getProperty("KeyStorePassword").toCharArray(),
                         properties.getProperty("IdPPublicCertAlias"),
                         properties.getProperty("PrivateKeyAlias"),
                         properties.getProperty("PrivateKeyPassword").toCharArray());
		
	}
	
	public KeyStore getKeyStore(){
		return keyStoreManager.getKeyStore();
	}
	
	public String getString(String key){
		return properties.getProperty(key);
	}
	
	public boolean getBoolean(String key){
		return Boolean.parseBoolean(properties.getProperty(key));
		
	}

	public IdentityProviderKeyStoreManager getKeyStoreManager() {
		return keyStoreManager;
	}

}
