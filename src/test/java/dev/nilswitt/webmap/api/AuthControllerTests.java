package dev.nilswitt.webmap.api;

import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.UserRepository;
import dev.nilswitt.webmap.security.JWTTokenComponent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.ObjectMapper;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.properties")
class AuthControllerTests {

    @Autowired
    MockMvcTester mockMvcTester;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JWTTokenComponent jwtComponent;

    @Test
    void shouldObtainTokenWithValidCredentials() throws UnsupportedEncodingException {
        // Ensure a user with known password exists
        String username = "auth-test-user";
        String rawPassword = "testpassword123";

        Optional<User> existing = userRepository.findByUsername(username);
        existing.ifPresent(userRepository::delete);

        User user = new User(username, "authtest@mock.test", "Auth", "Test");
        user.setPassword(passwordEncoder.encode(rawPassword));
        user = userRepository.save(user);

        ObjectMapper mapper = new ObjectMapper();
        String payload = mapper.writeValueAsString(Map.of(
                "username", username,
                "password", rawPassword
        ));

        var result = mockMvcTester.post().contentType(MediaType.APPLICATION_JSON).content(payload).uri("/api/token").exchange();

        assertThat(result).hasStatusOk().bodyJson().extractingPath("$.token").asString().isNotBlank();

        userRepository.delete(user);
    }

    @Test
    void shouldReturnErrorForInvalidCredentials() throws UnsupportedEncodingException {
        ObjectMapper mapper = new ObjectMapper();
        String payload = mapper.writeValueAsString(Map.of(
                "username", "nonexistent-user",
                "password", "wrongpassword"
        ));

        var result = mockMvcTester.post().contentType(MediaType.APPLICATION_JSON).content(payload).uri("/api/token").exchange();

        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldValidateTokenSuccessfully() throws UnsupportedEncodingException {
        // Create user and generate a valid token
        String username = "auth-validate-user";
        Optional<User> existing = userRepository.findByUsername(username);
        existing.ifPresent(userRepository::delete);

        User user = new User(username, "authvalidate@mock.test", "Validate", "Test");
        user.setPassword(passwordEncoder.encode("password"));
        user = userRepository.save(user);

        String token = jwtComponent.generateToken(user);

        var result = mockMvcTester.get().uri("/api/token")
                .header("Authorization", "Bearer " + token)
                .exchange();

        assertThat(result).hasStatusOk().bodyJson().extractingPath("$.valid").convertTo(Boolean.class).isEqualTo(true);
        assertThat(result).bodyJson().extractingPath("$.user").isNotNull();

        userRepository.delete(user);
    }

    @Test
    void shouldRejectValidationWithoutToken() throws UnsupportedEncodingException {
        var result = mockMvcTester.get().uri("/api/token").exchange();

        assertThat(result).hasStatus4xxClientError();
    }

    @Test
    void shouldRejectValidationWithInvalidToken() throws UnsupportedEncodingException {
        var result = mockMvcTester.get().uri("/api/token")
                .header("Authorization", "Bearer invalidtokenvalue")
                .exchange();

        assertThat(result).hasStatus4xxClientError();
    }
}


