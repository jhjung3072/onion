package com.onion.config;

import com.onion.user.User;
import com.onion.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


public class OnionUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepo;
	
	//유저의 정보(by Email)를 불러와서 UserDetails로 리턴
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepo.findByEmail(email);
		if (user != null) {
			return new OnionUserDetails(user);
		}
		
		throw new UsernameNotFoundException("해당 이메일을 가진 유저을 찾을 수 없습니다: " + email);
	}

}
