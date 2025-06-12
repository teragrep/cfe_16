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
import com.teragrep.cfe_16.AckManager;
import com.teragrep.cfe_16.EventManager;
import com.teragrep.cfe_16.bo.HeaderInfo;
import com.teragrep.cfe_16.bo.HttpEventData;
import com.teragrep.cfe_16.bo.Session;
import com.teragrep.cfe_16.exceptionhandling.AuthenticationTokenMissingException;
import com.teragrep.cfe_16.exceptionhandling.ChannelNotFoundException;
import com.teragrep.cfe_16.exceptionhandling.ChannelNotProvidedException;
import com.teragrep.cfe_16.exceptionhandling.EventFieldBlankException;
import com.teragrep.cfe_16.exceptionhandling.EventFieldMissingException;
import com.teragrep.cfe_16.exceptionhandling.SessionNotFoundException;
import com.teragrep.cfe_16.service.HECService;
import com.teragrep.rlp_03.Server;
import com.teragrep.rlp_03.ServerFactory;
import com.teragrep.rlp_03.config.Config;
import com.teragrep.rlp_03.delegate.DefaultFrameDelegate;
import com.teragrep.rlp_03.delegate.FrameDelegate;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

/*
 * Tests the functionality of HECServiceImpl
 */

@SpringBootTest
@TestPropertySource(properties = {
        "syslog.server.host=127.0.0.1",
        "syslog.server.port=1601",
        "syslog.server.protocol=RELP",
        "max.channels=1000000",
        "max.ack.value=1000000",
        "max.ack.age=20000",
        "max.session.age=30000",
        "poll.time=30000",
        "poll.time=30000",
        "server.print.times=true"
})
public class ServiceAndEventManagerIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAndEventManagerIT.class);
    private static final String hostname = "localhost";
    private static final ServerSocket serverSocket = getSocket();
    private static final Integer port = 1601;
    private static Server server;
    private final HeaderInfo headerInfo = new HeaderInfo();
    @Autowired
    private HECService service;
    @Autowired
    private AckManager ackManager;
    private MockHttpServletRequest request1;
    private MockHttpServletRequest request2;
    private MockHttpServletRequest request3;
    private MockHttpServletRequest request4;
    private MockHttpServletRequest request5;
    private String eventInJson;
    private String channel1;
    private String channel2;
    private String channel3;
    private String defaultChannel;
    private String authToken1;
    private String authToken2;
    private String authToken3;
    private String authToken4;
    private String ackRequest;
    private ObjectMapper objectMapper;
    private JsonNode ackRequestNode;
    @Autowired
    private EventManager eventManager;

    @BeforeAll
    public static void init_x() throws IOException, InterruptedException {
        Supplier<FrameDelegate> frameDelegateSupplier = () -> new DefaultFrameDelegate(
                (frame) -> LOGGER.debug(frame.relpFrame().payload().toString())
        );
        Config config = new Config(port, 1);
        ServerFactory serverFactory = new ServerFactory(config, frameDelegateSupplier);

        server = serverFactory.create();
        Thread serverThread = new Thread(server);
        serverThread.start();
        server.startup.waitForCompletion();
    }

    @AfterAll
    public static void cleanup() throws InterruptedException {
        server.stop();
    }

    private static ServerSocket getSocket() {
        return Assertions.assertDoesNotThrow(() -> new ServerSocket(1234), "Could not get a new Server Socket");
    }

    @AfterAll
    public static void closeServerSocket() {
        Assertions.assertDoesNotThrow(serverSocket::close);
    }

    /*
     * Opens a ServerSocket so that the sending an event won't produce an error. 4
     * Mock requests are created. request2 will not have authorization token
     * assigned to it. eventInJson variable is the body of the request as a string
     * when sending an event and ackRequest is the body of the request when
     * requesting Ack statuses.
     */
    @BeforeEach
    public void initialize() {
        objectMapper = new ObjectMapper();
        request1 = new MockHttpServletRequest();
        request2 = new MockHttpServletRequest();
        request3 = new MockHttpServletRequest();
        request4 = new MockHttpServletRequest();
        request5 = new MockHttpServletRequest();

        eventInJson = "{\"sourcetype\": \"mysourcetype\", \"event\": \"Hello, world!\", \"host\": \"localhost\", \"source\": \"mysource\", \"index\": \"myindex\"}";

        channel1 = "CHANNEL_11111";
        channel2 = "CHANNEL_22222";
        channel3 = "CHANNEL_33333";

        authToken1 = "AUTH_TOKEN_12223";
        authToken2 = "AUTH_TOKEN_16664";
        authToken3 = "AUTH_TOKEN_23667";
        authToken4 = "AUTH_TOKEN_23249";

        request1.addHeader("Authorization", authToken1);
        request3.addHeader("Authorization", authToken2);
        request4.addHeader("Authorization", authToken3);
        request5.addHeader("Authorization", authToken4);

        ackRequest = "{\"acks\": [1,3,4]}";

        ackRequestNode = Assertions.assertDoesNotThrow(() -> objectMapper.readTree(ackRequest));

        defaultChannel = Session.DEFAULT_CHANNEL;
    }

    /*
     * Tests the sendEvents() and getAcks() method of the service.
     */
    @Test
    public void sendEventsAndGetAcksTest() {
        String supposedResponse;

        supposedResponse = "{\"text\":\"Success\",\"code\":0,\"ackID\":0}";
        Assertions
                .assertEquals(supposedResponse, service.sendEvents(request1, channel3, eventInJson).toString(), "Service should return JSON object with fields 'text', 'code' and 'ackID' (ackID " + "should be 0)");

        supposedResponse = "{\"text\":\"Success\",\"code\":0,\"ackID\":1}";
        Assertions
                .assertEquals(supposedResponse, service.sendEvents(request1, channel3, eventInJson).toString(), "Service should return JSON object with fields 'text', 'code' and 'ackID' (ackID " + "should be 1)");

        supposedResponse = "{\"text\":\"Success\",\"code\":0,\"ackID\":0}";
        Assertions
                .assertEquals(supposedResponse, service.sendEvents(request1, channel2, eventInJson).toString(), "Service should return JSON object with fields 'text', 'code' and 'ackID' (ackID " + "should be 0)");

        Assertions
                .assertEquals(supposedResponse, service.sendEvents(request3, channel3, eventInJson).toString(), "Service should return JSON object with fields 'text', 'code' and 'ackID' (ackID " + "should be 0)");

        supposedResponse = "{\"acks\":{\"1\":true,\"3\":false,\"4\":false}}";
        Assertions
                .assertEquals(supposedResponse, service.getAcks(request1, channel3, ackRequestNode).toString(), "JSON object should be returned with ack statuses.");
    }

    /*
     * Tests sending a request with no authentication token. In this case
     * AuthenticationTokenMissingException is expected to happen.
     */
    @Test
    public void sendEventsWithoutAuthTokenTest() {
        Assertions.assertThrows(AuthenticationTokenMissingException.class, () -> {
            service.sendEvents(request2, eventInJson, channel1);
        });
    }

    /*
     * Tests sending a request with no channel provided. In this case no Ack id is
     * returned.
     */
    @Test
    public void sendEventsWithoutChannelTest() {
        String supposedResponse = "{\"text\":\"Success\",\"code\":0}";
        String response = service.sendEvents(request1, null, eventInJson).toString();
        Assertions
                .assertEquals(
                        supposedResponse, response, "Service should return JSON object with fields 'text' and 'code'"
                );
    }

    /*
     * Tests getting the Ack statuses without providing channel in the request. In
     * this case ChannelNotProvidedException is expected to happen.
     */
    @Test
    public void getAcksWithoutChannel() {
        Assertions.assertThrows(ChannelNotProvidedException.class, () -> {
            service.getAcks(request1, null, ackRequestNode);
        });
    }

    /*
     * Tests getting the Ack statuses without providing an authentication token in
     * the request. In this case AuthenticationTokenMissingException is expected to
     * happen.
     */
    @Test
    public void getAcksWithoutAuthTokenTest() {
        Assertions.assertThrows(AuthenticationTokenMissingException.class, () -> {
            service.getAcks(request2, channel1, ackRequestNode);
        });
    }

    /*
     * Tests trying to get Ack statuses with an authentication that is not used to
     * send events. In this case SessionNotFoundException is expected to happen.
     */
    @Test
    public void getAcksWithUnusedAuthToken() {
        Assertions.assertThrows(SessionNotFoundException.class, () -> {
            service.getAcks(request4, channel1, ackRequestNode);
        });
    }

    /*
     * Tests trying to get Ack statuses with a channel that is does not exist in the
     * session. In this case ChannelNotFoundException is expected to happen.
     */
    @Test
    public void getAcksWithUnusedChannel() {
        Assertions.assertThrows(ChannelNotFoundException.class, () -> {
            service.sendEvents(request5, channel1, eventInJson);
            service.getAcks(request5, channel2, ackRequestNode);
        });
    }

    /*
     * Tests the EventManager's convertData() method. First we create a Json node as
     * string, that we give as a parameter for convertData(). We also create a
     * supposed response Json node as string. convertData() returns an ObjectNode
     * object, which we convert to string here, so we can easily compare it to our
     * supposed response.
     */
    @Test
    public void convertDataTest() {
        /*AckManager ackManager = new AckManager();*/
        String allEventsInJson = "{\"sourcetype\": \"mysourcetype\", \"event\": \"Hello, "
                + "world!\", \"host\": \"localhost\", \"source\": \"mysource\", \"index\": " + "\"myindex\"}";
        String supposedResponse = "{\"text\":\"Success\",\"code\":0,\"ackID\":2}";
        String response = eventManager
                .convertData(authToken1, channel1, allEventsInJson, headerInfo, ackManager)
                .toString();
        Assertions.assertEquals(supposedResponse, response, "Should get a JSON with fields text, code and ackID");
    }

    /*
     * Tests the EventManager's convertDataWithDefaultChannel() method which is
     * called when a channel is not provided in a request. First we create a Json
     * node as string, that we give as a parameter for convertData(). We also create
     * a supposed response Json node as string. convertData() returns an ObjectNode
     * object, which we convert to string here, so we can easily compare it to our
     * supposed response.
     */
    @Test
    public void convertDataTestWithDefaultChannel() {
        /* AckManager ackManager = new AckManager(); */
        String allEventsInJson = "{\"sourcetype\": \"mysourcetype\", \"event\": \"Hello, "
                + "world!\", \"host\": \"localhost\", \"source\": \"mysource\", \"index\": " + "\"myindex\"}";
        String supposedResponse = "{\"text\":\"Success\",\"code\":0}";

        Assertions
                .assertEquals(
                        supposedResponse, eventManager
                                .convertData(authToken1, defaultChannel, allEventsInJson, headerInfo, ackManager)
                                .toString(),
                        "Should get a JSON with fields text and code."
                );

    }

    /*
     * Tests attempting to send a request, which has no "event"-field in it's body.
     * When this is the case, EventFieldMissingException is expected to happen.
     */
    @Test
    public void noEventFieldInRequestTest() {
        Assertions.assertThrows(EventFieldMissingException.class, () -> {
            /*AckManager ackManager = new AckManager();*/
            String allEventsInJson = "{\"sourcetype\": \"mysourcetype\", \"host\": \"localhost\","
                    + " \"source\": \"mysource\", \"index\": \"myindex\"}";
            eventManager.convertData(authToken1, channel1, allEventsInJson, headerInfo, ackManager);
        });
    }

    /*
     * Tests attempting to send a request, which has a blank "event"-field. When
     * this is the case, EventFieldBlankException is expected to happen.
     */
    @Test
    public void eventFieldBlankInRequestTest() {
        Assertions.assertThrows(EventFieldBlankException.class, () -> {
            /*AckManager ackManager = new AckManager();*/
            String allEventsInJson = "{\"sourcetype\": \"mysourcetype\", \"event\": \"\", "
                    + "\"host\": \"localhost\", \"source\": \"mysource\", \"index\": \"myindex\"}";
            eventManager.convertData(authToken1, channel1, allEventsInJson, headerInfo, ackManager);
        });
    }

    /*
     * EventManager needs to handle the time stamp, if it is provided in the
     * request. This method tests the handling of time.
     */
    @Test
    public void handleTimeTest() {
        // Content strings are created with different kinds of "time"-fields amd they
        // are read into a JsonNode object.

        // content1: time is in epoch seconds (10 digits)
        String content1 = "{\"event\": \"Pony 1 has left the barn\", \"sourcetype\": "
                + "\"mysourcetype\", \"time\": 1277464192}";
        // content2: "time"-field is not given
        String content2 = "{\"event\": \"Pony 1 has left the barn\", \"sourcetype\": " + "\"mysourcetype\"}";
        // content3: time is given in epoch seconds and a decimal giving the epoch
        // milliseconds
        String content3 = "{\"event\": \"Pony 1 has left the barn\", \"sourcetype\": "
                + "\"mysourcetype\", \"time\": 1433188255.253}";
        // content4: time is given inepoch milliseconds (13 digits)
        String content4 = "{\"event\": \"Pony 1 has left the barn\", \"sourcetype\": "
                + "\"mysourcetype\", \"time\": 1433188255253}";
        // content5: time is given as a string
        String content5 = "{\"event\": \"Pony 1 has left the barn\", \"sourcetype\": "
                + "\"mysourcetype\", \"time\": \"1433188255253\"}";
        // content6: time is given with too small amount of digits
        String content6 = "{\"event\": \"Pony 1 has left the barn\", \"sourcetype\": "
                + "\"mysourcetype\", \"time\": 143318}";
        // content7: time is given in epoch centiseconds
        String content7 = "{\"event\": \"Pony 1 has left the barn\", \"sourcetype\": "
                + "\"mysourcetype\", \"time\": 143318825525}";
        // content8: time is given with too many digits
        String content8 = "{\"event\": \"Pony 1 has left the barn\", \"sourcetype\": "
                + "\"mysourcetype\", \"time\": 1433188255252321}";

        final JsonNode node1 = Assertions.assertDoesNotThrow(() -> objectMapper.readTree(content1));
        final JsonNode node2 = Assertions.assertDoesNotThrow(() -> objectMapper.readTree(content2));
        final JsonNode node3 = Assertions.assertDoesNotThrow(() -> objectMapper.readTree(content3));
        final JsonNode node4 = Assertions.assertDoesNotThrow(() -> objectMapper.readTree(content4));
        final JsonNode node5 = Assertions.assertDoesNotThrow(() -> objectMapper.readTree(content5));
        final JsonNode node6 = Assertions.assertDoesNotThrow(() -> objectMapper.readTree(content6));
        final JsonNode node7 = Assertions.assertDoesNotThrow(() -> objectMapper.readTree(content7));
        final JsonNode node8 = Assertions.assertDoesNotThrow(() -> objectMapper.readTree(content8));

        HttpEventData testData1 = Assertions
                .assertDoesNotThrow(
                        () -> Whitebox.invokeMethod(eventManager, "handleTime", new HttpEventData(), node1, null), "Could not invokeMethods properly"
                );
        HttpEventData testData2 = Assertions
                .assertDoesNotThrow(
                        () -> Whitebox.invokeMethod(eventManager, "handleTime", new HttpEventData(), node2, null), "Could not invokeMethods properly"
                );
        HttpEventData testData3 = Assertions
                .assertDoesNotThrow(
                        () -> Whitebox.invokeMethod(eventManager, "handleTime", new HttpEventData(), node3, null), "Could not invokeMethods properly"
                );
        HttpEventData testData4 = Assertions
                .assertDoesNotThrow(
                        () -> Whitebox.invokeMethod(eventManager, "handleTime", new HttpEventData(), node4, null), "Could not invokeMethods properly"
                );
        HttpEventData testData5 = Assertions
                .assertDoesNotThrow(
                        () -> Whitebox.invokeMethod(eventManager, "handleTime", new HttpEventData(), node5, null), "Could not invokeMethods properly"
                );
        HttpEventData testData6 = Assertions
                .assertDoesNotThrow(
                        () -> Whitebox.invokeMethod(eventManager, "handleTime", new HttpEventData(), node6, null), "Could not invokeMethods properly"
                );
        HttpEventData testData7 = Assertions
                .assertDoesNotThrow(
                        () -> Whitebox.invokeMethod(eventManager, "handleTime", new HttpEventData(), node7, null), "Could not invokeMethods properly"
                );
        HttpEventData testData8 = Assertions
                .assertDoesNotThrow(
                        () -> Whitebox.invokeMethod(eventManager, "handleTime", new HttpEventData(), node8, null), "Could not invokeMethods properly"
                );

        /*
         * Testing the getTimeSource(), isTimeParsed() and getTimeAsLong() methods from
         * the HttpEventData objects that were returned from EventManager's handleTime()
         * method.
         */
        Assertions
                .assertEquals(
                        "reported", testData1.getTimeSource(),
                        "Time source should be 'reported' when the time is specified in a request"
                );
        Assertions
                .assertTrue(
                        testData1.isTimeParsed(), "timeParsed should be true when the time is specified in a request"
                );
        Assertions
                .assertEquals(
                        1277464192000L, testData1.getTimeAsLong(),
                        "Time should have been converted to epoch milliseconds"
                );

        Assertions
                .assertEquals(
                        "generated", testData2.getTimeSource(),
                        "Time source should be 'generated' when it's not specified in a request"
                );
        Assertions
                .assertFalse(
                        testData2.isTimeParsed(), "timeParsed should be false when time is not specified in a request"
                );
        Assertions
                .assertEquals(
                        0, testData2.getTimeAsLong(), "Time as long should be 0 when time is not specified in a request"
                );

        Assertions
                .assertEquals(
                        "reported", testData3.getTimeSource(),
                        "Time source should be 'reported' when the time is specified in a request"
                );
        Assertions
                .assertTrue(testData3.isTimeParsed(), "timeParsed should be true when time is specified in a request.");
        Assertions
                .assertEquals(
                        1433188255253L, testData3.getTimeAsLong(),
                        "Time should be converted to epoch milliseconds when it's provided in a request in "
                                + "epoch seconds with decimals."
                );

        Assertions
                .assertEquals(
                        "reported", testData4.getTimeSource(),
                        "Time source should be 'reported' when the time is specified in a request"
                );
        Assertions
                .assertTrue(testData4.isTimeParsed(), "timeParsed should be true when time is specified in a request.");
        Assertions
                .assertEquals(
                        1433188255253L, testData4.getTimeAsLong(),
                        "Time should be in epoch milliseconds when it is provided as epoch milliseconds in "
                                + "the request"
                );

        Assertions
                .assertEquals(
                        "generated", testData5.getTimeSource(),
                        "Time source should be 'generated' when time is given as a string in a request"
                );
        Assertions
                .assertFalse(
                        testData5.isTimeParsed(),
                        "timeParsed should be false when time is given as a string in a request"
                );
        Assertions
                .assertEquals(
                        0, testData5.getTimeAsLong(), "Time should be 0 when time is given as a string in a request"
                );

        Assertions
                .assertEquals(
                        "generated", testData6.getTimeSource(),
                        "Time source should be 'generated' when time is given as an integer with less than 10"
                                + " digits"
                );
        Assertions
                .assertFalse(
                        testData6.isTimeParsed(),
                        "timeParsed should be false when time is given as an integer with less than 10 digits"
                );
        Assertions
                .assertEquals(143318, testData6.getTimeAsLong(), "Time as long should be as provided in the request.");

        Assertions
                .assertEquals(
                        "reported", testData7.getTimeSource(),
                        "Time source should be 'reported' when the time is specified in a request with 10-13 "
                                + "digits"
                );
        Assertions
                .assertTrue(
                        testData7.isTimeParsed(),
                        "timeParsed should be true when time is specified in a request with 10-13 digits"
                );
        Assertions
                .assertEquals(
                        1433188255250L, testData7.getTimeAsLong(),
                        "Time should be converted to epoch milliseconds when provided in a request with 10-13"
                                + " digits"
                );

        Assertions
                .assertEquals(
                        "generated", testData8.getTimeSource(),
                        "Time source should be 'generated' when time is given as an integer with more than 13"
                                + " digits"
                );
        Assertions
                .assertFalse(
                        testData8.isTimeParsed(),
                        "timeParsed should be false when time is given as an integer with more than 13 digits"
                );
        Assertions
                .assertEquals(
                        1433188255252321L, testData8.getTimeAsLong(), "Time should be as it's provided in a request."
                );
    }

    /*
     * Testing using EventManager's convertData() method by sending multiple events
     * at once.
     */
    public void sendingMultipleEventsTest() {
        AckManager ackManager = new AckManager();
        String allEventsInJson = "{\"event\": \"Pony 1 has left the barn\", \"sourcetype\": "
                + "\"mysourcetype\", \"time\": 1426279439}{\"event\": \"Pony 2 has left the "
                + "barn\"}{\"event\": \"Pony 3 has left the barn\", \"sourcetype\": "
                + "\"newsourcetype\"}{\"event\": \"Pony 4 has left the barn\"}";
        String supposedResponse = "{\"text\":\"Success\",\"code\":0,\"ackID\":0}";
        Assertions
                .assertEquals(
                        supposedResponse, eventManager
                                .convertData(authToken1, channel1, allEventsInJson, headerInfo, ackManager)
                                .toString(),
                        "Should get a JSON with fields text, code and ackID"
                );

    }

    /*
     * Testing using EventManager's convertDataWithDefaultChannel() method by
     * sending multiple events at once.
     */
    public void sendingMultipleEventsWithDefaultChannelTest() {
        AckManager ackManager = new AckManager();
        String allEventsInJson = "{\"event\": \"Pony 1 has left the barn\", \"sourcetype\": "
                + "\"mysourcetype\", \"time\": 1426279439}{\"event\": \"Pony 2 has left the "
                + "barn\"}{\"event\": \"Pony 3 has left the barn\", \"sourcetype\": "
                + "\"newsourcetype\"}{\"event\": \"Pony 4 has left the barn\"}";
        String supposedResponse = "{\"text\":\"Success\",\"code\":0,\"ackID\":0}";
        Assertions
                .assertEquals(
                        supposedResponse, eventManager
                                .convertData(authToken1, defaultChannel, allEventsInJson, headerInfo, ackManager)
                                .toString(),
                        "Should get a JSON with fields text, code and ackID"
                );
    }
}
