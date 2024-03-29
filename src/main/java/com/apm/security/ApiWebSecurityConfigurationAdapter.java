package com.apm.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class APIWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

	@Autowired
	APIAuthenticationEntryPoint authenticationEntryPoint;
	@Autowired
	APILogoutSuccessHandler logoutSuccessHandler;	
	@Autowired
	APIAuthenticationProvider customAuthenticationProvider;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(customAuthenticationProvider);
	}

	// refer -
	// https://crazygui.wordpress.com/2014/08/29/secure-rest-services-using-spring-security/

	protected void configure(HttpSecurity http) throws Exception {
			http
				.addFilterBefore(restFilter(), UsernamePasswordAuthenticationFilter.class)
				.authorizeRequests()
				// allow user signup using PUT method
				.antMatchers("/v1/users", HttpMethod.GET.toString()).permitAll()
		        .antMatchers("/v1/users", HttpMethod.PUT.toString()).permitAll()
		        // allow all OPTIONS method for CORS
		        .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
		        // swagger
		        .antMatchers("/v2/api-docs").permitAll()
		        .antMatchers("/webjars/**").permitAll()
		        .antMatchers("/images/**").permitAll()
		        .antMatchers("/swagger*").permitAll()
		        
		        .antMatchers("/v1/users/**/registrationConfirm", HttpMethod.GET.toString()).permitAll()
		        .and()
				.authorizeRequests().anyRequest().authenticated().and()
				.authenticationProvider(customAuthenticationProvider)
				.exceptionHandling()
				.authenticationEntryPoint(authenticationEntryPoint)
				.and()
			
		    .formLogin()
				.permitAll()
				.loginProcessingUrl("/user/login")
				.and()
			
			.logout()
				.permitAll()
				.logoutUrl("/user/logout")
				.logoutSuccessHandler(logoutSuccessHandler)
				.and()
				
				.addFilterAfter(new CsrfHeaderFilter(), CsrfFilter.class)
				.csrf()
				.disable()
				//.csrfTokenRepository(csrfTokenRepository())
				;
				
	}

	private CsrfTokenRepository csrfTokenRepository() {
		HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
		repository.setHeaderName("X-XSRF-TOKEN");
		return repository;
	}
	
	@Bean 
	 public APIUsernamePasswordAuthenticationFilter restFilter() throws Exception { 
		APIUsernamePasswordAuthenticationFilter myFilter = new APIUsernamePasswordAuthenticationFilter(new AntPathRequestMatcher("/user/login")); 
	     myFilter.setAuthenticationManager(authenticationManager()); 
	 
	     return myFilter; 
	 }
	
	@Bean 
	 public CORSFilter corsFilter() throws Exception { 
		return new CORSFilter(); 
	 }
}