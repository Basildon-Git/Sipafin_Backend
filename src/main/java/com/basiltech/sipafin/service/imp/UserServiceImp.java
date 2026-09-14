package com.basiltech.sipafin.service.imp;

import com.basiltech.sipafin.dto.UserDtos;
import com.basiltech.sipafin.model.Branch;
import com.basiltech.sipafin.model.UserAccount;
import com.basiltech.sipafin.model.UserRole;
import com.basiltech.sipafin.repository.BranchRepository;
import com.basiltech.sipafin.repository.UserAccountRepository;
import com.basiltech.sipafin.service.UserService;
import com.basiltech.sipafin.web.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService {

    private final UserAccountRepository userAccountRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UserDtos.UserResponse> getAllUsers() {
        return userAccountRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(UserAccount::getId).reversed())
                .map(this::toUserResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDtos.UserResponse> searchUsers(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllUsers();
        }

        return userAccountRepository
                .findByFullNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(keyword, keyword)
                .stream()
                .sorted(Comparator.comparing(UserAccount::getId).reversed())
                .map(this::toUserResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDtos.UserResponse> getUsersByRole(UserRole role) {
        return userAccountRepository.findByRole(role)
                .stream()
                .sorted(Comparator.comparing(UserAccount::getId).reversed())
                .map(this::toUserResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDtos.UserResponse> getUsersByEnabled(boolean enabled) {
        return userAccountRepository.findByEnabled(enabled)
                .stream()
                .sorted(Comparator.comparing(UserAccount::getId).reversed())
                .map(this::toUserResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDtos.UserResponse getUserById(Long id) {
        UserAccount userAccount = findUser(id);
        return toUserResponse(userAccount);
    }

    @Override
    @Transactional
    public UserDtos.UserResponse updateUser(Long id, UserDtos.UpdateUserRequest request) {
        UserAccount userAccount = findUser(id);

        Branch branch = resolveBranch(request.branchId());

        if (request.role() == UserRole.BRANCH_USER && branch == null) {
            throw new IllegalArgumentException("Branch is required for branch users");
        }

        userAccount.setFullName(request.fullName());
        userAccount.setRole(request.role());
        userAccount.setBranch(branch);
        userAccount.setEnabled(request.enabled());

        UserAccount savedUser = userAccountRepository.save(userAccount);

        return toUserResponse(savedUser);
    }

    @Override
    @Transactional
    public void changePassword(String username, UserDtos.ChangePasswordRequest request) {
        UserAccount userAccount = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), userAccount.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        userAccount.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userAccountRepository.save(userAccount);
    }

    @Override
    @Transactional
    public void resetPassword(Long id, UserDtos.ResetPasswordRequest request) {
        UserAccount userAccount = findUser(id);
        userAccount.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userAccountRepository.save(userAccount);
    }

    @Override
    @Transactional
    public void enableUser(Long id) {
        UserAccount userAccount = findUser(id);
        userAccount.setEnabled(true);
        userAccountRepository.save(userAccount);
    }

    @Override
    @Transactional
    public void disableUser(Long id) {
        UserAccount userAccount = findUser(id);
        userAccount.setEnabled(false);
        userAccountRepository.save(userAccount);
    }

    private UserAccount findUser(Long id) {
        return userAccountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private Branch resolveBranch(Long branchId) {
        if (branchId == null || branchId <= 0) {
            return null;
        }

        return branchRepository.findById(branchId)
                .orElseThrow(() -> new NotFoundException("Branch not found"));
    }

    private UserDtos.UserResponse toUserResponse(UserAccount userAccount) {
        Branch branch = userAccount.getBranch();

        return new UserDtos.UserResponse(
                userAccount.getId(),
                userAccount.getFullName(),
                userAccount.getUsername(),
                userAccount.getRole(),
                branch == null ? null : branch.getId(),
                branch == null ? null : branch.getName(),
                userAccount.isEnabled(),
                userAccount.getCreatedAt(),
                userAccount.getUpdatedAt()
        );
    }
}