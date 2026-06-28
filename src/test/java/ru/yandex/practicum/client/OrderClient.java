package ru.yandex.practicum.client;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import ru.yandex.practicum.model.Order;

import static io.restassured.RestAssured.given;

public class OrderClient {

    private static final String ORDER_PATH = "/api/orders";

    @Step("Создание заказа")
    public Response createOrder(Order order, String accessToken) {
        RequestSpecification request = given()
                .header("Content-type", "application/json");

        if (accessToken != null) {
            request.header("Authorization", accessToken);
        }

        return request
                .body(order)
                .when()
                .post(ORDER_PATH);
    }

    @Step("Получение заказов пользователя")
    public Response getOrders(String token) {
        RequestSpecification request = given()
                .header("Content-type", "application/json");

        if (token != null) {
            request.header("Authorization", token);
        }

        return request
                .when()
                .get(ORDER_PATH);
    }
}
