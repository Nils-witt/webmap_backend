package dev.nilswitt.webmap.base.ui.views.login;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.nilswitt.webmap.email.PasswordResetService;

@Route(value = "forgot-password", autoLayout = false)
@PageTitle("Forgot Password")
@AnonymousAllowed
public class ForgotPasswordView extends VerticalLayout {

    public ForgotPasswordView(PasswordResetService passwordResetService) {
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        setSizeFull();

        EmailField emailField = new EmailField("Email");
        emailField.setWidth("360px");
        emailField.setRequiredIndicatorVisible(true);

        Button sendButton = new Button("Send reset link", event -> {
            if (emailField.isEmpty()) {
                Notification notification = Notification.show("Please enter your email address.");
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            passwordResetService.requestPasswordReset(emailField.getValue().trim());
            Notification.show("If this account exists, a reset link has been sent.", 4000, Notification.Position.MIDDLE);
            UI.getCurrent().navigate("login");
        });

        Button backButton = new Button("Back to login", event -> UI.getCurrent().navigate("login"));

        add(new H2("Reset your password"), emailField, sendButton, backButton);
    }
}



