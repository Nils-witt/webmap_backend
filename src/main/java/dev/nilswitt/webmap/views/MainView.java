package dev.nilswitt.webmap.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.security.AuthenticationContext;
import dev.nilswitt.webmap.base.ui.MainLayout;
import dev.nilswitt.webmap.entities.User;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route(value = "ui", layout = MainLayout.class)
@RouteAlias("")
@Menu(order = 0, icon = "vaadin:user", title = "Home")
@PermitAll
public class MainView extends VerticalLayout {
    private static final Logger log = LoggerFactory.getLogger(MainView.class);

    public MainView(AuthenticationContext authContext) {

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().setOverflow(Style.Overflow.HIDDEN);

        H2 title = new H2("Home");
        add(title);
        authContext.getAuthenticatedUser(User.class).ifPresent(user -> {
            add(new H2("Welcome, " + user.getUsername() + "!"));
        });
    }
}
