package com.bot.facebook.template;

import com.core.models.UO;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.util.ArrayList;
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
        final ArrayList<String> properties = new ArrayList<>();
        properties.add(new StrSubstitutor(ImmutableMap.of("id", uo.getId())).replace(id));
        if (StringUtils.isNotBlank(uo.getName()))
            properties.add(new StrSubstitutor(ImmutableMap.of("name", uo.getName())).replace(name));
        if (StringUtils.isNotBlank(uo.getShortName()))
            properties.add(new StrSubstitutor(ImmutableMap.of("shortName", uo.getShortName())).replace(shortName));
        if (StringUtils.isNotBlank(uo.getAddress()))
            properties.add(new StrSubstitutor(ImmutableMap.of("address", uo.getAddress())).replace(address));
        if (StringUtils.isNotBlank(uo.getKved()))
            properties.add(new StrSubstitutor(ImmutableMap.of("kved", uo.getKved())).replace(kved));
        if (StringUtils.isNotBlank(uo.getBoss()))
            properties.add(new StrSubstitutor(ImmutableMap.of("boss", uo.getBoss())).replace(boss));
        if (StringUtils.isNotBlank(uo.getStan()))
            properties.add(new StrSubstitutor(ImmutableMap.of("stan", uo.getStan())).replace(stan));
        if (uo.getFounders() != null && !uo.getFounders().isEmpty()) {
            properties.add(founders);
            properties.add(uo.getFounders().stream().map(f -> new StrSubstitutor(ImmutableMap.of("founder", f)).replace(founder)).collect(Collectors.joining("\n")));
        }
        return properties.stream().collect(Collectors.joining("\n"));
    }
}
