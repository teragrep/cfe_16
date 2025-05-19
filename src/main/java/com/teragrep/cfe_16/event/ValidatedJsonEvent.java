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

import com.fasterxml.jackson.databind.JsonNode;
import com.teragrep.cfe_16.exceptionhandling.EventFieldBlankException;
import com.teragrep.cfe_16.exceptionhandling.EventFieldMissingException;
import java.util.Objects;

public class ValidatedJsonEvent implements JsonEvent {

    private final JsonEvent jsonEvent;

    public ValidatedJsonEvent(JsonEvent jsonEvent) {
        this.jsonEvent = jsonEvent;
    }

    @Override
    public JsonNode event() {
        // Event field completely missing
        if (!this.node().has("event")) {
            throw new EventFieldMissingException("event field is missing");
        }
        // Event field contains subfield "message"
        else if (this.node().get("event").isObject() && this.node().get("event").has("message")) {
            if (
                this.node().get("event").get("message").isTextual()
                        && !Objects.equals(this.node().get("event").get("message").asText(), "")
            ) {
                return this.node().get("event").get("message");
            }
        }
        // Event field has a String value
        else if (this.node().get("event").isTextual() && !Objects.equals(this.node().get("event").asText(), "")) {
            return this.jsonEvent.event();
        }
        throw new EventFieldBlankException("jsonEvent node's event not valid");
    }

    @Override
    public JsonNode node() {
        if (this.jsonEvent != null && this.jsonEvent.node() != null && this.jsonEvent.node().isObject()) {
            return this.jsonEvent.node();
        }
        throw new IllegalStateException("jsonEvent node not valid");
    }

    /**
     * Return the time from the {@link #jsonEvent}. If it is null, it is the responsibility of someone else to generate
     * a valid time.
     * 
     * @return time as it is reported in the {@link #jsonEvent}, since it might be null, which is valid
     */
    @Override
    public JsonNode time() {
        return this.jsonEvent.time();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ValidatedJsonEvent that = (ValidatedJsonEvent) o;
        return Objects.equals(jsonEvent, that.jsonEvent);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(jsonEvent);
    }
}
