package dev.nilswitt.webmap.views.filters;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import dev.nilswitt.webmap.entities.MapGroup;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.util.function.Consumer;

public class MapGroupFilter extends EntityFilter<MapGroup> {

    public enum Columns {
        NAME
    }

    public MapGroupFilter(Consumer<Example<MapGroup>> filter) {
        super(filter);
    }

    ExampleMatcher buildMatcher() {
        return ExampleMatcher.matchingAll()
                .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withIgnorePaths("id", "mapItems", "mapOverlays");
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
