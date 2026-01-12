package dev.nilswitt.webmap;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(UserRepository repository) {

        if(repository.count() > 0) {
            log.info("Database already initialized");
            return args -> {};
        }
        return args -> {

            log.info("Preloading " + repository.save(new User("Demo.user", "demo@test.local","Demo", "User")));
        };
    }
}