package com.spark.models;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @author Taras Zubrei
 */
public class Record implements Serializable{
    private Long id;
    private String name;
    private String shortName;
    private String address;
    private String boss;
    private String kved;
    private String stan;
    private List<String> founders;

    public Record() {
    }

    public Record(Long id, String name, String shortName, String address, String boss, String kved, String stan, List<String> founders) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.address = address;
        this.boss = boss;
        this.kved = kved;
        this.stan = stan;
        this.founders = founders;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
        if (!(o instanceof Record)) return false;
        Record record = (Record) o;
        return Objects.equals(id, record.id) &&
                Objects.equals(name, record.name) &&
                Objects.equals(shortName, record.shortName) &&
                Objects.equals(address, record.address) &&
                Objects.equals(boss, record.boss) &&
                Objects.equals(kved, record.kved) &&
                Objects.equals(stan, record.stan) &&
                Objects.equals(founders, record.founders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, shortName, address, boss, kved, stan, founders);
    }

    @Override
    public String toString() {
        return "Record{" +
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
