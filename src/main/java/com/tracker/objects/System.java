package com.tracker.objects;

public class System {
    private String name;
    private int id;
    private int warp;

    System() {
        this.name = null;
        this.id = 0;
        this.warp = 0;
    }

    public String getName() {
        return this.name;
    }

    public int getId() {
        return this.id;
    }

    public int getWarp() {
        return this.warp;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
