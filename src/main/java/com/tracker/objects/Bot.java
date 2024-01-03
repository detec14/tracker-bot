package com.tracker.objects;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Bot {
    @JsonProperty("bot-token")
    private String token;

    Bot() {
        this.token = null;
    }

    public Bot(String token) {
        this.token = token;
    }

    public static Bot loadConfig() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(new File("assets/config.json"), Bot.class);
    }

    public String getToken() {
        return this.token;
    }
}
