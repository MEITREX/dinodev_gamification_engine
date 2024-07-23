package de.unistuttgart.iste.meitrex.rulesengine.util;

/**
 * Interface for persisting events.
 *
 * @param <E> the (result) type of events to persist
 * @param <R> the request/input type of events to persist
 */
public interface EventPersistence<E, R> {

    /**
     * Persists an event.
     *
     * @param eventRequest the event to persist
     * @return the persisted event
     * @apiNote This method is only called if {@link #exists(Object)} returns false.
     */
    E persistEvent(R eventRequest);

    /**
     * Checks if an event exists.
     *
     * @param eventRequest the event to check
     * @return true if the event exists, false otherwise
     */
    boolean exists(R eventRequest);

    /**
     * Gets an event from a request.
     *
     * @param event the event request
     * @return the event
     * @apiNote This method may only be called if {@link #exists(Object)} returns true.
     */
    E getEvent(R event);

}
