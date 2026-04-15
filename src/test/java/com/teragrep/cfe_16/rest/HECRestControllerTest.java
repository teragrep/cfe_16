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
package com.teragrep.cfe_16.rest;

import com.teragrep.cfe_16.response.AcknowledgedJsonResponse;
import com.teragrep.cfe_16.response.JsonResponse;
import com.teragrep.cfe_16.server.TestServer;
import com.teragrep.cfe_16.server.TestServerFactory;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tools.jackson.databind.JsonNode;

@TestPropertySource(properties = {
        "syslog.server.host=127.0.0.1",
        "syslog.server.port=1248",
        "syslog.server.protocol=RELP",
        "max.channels=1000000",
        "max.ack.value=1000000",
        "max.ack.age=20000",
        "max.session.age=30000",
        "poll.time=30000",
        "spring.devtools.add-properties=false",
        "server.print.times=true"
})
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class HECRestControllerTest {

    private static final int SERVER_PORT = 1248;
    private static final ConcurrentLinkedDeque<byte[]> messageList = new ConcurrentLinkedDeque<>();
    private static final AtomicLong openCount = new AtomicLong();
    private static final AtomicLong closeCount = new AtomicLong();
    private static TestServer server;
    @Autowired
    private HECRestController hecRestController;

    @BeforeAll
    static void init() {
        final TestServerFactory serverFactory = new TestServerFactory();
        server = Assertions
                .assertDoesNotThrow(() -> serverFactory.create(SERVER_PORT, messageList, openCount, closeCount));
        server.run();
    }

    @AfterAll
    static void cleanup() {
        Assertions.assertDoesNotThrow(() -> server.close());
    }

    @AfterEach
    void clear() {
        openCount.set(0);
        closeCount.set(0);
        messageList.clear();
    }

    @Test
    @DisplayName("test JSON sendEvents endpoint with channel present")
    void testJsonSendEventsEndpointWithChannelPresent() {
        final MockHttpServletRequest request1 = new MockHttpServletRequest();
        request1.addHeader("Authorization", "AUTH_TOKEN_11111");
        final String channel1 = "CHANNEL_11111";
        final String eventInJson = "{\"sourcetype\":\"access\", \"source\":\"/var/log/access.log\", "
                + "\"event\": {\"message\":\"Access log test message 1\"}} "
                + "{\"sourcetype\":\"access\", \"source\":\"/var/log/access.log\", \"event\": "
                + "{\"message\":\"Access log test message 2\"}}";

        final ResponseEntity<JsonNode> variable = Assertions
                .assertDoesNotThrow(() -> this.hecRestController.sendEvents(request1, eventInJson, channel1));
        final AcknowledgedJsonResponse expectedResponse = new AcknowledgedJsonResponse("Success", 0);
        final ResponseEntity<JsonNode> jsonNodeResponseEntity = expectedResponse.asJsonNodeResponseEntity();

        Assertions.assertEquals(jsonNodeResponseEntity, variable);
    }

    @Test
    @DisplayName("test JSON sendEvents endpoint without channel present")
    void testJsonSendEventsEndpointWithoutChannelPresent() {
        final MockHttpServletRequest request1 = new MockHttpServletRequest();
        request1.addHeader("Authorization", "AUTH_TOKEN_11111");
        final String eventInJson = "{\"sourcetype\":\"access\", \"source\":\"/var/log/access.log\", "
                + "\"event\": {\"message\":\"Access log test message 1\"}} "
                + "{\"sourcetype\":\"access\", \"source\":\"/var/log/access.log\", \"event\": "
                + "{\"message\":\"Access log test message 2\"}}";

        final ResponseEntity<JsonNode> responseEntity = Assertions
                .assertDoesNotThrow(() -> this.hecRestController.sendEvents(request1, eventInJson, null));
        final JsonResponse expectedResponse = new JsonResponse("Success");
        final ResponseEntity<JsonNode> expectedResponseEntity = expectedResponse.asJsonNodeResponseEntity();

        Assertions.assertEquals(expectedResponseEntity, responseEntity);
    }

    @Test
    @DisplayName("test multiValueMap sendEvents endpoint with channel present")
    void testMultiValueMapSendEventsEndpointWithChannelPresent() {
        final MockHttpServletRequest request1 = new MockHttpServletRequest();
        request1.addHeader("Authorization", "AUTH_TOKEN_11111");
        final String channel1 = "CHANNEL_11111";
        // Send JSON without the outer object brackets
        final String eventInJson = "\"sourcetype\":\"access\", \"source\":\"/var/log/access.log\", "
                + "\"event\": {\"message\":\"Access log test message 1\"}} "
                + "{\"sourcetype\":\"access\", \"source\":\"/var/log/access.log\", \"event\": "
                + "{\"message\":\"Access log test message 2\"}";
        final MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("channel", channel1);
        multiValueMap.add(eventInJson, null);

        final ResponseEntity<JsonNode> variable = Assertions
                .assertDoesNotThrow(() -> this.hecRestController.sendEvents(request1, multiValueMap, channel1));
        final AcknowledgedJsonResponse expectedResponse = new AcknowledgedJsonResponse("Success", 0);
        final ResponseEntity<JsonNode> jsonNodeResponseEntity = expectedResponse.asJsonNodeResponseEntity();

        Assertions.assertEquals(jsonNodeResponseEntity, variable);
    }

    @Test
    @DisplayName("test multiValueMap sendEvents endpoint without channel present")
    void testMultiValueMapSendEventsEndpointWithoutChannelPresent() {
        final MockHttpServletRequest request1 = new MockHttpServletRequest();
        request1.addHeader("Authorization", "AUTH_TOKEN_11111");
        // Send JSON without the outer object brackets
        final String eventInJson = "\"sourcetype\":\"access\", \"source\":\"/var/log/access.log\", "
                + "\"event\": {\"message\":\"Access log test message 1\"}} "
                + "{\"sourcetype\":\"access\", \"source\":\"/var/log/access.log\", \"event\": "
                + "{\"message\":\"Access log test message 2\"}";
        final MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add(eventInJson, null);

        final ResponseEntity<JsonNode> variable = Assertions
                .assertDoesNotThrow(() -> this.hecRestController.sendEvents(request1, multiValueMap, null));
        final JsonResponse expectedResponse = new JsonResponse("Success");
        final ResponseEntity<JsonNode> jsonNodeResponseEntity = expectedResponse.asJsonNodeResponseEntity();

        Assertions.assertEquals(jsonNodeResponseEntity, variable);
    }
}
