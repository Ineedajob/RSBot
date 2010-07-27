package org.rsbot.event;

import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.rsbot.event.events.RSEvent;

public class EventManager implements Runnable {
    public static class KillEvent extends RSEvent {
        private static final long serialVersionUID = 3426050317048250049L;

        @Override
        public void dispatch(final EventListener el) {
        }

        @Override
        public long getMask() {
            return -1;
        }
    }

    private final Logger log = Logger.getLogger(EventManager.class.getName());
    private Thread eventThread;

    private final EventMulticaster multicaster = new EventMulticaster();
    private final Map<Integer, EventObject> queue = new HashMap<Integer, EventObject>();

    private final Object threadLock = new Object();

    /**
     * Adds the event to the queue for the EventManager to process.
     * <p/>
     * Events are processed with the default mask.
     */
    public void addToQueue(final EventObject e) {
        // System.out.println(("addToQueue - " + EventManager.queue.size());
        synchronized (queue) {
            boolean added = false;
            for (int off = 0; off < queue.size(); off++) {
                if (!queue.containsKey(off)) {
                    queue.put(off, e);
                    added = true;
                    break;
                }
            }
            if (!added) {
                queue.put(queue.size(), e);
            }
            queue.notifyAll();
        }
    }

    /**
     * Returns the multicaster that all the events get sent to.
     */
    public EventMulticaster getMulticaster() {
        return multicaster;
    }

    /**
     * Is this thread the event thread?
     */
    public boolean isEventThread() {
        synchronized (threadLock) {
            return Thread.currentThread() == eventThread;
        }
    }

    /**
     * Is the event thread alive?
     */
    public boolean isEventThreadAlive() {
        synchronized (threadLock) {
            return eventThread != null;
        }
    }

    /**
     * If clear then we clear the queue then kill. If not then we finish
     * processing then kill. If sleep then when return the event thread is dead
     * or we have been interupted. Either way the thread will eventually die.
     */
    public void killThread(final boolean clear, final boolean wait) {
        final EventObject event = new KillEvent();
        synchronized (event) {
            addToQueue(event);
            if (wait) {
                try {
                    event.wait();
                } catch (final Exception e) {
                    log.info("Event Queue: " + e.toString());
                }
            }
        }
    }

    /**
     * Process the event. This dispatches the event.
     */
    public void processEvent(final EventObject event) {
        multicaster.fireEvent(event);
    }

    /**
     * Registers a listener.
     *
     * @param listener the listener to add
     */
    public void registerListener(final EventListener listener) {
        multicaster.addListener(listener);
    }

    /**
     * Removes the listener from the list. Use removeListener(T) instead.
     */
    public <T extends EventListener> void removeListener(final Class<T> c, final T el) { // TODO
        // remove me
        multicaster.removeListener(el);
    }

    /**
     * Removes the listener from the list.
     */
    public <T extends EventListener> void removeListener(final T el) { // TODO
        // remove me
        multicaster.removeListener(el);
    }

    /**
     * The thread entry point.
     */
    public void run() {
        if (!isEventThread())
            throw new IllegalThreadStateException();
        while (true) {
            try {
                EventObject event = null;
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (final Exception e) {
                            log.info("Event Queue: " + e.toString());
                        }
                    }
                    int emptySpots = 0;
                    for (int off = 0; off < queue.size() + emptySpots; off++) {
                        if (!queue.containsKey(off)) {
                            emptySpots++;
                            continue;
                        }
                        event = queue.remove(off);
                        break;
                    }
                }
                if (event instanceof KillEvent) {
                    eventThread = null;
                    event.notifyAll();
                    return;
                }
                try {
                    processEvent(event);
                } catch (final ThreadDeath td) {
                    eventThread = null;
                    event.notifyAll();
                    return;
                } catch (final Throwable e) {
                    e.printStackTrace();
                }
                synchronized (event) {
                    event.notifyAll();
                }
            } catch (final Exception e) {
                log.info("Event Queue: " + e.toString());
            }
        }
    }

    /**
     * Spawns a daemon event thread. Only one can be created unless it is
     * killed.
     */
    public void start() {
        synchronized (threadLock) {
            if (eventThread != null)
                throw new IllegalThreadStateException();
            eventThread = new Thread(this, "EventQueue");
            eventThread.setDaemon(true);
			eventThread.start();
		}
	}
}
