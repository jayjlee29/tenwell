# tenwell

Simple SAML2 SSO
saml2을 이용한 web 기반의 SSO 프로젝트 입니다.


# 프로젝트 설명  

* tenwell-identity-agent  
SP에서 사용될 Agent Filter


* tenwell-identity-agent-sample     
SP 예제 WAS Application  


* tenwell-identity-core   
IDP 구성을 위한 Core library  


* tenwell-identity-server  
IDP 예제 WAS Application



# 환경설정
* IDP
  * tenwell-identity-server ssl 설정
  * idp.properties 설정


* SP  
  * saml.properties 설정


# hosts 설정
<pre>
127.0.0.1 sample.tenwell.org
127.0.0.1 idp.tenwell.org
</pre>
