package ru.yandex.practicum.client;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.model.UserCreds;

import static io.restassured.RestAssured.given;

public class UserClient {

    private static final String BASE_PATH = "/api/auth/register";
    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String USER_DATA_PATH = "/api/auth/user";

    @Step("Регистрация нового пользователя")
    public Response createUser(User user) {
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(user)
                .when()
                .post(BASE_PATH);
    }

    @Step("Вход в систему")
    public Response loginUser(UserCreds userCreds) {
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(userCreds)
                .when()
                .post(LOGIN_PATH);
    }

    //Нужно только для очистки данных после тестов
    @Step("Удаление пользователя")
    public void removeUser(String bearerToken) {
        given()
                .header("Authorization", bearerToken)
                .delete(USER_DATA_PATH);
    }


    @Step("Изменение данных пользователя. С авторизацией")
    public Response updateUserWithToken(User user, String bearerToken) {
        return given()
                .header("Authorization", bearerToken)
                .header("Content-type", "application/json")
                .and()
                .body(user)
                .when()
                .patch(USER_DATA_PATH);
    }

    @Step("Изменение данных пользователя. Без авторизации")
    public Response updateUserWithoutToken(User user) {
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(user)
                .when()
                .patch(USER_DATA_PATH);
    }
}
