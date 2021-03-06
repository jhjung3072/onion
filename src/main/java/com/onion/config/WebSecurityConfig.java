package com.onion.config;

import com.onion.config.oauth.CustomerOAuth2UserService;
import com.onion.config.oauth.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {


	@Bean
	public UserDetailsService userDetailsService() {
		return new OnionUserDetailsService();
	}

	@Autowired private DataSource dataSource;
	@Autowired private CustomerOAuth2UserService oAuth2UserService;
	@Autowired private DatabaseLoginSuccessHandler databaseLoginHandler;

	private final OAuth2LoginSuccessHandler oauth2LoginHandler;

	public WebSecurityConfig(@Lazy OAuth2LoginSuccessHandler oauth2LoginHandler){
		this.oauth2LoginHandler=oauth2LoginHandler;
	}



	// PasswordEncoder??? ???????????? ???????????? ???????????? ?????????
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	//DaoAuthenticationProvider??? ???????????? ????????? ID, Password??? ????????? ID, Password??? ??????
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService());
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/account_details").authenticated()
				.anyRequest().permitAll()
				.and()
				.formLogin()
					.loginPage("/login")
					.successHandler(databaseLoginHandler)
					.permitAll()
				.and()
				.oauth2Login()
					.loginPage("/login")
					.userInfoEndpoint()
					.userService(oAuth2UserService)
					.and()
					.successHandler(oauth2LoginHandler)
				.and()
				.logout()
					.permitAll()
					.logoutSuccessUrl("/")
				.and()
				.rememberMe()
					.userDetailsService(userDetailsService())
					.tokenRepository(tokenRepository())
				.and()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
		;
	}

	@Bean
	public PersistentTokenRepository tokenRepository() {
		JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
		jdbcTokenRepository.setDataSource(dataSource);
		return jdbcTokenRepository;
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/images/**", "/js/**", "/webjars/**")
				.requestMatchers(PathRequest.toStaticResources().atCommonLocations());
	}

	
}
