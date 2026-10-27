package com.studenterp.service;

import com.studenterp.dto.AuthResponse;
import com.studenterp.dto.LoginRequest;
import com.studenterp.dto.RegisterRequest;
import com.studenterp.entity.Role;
import com.studenterp.entity.User;
import com.studenterp.entity.UserRole;
import com.studenterp.repository.RoleRepository;
import com.studenterp.repository.UserRepository;
import com.studenterp.repository.UserRoleRepository;
import com.studenterp.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .active(true)
                .build();
        user = userRepository.save(user);

        Role.RoleType roleType = Role.RoleType.valueOf(request.getRole().toUpperCase());
        Role role = roleRepository.findByName(roleType)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        UserRole userRole = UserRole.builder().user(user).role(role).build();
        userRoleRepository.save(userRole);

        String token = jwtTokenProvider.generateToken(user.getUsername());
        return new AuthResponse(token, user.getUsername(), roleType.name());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        var roles = userRoleRepository.findByUserId(user.getId());
        String roleName = roles.isEmpty() ? "UNKNOWN" : roles.get(0).getRole().getName().name();

        return new AuthResponse(token, user.getUsername(), roleName);
    }
}
