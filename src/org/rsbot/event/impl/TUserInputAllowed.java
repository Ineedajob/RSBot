package org.rsbot.event.impl;

import java.awt.Graphics;

import org.rsbot.client.input.Listener;
import org.rsbot.event.listeners.TextPaintListener;
import org.rsbot.util.StringUtil;

public class TUserInputAllowed implements TextPaintListener {

    public int drawLine(final Graphics render, int idx) {
        StringUtil.drawLine(render, idx++, "User Input: " + (Listener.blocked ? "[red]Disabled" : "[green]Enabled"));
        return idx;
    }
}
