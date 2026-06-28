package ru.yandex.practicum.test;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.client.OrderClient;
import ru.yandex.practicum.client.UserClient;
import ru.yandex.practicum.model.User;

import static org.hamcrest.Matchers.equalTo;

public class ReceivingOrdersTest {

    private static final String USER_EMAIL = "qwe@qwe.qwe1";
    private static final String USER_PASSWORD = "qwe123qwe1";
    private static final String USER_NAME = "Абоба1";

    private OrderClient orderClient;
    private UserClient userClient;
    private String accessToken;

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "https://qa-stellarburgers.education-services.ru";
        orderClient = new OrderClient();
        userClient = new UserClient();
    }

    @Test
    @DisplayName("Получение заказов авторизованным пользователем")
    public void shouldGetOrdersForAuthorizedUser() {
        User user = new User(USER_EMAIL, USER_PASSWORD, USER_NAME);
        accessToken = userClient.createUser(user).then().extract().path("accessToken");

        Response response = orderClient.getOrders(accessToken);

        response.then().assertThat()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Получение заказов неавторизованным пользователем")
    public void shouldReturn401WhenGettingOrdersWithoutToken() {
        Response response = orderClient.getOrders(null);

        response.then().assertThat()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @AfterEach
    public void cleanUp() {
        if (accessToken != null) {
            userClient.removeUser(accessToken);
        }
    }
}
