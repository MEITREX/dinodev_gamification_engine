package de.unistuttgart.iste.meitrex.rulesengine;

import de.unistuttgart.iste.meitrex.generated.dto.*;
import de.unistuttgart.iste.meitrex.rulesengine.util.EventPersistence;
import de.unistuttgart.iste.meitrex.rulesengine.util.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import reactor.test.StepVerifier;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Basic tests for the gamification engine.
 */
class GamificationEngineTest {

    @SuppressWarnings("unchecked")
    private final EventPersistence<Event, CreateEventInput> eventPersistence = mock(EventPersistence.class);

    private final EventPublisher<Event, CreateEventInput> eventPublisher = new EventPublisher<>(eventPersistence);

    private GamificationEngine gamificationEngine;

    private final DefaultEvent expectedEvent = DefaultEvent.builder()
            .setEventType(DefaultEventTypes.SYSTEM_MESSAGE)
            .build();

    @BeforeEach
    void setUp() {
        gamificationEngine = new GamificationEngine(eventPublisher);

        when(eventPersistence.exists(any(CreateEventInput.class))).thenReturn(false);
        when(eventPersistence.getEvent(any(CreateEventInput.class))).thenThrow(new RuntimeException(
                "should not be invoked"));
        when(eventPersistence.persistEvent(any())).thenReturn(expectedEvent);

        // Mock rules
        Rule systemMessageRuleFalse = new MockRule(DefaultEventTypes.SYSTEM_MESSAGE.getIdentifier(), false);
        Rule systemMessageRuleTrue = new MockRule(DefaultEventTypes.SYSTEM_MESSAGE.getIdentifier(), true);
        Rule otherEventRule = new MockRule("OTHER_EVENT", true);

        gamificationEngine.getRuleRegistry().register(systemMessageRuleFalse);
        gamificationEngine.getRuleRegistry().register(systemMessageRuleTrue);
        gamificationEngine.getRuleRegistry().register(otherEventRule);
    }

    @Test
    @Timeout(5)
    void testCorrectRuleActionsExecuted() {
        CreateEventInput event = CreateEventInput.builder()
                .setEventTypeIdentifier(DefaultEventTypes.SYSTEM_MESSAGE.getIdentifier())
                .setEventData(List.of(new DataFieldInput("message", AllowedDataType.STRING, "Test System Message")))
                .build();

        var stepVerifier = StepVerifier.create(eventPublisher.getEventStream());

        // Verify that the correct rule was executed
        var verifyLater = stepVerifier.expectNext(expectedEvent).thenCancel().verifyLater();

        assertDoesNotThrow(() -> gamificationEngine.getEventPublisher().publishEvent(event));

        verifyLater.verify();
    }

    static class MockRule implements Rule {

        private final String  triggerEventType;
        private final boolean conditionResult;

        MockRule(String triggerEventType, boolean conditionResult) {
            this.triggerEventType = triggerEventType;
            this.conditionResult = conditionResult;
        }

        @Override
        public UUID getId() {
            return UUID.randomUUID();
        }

        @Override
        public List<String> getTriggerEventTypeIdentifiers() {
            return List.of(triggerEventType);
        }

        @Override
        public boolean checkCondition(Event triggerEvent) {
            return conditionResult;
        }

        @Override
        public Optional<CreateEventInput> executeAction(Event triggerEvent) {
            if (!triggerEvent.getEventType().getIdentifier().equals(triggerEventType)) {
                fail("Rule should only be executed for the specified event type.");
            }

            if (!conditionResult) {
                fail("Action should not be executed if condition is false.");
            }
            return Optional.empty();
        }
    }
}
