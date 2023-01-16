package com.github.sirblobman.discord.slimy.data;

import java.util.Arrays;
import java.util.Objects;

public record FAQSolution(String pluginName, String question, String answer, String... related) {

    public FAQSolution(String pluginName, String question, String answer, String... related) {
        this.pluginName = pluginName;
        this.question = Objects.requireNonNull(question, "question must not be null!");
        this.answer = Objects.requireNonNull(answer, "answer must not be null!");
        this.related = Objects.requireNonNull(related, "related must not be null!");
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof FAQSolution solution)) return false;
        return Objects.equals(pluginName, solution.pluginName) && question.equals(solution.question) && answer.equals(solution.answer) && Arrays.equals(related, solution.related);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(pluginName, question, answer);
        result = 31 * result + Arrays.hashCode(related);
        return result;
    }

    @Override
    public String toString() {
        return "FAQSolution{" + "pluginName='" + pluginName + '\'' + ", question='" + question + '\'' + ", answer='" + answer + '\'' + ", related=" + Arrays.toString(related) + '}';
    }
}
