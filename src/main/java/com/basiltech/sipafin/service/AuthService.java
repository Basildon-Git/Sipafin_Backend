package com.basiltech.sipafin.service;

import com.basiltech.sipafin.dto.UserDtos;
import com.basiltech.sipafin.model.UserAccount;

public interface AuthService {

    UserDtos.AuthResponse login(UserDtos.LoginRequest request);

    UserDtos.AuthResponse register(UserDtos.RegisterUserRequest request);

    UserDtos.UserResponse profile(String username);

    String generateToken(UserAccount userAccount);
}