package com.teragrep.cfe_16.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teragrep.cfe_16.exceptionhandling.EventFieldBlankException;
import com.teragrep.cfe_16.exceptionhandling.EventFieldMissingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ValidatedJsonEventTest {

    @Test
    @DisplayName("event() throws EventFieldMissingException if node does not have event node")
    void eventThrowsEventFieldMissingExceptionIfNodeDoesNotHaveEventNode() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.createObjectNode().put("NotEvent", "eventData");

        final ValidatedJsonEvent validatedJsonEvent = new ValidatedJsonEvent(
            new DefaultJsonEvent(
                jsonNode
            )
        );

        final Exception exception = Assertions.assertThrowsExactly(EventFieldMissingException.class, validatedJsonEvent::event);

        Assertions.assertEquals("event field is missing", exception.getMessage());
    }

    @Test
    @DisplayName("event() throws EventFieldBlankException if event node is an integer")
    void eventThrowsEventFieldBlankExceptionIfEventNodeIsAnInteger() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.createObjectNode().put("event", 123);

        final ValidatedJsonEvent validatedJsonEvent = new ValidatedJsonEvent(
            new DefaultJsonEvent(
                jsonNode
            )
        );

        final Exception exception = Assertions.assertThrowsExactly(EventFieldBlankException.class, validatedJsonEvent::event);

        Assertions.assertEquals("jsonEvent node's event not valid", exception.getMessage());
    }

    @Test
    @DisplayName("event() throws EventFieldBlankException is event is an empty string")
    void eventThrowsEventFieldBlankExceptionIsEventIsAnEmptyString() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.createObjectNode().put("event", "");

        final ValidatedJsonEvent validatedJsonEvent = new ValidatedJsonEvent(
            new DefaultJsonEvent(
                jsonNode
            )
        );

        final Exception exception = Assertions.assertThrowsExactly(EventFieldBlankException.class, validatedJsonEvent::event);

        Assertions.assertEquals("jsonEvent node's event not valid", exception.getMessage());
    }

    @Test
    @DisplayName("event() returns JsonNode if event exists and is a filled String")
    void eventReturnsJsonNodeIfEventExistsAndIsAFilledString() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.createObjectNode().put("event", "Valid event");

        final ValidatedJsonEvent validatedJsonEvent = new ValidatedJsonEvent(
            new DefaultJsonEvent(
                jsonNode
            )
        );

        final JsonNode returnedNode = Assertions.assertDoesNotThrow(validatedJsonEvent::event);

        final JsonNode expectedNode = mapper.convertValue("Valid event", JsonNode.class);

        Assertions.assertEquals(expectedNode, returnedNode);
    }

    @Test
    @DisplayName("node() throws IllegalStateException if field is null")
    void nodeThrowsIllegalStateExceptionIfFieldIsNull() {
        final ValidatedJsonEvent validatedJsonEvent = new ValidatedJsonEvent(
            new DefaultJsonEvent(
                null
            )
        );

        final Exception exception = Assertions.assertThrowsExactly(IllegalStateException.class, validatedJsonEvent::node);

        Assertions.assertEquals("jsonEvent node not valid", exception.getMessage());
    }

    @Test
    @DisplayName("node() throws IllegalStateException if node is null")
    void nodeThrowsIllegalStateExceptionIfNodeIsNull() {
        final ValidatedJsonEvent validatedJsonEvent = new ValidatedJsonEvent(
            new DefaultJsonEvent(
                new ObjectMapper().nullNode()
            )
        );

        final Exception exception = Assertions.assertThrowsExactly(IllegalStateException.class, validatedJsonEvent::node);

        Assertions.assertEquals("jsonEvent node not valid", exception.getMessage());
    }

    @Test
    @DisplayName("node() throws IllegalStateException if node is not an object")
    void nodeThrowsIllegalStateExceptionIfNodeIsNotAnObject() {
        final ValidatedJsonEvent validatedJsonEvent = new ValidatedJsonEvent(
            new DefaultJsonEvent(
                new ObjectMapper().createArrayNode()
            )
        );

        final Exception exception = Assertions.assertThrowsExactly(IllegalStateException.class, validatedJsonEvent::node);

        Assertions.assertEquals("jsonEvent node not valid", exception.getMessage());
    }

    @Test
    @DisplayName("node() returns JsonNode if node exists and is an object")
    void nodeReturnsJsonNodeIfNodeExistsAndIsAnObject() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.createObjectNode().set("event", mapper.createObjectNode());

        final ValidatedJsonEvent validatedJsonEvent = new ValidatedJsonEvent(
            new DefaultJsonEvent(
                jsonNode
            )
        );

        final JsonNode returnedNode = Assertions.assertDoesNotThrow(validatedJsonEvent::node);

        Assertions.assertEquals(jsonNode, returnedNode);
    }

    @Test
    @DisplayName("Happy equals test")
    void happyEqualsTest() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.createObjectNode().set("event", mapper.createObjectNode());

        final ValidatedJsonEvent validatedJsonEvent1 = new ValidatedJsonEvent(
            new DefaultJsonEvent(
                jsonNode
            )
        );

        final ValidatedJsonEvent validatedJsonEvent2 = new ValidatedJsonEvent(
            new DefaultJsonEvent(
                jsonNode
            )
        );

        Assertions.assertEquals(validatedJsonEvent1, validatedJsonEvent2);
    }

    @Test
    @DisplayName("Unhappy equals test")
    void unhappyEqualsTest() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.createObjectNode().set("event", mapper.createObjectNode());

        final ValidatedJsonEvent validatedJsonEvent1 = new ValidatedJsonEvent(
            new DefaultJsonEvent(
                jsonNode
            )
        );

        final ValidatedJsonEvent validatedJsonEvent2 = new ValidatedJsonEvent(
            new DefaultJsonEvent(
                mapper.createObjectNode().set("event", mapper.createObjectNode().put("data", "data"))
            )
        );

        Assertions.assertNotEquals(validatedJsonEvent1, validatedJsonEvent2);
    }
}