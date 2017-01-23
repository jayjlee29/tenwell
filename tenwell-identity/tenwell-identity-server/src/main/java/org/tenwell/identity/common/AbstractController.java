package org.tenwell.identity.common;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

public abstract class AbstractController {
	@Autowired
	protected ServletContext context;
	
	@Resource(name="messageSource")
	protected MessageSource messageSource;
	
/*	@Resource(name="jsonView")
	protected MappingJacksonJsonView jsonView;*/
}
