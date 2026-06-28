package ru.yandex.practicum.test;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.yandex.practicum.client.UserClient;
import ru.yandex.practicum.model.User;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CreateUserTest {

    private String accessToken;

    private UserClient userClient;

    private static final String USER_EMAIL = "qwe@qwe.qwe1";
    private static final String USER_PASSWORD = "qwe123qwe1";
    private static final String USER_NAME = "Абоба1";

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "https://qa-stellarburgers.education-services.ru";
        userClient = new UserClient();
    }

    @Test
    @DisplayName("Регистрация пользователя")
    public void shouldReturn200WhenCreateNewUser() {
        User user = new User(USER_EMAIL, USER_PASSWORD, USER_NAME);

        Response response = userClient.createUser(user);

        response.then().assertThat()
                .statusCode(200)
                .and()
                .body("success", equalTo(true))
                .and()
                .body("user.email", equalTo(USER_EMAIL))
                .body("user.name", equalTo(USER_NAME))
                .and()
                .body("accessToken", notNullValue())
                .and()
                .body("refreshToken", notNullValue());

        accessToken = response.then().extract().path("accessToken");
    }

    @Test
    @DisplayName("Регистрация пользователя, который уже зарегистрирован")
    public void shouldReturn403WhenCreateDuplicateUser() {
        User user = new User(USER_EMAIL, USER_PASSWORD, USER_NAME);
        Response response = userClient.createUser(user);
        Response response2 = userClient.createUser(user);

        response2.then().assertThat()
                .statusCode(403)
                .and()
                .body("success", equalTo(false))
                .and()
                .body("message", equalTo("User already exists"));


        accessToken = response.then().extract().path("accessToken");
    }

    @ParameterizedTest
    @MethodSource("createInvalidUser")
    @DisplayName("Создание пользователя без обязательных полей")
    public void shouldReturn403WhenCreateUserWithoutRequiredFields(User invalidUser) {
        Response response = userClient.createUser(invalidUser);

        response.then().assertThat()
                .statusCode(403)
                .and()
                .body("success", equalTo(false))
                .and()
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @AfterEach
    public void cleanUp() {
        if (accessToken != null) {
            userClient.removeUser(accessToken);
        }
    }


    private static Stream<Arguments> createInvalidUser() {
        return Stream.of(
                Arguments.of(new User(null, USER_PASSWORD, USER_NAME)),
                Arguments.of(new User(USER_EMAIL, null, USER_NAME)),
                Arguments.of(new User(USER_EMAIL, USER_PASSWORD, null)),
                Arguments.of(new User(null, null, null))
        );
    }
}
