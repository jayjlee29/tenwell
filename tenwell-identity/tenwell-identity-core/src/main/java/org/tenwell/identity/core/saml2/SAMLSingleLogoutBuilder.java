package org.tenwell.identity.core.saml2;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.SessionIndex;
import org.opensaml.saml2.core.impl.LogoutRequestBuilder;
import org.opensaml.saml2.core.impl.LogoutResponseBuilder;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml2.core.impl.SessionIndexBuilder;
import org.opensaml.xml.security.x509.X509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tenwell.identity.core.IdentityProviderConfig;
import org.tenwell.identity.core.dto.SAMLReqValidationResponseVO;
import org.tenwell.identity.core.dto.SAMLSingleLogoutRequestVO;
import org.tenwell.identity.core.exception.IdentityException;
import org.tenwell.identity.core.saml2.cert.SignKeyDataHolder;

public class SAMLSingleLogoutBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(SAMLSingleLogoutBuilder.class);
	
	static public SAMLSingleLogoutRequestVO buildLogoutRequest(IdentityProviderConfig config, String subject, String sessionId, String reason, String destination,
            												String nameIDFormat, String tenantDomain, String requestsigningAlgorithmUri, String requestDigestAlgoUri) throws IdentityException{
		
		SAMLSingleLogoutRequestVO logoutRequestDTO = new SAMLSingleLogoutRequestVO();
		
		try{
			
			LogoutRequest logoutReq = new LogoutRequestBuilder().buildObject();
			logoutReq.setID(SAMLSSOUtil.createID());
	
	        DateTime issueInstant = new DateTime();
	        logoutReq.setIssueInstant(issueInstant);
	        
	        logoutReq.setIssuer(SAMLSSOUtil.buildIssuer(tenantDomain));
	        logoutReq.setNotOnOrAfter(new DateTime(issueInstant.getMillis() + 5 * 60 * 1000));
	        
	        NameID nameId = new NameIDBuilder().buildObject();
	        nameId.setFormat(nameIDFormat);
	        nameId.setValue(subject);
	        logoutReq.setNameID(nameId);
	
	        SessionIndex sessionIndex = new SessionIndexBuilder().buildObject();
	        sessionIndex.setSessionIndex(sessionId);
	        logoutReq.getSessionIndexes().add(sessionIndex);
	
	        if (destination != null) {
	            logoutReq.setDestination(destination);
	        }
	
	        logoutReq.setReason(reason);

       
    		String keyAlias= config.getString("PrivateKeyAlias");
        	String passwd= config.getString("PrivateKeyPassword");
    		X509Credential cred = new SignKeyDataHolder(config.getKeyStore(), keyAlias, passwd);
            SAMLSSOUtil.setSignature(logoutReq, requestsigningAlgorithmUri, requestDigestAlgoUri, cred);
        
			logoutRequestDTO.setAssertionConsumerURL(destination);
			logoutRequestDTO.setLogoutResponse(SAMLSSOUtil.marshall(logoutReq));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new IdentityException("Build Error Logout Request Message", e);
		}
        return logoutRequestDTO;
		
	}
	
	static public SAMLReqValidationResponseVO buildLogoutResponse(IdentityProviderConfig config, String id, String status, String statMsg, String acsUrl, String destination, boolean
            isSignResponse, String tenantDomain, String responseSigningAlgorithmUri, String responseDigestAlgoUri) throws IdentityException{
		
		SAMLReqValidationResponseVO respDTO = new SAMLReqValidationResponseVO();
		try{
			LogoutResponse logoutResp = new LogoutResponseBuilder().buildObject();
	        logoutResp.setID(SAMLSSOUtil.createID());
	        logoutResp.setInResponseTo(id);
	        logoutResp.setIssuer(SAMLSSOUtil.buildIssuer(tenantDomain));
	        logoutResp.setStatus(SAMLSSOUtil.buildStatus(status, statMsg));
	        logoutResp.setIssueInstant(new DateTime());
	        logoutResp.setDestination(destination);
        	
        	if(isSignResponse && SAMLSSOConstants.StatusCodes.SUCCESS_CODE.equals(status)){
        		String keyAlias= config.getString("PrivateKeyAlias");
            	String passwd= config.getString("PrivateKeyPassword");
        		X509Credential cred = new SignKeyDataHolder(config.getKeyStore(), keyAlias, passwd);
                SAMLSSOUtil.setSignature(logoutResp, responseSigningAlgorithmUri, responseDigestAlgoUri, cred);
        	}
        	
        	respDTO.setLogOutReq(true);
        	respDTO.setAssertionConsumerURL(acsUrl);
        	respDTO.setDestination(destination);
        	respDTO.setLogoutResponse(SAMLSSOUtil.marshall(logoutResp));
        	
        } catch(Exception e){
        	throw new IdentityException("Build Error Logout Response Message in Signature", e);
        }
        
        return respDTO;
		
	}

}
