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

public class LoginUserTest {

    private String accessToken;

    private UserClient userClient;
    private User user;

    private static final String USER_EMAIL = "qwe@qwe.qwe1";
    private static final String USER_PASSWORD = "qwe123qwe1";
    private static final String USER_NAME = "Абоба1";

    private static final String WRONG_USER_EMAIL = "qwe@qwe.qwe1123";
    private static final String WRONG_USER_PASSWORD = "qwe";

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "https://qa-stellarburgers.education-services.ru";
        user = new User(USER_EMAIL, USER_PASSWORD, USER_NAME);
        userClient = new UserClient();

        Response response = userClient.createUser(user);

        accessToken = response.then().extract().path("accessToken");
    }

    @Test
    @DisplayName("Вход в систему под существующим пользователем")
    public void shouldReturn200WhenUserRightLogin() {
        UserCreds creds = new UserCreds(USER_EMAIL, USER_PASSWORD);

        Response response = userClient.loginUser(creds);

        response.then().assertThat()
                .statusCode(200)
                .and()
                .body("success", equalTo(true))
                .and()
                .body("user.email", equalTo(USER_EMAIL))
                .body("user.name", equalTo(USER_NAME))
                .and()
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @ParameterizedTest
    @MethodSource("InvalidUserCreds")
    @DisplayName("Вход в систему под несуществующим пользователем")
    public void shouldReturn401WhenUserFailureLogin(UserCreds invalidCreds) {
        Response response = userClient.loginUser(invalidCreds);

        response.then().assertThat()
                .statusCode(401)
                .and()
                .body("message", equalTo("email or password are incorrect"));
    }


    @AfterEach
    public void cleanUp() {
        if (accessToken != null) {
            userClient.removeUser(accessToken);
        }
    }


    private static Stream<Arguments> InvalidUserCreds() {
        return Stream.of(
                Arguments.of(new UserCreds(null, WRONG_USER_PASSWORD)),
                Arguments.of(new UserCreds(WRONG_USER_EMAIL, null)),
                Arguments.of(new UserCreds(WRONG_USER_EMAIL, WRONG_USER_PASSWORD)),
                Arguments.of(new UserCreds(null, null))
        );
    }
}
