package de.peachbiscuit174.peachpaperlib.api.managers;

import de.peachbiscuit174.peachpaperlib.PeachPaperLib;
import de.peachbiscuit174.peachpaperlib.scheduler.LibraryScheduler;

public class SchedulerManager {

    private final LibraryScheduler scheduler = new LibraryScheduler(PeachPaperLib.getPlugin());

    public LibraryScheduler getScheduler() {
        return scheduler;
    }
}
