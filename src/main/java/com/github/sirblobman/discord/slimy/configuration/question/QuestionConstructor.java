package com.github.sirblobman.discord.slimy.configuration.question;

import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;

public final class QuestionConstructor extends Constructor {
    public QuestionConstructor() {
        super(new LoaderOptions());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Object constructObject(Node node) {
        Tag tag = node.getTag();
        if (tag == Tag.MAP) {
            Map<String, Object> map = (Map<String, Object>) super.constructObject(node);
            if (map.containsKey("question")) {
                Question question = new Question();
                question.setPlugin((String) map.get("plugin"));
                question.setQuestion((String) map.get("question"));
                question.setAnswer((String) map.get("answer"));
                question.setRelated((List<String>) map.get("related"));
                return question;
            }
        }

        return super.constructObject(node);
    }
}
