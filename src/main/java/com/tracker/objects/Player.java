package com.tracker.objects;

public class Player {
    protected String name;
    protected String alliance;
    protected int warp;

    Player() {
        this.name = null;
        this.alliance = null;
        this.warp = 0;
    }

    protected Player(String name) {
        this.name = name;
        this.alliance = null;
        this.warp = 0;
    }

    public String getName() {
        return this.name;
    }

    public String getAlliance() {
        return this.alliance;
    }

    public void setAlliance(String alliance) {
        this.alliance = alliance;
    }

    public int getWarp() {
        return this.warp;
    }

    public void setWarp(int warp) {
        this.warp = warp;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
