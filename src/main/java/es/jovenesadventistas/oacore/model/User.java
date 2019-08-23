package es.jovenesadventistas.oacore.model;

import org.bson.types.ObjectId;
import org.owasp.encoder.Encode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import es.jovenesadventistas.arnion.workflow.Workflow;

public class User {
	@Id
	private ObjectId id = new ObjectId();

	@Indexed(unique = true)
	private String username;
	
	private String roles; // split by , to separate roles
	private boolean enabled = true;
	private String password;
	private String name;
	private String surname;
	private String email;
	private String avatar = "http://lorempixel.com/100/100/people/10/";
	private String profileBackground = "http://lorempixel.com/100/100/people/10/";
	private String securityQuestion;
	private String securityAnswer;
	
	private Workflow mainWorkflow;

	// @DBRef  // It will be eager loaded private List<Actividad> activities;

	public User() {
	}

	// @PersistenceConstructor public User(String username, String roles, String password, String name) USE LITERAL NAMES 
	
	public static User createUser(String username, String password, String roles, String name, String surname, String email,
			String securityQuestion, String securityAnswer) {
		User u = new User();
		u.username = Encode.forHtmlContent(username);
		u.password = password; // Encode.forHtmlContent(pass));
		u.roles = Encode.forHtmlContent(roles);
		u.name = Encode.forHtmlContent(name);
		u.email = Encode.forHtmlContent(email);
		u.surname = Encode.forHtmlContent(surname);
		u.securityQuestion = Encode.forHtmlContent(securityQuestion);
		u.securityAnswer = Encode.forHtmlContent(securityAnswer);
		u.enabled = true;
		return u;
	}

	/**
	 * Converts a byte array to a hex string
	 * 
	 * @param b converts a byte array to a hex string; nice for storing
	 * @return the corresponding hex string
	 */
	public static String byteArrayToHexString(byte[] b) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < b.length; i++) {
			sb.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}

	/**
	 * Converts a hex string to a byte array
	 * 
	 * @param hex string to convert
	 * @return equivalent byte array
	 */
	public static byte[] hexStringToByteArray(String hex) {
		byte[] r = new byte[hex.length() / 2];
		for (int i = 0; i < r.length; i++) {
			String h = hex.substring(i * 2, (i + 1) * 2);
			r[i] = (byte) Integer.parseInt(h, 16);
		}
		return r;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		if (id != null)
			this.id = id;
	}

	public String getLogin() {
		return username;
	}

	public void setLogin(String login) {
		this.username = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRoles() {
		return roles;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}

	public String toString() {
		return "" + id + " " + username + " " + password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLname() {
		return surname;
	}

	public void setLname(String lname) {
		this.surname = lname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getProfileBackground() {
		return profileBackground;
	}

	public void setProfileBackground(String profileBackground) {
		this.profileBackground = profileBackground;
	}

	public String getPreguntaDeSeguridad() {
		return securityQuestion;
	}

	public void setPreguntaDeSeguridad(String preguntaDeSeguridad) {
		this.securityQuestion = preguntaDeSeguridad;
	}

	public String getRespuestaDeSeguridad() {
		return securityAnswer;
	}

	public void setRespuestaDeSeguridad(String respuestaDeSeguridad) {
		this.securityAnswer = respuestaDeSeguridad;
	}

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Workflow getMainWorkflow() {
		return mainWorkflow;
	}

	public void setMainWorkflow(Workflow mainWorkflow) {
		this.mainWorkflow = mainWorkflow;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getSecurityQuestion() {
		return securityQuestion;
	}

	public void setSecurityQuestion(String securityQuestion) {
		this.securityQuestion = securityQuestion;
	}

	public String getSecurityAnswer() {
		return securityAnswer;
	}

	public void setSecurityAnswer(String securityAnswer) {
		this.securityAnswer = securityAnswer;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof User) {
			User u2 = (User) o;
			return this.id == u2.id;
		}
		return false;
	}
}