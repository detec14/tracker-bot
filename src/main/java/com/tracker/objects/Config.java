package com.tracker.objects;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Config {
    public static final String CONFIG_PATH = "assets/servers/";
    private ConfigStatic staticConfig;
    private ConfigDynamic dynamicConfig;
    private Long botId;

    public Config(ConfigStatic sconfig, ConfigDynamic dconfig) {
        this.staticConfig = sconfig;
        this.dynamicConfig = dconfig;
        this.botId = Long.valueOf(0);
    }

    public static Config load(Long serverId) throws Exception {
        String configPath = CONFIG_PATH + serverId + "/";
        ObjectMapper mapper = new ObjectMapper();
        ConfigDynamic dynamic = null;

         if (!Files.exists(Paths.get(configPath))) {
            try {
                Files.createDirectory(Paths.get(configPath));
            } catch (IOException e) {
                throw new Exception("Unable to create configuration " +
                    "directory for server " + serverId);
            }
        }

        mapper.registerModule(new JavaTimeModule());
        
        try {
            dynamic = mapper.readValue(new File(configPath + 
                "dynamic.json"), ConfigDynamic.class);
        } catch (Exception e) {
            dynamic = new ConfigDynamic(serverId);
        }

        return new Config(mapper.readValue(new File("assets/static.json"), 
            ConfigStatic.class), dynamic);
    }

    public void save() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.registerModule(new JavaTimeModule());

        try {
            mapper.writeValue(new File(CONFIG_PATH + this.dynamicConfig.getServer().getId() + 
                "/dynamic.json"), this.dynamicConfig);
        } catch (Exception e) {
            java.lang.System.out.println("Config.save: Cannot save configuration!");
        }
    }

    public ConfigStatic getStatic() {
        return this.staticConfig;
    }

    public ConfigDynamic getDynamic() {
        return this.dynamicConfig;
    }

    public Long getBotId() {
        return this.botId;
    }

    public void setBotId(Long botId) {
        this.botId = botId;
    }
}
