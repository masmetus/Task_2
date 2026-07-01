package ru.yandex.practicum.test;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.client.OrderClient;
import ru.yandex.practicum.client.UserClient;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.model.User;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CreateOrderTest {

    private static final String USER_EMAIL = "qwe@qwe.qwe1";
    private static final String USER_PASSWORD = "qwe123qwe1";
    private static final String USER_NAME = "Абоба1";

    private OrderClient orderClient;
    private UserClient userClient;
    private String accessToken;

    private final List<String> ingredients = List.of("691577430cc94f001a65b862","691577430cc94f001a65b859");

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "https://qa-stellarburgers.education-services.ru";
        orderClient = new OrderClient();
        userClient = new UserClient();
    }


    @Test
    @DisplayName("Создание заказа с авторизацией")
    public void createOrderWithAuth() {
        User user = new User(USER_EMAIL, USER_PASSWORD, USER_NAME);
        accessToken = userClient.createUser(user).then().extract().path("accessToken");

        Order order = new Order(ingredients);
        Response response = orderClient.createOrder(order, accessToken);

        response.then().assertThat()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("name", notNullValue())
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    public void createOrderWithoutAuth() {
        Order order = new Order(ingredients);
        Response response = orderClient.createOrder(order, null);

        response.then().assertThat()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("name", notNullValue())
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа с ингредиентами")
    public void createOrderWithIngredients() {
        Order order = new Order(ingredients);
        Response response = orderClient.createOrder(order, null);

        response.then().assertThat()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    public void createOrderWithoutIngredients() {
        Order order = new Order(List.of());
        Response response = orderClient.createOrder(order, null);

        response.then().assertThat()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем")
    public void createOrderWithInvalidHash() {
        Order order = new Order(List.of("ergergerg"));
        Response response = orderClient.createOrder(order, null);

        response.then().assertThat()
                .statusCode(500);
    }

    @AfterEach
    public void tearDown() {
        if (accessToken != null) {
            userClient.removeUser(accessToken);
        }
    }
}
