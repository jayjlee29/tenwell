/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.tenwell.identity.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tenwell.identity.core.saml2.SAMLSSOConstants;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class IdentityContextEventListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityContextEventListener.class);
    
    
    public void contextInitialized(ServletContextEvent servletContextEvent) {
    	LOGGER.info("WingsIdentityContextEventListener contextInitialized!!!");
        try {
        	
        	IdentityProviderConfig config = IdentityProviderConfig.getConfig(servletContextEvent.getServletContext());
        	config.initConfig(servletContextEvent.getServletContext());
        	servletContextEvent.getServletContext().setAttribute(SAMLSSOConstants.CONFIG_BEAN_NAME, config);
           
        } catch (Exception e){
            LOGGER.error(e.getMessage(), e);
        } 
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    	LOGGER.info("WingsIdentityContextEventListener contextDestroyed!!!");
    }
    
}
