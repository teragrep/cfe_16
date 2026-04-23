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
package com.teragrep.cfe_16.service;

import com.teragrep.cfe_16.connection.RelpConnection;
import com.teragrep.cfe_16.response.AcknowledgementResponse;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.JsonNode;
import com.teragrep.cfe_16.*;
import com.teragrep.cfe_16.bo.Ack;
import com.teragrep.cfe_16.bo.HeaderInfo;
import com.teragrep.cfe_16.bo.Session;
import com.teragrep.cfe_16.bo.XForwardedForStub;
import com.teragrep.cfe_16.bo.XForwardedHostStub;
import com.teragrep.cfe_16.bo.XForwardedProtoStub;
import com.teragrep.cfe_16.exceptionhandling.AuthenticationTokenMissingException;
import com.teragrep.cfe_16.exceptionhandling.ChannelNotFoundException;
import com.teragrep.cfe_16.exceptionhandling.ChannelNotProvidedException;
import com.teragrep.cfe_16.exceptionhandling.InternalServerErrorException;
import com.teragrep.cfe_16.exceptionhandling.SessionNotFoundException;
import com.teragrep.cfe_16.response.AcknowledgedJsonResponse;
import com.teragrep.cfe_16.response.ExceptionEvent;
import com.teragrep.cfe_16.response.ExceptionEventContext;
import com.teragrep.cfe_16.response.ExceptionJsonResponse;
import com.teragrep.cfe_16.response.JsonResponse;
import com.teragrep.cfe_16.response.Response;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Implementation of the REST Service back end.
 */
