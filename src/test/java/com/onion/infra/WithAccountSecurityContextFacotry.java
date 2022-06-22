package com.onion.infra;

import com.onion.config.OnionUserDetailsService;
import com.onion.domain.User;
import com.onion.location.LocationRepository;
import com.onion.user.RoleService;
import com.onion.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;


public class WithAccountSecurityContextFacotry implements WithSecurityContextFactory<WithAccount> {

    @Autowired private UserService userService;
    @Autowired private LocationRepository locationRepository;

    @Autowired private RoleService roleService;
    @Autowired private OnionUserDetailsService userDetailsService;

    @Override
    public SecurityContext createSecurityContext(WithAccount withAccount) {
        String email = withAccount.value();

        User newUser = new User();
        newUser.setNickname("nickname");
        newUser.setEmail(email);
        newUser.setEnabled(true);
        newUser.setBio("hi");
        newUser.setEmailVerified(true);
        newUser.setPassword("1234");
        newUser.setLocation(locationRepository.findById(5).get());
        userService.registerUser(newUser);


        UserDetails principal = userDetailsService.loadUserByUsername(email);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
