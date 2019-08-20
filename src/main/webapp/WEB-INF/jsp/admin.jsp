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
	<!-- link rel="shortcut icon" type="image/png" href="${s}/img/news-icon.png"/ -->
	<title><s:message code="shortSiteName" text="OACore"
			htmlEscape="true" /> | <s:message
			code="${e:forHtmlContent(pageTitle)}"
			text="${e:forHtmlContent(defaultPageTitle)}" htmlEscape="true" /></title>

	<link href="${s}/bootstrap-4.3.1-dist/css/bootstrap.min.css" rel="stylesheet">
	<link href="${s}/css/main.css" rel="stylesheet">
</head>
<body style="overflow: hidden;">


<script src="${s}/js/jquery-3.4.1.min.js"></script>
<script src="${s}/js/popper.min.js"></script>
<script src="${s}/bootstrap-4.3.1-dist/js/bootstrap.min.js"></script>

<script type="text/javascript" src="${s}/dist/app.bundle.js"></script>