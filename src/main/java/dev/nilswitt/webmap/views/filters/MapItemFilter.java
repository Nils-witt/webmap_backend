package dev.nilswitt.webmap.views.filters;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import dev.nilswitt.webmap.entities.MapGroup;
import dev.nilswitt.webmap.entities.MapItem;
import dev.nilswitt.webmap.entities.repositories.MapGroupRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.util.function.Consumer;

public class MapItemFilter extends EntityFilter<MapItem> {

    private final MapGroupRepository mapGroupRepository;
    public enum Columns {
        NAME
    }

    public MapItemFilter(Consumer<Example<MapItem>> filter, MapGroupRepository mapGroupRepository) {
        super(filter);
        this.mapGroupRepository = mapGroupRepository;
    }

    ExampleMatcher buildMatcher() {
        return ExampleMatcher.matchingAll()
                .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withIgnorePaths("id");
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
            case "mapGroup" -> {
                ComboBox<MapGroup> mapGroupField = new ComboBox<>();
                mapGroupField.setItemLabelGenerator(MapGroup::getName);
                mapGroupField.setItems(this.mapGroupRepository.findAll());
                mapGroupField.setClearButtonVisible(true);
                mapGroupField.addValueChangeListener(event -> {
                    getEntityProbe().setMapGroup(event.getValue());
                    update();
                });
                return mapGroupField;
            }
            default -> {
                return new H6("");
            }
        }
    }
}
