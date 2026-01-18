package dev.nilswitt.webmap.views.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordChangeDialog extends Dialog {

    private final PasswordField passwordField = new PasswordField("New Password");
    private final PasswordField confirmPasswordField = new PasswordField("Confirm Password");

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private User user;

    public PasswordChangeDialog(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        setHeaderTitle("Change Password");
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
        setWidth("400px");


        FormLayout formLayout = new FormLayout();
        formLayout.add(passwordField, confirmPasswordField);
        add(formLayout);

        getFooter().add(createCancelButton(), createChangeButton());
    }


    @Override
    public void open() {
        this.user = null;
        this.passwordField.clear();
        this.confirmPasswordField.clear();
        super.open();
    }

    public void open(User user) {
        this.passwordField.clear();
        this.confirmPasswordField.clear();
        this.user = user;

        super.open();
    }

    private Button createCancelButton() {
        Button cancelButton = new Button("Cancel");
        cancelButton.addClickListener(event -> close());
        return cancelButton;
    }

    private void setError(String message) {
        // Implement error display logic here (e.g., show a notification or set error messages on fields)
    }

    private Button createChangeButton() {
        Button changeButton = new Button("Change Password");
        changeButton.addClickListener(event -> {
            if (this.user == null) {
                setError("User is not set.");
                return;
            }
            if (!this.passwordField.getValue().equals(this.confirmPasswordField.getValue())) {
                setError("Passwords do not match.");
                return;
            }
            if (this.passwordField.getValue().isEmpty() || this.passwordField.getValue().equalsIgnoreCase("")) {
                setError("Password cannot be empty.");
                return;
            }
            this.user.setPassword(passwordEncoder.encode(this.passwordField.getValue()));
            this.userRepository.save(user);
            close();

        });
        return changeButton;
    }
}
