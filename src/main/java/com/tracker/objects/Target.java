package com.tracker.objects;

import java.time.LocalDateTime;

public class Target extends Player {
    private Boolean spotted;
    private LocalDateTime time;
    private String system;
    private Long tracker;

    Target() {
        super();

        this.spotted = false;
        this.time = null;
        this.system = null;
        this.tracker = Long.valueOf(0);
    }

    public Target(String name) {
        super(name);

        this.spotted = false;
        this.time = null;
        this.system = null;
        this.tracker = Long.valueOf(0);
    }

    public Boolean isSpotted() {
        return this.spotted;
    }

    public LocalDateTime getTime() {
        return this.time;
    }

    public String getSystem() {
        return this.system;
    }

    public Long getTracker() {
        return this.tracker;
    }

    public void setSpotted(String system, Long tracker) {
        this.spotted = true;
        this.time = LocalDateTime.now();
        this.system = system;
        this.tracker = tracker;
    }

    public void clearSpotted() {
        this.spotted = false;
        this.time = null;
        this.system = null;
        this.tracker = Long.valueOf(0);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Target)) {
            return false;
        }

        Target target = (Target) obj;

        if (target.getName().equals(this.name)) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
