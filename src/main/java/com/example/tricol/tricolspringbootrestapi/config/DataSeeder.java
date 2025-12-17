package com.example.tricol.tricolspringbootrestapi.config;

import com.example.tricol.tricolspringbootrestapi.model.UserApp;
import com.example.tricol.tricolspringbootrestapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin0").isEmpty()) {
            UserApp admin = new UserApp();
            admin.setUsername("admin0");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setEnabled(true);
            
            userRepository.save(admin);
        }
    }
}
