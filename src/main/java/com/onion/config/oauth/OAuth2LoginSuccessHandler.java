package com.onion.config.oauth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.onion.domain.AuthenticationType;
import com.onion.domain.User;
import com.onion.exception.LocationNotFoundException;
import com.onion.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	@Autowired private UserService userService;
	
	// 소셜 로그인(구글, 페이스북) 로그인 성공 후 작업
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws ServletException, IOException {
		CustomerOAuth2User oauth2User = (CustomerOAuth2User) authentication.getPrincipal();
		
		String name = oauth2User.getName();
		String email = oauth2User.getEmail();
		String clientName = oauth2User.getClientName();
		
		AuthenticationType authenticationType = getAuthenticationType(clientName);
		
		User user = userService.getUserByEmail(email);
		if (user == null) { // 회원가입이 안된 새로운 회원일 경우
			try {
				userService.addNewUserUponOAuthLogin(name, email, authenticationType);
			} catch (LocationNotFoundException e) {
				throw new RuntimeException(e);
			}
		} else { // 이미 회원 가입을 했지만 소셜 로그인을 한 경우
			oauth2User.setNickname(user.getNickname());
			userService.updateAuthenticationType(user, authenticationType);
		}
		
		super.onAuthenticationSuccess(request, response, authentication);
	}
	
	private AuthenticationType getAuthenticationType(String clientName) {
		if (clientName.equals("Google")) {
			return AuthenticationType.GOOGLE;
		}  else {
			return AuthenticationType.DATABASE;
		}
	}

}
