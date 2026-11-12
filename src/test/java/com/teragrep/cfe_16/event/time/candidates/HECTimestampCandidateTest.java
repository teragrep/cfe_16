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
package com.teragrep.cfe_16.event.time.candidates;

import com.teragrep.cfe_16.event.time.Instantiable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class HECTimestampCandidateTest {

    @Test
    void testInvalidInput() {
        final HECTimestampCandidate candidates = new HECTimestampCandidateImpl();
        final Instantiable i = candidates.toInstantiable("not a valid time");
        Assertions.assertTrue(i.isStub());
        Assertions.assertThrows(UnsupportedOperationException.class, i::toInstant);
    }

    @Test
    void test10Digits() {
        final HECTimestampCandidate candidates = new HECTimestampCandidateImpl();
        final Instantiable i = candidates.toInstantiable("1234567890");
        Assertions.assertFalse(i.isStub());
        Assertions.assertEquals(1234567890000L, i.toInstant().toEpochMilli());
        Assertions.assertEquals(0, i.toInstant().getNano());
    }

    @Test
    void test13Digits() {
        final HECTimestampCandidate candidates = new HECTimestampCandidateImpl();
        final Instantiable i = candidates.toInstantiable("1234567899999");
        Assertions.assertFalse(i.isStub());
        Assertions.assertEquals(1234567899999L, i.toInstant().toEpochMilli());
        Assertions.assertEquals(999000000, i.toInstant().getNano());
    }

    @Test
    void test16Digits() {
        final HECTimestampCandidate candidates = new HECTimestampCandidateImpl();
        final Instantiable i = candidates.toInstantiable("1234567899999999");
        Assertions.assertFalse(i.isStub());
        Assertions.assertEquals(1234567899999L, i.toInstant().toEpochMilli());
        Assertions.assertEquals(999999000, i.toInstant().getNano());
    }

    @Test
    void test19Digits() {
        final HECTimestampCandidate candidates = new HECTimestampCandidateImpl();
        final Instantiable i = candidates.toInstantiable("1234567899999999999");
        Assertions.assertFalse(i.isStub());
        Assertions.assertEquals(1234567899999L, i.toInstant().toEpochMilli());
        Assertions.assertEquals(999999999, i.toInstant().getNano());
    }

    @Test
    void testOtherAmountOfDigits() {
        final HECTimestampCandidate candidates = new HECTimestampCandidateImpl();
        final Instantiable i = candidates.toInstantiable("12345678999999999999999");
        Assertions.assertTrue(i.isStub());
        Assertions.assertThrows(UnsupportedOperationException.class, i::toInstant);
    }

    @Test
    void testEndsInPeriod() {
        final HECTimestampCandidate candidates = new HECTimestampCandidateImpl();
        final Instantiable i = candidates.toInstantiable("1433188255.");
        Assertions.assertFalse(i.isStub());
        Assertions.assertEquals(1433188255000L, i.toInstant().toEpochMilli());
        Assertions.assertEquals(0, i.toInstant().getNano());
    }

    @Test
    void test1DecimalPlaces() {
        final HECTimestampCandidate candidates = new HECTimestampCandidateImpl();
        final Instantiable i = candidates.toInstantiable("1433188255.1");
        Assertions.assertFalse(i.isStub());
        Assertions.assertEquals(1433188255100L, i.toInstant().toEpochMilli());
        Assertions.assertEquals(100000000, i.toInstant().getNano());
    }

    @Test
    void test2DecimalPlaces() {
        final HECTimestampCandidate candidates = new HECTimestampCandidateImpl();
        final Instantiable i = candidates.toInstantiable("1433188255.12");
        Assertions.assertFalse(i.isStub());
        Assertions.assertEquals(1433188255120L, i.toInstant().toEpochMilli());
        Assertions.assertEquals(120000000, i.toInstant().getNano());
    }

    @Test
    void test3DecimalPlaces() {
        final HECTimestampCandidate candidates = new HECTimestampCandidateImpl();
        final Instantiable i = candidates.toInstantiable("1433188255.123");
        Assertions.assertFalse(i.isStub());
        Assertions.assertEquals(1433188255123L, i.toInstant().toEpochMilli());
        Assertions.assertEquals(123000000, i.toInstant().getNano());
    }

    @Test
    void test4DecimalPlaces() {
        final HECTimestampCandidate candidates = new HECTimestampCandidateImpl();
        final Instantiable i = candidates.toInstantiable("1433188255.1234");
        Assertions.assertFalse(i.isStub());
        Assertions.assertEquals(1433188255123L, i.toInstant().toEpochMilli());
        Assertions.assertEquals(123400000, i.toInstant().getNano());
    }

    @Test
    void test5DecimalPlaces() {
        final HECTimestampCandidate candidates = new HECTimestampCandidateImpl();
        final Instantiable i = candidates.toInstantiable("1433188255.12345");
        Assertions.assertFalse(i.isStub());
        Assertions.assertEquals(1433188255123L, i.toInstant().toEpochMilli());
        Assertions.assertEquals(123450000, i.toInstant().getNano());
    }

    @Test
    void test6DecimalPlaces() {
        final HECTimestampCandidate candidates = new HECTimestampCandidateImpl();
        final Instantiable i = candidates.toInstantiable("1433188255.123456");
        Assertions.assertFalse(i.isStub());
        Assertions.assertEquals(1433188255123L, i.toInstant().toEpochMilli());
        Assertions.assertEquals(123456000, i.toInstant().getNano());
    }

    @Test
    void test7DecimalPlaces() {
        final HECTimestampCandidate candidates = new HECTimestampCandidateImpl();
        final Instantiable i = candidates.toInstantiable("1433188255.1234567");
        Assertions.assertFalse(i.isStub());
        Assertions.assertEquals(1433188255123L, i.toInstant().toEpochMilli());
        Assertions.assertEquals(123456700, i.toInstant().getNano());
    }

    @Test
    void test8DecimalPlaces() {
        final HECTimestampCandidate candidates = new HECTimestampCandidateImpl();
        final Instantiable i = candidates.toInstantiable("1433188255.12345678");
        Assertions.assertFalse(i.isStub());
        Assertions.assertEquals(1433188255123L, i.toInstant().toEpochMilli());
        Assertions.assertEquals(123456780, i.toInstant().getNano());
    }

    @Test
    void test9DecimalPlaces() {
        final HECTimestampCandidate candidates = new HECTimestampCandidateImpl();
        final Instantiable i = candidates.toInstantiable("1433188255.123456789");
        Assertions.assertFalse(i.isStub());
        Assertions.assertEquals(1433188255123L, i.toInstant().toEpochMilli());
        Assertions.assertEquals(123456789, i.toInstant().getNano());
    }

    @Test
    void test10DecimalPlaces() {
        final HECTimestampCandidate candidates = new HECTimestampCandidateImpl();
        final Instantiable i = candidates.toInstantiable("1433188255.1234567890");
        Assertions.assertTrue(i.isStub());
        Assertions.assertThrows(UnsupportedOperationException.class, i::toInstant);
    }
}
