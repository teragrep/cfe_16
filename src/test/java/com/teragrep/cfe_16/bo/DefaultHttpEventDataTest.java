package com.teragrep.cfe_16.bo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultHttpEventDataTest {

    @Test
    @DisplayName("Happy equals test")
    void happyEqualsTest() {
        final DefaultHttpEventData defaultHttpEventData1 = new DefaultHttpEventData(
            "channel 1",
            "event",
            "auth token",
            123
        );

        final DefaultHttpEventData defaultHttpEventData2 = new DefaultHttpEventData(
            "channel 1",
            "event",
            "auth token",
            123
        );

        Assertions.assertEquals(defaultHttpEventData1, defaultHttpEventData2);
    }

    @Test
    @DisplayName("Unhappy equals test")
    void unhappyEqualsTest() {
        final DefaultHttpEventData defaultHttpEventData1 = new DefaultHttpEventData(
            "channel 1",
            "event",
            "auth token",
            123
        );

        final DefaultHttpEventData defaultHttpEventData2 = new DefaultHttpEventData(
            "channel 123", // Not the same
            "event",
            "auth token",
            123
        );

        Assertions.assertNotEquals(defaultHttpEventData1, defaultHttpEventData2);
    }

    @Test
    @DisplayName("getEvent returns event")
    void getEventReturnsEvent() {
        final DefaultHttpEventData defaultHttpEventData = new DefaultHttpEventData(
            "channel 1",
            "event",
            "auth token",
            123
        );

        final String expectedResult = "event";

        Assertions.assertEquals(expectedResult, defaultHttpEventData.getEvent());
    }

    @Test
    @DisplayName("getChannel returns channel")
    void getChannelReturnsChannel() {
        final DefaultHttpEventData defaultHttpEventData = new DefaultHttpEventData(
            "channel 1",
            "event",
            "auth token",
            123
        );

        final String expectedResult = "channel 1";

        Assertions.assertEquals(expectedResult, defaultHttpEventData.getChannel());
    }

    @Test
    @DisplayName("getAuthenticationToken returns authentication token")
    void getAuthenticationTokenReturnsAuthenticationToken() {
        final DefaultHttpEventData defaultHttpEventData = new DefaultHttpEventData(
            "channel 1",
            "event",
            "auth token",
            123
        );

        final String expectedResult = "auth token";

        Assertions.assertEquals(expectedResult, defaultHttpEventData.getAuthenticationToken());
    }

    @Test
    @DisplayName("getAckID returns ackID if not null")
    void getAckIdReturnsAckIdIfNotNull() {
        final DefaultHttpEventData defaultHttpEventData = new DefaultHttpEventData(
            "channel 1",
            "event",
            "auth token",
            123
        );

        final Integer expectedResult = 123;

        Assertions.assertEquals(expectedResult, defaultHttpEventData.getAckID());
    }

    @Test
    @DisplayName("getAckID returns null if ackID is null")
    void getAckIdReturnsNullIfAckIdIsNull() {
        final DefaultHttpEventData defaultHttpEventData = new DefaultHttpEventData(
            "channel 1",
            "event",
            "auth token",
            null
        );

        final Integer expectedResult = null;

        Assertions.assertEquals(expectedResult, defaultHttpEventData.getAckID());
    }
}