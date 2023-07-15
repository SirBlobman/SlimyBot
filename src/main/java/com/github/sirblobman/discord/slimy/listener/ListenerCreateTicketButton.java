package com.github.sirblobman.discord.slimy.listener;

import java.awt.Color;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.sirblobman.discord.slimy.SlimyBot;
import com.github.sirblobman.discord.slimy.manager.TicketManager;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public final class ListenerCreateTicketButton extends SlimyBotListener {
    private Modal.Builder modalBuilder;

    public ListenerCreateTicketButton(@NotNull SlimyBot bot) {
        super(bot);
        this.modalBuilder = getCreateTicketModalBuilder();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent e) {
        Button button = e.getButton();
        String buttonId = button.getId();

        if (!Objects.equals(buttonId, "slimy-bot-create-ticket")) {
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
    public void onModalInteraction(@NotNull ModalInteractionEvent e) {
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

        Guild guild = member.getGuild();
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
        Role supportRole = ticketManager.getSupportRole(guild);

        CompletableFuture<TextChannel> ticketChannelFuture = ticketManager.createTicketChannelFor(member);
        TextChannel ticketChannel = ticketChannelFuture.join();

        MessageCreateBuilder builder = new MessageCreateBuilder();
        builder.addContent(supportRole.getAsMention()).addContent("\n");
        builder.addContent(MarkdownUtil.bold("New Ticket")).addContent("\n");
        builder.addContent(MarkdownUtil.bold("Made by:") + " ").addContent(member.getAsMention()).addContent("\n");
        builder.addContent(MarkdownUtil.bold("Title:") + " ").addContent(title).addContent("\n");
        builder.addContent(MarkdownUtil.bold("Plugin:") + " ").addContent(pluginName).addContent("\n");
        builder.addContent(MarkdownUtil.bold("Description:") + " ").addContent(description).addContent("\n");

        MessageCreateData message = builder.build();
        ticketChannel.sendMessage(message).queue();

        EmbedBuilder errorEmbed = getClickedByEmbed(member);
        errorEmbed.addField("Success", "Your ticket was created successfully.", false);
        MessageCreateData message2 = getMessage(errorEmbed);
        interaction.sendMessage(message2).setEphemeral(true).queue();
    }

    private @NotNull TicketManager getTicketManager() {
        SlimyBot discordBot = getBot();
        return discordBot.getTicketManager();
    }

    private @NotNull EmbedBuilder getClickedByEmbed(@NotNull Member sender) {
        User user = sender.getUser();
        String footerIconURL = user.getAvatarUrl();

        String mentionTag = sender.getEffectiveName();
        String footerMessage = ("Clicked by " + mentionTag);

        return new EmbedBuilder().setFooter(footerMessage, footerIconURL);
    }

    private @NotNull EmbedBuilder getErrorEmbed(@Nullable Member sender) {
        EmbedBuilder builder = (sender != null ? getClickedByEmbed(sender) : new EmbedBuilder());
        builder.setColor(Color.RED);
        builder.setTitle("Button Error");
        builder.setDescription("An error occurred when clicking that button.");
        return builder;
    }

    private @NotNull MessageCreateData getMessage(@NotNull EmbedBuilder embedBuilder) {
        MessageCreateBuilder builder = new MessageCreateBuilder();
        MessageEmbed embed = embedBuilder.build();
        builder.setEmbeds(embed);
        return builder.build();
    }

    private @NotNull Modal getCreateTicketModal() {
        Modal.Builder builder = getCreateTicketModalBuilder();
        return builder.build();
    }

    private @NotNull Modal.Builder getCreateTicketModalBuilder() {
        if (this.modalBuilder != null) {
            return this.modalBuilder;
        }

        TextInput.Builder pluginComponentBuilder = TextInput.create("plugin", "Plugin", TextInputStyle.SHORT);
        pluginComponentBuilder.setRequiredRange(1, 32);
        pluginComponentBuilder.setRequired(true);
        pluginComponentBuilder.setPlaceholder("PluginName");

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

        return (this.modalBuilder = modalBuilder);
    }
}
