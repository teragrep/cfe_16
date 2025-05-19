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

        final ValidatedJsonEvent validatedJsonEvent = new ValidatedJsonEvent(new DefaultJsonEvent(jsonNode));

        final Exception exception = Assertions
                .assertThrowsExactly(EventFieldBlankException.class, validatedJsonEvent::event);

        Assertions.assertEquals("jsonEvent node's event not valid", exception.getMessage());
    }

    @Test
    @DisplayName("event() throws EventFieldBlankException if message node value is an empty String")
    void eventThrowsEventFieldBlankExceptionIfMessageNodeValueIsAnEmptyString() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.createObjectNode().set("event", mapper.createObjectNode().put("message", ""));

        final ValidatedJsonEvent validatedJsonEvent = new ValidatedJsonEvent(new DefaultJsonEvent(jsonNode));

        final Exception exception = Assertions
                .assertThrowsExactly(EventFieldBlankException.class, validatedJsonEvent::event);

        Assertions.assertEquals("jsonEvent node's event not valid", exception.getMessage());
    }

    @Test
    @DisplayName("event() throws EventFieldBlankException if message node value is an empty Object")
    void eventThrowsEventFieldBlankExceptionIfMessageNodeValueIsAnEmptyObject() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper
                .createObjectNode()
                .set("event", mapper.createObjectNode().set("message", mapper.createObjectNode()));

        final ValidatedJsonEvent validatedJsonEvent = new ValidatedJsonEvent(new DefaultJsonEvent(jsonNode));

        final Exception exception = Assertions
                .assertThrowsExactly(EventFieldBlankException.class, validatedJsonEvent::event);

        Assertions.assertEquals("jsonEvent node's event not valid", exception.getMessage());
    }

    @Test
    @DisplayName("event() returns JsonNode if message exists and is a filled String")
    void eventReturnsJsonNodeIfMessageExistsAndIsAFilledString() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper
                .createObjectNode()
                .set("event", mapper.createObjectNode().put("message", "Valid event"));

        final ValidatedJsonEvent validatedJsonEvent = new ValidatedJsonEvent(new DefaultJsonEvent(jsonNode));

        final JsonNode returnedNode = Assertions.assertDoesNotThrow(validatedJsonEvent::event);

        final JsonNode expectedNode = mapper.convertValue("Valid event", JsonNode.class);

        Assertions.assertEquals(expectedNode, returnedNode);
    }
}
