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

import java.util.Objects;
import java.util.Set;
import org.springframework.util.MultiValueMap;

/**
 * Cleans the request body so, that only the body of the request is left in the string. This is needed when calling the
 * endpoint that consumes MediaType.APPLICATION_FORM_URLENCODED_VALUE <b>The actual body needed is stored as a key, and
 * not a value for a key</b> Example of body sent as a parameter: {channel=[CHANNEL_11111], {"sourcetype":
 * "mysourcetype", "event": "Hello, world!"}=[]} Example of cleaned body returned by the cleanAckRequestBody():
 * {"sourcetype": "mysourcetype", "event": "Hello, world!"}
 */
public final class MultiValueMapRequest {

    private final MultiValueMap<String, String> multiValueMap;

    public MultiValueMapRequest(final MultiValueMap<String, String> multiValueMap) {
        this.multiValueMap = multiValueMap;
    }

    public String asCleanedJsonString() throws IllegalStateException {
        final String valueToReturn;
        // Remove the channel, if it is present
        multiValueMap.remove("channel");
        final Set<String> keys = multiValueMap.keySet();

        // Check if the parameters contains more entries than expected from the request
        if (keys.size() != 1) {
            throw new IllegalStateException(
                    "application/x-www-form-urlencoded request contains more parameters than expected"
            );
        }
        else {
            // Get the value of the first key
            valueToReturn = keys.iterator().next();
        }

        return valueToReturn;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final MultiValueMapRequest that = (MultiValueMapRequest) o;
        return Objects.equals(multiValueMap, that.multiValueMap);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(multiValueMap);
    }
}
