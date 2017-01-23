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
package org.tenwell.identity.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tenwell.identity.agent.exception.SSOAgentException;
import org.tenwell.identity.agent.saml.SSOAgentX509Credential;
import org.tenwell.identity.agent.saml.SSOAgentX509KeyStoreCredential;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TenwellSSOContextEventListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenwellSSOContextEventListener.class);
    private static Properties properties;

    public void contextInitialized(ServletContextEvent servletContextEvent) {
    	LOGGER.info("TenwellSSOContextEventListener contextInitialized!!!");
        properties = new Properties();
        try {
        	//properties.load(servletContextEvent.getServletContext().getResourceAsStream("/WEB-INF/classes/travelocity.properties"));
        	properties.load(servletContextEvent.getServletContext().getResourceAsStream("/WEB-INF/classes/saml/saml.properties"));
        	InputStream keyStoreInputStream = servletContextEvent.getServletContext().getResourceAsStream(properties.getProperty("KeyStore"));
            
            SSOAgentX509Credential credential =
                    new SSOAgentX509KeyStoreCredential(keyStoreInputStream,
                            properties.getProperty("KeyStorePassword").toCharArray(),
                            properties.getProperty("IdPPublicCertAlias"),
                            properties.getProperty("PrivateKeyAlias"),
                            properties.getProperty("PrivateKeyPassword").toCharArray());
            SSOAgentConfig config = new SSOAgentConfig();
            config.initConfig(servletContextEvent.getServletContext(), properties);
            config.getSAML2().setSSOAgentX509Credential(credential);
            config.getSAML2().setAssertionEncrypted(Boolean.parseBoolean(properties.getProperty("SAML2.EnableAssertionEncryption")));
            servletContextEvent.getServletContext().setAttribute(SSOAgentConstants.CONFIG_BEAN_NAME, config);
        } catch (IOException e){
            LOGGER.error(e.getMessage(), e);
        } catch (SSOAgentException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    	LOGGER.info("TenwellSSOContextEventListener contextDestroyed!!!");
    }

    /**
     * Get the properties of the sample
     * @return Properties
     */
    public static Properties getProperties(){
        return properties;
    }
}
