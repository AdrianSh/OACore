package es.jovenesadventistas.oacore.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.Principal;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import javax.naming.SizeLimitExceededException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import es.jovenesadventistas.oacore.LocalData;
import es.jovenesadventistas.oacore.Messages;
import es.jovenesadventistas.oacore.UserDetails;
import es.jovenesadventistas.oacore.model.User;
import es.jovenesadventistas.oacore.repository.UserRepository;

@Controller
public class UserController {
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	@Autowired
	private LocalData localData;

	@Autowired
	private Environment env;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	Messages messages;

	private static UserController instance;

	@ModelAttribute
	public void addAttributes(Model model, Locale locale, HttpServletRequest httpServletRequest) {
		model.addAttribute("s", "/static");
		model.addAttribute("siteUrl", env.getProperty("es.jovenesadventistas.oacore.site-url"));
		model.addAttribute("siteName", env.getProperty("es.jovenesadventistas.oacore.site-name"));
		model.addAttribute("shortSiteName", env.getProperty("es.jovenesadventistas.oacore.short-site-name"));
		model.addAttribute("pageTitle", httpServletRequest.getRequestURI());
		model.addAttribute("defaultPageTitle", ";)");
		
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		String formattedDate = dateFormat.format(new Date());

		model.addAttribute("serverTime", formattedDate);
	}

	public UserController() {
		UserController.instance = this;
	}

	public static UserController getInstance() {
		return UserController.instance;
	}

	@RequestMapping(value = "/settings", method = RequestMethod.GET)
	@Transactional
	public String ajustes(Locale locale, Model model) {
		String returnn = "redirect:home";
		if (ping()) {
			User u = this.getPrincipal().getUser();
			model.addAttribute("user", u);
			model.addAttribute("email", Encode.forHtmlContent(u.getEmail()));
			returnn = "settings";
		}
		return returnn;
	}
	
	@ExceptionHandler(SizeLimitExceededException.class)
    public String sizeLimitExceededException(SizeLimitExceededException exc) {
        return "redirect:/profile";
    }
	
	@ExceptionHandler(MultipartException.class)
	@ResponseStatus(value = HttpStatus.PAYLOAD_TOO_LARGE)
	public ResponseEntity<?> handleMultipartException(MultipartException ex) {
	    return ResponseEntity.badRequest().build();
	}
	
	@RequestMapping(value = "/settings", method = RequestMethod.POST)
	@Transactional
	public String handleFileAjustes(@RequestParam("avatar") MultipartFile avatar, @RequestParam("email") String email,
			@RequestParam("pass") String newPassword, @RequestParam("oldpass") String oldPassword, Model model) {

		String returnn = "redirect:/";

		UserDetails uds = this.getPrincipal();
		User u = uds.getUser();
		ObjectId id = u.getId();

		if(!passwordEncoder.matches(oldPassword, uds.getPassword())) {
			model.addAttribute("error", "It is not your current password.");
			model.addAttribute("user", u);
			return "settings";
		}
			
		if (!avatar.isEmpty()) {
			try {
				byte[] bytes = avatar.getBytes();
				BufferedOutputStream stream = new BufferedOutputStream(
						new FileOutputStream(localData.getFile("user", id.toString())));
				stream.write(bytes);
				stream.close();
				u.setAvatar("user/" + u.getId() + "/photo");
				model.addAttribute("avatar",
						Encode.forUriComponent(localData.getFile("user", id.toString()).getAbsolutePath()));
				// ContextInitializer.getFile("user", id).getAbsolutePath();
			} catch (Exception e) {
				return "You failed to upload an avatar, userid: " + id + " => " + e.getMessage();
			}
		}

		if (!email.isEmpty()) {
			u.setEmail(email);
			model.addAttribute("email", Encode.forHtmlContent(email));
		}

		if (!newPassword.isEmpty()) {
			if(newPassword == null || newPassword.length() < 4)
				model.addAttribute("error", "Password should contains at least 4 characters");
			else
				u.setPassword(passwordEncoder.encode(newPassword));
		}
		model.addAttribute("user", u);
		userRepository.save(u);

		this.reloadPrincipal();
		return returnn;
	}
	
