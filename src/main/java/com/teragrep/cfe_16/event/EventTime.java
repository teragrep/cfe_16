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
import com.teragrep.cfe_16.bo.HttpEventData;
import com.teragrep.cfe_16.bo.TimestampedHttpEventData;
import java.math.BigDecimal;
import java.util.Objects;

public final class EventTime {

    private final HttpEventData eventData;
    private final TimestampedHttpEventData previousEvent;
    private final JsonNode timeObject;

    public EventTime(HttpEventData eventData, TimestampedHttpEventData previousEvent, JsonNode timeObject) {
        this.eventData = eventData;
        this.previousEvent = previousEvent;
        this.timeObject = timeObject;
    }

    public TimestampedHttpEventData timestampedHttpEventData(long defaultValue) {
        final String timeSource;
        final String time;
        final long timeAsLong;
        final boolean timeParsed;

        // No time provided in the event
        if (timeObject == null) {
            // Previous event does not have a proper time
            if (previousEvent == null || previousEvent.isDefault()) {
                // Use default value
                time = String.valueOf(defaultValue);
                timeAsLong = defaultValue;
            }
            else {
                time = previousEvent.time();
                timeAsLong = previousEvent.timeAsLong();
            }
            timeParsed = false;
            timeSource = "generated";
        }
        // Check if time is a double and convert to long
        else if (timeObject.isDouble()) {
            final long decimalsRemoved = this.removeDecimal(timeObject.doubleValue());
            time = String.valueOf(decimalsRemoved);
            timeAsLong = decimalsRemoved;
            timeParsed = true;
            timeSource = "reported";
        }
        // Time is a number, no calculations required
        else if (timeObject.canConvertToLong()) {
            time = timeObject.asText();
            timeAsLong = timeObject.asLong();
            timeParsed = true;
            timeSource = "reported";
        }
        // Time is a String
        else if (timeObject.isTextual()) {
            // Try to convert the String to a long (if not convertable, default to 0L)
            final long tryAsLong = timeObject.asLong(0L);
            if (tryAsLong != 0L) {
                time = String.valueOf(tryAsLong);
                timeAsLong = tryAsLong;
                timeParsed = false;
                timeSource = "generated";
            }
            // Previous event contains a time
            else if (previousEvent != null && !previousEvent.isDefault()) {
                if (previousEvent.timeParsed()) {
                    time = previousEvent.time();
                    timeAsLong = previousEvent.timeAsLong();
                    timeParsed = true;
                    timeSource = "reported";
                }
                else {
                    time = previousEvent.time();
                    timeAsLong = previousEvent.timeAsLong();
                    timeParsed = false;
                    timeSource = "generated";
                }
            }
            // No time found in current or previous event
            else {
                // Use default value
                time = String.valueOf(defaultValue);
                timeAsLong = defaultValue;
                timeParsed = false;
                timeSource = "generated";
            }
        }
        // Unknown format
        else {
            // Use default value
            time = String.valueOf(defaultValue);
            timeAsLong = defaultValue;
            timeParsed = false;
            timeSource = "generated";
        }

        return new TimestampedHttpEventData(this.eventData, timeSource, time, timeAsLong, timeParsed);
    }

    /**
     * Takes a double value as a parameter, removes the decimal point from that value and returns the number as a long
     * value.
     */
    private long removeDecimal(double doubleValue) {
        BigDecimal doubleValueWithDecimal = BigDecimal.valueOf(doubleValue);
        String stringValue = doubleValueWithDecimal.toString();
        String stringValueWithoutDecimal = stringValue.replace(".", "");

        return Long.parseLong(stringValueWithoutDecimal);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EventTime eventTime = (EventTime) o;
        return Objects.equals(eventData, eventTime.eventData) && Objects.equals(previousEvent, eventTime.previousEvent)
                && Objects.equals(timeObject, eventTime.timeObject);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(eventData);
        result = 31 * result + Objects.hashCode(previousEvent);
        result = 31 * result + Objects.hashCode(timeObject);
        return result;
    }
}
