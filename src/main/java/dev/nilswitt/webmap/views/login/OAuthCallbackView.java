package dev.nilswitt.webmap.views.login;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.security.AuthenticationContext;
import dev.nilswitt.webmap.base.ui.MainLayout;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.SecurityGroupRepository;
import dev.nilswitt.webmap.entities.repositories.UserRepository;
import dev.nilswitt.webmap.records.OAuthUserInfo;
import dev.nilswitt.webmap.records.OpenIdInfo;
import dev.nilswitt.webmap.security.MutableApi;
import dev.nilswitt.webmap.views.LoginView;
import dev.nilswitt.webmap.views.MainView;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Slf4j
@Route(value = "/oauth2/callback", layout = MainLayout.class)
@AnonymousAllowed
public class OAuthCallbackView extends VerticalLayout implements BeforeEnterObserver {
    private final UserRepository userRepository;
    private final HttpSession httpSession;
    private final OAuth20Service oAuth20Service;
    private final OpenIdInfo openIdInfo;
    private final AuthenticationContext authenticationContext;
    private final SecurityGroupRepository securityGroupRepository;

    public OAuthCallbackView(UserRepository userRepository, HttpSession session, OpenIdInfo openIdInfo, AuthenticationContext authenticationContext, SecurityGroupRepository securityGroupRepository) {
        this.userRepository = userRepository;
        this.httpSession = session;
        this.openIdInfo = openIdInfo;
        this.authenticationContext = authenticationContext;

        this.oAuth20Service = new ServiceBuilder(openIdInfo.clientId())
                .apiSecret(openIdInfo.clientSecret())
                .defaultScope(openIdInfo.scopes())
                .callback(openIdInfo.callbackUrl())
                .build(new MutableApi(openIdInfo.baseUrl(), openIdInfo.tokenUrl()));
        this.securityGroupRepository = securityGroupRepository;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticationContext.isAuthenticated()) {
            UI.getCurrent().navigate(MainView.class);
            return;
        }

        log.info("TestView initialized with session ID: " + this.httpSession.getAttribute("state"));

        Optional<String> state = event.getLocation()
                .getQueryParameters().getSingleParameter("state");
        Optional<String> code = event.getLocation().getQueryParameters().getSingleParameter("code");
        log.info(state.toString());

        if (state.isEmpty() || code.isEmpty()) {
            UI.getCurrent().navigate(LoginView.class);
            return;
        }

        if (this.httpSession.getAttribute("state") == null || !this.httpSession.getAttribute("state").equals(state.get())) {
            UI.getCurrent().navigate(LoginView.class);
            return;
        }
        OAuth2AccessToken accessToken = null;
        try {
            accessToken = this.oAuth20Service.getAccessToken(code.get());
        } catch (IOException | InterruptedException | ExecutionException e) {
            UI.getCurrent().navigate(LoginView.class);
            throw new RuntimeException(e);
        }
        final OAuthRequest request = new OAuthRequest(Verb.GET, openIdInfo.userInfoUrl());
        this.oAuth20Service.signRequest(accessToken, request);
        try (Response response = this.oAuth20Service.execute(request)) {
            ObjectMapper mapper = new ObjectMapper();


            User user = new User();

            OAuthUserInfo oAuthUserInfo = mapper.readValue(response.getBody(), OAuthUserInfo.class);
            Optional<User> optUser = userRepository.findByUsername(oAuthUserInfo.preferred_username());

            if (optUser.isPresent()) {
                user = optUser.get();
            } else {
                user.setUsername(oAuthUserInfo.preferred_username());
                user.setPassword("--oauth--");
                user.setLastName(oAuthUserInfo.name());
                user.setFirstName(oAuthUserInfo.name());
            }

            user.setEmail(oAuthUserInfo.email());

            HashSet<SecurityGroup> securityGroups = new HashSet<>();
            for (String groupName : oAuthUserInfo.groups()) {
                securityGroups.addAll(securityGroupRepository.findBySsoGroupName(groupName));
            }
            user.setSecurityGroups(securityGroups);
            user = userRepository.save(user);

            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities()
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(token);
            VaadinSession.getCurrent().getSession().setAttribute("SPRING_SECURITY_CONTEXT", context);

            UI.getCurrent().getPage().reload();
            return;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        UI.getCurrent().navigate(LoginView.class);
    }
}
