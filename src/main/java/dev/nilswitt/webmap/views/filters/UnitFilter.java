package dev.nilswitt.webmap.views.filters;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import dev.nilswitt.webmap.entities.Unit;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.util.function.Consumer;

public class UnitFilter extends  EntityFilter<Unit> {

    private boolean ignoreStatus = false;

    public enum Columns {
        NAME,
        STATUS

    }

    public UnitFilter(Consumer<Example<Unit>> filter) {
        super(filter);
    }

    ExampleMatcher buildMatcher() {
        ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withIgnorePaths("id", "speakRequest", "position.latitude", "position.longitude", "position.altitude","icon");
        if (!ignoreStatus) {
            matcher = matcher.withMatcher("status", ExampleMatcher.GenericPropertyMatchers.exact());
        }else {
            matcher = matcher.withIgnorePaths("status");
        }
        return matcher;
    }

    @Override
    public Component getComponent(String columnKey) {
        switch (columnKey) {
            case "NAME":
                TextField name = new TextField();
                name.setClearButtonVisible(true);
                name.setValueChangeMode(ValueChangeMode.EAGER);
                name.addValueChangeListener(e -> {
                    getEntityProbe().setName(e.getValue());
                    update();
                });
                return name;
            case "STATUS":
                IntegerField statusField = new IntegerField();
                statusField.setClearButtonVisible(true);
                statusField.setValueChangeMode(ValueChangeMode.EAGER);
                statusField.addValueChangeListener(e -> {
                    if (e.getValue() == null) {
                        ignoreStatus = true;
                    } else {
                        ignoreStatus = false;
                        getEntityProbe().setStatus(e.getValue());
                    }
                    update();
                });
                return statusField;
        }
        return new H6("");
    }
}
