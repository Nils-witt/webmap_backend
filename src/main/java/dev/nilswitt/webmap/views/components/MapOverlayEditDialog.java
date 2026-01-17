package dev.nilswitt.webmap.views.components;

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import dev.nilswitt.webmap.entities.MapOverlay;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.repositories.SecurityGroupRepository;
import dev.nilswitt.webmap.records.OverlayConfig;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

public class MapOverlayEditDialog extends Dialog {

    private MapOverlay mapOverlay = new MapOverlay();
    private final Binder<MapOverlay> binder = new Binder<>(MapOverlay.class);
    private final Consumer<MapOverlay> editCallback;

    private final TextField nameField = new TextField("Name");
    private final TextField baseUrlField = new TextField("Base Url");
    private final TextField basePathField = new TextField("Base Path");
    private final TextField tilePatternField = new TextField("Tile Pattern");
    private final IntegerField layerVersionField = new IntegerField("Version");
    private final MultiSelectComboBox<SecurityGroup> securityGroupsField = new MultiSelectComboBox<>("Security Groups");
    private final SecurityGroupRepository securityGroupRepository;


    public MapOverlayEditDialog(Consumer<MapOverlay> editCallback, SecurityGroupRepository securityGroupRepository) {
        this.securityGroupRepository = securityGroupRepository;
        this.editCallback = editCallback;
        this.setModality(ModalityMode.STRICT);
        this.setCloseOnOutsideClick(false);
        this.setHeaderTitle("Edit Overlay");
        this.setWidth("100%");
        this.binder.bind(nameField, MapOverlay::getName, MapOverlay::setName);
        this.binder.bind(baseUrlField, MapOverlay::getBaseUrl, MapOverlay::setBaseUrl);
        this.binder.bind(basePathField, MapOverlay::getBasePath, MapOverlay::setBasePath);
        this.binder.bind(tilePatternField, MapOverlay::getTilePathPattern, MapOverlay::setTilePathPattern);
        this.binder.bind(layerVersionField, MapOverlay::getLayerVersion, MapOverlay::setLayerVersion);
        this.binder.bind(securityGroupsField, MapOverlay::getSecurityGroups, MapOverlay::setSecurityGroups);

        this.securityGroupsField.setItemLabelGenerator(SecurityGroup::getName);
        this.securityGroupsField.setItems(securityGroupRepository.findAll());

        this.nameField.setRequired(true);

        this.layerVersionField.setMin(0);
        this.layerVersionField.setStepButtonsVisible(true);
        this.layerVersionField.setStep(1);

        FormLayout formLayout = new FormLayout();
        formLayout.setAutoResponsive(true);
        formLayout.addFormRow(this.nameField);
        formLayout.addFormRow(this.baseUrlField);
        formLayout.addFormRow(this.basePathField);
        formLayout.addFormRow(this.tilePatternField);
        formLayout.addFormRow(this.layerVersionField);
        formLayout.addFormRow(this.securityGroupsField);


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

        Button cancelButton = new Button("Cancel", event -> close());

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
