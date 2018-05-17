package com.core.models;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author Taras Zubrei
 */
public class FOP {
    private String name;
    private String address;
    private String kved;
    private String stan;

    public FOP() {
    }

    public FOP(String name, String address, String kved, String stan) {
        if (StringUtils.isNotBlank(name)) this.name = name.trim();
        if (StringUtils.isNotBlank(address)) this.address = address.trim();
        if (StringUtils.isNotBlank(kved)) this.kved = kved.trim();
        if (StringUtils.isNotBlank(stan)) this.stan = stan.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FOP)) return false;
        FOP fop = (FOP) o;
        return Objects.equals(name, fop.name) &&
                Objects.equals(address, fop.address) &&
                Objects.equals(kved, fop.kved) &&
                Objects.equals(stan, fop.stan);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address, kved, stan);
    }

    @Override
    public String toString() {
        return "FOP{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", kved='" + kved + '\'' +
                ", stan='" + stan + '\'' +
                '}';
    }
}
