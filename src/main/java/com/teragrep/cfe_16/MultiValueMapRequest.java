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

public final class MultiValueMapRequest {

    private final MultiValueMap<String, String> multiValueMap;

    public MultiValueMapRequest(final MultiValueMap<String, String> multiValueMap) {
        this.multiValueMap = multiValueMap;
    }

    public String asCleanedJsonString() {
        final String valueToReturn;
        // Remove the channel, if it is present
        multiValueMap.remove("channel");
        final Set<String> keys = multiValueMap.keySet();

        if (keys.size() == 1) {
            valueToReturn = keys.iterator().next();
        }
        else {
            final String multiValueMapString = multiValueMap.toString();
            valueToReturn = this.removeFirstAndLastCharacters(multiValueMapString);
        }

        return valueToReturn;
    }

    private String removeFirstAndLastCharacters(final String string) {
        return string.substring(1, string.length() - 1);
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
