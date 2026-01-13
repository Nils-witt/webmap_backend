package dev.nilswitt.webmap;

import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.SecurityGroupRepository;
import dev.nilswitt.webmap.entities.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Optional;

@Configuration
class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(UserRepository repository, SecurityGroupRepository securityGroupRepository) {
        if (repository.count() == 0) {
            log.info("Preloading " + repository.save(new User("Demo.user", "demo@test.local", "Demo", "User")));
        }

        Optional<SecurityGroup> adminGroupOpt = securityGroupRepository.findByName("SuperAdmins");
        if (adminGroupOpt.isEmpty()) {
            SecurityGroup adminGroup = new SecurityGroup("SuperAdmins", new HashSet<>(SecurityGroup.availableRoles()));
            securityGroupRepository.save(adminGroup);
            log.info("Preloading " + adminGroup);
        } else {
            adminGroupOpt.get().setRoles(new HashSet<>(SecurityGroup.availableRoles()));
            securityGroupRepository.save(adminGroupOpt.get());
        }


        return args -> {
        };
    }
}