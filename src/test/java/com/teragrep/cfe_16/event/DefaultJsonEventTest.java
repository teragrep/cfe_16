package com.teragrep.cfe_16.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultJsonEventTest {

    @Test
    @DisplayName("event() returns JsonNode if available")
    void eventReturnsJsonNodeIfAvailable() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.createObjectNode().set("event", mapper.createObjectNode());

        final DefaultJsonEvent defaultJsonEvent = new DefaultJsonEvent(jsonNode);

        final JsonNode expectedJsonNode = mapper.createObjectNode();

        Assertions.assertEquals(expectedJsonNode, defaultJsonEvent.event(), "Returned JsonNode doesn't match the expected one");
    }

    @Test
    @DisplayName("node() returns the jsonNode field if not null")
    void nodeReturnsTheJsonNodeFieldIfNotNull() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.createObjectNode().set("event", mapper.createObjectNode());

        final DefaultJsonEvent defaultJsonEvent = new DefaultJsonEvent(jsonNode);

        final JsonNode expectedJsonNode = mapper.createObjectNode().set("event", mapper.createObjectNode());

        Assertions.assertEquals(expectedJsonNode, defaultJsonEvent.node(), "Returned JsonNode doesn't match the expected one");
    }

    @Test
    @DisplayName("Happy equals test")
    void happyEqualsTest() {
        final ObjectMapper mapper = new ObjectMapper();

        final DefaultJsonEvent defaultJsonEvent1 = new DefaultJsonEvent(mapper.createObjectNode().set("event", mapper.createObjectNode()));
        final DefaultJsonEvent defaultJsonEvent2 = new DefaultJsonEvent(mapper.createObjectNode().set("event", mapper.createObjectNode()));

        Assertions.assertEquals(defaultJsonEvent1, defaultJsonEvent2, "Happy equals test failed");
    }

    @Test
    @DisplayName("Unhappy equals test")
    void unhappyEqualsTest() {
        final ObjectMapper mapper = new ObjectMapper();

        final DefaultJsonEvent defaultJsonEvent1 = new DefaultJsonEvent(mapper.createObjectNode().set("event", mapper.createObjectNode()));
        final DefaultJsonEvent defaultJsonEvent2 = new DefaultJsonEvent(mapper.createObjectNode().set("eventNotTheSame", mapper.createObjectNode()));

        Assertions.assertNotEquals(defaultJsonEvent1, defaultJsonEvent2, "Unhappy equals test failed");
    }
}