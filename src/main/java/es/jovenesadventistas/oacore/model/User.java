package es.jovenesadventistas.oacore.model;

import org.bson.types.ObjectId;
import org.owasp.encoder.Encode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

public class User {
	@Id
	private ObjectId id = new ObjectId();

	@Indexed(unique = true)
	private String username;
	private String roles; // split by , to separate roles
	private boolean enabled = true;
	private String password;
	private String name;
	private String lname;
	private String email;
	private String avatar = "http://lorempixel.com/100/100/people/10/";
	private String profileBackground = "http://lorempixel.com/100/100/people/10/";
	private String preguntaDeSeguridad;
	private String respuestaDeSeguridad;

	// @DBRef  // It will be eager loaded private List<Actividad> activities;

	public User() {
	}

	// @PersistenceConstructor public User(String username, String roles, String password, String name) USE LITERAL NAMES 
	
	public static User createUser(String login, String pass, String roles, String nombre, String apellido, String email,
			String preguntaDeSeguridad, String respuestaDeSeguridad) {
		User u = new User();
		u.username = Encode.forHtmlContent(login);
		u.password = pass; // Encode.forHtmlContent(pass));
		u.roles = Encode.forHtmlContent(roles);
		u.name = Encode.forHtmlContent(nombre);
		u.email = Encode.forHtmlContent(email);
		u.lname = Encode.forHtmlContent(apellido);
		u.preguntaDeSeguridad = Encode.forHtmlContent(preguntaDeSeguridad);
		u.respuestaDeSeguridad = Encode.forHtmlContent(respuestaDeSeguridad);
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
		return lname;
	}

	public void setLname(String lname) {
		this.lname = lname;
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
		return preguntaDeSeguridad;
	}

	public void setPreguntaDeSeguridad(String preguntaDeSeguridad) {
		this.preguntaDeSeguridad = preguntaDeSeguridad;
	}

	public String getRespuestaDeSeguridad() {
		return respuestaDeSeguridad;
	}

	public void setRespuestaDeSeguridad(String respuestaDeSeguridad) {
		this.respuestaDeSeguridad = respuestaDeSeguridad;
	}

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
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