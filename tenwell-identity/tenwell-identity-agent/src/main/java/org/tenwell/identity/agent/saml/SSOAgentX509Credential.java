/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
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
 *
 *
 */

package org.tenwell.identity.agent.saml;


import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import org.tenwell.identity.agent.exception.SSOAgentException;

public interface SSOAgentX509Credential {

    PublicKey getPublicKey() throws SSOAgentException;

    PrivateKey getPrivateKey() throws SSOAgentException;

    X509Certificate getEntityCertificate() throws SSOAgentException;

}
