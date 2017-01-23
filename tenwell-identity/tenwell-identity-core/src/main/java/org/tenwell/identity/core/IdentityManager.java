package org.tenwell.identity.core;


import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.SessionIndex;
import org.opensaml.saml2.core.Subject;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.security.x509.X509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tenwell.identity.core.dto.SAMLAuthnReqVO;
import org.tenwell.identity.core.dto.SAMLSingleLogoutRequestVO;
import org.tenwell.identity.core.exception.IdentityException;
import org.tenwell.identity.core.saml2.SAMLSSOSigner;
import org.tenwell.identity.core.saml2.cert.X509CredentialImpl;

public class IdentityManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(IdentityManager.class);

	IdentityProviderConfig config;
	public IdentityManager(IdentityProviderConfig config){
		LOGGER.debug("WingsSAMLManager Initialized");
		
		this.config = config;
		String beanName = config.getString("auth.bean");
	}
	
	public Object validationRequest(XMLObject request, String acsUrl, String alias, boolean enableSigned) throws IdentityException{
		
		if(request instanceof AuthnRequest){
			SAMLAuthnReqVO authnReqDTO = validationAnthnRequest((AuthnRequest)request, alias, enableSigned);
			return authnReqDTO;
        } else if(request instanceof LogoutRequest){
        	SAMLSingleLogoutRequestVO logoutDTO =validationLogoutRequest((LogoutRequest)request, acsUrl, alias, enableSigned);
        	return logoutDTO;
        } else {
        	throw new IdentityException("Nonvalidated SAML2 Request!!");
        }
	}
	
	SAMLAuthnReqVO validationAnthnRequest(AuthnRequest authnReq, String spPublicAlias, boolean isSigned) throws IdentityException{
		
		if(isSigned && authnReq.getSignature() != null){
			LOGGER.debug("AuthnRequest Signature validation checking...");
			IdentityProviderKeyStoreManager ksCred = config.getKeyStoreManager();
			X509Credential credential = new X509CredentialImpl(ksCred.getCertificate(spPublicAlias));
			SAMLSSOSigner.validateXMLSignature(authnReq, credential);
		} else if(isSigned && authnReq.getSignature() == null){
			throw new IdentityException("AuthnRequest Signature is null");
		}
		
		SAMLAuthnReqVO authnReqDTO = new SAMLAuthnReqVO();
 		Issuer issuer = authnReq.getIssuer();
        Subject subject = authnReq.getSubject();
        
        if(issuer!=null){
        	authnReqDTO.setIssuer(issuer.getValue());
        }
        
        if (subject != null && subject.getNameID() != null) {
        	authnReqDTO.setSubject(subject.getNameID().getValue());
        }
       
        authnReqDTO.setAssertionConsumerURL(authnReq.getAssertionConsumerServiceURL());
        authnReqDTO.setId(authnReq.getID());
        authnReqDTO.setCertAlias(config.getString("SpCertAlias"));
        
        //get idp.properties
        authnReqDTO.setDoEnableEncryptedAssertion(config.getBoolean("SAML2.EnableAssertionEncryption"));
        authnReqDTO.setDoSignAssertions(config.getBoolean("SAML2.EnableAssertionSigning"));
        authnReqDTO.setDoSignResponse(config.getBoolean("SAML2.EnableResponseSigning"));
        authnReqDTO.setDoSingleLogout(config.getBoolean("SAML2.EnableSLO"));
        
        
		return authnReqDTO;
	}
	
	SAMLSingleLogoutRequestVO validationLogoutRequest(LogoutRequest logoutReq, String acsUrl, String spPublicAlias, boolean isSigned) throws IdentityException{
		
		SAMLSingleLogoutRequestVO logoutDTO = new SAMLSingleLogoutRequestVO();
		
		if(isSigned && logoutReq.getSignature() != null){
			LOGGER.debug("LogoutRequest Signature validation checking...");
			IdentityProviderKeyStoreManager ksCred = config.getKeyStoreManager();
			X509Credential credential = new X509CredentialImpl(ksCred.getCertificate(spPublicAlias));
			SAMLSSOSigner.validateXMLSignature(logoutReq, credential);
		} else if(isSigned && logoutReq.getSignature() == null){
			throw new IdentityException("LogoutRequest Signature is null");
		}
		
		logoutDTO.setId(logoutReq.getID());
		if(logoutReq.getIssuer()!=null){
			logoutDTO.setIssuer(logoutReq.getIssuer().getValue());
		} else{
			throw new IdentityException("Issuer is empty");
		}
		
		if(logoutReq.getSessionIndexes() !=null && logoutReq.getSessionIndexes().size() > 0){
			SessionIndex requestSessionIndex = logoutReq.getSessionIndexes().get(0);
			logoutDTO.setRpSessionId(requestSessionIndex.getSessionIndex());
		} else {
			throw new IdentityException("Session Index validation for Logout failed.");
		}
		
		logoutDTO.setAssertionConsumerURL(acsUrl);
		logoutDTO.setDestination(logoutReq.getDestination());
		return logoutDTO;
		
	}

}
