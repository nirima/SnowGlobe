package com.nirima.snowglobe.web.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by magnayn on 26/04/2017.
 */
public class Globe implements Serializable {
    public String id;
    public String type;

    public String name;
    public String description;

    public Date lastUpdate;
    public Date created;

    public List<String> tags = new ArrayList<>();

    public List<String> configFiles = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Globe globe = (Globe) o;
        return Objects.equals(id, globe.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
