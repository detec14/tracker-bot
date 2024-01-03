package com.tracker.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.tracker.listeners.CommandListener;
import com.tracker.objects.Config;
import com.tracker.objects.Target;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;

public class TargetCommand implements ICommand {
    private static final String CMD_TARGET = "target";
    private static final String CMD_ADD = "add";
    private static final String CMD_REMOVE = "remove";
    private static final String OPT_NAME = "name";
    private static final String OPT_ALLIANCE = "alliance";
    private static final String OPT_WARP = "max-warp";

    private Config config;

    public TargetCommand(Config config) {
        this.config = config;
    }

    @Override
    public void register(CommandCreateAction command) {
        SubcommandGroupData sub = new SubcommandGroupData(CMD_TARGET, "Add/remove target for/from tracking.");

        sub.addSubcommands(new SubcommandData(CMD_ADD, "Add new target for tracking.")
            .addOptions(new OptionData(OptionType.STRING, OPT_NAME, "Name of the player to be tracked.")
                .setRequired(true))
            .addOptions(new OptionData(OptionType.STRING, OPT_ALLIANCE, "Players alliance name.")
                .setRequired(false))
            .addOptions(new OptionData(OptionType.INTEGER, OPT_WARP, "Players maximal warp range.")
                .setRequired(false)));
        
        sub.addSubcommands(new SubcommandData(CMD_REMOVE, "Remove target from tracking.")
            .addOptions(new OptionData(OptionType.STRING, OPT_NAME, "Name of the player to be removed.")
                .setRequired(true).setAutoComplete(true)));

        command.addSubcommandGroups(sub);
    }

    @Override
    public Boolean checkAndRun(SlashCommandInteractionEvent event) {
        Target target = this.config.getDynamic().findTarget(event.getOption(OPT_NAME).getAsString());

        switch (event.getSubcommandName()) {
            case CMD_ADD:
                if (target == null) {
                    target = new Target(event.getOption(OPT_NAME).getAsString());
                }
                if (event.getOption(OPT_ALLIANCE) != null) {
                    target.setAlliance(event.getOption(OPT_ALLIANCE).getAsString());
                }
                if (event.getOption(OPT_WARP) != null) {
                    target.setWarp(event.getOption(OPT_WARP).getAsInt());
                }
                this.config.getDynamic().addTarget(target);

                List<Object> listeners = event.getJDA().getEventManager().getRegisteredListeners();
                for (Object listener : listeners) {
                    if (listener instanceof CommandListener) {
                        ((CommandListener) listener).refreshCommands(event.getGuild());
                        break;
                    }
                }

                event.reply(target.getName() + " successfully added!").queue();
                break;

            case CMD_REMOVE:
                if (target == null) {
                    return false;
                }

                this.config.getDynamic().removeTarget(target);

                event.reply(target.getName() + " successfully removed!").queue();
                break;

            default:
                return false;
        }

        this.config.save();

        return true;
    }

    @Override
    public List<Choice> getChoices(CommandAutoCompleteInteractionEvent event) {
        ArrayList<Target> targets = this.config.getDynamic().getTargets();

        if (!event.getSubcommandName().equals(CMD_REMOVE)) {
            return null;
        }

        switch (event.getFocusedOption().getName()) {
        case OPT_NAME:
            return targets.stream().filter(target -> target.getName().toLowerCase()
                .startsWith(event.getFocusedOption().getValue().toLowerCase()))
                .map(target -> new Choice(target.getName(), target.getName()))
                .limit(25).collect(Collectors.toList());
        }

        return null;
    }
}
