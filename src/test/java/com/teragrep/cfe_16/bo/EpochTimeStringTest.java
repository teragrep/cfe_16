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

class EpochTimeStringTest {

    @Test
    @DisplayName("Happy equals test")
    void happyEqualsTest() {
        final EpochTimeString epochTimeString1 = new EpochTimeString("123", 123L);
        final EpochTimeString epochTimeString2 = new EpochTimeString("123", 123L);

        Assertions.assertEquals(epochTimeString1, epochTimeString2);
    }

    @Test
    @DisplayName("Unhappy equals test")
    void unhappyEqualsTest() {
        final EpochTimeString epochTimeString1 = new EpochTimeString("123", 123L);
        final EpochTimeString epochTimeString2 = new EpochTimeString("1234567", 123L);

        Assertions.assertNotEquals(epochTimeString1, epochTimeString2);
    }

    @Test
    @DisplayName("asEpochMillis() returns timeAsLong if timeString is 13 characters long")
    void asEpochMillisReturnsTimeAsLongIfTimeStringIs13CharactersLong() {
        final EpochTimeString epochTimeString = new EpochTimeString("1234567890123", 123L);

        final long expectedReturn = 123L;

        Assertions.assertEquals(expectedReturn, epochTimeString.asEpochMillis());
    }

    @Test
    @DisplayName("asEpochMillis() converts a 10 digit epoch timestamp to 13 characters")
    void asEpochMillisConvertsA10DigitEpochTimestampTo13Characters() {
        final EpochTimeString epochTimeString = new EpochTimeString("1234567890", 1234567890L);

        final long expectedReturn = 1234567890000L;

        Assertions.assertEquals(expectedReturn, epochTimeString.asEpochMillis());
    }

    @Test
    @DisplayName("asEpochMillis() converts an 11 digit epoch timestamp to 13 characters")
    void asEpochMillisConvertsAn11DigitEpochTimestampTo13Characters() {
        final EpochTimeString epochTimeString = new EpochTimeString("12345678901", 12345678901L);

        final long expectedReturn = 1234567890100L;

        Assertions.assertEquals(expectedReturn, epochTimeString.asEpochMillis());
    }

    @Test
    @DisplayName("asEpochMillis() converts a 12 digit epoch timestamp to 13 characters")
    void asEpochMillisConvertsA12DigitEpochTimestampTo13Characters() {
        final EpochTimeString epochTimeString = new EpochTimeString("123456789012", 123456789012L);

        final long expectedReturn = 1234567890120L;

        Assertions.assertEquals(expectedReturn, epochTimeString.asEpochMillis());
    }
}
