package dev.nilswitt.webmap.views.components;

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import dev.nilswitt.webmap.entities.MapBaseLayer;

import java.util.function.Consumer;

public class MapBaseLayerEditDialog extends Dialog {

    private MapBaseLayer mapBaseLayer = new MapBaseLayer();
    private final Binder<MapBaseLayer> binder = new Binder<>(MapBaseLayer.class);
    private final Consumer<MapBaseLayer> editCallback;

    private final TextField nameField = new TextField("Name");
    private final TextField urlField = new TextField("Url");


    public MapBaseLayerEditDialog(Consumer<MapBaseLayer> editCallback) {
        this.editCallback = editCallback;
        this.setModality(ModalityMode.STRICT);
        this.setCloseOnOutsideClick(false);
        this.setHeaderTitle("Edit Base Layer");

        this.binder.bind(nameField, MapBaseLayer::getName, MapBaseLayer::setName);
        this.binder.bind(urlField, MapBaseLayer::getUrl, MapBaseLayer::setUrl);

        this.nameField.setRequired(true);

        FormLayout formLayout = new FormLayout();
        formLayout.setAutoResponsive(true);
        formLayout.addFormRow(this.nameField);
        formLayout.addFormRow(this.urlField);


        Button saveButton = new Button("Save", event -> {
            if (this.mapBaseLayer == null) {
                this.mapBaseLayer = new MapBaseLayer();
            }
            if (this.binder.writeBeanIfValid(mapBaseLayer)) {
                if (this.editCallback != null) {
                    this.editCallback.accept(mapBaseLayer);
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

    public void open(MapBaseLayer mapBaseLayer) {
        this.mapBaseLayer = mapBaseLayer;
        this.binder.readBean(mapBaseLayer);
        if (mapBaseLayer == null) {
            this.setHeaderTitle("Create Base Layer");

        } else {
            this.setHeaderTitle("Edit Base Layer: " + mapBaseLayer.getName());
        }
        super.open();
    }
}
