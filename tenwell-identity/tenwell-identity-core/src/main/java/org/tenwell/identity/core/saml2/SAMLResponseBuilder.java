package org.tenwell.identity.core.saml2;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.xml.security.signature.XMLSignature;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml1.core.NameIdentifier;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusMessage;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml2.core.impl.AttributeBuilder;
import org.opensaml.saml2.core.impl.AttributeStatementBuilder;
import org.opensaml.saml2.core.impl.AudienceBuilder;
import org.opensaml.saml2.core.impl.AudienceRestrictionBuilder;
import org.opensaml.saml2.core.impl.AuthnContextBuilder;
import org.opensaml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml2.core.impl.AuthnStatementBuilder;
import org.opensaml.saml2.core.impl.ConditionsBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml2.core.impl.StatusBuilder;
import org.opensaml.saml2.core.impl.StatusCodeBuilder;
import org.opensaml.saml2.core.impl.StatusMessageBuilder;
import org.opensaml.saml2.core.impl.SubjectBuilder;
import org.opensaml.saml2.core.impl.SubjectConfirmationBuilder;
import org.opensaml.saml2.core.impl.SubjectConfirmationDataBuilder;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.encryption.EncryptionConstants;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.signature.SignatureConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tenwell.identity.core.IdentityProviderConfig;
import org.tenwell.identity.core.IdentityContextEventListener;
import org.tenwell.identity.core.dto.SAMLAuthnReqVO;
import org.tenwell.identity.core.dto.SAMLReqValidationResponseVO;
import org.tenwell.identity.core.exception.IdentityException;
import org.tenwell.identity.core.saml2.cert.SignKeyDataHolder;
import org.tenwell.identity.core.servlet.IdentityProviderServlet;


