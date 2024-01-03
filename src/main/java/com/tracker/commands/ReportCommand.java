package com.tracker.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.tracker.objects.Config;
import com.tracker.objects.Target;
import com.tracker.objects.Tracker;
import com.tracker.objects.System;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;

public class ReportCommand implements ICommand {
    private static final String CMD_REPORT = "report";
    private static final String OPT_TARGET = "target";
    private static final String OPT_SYSTEM = "system";

    private Config config;

    public ReportCommand(Config config) {
        this.config = config;
    }

    @Override
    public void register(CommandCreateAction command) {
        SubcommandData sub;

        sub = new SubcommandData(CMD_REPORT, "Report a target.");
        sub.addOptions(new OptionData(OptionType.STRING, OPT_TARGET, "Target name.")
            .setRequired(true).setAutoComplete(true));

        sub.addOptions(new OptionData(OptionType.STRING, OPT_SYSTEM, 
            "System where target was spotted.")
            .setRequired(true).setAutoComplete(true));

        command.addSubcommands(sub);
    }

    @Override
    public Boolean checkAndRun(SlashCommandInteractionEvent event) {
        Tracker tracker = new Tracker(event.getUser().getIdLong(), event.getUser().getName());

        switch (event.getSubcommandName()) {
            case CMD_REPORT:
                this.config.getDynamic().spottedTarget(event.getOption(OPT_TARGET).getAsString(), 
                    event.getOption(OPT_SYSTEM).getAsString(), tracker);
                
                event.reply("Stored " + event.getOption(OPT_TARGET).getAsString() + " in "
                    + event.getOption(OPT_SYSTEM).getAsString() + "!").queue();
                break;

            default:
                return false;
        }

        this.config.save();

        return true;
    }

    @Override
    public List<Choice> getChoices(CommandAutoCompleteInteractionEvent event) {
        final ArrayList<System> systems = this.config.getStatic().getAllSystems();
        ArrayList<Target> targets = this.config.getDynamic().getTargets();

        if (!event.getSubcommandName().equals(CMD_REPORT)) {
            return null;
        }

        switch (event.getFocusedOption().getName()) {
        case OPT_SYSTEM:
            return systems.stream().filter(system -> system.getName().toLowerCase()
                .startsWith(event.getFocusedOption().getValue().toLowerCase()))
                .map(system -> new Choice(system.getName(), system.getName()))
                .limit(25).collect(Collectors.toList());

        case OPT_TARGET:
            return targets.stream().filter(target -> target.getName().toLowerCase()
                .startsWith(event.getFocusedOption().getValue().toLowerCase()))
                .map(target -> new Choice(target.getName(), target.getName()))
                .limit(25).collect(Collectors.toList());
        }

        return null;
    }
}
