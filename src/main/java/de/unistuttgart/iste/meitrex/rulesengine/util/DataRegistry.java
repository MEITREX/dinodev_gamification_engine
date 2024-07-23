package de.unistuttgart.iste.meitrex.rulesengine.util;

import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * A registry for data objects that can be identified by a unique identifier.
 *
 * @param <T> the type of the data objects
 * @param <I> the type of the unique identifier
 */
@RequiredArgsConstructor
public abstract class DataRegistry<T, I> {

    protected final Map<I, T> predefinedData;

    protected abstract I getId(T data);

    public Optional<T> findById(I id) {
        return Optional.ofNullable(predefinedData.get(id));
    }

    public Collection<T> getAll() {
        return predefinedData.values();
    }

    public boolean containsId(I id) {
        return predefinedData.containsKey(id);
    }

    public void register(T data) {
        predefinedData.put(getId(data), data);
    }

    public void unregister(I id) {
        predefinedData.remove(id);
    }
}
