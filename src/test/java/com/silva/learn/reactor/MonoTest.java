package com.silva.learn.reactor;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
class MonoTest {

    @Test
    void monoSubscriber() {
        String name = "Danilo Silva";
        Mono<String> publisher = Mono.just(name)
                .log();

        publisher.subscribe();

        log.info("-----------------------");
        StepVerifier.create(publisher)
                .expectNext("Danilo Silva")
                .verifyComplete();
    }

    @Test
    void monoSubscriberConsumer() {
        String name = "Danilo Silva";
        Mono<String> publisher = Mono.just(name)
                .log();

        publisher.subscribe(s -> log.info("Value: {}", s));

        log.info("-----------------------");
        StepVerifier.create(publisher)
                .expectNext("Danilo Silva")
                .verifyComplete();
    }

    @Test
    void monoSubscriberConsumerError() {
        String name = "Danilo Silva";
        Mono<String> publisher = Mono.just(name)
                .map(s -> {
                    throw new RuntimeException("Testing mono with error ");
                });

        publisher.subscribe(
                s -> log.info("Value: {}", s),
                error -> log.error("Something bad happened: {}", error.getMessage()));

        publisher.subscribe(
                s -> log.info("Value: {}", s),
                Throwable::printStackTrace
        );

        StepVerifier.create(publisher)
                .expectError(RuntimeException.class)
                .verify();
    }
}
