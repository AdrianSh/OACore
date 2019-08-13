<%@ include file="../../jspf/header.jspf"%>
<!-- Page Content -->
<section class="container">
	<div class="row">
		<section class="col-md-12">
			<div class="row">
				<c:if test="${not empty error }">
					<p>${e:forHtmlContent(error)}</p>
				</c:if>
				<h2>Recover your password</h2>
				<p>If you have forgotten your password, enter your email, username and the answer to your security question:</p>

					<form method="POST" action="${siteUrl}/recover" role="form" class="col-md-12">
						<input type="hidden" name="${_csrf.parameterName}"
							value="${_csrf.token}" />
						<div class="row">
							<div class="col-xs-12 col-sm-4 col-md-4">
								<div class="form-group">
									<label>Email:</label> <input class="form-control input-lg label_better" type="email" name="email" required class="form-control">
								</div>
							</div>
						</div>
						<div class="row">
							<div class="col-xs-12 col-sm-4 col-md-4">
								<div class="form-group">
									<label>Username:</label> <input class="form-control input-lg label_better" type="text" name="username" required class="form-control">
								</div>
							</div>
						</div>
						<div class="row">
							<div class="col-xs-12 col-sm-4 col-md-4">
								<div class="form-group">
									<label>Answer to your security question:</label> <input class="form-control input-lg label_better" type="password" name="securityAnswer" required class="form-control">
								</div>
							</div>
						</div>
						<div class="row">
							<div class="col-xs-12 col-sm-4 col-md-4">
								<input type="submit" value="Recover" class="btn btn-primary btn-block btn-lg" tabindex="7">
							</div>
						</div>
					</form>
			</div>
		</section>
	</div>
</section>
<!-- /.container -->

<%@ include file="../../jspf/footer.jspf"%>