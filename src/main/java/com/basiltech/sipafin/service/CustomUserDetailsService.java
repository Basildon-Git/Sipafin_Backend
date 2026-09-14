package com.basiltech.sipafin.service;

import com.basiltech.sipafin.model.UserAccount;
import com.basiltech.sipafin.repository.UserAccountRepository;
import com.basiltech.sipafin.web.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserAccount userAccount = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return User.builder()
                .username(userAccount.getUsername())
                .password(userAccount.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority(userAccount.getRole().name())))
                .disabled(!userAccount.isEnabled())
                .build();
    }
}