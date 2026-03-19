package dev.nilswitt.webmap.base.ui.views.login;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.nilswitt.webmap.email.PasswordResetService;
import dev.nilswitt.webmap.exceptions.InvalidPasswordResetTokenException;

@Route(value = "reset-password", autoLayout = false)
@PageTitle("Set New Password")
@AnonymousAllowed
public class ResetPasswordView extends VerticalLayout implements BeforeEnterObserver {

    private final PasswordResetService passwordResetService;
    private final PasswordField passwordField = new PasswordField("New password");
    private final PasswordField confirmPasswordField = new PasswordField("Confirm password");
    private final Button submitButton = new Button("Set new password");

    private String token;

    public ResetPasswordView(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;

        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        setSizeFull();

        passwordField.setWidth("360px");
        confirmPasswordField.setWidth("360px");

        submitButton.addClickListener(event -> handleSubmit());

        Button backButton = new Button("Back to login", event -> UI.getCurrent().navigate("login"));

        add(new H2("Choose a new password"), passwordField, confirmPasswordField, submitButton, backButton);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.token = event.getLocation().getQueryParameters().getSingleParameter("token").orElse(null);
        if (this.token == null || this.token.isBlank()) {
            submitButton.setEnabled(false);
            Notification notification = Notification.show("Missing reset token.");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void handleSubmit() {
        if (this.token == null || this.token.isBlank()) {
            Notification notification = Notification.show("Missing reset token.");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        String password = this.passwordField.getValue();
        String confirm = this.confirmPasswordField.getValue();

        if (password == null || password.isBlank()) {
            Notification notification = Notification.show("Password is required.");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (!password.equals(confirm)) {
            Notification notification = Notification.show("Passwords do not match.");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            this.passwordResetService.resetPassword(this.token, password);
            Notification.show("Password updated. You can now log in.", 4000, Notification.Position.MIDDLE);
            UI.getCurrent().navigate("login");
        } catch (InvalidPasswordResetTokenException ex) {
            Notification notification = Notification.show(ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}


