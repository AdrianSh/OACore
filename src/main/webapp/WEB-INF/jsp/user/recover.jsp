<%@ include file="../../jspf/header.jspf"%>
<!-- Page Content -->
<section class="container">
	<div class="row">
		<section class="col-md-12">
			<div class="row">
				<c:if test="${not empty error }">
				<p>${e:forHtmlContent(error)}</p>
				</c:if>
				<h2>Your new password</h2>
				<p>
					Your new password is: <span style="font-weight: bolder">${newPass}</span>
					remember to change it on the settings page!
				</p>
			</div>
		</section>
	</div>
</section>
<!-- /.container -->

<%@ include file="../../jspf/footer.jspf"%>