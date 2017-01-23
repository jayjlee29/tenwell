package org.tenwell.identity.core.saml2.cert;

import org.apache.xml.security.signature.XMLSignature;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.CredentialContextSet;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.x509.X509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tenwell.identity.core.exception.IdentityException;

import javax.crypto.SecretKey;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;

public class SignKeyDataHolder implements X509Credential {
	private static final Logger LOGGER = LoggerFactory.getLogger(SignKeyDataHolder.class);
    private String signatureAlgorithm = null;
    private X509Certificate[] issuerCerts = null;
    private PrivateKey issuerPK = null;
    
    /**
     * Test code(main)
     * @param args
     */
    public static void main(String[] args) {
		// TODO Auto-generated method stub
    	String keystorepath = "D:/work/workspace/wings-identity/wings-identity-server/src/main/resources/wso2carbon.jks";
    	String alias = "verisignclass3g3ca";
    	String passwd = "wso2carbon";
    	try {
    		InputStream is = new FileInputStream(keystorepath);
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(is, passwd.toCharArray());
			SignKeyDataHolder s = new SignKeyDataHolder(ks, alias, passwd);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
    
    public SignKeyDataHolder(KeyStore ks, String keyAlias, String passwd) throws Exception {
    	try {
    		
            Certificate[] certificates = ks.getCertificateChain(keyAlias);
            
            issuerPK = (PrivateKey)ks.getKey(keyAlias, passwd.toCharArray());
            issuerCerts = new X509Certificate[certificates.length];
            int i = 0;
            for (Certificate certificate : certificates) {
                issuerCerts[i++] = (X509Certificate) certificate;
            }
            signatureAlgorithm = XMLSignature.ALGO_ID_SIGNATURE_RSA;
            String pubKeyAlgo = issuerCerts[0].getPublicKey().getAlgorithm();
            if (pubKeyAlgo.equalsIgnoreCase("DSA")) {
                signatureAlgorithm = XMLSignature.ALGO_ID_SIGNATURE_DSA;
            }

        } catch (Exception e) {
            throw new IdentityException("Error while reading the key", e);
        }
    }


    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public Collection<X509CRL> getCRLs() {
        return null;
    }

    public X509Certificate getEntityCertificate() {
        return issuerCerts[0];
    }

    public Collection<X509Certificate> getEntityCertificateChain() {
        return Arrays.asList(issuerCerts);
    }

    public CredentialContextSet getCredentalContextSet() {
        // TODO Auto-generated method stub
        return null;
    }

    public Class<? extends Credential> getCredentialType() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getEntityId() {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<String> getKeyNames() {
        // TODO Auto-generated method stub
        return null;
    }

    public PrivateKey getPrivateKey() {
        return issuerPK;
    }

    public PublicKey getPublicKey() {
        return issuerCerts[0].getPublicKey();
    }

    public SecretKey getSecretKey() {
        // TODO Auto-generated method stub
        return null;
    }

    public UsageType getUsageType() {
        // TODO Auto-generated method stub
        return null;
    }
}
