package com.tcs.vetclinic;

import static io.qameta.allure.Allure.step;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.tcs.vetclinic.domain.person.Person;
import io.qameta.allure.AllureId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Collections;

public class VetClinicTests {

    RestTemplate restTemplate = new RestTemplate();

    @Test
    @DisplayName("Создание пользователя с валидными id и именем")
    @AllureId("1")
    public void createUserWithValidData() {
        String postUrl = "http://localhost:8080/api/person";

        Person person = new Person(10L, "John Doe");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        step("Выполняем POST запрос для создания пользователя", () -> {
            HttpEntity<Person> requestEntity = new HttpEntity<>(person, headers);

            ResponseEntity<Long> createPersonResponse = restTemplate.exchange(
                    postUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Long.class
            );

            step("Убеждаемся, что ответ содержит id пользователя", () -> {
                assertNotNull(createPersonResponse.getBody());
            });

            step("Проверяем, что пользователь доступен через GET запрос", () -> {
                String getUrl = "http://localhost:8080/api/person/%s".formatted(createPersonResponse.getBody());
                ResponseEntity<Person> getResponseEntity = restTemplate.getForEntity(getUrl, Person.class);

                assertNotNull(getResponseEntity);
                assertEquals(createPersonResponse.getBody(), getResponseEntity.getBody().getId());
                assertEquals(person.getName(), getResponseEntity.getBody().getName());
            });
        });
    }

    @Test
    @DisplayName("Создание пользователя без id и с валидным именем")
    @AllureId("2")
    public void createUserWithoutId() {
        String postUrl = "http://localhost:8080/api/person";

        Person person = new Person("Alice");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        step("Выполняем POST запрос для создания пользователя без id", () -> {
            HttpEntity<Person> requestEntity = new HttpEntity<>(person, headers);

            ResponseEntity<Long> createPersonResponse = restTemplate.exchange(
                    postUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Long.class
            );

            step("Проверяем, что ответ содержит id пользователя", () -> {
                assertNotNull(createPersonResponse.getBody());
            });

            step("Убеждаемся, что пользователь доступен через GET запрос", () -> {
                String getUrl = "http://localhost:8080/api/person/%s".formatted(createPersonResponse.getBody());
                ResponseEntity<Person> getResponseEntity = restTemplate.getForEntity(getUrl, Person.class);

                assertNotNull(getResponseEntity);
                assertEquals(createPersonResponse.getBody(), getResponseEntity.getBody().getId());
                assertEquals(person.getName(), getResponseEntity.getBody().getName());
            });
        });
    }

