package com.tracker;

import com.tracker.listeners.ServerListener;
import com.tracker.objects.Bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class App {
    public static void main(String[] args) throws Exception
    {
        Bot bot = Bot.loadConfig();

        JDA jda = JDABuilder.createDefault(bot.getToken())
            .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.DIRECT_MESSAGES, 
                GatewayIntent.GUILD_MEMBERS)
            .setStatus(OnlineStatus.ONLINE)
            .setActivity(Activity.customStatus("Catch them all!"))
            .build()
            .awaitReady();
        
        ServerListener.loadServers(jda);

        jda.addEventListener(new ServerListener());

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                jda.shutdownNow();
            }
        });
        
        jda.awaitShutdown();
    }
}
