package com.tracker.objects;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ConfigStatic {
    private ArrayList<Zone> zones;

    ConfigStatic() {
        this.zones = new ArrayList<>();
    }

    public ArrayList<Zone> getZones() {
        return this.zones;
    }

    public Zone findZone(String name) {
        for (Zone zone : this.zones) {
            if (zone.getName().equals(name)) {
                return zone;
            }
        }
        return null;
    }

    public ArrayList<System> getAllSystems() {
        ArrayList<System> systems = new ArrayList<>();

        for (Zone zone : this.zones) {
            systems.addAll(zone.getSystems());
        }

        return systems;
    }

    public ArrayList<String> getAllZoneMapNames() {
        return new ArrayList<>(this.zones.stream()
            .map(Zone::getNameMap).collect(Collectors.toList())
            .stream().distinct().collect(Collectors.toList()));
    }
}
