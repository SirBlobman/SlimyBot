package com.github.sirblobman.discord.slimy.listener;

import java.awt.Color;
import java.util.concurrent.CompletableFuture;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.manager.TicketManager;
import com.github.sirblobman.discord.slimy.object.InvalidConfigurationException;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public final class ListenerCreateTicketButton extends SlimyBotListener {
    public ListenerCreateTicketButton(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent e) {
        Button button = e.getButton();
        String buttonId = button.getId();

        DiscordBot discordBot = getDiscordBot();
        Logger logger = discordBot.getLogger();
        logger.info("Detected button click with id '" + buttonId + "'.");

        if (buttonId == null || !buttonId.equals("slimy-bot-create-ticket")) {
            return;
        }

        Member member = e.getMember();
        if (member == null) {
            return;
        }

        Modal modal = getCreateTicketModal();
        e.replyModal(modal).queue();
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent e) {
        String modalId = e.getModalId();
        if (!modalId.equals("slimy-bot-create-ticket")) {
            return;
        }

        Member member = e.getMember();
        if (member == null) {
            return;
        }

        e.deferReply(true).submit(false);
        InteractionHook interaction = e.getHook();

        ModalMapping pluginMapping = e.getValue("plugin");
        ModalMapping titleMapping = e.getValue("title");
        ModalMapping descriptionMapping = e.getValue("description");
        if (pluginMapping == null || titleMapping == null || descriptionMapping == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(member);
            errorEmbed.addField("Error", "Please fill out the form properly.", false);
            MessageCreateData message = getMessage(errorEmbed);
            interaction.sendMessage(message).setEphemeral(true).queue();
            return;
        }

        TicketManager ticketManager = getTicketManager();
        if (ticketManager.hasTicketChannel(member)) {
            EmbedBuilder errorEmbed = getErrorEmbed(member);
            errorEmbed.addField("Error", "Your previous ticket is still open.", false);
            MessageCreateData message = getMessage(errorEmbed);
            interaction.sendMessage(message).setEphemeral(true).queue();
            return;
        }

        String pluginName = pluginMapping.getAsString();
        String title = titleMapping.getAsString();
        String description = descriptionMapping.getAsString();
        Role supportRole = ticketManager.getSupportRole();
        if (supportRole == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(member);
            errorEmbed.addField("Error", "An error occurred while creating your ticket.", false);
            MessageCreateData message = getMessage(errorEmbed);
            interaction.sendMessage(message).setEphemeral(true).queue();
            throw new IllegalStateException("Invalid support role!");
        }

        try {
            CompletableFuture<TextChannel> ticketChannelFuture = ticketManager.createTicketChannelFor(member);
            TextChannel ticketChannel = ticketChannelFuture.join();

            MessageCreateBuilder builder = new MessageCreateBuilder();
            builder.addContent(supportRole.getAsMention()).addContent("\n");
            builder.addContent(formatBold("New Ticket")).addContent("\n");
            builder.addContent(formatBold("Made by: ")).addContent(member.getAsMention()).addContent("\n");
            builder.addContent(formatBold("Title: ")).addContent(title).addContent("\n");
            builder.addContent(formatBold("Plugin: ")).addContent(pluginName).addContent("\n");
            builder.addContent(formatBold("Description: ")).addContent(description).addContent("\n");

            MessageCreateData message = builder.build();
            ticketChannel.sendMessage(message).queue();

            EmbedBuilder errorEmbed = getClickedByEmbed(member);
            errorEmbed.addField("Success", "Your ticket was created successfully.", false);
            MessageCreateData message2 = getMessage(errorEmbed);
            interaction.sendMessage(message2).setEphemeral(true).queue();
        } catch (InvalidConfigurationException ex) {
            EmbedBuilder errorEmbed = getErrorEmbed(member);
            errorEmbed.addField("Error", "An error occurred while creating your ticket.", false);
            MessageCreateData message = getMessage(errorEmbed);
            interaction.sendMessage(message).setEphemeral(true).queue();
            throw new IllegalStateException(ex);
        }
    }

    private TicketManager getTicketManager() {
        DiscordBot discordBot = getDiscordBot();
        return discordBot.getTicketManager();
    }

    private EmbedBuilder getClickedByEmbed(Member sender) {
        User user = sender.getUser();
        String footerIconURL = user.getAvatarUrl();

        String mentionTag = sender.getEffectiveName();
        String footerMessage = ("Clicked by " + mentionTag);

        return new EmbedBuilder().setFooter(footerMessage, footerIconURL);
    }

    private EmbedBuilder getErrorEmbed(@Nullable Member sender) {
        EmbedBuilder builder = (sender != null ? getClickedByEmbed(sender) : new EmbedBuilder());
        builder.setColor(Color.RED);
        builder.setTitle("Button Error");
        builder.setDescription("An error occurred when clicking that button.");
        return builder;
    }

    private MessageCreateData getMessage(EmbedBuilder embedBuilder) {
        MessageCreateBuilder messageBuilder = new MessageCreateBuilder();
        MessageEmbed embed = embedBuilder.build();
        messageBuilder.setEmbeds(embed);
        return messageBuilder.build();
    }

    private Modal getCreateTicketModal() {
        TextInput.Builder pluginComponentBuilder = TextInput.create("plugin", "Plugin", TextInputStyle.SHORT);
        pluginComponentBuilder.setRequiredRange(1, 32);
        pluginComponentBuilder.setRequired(true);
        pluginComponentBuilder.setPlaceholder("CombatLogX");

        TextInput.Builder titleComponentBuilder = TextInput.create("title", "Title", TextInputStyle.SHORT);
        titleComponentBuilder.setRequiredRange(1, 64);
        titleComponentBuilder.setRequired(true);
        titleComponentBuilder.setPlaceholder("Choose a title for your ticket...");

        TextInput.Builder descriptionComponentBuilder = TextInput.create("description", "Description",
                TextInputStyle.PARAGRAPH);
        descriptionComponentBuilder.setRequiredRange(10, MessageEmbed.VALUE_MAX_LENGTH);
        descriptionComponentBuilder.setRequired(true);
        descriptionComponentBuilder.setPlaceholder("Write a description for your ticket. " +
                "Make sure to include any relevant plugin versions.");

        TextInput pluginComponent = pluginComponentBuilder.build();
        TextInput titleComponent = titleComponentBuilder.build();
        TextInput descriptionComponent = descriptionComponentBuilder.build();

        Modal.Builder modalBuilder = Modal.create("slimy-bot-create-ticket", "Create Ticket");
        modalBuilder.addActionRow(pluginComponent);
        modalBuilder.addActionRow(titleComponent);
        modalBuilder.addActionRow(descriptionComponent);
        return modalBuilder.build();
    }
}
