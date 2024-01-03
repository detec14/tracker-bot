package com.tracker.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.tracker.commands.DisplayCommand;
import com.tracker.commands.EnrollmentCommand;
import com.tracker.commands.ICommand;
import com.tracker.commands.ReportCommand;
import com.tracker.commands.TargetCommand;
import com.tracker.objects.Config;
import com.tracker.objects.Tracker;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;

public class CommandListener extends ListenerAdapter {
    private static final String CMD_TRACKER = "tracker";

    private Config config;
    private ArrayList<ICommand> commands;

    public CommandListener(Config config, Guild server) {
        this.config = config;
        this.commands = new ArrayList<>(Arrays.asList(
                new EnrollmentCommand(config),
                new DisplayCommand(config),
                new ReportCommand(config),
                new TargetCommand(config)
        ));

        refreshCommands(server);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getMember().getUser().isBot()) {
            return;
        } else if (event.getChannel().getIdLong() != this.config.getDynamic().getServer().getChannel()) {
            return;
        }

        switch (event.getName()) {
            case CMD_TRACKER:
                Boolean done = false;
                for (ICommand command : this.commands) {
                    if ((done = command.checkAndRun(event)) == true) {
                        break;
                    }
                }

                if (done == true) {
                    break;
                }

            default:
                event.reply("Command not supported!").queue();
                return;
        }
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        User user = retrieveUser(event.getGuild(), event.getUserId(), event.getUser());

        if (user.isBot()) {
            return;
        } else if (event.getChannel().getIdLong() != this.config.getDynamic().getServer().getChannel()) {
            return;
        } else if (event.getMessageAuthorIdLong() != this.config.getBotId()) {
            return;
        }

        Tracker tracker = this.config.getDynamic().findTracker(user.getEffectiveName());
        if (tracker == null) {
            return;
        }

        if (event.getReaction().getEmoji().getAsReactionCode().equals("❌")) {
            this.config.getDynamic().clearEnrolledTracker(tracker);
            this.config.save();
        } else if (event.getReaction().getEmoji().getAsReactionCode().equals("✅")) {
            this.config.getDynamic().setEnrolledTracker(tracker);
            this.config.save();
        }
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        User user = retrieveUser(event.getGuild(), event.getUserId(), event.getUser());

        if (user.isBot()) {
            return;
        } else if (event.getChannel().getIdLong() != this.config.getDynamic().getServer().getChannel()) {
            return;
        }

        Tracker tracker = this.config.getDynamic().findTracker(user.getEffectiveName());
        if (tracker == null) {
            return;
        }

        if (event.getReaction().getEmoji().getAsReactionCode().equals("✅")) {
            this.config.getDynamic().clearEnrolledTracker(tracker);
            this.config.save();
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        List<Command.Choice> options;

        for (ICommand command : this.commands) {
            options = command.getChoices(event);
            if (options != null) {
                event.replyChoices(options).queue();
                return;
            }
        }
    }

    public void refreshCommands(Guild server) {
        CommandCreateAction cmd = server.upsertCommand(CMD_TRACKER, "Tracking command.");
        for (ICommand command : this.commands) {
            command.register(cmd);
        }
        cmd.queue();
    }

    private User retrieveUser(Guild server, String id, User user) {
        if (user != null) {
            return user;
        }

        return server.retrieveMemberById(id).complete().getUser();
    }
}
