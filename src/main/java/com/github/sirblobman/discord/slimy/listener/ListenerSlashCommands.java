package com.github.sirblobman.discord.slimy.listener;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.SlashCommandManager;
import com.github.sirblobman.discord.slimy.command.slash.SlashCommand;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public final class ListenerSlashCommands extends SlimyBotListener {
    public ListenerSlashCommands(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    public void onSlashCommand(SlashCommandEvent e) {
        DiscordBot discordBot = getDiscordBot();
        SlashCommandManager slashCommandManager = discordBot.getSlashCommandManager();

        String commandName = e.getName();
        SlashCommand command = slashCommandManager.getCommand(commandName);
        if(command == null) return;

        boolean ephemeral = command.isEphemeral();
        e.deferReply(ephemeral).queue();

        Message message = command.execute(e);
        if(message != null) {
            e.getHook().sendMessage(message).queue();
        } else {
            e.getHook().sendMessage("Done.").queue();
        }
    }
}
