package com.tracker.objects;

import java.time.LocalDateTime;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Tracker extends Player {
    private Long id;
    private Boolean enrolled;
    @JsonProperty("max-runs-per-day")
    private int maxRunsPerDay;
    private int ops;
    @JsonProperty("request-time")
    private LocalDateTime requestTime;
    private ArrayList<String> zones;
    @JsonProperty("reports")
    private int reportCount;

    Tracker() {
        super();

        this.id = Long.valueOf(0);
        this.enrolled = false;
        this.maxRunsPerDay = 2;
        this.ops = 0;
        this.requestTime = null;
        this.zones = new ArrayList<>();
        this.reportCount = 0;
    }

    public Tracker(Long id, String name) {
        super(name);

        this.id = id;
        this.enrolled = false;
        this.maxRunsPerDay = 2;
        this.ops = 0;
        this.requestTime = null;
        this.zones = new ArrayList<>();
        this.reportCount = 0;
    }

    public Long getId() {
        return this.id;
    }

    public Boolean isEnrolled() {
        return this.enrolled;
    }

    public void setEnrolled() {
        this.enrolled = true;
    }

    public void clearEnrolled() {
        this.enrolled = false;
    }

    public int getMaxRunsPerDay() {
        return this.maxRunsPerDay;
    }

    public void setMaxRunsPerDay(int maxRunsPerDay) {
        this.maxRunsPerDay = maxRunsPerDay;
    }

    public int getOps() {
        return this.ops;
    }

    public void setOps(int ops) {
        this.ops = ops;
    }

    public LocalDateTime getRequestTime() {
        return this.requestTime;
    }

    public void updateRequestTime() {
        this.requestTime = LocalDateTime.now();
    }

    public ArrayList<String> getZones() {
        return this.zones;
    }

    public void addZone(String zone) {
        if (this.zones.contains(zone)) {
            return;
        }
        this.zones.add(zone);
    }

    public void removeZone(String zone) {
        this.zones.remove(zone);
    }

    public void clearZones() {
        this.zones.clear();
    }

    public int getReportCount() {
        return reportCount;
    }

    public void incrementReportCount() {
        this.reportCount++;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Tracker)) {
            return false;
        }

        Tracker tracker = (Tracker) obj;

        if (tracker.getId().equals(this.id) && tracker.getName().equals(this.name)) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
