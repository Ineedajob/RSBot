package org.rsbot.gui;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.*;

import javax.swing.JPanel;

import org.rsbot.bot.Bot;
import org.rsbot.event.EventManager;
import org.rsbot.script.methods.Mouse;


public class BotPanel extends JPanel {

	private static final long serialVersionUID = 2269767882075468055L;

	private Bot bot;
	private int offX;
	private boolean present;

	public BotPanel() {
		setSize(new Dimension(BotGUI.PANEL_WIDTH, BotGUI.PANEL_HEIGHT));
		setMinimumSize(new Dimension(BotGUI.PANEL_WIDTH, BotGUI.PANEL_HEIGHT));
		setPreferredSize(new Dimension(BotGUI.PANEL_WIDTH, BotGUI.PANEL_HEIGHT));
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent evt) {
				if (bot != null) {
					bot.resize(getWidth(), getHeight());
					offset();
				}
				requestFocus();
			}
		});
		addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent e) {
				redispatch(e);
				if (!hasFocus()) {
					requestFocus();
				}
			}

			public void mouseEntered(MouseEvent e) {
				
			}

			public void mouseExited(MouseEvent e) {
				redispatch(e);
			}

			public void mousePressed(MouseEvent e) {
				redispatch(e);
			}

			public void mouseReleased(MouseEvent e) {
				redispatch(e);
			}
			
		});
		addMouseMotionListener(new MouseMotionListener() {

			public void mouseDragged(MouseEvent e) {
				redispatch(e);
			}

			public void mouseMoved(MouseEvent e) {
				redispatch(e);
			}
			
		});
		addMouseWheelListener(new MouseWheelListener() {

			public void mouseWheelMoved(MouseWheelEvent e) {
				redispatch(e);
			}

		});
		addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				redispatch(e);
			}

			public void keyReleased(KeyEvent e) {
				redispatch(e);
			}

			public void keyTyped(KeyEvent e) {
				redispatch(e);
			}
			
		});
	}

	public void offset() {
		if (bot.getCanvas() != null) {
			// center canvas horizontally if not filling container
			offX = (getWidth() - bot.getCanvas().getWidth()) / 2;
		}
	}

	public void setBot(Bot bot) {
		if (this.bot != null) {
			this.bot.setPanel(null);
		}
		this.bot = bot;
		if (bot != null) {
			bot.setPanel(this);
			if (bot.getCanvas() != null) {
				offset();
			}
		}
	}

	public void paintComponent(Graphics g) {
		super.paintComponents(g);
		if (bot == null) {
			g.setColor(new Color(240, 240, 240));
			g.fillRect(0, 0, getWidth(), getHeight());
		} else {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.drawImage(bot.getImage(), offX, 0, null);
		}
	}

	private void redispatch(final MouseEvent e) {
		if (bot != null && bot.getLoader().getComponentCount() > 0) {
			Mouse mouse = bot.getMethodContext().mouse;
			if (mouse == null) {
				return; // client cannot currently accept events
			}
			boolean present = mouse.isPresent();
			Component c = bot.getLoader().getComponent(0);
			// account for horizontal offset
			e.translatePoint(-offX, 0);
			// fire human mouse event for scripts
			dispatchHuman(c, e);
			if (bot.disableInput) {
				return;
			}
			if (e.getX() > 0 && e.getX() < c.getWidth() && e.getY() < c.getHeight() // account for edges
					&& e.getID() != MouseEvent.MOUSE_EXITED) {
				if (present) {
					if (e instanceof MouseWheelEvent) {
						MouseWheelEvent mwe = (MouseWheelEvent) e;
						c.dispatchEvent(new MouseWheelEvent(c, e.getID(), System.currentTimeMillis(),
								0, e.getX(), e.getY(), 0, e.isPopupTrigger(),
								mwe.getScrollType(), mwe.getScrollAmount(), mwe.getWheelRotation()));
					} else {
						c.dispatchEvent(new MouseEvent(c, e.getID(), System.currentTimeMillis(),
								0, e.getX(), e.getY(), 0, e.isPopupTrigger(), e.getButton()));
					}
				} else {
					c.dispatchEvent(new MouseEvent(c, MouseEvent.MOUSE_ENTERED, System.currentTimeMillis(),
							0, e.getX(), e.getY(), 0, false));
				}
			} else if (present) {
				c.dispatchEvent(new MouseEvent(c, MouseEvent.MOUSE_EXITED, System.currentTimeMillis(),
						0, e.getX(), e.getY(), 0, false));
			}
		}
	}

	private void redispatch(AWTEvent e) {
		if (bot != null) {
			EventManager m = bot.getEventManager();
			if (m != null) {
				m.dispatchEvent(e);
			}
			if (!bot.disableInput && bot.getLoader().getComponentCount() > 0) {
				Component c = bot.getLoader().getComponent(0);
				c.dispatchEvent(e);
			}
		}
	}

	private void dispatchHuman(Component c, MouseEvent e) {
		if (e.getX() > 0 && e.getX() < c.getWidth() && e.getY() < c.getHeight()
				&& e.getID() != MouseEvent.MOUSE_EXITED) {
			if (present) {
				bot.getEventManager().dispatchEvent(e);
			} else {
				present = true;
				bot.getEventManager().dispatchEvent(new MouseEvent(c,
						MouseEvent.MOUSE_ENTERED, System.currentTimeMillis(),
						0, e.getX(), e.getY(), 0, false));
			}
		} else if (present) {
			present = false;
			bot.getEventManager().dispatchEvent(new MouseEvent(c,
					MouseEvent.MOUSE_EXITED, System.currentTimeMillis(),
					0, e.getX(), e.getY(), 0, false));
		}
	}

}
