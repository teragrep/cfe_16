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

        Assertions
                .assertEquals(expectedJsonNode, eventString.node(), "Returned JsonNode doesn't match the expected one");
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
