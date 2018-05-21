package com.core.models;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @author Taras Zubrei
 */
public class UO implements Serializable {
    private String id;
    private String name;
    private String shortName;
    private String address;
    private String boss;
    private String kved;
    private String stan;
    private List<String> founders;

    public UO() {
    }

    public UO(String id, String name, String shortName, String address, String boss, String kved, String stan, List<String> founders) {
        this.id = id;
        if (StringUtils.isNotBlank(name)) this.name = name.trim();
        if (StringUtils.isNotBlank(shortName)) this.shortName = shortName.trim();
        if (StringUtils.isNotBlank(address)) this.address = address.trim();
        if (StringUtils.isNotBlank(boss)) this.boss = boss.trim();
        if (StringUtils.isNotBlank(kved)) this.kved = kved.trim();
        if (StringUtils.isNotBlank(stan)) this.stan = stan.trim();
        this.founders = founders;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBoss() {
        return boss;
    }

    public void setBoss(String boss) {
        this.boss = boss;
    }

    public String getKved() {
        return kved;
    }

    public void setKved(String kved) {
        this.kved = kved;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public List<String> getFounders() {
        return founders;
    }

    public void setFounders(List<String> founders) {
        this.founders = founders;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UO)) return false;
        UO uo = (UO) o;
        return Objects.equals(id, uo.id) &&
                Objects.equals(name, uo.name) &&
                Objects.equals(shortName, uo.shortName) &&
                Objects.equals(address, uo.address) &&
                Objects.equals(boss, uo.boss) &&
                Objects.equals(kved, uo.kved) &&
                Objects.equals(stan, uo.stan) &&
                Objects.equals(founders, uo.founders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, shortName, address, boss, kved, stan, founders);
    }

    @Override
    public String toString() {
        return "UO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", shortName='" + shortName + '\'' +
                ", address='" + address + '\'' +
                ", boss='" + boss + '\'' +
                ", kved='" + kved + '\'' +
                ", stan='" + stan + '\'' +
                ", founders=" + founders +
                '}';
    }
}
