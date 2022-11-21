package ru.alfabank.skillbox.examples.httpsatus.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

@Slf4j
@Validated
@RequestMapping("/5xx")
@RestController
@RequiredArgsConstructor
public class HttpStatus5xxExamplesController {

    // Внутренняя ошибка сервера
    @GetMapping("/500")
    public ResponseEntity<String> internalSererError() {
        throw new RuntimeException("Some Internal Serer Error happened");
    }

    // сервер в настоящее время не может обработать запрос из-за временной перегрузки
    // или планового технического обслуживания
    @GetMapping("/503")
    public ResponseEntity<String> serviceUnavailable() {
        throw new AsyncRequestTimeoutException();
    }

    // Этот ответ об ошибке предоставляется, когда сервер действует как шлюз
    // и не может получить ответ вовремя
    @SneakyThrows
    @GetMapping("/504")
    public ResponseEntity<String> gatewayTimeout() {
        Thread.sleep(5000);
        return ResponseEntity.ok().build();
    }
}
