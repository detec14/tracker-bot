package com.tracker.objects;

public class Server {
    private Long id;
    private Long channel;

    Server() {
        this.id = Long.valueOf(0);
        this.channel = Long.valueOf(0);
    }

    public Server(Long id) {
        this.id = id;
        this.channel = Long.valueOf(0);
    }

    public long getId() {
        return this.id;
    }

    public Long getChannel() {
        return this.channel;
    }

    public void setChannel(Long channel) {
        this.channel = channel;
    }
}
