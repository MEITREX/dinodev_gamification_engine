package de.unistuttgart.iste.meitrex.rulesengine;

import de.unistuttgart.iste.meitrex.generated.dto.EventType;
import de.unistuttgart.iste.meitrex.rulesengine.util.DataRegistry;

import java.util.*;

/**
 * Registry for event types. Automatically contains the {@link DefaultEventTypes}.
 */
public class EventTypeRegistry extends DataRegistry<EventType, String> {

    public EventTypeRegistry() {
        super(new HashMap<>(Map.ofEntries(
                entry(DefaultEventTypes.USER_MESSAGE),
                entry(DefaultEventTypes.UNKNOWN),
                entry(DefaultEventTypes.SYSTEM_MESSAGE)
        )));
    }

    @Override
    protected String getId(EventType data) {
        return data.getIdentifier();
    }

    private static Map.Entry<String, EventType> entry(EventType eventType) {
        return Map.entry(eventType.getIdentifier(), eventType);
    }
}
