package com.github.sirblobman.discord.slimy.configuration.question;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.sirblobman.discord.slimy.SlimyBot;

import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

public final class QuestionConfiguration {
    private final Map<String, Question> map;

    public QuestionConfiguration() {
        this.map = new LinkedHashMap<>();
    }

    public @NotNull Map<String, Question> getQuestions() {
        return Collections.unmodifiableMap(this.map);
    }

    public boolean isQuestion(@NotNull String id) {
        return this.map.containsKey(id);
    }

    public @Nullable Question getQuestion(@NotNull String id) {
        return this.map.get(id);
    }

    public void addQuestion(@NotNull String id, @NotNull Question question) {
        this.map.put(id, question);
    }
    public void removeQuestion(@NotNull String id) {
        this.map.remove(id);
    }

    public void saveAll(@NotNull SlimyBot bot) {
        Yaml yaml = new Yaml(new QuestionRepresenter());
        Path path = Path.of("questions.yml");
        try(BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING)) {
            yaml.dump(this.map, writer);
        } catch (IOException | SecurityException ex) {
            Logger logger = bot.getLogger();
            logger.error("Failed to write to questions.yml file.");
        }
    }
}
