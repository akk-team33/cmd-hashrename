package net.team33.hashrename;

public class Activity {

    private long started = 0;
    private long finished = 0;

    public final Runnable add(final Runnable body) {
        start();
        return () -> {
            try {
                body.run();
            } finally {
                quit();
            }
        };
    }

    public final synchronized void join() throws InterruptedException {
        while (0 == started || started > finished) {
            wait();
        }
    }

    private synchronized void start() {
        started += 1;
    }

    private synchronized void quit() {
        finished += 1;
        notifyAll();
    }
}
