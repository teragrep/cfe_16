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

import com.cloudbees.syslog.SyslogMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonStreamParser;
import com.teragrep.cfe_16.bo.Ack;
import com.teragrep.cfe_16.bo.HeaderInfo;
import com.teragrep.cfe_16.bo.HttpEventData;
import com.teragrep.cfe_16.bo.Session;
import com.teragrep.cfe_16.config.Configuration;
import com.teragrep.cfe_16.exceptionhandling.EventFieldBlankException;
import com.teragrep.cfe_16.exceptionhandling.EventFieldMissingException;
import com.teragrep.cfe_16.exceptionhandling.InternalServerErrorException;
import com.teragrep.cfe_16.sender.AbstractSender;
import com.teragrep.cfe_16.sender.SenderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/*
 * Manager that handles the event sent in a request.
 *
 */
@Component
public class EventManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventManager.class);
    private final ObjectMapper objectMapper;

    @Autowired
    private Configuration configuration;
    
    private AbstractSender sender;
    
    public EventManager() {
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void setupSender() {
        LOGGER.debug("Setting up sender");
        try {
            this.sender = SenderFactory.createSender(this.configuration.getSysLogProtocol(),
                                                     this.configuration.getSyslogHost(),
                                                     this.configuration.getSyslogPort());
        } catch (IOException e) {
            LOGGER.error("Error creating sender", e);
            throw new InternalServerErrorException();
        }        
    }
    
    /*
     * Method used when converting data and the channel is specified in the request.
     * Takes authentication token, all events sent in a request (in JSON format) and
     * the channel name as string parameters. Returns a JSON node with ack id if
     * everything is successful. Example: {"text":"Success","code":0,"ackID":0}
     */
    public ObjectNode convertData(String authToken, 
                                  String channel, 
                                  String allEventsInJson, 
                                  HeaderInfo headerInfo,
                                  AckManager ackManager) {
        HttpEventData previousEvent = null;
        
        ackManager.initializeContext(authToken, channel);
        int ackId = ackManager.getCurrentAckValue(authToken, channel);
        boolean incremented = ackManager.incrementAckValue(authToken, channel);
        if (!incremented) {
            throw new InternalServerErrorException("Ack value couldn't be incremented.");
        }
        Ack ack = new Ack(ackId, false);
        boolean addedAck = ackManager.addAck(authToken, channel, ack);
        if (!addedAck) {
            throw new InternalServerErrorException("Ack ID " + ackId + " couldn't be added to the Ack set.");
        }
        JsonStreamParser parser = new JsonStreamParser(allEventsInJson);

        /*
         * There can be multiple events in one request. Here they are handled one by
         * one. The event is saved in a string variable and is converted into
         * HttpEventData object. Metadata is assigned to the object. HttpEventData is
         * converted into SyslogMessage and saved in a list in a RequestInfo object.
         * After the event is handled, it is assigned as a value to previousEvent
         * variable.
         */
        HttpEventData eventData = null;
        Converter converter = new Converter();
        List<SyslogMessage> syslogMessages = new ArrayList<SyslogMessage>();
        while (parser.hasNext()) {
            previousEvent = eventData;
            String jsonObjectStr = parser.next().toString();
            eventData = verifyJsonData(jsonObjectStr, previousEvent);
            eventData = assignMetaData(eventData, authToken, channel);
            SyslogMessage syslogMessage = converter.httpToSyslog(eventData, headerInfo);
            syslogMessages.add(syslogMessage);
        }

        /*
         * SyslogMessage syslogMessage =
         * converter.getHeaderInfoSyslogMessage(headerInfo);
         * requestInfo.getConvertedData().add(syslogMessage);
         */
        /*
         * After all the events are sent, previousEvent object is set to null, the
         * events are sent with the ackManager and ack id and JSON node with an ack id
         * will be returned informing that the sending of the events has been
         * successful.
         */
        previousEvent = null;

        // create a new object to avoid blocking of threads because
        // the SyslogMessageSender.sendMessage() is synchronized
        try {
            SyslogMessage[] messages = syslogMessages.toArray(new SyslogMessage[syslogMessages.size()]);
            this.sender.sendMessages(messages);
        } catch (IOException e) {
            throw new InternalServerErrorException(e);
        }

        boolean shouldAck = channel != null && !channel.equals(Session.DEFAULT_CHANNEL);
        
        if (shouldAck) {
            boolean acked = ackManager.acknowledge(authToken, channel, ackId);
            if (!acked) {
                throw new InternalServerErrorException("Ack ID " + ackId + " not Acked.");
            }
        }

        ObjectNode responseNode = this.objectMapper.createObjectNode();

        responseNode.put("text", "Success");
        responseNode.put("code", 0);
        if (shouldAck) {
            responseNode.put("ackID", ackId);
        }

        return responseNode;
    }

    /*
     * Pre-handles the event and assigns it's information into HttpEventData object
     * and returns it. Information from the string is read with ObjectMapper into a
     * JsonNode. After that the supposed fields are checked from the JsonNode and if
     * the field is found, information from it will be saved in HttpEventData
     * object. Finally handleTime() is called to assign correct time information to
     * the object. When multiple events are sent in one request, the value of the
     * fields are saved for the following events. The values can be overriden. If
     * the value is overridden, it will stay as so for the following events if it is
     * not overridden.
     */
    private HttpEventData verifyJsonData(String eventInJson, HttpEventData previousEvent) {

        HttpEventData eventData = new HttpEventData();

        /*
         * Event field cannot be missing or blank. Throws an exception if this is the
         * case.
         */
        JsonNode jsonObject;
        try {
            jsonObject = this.objectMapper.readTree(eventInJson);
        } catch (IOException e) {
            jsonObject = null;
        }
        
        if (jsonObject != null) {
            JsonNode event = jsonObject.get("event");
            if (event != null) {
                eventData.setEvent(event.toString());
            } else {
                throw new EventFieldMissingException();
            }
            if (eventData.getEvent().matches("\"\"") || eventData.getEvent() == null) {
                throw new EventFieldBlankException();
            }            
            eventData = handleTime(eventData, jsonObject, previousEvent);
        }
        return eventData;
    }

    /*
     * The time stamp of the event can be given as epoch time in the request.
     */
    private HttpEventData handleTime(HttpEventData eventData, JsonNode jsonObject, HttpEventData previousEvent) {
        JsonNode timeObject = jsonObject.get("time");

        /*
         * If the time is given as a string rather than as a numeral value, the time is
         * handled in a same way as it is handled when time is not given in a request.
         */
        if (timeObject == null || timeObject.isTextual()) {
            eventData.setTimeParsed(false);
            eventData.setTimeSource("generated");
            if (previousEvent != null) {
                if (previousEvent.isTimeParsed()) {
                    eventData.setTimeAsLong(previousEvent.getTimeAsLong());
                    eventData.setTime(previousEvent.getTime());
                    eventData.setTimeParsed(true);
                    eventData.setTimeSource("reported");
                }
            }
            /*
             * If the time is given as epoch seconds with a decimal (example:
             * 1433188255.253), the decimal point must be removed and time is assigned to
             * HttpEventData object as a long value. convertTimeToEpochMillis() will check
             * that correct time format is used.
             */
        } else if (timeObject.isDouble()) {
            eventData.setTimeAsLong(removeDecimal(timeObject.asDouble()));
            eventData.setTime(String.valueOf(eventData.getTimeAsLong()));
            eventData.setTimeParsed(true);
            eventData.setTimeSource("reported");
            eventData = convertTimeToEpochMillis(eventData);
            /*
             * If the time is given in a numeral value, it is assigned to HttpEventData
             * object as a long value. convertTimeToEpochMillis() will check that correct
             * time format is used.
             */
        } else if (timeObject.canConvertToLong()) {
            eventData.setTimeAsLong(timeObject.asLong());
            eventData.setTime(jsonObject.get("time").asText());
            eventData.setTimeParsed(true);
            eventData.setTimeSource("reported");
            eventData = convertTimeToEpochMillis(eventData);
        } else {
            eventData.setTimeParsed(false);
            eventData.setTimeSource("generated");
        }

        return eventData;
    }

    /*
     * Takes a double value as a parameter, removes the decimal point from that
     * value and returns the number as a long value.
     */
    private long removeDecimal(double doubleValue) {
        BigDecimal doubleValueWithDecimal = BigDecimal.valueOf(doubleValue);
        String stringValue = doubleValueWithDecimal.toString();
        String stringValueWithoutDecimal = stringValue.replace(".", "");
        long longValue = Long.parseLong(stringValueWithoutDecimal);

        return longValue;
    }

    /*
     * Converts the given time stamp into epoch milliseconds. Takes a HttpEventData
     * object as a parameter. Gets the time from the variable set in the
     * HttpEventData object. If the time value in the object has 13 digits, it means
     * that time has been already given in epoch milliseconds.
     */
    private HttpEventData convertTimeToEpochMillis(HttpEventData eventData) {
        String timeString = eventData.getTime();
        if (timeString.length() == 13) {
            return eventData;
        } else if (timeString.length() >= 10 && timeString.length() < 13) {
            eventData.setTimeAsLong(eventData.getTimeAsLong() * (long) Math.pow(10, ((13 - timeString.length()))));
        } else {
            eventData.setTimeParsed(false);
            eventData.setTimeSource("generated");
        }
        return eventData;
    }

    /*
     * Assigns the metadata (authentication token and channel name) to the
     * HttpEventData object.
     */
    private HttpEventData assignMetaData(HttpEventData eventData, String authToken, String channel) {

        eventData.setAuthenticationToken(authToken);
        eventData.setChannel(channel);

        return eventData;
    }
}
