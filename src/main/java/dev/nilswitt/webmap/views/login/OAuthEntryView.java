package dev.nilswitt.webmap.views.login;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.nilswitt.webmap.records.OpenIdInfo;
import dev.nilswitt.webmap.security.MutableApi;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
@Route(value = "/oauth2/login")
@AnonymousAllowed
public class OAuthEntryView extends VerticalLayout implements BeforeEnterObserver {
    private final HttpSession httpSession;
    private final OAuth20Service oAuth20Service;

    public OAuthEntryView(HttpSession session, OpenIdInfo openIdInfo) {
        add("This page should never be visible, you will be redirected to the SSO provider.");
        this.httpSession = session;
        this.oAuth20Service = new ServiceBuilder(openIdInfo.clientId())
                .apiSecret(openIdInfo.clientSecret())
                .defaultScope(openIdInfo.scopes())
                .callback(openIdInfo.callbackUrl())
                .build(new MutableApi(openIdInfo.baseUrl(), openIdInfo.tokenUrl()));

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String secretState = "secret" + new Random().nextInt(999_999);
        String url = oAuth20Service.getAuthorizationUrl(secretState);
        httpSession.setAttribute("state", secretState);

        UI.getCurrent().getPage().setLocation(url);
    }
}
