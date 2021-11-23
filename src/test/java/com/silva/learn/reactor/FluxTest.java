package com.silva.learn.reactor;

import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
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

    @Test
    void fluxSubscriberFromList() {
        Flux<Integer> publisher = Flux.fromIterable(List.of(9, 7, 8, 6, 5))
                .log();

        publisher.subscribe(i -> log.info("Number: {}", i));

        log.info("-----------------------");
        StepVerifier.create(publisher)
                .expectNext(9, 7, 8, 6, 5)
                .verifyComplete();
    }

    @Test
    void fluxSubscriberNumbersError() {
        Flux<Integer> publisher = Flux.range(1, 5)
                .log()
                .map(i -> {
                    if (i == 4) {
                        throw new IndexOutOfBoundsException("Index error");
                    }
                    return i;
                });

        publisher.subscribe(
                i -> log.info("Number: {}", i),
                Throwable::printStackTrace,
                () -> log.info("FINISHED"));

        log.info("-----------------------");
        StepVerifier.create(publisher)
                .expectNext(1, 2, 3)
                .expectError(IndexOutOfBoundsException.class)
                .verify();
    }

    @Test
    void fluxSubscriberNumbersErrorUglyBackpressure() {
        Flux<Integer> publisher = Flux.range(1, 10)
                .log();

        publisher.subscribe(new Subscriber<Integer>() {
            private int count = 0;
            private Subscription subscription;
            private int requestCount = 2;

            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                this.subscription.request(requestCount);
            }

            @Override
            public void onNext(Integer integer) {
                count++;
                if (count >= 2) {
                    count = 0;
                    this.subscription.request(requestCount);
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        });

        log.info("-----------------------");
        StepVerifier.create(publisher)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }
}
