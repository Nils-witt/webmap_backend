package dev.nilswitt.webmap.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.nilswitt.webmap.records.ApplicationInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Route(value = "login", autoLayout = false)
@PageTitle("Login")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginOverlay login;

    public LoginView(ApplicationInfo applicationInfo) {
        login = new LoginOverlay();
        login.setTitle("WebMap");
        login.setDescription("Admin");
        Paragraph text = new Paragraph("Version: " + applicationInfo.version());
        text.getStyle().set("text-align", "center");
        login.getFooter().add(new Button("SSO", event -> {
            getUI().ifPresent(ui -> ui.navigate("/oauth2/login"));
        }));
        login.getFooter().add(text);

        login.setAction("login");
        login.setForgotPasswordButtonVisible(false);
        VerticalLayout layout = new VerticalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        layout.add(login);
        layout.setSizeFull();

        add(layout);
        login.setOpened(true);
        setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            login.setError(true);
        }
    }
}
