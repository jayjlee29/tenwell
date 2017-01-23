package org.tenwell.identity.core.saml2.encryption;

import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.encryption.Encrypter;
import org.opensaml.xml.encryption.EncryptionConstants;
import org.opensaml.xml.encryption.EncryptionParameters;
import org.opensaml.xml.encryption.KeyEncryptionParameters;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.x509.X509Credential;
import org.tenwell.identity.core.exception.IdentityException;


/**
  * @FileName : SAMLEncrypter.java
  * @Project : wings-identity-core
  * @Date : 2016. 12. 26. 
  * @Author : jglee
  * @History :
  * @Description : SAML Assertion을 암호화한다.
  */
public class SAMLEncrypter {


    /**
      * @Description : SAML2 Assertion을 AES암호화 한다.
      * @Method : doEncryptedAssertion
      * @param assertion
      * @param cred
      * @param alias SP Cert Public Key Alias 
      * @param encryptionAlgorithm
      * @return 
      * @throws Exception  
      */
    static public EncryptedAssertion doEncryptedAssertion(Assertion assertion, X509Credential cred, String alias, String encryptionAlgorithm) throws Exception{
    	
    	try {

            Credential symmetricCredential = SecurityHelper.getSimpleCredential(
                    SecurityHelper.generateSymmetricKey(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256));

            EncryptionParameters encParams = new EncryptionParameters();
            encParams.setAlgorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256);
            encParams.setEncryptionCredential(symmetricCredential);

            KeyEncryptionParameters keyEncryptionParameters = new KeyEncryptionParameters();
            keyEncryptionParameters.setAlgorithm(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSA15);
            keyEncryptionParameters.setEncryptionCredential(cred);

            Encrypter encrypter = new Encrypter(encParams, keyEncryptionParameters);
            encrypter.setKeyPlacement(Encrypter.KeyPlacement.INLINE);

            EncryptedAssertion encrypted = encrypter.encrypt(assertion);
            return encrypted;
        } catch (Exception e) {
            throw new IdentityException("Error while Encrypting Assertion", e);
        }
    	
    }
}
