package org.tenwell.identity.login.web;

import java.net.URLDecoder;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.tenwell.identity.common.AbstractController;
import org.tenwell.identity.common.exception.LoginException;
import org.tenwell.identity.common.resolver.ParamMap;
import org.tenwell.identity.core.dto.SAMLSessionVO;
import org.tenwell.identity.core.exception.IdentityException;
import org.tenwell.identity.core.saml2.SAMLSSOConstants;
import org.tenwell.identity.core.saml2.SAMLSSOUtil;
import org.tenwell.identity.core.util.StringUtil;
import org.tenwell.identity.login.service.IdentityService;

@Controller
public class IdentityController extends AbstractController{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(IdentityController.class);
	
	@Autowired
	IdentityService service;
	
	/**
	 * method POST일경우 로그인 처리 수행
	 * 			GET일경우 로그인폼 요청
	 * @throws IdentityException 
	 * @throws LoginException 
	 */
	@RequestMapping(method=RequestMethod.POST, value = "/login")
	public ModelAndView loginPost(HttpServletRequest request, ParamMap param, Model model) throws IdentityException, LoginException {
		
		LOGGER.debug("SSO Login POST Processing...");
		
		ModelAndView view = new ModelAndView();
		String issuer = StringUtil.nvl(request.getParameter("issuer"), "sample.tenwell.org");
		String acsUrl = StringUtil.decodeURL(request.getParameter("acsUrl"), "UTF-8");
		String username = StringUtil.nvl(request.getParameter("username"), "");
		String password = StringUtil.nvl(request.getParameter("password"), "");
		//String sessionTokenId = "";
		
		if("".equals(username) || "".equals(password)){
			throw new IdentityException("The Service did not receive the necessary information to log in(username, password)");
		}
		
		HttpSession session = request.getSession(false);
		if(session==null || session.getAttribute(SAMLSSOConstants.SESSION_BEAN_NAME)==null){
			if("".equals(acsUrl)){
				//SP를 통하여 IDP의 로그인 화면에 접근해야 한다.
				throw new IdentityException("Have access to an invalid path. The session & ascUrl for saml is null");
			} else {
				String errorMsg = String.format("%s\r\n%s", acsUrl, messageSource.getMessage("IDENTITY.comn.acsurl", null, Locale.KOREAN));
				throw new IdentityException(errorMsg, acsUrl);
			}
		}
		
		SAMLSessionVO samlSessionVO = (SAMLSessionVO)session.getAttribute(SAMLSSOConstants.SESSION_BEAN_NAME);
		
		
		if(samlSessionVO.isLoggedIn()){
			//이미 로그인됨
			throw new IdentityException(String.format("This is the session you're already signed in to. call ASC URL %s", acsUrl));
		}
		
		try{
			SAMLSessionVO sessionVO = service.authentication(samlSessionVO, param);
			if(sessionVO.isLoggedIn()){
				LOGGER.info("You have successfully logged in, go to the ASCUrl");
				acsUrl = sessionVO.getAssertionConsumerServiceURL(issuer);
				session.setAttribute(SAMLSSOConstants.SESSION_BEAN_NAME, sessionVO);
				return new ModelAndView("redirect:" + acsUrl);
			} 
			
		} catch(Exception e){
			LOGGER.error("", e);
			view.addObject("result", false);
			view.addObject("resultMsg", e.getMessage());
		} 
		ModelAndView redirectMV = new ModelAndView("redirect:/login");
		return redirectMV;
	}
	
	
	/**
	 * method POST일경우 로그인 처리 수행
	 * 			GET일경우 로그인폼 요청
	 */
	@RequestMapping(method=RequestMethod.GET, value = "/login")
	public String loginGet(HttpServletRequest request, Locale locale, Model model) throws IdentityException {
		
		String acsUrl = StringUtil.decodeURL(request.getParameter("acsUrl"), "UTF-8");
		String redirectUrl = StringUtil.decodeURL(request.getParameter("redirectUrl"), "UTF-8");
		String message = StringUtil.nvl(request.getParameter("message"), "");
		String issuer = StringUtil.nvl(request.getParameter("issuer"), "");
		HttpSession session = request.getSession(false);
		
		if(session==null || session.getAttribute(SAMLSSOConstants.SESSION_BEAN_NAME)==null){
			if("".equals(acsUrl)){
				throw new IdentityException("No session created.");
			} else {
			    return "redirect:" + acsUrl;
			}
			
		}
		
		SAMLSessionVO sessionVO = (SAMLSessionVO)session.getAttribute(SAMLSSOConstants.SESSION_BEAN_NAME);
		String sessionTokenId = SAMLSSOUtil.getSessionTokenId(request);
		
		
		model.addAttribute("sessionTokenId", sessionTokenId);
		model.addAttribute("message", message);
		model.addAttribute("redirectUrl", redirectUrl);
		model.addAttribute("acsUrl", acsUrl);
		model.addAttribute("issuer", issuer);
		LOGGER.info("Loading Intergated SSO Login Form");
		return "login";
	}
	
	@RequestMapping(value = "/error")
	public void error(HttpServletRequest request, Locale locale, Model model) throws IdentityException {
		
		//AbstractSessionVO sessionVO = AbstractSessionVO.getSessionVO();
		Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
		throw new IdentityException("An error occurred in IdentityProviderServlet.\r\n" + throwable.getMessage(), throwable);
		//return "error";
	}
	
}
