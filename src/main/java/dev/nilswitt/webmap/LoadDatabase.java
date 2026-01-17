package dev.nilswitt.webmap;

import dev.nilswitt.webmap.entities.DatabaseInitAdminUserRecord;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.SecurityGroupRepository;
import dev.nilswitt.webmap.entities.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;

@Configuration
class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);


    @Bean
    CommandLineRunner initDatabase(UserRepository repository, SecurityGroupRepository securityGroupRepository, DatabaseInitAdminUserRecord adminUserRecord, PasswordEncoder passwordEncoder) {
        Optional<SecurityGroup> adminGroupOpt = securityGroupRepository.findByName("SuperAdmins");
        SecurityGroup adminGroup;

        if (adminGroupOpt.isEmpty()) {
            adminGroup = new SecurityGroup("SuperAdmins", new HashSet<>(SecurityGroup.availableRoles()));

            log.info("Preloading " + adminGroup);
        } else {
            adminGroup = adminGroupOpt.get();
            adminGroup.setRoles(new HashSet<>(SecurityGroup.availableRoles()));
        }
        securityGroupRepository.save(adminGroup);


        if (adminUserRecord.create().equalsIgnoreCase("true")) {
            Optional<User> adminUserOpt = repository.findByUsername(adminUserRecord.username());
            if (adminUserOpt.isEmpty()) {
                User adminUser = new User(adminUserRecord.username(), "admin@admin.local", "Admin", "Admin");
                adminUser.setPassword(passwordEncoder.encode(adminUserRecord.password()));
                adminUser.addSecurityGroup(adminGroup);
                repository.save(adminUser);
            } else {
                if (adminUserRecord.force().equalsIgnoreCase("true")) {
                    User adminUser = adminUserOpt.get();
                    adminUser.setPassword(passwordEncoder.encode(adminUserRecord.password()));
                    adminUser.addSecurityGroup(adminGroup);
                    repository.save(adminUser);
                }
            }
        }

        return args -> {
        };
    }
}