/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.tenwell.identity.core.exception;

/**
 * Exception implementation to be used inside SAML2 SSO Identity Provider impl.
 */
public class IdentityException extends Exception {
	
	public enum ERROR_IDENTITY_CODE{
		ERROR_IDENTITY_UNKNOWN,
		ERROR_INVAILD_USER,
		ERROR_INVAILD_USER_PASSWORD,
		ERROR_EXPIRED_USER,
		ERROR_LOCKED_USER,
		ERROR_NOT_USE_USER
		
	};

    private static final long serialVersionUID = 7027553884968546755L;
    String acsUrl;
    ERROR_IDENTITY_CODE errorCode = ERROR_IDENTITY_CODE.ERROR_IDENTITY_UNKNOWN;
    
    public IdentityException(ERROR_IDENTITY_CODE errorCode, String message) {
    	super(message);
    	this.errorCode = errorCode;
    }
    
    public IdentityException(String message) {
        super(message);
    }

    public IdentityException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public IdentityException(String message, String acsUrl) {
        super(message);
        this.acsUrl = acsUrl;
    }

	public String getAcsUrl() {
		return acsUrl;
	}
	
	public ERROR_IDENTITY_CODE getErrorCode(){
    	return errorCode;
    }
}
