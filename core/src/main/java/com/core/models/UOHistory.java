package com.core.models;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Taras Zubrei
 */
@Entity("uo")
public class UOHistory {
    @Id
    private String id;
    private final List<UO> data = new ArrayList<>();

    public UOHistory() {
    }

    public UOHistory(String id, Iterable<UO> data) {
        this.id = id;
        data.forEach(this.data::add);
    }

    public UOHistory(UO record) {
        this.id = record.getId();
        data.add(record);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<UO> getData() {
        return data;
    }

    public UOHistory addUO(UO record) {
        data.add(record);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UOHistory)) return false;
        UOHistory uoHistory = (UOHistory) o;
        return Objects.equals(id, uoHistory.id) &&
                Objects.equals(data, uoHistory.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, data);
    }

    @Override
    public String toString() {
        return "UOHistory{" +
                "id='" + id + '\'' +
                ", data=" + data +
                '}';
    }
}
