package ru.alfabank.skillbox.examples.httpsatus.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import ru.alfabank.skillbox.examples.httpsatus.repository.SimpleRepository;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

@Slf4j
@Validated
@RequestMapping("/3xx")
@RestController
@RequiredArgsConstructor
public class HttpStatus3xxExampleController {

    private static final URI REDIRECT_POST_URL = URI.create("/app/3xx/send");
    private static final URI REDIRECT_GET_URL = URI.create("/app/3xx/read/");

    private final SimpleRepository repository;

    private final long dummyLastModified =
        LocalDateTime.of(2022, 9, 17, 1, 1, 1)
            .atZone(ZoneId.of("GMT")).toInstant().toEpochMilli();

    // Simple example for GET method
    @GetMapping("/read")
    public ResponseEntity<String> getExample() {
        return ResponseEntity.ok("GET result");
    }

    // Simple example for POST method
    @PostMapping("/send")
    public ResponseEntity<String> postExample() {
        return ResponseEntity.ok("POST result");
    }

    // Simply present how redirections work
    @GetMapping("/302/redirect")
    public ResponseEntity<?> redirect302Get() {
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(REDIRECT_GET_URL).build();
    }

    // Redirections that MAY change subsequent request METHOD to GET
    // Results to 200 OK because it is
    // POST request - POST redirect - GET subsequent request to GET endpoint
    @PostMapping("/301/redirect")
    public ResponseEntity<?> redirect301Post() {
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
            .location(REDIRECT_GET_URL).build();
    }

    // Redirections that MAY change subsequent request METHOD to GET
    // Results to 200 OK because it is
    // POST request - POST redirect - GET subsequent request to GET endpoint
    @PostMapping("/302/redirect")
    public ResponseEntity<?> redirect302Post() {
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(REDIRECT_GET_URL).build();
    }

    // Redirections that SHOULD change subsequent request METHOD to GET
    // Results to 200 Ok because it is
    // POST request - POST redirect - GET subsequent request to GET endpoint
    @PostMapping("/303/redirect")
    public ResponseEntity<?> redirect303PostToGet() {
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
            .location(REDIRECT_GET_URL).build();
    }

    // Первый запрос на чтение должен получить ответ 200 и установленные headers Cache-Control, Last-Modified
    // чтобы второй запрос уже был с заголовком If-Modified-Since, значение которого будет сравниваться
    // с dummyLastModified и, если будет понятно, что запрашиваемый объект не изменялся, то
    // вернется 304 - "используй кеш"
    @GetMapping("/304/readCached")
    public ResponseEntity<String> doNotSendValueBackIfNotModified(Integer id, WebRequest request) {
        if (request.checkNotModified(dummyLastModified)) {
            log.info("No need to send data back to the client");
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }
        var value = repository.read(id);
        if (value != null) {
            log.info("Read value: {} for id: {}", value, id);
            return ResponseEntity.status(HttpStatus.OK).cacheControl(getCacheControl()).body(value);
        } else {
            log.info("Not found id: {} for reading", id);
            return ResponseEntity.notFound().build();
        }
    }

    private CacheControl getCacheControl() {
        return CacheControl
            .maxAge(1L, TimeUnit.MINUTES)
            .cachePrivate()
            .noTransform()
            .mustRevalidate();
    }

    // Redirections that SHOULD not change subsequent request METHOD to GET
    // Results to 200 Ok because it is
    // POST request - POST redirect - POST subsequent request to POST endpoint
    @PostMapping("/307/redirectToPost")
    public ResponseEntity<?> postRedirectToPost307(HttpServletRequest request) {
        log.info("Method: {}", request.getMethod());
        return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
            .location(REDIRECT_POST_URL).build();
    }

    // Results is 405 because there is no GET /create method
    // because it is
    // POST request - POST redirect - POST subsequent request to GET endpoint
    @PostMapping("/307/redirectToGet")
    public ResponseEntity<?> postRedirectToGet307(HttpServletRequest request) {
        log.info("Method: {}", request.getMethod());
        return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
            .location(REDIRECT_GET_URL).build();
    }

    // Redirections that SHOULD not change subsequent request METHOD to GET
    // Results to 200 Ok because it is
    // POST request - POST redirect - POST subsequent request to POST endpoint
    @PostMapping("/308/redirectToPost")
    public ResponseEntity<?> postRedirectToPost308(HttpServletRequest request) {
        log.info("Method: {}", request.getMethod());
        return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT)
            .location(REDIRECT_POST_URL).build();
    }

    // Results is 405 because there is no GET /create method
    // because it is
    // POST request - POST redirect - POST subsequent request to GET endpoint
    @PostMapping("/308/redirectToGet")
    public ResponseEntity<?> postRedirectToGet308(HttpServletRequest request) {
        log.info("Method: {}", request.getMethod());
        return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT)
            .location(REDIRECT_GET_URL).build();
    }
}
