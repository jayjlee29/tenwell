# tenwell
Simple SSO
- saml2을 이용한 web 기반의 SSO 프로젝트 입니다.
- WSO2를 참조하여 간단하게 개발했습니다.(https://github.com/wso2/product-is)

# 프로젝트 설명
- tenwell-identity-agent
 - SP에서 사용될 Agent Filter
- tenwell-identity-agent-sample
 - SP 예제 WAS Application
- tenwell-identity-core
 - IDP 구성을 위한 Core library
- tenwell-identity-server
 - IDP 예제 WAS Application

# Quick Start
## IDP
- Tomcat server.xml 설정 
```
    <Connector 
      SSLEnabled="true" 
      URIEncoding="UTF-8" 
      clientAuth="false" 
      keystoreFile="jks 경로" 
      keystorePass="jks " 
      maxHttpHeaderSize="65536" 
      maxThreads="150" port="8443" 
      protocol="org.apache.coyote.http11.Http11Protocol" 
      scheme="https" 
      secure="true" 
      sslProtocol="TLS"/>
 ```

- idp.properties 중요설정
```
#A unique identifier for this SAML 2.0 Service Provider application
SAML2.IdPEntityId=idp.tenwell.org

#The URL of the SAML 2.0 Identity Provider
SAML2.IdPURL=https://idp.tenwell.org:8443/identity/samlsso

SAML2.LoginURL=https://idp.tenwell.org:8443/identity/login

#Identifier given for the Service Provider for SAML 2.0 attributes 
#exchange
#SAML2.AttributeConsumingServiceIndex=1701087467

#Specify if SingleLogout is enabled/disabled
SAML2.EnableSLO=true

#This is the URL that is used for SLO
SAML2.SLOURL=logout

#Specify if AuthnRequest element is signed
SAML2.EnableRequestSigning=true

#Specify if SAMLResponse element is signed
SAML2.EnableResponseSigning=true

#Specify if SAMLAssertion element is signed
SAML2.EnableAssertionSigning=true

#Specify if SAMLAssertion element is encrypted
SAML2.EnableAssertionEncryption=true

KeyStore=WEB-INF/classes/identity.jks

#Password of the KeyStore for SAML and OpenID
KeyStorePassword=tenwell

#Alias of the IdP's public certificate
IdPPublicCertAlias=identity

#Alias of the SP's private key 

PrivateKeyAlias=identity

#Private key password to retrieve the private key used to sign 
#AuthnRequest and LogoutRequest messages
PrivateKeyPassword=tenwell

#Custom SAML post binding request page
#SAML.PostBinding.RequestPage=path/to/the/html

#Additional request parameters
#QueryParams=tenantDomain=-1234

#SAML2.IsForceAuthn=true

SpCertAlias=sample

ssl.enable=true
```

##SP
- saml.properties 중요설정
```
#A unique identifier for this SAML 2.0 Service Provider application
SAML2.SPEntityId=sample.tenwell.org

#The URL of the SAML 2.0 Assertion Consumer
SAML2.AssertionConsumerURL=http://sample.tenwell.org:8080/sample/samlsso

#A unique identifier for this SAML 2.0 Service Provider application
SAML2.IdPEntityId=idp.tenwell.org

#The URL of the SAML 2.0 Identity Provider
SAML2.IdPURL=https://idp.tenwell.org/identity/samlsso
```


##hosts 설정
```
127.0.0.1 sample.tenwell.org
127.0.0.1 idp.tenwell.org
```
