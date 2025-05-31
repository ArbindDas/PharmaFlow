package com.JSR.PharmaFlow.Services;

import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String username)throws UsernameNotFoundException {
        Optional <Users> users = usersRepository.findByEmail (username);
        if (users.isPresent ()){
            // Map roles to GrantedAuthority, ensuring the ROLE_ prefix is added if not already present
            List < GrantedAuthority > authorities = users.get ().getRoles ()
                    .stream().map (role -> new SimpleGrantedAuthority ("ROLE_" + role.name()))
                    .collect( Collectors.toList());

            return org.springframework.security.core.userdetails.User.builder ()
                    .username (users.get ().getEmail ())
                    .password (users.get ().getPassword ())
                    .authorities (authorities)
                    .build ();
        }
        throw new UsernameNotFoundException("User not found with username " + username);
    }
}
