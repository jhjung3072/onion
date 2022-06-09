package com.onion.config;

import com.onion.domain.User;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class DatabaseLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	// DB 로그인 성공 후 작업
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws ServletException, IOException {
		OnionUserDetails userDetails = (OnionUserDetails) authentication.getPrincipal();
		User user = userDetails.getUser();

		super.onAuthenticationSuccess(request, response, authentication);
	}
	
	
}
