package com.zdanovich.telegrambot.logic;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReceiveUserData extends AbstractAction {

    private final static Logger logger = LoggerFactory.getLogger(AbstractAction.class);

    private Map<String, String> receiveData;
    private int index;

    private String requestMethod;

    private RequestBuilder requestToBackend;

    private String responseToFrontend;

    public ReceiveUserData(Map<String, String> receiveData, String requestMethod, RequestBuilder requestToBackend) {
        this.receiveData = receiveData;
        this.requestMethod = requestMethod;
        this.requestToBackend = requestToBackend;
    }

    @Override
    public void processInput(String message) {
        if ((responseToFrontend = validateInput(message)) != null) {
            Flow.start();
            requestToBackend = null;
            return;
        }

        String currentReceiveDataName = getCurrentReceiveDataName();
        String currentReceiveDataRegex = getReceiveDataRegex(currentReceiveDataName);

        if (message.matches(currentReceiveDataRegex)) {

            if (requestMethod.equals(HttpGet.METHOD_NAME) || requestMethod.equals(HttpDelete.METHOD_NAME)) {

                requestToBackend.addParameter(currentReceiveDataName, message);

            } else if (requestMethod.equals(HttpPost.METHOD_NAME) || requestMethod.equals(HttpPut.METHOD_NAME)) {

                String jsonRequest = new JSONObject(getRequestToBackend()).put(currentReceiveDataName, message).toString();
                requestToBackend.setEntity(new StringEntity(jsonRequest, ContentType.APPLICATION_JSON));

            }
        } else {
            Flow.start();
            requestToBackend = null;
            responseToFrontend = String.format("A strange %s for a city, let's start over.", currentReceiveDataName);
        }
    }

    @Override
    public HttpUriRequest requestToBackend() {
        if (requestToBackend == null) {
            return null;
        }
        if (isAllDataReceived()) {

            return requestToBackend.build();

        }
        if (requestMethod.equals(HttpPost.METHOD_NAME) || requestMethod.equals(HttpPut.METHOD_NAME)) {

            String parameter = new JSONObject(getRequestToBackend()).getString(getCurrentReceiveDataName());

            return RequestBuilder.create(HttpGet.METHOD_NAME)
                    .setUri(requestToBackend.getUri()).addParameter(getCurrentReceiveDataName(), parameter).build();

        }
        return null;
    }

    private String getRequestToBackend() {
        String json = "{}";
        HttpEntity httpEntity = requestToBackend.getEntity();
        if (httpEntity != null) {
            try (InputStream inputStream = httpEntity.getContent()) {
                json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            } catch (IOException e) {
                logger.error("IO Exception: ", e);
            }
        }
        return json;
    }

    @Override
    public void processResponseFromBackend(String response) {
        JSONObject jsonObject = new JSONObject(response);

        switch (requestMethod) {
            case HttpPost.METHOD_NAME: {

                if (jsonObject.getInt("code") == HttpStatus.SC_OK) {

                    if (isAllDataReceived()) {

                        Flow.start();
                        printCity("City created\n", jsonObject);

                    } else {

                        Flow.start();
                        printCity("I know that city\n", jsonObject);

                    }
                } else {

                    if (isAllDataReceived()) {

                        Flow.start();
                        responseToFrontend = String.format("Something went wrong on backend\n%s", jsonObject.toString());

                    } else {

                        Flow.setNextAction(this);
                        index++;
                        responseToFrontend = String.format("Please, enter city %s", getCurrentReceiveDataName());

                    }

                }
                break;

            }
            case HttpGet.METHOD_NAME: {

                Flow.start();
                if (jsonObject.getInt("code") == HttpStatus.SC_OK) {

                    printCity("I know that city\n", jsonObject);

                } else {

                    responseToFrontend = String.format("Sorry, I don't know city '%s'",
                            requestToBackend.getParameters().get(0).getValue());

                }
                break;

            }
            case HttpPut.METHOD_NAME: {
                if (jsonObject.getInt("code") == HttpStatus.SC_OK) {

                    if (isAllDataReceived()) {

                        Flow.start();
                        printCity("City update\n", jsonObject);

                    } else {

                        Flow.setNextAction(this);
                        index++;
                        responseToFrontend = String.format("Please, enter city %s", getCurrentReceiveDataName());

                    }
                } else {

                    Flow.start();
                    String name = new JSONObject(getRequestToBackend()).getString(getCurrentReceiveDataName());
                    responseToFrontend = String.format("Sorry, I don't know city '%s'", name);

                }
                break;
            }
            case HttpDelete.METHOD_NAME: {

                Flow.start();
                if (jsonObject.getInt("code") == HttpStatus.SC_OK) {

                    responseToFrontend = String.format("City deleted\n%s", jsonObject.toString());

                } else {

                    responseToFrontend = String.format("Sorry, I don't know city '%s'",
                            requestToBackend.getParameters().get(0).getValue());

                }
                break;
            }
        }
    }

    public void printCity(String preffix, JSONObject jsonObject) {
        StringBuilder stringBuilder = new StringBuilder(preffix);
        Flow.receiveAllData.keySet().forEach(key ->
                stringBuilder.append(key).append(": ").append(jsonObject.getString(key)).append("\n"));
        responseToFrontend = stringBuilder.toString();
    }

    @Override
    public String responseToFrontend() {
        return responseToFrontend;
    }

    public String getCurrentReceiveDataName() {
        List<String> keys = new ArrayList<>(this.receiveData.keySet());
        return keys.get(index);
    }

    public String getReceiveDataRegex(String receiveDataName) {
        return this.receiveData.get(receiveDataName);
    }

    public boolean isAllDataReceived() {
        return receiveData.size() == index + 1;
    }
}
