package dev.nilswitt.webmap.views.components;

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import dev.nilswitt.webmap.entities.MapGroup;
import dev.nilswitt.webmap.entities.MapItem;
import dev.nilswitt.webmap.entities.repositories.MapGroupRepository;

import java.util.function.Consumer;

public class MapItemEditDialog extends Dialog {

    private MapItem mapItem = new MapItem();
    private final Binder<MapItem> binder = new Binder<>(MapItem.class);
    private final Consumer<MapItem> editCallback;

    private final TextField nameField = new TextField("Name");
    private final NumberField latitudeField = new NumberField("Latitude");
    private final NumberField longitudeField = new NumberField("Longitude");
    private final NumberField altitudeField = new NumberField("Altitude");
    private final ComboBox<MapGroup> mapGroupComboBox = new ComboBox<>("Map Group");

    public MapItemEditDialog(Consumer<MapItem> editCallback, MapGroupRepository mapGroupRepository) {
        this.editCallback = editCallback;
        this.setModality(ModalityMode.STRICT);
        this.setCloseOnOutsideClick(false);
        this.setHeaderTitle("Edit Overlay");

        this.mapGroupComboBox.setItemLabelGenerator(MapGroup::getName);
        this.mapGroupComboBox.setItems(mapGroupRepository.findAll());

        this.binder.bind(nameField, MapItem::getName, MapItem::setName);
        this.binder.bind(mapGroupComboBox, MapItem::getMapGroup, MapItem::setMapGroup);

        this.nameField.setRequired(true);
        binder.forField(latitudeField)
                .withNullRepresentation(0.0)
                .bind(unit -> unit.getPosition().getLatitude(), (unit, value) -> unit.getPosition().setLatitude(value != null ? value : 0.0));
        binder.forField(longitudeField)
                .withNullRepresentation(0.0)
                .bind(unit -> unit.getPosition().getLongitude(), (unit, value) -> unit.getPosition().setLongitude(value != null ? value : 0.0));
        binder.forField(altitudeField)
                .withNullRepresentation(0.0)
                .bind(unit -> unit.getPosition().getAltitude(), (unit, value) -> unit.getPosition().setAltitude(value != null ? value : 0.0));

        FormLayout formLayout = new FormLayout();
        formLayout.setAutoResponsive(true);
        formLayout.addFormRow(this.nameField);
        formLayout.addFormRow(this.mapGroupComboBox);
        formLayout.addFormRow(this.latitudeField, this.longitudeField);
        formLayout.addFormRow(this.altitudeField);


        Button saveButton = new Button("Save", event -> {
            if (this.mapItem == null) {
                this.mapItem = new MapItem();
            }
            if (this.binder.writeBeanIfValid(mapItem)) {
                if (this.editCallback != null) {
                    this.editCallback.accept(mapItem);
                }
                this.close();

            } else {
                this.setError("Please correct the errors before saving.");
            }
        });
        saveButton.setThemeVariant(ButtonVariant.LUMO_PRIMARY, true);

        Button cancelButton = new Button("Cancel", event -> {
            close();
        });
        cancelButton.setThemeVariant(ButtonVariant.LUMO_WARNING, true);
        this.add(formLayout);
        this.getFooter().add(saveButton);
        this.getFooter().add(cancelButton);

    }

    public void setError(String message) {
        // Implementation for setting error message
    }

    public void open(MapItem mapItem) {
        this.mapItem = mapItem;
        this.binder.readBean(mapItem);
        if (mapItem == null) {
            this.setHeaderTitle("Create Item");

        } else {
            this.setHeaderTitle("Edit Item: " + mapItem.getName());
        }
        super.open();
    }
}
