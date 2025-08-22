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

import com.teragrep.cfe_16.server.TestServer;
import com.teragrep.cfe_16.server.TestServerFactory;
import com.teragrep.cfe_16.service.HECService;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@TestPropertySource(properties = {
        "syslog.server.host=127.0.0.1",
        "syslog.server.port=1236",
        "syslog.server.protocol=RELP",
        "max.channels=1000000",
        "max.ack.value=1000000",
        "max.ack.age=20000",
        "max.session.age=30000",
        "poll.time=30000",
        "server.print.times=true"
})
@SpringBootTest
public class SendMultipleEventsIT {

    private static final int SERVER_PORT = 1236;
    private static final ConcurrentLinkedDeque<byte[]> messageList = new ConcurrentLinkedDeque<>();
    private static final AtomicLong openCount = new AtomicLong();
    private static final AtomicLong closeCount = new AtomicLong();
    private static TestServer server;
    @Autowired
    private HECService service;
    private MockHttpServletRequest request1;
    private String eventInJson;
    private String channel1;

    @BeforeAll
    public static void init() {
        TestServerFactory serverFactory = new TestServerFactory();
        server = Assertions
                .assertDoesNotThrow(() -> serverFactory.create(SERVER_PORT, messageList, openCount, closeCount));
        server.run();
    }

    @AfterAll
    public static void close() {
        try {
            server.close();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    public void clear() {
        openCount.set(0);
        closeCount.set(0);
        messageList.clear();
    }

    @BeforeEach
    public void initEach() {

        this.request1 = new MockHttpServletRequest();
        this.request1.addHeader("Authorization", "AUTH_TOKEN_11111");
        this.channel1 = "CHANNEL_11111";
        this.eventInJson = "{\"sourcetype\":\"access\", \"source\":\"/var/log/access.log\", \"event\": {\"message\":\"Access log test message 1\"}} {\"sourcetype\":\"access\", \"source\":\"/var/log/access.log\", \"event\": {\"message\":\"Access log test message 2\"}}";

    }

    @Test
    public void sendEventsTest() throws IOException, InterruptedException, ExecutionException {
        final int NUMBER_OF_EVENTS_TO_BE_SENT = 100;
        List<CompletableFuture<String>> futures = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_EVENTS_TO_BE_SENT; i++) {
            CompletableFuture<String> f = CompletableFuture
                    .supplyAsync(() -> service.sendEvents(request1, channel1, eventInJson).toString());
            futures.add(f);
        }
        List<String> supposedResponses = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_EVENTS_TO_BE_SENT; i++) {
            final String supposedResponse = "{\"text\":\"Success\",\"code\":0,\"ackID\":" + i + "}";
            supposedResponses.add(supposedResponse);
        }
        int countFuture = 0;
        for (Future<String> f : futures) {
            final String actualResponse = f.get();
            Assertions
                    .assertTrue(supposedResponses.contains(actualResponse), "Service should return JSON object with fields 'text', 'code' and 'ackID' (ackID should be " + countFuture + ")");
            countFuture++;
        }

        Assertions.assertEquals(NUMBER_OF_EVENTS_TO_BE_SENT, countFuture, "All futures have NOT been looped through");

        Assertions.assertEquals(NUMBER_OF_EVENTS_TO_BE_SENT * 2, messageList.size());
    }
}
