package dev.nilswitt.webmap.views.filters;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import dev.nilswitt.webmap.entities.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.util.function.Consumer;

public class UserFilter extends EntityFilter<User> {
    Logger logger = LogManager.getLogger(UnitFilter.class);

    public UserFilter(Consumer<Example<User>> filter) {
        super(filter);
    }

    ExampleMatcher buildMatcher() {
        return ExampleMatcher.matchingAll()
                .withMatcher("username", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("firstName", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("lastName", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("email", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withIgnorePaths("isEnabled", "isLocked", "password", "securityGroups", "id");
    }

    @Override
    public Component getComponent(String columnKey) {
        switch (columnKey) {
            case "username" -> {
                TextField usernameField = new TextField();
                usernameField.setClearButtonVisible(true);
                usernameField.setValueChangeMode(ValueChangeMode.EAGER);
                usernameField.addValueChangeListener(event -> {
                    getEntityProbe().setUsername(event.getValue().isEmpty() ? null : event.getValue());
                    update();
                });
                return usernameField;
            }
            case "firstName" -> {
                TextField firstNameField = new TextField();
                firstNameField.setClearButtonVisible(true);
                firstNameField.setValueChangeMode(ValueChangeMode.EAGER);
                firstNameField.addValueChangeListener(event -> {
                    getEntityProbe().setFirstName(event.getValue().isEmpty() ? null : event.getValue());
                    update();
                });
                return firstNameField;
            }
            case "lastName" -> {
                TextField lastNameField = new TextField();
                lastNameField.setClearButtonVisible(true);
                lastNameField.setValueChangeMode(ValueChangeMode.EAGER);
                lastNameField.addValueChangeListener(event -> {
                    getEntityProbe().setLastName(event.getValue().isEmpty() ? null : event.getValue());
                    update();
                });
                return lastNameField;
            }
            case "email" -> {
                TextField emailField = new TextField();
                emailField.setClearButtonVisible(true);
                emailField.setValueChangeMode(ValueChangeMode.EAGER);
                emailField.addValueChangeListener(event -> {
                    getEntityProbe().setEmail(event.getValue().isEmpty() ? null : event.getValue());
                    update();
                    logger.info("Filtering by email: {}", getEntityProbe().getEmail());
                });
                return emailField;
            }
            default -> {
                return new H6("");
            }
        }
    }
}
