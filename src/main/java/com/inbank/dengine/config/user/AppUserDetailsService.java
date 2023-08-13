package com.inbank.dengine.config.user;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class AppUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        if (username == null) return null;

        if (username.equals("inbank")) {
            return new AppUserDetails(username, "inbank@123", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
        } else if (username.equals("test-user")) {
            return new AppUserDetails(username, "test@123", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
        }
        throw new UsernameNotFoundException("User not Found");
    }

}
