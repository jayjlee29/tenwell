package org.tenwell.identity.core.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.xml.security.signature.XMLSignature;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.signature.SignatureConstants;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tenwell.identity.core.IdentityProviderConfig;
import org.tenwell.identity.core.IdentityManager;
import org.tenwell.identity.core.dto.SAMLAuthnReqVO;
import org.tenwell.identity.core.dto.SAMLReqValidationResponseVO;
import org.tenwell.identity.core.dto.SAMLRespVO;
import org.tenwell.identity.core.dto.SAMLSessionVO;
import org.tenwell.identity.core.dto.SAMLSingleLogoutRequestVO;
import org.tenwell.identity.core.exception.IdentityException;
import org.tenwell.identity.core.saml2.SAMLLogoutRequestSender;
import org.tenwell.identity.core.saml2.SAMLResponseBuilder;
import org.tenwell.identity.core.saml2.SAMLSSOConstants;
import org.tenwell.identity.core.saml2.SAMLSSOUtil;
import org.tenwell.identity.core.saml2.SAMLSingleLogoutBuilder;
import org.tenwell.identity.core.util.StringUtil;

public class IdentityProviderServlet extends HttpServlet {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(IdentityProviderServlet.class);
	
	IdentityManager samlManager = null;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException  {
		// TODO Auto-generated method stub
		//super.service(req, resp);
		try{
			handleRequest(req, resp, req.getMethod()=="POST"?true:false);
		} catch(IdentityException e){
			LOGGER.error("IdentityProviderServlet Exception", e);
			throw new ServletException("IdentityProviderServlet Exception", e);
		} catch(Exception e){
			LOGGER.error("Unknown Exception", e);
			throw new ServletException("Unknown Exception", e);
		}
	}
	
	
	
	void handleRequest(HttpServletRequest req, HttpServletResponse resp, boolean isPost) throws IdentityException{
		LOGGER.info("Processing handleRequest in Identity Provider Service(IDP), Request URI is {}", req.getRequestURI());
		
		samlManager = new IdentityManager(IdentityProviderConfig.getConfig(req));
		
		String requestUri = req.getRequestURI();
		String samlRequest = req.getParameter("SAMLRequest");
		String acsUrl = req.getParameter("acsUrl");
		String sessionTokenId = SAMLSSOUtil.getSessionTokenId(req);
		String httpBinding = req.getParameter("SAML2.HTTPBinding");
		
		resp.setHeader("Access-Control-Allow-Origin", "*");
		
        
        /*
         * web.xml servlet에 설정된 mapping samlsso만 수행됨 
         */
		if(samlRequest==null || samlRequest.isEmpty()){
			
			SAMLSessionVO sessionDTO = getSession(req);
			
			if(sessionDTO!=null){
				String queryString = req.getQueryString();
				String loginUrl = IdentityProviderConfig.getConfig(req).getString("SAML2.LoginURL");
				if(queryString != null){
					loginUrl += "?" + queryString;
				}
				try {
					resp.sendRedirect(loginUrl);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					throw new IdentityException("handleRequest IO Exception", e);
				}
			} else {
				if(acsUrl==null){
					throw new IdentityException("The Wrong approach. Take the right path.");
				}
				
				try {
					resp.sendRedirect(acsUrl);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					throw new IdentityException("handleRequest IO Exception", e);
				}
			}
			
		} else {
			
			Object resDTO = processSAMLRequest(req, resp, sessionTokenId, isPost);
			
			if(resDTO instanceof SAMLReqValidationResponseVO){
				/*if("HTTP-Redirect".equals(httpBinding)){
					sendRedirect(req, resp, (SAMLReqValidationResponseVO)resDTO);
				} else {
					sendResponse(req, resp, (SAMLReqValidationResponseVO)resDTO);
				}*/
				
				sendResponse(req, resp, (SAMLReqValidationResponseVO)resDTO);
				
			} else if(resDTO instanceof SAMLRespVO) {
				//sendResponse(req, resp, (WingsSAMLRespDTO)resDTO);
				throw new IdentityException("WingsSAMLRespDTO Response Message ");
			} else {
				throw new IdentityException("Unknown SAML2 Response Message ");
			}
		}
	}
	
