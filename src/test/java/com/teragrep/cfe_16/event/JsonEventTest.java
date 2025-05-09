package com.teragrep.cfe_16.event;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JsonEventTest {

    @Test
    @DisplayName("event() returns JsonNode if available")
    void eventReturnsJsonNodeIfAvailable() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.createObjectNode().set("event", mapper.createObjectNode());

        final JsonEvent jsonEvent = new JsonEvent(jsonNode);

        final JsonNode expectedJsonNode = mapper.createObjectNode();

        Assertions.assertEquals(expectedJsonNode, jsonEvent.event(), "Returned JsonNode doesn't match the expected one");
    }

    @Test
    @DisplayName("node() returns the jsonNode field if not null")
    void nodeReturnsTheJsonNodeFieldIfNotNull() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.createObjectNode().set("event", mapper.createObjectNode());

        final JsonEvent jsonEvent = new JsonEvent(jsonNode);

        final JsonNode expectedJsonNode = mapper.createObjectNode().set("event", mapper.createObjectNode());

        Assertions.assertEquals(expectedJsonNode, jsonEvent.node(), "Returned JsonNode doesn't match the expected one");
    }

    @Test
    @DisplayName("Happy equals test")
    void happyEqualsTest() {
        final ObjectMapper mapper = new ObjectMapper();

        final JsonEvent jsonEvent1 = new JsonEvent(mapper.createObjectNode().set("event", mapper.createObjectNode()));
        final JsonEvent jsonEvent2 = new JsonEvent(mapper.createObjectNode().set("event", mapper.createObjectNode()));

        Assertions.assertEquals(jsonEvent1, jsonEvent2, "Happy equals test failed");
    }

    @Test
    @DisplayName("Unhappy equals test")
    void unhappyEqualsTest() {
        final ObjectMapper mapper = new ObjectMapper();

        final JsonEvent jsonEvent1 = new JsonEvent(mapper.createObjectNode().set("event", mapper.createObjectNode()));
        final JsonEvent jsonEvent2 = new JsonEvent(mapper.createObjectNode().set("eventNotTheSame", mapper.createObjectNode()));

        Assertions.assertNotEquals(jsonEvent1, jsonEvent2, "Unhappy equals test failed");
    }
}