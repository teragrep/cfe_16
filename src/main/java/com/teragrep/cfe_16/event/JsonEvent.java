package com.teragrep.cfe_16.event;

import com.fasterxml.jackson.databind.JsonNode;

public interface JsonEvent {
    JsonNode event();

    JsonNode node();

    JsonNode time();
}
