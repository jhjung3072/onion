package com.onion.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.onion.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class UserRestController {
	@Autowired
	private UserService service;
	
	// 유저 이름 중복
	@PostMapping("/users/check_unique_email")
	public String checkDuplicateEmail(Integer id, String email) {
		return service.isEmailUnique(id, email) ? "OK" : "Duplicated";
	}
	

}
