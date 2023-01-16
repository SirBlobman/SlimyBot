package com.github.sirblobman.discord.slimy.listener;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.slash.SlashCommandFAQ;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public final class ListenerFAQButtons extends SlimyBotListener {

    private final SlashCommandFAQ slashCommandFAQ;

    public ListenerFAQButtons(final DiscordBot discordBot, final SlashCommandFAQ slashCommandFAQ) {
        super(discordBot);
        this.slashCommandFAQ = slashCommandFAQ;
    }

    @Override
    public void onButtonInteraction(final ButtonInteractionEvent event) {
        Button button = event.getButton();
        String id = button.getId();

        Member member = event.getMember();
        ButtonInteraction interaction = event.getInteraction();

        if (id == null || !id.startsWith("faq-")) return;
        String questionId = id.substring(4);

        if (questionId.equals("close")) {
            interaction.getMessage()
                    .delete()
                    .queue();

            return;
        }

        MessageEditData messageEditData = MessageEditData.fromCreateData(slashCommandFAQ.buildResponse(questionId, member));
        interaction.editMessage(messageEditData)
                .queue();
    }
}
