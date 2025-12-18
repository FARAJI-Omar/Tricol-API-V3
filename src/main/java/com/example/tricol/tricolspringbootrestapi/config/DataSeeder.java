package com.example.tricol.tricolspringbootrestapi.config;

import com.example.tricol.tricolspringbootrestapi.enums.RoleEnum;
import com.example.tricol.tricolspringbootrestapi.model.Permission;
import com.example.tricol.tricolspringbootrestapi.model.RoleApp;
import com.example.tricol.tricolspringbootrestapi.model.UserApp;
import com.example.tricol.tricolspringbootrestapi.repository.PermissionRepository;
import com.example.tricol.tricolspringbootrestapi.repository.RoleRepository;
import com.example.tricol.tricolspringbootrestapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        if (permissionRepository.count() == 0) {
            seedPermissions();
            seedAdminRole();
        }
        
        UserApp admin = userRepository.findByUsername("admin0").orElse(null);
        if (admin == null) {
            admin = new UserApp();
            admin.setUsername("admin0");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setEnabled(true);
            System.out.println(">>>admin0 created<<<");
        }
        
        if (admin.getRole() == null) {
            admin.setRole(roleRepository.findByName(RoleEnum.ADMIN).orElse(null));
            userRepository.save(admin);
            System.out.println(">>>admin0 role assigned<<<");
        }
    }

    private void seedPermissions() {
        String[] permissionCodes = {
                "FOURNISSEUR_CREATE", "FOURNISSEUR_READ", "FOURNISSEUR_UPDATE", "FOURNISSEUR_DELETE",
                "PRODUIT_CREATE", "PRODUIT_READ", "PRODUIT_UPDATE", "PRODUIT_DELETE",
                "COMMANDE_CREATE", "COMMANDE_READ", "COMMANDE_UPDATE", "COMMANDE_DELETE", "COMMANDE_VALIDATE", "COMMANDE_RECEIVE",
                "STOCK_READ", "STOCK_MOVEMENT_READ",
                "BON_SORTIE_CREATE", "BON_SORTIE_READ", "BON_SORTIE_VALIDATE", "BON_SORTIE_CANCEL",
                "ADMIN_USER_MANAGE", "ADMIN_ROLE_ASSIGN", "ADMIN_PERMISSION_MANAGE", "ADMIN_AUDIT_READ"
        };

        for (String code : permissionCodes) {
            Permission permission = new Permission();
            permission.setCode(code);
            permissionRepository.save(permission);
        }
    }

    private void seedAdminRole() {
        RoleApp adminRole = new RoleApp();
        adminRole.setName(RoleEnum.ADMIN);
        
        Set<Permission> allPermissions = new HashSet<>(permissionRepository.findAll());
        adminRole.setPermissions(allPermissions);
        
        roleRepository.save(adminRole);
    }
}
