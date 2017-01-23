<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Wings SSO Sample</title>
</head>
<body>
	<h1>Wings Sample Hello home!</h1><p>
	Session Token : ${sessionTokenId}<br/>
	User Logged : ${subjectId }<br/>
	Attr : ${attr}
	<br/>
	${serverTime}
	</p>
	<a href="logout?SAML2.HTTPBinding=HTTP-Redirect&acsUrl=${acsUrl}">logout(HTTP-Redirect)</a>&nbsp;&nbsp;&nbsp;&nbsp;
	<a href="logout?SAML2.HTTPBinding=HTTP-POST&acsUrl=${acsUrl}">logout(HTTP-POST)</a>
</body>
</html>






