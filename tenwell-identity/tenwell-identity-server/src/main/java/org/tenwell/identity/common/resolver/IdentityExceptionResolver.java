package org.tenwell.identity.common.resolver;

import java.sql.SQLException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

public class IdentityExceptionResolver implements HandlerExceptionResolver{
	
	static Logger LOGGER = LoggerFactory.getLogger(IdentityExceptionResolver.class);
	
	@Resource(name="messageSource")
	MessageSource messageSource;
	@Resource(name="jsonView")
	MappingJacksonJsonView jsonView;
	 
	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		// TODO Auto-generated method stub
		LOGGER.error(ex.getMessage(), ex);
		String contentType = request.getContentType();
		ModelAndView mv = new ModelAndView(jsonView);
		if(ex instanceof SQLException) {
			int sqlErrorCode = ((SQLException)ex).getErrorCode();
			mv.addObject("err", String.format("%s [error code %d]", ex.getClass().getName(), sqlErrorCode));
			mv.addObject("traceInfo", ex.toString());
		} else {
			mv.addObject("err", ex.getClass().getName());
			mv.addObject("traceInfo", ex.toString());
		}
		return mv;
	}

}
