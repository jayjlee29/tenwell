package org.tenwell.identity.common.exception;

public class LoginException extends Exception{
	
	public enum LOGIN_RESULT_CODE{
		UNKNOWN_ERROR,
		INCORRECT_PASSWORD,
		
	}
	
	LOGIN_RESULT_CODE resultCode;
	
	public LoginException(LOGIN_RESULT_CODE resultCode, String errorMessage) {
		super(errorMessage);
		// TODO Auto-generated constructor stub
		this.resultCode = resultCode;
	}
	
	public LoginException(String errorMessage) {
		super(errorMessage);
		// TODO Auto-generated constructor stub
	}

}
