package com.spark.models;

import org.apache.spark.sql.Row;
import reactor.core.support.UUIDUtils;

import java.util.Objects;

/**
 * @author Taras Zubrei
 */
public class FOP {
    private String id;
    private String name;
    private String address;
    private String kved;
    private String stan;

    public FOP() {
        this.id = UUIDUtils.create().toString();
    }

    public FOP(String name, String address, String kved, String stan) {
        this();
        this.name = name;
        this.address = address;
        this.kved = kved;
        this.stan = stan;
    }

    public static FOP fromXml(Row row) {
        return new FOP(
                row.getString(0),
                row.getString(1),
                row.getString(2),
                row.getString(3)
        );
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
        return Objects.equals(id, fop.id) &&
                Objects.equals(name, fop.name) &&
                Objects.equals(address, fop.address) &&
                Objects.equals(kved, fop.kved) &&
                Objects.equals(stan, fop.stan);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, address, kved, stan);
    }

    @Override
    public String toString() {
        return "FOP{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", kved='" + kved + '\'' +
                ", stan='" + stan + '\'' +
                '}';
    }
}
