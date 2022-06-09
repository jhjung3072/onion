package com.onion.config;

import javax.servlet.http.HttpServletRequest;


public class Utility {
	
	// 현재 페이지의 url 중 ServletPath 지우고 반환(이메일 인증 코드와 패스워드 초기화 링크에 사용)
	public static String getSiteURL(HttpServletRequest request) {
		String siteURL = request.getRequestURL().toString();
		return siteURL.replace(request.getServletPath(), "");
	}

}
