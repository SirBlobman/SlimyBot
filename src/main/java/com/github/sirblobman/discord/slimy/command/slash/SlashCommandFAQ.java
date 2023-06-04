package com.github.sirblobman.discord.slimy.command.slash;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.configuration.question.Question;
import com.github.sirblobman.discord.slimy.configuration.question.QuestionConfiguration;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public final class SlashCommandFAQ extends SlashCommand {
    private static final Button CLOSE;

    static {
        CLOSE = Button.danger("faq-close", "Close");
    }

    public SlashCommandFAQ(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    public @NotNull CommandData getCommandData() {
        return Commands.slash("faq", "Get some default answers to common questions.")
                .addOption(OptionType.STRING, "id", "The ID of the question.",
                        true, true);
    }

    @Override
    public @NotNull MessageCreateData execute(@NotNull SlashCommandInteractionEvent e) {
        Member member = e.getMember();
        if (member == null) {
            EmbedBuilder embed = getErrorEmbed(null);
            embed.addField("Error", "This command can only be executed in a guild.", false);
            return getMessage(embed);
        }

        OptionMapping questionIdOption = e.getOption("id");
        if (questionIdOption == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(member);
            errorEmbed.addField("Error", "Missing Argument 'id'.", false);
            return getMessage(errorEmbed);
        }

        String questionId = questionIdOption.getAsString();
        return buildResponse(questionId, member);
    }

    @Override
    public void onAutoComplete(@NotNull CommandAutoCompleteInteraction e) {
        DiscordBot discordBot = getDiscordBot();
        QuestionConfiguration configuration = discordBot.getQuestionConfiguration();
        Map<String, Question> questionMap = configuration.getQuestions();
        List<Command.Choice> choices = questionMap.keySet().stream()
                .filter(word -> word.startsWith(e.getFocusedOption().getValue()))
                .map(word -> new Command.Choice(word, word))
                .toList();
        e.replyChoices(choices).queue();
    }

    private @NotNull Question getQuestion(@NotNull String id) {
        DiscordBot discordBot = getDiscordBot();
        QuestionConfiguration configuration = discordBot.getQuestionConfiguration();

        Question question = configuration.getQuestion(id);
        if (question == null) {
            throw new IllegalStateException("Failed to find a question with id '" + id + "'.");
        }

        return question;
    }

    private @NotNull EmbedBuilder getEmbed(@NotNull Member member, @NotNull Question question, @NotNull String id) {
        EmbedBuilder embed = getExecutedByEmbed(member);
        embed.setColor(Color.GREEN);
        embed.setTitle("FAQ");
        embed.setDescription("Question ID: " + id);

        String pluginName = question.getPlugin();
        if (pluginName != null) {
            embed.addField("Plugin", pluginName, false);
        }

        String questionText = question.getQuestion();
        embed.addField("Question", questionText, false);

        String answer = question.getAnswer();
        embed.addField("Answer", answer, false);

        List<String> related = question.getRelated();
        int relatedSize = related.size();
        if (relatedSize > 0) {
            String relatedString = String.join(", ", related);
            embed.addField("Related", relatedString, false);
        }

        return embed;
    }

    public @NotNull MessageCreateData buildResponse(@NotNull String id, @NotNull Member sender) {
        try {
            Question question = getQuestion(id);
            MessageEmbed embed = getEmbed(sender, question, id).build();
            Button[] buttons = question.getRelated().stream()
                    .map(related -> Button.primary("faq-" + related, related))
                    .toArray(Button[]::new);

            MessageCreateBuilder message = new MessageCreateBuilder().addEmbeds(embed);
            if (buttons.length > 0) {
                message.addActionRow(buttons);
            }

            return message.addActionRow(CLOSE).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            EmbedBuilder errorEmbed = getErrorEmbed(sender);
            errorEmbed.addField("Error", ex.getMessage(), false);
            return getMessage(errorEmbed);
        }
    }
}
