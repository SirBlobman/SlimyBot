package com.github.sirblobman.discord.slimy.object;

import java.util.Objects;

public final class FAQSolution {
    private final String pluginName, question, answer;

    public FAQSolution(String pluginName, String question, String answer) {
        this.pluginName = pluginName;
        this.question = Objects.requireNonNull(question, "question must not be null!");
        this.answer = Objects.requireNonNull(answer, "answer must not be null!");
    }
    
    public String getPluginName() {
        return this.pluginName;
    }
    
    public String getQuestion() {
        return this.question;
    }
    
    public String getAnswer() {
        return this.answer;
    }
}
