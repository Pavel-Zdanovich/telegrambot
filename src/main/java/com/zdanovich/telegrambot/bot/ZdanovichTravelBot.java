package com.zdanovich.telegrambot.bot;

import com.zdanovich.telegrambot.logic.AbstractAction;
import com.zdanovich.telegrambot.logic.Flow;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.ChosenInlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery;
import org.telegram.telegrambots.meta.api.objects.payments.ShippingQuery;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ZdanovichTravelBot extends TelegramLongPollingBot {

    private final static Logger logger = LoggerFactory.getLogger(ZdanovichTravelBot.class);

    public static final String BACKEND_URI = "http://localhost:8080/";

    private Long chatId;

    private Message message;
    private Message editedMessage;
    private Message channelPost;
    private Message editedChannelPost;
    private InlineQuery inlineQuery;
    private ChosenInlineQuery chosenInlineQuery;
    private CallbackQuery callbackQuery;
    private PreCheckoutQuery preCheckoutQuery;
    private ShippingQuery shippingQuery;
    private Poll poll;
    private PollAnswer pollAnswer;

    @Override
    public void onUpdateReceived(Update update) {
        parseUpdate(update);
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {

                String text = message.getText().trim();

                AbstractAction action = Flow.getCurrentAction();

                action.processInput(text);

                HttpUriRequest requestToBackend;
                if ((requestToBackend = action.requestToBackend()) != null) {
                    String responseFromBackend = request(requestToBackend);
                    action.processResponseFromBackend(responseFromBackend);
                }

                String responseToFrontend;
                if ((responseToFrontend = action.responseToFrontend()) != null) {
                    response(responseToFrontend);
                }

            } else {
                response("You're forgot to write a message :)");
            }
        } else {
            response("I can only read and write :)");
        }
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        updates.forEach(this::onUpdateReceived);
    }

    private void parseUpdate(Update update) {
        if (update.hasMessage()) {
            message = update.getMessage();
            chatId = message.getChatId();
        }
        if (update.hasEditedMessage()) {
            editedMessage = update.getEditedMessage();
            chatId = editedMessage.getChatId();
        }
        if (update.hasChannelPost()) {
            channelPost = update.getChannelPost();
            chatId = channelPost.getChatId();
        }
        if (update.hasEditedChannelPost()) {
            editedChannelPost = update.getEditedChannelPost();
            chatId = editedChannelPost.getChatId();
        }
        if (update.hasInlineQuery()) {
            inlineQuery = update.getInlineQuery();
        }
        if (update.hasChosenInlineQuery()) {
            chosenInlineQuery = update.getChosenInlineQuery();
        }
        if (update.hasCallbackQuery()) {
            callbackQuery = update.getCallbackQuery();
        }
        if (update.hasPreCheckoutQuery()) {
            preCheckoutQuery = update.getPreCheckoutQuery();
        }
        if (update.hasShippingQuery()) {
            shippingQuery = update.getShippingQuery();
        }
        if (update.hasPoll()) {
            poll = update.getPoll();
        }
        if (update.hasPollAnswer()) {
            pollAnswer = update.getPollAnswer();
        }
    }

    private String request(HttpUriRequest httpUriRequest) {
        String response = null;
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse closeableHttpResponse = httpClient.execute(httpUriRequest);
             InputStream inputStream = closeableHttpResponse.getEntity().getContent()) {

            response = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            response = new JSONObject(response.isEmpty() ? "{}" : response)
                    .put("code", closeableHttpResponse.getStatusLine().getStatusCode())
                    .put("reason", closeableHttpResponse.getStatusLine().getReasonPhrase())
                    .toString();

        } catch (IOException e) {
            logger.error("IO exception: ", e);
        }
        return response;
    }

    private void response(String response) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.enableMarkdown(true);
            sendMessage.setChatId(chatId);
            sendMessage.setText(response);

            this.execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.error("Telegram exception: ", e);
        }
    }

    @Override
    public String getBotUsername() {
        return "ZdanovichTravelBot";
    }

    @Override
    public String getBotToken() {
        return "939075418:AAEQr-0NK48DnoGE_f9mPyU4uHLPJvnkPsU";
    }
}
