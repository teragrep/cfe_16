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
package com.teragrep.cfe_16.it;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teragrep.cfe_16.Acknowledgements;
import com.teragrep.cfe_16.RequestHandler;
import com.teragrep.cfe_16.SessionManager;
import com.teragrep.cfe_16.TokenManager;
import com.teragrep.cfe_16.config.Configuration;
import com.teragrep.cfe_16.connection.RelpConnection;
import com.teragrep.cfe_16.exceptionhandling.*;
import com.teragrep.cfe_16.server.TestServer;
import com.teragrep.cfe_16.server.TestServerFactory;
import com.teragrep.cfe_16.service.HECService;
import com.teragrep.cfe_16.service.HECServiceImpl;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.*;

import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.*;

/*
 * Tests the functionality of HECServiceImpl
 */

public final class ServiceAndHECBatchIT {

    /*
     * Tests the sendEvents() and getAcks() method of the service.
     */
    @Test
    public void sendEventsAndGetAcksTest() {
        final int port = 1603;
        final ConcurrentLinkedDeque<byte[]> messageList = new ConcurrentLinkedDeque<>();
        final AtomicLong openCount = new AtomicLong();
        final AtomicLong closeCount = new AtomicLong();
        final TestServerFactory serverFactory = new TestServerFactory();
        final TestServer server = Assertions
                .assertDoesNotThrow(() -> serverFactory.create(port, messageList, openCount, closeCount));
        server.run();

        final Configuration configuration = new Configuration(
                "127.0.0.1",
                "RELP",
                port,
                100000,
                20000,
                30000,
                1000000,
                30000,
                true
        );

        final HECService service = new HECServiceImpl(
                new Acknowledgements(configuration),
                new SessionManager(),
                new TokenManager(),
                new RequestHandler(),
                configuration,
                new RelpConnection("127.0.0.1", port)
        );

        final String authToken1 = "AUTH_TOKEN_12223";
        final String authToken2 = "AUTH_TOKEN_16664";

        final MockHttpServletRequest request1 = new MockHttpServletRequest();
        request1.addHeader("Authorization", authToken1);
        final MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.addHeader("Authorization", authToken2);

        final String eventInJson = "{\"sourcetype\": \"mysourcetype\", \"event\": \"Hello, world!\", \"host\": \"localhost\", \"source\": \"mysource\", \"index\": \"myindex\"}";
        final String channel2 = "CHANNEL_22222";
        final String channel3 = "CHANNEL_33333";

        final ObjectMapper objectMapper = new ObjectMapper();
        final String ackRequest = "{\"acks\": [1,3,4]}";
        final JsonNode ackRequestNode = Assertions.assertDoesNotThrow(() -> objectMapper.readTree(ackRequest));

        final String firstResponse = "{\"text\":\"Success\",\"code\":0,\"ackID\":0}";
        Assertions
                .assertEquals(firstResponse, service.sendEvents(request1, channel3, eventInJson).toString(), "Service should return JSON object with fields 'text', 'code' and 'ackID' (ackID should be 0)");

        final String secondResponse = "{\"text\":\"Success\",\"code\":0,\"ackID\":1}";
        Assertions
                .assertEquals(secondResponse, service.sendEvents(request1, channel3, eventInJson).toString(), "Service should return JSON object with fields 'text', 'code' and 'ackID' (ackID should be 1)");

        final String supposedResponse = "{\"text\":\"Success\",\"code\":0,\"ackID\":0}";
        Assertions
                .assertEquals(supposedResponse, service.sendEvents(request1, channel2, eventInJson).toString(), "Service should return JSON object with fields 'text', 'code' and 'ackID' (ackID should be 0)");

        Assertions
                .assertEquals(supposedResponse, service.sendEvents(request2, channel3, eventInJson).toString(), "Service should return JSON object with fields 'text', 'code' and 'ackID' (ackID should be 0)");

        final String thirdResponse = "{\"acks\":{\"1\":true,\"3\":false,\"4\":false}}";
        Assertions
                .assertEquals(thirdResponse, service.getAcks(request1, channel3, ackRequestNode).toString(), "JSON object should be returned with ack statuses.");

        Assertions.assertDoesNotThrow(server::close);
    }

