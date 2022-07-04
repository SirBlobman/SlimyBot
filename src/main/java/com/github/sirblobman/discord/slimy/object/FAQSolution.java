package com.github.sirblobman.discord.slimy.object;

import java.util.Objects;

public record FAQSolution(String pluginName, String question, String answer) {
    public FAQSolution(String pluginName, String question, String answer) {
        this.pluginName = pluginName;
        this.question = Objects.requireNonNull(question, "question must not be null!");
        this.answer = Objects.requireNonNull(answer, "answer must not be null!");
    }
}
