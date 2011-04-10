package org.rsbot.gui;

import org.rsbot.script.methods.Environment;
import org.rsbot.util.GlobalConfiguration;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.MalformedURLException;

public class BotToolBar extends JToolBar {

	private static final long serialVersionUID = -1861866523519184211L;

	public static final int RUN_SCRIPT = 0;
	public static final int PAUSE_SCRIPT = 1;
	public static final int RESUME_SCRIPT = 2;

	public static final ImageIcon ICON_HOME;
	public static final ImageIcon ICON_BOT;

	public static final Image IMAGE_CLOSE;
	public static final Image IMAGE_CLOSE_OVER;

	static {
		ImageIcon icon = null;
		try {
			icon = new ImageIcon(GlobalConfiguration.RUNNING_FROM_JAR ?
			                     BotToolBar.class.getResource(GlobalConfiguration.Paths.Resources.ICON_HOME) :
			                     new File(GlobalConfiguration.Paths.ICON_HOME).toURI().toURL());
		} catch (MalformedURLException ignored) {
		}
		ICON_HOME = icon;
		try {
			icon = new ImageIcon(GlobalConfiguration.RUNNING_FROM_JAR ?
			                     BotToolBar.class.getResource(GlobalConfiguration.Paths.Resources.ICON_BOT) :
			                     new File(GlobalConfiguration.Paths.ICON_BOT).toURI().toURL());
		} catch (MalformedURLException ignored) {
			icon = null;
		}
		ICON_BOT = icon;
		try {
			icon = new ImageIcon(GlobalConfiguration.RUNNING_FROM_JAR ?
			                     BotToolBar.class.getResource(GlobalConfiguration.Paths.Resources.ICON_CLOSE) :
			                     new File(GlobalConfiguration.Paths.ICON_CLOSE).toURI().toURL());
		} catch (MalformedURLException ignored) {
		}
		IMAGE_CLOSE = icon == null ? null : icon.getImage();
		try {
			icon = new ImageIcon(GlobalConfiguration.RUNNING_FROM_JAR ?
			                     BotToolBar.class.getResource(GlobalConfiguration.Paths.Resources.ICON_CLOSE_OVER) :
			                     new File(GlobalConfiguration.Paths.ICON_CLOSE_OVER).toURI().toURL());
		} catch (MalformedURLException ignored) {
		}
		IMAGE_CLOSE_OVER = icon == null ? null : icon.getImage();
	}

	private JButton userInputButton;
	private JButton runScriptButton;

	private ActionListener listener;
	private int idx;
	private int inputState = Environment.INPUT_KEYBOARD | Environment.INPUT_MOUSE;
	private boolean inputOverride = true;

	public BotToolBar(ActionListener listener) {
		this.listener = listener;

		userInputButton = new JButton("Input", new ImageIcon(getInputImage(inputOverride, inputState)));
		userInputButton.addActionListener(listener);
		userInputButton.setFocusable(false);

		runScriptButton = new JButton("Run", new ImageIcon(
				GlobalConfiguration.getImage(GlobalConfiguration.Paths.Resources.ICON_PLAY,
				                             GlobalConfiguration.Paths.ICON_PLAY)));
		runScriptButton.addActionListener(listener);
		runScriptButton.setFocusable(false);

		HomeButton home = new HomeButton(ICON_HOME);

		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		setFloatable(false);
		add(home);
		add(new AddButton(listener));
		add(Box.createHorizontalGlue());
		add(runScriptButton);
		add(userInputButton);

		updateSelection(false);
	}

	public void addTab() {
		int idx = getComponentCount() - 4;
		add(new BotButton("RuneScape", ICON_BOT), idx);
		validate();
		setSelection(idx);
	}

