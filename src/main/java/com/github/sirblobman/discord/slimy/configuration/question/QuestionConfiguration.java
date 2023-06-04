package com.github.sirblobman.discord.slimy.configuration.question;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class QuestionConfiguration {
    private final Map<String, Question> map;

    public QuestionConfiguration() {
        this.map = new LinkedHashMap<>();
    }

    public @Nullable Question getQuestion(@NotNull String id) {
        return this.map.get(id);
    }

    public void addQuestion(@NotNull String id, @NotNull Question question) {
        this.map.put(id, question);
    }

    public @NotNull Map<String, Question> getQuestions() {
        return Collections.unmodifiableMap(this.map);
    }
}
