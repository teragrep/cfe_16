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

import com.teragrep.cfe_16.bo.HECRecord;
import com.teragrep.cfe_16.event.time.DoubleTime;
import com.teragrep.cfe_16.event.time.GeneratedTime;
import com.teragrep.cfe_16.event.time.NumericalTime;
import com.teragrep.cfe_16.event.time.TextualTime;
import com.teragrep.cfe_16.event.time.Time;
import java.util.Objects;

public final class EventTime {

    private final HECRecord previousEvent;
    private final TimeObject timeObject;

    public EventTime(HECRecord previousEvent, TimeObject timeObject) {
        this.previousEvent = previousEvent;
        this.timeObject = timeObject;
    }

    public Time asTime(long defaultValue) {
        // No time provided in the event
        if (timeObject.isStub()) {
            return new GeneratedTime(previousEvent, defaultValue);
        }
        // Check if time is a double
        else if (timeObject.isDouble()) {
            return new DoubleTime(timeObject);
        }
        // Time is a number, no calculations required
        else if (timeObject.canConvertToLong()) {
            return new NumericalTime(timeObject);
        }
        // Time is a String
        else if (timeObject.isTextual()) {
            return new TextualTime(previousEvent, timeObject);
        }
        // Unknown format
        else {
            return new GeneratedTime(previousEvent, defaultValue);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EventTime eventTime = (EventTime) o;
        return previousEvent.equals(eventTime.previousEvent) && timeObject.equals(eventTime.timeObject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(previousEvent, timeObject);

    }
}
