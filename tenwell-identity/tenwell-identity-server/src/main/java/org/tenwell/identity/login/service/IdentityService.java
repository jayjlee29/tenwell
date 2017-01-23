package org.tenwell.identity.login.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.tenwell.identity.common.exception.LoginException;
import org.tenwell.identity.common.resolver.ParamMap;
import org.tenwell.identity.core.dto.SAMLSessionVO;
import org.tenwell.identity.core.exception.IdentityException;

@Service
public class IdentityService {
	private static final Logger LOGGER = LoggerFactory.getLogger(IdentityService.class);
	
	/**
	 * 통합 로그인시 수행함
	 * 인증실패시 WingsIdentitySAML2SSOException 발생시킨다.
	 * 정의되지 않는 예외사항은 Exception을 발생시키고 Controller에서 처리한다.
	 * 인증 실패시 정확한 정보를 유저에게 보여주지 않는다.(다음과 같음)
		1. user 명이 틀렸는지
		2. password 가 틀렸는지 구분하지 않음
	 * 인증 실패 메시지는 IDENTITY.login.loginfail003 메세지로 통일
	 * 기타 메세지는 exception 메세지로 통일
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public SAMLSessionVO authentication(SAMLSessionVO sessionVO, ParamMap params) throws Exception{
		// TODO Auto-generated method stub
		boolean isSuccess = false;
		String username = StringUtils.defaultString((String) params.get("username"));
		String password = StringUtils.defaultString((String) params.get("password"), "");
		
		if(username.equals(password)){
			sessionVO.setLoggedIn(true);
			sessionVO.setLoggedInDate(new Date());
			sessionVO.setUserId(username);
			Map claimMapping = new HashMap();
			claimMapping.put("username", username);
			claimMapping.put("email", username + "@tenwell.org");
			claimMapping.put("tel", "0101112222");
			sessionVO.setAttributes(claimMapping);
			
		} else {
			throw new IdentityException("incorrect password");
		}
		
		
		return sessionVO;
	}
	
	
}
