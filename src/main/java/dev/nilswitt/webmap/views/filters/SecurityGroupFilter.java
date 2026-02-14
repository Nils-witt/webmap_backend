package dev.nilswitt.webmap.views.filters;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import dev.nilswitt.webmap.entities.SecurityGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.util.function.Consumer;

public class SecurityGroupFilter extends EntityFilter<SecurityGroup> {

    public enum Columns {
        NAME
    }

    public SecurityGroupFilter(Consumer<Example<SecurityGroup>> filter) {
        super(filter);
    }

    ExampleMatcher buildMatcher() {
        return ExampleMatcher.matchingAll()
                .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withIgnorePaths("id", "users", "roles", "overlays", "ssoGroupName");
    }

    @Override
    public Component getComponent(String columnKey) {
        switch (columnKey) {
            case "NAME" -> {
                TextField nameField = new TextField();
                nameField.setClearButtonVisible(true);
                nameField.setValueChangeMode(ValueChangeMode.EAGER);
                nameField.addValueChangeListener(event -> {
                    getEntityProbe().setName(event.getValue().isEmpty() ? null : event.getValue());
                    update();
                });
                return nameField;
            }
            default -> {
                return new H6("");
            }
        }
    }
}
