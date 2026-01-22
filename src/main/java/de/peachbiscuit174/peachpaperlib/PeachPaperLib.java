package de.peachbiscuit174.peachpaperlib;

import de.peachbiscuit174.peachpaperlib.other.HolidayGreetingListener;
import de.peachbiscuit174.peachpaperlib.scheduler.LibraryScheduler;
import de.peachbiscuit174.peachpaperlib.updatecheck.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class of the PeachPaperLib.
 * Handles the initialization of all managers, metrics, and the update system.
 * @author peachbiscuit174
 * @since 1.0.0
 */
public final class PeachPaperLib extends JavaPlugin {
    private static Plugin plugin;
    private static UpdateChecker updateChecker;
    private static LibraryScheduler scheduler;
    private Metrics metrics;

    public static LibraryScheduler getScheduler() {
        return scheduler;
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;

        int pluginId = 28644;
        metrics = new Metrics(this, pluginId);

        updateChecker = new UpdateChecker(this);
        updateChecker.bootstrap();
        getServer().getPluginManager().registerEvents(new HolidayGreetingListener(), this);
        scheduler = new LibraryScheduler(plugin);

        getLogger().info("----------------------------------");
        getLogger().info("PeachPaperLib has been loaded successfully.");
        getLogger().info("Version: " + getPluginMeta().getVersion());
        getLogger().info("Developed by PeachBiscuit174");
        getLogger().info("Thank you for using PeachPaperLib (PPL) :D");
        getLogger().info("----------------------------------");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (scheduler != null) {
            scheduler.shutdown();
        }

        scheduler = null;
        updateChecker = null;
        metrics = null;
        getLogger().info("PeachPaperLib disabled.");
    }
}