    /*
     * Tests sending a request with no authentication token. In this case
     * AuthenticationTokenMissingException is expected to happen.
     */
    @Test
    public void sendEventsWithoutAuthTokenTest() {
        final int port = 1604;
        final ConcurrentLinkedDeque<byte[]> messageList = new ConcurrentLinkedDeque<>();
        final AtomicLong openCount = new AtomicLong();
        final AtomicLong closeCount = new AtomicLong();

        final TestServerFactory serverFactory = new TestServerFactory();
        final TestServer server = Assertions
                .assertDoesNotThrow(() -> serverFactory.create(port, messageList, openCount, closeCount));
        server.run();

        final Configuration configuration = new Configuration(
                "127.0.0.1",
                "RELP",
                port,
                100000,
                20000,
                30000,
                1000000,
                30000,
                true
        );

        final HECService service = new HECServiceImpl(
                new Acknowledgements(configuration),
                new SessionManager(),
                new TokenManager(),
                new RequestHandler(),
                configuration,
                new RelpConnection("127.0.0.1", port)
        );

        final MockHttpServletRequest request = new MockHttpServletRequest();

        final String eventInJson = "{\"sourcetype\": \"mysourcetype\", \"event\": \"Hello, world!\", \"host\": \"localhost\", \"source\": \"mysource\", \"index\": \"myindex\"}";
        final String channel1 = "CHANNEL_11111";

        Assertions.assertThrows(AuthenticationTokenMissingException.class, () -> {
            service.sendEvents(request, eventInJson, channel1);
        });

        Assertions.assertDoesNotThrow(server::close);
    }

    /*
     * Tests sending a request with no channel provided. In this case no Ack id is
     * returned.
     */
    @Test
    public void sendEventsWithoutChannelTest() {
        final int port = 1605;
        final ConcurrentLinkedDeque<byte[]> messageList = new ConcurrentLinkedDeque<>();
        final AtomicLong openCount = new AtomicLong();
        final AtomicLong closeCount = new AtomicLong();
        final TestServerFactory serverFactory = new TestServerFactory();
        final TestServer server = Assertions
                .assertDoesNotThrow(() -> serverFactory.create(port, messageList, openCount, closeCount));
        server.run();

        final Configuration configuration = new Configuration(
                "127.0.0.1",
                "RELP",
                port,
                100000,
                20000,
                30000,
                1000000,
                30000,
                true
        );

        final HECService service = new HECServiceImpl(
                new Acknowledgements(configuration),
                new SessionManager(),
                new TokenManager(),
                new RequestHandler(),
                configuration,
                new RelpConnection("127.0.0.1", port)
        );

        final String authToken1 = "AUTH_TOKEN_12223";
        final String authToken2 = "AUTH_TOKEN_16664";

        final MockHttpServletRequest request1 = new MockHttpServletRequest();
        request1.addHeader("Authorization", authToken1);
        final MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.addHeader("Authorization", authToken2);

        final String eventInJson = "{\"sourcetype\": \"mysourcetype\", \"event\": \"Hello, world!\", \"host\": \"localhost\", \"source\": \"mysource\", \"index\": \"myindex\"}";

        final String supposedResponse = "{\"text\":\"Success\",\"code\":0}";
        final String response = service.sendEvents(request1, null, eventInJson).toString();
        Assertions
                .assertEquals(
                        supposedResponse, response, "Service should return JSON object with fields 'text' and 'code'"
                );

        Assertions.assertDoesNotThrow(server::close);
    }

    /*
     * Tests getting the Ack statuses without providing channel in the request. In
     * this case ChannelNotProvidedException is expected to happen.
     */
    @Test
    public void getAcksWithoutChannel() {
        final int port = 1606;
        final ConcurrentLinkedDeque<byte[]> messageList = new ConcurrentLinkedDeque<>();
        final AtomicLong openCount = new AtomicLong();
        final AtomicLong closeCount = new AtomicLong();

        final TestServerFactory serverFactory = new TestServerFactory();
        final TestServer server = Assertions
                .assertDoesNotThrow(() -> serverFactory.create(port, messageList, openCount, closeCount));
        server.run();

        final Configuration configuration = new Configuration(
                "127.0.0.1",
                "RELP",
                port,
                100000,
                20000,
                30000,
                1000000,
                30000,
                true
        );

        final HECService service = new HECServiceImpl(
                new Acknowledgements(configuration),
                new SessionManager(),
                new TokenManager(),
                new RequestHandler(),
                configuration,
                new RelpConnection("127.0.0.1", port)
        );

        final String authToken = "AUTH_TOKEN_12223";

        final MockHttpServletRequest request1 = new MockHttpServletRequest();
        request1.addHeader("Authorization", authToken);

        final ObjectMapper objectMapper = new ObjectMapper();
        final String ackRequest = "{\"acks\": [1,3,4]}";
        final JsonNode ackRequestNode = Assertions.assertDoesNotThrow(() -> objectMapper.readTree(ackRequest));

        Assertions.assertThrows(ChannelNotProvidedException.class, () -> {
            service.getAcks(request1, null, ackRequestNode);
        });

        Assertions.assertDoesNotThrow(server::close);
    }

