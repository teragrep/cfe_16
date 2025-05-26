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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimestampedHttpEventDataTest {

    @Test
    @DisplayName("TimeSource() returns \"generated\" if time field is null")
    void timeSourceReturnsGeneratedIfTimeFieldIsNull() {
        final TimestampedHttpEventData timestampedHttpEventData = new TimestampedHttpEventData(
                new DefaultHttpEventData(),
                "timeSource",
                null,
                123L,
                true
        );

        Assertions.assertEquals("generated", timestampedHttpEventData.timeSource());
    }

    @Test
    @DisplayName("TimeSource() returns the timeSource even if time is less than 10 characters long")
    void timeSourceReturnsTheTimeSourceEvenIfTimeIsLessThan10CharactersLong() {
        final TimestampedHttpEventData timestampedHttpEventData = new TimestampedHttpEventData(
                new DefaultHttpEventData(),
                "timeSource",
                "123456",
                123L,
                true
        );

        Assertions.assertEquals("timeSource", timestampedHttpEventData.timeSource());
    }

    @Test
    @DisplayName("TimeSource() returns the timeSource even if time is more than 13 characters long")
    void timeSourceReturnsTheTimeSourceEvenIfTimeIsMoreThan13CharactersLong() {
        final TimestampedHttpEventData timestampedHttpEventData = new TimestampedHttpEventData(
                new DefaultHttpEventData(),
                "timeSource",
                "12345678901234",
                123L,
                true
        );

        Assertions.assertEquals("timeSource", timestampedHttpEventData.timeSource());
    }

    @Test
    @DisplayName("TimeSource returns timeSource if time is between 10 and 13 characters")
    void timeSourceReturnsTimeSourceIfTimeIsBetween10And13Characters() {
        final TimestampedHttpEventData timestampedHttpEventData = new TimestampedHttpEventData(
                new DefaultHttpEventData(),
                "timeSource",
                "1234567890123",
                123L,
                true
        );

        Assertions.assertEquals("timeSource", timestampedHttpEventData.timeSource());
    }

    @Test
    @DisplayName("Happy equals test")
    void happyEqualsTest() {
        final TimestampedHttpEventData timestampedHttpEventData1 = new TimestampedHttpEventData(
                new DefaultHttpEventData(),
                "timeSource",
                "1234567890123",
                123L,
                true
        );

        final TimestampedHttpEventData timestampedHttpEventData2 = new TimestampedHttpEventData(
                new DefaultHttpEventData(),
                "timeSource",
                "1234567890123",
                123L,
                true
        );
        Assertions.assertEquals(timestampedHttpEventData1, timestampedHttpEventData2);
    }

    @Test
    @DisplayName("Unhappy equals test")
    void unhappyEqualsTest() {
        final TimestampedHttpEventData timestampedHttpEventData1 = new TimestampedHttpEventData(
                new DefaultHttpEventData(),
                "timeSource",
                "1234567890123",
                123L,
                true
        );

        final TimestampedHttpEventData timestampedHttpEventData2 = new TimestampedHttpEventData(
                new DefaultHttpEventData(),
                "timeSourceIsNotTheSame",
                "1234567890123",
                123L,
                true
        );

        Assertions.assertNotEquals(timestampedHttpEventData1, timestampedHttpEventData2);
    }

    @Test
    @DisplayName("isDefault() returns false if object is not default")
    void isDefaultReturnsFalseIfObjectIsIsDefault() {
        final TimestampedHttpEventData timestampedHttpEventData = new TimestampedHttpEventData(
                new DefaultHttpEventData(),
                "timeSource",
                "1234567890123",
                123L,
                true
        );

        Assertions.assertFalse(timestampedHttpEventData.isDefault());
    }

    @Test
    @DisplayName("isDefault() returns true if object is default")
    void isDefaultReturnsTrueIfObjectIsDefault() {
        final TimestampedHttpEventData timestampedHttpEventData = new TimestampedHttpEventData();

        Assertions.assertTrue(timestampedHttpEventData.isDefault());
    }
}
