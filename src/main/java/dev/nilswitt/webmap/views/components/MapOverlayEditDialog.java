package dev.nilswitt.webmap.views.components;

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import dev.nilswitt.webmap.entities.MapOverlay;
import dev.nilswitt.webmap.entities.User;

import java.util.function.Consumer;

public class MapOverlayEditDialog extends Dialog {

    private MapOverlay mapOverlay = new MapOverlay();
    private final Binder<MapOverlay> binder = new Binder<>(MapOverlay.class);
    private final Consumer<MapOverlay> editCallback;

    private final TextField nameField = new TextField("Name");


    public MapOverlayEditDialog(Consumer<MapOverlay> editCallback) {
        this.editCallback = editCallback;
        this.setModality(ModalityMode.STRICT);
        this.setCloseOnOutsideClick(false);
        this.setHeaderTitle("Edit Overlay");

        this.binder.bind(nameField, MapOverlay::getName, MapOverlay::setName);

        this.nameField.setRequired(true);

        FormLayout formLayout = new FormLayout();
        formLayout.setAutoResponsive(true);
        formLayout.addFormRow(this.nameField);


        Button saveButton = new Button("Save", event -> {
            if (this.mapOverlay == null) {
                this.mapOverlay = new MapOverlay();
            }
            if (this.binder.writeBeanIfValid(mapOverlay)) {
                if (this.editCallback != null) {
                    this.editCallback.accept(mapOverlay);
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

    public void open(MapOverlay mapOverlay) {
        this.mapOverlay = mapOverlay;
        this.binder.readBean(mapOverlay);
        if (mapOverlay == null) {
            this.setHeaderTitle("Create Overlay");

        } else {
            this.setHeaderTitle("Edit Overlay: " + mapOverlay.getName());
        }
        super.open();
    }
}
