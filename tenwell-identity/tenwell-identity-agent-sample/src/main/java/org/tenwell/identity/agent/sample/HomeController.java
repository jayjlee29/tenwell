package org.tenwell.identity.agent.sample;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.tenwell.identity.agent.SSOAgentConfig;
import org.tenwell.identity.agent.SSOAgentConstants;
import org.tenwell.identity.agent.bean.AgentSessionBean;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	@Autowired
    ServletContext context;
	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/home")
	public String home(HttpServletRequest request, Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);
		
	    String subjectId = null;
	    Map<String, List<String>> openIdAttributes = null; 
	    Map<String, String> saml2SSOAttributes = null;
	    
	    SSOAgentConfig ssoAgentConfig = (SSOAgentConfig)context.getAttribute(SSOAgentConstants.CONFIG_BEAN_NAME);
	    
	    String redirectUrl = "redirect:" + ssoAgentConfig.getSAML2().getIdPURL();

	    AgentSessionBean sessionBean = (AgentSessionBean)request.getSession(true).getAttribute(SSOAgentConstants.SESSION_BEAN_NAME);
	    AgentSessionBean.AccessTokenResponseBean accessTokenResponseBean = null;
	    
	    if(sessionBean != null){
	        if(sessionBean.getSAML2SSO() != null) {
	            subjectId = sessionBean.getSAML2SSO().getSubjectId();
	            saml2SSOAttributes = sessionBean.getSAML2SSO().getSubjectAttributes();
	            accessTokenResponseBean = sessionBean.getSAML2SSO().getAccessTokenResponseBean();
	        } else {
	        	//로그인 리다이렉트
	        	logger.info("Redirect {}", redirectUrl);
		        return redirectUrl;
	        }
	    } else {
	    	//로그인 리다이렉트
	    	//redirectUrl = "redirect:" + ssoAgentConfig.getSAML2().getACSURL();
	    	String acsUrl = ssoAgentConfig.getSAML2().getACSURL();
	    	redirectUrl = "redirect:" + ssoAgentConfig.getSAML2().getIdPURL();
	    	try {
	    		ssoAgentConfig.getSAML2().getACSURL();
	    		redirectUrl += "?acsUrl=" + URLEncoder.encode(acsUrl, "utf-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				logger.error("", e);
			} 
	    	
	    	logger.info("Redirect {}", redirectUrl);
	        return redirectUrl;
	    }
		
		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		String formattedDate = dateFormat.format(date);
		model.addAttribute("subjectId", subjectId );
		model.addAttribute("serverTime", formattedDate );
		model.addAttribute("acsUrl", ssoAgentConfig.getSAML2().getACSURL());
		model.addAttribute("sessionTokenId", sessionBean.getSAML2SSO().getSessionIndex());
		model.addAttribute("attr", saml2SSOAttributes.toString());
		
		return "home";
	}
	
	@RequestMapping(value = "/logout")
	public String logout(HttpServletRequest request, Locale locale, Model model) {
		
		return "redirect:home";
	}
	
}
