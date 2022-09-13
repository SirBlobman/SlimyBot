package com.github.sirblobman.discord.slimy.listener;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.slash.SlashCommand;
import com.github.sirblobman.discord.slimy.manager.SlashCommandManager;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public final class ListenerSlashCommands extends SlimyBotListener {
    public ListenerSlashCommands(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        DiscordBot discordBot = getDiscordBot();
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
}
