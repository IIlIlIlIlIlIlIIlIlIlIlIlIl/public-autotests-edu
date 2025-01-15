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

public class UnitTests {

    RestTemplate restTemplate = new RestTemplate();

    @Test
    @DisplayName("Добавление пользователя с заданными id и именем")
    @AllureId("1")
    public void test1() {
        String postUrl = "http://localhost:8080/api/person";
        Person person = new Person(100L, "Alex");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        step("Выполняем POST /person с параметрами id = 100, name = 'Alex'", () -> {
            HttpEntity<Person> requestEntity = new HttpEntity<>(person, headers);

            ResponseEntity<Long> createPersonResponse = restTemplate.exchange(
                    postUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Long.class
            );

            step("Убеждаемся, что в ответе POST /person вернулся id", () -> {
                assertNotNull(createPersonResponse.getBody());
            });

            step("Проверяем, что GET /person/{id} возвращает корректного пользователя", () -> {
                String getUrl = "http://localhost:8080/api/person/%s".formatted(createPersonResponse.getBody());
                ResponseEntity<Person> getResponseEntity = restTemplate.getForEntity(getUrl, Person.class);

                assertNotNull(getResponseEntity);
                assertEquals(createPersonResponse.getBody(), getResponseEntity.getBody().getId());
                assertEquals(person.getName(), getResponseEntity.getBody().getName());
            });
        });
    }

