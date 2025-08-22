/*
 * HTTP Event Capture to RFC5424 CFE_16
 * Copyright (C) 2021-2025 Suomen Kanuuna Oy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 * Additional permission under GNU Affero General Public License version 3
 * section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with other code, such other code is not for that reason alone subject to any
 * of the requirements of the GNU Affero GPL version 3 as long as this Program
 * is the same Program as licensed from Suomen Kanuuna Oy without any additional
 * modifications.
 *
 * Supplemented terms under GNU Affero General Public License version 3
 * section 7
 *
 * Origin of the software must be attributed to Suomen Kanuuna Oy. Any modified
 * versions must be marked as "Modified version of" The Program.
 *
 * Names of the licensors and authors may not be used for publicity purposes.
 *
 * No rights are granted for use of trade names, trademarks, or service marks
 * which are in The Program if any.
 *
 * Licensee must indemnify licensors and authors for any liability that these
 * contractual assumptions impose on licensors and authors.
 *
 * To the extent this program is licensed as part of the Commercial versions of
 * Teragrep, the applicable Commercial License may apply to this file if you as
 * a licensee so wish it.
 */
package com.teragrep.cfe_16.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JsonEventImplTest {

    @Test
    @DisplayName("event() returns EventStub if node does not have event node")
    void eventReturnsEventStubIfNodeDoesNotHaveEventNode() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.createObjectNode().put("NotEvent", "eventData");

        final JsonEventImpl jsonEventImpl = new JsonEventImpl(jsonNode);

        final EventMessage returnedEventMessage = jsonEventImpl.asEvent();

        Assertions.assertTrue(returnedEventMessage::isStub);
    }

    @Test
    @DisplayName("event() returns EventStub if event node is an integer")
    void eventReturnsEventStubIfEventNodeIsAnInteger() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.createObjectNode().put("event", 123);

        final JsonEventImpl jsonEventImpl = new JsonEventImpl(jsonNode);

        final EventMessage returnedEventMessage = jsonEventImpl.asEvent();

        Assertions.assertTrue(returnedEventMessage::isStub);
    }

    @Test
    @DisplayName("event() returns EventStub is event is an empty string")
    void eventReturnsEventStubIsEventIsAnEmptyString() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.createObjectNode().put("event", "");

        final JsonEventImpl jsonEventImpl = new JsonEventImpl(jsonNode);

        final EventMessage returnedEventMessage = jsonEventImpl.asEvent();

        Assertions.assertTrue(returnedEventMessage::isStub);
    }

    @Test
    @DisplayName("event() returns EventImpl if event exists and is a filled String")
    void eventReturnsEventImplIfEventExistsAndIsAFilledString() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.createObjectNode().put("event", "Valid event");

        final JsonEventImpl jsonEventImpl = new JsonEventImpl(jsonNode);

        final EventMessage returnedEventMessage = jsonEventImpl.asEvent();

        final EventMessage expectedEventMessage = new EventMessageImpl("Valid event");

        Assertions.assertEquals(expectedEventMessage, returnedEventMessage);
    }

    @Test
    @DisplayName("node() throws IllegalStateException if field is null")
    void asNodeThrowsIllegalStateExceptionIfFieldIsNull() {
        final JsonEventImpl jsonEventImpl = new JsonEventImpl(null);

        final Exception exception = Assertions.assertThrowsExactly(IllegalStateException.class, jsonEventImpl::asNode);

        Assertions.assertEquals("jsonEvent node not valid", exception.getMessage());
    }

    @Test
    @DisplayName("node() throws IllegalStateException if node is null")
    void nodeThrowsIllegalStateExceptionIfNodeIsNull() {
        final JsonEventImpl jsonEventImpl = new JsonEventImpl(new ObjectMapper().nullNode());

        final Exception exception = Assertions.assertThrowsExactly(IllegalStateException.class, jsonEventImpl::asNode);

        Assertions.assertEquals("jsonEvent node not valid", exception.getMessage());
    }

    @Test
    @DisplayName("node() throws IllegalStateException if node is not an object")
    void nodeThrowsIllegalStateExceptionIfNodeIsNotAnObject() {
        final JsonEventImpl jsonEventImpl = new JsonEventImpl(new ObjectMapper().createArrayNode());

        final Exception exception = Assertions.assertThrowsExactly(IllegalStateException.class, jsonEventImpl::asNode);

        Assertions.assertEquals("jsonEvent node not valid", exception.getMessage());
    }

    @Test
    @DisplayName("node() returns JsonNode if node exists and is an object")
    void nodeReturnsJsonNodeIfNodeExistsAndIsAnObject() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.createObjectNode().set("event", mapper.createObjectNode());

        final JsonEventImpl jsonEventImpl = new JsonEventImpl(jsonNode);

        final JsonNode returnedNode = Assertions.assertDoesNotThrow(jsonEventImpl::asNode);

        Assertions.assertEquals(jsonNode, returnedNode);
    }

    @Test
    @DisplayName("Happy equals test")
    void happyEqualsTest() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.createObjectNode().set("event", mapper.createObjectNode());

        final JsonEventImpl jsonEventImpl1 = new JsonEventImpl(jsonNode);

        final JsonEventImpl jsonEventImpl2 = new JsonEventImpl(jsonNode);

        Assertions.assertEquals(jsonEventImpl1, jsonEventImpl2);
    }

    @Test
    @DisplayName("Unhappy equals test")
    void unhappyEqualsTest() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.createObjectNode().set("event", mapper.createObjectNode());

        final JsonEventImpl jsonEventImpl1 = new JsonEventImpl(jsonNode);

        final JsonEventImpl jsonEventImpl2 = new JsonEventImpl(
                mapper.createObjectNode().set("event", mapper.createObjectNode().put("data", "data"))
        );

        Assertions.assertNotEquals(jsonEventImpl1, jsonEventImpl2);
    }
}
