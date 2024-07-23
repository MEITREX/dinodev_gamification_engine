# Gamification Engine

This repository contains the gamification engine of [DinoDev](https://github.com/MEITREX/dinodev), but it can be used
for other projects as well.
See also the [DinoDev Wiki](https://github.com/MEITREX/dinodev/wiki/).

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=MEITREX_gamification_engine&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=MEITREX_gamification_engine)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=MEITREX_gamification_engine&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=MEITREX_gamification_engine)

## Concept

The concept is inspired by the work
of [Garcia et al.](https://www.sciencedirect.com/science/article/pii/S0164121217301218?casa_token=s-cYhGuPw1AAAAAA:FUTHVyjbcEeDX3PVIBFWNGp3LQdcmTj3rORLPnUW239XbvYrFg82kqyDq2R_rVqA1LVqvL5ZFf4)
There are three main elements in the gamification engine:

- **Events**: Events represent user behavior that can be tracked and rewarded. For example, a user reviews a pull
  request or creates a new issue.
- **Event Types**: Each event has a type. The type defines the event's properties.
- **Rules**: Rules define actions which are executed when an event occurs and a condition is met. For example, a user
  reviews a pull request and receives 10 points.

This engine is completely generic, there are no game elements implemented. You can define your own event types and
rules.

## EventPublisher

This repository contains an event publisher which can be used whenever a pub-sub pattern is needed. It is based
on [Project Reactor](https://projectreactor.io/).

## Example usage

The following example assumes a Spring Boot application, but the gamification engine is not bound to Spring Boot.

### Define an event type

Define the event type you need using the builder pattern. The following example defines an event type for gaining XP.

```java
public static final EventType XP_GAIN = DefaultEventType.builder()
        .setIdentifier("XP_GAIN")
        .setDescription("A user gained XP.")
        .setDefaultVisibility(EventVisibility.PRIVATE)
        .setEventSchema(DefaultSchemaDefinition.builder().setFields(
                        List.of(
                                DefaultFieldSchemaDefinition.builder()
                                        .setName("xp")
                                        .setType(AllowedDataType.INTEGER)
                                        .setDescription("The amount of XP gained.")
                                        .setRequired(true)
                                        .build()))
                .build())
        .setMessageTemplate("gained ${xp} XP!")
        .build();
```

### Register the event types

Register the event types in the `EventTypeRegistry`.

```java
@Bean
public EventTypeRegistry eventTypeRegistry() {
    EventTypeRegistry eventTypeRegistry = new EventTypeRegistry();

    eventTypeRegistry.register(XP_GAIN);
    eventTypeRegistry.register(ISSUE_CREATED);
    // add any other event types here

    return eventTypeRegistry;
}
```

### Implement a rule

Implement a rule that rewards the user with XP.

```java
public class XpGainRule implements Rule {

    @Override
    public List<String> getTriggerEventTypeIdentifiers() {
        return List.of(ISSUE_CREATED.getIdentifier());
    }

    @Override
    public boolean checkCondition(Event triggerEvent) {
        return true; // all events of type ISSUE_CREATED trigger this rule
    }

    @Override
    public synchronized Optional<CreateEventInput> executeAction(Event triggerEvent) {
        // add your logic here

        return Optional.of(getXpGainEvent(triggerEvent, xpToAdd));
    }

    private CreateEventInput getXpGainEvent(Event triggerEvent, int xpToAdd) {
        return CreateEventInput.builder()
                .setEventTypeIdentifier(XP_GAIN.getIdentifier())
                .setProjectId(triggerEvent.getProjectId())
                .setUserId(triggerEvent.getUserId())
                .setEventData(List.of(
                        new TemplateFieldInput("xp", AllowedDataType.INTEGER, Integer.toString(xpToAdd))))
                .setMessage(getXpMessage(triggerEvent, xpToAdd))
                .setParentId(triggerEvent.getId())
                .build();
    }
}
```

### Register the rules

Register the rules in the `RuleRegistry`.
The following code registers all bean of type `Rule` in the `RuleRegistry`.

```java
@Bean
RuleRegistry ruleRegistry(ApplicationContext applicationContext) {
    RuleRegistry ruleRegistry = new RuleRegistry();

    Map<String, Rule> ruleBeans = applicationContext.getBeansOfType(Rule.class);

    // Register each Rule bean
    for (Rule rule : ruleBeans.values()) {
        ruleRegistry.register(rule);
    }

    return ruleRegistry;
}
```

### Provide an event publisher

```java
@Bean
public DefaultEventPublisher eventPublisher(EventPersistenceService eventService) {
    return new DefaultEventPublisher(eventService);
}
```

### Setup the gamification engine

```java
@Bean
public GamificationEngine gamificationEngine(
        EventPublisher<Event, CreateEventInput> eventPublisher,
        EventTypeRegistry eventTypeRegistry,
        RuleRegistry ruleRegistry
) {
    return new GamificationEngine(eventPublisher, ruleRegistry, eventTypeRegistry);
}
```

### Publish an event

```java
public class IssueService {

    private final EventPublisher<Event, CreateEventInput> eventPublisher;

    public IssueService(EventPublisher<Event, CreateEventInput> eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void createIssue(String projectId, String userId) {
        // issue creation logic
        
        Event event = Event.builder()
                .setEventTypeIdentifier(ISSUE_CREATED.getIdentifier())
                .setProjectId(projectId)
                .setUserId(userId)
                // ....
                .build();

        eventPublisher.publish(event);
        
        // the gamification engine will execute the rules
    }
}
```

```