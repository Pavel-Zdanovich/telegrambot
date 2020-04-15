package com.zdanovich.telegrambot.logic;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Flow {

    private final static Logger logger = LoggerFactory.getLogger(Flow.class);

    public static final Map<String, String> COMMAND_TO_METHOD = new HashMap<String, String>() {
        {
            put("/create", HttpPost.METHOD_NAME);
            put("/read", HttpGet.METHOD_NAME);
            put("/update", HttpPut.METHOD_NAME);
            put("/delete", HttpDelete.METHOD_NAME);
        }
    };

    public static final Map<String, String> receiveAllData = new HashMap<String, String>() {
        {
            put("name", "^[a-zA-Z\\s]+$");
            put("description", "^[a-zA-Z0-9\\s]+$");
        }
    };

    public static final Map<String, String> receiveName = new HashMap<String, String>() {
        {
            put("name", "^[a-zA-Z]+$");
        }
    };

    public static final Map<String, Map<String, String>> COMMAND_TO_RECEIVE_DATA = new HashMap<String, Map<String, String>>() {
        {
            put("/create", receiveAllData);
            put("/read", receiveName);
            put("/update", receiveAllData);
            put("/delete", receiveName);
        }
    };

    public static final AbstractAction INITIAL_QUERY = new Command();

    private static AbstractAction action = INITIAL_QUERY;

    private Flow() {}

    public static void start() {
        action = INITIAL_QUERY;
    }

    public static AbstractAction getCurrentAction() {
        return action;
    }

    public static void setNextAction(AbstractAction abstractAction) {
        Flow.action = abstractAction;
    }

}
