package dev.nilswitt.webmap.views.components;

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import dev.nilswitt.webmap.entities.UserRole;

import java.util.function.Consumer;

public class RoleEditDialog extends Dialog {

    private UserRole userRole = new UserRole();
    private final Binder<UserRole> binder = new Binder<>(UserRole.class);
    private final Consumer<UserRole> editCallback;

    private final TextField nameField = new TextField("Name");


    public RoleEditDialog(Consumer<UserRole> editCallback) {
        this.editCallback = editCallback;
        this.setModality(ModalityMode.STRICT);
        this.setCloseOnOutsideClick(false);
        this.setHeaderTitle("Edit User");

        this.binder.bind(nameField, UserRole::getName, UserRole::setName);

        this.nameField.setRequired(true);

        FormLayout formLayout = new FormLayout();
        formLayout.setAutoResponsive(true);
        formLayout.addFormRow(this.nameField);


        Button saveButton = new Button("Save", event -> {
            if (this.userRole == null) {
                this.userRole = new UserRole();
            }
            if (this.binder.writeBeanIfValid(userRole)) {
                if (this.editCallback != null) {
                    this.editCallback.accept(userRole);
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

    public void open(UserRole userRole) {
        this.userRole = userRole;
        this.binder.readBean(userRole);
        if (userRole == null) {
            this.setHeaderTitle("Create Overlay");

        } else {
            this.setHeaderTitle("Edit Overlay: " + userRole.getName());
        }
        super.open();
    }
}
