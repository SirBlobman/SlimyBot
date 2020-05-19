package com.SirBlobman.discord.slimy.command.discord;

import java.awt.*;
import java.io.InputStream;
import java.util.Map;

import com.SirBlobman.discord.slimy.DiscordBot;
import com.SirBlobman.discord.slimy.command.CommandInformation;
import com.SirBlobman.discord.slimy.object.FAQSolution;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

public class DiscordCommandFAQ extends DiscordCommand {
    public DiscordCommandFAQ(DiscordBot discordBot) {
        super(discordBot);
    }
    
    @Override
    public CommandInformation getCommandInformation() {
        return new CommandInformation("faq", "Use this command to get some default responses to questions.", "<id>", "questions", "ask");
    }
    
    @Override
    public boolean hasPermission(Member sender) {
        if(sender == null || sender.isFake()) return false;
    
        Guild guild = sender.getGuild();
        String guildId = guild.getId();
        return guildId.equals("472253228856246299");
    }
    
    @Override
    public void execute(Member sender, TextChannel channel, String label, String[] args) {
        if(args.length < 1) {
            sendErrorEmbed(sender, channel, "Not enough arguments.");
            return;
        }
        
        String questionId = args[0].toLowerCase();
        FAQSolution faqSolution = getSolution(questionId);
        if(faqSolution == null) {
            sendErrorEmbed(sender, channel, "Unknown FAQ '" + questionId + "'.");
            return;
        }
        
        String pluginName = faqSolution.getPluginName();
        String question = faqSolution.getQuestion();
        String answer = faqSolution.getAnswer();
    
        EmbedBuilder builder = getExecutedByEmbed(sender);
        builder.setColor(Color.GREEN);
        builder.setTitle("FAQ");
        builder.setDescription("Question ID: " + questionId);
        if(pluginName != null) builder.addField("Plugin", pluginName, false);
        builder.addField("Question", question, false);
        builder.addField("Answer", answer, false);
    
        MessageEmbed embed = builder.build();
        channel.sendMessage(embed).queue();
    }
    
    private FAQSolution getSolution(String id) {
        Class<?> currentClass = getClass();
        ClassLoader classLoader = currentClass.getClassLoader();
        InputStream stream = classLoader.getResourceAsStream("questions.yml");
        if(stream == null) {
            Logger logger = this.discordBot.getLogger();
            logger.info("'questions.yml' does not exist in the jar!");
            return null;
        }
    
        Yaml yaml = new Yaml();
        Map<String, Map<String, String>> configValues = yaml.load(stream);
        Map<String, String> solutionMap = configValues.getOrDefault(id, null);
        if(solutionMap == null) return null;
        
        String pluginName = solutionMap.getOrDefault("plugin", null);
        String question = solutionMap.getOrDefault("question", null);
        String answer = solutionMap.getOrDefault("answer", null);
        return new FAQSolution(pluginName, question, answer);
    }
}