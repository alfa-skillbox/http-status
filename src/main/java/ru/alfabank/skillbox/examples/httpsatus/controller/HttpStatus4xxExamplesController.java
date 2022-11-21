package ru.alfabank.skillbox.examples.httpsatus.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Positive;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Slf4j
@Validated
@RequestMapping("/4xx")
@RestController
@RequiredArgsConstructor
public class HttpStatus4xxExamplesController {

    // Ошибка валидации запроса по наличию required параметра
    @GetMapping("/400/requiredParam")
    public ResponseEntity<String> badRequest(@Digits(integer = 3, fraction = 0)
                                             @Positive
                                             @RequestParam(value = "id", required = true) Integer id) {
        return ResponseEntity.ok().build();
    }

    // Ошибка чтения из запроса
    @PostMapping(value = "/400/binding", consumes = APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> badRequestMediaType(@RequestParam(value = "value", required = true) String value) {
        return ResponseEntity.ok().build();
    }

    // Ошибка аутентификации - проблемы с credentials (login/password, token и другая кастоммная аутентификация)
    @GetMapping("/401")
    public ResponseEntity<String> unauthorized() {
        return ResponseEntity.ok().build();
    }

    // Ошибка авторизации - проблемы с ролями и пермишенсами (для данного пользователя аутентификация прошла,
    // но не хватает правв для запроса данного ресурса)
    @DeleteMapping("/403")
    public ResponseEntity<String> forbidden() {
        return ResponseEntity.ok().build();
    }

    // Сервер не может найти запрашиваемый ресурс. Также применяется, когда Spring не может найти
    // handler для обработки запроса
    @GetMapping("/404")
    public ResponseEntity<String> notFound(@Digits(integer = 3, fraction = 0)
                                           @Positive
                                           @RequestParam(value = "id") Integer id) {
        return ResponseEntity.notFound().build();
    }

}