	public void removeTab(int idx) {
		remove(idx);
		revalidate();
		repaint();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setSelection(0);
			}
		});
	}

	public void setTabLabel(int idx, String label) {
		((BotButton) getComponentAtIndex(idx)).setText(label);
	}

	public int getCurrentTab() {
		if (idx > -1 && idx < getComponentCount() - 3) {
			return idx;
		} else {
			return -1;
		}
	}

	public int getScriptButton() {
		String label = runScriptButton.getText();
		if (label.equals("Run")) {
			return RUN_SCRIPT;
		} else if (label.equals("Pause")) {
			return PAUSE_SCRIPT;
		} else if (label.equals("Resume")) {
			return RESUME_SCRIPT;
		} else {
			throw new IllegalStateException("Illegal script button state!");
		}
	}

	public void setHome(boolean home) {
		userInputButton.setEnabled(!home);
		runScriptButton.setEnabled(!home);
	}

	public void setInputState(int state) {
		inputState = state;
	}

	public void setOverrideInput(boolean selected) {
		inputOverride = selected;
	}

	public void updateInputButton() {
		userInputButton.setIcon(new ImageIcon(getInputImage(inputOverride, inputState)));
	}

	public void setScriptButton(int state) {
		String text, pathResource, pathFile;

		if (state == RUN_SCRIPT) {
			text = "Run";
			pathResource = GlobalConfiguration.Paths.Resources.ICON_PLAY;
			pathFile = GlobalConfiguration.Paths.ICON_PLAY;
		} else if (state == PAUSE_SCRIPT) {
			text = "Pause";
			pathResource = GlobalConfiguration.Paths.Resources.ICON_PAUSE;
			pathFile = GlobalConfiguration.Paths.ICON_PAUSE;
		} else if (state == RESUME_SCRIPT) {
			text = "Resume";
			pathResource = GlobalConfiguration.Paths.Resources.ICON_PLAY;
			pathFile = GlobalConfiguration.Paths.ICON_PLAY;
		} else {
			throw new IllegalArgumentException("Illegal button state: " + state + "!");
		}

		runScriptButton.setText(text);
		try {
			runScriptButton.setIcon(new ImageIcon(
					GlobalConfiguration.RUNNING_FROM_JAR ? getClass().getResource(pathResource) :
					new File(pathFile).toURI().toURL()));
		} catch (final MalformedURLException e1) {
			e1.printStackTrace();
		}
		revalidate();
	}

	private void setSelection(int idx) {
		updateSelection(true);
		this.idx = idx;
		updateSelection(false);
		listener.actionPerformed(new ActionEvent(this, 0, "Tab"));
	}

	private void updateSelection(boolean enabled) {
		int idx = getCurrentTab();
		if (idx >= 0) {
			getComponent(idx).setEnabled(enabled);
			getComponent(idx).repaint();
		}
	}

	private Image getInputImage(boolean override, int state) {
		if (override || state == (Environment.INPUT_KEYBOARD | Environment.INPUT_MOUSE)) {
			return GlobalConfiguration.getImage(GlobalConfiguration.Paths.Resources.ICON_TICK,
			                                    GlobalConfiguration.Paths.ICON_TICK);
		} else if (state == Environment.INPUT_KEYBOARD) {
			return GlobalConfiguration.getImage(GlobalConfiguration.Paths.Resources.ICON_KEYBOARD,
			                                    GlobalConfiguration.Paths.ICON_KEYBOARD);
		} else if (state == Environment.INPUT_MOUSE) {
			return GlobalConfiguration.getImage(GlobalConfiguration.Paths.Resources.ICON_MOUSE,
			                                    GlobalConfiguration.Paths.ICON_MOUSE);
		} else {
			return GlobalConfiguration.getImage(GlobalConfiguration.Paths.Resources.ICON_DELETE,
			                                    GlobalConfiguration.Paths.ICON_DELETE);
		}
	}

	/**
	 * @author Jacmob
	 */
	private class HomeButton extends JPanel {

		private static final long serialVersionUID = 938456324328L;

		private Image image;
		private boolean hovered;

		public HomeButton(ImageIcon icon) {
			super(new BorderLayout());
			this.image = icon.getImage();
			setBorder(new EmptyBorder(3, 6, 2, 3));
			setPreferredSize(new Dimension(24, 22));
			setMaximumSize(new Dimension(24, 22));
			setFocusable(false);
			addMouseListener(new MouseAdapter() {
				public void mouseReleased(MouseEvent e) {
					setSelection(getComponentIndex(HomeButton.this));
				}

				public void mouseEntered(MouseEvent e) {
					hovered = true;
					repaint();
				}

				public void mouseExited(MouseEvent e) {
					hovered = false;
					repaint();
				}
			});
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			                                  RenderingHints.VALUE_ANTIALIAS_ON);
			if (getComponentIndex(this) == idx) {
				g.setColor(new Color(255, 255, 255, 200));
				g.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 1, 4, 4);
				g.setColor(new Color(180, 180, 180, 200));
				g.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 1, 4, 4);
			} else if (hovered) {
				g.setColor(new Color(255, 255, 255, 150));
				g.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 1, 4, 4);
				g.setColor(new Color(180, 180, 180, 150));
				g.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 1, 4, 4);
			}
			g.drawImage(image, 3, 3, null);
		}

	}

	/**
	 * @author Tekk
	 */
	private class BotButton extends JPanel {

		private static final long serialVersionUID = 329845763420L;

		private JLabel nameLabel;
		private boolean hovered;
		private boolean close;

		public BotButton(String text, Icon icon) {
			super(new BorderLayout());
			setBorder(new EmptyBorder(3, 6, 2, 3));
			nameLabel = new JLabel(text);
			nameLabel.setIcon(icon);
			nameLabel.setPreferredSize(new Dimension(85, 22));
			nameLabel.setMaximumSize(new Dimension(85, 22));
			add(nameLabel, BorderLayout.WEST);

			setPreferredSize(new Dimension(110, 22));
			setMaximumSize(new Dimension(110, 22));
			setFocusable(false);
			addMouseListener(new MouseAdapter() {
				public void mouseReleased(MouseEvent e) {
					if (hovered && close) {
						int idx = BotToolBar.this.getComponentIndex(BotButton.this);
						listener.actionPerformed(new ActionEvent(this,
						                                         ActionEvent.ACTION_PERFORMED, "Close." + idx));
					} else {
						setSelection(getComponentIndex(BotButton.this));
					}
				}

				public void mouseEntered(MouseEvent e) {
					hovered = true;
					repaint();
				}

				public void mouseExited(MouseEvent e) {
					hovered = false;
					repaint();
				}
			});
			addMouseMotionListener(new MouseMotionAdapter() {
				public void mouseMoved(MouseEvent e) {
					close = e.getX() > 95;
					repaint();
				}
			});
		}

		public void setText(String label) {
			nameLabel.setText(label);
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			                                  RenderingHints.VALUE_ANTIALIAS_ON);
			if (getComponentIndex(this) == idx) {
				g.setColor(new Color(255, 255, 255, 200));
				g.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 1, 4, 4);
				g.setColor(new Color(180, 180, 180, 200));
				g.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 1, 4, 4);
			} else if (hovered) {
				g.setColor(new Color(255, 255, 255, 150));
				g.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 1, 4, 4);
				g.setColor(new Color(180, 180, 180, 150));
				g.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 1, 4, 4);
			} else {
				g.setColor(new Color(180, 180, 180, 150));
				g.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 1, 4, 4);
			}
			if (hovered && close) {
				g.drawImage(IMAGE_CLOSE_OVER, 90, 3, null);
			} else {
				g.drawImage(IMAGE_CLOSE, 90, 3, null);
			}
		}
	}

	private static class AddButton extends JComponent {

		private static final long serialVersionUID = 1L;

		private static final Image ICON;
		private static final Image ICON_OVER;
		private static final Image ICON_DOWN;
		private boolean hovered = false;
		private boolean pressed = false;

		static {
			Image icon = null;
			try {
				icon = new ImageIcon(GlobalConfiguration.RUNNING_FROM_JAR ?
				                     BotToolBar.class.getResource(GlobalConfiguration.Paths.Resources.ICON_ADD) :
				                     new File(GlobalConfiguration.Paths.ICON_ADD).toURI().toURL()).getImage();
			} catch (MalformedURLException ignored) {
			}
			ICON = icon;
			try {
				icon = new ImageIcon(GlobalConfiguration.RUNNING_FROM_JAR ?
				                     BotToolBar.class.getResource(GlobalConfiguration.Paths.Resources.ICON_ADD_OVER) :
				                     new File(GlobalConfiguration.Paths.ICON_ADD_OVER).toURI().toURL()).getImage();
			} catch (MalformedURLException ignored) {
				icon = null;
			}
			ICON_OVER = icon;
			try {
				icon = new ImageIcon(GlobalConfiguration.RUNNING_FROM_JAR ?
				                     BotToolBar.class.getResource(GlobalConfiguration.Paths.Resources.ICON_ADD_DOWN) :
				                     new File(GlobalConfiguration.Paths.ICON_ADD_DOWN).toURI().toURL()).getImage();
			} catch (MalformedURLException ignored) {
				icon = null;
			}
			ICON_DOWN = icon;
		}

		public AddButton(final ActionListener listener) {
			setPreferredSize(new Dimension(20, 20));
			setMaximumSize(new Dimension(20, 20));
			setFocusable(false);
			addMouseListener(new MouseAdapter() {
				public void mouseEntered(MouseEvent e) {
					hovered = true;
					repaint();
				}

				public void mouseExited(MouseEvent e) {
					hovered = false;
					repaint();
				}

				public void mousePressed(MouseEvent e) {
					pressed = true;
					repaint();
				}

				public void mouseReleased(MouseEvent e) {
					pressed = false;
					repaint();
					listener.actionPerformed(new ActionEvent(this, e.getID(), "File.New Bot"));
				}
			});
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (pressed) {
				g.drawImage(ICON_DOWN, 2, 2, null);
			} else if (hovered) {
				g.drawImage(ICON_OVER, 2, 2, null);
			} else {
				g.drawImage(ICON, 2, 2, null);
			}
		}

	}

}