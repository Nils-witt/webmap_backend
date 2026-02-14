package dev.nilswitt.webmap.views.components;

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import dev.nilswitt.webmap.entities.MapGroup;
import lombok.extern.log4j.Log4j2;

import java.util.function.Consumer;

@Log4j2
public class MapGroupEditDialog extends Dialog {

    private MapGroup securityGroup = null;
    private final Binder<MapGroup> binder = new Binder<>(MapGroup.class);
    private final Consumer<MapGroup> editCallback;

    private final TextField nameField = new TextField("Name");


    public MapGroupEditDialog(Consumer<MapGroup> editCallback) {
        this.editCallback = editCallback;
        this.setModality(ModalityMode.STRICT);
        this.setCloseOnOutsideClick(false);
        this.setHeaderTitle("Edit User");

        this.binder.bind(nameField, MapGroup::getName, MapGroup::setName);
        this.nameField.setRequired(true);


        FormLayout formLayout = new FormLayout();
        formLayout.setAutoResponsive(true);
        formLayout.addFormRow(this.nameField);


        Button saveButton = new Button("Save", event -> {
            if (this.securityGroup == null) {
                this.securityGroup = new MapGroup();
            }
            if (this.binder.writeBeanIfValid(securityGroup)) {
                if (this.editCallback == null) {
                    this.setError("No callback defined for saving changes.");
                    log.info("No callback defined for saving changes.");
                    return;
                }
                this.editCallback.accept(securityGroup);

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

    public void open(MapGroup securityGroup) {
        this.securityGroup = securityGroup;
        this.binder.readBean(securityGroup);
        if (securityGroup == null) {
            this.setHeaderTitle("Create Security Group");

        } else {
            this.setHeaderTitle("Edit Security Group: " + securityGroup.getName());
        }
        super.open();
    }
}
