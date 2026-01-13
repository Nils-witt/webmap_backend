package dev.nilswitt.webmap.views.components;

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import dev.nilswitt.webmap.entities.SecurityGroup;

import java.util.function.Consumer;

public class SecurityGroupEditDialog extends Dialog {

    private SecurityGroup securityGroup = null;
    private final Binder<SecurityGroup> binder = new Binder<>(SecurityGroup.class);
    private final Consumer<SecurityGroup> editCallback;

    private final TextField nameField = new TextField("Name");
    private final MultiSelectComboBox<String> rolesField = new MultiSelectComboBox<>("Roles");


    public SecurityGroupEditDialog(Consumer<SecurityGroup> editCallback) {
        this.editCallback = editCallback;
        this.setModality(ModalityMode.STRICT);
        this.setCloseOnOutsideClick(false);
        this.setHeaderTitle("Edit User");

        this.binder.bind(nameField, SecurityGroup::getName, SecurityGroup::setName);
        this.binder.bind(rolesField, SecurityGroup::getRoles, SecurityGroup::setRoles);

        this.nameField.setRequired(true);

        this.rolesField.setRequired(false);
        this.rolesField.setItems(SecurityGroup.availableRoles());

        FormLayout formLayout = new FormLayout();
        formLayout.setAutoResponsive(true);
        formLayout.addFormRow(this.nameField);
        formLayout.addFormRow(this.rolesField);


        Button saveButton = new Button("Save", event -> {
            if (this.securityGroup == null) {
                this.securityGroup = new SecurityGroup("NaN");
            }
            if (this.binder.writeBeanIfValid(securityGroup)) {
                if (this.editCallback != null && this.securityGroup.getName().equals(this.nameField.getValue())) {
                    this.editCallback.accept(securityGroup);
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

    public void open(SecurityGroup securityGroup) {
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
