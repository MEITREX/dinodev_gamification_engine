package de.unistuttgart.iste.meitrex.rulesengine.util;

/**
 * A non-persistent event publisher that does not persist events, which uses the same type for the event and the event
 * request. The {@link #publishEvent(Object)} method will always transmit the event to the subscribers.
 *
 * @param <E> the type of events to publish
 */
public class NonPersistentEventPublisher<E> extends EventPublisher<E, E> {

    public NonPersistentEventPublisher() {
        super(new NoPersistence<>());
    }

    private static class NoPersistence<E> implements EventPersistence<E, E> {

        @Override
        public E persistEvent(E eventRequest) {
            return eventRequest;
        }

        @Override
        public boolean exists(E eventRequest) {
            return false;
        }

        @Override
        public E getEvent(E event) {
            return event;
        }
    }
}
