package com.white.backend;

import com.white.backend.authentication.entity.User;
import com.white.backend.authentication.repository.UserRepository;
import com.white.backend.shared.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@RequiredArgsConstructor
public class BackendApplication implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        userRepository.save(User.builder()
                .username("user")
                .password(passwordEncoder.encode("password"))
                .email("")
                .name("User")
                .avatar("")
                .role(Role.USER)
                .build());
    }
}
