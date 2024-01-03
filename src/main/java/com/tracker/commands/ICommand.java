package com.tracker.commands;

import java.util.List;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;

public interface ICommand {
    public void register(CommandCreateAction command);
    public Boolean checkAndRun(SlashCommandInteractionEvent event);
    public List<Command.Choice> getChoices(CommandAutoCompleteInteractionEvent event);
}
