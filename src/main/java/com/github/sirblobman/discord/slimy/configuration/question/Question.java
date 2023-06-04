package com.github.sirblobman.discord.slimy.configuration.question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Question {
    private String plugin;
    private String question;
    private String answer;
    private final List<String> related;

    public Question() {
        this.plugin = null;
        this.question = "";
        this.answer = "";
        this.related = new ArrayList<>();
    }

    public @Nullable String getPlugin() {
        return this.plugin;
    }

    public void setPlugin(@Nullable String plugin) {
        this.plugin = plugin;
    }

    public @NotNull String getQuestion() {
        return this.question;
    }

    public void setQuestion(@NotNull String question) {
        if (question.isBlank()) {
            throw new IllegalStateException("question must not be blank.");
        }

        this.question = question;
    }

    public @NotNull String getAnswer() {
        return this.answer;
    }

    public void setAnswer(@NotNull String answer) {
        if (answer.isBlank()) {
            throw new IllegalStateException("answer must not be blank.");
        }

        this.answer = answer;
    }

    public @NotNull List<String> getRelated() {
        return Collections.unmodifiableList(this.related);
    }

    public void setRelated(@NotNull List<String> related) {
        this.related.clear();
        this.related.addAll(related);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }

        if (!(object instanceof Question other)) {
            return false;
        }

        return Objects.equals(this.plugin, other.plugin) && Objects.equals(this.question, other.question) && Objects.equals(this.answer, other.answer) && this.related.equals(other.related);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(this.plugin, this.question, this.answer);
        return ((31 * result) + this.related.hashCode());
    }

    @Override
    public @NotNull String toString() {
        String format = "Question{plugin=%s,question=%s,answer=%s,related=%s}";
        return String.format(Locale.US, format, this.plugin, this.question, this.answer, this.related);
    }
}
