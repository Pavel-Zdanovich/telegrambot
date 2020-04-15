package com.zdanovich;

import com.zdanovich.telegrambot.bot.ZdanovichTravelBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new ZdanovichTravelBot());
        } catch (TelegramApiRequestException e) {
            logger.error("Telegram exception: ", e);
        }
    }

}
