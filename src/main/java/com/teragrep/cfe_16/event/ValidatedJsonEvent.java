package com.teragrep.cfe_16.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.teragrep.cfe_16.exceptionhandling.EventFieldBlankException;
import com.teragrep.cfe_16.exceptionhandling.EventFieldMissingException;
import java.util.Objects;

public class ValidatedJsonEvent implements JsonEvent {

    private final JsonEvent jsonEvent;

    public ValidatedJsonEvent(JsonEvent jsonEvent) {this.jsonEvent = jsonEvent;}

    @Override
    public JsonNode event() {
        // Event field completely missing
        if(!this.node().has("event")) {
            throw new EventFieldMissingException("event field is missing");
        }
        // Event field contains subfield "message"
        else if (this.node().get("event").isObject() && this.node().get("event").has("message")) {
            if (this.node().get("event").get("message").isTextual() && !Objects.equals(this.node().get("event").get("message").asText(), "")) {
                return this.node().get("event").get("message");
            }
        }
        // Event field has a String value
        else if (this.node().get("event").isTextual() && !Objects.equals(this.node().get("event").asText(), "")) {
            return this.jsonEvent.event();
        }
        throw new EventFieldBlankException("jsonEvent node's event not valid");
    }

    @Override
    public JsonNode node() {
        if (this.jsonEvent != null && this.jsonEvent.node() != null && this.jsonEvent.node().isObject()) {
            return this.jsonEvent.node();
        }
        throw new IllegalStateException("jsonEvent node not valid");
    }

    /** Return the time from the {@link #jsonEvent}. If it is null, it is the responsibility of someone else to generate a valid time.
     * @return time as it is reported in the {@link #jsonEvent}, since it might be null, which is valid
     */
    @Override
    public JsonNode time() {
        return this.jsonEvent.time();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {return false;}

        ValidatedJsonEvent that = (ValidatedJsonEvent) o;
        return Objects.equals(jsonEvent, that.jsonEvent);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(jsonEvent);
    }
}
