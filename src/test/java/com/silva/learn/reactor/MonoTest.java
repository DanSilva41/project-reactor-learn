package com.silva.learn.reactor;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
class MonoTest {

    @Test
    void test() {
        String name = "Danilo Silva";
        Mono<String> publisher = Mono.just(name)
                .log();

        publisher.subscribe();

        log.info("-----------------------");
        StepVerifier.create(publisher)
                .expectNext("Danilo Silva")
                .verifyComplete();
    }
}