    @Test
    @DisplayName("Обновление информации о пользователе")
    @AllureId("3")
    public void updateUserDetails() {
        String url = "http://localhost:8080/api/person/15";

        Person person = new Person("Robert");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        step("Отправляем PUT запрос для изменения данных пользователя", () -> {
            HttpEntity<Person> requestEntity = new HttpEntity<>(person, headers);

            restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    requestEntity,
                    Long.class
            );

            step("Проверяем, что имя пользователя обновилось", () -> {
                String getUrl = "http://localhost:8080/api/person/15";
                ResponseEntity<Person> getResponseEntity = restTemplate.getForEntity(getUrl, Person.class);

                assertNotNull(getResponseEntity);
                assertEquals("Robert", getResponseEntity.getBody().getName());
            });
        });
    }

    @Test
    @DisplayName("Попытка обновления пользователя с несуществующим id")
    @AllureId("4")
    public void updateUserNonExistentId() {
        String url = "http://localhost:8080/api/person/9999999";

        Person person = new Person("Michael");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        step("Отправляем PUT запрос для обновления пользователя с несуществующим id", () -> {
            HttpEntity<Person> requestEntity = new HttpEntity<>(person, headers);

            try {
                restTemplate.exchange(
                        url,
                        HttpMethod.PUT,
                        requestEntity,
                        Long.class
                );
            } catch (Exception e) {
                step("Проверяем, что сервер возвращает 404", () -> {
                    assertEquals(true, e.getMessage().contains("404"));
                });
            }
        });
    }

    @Test
    @DisplayName("Получение существующего пользователя")
    @AllureId("5")
    public void getExistingUser() {
        String url = "http://localhost:8080/api/person/20";

        step("Отправляем GET запрос и проверяем данные пользователя", () -> {
            ResponseEntity<Person> getResponseEntity = restTemplate.getForEntity(url, Person.class);

            assertNotNull(getResponseEntity);
            assertEquals("Emily", getResponseEntity.getBody().getName());
        });
    }

    @Test
    @DisplayName("GET запрос с неверным id")
    @AllureId("6")
    public void getInvalidUserId() {
        String url = "http://localhost:8080/api/person/987654321";

        step("Убеждаемся, что сервер возвращает 404", () -> {
            try {
                restTemplate.getForEntity(url, Person.class);
                assertEquals(true, false);
            } catch (Exception e) {
                assertEquals(true, e.getMessage().contains("404"));
            }
        });
    }

    @Test
    @DisplayName("Получение списка всех пользователей")
    @AllureId("7")
    public void getAllUsers() {
        String url = "http://localhost:8080/api/person";

        step("Отправляем GET запрос и проверяем, что запрос успешен", () -> {
            try {
                ResponseEntity<Person[]> getResponseEntity = restTemplate.getForEntity(url, Person[].class);

                assertEquals(getResponseEntity.getStatusCode(), HttpStatus.OK);
            } catch (Exception e) {
                assertEquals(true, false);
            }
        });
    }

    @Test
    @DisplayName("Получение списка пользователей с ограничением размера")
    @AllureId("8")
    public void getUsersWithLimit() {
        String url = "http://localhost:8080/api/person?size=5";

        step("Убеждаемся, что количество возвращенных пользователей соответствует указанному размеру", () -> {
            ResponseEntity<Person[]> allUsersRes = restTemplate.getForEntity(url, Person[].class);
            Person[] allUsers = allUsersRes.getBody();

            assertEquals(allUsers.length, 5);
        });
    }

    @Test
    @DisplayName("Получение пользователей в обратном порядке")
    @AllureId("9")
    public void getUsersInDescendingOrder() {
        String url1 = "http://localhost:8080/api/person?size=10000000";

        step("Сортируем пользователей в обратном порядке и сверяем с результатом запроса", () -> {
            ResponseEntity<Person[]> allUsersRes = restTemplate.getForEntity(url1, Person[].class);
            Person[] sortedPersons = allUsersRes.getBody();

            Arrays.sort(sortedPersons, Comparator.comparingLong(Person::getId).reversed());

            step("Убеждаемся, что пользователи возвращены в порядке DESC", () -> {
                String urlDesc = "http://localhost:8080/api/person?sort=DESC&size=10000000";

                ResponseEntity<Person[]> allUsersResDesc = restTemplate.getForEntity(urlDesc, Person[].class);
                Person[] allUsersDesc = allUsersResDesc.getBody();

                assertEquals(Arrays.equals(allUsersDesc, sortedPersons), true);
            });
        });
    }

    @Test
    @DisplayName("Удаление существующего пользователя")
    @AllureId("10")
    public void deleteUser() {
        String url = "http://localhost:8080/api/person";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Person person = new Person(200L, "TemporaryUser");

        step("Создаем пользователя для удаления", () -> {
            HttpEntity<Person> requestEntity = new HttpEntity<>(person, headers);

            ResponseEntity<Long> createPersonResponse = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Long.class
            );

            String personUrl = "http://localhost:8080/api/person/%s".formatted(createPersonResponse.getBody());

            step("Отправляем DELETE запрос для удаления пользователя", () -> {
                restTemplate.exchange(
                        personUrl,
                        HttpMethod.DELETE,
                        requestEntity,
                        Long.class
                );
                step("Убеждаемся, что пользователь удален, выполняя GET запрос", () -> {
                    try {
                        restTemplate.getForEntity(personUrl, Person.class);
                    } catch (Exception e) {
                        assertEquals(true, e.getMessage().contains("404"));
                    }
                });
            });
        });
    }

    @Test
    @DisplayName("Удаление пользователя с несуществующим id")
    @AllureId("11")
    public void deleteNonExistentUser() {
        String url = "http://localhost:8080/api/person/555555";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        step("Отправляем DELETE запрос для несуществующего пользователя", () -> {
            try {
                restTemplate.delete(url);
            } catch (Exception e) {
                step("Проверяем, что сервер возвращает 409", () -> {
                    assertEquals(true, e.getMessage().contains("409"));
                });

            }
        });
    }
}
