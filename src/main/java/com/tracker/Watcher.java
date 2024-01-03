package com.tracker;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.tracker.commands.DisplayCommand;
import com.tracker.objects.Config;
import com.tracker.objects.Target;
import com.tracker.objects.Tracker;
import com.tracker.objects.Zone;

import net.dv8tion.jda.api.entities.Guild;

public class Watcher implements Runnable {
    private final AtomicBoolean running;
    private Thread worker;
    private Config config;
    private Guild server;

    public Watcher(Config config, Guild server) {
        this.running = new AtomicBoolean(false);
        this.config = config;
        this.server = server;
    }

    public void start() {
        this.worker = new Thread(this);
        this.worker.start();
    }

    public void stop() {
        this.running.set(false);
        this.worker.interrupt();
    }

    @Override
    public void run() {
        this.running.set(true);
        
        while (this.running.get()) {
            try {
                Thread.sleep(1000);

                Boolean save = Stream.of(
                    refreshTargets(), requestAction(), assignZones())
                    .reduce(false, Boolean::logicalOr);
                
                if (save == true) {
                    this.config.save();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private Boolean refreshTargets() {
        Calendar calendar = Calendar.getInstance();
        Boolean save = false;

        for (Target target : this.config.getDynamic().getTargets()) {
            if (target.isSpotted()) {
                calendar.setTimeInMillis(target.getTime().getTime());
                calendar.add(Calendar.MINUTE, this.config.getDynamic().getTargetAegingMinutes());

                if (Timestamp.from(Instant.now()).after(new Timestamp(calendar.getTimeInMillis()))) {
                    target.clearSpotted();
                    save = true;
                }
            }
        }

        return save;
    }

    private Boolean requestAction() {
        Calendar calendar = Calendar.getInstance();
        Timestamp now = Timestamp.from(Instant.now());
        ArrayList<Tracker> list = new ArrayList<>();

        for (Tracker tracker : this.config.getDynamic().getTrackers()) {
            if (tracker.getRequestTime() == null) {
                list.add(tracker);
                continue;
            }

            calendar.setTimeInMillis(tracker.getRequestTime().getTime());
            calendar.add(Calendar.HOUR, 24 / tracker.getMaxRunsPerDay());

            if (now.after(new Timestamp(calendar.getTimeInMillis()))) {
                list.add(tracker);
            }
        }

        if (list.isEmpty()) {
            return false;
        }

        String msg = "";
        
        msg += "Hello guys!\nIt's time for tracking. Are you ready?\n";
        msg += "Acknowledge your participation with ✅, reject with ❌ on this message.\n\n";
        for (Tracker tracker : list) {
            msg += this.server.retrieveMemberById(tracker.getId()).complete().getAsMention() + "\n";
            tracker.updateRequestTime();
            tracker.clearZones();
            tracker.clearEnrolled();
        }

        this.server.getTextChannelById(this.config.getDynamic().getServer().getChannel())
               .sendMessage(msg).queue();

        return true;
    }

    private Boolean assignZones() {
        ArrayList<Zone> freeZones = new ArrayList<>(this.config.getStatic().getZones());
        ArrayList<Tracker> freeTrackers = new ArrayList<>();

        for (Tracker tracker : this.config.getDynamic().getTrackers()) {
            if (tracker.isEnrolled() == false) {
                continue;
            } else if (tracker.getZones().isEmpty()) {
                freeTrackers.add(tracker);
            } else {
                for (String zone : tracker.getZones()) {
                    freeZones.remove(this.config.getStatic().findZone(zone));
                }
            }
        }

        if (freeTrackers.isEmpty() || freeZones.isEmpty()) {
            return false;
        }

        int zonesPerTracker = Integer.min(freeZones.size() / freeTrackers.size(),
            this.config.getDynamic().getTrackerMaxZoneCount());
        int i = 0;

        Collections.shuffle(freeZones);

        for (Tracker tracker : freeTrackers) {
            ArrayList<String> mapNames = this.config.getStatic().getAllZoneMapNames();
            String zones = "";

            for (Zone zone : freeZones.subList(i, i + zonesPerTracker)) {
                tracker.addZone(zone.getName());

                zones += "- " + zone.getName() + " (" + zone.getNameMap().toUpperCase() + "): ";
                zones += zone.getSystems().stream().map(Object::toString)
                    .collect(Collectors.joining(", "));
                zones += "\n";

                mapNames.remove(zone.getNameMap());
            }
            i += zonesPerTracker;
            
            String url = DisplayCommand.getMapUrl() + "/?zone=";
            url += mapNames.stream().map(Object::toString)
                .collect(Collectors.joining("&zone="));
            String msg = this.server.retrieveMemberById(tracker.getId()).complete().getAsMention();
            msg += ", your assigned zones/systems:\n" + zones;
            msg += "\nHere the systems map with highlighted zones: " + url;

            this.server.getTextChannelById(this.config.getDynamic().getServer().getChannel())
                .sendMessage(msg).queue();
        }

        return true;
    }
}
