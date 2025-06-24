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
package com.teragrep.cfe_16.bo;

import com.teragrep.cfe_16.event.Event;
import com.teragrep.cfe_16.event.EventImpl;
import com.teragrep.cfe_16.event.time.SpecifiedTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HECRecordImplTest {

    @Test
    @DisplayName("TimeSource() returns \"generated\" if time field is null")
    void timeSourceReturnsGeneratedIfTimeFieldIsNull() {
        final HECRecordImpl httpEventDataImpl = new HECRecordImpl(
                "channel",
                new EventImpl("event"),
                "authToken",
                1,
                new SpecifiedTime(123L, null, true, "timeSource")
        );

        Assertions.assertEquals("generated", httpEventDataImpl.time().source());
    }

    @Test
    @DisplayName("TimeSource() returns the timeSource even if time is less than 10 characters long")
    void timeSourceReturnsTheTimeSourceEvenIfTimeIsLessThan10CharactersLong() {
        final HECRecordImpl httpEventDataImpl = new HECRecordImpl(
                "channel",
                new EventImpl("event"),
                "authToken",
                1,
                new SpecifiedTime(123L, "123456", true, "timeSource")
        );

        Assertions.assertEquals("timeSource", httpEventDataImpl.time().source());
    }

    @Test
    @DisplayName("TimeSource() returns the timeSource even if time is more than 13 characters long")
    void timeSourceReturnsTheTimeSourceEvenIfTimeIsMoreThan13CharactersLong() {
        final HECRecordImpl httpEventDataImpl = new HECRecordImpl(
                "channel",
                new EventImpl("event"),
                "authToken",
                1,
                new SpecifiedTime(123L, "12345678901234", true, "timeSource")
        );

        Assertions.assertEquals("timeSource", httpEventDataImpl.time().source());
    }

    @Test
    @DisplayName("TimeSource returns timeSource if time is between 10 and 13 characters")
    void timeSourceReturnsTimeSourceIfTimeIsBetween10And13Characters() {
        final HECRecordImpl httpEventDataImpl = new HECRecordImpl(
                "channel",
                new EventImpl("event"),
                "authToken",
                1,
                new SpecifiedTime(123L, "1234567890123", true, "timeSource")
        );

        Assertions.assertEquals("timeSource", httpEventDataImpl.time().source());
    }

    @Test
    @DisplayName("Happy equals test")
    void happyEqualsTest() {
        final HECRecordImpl httpEventDataImpl1 = new HECRecordImpl(
                "channel",
                new EventImpl("event"),
                "authToken",
                1,
                new SpecifiedTime(123L, "1234567890123", true, "timeSource")
        );

        final HECRecordImpl httpEventDataImpl2 = new HECRecordImpl(
                "channel",
                new EventImpl("event"),
                "authToken",
                1,
                new SpecifiedTime(123L, "1234567890123", true, "timeSource")
        );
        Assertions.assertEquals(httpEventDataImpl1, httpEventDataImpl2);
    }

    @Test
    @DisplayName("Unhappy equals test")
    void unhappyEqualsTest() {
        final HECRecordImpl httpEventDataImpl1 = new HECRecordImpl(
                "channel",
                new EventImpl("event"),
                "authToken",
                1,
                new SpecifiedTime(123L, "1234567890123", true, "timeSource")
        );

        final HECRecordImpl httpEventDataImpl2 = new HECRecordImpl(
                "channel",
                new EventImpl("event"),
                "authToken",
                1,
                new SpecifiedTime(123L, "1234567890123", true, "timeSourceIsNotTheSame")
        );

        Assertions.assertNotEquals(httpEventDataImpl1, httpEventDataImpl2);
    }

    @Test
    @DisplayName("Event returns event")
    void eventReturnsEvent() {
        final HECRecordImpl defaultHttpEventData = new HECRecordImpl(
                "channel",
                new EventImpl("event"),
                "authToken",
                1,
                new SpecifiedTime(123L, "1234567890123", true, "timeSource")
        );

        final Event expectedResult = new EventImpl("event");

        Assertions.assertEquals(expectedResult, defaultHttpEventData.event());
    }

    @Test
    @DisplayName("Channel returns channel")
    void channelReturnsChannel() {
        final HECRecordImpl defaultHttpEventData = new HECRecordImpl(
                "channel",
                new EventImpl("event"),
                "authToken",
                1,
                new SpecifiedTime(123L, "1234567890123", true, "timeSource")
        );

        final String expectedResult = "channel";

        Assertions.assertEquals(expectedResult, defaultHttpEventData.channel());
    }

    @Test
    @DisplayName("AuthenticationToken returns authentication token")
    void authenticationTokenReturnsAuthenticationToken() {
        final HECRecordImpl defaultHttpEventData = new HECRecordImpl(
                "channel",
                new EventImpl("event"),
                "authToken",
                1,
                new SpecifiedTime(123L, "1234567890123", true, "timeSource")
        );

        final String expectedResult = "authToken";

        Assertions.assertEquals(expectedResult, defaultHttpEventData.authenticationToken());
    }

    @Test
    @DisplayName("AckID returns ackID if not null")
    void ackIdReturnsAckIdIfNotNull() {
        final HECRecordImpl defaultHttpEventData = new HECRecordImpl(
                "channel",
                new EventImpl("event"),
                "authToken",
                123,
                new SpecifiedTime(123L, "1234567890123", true, "timeSource")
        );

        final Integer expectedResult = 123;

        Assertions.assertEquals(expectedResult, defaultHttpEventData.ackID());
    }

    @Test
    @DisplayName("AckID returns null if ackID is null")
    void ackIdReturnsNullIfAckIdIsNull() {
        final HECRecordImpl defaultHttpEventData = new HECRecordImpl(
                "channel",
                new EventImpl("event"),
                "authToken",
                null,
                new SpecifiedTime(123L, "1234567890123", true, "timeSource")
        );

        Assertions.assertNull(defaultHttpEventData.ackID());
    }
}
