package com.github.sirblobman.discord.slimy.listener;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.slash.SlashCommandFAQ;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public final class ListenerQuestionButtons extends SlimyBotListener {

    private final SlashCommandFAQ command;

    public ListenerQuestionButtons(@NotNull DiscordBot discordBot, @NotNull SlashCommandFAQ command) {
        super(discordBot);
        this.command = command;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Member member = event.getMember();
        if (member == null) {
            return;
        }

        Button button = event.getButton();
        String id = button.getId();
        if (id == null || !id.startsWith("faq-")) {
            return;
        }

        ButtonInteraction interaction = event.getInteraction();
        String questionId = id.substring(4);
        if (questionId.equals("close")) {
            interaction.editMessage("FAQ Closed.").queue();
            InteractionHook hook = interaction.getHook();
            hook.editOriginalEmbeds().queue();
            hook.editOriginalComponents().queue();
            return;
        }

        MessageCreateData response = buildResponse(questionId, member);
        interaction.editMessage(MessageEditData.fromCreateData(response)).queue();
    }

    private @NotNull SlashCommandFAQ getCommand() {
        return this.command;
    }

    private @NotNull MessageCreateData buildResponse(@NotNull String id, @NotNull Member member) {
        SlashCommandFAQ command = getCommand();
        return command.buildResponse(id, member);
    }
}
