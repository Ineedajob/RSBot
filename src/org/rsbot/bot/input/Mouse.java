package org.rsbot.bot.input;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public abstract class Mouse extends Focus implements MouseListener, MouseMotionListener, MouseWheelListener {

    private int x;
    private int y;
    private boolean present;
    private boolean pressed;

    private int clientX;
    private int clientY;
    private int clientPressX = -1;
    private int clientPressY = -1;
    private long clientPressTime = -1;
    private boolean clientPresent;
    private boolean clientPressed;

    public abstract void _mouseClicked(MouseEvent e);

    public abstract void _mouseDragged(MouseEvent e);

    public abstract void _mouseEntered(MouseEvent e);

    public abstract void _mouseExited(MouseEvent e);

    public abstract void _mouseMoved(MouseEvent e);

    public abstract void _mousePressed(MouseEvent e);

    public abstract void _mouseReleased(MouseEvent e);

    public abstract void _mouseWheelMoved(MouseWheelEvent e);

    public abstract Component getComponent();

    public abstract boolean isLoggingMouseMovements();

    public long getClientPressTime() {
        return clientPressTime;
    }

    public int getClientPressX() {
        return clientPressX;
    }

    public int getClientPressY() {
        return clientPressY;
    }

    public int getClientX() {
        return clientX;
    }

    public int getClientY() {
        return clientY;
    }
    
    public boolean isClientPressed() {
    	return clientPressed;
    }
    
    public boolean isClientPresent() {
    	return clientPresent;
    }
    
    public int getRealX() {
    	return x;
    }
    
    public int getRealY() {
    	return y;
    }
    
    public boolean isRealPresent() {
    	return present;
    }
    
    public boolean isRealPressed() {
    	return pressed;
    }

    public final void mouseClicked(final MouseEvent e) {
        // System.out.println(("MC");
        x = e.getX();
        y = e.getY();

        if (!Listener.blocked) {
            clientX = x;
            clientY = y;
            _mouseClicked(e);
        }
        e.consume();
    }

    public final void mouseDragged(final MouseEvent e) {
        // System.out.println(("MD");
        x = e.getX();
        y = e.getY();

        if (!Listener.blocked) {
            clientX = x;
            clientY = y;
            _mouseDragged(e);
        }
        e.consume();
    }

    public final void mouseEntered(final MouseEvent e) {
        // System.out.println(("MEnt");
        present = true;
        x = e.getX();
        y = e.getY();

        if (!Listener.blocked) {
        	clientPresent = true;
            clientX = x;
            clientY = y;
            _mouseEntered(e);
        }
        e.consume();
    }

    public final void mouseExited(final MouseEvent e) {
        // System.out.println(("MExt");
        present = false;
        x = e.getX();
        y = e.getY();

        if (!Listener.blocked) {
        	clientPresent = false;
            _mouseExited(e);
        }
        e.consume();
    }

    public final void mouseMoved(final MouseEvent e) {
        // System.out.println(("MM");
        x = e.getX();
        y = e.getY();

        if (!Listener.blocked) {
            clientX = x;
            clientY = y;
            _mouseMoved(e);
        }
        e.consume();
    }

    public final void mousePressed(final MouseEvent e) {
        // System.out.println(("MP");
        pressed = true;

        x = e.getX();
        y = e.getY();

        if (!Listener.blocked) {
        	clientPressed = true;
            clientX = x;
            clientY = y;
            _mousePressed(e);
        }
        e.consume();
    }

    public final void mouseReleased(final MouseEvent e) {
        // System.out.println(("MR");
        pressed = false;
        x = e.getX();
        y = e.getY();

        if (!Listener.blocked) {
            clientX = x;
            clientY = y;
            clientPressX = x;
            clientPressY = y;
            clientPressTime = System.currentTimeMillis();
            clientPressed = false;

            _mouseReleased(e);
        }
        e.consume();
    }

    public void mouseWheelMoved(final MouseWheelEvent e) {
        // System.out.println(("WHL");
        if (!Listener.blocked) {
            try {
                _mouseWheelMoved(e);
            } catch (AbstractMethodError ame) {
                // it might not be implemented!
            }
        }
        e.consume();
    }

    public final void sendEvent(final MouseEvent e) {
        this.clientX = e.getX();
        this.clientY = e.getY();
        try {
            if (e.getID() == MouseEvent.MOUSE_CLICKED) {
                _mouseClicked(e);
            } else if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
                _mouseDragged(e);
            } else if (e.getID() == MouseEvent.MOUSE_ENTERED) {
            	clientPresent = true;
                _mouseEntered(e);
            } else if (e.getID() == MouseEvent.MOUSE_EXITED) {
            	clientPresent = false;
                _mouseExited(e);
            } else if (e.getID() == MouseEvent.MOUSE_MOVED) {
                _mouseMoved(e);
            } else if (e.getID() == MouseEvent.MOUSE_PRESSED) {
            	clientPressed = true;
                _mousePressed(e);
            } else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
                clientPressX = e.getX();
                clientPressY = e.getY();
                clientPressTime = System.currentTimeMillis();
                clientPressed = false;
                _mouseReleased(e);
            } else if (e.getID() == MouseEvent.MOUSE_WHEEL) {
                try {
                    _mouseWheelMoved((MouseWheelEvent) e);
                } catch (AbstractMethodError ignored) {
                    // it might not be implemented!
                }
            } else {
                throw new InternalError(e.toString());
            }
        } catch (NullPointerException ignored) {
            // client may throw NPE when a listener
            // is being re-instantiated.
        }
    }
}
