package com.teragrep.cfe_16.event;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teragrep.cfe_16.exceptionhandling.EventFieldBlankException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ValidatedJsonEventMessagesTest {

    @Test
    @DisplayName("event() throws EventFieldBlankException if message node value is not a String")
    void eventThrowsEventFieldBlankExceptionIfMessageNodeValueIsNotAString() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.createObjectNode().set("event", mapper.createObjectNode().put("message", 123));

        final ValidatedJsonEvent validatedJsonEvent = new ValidatedJsonEvent(
            new DefaultJsonEvent(
                jsonNode
            )
        );

        final Exception exception = Assertions.assertThrowsExactly(EventFieldBlankException.class, validatedJsonEvent::event);

        Assertions.assertEquals("jsonEvent node's event not valid", exception.getMessage());
    }

    @Test
    @DisplayName("event() throws EventFieldBlankException if message node value is an empty String")
    void eventThrowsEventFieldBlankExceptionIfMessageNodeValueIsAnEmptyString() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.createObjectNode().set("event", mapper.createObjectNode().put("message", ""));

        final ValidatedJsonEvent validatedJsonEvent = new ValidatedJsonEvent(
            new DefaultJsonEvent(
                jsonNode
            )
        );

        final Exception exception = Assertions.assertThrowsExactly(EventFieldBlankException.class, validatedJsonEvent::event);

        Assertions.assertEquals("jsonEvent node's event not valid", exception.getMessage());
    }

    @Test
    @DisplayName("event() throws EventFieldBlankException if message node value is an empty Object")
    void eventThrowsEventFieldBlankExceptionIfMessageNodeValueIsAnEmptyObject() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.createObjectNode().set("event", mapper.createObjectNode().set("message", mapper.createObjectNode()));

        final ValidatedJsonEvent validatedJsonEvent = new ValidatedJsonEvent(
            new DefaultJsonEvent(
                jsonNode
            )
        );

        final Exception exception = Assertions.assertThrowsExactly(EventFieldBlankException.class, validatedJsonEvent::event);

        Assertions.assertEquals("jsonEvent node's event not valid", exception.getMessage());
    }

    @Test
    @DisplayName("event() returns JsonNode if message exists and is a filled String")
    void eventReturnsJsonNodeIfMessageExistsAndIsAFilledString() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.createObjectNode().set("event", mapper.createObjectNode().put("message", "Valid event"));

        final ValidatedJsonEvent validatedJsonEvent = new ValidatedJsonEvent(
            new DefaultJsonEvent(
                jsonNode
            )
        );

        final JsonNode returnedNode = Assertions.assertDoesNotThrow(validatedJsonEvent::event);

        final JsonNode expectedNode = mapper.convertValue("Valid event", JsonNode.class);

        Assertions.assertEquals(expectedNode, returnedNode);
    }
}