    /*
     * Tests getting the Ack statuses without providing an authentication token in
     * the request. In this case AuthenticationTokenMissingException is expected to
     * happen.
     */
    @Test
    public void getAcksWithoutAuthTokenTest() {
        final int port = 1607;
        final ConcurrentLinkedDeque<byte[]> messageList = new ConcurrentLinkedDeque<>();
        final AtomicLong openCount = new AtomicLong();
        final AtomicLong closeCount = new AtomicLong();

        final TestServerFactory serverFactory = new TestServerFactory();
        final TestServer server = Assertions
                .assertDoesNotThrow(() -> serverFactory.create(port, messageList, openCount, closeCount));
        server.run();

        final Configuration configuration = new Configuration(
                "127.0.0.1",
                "RELP",
                port,
                100000,
                20000,
                30000,
                1000000,
                30000,
                true
        );

        final HECService service = new HECServiceImpl(
                new Acknowledgements(configuration),
                new SessionManager(),
                new TokenManager(),
                new RequestHandler(),
                configuration,
                new RelpConnection("127.0.0.1", port)
        );

        final MockHttpServletRequest request = new MockHttpServletRequest();

        final String channel1 = "CHANNEL_11111";

        final ObjectMapper objectMapper = new ObjectMapper();
        final String ackRequest = "{\"acks\": [1,3,4]}";
        final JsonNode ackRequestNode = Assertions.assertDoesNotThrow(() -> objectMapper.readTree(ackRequest));

        Assertions.assertThrows(AuthenticationTokenMissingException.class, () -> {
            service.getAcks(request, channel1, ackRequestNode);
        });

        Assertions.assertDoesNotThrow(server::close);
    }

    /*
     * Tests trying to get Ack statuses with an authentication that is not used to
     * send events. In this case SessionNotFoundException is expected to happen.
     */
    @Test
    public void getAcksWithUnusedAuthToken() {
        final int port = 1608;
        final ConcurrentLinkedDeque<byte[]> messageList = new ConcurrentLinkedDeque<>();
        final AtomicLong openCount = new AtomicLong();
        final AtomicLong closeCount = new AtomicLong();

        final TestServerFactory serverFactory = new TestServerFactory();
        final TestServer server = Assertions
                .assertDoesNotThrow(() -> serverFactory.create(port, messageList, openCount, closeCount));
        server.run();

        final Configuration configuration = new Configuration(
                "127.0.0.1",
                "RELP",
                port,
                100000,
                20000,
                30000,
                1000000,
                30000,
                true
        );

        final HECService service = new HECServiceImpl(
                new Acknowledgements(configuration),
                new SessionManager(),
                new TokenManager(),
                new RequestHandler(),
                configuration,
                new RelpConnection("127.0.0.1", port)
        );

        final String authToken = "AUTH_TOKEN_12223";

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", authToken);

        final String channel = "CHANNEL_22222";

        final ObjectMapper objectMapper = new ObjectMapper();
        final String ackRequest = "{\"acks\": [1,3,4]}";
        final JsonNode ackRequestNode = Assertions.assertDoesNotThrow(() -> objectMapper.readTree(ackRequest));

        Assertions.assertThrows(SessionNotFoundException.class, () -> {
            service.getAcks(request, channel, ackRequestNode);
        });

        Assertions.assertDoesNotThrow(server::close);
    }

    /*
     * Tests trying to get Ack statuses with a channel that is does not exist in the
     * session. In this case ChannelNotFoundException is expected to happen.
     */
    @Test
    public void getAcksWithUnusedChannel() {
        final int port = 1609;
        final ConcurrentLinkedDeque<byte[]> messageList = new ConcurrentLinkedDeque<>();
        final AtomicLong openCount = new AtomicLong();
        final AtomicLong closeCount = new AtomicLong();

        final TestServerFactory serverFactory = new TestServerFactory();
        final TestServer server = Assertions
                .assertDoesNotThrow(() -> serverFactory.create(port, messageList, openCount, closeCount));
        server.run();

        final Configuration configuration = new Configuration(
                "127.0.0.1",
                "RELP",
                port,
                100000,
                20000,
                30000,
                1000000,
                30000,
                true
        );

        final HECService service = new HECServiceImpl(
                new Acknowledgements(configuration),
                new SessionManager(),
                new TokenManager(),
                new RequestHandler(),
                configuration,
                new RelpConnection("127.0.0.1", port)
        );

        final String authToken = "AUTH_TOKEN_12223";

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", authToken);

        final String eventInJson = "{\"sourcetype\": \"mysourcetype\", \"event\": \"Hello, world!\", \"host\": \"localhost\", \"source\": \"mysource\", \"index\": \"myindex\"}";
        final String channel1 = "CHANNEL_11111";
        final String channel2 = "CHANNEL_22222";

        final ObjectMapper objectMapper = new ObjectMapper();
        final String ackRequest = "{\"acks\": [1,3,4]}";
        final JsonNode ackRequestNode = Assertions.assertDoesNotThrow(() -> objectMapper.readTree(ackRequest));

        Assertions.assertThrows(ChannelNotFoundException.class, () -> {
            service.sendEvents(request, channel1, eventInJson);
            service.getAcks(request, channel2, ackRequestNode);
        });

        Assertions.assertDoesNotThrow(server::close);
    }

