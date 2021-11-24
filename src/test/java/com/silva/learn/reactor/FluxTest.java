package com.silva.learn.reactor;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.ConnectableFlux;
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

        publisher.subscribe(new Subscriber<>() {
            private int count = 0;
            private Subscription subscription;
            private final int requestCount = 2;

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

    @Test
    void fluxSubscriberNumbersErrorNotUglyBackpressure() {
        Flux<Integer> publisher = Flux.range(1, 10)
                .log();

        publisher.subscribe(new BaseSubscriber<>() {
            private int count = 0;
            private final int requestCount = 2;

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(requestCount);
            }

            @Override
            protected void hookOnNext(Integer value) {
                count++;
                if (count >= requestCount) {
                    count = 0;
                    request(requestCount);
                }
            }
        });

        log.info("-----------------------");
        StepVerifier.create(publisher)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }

    @Test
    void fluxSubscriberPrettyBackpressure() {
        Flux<Integer> publisher = Flux.range(1, 10)
                .log()
                .limitRate(3);

        publisher.subscribe(i -> log.info("Number: {}", i));

        log.info("-----------------------");
        StepVerifier.create(publisher)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }

    @Test
    void fluxSubscriberIntervalOne() throws InterruptedException {
        Flux<Long> interval = Flux.interval(Duration.ofMillis(100))
                .take(10)
                .log();

        interval.subscribe(i -> log.info("Number: {}", i));

        Thread.sleep(3000);

        StepVerifier.create(interval)
                .expectNext(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L)
                .verifyComplete();
    }

    @Test
    void fluxSubscriberIntervalTwo() {
        StepVerifier.withVirtualTime(this::createIntervalOfOneDay)
                .expectSubscription()
                .expectNoEvent(Duration.ofHours(25)) // Make sure nothing is being published
                .thenAwait(Duration.ofDays(1))
                .expectNext(0L)
                .thenAwait(Duration.ofDays(1))
                .expectNext(1L)
                .thenCancel()
                .verify();
    }

    private Flux<Long> createIntervalOfOneDay() {
        return Flux.interval(Duration.ofDays(1))
                .log();
    }

    @Test
    void connectableFlux() {
        ConnectableFlux<Integer> connectableFlux = Flux.range(1, 10)
                .delayElements(Duration.ofMillis(100))
                .publish();

//        connectableFlux.connect();
//
//        log.info("Thread sleeping of 300ms");
//        Thread.sleep(300);
//
//        connectableFlux.subscribe(i -> log.info("Sub1 number: {}", i));
//
//        log.info("Thread sleeping of 300ms");
//        Thread.sleep(300);
//
//        connectableFlux.subscribe(i -> log.info("Sub2 number: {}", i));

        StepVerifier.create(connectableFlux)
                .then(connectableFlux::connect)
                .thenConsumeWhile(i -> i <= 5)
                .expectNext(6, 7, 8, 9, 10)
                .expectComplete()
                .verify();
    }
}
