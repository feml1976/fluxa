package com.fml.fluxa.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fml.fluxa.auth.application.dto.LoginRequest;
import com.fml.fluxa.auth.application.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Auth — Tests de Integración")
class AuthIntegrationTest {

    @Autowired TestRestTemplate restTemplate;
    @Autowired ObjectMapper objectMapper;

    private static final String PASSWORD = "Test1234!";

    // ── Helpers ───────────────────────────────────────────────────

    private void register(String email) {
        restTemplate.postForEntity("/api/v1/auth/register",
                new RegisterRequest("Test", "User", email, PASSWORD), String.class);
    }

    private String loginAndGetToken(String email) throws Exception {
        ResponseEntity<String> resp = restTemplate.postForEntity("/api/v1/auth/login",
                new LoginRequest(email, PASSWORD), String.class);
        return objectMapper.readTree(resp.getBody()).path("data").path("accessToken").asText();
    }

    // ── Tests ─────────────────────────────────────────────────────

    @Test
    @DisplayName("1. Contexto de Spring arranca sin errores (smoke test)")
    void contextLoads() {
        assertThat(restTemplate).isNotNull();
    }

    @Test
    @DisplayName("2. POST /register → 201 Created con datos del usuario en el body")
    void register_returnsCreated_withUserData() throws Exception {
        RegisterRequest req = new RegisterRequest("María", "González", "registro@fluxa.test", PASSWORD);

        ResponseEntity<String> resp = restTemplate.postForEntity("/api/v1/auth/register", req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode body = objectMapper.readTree(resp.getBody());
        assertThat(body.path("success").asBoolean()).isTrue();
        assertThat(body.path("data").path("email").asText()).isEqualTo("registro@fluxa.test");
        assertThat(body.path("data").path("id").asLong()).isPositive();
    }

    @Test
    @DisplayName("3. POST /register con email duplicado → 422 Unprocessable Entity")
    void register_withDuplicateEmail_returns422() throws Exception {
        RegisterRequest req = new RegisterRequest("Carlos", "López", "duplicado@fluxa.test", PASSWORD);
        restTemplate.postForEntity("/api/v1/auth/register", req, String.class); // primera vez — OK

        ResponseEntity<String> resp = restTemplate.postForEntity("/api/v1/auth/register", req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(resp.getBody()).contains("ya está registrado");
    }

    @Test
    @DisplayName("4. POST /login → 200 OK con accessToken, refreshToken y tokenType=Bearer")
    void login_returnsTokens() throws Exception {
        String email = "login@fluxa.test";
        register(email);

        ResponseEntity<String> resp = restTemplate.postForEntity("/api/v1/auth/login",
                new LoginRequest(email, PASSWORD), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = objectMapper.readTree(resp.getBody()).path("data");
        assertThat(data.path("accessToken").asText()).isNotBlank();
        assertThat(data.path("refreshToken").asText()).isNotBlank();
        assertThat(data.path("tokenType").asText()).isEqualTo("Bearer");
        assertThat(data.path("expiresIn").asLong()).isPositive();
    }

    @Test
    @DisplayName("5. GET /me con token válido → 200 OK con email del usuario autenticado")
    void getMe_withValidToken_returnsAuthenticatedUser() throws Exception {
        String email = "me@fluxa.test";
        register(email);
        String token = loginAndGetToken(email);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/v1/auth/me", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = objectMapper.readTree(resp.getBody()).path("data");
        assertThat(data.path("email").asText()).isEqualTo(email);
        assertThat(data.path("isActive").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("6. POST /login con contraseña incorrecta → 401 Unauthorized")
    void login_withWrongPassword_returns401() throws Exception {
        String email = "wrong_pass@fluxa.test";
        register(email);

        ResponseEntity<String> resp = restTemplate.postForEntity("/api/v1/auth/login",
                new LoginRequest(email, "WrongPass99!"), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resp.getBody()).contains("Credenciales inválidas");
    }

    @Test
    @DisplayName("7. POST /login con usuario inexistente → 401 Unauthorized")
    void login_withNonExistentUser_returns401() {
        ResponseEntity<String> resp = restTemplate.postForEntity("/api/v1/auth/login",
                new LoginRequest("noexiste@fluxa.test", PASSWORD), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("8. GET /income/sources sin token → solicitud rechazada (4xx)")
    void protectedEndpoint_withoutToken_isRejected() {
        ResponseEntity<String> resp = restTemplate.getForEntity("/api/v1/income/sources", String.class);

        assertThat(resp.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    @DisplayName("9. GET /income/sources con token válido → 200 OK (acceso autorizado)")
    void protectedEndpoint_withValidToken_isAllowed() throws Exception {
        String email = "authorized@fluxa.test";
        register(email);
        String token = loginAndGetToken(email);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/v1/income/sources", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