    /*
     * Testing using EventManager's convertData() method by sending multiple events
     * at once.
     */
    @Test
    public void sendingMultipleEventsTest() {
        final int port = 1610;
        final ConcurrentLinkedDeque<byte[]> messageList = new ConcurrentLinkedDeque<>();
        final AtomicLong openCount = new AtomicLong();
        final AtomicLong closeCount = new AtomicLong();

        final TestServerFactory serverFactory = new TestServerFactory();
        final TestServer server = Assertions
                .assertDoesNotThrow(() -> serverFactory.create(port, messageList, openCount, closeCount));
        server.run();

        final Configuration configuration = new Configuration(
                "127.0.0.1",
                "RELP",
                port,
                100000,
                20000,
                30000,
                1000000,
                30000,
                true
        );

        final HECService service = new HECServiceImpl(
                new Acknowledgements(configuration),
                new SessionManager(),
                new TokenManager(),
                new RequestHandler(),
                configuration,
                new RelpConnection("127.0.0.1", port)
        );

        final String authToken1 = "AUTH_TOKEN_12223";
        final String authToken2 = "AUTH_TOKEN_16664";

        final MockHttpServletRequest request1 = new MockHttpServletRequest();
        request1.addHeader("Authorization", authToken1);
        final MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.addHeader("Authorization", authToken2);

        final String channel1 = "CHANNEL_11111";

        final String allEventsInJson = "{\"event\": \"Pony 1 has left the barn\", \"sourcetype\": \"mysourcetype\", \"time\": 1426279439}{\"event\": \"Pony 2 has left the barn\"}{\"event\": \"Pony 3 has left the barn\", \"sourcetype\": \"newsourcetype\"}{\"event\": \"Pony 4 has left the barn\"}";
        final String supposedResponse = "{\"text\":\"Success\",\"code\":0,\"ackID\":0}";
        Assertions
                .assertEquals(supposedResponse, service.sendEvents(request1, channel1, allEventsInJson).toString(), "Should get a JSON with fields text, code and ackID");

        Assertions.assertDoesNotThrow(server::close);
    }

    /*
     * Testing using EventManager's convertDataWithDefaultChannel() method by
     * sending multiple events at once.
     */
    @Test
    public void sendingMultipleEventsWithDefaultChannelTest() {
        final int port = 1611;
        final ConcurrentLinkedDeque<byte[]> messageList = new ConcurrentLinkedDeque<>();
        final AtomicLong openCount = new AtomicLong();
        final AtomicLong closeCount = new AtomicLong();

        final TestServerFactory serverFactory = new TestServerFactory();
        final TestServer server = Assertions
                .assertDoesNotThrow(() -> serverFactory.create(port, messageList, openCount, closeCount));
        server.run();

        final Configuration configuration = new Configuration(
                "127.0.0.1",
                "RELP",
                port,
                100000,
                20000,
                30000,
                1000000,
                30000,
                true
        );

        final HECService service = new HECServiceImpl(
                new Acknowledgements(configuration),
                new SessionManager(),
                new TokenManager(),
                new RequestHandler(),
                configuration,
                new RelpConnection("127.0.0.1", port)
        );

        final String authToken = "AUTH_TOKEN_12223";

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", authToken);

        final String allEventsInJson = "{\"event\": \"Pony 1 has left the barn\", \"sourcetype\": \"mysourcetype\", \"time\": 1426279439}{\"event\": \"Pony 2 has left the barn\"}{\"event\": \"Pony 3 has left the barn\", \"sourcetype\": \"newsourcetype\"}{\"event\": \"Pony 4 has left the barn\"}";
        final String supposedResponse = "{\"text\":\"Success\",\"code\":0}";
        Assertions
                .assertEquals(supposedResponse, service.sendEvents(request, null, allEventsInJson).toString(), "Should get a JSON with fields text, code and ackID");

        Assertions.assertDoesNotThrow(server::close);
    }
}
