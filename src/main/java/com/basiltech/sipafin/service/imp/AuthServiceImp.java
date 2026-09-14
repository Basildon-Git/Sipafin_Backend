package com.basiltech.sipafin.service.imp;

import com.basiltech.sipafin.dto.UserDtos;
import com.basiltech.sipafin.model.Branch;
import com.basiltech.sipafin.model.UserAccount;
import com.basiltech.sipafin.model.UserRole;
import com.basiltech.sipafin.repository.BranchRepository;
import com.basiltech.sipafin.repository.UserAccountRepository;
import com.basiltech.sipafin.service.AuthService;
import com.basiltech.sipafin.web.ConflictException;
import com.basiltech.sipafin.web.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthServiceImp implements AuthService {

    private static final long ACCESS_TOKEN_EXPIRY_SECONDS = 60L * 60L * 8L;

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final PasswordEncoder passwordEncoder;
    private final UserAccountRepository userAccountRepository;
    private final BranchRepository branchRepository;

    @Override
    public UserDtos.AuthResponse login(UserDtos.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserAccount userAccount = userAccountRepository.findByUsername(request.username())
                .orElseThrow(() -> new NotFoundException("User not found"));

        String token = generateToken(userAccount);

        return new UserDtos.AuthResponse(
                "Bearer",
                token,
                ACCESS_TOKEN_EXPIRY_SECONDS,
                toUserResponse(userAccount)
        );
    }

    @Override
    @Transactional
    public UserDtos.AuthResponse register(UserDtos.RegisterUserRequest request) {
        if (userAccountRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username already exists");
        }

        Branch branch = resolveBranch(request.branchId());

        if (request.role() == UserRole.BRANCH_USER && branch == null) {
            throw new IllegalArgumentException("Branch is required for branch users");
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setFullName(request.fullName());
        userAccount.setUsername(request.username());
        userAccount.setPasswordHash(passwordEncoder.encode(request.password()));
        userAccount.setRole(request.role());
        userAccount.setBranch(branch);
        userAccount.setEnabled(true);

        UserAccount savedUser = userAccountRepository.save(userAccount);

        String token = generateToken(savedUser);

        return new UserDtos.AuthResponse(
                "Bearer",
                token,
                ACCESS_TOKEN_EXPIRY_SECONDS,
                toUserResponse(savedUser)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserDtos.UserResponse profile(String username) {
        UserAccount userAccount = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return toUserResponse(userAccount);
    }

    @Override
    public String generateToken(UserAccount userAccount) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(ACCESS_TOKEN_EXPIRY_SECONDS);

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer("sipafin")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(userAccount.getUsername())
                .claim("userId", userAccount.getId())
                .claim("fullName", userAccount.getFullName())
                .claim("username", userAccount.getUsername())
                .claim("role", userAccount.getRole().name());

        if (userAccount.getBranch() != null) {
            claimsBuilder.claim("branchId", userAccount.getBranch().getId());
        }

        JwtClaimsSet claims = claimsBuilder.build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
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