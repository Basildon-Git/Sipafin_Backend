package com.basiltech.sipafin.controller;

import com.basiltech.sipafin.dto.UserDtos;
import com.basiltech.sipafin.model.UserRole;
import com.basiltech.sipafin.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

   // @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @GetMapping("/getAll")
    public List<UserDtos.UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    //@PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @GetMapping("/search")
    public List<UserDtos.UserResponse> searchUsers(@RequestParam String keyword) {
        return userService.searchUsers(keyword);
    }

    //@PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @GetMapping("/role/{role}")
    public List<UserDtos.UserResponse> getUsersByRole(@PathVariable UserRole role) {
        return userService.getUsersByRole(role);
    }

   // @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @GetMapping("/status/{enabled}")
    public List<UserDtos.UserResponse> getUsersByEnabled(@PathVariable boolean enabled) {
        return userService.getUsersByEnabled(enabled);
    }

    //@PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @GetMapping("/{id}")
    public UserDtos.UserResponse getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    //@PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    public UserDtos.UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDtos.UpdateUserRequest request
    ) {
        return userService.updateUser(id, request);
    }

    @PostMapping("/change-password")
    public void changePassword(
            Authentication authentication,
            @Valid @RequestBody UserDtos.ChangePasswordRequest request
    ) {
        userService.changePassword(authentication.getName(), request);
    }

    //@PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/{id}/reset-password")
    public void resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody UserDtos.ResetPasswordRequest request
    ) {
        userService.resetPassword(id, request);
    }

   // @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}/enable")
    public void enableUser(@PathVariable Long id) {
        userService.enableUser(id);
    }

    //@PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}/disable")
    public void disableUser(@PathVariable Long id) {
        userService.disableUser(id);
    }
}