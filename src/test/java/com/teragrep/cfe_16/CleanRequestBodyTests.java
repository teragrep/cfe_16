package com.teragrep.cfe_16;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CleanRequestBodyTests {
    @Test
    public void testCleanRequestBodyNormal() {
        String input = "{channel=[CHANNEL_11111], {\"sourcetype\": \"mysourcetype\", \"event\": \"Hello, world!\"}=[]}";
        String channel = "CHANNEL_11111";
        RequestBodyCleaner requestBodyCleaner = new RequestBodyCleaner();
        String cleaned = requestBodyCleaner.cleanAckRequestBody(input, channel);
        Assertions.assertEquals("{\"sourcetype\": \"mysourcetype\", \"event\": \"Hello, world!\"}", cleaned, "Did not clean channel properly");
    }

    @Test
    public void testCleanRequestBodyNoMatch() {
        String input = "{channel=[CHANNEL_11111], {\"sourcetype\": \"mysourcetype\", \"event\": \"Hello, world!\"}=[]}";
        String channel = "CHANNEL_22222";
        RequestBodyCleaner requestBodyCleaner = new RequestBodyCleaner();
        String cleaned = requestBodyCleaner.cleanAckRequestBody(input, channel);
        Assertions.assertEquals("TODO: Implement proper response", cleaned, "Did not clean channel properly");
    }

    @Test
    public void testCleanRequestBodyEvil() {
        String input = "{channel=[CHANNEL_11111], {\"sourcetype\": \"mysourcetype\", \"event\": \"Hello, world!\"}=[]}";
        String channel = ".*]|[\\";
        RequestBodyCleaner requestBodyCleaner = new RequestBodyCleaner();
        String cleaned = requestBodyCleaner.cleanAckRequestBody(input, channel);
        Assertions.assertEquals("TODO: Implement proper response", cleaned, "Did not clean channel properly");
    }
}
