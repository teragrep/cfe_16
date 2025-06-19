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
package com.teragrep.cfe_16.event.time;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teragrep.cfe_16.event.TimeObjectImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DoubleTimeTest {

    @Test
    @DisplayName("happy equals test")
    void happyEqualsTest() {
        final DoubleTime specifiedTime1 = new DoubleTime(
                new TimeObjectImpl(new ObjectMapper().createObjectNode().numberNode(123.45))
        );
        final DoubleTime specifiedTime2 = new DoubleTime(
                new TimeObjectImpl(new ObjectMapper().createObjectNode().numberNode(123.45))
        );

        Assertions.assertEquals(specifiedTime1, specifiedTime2);
    }

    @Test
    @DisplayName("unhappy equals test")
    void unhappyEqualsTest() {
        final DoubleTime specifiedTime1 = new DoubleTime(
                new TimeObjectImpl(new ObjectMapper().createObjectNode().numberNode(123.45))
        );
        final DoubleTime specifiedTime2 = new DoubleTime(
                new TimeObjectImpl(new ObjectMapper().createObjectNode().numberNode(123.456))
        );

        Assertions.assertNotEquals(specifiedTime1, specifiedTime2);
    }

    @Test
    @DisplayName("asLong() returns the timeAsLong field")
    void asLongReturnsTheTimeAsLongField() {
        final DoubleTime specifiedTime1 = new DoubleTime(
                new TimeObjectImpl(new ObjectMapper().createObjectNode().numberNode(123.45))
        );

        Assertions.assertEquals(12345L, specifiedTime1.asLong());
    }

    @Test
    @DisplayName("asString() returns the numberNode value as String")
    void asStringReturnsTheNumberNodeValueAsString() {
        final DoubleTime specifiedTime1 = new DoubleTime(
                new TimeObjectImpl(new ObjectMapper().createObjectNode().numberNode(123.45))
        );

        Assertions.assertEquals("12345", specifiedTime1.asString());
    }

    @Test
    @DisplayName("parsed() returns true")
    void parsedReturnsTrue() {
        final DoubleTime specifiedTime1 = new DoubleTime(
                new TimeObjectImpl(new ObjectMapper().createObjectNode().numberNode(123.45))
        );

        Assertions.assertTrue(specifiedTime1.parsed());
    }

    @Test
    @DisplayName("source() returns \"reported\"")
    void sourceReturnsReported() {
        final DoubleTime specifiedTime1 = new DoubleTime(
                new TimeObjectImpl(new ObjectMapper().createObjectNode().numberNode(123.45))
        );

        Assertions.assertEquals("reported", specifiedTime1.source());
    }
}
