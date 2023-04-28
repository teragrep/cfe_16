/*
 * HTTP Event Capture to RFC5424 CFE_16
 * Copyright (C) 2021  Suomen Kanuuna Oy
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

import com.teragrep.cfe_16.bo.HeaderInfo;
import com.teragrep.cfe_16.bo.HttpEventData;
import org.springframework.stereotype.Component;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.SDElement;
import com.cloudbees.syslog.Severity;
import com.cloudbees.syslog.SyslogMessage;

/*
 * Converts HTTP Event Data into a Syslog message.
 * 
 * This class is NOT thread safe!
 *
 */
@Component
public class Converter {

    private Severity severity;
    private Facility facility;

    private SDElement metadataSDE;
    private SDElement headerSDE;

    public SyslogMessage httpToSyslog(HttpEventData httpEventData, HeaderInfo headerInfo) {

        setEventSeverity();
        setEventFacility();

        setStructuredDataParams(httpEventData);
        setHeaderSDE(headerInfo);
        
        SyslogMessage syslogMessage;
        if (httpEventData.isTimeParsed()) {

            /*
             * Creates a Syslogmessage with a time stamp
             */
            syslogMessage = new SyslogMessage().withTimestamp(httpEventData.getTimeAsLong()).withSeverity(severity)
                    .withAppName("capsulated").withHostname("cfe-16").withFacility(facility).withSDElement(metadataSDE)
                    .withSDElement(headerSDE).withMsg(httpEventData.getEvent());

        } else {

            /*
             * Creates a Syslogmessage without timestamp, because the time is already given
             * in the request.
             */
            syslogMessage = new SyslogMessage().withSeverity(severity).withAppName("capsulated").withHostname("cfe-16")
                    .withFacility(facility).withSDElement(metadataSDE).withSDElement(headerSDE)
                    .withMsg(httpEventData.getEvent());
        }

        return syslogMessage;
    }

    /*
     * Event severity is coded to always be INFORMATIONAL
     */
    private void setEventSeverity() {
        severity = Severity.INFORMATIONAL;
    }

    /*
     * Event facility is coded to always be USER
     */
    private void setEventFacility() {
        facility = Facility.USER;
    }

    /*
     * Gets the data from the HTTP Event Data and adds it to SD Element as SD
     * Parameters.
     */
    private void setStructuredDataParams(HttpEventData eventData) {
        metadataSDE = new SDElement("cfe_16-metadata@48577");

        if (eventData.getAuthenticationToken() != null) {
            metadataSDE.addSDParam("authentication_token", eventData.getAuthenticationToken());
        }

        if (eventData.getChannel() != null) {
            metadataSDE.addSDParam("channel", eventData.getChannel());
        }

        if (eventData.getAckID() != null) {
            metadataSDE.addSDParam("ack_id", String.valueOf(eventData.getAckID()));
        }

        if (eventData.getTimeSource() != null) {
            metadataSDE.addSDParam("time_source", eventData.getTimeSource());
        }

        if (eventData.isTimeParsed()) {
            metadataSDE.addSDParam("time_parsed", "true");
            metadataSDE.addSDParam("time", eventData.getTime());
        }
    }

    public SyslogMessage getHeaderInfoSyslogMessage(HeaderInfo headerInfo) {

        SyslogMessage syslogMessage = null;
        setHeaderSDE(headerInfo);
        syslogMessage = new SyslogMessage().withSeverity(severity).withAppName("capsulated").withHostname("cfe-16")
                .withFacility(facility).withSDElement(headerSDE);

        return syslogMessage;
    }

    private void setHeaderSDE(HeaderInfo headerInfo) {
        headerSDE = new SDElement("cfe_16-origin@48577");

        if (headerInfo.getxForwardedFor() != null) {
            headerSDE.addSDParam("X-Forwarded-For", headerInfo.getxForwardedFor());
        }
        if (headerInfo.getxForwardedHost() != null) {
            headerSDE.addSDParam("X-Forwarded-Host", headerInfo.getxForwardedHost());
        }
        if (headerInfo.getxForwardedProto() != null) {
            headerSDE.addSDParam("X-Forwarded-Proto", headerInfo.getxForwardedProto());
        }
    }
}
