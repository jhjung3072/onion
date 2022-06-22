package com.onion.config;

import com.onion.config.oauth.CustomerOAuth2User;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import javax.servlet.http.HttpServletRequest;


public class Utility {
	
	// 현재 페이지의 url 중 ServletPath 지우고 반환(이메일 인증 코드와 패스워드 초기화 링크에 사용)
	public static String getSiteURL(HttpServletRequest request) {
		String siteURL = request.getRequestURL().toString();
		return siteURL.replace(request.getServletPath(), "");
	}

	// 승인된 회원 이메일 GET, 참고 https://velog.io/@ysb05222/%EB%A1%9C%EA%B7%B8%EC%9D%B8-%ED%95%98%EA%B8%B0
	public static String getEmailOfAuthenticatedCustomer(HttpServletRequest request) {
		Object principal = request.getUserPrincipal();
		if (principal == null) return null;

		String customerEmail = null;

		// 폼 로그인 회원이거나 쿠기로그인(로그인 기억) 회원일 경우 회원 이메일 리턴
		// 소셜 로그인일 경우 회원 이메일 리턴
		if (principal instanceof UsernamePasswordAuthenticationToken
				|| principal instanceof RememberMeAuthenticationToken) {
			customerEmail = request.getUserPrincipal().getName();
		} else if (principal instanceof OAuth2AuthenticationToken) {
			OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) principal;
			CustomerOAuth2User oauth2User = (CustomerOAuth2User) oauth2Token.getPrincipal();
			customerEmail = oauth2User.getEmail();
		}

		return customerEmail;
	}
}
