package com.tracker.objects;

import java.util.ArrayList;
import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Zone {
    private String name;
    @JsonProperty("name-map")
    private String nameMap;
    private ArrayList<System> systems;
    private Space space;

    Zone() {
        this.name = null;
        this.nameMap = null;
        this.systems = new ArrayList<>();
        this.space = Space.Unknown;
    }

    public String getName() {
        return this.name;
    }

    public String getNameMap() {
        return this.nameMap;
    }
    
    public ArrayList<System> getSystems() {
        return this.systems;
    }

    public Space getSpace() {
        return this.space;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Zone)) {
            return false;
        }

        Zone zone = (Zone) obj;

        if (zone.getName().equals(this.name) && zone.getSpace().equals(this.space)) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", this.name, this.space);
    }

    public int getWarp() {
        return this.systems.stream().max(Comparator.comparing(System::getWarp)).get().getWarp();
    }
}
