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
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HECTimeObjectImplTest {

    @Test
    @DisplayName("isDouble() returns true for a DoubleNode")
    void isDoubleReturnsTrueForADoubleNode() {
        final JsonNode jsonNode = new DoubleNode(2.2);

        final TimeObjectImpl timeObject = new TimeObjectImpl(jsonNode);

        Assertions.assertTrue(timeObject::isDouble);
    }

    @Test
    @DisplayName("isDouble() returns false for an IntNode")
    void isDoubleReturnsFalseForAnIntNode() {
        final JsonNode jsonNode = new IntNode(2);

        final TimeObjectImpl timeObject = new TimeObjectImpl(jsonNode);

        Assertions.assertFalse(timeObject::isDouble);
    }

    @Test
    @DisplayName("isDouble() returns false for a LongNode")
    void isDoubleReturnsFalseForALongNode() {
        final JsonNode jsonNode = new LongNode(2L);

        final TimeObjectImpl timeObject = new TimeObjectImpl(jsonNode);

        Assertions.assertFalse(timeObject::isDouble);
    }

    @Test
    @DisplayName("isDouble() returns false for an ObjectNode")
    void isDoubleReturnsFalseForAnObjectNode() {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode jsonNode = objectMapper.createObjectNode();

        final TimeObjectImpl timeObject = new TimeObjectImpl(jsonNode);

        Assertions.assertFalse(timeObject::isDouble);
    }

    @Test
    @DisplayName("isDouble() returns false for a TextNode")
    void isDoubleReturnsFalseForATextNode() {
        final JsonNode jsonNode = new TextNode("2");

        final TimeObjectImpl timeObject = new TimeObjectImpl(jsonNode);

        Assertions.assertFalse(timeObject::isDouble);
    }

    @Test
    @DisplayName("isDouble() returns false for a NullNode")
    void isDoubleReturnsFalseForANullNode() {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode jsonNode = objectMapper.nullNode();

        final TimeObjectImpl timeObject = new TimeObjectImpl(jsonNode);

        Assertions.assertFalse(timeObject::isDouble);
    }

    @Test
    @DisplayName("asDouble() returns value as double for a DoubleNode")
    void asDoubleReturnsValueAsDoubleForADoubleNode() {
        final JsonNode jsonNode = new DoubleNode(2.2);

        final TimeObjectImpl timeObject = new TimeObjectImpl(jsonNode);

        Assertions.assertEquals(2.2, timeObject.asDouble(), "asDouble() did not return the correct value");
    }

    @Test
    @DisplayName("canConvertToLong() returns true for an IntNode")
    void canConvertToLongReturnsTrueForAnIntNode() {
        final JsonNode jsonNode = new IntNode(2);

        final TimeObjectImpl timeObject = new TimeObjectImpl(jsonNode);

        Assertions.assertTrue(timeObject::canConvertToLong);
    }

    @Test
    @DisplayName("canConvertToLong() returns true for a LongNode")
    void canConvertToLongReturnsTrueForALongNode() {
        final JsonNode jsonNode = new LongNode(2L);

        final TimeObjectImpl timeObject = new TimeObjectImpl(jsonNode);

        Assertions.assertTrue(timeObject::canConvertToLong);
    }

    @Test
    @DisplayName("canConvertToLong() returns false for a TextNode")
    void canConvertToLongReturnsFalseForATextNode() {
        final JsonNode jsonNode = new TextNode("2");

        final TimeObjectImpl timeObject = new TimeObjectImpl(jsonNode);

        Assertions.assertFalse(timeObject::canConvertToLong);
    }

    @Test
    @DisplayName("asLong() converts an IntNode to long")
    void asLongConvertsAnIntNodeToLong() {
        final JsonNode jsonNode = new IntNode(2);

        final TimeObjectImpl timeObject = new TimeObjectImpl(jsonNode);

        Assertions.assertEquals(2L, timeObject.asLong());
    }

    @Test
    @DisplayName("asLong() converts a TextNode containing numbers to a long")
    void asLongConvertsATextNodeContainingNumbersToALong() {
        final JsonNode jsonNode = new TextNode("2");

        final TimeObjectImpl timeObject = new TimeObjectImpl(jsonNode);

        Assertions.assertEquals(2L, timeObject.asLong());
    }

    @Test
    @DisplayName("asLong(defaultValue) uses the defaultValue if a TextNode contains at least one letter")
    void asLongDefaultValueUsesTheDefaultValueIfATextNodeContainsAtLeastOneLetter() {
        final JsonNode jsonNode = new TextNode("2a");

        final TimeObjectImpl timeObject = new TimeObjectImpl(jsonNode);

        Assertions.assertEquals(9L, timeObject.asLong(9L));
    }

    @Test
    @DisplayName("isTextual() returns true for a TextNode")
    void isTextualReturnsTrueForATextNode() {
        final JsonNode jsonNode = new TextNode("2");

        final TimeObjectImpl timeObject = new TimeObjectImpl(jsonNode);

        Assertions.assertTrue(timeObject::isTextual);
    }

    @Test
    @DisplayName("isTextual() returns false for an ObjectNode")
    void isTextualReturnsFalseForAnObjectNode() {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode jsonNode = objectMapper.createObjectNode();

        final TimeObjectImpl timeObject = new TimeObjectImpl(jsonNode);

        Assertions.assertFalse(timeObject::isTextual);
    }

    @Test
    @DisplayName("asText converts an IntNode to a String")
    void asTextConvertsANIntNodeToAString() {
        final JsonNode jsonNode = new IntNode(1234);

        final TimeObjectImpl timeObject = new TimeObjectImpl(jsonNode);

        Assertions.assertEquals("1234", timeObject.asText());
    }

    @Test
    @DisplayName("isStub() returns false")
    void isStubReturnsFalse() {
        final JsonNode jsonNode = new IntNode(1234);

        final TimeObjectImpl timeObject = new TimeObjectImpl(jsonNode);

        Assertions.assertFalse(timeObject::isStub);
    }

    @Test
    @DisplayName("Happy equals test")
    void happyEqualsTest() {
        final JsonNode jsonNode = new IntNode(1234);

        final TimeObjectImpl timeObject1 = new TimeObjectImpl(jsonNode);
        final TimeObjectImpl timeObject2 = new TimeObjectImpl(jsonNode);

        Assertions.assertEquals(timeObject1, timeObject2);
    }

    @Test
    @DisplayName("Unhappy equals test")
    void unhappyEqualsTest() {
        final JsonNode jsonNode1 = new IntNode(1234);
        final JsonNode jsonNode2 = new IntNode(12345);

        final TimeObjectImpl timeObject1 = new TimeObjectImpl(jsonNode1);
        final TimeObjectImpl timeObject2 = new TimeObjectImpl(jsonNode2);

        Assertions.assertNotEquals(timeObject1, timeObject2);
    }
}
