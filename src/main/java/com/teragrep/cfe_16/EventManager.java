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

import com.cloudbees.syslog.SyslogMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonStreamParser;
import com.teragrep.cfe_16.bo.Ack;
import com.teragrep.cfe_16.bo.HeaderInfo;
import com.teragrep.cfe_16.bo.HttpEventData;
import com.teragrep.cfe_16.bo.Session;
import com.teragrep.cfe_16.bo.TimestampedHttpEventDataStub;
import com.teragrep.cfe_16.config.Configuration;
import com.teragrep.cfe_16.event.EventTime;
import com.teragrep.cfe_16.event.JsonEvent;
import com.teragrep.cfe_16.event.JsonEventImpl;
import com.teragrep.cfe_16.exceptionhandling.InternalServerErrorException;
import com.teragrep.cfe_16.connection.AbstractConnection;
import com.teragrep.cfe_16.connection.ConnectionFactory;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
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

    private AbstractConnection connection;

    public EventManager() {
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void setupSender() {
        LOGGER.debug("Setting up connection");
        try {
            this.connection = ConnectionFactory
                    .createSender(
                            this.configuration.getSysLogProtocol(), this.configuration.getSyslogHost(),
                            this.configuration.getSyslogPort()
                    );
        }
        catch (IOException e) {
            LOGGER.error("Error creating connection", e);
            throw new InternalServerErrorException();
        }
    }

    /*
     * Method used when converting data and the channel is specified in the request.
     * Takes authentication token, all events sent in a request (in JSON format) and
     * the channel name as string parameters. Returns a JSON node with ack id if
     * everything is successful. Example: {"text":"Success","code":0,"ackID":0}
     */
    public ObjectNode convertData(
            String authToken,
            String channel,
            String allEventsInJson,
            HeaderInfo headerInfo,
            Acknowledgements acknowledgements
    ) {
        HttpEventData previousEvent = new TimestampedHttpEventDataStub();

        acknowledgements.initializeContext(authToken, channel);
        int ackId = acknowledgements.getCurrentAckValue(authToken, channel);
        boolean incremented = acknowledgements.incrementAckValue(authToken, channel);
        if (!incremented) {
            throw new InternalServerErrorException("Ack value couldn't be incremented.");
        }
        Ack ack = new Ack(ackId, false);
        boolean addedAck = acknowledgements.addAck(authToken, channel, ack);
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
        HttpEventData eventData;
        Converter converter = new Converter(headerInfo);
        List<SyslogMessage> syslogMessages = new ArrayList<>();
        while (parser.hasNext()) {
            String jsonObjectStr = parser.next().toString();
            try {
                /*
                 * Event field cannot be missing or blank. Throws an exception if this is the
                 * case.
                 */
                final JsonEvent jsonEvent = new JsonEventImpl(new ObjectMapper().readTree(jsonObjectStr));
                /*
                * Construct TimestampedHttpEventData with correct time values, based on the previous event
                *
                * Can throw an EventFieldBlankException
                */
                eventData = new EventTime(
                        channel,
                        jsonEvent.asEvent(),
                        authToken,
                        0,
                        previousEvent,
                        jsonEvent.asTimeObject()
                ).timestampedHttpEventData(Instant.now().toEpochMilli());

                // Set the previous event if the "current" event was parsed without an exception
                previousEvent = eventData;
            }
            catch (JsonProcessingException e) {
                LOGGER.error("Problem processing JsonObjectString <{}>", jsonObjectStr);
                continue;
            }

            syslogMessages
                    .add(
                            converter
                                    .httpToSyslog(
                                            new TimestampedHttpEventData(
                                                    new DefaultHttpEventData(channel, eventData.event(), authToken, null)
                                            ),
                                        headerInfo
                                    )
                    );
        }

        // create a new object to avoid blocking of threads because
        // the SyslogMessageSender.sendMessage() is synchronized
        try {
            this.connection.sendMessages(syslogMessages);
        }
        catch (IOException e) {
            throw new InternalServerErrorException(e);
        }

        boolean shouldAck = channel != null && !channel.equals(Session.DEFAULT_CHANNEL);

        if (shouldAck) {
            boolean acked = acknowledgements.acknowledge(authToken, channel, ackId);
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
}
