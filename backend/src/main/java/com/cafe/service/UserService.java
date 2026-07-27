package com.cafe.service;

import com.cafe.dto.CreateStaffRequest;
import com.cafe.entity.Role;
import com.cafe.entity.User;
import com.cafe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Admin-only: create WORKER or ADMIN accounts.
    // Regular USER accounts are created via public /api/auth/register instead.
    public User createStaff(CreateStaffRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (request.getRole() == Role.USER) {
            throw new IllegalArgumentException("Use /api/auth/register for USER accounts");
        }

        User staff = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(true)
                .build();

        return userRepository.save(staff);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User setEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEnabled(enabled);
        return userRepository.save(user);
    }
}
