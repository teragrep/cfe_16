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
package com.teragrep.cfe_16.event.time;

import com.teragrep.cfe_16.bo.HttpEventData;
import com.teragrep.cfe_16.event.TimeObject;
import java.util.Objects;

public final class TextualTime implements Time {

    private final HttpEventData previousEvent;
    private final TimeObject timeObject;

    public TextualTime(HttpEventData previousEvent, TimeObject timeObject) {
        this.previousEvent = previousEvent;
        this.timeObject = timeObject;
    }

    @Override
    public long asLong() {
        // Try to convert the String to a long (if not convertable, default to 0L)
        final long asLong = timeObject.asLong(0L);
        if (asLong != 0L) {
            return asLong;
        }
        else if (!previousEvent.isStub()) {
            return previousEvent.time().asLong();
        }
        return 0;
    }

    @Override
    public String asString() {
        final long asLong = timeObject.asLong(0L);

        if (asLong != 0L) {
            return String.valueOf(asLong);
        }
        else {
            return previousEvent.time().asString();
        }
    }

    @Override
    public boolean parsed() {
        final long asLong = timeObject.asLong(0L);

        if (asLong != 0L) {
            return false;
        }
        else
            return !previousEvent.isStub() && previousEvent.time().parsed();
    }

    @Override
    public String source() {
        final long asLong = timeObject.asLong(0L);

        if (asLong != 0L) {
            return "generated";
        }
        else if (!previousEvent.isStub() && previousEvent.time().parsed()) {
            return "reported";
        }
        return "generated";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TextualTime that = (TextualTime) o;
        return Objects.equals(timeObject, that.timeObject) && Objects.equals(previousEvent, that.previousEvent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(previousEvent, timeObject);
    }
}
