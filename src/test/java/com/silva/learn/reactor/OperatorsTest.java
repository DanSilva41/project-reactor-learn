package com.silva.learn.reactor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

@Slf4j
class OperatorsTest {

    @Test
    void subscribeOnSimple() {
        Flux<Integer> flux = Flux.range(1, 4)
                .map(i -> {
                    log.info("Map 1 -> Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Map 2 -> Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                });

        StepVerifier.create(flux)
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    void publishOnSimple() {
        Flux<Integer> flux = Flux.range(1, 4)
                .map(i -> {
                    log.info("Map 1 -> Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Map 2 -> Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                });

        StepVerifier.create(flux)
                .then(flux::subscribe)
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    void multipleSubscribeOnSimple() {
        Flux<Integer> flux = Flux.range(1, 4)
                .subscribeOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Map 1 -> Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .subscribeOn(Schedulers.single())
                .map(i -> {
                    log.info("Map 2 -> Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                });

        StepVerifier.create(flux)
                .then(flux::subscribe)
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    void multiplePublishOnSimple() {
        Flux<Integer> flux = Flux.range(1, 4)
                .publishOn(Schedulers.single())
                .map(i -> {
                    log.info("Map 1 -> Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Map 2 -> Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                });

        StepVerifier.create(flux)
                .then(flux::subscribe)
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    void publishAndSubscribeOnSimple() {
        Flux<Integer> flux = Flux.range(1, 4)
                .publishOn(Schedulers.single())
                .map(i -> {
                    log.info("Map 1 -> Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Map 2 -> Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                });

        StepVerifier.create(flux)
                .then(flux::subscribe)
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    void subscribeAndPublishOnSimple() {
        Flux<Integer> flux = Flux.range(1, 4)
                .subscribeOn(Schedulers.single())
                .map(i -> {
                    log.info("Map 1 -> Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Map 2 -> Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                });

        StepVerifier.create(flux)
                .then(flux::subscribe)
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    void subscribeOnIO() {
        Mono<List<String>> monoLines = Mono.fromCallable(() ->
                        Files.readAllLines(Paths.get(this.getClass().getClassLoader().getResource("text-file.txt").toURI()), StandardCharsets.UTF_8))
                .log()
                .subscribeOn(Schedulers.boundedElastic());

        StepVerifier.create(monoLines)
                .expectSubscription()
                .assertNext(list -> {
                    assertFalse(list.isEmpty());
                    log.info("Size list: {}", list.size());
                })
                .verifyComplete();
    }

    @Test
    void switchIfEmptyOperator() {
        Flux<Object> notEmptyFlux = Flux.empty()
                .switchIfEmpty(Flux.just("not empty anymore"))
                .log();

        StepVerifier.create(notEmptyFlux)
                .expectSubscription()
                .expectNext("not empty anymore")
                .expectComplete()
                .verify();
    }

    @Test
    void deferOperator() {
        Mono<Long> just = Mono.just(System.currentTimeMillis());
        Mono<Long> defer = Mono.defer(() -> Mono.just(System.currentTimeMillis()));

        just.subscribe(l -> log.info("time: {}", l));
        just.subscribe(l -> log.info("time: {}", l));
        just.subscribe(l -> log.info("time: {}", l));
        just.subscribe(l -> log.info("time: {}", l));
        log.info("--------------------");
        defer.subscribe(l -> log.info("time: {}", l));
        defer.subscribe(l -> log.info("time: {}", l));
        defer.subscribe(l -> log.info("time: {}", l));
        defer.subscribe(l -> log.info("time: {}", l));

        final var atomicLong = new AtomicLong();
        defer.subscribe(atomicLong::set);
        assertTrue(atomicLong.get() > 0);
    }
}
