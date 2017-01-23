/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.tenwell.identity.agent.saml;


import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tenwell.identity.agent.SSOAgentConstants;
import org.tenwell.identity.agent.TenwellSSOAgentHttpSessionAttributeListener;
import org.tenwell.identity.agent.bean.AgentSessionBean;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class SSOAgentSessionManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(SSOAgentSessionManager.class);
    /*
     * Session Index at the IdP is mapped to the session at the SP so that a single logout request
     * can be handled by invalidating the SP session mapped to IdP Session Index.
     */
    private static Map<String, Set<HttpSession>> ssoSessionsMap =
            new HashMap<String, Set<HttpSession>>();

    private SSOAgentSessionManager() {
    }

    public static void invalidateSession(HttpSession session) {
        AgentSessionBean sessionBean = (AgentSessionBean) session.getAttribute(
                SSOAgentConstants.SESSION_BEAN_NAME);
        if (sessionBean != null && sessionBean.getSAML2SSO() != null) {
            String sessionIndex = sessionBean.getSAML2SSO().getSessionIndex();
            if (sessionIndex != null) {
                Set<HttpSession> sessions = ssoSessionsMap.get(sessionIndex);
                sessions.remove(session);
            }
        }
    }

    public static Set<HttpSession> invalidateAllSessions(HttpSession session) {
        AgentSessionBean sessionBean = (AgentSessionBean) session.getAttribute(
                SSOAgentConstants.SESSION_BEAN_NAME);
        Set<HttpSession> sessions = new HashSet<HttpSession>();
        if (sessionBean != null && sessionBean.getSAML2SSO() != null) {
            String sessionIndex = sessionBean.getSAML2SSO().getSessionIndex();
            if (sessionIndex != null) {
                sessions = ssoSessionsMap.remove(sessionIndex);
            }
        }
        if (sessions == null) {
            sessions = new HashSet<HttpSession>();
        }
        return sessions;
    }

    public static Set<HttpSession> invalidateAllSessions(String sessionIndex) {
        Set<HttpSession> sessions = ssoSessionsMap.remove(sessionIndex);
        if (sessions == null) {
            sessions = new HashSet<HttpSession>();
        }
        return sessions;
    }

    public static void addAuthenticatedSession(HttpSession session) {
        String sessionIndex = ((AgentSessionBean) session.getAttribute(
                SSOAgentConstants.SESSION_BEAN_NAME)).getSAML2SSO().getSessionIndex();
        if (ssoSessionsMap.get(sessionIndex) != null) {
            ssoSessionsMap.get(sessionIndex).add(session);
            LOGGER.debug("Add Authenticated Old Session Token{}", sessionIndex);
        } else {
            Set<HttpSession> sessions = new HashSet<HttpSession>();
            sessions.add(session);
            ssoSessionsMap.put(sessionIndex, sessions);
            LOGGER.debug("Add Authenticated New Session Token {}", sessionIndex);
        }
    }
    
    public static void setAuthenticatedSession(HttpSession session) {
    	
    	if(session == null)
    		return;
    	
    	if(session.getAttribute(SSOAgentConstants.SESSION_BEAN_NAME)==null)
    		return;
    	
    	if(((AgentSessionBean) session.getAttribute(
                SSOAgentConstants.SESSION_BEAN_NAME)).getSAML2SSO()==null)
    		return;
    				
        String sessionIndex = ((AgentSessionBean) session.getAttribute(
                SSOAgentConstants.SESSION_BEAN_NAME)).getSAML2SSO().getSessionIndex();
        
        if(sessionIndex==null || "".equals(sessionIndex))
        	return;
        
        if(!ssoSessionsMap.containsKey(sessionIndex)){
        	LOGGER.debug("Doesn't contain Session Token in Context {}", sessionIndex);
        	addAuthenticatedSession(session);
        }
        
    }
}
