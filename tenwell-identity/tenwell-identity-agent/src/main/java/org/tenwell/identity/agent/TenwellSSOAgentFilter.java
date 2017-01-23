/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.tenwell.identity.agent;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Properties;

public class TenwellSSOAgentFilter extends SSOAgentFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenwellSSOAgentFilter.class);

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String CHARACTER_ENCODING = "UTF-8";
    private static Properties properties;
    protected FilterConfig filterConfig = null;

    static{
        properties = TenwellSSOContextEventListener.getProperties();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }
    
    static void printCookie(HttpServletRequest request){
		
		Cookie[] cookie = request.getCookies();
		for(int i=0;cookie!=null && i<cookie.length;i++){
			
			Cookie c = cookie[i];
			LOGGER.debug("########### Request Cookie [" +i+ "] ###############");
			LOGGER.debug("Request Cookie Domain : " + c.getDomain());
			LOGGER.debug("Request Cookie Path : " + c.getPath());
			LOGGER.debug("Request Cookie Name : " + c.getName());
			LOGGER.debug("Request Cookie Value : " + c.getValue());
			LOGGER.debug("Request Cookie MaxAge : " + c.getMaxAge());
			LOGGER.debug("Request Cookie Version : " + c.getVersion());
			LOGGER.debug("Request Cookie Comment : " + c.getComment());
			LOGGER.debug("########### end Request Cookie ###############");
			
		}
	}
    
    static boolean isAjax(HttpServletRequest request){
    	
		String requestURI = request.getRequestURI();
		String requestUrl = request.getServletPath();
		String xhrFlag = request.getHeader("X-Requested-With");
		if(xhrFlag==null){
			return false;
		}
		xhrFlag = xhrFlag.toUpperCase();
		
		/*if(requestURI!=null 
				&& requestURI.contains("/biz/")
				&& "XMLHTTPREQUEST".equals(xhrFlag)){*/
		if("XMLHTTPREQUEST".equals(xhrFlag)){
			/*
			 * view 호출
			 * ajax 호출이 아님
			 */
			//property 선택 필요
			LOGGER.debug(String.format("requestUrl %s requestURI %s 는 view 입니다.", requestUrl, requestURI));
			return true;

		}
    	
    	return false;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException,  ServletException {
    	
    	((HttpServletResponse)servletResponse).setHeader("Access-Control-Allow-Origin", "*");
    	
        String httpBinding = servletRequest.getParameter(SSOAgentConstants.SSOAgentConfig.SAML2.HTTP_BINDING);
        if(httpBinding != null && !httpBinding.isEmpty()){
            if("HTTP-POST".equals(httpBinding)){
                httpBinding = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
            } else if ("HTTP-Redirect".equals(httpBinding)) {
                httpBinding = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect";
            } else {
                LOGGER.info("Unknown SAML2 HTTP Binding. Defaulting to HTTP-POST");
                httpBinding = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
            }
        } else {
        	LOGGER.info("SAML2 HTTP Binding not found in request. Defaulting to HTTP-POST");
            httpBinding = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
        }
        SSOAgentConfig config = (SSOAgentConfig)filterConfig.getServletContext().
                getAttribute(SSOAgentConstants.CONFIG_BEAN_NAME);
        config.getSAML2().setHttpBinding(httpBinding);

        if (StringUtils.isNotEmpty(servletRequest.getParameter(USERNAME)) &&
                StringUtils.isNotEmpty(servletRequest.getParameter(PASSWORD))) {

            String authorization = servletRequest.getParameter(USERNAME) + ":" + servletRequest.getParameter(PASSWORD);
            // Base64 encoded username:password value
            authorization = new String(Base64.encode(authorization.getBytes(CHARACTER_ENCODING)));
            String htmlPayload = "<html>\n" +
                    "<body>\n" +
                    "<p>You are now redirected back to " + properties.getProperty("SAML2.IdPURL") + " \n" +
                    "If the redirection fails, please click the post button.</p>\n" +
                    "<form method='post' action='" +  properties.getProperty("SAML2.IdPURL") + "'>\n" +
                    "<input type='hidden' name='sectoken' value='" + authorization + "'/>\n" +
                    "<p>\n" +
                    "<!--$saml_params-->\n" +
                    "<button type='submit'>POST</button>\n" +
                    "</p>\n" +
                    "</form>\n" +
                    "<script type='text/javascript'>\n" +
                    "document.forms[0].submit();\n" +
                    "</script>\n" +
                    "</body>\n" +
                    "</html>";
            config.getSAML2().setPostBindingRequestHTMLPayload(htmlPayload);
        } else {
            // Reset previously sent HTML payload
            config.getSAML2().setPostBindingRequestHTMLPayload(null);
        } 
        servletRequest.setAttribute(SSOAgentConstants.CONFIG_BEAN_NAME,config);
        super.doFilter(servletRequest, servletResponse, filterChain);
    }

    @Override
    public void destroy() {

    }
}
