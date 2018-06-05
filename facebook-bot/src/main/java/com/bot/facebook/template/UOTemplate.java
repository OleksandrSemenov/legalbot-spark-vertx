package com.bot.facebook.template;

import com.core.models.UO;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Taras Zubrei
 */
public class UOTemplate {
    private final String id;
    private final String name;
    private final String shortName;
    private final String address;
    private final String boss;
    private final String kved;
    private final String stan;
    private final String founder;
    private final String founders;


    public UOTemplate(String id, String name, String shortName, String address, String boss, String kved, String stan, String founder, String founders) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.address = address;
        this.boss = boss;
        this.kved = kved;
        this.stan = stan;
        this.founder = founder;
        this.founders = founders;
    }

    public String replace(UO uo) {
        final List<String> properties = new ArrayList<>();
        add(properties, new StrSubstitutor(ImmutableMap.of("id", uo.getId())).replace(id));
        if (StringUtils.isNotBlank(uo.getName()))
            add(properties, new StrSubstitutor(ImmutableMap.of("name", uo.getName())).replace(name));
        if (StringUtils.isNotBlank(uo.getShortName()))
            add(properties, new StrSubstitutor(ImmutableMap.of("shortName", uo.getShortName())).replace(shortName));
        if (StringUtils.isNotBlank(uo.getAddress()))
            add(properties, new StrSubstitutor(ImmutableMap.of("address", uo.getAddress())).replace(address));
        if (StringUtils.isNotBlank(uo.getKved()))
            add(properties, new StrSubstitutor(ImmutableMap.of("kved", uo.getKved())).replace(kved));
        if (StringUtils.isNotBlank(uo.getBoss()))
            add(properties, new StrSubstitutor(ImmutableMap.of("boss", uo.getBoss())).replace(boss));
        if (StringUtils.isNotBlank(uo.getStan()))
            add(properties, new StrSubstitutor(ImmutableMap.of("stan", uo.getStan())).replace(stan));
        if (uo.getFounders() != null && !uo.getFounders().isEmpty()) {
            add(properties, founders);
            uo.getFounders().stream().map(f -> new StrSubstitutor(ImmutableMap.of("founder", f)).replace(founder))
                    .forEach(text -> add(properties, text));
        }
        return properties.stream().collect(Collectors.joining("\n"));
    }

    private void add(List<String> properties, String line) {
        int size = properties.stream().mapToInt(String::length).sum() + line.length() + properties.size() * "\n".length();
        int start = properties.stream().collect(Collectors.joining("\n")).lastIndexOf('|');
        if (size - start >= 2000)
            properties.add("|" + line);
        else
            properties.add(line);
    }
}
