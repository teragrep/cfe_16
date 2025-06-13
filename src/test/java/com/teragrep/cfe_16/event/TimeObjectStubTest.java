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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimeObjectStubTest {

    @Test
    @DisplayName("isDouble() throws an UnsupportedOperationException if called")
    void isDoubleThrowsAnUnsupportedOperationExceptionIfCalled() {
        final TimeObjectStub stub = new TimeObjectStub();

        final UnsupportedOperationException unsupportedOperationException = Assertions
                .assertThrowsExactly(UnsupportedOperationException.class, stub::isDouble);

        Assertions
                .assertEquals(
                        "TimeObjectStub does not support this", unsupportedOperationException.getMessage(),
                        "Exception message was not what was expected"
                );
    }

    @Test
    @DisplayName("asDouble() throws an UnsupportedOperationException if called")
    void asDoubleThrowsAnUnsupportedOperationExceptionIfCalled() {
        final TimeObjectStub stub = new TimeObjectStub();

        final UnsupportedOperationException unsupportedOperationException = Assertions
                .assertThrowsExactly(UnsupportedOperationException.class, stub::asDouble);

        Assertions
                .assertEquals(
                        "TimeObjectStub does not support this", unsupportedOperationException.getMessage(),
                        "Exception message was not what was expected"
                );
    }

    @Test
    @DisplayName("canConvertToLong() throws an UnsupportedOperationException if called")
    void canConvertToLongThrowsAnUnsupportedOperationExceptionIfCalled() {
        final TimeObjectStub stub = new TimeObjectStub();

        final UnsupportedOperationException unsupportedOperationException = Assertions
                .assertThrowsExactly(UnsupportedOperationException.class, stub::canConvertToLong);

        Assertions
                .assertEquals(
                        "TimeObjectStub does not support this", unsupportedOperationException.getMessage(),
                        "Exception message was not what was expected"
                );
    }

    @Test
    @DisplayName("asLong() throws an UnsupportedOperationException if called")
    void asLongThrowsAnUnsupportedOperationExceptionIfCalled() {
        final TimeObjectStub stub = new TimeObjectStub();

        final UnsupportedOperationException unsupportedOperationException = Assertions
                .assertThrowsExactly(UnsupportedOperationException.class, stub::asLong);

        Assertions
                .assertEquals(
                        "TimeObjectStub does not support this", unsupportedOperationException.getMessage(),
                        "Exception message was not what was expected"
                );
    }

    @Test
    @DisplayName("asLong(defaultValue) throws an UnsupportedOperationException if called")
    void asLongWithDefaultValueThrowsAnUnsupportedOperationExceptionIfCalled() {
        final TimeObjectStub stub = new TimeObjectStub();

        final UnsupportedOperationException unsupportedOperationException = Assertions
                .assertThrowsExactly(UnsupportedOperationException.class, () -> stub.asLong(0L));

        Assertions
                .assertEquals(
                        "TimeObjectStub does not support this", unsupportedOperationException.getMessage(),
                        "Exception message was not what was expected"
                );
    }

    @Test
    @DisplayName("isTextual() throws an UnsupportedOperationException if called")
    void isTextualThrowsAnUnsupportedOperationExceptionIfCalled() {
        final TimeObjectStub stub = new TimeObjectStub();

        final UnsupportedOperationException unsupportedOperationException = Assertions
                .assertThrowsExactly(UnsupportedOperationException.class, stub::isTextual);

        Assertions
                .assertEquals(
                        "TimeObjectStub does not support this", unsupportedOperationException.getMessage(),
                        "Exception message was not what was expected"
                );
    }

    @Test
    @DisplayName("asText() throws an UnsupportedOperationException if called")
    void asTextThrowsAnUnsupportedOperationExceptionIfCalled() {
        final TimeObjectStub stub = new TimeObjectStub();

        final UnsupportedOperationException unsupportedOperationException = Assertions
                .assertThrowsExactly(UnsupportedOperationException.class, stub::asText);

        Assertions
                .assertEquals(
                        "TimeObjectStub does not support this", unsupportedOperationException.getMessage(),
                        "Exception message was not what was expected"
                );
    }

    @Test
    @DisplayName("hashCode() throws an UnsupportedOperationException if called")
    void hashCodeThrowsAnUnsupportedOperationExceptionIfCalled() {
        final TimeObjectStub stub = new TimeObjectStub();

        final UnsupportedOperationException unsupportedOperationException = Assertions
                .assertThrowsExactly(UnsupportedOperationException.class, stub::hashCode);

        Assertions
                .assertEquals(
                        "TimeObjectStub does not support this", unsupportedOperationException.getMessage(),
                        "Exception message was not what was expected"
                );
    }

    @Test
    @DisplayName("equals() throws an UnsupportedOperationException if called")
    void equalsThrowsAnUnsupportedOperationExceptionIfCalled() {
        final TimeObjectStub stub = new TimeObjectStub();

        final UnsupportedOperationException unsupportedOperationException = Assertions
                .assertThrowsExactly(UnsupportedOperationException.class, () -> stub.equals(new Object()));

        Assertions
                .assertEquals(
                        "TimeObjectStub does not support this", unsupportedOperationException.getMessage(),
                        "Exception message was not what was expected"
                );
    }

    @Test
    @DisplayName("isStub returns true")
    void isStubReturnsTrue() {
        final TimeObjectStub stub = new TimeObjectStub();

        Assertions.assertTrue(stub::isStub);
    }
}
