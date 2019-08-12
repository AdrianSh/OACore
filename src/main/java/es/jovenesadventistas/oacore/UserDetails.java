package es.jovenesadventistas.oacore;

import java.util.ArrayList;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import es.jovenesadventistas.oacore.model.User;
import es.jovenesadventistas.oacore.services.UserDetailsServiceImp;

public class UserDetails implements org.springframework.security.core.userdetails.UserDetails {
	private static final long serialVersionUID = 5693546765169908335L;
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	private User user;
	ArrayList<SimpleGrantedAuthority> roles;
	
	public UserDetails (User user) {
		this.user = user;
		
		 // build UserDetails object
        roles = new ArrayList<>();
        for (String r : user.getRoles().split("[,]")) {
        	roles.add(new SimpleGrantedAuthority("ROLE_" + r));
	        logger.info("Roles for " + user.getLogin() + " include " + roles.get(roles.size()-1));
        }
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public User getUser() {
		this.user = UserDetailsServiceImp.getInstance().attachUser(this.user);
		return this.user;
	}
	
	public boolean isAdmin() {
		return this.roles.toString().contains("ROLE_admin");
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return roles;
	}

	@Override
	public String getPassword() {
		return this.user.getPassword();
	}

	@Override
	public String getUsername() {
		return this.user.getLogin();
	}

	@Override
	public boolean isAccountNonExpired() {
		return this.user.getEnabled();
	}

	@Override
	public boolean isAccountNonLocked() {
		return this.user.getEnabled();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return this.user.getEnabled();
	}

	@Override
	public boolean isEnabled() {
		return this.user.getEnabled();
	}

}
