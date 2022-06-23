package com.onion;

import javax.servlet.http.HttpServletRequest;

import com.onion.config.Utility;
import com.onion.domain.User;
import com.onion.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// 로그인한 회원의 이메일을 이용하여 회원 객체 리턴
@Component
public class ControllerHelper {
	@Autowired private UserService userService;
	
	public User getAuthenticatedUser(HttpServletRequest request) {
		String email = Utility.getEmailOfAuthenticatedCustomer(request);
		return userService.getUserByEmail(email);
	}		
}
