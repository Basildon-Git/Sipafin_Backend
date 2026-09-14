package com.basiltech.sipafin.service;

import com.basiltech.sipafin.dto.UserDtos;
import com.basiltech.sipafin.model.UserRole;

import java.util.List;

public interface UserService {

    List<UserDtos.UserResponse> getAllUsers();

    List<UserDtos.UserResponse> searchUsers(String keyword);

    List<UserDtos.UserResponse> getUsersByRole(UserRole role);

    List<UserDtos.UserResponse> getUsersByEnabled(boolean enabled);

    UserDtos.UserResponse getUserById(Long id);

    UserDtos.UserResponse updateUser(Long id, UserDtos.UpdateUserRequest request);

    void changePassword(String username, UserDtos.ChangePasswordRequest request);

    void resetPassword(Long id, UserDtos.ResetPasswordRequest request);

    void enableUser(Long id);

    void disableUser(Long id);
}