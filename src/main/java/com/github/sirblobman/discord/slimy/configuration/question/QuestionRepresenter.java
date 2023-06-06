package com.github.sirblobman.discord.slimy.configuration.question;

import java.util.Set;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

public final class QuestionRepresenter extends Representer {
    public QuestionRepresenter() {
        super(new DumperOptions());
    }

    @Override
    protected MappingNode representJavaBean(Set<Property> properties, Object javaBean) {
        if (javaBean instanceof Question) {
            addClassTag(javaBean.getClass(), Tag.MAP);
        }

        return super.representJavaBean(properties, javaBean);
    }
}