@Service
public final class HECServiceImpl implements HECService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HECServiceImpl.class);
    private final Acknowledgements acknowledgements;
    private final SessionManager sessionManager;
    private final TokenManager tokenManager;
    private final RelpConnection relpConnection;

    private final XForwardedForStub xForwardedForStub;
    private final XForwardedHostStub xForwardedHostStub;
    private final XForwardedProtoStub xForwardedProtoStub;

    @Autowired
    public HECServiceImpl(
            final Acknowledgements acknowledgements,
            final SessionManager sessionManager,
            final TokenManager tokenManager,
            final RelpConnection relpConnection
    ) {
        this(
                acknowledgements,
                sessionManager,
                tokenManager,
                relpConnection,
                new XForwardedForStub(),
                new XForwardedHostStub(),
                new XForwardedProtoStub()
        );
    }

    private HECServiceImpl(
            final Acknowledgements acknowledgements,
            final SessionManager sessionManager,
            final TokenManager tokenManager,
            final RelpConnection relpConnection,
            final XForwardedForStub xForwardedForStub,
            final XForwardedHostStub xForwardedHostStub,
            final XForwardedProtoStub xForwardedProtoStub
    ) {
        this.acknowledgements = acknowledgements;
        this.sessionManager = sessionManager;
        this.tokenManager = tokenManager;
        this.relpConnection = relpConnection;
        this.xForwardedForStub = xForwardedForStub;
        this.xForwardedHostStub = xForwardedHostStub;
        this.xForwardedProtoStub = xForwardedProtoStub;
    }

    @Override
    public Response sendEvents(HttpServletRequest request, String channel, String eventInJson) {
        LOGGER.debug("Sending events to channel <{}>", channel);
        if (this.tokenManager.tokenIsMissing(request)) {
            throw new AuthenticationTokenMissingException("Authentication token must be provided");
        }

        String authHeader = request.getHeader("Authorization");
        LOGGER.debug("Creating new Header Info");

        String authToken;
        if (tokenManager.isTokenInBasic(authHeader)) {
            LOGGER.debug("Token was provided via Basic");
            authToken = this.tokenManager.getTokenFromBasic(authHeader);
        }
        else {
            LOGGER.debug("Token was provided via header");
            authToken = authHeader;
        }

        // if there is no channel, we'll use the default channel
        if (channel == null) {
            channel = Session.DEFAULT_CHANNEL;
            LOGGER.debug("Channel was not provided, using <{}>", channel);
        }

        Session session = this.sessionManager.getOrCreateSession(authToken);

        // if the channel is not in the session, let's add the channel into it
        if (!session.doesChannelExist(channel)) {
            LOGGER.debug("Adding channel <{}>", channel);
            session.addChannel(channel);
        }

        acknowledgements.initializeContext(authToken, channel);
        final int ackId = acknowledgements.getCurrentAckValue(authToken, channel);
        final boolean incremented = acknowledgements.incrementAckValue(authToken, channel);
        if (!incremented) {
            throw new InternalServerErrorException("Ack value couldn't be incremented.");
        }
        final Ack ack = new Ack(ackId, false);
        final boolean addedAck = acknowledgements.addAck(authToken, channel, ack);
        if (!addedAck) {
            throw new InternalServerErrorException("Ack ID " + ackId + " couldn't be added to the Ack set.");
        }

        final HeaderInfo headerInfo = new HeaderInfo(request);

        Response responseToReturn;

        try {
            this.relpConnection
                    .sendMessages(new SyslogBatch(new HECBatch(authToken, channel, eventInJson, headerInfo).toHECRecordList()).asSyslogMessages());

            final boolean shouldAck = !channel.equals(Session.DEFAULT_CHANNEL);

            if (shouldAck) {
                final boolean acked = acknowledgements.acknowledge(authToken, channel, ackId);
                if (!acked) {
                    throw new InternalServerErrorException("Ack ID " + ackId + " not Acked.");
                }
                else {
                    responseToReturn = new AcknowledgedJsonResponse("Success", ackId);
                }
            }
            else {
                responseToReturn = new JsonResponse("Success");
            }
        }
        catch (final StreamReadException | IOException e) {
            final ExceptionEventContext exceptionEventContext = new ExceptionEventContext(
                    headerInfo,
                    request.getHeader("user-agent"),
                    request.getRequestURI(),
                    request.getRemoteHost()
            );
            final ExceptionEvent event = new ExceptionEvent(exceptionEventContext, UUID.randomUUID(), e);
            event.logException();
            responseToReturn = new ExceptionJsonResponse(event);
        }

        return responseToReturn;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Response getAcks(
            final HttpServletRequest request,
            final String channel,
            final JsonNode requestedAcksInJson
    ) {

        // filter out error cases
        // authentication header is required always
        if (this.tokenManager.tokenIsMissing(request)) {
            throw new AuthenticationTokenMissingException("Authentication token must be provided");
        }

        String authHeader = request.getHeader("Authorization");

        String authToken;
        if (tokenManager.isTokenInBasic(authHeader)) {
            LOGGER.debug("Token was provided via Basic");
            authToken = this.tokenManager.getTokenFromBasic(authHeader);
        }
        else {
            LOGGER.debug("Token was provided via header");
            authToken = authHeader;
        }

        // channel is required
        if (channel == null) {
            throw new ChannelNotProvidedException("Channel must be provided when requesting ack statuses");
        }

        // session is also required
        Session session = this.sessionManager.getSession(authToken);
        if (session == null) {
            throw new SessionNotFoundException("Session not found for auth token " + authToken);
        }

        // if channel is not inside Session, it is considered an error case
        if (!session.doesChannelExist(channel)) {
            throw new ChannelNotFoundException();
        }
        session.touch();

        final JsonNode requestedAckStatuses = this.acknowledgements
                .getRequestedAckStatuses(authToken, channel, requestedAcksInJson);
        return new AcknowledgementResponse(requestedAckStatuses);
    }

    @Override
    public ResponseEntity<String> healthCheck(HttpServletRequest request) {
        if (this.tokenManager.tokenIsMissing(request)) {
            return new ResponseEntity<String>("Invalid HEC token", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<String>("HEC is available and accepting input", HttpStatus.OK);
    }
}
