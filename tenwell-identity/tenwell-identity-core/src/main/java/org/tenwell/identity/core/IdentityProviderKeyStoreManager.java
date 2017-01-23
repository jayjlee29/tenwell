package org.tenwell.identity.core;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tenwell.identity.core.exception.IdentityException;


public class IdentityProviderKeyStoreManager {
	private static final Log LOGGER = LogFactory.getLog(IdentityProviderKeyStoreManager.class);
    private PublicKey publicKey = null;
    private PrivateKey privateKey = null;
    private X509Certificate entityCertificate = null;
    private KeyStore keyStore = null;

    public IdentityProviderKeyStoreManager(KeyStore keyStore, String publicCertAlias,
                                          String privateKeyAlias, char[] privateKeyPassword) throws IdentityException {

        readX509Credentials(keyStore, publicCertAlias, privateKeyAlias, privateKeyPassword);
    }

    public IdentityProviderKeyStoreManager(InputStream keyStoreInputStream, char[] keyStorePassword,
                                          String publicCertAlias, String privateKeyAlias,
                                          char[] privateKeyPassword) throws IdentityException {

        readX509Credentials(keyStoreInputStream, keyStorePassword, publicCertAlias,
                privateKeyAlias, privateKeyPassword);
    }

    public PublicKey getPublicKey() throws IdentityException {
        return publicKey;
    }

    public PrivateKey getPrivateKey() throws IdentityException {
        return privateKey;
    }

    public X509Certificate getEntityCertificate() throws IdentityException {
        return entityCertificate;
    }

    public KeyStore getKeyStore() {
		return keyStore;
	}
    
    public X509Certificate getCertificate(String alias) throws IdentityException{
    	X509Certificate cert = null;
    	try {
			cert = (X509Certificate)keyStore.getCertificate(alias);
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			LOGGER.error("error while getting certificate : " + alias, e);
			throw new IdentityException("error while getting certificate : " + alias);
		}
    	return cert;
    }

	protected void readX509Credentials(KeyStore keyStore, String publicCertAlias,
                                       String privateKeyAlias, char[] privateKeyPassword)
            throws IdentityException {
    	
    	if(keyStore==null){
    		 throw new IdentityException("Identity Server KeyStore is null");
    	}

    	this.keyStore = keyStore;
    	
        try {
            entityCertificate = (X509Certificate) keyStore.getCertificate(publicCertAlias);
        } catch (KeyStoreException e) {
            throw new IdentityException(
                    "Error occurred while retrieving public certificate for alias " +
                            publicCertAlias, e);
        }
        publicKey = entityCertificate.getPublicKey();
        try {
            privateKey = (PrivateKey) keyStore.getKey(privateKeyAlias, privateKeyPassword);
            
        } catch (KeyStoreException e) {
            throw new IdentityException(
                    "Error occurred while retrieving private key for alias " +
                            privateKeyAlias, e);
        } catch (NoSuchAlgorithmException e) {
            throw new IdentityException(
                    "Error occurred while retrieving private key for alias " +
                            privateKeyAlias, e);
        } catch (UnrecoverableKeyException e) {
            throw new IdentityException(
                    "Error occurred while retrieving private key for alias " +
                            privateKeyAlias, e);
        }
    }

    protected void readX509Credentials(InputStream keyStoreInputStream, char[] keyStorePassword,
                                       String publicCertAlias, String privateKeyAlias,
                                       char[] privateKeyPassword)
            throws IdentityException {

        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(keyStoreInputStream, keyStorePassword);
            readX509Credentials(keyStore, publicCertAlias, privateKeyAlias, privateKeyPassword);
        } catch (Exception e) {
            throw new IdentityException("Error while loading key store file", e);
        } finally {
            if (keyStoreInputStream != null) {
                try {
                    keyStoreInputStream.close();
                } catch (IOException ignored) {
                	LOGGER.error("Ignoring IO Exception : ", ignored);
                    throw new IdentityException("Error while closing input stream of key store");
                }
            }
        }
    }
}
