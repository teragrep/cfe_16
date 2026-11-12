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

import com.teragrep.cfe_16.response.AcknowledgedJsonResponse;
import com.teragrep.cfe_16.response.Response;
import com.teragrep.cfe_16.server.TestServer;
import com.teragrep.cfe_16.server.TestServerFactory;
import com.teragrep.cfe_16.service.HECService;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.teragrep.rlo_06.RFC5424Frame;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
        "syslog.server.host=127.0.0.1",
        "syslog.server.port=1238",
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
public class SendSingleEventIT {

    private static final int SERVER_PORT = 1238;
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
        final TestServerFactory serverFactory = new TestServerFactory();
        server = Assertions
                .assertDoesNotThrow(() -> serverFactory.create(SERVER_PORT, messageList, openCount, closeCount));
        server.run();
    }

    @AfterAll
    public static void close() {
        Assertions.assertDoesNotThrow(() -> server.close());
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
        this.eventInJson = "{\"sourcetype\":\"access\", \"source\":\"/var/log/access.log\", "
                + "\"event\": {\"message\":\"Access log test message 1\"}} "
                + "{\"sourcetype\":\"access\", \"source\":\"/var/log/access.log\", \"event\": "
                + "{\"message\":\"Access log test message 2\"}}";
    }

    @Test
    public void test_1_Send1Event() {
        final Response supposedResponse = new AcknowledgedJsonResponse("Success", 0);
        Assertions
                .assertEquals(supposedResponse, service.sendEvents(request1, channel1, eventInJson), "Service should return JSON object with fields 'text', 'code' and 'ackID' (ackID " + "should be " + 0 + ")");

        Assertions
                .assertEquals(2, messageList.size(), "Number of events received should match the number of sent ones");
    }

    @Test
    public void test_2_NDJSONEvent() {
        final String eventJson = """
                {
                  "time": "1791298774.324591",
                  "source": "testi-event1",
                  "event": {
                    "stream": "TEST_STREAM1",
                    "time": "2026-10-06T14:59:34.125899443Z",
                    "log": {
                      "timestamp": "2026-10-06T14:59:34.125Z",
                      "message": "{\\"appName\\":\\"APP1\\"}"
                    },
                    "host": "testi.1.lan"
                  }
                }
                {
                  "time": 1791298774.32559,
                  "source": "testi-event2",
                  "event": {
                    "stream": "TEST_STREAM2",
                    "time": "2026-10-06T14:59:34.125899443Z",
                    "log": {
                      "timestamp": "2026-10-06T14:59:34.125Z",
                      "message": "{\\"appName\\":\\"APP2\\"}"
                    },
                    "host": "testi.2.lan"
                  }
                }
                {
                  "time": 1791298774.3269,
                  "source": "testi-event3",
                  "event": {
                    "stream": "TEST_STREAM3",
                    "time": "2026-10-06T14:59:34.125899443Z",
                    "log": {
                      "timestamp": "2026-10-06T14:59:34.125Z",
                      "message": "{\\"appName\\":\\"APP1\\"}"
                    },
                    "host": "testi.3.lan"
                  }
                }
                {
                  "time": 1791298774.327,
                  "source": "testi-event4",
                  "event": {
                    "stream": "TEST_STREAM4",
                    "time": "2026-10-06T14:59:34.125899443Z",
                    "log": {
                      "timestamp": "2026-10-06T14:59:34.125Z",
                      "message": "{\\"appName\\":\\"APP4\\"}"
                    },
                    "host": "testi.4.lan"
                  }
                }
                {
                  "time": 1791298774328,
                  "source": "testi-event5",
                  "event": {
                    "stream": "TEST_STREAM5",
                    "time": "2026-10-06T14:59:34.125899443Z",
                    "log": {
                      "timestamp": "2026-10-06T14:59:34.125Z",
                      "message": "{\\"appName\\":\\"APP5\\"}"
                    },
                    "host": "testi.5.lan"
                  }
                }
                {
                  "time": 1791298774.32,
                  "source": "testi-event6",
                  "event": {
                    "stream": "TEST_STREAM6",
                    "time": "2026-10-06T14:59:34.125899443Z",
                    "log": {
                      "timestamp": "2026-10-06T14:59:34.125Z",
                      "message": "{\\"appName\\":\\"APP6\\"}"
                    },
                    "host": "testi.6.lan"
                  }
                }
                {
                  "time": 1791298774.4,
                  "source": "testi-event7",
                  "event": {
                    "stream": "TEST_STREAM7",
                    "time": "2026-10-06T14:59:34.125899443Z",
                    "log": {
                      "timestamp": "2026-10-06T14:59:34.125Z",
                      "message": "{\\"appName\\":\\"APP7\\"}"
                    },
                    "host": "testi.7.lan"
                  }
                }
                {
                  "time": "1791298774.",
                  "source": "testi-event8",
                  "event": {
                    "stream": "TEST_STREAM8",
                    "time": "2026-10-06T14:59:34.125899443Z",
                    "log": {
                      "timestamp": "2026-10-06T14:59:34.125Z",
                      "message": "{\\"appName\\":\\"APP8\\"}"
                    },
                    "host": "testi.8.lan"
                  }
                }
                {
                  "time": "1791298774123456789",
                  "source": "testi-event9",
                  "event": {
                    "stream": "TEST_STREAM9",
                    "time": "2026-10-06T14:59:34.125899443Z",
                    "log": {
                      "timestamp": "2026-10-06T14:59:34.125Z",
                      "message": "{\\"appName\\":\\"APP9\\"}"
                    },
                    "host": "testi.9.lan"
                  }
                }
                """;
        final Response supposedResponse = new AcknowledgedJsonResponse("Success", 1);
        Assertions
                .assertEquals(supposedResponse, service.sendEvents(request1, channel1, eventJson), "Service should return JSON object with fields 'text', 'code' and 'ackID' (ackID " + "should be " + 1 + ")");

        Assertions
                .assertEquals(9, messageList.size(), "Number of events received should match the number of sent ones");
        final Pattern pattern = Pattern.compile(".*time_source=\"(?<source>.+)\".*time_parsed=\"(?<parsed>.+)\".*time=\"(?<time>[0-9]+)\".*generated=\"(?<generated>.+)\"].*");
        final String event1 = new String(messageList.pop(), StandardCharsets.UTF_8);
        final String event2 = new String(messageList.pop(), StandardCharsets.UTF_8);
        final String event3 = new String(messageList.pop(), StandardCharsets.UTF_8);
        final String event4 = new String(messageList.pop(), StandardCharsets.UTF_8);
        final String event5 = new String(messageList.pop(), StandardCharsets.UTF_8);
        final String event6 = new String(messageList.pop(), StandardCharsets.UTF_8);
        final String event7 = new String(messageList.pop(), StandardCharsets.UTF_8);
        final String event8 = new String(messageList.pop(), StandardCharsets.UTF_8);
        final String event9 = new String(messageList.pop(), StandardCharsets.UTF_8);

        final Matcher matcher1 = pattern.matcher(event1);
        final Matcher matcher2 = pattern.matcher(event2);
        final Matcher matcher3 = pattern.matcher(event3);
        final Matcher matcher4 = pattern.matcher(event4);
        final Matcher matcher5 = pattern.matcher(event5);
        final Matcher matcher6 = pattern.matcher(event6);
        final Matcher matcher7 = pattern.matcher(event7);
        final Matcher matcher8 = pattern.matcher(event8);
        final Matcher matcher9 = pattern.matcher(event9);

        Assertions.assertTrue(event1.contains("TEST_STREAM1"), "expected1 stream should match event1");
        Assertions.assertTrue(event2.contains("TEST_STREAM2"), "expected2 stream should match event2");
        Assertions.assertTrue(event3.contains("TEST_STREAM3"), "expected3 stream should match event3");
        Assertions.assertTrue(event4.contains("TEST_STREAM4"), "expected4 stream should match event4");
        Assertions.assertTrue(event5.contains("TEST_STREAM5"), "expected5 stream should match event5");
        Assertions.assertTrue(event6.contains("TEST_STREAM6"), "expected6 stream should match event6");
        Assertions.assertTrue(event7.contains("TEST_STREAM7"), "expected7 stream should match event7");
        Assertions.assertTrue(event8.contains("TEST_STREAM8"), "expected8 stream should match event8");
        Assertions.assertTrue(event9.contains("TEST_STREAM9"), "expected9 stream should match event9");

        Assertions.assertTrue(matcher1.find());
        Assertions.assertEquals("1791298774324", matcher1.group("time"));
        Assertions.assertEquals("reported", matcher1.group("source"));
        Assertions.assertEquals("true", matcher1.group("parsed"));
        Assertions.assertEquals("false", matcher1.group("generated"));

        Assertions.assertTrue(matcher2.find());
        Assertions.assertEquals("1791298774325", matcher2.group("time"));
        Assertions.assertEquals("reported", matcher2.group("source"));
        Assertions.assertEquals("true", matcher2.group("parsed"));
        Assertions.assertEquals("false", matcher2.group("generated"));

        Assertions.assertTrue(matcher3.find());
        Assertions.assertEquals("1791298774326", matcher3.group("time"));
        Assertions.assertEquals("reported", matcher3.group("source"));
        Assertions.assertEquals("true", matcher3.group("parsed"));
        Assertions.assertEquals("false", matcher3.group("generated"));

        Assertions.assertTrue(matcher4.find());
        Assertions.assertEquals("1791298774327", matcher4.group("time"));
        Assertions.assertEquals("reported", matcher4.group("source"));
        Assertions.assertEquals("true", matcher4.group("parsed"));
        Assertions.assertEquals("false", matcher4.group("generated"));

        Assertions.assertTrue(matcher5.find());
        Assertions.assertEquals("1791298774328", matcher5.group("time"));
        Assertions.assertEquals("reported", matcher5.group("source"));
        Assertions.assertEquals("true", matcher5.group("parsed"));
        Assertions.assertEquals("false", matcher5.group("generated"));

        Assertions.assertTrue(matcher6.find());
        Assertions.assertEquals("1791298774320", matcher6.group("time"));
        Assertions.assertEquals("reported", matcher6.group("source"));
        Assertions.assertEquals("true", matcher6.group("parsed"));
        Assertions.assertEquals("false", matcher6.group("generated"));

        Assertions.assertTrue(matcher7.find());
        Assertions.assertEquals("1791298774400", matcher7.group("time"));
        Assertions.assertEquals("reported", matcher7.group("source"));
        Assertions.assertEquals("true", matcher7.group("parsed"));
        Assertions.assertEquals("false", matcher7.group("generated"));

        Assertions.assertTrue(matcher8.find());
        Assertions.assertEquals("1791298774000", matcher8.group("time"));
        Assertions.assertEquals("reported", matcher8.group("source"));
        Assertions.assertEquals("true", matcher8.group("parsed"));
        Assertions.assertEquals("false", matcher8.group("generated"));

        Assertions.assertTrue(matcher9.find());
        Assertions.assertEquals("1791298774123", matcher9.group("time"));
        Assertions.assertEquals("reported", matcher9.group("source"));
        Assertions.assertEquals("true", matcher9.group("parsed"));
        Assertions.assertEquals("false", matcher9.group("generated"));
    }

    @Test
    public void test_3_EventSyslogConversion() {
        final String eventJson = """
                {
                  "time": "1791298774.324591",
                  "source": "testi-event1",
                  "event": {
                    "stream": "TEST_STREAM1",
                    "time": "2026-10-06T14:59:34.125899443Z",
                    "log": {
                      "timestamp": "2026-10-06T14:59:34.125Z",
                      "message": "{\\"appName\\":\\"APP1\\"}"
                    },
                    "host": "testi.1.lan"
                  }
                }
                {
                  "time": 1791298774.32559,
                  "source": "testi-event2",
                  "event": {
                    "stream": "TEST_STREAM2",
                    "time": "2026-10-06T14:59:34.125899443Z",
                    "log": {
                      "timestamp": "2026-10-06T14:59:34.125Z",
                      "message": "{\\"appName\\":\\"APP2\\"}"
                    },
                    "host": "testi.2.lan"
                  }
                }
                {
                  "time": 1791298774.3269,
                  "source": "testi-event3",
                  "event": {
                    "stream": "TEST_STREAM3",
                    "time": "2026-10-06T14:59:34.125899443Z",
                    "log": {
                      "timestamp": "2026-10-06T14:59:34.125Z",
                      "message": "{\\"appName\\":\\"APP1\\"}"
                    },
                    "host": "testi.3.lan"
                  }
                }
                {
                  "time": 1791298774.327,
                  "source": "testi-event4",
                  "event": {
                    "stream": "TEST_STREAM4",
                    "time": "2026-10-06T14:59:34.125899443Z",
                    "log": {
                      "timestamp": "2026-10-06T14:59:34.125Z",
                      "message": "{\\"appName\\":\\"APP4\\"}"
                    },
                    "host": "testi.4.lan"
                  }
                }
                {
                  "time": 1791298774328,
                  "source": "testi-event5",
                  "event": {
                    "stream": "TEST_STREAM5",
                    "time": "2026-10-06T14:59:34.125899443Z",
                    "log": {
                      "timestamp": "2026-10-06T14:59:34.125Z",
                      "message": "{\\"appName\\":\\"APP5\\"}"
                    },
                    "host": "testi.5.lan"
                  }
                }
                {
                  "time": 1791298774.32,
                  "source": "testi-event6",
                  "event": {
                    "stream": "TEST_STREAM6",
                    "time": "2026-10-06T14:59:34.125899443Z",
                    "log": {
                      "timestamp": "2026-10-06T14:59:34.125Z",
                      "message": "{\\"appName\\":\\"APP6\\"}"
                    },
                    "host": "testi.6.lan"
                  }
                }
                {
                  "time": 1791298774.4,
                  "source": "testi-event7",
                  "event": {
                    "stream": "TEST_STREAM7",
                    "time": "2026-10-06T14:59:34.125899443Z",
                    "log": {
                      "timestamp": "2026-10-06T14:59:34.125Z",
                      "message": "{\\"appName\\":\\"APP7\\"}"
                    },
                    "host": "testi.7.lan"
                  }
                }
                {
                  "time": "1791298774.",
                  "source": "testi-event8",
                  "event": {
                    "stream": "TEST_STREAM8",
                    "time": "2026-10-06T14:59:34.125899443Z",
                    "log": {
                      "timestamp": "2026-10-06T14:59:34.125Z",
                      "message": "{\\"appName\\":\\"APP8\\"}"
                    },
                    "host": "testi.8.lan"
                  }
                }
                {
                  "time": "1791298774123456789",
                  "source": "testi-event9",
                  "event": {
                    "stream": "TEST_STREAM9",
                    "time": "2026-10-06T14:59:34.125899443Z",
                    "log": {
                      "timestamp": "2026-10-06T14:59:34.125Z",
                      "message": "{\\"appName\\":\\"APP9\\"}"
                    },
                    "host": "testi.9.lan"
                  }
                }
                """;
        final Response supposedResponse = new AcknowledgedJsonResponse("Success", 2);
        Assertions
                .assertEquals(supposedResponse, service.sendEvents(request1, channel1, eventJson), "Service should return JSON object with fields 'text', 'code' and 'ackID' (ackID " + "should be " + 2 + ")");

        Assertions
                .assertEquals(9, messageList.size(), "Number of events received should match the number of sent ones");

        final RFC5424Frame frame = new RFC5424Frame(true);
        final List<String> timestamps = new ArrayList<>(messageList.size());
        int executedLoops = 0;
        while (!messageList.isEmpty()) {
            frame.load(new ByteArrayInputStream(messageList.pop()));
            Assertions.assertTrue(frame.next());
            timestamps.add(frame.timestamp.toString());
            executedLoops++;
        }

        Assertions.assertEquals(9, executedLoops);
        Assertions.assertEquals(9, timestamps.size());
        Assertions.assertEquals(1791298774324L, Instant.parse(timestamps.get(0)).toEpochMilli());
        Assertions.assertEquals(1791298774325L, Instant.parse(timestamps.get(1)).toEpochMilli());
        Assertions.assertEquals(1791298774326L, Instant.parse(timestamps.get(2)).toEpochMilli());
        Assertions.assertEquals(1791298774327L, Instant.parse(timestamps.get(3)).toEpochMilli());
        Assertions.assertEquals(1791298774328L, Instant.parse(timestamps.get(4)).toEpochMilli());
        Assertions.assertEquals(1791298774320L, Instant.parse(timestamps.get(5)).toEpochMilli());
        Assertions.assertEquals(1791298774400L, Instant.parse(timestamps.get(6)).toEpochMilli());
        Assertions.assertEquals(1791298774000L, Instant.parse(timestamps.get(7)).toEpochMilli());
        Assertions.assertEquals(1791298774123L, Instant.parse(timestamps.get(8)).toEpochMilli());
    }
}
