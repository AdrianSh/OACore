<%@ include file="../jspf/header.jspf"%>
<!-- Page Content -->
<section class="container">
	<div class="row">
		<section class="col-md-12">
			<div class="row">
				<form action="${siteUrl}/settings" class="col-md-12 form-register" enctype="multipart/form-data"
					role="form" method="POST">
					<input type="hidden" name="${_csrf.parameterName}"
						value="${_csrf.token}" />
					<div class="text-center">
						<h2>Settings</h2>
					</div>
					<div class="row">
						<div class="col-xs-12 col-sm-6 col-md-12">
							<div class="form-group">
							<label>Profile photo:</label>
								<input type="file" name="avatar" id="avatar" accept="image/*"
									class="form-control input-lg label_better" placeholder="Ruta"
									tabindex="1">
								<small class="form-text text-muted">Max 1Mb</small>
							</div>
						</div>
					</div>
					<div class="form-group">
					<label>Email:</label>
						<input type="email" name="email" id="email"
							class="form-control input-lg label_better" placeholder="${email}"
							tabindex="2">
							
					</div>
					<div class="row">
						<div class="col-xs-12 col-sm-6 col-md-12">
							<div class="form-group">
							<label>New password:</label>
								<input type="password" name="pass" id="password"
									class="form-control input-lg label_better"
									placeholder="New password" tabindex="3">
									
							</div>
						</div>
					</div>
					
					<div class="row">
						<div class="col-xs-12 col-sm-6 col-md-12">
							<div class="form-group">
							<label>Current password:</label>
								<input type="password" name="oldpass" id="oldpassword"
									class="form-control input-lg label_better"
									placeholder="Current password" required tabindex="3">
									
							</div>
						</div>
					</div>
					
					<div class="row">
						<div class="col-xs-12 col-md-3"></div>
						<div class="col-xs-12 col-md-6">
							<input type="submit" value="Update"
								class="btn btn-primary btn-block btn-lg" tabindex="4">
						</div>
					</div>
				</form>
			</div>
		</section>
	</div>
</section>
<!-- /.container -->

<%@ include file="../jspf/footer.jspf"%>