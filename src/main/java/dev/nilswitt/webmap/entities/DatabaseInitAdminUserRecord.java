package dev.nilswitt.webmap.entities;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record DatabaseInitAdminUserRecord(@Value("${application.admin.create:false}") String create,
                                          @Value("${application.admin.force:false}") String force,
                                          @Value("${application.admin.username:admin}") String username,
                                          @Value("${application.admin.password:admin}") String password) {
}