	/**
	 * 유효한 Session Bean만을 리턴한다.
	 * isLoggedIn 이 false라면 Session Bean을 null로 리턴한다.
	 * @param req
	 * @return WingsSessionDTO
	 */
	private SAMLSessionVO vaildateSession(HttpServletRequest req){
		
		LOGGER.debug("Vaildating Session Session Bean");
		HttpSession session = req.getSession(false);
		
		if(session==null || (SAMLSessionVO)session.getAttribute(SAMLSSOConstants.SESSION_BEAN_NAME) == null){
			LOGGER.debug("Session Bean(Session Attr ID is {}) is null or empty in vaildateSession Method", SAMLSSOConstants.SESSION_BEAN_NAME);
			return null;
		}
		
		SAMLSessionVO sessionDTO = (SAMLSessionVO)session.getAttribute(SAMLSSOConstants.SESSION_BEAN_NAME);
		
		if(!sessionDTO.isLoggedIn()){
			LOGGER.debug("Session bean exists but not logged in yet(return null)");
			return null;
		}
		
		return sessionDTO;
	}
	
	/**
	 * Session Bean만을 리턴한다.
	 * AbstractSessionVO.SESSION_ATTR_NAME이 있다면 무조건 리턴한다.
	 * @param req
	 * @return WingsSessionDTO
	 */
	private SAMLSessionVO getSession(HttpServletRequest req){
		
		HttpSession session = req.getSession(false);
		
		if(session==null || (SAMLSessionVO)session.getAttribute(SAMLSSOConstants.SESSION_BEAN_NAME) == null){
			LOGGER.debug("Getting Session Bean(Session Attr ID is {}) is null or empty in getSession Method", SAMLSSOConstants.SESSION_BEAN_NAME);
			return null;
		}
		
		SAMLSessionVO sessionDTO = (SAMLSessionVO)session.getAttribute(SAMLSSOConstants.SESSION_BEAN_NAME);
		
		return sessionDTO;
	}
	
	/**
	 * Session Bean을 제거한다.
	 * AbstractSessionVO.SESSION_ATTR_NAME을 제거한다.
	 * @param req
	 * @return void
	 */
	private void removeSession(HttpServletRequest req){
		
		HttpSession session = req.getSession(false);
		
		SAMLSessionVO sessionDTO = (SAMLSessionVO)session.getAttribute(SAMLSSOConstants.SESSION_BEAN_NAME);
		if(sessionDTO != null){
			LOGGER.debug("Remove Session Bean : {}", sessionDTO.toString());
		} else {
			LOGGER.debug("Remove Session Bean : Unknown Session Info");
		}
		
		session.invalidate();
		session = null;
	}
	
	private void updateSession(HttpServletRequest req, SAMLSessionVO sessionDTO){
		
		LOGGER.debug("Updating Session Bean {}", sessionDTO.toString());
		
		HttpSession session = req.getSession(false);
		if(session==null){
			session = req.getSession(true);
		} 
		
		session.setAttribute(SAMLSSOConstants.SESSION_BEAN_NAME, sessionDTO);
		
	}
	
