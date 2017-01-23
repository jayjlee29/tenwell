package org.tenwell.identity.agent;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tenwell.identity.agent.bean.AgentSessionBean;
import org.tenwell.identity.agent.saml.SSOAgentSessionManager;


public class TenwellSSOAgentHttpSessionAttributeListener implements HttpSessionAttributeListener  {

	private static final Logger LOGGER = LoggerFactory.getLogger(TenwellSSOAgentHttpSessionAttributeListener.class);
	@Override
	public void attributeAdded(HttpSessionBindingEvent event) {
		// TODO Auto-generated method stub
		String key = event.getName();
		
		if(SSOAgentConstants.SESSION_BEAN_NAME.equals(key)){
			AgentSessionBean bean = (AgentSessionBean)event.getValue();
			if(bean.getSAML2SSO()!=null){
				LOGGER.debug("HTTP Session attributeAdded Key : {}, Session Token ID : {}", key, bean.getSAML2SSO().getSessionIndex());
				SSOAgentSessionManager.setAuthenticatedSession(event.getSession());
			} else {
				LOGGER.debug("HTTP Session attributeAdded Key : {} Session Token ID is not exist", key);
			}
		}
		
		
		
	}

	@Override
	public void attributeRemoved(HttpSessionBindingEvent event) {
		// TODO Auto-generated method stub
		LOGGER.debug("HTTP Session attributeRemoved {}", event.toString());
	}

	@Override
	public void attributeReplaced(HttpSessionBindingEvent event) {
		// TODO Auto-generated method stub
		
	}

}