public class SAMLResponseBuilder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SAMLResponseBuilder.class);
	
	public static SAMLReqValidationResponseVO buildErrorResponse(IdentityProviderConfig config, SAMLAuthnReqVO authReqDTO, List<String> statusCodes, String statusMsg, String destination) throws IdentityException{
		
		if (statusCodes == null || statusCodes.isEmpty()) {
            throw new IdentityException("No Status Values");
        }
		
		SAMLReqValidationResponseVO resDTO = new SAMLReqValidationResponseVO();
		
		String IdPEntityId = config.getString("SAML2.IdPEntityId");
		
		try{
			Response response = new org.opensaml.saml2.core.impl.ResponseBuilder().buildObject();
	        response.setIssuer(SAMLSSOUtil.buildIssuer(IdPEntityId));
	        response.setID(SAMLSSOUtil.createID());
	        response.setInResponseTo(authReqDTO.getId());
	        response.setDestination(authReqDTO.getAssertionConsumerURL());
	        
	        
	        //response.setStatus(buildStatus(StatusCodes, null));
	        Status status = new StatusBuilder().buildObject();
	        StatusCode statusCode = null;
	        for (String statCode : statusCodes) {
	            statusCode = SAMLSSOUtil.buildStatusCode(statCode, statusCode);
	        }
	        status.setStatusCode(statusCode);
	        SAMLSSOUtil.buildStatusMsg(status, statusMsg);
	        response.setStatus(status);
	        
	        response.setVersion(SAMLVersion.VERSION_20);
	        DateTime issueInstant = new DateTime();
	        //DateTime notOnOrAfter = new DateTime(issueInstant.getMillis() + SAMLSSOUtil.getSAMLResponseValidityPeriod() * 60 * 1000L);
	        DateTime notOnOrAfter = new DateTime(issueInstant.getMillis() + 5 * 60 * 1000L);//5분후?
	        response.setIssueInstant(issueInstant);
	        
	        resDTO.setAssertionConsumerURL(authReqDTO.getAssertionConsumerURL());
	        resDTO.setResponse(SAMLSSOUtil.marshall(response));
	        
		} catch(Exception e) {
			throw new IdentityException("buildResponse Error..", e);
		}
		
		return resDTO;
		
	}
	
	
	public static SAMLReqValidationResponseVO buildResponse(SAMLAuthnReqVO authReqDTO, String idpEntryId, String sessionId
			, KeyStore keystore, String privateKeyAlias, String privateKeyPassword, boolean isPost) throws IdentityException{
		
		SAMLReqValidationResponseVO resDTO = new SAMLReqValidationResponseVO();
		try{
			//String IdPEntityId = config.getString("SAML2.IdPEntityId");
			
			Response response = new org.opensaml.saml2.core.impl.ResponseBuilder().buildObject();
	        response.setIssuer(SAMLSSOUtil.buildIssuer(idpEntryId));
	        response.setID(SAMLSSOUtil.createID());
	        response.setInResponseTo(authReqDTO.getId());
	        response.setDestination(authReqDTO.getAssertionConsumerURL());
	        response.setStatus(SAMLSSOUtil.buildStatus(SAMLSSOConstants.StatusCodes.SUCCESS_CODE, null));
	        response.setVersion(SAMLVersion.VERSION_20);
	        DateTime issueInstant = new DateTime();
	        DateTime notOnOrAfter = new DateTime(issueInstant.getMillis() + 5 * 60 * 1000L);
	        response.setIssueInstant(issueInstant);
	        Assertion assertion = buildAssertion(authReqDTO, idpEntryId, notOnOrAfter, sessionId, keystore, privateKeyAlias, privateKeyPassword);
	        
	        if (authReqDTO.isDoEnableEncryptedAssertion()) {

	            String tenantDoamin = authReqDTO.getTenantDomain();
	            String alias = authReqDTO.getCertAlias();
	            if (alias != null) {
	                EncryptedAssertion encryptedAssertion = SAMLSSOUtil.setEncryptedAssertion(keystore, assertion,
	                        EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256, alias, tenantDoamin);
	                response.getEncryptedAssertions().add(encryptedAssertion);
	            } else {
	            	throw new IdentityException("Encrypting Assertion is fail");
	            }
	        } else {
	            response.getAssertions().add(assertion);
	        }
	        
	        if(authReqDTO.isDoSignResponse()) {
				X509Credential cred = new SignKeyDataHolder(keystore, privateKeyAlias, privateKeyPassword);
	            SAMLSSOUtil.setSignature(response, XMLSignature.ALGO_ID_SIGNATURE_RSA, SignatureConstants.ALGO_ID_DIGEST_SHA1, cred);
	        }
	        
	        resDTO.setAssertionConsumerURL(authReqDTO.getAssertionConsumerURL());
	    	resDTO.setResponse(SAMLSSOUtil.marshall(response));
	        LOGGER.debug(resDTO.getResponse());
		} catch(Exception e){
			throw new IdentityException("buildResponse error", e);
		}
		
		return resDTO;
    }
	
	static Assertion buildAssertion(SAMLAuthnReqVO authReqDTO, String IdPEntityId, DateTime notOnOrAfter, String sessionId, KeyStore keystore, String privateKeyAlias, String privateKeyPassword) throws IdentityException {
        try {
        	
            DateTime currentTime = new DateTime();
            Assertion samlAssertion = new AssertionBuilder().buildObject();
            samlAssertion.setID(SAMLSSOUtil.createID());
            samlAssertion.setVersion(SAMLVersion.VERSION_20);
            samlAssertion.setIssuer(SAMLSSOUtil.buildIssuer(IdPEntityId));
            samlAssertion.setIssueInstant(currentTime);
            Subject subject = new SubjectBuilder().buildObject();

            NameID nameId = new NameIDBuilder().buildObject();
            nameId.setValue(authReqDTO.getSubject());
            if (authReqDTO.getNameIDFormat() != null) {
                nameId.setFormat(authReqDTO.getNameIDFormat());
            } else {
                nameId.setFormat(NameIdentifier.UNSPECIFIED);
            }

            subject.setNameID(nameId);

            SubjectConfirmation subjectConfirmation = new SubjectConfirmationBuilder()
                    .buildObject();
            subjectConfirmation.setMethod(SAMLSSOConstants.SUBJECT_CONFIRM_BEARER);
            SubjectConfirmationData scData = new SubjectConfirmationDataBuilder().buildObject();
            scData.setRecipient(authReqDTO.getAssertionConsumerURL());
            scData.setNotOnOrAfter(notOnOrAfter);
            if (!authReqDTO.isIdPInitSSOEnabled()) {
                scData.setInResponseTo(authReqDTO.getId());
            }
            subjectConfirmation.setSubjectConfirmationData(scData);
            subject.getSubjectConfirmations().add(subjectConfirmation);

            if (authReqDTO.getRequestedRecipients() != null && authReqDTO.getRequestedRecipients().length > 0) {
                for (String recipient : authReqDTO.getRequestedRecipients()) {
                    subjectConfirmation = new SubjectConfirmationBuilder()
                            .buildObject();
                    subjectConfirmation.setMethod(SAMLSSOConstants.SUBJECT_CONFIRM_BEARER);
                    scData = new SubjectConfirmationDataBuilder().buildObject();
                    scData.setRecipient(recipient);
                    scData.setNotOnOrAfter(notOnOrAfter);
                    if (!authReqDTO.isIdPInitSSOEnabled()) {
                        scData.setInResponseTo(authReqDTO.getId());
                    }
                    subjectConfirmation.setSubjectConfirmationData(scData);
                    subject.getSubjectConfirmations().add(subjectConfirmation);
                }
            }

            samlAssertion.setSubject(subject);

            AuthnStatement authStmt = new AuthnStatementBuilder().buildObject();
            authStmt.setAuthnInstant(new DateTime());

            AuthnContext authContext = new AuthnContextBuilder().buildObject();
            AuthnContextClassRef authCtxClassRef = new AuthnContextClassRefBuilder().buildObject();
            authCtxClassRef.setAuthnContextClassRef(AuthnContext.PASSWORD_AUTHN_CTX);
            authContext.setAuthnContextClassRef(authCtxClassRef);
            authStmt.setAuthnContext(authContext);
            if (authReqDTO.isDoSingleLogout()) {
                authStmt.setSessionIndex(sessionId);
            }
            samlAssertion.getAuthnStatements().add(authStmt);

            /*
                * If <AttributeConsumingServiceIndex> element is in the <AuthnRequest> and according to
                * the spec 2.0 the subject MUST be in the assertion
                */
           Map<String, Object> claims = SAMLSSOUtil.getAttributes(authReqDTO);
            if (claims != null && !claims.isEmpty()) {
                AttributeStatement attrStmt = buildAttributeStatement(claims);
                if (attrStmt != null) {
                    samlAssertion.getAttributeStatements().add(attrStmt);
                }
            }

            AudienceRestriction audienceRestriction = new AudienceRestrictionBuilder()
                    .buildObject();
            Audience issuerAudience = new AudienceBuilder().buildObject();
            issuerAudience.setAudienceURI(authReqDTO.getIssuerWithDomain());
            audienceRestriction.getAudiences().add(issuerAudience);
            if (authReqDTO.getRequestedAudiences() != null) {
                for (String requestedAudience : authReqDTO.getRequestedAudiences()) {
                    Audience audience = new AudienceBuilder().buildObject();
                    audience.setAudienceURI(requestedAudience);
                    audienceRestriction.getAudiences().add(audience);
                }
            }
            
            Conditions conditions = new ConditionsBuilder().buildObject();
            conditions.setNotBefore(currentTime);
            conditions.setNotOnOrAfter(notOnOrAfter);
            conditions.getAudienceRestrictions().add(audienceRestriction);
            samlAssertion.setConditions(conditions);

            if (authReqDTO.getDoSignAssertions()) {
				X509Credential cred = new SignKeyDataHolder(keystore, privateKeyAlias, privateKeyPassword);
	            SAMLSSOUtil.setSignature(samlAssertion, XMLSignature.ALGO_ID_SIGNATURE_RSA, SignatureConstants.ALGO_ID_DIGEST_SHA1, cred);
            }
			
            return samlAssertion;
        } catch (Exception e) {
            LOGGER.error("Error when reading claim values for generating SAML Response", e);
            throw new IdentityException("Error when reading claim values for generating SAML Response", e);
        }
    }
	
	static private AttributeStatement buildAttributeStatement(Map<String, Object> claims) throws IdentityException{
        AttributeStatement attStmt = new AttributeStatementBuilder().buildObject();
        Iterator<Map.Entry<String, Object>> iterator = claims.entrySet().iterator();
        boolean atLeastOneNotEmpty = false;
        for (int i = 0; i < claims.size(); i++) {
            Map.Entry<String, Object> claimEntry = iterator.next();
            String claimUri = claimEntry.getKey();
            Object claimValue = claimEntry.getValue();
            if (claimUri != null && !claimUri.trim().isEmpty() && claimValue != null && claimValue!=null) {
                atLeastOneNotEmpty = true;
                Attribute attribute = new AttributeBuilder().buildObject();
                attribute.setName(claimUri);
                //setting NAMEFORMAT attribute value to basic attribute profile
                attribute.setNameFormat(SAMLSSOConstants.NAME_FORMAT_BASIC);
                // look
                // https://wiki.shibboleth.net/confluence/display/OpenSAML/OSTwoUsrManJavaAnyTypes
                XSStringBuilder stringBuilder = (XSStringBuilder) Configuration.getBuilderFactory().
                        getBuilder(XSString.TYPE_NAME);
                XSString stringValue;
                
                if(claimValue instanceof String){
                	stringValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
                    stringValue.setValue((String) claimValue);
                    attribute.getAttributeValues().add(stringValue);
                } else if(claimValue instanceof ArrayList){
                	List claimList = (List)claimValue;
                	Iterator iter = claimList.iterator();
                	while(iter.hasNext()){
                		 String attValue = (String)iter.next();
                		 if (attValue != null && attValue.trim().length() > 0) {
                             stringValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
                             stringValue.setValue(attValue);
                             attribute.getAttributeValues().add(stringValue);
                         }
                	}
                } else {
                	stringValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
                    stringValue.setValue(claimValue.toString());
                    attribute.getAttributeValues().add(stringValue);
                }
                attStmt.getAttributes().add(attribute);
            }
        }
        if (atLeastOneNotEmpty) {
            return attStmt;
        } else {
            return null;
        }
    }
	
}
