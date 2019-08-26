<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib
	uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project"
	prefix="e"%>
<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>
<!DOCTYPE html>
<html lang="es">
<head>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<meta name="description" content="">
	<meta name="author" content="">
	<meta name="csrf" content="${_csrf.token}"/>
    <meta name="csrf_header" content="${_csrf.headerName}"/>

	<!-- link rel="shortcut icon" type="image/png" href="${s}/img/news-icon.png"/ -->
	<title><s:message code="shortSiteName" text="OACore"
			htmlEscape="true" /> | <s:message
			code="${e:forHtmlContent(pageTitle)}"
			text="${e:forHtmlContent(defaultPageTitle)}" htmlEscape="true" /></title>

	<link href="${s}/main.css" rel="stylesheet">
</head>
<body style="overflow: hidden;">

<script type="text/javascript" src="${s}/app.bundle.js"></script>