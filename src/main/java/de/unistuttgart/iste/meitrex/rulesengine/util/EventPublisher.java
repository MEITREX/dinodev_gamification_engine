package de.unistuttgart.iste.meitrex.rulesengine.util;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * A class for publishing events and managing an event stream. This class allows for the publishing of events and
 * provides a Flux stream of events for subscribers. It ensures that events are persisted and handles the case where
 * there are no subscribers or an error occurs in the event stream.
 * <p>
 * In contrast to similar pub-sub classes, the {@link #publishEvent(Object)} method can be called multiple times with
 * the same event request. If the event already exists, the subscribers will not be notified again.
 *
 * @param <E> the event type
 * @param <R> the event request type
 */
@Slf4j
@RequiredArgsConstructor
public class EventPublisher<E, R> {

    private Sinks.Many<E> sink        = initSink();
    @Nullable
    private Flux<E>       eventStream = null;

    @Getter
    private final EventPersistence<E, R> eventPersistence;

    /**
     * Publishes an event based on the given event request. If the event already exists, it is retrieved and returned.
     * Otherwise, the event is persisted and then emitted to the event stream. If emitting the event fails, it logs the
     * error accordingly.
     *
     * @param eventRequest the event request to publish
     * @return the published or retrieved event
     */
    public synchronized E publishEvent(R eventRequest) {
        if (eventPersistence.exists(eventRequest)) {
            return eventPersistence.getEvent(eventRequest);
        }

        E event = eventPersistence.persistEvent(eventRequest);

        Sinks.EmitResult result = emitRetryOnFail(event);
        if (result.isFailure()) {
            logFailure(result);
        }

        return event;
    }

    private void logFailure(Sinks.EmitResult emitResult) {
        switch (emitResult) {
            case FAIL_ZERO_SUBSCRIBER -> log.warn("Trying to emit event without subscribers");
            case FAIL_NON_SERIALIZED -> log.error("Failed to emit event due to non-serialized sink");
            case FAIL_CANCELLED -> log.error("Failed to emit event due to cancelled sink");
            case FAIL_OVERFLOW -> log.error("Failed to emit event because the sink has no buffer space");
            case FAIL_TERMINATED -> log.error("Failed to emit event because the sink has been terminated");
            default -> log.error("Failed to emit event due to unknown error");
        }
    }

    /**
     * Closes the event stream. Should only be used when the stream is no longer needed.
     */
    protected void close() {
        sink.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
    }

    /**
     * Provides a Flux stream of events that can be subscribed to.
     *
     * @return the Flux stream of events
     */
    public synchronized Flux<E> getEventStream() {
        if (eventStream == null) {
            eventStream = sink.asFlux();
            // we add a subscriber to the event stream to ensure that the stream is active
            // otherwise, the event sink would complete when the subscriber count reaches zero
            //noinspection CallingSubscribeInNonBlockingScope
            eventStream.subscribe(
                    event -> log.trace("Received event: {}", event),
                    error -> log.error("Error occurred", error),
                    () -> {
                        eventStream = null;
                        sink = initSink();
                        log.warn("Event stream completed");
                    }
            );
        }
        return eventStream;
    }

    private synchronized <T> Sinks.Many<T> initSink() {
        // many() to allow multiple signals
        // multicast() to share the sink among multiple subscribers
        // onBackpressureBuffer() to buffer signals until they can be processed
        return Sinks.many().multicast().onBackpressureBuffer();
    }

    private synchronized Sinks.EmitResult emitRetryOnFail(E event) {
        Sinks.EmitResult result = sink.tryEmitNext(event);

        if (result.isFailure() && result != Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER) {
            return sink.tryEmitNext(event);
        }

        return result;
    }
}
