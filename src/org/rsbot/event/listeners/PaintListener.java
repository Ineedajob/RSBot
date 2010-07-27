package org.rsbot.event.listeners;

import java.awt.Graphics;
import java.util.EventListener;

public interface PaintListener extends EventListener {
    public void onRepaint(Graphics render);
}
