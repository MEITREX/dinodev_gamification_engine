package de.unistuttgart.iste.meitrex.rulesengine;

import de.unistuttgart.iste.meitrex.generated.dto.*;

import java.util.*;

public class DefaultEventTypes {

    private DefaultEventTypes() {
    }

    public static final DefaultEventType SYSTEM_MESSAGE = DefaultEventType.builder()
            .setIdentifier("SYSTEM_MESSAGE")
            .setDescription("A message from the system.")
            .setDefaultVisibility(EventVisibility.INTERNAL)
            .setEventSchema(DefaultSchemaDefinition.builder()
                    .setFields(
                            List.of(
                                    DefaultFieldSchemaDefinition.builder()
                                            .setName("message")
                                            .setType(AllowedDataType.STRING)
                                            .setDescription("The message.")
                                            .setRequired(true)
                                            .build()))
                    .build())
            .setMessageTemplate("${message}")
            .build();

    public static final DefaultEventType USER_MESSAGE = DefaultEventType.builder()
            .setIdentifier("PLAYER_MESSAGE")
            .setDescription("A message from a user.")
            .setDefaultVisibility(EventVisibility.PUBLIC)
            .setEventSchema(DefaultSchemaDefinition.builder()
                    .setFields(
                            List.of(
                                    DefaultFieldSchemaDefinition.builder()
                                            .setName("message")
                                            .setType(AllowedDataType.STRING)
                                            .setDescription("The message.")
                                            .setRequired(true)
                                            .build()))
                    .build())
            .setMessageTemplate("${message}")
            .build();

    public static final DefaultEventType UNKNOWN = DefaultEventType.builder()
            .setIdentifier("UNKNOWN")
            .setDescription("An unknown event.")
            .setDefaultVisibility(EventVisibility.INTERNAL)
            .setEventSchema(DefaultSchemaDefinition.builder().setFields(List.of()).build())
            .setMessageTemplate("This is an unknown event.")
            .build();
}
