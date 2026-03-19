package dev.nilswitt.webmap.base.ui.views.components;

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep.LabelsPosition;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import dev.nilswitt.webmap.entities.Unit;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class UnitEditDialog extends Dialog {

    private Unit unit = new Unit();
    private final Binder<Unit> binder = new Binder<>(Unit.class);
    private final Consumer<Unit> editCallback;

    private final TextField nameField = new TextField("Name");
    private final NumberField latitudeField = new NumberField("Latitude");
    private final NumberField longitudeField = new NumberField("Longitude");
    private final NumberField altitudeField = new NumberField("Altitude");
    private final NumberField accuracyField = new NumberField("Accuracy");
    private final DateTimePicker posTimePicker = new DateTimePicker();

    public UnitEditDialog(Consumer<Unit> editCallback) {
        this.editCallback = editCallback;
        setModality(ModalityMode.STRICT);
        setCloseOnOutsideClick(false);
        setHeaderTitle("Edit Unit");

        configureFields();
        bindFields();

        FormLayout formLayout = new FormLayout();
        formLayout.addClassName("unit-form");
        formLayout.add(nameField, latitudeField, longitudeField, accuracyField, altitudeField, posTimePicker);
        formLayout.setResponsiveSteps(List.of(new ResponsiveStep("0", 1, LabelsPosition.ASIDE), new ResponsiveStep("600px", 2, LabelsPosition.ASIDE)));

        Button saveButton = new Button("Save", event -> {
            if (unit == null) {
                unit = new Unit();
            }
            if (binder.writeBeanIfValid(unit)) {
                if (editCallback != null) {
                    editCallback.accept(unit);
                }
                close();
            } else {
                setError("Please correct the errors before saving.");
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", event -> close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        add(formLayout);
        getFooter().add(saveButton, cancelButton);
    }

    private void configureFields() {
        nameField.setRequired(true);

        latitudeField.setMin(-90);
        latitudeField.setMax(90);
        longitudeField.setMin(-180);
        longitudeField.setMax(180);
    }

    private void bindFields() {
        binder.bind(nameField, Unit::getName, Unit::setName);
        binder.forField(latitudeField)
                .withNullRepresentation(0.0)
                .bind(unit -> unit.getPosition().getLatitude(), (unit, value) -> unit.getPosition().setLatitude(value != null ? value : 0.0));
        binder.forField(longitudeField)
                .withNullRepresentation(0.0)
                .bind(unit -> unit.getPosition().getLongitude(), (unit, value) -> unit.getPosition().setLongitude(value != null ? value : 0.0));
        binder.forField(altitudeField)
                .withNullRepresentation(0.0)
                .bind(unit -> unit.getPosition().getAltitude(), (unit, value) -> unit.getPosition().setAltitude(value != null ? value : 0.0));
        binder.forField(accuracyField).withNullRepresentation(0.0).bind(unit -> unit.getPosition().getAccuracy(), (unit, value) -> unit.getPosition().setAccuracy(value != null ? value : 0.0));
        binder.bind(posTimePicker, unit -> {
            if (unit.getPosition().getTimestamp() != null) {
                return unit.getPosition().getTimestamp().atZone(ZoneId.of("Europe/Berlin")).toLocalDateTime();
            } else {
                return null;
            }
        }, (unit, value) -> {
            assert value != null;
            unit.getPosition().setTimestamp(value.atZone(ZoneId.of("Europe/Berlin")).toInstant());
        });
    }

    public void setError(String message) {
        // TODO: add user-facing error message presentation
    }

    public void open(Unit unit) {
        this.unit = unit == null ? new Unit() : unit;
        binder.readBean(this.unit);
        if (unit == null) {
            setHeaderTitle("Create Unit");
        } else {
            setHeaderTitle("Edit Unit: " + unit.getName());
        }
        super.open();
    }
}

