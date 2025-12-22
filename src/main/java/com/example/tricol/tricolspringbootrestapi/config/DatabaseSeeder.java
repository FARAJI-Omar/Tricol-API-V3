package com.example.tricol.tricolspringbootrestapi.config;

import com.example.tricol.tricolspringbootrestapi.enums.PermissionEnum;
import com.example.tricol.tricolspringbootrestapi.enums.RoleEnum;
import com.example.tricol.tricolspringbootrestapi.model.Permission;
import com.example.tricol.tricolspringbootrestapi.model.RoleApp;
import com.example.tricol.tricolspringbootrestapi.model.UserApp;
import com.example.tricol.tricolspringbootrestapi.model.UserPermission;
import com.example.tricol.tricolspringbootrestapi.repository.PermissionRepository;
import com.example.tricol.tricolspringbootrestapi.repository.RoleRepository;
import com.example.tricol.tricolspringbootrestapi.repository.UserPermissionRepository;
import com.example.tricol.tricolspringbootrestapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        System.out.println("\n========================================");
        System.out.println("🌱 Starting Database Seeding");
        System.out.println("========================================\n");

        seedPermissions();
        seedRoles();
        seedAdmin();

        System.out.println("\n========================================");
        System.out.println("✅ Database Seeding Complete!");
        System.out.println("========================================\n");
    }

    private void seedPermissions() {
        if (permissionRepository.count() > 0) {
            System.out.println("✅ Permissions already exist (" + permissionRepository.count() + ")");
            return;
        }

        System.out.println("📝 Seeding permissions...");

        int count = 0;
        for (PermissionEnum permEnum : PermissionEnum.values()) {
            Permission permission = Permission.builder()
                    .name(permEnum)
                    .description(getPermissionDescription(permEnum))
                    .resource(getResourceFromPermission(permEnum))
                    .action(getActionFromPermission(permEnum))
                    .build();
            permissionRepository.save(permission);
            count++;
        }

        System.out.println("✅ Created " + count + " permissions\n");
    }

    private void seedRoles() {
        if (roleRepository.count() > 0) {
            System.out.println("✅ Roles already exist (" + roleRepository.count() + ")");
            return;
        }

        System.out.println("👥 Seeding roles...");

        // Create ADMIN role with all permissions
        createRoleWithPermissions(
                RoleEnum.ADMIN,
                "Administrator with full system access",
                PermissionEnum.values() // All permissions
        );

        // Create RESPONSABLE_ACHATS role
        createRoleWithPermissions(
                RoleEnum.RESPONSABLE_ACHATS,
                "Purchase manager",
                PermissionEnum.FOURNISSEUR_CREATE, PermissionEnum.FOURNISSEUR_READ,
                PermissionEnum.FOURNISSEUR_UPDATE, PermissionEnum.FOURNISSEUR_DELETE,
                PermissionEnum.PRODUIT_READ,
                PermissionEnum.COMMANDE_CREATE, PermissionEnum.COMMANDE_READ,
                PermissionEnum.COMMANDE_UPDATE, PermissionEnum.COMMANDE_VALIDATE,
                PermissionEnum.COMMANDE_CANCEL,
                PermissionEnum.STOCK_READ, PermissionEnum.STOCK_HISTORIQUE
        );

        // Create MAGASINIER role
        createRoleWithPermissions(
                RoleEnum.MAGASINIER,
                "Warehouse manager",
                PermissionEnum.PRODUIT_READ,
                PermissionEnum.COMMANDE_READ, PermissionEnum.COMMANDE_RECEIVE,
                PermissionEnum.STOCK_READ, PermissionEnum.STOCK_VALORISATION,
                PermissionEnum.STOCK_HISTORIQUE,
                PermissionEnum.BON_SORTIE_READ
        );

        // Create CHEF_ATELIER role
        createRoleWithPermissions(
                RoleEnum.CHEF_ATELIER,
                "Workshop manager",
                PermissionEnum.PRODUIT_READ,
                PermissionEnum.STOCK_READ,
                PermissionEnum.BON_SORTIE_CREATE, PermissionEnum.BON_SORTIE_READ,
                PermissionEnum.BON_SORTIE_VALIDATE, PermissionEnum.BON_SORTIE_CANCEL
        );

        System.out.println("✅ Created 4 roles with permissions\n");
    }

    private RoleApp createRoleWithPermissions(RoleEnum roleName, String description, PermissionEnum... permissionEnums) {
        RoleApp role = RoleApp.builder()
                .name(roleName)
                .description(description)
                .permissions(new HashSet<>())
                .build();

        // Add permissions to role
        for (PermissionEnum permEnum : permissionEnums) {
            Permission permission = permissionRepository.findByName(permEnum)
                    .orElseThrow(() -> new RuntimeException("Permission " + permEnum + " not found"));
            role.getPermissions().add(permission);
        }

        role = roleRepository.save(role);
        System.out.println("   ✓ " + roleName + " → " + permissionEnums.length + " permissions");
        return role;
    }

    private void seedAdmin() {
        if (userRepository.findByUsername("admin0").isPresent()) {
            System.out.println("✅ admin0 already exists");
            return;
        }

        System.out.println("👤 Creating admin0 user...");

        RoleApp adminRole = roleRepository.findByName(RoleEnum.ADMIN)
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

        UserApp admin = UserApp.builder()
                .username("admin0")
                .email("admin@tricol.com")
                .password(passwordEncoder.encode("123456"))
                .fullName("System Administrator")
                .enabled(true)
                .locked(false)
                .role(adminRole)
                .userPermissions(new HashSet<>())
                .build();

        admin = userRepository.save(admin);

        // Grant all permissions directly to admin user
        System.out.println("🔐 Granting all permissions to admin0...");
        int permCount = grantAllPermissionsToUser(admin);

        System.out.println("✅ admin0 created successfully!");
        System.out.println("   👤 Username: admin0");
        System.out.println("   📧 Email: admin@tricol.com");
        System.out.println("   🔑 Password: 123456");
        System.out.println("   🎭 Role: ADMIN");
        System.out.println("   🔐 Direct Permissions: " + permCount + " permissions granted");
        System.out.println("   ✨ Total Access: Role permissions + Direct permissions = FULL ACCESS\n");
    }

    private int grantAllPermissionsToUser(UserApp user) {
        int count = 0;
        for (PermissionEnum permEnum : PermissionEnum.values()) {
            Permission permission = permissionRepository.findByName(permEnum)
                    .orElseThrow(() -> new RuntimeException("Permission " + permEnum + " not found"));

            UserPermission userPermission = UserPermission.builder()
                    .user(user)
                    .permission(permission)
                    .active(true)
                    .grantedBy(user.getId()) // Self-granted for initial admin
                    .build();

            userPermissionRepository.save(userPermission);
            count++;
        }
        return count;
    }

    private String getPermissionDescription(PermissionEnum perm) {
        String name = perm.name();
        if (name.endsWith("_CREATE")) return "Create " + getResource(name);
        if (name.endsWith("_READ")) return "Read/View " + getResource(name);
        if (name.endsWith("_UPDATE")) return "Update " + getResource(name);
        if (name.endsWith("_DELETE")) return "Delete " + getResource(name);
        if (name.endsWith("_APPROVE")) return "Approve " + getResource(name);
        return "Manage " + name.replace("_", " ");
    }

    private String getResourceFromPermission(PermissionEnum perm) {
        String name = perm.name();
        return getResource(name);
    }

    private String getActionFromPermission(PermissionEnum perm) {
        String name = perm.name();
        if (name.endsWith("_CREATE")) return "CREATE";
        if (name.endsWith("_READ")) return "READ";
        if (name.endsWith("_UPDATE")) return "UPDATE";
        if (name.endsWith("_DELETE")) return "DELETE";
        if (name.endsWith("_APPROVE")) return "APPROVE";
        return "MANAGE";
    }

    private String getResource(String permissionName) {
        if (permissionName.startsWith("FOURNISSEUR_")) return "SUPPLIER";
        if (permissionName.startsWith("PRODUIT_")) return "PRODUCT";
        if (permissionName.startsWith("COMMANDE_")) return "ORDER";
        if (permissionName.startsWith("STOCK_")) return "STOCK";
        if (permissionName.startsWith("BON_SORTIE_")) return "EXIT_SLIP";
        if (permissionName.startsWith("USER_")) return "USER";
        if (permissionName.startsWith("AUDIT_")) return "AUDIT";
        return "SYSTEM";
    }
}
