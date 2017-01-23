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
package org.tenwell.identity.core.dto;

import java.io.Serializable;
import java.util.Date;

public class SAMLSingleLogoutRequestVO implements Serializable {

    private static final long serialVersionUID = -5086237688925774301L;
    
    private String id;
    private String issuer;
    private String username;
    private String company;
    private String destination;
    private String logoutResponse;
    private String rpSessionId;
    private String reason;
    private Date notOnOrAfter;
    private String assertionConsumerURL;

    public String getLogoutResponse() {
        return logoutResponse;
    }

    public void setLogoutResponse(String logoutResponse) {
        this.logoutResponse = logoutResponse;
    }

    public String getRpSessionId() {
        return rpSessionId;
    }

    public void setRpSessionId(String rpSessionId) {
        this.rpSessionId = rpSessionId;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Date getNotOnOrAfter() {
		return notOnOrAfter;
	}

	public void setNotOnOrAfter(Date notOnOrAfter) {
		this.notOnOrAfter = notOnOrAfter;
	}
	
	

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getAssertionConsumerURL() {
		return assertionConsumerURL;
	}

	public void setAssertionConsumerURL(String assertionConsumerURL) {
		this.assertionConsumerURL = assertionConsumerURL;
	}

	@Override
	public String toString() {
		return "WingsSingleLogoutRequestDTO [id=" + id + ", issuer=" + issuer + ", username="
				+ username + ", company=" + company + ", destination=" + destination + ", logoutResponse="
				+ logoutResponse + ", rpSessionId=" + rpSessionId + ", reason=" + reason + ", notOnOrAfter="
				+ notOnOrAfter + "]";
	}

	
    
}