    @Test
    @DisplayName("Добавление пользователя без id и с заданным именем")
    @AllureId("2")
    public void test2() {
        String postUrl = "http://localhost:8080/api/person";

        Person person = new Person("John");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        step("Выполняем POST /person с параметрами id = null, name = 'John'", () -> {
            HttpEntity<Person> requestEntity = new HttpEntity<>(person, headers);

            ResponseEntity<Long> createPersonResponse = restTemplate.exchange(
                    postUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Long.class
            );

            step("Убеждаемся, что в ответе POST /person вернулся id", () -> {
                assertNotNull(createPersonResponse.getBody());
            });

            step("Проверяем, что GET /person/{id} возвращает корректного пользователя", () -> {
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
    public void test3() {
        String url = "http://localhost:8080/api/person/5";

        Person person = new Person("Michael");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        step("Выполняем PUT /person для изменения данных пользователя", () -> {
            HttpEntity<Person> requestEntity = new HttpEntity<>(person, headers);

            ResponseEntity<Long> createPersonResponse = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    requestEntity,
                    Long.class
            );

            step("Убеждаемся, что GET /person/{id} возвращает обновленного пользователя", () -> {
                String getUrl = "http://localhost:8080/api/person/5";
                ResponseEntity<Person> getResponseEntity = restTemplate.getForEntity(getUrl, Person.class);

                assertNotNull(getResponseEntity);
                assertEquals("Michael", getResponseEntity.getBody().getName());
            });
        });
    }

    @Test
    @DisplayName("Попытка обновления несуществующего пользователя")
    @AllureId("4")
    public void test4() {
        String url = "http://localhost:8080/api/person/99999";

        Person person = new Person("NonExistent");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        step("Выполняем PUT /person с несуществующим id", () -> {
            HttpEntity<Person> requestEntity = new HttpEntity<>(person, headers);

            try {
                ResponseEntity<Long> createPersonResponse = restTemplate.exchange(
                        url,
                        HttpMethod.PUT,
                        requestEntity,
                        Long.class
                );
            } catch (Exception e) {
                step("Убеждаемся, что сервер возвращает 404", () -> {
                    assertEquals(true, e.getMessage().contains("404"));
                });
            }
        });
    }

    @Test
    @DisplayName("Получение данных существующего пользователя")
    @AllureId("5")
    public void test5() {
        String url = "http://localhost:8080/api/person/2";

        step("Убеждаемся, что GET /person/{id} возвращает корректного пользователя", () -> {
            ResponseEntity<Person> getResponseEntity = restTemplate.getForEntity(url, Person.class);

            assertNotNull(getResponseEntity);
            assertEquals("Michael", getResponseEntity.getBody().getName());
        });
    }

    @Test
    @DisplayName("Попытка получения пользователя с некорректным id")
    @AllureId("6")
    public void test6() {
        String url = "http://localhost:8080/api/person/999999";

        step("Убеждаемся, что GET /person/{id} возвращает 404", () -> {
            try {
                restTemplate.getForEntity(url, Person.class);
                assertEquals(true, false);
            }
            catch (Exception e) {
                assertEquals(true, e.getMessage().contains("404"));
            }
        });
    }

    @Test
    @DisplayName("Получение списка всех пользователей")
    @AllureId("7")
    public void test7() {
        String url = "http://localhost:8080/api/person";

        step("Проверяем, что GET /person возвращает успешный ответ", () -> {
            try {
                ResponseEntity<Person[]> getResponseEntity = restTemplate.getForEntity(url, Person[].class);

                assertEquals(getResponseEntity.getStatusCode(), HttpStatus.OK);
            }
            catch (Exception e) {
                assertEquals(true, false);
            }
        });
    }

    @Test
    @DisplayName("Получение списка пользователей с ограничением по количеству")
    @AllureId("8")
    public void test8() {
        String url = "http://localhost:8080/api/person?size=2";

        step("Убеждаемся, что количество пользователей в ответе равно 2", () -> {
            ResponseEntity<Person[]> allUsersRes = restTemplate.getForEntity(url, Person[].class);
            Person[] allUsers = allUsersRes.getBody();

            assertEquals(allUsers.length, 2);
        });
    }

    @Test
    @DisplayName("Получение пользователей в обратном порядке")
    @AllureId("9")
    public void test9() {
        String url1 = "http://localhost:8080/api/person?size=100";

        step("Получаем всех пользователей и сортируем по убыванию id", () -> {
            ResponseEntity<Person[]> allUsersRes = restTemplate.getForEntity(url1, Person[].class);
            Person[] sortedPersons = allUsersRes.getBody();

            Arrays.sort(sortedPersons, Comparator.comparingLong(Person::getId).reversed());

            step("Убеждаемся, что GET /person?sort=DESC возвращает пользователей в том же порядке", () -> {
                String urlDesc = "http://localhost:8080/api/person?sort=DESC&size=100";

                ResponseEntity<Person[]> allUsersResDesc = restTemplate.getForEntity(urlDesc, Person[].class);
                Person[] allUsersDesc = allUsersResDesc.getBody();

                assertEquals(Arrays.equals(allUsersDesc, sortedPersons), true);
            });
        });
    }

    @Test
    @DisplayName("Удаление пользователя")
    @AllureId("10")
    public void test10() {
        String url = "http://localhost:8080/api/person";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Person person = new Person(500L, "TestUser");

        step("Создаем пользователя для удаления", () -> {
            HttpEntity<Person> requestEntity = new HttpEntity<>(person, headers);

            ResponseEntity<Long> createPersonResponse = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Long.class
            );

            String personUrl = "http://localhost:8080/api/person/%s".formatted(createPersonResponse.getBody());

            step("Выполняем DELETE /person", () -> {
                restTemplate.exchange(
                        personUrl,
                        HttpMethod.DELETE,
                        requestEntity,
                        Long.class
                );
                step("Убеждаемся, что GET /person/{id} возвращает 404", () -> {
                    try {
                        ResponseEntity<Person> getResponseEntity = restTemplate.getForEntity(personUrl, Person.class);
                    } catch (Exception e) {
                        assertEquals(true, e.getMessage().contains("404"));
                    }
                });
            });
        });

    }

    @Test
    @DisplayName("Попытка удаления пользователя с несуществующим id")
    @AllureId("11")
    public void test11() {
        String url = "http://localhost:8080/api/person/99999";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        step("Выполняем DELETE /person с несуществующим id", () -> {
            try {
                restTemplate.delete(url);
            } catch (Exception e) {
                step("Убеждаемся, что сервер возвращает 409", () -> {
                    assertEquals(true, e.getMessage().contains("409"));
                });

            }
        });
    }
}

