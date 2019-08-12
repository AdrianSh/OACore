package es.jovenesadventistas.oacore.config;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import es.jovenesadventistas.oacore.LocalData;
import es.jovenesadventistas.oacore.services.UserDetailsServiceImp;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		http.authorizeRequests()
        		.antMatchers("/static/**", "/logout", "/403", "/**").permitAll()
				.mvcMatchers("/admin").hasRole("ADMIN")
        		.antMatchers("/admin/**").hasRole("ADMIN")
				.anyRequest().authenticated()
				.and()
			.formLogin()
				.permitAll()
	            .loginPage("/login")
	            .and()
			.logout()
				.logoutUrl("/logout")
				.logoutSuccessUrl("/login")
	            .permitAll();
	}
	
	@Bean
	public UserDetailsServiceImp springDataUserDetailsService() {
		return new UserDetailsServiceImp();
	}
	
	@Bean(name="passwordEncoder")
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Value("${es.jovenesadventistas.oacore.base-path}")
	private String basePath;
	
    @Bean(name="localData")
    public LocalData getLocalData() {
    	return new LocalData(new File(basePath));
    }    
}