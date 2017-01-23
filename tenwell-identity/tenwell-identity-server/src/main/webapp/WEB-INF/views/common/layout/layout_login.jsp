<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<title>Wings SSO</title>
	<link rel="stylesheet" type="text/css" href="/css/default.css " />
	
	<script type="text/javascript">
	var context = "${pageContext.request.contextPath }";
	</script>
	<script type="text/javascript" src="/js/jquery/jquery-1.9.1.min.js?version=${VERSION}"></script>
	<script type="text/javascript" src="/js/jquery/jquery.clipboard.min.js?version=${VERSION}"></script>
	<script type="text/javascript" src="/js/jquery/jquery-ui.min.js?version=${VERSION}"></script>
	<script type="text/javascript" src="/js/jquery/plugin/jquery.mask.min.js?version=${VERSION}"></script>
	<script type="text/javascript" src="/js/jquery/plugin/jquery.debounce.min.js?version=${VERSION}"></script>
	<script type="text/javascript" src="/js/jquery/jquery.form.min.js?version=${VERSION}"></script>
	<script type="text/javascript" src="/js/jquery/jquery.validate.min.js?version=${VERSION}"></script>
	<script type="text/javascript" src="/js/jquery.cookie.min.js?version=${VERSION}"></script>
	
	<script type="text/javascript" src="/js/comm/comm.js?version=${VERSION}"></script>
	<script type="text/javascript" src="/js/comm/comm.withCallback.js?version=${VERSION}"></script>
	<script type="text/javascript" src="/js/comm/comm.popup.js?version=${VERSION}"></script>
	<script type="text/javascript" src="/BizSource/js/comm/management/lang_comm.js?version=${VERSION}"></script>
	<script type="text/javascript" src="/BizSource/js/comm/management/lang_manager.js?version=${VERSION}"></script>
	<tiles:insertAttribute name="userScript"/>
</head>
<body>
<tiles:insertAttribute name="body"/>
<tiles:insertAttribute name="inc_messagebox"/>
</body>
</html>