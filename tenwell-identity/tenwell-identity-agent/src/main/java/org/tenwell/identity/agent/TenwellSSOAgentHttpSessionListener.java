package org.tenwell.identity.agent;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tenwell.identity.agent.bean.AgentSessionBean;

public class TenwellSSOAgentHttpSessionListener implements HttpSessionListener  {

	private static final Logger LOGGER = LoggerFactory.getLogger(TenwellSSOAgentHttpSessionListener.class);

	@Override
	public void sessionCreated(HttpSessionEvent se) {
		// TODO Auto-generated method stub
		HttpSession session = se.getSession();
		if(session!=null && session.getAttribute(SSOAgentConstants.SESSION_BEAN_NAME)!=null){
			AgentSessionBean bean = (AgentSessionBean)session.getAttribute(SSOAgentConstants.SESSION_BEAN_NAME);
			if(bean.getSAML2SSO()!=null){
				LOGGER.debug("HTTP Session created Session Token ID {}", bean.getSAML2SSO().getSessionIndex());
			}else {
				LOGGER.debug("HTTP Session created with Unknown Session Token ID");
			}
			
		} else {
			LOGGER.debug("HTTP Session created without LoggedInSessionBean");
		}
        
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		// TODO Auto-generated method stub
		LOGGER.debug("HTTP Session destroyed {}", se.toString());
	}
}
