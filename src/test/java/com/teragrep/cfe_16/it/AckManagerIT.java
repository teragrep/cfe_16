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
import com.teragrep.cfe_16.bo.Ack;
import com.teragrep.cfe_16.bo.Session;
import com.teragrep.cfe_16.config.Configuration;
import com.teragrep.cfe_16.exceptionhandling.ServerIsBusyException;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;

/*
 * Tests the functionality of ackManager
 */
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "syslog.server.host=127.0.0.1",
        "syslog.server.port=1234",
        "syslog.server.protocol=TCP",
        "max.channels=1000000",
        "max.ack.value=1000000",
        "max.ack.age=20000",
        "max.session.age=30000",
        "poll.time=30000",
        "server.print.times=true"
})
public class AckManagerIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(AckManagerIT.class);
    private String authToken1;
    private String authToken2;

    private String channel1;
    private String channel2;

    @Autowired
    private Configuration configuration;
    @Autowired
    private AckManager ackManager;

    /*
     * Initializes 2 channels. getCurrentAckValue in channel 1 is called 3 times
     * which means that the next Ack id given to a sent event should be 3.
     */
    @BeforeEach
    public void initialize() {

        authToken1 = "AUTH_TOKEN_11111";
        authToken2 = "AUTH_TOKEN_22222";

        channel1 = "CHANNEL_11111";
        channel2 = "CHANNEL_22222";

    }

    /*
     * In initialize() we call channel 1's getCurrentAckValue 3 times, so the next
     * time we call it, the currentAckValue() should return 3 and increase the ack
     * value by 1, so the next time it will be 4. We have not called
     * getCurrentAckValue() in channel2 yet, so it should be 0.
     */
    @Test
    public void getCurrentAckValueTest() {
        int currentAckValue;
        currentAckValue = ackManager.getCurrentAckValue(this.authToken1, this.channel1);
        ackManager.incrementAckValue(this.authToken1, this.channel1);

        currentAckValue = ackManager.getCurrentAckValue(this.authToken1, this.channel1);
        ackManager.incrementAckValue(this.authToken1, this.channel1);

        currentAckValue = ackManager.getCurrentAckValue(this.authToken1, this.channel1);
        ackManager.incrementAckValue(this.authToken1, this.channel1);

        currentAckValue = ackManager.getCurrentAckValue(this.authToken1, this.channel1);
        ackManager.incrementAckValue(this.authToken1, this.channel1);

        Assertions.assertEquals(3, currentAckValue, "channel1 current ack value should be 3");

        currentAckValue = ackManager.getCurrentAckValue(this.authToken1, this.channel1);
        ackManager.incrementAckValue(this.authToken1, this.channel1);
        Assertions.assertEquals(4, currentAckValue, "channel1 current ack value should be 4");

        currentAckValue = ackManager.getCurrentAckValue(this.authToken2, this.channel2);
        Assertions.assertEquals(0, currentAckValue, "channel2 current ack value should be 0");
    }

    /*
     * First we acknowledge the Ack with an id of 0 in channel 1, then we test that
     * it is indeed acknowledged. All the other Acks should not be acknowledged. If
     * Ack id is not used at all, isAckAcknowledged should return false.
     */
    @Test
    public void acknowledgeTest() {
        ackManager.initializeContext(this.authToken1, this.channel1);
        ackManager.addAck(this.authToken1, this.channel1, new Ack(0, false));
        ackManager.acknowledge(this.authToken1, this.channel1, 0);
        Assertions
                .assertTrue(ackManager.isAckAcknowledged(this.authToken1, this.channel1, 0), "ackId 0 should be acknowledged for channel 1");
        Assertions
                .assertFalse(ackManager.isAckAcknowledged(this.authToken1, this.channel1, 1), "ackId 1 should not be acknowledged for channel 1");
        Assertions
                .assertFalse(ackManager.isAckAcknowledged(this.authToken1, this.channel1, 10), "ackId 10 is not used yet for channel 1, so isAckAcknowledged should return false");
        ackManager.incrementAckValue(this.authToken1, this.channel1);
        ackManager.initializeContext(this.authToken2, this.channel2);
        Assertions
                .assertFalse(ackManager.isAckAcknowledged(this.authToken2, this.channel2, 0), "ackId 0 is not used yet for channel 2 so isAckAcknowledged should return false");
    }

    /*
     * Tests getting the Ack statuses from ackManager. First we create the request
     * bodies and the supposed responses as strings. Then we create the nodes for
     * the requests and read the strings into the node object. After that
     * ackManager's getRequestedAckStatuses() is called with the JsonNode requests
     * and the response is compared to the supposed responses.
     */
    @Test
    public void getRequestedAckStatusesTest() {
        String requestAsString = "{\"acks\": [1,3,4]}";
        String notIntRequestAsString = "{\"acks\": [\"a\",\"b\",\"c\"]}";
        String faultyRequestAsString = "{\"test\": [1,3,4]}";
        String supposedResponseAsStringOneTrue = "{\"1\":true,\"3\":false,\"4\":false}";
        String supposedResponseAsStringAllFalse = "{\"1\":false,\"3\":false,\"4\":false}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode emptyJsonNode = mapper.createObjectNode();
        JsonNode queryNode = Assertions.assertDoesNotThrow(() -> mapper.readTree(requestAsString));
        JsonNode faultyNode = Assertions.assertDoesNotThrow(() -> mapper.readTree(faultyRequestAsString));
        JsonNode notIntNode = Assertions.assertDoesNotThrow(() -> mapper.readTree(notIntRequestAsString));

        ackManager.initializeContext(this.authToken1, this.channel1);

        Assertions.assertTrue(ackManager.addAck(this.authToken1, this.channel1, new Ack(1, false)));
        Assertions.assertTrue(ackManager.acknowledge(this.authToken1, this.channel1, 1));
        Assertions
                .assertEquals(emptyJsonNode, ackManager.getRequestedAckStatuses(this.authToken1, "", null), "getRequestedAckStatuses should return null, when providing a null value as a " + "parameter");

        Assertions
                .assertEquals(
                        supposedResponseAsStringOneTrue, ackManager
                                .getRequestedAckStatuses(this.authToken1, this.channel1, queryNode)
                                .toString(),
                        "ackId 1 status should be true on channel1, others should be false."
                );

        Assertions
                .assertEquals(
                        supposedResponseAsStringAllFalse, ackManager
                                .getRequestedAckStatuses(this.authToken1, this.channel1, queryNode)
                                .toString(),
                        "ackId 1 status should be false on channel1 after requesting it's status once. All "
                                + "others should be false as well"
                );

        ackManager.initializeContext(this.authToken2, this.channel2);
        Assertions
                .assertEquals(
                        supposedResponseAsStringAllFalse, ackManager
                                .getRequestedAckStatuses(this.authToken2, this.channel2, queryNode)
                                .toString(),
                        "All ack statuses should be false for channel2"
                );

        Assertions
                .assertEquals(
                        emptyJsonNode, ackManager.getRequestedAckStatuses(this.authToken1, this.channel1, emptyJsonNode), "An empty JsonNode should be returned when querying with an empty JsonNode"
                );

        Assertions
                .assertEquals(
                        emptyJsonNode, ackManager.getRequestedAckStatuses(this.authToken1, this.channel1, faultyNode), "An empty JsonNode should be returned when querying with a JsonNode that has no " + "\"acks\" field in it."
                );

        // Lambda requires parameter to be final, so noIntNode cannot be used
        final JsonNode finalNotIntNode = notIntNode;
        Assertions
                .assertThrowsExactly(
                        IllegalArgumentException.class, () -> ackManager
                                .getRequestedAckStatuses(this.authToken1, this.channel1, finalNotIntNode),
                        "An empty JsonNode should be returned when querying with a JsonNode that has "
                                + "\"acks\" field in it, but it has something else than integers as it's " + "values"
                );
    }

    @Test
    public void getCurrentAckValueAndIncrementTest() {
        Assertions
                .assertEquals(0, this.ackManager.getCurrentAckValue(this.authToken1, Session.DEFAULT_CHANNEL), "AckManager should return 0");
        this.ackManager.incrementAckValue(this.authToken1, Session.DEFAULT_CHANNEL);
        Assertions
                .assertEquals(1, this.ackManager.getCurrentAckValue(this.authToken1, Session.DEFAULT_CHANNEL), "AckManager should return 1");

        this.ackManager.incrementAckValue(this.authToken1, Session.DEFAULT_CHANNEL);
        Assertions
                .assertEquals(2, this.ackManager.getCurrentAckValue(this.authToken1, Session.DEFAULT_CHANNEL), "AckManager should return 2");

    }

    /*
     * Tests deleting the Ack from ackManager. First we get the list that is
     * currently in the ackManager and save it to list1 variable. After that we
     * delete an Ack from the ackManager's list, then we get the list from
     * ackManager again and save it to list2 variable. These 2 lists are then
     * compared to each other.
     */
    @Test
    public void deleteAckTest() {
        AckManager ackManager1 = new AckManager();
        AckManager ackManager2 = new AckManager();

        ackManager1.initializeContext(this.authToken1, this.channel1);
        Assertions.assertTrue(ackManager1.addAck(this.authToken1, this.channel1, new Ack(0, false)));
        Assertions.assertTrue(ackManager1.addAck(this.authToken1, this.channel1, new Ack(1, false)));

        Map<Integer, Ack> list1 = ackManager1.getAckList(this.authToken1, this.channel1);
        int list1Size = ackManager1.getAckListSize(this.authToken1, this.channel1);
        Assertions.assertEquals(2, list1Size, "Ack list 1 size should be 2.");

        Ack deletedAck = list1.values().iterator().next();

        ackManager2.initializeContext(this.authToken2, this.channel2);
        Assertions.assertTrue(ackManager2.addAck(this.authToken2, this.channel2, new Ack(0, false)));

        ackManager2.initializeContext(this.authToken2, this.channel2);
        Assertions.assertTrue(ackManager2.addAck(this.authToken2, this.channel2, new Ack(1, false)));

        ackManager2.deleteAckFromList(this.authToken2, this.channel2, deletedAck);
        Map<Integer, Ack> list2 = ackManager2.getAckList(this.authToken2, this.channel2);
        int list2Size = list2.size();

        Assertions.assertNotEquals(list1, list2, "Ack lists should not be same");
        Assertions.assertEquals(list1Size - 1, list2Size, "list2 should be shorter by one index");
        Assertions.assertFalse(list2.containsKey(deletedAck.getId()), "list2 should not contain the deleted ack");
    }

    /*
     * Max Ack value is set to 2, so the Ack list should be full after
     * getCurrentAckValue() is called 3 times. getCurrentAckValue is called 4 times
     * here, so ServerIsBusyException is expected to happen.
     */
    @Test
    public void maxAckValueTest() {
        Assertions.assertThrows(ServerIsBusyException.class, () -> {
            /* AckManager ackManager = new AckManager(); */

            this.configuration.setMaxAckValue(2);
            int ackId;

            ackId = ackManager.getCurrentAckValue(this.authToken1, this.channel1);
            ackManager.incrementAckValue(this.authToken1, this.channel1);
            ackManager.addAck(this.authToken1, this.channel1, new Ack(ackId, false));

            ackId = ackManager.getCurrentAckValue(this.authToken1, this.channel1);
            ackManager.incrementAckValue(this.authToken1, this.channel1);
            ackManager.addAck(this.authToken1, this.channel1, new Ack(ackId, false));

            ackId = ackManager.getCurrentAckValue(this.authToken1, this.channel1);
            ackManager.incrementAckValue(this.authToken1, this.channel1);
            ackManager.addAck(this.authToken1, this.channel1, new Ack(ackId, false));

            ackId = ackManager.getCurrentAckValue(this.authToken1, this.channel1);
            ackManager.incrementAckValue(this.authToken1, this.channel1);
            ackManager.addAck(this.authToken1, this.channel1, new Ack(ackId, false));
        });
    }
}