	@RequestMapping(value = { "/register", "/user/create" }, params = { "firstname", "surname", "username", "email",
			"securityQuestion", "securityAnswer", "password", "passwordConfirmation" }, method = RequestMethod.POST)
	@Transactional
	public String crearUsuario(@RequestParam("username") String username, @RequestParam("passwordConfirmation") String passwordConfirmation,
			@RequestParam("password") String password, @RequestParam("firstname") String firstname,
			@RequestParam("surname") String surname, @RequestParam("email") String email,
			@RequestParam("securityQuestion") String securityQuestion, Model model, @RequestParam("securityAnswer") String securityAnswer,
			HttpServletRequest request, HttpServletResponse response, HttpSession session, Locale locale) {
		String returnn = "redirect:/";
		try {
			/* User u1 = (User) entityManager.createNamedQuery("userByLogin")
					.setParameter("loginParam", Encode.forHtmlContent(login)).getSingleResult(); */
			
			User u1 = userRepository.findByUsername(Encode.forHtmlContent(username));
			model.addAttribute("error",
					"Username already exists: '" + Encode.forHtmlContent(u1.getLogin()) + "'");
			returnn = "login";
		} catch (NullPointerException e) {
			returnn = "login";
			if (!password.equals(passwordConfirmation)) {
				logger.info("Password match fail: {}, {}", password, passwordConfirmation);
				model.addAttribute("error", "Passwords do not match, verify all data.");
				returnn = "login";
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
			if (username == null || username.length() < 4 || password == null || password.length() < 4 || firstname == null
					|| surname == null || email == null) {
				model.addAttribute("error",
						"Verify all fields and remember that the username and password must have at least 4 characters.");
				returnn = "login";
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			} else {				
				User user = User.createUser(username, passwordEncoder.encode(password), "user", firstname, surname, email,
						securityQuestion, securityAnswer);
				
				userRepository.save(user);
				
				logger.info("User registered {} with password hash {}", user.getLogin(), user.getPassword());
				
				model.addAttribute("alert", "You have successfully registered, what are you waiting for? Log in!");
				// sets the anti-csrf token
				getTokenForSession(session);
			}
		}
		return returnn;
	}

	@RequestMapping(value = "/profile", method = RequestMethod.GET)
	@Transactional
	public String perfil(Locale locale, Model model) {
		String returnn = "redirect:home";
		if (ping()){
			User u = this.getPrincipal().getUser();
			model.addAttribute("user", u);

			returnn = "profile";
		}

		return returnn;
	}

	@ResponseBody
	@RequestMapping(value = "/user/{id}/photo", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
	public byte[] userPhoto(@PathVariable("id") ObjectId id) throws IOException {
		try {
			File f = localData.getFile("user", id.toString());
			InputStream in = null;
			if (f.exists()) {
				in = new BufferedInputStream(new FileInputStream(f));
			} else {
				in = new BufferedInputStream(this.getClass().getClassLoader().getResourceAsStream("unknown-user.jpg"));
			}

			return IOUtils.toByteArray(in);
		} catch (IOException e) {
			logger.warn("Error loading ..." + id, e);
			throw e;
		} 
	}

	/**
	 * Logout (also returns to home view).
	 */
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String logout() {
		return "logout";
	}
	
	/** Uploads a photo for a user
	 * 
	 * @param id
	 *            of user
	 * @param photo
	 *            to upload
	 * @return
	 */
	@RequestMapping(value = "/user", method = RequestMethod.POST)
	@Transactional
	public @ResponseBody String handleFileUpload(@RequestParam("photo") MultipartFile photo,
			@RequestParam("id") String id) {
		if (!photo.isEmpty()) {
			try {
				byte[] bytes = photo.getBytes();
				BufferedOutputStream stream = new BufferedOutputStream(
						new FileOutputStream(localData.getFile("user", id)));
				stream.write(bytes);
				stream.close();
				return "You successfully uploaded " + id + " into " + localData.getFile("user", id).getAbsolutePath()
						+ "!";
			} catch (Exception e) {
				return "You failed to upload " + id + " => " + e.getMessage();
			}
		} else {
			return "You failed to upload a photo for " + id + " because the file was empty.";
		}
	}

	/**
	 * Olvidar contraseña.
	 */
	@RequestMapping(value = { "/forgot", "/forgotpassword" }, method = RequestMethod.GET)
	@Transactional
	public String olvidoPassWebb(Locale locale, Model model, HttpSession session) {
		return "user/forgot";
	}

	@RequestMapping(value = "/recover", method = RequestMethod.POST)
	@Transactional
	public String regenerarpass(@RequestParam("email") String email, @RequestParam("username") String username,
			@RequestParam("securityAnswer") String securityAnswer, Locale locale, Model model, HttpSession session) {
		String returnn = "user/recover";
		try {
			User user = userRepository.findByUsername(Encode.forHtmlContent(username));

			if (user == null) {
				model.addAttribute("error", "Some of the data entered does not match.");
				returnn = "user/forgot";
			} else {
				if (user.getEmail().equals(Encode.forHtmlContent(email))
						&& user.getRespuestaDeSeguridad().equals(Encode.forHtmlContent(securityAnswer))) {
					logger.debug("New password assigned.");
					String random = Encode.forHtmlContent(generarStringPass());
					model.addAttribute("newPass", random);
					user.setPassword(passwordEncoder.encode(random));
					userRepository.save(user);
				} else {
					model.addAttribute("error", "Some of the data entered does not match.");
					logger.info(user.getLogin() + "!=" + Encode.forHtmlContent(username) + "  + "
							+ user.getRespuestaDeSeguridad() + "!= " + Encode.forHtmlContent(securityAnswer));
				}
			}
		} catch (NullPointerException e) {
			logger.debug("Some error while recovering password:", e);
			returnn = "redirect:/";
		}
		return returnn;
	}

	private String generarStringPass() {
		SecureRandom random = new SecureRandom();
		return new BigInteger(130, random).toString(32);
	}

	/*
	 * Returns true if the user is logged in
	 */
	protected static boolean ping() {
		// - org.springframework.security.authentication.AnonymousAuthenticationToken
		// -
		// org.springframework.security.authentication.UsernamePasswordAuthenticationToken
		Principal p = SecurityContextHolder.getContext().getAuthentication();
		return p instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
	}

	protected UserDetails getPrincipal() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return principal instanceof UserDetails ? (UserDetails) principal : null;
	}

	protected void reloadPrincipal() {
		UserDetails principal = this.getPrincipal();
		principal.setUser(userRepository.findById(principal.getUser().getId())); // entityManager.find(User.class, principal.getUser().getId()));
	}

	/**
	 * Returns true if the user is logged in and is an admin
	 */
	protected static boolean isAdmin() {
		try {
			return SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString()
					.contains("ROLE_admin");
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Checks the anti-csrf token for a session against a value
	 * 
	 * @param session
	 * @param token
	 * @return the token
	 */
	static boolean isTokenValid(HttpSession session, String token) {
		Object t = session.getAttribute("csrf_token");
		return (t != null) && t.equals(token);
	}

	/**
	 * Returns an anti-csrf token for a session, and stores it in the session
	 * 
	 * @param session
	 * @return
	 */
	static String getTokenForSession(HttpSession session) {
		String token = UUID.randomUUID().toString();
		session.setAttribute("csrf_token", token);
		return token;
	}
}
