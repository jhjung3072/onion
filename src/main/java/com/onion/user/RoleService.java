package com.onion.user;

import com.onion.domain.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Role findByName(String name){
        return roleRepository.findByName(name);
    }
}
