package com.github.sirblobman.discord.slimy.command.slash;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.discord.slimy.SlimyBot;
import com.github.sirblobman.discord.slimy.configuration.guild.GuildConfiguration;
import com.github.sirblobman.discord.slimy.configuration.question.Question;
import com.github.sirblobman.discord.slimy.configuration.question.QuestionConfiguration;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public final class SlashCommandFAQAdmin extends SlashCommand {
    public SlashCommandFAQAdmin(@NotNull SlimyBot discordBot) {
        super(discordBot);
    }

    @Override
    public boolean isEphemeral() {
        return true;
    }

    @Override
    public @NotNull CommandData getCommandData() {
        OptionData id = new OptionData(OptionType.STRING, "id", "The identifier for an FAQ.",
                true, true);
        OptionData plugin = new OptionData(OptionType.STRING, "plugin",
                "The plugin that applies to this question.");
        OptionData question = new OptionData(OptionType.STRING, "question",
                "The question being answered.");
        OptionData answer = new OptionData(OptionType.STRING, "answer", "The answer to the question.");
        OptionData related = new OptionData(OptionType.STRING, "related",
                "A list of related FAQ id values separated by commas.", false);

        OptionData pluginRequired = OptionData.fromData(plugin.toData()).setRequired(true);
        OptionData questionRequired = OptionData.fromData(question.toData()).setRequired(true);
        OptionData answerRequired = OptionData.fromData(answer.toData()).setRequired(true);
        OptionData relatedRequired = OptionData.fromData(related.toData()).setRequired(true);

        SubcommandData[] subcommands = {
                new SubcommandData("add", "Create a new FAQ.")
                        .addOptions(id, pluginRequired, questionRequired, answerRequired, relatedRequired),
                new SubcommandData("delete", "Remove an FAQ.")
                        .addOptions(id),
                new SubcommandData("edit", "Modify an existing FAQ.")
                        .addOptions(id, plugin, question, answer, related)
        };
        return Commands.slash("faq-admin", "Modify and delete FAQ embeds.").addSubcommands(subcommands);
    }

    @Override
    public @NotNull MessageCreateData execute(@NotNull SlashCommandInteractionEvent e) {
        Member member = e.getMember();
        if (member == null) {
            return createError(null, "This command can only be executed in a server.");
        }

        Guild guild = member.getGuild();
        SlimyBot bot = getDiscordBot();
        GuildConfiguration configuration = bot.getGuildConfiguration(guild);
        if (configuration == null) {
            return createError(member, "The bot is not configured for this server.");
        }

        Role supportRole = configuration.getSupportRole(guild);
        if (!member.getRoles().contains(supportRole)) {
            return createError(member, "You do not have access to this command.");
        }

        String sub = e.getSubcommandName();
        if (sub == null) {
            return createError(member, "Missing sub command.");
        }

        return switch (sub) {
            case "add" -> add(member, e);
            case "delete" -> delete(member, e);
            case "edit" -> edit(member, e);
            default -> createError(member, "Invalid sub command '" + sub + "'.");
        };
    }

    @Override
    public void onAutoComplete(@NotNull CommandAutoCompleteInteraction e) {
        AutoCompleteQuery focusedOption = e.getFocusedOption();
        String optionId = focusedOption.getName();
        if (optionId.equals("id")) {
            SlimyBot discordBot = getDiscordBot();
            QuestionConfiguration configuration = discordBot.getQuestionConfiguration();
            Map<String, Question> questionMap = configuration.getQuestions();
            List<Command.Choice> choices = questionMap.keySet().stream()
                    .filter(word -> word.startsWith(e.getFocusedOption().getValue()))
                    .map(word -> new Command.Choice(word, word))
                    .toList();
            e.replyChoices(choices).queue();
        }
    }

    private boolean isQuestion(@NotNull String id) {
        SlimyBot discordBot = getDiscordBot();
        QuestionConfiguration configuration = discordBot.getQuestionConfiguration();
        return configuration.isQuestion(id);
    }

    private @NotNull MessageCreateData add(@NotNull Member member, @NotNull SlashCommandInteractionEvent e) {
        String id = e.getOption("id", OptionMapping::getAsString);
        if (id == null) {
            return createError(member, "Missing option 'id'.");
        }

        if (isQuestion(id)) {
            return createError(member, "An FAQ with id '" + id + "' already exists.");
        }

        String plugin = e.getOption("plugin", OptionMapping::getAsString);
        String question = e.getOption("question", OptionMapping::getAsString);
        String answer = e.getOption("answer", OptionMapping::getAsString);
        String related = e.getOption("related", OptionMapping::getAsString);
        if (plugin == null || question == null || answer == null || related == null) {
            return createError(member, "All options for this command are required.");
        }

        List<String> relatedList = new ArrayList<>();
        Collections.addAll(relatedList, related.split(Pattern.quote(";")));

        Question faq = new Question();
        faq.setPlugin(plugin);
        faq.setQuestion(question);
        faq.setAnswer(answer);
        faq.setRelated(relatedList);

        SlimyBot discordBot = getDiscordBot();
        QuestionConfiguration configuration = discordBot.getQuestionConfiguration();
        configuration.addQuestion(id, faq);
        configuration.saveAll(discordBot);
        return MessageCreateData.fromContent("Successfully added an FAQ.");
    }

    private @NotNull MessageCreateData delete(@NotNull Member member, @NotNull SlashCommandInteractionEvent e) {
        String id = e.getOption("id", OptionMapping::getAsString);
        if (id == null) {
            return createError(member, "Missing option 'id'.");
        }

        if (!isQuestion(id)) {
            return createError(member, "An FAQ with id '" + id + "' does not exist.");
        }

        SlimyBot discordBot = getDiscordBot();
        QuestionConfiguration configuration = discordBot.getQuestionConfiguration();
        configuration.removeQuestion(id);
        configuration.saveAll(discordBot);
        return MessageCreateData.fromContent("Successfully deleted FAQ with id '" + id + "'.");
    }

    private @NotNull MessageCreateData edit(@NotNull Member member, @NotNull SlashCommandInteractionEvent e) {
        String id = e.getOption("id", OptionMapping::getAsString);
        if (id == null) {
            return createError(member, "Missing option 'id'.");
        }

        SlimyBot discordBot = getDiscordBot();
        QuestionConfiguration configuration = discordBot.getQuestionConfiguration();
        Question faq = configuration.getQuestion(id);
        if (faq == null) {
            return createError(member, "An FAQ with id '" + id + "' does not exist.");
        }

        String plugin = e.getOption("plugin", OptionMapping::getAsString);
        String question = e.getOption("question", OptionMapping::getAsString);
        String answer = e.getOption("answer", OptionMapping::getAsString);
        String related = e.getOption("related", OptionMapping::getAsString);
        boolean changes = false;

        if (plugin != null) {
            faq.setPlugin(plugin);
            changes = true;
        }

        if (question != null) {
            faq.setQuestion(question);
            changes = true;
        }

        if (answer != null) {
            faq.setAnswer(answer);
            changes = true;
        }

        if (related != null) {
            List<String> relatedList = new ArrayList<>();
            Collections.addAll(relatedList, related.split(Pattern.quote(";")));
            faq.setRelated(relatedList);
            changes = true;
        }

        if (changes) {
            configuration.saveAll(discordBot);
            return MessageCreateData.fromContent("Successfully edited FAQ with id '" + id + "'.");
        } else {
            return MessageCreateData.fromContent("No changes were made. Make sure to set at least one option.");
        }
    }
}
