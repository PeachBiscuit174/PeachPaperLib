package de.peachbiscuit174.peachpaperlib.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * A high-performance, resource-efficient scheduler for Minecraft libraries.
 * Manages independent thread pools and provides centralized synchronization
 * with the Bukkit main thread.
 *
 * @author peachbiscuit174
 * @since 1.0.0
 */
public class LibraryScheduler {

    private final Plugin libraryOwner;
    private final ThreadPoolExecutor asyncExecutor;
    private final ScheduledExecutorService timerService;
    private final Queue<Runnable> syncQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    /**
     * Initializes the scheduler with named thread factories and efficient pooling.
     * Starts a Bukkit task to process the internal sync queue every tick.
     *
     * @param libraryOwner The plugin instance owning this scheduler.
     */
    public LibraryScheduler(@NotNull Plugin libraryOwner) {
        this.libraryOwner = libraryOwner;

        // Named Thread Factory for better debugging and profiling
        ThreadFactory asyncFactory = new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(1);
            @Override
            public Thread newThread(@NotNull Runnable r) {
                return new Thread(r, "PPL-Async-" + counter.getAndIncrement());
            }
        };

        // Optimized Pool: core threads for stability, max threads for peaks, 60s idle timeout
        this.asyncExecutor = new ThreadPoolExecutor(
                2, 8, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1024),
                asyncFactory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        this.timerService = Executors.newSingleThreadScheduledExecutor(r ->
                new Thread(r, "PPL-Timer"));

        // Central task to process the sync queue every tick (approx. every 50ms)
        Bukkit.getScheduler().runTaskTimer(libraryOwner, this::processSyncQueue, 1L, 1L);
    }

    // --- EXECUTION METHODS ---

    /**
     * Schedules a task to be executed on the Bukkit main thread in the next tick.
     *
     * @param runnable The task to execute.
     */
    public void runSync(@NotNull Runnable runnable) {
        if (!isShutdown.get()) syncQueue.add(runnable);
    }

    /**
     * Executes a task asynchronously using the internal thread pool.
     *
     * @param runnable The task to execute.
     */
    public void runAsync(@NotNull Runnable runnable) {
        if (!isShutdown.get()) asyncExecutor.execute(runnable);
    }

    // --- DELAYED & REPEATING ---

    /**
     * Schedules a task for synchronous execution after a specific delay.
     *
     * @param runnable The task to execute.
     * @param delay    The time to delay.
     * @param unit     The unit of the delay parameter.
     * @return A ScheduledFuture representing pending completion of the task.
     */
    public ScheduledFuture<?> runSyncDelayed(Runnable runnable, long delay, TimeUnit unit) {
        return timerService.schedule(() -> runSync(runnable), delay, unit);
    }

    /**
     * Schedules a task for asynchronous execution after a specific delay.
     *
     * @param runnable The task to execute.
     * @param delay    The time to delay.
     * @param unit     The unit of the delay parameter.
     * @return A ScheduledFuture representing pending completion of the task.
     */
    public ScheduledFuture<?> runAsyncDelayed(Runnable runnable, long delay, TimeUnit unit) {
        return timerService.schedule(() -> runAsync(runnable), delay, unit);
    }

    /**
     * Schedules a task for synchronous execution that repeats at a fixed rate.
     *
     * @param runnable The task to execute.
     * @param delay    The time to delay first execution.
     * @param period   The period between successive executions.
     * @param unit     The unit of the delay and period parameters.
     * @return A ScheduledFuture representing pending completion of the series of repeated tasks.
     */
    public ScheduledFuture<?> runSyncRepeating(Runnable runnable, long delay, long period, TimeUnit unit) {
        return timerService.scheduleAtFixedRate(() -> runSync(runnable), delay, period, unit);
    }

    /**
     * Schedules a task for asynchronous execution that repeats at a fixed rate.
     *
     * @param runnable The task to execute.
     * @param delay    The time to delay first execution.
     * @param period   The period between successive executions.
     * @param unit     The unit of the delay and period parameters.
     * @return A ScheduledFuture representing pending completion of the series of repeated tasks.
     */
    public ScheduledFuture<?> runAsyncRepeating(Runnable runnable, long delay, long period, TimeUnit unit) {
        return timerService.scheduleAtFixedRate(() -> runAsync(runnable), delay, period, unit);
    }

    // --- UTILITIES ---

    /**
     * Safely executes a task on the main thread only if the specified player is online.
     *
     * @param uuid The UUID of the player.
     * @param task The logic to run with the player instance.
     */
    public void runSafe(@NotNull UUID uuid, @NotNull Consumer<Player> task) {
        runSync(() -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                task.accept(player);
            }
        });
    }

    /**
     * Processes all pending tasks in the sync queue.
     * Executed by the central Bukkit timer task.
     */
    private void processSyncQueue() {
        Runnable task;
        while ((task = syncQueue.poll()) != null) {
            try {
                task.run();
            } catch (Exception e) {
                libraryOwner.getLogger().severe("Error in PPL Sync Task: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Gracefully shuts down the internal executors and processes the remaining sync queue.
     * This is be called in the PeachPaperLib plugin's {@code onDisable()} method.
     */
    public void shutdown() {
        if (isShutdown.getAndSet(true)) return;

        timerService.shutdown();
        asyncExecutor.shutdown();
        try {
            // Wait for existing tasks to terminate
            if (!asyncExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                asyncExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            asyncExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        processSyncQueue();
    }
}