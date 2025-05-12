package com.teragrep.cfe_16.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EventStringTest {

    @Test
    @DisplayName("node() returns the jsonNode field if not null")
    void nodeReturnsTheJsonNodeFieldIfNotNull() throws JsonProcessingException {
        final EventString eventString = new EventString("{\"event\": {}}");

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode expectedJsonNode = mapper.createObjectNode().set("event", mapper.createObjectNode());

        Assertions.assertEquals(expectedJsonNode, eventString.node(), "Returned JsonNode doesn't match the expected one");
    }

    @Test
    @DisplayName("node() throws JsonProcessingException if event is not in JSON format")
    void nodeThrowsJsonProcessingExceptionIfEventIsNotInJsonFormat() {
        final EventString eventString = new EventString("NotJson");

        Assertions.assertThrows(JsonProcessingException.class, eventString::node);
    }

    @Test
    @DisplayName("Happy equals test")
    void happyEqualsTest() {
        final ObjectMapper objectMapper = new ObjectMapper();
        final EventString eventString1 = new EventString("{\"event\": {}}", objectMapper);
        final EventString eventString2 = new EventString("{\"event\": {}}", objectMapper);

        Assertions.assertEquals(eventString1, eventString2, "Happy equals test failed");
    }

    @Test
    @DisplayName("Unhappy equals test")
    void unhappyEqualsTest() {
        final ObjectMapper objectMapper = new ObjectMapper();
        final EventString eventString1 = new EventString("{\"event\": {\"data\": {}}", objectMapper);
        final EventString eventString2 = new EventString("{\"event\": {}", objectMapper);

        Assertions.assertNotEquals(eventString1, eventString2, "Unhappy equals test failed");
    }
}