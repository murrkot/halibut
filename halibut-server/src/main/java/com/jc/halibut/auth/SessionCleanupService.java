package com.jc.halibut.auth;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SessionCleanupService {
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);
    private static final int INITIAL_DELAY_MINUTES = 2;
    private static final int CLEANUP_INTERVAL_MINUTES = 5;

    private SessionCleanupService() {
    }

    public static void ensureStarted(ActiveSessionRepository repository) {
        if (repository == null) {
            return;
        }
        if (!STARTED.compareAndSet(false, true)) {
            return;
        }

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());
        executor.scheduleAtFixedRate(() -> {
            try {
                repository.deleteExpiredSessions();
            } catch (RuntimeException ignored) {
            }
        }, INITIAL_DELAY_MINUTES, CLEANUP_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    private static class DaemonThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable task) {
            Thread thread = new Thread(task, "session-cleanup");
            thread.setDaemon(true);
            return thread;
        }
    }
}
