package es.jovenesadventistas.oacore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import es.jovenesadventistas.oacore.model.User;
import es.jovenesadventistas.oacore.repository.UserRepository;

public class UserDetailsServiceImp implements UserDetailsService {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	private static UserDetailsServiceImp instance;
	
	@Autowired
	private UserRepository repository;
	
    @Autowired
	public UserDetailsServiceImp() {
    	UserDetailsServiceImp.instance = this;
	}
    
    public static UserDetailsServiceImp getInstance() {
    	return UserDetailsServiceImp.instance;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username){
    	try {
    		User u = repository.findByUsername(username);
	        logger.warn("loadUserByUsername('" + username + "'): " + (u!= null ? u.getLogin() : ""));
	        return new es.jovenesadventistas.oacore.UserDetails(u);
	    } catch (Exception e) {
    		logger.info("No such user: " + username, e);
    		throw new UsernameNotFoundException(username, e);
    	}
    }
    
    public User attachUser(User u) {
    	return repository.findById(u.getId());
    }
}