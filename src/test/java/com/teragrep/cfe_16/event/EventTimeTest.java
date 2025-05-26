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
import com.teragrep.cfe_16.bo.DefaultHttpEventData;
import com.teragrep.cfe_16.bo.TimestampedHttpEventData;
import java.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EventTimeTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void initialize() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Test timestampedHttpEventData with epoch seconds, 10 digits")
    void testTimestampedHttpEventDataWithEpochSeconds10Digits() throws JsonProcessingException {
        String content = "{\"event\": \"Pony 1 has left the barn\", \"sourcetype\": "
                + "\"mysourcetype\", \"time\": 1277464192}";

        final JsonNode jsonNode = objectMapper.readTree(content);

        final TimestampedHttpEventData httpEventDataWithHandledTime = new EventTime(
                new TimestampedHttpEventData(),
                null,
                new DefaultJsonEvent(jsonNode).time()
        ).timestampedHttpEventData(Instant.now().toEpochMilli());

        Assertions
                .assertAll(
                        () -> Assertions
                                .assertEquals(
                                        "reported", httpEventDataWithHandledTime.timeSource(),
                                        "Time source should be 'reported' when the time is specified in a request"
                                ),
                        () -> Assertions
                                .assertTrue(
                                        httpEventDataWithHandledTime.timeParsed(),
                                        "timeParsed should be true when the time is specified in a request"
                                ),
                        () -> Assertions
                                .assertEquals(
                                        1277464192L, httpEventDataWithHandledTime.timeAsLong(),
                                        "Time should have been converted to epoch milliseconds"
                                )
                );
    }

    @Test
    @DisplayName("Test timestampedHttpEventData with no time, will use default value")
    void testTimestampedHttpEventDataWithNoTimeWillUseDefaultValue() throws JsonProcessingException {
        String content = "{\"event\": \"Pony 1 has left the barn\", \"sourcetype\": " + "\"mysourcetype\"}";

        final JsonNode jsonNode = objectMapper.readTree(content);

        final long currentEpoch = Instant.now().toEpochMilli();

        final TimestampedHttpEventData httpEventDataWithHandledTime = new EventTime(
                new TimestampedHttpEventData(),
                null,
                new DefaultJsonEvent(jsonNode).time()
        ).timestampedHttpEventData(currentEpoch);

        Assertions
                .assertAll(
                        () -> Assertions
                                .assertEquals(
                                        "generated", httpEventDataWithHandledTime.timeSource(),
                                        "Time source should be 'generated' when it's not specified in a request"
                                ),
                        () -> Assertions
                                .assertFalse(
                                        httpEventDataWithHandledTime.timeParsed(),
                                        "timeParsed should be false when time is not specified in a request"
                                ),
                        () -> Assertions
                                .assertEquals(
                                        currentEpoch, httpEventDataWithHandledTime.timeAsLong(),
                                        "Time as long should be the defaultValue provided when time is not specified in a request"
                                )
                );
    }

    @Test
    @DisplayName("Test timestampedHttpEventData with epoch seconds and decimal milliseconds")
    void testTimestampedHttpEventDataWithEpochSecondsAndDecimalMilliseconds() throws JsonProcessingException {
        String content = "{\"event\": \"Pony 1 has left the barn\", \"sourcetype\": "
                + "\"mysourcetype\", \"time\": 1433188255.253}";

        final JsonNode jsonNode = objectMapper.readTree(content);

        final TimestampedHttpEventData httpEventDataWithHandledTime = new EventTime(
                new TimestampedHttpEventData(),
                null,
                new DefaultJsonEvent(jsonNode).time()
        ).timestampedHttpEventData(Instant.now().toEpochMilli());

        Assertions
                .assertAll(
                        () -> Assertions
                                .assertEquals(
                                        "reported", httpEventDataWithHandledTime.timeSource(),
                                        "Time source should be 'reported' when the time is specified in a request"
                                ),
                        () -> Assertions
                                .assertTrue(
                                        httpEventDataWithHandledTime.timeParsed(),
                                        "timeParsed should be true when time is specified in a request"
                                ),
                        () -> Assertions
                                .assertEquals(
                                        1433188255253L, httpEventDataWithHandledTime.timeAsLong(),
                                        "Time should be converted to epoch milliseconds when it's provided in a request in "
                                                + "epoch seconds with decimals"
                                )
                );
    }

    @Test
    @DisplayName("Test handleTime with epoch milliseconds, 13 digits")
    void testTimestampedHttpEventDataWithEpochMilliseconds13digits() throws JsonProcessingException {
        String content = "{\"event\": \"Pony 1 has left the barn\", "
                + "\"sourcetype\":\"mysourcetype\", \"time\": 1433188255253}";

        final JsonNode jsonNode = objectMapper.readTree(content);

        final TimestampedHttpEventData httpEventDataWithHandledTime = new EventTime(
                new TimestampedHttpEventData(),
                null,
                new DefaultJsonEvent(jsonNode).time()
        ).timestampedHttpEventData(Instant.now().toEpochMilli());

        Assertions
                .assertAll(
                        () -> Assertions
                                .assertEquals(
                                        "reported", httpEventDataWithHandledTime.timeSource(),
                                        "Time source should be 'reported' when the time is specified in a request"
                                ),
                        () -> Assertions
                                .assertTrue(
                                        httpEventDataWithHandledTime.timeParsed(),
                                        "timeParsed should be true when time is specified in a request"
                                ),
                        () -> Assertions
                                .assertEquals(
                                        1433188255253L, httpEventDataWithHandledTime.timeAsLong(),
                                        "Time should be converted to epoch milliseconds when it's provided in a request in "
                                                + "epoch seconds with decimals"
                                )
                );
    }

    @Test
    @DisplayName("Test timestampedHttpEventData with time as String")
    void testTimestampedHttpEventDataWithTimeAsString() throws JsonProcessingException {
        String content = "{\"event\": \"Pony 1 has left the barn\", "
                + "\"sourcetype\":\"mysourcetype\", \"time\": \"1433188255253\"}";

        final JsonNode jsonNode = objectMapper.readTree(content);

        final TimestampedHttpEventData httpEventDataWithHandledTime = new EventTime(
                new TimestampedHttpEventData(),
                null,
                new DefaultJsonEvent(jsonNode).time()
        ).timestampedHttpEventData(Instant.now().toEpochMilli());

        Assertions
                .assertAll(
                        () -> Assertions
                                .assertEquals(
                                        "generated", httpEventDataWithHandledTime.timeSource(),
                                        "Time source should be 'generated' when time is given as a string in a request"
                                ),
                        () -> Assertions
                                .assertFalse(
                                        httpEventDataWithHandledTime.timeParsed(),
                                        "timeParsed should be false when time is given as a string in a request"
                                ),
                        () -> Assertions
                                .assertEquals(
                                        1433188255253L, httpEventDataWithHandledTime.timeAsLong(),
                                        "Time should be converted to long when time is given as a string in a request"
                                )
                );
    }

    @Test
    @DisplayName("Test timestampedHttpEventData with too little digits")
    void testTimestampedHttpEventDataWithTooLittleDigits() throws JsonProcessingException {
        String content = "{\"event\": \"Pony 1 has left the barn\", "
                + "\"sourcetype\":\"mysourcetype\", \"time\": 143318}";

        final JsonNode jsonNode = objectMapper.readTree(content);

        final TimestampedHttpEventData httpEventDataWithHandledTime = new EventTime(
                new TimestampedHttpEventData(),
                null,
                new DefaultJsonEvent(jsonNode).time()
        ).timestampedHttpEventData(Instant.now().toEpochMilli());

        Assertions
                .assertAll(
                        () -> Assertions
                                .assertEquals(
                                        "reported", httpEventDataWithHandledTime.timeSource(),
                                        "Time source should be 'reported' when time is given as an integer with less "
                                                + "than 10" + " digits"
                                ),
                        () -> Assertions
                                .assertTrue(
                                        httpEventDataWithHandledTime.timeParsed(),
                                        "timeParsed should be false when time is given as an integer with less than 10 "
                                                + "digits"
                                ),
                        () -> Assertions
                                .assertEquals(
                                        143318L, httpEventDataWithHandledTime.timeAsLong(),
                                        "Time as long should be as provided in the request"
                                )
                );
    }

    @Test
    @DisplayName("Test timestampedHttpEventData with epoch centiseconds")
    void testTimestampedHttpEventDataWithEpochCentiseconds() throws JsonProcessingException {
        String content = "{\"event\": \"Pony 1 has left the barn\", "
                + "\"sourcetype\":\"mysourcetype\", \"time\": 143318825525}";

        final JsonNode jsonNode = objectMapper.readTree(content);

        final TimestampedHttpEventData httpEventDataWithHandledTime = new EventTime(
                new TimestampedHttpEventData(),
                null,
                new DefaultJsonEvent(jsonNode).time()
        ).timestampedHttpEventData(Instant.now().toEpochMilli());

        Assertions
                .assertAll(
                        () -> Assertions
                                .assertEquals(
                                        "reported", httpEventDataWithHandledTime.timeSource(),
                                        "Time source should be 'reported' when the time is specified in a request "
                                                + "with 10-13 " + "digits"
                                ),
                        () -> Assertions
                                .assertTrue(
                                        httpEventDataWithHandledTime.timeParsed(),
                                        "timeParsed should be true when time is specified in a request with 10-13 digits"
                                ),
                        () -> Assertions
                                .assertEquals(
                                        143318825525L, httpEventDataWithHandledTime.timeAsLong(),
                                        "Time should be converted to epoch milliseconds when provided in a request with "
                                                + "10-13 digits"
                                )
                );
    }

    @Test
    @DisplayName("Test timestampedHttpEventData with 14 digits")
    void testTimestampedHttpEventDataWith14Digits() throws JsonProcessingException {
        String content = "{\"event\": \"Pony 1 has left the barn\", "
                + "\"sourcetype\":\"mysourcetype\", \"time\": 14331882552523}";

        final JsonNode jsonNode = objectMapper.readTree(content);

        final TimestampedHttpEventData httpEventDataWithHandledTime = new EventTime(
                new TimestampedHttpEventData(),
                null,
                new DefaultJsonEvent(jsonNode).time()
        ).timestampedHttpEventData(Instant.now().toEpochMilli());

        Assertions
                .assertAll(
                        () -> Assertions
                                .assertEquals(
                                        "reported", httpEventDataWithHandledTime.timeSource(),
                                        "Time source should be 'reported' when time is given as an integer with more "
                                                + "than 13 digits"
                                ),
                        () -> Assertions
                                .assertTrue(
                                        httpEventDataWithHandledTime.timeParsed(),
                                        "timeParsed should be false when time is given as an integer with more than 13 "
                                                + "digits"
                                ),
                        () -> Assertions
                                .assertEquals(
                                        14331882552523L, httpEventDataWithHandledTime.timeAsLong(),
                                        "Time should be as it's provided in a request."
                                )
                );
    }

    @Test
    @DisplayName("Happy equals test")
    void happyEqualsTest() throws JsonProcessingException {
        String content = "{\"event\": \"Pony 1 has left the barn\", "
                + "\"sourcetype\":\"mysourcetype\", \"time\": 1433188255252321}";

        final JsonNode jsonNode = objectMapper.readTree(content);

        final EventTime eventTime1 = new EventTime(
                new TimestampedHttpEventData(),
                null,
                new DefaultJsonEvent(jsonNode).time()
        );

        final EventTime eventTime2 = new EventTime(
                new TimestampedHttpEventData(),
                null,
                new DefaultJsonEvent(jsonNode).time()
        );

        Assertions.assertEquals(eventTime1, eventTime2);
    }

    @Test
    @DisplayName("Unhappy equals test")
    void unhappyEqualsTest() throws JsonProcessingException {
        String content = "{\"event\": \"Pony 1 has left the barn\", "
                + "\"sourcetype\":\"mysourcetype\", \"time\": 1433188255252321}";

        final JsonNode jsonNode = objectMapper.readTree(content);

        final EventTime eventTime1 = new EventTime(
                new TimestampedHttpEventData(),
                null,
                new DefaultJsonEvent(jsonNode).time()
        );

        final EventTime eventTime2 = new EventTime(
                new TimestampedHttpEventData(),
                new TimestampedHttpEventData(),
                new DefaultJsonEvent(jsonNode).time()
        );

        Assertions.assertNotEquals(eventTime1, eventTime2);
    }

    @Test
    @DisplayName("timestampedHttpEventData() sets time as null if previousEvent is constructed with default ctor")
    void timestampedHttpEventDataSetsTimeAsNullIfPreviousEventIsConstructedWithDefaultCtor() {
        final TimestampedHttpEventData previousEvent = new TimestampedHttpEventData(); // Default ctor

        final TimestampedHttpEventData currentEvent = new TimestampedHttpEventData(
                new DefaultHttpEventData(),
                "timeSource",
                "time",
                123456L,
                true
        );

        final EventTime eventTime = new EventTime(currentEvent, previousEvent, null);

        final long currentEpoch = Instant.now().toEpochMilli();
        final TimestampedHttpEventData result = eventTime.timestampedHttpEventData(currentEpoch); // Should use the defaultValue param, since previous event is default

        Assertions.assertEquals(currentEpoch, result.timeAsLong());
        Assertions.assertEquals(String.valueOf(currentEpoch), result.time());
    }
}
