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

class TimestampedHttpEventDataStubTest {

    @Test
    @DisplayName("event() throws IllegalStateException if called")
    void eventThrowsIllegalStateExceptionIfCalled() {
        final TimestampedHttpEventDataStub stub = new TimestampedHttpEventDataStub();

        final IllegalStateException illegalStateException = Assertions
                .assertThrowsExactly(IllegalStateException.class, stub::event);

        Assertions
                .assertEquals(
                        "TimestampedHttpEventDataStub does not support this", illegalStateException.getMessage(),
                        "Exception message was not what was expected"
                );
    }

    @Test
    @DisplayName("channel() throws IllegalStateException if called")
    void channelThrowsIllegalStateExceptionIfCalled() {
        final TimestampedHttpEventDataStub stub = new TimestampedHttpEventDataStub();

        final IllegalStateException illegalStateException = Assertions
                .assertThrowsExactly(IllegalStateException.class, stub::channel);

        Assertions
                .assertEquals(
                        "TimestampedHttpEventDataStub does not support this", illegalStateException.getMessage(),
                        "Exception message was not what was expected"
                );
    }

    @Test
    @DisplayName("authenticationToken() throws IllegalStateException if called")
    void authenticationTokenThrowsIllegalStateExceptionIfCalled() {
        final TimestampedHttpEventDataStub stub = new TimestampedHttpEventDataStub();

        final IllegalStateException illegalStateException = Assertions
                .assertThrowsExactly(IllegalStateException.class, stub::authenticationToken);

        Assertions
                .assertEquals(
                        "TimestampedHttpEventDataStub does not support this", illegalStateException.getMessage(),
                        "Exception message was not what was expected"
                );
    }

    @Test
    @DisplayName("ackID() throws IllegalStateException if called")
    void ackIDThrowsIllegalStateExceptionIfCalled() {
        final TimestampedHttpEventDataStub stub = new TimestampedHttpEventDataStub();

        final IllegalStateException illegalStateException = Assertions
                .assertThrowsExactly(IllegalStateException.class, stub::ackID);

        Assertions
                .assertEquals(
                        "TimestampedHttpEventDataStub does not support this", illegalStateException.getMessage(),
                        "Exception message was not what was expected"
                );
    }

    @Test
    @DisplayName("timeSource() throws IllegalStateException if called")
    void timeSourceThrowsIllegalStateExceptionIfCalled() {
        final TimestampedHttpEventDataStub stub = new TimestampedHttpEventDataStub();

        final IllegalStateException illegalStateException = Assertions
                .assertThrowsExactly(IllegalStateException.class, stub::timeSource);

        Assertions
                .assertEquals(
                        "TimestampedHttpEventDataStub does not support this", illegalStateException.getMessage(),
                        "Exception message was not what was expected"
                );
    }

    @Test
    @DisplayName("time() throws IllegalStateException if called")
    void timeThrowsIllegalStateExceptionIfCalled() {
        final TimestampedHttpEventDataStub stub = new TimestampedHttpEventDataStub();

        final IllegalStateException illegalStateException = Assertions
                .assertThrowsExactly(IllegalStateException.class, stub::time);

        Assertions
                .assertEquals(
                        "TimestampedHttpEventDataStub does not support this", illegalStateException.getMessage(),
                        "Exception message was not what was expected"
                );
    }

    @Test
    @DisplayName("timeAsLong() throws IllegalStateException if called")
    void timeAsLongThrowsIllegalStateExceptionIfCalled() {
        final TimestampedHttpEventDataStub stub = new TimestampedHttpEventDataStub();

        final IllegalStateException illegalStateException = Assertions
                .assertThrowsExactly(IllegalStateException.class, stub::timeAsLong);

        Assertions
                .assertEquals(
                        "TimestampedHttpEventDataStub does not support this", illegalStateException.getMessage(),
                        "Exception message was not what was expected"
                );
    }

    @Test
    @DisplayName("timeParsed() throws IllegalStateException if called")
    void timeParsedThrowsIllegalStateExceptionIfCalled() {
        final TimestampedHttpEventDataStub stub = new TimestampedHttpEventDataStub();

        final IllegalStateException illegalStateException = Assertions
                .assertThrowsExactly(IllegalStateException.class, stub::timeParsed);

        Assertions
                .assertEquals(
                        "TimestampedHttpEventDataStub does not support this", illegalStateException.getMessage(),
                        "Exception message was not what was expected"
                );
    }

    @Test
    @DisplayName("hashCode() throws IllegalStateException if called")
    void hashCodeThrowsIllegalStateExceptionIfCalled() {
        final TimestampedHttpEventDataStub stub = new TimestampedHttpEventDataStub();

        final IllegalStateException illegalStateException = Assertions
                .assertThrowsExactly(IllegalStateException.class, stub::hashCode);

        Assertions
                .assertEquals(
                        "TimestampedHttpEventDataStub does not support this", illegalStateException.getMessage(),
                        "Exception message was not what was expected"
                );
    }

    @Test
    @DisplayName("equals() throws IllegalStateException if called")
    void equalsThrowsIllegalStateExceptionIfCalled() {
        final TimestampedHttpEventDataStub stub = new TimestampedHttpEventDataStub();

        final IllegalStateException illegalStateException = Assertions
                .assertThrowsExactly(IllegalStateException.class, () -> stub.equals(new Object()));

        Assertions
                .assertEquals(
                        "TimestampedHttpEventDataStub does not support this", illegalStateException.getMessage(),
                        "Exception message was not what was expected"
                );
    }

    @Test
    @DisplayName("isStub() returns true")
    void isStubReturnsTrue() {
        final TimestampedHttpEventDataStub stub = new TimestampedHttpEventDataStub();
        Assertions.assertTrue(stub::isStub, "isStub() did not return true");
    }
}
