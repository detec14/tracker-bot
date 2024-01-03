package com.tracker.objects;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfigDynamic {
    private ArrayList<Target> targets;
    private ArrayList<Tracker> trackers;
    @JsonProperty("target-aeging-minutes")
    private int targetAegingMinutes;
    @JsonProperty("tracker-max-zones")
    private int trackerMaxZoneCount;
    private Server server;

    ConfigDynamic() {
        this.targets = new ArrayList<>();
        this.trackers = new ArrayList<>();
        this.targetAegingMinutes = 6 * 60;
        this.trackerMaxZoneCount = 2;
        this.server = null;
    }

    public ConfigDynamic(Long serverId) {
        this.targets = new ArrayList<>();
        this.trackers = new ArrayList<>();
        this.targetAegingMinutes = 6 * 60;
        this.trackerMaxZoneCount = 2;
        this.server = new Server(serverId);
    }

    public ArrayList<Target> getTargets() {
        return this.targets;
    }

    public ArrayList<Tracker> getTrackers() {
        return this.trackers;
    }

    public void addTarget(Target target) {
        if (this.targets.contains(target)) {
            return;
        }
        this.targets.add(target);
    }

    public void removeTarget(Target target) {
        this.targets.remove(target);
    }

    public void spottedTarget(String name, String system, Tracker tracker) {
        Target target = findTarget(name);
        if (target != null) {
            target.setSpotted(system, tracker.getId());
        }
    }

    public void addTracker(Tracker tracker) {
        if (this.trackers.contains(tracker)) {
            return;
        }
        this.trackers.add(tracker);
    }

    public void removeTracker(Tracker tracker) {
        this.trackers.remove(tracker);
    }

    public int getTargetAegingMinutes() {
        return this.targetAegingMinutes;
    }

    public void setTargetAegingMinutes(int aegingMinutes) {
        this.targetAegingMinutes = aegingMinutes;
    }

    public int getTrackerMaxZoneCount() {
        return this.trackerMaxZoneCount;
    }

    public void setTrackerMaxZoneCount(int trackerMaxZoneCount) {
        this.trackerMaxZoneCount = trackerMaxZoneCount;
    }

    public Server getServer() {
        return this.server;
    }

    public void setEnrolledTracker(Tracker tracker) {
        Tracker t = this.trackers.get(this.trackers.indexOf(tracker));
        if (t != null) {
            t.setEnrolled();
        }
    }

    public void clearEnrolledTracker(Tracker tracker) {
        Tracker t = this.trackers.get(this.trackers.indexOf(tracker));
        if (t != null) {
            t.clearEnrolled();
            t.clearZones();
        }
    }

    public Target findTarget(String name) {
        for (Target target : this.targets) {
            if (target.getName().equals(name)) {
                return target;
            }
        }
        return null;
    }

    public Tracker findTracker(String name) {
        for (Tracker tracker : this.trackers) {
            if (tracker.getName().equals(name)) {
                return tracker;
            }
        }
        return null;
    }
}
