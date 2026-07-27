package com.cafe.config;

import com.cafe.entity.Role;
import com.cafe.entity.User;
import com.cafe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Creates a default ADMIN account on first startup so you have a way in.
// CHANGE THIS PASSWORD before deploying anywhere real.
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail("admin@cafe.com")) {
            return;
        }

        User admin = User.builder()
                .name("Default Admin")
                .email("admin@cafe.com")
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        userRepository.save(admin);
        System.out.println(">>> Seeded default admin: admin@cafe.com / Admin@123 (change this password!)");
    }
}
