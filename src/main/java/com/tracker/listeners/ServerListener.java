package com.tracker.listeners;

import java.io.File;
import java.util.List;

import com.tracker.Watcher;
import com.tracker.objects.Config;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ServerListener extends ListenerAdapter {

    public static void loadServers(JDA jda) {
        File[] directories = new File(Config.CONFIG_PATH).listFiles(File::isDirectory);

        for (File dir : directories) {
            Guild server = jda.getGuildById(dir.getName());
            configureServer(server);
        }
    }

    private static void configureServer(Guild server) {
        Config config = null;

        try {
            config = Config.load(server.getIdLong());
        } catch (Exception e) {
            System.out.println("ServerListener.configureServer: Unable to load configuration!");
            System.out.println("   -> " + e.getMessage());
            return;
        }

        List<TextChannel> channels = server.getTextChannelsByName("tracking", true);
        if (channels.isEmpty()) {
            System.out.println("ServerListener.configureServer: Unable to get channel!");
            return;
        }

        config.getDynamic().getServer().setChannel(channels.get(0).getIdLong());
        config.setBotId(server.getJDA().getSelfUser().getIdLong());

        server.getJDA().addEventListener(new CommandListener(config, server));
        
        Watcher watcher = new Watcher(config, server);
        watcher.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                watcher.stop();
            }
        });
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        configureServer(event.getGuild());
    }
}
