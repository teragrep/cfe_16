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