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
package com.teragrep.cfe_16;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonSyntaxException;
import com.teragrep.cfe_16.bo.HECRecord;
import com.teragrep.cfe_16.bo.HECRecordImpl;
import com.teragrep.cfe_16.bo.HeaderInfo;
import com.teragrep.cfe_16.event.EventImpl;
import com.teragrep.cfe_16.event.time.HECTimeImpl;
import com.teragrep.cfe_16.event.time.HECTimeImplWithFallback;
import com.teragrep.cfe_16.event.time.HECTimeStub;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HECBatchTest {

    private static final String channel1 = "CHANNEL_11111";
    private static final String authToken1 = "AUTH_TOKEN_12223";

    @Test
    public void asHttpEventDataListTest() {
        String allEventsInJson = "{\"sourcetype\": \"mysourcetype\", \"event\": \"Hello, world!\", \"host\": \"localhost\", \"source\": \"mysource\", \"index\": \"myindex\", \"time\": 123456}";
        HECRecord supposedResponse = new HECRecordImpl(
                channel1,
                new EventImpl("Hello, world!"),
                authToken1,
                0,
                new HECTimeImplWithFallback(new HECTimeImpl(new ObjectMapper().createObjectNode().numberNode(123456)), new HECTimeStub()), new HeaderInfo()
        );
        final List<HECRecord> supposedList = new ArrayList<>();
        supposedList.add(supposedResponse);

        final HECBatch HECBatch = new HECBatch(authToken1, channel1, allEventsInJson, new HeaderInfo());
        List<HECRecord> response = HECBatch.asHttpEventDataList();
        Assertions.assertEquals(supposedList, response, "Should get a JSON with fields text, code and ackID");
    }

    /**
     * Tests for JsonSyntaxException
     */
    @Test
    public void asHttpEventDataListUsesAStubIfParsingFailsWithMalformedJSONTest() {
        String allEventsInJson = "{\"sourcetype\": \"mysourcetype\", \"event\": {{{{}}}}";
        final HECBatch HECBatch = new HECBatch(authToken1, channel1, allEventsInJson, new HeaderInfo());

        Assertions.assertThrowsExactly(JsonSyntaxException.class, () -> HECBatch.asHttpEventDataList().toString());
    }

    /**
     * Tests for EventStub existence, since the Event should not be valid
     */
    @Test
    public void asHttpEventDataListUsesAStubIfParsingFailsWithEmptyJSONTest() {
        String allEventsInJson = "{\"sourcetype\": \"mysourcetype\", \"event\": null}";
        String supposedResponse = "EventStub does not support this";
        final HECBatch HECBatch = new HECBatch(authToken1, channel1, allEventsInJson, new HeaderInfo());
        Exception exception = Assertions
                .assertThrowsExactly(
                        UnsupportedOperationException.class, () -> HECBatch.asHttpEventDataList().toString()
                );
        Assertions
                .assertEquals(
                        supposedResponse, exception.getMessage(), "Exception message was not what it was supposed to be"
                );
    }

    @Test
    public void noEventFieldInRequestTest() {
        String allEventsInJson = "{\"sourcetype\": \"mysourcetype\", \"host\": \"localhost\", \"source\": \"mysource\", \"index\": \"myindex\"}";
        final HECBatch HECBatch = new HECBatch(authToken1, channel1, allEventsInJson, new HeaderInfo());

        Assertions.assertThrows(UnsupportedOperationException.class, () -> HECBatch.asHttpEventDataList().toString());
    }

    @Test
    public void eventFieldBlankInRequestTest() {
        String allEventsInJson = "{\"sourcetype\": \"mysourcetype\", \"event\": \"\", \"host\": \"localhost\", \"source\": \"mysource\", \"index\": \"myindex\"}";
        final HECBatch HECBatch = new HECBatch(authToken1, channel1, allEventsInJson, new HeaderInfo());

        Assertions.assertThrows(UnsupportedOperationException.class, () -> HECBatch.asHttpEventDataList().toString());
    }
}
