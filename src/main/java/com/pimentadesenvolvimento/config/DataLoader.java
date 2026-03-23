package com.pimentadesenvolvimento.config;

import com.pimentadesenvolvimento.domain.Role;
import com.pimentadesenvolvimento.domain.User;
import com.pimentadesenvolvimento.repository.RoleRepository;
import com.pimentadesenvolvimento.repository.UserRepository;
import com.pimentadesenvolvimento.security.SecurityRoles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;

    @Value("${app.admin.default-password}")
    private String defaultAdminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        log.info(Messages.DataLoader.START_LOADING);

        Role adminRole = roleRepository.findByName(SecurityRoles.NAME_ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(SecurityRoles.NAME_ROLE_ADMIN)));

        Role userRole = roleRepository.findByName(SecurityRoles.NAME_ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(SecurityRoles.NAME_ROLE_USER)));

        createAdminIfNotFound(adminRole, userRole);

        log.info(Messages.DataLoader.LOAD_FINISHED);
    }

    private void createAdminIfNotFound(Role adminRole, Role userRole) {
        String adminUsername = "admin";

        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            // Require explicit password configuration for admin user
            if (defaultAdminPassword == null || defaultAdminPassword.trim().isEmpty()) {
                throw new IllegalStateException(
                        "Default admin password must be configured via app.admin.default-password property or APP_ADMIN_DEFAULT_PASSWORD environment variable. " +
                                "Password must be at least 10 characters with uppercase, lowercase, number, and special character."
                );
            }

            User admin = new User();
            admin.setName("System Administrator");
            admin.setUsername(adminUsername);
            admin.setEmail("admin@template.com");
            admin.setPassword(encoder.encode(defaultAdminPassword));

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            roles.add(userRole);
            admin.setRoles(roles);

            userRepository.save(admin);
            log.warn("Admin user created with configured password. ENSURE this password is changed immediately in production!");
            log.info(Messages.DataLoader.ADMIN_CREATED);
        } else {
            log.info(Messages.DataLoader.ADMIN_EXISTS);
        }
    }
}