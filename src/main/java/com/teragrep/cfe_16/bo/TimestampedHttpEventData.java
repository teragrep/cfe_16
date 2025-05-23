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

import java.util.Objects;

public final class TimestampedHttpEventData implements HttpEventData {

    private final HttpEventData eventData;
    private final String timeSource;
    private final String time;
    private final long timeAsLong;
    private final boolean timeParsed;
    private final boolean isDefault;

    public TimestampedHttpEventData(
            HttpEventData eventData,
            String timeSource,
            String time,
            long timeAsLong,
            boolean timeParsed,
            boolean isDefault
    ) {
        this.eventData = eventData;
        this.timeSource = timeSource;
        this.time = time;
        this.timeAsLong = timeAsLong;
        this.timeParsed = timeParsed;
        this.isDefault = isDefault;
    }

    public TimestampedHttpEventData(
            HttpEventData eventData,
            String timeSource,
            String time,
            long timeAsLong,
            boolean timeParsed
    ) {
        this(eventData, timeSource, time, timeAsLong, timeParsed, false);
    }

    public TimestampedHttpEventData(final DefaultHttpEventData defaultHttpEventData) {
        this(defaultHttpEventData, false);
    }

    public TimestampedHttpEventData(final DefaultHttpEventData defaultHttpEventData, boolean isDefault) {
        this(defaultHttpEventData, "timeSource", "time", 0L, false, isDefault);
    }

    public TimestampedHttpEventData() {
        this(new DefaultHttpEventData(), true);
    }

    public HttpEventData eventData() {
        return eventData;
    }

    public String event() {
        return this.eventData.event();
    }

    public String channel() {
        return this.eventData.channel();
    }

    public String authenticationToken() {
        return this.eventData.authenticationToken();
    }

    public String timeSource() {
        if (this.time == null) {
            return "generated";
        }
        return this.timeSource;
    }

    public String time() {
        return this.time;
    }

    public long timeAsLong() {
        return this.timeAsLong;
    }

    @Override
    public Integer ackID() {
        return this.eventData.ackID();
    }

    public boolean timeParsed() {
        if (this.time == null) {
            return false;
        }
        if (this.time.length() < 10 || this.time.length() > 13) {
            return false;
        }
        return this.timeParsed;
    }

    public boolean isDefault() {
        return this.isDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TimestampedHttpEventData that = (TimestampedHttpEventData) o;
        return timeAsLong() == that.timeAsLong() && timeParsed() == that.timeParsed() && Objects
                .equals(eventData, that.eventData) && Objects.equals(timeSource(), that.timeSource())
                && Objects.equals(time(), that.time());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(eventData);
        result = 31 * result + Objects.hashCode(timeSource());
        result = 31 * result + Objects.hashCode(time());
        result = 31 * result + Long.hashCode(timeAsLong());
        result = 31 * result + Boolean.hashCode(timeParsed());
        return result;
    }
}
