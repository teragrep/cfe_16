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

import com.teragrep.cfe_16.event.time.candidates.HECTimestampCandidate;
import com.teragrep.cfe_16.event.time.candidates.HECTimestampCandidateImpl;
import tools.jackson.databind.JsonNode;
import com.teragrep.cfe_16.event.JsonEvent;
import com.teragrep.cfe_16.exceptionhandling.EventFieldException;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;

public final class HECTimeImpl implements HECTime {

    private static final Logger LOGGER = LoggerFactory.getLogger(HECTimeImpl.class);
    private final JsonEvent jsonEvent;

    public HECTimeImpl(final JsonEvent jsonEvent) {
        this.jsonEvent = jsonEvent;
    }

    @Override
    public long toEpochMilli(final long defaultValue) {
        final String timeString = timeString(timeNode(defaultValue));

        final HECTimestampCandidate candidates = new HECTimestampCandidateImpl();
        final Instantiable instantiable = candidates.toInstantiable(timeString);

        final long returnedTime;
        if (instantiable.isStub()) {
            returnedTime = defaultValue;
        }
        else {
            returnedTime = instantiable.toInstant().toEpochMilli();
        }

        return returnedTime;
    }

    private JsonNode timeNode(final long defaultValue) {
        JsonNode timeNode;
        try {
            timeNode = jsonEvent.asTimeJsonNode();
        }
        catch (final EventFieldException e) {
            LOGGER.info("Could not parse time from JSON, so using defaultValue provided <[{}]>", defaultValue);
            timeNode = new ObjectMapper().createObjectNode();
        }
        return timeNode;
    }

    private String timeString(final JsonNode timeNode) {
        if (timeNode.isDouble()) {
            return timeNode.asDecimal().toString();
        }
        else if (timeNode.isInt()) {
            return timeNode.toString();
        }
        else if (timeNode.isObject() || timeNode.isArray()) {
            return "";
        }
        else if (timeNode.isNull() || timeNode.asString().equals("null")) {
            return "";
        }
        else {
            return timeNode.asString();
        }
    }

    @Override
    public boolean isParsed() {
        return toEpochMilli(0L) != 0L;
    }

    @Override
    public String source() {
        final String returnedSource;

        if (isParsed()) {
            returnedSource = "reported";
        }
        else if (!timeString(timeNode(0L)).isEmpty()) {
            returnedSource = "reported";
        }
        else {
            returnedSource = "generated";
        }

        return returnedSource;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final HECTimeImpl hecTime = (HECTimeImpl) o;
        return Objects.equals(jsonEvent, hecTime.jsonEvent);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(jsonEvent);
    }

    @Override
    public boolean isStub() {
        return false;
    }
}
