package ru.alfabank.skillbox.examples.httpsatus.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import ru.alfabank.skillbox.examples.httpsatus.repository.SimpleRepository;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Slf4j
@Validated
@RequestMapping("/2xx")
@RestController
@RequiredArgsConstructor
public class HttpStatus2xxExamplesController {

    private final SimpleRepository repository;

    @PostMapping(value = "/create", consumes = APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Integer> create(@NotBlank @RequestParam(value = "value") String value) {
        log.info("Create new with value {}", value);
        var id = repository.create(value);
        log.info("Created id: {}", id);
        return ResponseEntity.status(200).body(id);
    }

    @GetMapping("/read")
    public ResponseEntity<String> read(@Digits(integer = 3, fraction = 0)
                                       @Positive
                                       @RequestParam(value = "id") Integer id,
                                       WebRequest request) {
        log.info("Read id {}", id);
        var value = repository.read(id);
        if (value != null) {
            log.info("Read value: {} for id: {}", value, id);
            return ResponseEntity.status(200).body(value);
        } else {
            //  return ResponseEntity.status(200).body("No item with such id has been found");
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> update(WebRequest request,
                                         @Digits(integer = 3, fraction = 0)
                                         @Positive
                                         @PathVariable(value = "id") Integer id,
                                         @NotBlank @RequestParam(value = "value") String value) {
        log.info("Update id {}, value {}", id, value);
        var updated = repository.update(id, value);
        if (updated == null) {
            log.info("Created new id {} with value: {}", id, value);
            return ResponseEntity.created(URI.create("/2xx/read/" + id)).eTag(value).build();
        } else {
            log.info("Updated previous value: {} by new value: {} for id: {}", updated, value, id);
            if (request.checkNotModified(updated)) {
                return ResponseEntity.noContent().cacheControl(getCacheControl()).eTag(value).build();
            } else {
                return ResponseEntity.status(HttpStatus.OK)
                    .cacheControl(getCacheControl()).eTag(value)
                    .body(updated);
            }
        }
    }

    private CacheControl getCacheControl() {
        return CacheControl
            .maxAge(1L, TimeUnit.MINUTES)
            .cachePrivate()
            .noTransform()
            .mustRevalidate();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(@Digits(integer = 3, fraction = 0)
                                         @Positive
                                         @RequestParam(value = "id") Integer id) {
        log.info("Delete id {}", id);
        var deleted = repository.delete(id);
        if (deleted != null) {
            log.info("Deleted id: {} with value: {}", id, deleted);
            return ResponseEntity.status(200).body(deleted);
        } else {
            // return ResponseEntity.status(200).body("No item has been deleted");
            return ResponseEntity.notFound().build();
        }
    }
}
