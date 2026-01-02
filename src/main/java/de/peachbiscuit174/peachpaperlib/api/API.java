package de.peachbiscuit174.peachpaperlib.api;

import de.peachbiscuit174.peachpaperlib.api.managers.ItemsManager;
import de.peachbiscuit174.peachpaperlib.api.managers.SchedulerManager;

public class API {
    private static final ItemsManager itemsManager = new ItemsManager();

    public static ItemsManager getItemsManager() {
        return itemsManager;
    }

    private static final SchedulerManager schedulerManager = new SchedulerManager();

    public static SchedulerManager getSchedulerManager() {
        return schedulerManager;
    }
}