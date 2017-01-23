/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *
 */

package org.tenwell.identity.agent;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tenwell.identity.agent.exception.SSOAgentException;
import org.tenwell.identity.agent.saml.SAML2SSOManager;
import org.tenwell.identity.agent.util.SSOAgentUtils;

import java.io.IOException;
import java.util.Enumeration;

/**
 * Servlet Filter implementation class SSOAgentFilter
 */
public class SSOAgentFilter implements Filter {

	 private static final Logger LOGGER = LoggerFactory.getLogger(SSOAgentFilter.class);

    /**
     * @see Filter#init(FilterConfig)
     */
    @Override
    public void init(FilterConfig fConfig) throws ServletException {
        return;
    }

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {

        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            
            SSOAgentConfig ssoAgentConfig = (SSOAgentConfig) request.
                    getAttribute(SSOAgentConstants.CONFIG_BEAN_NAME);
            if (ssoAgentConfig == null) {
                throw new SSOAgentException("Cannot find " + SSOAgentConstants.CONFIG_BEAN_NAME +
                        " set a request attribute. Unable to proceed further");
            }

            SSOAgentRequestResolver resolver =
                    new SSOAgentRequestResolver(request, response, ssoAgentConfig);

            if (resolver.isURLToSkip()) {
            	LOGGER.debug("Skip URL {}", request.getRequestURI());
                chain.doFilter(servletRequest, servletResponse);
                return;
            }

            SAML2SSOManager samlSSOManager = null;

            if (resolver.isSLORequest()) {
            	LOGGER.debug("Received Single Log Out(SLO) Request & Response");
                samlSSOManager = new SAML2SSOManager(ssoAgentConfig);
                samlSSOManager.doSLO(request);

            } else if (resolver.isSAML2SSOResponse()) {
            	LOGGER.debug("Received SAML2 Response");
                samlSSOManager = new SAML2SSOManager(ssoAgentConfig);
                try {
                    samlSSOManager.processResponse(request, response);
                } catch (SSOAgentException e) {
                    handleException(request, e);
                }

            } else if (resolver.isSLOURL()) {
            	
            	LOGGER.debug("Sending Logout Request Message to Identity Provider Service(IDP)");
            	
                samlSSOManager = new SAML2SSOManager(ssoAgentConfig);
                if (resolver.isHttpPostBinding()) {

                    ssoAgentConfig.getSAML2().setPassiveAuthn(false);
                    String htmlPayload = samlSSOManager.buildPostRequest(request, response, true);
                    SSOAgentUtils.sendPostResponse(request, response, htmlPayload);

                } else {
                    //if "SSOAgentConstants.HTTP_BINDING_PARAM" is not defined, default to redirect
                    ssoAgentConfig.getSAML2().setPassiveAuthn(false);
                    response.sendRedirect(samlSSOManager.buildRedirectRequest(request, true));
                }
                return;

            } else if (resolver.isSAML2SSOURL()) {
            	
            	LOGGER.debug("Received SAML SSO URL");
            	
                samlSSOManager = new SAML2SSOManager(ssoAgentConfig);
                if (resolver.isHttpPostBinding()) {
                    ssoAgentConfig.getSAML2().setPassiveAuthn(false);
                    String htmlPayload = samlSSOManager.buildPostRequest(request, response, false);
                    SSOAgentUtils.sendPostResponse(request, response, htmlPayload);
                    return;
                } else {
                    ssoAgentConfig.getSAML2().setPassiveAuthn(false);
                    response.sendRedirect(samlSSOManager.buildRedirectRequest(request, false));
                }
                return;

            } else if (resolver.isPassiveAuthnRequest()) {
            	
            	LOGGER.debug("Requesting authorization to Identity Provider Service(IDP) {}", request.getRequestURL().toString());
            	
                samlSSOManager = new SAML2SSOManager(ssoAgentConfig);
                ssoAgentConfig.getSAML2().setPassiveAuthn(true);
                
                if (resolver.isHttpPostBinding()) {
                	String htmlPayload = samlSSOManager.buildPostRequest(request, response, false);
                    SSOAgentUtils.sendPostResponse(request, response, htmlPayload);
                } else {
                	response.sendRedirect(samlSSOManager.buildRedirectRequest(request, false));
                }
                
                return;
            }
            // pass the request along the filter chain
            LOGGER.debug("Pass the Request along the filter chain");
            chain.doFilter(request, response);

        } catch (SSOAgentException e) {
            LOGGER.error("An error has occurred", e);
            throw e;
        }
    }


    /**
     * @see Filter#destroy()
     */
    @Override
    public void destroy() {
        return;
    }

    protected void handleException(HttpServletRequest request, SSOAgentException e)
            throws SSOAgentException {

        if (request.getSession(false) != null) {
            request.getSession(false).removeAttribute(SSOAgentConstants.SESSION_BEAN_NAME);
        }
        throw e;
    }

}
