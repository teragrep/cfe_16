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
package com.teragrep.cfe_16;

import static org.junit.jupiter.api.Assertions.*;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

class MultiValueMapRequestTest {

    @Test
    @DisplayName("test MultiValueMapRequest with channel present")
    void testMultiValueMapRequestWithChannelPresent() {
        final MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("channel", "CHANNEL_11111");
        multiValueMap.add("{\"sourcetype\": \"mysourcetype\", \"event\": \"Hello, world!\"}", null);
        final MultiValueMapRequest multiValueMapRequest = new MultiValueMapRequest(multiValueMap);

        final String cleaned = multiValueMapRequest.asCleanedJsonString();
        Assertions
                .assertEquals(
                        "{\"sourcetype\": \"mysourcetype\", \"event\": \"Hello, world!\"}", cleaned,
                        "Did not clean channel properly"
                );
    }

    @Test
    @DisplayName("test MultiValueMapRequest without channel present")
    void testMultiValueMapRequestWithoutChannelPresent() {
        final MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("{\"sourcetype\": \"mysourcetype\", \"event\": \"Hello, world!\"}", null);
        final MultiValueMapRequest multiValueMapRequest = new MultiValueMapRequest(multiValueMap);

        final String cleaned = multiValueMapRequest.asCleanedJsonString();
        Assertions
                .assertEquals(
                        "{\"sourcetype\": \"mysourcetype\", \"event\": \"Hello, world!\"}", cleaned,
                        "Did not clean channel properly"
                );
    }

    @Test
    @DisplayName("asCleanedJsonString with more than 2 keys")
    void asCleanedJsonStringWithMoreThan2Keys() {
        final MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("channel", "CHANNEL_11111");
        multiValueMap.add("somethingElse", "asdfg");
        multiValueMap.add("{\"sourcetype\": \"mysourcetype\", \"event\": \"Hello, world!\"}", null);
        final MultiValueMapRequest multiValueMapRequest = new MultiValueMapRequest(multiValueMap);

        final IllegalStateException illegalStateException = assertThrowsExactly(
                IllegalStateException.class, multiValueMapRequest::asCleanedJsonString
        );

        Assertions
                .assertEquals(
                        "application/x-www-form-urlencoded request contains more parameters than expected",
                        illegalStateException.getMessage()
                );
    }

    @Test
    @DisplayName("test that asCleanedJsonString does not modify multiValueMap field")
    void testThatAsCleanedJsonStringDoesNotModifyMultiValueMapField() {
        final MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("channel", "CHANNEL_11111");
        multiValueMap.add("{\"sourcetype\": \"mysourcetype\", \"event\": \"Hello, world!\"}", null);
        final MultiValueMapRequest multiValueMapRequest = new MultiValueMapRequest(multiValueMap);

        final String cleaned = multiValueMapRequest.asCleanedJsonString();
        Assertions
                .assertEquals(
                        "{\"sourcetype\": \"mysourcetype\", \"event\": \"Hello, world!\"}", cleaned,
                        "Did not clean channel properly"
                );

        final MultiValueMap<String, String> nonModifiedMultiValueMap = new LinkedMultiValueMap<>();
        nonModifiedMultiValueMap.add("channel", "CHANNEL_11111");
        nonModifiedMultiValueMap.add("{\"sourcetype\": \"mysourcetype\", \"event\": \"Hello, world!\"}", null);
        final MultiValueMapRequest nonModifiedMultiValueMapRequest = new MultiValueMapRequest(nonModifiedMultiValueMap);

        Assertions.assertEquals(nonModifiedMultiValueMapRequest, multiValueMapRequest);
    }

    @Test
    @DisplayName("equalsVerifier test")
    void equalsVerifierTest() {
        EqualsVerifier.forClass(MultiValueMapRequest.class).verify();
    }
}
