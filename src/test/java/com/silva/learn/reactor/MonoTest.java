package com.silva.learn.reactor;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
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

        log.info("-----------------------");
        StepVerifier.create(publisher)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void monoSubscriberConsumerComplete() {
        String name = "Danilo Silva";
        Mono<String> publisher = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        publisher.subscribe(
                s -> log.info("Value: {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED"));

        log.info("-----------------------");
        StepVerifier.create(publisher)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    void monoSubscriberConsumerSubscription() {
        String name = "Danilo Silva";
        Mono<String> publisher = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        publisher.subscribe(
                s -> log.info("Value: {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED"),
                Subscription::cancel);

        log.info("-----------------------");
        StepVerifier.create(publisher)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    void monoSubscriberConsumerSubscriptionBackpressure() {
        String name = "Danilo Silva";
        Mono<String> publisher = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        publisher.subscribe(
                s -> log.info("Value: {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED"),
                subscription -> subscription.request(5));

        log.info("-----------------------");
        StepVerifier.create(publisher)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    void monoDoOnMethods() {
        String name = "Danilo Silva";
        Mono<Object> publisher = Mono.just(name)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> log.info("Subscribed"))
                .doOnRequest(longNumber -> log.info("Requested recevied, start doing something..."))
                .doOnNext(v -> log.info("Value is here. Executing doOnNext {}", v))
                .flatMap(v -> Mono.empty())
                .doOnNext(v -> log.info("Value is here. Executing doOnNext {}", v))
                .doOnSuccess(s -> log.info("doOnSuccess executed {}", s));

        publisher.subscribe(
                s -> log.info("Value: {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED"),
                subscription -> subscription.request(5));

        log.info("-----------------------");

        StepVerifier.create(publisher)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void monoDoOnError() {
        Mono<Object> monoError = Mono.error(new IllegalArgumentException("Illeal expected argument"))
                .doOnError(e -> MonoTest.log.error("Error message: {}", e.getMessage()))
                .doOnNext(s -> log.info("Executing this doOnNext"))
                .log();

        StepVerifier.create(monoError)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void monoOnErrorResume() {
        final var name = "Danilo Silva";
        Mono<Object> monoError = Mono.error(new IllegalArgumentException("Illeal expected argument"))
                .doOnError(e -> MonoTest.log.error("Error message: {}", e.getMessage()))
                .onErrorResume(s -> { // Fallback
                    log.info("Executing this doOnNext");
                    return Mono.just(name);
                })
                .log();

        StepVerifier.create(monoError)
                .expectNext("Danilo Silva")
                .verifyComplete();
    }

    @Test
    void monoOnErrorReturn() {
        final var empty = "EMPTY";
        Mono<Object> monoError = Mono.error(new IllegalArgumentException("Illegal expected argument"))
                .doOnError(e -> MonoTest.log.error("Error message: {}", e.getMessage()))
                .onErrorReturn(empty)
                .log();

        StepVerifier.create(monoError)
                .expectNext("EMPTY")
                .verifyComplete();
    }
}
