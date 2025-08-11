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

import com.teragrep.cfe_16.Acknowledgements;
import com.teragrep.cfe_16.RequestHandler;
import com.teragrep.cfe_16.SessionManager;
import com.teragrep.cfe_16.TokenManager;
import com.teragrep.cfe_16.config.Configuration;
import com.teragrep.cfe_16.connection.RelpConnection;
import com.teragrep.cfe_16.server.TestServer;
import com.teragrep.cfe_16.server.TestServerFactory;
import com.teragrep.cfe_16.service.HECService;
import com.teragrep.cfe_16.service.HECServiceImpl;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class SendMultipleEventsIT {

    @Test
    public void sendEventsTest() throws InterruptedException, ExecutionException {
        final int SERVER_PORT = 1236;
        final ConcurrentLinkedDeque<byte[]> messageList = new ConcurrentLinkedDeque<>();
        final AtomicLong openCount = new AtomicLong();
        final AtomicLong closeCount = new AtomicLong();
        final TestServerFactory serverFactory = new TestServerFactory();
        final TestServer server = Assertions
                .assertDoesNotThrow(() -> serverFactory.create(SERVER_PORT, messageList, openCount, closeCount));
        server.run();

        final Configuration configuration = new Configuration(
                "127.0.0.1",
                "RELP",
                SERVER_PORT,
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
                new RelpConnection("127.0.0.1", SERVER_PORT)
        );
        final MockHttpServletRequest request1 = new MockHttpServletRequest();
        request1.addHeader("Authorization", "AUTH_TOKEN_11111");

        final String channel1 = "CHANNEL_11111";

        final String eventInJson = "{\"sourcetype\":\"access\", \"source\":\"/var/log/access.log\", \"event\": {\"message\":\"Access log test message 1\"}} {\"sourcetype\":\"access\", \"source\":\"/var/log/access.log\", \"event\": {\"message\":\"Access log test message 2\"}}";
        final int NUMBER_OF_EVENTS_TO_BE_SENT = 100;
        final List<CompletableFuture<String>> futures = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_EVENTS_TO_BE_SENT; i++) {
            final CompletableFuture<String> f = CompletableFuture
                    .supplyAsync(() -> service.sendEvents(request1, channel1, eventInJson).toString());
            futures.add(f);
        }
        final List<String> supposedResponses = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_EVENTS_TO_BE_SENT; i++) {
            final String supposedResponse = "{\"text\":\"Success\",\"code\":0,\"ackID\":" + i + "}";
            supposedResponses.add(supposedResponse);
        }
        int countFuture = 0;
        for (final Future<String> future : futures) {
            final String actualResponse = future.get();
            Assertions
                    .assertTrue(supposedResponses.contains(actualResponse), "Service should return JSON object with fields 'text', 'code' and 'ackID' (ackID should be " + countFuture + ")");
            countFuture++;
        }

        Assertions.assertEquals(NUMBER_OF_EVENTS_TO_BE_SENT, countFuture, "All futures have NOT been looped through");

        Assertions.assertEquals(NUMBER_OF_EVENTS_TO_BE_SENT * 2, messageList.size());

        try {
            server.close();
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