	/**
	 * AuthnRequest를 요청시 수행
	 * SAML2 Response을 응답한다.
	 * AuthnRequest -> Response
	 * LogoutRequest -> LogoutResponse
	 * @param req
	 * @param resp
	 * @param newSessionId
	 * @param isPost
	 * @return
	 * @throws Exception 
	 * @throws WingsIdentityException
	 */
	private Object processSAMLRequest(HttpServletRequest req, HttpServletResponse resp, String sessionTokenId, boolean isPost) throws IdentityException{
		
		IdentityProviderConfig config = IdentityProviderConfig.getConfig(req);
		
		SAMLReqValidationResponseVO respDTO = new SAMLReqValidationResponseVO();
		String samlRequest = req.getParameter("SAMLRequest");
		if(samlRequest==null || "".equals(samlRequest)){
			throw new IdentityException("SAMLRequest is Emply");
		}
			
		XMLObject request = null;

        if (isPost) {
            request = SAMLSSOUtil.unmarshall(SAMLSSOUtil.decodeForPost(samlRequest));
        } else {
            request = SAMLSSOUtil.unmarshall(SAMLSSOUtil.decode(samlRequest));
        }
        
        String IdPEntityId = config.getString("SAML2.IdPEntityId");
		//1. AuthnRequest 인증
        //String IdPPublicCertAlias = config.getString("IdPPublicCertAlias");
        boolean enableRequestSigning = config.getBoolean("SAML2.EnableRequestSigning");
        String spCertAlias = config.getString("SpCertAlias");
        Object reqDTO = samlManager.validationRequest(request, req.getParameter("acsUrl"), spCertAlias, enableRequestSigning);
        
        if(reqDTO instanceof SAMLAuthnReqVO){
        	
        	SAMLAuthnReqVO authnReqDTO = (SAMLAuthnReqVO)reqDTO;
        	
        	//2. AuthnRequest 인중이 성공하면 sessionID를 쿠키에 넣고 Response를 생성한다.
    		if(authnReqDTO!=null){
    			
    			SAMLSessionVO sessionDTO = vaildateSession(req);
    			
    			if(sessionTokenId!=null 
    					&& sessionDTO!=null && sessionDTO.isLoggedIn()){
    				LOGGER.debug("Authenticated Session User : {}, Session Token : {}", sessionDTO.getUserId(), sessionTokenId);
    				
    				sessionDTO.addAssertionConsumerServiceURL(authnReqDTO.getIssuer(), authnReqDTO.getAssertionConsumerURL());
    				sessionDTO.setSessionId(sessionTokenId);
    				
    				authnReqDTO.setSubject(sessionDTO.getUserId());
    				authnReqDTO.setSamlAttributes(sessionDTO.getAttributes());
    				
    				updateSession(req, sessionDTO);
    				
    				respDTO = SAMLResponseBuilder.buildResponse(authnReqDTO, IdPEntityId, sessionTokenId, config.getKeyStore(), config.getString("PrivateKeyAlias"), config.getString("PrivateKeyPassword"), isPost);
    				
    			} else {
    				LOGGER.debug("Unauthenticated Session Token {}", sessionTokenId);
        			List<String> statusCodes = new ArrayList<String>();
                    statusCodes.add(SAMLSSOConstants.StatusCodes.NO_PASSIVE);
                    statusCodes.add(SAMLSSOConstants.StatusCodes.IDENTITY_PROVIDER_ERROR);
                    respDTO = SAMLResponseBuilder.buildErrorResponse(config, authnReqDTO, statusCodes, "Cannot authenticate Subject in Passive Mode", authnReqDTO.getAssertionConsumerURL());
                    
                    sessionDTO = new SAMLSessionVO();
                    sessionDTO.addAssertionConsumerServiceURL(authnReqDTO.getIssuer(), authnReqDTO.getAssertionConsumerURL());
    				sessionDTO.setSessionId(sessionTokenId);
    				updateSession(req, sessionDTO);
    			}
    			
    			SAMLSSOUtil.storeTokenIdCookie(sessionTokenId, req, resp, authnReqDTO.getTenantDomain(), config.getBoolean("ssl.enable"));
    			
    			return respDTO;
    			
    		}  else {
    			LOGGER.debug("Invaild Saml2 Request");
    		}
        } else if(reqDTO instanceof SAMLSingleLogoutRequestVO){
        	SAMLSingleLogoutRequestVO singleLogoutRequestDTO = (SAMLSingleLogoutRequestVO)reqDTO;
        	SAMLSessionVO sessionDTO = vaildateSession(req);
        	String acsUrl = req.getParameter("acsUrl");
        	if(sessionDTO==null){
        		respDTO = SAMLSingleLogoutBuilder.buildLogoutResponse(config, singleLogoutRequestDTO.getId(), SAMLSSOConstants.StatusCodes.IDENTITY_PROVIDER_ERROR, "This session has already been removed."
        				, singleLogoutRequestDTO.getAssertionConsumerURL(), singleLogoutRequestDTO.getAssertionConsumerURL(), config.getBoolean("SAML2.EnableResponseSigning"), singleLogoutRequestDTO.getCompany()
        				, XMLSignature.ALGO_ID_SIGNATURE_RSA, SignatureConstants.ALGO_ID_DIGEST_SHA1);
            	LOGGER.debug("This session has already been removed.");
        		
        	} else {
        		LOGGER.debug("Current Session User {} is Token {}", sessionDTO.getUserId(), sessionDTO.getSessionId());
            	LOGGER.debug("Service Provider Session Token Id {}", singleLogoutRequestDTO.getRpSessionId());
            	
            	Set<String> issuers = sessionDTO.getIssuers();
            	Iterator iter = issuers.iterator();
            	
            	ArrayList<SAMLSingleLogoutRequestVO> logoutReqDTOs = new ArrayList<SAMLSingleLogoutRequestVO>();
            	while(iter.hasNext()){
            		String issuer = (String)iter.next();
            		String ACSUrl = sessionDTO.getAssertionConsumerServiceURL(issuer);
            		LOGGER.debug("For Logout ACSUrl {} {}", issuer, ACSUrl);
            		
            		try {
            			
            			SAMLSingleLogoutRequestVO logoutRequestDTO = SAMLSingleLogoutBuilder.buildLogoutRequest(config, "", sessionTokenId, singleLogoutRequestDTO.getReason()
                				, ACSUrl, sessionDTO.getDomain(), issuer, XMLSignature.ALGO_ID_SIGNATURE_RSA, SignatureConstants.ALGO_ID_DIGEST_SHA1);
            			logoutReqDTOs.add(logoutRequestDTO);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						throw new IdentityException("Logouot Request Error", e);
					}
            	}
            	
            	SAMLLogoutRequestSender sender = SAMLLogoutRequestSender.getInstance();
            	sender.sendLogoutRequests(logoutReqDTOs.toArray(new SAMLSingleLogoutRequestVO[logoutReqDTOs.size()]));
            	
            	respDTO = SAMLSingleLogoutBuilder.buildLogoutResponse(config, singleLogoutRequestDTO.getId(), SAMLSSOConstants.StatusCodes.SUCCESS_CODE, "Logout Success", acsUrl, singleLogoutRequestDTO.getDestination()
            			, config.getBoolean("SAML2.EnableResponseSigning"), singleLogoutRequestDTO.getCompany(), XMLSignature.ALGO_ID_SIGNATURE_RSA, SignatureConstants.ALGO_ID_DIGEST_SHA1);
            	
            	LOGGER.debug("Remove Session of Identity Provider Service");
            	removeSession(req);
        	}
        	
        } else {
        	throw new IdentityException("processSAMLRequest fail");
        }
		
		return respDTO;
		
	}
	
    
    
