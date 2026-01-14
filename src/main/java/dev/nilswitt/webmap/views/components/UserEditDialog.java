package dev.nilswitt.webmap.views.components;

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.SecurityGroupRepository;


import java.util.function.Consumer;

public class UserEditDialog extends Dialog {

    private User user = new User();
    private final Binder<User> binder = new Binder<>(User.class);
    private final Consumer<User> editCallback;

    private final TextField firstNameField = new TextField("First name");
    private final TextField lastNameField = new TextField("Last name");
    private final TextField usernameField = new TextField("Username");
    private final EmailField emailField = new EmailField("Email address");
    private final Checkbox enabledCheckbox = new Checkbox("Enabled");
    private final Checkbox lockedCheckbox = new Checkbox("Locked");
    private final MultiSelectComboBox<SecurityGroup> rolesField = new MultiSelectComboBox<>("Roles");

    public UserEditDialog(Consumer<User> editCallback, SecurityGroupRepository securityGroupRepository) {
        this.editCallback = editCallback;
        this.setModality(ModalityMode.STRICT);
        this.setCloseOnOutsideClick(false);
        this.setHeaderTitle("Edit User");

        this.binder.bind(usernameField, User::getUsername, User::setUsername);
        this.binder.bind(emailField, User::getEmail, User::setEmail);
        this.binder.bind(firstNameField, User::getFirstName, User::setFirstName);
        this.binder.bind(lastNameField, User::getLastName, User::setLastName);
        this.usernameField.setRequired(true);
        this.firstNameField.setRequired(true);
        this.lastNameField.setRequired(true);
        this.emailField.setRequired(true);

        this.rolesField.setItemLabelGenerator(SecurityGroup::getName);
        this.rolesField.setItems(securityGroupRepository.findAll());
        this.binder.bind(rolesField, User::getSecurityGroups, User::setSecurityGroups);

        this.binder.bind(enabledCheckbox, User::isEnabled, User::setEnabled);
        this.binder.bind(lockedCheckbox, (user) -> !user.isAccountNonLocked(), User::setLocked);

        FormLayout formLayout = new FormLayout();
        formLayout.setAutoResponsive(true);
        formLayout.addFormRow(this.firstNameField, this.lastNameField);
        formLayout.addFormRow(this.usernameField, this.emailField);
        formLayout.addFormRow(this.rolesField);
        formLayout.addFormRow(this.enabledCheckbox, this.lockedCheckbox);


        Button saveButton = new Button("Save", event -> {
            if (this.user == null) {
                this.user = new User();
            }
            if (this.binder.writeBeanIfValid(user)) {
                if (this.editCallback != null) {
                    this.editCallback.accept(user);
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

    public void open(User user) {
        this.user = user;
        this.binder.readBean(user);
        if (user == null) {
            this.setHeaderTitle("Create User");
            this.usernameField.setReadOnly(false);

        } else {
            this.setHeaderTitle("Edit User: " + user.getUsername());
            this.usernameField.setReadOnly(true);
            this.usernameField.setTooltipText("Username cannot be changed.");

        }
        super.open();
    }
}
