package com.onion.config;

import com.onion.domain.Role;
import com.onion.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;


public class OnionUserDetails implements UserDetails {
	private static final long serialVersionUID = 1L;
	private User user;

	public OnionUserDetails(User user) {
		this.user = user;
	}

	// 해당 유저의 권한 목록
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Set<Role> roles = user.getRoles();

		List<SimpleGrantedAuthority> authories = new ArrayList<>();

		for (Role role : roles) {
			authories.add(new SimpleGrantedAuthority(role.getName()));
		}

		return authories;
	}

	// 패스워드
	@Override
	public String getPassword() {
		return user.getPassword();
	}

	// 이메일
	@Override
	public String getUsername() {
		return user.getEmail();
	}

	// 계정 만료 여부
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	// 계정 잠김 여부
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	// 패스워드 만료 여부
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	// 활성화 여부
	@Override
	public boolean isEnabled() {
		return user.isEnabled();
	}


	// 권한 있는지 유무
	public boolean hasRole(String roleName) {
		return user.hasRole(roleName);
	}

	public User getUser() {
		return user;
	}
}
