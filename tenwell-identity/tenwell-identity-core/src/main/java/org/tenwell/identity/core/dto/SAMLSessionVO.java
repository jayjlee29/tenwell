package org.tenwell.identity.core.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SAMLSessionVO implements Serializable {

	private static final long serialVersionUID = -6636885227516917352L;
	
	boolean isLoggedIn;
	Date loggedInDate;
	String userId;
	String domain;
	String sessionId;
	Map<String, String> assertionConsumerServiceURLs = new HashMap();
	Map<String, Object> attributes = new HashMap();;
	
	public boolean isLoggedIn() {
		return isLoggedIn;
	}
	public void setLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}
	public Date getLoggedInDate() {
		return loggedInDate;
	}
	public void setLoggedInDate(Date loggedInDate) {
		this.loggedInDate = loggedInDate;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	public void addAssertionConsumerServiceURL(String issuer, String assertionConsumerServiceURL){
		if(assertionConsumerServiceURLs==null)
			assertionConsumerServiceURLs = new HashMap<String, String>();
		
		assertionConsumerServiceURLs.put(issuer, assertionConsumerServiceURL);
	}
	
	public void removeAssertionConsumerServiceURL(String issuer, String assertionConsumerServiceURL){
		if(assertionConsumerServiceURLs==null)
			assertionConsumerServiceURLs = new HashMap<String, String>();
		
		assertionConsumerServiceURLs.remove(issuer);
	}
	
	public String getAssertionConsumerServiceURL(String issuer){
		
		return assertionConsumerServiceURLs.get(issuer);
	}
	
	/**
	 * 세션당 SAML을 요청한 SP Entity ID 목록
	 * @return
	 */
	public Set<String> getIssuers(){
		
		if(assertionConsumerServiceURLs==null)
			return null;
		return assertionConsumerServiceURLs.keySet();
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public Map<String, Object> getAttributes() {
		return attributes;
	}
	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}
	
}
