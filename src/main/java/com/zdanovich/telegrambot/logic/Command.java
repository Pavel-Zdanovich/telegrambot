package com.zdanovich.telegrambot.logic;

import com.zdanovich.telegrambot.bot.ZdanovichTravelBot;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Command extends AbstractAction {

    private final static Logger logger = LoggerFactory.getLogger(AbstractAction.class);

    private String responseToFrontend;

    @Override
    public void processInput(String message) {
        if ((responseToFrontend = validateInput(message)) != null) {
            Flow.start();
            return;
        }

        if (!message.matches("^\\/[a-z]+$")) {

            responseToFrontend = String.format("I don't understand such command: %s", message);
            logger.debug("Invalid command: {}", message);
            return;

        }

        if (Flow.COMMAND_TO_METHOD.containsKey(message) && Flow.COMMAND_TO_RECEIVE_DATA.containsKey(message)) {

            String method = Flow.COMMAND_TO_METHOD.get(message);
            Map<String, String> receiveData = Flow.COMMAND_TO_RECEIVE_DATA.get(message);

            ReceiveUserData receiveUserData = new ReceiveUserData(receiveData, method,
                    RequestBuilder.create(method).setUri(ZdanovichTravelBot.BACKEND_URI));

            Flow.setNextAction(receiveUserData);

            responseToFrontend = String.format("Please, enter city %s", receiveUserData.getCurrentReceiveDataName());

        } else {

            responseToFrontend = String.format("I haven't learned a command: %s\nI know only: %s",
                    message, String.join(", ", Flow.COMMAND_TO_METHOD.keySet()));
            logger.debug("Unknown command: {}", message);

        }
    }

    @Override
    public HttpUriRequest requestToBackend() {
        return null;
    }

    @Override
    public void processResponseFromBackend(String response) {
        return;
    }

    @Override
    public String responseToFrontend() {
        return responseToFrontend;
    }
}
