package com.zdanovich.telegrambot.logic;

import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAction {

    private final static Logger logger = LoggerFactory.getLogger(AbstractAction.class);

    public abstract void processInput(String message);

    public abstract HttpUriRequest requestToBackend();

    public abstract void processResponseFromBackend(String response);

    public abstract String responseToFrontend();

    protected String validateInput(String message) {
        String responseToFrontend = null;
        if (message.isEmpty()) {
            responseToFrontend = "You forgot to write a message :)";
            logger.debug("Message is empty: '{}'", message);
        } else if (message.equals("/start") || message.equals("/exit")) {
            responseToFrontend = "OK, let's start over.";
            logger.debug("User wrote: {}", message);
        }
        return responseToFrontend;
    }
}
