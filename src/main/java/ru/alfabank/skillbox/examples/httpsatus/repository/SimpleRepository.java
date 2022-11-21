package ru.alfabank.skillbox.examples.httpsatus.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public interface SimpleRepository {

    Integer create(String value);

    String read(Integer id);

    String update(Integer id, String value);

    String delete(Integer id);

    @Slf4j
    @Repository
    class SimpleRepositoryImpl implements SimpleRepository {

        private final Map<Integer, String> datasource = new ConcurrentHashMap<>();
        private final AtomicInteger nextId = new AtomicInteger(0);

        @Override
        public Integer create(String value) {
            var key = nextId.incrementAndGet();
            while (datasource.get(key) != null) {
                key = nextId.incrementAndGet();
            }
            datasource.put(key, value);
            return key;
        }

        @Override
        public String read(Integer id) {
            return datasource.get(id);
        }

        @Override
        public String update(Integer id, String value) {
            return datasource.put(id, value);
        }

        @Override
        public String delete(Integer id) {
            return datasource.remove(id);
        }
    }
}
