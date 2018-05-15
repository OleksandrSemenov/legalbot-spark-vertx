package com.spark.models;

import java.util.List;
import java.util.Objects;

/**
 * @author Taras Zubrei
 */
@Event
public class UOUpdate {
    private final Long id;
    private final List<UO> previous;
    private final List<UO> actual;

    public UOUpdate(Long id, List<UO> previous, List<UO> actual) {
        this.id = id;
        this.previous = previous;
        this.actual = actual;
    }

    public Long getId() {
        return id;
    }

    public List<UO> getPrevious() {
        return previous;
    }

    public List<UO> getActual() {
        return actual;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UOUpdate)) return false;
        UOUpdate uoUpdate = (UOUpdate) o;
        return Objects.equals(id, uoUpdate.id) &&
                Objects.equals(previous, uoUpdate.previous) &&
                Objects.equals(actual, uoUpdate.actual);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, previous, actual);
    }

    @Override
    public String toString() {
        return "UOUpdate{" +
                "id=" + id +
                ", previous=" + previous +
                ", actual=" + actual +
                '}';
    }
}