    private static void sendResponse(HttpServletRequest req, HttpServletResponse resp, SAMLReqValidationResponseVO responseDTO)
            throws IdentityException {

        String acsUrl = null;
        String response = null;

        if(responseDTO.isLogOutReq()){
        	LOGGER.debug("This Response is Single Logout Message {}", responseDTO.isLogOutReq());
        	response = SAMLSSOUtil.encode(responseDTO.getLogoutResponse());
        	acsUrl = responseDTO.getAssertionConsumerURL();
        } else {
        	response = SAMLSSOUtil.encode(responseDTO.getResponse());
        	acsUrl = responseDTO.getAssertionConsumerURL();
        }

        if (acsUrl == null || acsUrl.trim().length() == 0) {
            // if ACS is null. Send to error page
            throw new IdentityException("Assertion Consumer Service URL(ACS URL) is Null");
        }

        if (response == null || response.trim().length() == 0) {
            // if response is null
            throw new IdentityException("Response message is Null");
        }
        
        String requestURL = StringUtil.nvl(req.getParameter("requestURL"));
        String queryString = StringUtil.nvl(req.getParameter("queryString"));
        
        resp.setContentType("text/html");
        resp.setCharacterEncoding("utf-8");
        
        PrintWriter out;
		try {
			out = resp.getWriter();
			out.println("<html>");
			out.println("<header>");
			out.println("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
			out.println("<link rel='stylesheet' type='text/css' href='/css/default.css'>");
			out.println("</header>");
            out.println("<body>");
            out.println("<p></p>");
            out.println("<div class='loading'>");
            out.println("<div class='loading-content'>");
            out.println("<form method='post' action='" + Encode.forHtmlAttribute(acsUrl) + "'>");
            out.println("<p class='loading-txt'>You are now redirected back to " + Encode.forHtmlContent(acsUrl));
            out.println(" If the redirection fails, please click the post button.</p>");
            out.println("<input type='hidden' name='SAMLResponse' value='" + Encode.forHtmlAttribute(response) + "'>");
            /*if(relayState != null) {
                out.println("<input type='hidden' name='RelayState' value='" + Encode.forHtmlAttribute(relayState) + "'>");
            }

            if (authenticatedIdPs != null && !authenticatedIdPs.isEmpty()) {
                out.println("<input type='hidden' name='AuthenticatedIdPs' value='" +
                        Encode.forHtmlAttribute(authenticatedIdPs) + "'>");
            }*/
            
            if (!requestURL.isEmpty()) {
                out.println("<input type='hidden' name='requestURL' value='" + requestURL + "'>");
            }
            
            if (!queryString.isEmpty()) {
                out.println("<input type='hidden' name='queryString' value='" + queryString + "'>");
            }
            
            out.println("<button type='submit' class='load-submit'>POST</button>");
            out.println("</form>");
            out.println("<script type='text/javascript'>");
            out.println("document.forms[0].submit();");
            out.println("</script>");
            out.println("</div>");
            out.println("</div>");
            out.println("<span class='load-bg'></span>");
            out.println("</body>");
            out.println("</html>");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new IdentityException("sendResponse IO Exception", e);
		}
            
        
        LOGGER.debug("Send Post Response {}", acsUrl);
    }
    
    static void sendRedirect(HttpServletRequest req, HttpServletResponse resp, SAMLReqValidationResponseVO resDTO){
        
    	IdentityProviderConfig config;
		try {
			config = IdentityProviderConfig.getConfig(req);
			
			String acsUrl = null;
	        String response = null;

	        if(resDTO.isLogOutReq()){
	        	LOGGER.debug("This Response is Single Logout Message {}", resDTO.isLogOutReq());
	        	response = SAMLSSOUtil.encode(resDTO.getLogoutResponse());
	        	acsUrl = resDTO.getAssertionConsumerURL();
	        } else {
	        	response = SAMLSSOUtil.encode(resDTO.getResponse());
	        	acsUrl = resDTO.getAssertionConsumerURL();
	        }

	        if (acsUrl == null || acsUrl.trim().length() == 0) {
	            // if ACS is null. Send to error page
	            throw new IdentityException("Assertion Consumer Service URL(ACS URL) is Null");
	        }

	        if (response == null || response.trim().length() == 0) {
	            // if response is null
	            throw new IdentityException("Response message is Null");
	        }
	        
			
			String requestURL = StringUtil.nvl(req.getParameter("requestURL"));
		    String queryString = StringUtil.nvl(req.getParameter("queryString"));
		    
		    String redirectUrl = acsUrl;
		    redirectUrl += "?SAMLResponse=" + URLEncoder.encode(response, "UTF-8");
		    redirectUrl += "&requestUrl=" + requestURL;
		    redirectUrl += "&queryString=" + queryString;
        
			resp.sendRedirect(redirectUrl);
			
			 LOGGER.debug("Send Redirect {}", redirectUrl);
		} catch (IdentityException e) {
			// TODO Auto-generated catch block
			LOGGER.error("sendRedirect Method Error", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Redirect error", e);
		}

        
    }
}
