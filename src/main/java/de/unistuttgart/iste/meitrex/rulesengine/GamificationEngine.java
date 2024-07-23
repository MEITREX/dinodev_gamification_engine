package de.unistuttgart.iste.meitrex.rulesengine;

import de.unistuttgart.iste.meitrex.generated.dto.CreateEventInput;
import de.unistuttgart.iste.meitrex.generated.dto.Event;
import de.unistuttgart.iste.meitrex.rulesengine.util.EventPublisher;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;

/**
 * The GamificationEngine class is responsible for processing events, applying rules, and executing actions if the
 * conditions of the rules are met.
 */
@Setter
@Getter
@Slf4j
public class GamificationEngine {

    /**
     * The EventPublisher used by the GamificationEngine. The engine subscribes to the EventPublisher to check rules and
     * execute actions.
     */
    private EventPublisher<Event, CreateEventInput> eventPublisher;

    /**
     * The RuleRegistry used by the GamificationEngine. Add rules here to be used by the engine.
     */
    private RuleRegistry ruleRegistry = new RuleRegistry();

    /**
     * Registry for event types.
     */
    private EventTypeRegistry eventTypeRegistry = new EventTypeRegistry();

    /**
     * Constructs a new GamificationEngine with the given EventPublisher.
     *
     * @param eventPublisher the EventPublisher used by the GamificationEngine
     */
    public GamificationEngine(EventPublisher<Event, CreateEventInput> eventPublisher) {
        this.eventPublisher = eventPublisher;
        initialize();
    }

    /**
     * Constructs a new GamificationEngine with the given EventPublisher, RuleRegistry, and EventTypeRegistry.
     */
    public GamificationEngine(EventPublisher<Event, CreateEventInput> eventPublisher,
            RuleRegistry ruleRegistry,
            EventTypeRegistry eventTypeRegistry) {
        this.eventPublisher = eventPublisher;
        this.ruleRegistry = ruleRegistry;
        this.eventTypeRegistry = eventTypeRegistry;
        initialize();
    }

    private void initialize() {
        eventPublisher.getEventStream().subscribe(this::processEvent, this::handleError, this::handleCompletion);
    }

    private void processEvent(Event event) {
        log.info("Received event: {}", formatEvent(event));

        ruleRegistry.getRulesForEventType(event.getEventType().getIdentifier())
                .stream()
                .filter(rule -> rule.checkCondition(event))
                .map(rule -> rule.executeAction(event))
                .forEach(responseEvent -> responseEvent.ifPresent(eventPublisher::publishEvent));
    }

    private void handleError(Throwable t) {
        log.error("Error occurred", t);
        initialize();
    }

    private void handleCompletion() {
        log.warn("Event stream completed");
        initialize();
    }

    private String formatEvent(Event event) {
        return MessageFormat.format("[{0}][{1}] {2}, {3}",
                event.getVisibility(), event.getEventType().getIdentifier(), event.getMessage(), event.getEventData());
    }
}
