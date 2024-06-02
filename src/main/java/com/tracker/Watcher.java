package com.tracker;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
    enum RequestMode {
        FIXED,
        DYNAMIC
    }

    private final AtomicBoolean running;
    private LocalTime lastTrackerReqTime;
    private Thread worker;
    private Config config;
    private Guild server;

    public Watcher(Config config, Guild server) {
        this.running = new AtomicBoolean(false);
        this.lastTrackerReqTime = LocalTime.of(0, 0, 0);
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
            } catch (Exception e) {
                System.out.println("Exception in Watcher thread (" + 
                    e.getMessage() + ")");
            }
        }
    }

    private Boolean refreshTargets() {
        Boolean save = false;

        for (Target target : this.config.getDynamic().getTargets()) {
            if (target.isSpotted()) {
                LocalDateTime aeging = target.getTime().plusMinutes(
                    this.config.getDynamic().getTargetAegingMinutes());

                if (LocalDateTime.now().isAfter(aeging)) {
                    target.clearSpotted();
                    save = true;
                }
            }
        }

        return save;
    }

    private Boolean requestRequired(Tracker tracker, RequestMode mode) {
        switch (mode) {
        case DYNAMIC:
            if (tracker.getRequestTime() == null) {
                return true;
            }

            LocalDateTime next = tracker.getRequestTime().plusHours(
                24 / tracker.getMaxRunsPerDay());

            if (LocalDateTime.now().isAfter(next)) {
                return true;
            }

            break;

        case FIXED:
            final ArrayList<LocalTime> requestTimes = new ArrayList<>() {{
                add(LocalTime.parse("10:00:00"));
                add(LocalTime.parse("18:00:00"));
            }};
            LocalTime current = LocalTime.now(ZoneId.of("Europe/Berlin"));

            for (LocalTime time : requestTimes) {
                if (current.isAfter(time.minusMinutes(1)) && current.isBefore(time.plusMinutes(1))) {
                    return true;
                }
            }

            break;
        }

        return false;
    }

    private Boolean requestAction() {
        ArrayList<Tracker> list = new ArrayList<>();
        RequestMode mode = RequestMode.FIXED;
        LocalTime now = LocalTime.now(ZoneId.of("Europe/Berlin"));

        for (Tracker tracker : this.config.getDynamic().getTrackers()) {
            if (requestRequired(tracker, mode)) {
                list.add(tracker);
            }
        }

        if (list.isEmpty()) {
            return false;
        } else if ((mode == RequestMode.FIXED) && (now.isBefore(this.lastTrackerReqTime.plusMinutes(15)))) {
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

        this.lastTrackerReqTime = now;

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

        Collections.shuffle(freeZones);

        for (Tracker tracker : freeTrackers) {
            ArrayList<String> mapNames = new ArrayList<>();
            String zones = "";

            for (int i = 0; i < freeZones.size(); i++) {
                if (tracker.getZones().size() >= zonesPerTracker) {
                    break;
                }

                Zone zone = freeZones.get(i);

                if ((tracker.getWarp() != 0) && (tracker.getWarp() < zone.getWarp())) {
                    continue;
                }

                tracker.addZone(zone.getName());

                zones += "- " + zone.getName() + " (" + zone.getNameMap().toUpperCase() + "): ";
                zones += zone.getSystems().stream().map(Object::toString)
                    .collect(Collectors.joining(", "));
                zones += "\n";

                mapNames.add(zone.getNameMap());
                freeZones.remove(i);
                --i;
            }

            String msg = "";

            if (tracker.getZones().isEmpty()) {
                msg = "Either all zones were already assigned or your warp doesn't fit any free zone!\n";
                msg += "You will be considered again on the next round. Enjoy your free time ;-)";
            } else {
                String url = DisplayCommand.generateMapPictureUrl(mapNames);
                msg = this.server.retrieveMemberById(tracker.getId()).complete().getAsMention();
                msg += ", your assigned zones/systems:\n" + zones;
                
                if (url != null) {
                    msg += "\nHere the systems map with highlighted zones:\n" + url;
                }
            }

            this.server.getTextChannelById(this.config.getDynamic().getServer().getChannel())
                .sendMessage(msg).queue();
        }

        return true;
    }
}
