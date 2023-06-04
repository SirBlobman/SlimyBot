package com.github.sirblobman.discord.slimy.listener;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.discord.slimy.SlimyBot;
import com.github.sirblobman.discord.slimy.command.slash.SlashCommand;
import com.github.sirblobman.discord.slimy.manager.SlashCommandManager;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public final class ListenerSlashCommands extends SlimyBotListener {
    public ListenerSlashCommands(@NotNull SlimyBot discordBot) {
        super(discordBot);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
        SlimyBot discordBot = getDiscordBot();
        SlashCommandManager slashCommandManager = discordBot.getSlashCommandManager();
        InteractionHook interaction = e.getHook();

        String commandName = e.getName();
        SlashCommand command = slashCommandManager.getCommand(commandName);
        if (command == null) {
            e.reply("Unknown command '/" + commandName + "'.").queue();
            return;
        }

        boolean ephemeral = command.isEphemeral();
        e.deferReply(ephemeral).queue();

        MessageCreateData message = command.execute(e);
        if (message != null) {
            interaction.sendMessage(message).queue();
        } else {
            interaction.sendMessage("Done.").queue();
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent e) {
        SlimyBot discordBot = getDiscordBot();
        SlashCommandManager commandManager = discordBot.getSlashCommandManager();

        String commandName = e.getName();
        SlashCommand command = commandManager.getCommand(commandName);
        if (command == null) {
            return;
        }

        command.onAutoComplete(e);
    }
}
