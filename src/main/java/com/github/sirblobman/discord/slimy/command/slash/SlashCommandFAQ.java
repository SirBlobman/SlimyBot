package com.github.sirblobman.discord.slimy.command.slash;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.data.FAQSolution;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

public final class SlashCommandFAQ extends SlashCommand {

    private final Map<String, FAQSolution> solutionMap;

    public SlashCommandFAQ(DiscordBot discordBot) {
        super(discordBot, "faq");
        this.solutionMap = loadFaq();
    }

    @Override
    public CommandData getCommandData() {
        String commandName = getCommandName();
        return Commands.slash(commandName, "Get some default answers to common questions.")
                .addOption(OptionType.STRING, "id", "The ID of the question.", true, true);
    }

    @Override
    public MessageCreateData execute(SlashCommandInteractionEvent e) {
        Member sender = e.getMember();
        OptionMapping questionIdOption = e.getOption("id");
        if (questionIdOption == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(sender);
            errorEmbed.addField("Error", "Missing Argument 'id'.", false);
            return getMessage(errorEmbed);
        }

        String questionId = questionIdOption.getAsString();
        try {
            FAQSolution solution = getSolution(questionId);
            EmbedBuilder builder = getEmbed(sender, solution, questionId);
            return getMessage(builder);
        } catch (Exception ex) {
            ex.printStackTrace();
            EmbedBuilder errorEmbed = getErrorEmbed(sender);
            errorEmbed.addField("Error", ex.getMessage(), false);
            return getMessage(errorEmbed);
        }
    }

    private Map<String, FAQSolution> loadFaq() {
        File file = new File("questions.yml");
        if (!file.exists()) {
            throw new IllegalStateException("'questions.yml' does not exist!");
        }

        try {
            FileInputStream inputStream = new FileInputStream(file);
            Map<String, Map<String, String>> basicSolutionMap = new Yaml().load(inputStream);

            Map<String, FAQSolution> solutionMap = new HashMap<>(basicSolutionMap.size(), 1);

            basicSolutionMap.forEach((solutionId, map) -> {
                String plugin = map.get("plugin");
                String question = map.get("question");
                String answer = map.get("answer");

                FAQSolution solution = new FAQSolution(plugin, question, answer);
                solutionMap.put(solutionId, solution);
            });

            return solutionMap;
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private FAQSolution getSolution(String questionId) {
        if(this.solutionMap == null) throw new IllegalStateException("Could not find a solution with id '" + questionId + "'.");
        FAQSolution faqSolution = this.solutionMap.get(questionId);
        if(faqSolution == null) throw new IllegalStateException("Could not find a solution with id '" + questionId + "'.");

        return faqSolution;
    }

    private EmbedBuilder getEmbed(Member sender, FAQSolution solution, String questionId) {
        Objects.requireNonNull(solution, "sender must not be null!");
        Objects.requireNonNull(solution, "solution must not be null!");

        EmbedBuilder builder = getExecutedByEmbed(sender);
        builder.setColor(Color.GREEN);
        builder.setTitle("FAQ");
        builder.setDescription("Question ID: " + questionId);

        String pluginName = solution.pluginName();
        if (pluginName != null) {
            builder.addField("Plugin", pluginName, false);
        }

        String question = solution.question();
        builder.addField("Question", question, false);

        String answer = solution.answer();
        builder.addField("Answer", answer, false);

        return builder;
    }

    @Override
    public void onAutoComplete(final @NotNull CommandAutoCompleteInteraction event) {
        List<Command.Choice> choices = this.solutionMap.keySet()
            .stream()
            .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
            .map(word -> new Command.Choice(word, word))
            .toList();

        event.replyChoices(choices).queue();
    }
}
