package org.tenwell.identity.core.saml2;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusMessage;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.StatusBuilder;
import org.opensaml.saml2.core.impl.StatusCodeBuilder;
import org.opensaml.saml2.core.impl.StatusMessageBuilder;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.signature.SignableXMLObject;
import org.opensaml.xml.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tenwell.identity.core.IdentityProviderKeyStoreManager;
import org.tenwell.identity.core.dto.SAMLAuthnReqVO;
import org.tenwell.identity.core.dto.SAMLSessionVO;
import org.tenwell.identity.core.exception.IdentityException;
import org.tenwell.identity.core.saml2.cert.X509CredentialImpl;
import org.tenwell.identity.core.saml2.encryption.SAMLEncrypter;
import org.tenwell.identity.core.util.UUIDUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class SAMLSSOUtil {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SAMLSSOUtil.class);
	
	private static final char[] charMapping = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
	            'k', 'l', 'm', 'n', 'o', 'p'};
	private static final Set<Character> UNRESERVED_CHARACTERS = new HashSet<>();
	 
	private static boolean isBootStrapped = false;
	private static Random random = new Random();
	private static final int ENTITY_EXPANSION_LIMIT = 0;

	private static final String SECURITY_MANAGER_PROPERTY = Constants.XERCES_PROPERTY_PREFIX +Constants.SECURITY_MANAGER_PROPERTY;
	
	static {
        for (char c = 'a'; c <= 'z'; c++)
            UNRESERVED_CHARACTERS.add(Character.valueOf(c));

        for (char c = 'A'; c <= 'A'; c++)
            UNRESERVED_CHARACTERS.add(Character.valueOf(c));

        for (char c = '0'; c <= '9'; c++)
            UNRESERVED_CHARACTERS.add(Character.valueOf(c));

        UNRESERVED_CHARACTERS.add(Character.valueOf('-'));
        UNRESERVED_CHARACTERS.add(Character.valueOf('.'));
        UNRESERVED_CHARACTERS.add(Character.valueOf('_'));
        UNRESERVED_CHARACTERS.add(Character.valueOf('~'));
    }
	
	
	/**
     * Constructing the AuthnRequest Object from a String
     *
     * @param authReqStr Decoded AuthReq String
     * @return AuthnRequest Object
     * @throws org.wso2.carbon.identity.base.IdentityException
     */
    public static XMLObject unmarshall(String authReqStr) throws IdentityException {
        InputStream inputStream = null;
        try {
            doBootstrap();
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);

            documentBuilderFactory.setExpandEntityReferences(false);
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            SecurityManager securityManager = new SecurityManager();
            securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
            documentBuilderFactory.setAttribute(SECURITY_MANAGER_PROPERTY, securityManager);

            DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
            docBuilder.setEntityResolver(new SAMLEntityResolver());
            inputStream = new ByteArrayInputStream(authReqStr.trim().getBytes(StandardCharsets.UTF_8));
            Document document = docBuilder.parse(inputStream);
            Element element = document.getDocumentElement();
            UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
            return unmarshaller.unmarshall(element);
        } catch (Exception e) {
            LOGGER.error("Error in constructing AuthRequest from the encoded String", e);
            throw new IdentityException("Error in constructing AuthRequest from the encoded String ", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error("Error while closing the stream", e);
                }
            }
        }
    }

    /**
     * Serialize the Auth. Request
     *
     * @param xmlObject
     * @return serialized auth. req
     */
    public static String marshall(XMLObject xmlObject) throws IdentityException {

        ByteArrayOutputStream byteArrayOutputStrm = null;
        try {
            doBootstrap();
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                    "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");

            MarshallerFactory marshallerFactory = org.opensaml.xml.Configuration.getMarshallerFactory();
            Marshaller marshaller = marshallerFactory.getMarshaller(xmlObject);
            Element element = marshaller.marshall(xmlObject);

            byteArrayOutputStrm = new ByteArrayOutputStream();
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            LSSerializer writer = impl.createLSSerializer();
            LSOutput output = impl.createLSOutput();
            output.setByteStream(byteArrayOutputStrm);
            writer.write(element, output);
            return byteArrayOutputStrm.toString("UTF-8");
        } catch (Exception e) {
            LOGGER.error("Error Serializing the SAML Response");
            throw new IdentityException("Error Serializing the SAML Response", e);
        } finally {
            if (byteArrayOutputStrm != null) {
                try {
                    byteArrayOutputStrm.close();
                } catch (IOException e) {
                    LOGGER.error("Error while closing the stream", e);
                }
            }
        }
    }

    /**
     * Encoding the response
     *
     * @param xmlString String to be encoded
     * @return encoded String
     */
    public static String encode(String xmlString) {
        // Encoding the message
        String encodedRequestMessage =
                Base64.encodeBytes(xmlString.getBytes(StandardCharsets.UTF_8),
                        Base64.DONT_BREAK_LINES);
        return encodedRequestMessage.trim();
    }

    /**
     * Decoding and deflating the encoded AuthReq
     *
     * @param encodedStr encoded AuthReq
     * @return decoded AuthReq
     */
    public static String decode(String encodedStr) throws IdentityException {
        try {
            org.apache.commons.codec.binary.Base64 base64Decoder = new org.apache.commons.codec.binary.Base64();
            byte[] xmlBytes = encodedStr.getBytes("UTF-8");
            byte[] base64DecodedByteArray = base64Decoder.decode(xmlBytes);

            try {
                Inflater inflater = new Inflater(true);
                inflater.setInput(base64DecodedByteArray);
                byte[] xmlMessageBytes = new byte[5000];
                int resultLength = inflater.inflate(xmlMessageBytes);

                if (!inflater.finished() ){
                    throw new RuntimeException("End of the compressed data stream has NOT been reached");
                }

                inflater.end();
                String decodedString = new String(xmlMessageBytes, 0, resultLength, "UTF-8");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Request message " + decodedString);
                }
                return decodedString;

            } catch (DataFormatException e) {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(base64DecodedByteArray);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                InflaterInputStream iis = new InflaterInputStream(byteArrayInputStream);
                byte[] buf = new byte[1024];
                int count = iis.read(buf);
                while (count != -1) {
                    byteArrayOutputStream.write(buf, 0, count);
                    count = iis.read(buf);
                }
                iis.close();
                String decodedStr = new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Request message " + decodedStr, e);
                }
                return decodedStr;
            }
        } catch (IOException e) {
            throw new IdentityException("Error when decoding the SAML Request.", e);
        }

    }


    public static String decodeForPost(String encodedStr) throws IdentityException {
        try {
            org.apache.commons.codec.binary.Base64 base64Decoder = new org.apache.commons.codec.binary.Base64();
            byte[] xmlBytes = encodedStr.getBytes("UTF-8");
            byte[] base64DecodedByteArray = base64Decoder.decode(xmlBytes);

            String decodedString = new String(base64DecodedByteArray, "UTF-8");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Request message " + decodedString);
            }
            return decodedString;

        } catch (IOException e) {
            throw new IdentityException(
                    "Error when decoding the SAML Request.", e);
        }

    }
    

    public static void doBootstrap() {
        if (!isBootStrapped) {
            try {
                DefaultBootstrap.bootstrap();
                isBootStrapped = true;
            } catch (ConfigurationException e) {
                LOGGER.error("Error in bootstrapping the OpenSAML2 library", e);
            }
        }
    }

    /**
     * Sign the SAML Assertion
     *
     * @param response
     * @param signatureAlgorithm
     * @param digestAlgorithm
     * @param cred
     * @return
     * @throws IdentityException
     */
    public static Assertion setSignature(Assertion response, String signatureAlgorithm, String digestAlgorithm,
                                         X509Credential cred) throws IdentityException {

        return (Assertion) doSetSignature(response, signatureAlgorithm, digestAlgorithm, cred);
    }

    /**
     * Sign the SAML Response message
     *
     * @param response
     * @param signatureAlgorithm
     * @param digestAlgorithm
     * @param cred
     * @return
     * @throws IdentityException
     */
    public static Response setSignature(Response response, String signatureAlgorithm, String digestAlgorithm,
                                        X509Credential cred) throws IdentityException {

        return (Response) doSetSignature(response, signatureAlgorithm, digestAlgorithm, cred);
    }

    /**
     * Sign the SAML LogoutResponse message
     *
     * @param response
     * @param signatureAlgorithm
     * @param digestAlgorithm
     * @param cred
     * @return
     * @throws WingsIdentityException
     */
    public static LogoutResponse setSignature(LogoutResponse response, String signatureAlgorithm, String
            digestAlgorithm, X509Credential cred) throws IdentityException {

        return (LogoutResponse) doSetSignature(response, signatureAlgorithm, digestAlgorithm, cred);
    }
    

    /**
     *  Sign SAML Logout Request message
     *
     * @param request
     * @param signatureAlgorithm
     * @param digestAlgorithm
     * @param cred
     * @return
     * @throws WingsIdentityException
     */
    public static LogoutRequest setSignature(LogoutRequest request, String signatureAlgorithm, String
            digestAlgorithm, X509Credential cred) throws IdentityException {

        return (LogoutRequest) doSetSignature(request, signatureAlgorithm, digestAlgorithm, cred);
    }
    
    
    /**
     * Generic method to sign SAML Logout Request
     *
     * @param request
     * @param signatureAlgorithm
     * @param digestAlgorithm
     * @param cred
     * @return
     * @throws IdentityException
     */
    private static SignableXMLObject doSetSignature(SignableXMLObject request, String signatureAlgorithm, String
            digestAlgorithm, X509Credential cred) throws IdentityException {
    		
        try {
        	SAMLSSOSigner ssoSigner = new SAMLSSOSigner();
            return ssoSigner.setSignature(request, signatureAlgorithm, digestAlgorithm, cred);

        } catch (Exception e) {
            throw new IdentityException("Error while signing the XML object.", e);
        }
    }

    
    public static EncryptedAssertion setEncryptedAssertion(KeyStore keystore, Assertion assertion, String encryptionAlgorithm, 
                                                           String alias, String domainName) throws IdentityException {
        doBootstrap();
        
        try {
            X509Credential cred = SAMLSSOUtil.getX509CredentialImpl(keystore, alias);
            return SAMLEncrypter.doEncryptedAssertion(assertion, cred, alias, encryptionAlgorithm);
        } catch (Exception e) {
            throw new IdentityException("Error while encrypting the SAML Assertion", e);
        }
    }

    public static String createID() {

        byte[] bytes = new byte[20]; // 160 bits
        random.nextBytes(bytes);

        char[] chars = new char[40];

        for (int i = 0; i < bytes.length; i++) {
            int left = (bytes[i] >> 4) & 0x0f;
            int right = bytes[i] & 0x0f;
            chars[i * 2] = charMapping[left];
            chars[i * 2 + 1] = charMapping[right];
        }

        return String.valueOf(chars);
    }

    /**
     * Generate the key store name from the domain name
     *
     * @param tenantDomain tenant domain name
     * @return key store file name
     */
    public static String generateKeyStoreNameFromDomainName(String tenantDomain) {
        String ksName = tenantDomain.trim().replace(".", "-");
        return ksName + ".jks";
    }
    
    
    /**
     * Generate the key store name from the domain name
     *
     * @param tenantDomain tenant domain name
     * @return key store file name
     */
    public static String getAliasFromDomainName(String tenantDomain) {
        String ksName = tenantDomain.trim().replace(".", "-");
        return ksName + ".jks";
    }
    
    /**
     * Get the X509CredentialImpl object for a particular tenant
     *
     * @param tenantDomain
     * @param alias
     * @return X509CredentialImpl object containing the public certificate of
     * that tenant
     * @throws org.tenwell.identity.core.exception.IdentityException.carbon.identity.sso.saml.exception.IdentitySAML2SSOException Error when creating X509CredentialImpl object
     */
    public static X509CredentialImpl getX509CredentialImpl(KeyStore keystore, String alias)
            throws IdentityException {

        X509CredentialImpl credentialImpl = null;

        try {
            //Certificate cert = keystore.getCertificate(alias);
            java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate) keystore.getCertificate(alias);
            credentialImpl = new X509CredentialImpl(cert);

        } catch (Exception e) {
            String errorMsg = "Error Credential alias " + alias;
            throw new IdentityException(errorMsg, e);
        }
        return credentialImpl;
    }
    
    /**
     * Return a Array of Claims containing requested attributes and values
     *
     * @param authnReqDTO
     * @return Map with attributes and values
     * @throws IdentityException
     */
    public static Map<String, Object> getAttributes(SAMLAuthnReqVO authnReqDTO) throws IdentityException {

		try {
			//returnMap = BeanUtils.describe(authnReqDTO.getSessionDTO());
			return authnReqDTO.getSamlAttributes();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new IdentityException("getAttributes Error Occupied",e);
		}
    }
    
    public static String getSessionTokenId(HttpServletRequest req) {
    	String sessionTokenId = null;
    	Cookie ssoTokenIdCookie = SAMLSSOUtil.getTokenIdCookie(req);
    	
    	if (ssoTokenIdCookie != null) {
        	sessionTokenId = ssoTokenIdCookie.getValue();
        }
    	
    	if(sessionTokenId == null || "".equals(sessionTokenId.trim())){
        	HttpSession session = req.getSession(false);
        	if(session!=null 
        			&& session.getAttribute(SAMLSSOConstants.SESSION_BEAN_NAME)!=null){
        		SAMLSessionVO sessionDTO = (SAMLSessionVO)session.getAttribute(SAMLSSOConstants.SESSION_BEAN_NAME);
        		sessionTokenId = sessionDTO.getSessionId();
        	}
        		
        }
        
        if(sessionTokenId == null || "".equals(sessionTokenId.trim())){
        	sessionTokenId = UUIDUtil.generateUUID();
        	LOGGER.debug("To Generate  Session Token ID : {}", sessionTokenId);
        } else {
        	LOGGER.debug("It returns the Session Token ID that has already been generated : {}", sessionTokenId);
        }
        
        return sessionTokenId;
    }
    
    private static Cookie getTokenIdCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (StringUtils.equals(cookie.getName(), SAMLSSOConstants.SAML_SSO_TOKEN_ID)) {
                    return cookie;
                }
            }
        }
        return null;
    }
	
	/**
     * @param sessionId
     * @param req
     * @param resp
     */
    public static void storeTokenIdCookie(String sessionId, HttpServletRequest req, HttpServletResponse resp, String tenantDomain, boolean isSecure) {
        Cookie samlssoTokenIdCookie = new Cookie(SAMLSSOConstants.SAML_SSO_TOKEN_ID, sessionId);
        samlssoTokenIdCookie.setMaxAge(-1);
        samlssoTokenIdCookie.setSecure(isSecure);
        //samlssoTokenIdCookie.setHttpOnly(true); for servlet 3.0 over
        samlssoTokenIdCookie.setPath("; HttpOnly;");	//for servlet 2.5
        resp.addCookie(samlssoTokenIdCookie);
    }

    public static void removeTokenIdCookie(HttpServletRequest req, HttpServletResponse resp) {

        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (StringUtils.equals(cookie.getName(), SAMLSSOConstants.SAML_SSO_TOKEN_ID)) {
                    cookie.setMaxAge(0);
                    resp.addCookie(cookie);
                    break;
                }
            }
        }
    }
    
    
    public static Issuer buildIssuer(String issuerStr) {
	   	Issuer issuer = new IssuerBuilder().buildObject();
	   	issuer.setValue(issuerStr);
		issuer.setFormat(SAMLSSOConstants.NAME_ID_POLICY_ENTITY);
		return issuer;
   }
    
    public static Status buildStatus(String status, String statMsg) {

        Status stat = new StatusBuilder().buildObject();

        // Set the status code
        StatusCode statCode = new StatusCodeBuilder().buildObject();
        statCode.setValue(status);
        stat.setStatusCode(statCode);

        // Set the status Message
        if (statMsg != null) {
            StatusMessage statMesssage = new StatusMessageBuilder().buildObject();
            statMesssage.setMessage(statMsg);
            stat.setStatusMessage(statMesssage);
        }

        return stat;
    }
    
    public static StatusCode buildStatusCode(String parentStatusCode, StatusCode childStatusCode) throws IdentityException {

        if (parentStatusCode == null) {
            throw new IdentityException("Invalid SAML Response Status Code");
        }

        StatusCode statusCode = new StatusCodeBuilder().buildObject();
        statusCode.setValue(parentStatusCode);

        //Set the status Message
        if (childStatusCode != null) {
            statusCode.setStatusCode(childStatusCode);
            return statusCode;
        } else {
            return statusCode;
        }
    }
	
	/**
     * Set the StatusMessage for Status of Response
     *
     * @param statusMsg
     * @return
     */
    public static Status buildStatusMsg(Status status, String statusMsg) {
        if (statusMsg != null) {
            StatusMessage statusMesssage = new StatusMessageBuilder().buildObject();
            statusMesssage.setMessage(statusMsg);
            status.setStatusMessage(statusMesssage);
        }
        return status;
    }

}
