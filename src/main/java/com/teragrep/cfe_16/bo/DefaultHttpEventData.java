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

public final class DefaultHttpEventData implements HttpEventData {

    private final String channel;
    private final String event;
    private final String authenticationToken;
    private final Integer ackID;

    public DefaultHttpEventData() {
        this(
            "",
            "",
            "",
            null
        );
    }

    public DefaultHttpEventData(String event) {
        this(
            "",
            event,
            "",
            null
        );
    }

    public DefaultHttpEventData(
        String channel,
        String event,
        String authenticationToken,
        Integer ackID
    ) {
        this.channel = channel;
        this.event = event;
        this.authenticationToken = authenticationToken;
        this.ackID = ackID;
    }

    public String getEvent() {
        return event;
    }

    public String getChannel() {
        return channel;
    }

    public String getAuthenticationToken() {
        return authenticationToken;
    }

    public Integer getAckID() {
        return ackID;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {return false;}

        DefaultHttpEventData that = (DefaultHttpEventData) o;
        return Objects.equals(getChannel(), that.getChannel()) && Objects.equals(
            getEvent(), that.getEvent()) && Objects.equals(getAuthenticationToken(),
            that.getAuthenticationToken()) && Objects.equals(getAckID(), that.getAckID());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getChannel());
        result = 31 * result + Objects.hashCode(getEvent());
        result = 31 * result + Objects.hashCode(getAuthenticationToken());
        result = 31 * result + Objects.hashCode(getAckID());
        return result;
    }

    @Override
    public String toString() {
        return "DefaultHttpEventData{" +
            "channel='" + channel + '\'' +
            ", event='" + event + '\'' +
            ", authenticationToken='" + authenticationToken + '\'' +
            '}';
    }
}
