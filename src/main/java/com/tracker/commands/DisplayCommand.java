package com.tracker.commands;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.tracker.objects.Config;
import com.tracker.objects.Target;
import com.tracker.objects.Tracker;
import com.tracker.objects.Zone;
import com.utils.SplitString;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;

public class DisplayCommand implements ICommand {
    private static final String CMD_STATISTICS = "show";
    private static final String CMD_TRACKERS = "trackers";
    private static final String CMD_TARGETS = "targets";
    private static final String CMD_SYSTEMS = "systems";
    private static final String CMD_ZONES = "zones";
    private static final String OPT_NAME = "name";

    private Config config;

    public DisplayCommand(Config config) {
        this.config = config;
    }

    @Override
    public void register(CommandCreateAction command) {
        SubcommandGroupData sub = new SubcommandGroupData(CMD_STATISTICS, "Show tracker information.");

        sub.addSubcommands(
            new SubcommandData(CMD_TRACKERS, "Summary about current sign-in trackers."),
            new SubcommandData(CMD_TARGETS, "Summary about current targets."),
            new SubcommandData(CMD_SYSTEMS, "Show systems/zones map."),
            new SubcommandData(CMD_ZONES, "Show information about zones.")
                .addOptions(new OptionData(OptionType.STRING, OPT_NAME, "Name of the zone.")
                    .setRequired(true).setAutoComplete(true)));

        command.addSubcommandGroups(sub);
    }

    @Override
    public Boolean checkAndRun(SlashCommandInteractionEvent event) {
        switch (event.getSubcommandName()) {
        case CMD_TRACKERS:
            sendTrackersSummary(event);
            break;

        case CMD_TARGETS:
            sendTargetsSummary(event);
            break;

        case CMD_SYSTEMS:
            sendSystemsPicture(event);
            break;

        case CMD_ZONES:
            sendZoneSummary(event, event.getOption(OPT_NAME).getAsString());
            break;

        default:
            return false;
        }

        return true;
    }

    @Override
    public List<Choice> getChoices(CommandAutoCompleteInteractionEvent event) {
        final ArrayList<Zone> zones = this.config.getStatic().getZones();

        if (!event.getSubcommandName().equals(CMD_ZONES)) {
            return null;
        }

        switch (event.getFocusedOption().getName()) {
        case OPT_NAME:
            return zones.stream().filter(system -> system.getName().toLowerCase()
                .startsWith(event.getFocusedOption().getValue().toLowerCase()))
                .map(system -> new Command.Choice(system.getName(), system.getName()))
                .limit(25).collect(Collectors.toList());
        }

        return null;
    }

    private String generateTrackersSummary() {
        String output = "";

        output += "```\n";
        output += "Tracker              Req/Day    Enrolled   Assignment\n";
        output += "==================== ========== ========== ========================================\n";
        for (Tracker tracker : this.config.getDynamic().getTrackers()) {
            String systems = "";
            if (tracker.getZones() != null) {
                for (String zone : tracker.getZones()) {
                    systems += zone + " [";
                    systems += this.config.getStatic().findZone(zone).getSystems()
                        .stream().map(Object::toString)
                        .collect(Collectors.joining(", "));
                    systems += "], ";
                }
                if (systems.endsWith(", ")) {
                    systems = systems.substring(0, systems.lastIndexOf(","));
                }
            } else {
                systems = "-";
            }

            List<String> list = SplitString.splitString(systems, 40);

            output += String.format("%-20s %-10d %-10s %s\n", 
                tracker.getName(), tracker.getMaxRunsPerDay(), tracker.isEnrolled(), list.get(0));
            if (list.size() > 1) {
                for (String line : list.subList(1, list.size())) {
                    output += String.format("%-42s %s\n", " ", line);
                }
            }
        }
        output += "```";

        return output;
    }

    private String generateTargetsSummary() {
        String output = "";

        output += "```\n";
        output += "Target               Spotted    System                     Time\n";
        output += "==================== ========== ========================== ========================\n";
        for (Target target : this.config.getDynamic().getTargets()) {
            output += String.format("%-20s %-10s %-26s %s\n", 
                target.getName(), target.isSpotted(),
                (target.getSystem() == null) ? "-" : target.getSystem(),
                (target.getTime() == null) ? "-" : DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .format(target.getTime()));
        }
        output += "```";

        return output;
    }

    private String generateZoneSummary(String zone) {
        String output = "";

        output += "```\n";
        output += "Zone: " + zone + "\n";
        output += "===================================================================================\n";

        output += this.config.getStatic().findZone(zone).getSystems()
            .stream().map(Object::toString).collect(Collectors.joining(", "));

        output += "```";

        return output;
    }

    private void sendMessageIntoChannel(String msg, SlashCommandInteractionEvent event) {
        int s = Math.min(1950, msg.length());

        event.reply(msg.substring(0, s)).queue();

        for (; s < msg.length(); s += 1950) {
            event.getChannel().sendMessage(msg.substring(s, Math.min(s + 1950, msg.length()))).queue();
        }
    }

    private void sendTrackersSummary(SlashCommandInteractionEvent event) {
        sendMessageIntoChannel(generateTrackersSummary(), event);
    }

    private void sendTargetsSummary(SlashCommandInteractionEvent event) {
        sendMessageIntoChannel(generateTargetsSummary(), event);
    }

    private void sendSystemsPicture(SlashCommandInteractionEvent event) {
        event.reply("Check: " + getMapUrl()).queue();
    }

    private void sendZoneSummary(SlashCommandInteractionEvent event, String zone) {
        sendMessageIntoChannel(generateZoneSummary(zone), event);
    }

    public static String getMapUrl() {
        return "https://picklemap.000webhostapp.com";
    }
}
