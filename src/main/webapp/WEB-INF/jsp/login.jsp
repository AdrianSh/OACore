<%@ page pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>


<%@ include file="../jspf/header.jspf"%>

<div class="container">
<c:choose>
	<c:when test="${not empty user}">
	<h1>Welcome please read about us!</h1>
	</c:when>
	<c:otherwise>
	<section class="col-md-12">
		<div class="row">
			<form action="${siteUrl}/login" role="form" class="col-md-12" method="POST">
				<input type="hidden" name="${_csrf.parameterName}"
					value="${_csrf.token}" />
				<h2>
					Login
				</h2>
				<div class="row">
					<div class="col-xs-12 col-sm-4 col-md-4">
						<div class="form-group">
							<input type="text" name="username" id="username"
								class="form-control input-lg label_better" placeholder="Username"
								tabindex="1">
						</div>
					</div>
					<div class="col-xs-12 col-sm-4 col-md-4">
						<div class="form-group">
							<input type="password" name="password" id="password"
								class="form-control input-lg label_better"
								placeholder="*********" tabindex="2">
							<a href="${siteUrl}/forgot">Forgot password?</a>
						</div>
					</div>
					<div class="col-xs-12 col-sm-2 col-md-2">
						<div class="form-group">
							<input type="submit" value="Login"
							class="btn btn-primary btn-block btn-lg" tabindex="3">
						</div>
					</div>
				</div>
			</form>
		</div>
	</section>

	<section class="col-md-12">
		<div class="row">
			<form action="${siteUrl}/register" role="form" class="col-md-12 form-register" method="POST">
				<input type="hidden" name="${_csrf.parameterName}"
					value="${_csrf.token}" />
				<h2>
					Sign up <small>now!</small>
				</h2>
				<div class="row">
					<div class="col-xs-12 col-sm-6 col-md-6">
						<div class="form-group">
							<input type="text" name="firstname" id="first_name"
								class="form-control input-lg label_better" placeholder="Firstname"
								tabindex="4">
						</div>
					</div>
					<div class="col-xs-12 col-sm-6 col-md-6">
						<div class="form-group">
							<input type="text" name="surname" id="last_name"
								class="form-control input-lg label_better"
								placeholder="Surname" tabindex="5">
						</div>
					</div>
				</div>
				<div class="form-group">
					<input type="text" name="username" id="display_name"
						class="form-control input-lg label_better"
						placeholder="Username" tabindex="6">
				</div>
				<div class="form-group">
					<input type="email" name="email" id="email"
						class="form-control input-lg label_better" placeholder="Email"
						tabindex="7">
				</div>
				<div class="form-group">
					<input type="text" name="securityQuestion" id="securityQuestion"
						class="form-control input-lg label_better"
						placeholder="Security Question" tabindex="8">
				</div>
				<div class="form-group">
					<input type="text" name="securityAnswer" id="securityAnswer"
						class="form-control input-lg label_better"
						placeholder="Security Answer" tabindex="9">
				</div>
				<div class="row">
					<div class="col-xs-12 col-sm-6 col-md-6">
						<div class="form-group">
							<input type="password" name="password" id="rpassword"
								class="form-control input-lg label_better"
								placeholder="Password" tabindex="10">
						</div>
					</div>
					<div class="col-xs-12 col-sm-6 col-md-6">
						<div class="form-group">
							<input type="password" name="passwordConfirmation"
								id="password_confirmation"
								class="form-control input-lg label_better"
								placeholder="Repeat password" tabindex="11">
						</div>
					</div>
				</div>
				<div class="row">
					<div class="col-xs-8 col-sm-9 col-md-9">
						Clicking on <strong class="label label-primary">Register</strong> indicate that you have read and agree to the terms presented in the <a href="#" data-toggle="modal"
							data-target="#t_and_c_m">Terms and Conditions</a> agreement.
					</div>
				</div>
				<div class="row">
					<div class="col-xs-12 col-md-3"></div>
					<div class="col-xs-12 col-md-6">
						<input type="submit" value="Register"
							class="btn btn-primary btn-block btn-lg" tabindex="12">
					</div>
				</div>
			</form>
		</div>
			<!-- Modal -->
		<div class="modal fade" id="t_and_c_m" tabindex="-1" role="dialog"
			aria-labelledby="myModalLabel" aria-hidden="true">
			<div class="modal-dialog modal-lg">
				<div class="modal-content">
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal"
							aria-hidden="true">x</button>
						<h4 class="modal-title" id="myModalLabel">Terms & Conditions</h4>
					</div>
					<div class="modal-body">
						<p>Lorem ipsum dolor sit amet, consectetur adipisicing elit.
							Similique, itaque, modi, aliquam nostrum at sapiente
							consequuntur natus odio reiciendis perferendis rem nisi tempore
							possimus ipsa porro delectus quidem dolorem ad.</p>
						<p>Lorem ipsum dolor sit amet, consectetur adipisicing elit.
							Similique, itaque, modi, aliquam nostrum at sapiente
							consequuntur natus odio reiciendis perferendis rem nisi tempore
							possimus ipsa porro delectus quidem dolorem ad.</p>
						<p>Lorem ipsum dolor sit amet, consectetur adipisicing elit.
							Similique, itaque, modi, aliquam nostrum at sapiente
							consequuntur natus odio reiciendis perferendis rem nisi tempore
							possimus ipsa porro delectus quidem dolorem ad.</p>
						<p>Lorem ipsum dolor sit amet, consectetur adipisicing elit.
							Similique, itaque, modi, aliquam nostrum at sapiente
							consequuntur natus odio reiciendis perferendis rem nisi tempore
							possimus ipsa porro delectus quidem dolorem ad.</p>
						<p>Lorem ipsum dolor sit amet, consectetur adipisicing elit.
							Similique, itaque, modi, aliquam nostrum at sapiente
							consequuntur natus odio reiciendis perferendis rem nisi tempore
							possimus ipsa porro delectus quidem dolorem ad.</p>
						<p>Lorem ipsum dolor sit amet, consectetur adipisicing elit.
							Similique, itaque, modi, aliquam nostrum at sapiente
							consequuntur natus odio reiciendis perferendis rem nisi tempore
							possimus ipsa porro delectus quidem dolorem ad.</p>
						<p>Lorem ipsum dolor sit amet, consectetur adipisicing elit.
							Similique, itaque, modi, aliquam nostrum at sapiente
							consequuntur natus odio reiciendis perferendis rem nisi tempore
							possimus ipsa porro delectus quidem dolorem ad.</p>
					</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-primary"
							data-dismiss="modal">I Agree</button>
					</div>
				</div>
				<!-- /.modal-content -->
			</div>
			<!-- /.modal-dialog -->
		</div>
		<!-- /.modal -->
	</section>
	</c:otherwise>
	</c:choose>
</div>

<%@ include file="../jspf/footer.jspf"%>
