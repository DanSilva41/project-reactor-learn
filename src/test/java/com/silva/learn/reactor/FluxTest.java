package com.silva.learn.reactor;

import java.util.Comparator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@Slf4j
class FluxTest {

    @Test
    void fluxSubscriber() {
        Flux<String> publisher = Flux.just("Pereira", "Silva", "Danilo")
                .sort(Comparator.comparing(String::toString))
                .log();

        StepVerifier.create(publisher)
                .expectNext("Danilo", "Pereira", "Silva")
                .verifyComplete();
    }

    @Test
    void fluxSubscriberNumbers() {
        Flux<Integer> publisher = Flux.range(1, 5)
                .log();

        publisher.subscribe(i -> log.info("Number: {}", i));

        log.info("-----------------------");
        StepVerifier.create(publisher)
                .expectNext(1, 2, 3, 4, 5)
                .verifyComplete();
    }
}
