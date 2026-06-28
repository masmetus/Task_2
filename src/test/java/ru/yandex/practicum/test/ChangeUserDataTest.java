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
import ru.yandex.practicum.model.UserCreds;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class ChangeUserDataTest {

    private static final String USER_EMAIL = "qwe@qwe.qwe1";
    private static final String USER_PASSWORD = "qwe123qwe1";
    private static final String USER_NAME = "Абоба1";

    private User user;
    private UserClient userClient;

    private String accessToken;

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "https://qa-stellarburgers.education-services.ru";
        userClient = new UserClient();
    }

    @MethodSource("usersForUnauthorizedUpdate")
    @ParameterizedTest
    @DisplayName("Обновление пользователя без токена")
    public void shouldReturn401WhenUpdatingUserWithoutToken(User updateUser) {
        Response response = userClient.updateUserWithoutToken(updateUser);

        response.then().assertThat()
                .statusCode(401)
                .and()
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Test
    @DisplayName("Обновление email пользователя")
    public void shouldUpdateEmailField() {
        user = new User(USER_EMAIL, USER_PASSWORD, USER_NAME);

        accessToken = userClient.createUser(user)
                .then()
                .extract()
                .path("accessToken");

        User updateUser = new User("new@mail.ru", USER_PASSWORD, USER_NAME);

        Response response = userClient.updateUserWithToken(updateUser, accessToken);

        response.then().assertThat()
                .statusCode(200)
                .and()
                .body("success", equalTo(true))
                .body("user.email", equalTo("new@mail.ru"))
                .body("user.name", equalTo(USER_NAME));
    }

    @Test
    @DisplayName("Обновление password пользователя")
    public void shouldUpdatePasswordField() {
        user = new User(USER_EMAIL, USER_PASSWORD, USER_NAME);

        accessToken = userClient.createUser(user)
                .then()
                .extract()
                .path("accessToken");

        User updateUser = new User(USER_EMAIL, "newuserpassword", USER_NAME);

        Response response = userClient.updateUserWithToken(updateUser, accessToken);

        response.then().assertThat()
                .statusCode(200)
                .and()
                .body("success", equalTo(true))
                .body("user.email", equalTo(USER_EMAIL))
                .body("user.name", equalTo(USER_NAME));

        Response oldLogin = userClient.loginUser(new UserCreds(USER_EMAIL, USER_PASSWORD));

        oldLogin.then().statusCode(401);

        Response newLogin = userClient.loginUser(
                new UserCreds(USER_EMAIL, "newuserpassword")
        );

        newLogin.then()
                .statusCode(200)
                .body("accessToken", notNullValue());
    }

    @Test
    @DisplayName("Обновление name пользователя")
    public void shouldUpdateNameField() {
        user = new User(USER_EMAIL, USER_PASSWORD, USER_NAME);

        accessToken = userClient.createUser(user)
                .then()
                .extract()
                .path("accessToken");

        User updateUser = new User(USER_EMAIL, USER_PASSWORD, "newusername");

        Response response = userClient.updateUserWithToken(updateUser, accessToken);

        response.then().assertThat()
                .statusCode(200)
                .and()
                .body("success", equalTo(true))
                .body("user.email", equalTo(USER_EMAIL))
                .body("user.name", equalTo("newusername"));
    }

    @AfterEach
    public void cleanUp() {
        if (accessToken != null) {
            userClient.removeUser(accessToken);
        }
    }

    private static Stream<Arguments> usersForUnauthorizedUpdate() {
        return Stream.of(
                Arguments.of(new User("new@mail.ru", USER_PASSWORD, USER_NAME)),
                Arguments.of(new User(USER_EMAIL, "newpassword", USER_NAME)),
                Arguments.of(new User(USER_EMAIL, USER_PASSWORD, "newusername"))
        );
    }
}
