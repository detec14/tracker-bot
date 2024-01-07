package com.tracker.commands;

import java.util.List;

import com.tracker.objects.Config;
import com.tracker.objects.Tracker;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;

public class EnrollmentCommand implements ICommand {
    private static final String CMD_ENROLLMENT = "enroll";
    private static final String CMD_ENROLL_ENABLE = "enable";
    private static final String CMD_ENROLL_DISABLE = "disable";
    private static final String OPT_WARP = "max-warp";
    private static final String OPT_RUNS_PER_DAY = "max-runs-per-day";
    private static final String OPT_OPS = "ops-level";

    private Config config;

    public EnrollmentCommand(Config config) {
        this.config = config;
    }

    @Override
    public void register(CommandCreateAction command) {
        SubcommandGroupData sub = new SubcommandGroupData(CMD_ENROLLMENT, "Sign in/out for/from player tracking.");

        sub.addSubcommands(new SubcommandData(CMD_ENROLL_ENABLE, "Sign-in for player tracking.")
            .addOptions(new OptionData(OptionType.INTEGER, OPT_WARP, "Your maximal warp range.")
                .setRequired(true))
            .addOptions(new OptionData(OptionType.INTEGER, OPT_RUNS_PER_DAY, "The maximal amount of runs you want to do per day.")
                .setRequired(false))
            .addOptions(new OptionData(OptionType.INTEGER, OPT_OPS, "Your OPS level.")
                .setRequired(false)));
        
        sub.addSubcommands(new SubcommandData(CMD_ENROLL_DISABLE, "Sign-off from player tracking."));

        command.addSubcommandGroups(sub);
    }

    @Override
    public Boolean checkAndRun(SlashCommandInteractionEvent event) {
        Tracker tracker = this.config.getDynamic().findTracker(event.getUser().getEffectiveName());
        if (tracker == null) {
            tracker = new Tracker(event.getUser().getIdLong(), event.getUser().getEffectiveName());
        }

        switch (event.getSubcommandName()) {
            case CMD_ENROLL_ENABLE:
                if (event.getOption(OPT_WARP) != null) {
                    tracker.setWarp(event.getOption(OPT_WARP).getAsInt());
                }
                if (event.getOption(OPT_RUNS_PER_DAY) != null) {
                    tracker.setMaxRunsPerDay(event.getOption(OPT_RUNS_PER_DAY).getAsInt());
                }
                if (event.getOption(OPT_OPS) != null) {
                    tracker.setOps(event.getOption(OPT_OPS).getAsInt());
                }
                this.config.getDynamic().addTracker(tracker);

                event.reply(event.getUser().getEffectiveName() + " successfully signed-in!").queue();
                break;

            case CMD_ENROLL_DISABLE:
                this.config.getDynamic().removeTracker(tracker);

                event.reply(event.getUser().getEffectiveName() + " successfully signed-off!").queue();
                break;

            default:
                return false;
        }

        this.config.save();

        return true;
    }

    @Override
    public List<Choice> getChoices(CommandAutoCompleteInteractionEvent event) {
        return null;
    }
}
