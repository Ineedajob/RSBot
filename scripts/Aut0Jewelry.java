import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Bank;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSInterface;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSModel;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSPlayer;
import org.rsbot.script.wrappers.RSTile;

@ScriptManifest(authors = "Aut0r", keywords = "Crafting", name = "Aut0Jewelry", version = 1.21, description = "Supports Jewelry and Gem Cutting")
public class Aut0Jewelry extends Script implements PaintListener, MouseListener {
	public double version = 1.21;
	public String scriptName = "Aut0Jewelry";
	public boolean canceled = false, gem, globalBanking = false;
	public String status, craftLoc, hh, ss, mm, action, t;
	public int cosmic = 564, goldBarID = 2357, silverBarID = 2355,
			openBank = 0, failCounter = 0, chiselID;
	public int gemID, sapphire = 1607, emerald = 1605, ruby = 1603,
			diamond = 1601, dragonstone = 1615, uncutGemID;
	public int uncutSapphire = 1623, uncutEmerald = 1621, uncutRuby = 1619,
			uncutDiamond = 1617, uncutDragonstone = 1631;
	public int barID, gainedXp, gainedLvl;
	public String furnaceAction, gemName, itemName, barName, enchantedName,
			spellName;
	public int animation, session, itemsInBank, bankID, smithID, itemID,
			mouldID, exp, itemsPrice, gemPrice = 0, barPrice = 0, itemsMade,
			startExp, startLvl, ifParent, ifChild, enchantedID, component,
			speedMouse;
	public long startTime, checkMin = 1, totalseconds;
	public float secCraExp;
	int expToCraLvl = 0, secCraLvl, minCraLvl, hrCraLvl, itemsTL;
	String secC0, minC0, hrC0;
	Rectangle tab1 = new Rectangle(380, 30, 135, 125);
	private final Color transGreen = new Color(0, 255, 0, 125);
	private Line[] pathToFurnace = null;
	private Line[] pathToBank = null;
	private Line[] structType = null;
	public RSArea[] AREAS = null;
	private ArrayList<RSTile> path;
	private static final int BOOK_KNOWLEDGE_ID = 11640;
	private static final int LAMP_ID = 2528;
	private static final int MYSTERY_BOX_ID = 6199;
	private static final int BOX_ID = 14664;

	private enum State {
		OPENBANK, OPENCHEST, BANK, CRAFT, WALKTOBANK, WALKTOFURNACE, CUTTING, OPENGBANK, GBANK, ANTIBAN, CHILL
	}

	public State getState() {
		if (globalBanking) {
			if (inventory.contains(chiselID) && inventory.contains(uncutGemID)) {
				return State.CUTTING;
			} else if (!inventory.contains(chiselID)
					|| !inventory.contains(uncutGemID)) {
				if (!bank.isOpen()) {
					return State.OPENGBANK;
				} else {
					return State.GBANK;
				}
			}
		} else {
			int randAntiBanGen = random(1, 40);
			if (randAntiBanGen <= 2 && !atBank()) {
				return State.ANTIBAN;
			}
			//
			// if(inventory.contains(itemID) && !inventory.contains(barID) &&
			// !atBank()) {
			// return State.WALKTOBANK;
			// } else if(!inventory.contains(itemID) &&
			// inventory.contains(barID) && !atFurnace()) {
			// return State.WALKTOFURNACE;
			// } else if(inventory.contains(itemID) &&
			// !inventory.contains(barID) && atBank()) {
			// if(!bank.isOpen()) {
			// return State.OPENBANK;
			// } else {
			// return State.BANK;
			// }
			// } else if(!inventory.contains(itemID) ||
			// !inventory.contains(barID) && atBank()) {
			// if(!bank.isOpen()) {
			// return State.OPENBANK;
			// } else {
			// return State.BANK;
			// }
			// } else if(inventory.contains(gemID) || inventory.contains(barID)
			// && atFurnace()) {
			// return State.CRAFT;
			// } else if(inventory.contains(barID) || inventory.contains(gemID))
			// {
			// return State.WALKTOFURNACE;
			// } else {
			// return State.WALKTOBANK;
			// }
			//
			if (atBank()) {
				if (inventory.contains(itemID)) {
					if (!bank.isOpen()) {
						return State.OPENBANK;
					} else {
						return State.BANK;
					}
				} else if (atBank() && inventory.contains(enchantedID)
						&& !inventory.contains(itemID)) {
					if (!bank.isOpen()) {
						return State.OPENBANK;
					} else {
						return State.BANK;
					}
				} else if (atBank() && !inventory.contains(barID)
						|| !inventory.contains(gemID) && gem) {
					if (!bank.isOpen()) {
						return State.OPENBANK;
					} else {
						return State.BANK;
					}
				} else if (atBank() && readyToCraft()) {
					return State.WALKTOFURNACE;
				}
			}
			if (atFurnace()) {
				if (inventory.getCount(barID) >= 1
						|| inventory.getCount(gemID) >= 1) {
					return State.CRAFT;
				} else if (inventory.getCount(barID) < 1
						|| inventory.getCount(gemID) < 1) {
					return State.WALKTOBANK;
				}
			}
			if (!atBank() || !atFurnace()) {
				if (inventory.getCount(barID) >= 1) {
					return State.WALKTOFURNACE;
				} else if (!inventory.contains(barID)
						|| inventory.contains(itemID)) {
					return State.WALKTOBANK;
				}
			}
		}
		return State.CHILL;
	}

	@Override
	public void onFinish() {
		log("Thanks for Using " + scriptName + " by Aut0r");
	}

	@Override
	public boolean onStart() {
		try {
			GUI g = new GUI();
			g.setVisible(true);
			while (g.isVisible())
				sleep(100);
			if (!globalBanking) {
				path = generatePath(pathToBank);
				structType = pathToBank;
				path = generatePath(pathToFurnace);
				structType = pathToFurnace;
			}
			if (canceled) {
				log.severe("User terminated script from gui.");
				log.severe("Quitting...");
				return false;
			}
			if (game.isLoggedIn()) {
				startExp = skills.getCurrentExp(Skills.CRAFTING);
				startLvl = skills.getCurrentLevel(Skills.CRAFTING);
			} else {
				log.severe("[FETAL] Script failed to load current levels/xp.  You must be logged in.");
				return false;
			}
			priceLoader();
			startTime = System.currentTimeMillis();
		} catch (Exception e) {
			log(e.getCause().toString());
			return false;
		}
		return true;
	}

	public void priceLoader() {
		if (gem && !globalBanking) {
			log("Loading " + gemName + " Prices...");
			gemPrice = grandExchange.lookup(gemID).getGuidePrice();
			log("Loading " + barName + " Prices...");
			barPrice = grandExchange.lookup(barID).getGuidePrice();
			log("Loading " + itemName + " Prices...");
			itemsPrice = grandExchange.lookup(itemID).getGuidePrice();
		} else {
			log("Loading " + barName + " Prices...");
			barPrice = grandExchange.lookup(barID).getGuidePrice();
			log("Loading " + itemName + " Prices...");
			itemsPrice = grandExchange.lookup(itemID).getGuidePrice();
		}

	}

	@Override
	public int loop() {
		try {
			if (bank.isOpen()) {
				if (inventory.containsOneOf(BOX_ID, BOOK_KNOWLEDGE_ID, LAMP_ID,
						MYSTERY_BOX_ID)) {
					bank.close();
					sleep(random(300, 500));
					log("Rewards-Box failsafed.");
					game.openTab(Game.TAB_INVENTORY);
					return 200;
				}
			}
			if (!bank.isOpen() && !walking.isRunEnabled()
					&& walking.getEnergy() > 60) {
				walking.setRun(true);
				waitForEnableRun(1000);
			}
			furnaceAction = "Use " + barName + " -> Furnace";
			RSObject banker = objects.getNearest(bankID);
			mouse.setSpeed(speedMouse);
			final State state = getState(); // Gets the state
			switch (state) { // Switches between these states based on getState
			case OPENGBANK:
				status = "Opening Bank";
				if (openBank < 2) {
					bank.open();
					mouse.moveRandomly(50);
					if (waitForBankIF(2000)) {
						openBank++;
					} else {
						bank.open();
						mouse.moveRandomly(50);
						waitForBankIF(2000);
						openBank++;
					}
				} else {
					game.openTab(random(0, 17));
					sleep(random(300, 600));
					game.openTab(Game.TAB_INVENTORY);
					openBank = 0;
				}
				break;
			case GBANK:
				status = "Banking";
				if (!bank.isOpen()) {
					break;
				}
				openBank = 0;
				if (inventory.getCount() > 1) {
					bank.depositAllExcept(chiselID);
					waitForDepositedItem(2500);
				}
				if (checkBank(uncutGemID, 27) >= 1) {
					withdraw(uncutGemID, itemsInBank);
					waitForWithdrawnItem(uncutGemID, 2000);
				}
				if (inventory.getCount(gemID) < 1
						&& inventory.getCount(chiselID) == 1) {
					bank.close();
					break;
				} else if (inventory.getCount(chiselID) != 1) {
					bank.depositAllExcept(chiselID);
					waitForDepositedItem(2500);
					if (checkBank(chiselID, 1) >= 1) {
						withdraw(chiselID, 1);
						waitForWithdrawnItem(chiselID, 2000);
					}
				}
			case CUTTING:
				status = "Cutting " + barName;
				if (!isCutting()
						&& interfaces.getComponent(ifParent, ifChild).isValid()) {
					interfaces.getComponent(ifParent, ifChild).doAction(
							"Cut All");
				}
				if (isCutting() && inventory.contains(uncutGemID)) {
					sleep(random(500, 750));
				} else if (!isCutting() && inventory.contains(uncutGemID)) {
					RSItem RSI_uncut = inventory.getItem(uncutGemID);
					RSItem RSI_chisel = inventory.getItem(chiselID);
					if (RSI_uncut != null && RSI_uncut != null
							&& inventory.contains(uncutGemID)
							&& inventory.contains(chiselID)) {
						if (!inventory.isItemSelected()) {
							if (RSI_chisel != null) {
								RSI_chisel.doAction("Use");
								waitForInventoryAction(1000);
							}
						}
						if (inventory.isItemSelected()) {
							if (RSI_uncut != null) {
								RSI_uncut.doAction("Use Chisel -> " + barName);
								waitForIF(interfaces.get(ifParent),
										random(2000, 3000));
							}
						}
						sleep(random(300, 600));
					}
				}
				break;
			case OPENBANK:
				status = "Opening Bank";
				if (openBank < 2) {
					if (!banker.doAction("Use-quickly")) {
						camera.turnTo(banker);
					}
					mouse.moveRandomly(50);
					waitForBankIF(2000);
					sleep(random(100, 200));
					openBank++;
				} else {
					game.openTab(random(0, 17));
					sleep(random(300, 600));
					game.openTab(Game.TAB_INVENTORY);
					openBank = 0;
				}
				break;
			case OPENCHEST:
				status = "Opening Bank Chest";
				if (openBank < 2) {
					if (!banker.doAction("Bank")) {
						camera.turnTo(banker);
					}
					mouse.moveRandomly(50);
					waitForBankIF(2000);
					openBank++;
				} else {
					game.openTab(random(0, 17));
					sleep(random(300, 600));
					game.openTab(Game.TAB_INVENTORY);
					openBank = 0;
				}
				break;
			case BANK:
				status = "Banking";
				if (!bank.isOpen()) {
					break;
				}
				openBank = 0;
				if (inventory.contains(itemID)) {
					bank.depositAllExcept(mouldID);
					waitForDepositedItem(2500);
				}
				if (!gem) {
					if (checkBank(barID, 27) == 27) {
						withdraw(barID, itemsInBank);
						waitForWithdrawnItem(barID, 2000);
					} else if (checkBank(barID, 27) <= 26) {
						log.severe("Last run.");
						withdraw(barID, itemsInBank);
						waitForWithdrawnItem(barID, 2000);
					}
				} else {
					if (checkBank(barID, 13) == 13) {
						withdraw(barID, itemsInBank);
						waitForWithdrawnItem(barID, 2000);
					} else if (checkBank(barID, 13) <= 12) {
						log.severe("Last run.");
						withdraw(barID, itemsInBank);
						waitForWithdrawnItem(barID, 2000);
					}
					if (checkBank(gemID, 13) == 13) {
						withdraw(gemID, itemsInBank);
						waitForWithdrawnItem(gemID, 2000);
					} else if (checkBank(gemID, 13) <= 12) {
						log.severe("Last run.");
						withdraw(gemID, itemsInBank);
						waitForWithdrawnItem(gemID, 2000);
					}
				}
				if (readyToCraft()) {
					bank.close();
					break;
				}
			case CRAFT:
				status = "Crafting " + itemName;
				if (interfaces.getComponent(ifParent, ifChild).isValid()) {
					interfaces.getComponent(ifParent, ifChild).doAction(
							"Make All");
					sleep(random(400, 700));
				} else if (!isIdle() && inventory.contains(barID)) {
					if (!gem) {
						sleep(random(750, 1000));
					} else if (gem) {
						sleep(random(500, 750));
					}
				} else if (isIdle()
						&& !interfaces.getComponent(ifParent, ifChild)
								.isValid()) {
					RSObject smithObj = objects.getNearest(smithID);
					RSItem itemOnFurn = inventory.getItem(barID);
					if (smithObj != null && itemOnFurn != null
							&& inventory.contains(barID)) {
						if (!inventory.isItemSelected()) {
							if (itemOnFurn != null) {
								itemOnFurn.doAction("Use");
								waitForInventoryAction(1000);
							}
						}
						RSModel furModel = smithObj.getModel();
						if (furModel != null) {
							if (failCounter > 2) {
								failCounter = 0;
								inventory.clickSelectedItem();
								break;
							}
							if (doActionAtModel(furModel, furnaceAction)) {
								waitForIF(interfaces.get(ifParent),
										random(2000, 3000));
								break;
							} else {
								camera.turnTo(smithObj);
								failCounter++;
							}
							sleep(random(300, 600));
						}
					}
				}
				break;
			case WALKTOFURNACE:
				status = "Walking to Furnace";
				try {
					if (readyToCraft() && atFurnace()) {
						break;
					}
					if (step(path) == path.size()) {
						path = generatePath(pathToFurnace);
						structType = pathToFurnace;
					}
				} catch (Exception e) {
				}
				break;
			case WALKTOBANK:
				status = "Walking to Bank";
				try {
					if (readyToBank() && atBank()) {
						break;
					}
					if (step(path) == path.size()) {
						path = generatePath(pathToBank);
						structType = pathToBank;
					}
				} catch (Exception e) {
				}
				break;
			case ANTIBAN:
				try {
					int ran = random(0, 6);
					switch (ran) {
					case 0:
						int cam = random(0, 3);
						switch (cam) {
						case 0:
							status = "Camera Thread 1";
							new CameraRotateThread().start();
							break;
						case 1:
							status = "Camera Thread 2";
							new CameraRotateThread().start();
							new CameraRotateThread().start();
							break;
						case 3:
							status = "Camera Thread 3";
							new CameraHeightThread().start();
							new CameraRotateThread().start();
							break;
						}
						break;
					case 1:
						status = "Hover Player Thread";
						hoverPlayer();
						sleep(random(550, 1800));
						mouse.moveRandomly(750);
						sleep(random(400, 1000));
						break;
					case 2:
						status = "Hover Object Thread";
						hoverObject();
						sleep(random(550, 1800));
						mouse.moveRandomly(750);
						sleep(random(400, 1000));
						break;
					case 3:
						status = "Mouse Thread 1";
						if (System.currentTimeMillis() - mouse.getPressTime() > 5000) {
							Point mouseP = mouse.getLocation();
							sleep(random(500, 1000));
							if (mouseP.equals(mouse.getLocation())) {
								mouse.moveRandomly(200);
							}
						}
						break;
					case 4:
						status = "Camera Thread 4";
						new CameraHeightThread().start();
						break;
					case 5:
						status = "Camera Thread 5";
						new CameraRotateThread().start();
						break;
					case 6:
						status = "Mouse Thread 2 ";
						if (System.currentTimeMillis() - mouse.getPressTime() > 5000) {
							Point mouseP = mouse.getLocation();
							sleep(random(500, 1000));
							if (mouseP.equals(mouse.getLocation())) {
								mouse.moveRandomly(200);
							}
						}
						break;
					}
					sleep(random(100, 200));
				} catch (Exception e) {
				}
				break;
			case CHILL:
				break;
			}
		} catch (Exception ignored) {
		}
		return 50;
	}

	/**
	 * Methods
	 **/
	public void quit() {
		stopScript();
	}

	public boolean isCutting() {
		if (getMyPlayer().getAnimation() == -1) {
			sleep(random(800, 1000));
			if (getMyPlayer().getAnimation() == -1) {
				sleep(random(400, 600));
				if (getMyPlayer().getAnimation() == -1) {
					return false;
				}
			}
		}
		return true;
	}

	boolean hoverPlayer() {
		RSPlayer player = null;
		RSPlayer[] validPlayers = players.getAll();

		player = validPlayers[random(0, validPlayers.length - 1)];
		if (player != null) {
			try {
				String playerName = player.getName();
				String myPlayerName = getMyPlayer().getName();
				if (playerName.equals(myPlayerName)) {
					return false;
				}
			} catch (NullPointerException e) {
			}
			try {
				RSTile targetLoc = player.getLocation();
				// String name = player.getName();
				Point checkPlayer = calc.tileToScreen(targetLoc);
				if (calc.pointOnScreen(checkPlayer) && checkPlayer != null) {
					mouse.click(checkPlayer, 5, 5, false);
				} else {
					return false;
				}
				return true;
			} catch (Exception ignored) {
			}
		}
		return false;
	}

	public void hoverObject() {
		try {
			examineRandomObject(5);
			sleep(random(50, 1000));
			int mousemoveAfter2 = random(0, 4);
			sleep(random(100, 800));
			if (mousemoveAfter2 == 1 && mousemoveAfter2 == 2) {
				mouse.move(1, 1, 760, 500);
			}
		} catch (Exception ignored) {
		}
	}

	public RSTile examineRandomObject(int scans) {
		RSTile start = getMyPlayer().getLocation();
		ArrayList<RSTile> possibleTiles = new ArrayList<RSTile>();
		for (int h = 1; h < scans * scans; h += 2) {
			for (int i = 0; i < h; i++) {
				for (int j = 0; j < h; j++) {
					int offset = (h + 1) / 2 - 1;
					if (i > 0 && i < h - 1) {
						j = h - 1;
					}
					RSTile tile = new RSTile(start.getX() - offset + i,
							start.getY() - offset + j);
					RSObject objectToList = objects.getTopAt(tile);
					if (objectToList != null
							&& calc.tileOnScreen(objectToList.getLocation())) {
						possibleTiles.add(objectToList.getLocation());
					}
				}
			}
		}
		if (possibleTiles.size() == 0) {
			return null;
		}
		if (possibleTiles.size() > 0 && possibleTiles != null) {
			final RSTile objectLoc = possibleTiles.get(random(0,
					possibleTiles.size()));
			Point objectPoint = calc.tileToScreen(objectLoc);
			if (objectPoint != null) {
				try {
					mouse.move(objectPoint);
					if (menu.doAction("Examine")) {
					} else {
					}
					try {
						sleep(random(100, 500));
					} catch (Exception ignored) {
					}
				} catch (NullPointerException ignored) {
				}
			}
		}
		return null;
	}

	public boolean checkItem(int i) {
		if (bank.isOpen() && bank.getCount(i) <= 1) {
			sleep(random(25, 100));
			if (bank.isOpen() && bank.getCount(i) <= 1) {
				sleep(random(100, 300));
				if (bank.isOpen() && bank.getCount(i) <= 1) {
					sleep(random(400, 600));
					bank.close();
					log.severe("[FETAL] Out of (" + i + "). Stopping...");
					return false;
				}
			}
		}
		return true;
	}

	public int checkBank(int itemID, int amountToWithdraw) {
		if (checkItem(itemID)) {
			if (bank.getCount(itemID) <= amountToWithdraw) {
				itemsInBank = bank.getCount(itemID) - 1;
			} else {
				itemsInBank = amountToWithdraw;
			}
			return itemsInBank;
		} else {
			quit();
		}
		return 1;
	}

	public boolean doActionAtModel(RSModel model, String action) {
		if (model != null) {
			int iters = random(10, 15);
			while (--iters > 0 && !menu.contains(action)) {
				mouse.move(model.getPoint());
				sleep(random(20, 60));
				if (menu.contains(action)) {
					sleep(random(20, 60));
					if (menu.contains(action)) {
						sleep(random(20, 60));
						if (menu.contains(action)) {
							break;
						}
					}
				}
			}
			if (menu.contains(action)) {
				return menu.doAction(action);
			} else {
				mouse.move(model.getPoint());
				return menu.doAction(action);
			}
		}
		return false;
	}

	public boolean readyToCraft() {
		if (!gem) {
			if (inventory.contains(barID) && inventory.contains(mouldID)) {
				return true;
			} else {
				return false;
			}
		} else {
			if (inventory.contains(barID) && inventory.contains(gemID)
					&& inventory.contains(mouldID)) {
				return true;
			} else {
				return false;
			}
		}
	}

	public boolean readyToBank() {
		if (inventory.contains(itemID) && !inventory.contains(barID)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isIdle() {
		if (getMyPlayer().getAnimation() == -1) {
			sleep(random(800, 1000));
			if (getMyPlayer().getAnimation() == -1) {
				sleep(random(400, 600));
				if (getMyPlayer().getAnimation() == -1) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean waitToMove(int timeout) {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < timeout) {
			if (getMyPlayer().isMoving()) {
				return true;
			}
			sleep(5, 15);
		}
		return false;
	}

	public boolean atBank() {
		return AREAS[0].contains(getMyPlayer().getLocation());
	}

	public boolean atFurnace() {
		return AREAS[1].contains(getMyPlayer().getLocation());
	}

	private boolean withdraw(final int itemID, final int count) {
		if (count < 0)
			throw new IllegalArgumentException("count < 0 (" + count + ")");
		if (!bank.isOpen())
			return false;
		final RSItem item = bank.getItem(itemID);
		if (item == null || !item.isComponentValid())
			return false;
		final int inventoryCount = inventory.getCount(true);
		switch (count) {
		case 0: // Withdraw All
			item.doAction("Withdraw-All");
			break;
		case 1: // Withdraw 1
			item.doClick(true);
			break;
		case 5: // Withdraw 5
		case 10: // Withdraw 10
			item.doAction("Withdraw-" + count);
			break;
		default: // Withdraw x
			if (item.doClick(false)) {
				sleep(random(200, 500));
				if (menu.contains("Withdraw-" + count)) {
					if (menu.doAction("Withdraw-" + count)) {
						sleep(random(100, 200));
						return true;
					}
					return false;
				}
				if (item.doAction("Withdraw-X")) {
					sleep(random(1000, 1300));
					keyboard.sendText("" + count, true);
				}
				sleep(random(100, 200));
			}
			break;
		}
		return (inventory.getCount(true) > inventoryCount)
				|| (inventory.getCount(true) == 28);
	}

	public boolean waitForIF(RSInterface iface, int timeout) {
		long start = System.currentTimeMillis();

		while (System.currentTimeMillis() - start < timeout) {
			if (iface.isValid()) {
				return true;
			}
			sleep(100);
		}
		return false;
	}

	public boolean waitForInventoryAction(int timeout) {
		long start = System.currentTimeMillis();

		while (System.currentTimeMillis() - start < timeout) {
			if (inventory.isItemSelected()) {
				return true;
			}
			sleep(100);
		}
		return false;
	}

	private boolean waitForWithdrawnItem(int itemID, int timeout) {
		int startCount = inventory.getCount(itemID);
		long start = System.currentTimeMillis();

		while (System.currentTimeMillis() - start < timeout) {
			if (inventory.getCount(itemID) > startCount) {
				return true;
			}
			sleep(100);
		}
		return false;
	}

	private boolean waitForDepositedItem(int timeout) {
		long start = System.currentTimeMillis();
		int startCount = inventory.getCount(true);

		while (System.currentTimeMillis() - start < timeout) {
			if (inventory.getCount(true) < startCount) {
				return true;
			}
			sleep(100);
		}
		return false;
	}

	public boolean waitForBankIF(int timeout) {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < timeout) {
			if (interfaces.get(Bank.INTERFACE_BANK).isValid()) {
				return true;
			}
			sleep(100);
		}
		return false;
	}

	public boolean waitForEnableRun(int timeout) {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < timeout) {
			if (walking.isRunEnabled()) {
				return true;
			}
			sleep(100);
		}
		return false;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void onRepaint(Graphics a) {
		int currentExp = skills.getCurrentExp(Skills.CRAFTING);
		int currentLvl = skills.getCurrentLevel(Skills.CRAFTING);
		gainedXp = currentExp - startExp;
		gainedLvl = currentLvl - startLvl;
		itemsMade = (gainedXp / exp);
		int cutPerHour = (int) (itemsMade * 3600000D / (System
				.currentTimeMillis() - startTime));
		int expPerHour = (int) ((gainedXp * 3600000D) / (System
				.currentTimeMillis() - startTime));
		double profit, r__pph;
		if (gem) {
			profit = (double) ((itemsMade * itemsPrice) - ((barPrice * itemsMade) + (gemPrice * itemsMade))) / 1000;
			r__pph = ((itemsMade * itemsPrice * 3600000D)
					/ ((System.currentTimeMillis() - startTime)
							- (+cutPerHour * gemPrice) + (+cutPerHour * barPrice)) / 1000);
		} else {
			profit = (double) ((itemsMade * itemsPrice) - (barPrice * itemsMade)) / 1000;
			r__pph = ((itemsMade * itemsPrice * 3600000D
					/ (System.currentTimeMillis() - startTime) - (+cutPerHour * barPrice)) / 1000);
		}
		long yr__pph = (long) (r__pph * 100);
		double profitPerHour = (double) yr__pph / 100;
		long millis = System.currentTimeMillis() - startTime;
		long hours = millis / (1000 * 60 * 60);
		millis -= hours * (1000 * 60 * 60);
		long minutes = millis / (1000 * 60);
		millis -= minutes * (1000 * 60);
		long seconds = millis / 1000;
		if (hours <= 9) {
			hh = "0";
		} else {
			hh = "";
		}
		if (minutes <= 9) {
			mm = "0";
		} else {
			mm = "";
		}
		if (seconds <= 9) {
			ss = "0";
		} else {
			ss = "";
		}
		expToCraLvl = skills.getExpToNextLevel(Skills.CRAFTING);
		if ((minutes > 0 || hours > 0 || seconds > 0) && gainedXp > 0) {
			secCraExp = (float) gainedXp
					/ (float) (seconds + minutes * 60 + hours * 60 * 60);
		}
		if (secCraExp > 0) {
			secCraLvl = (int) (expToCraLvl / secCraExp);
		}
		if (secCraLvl >= 60) {
			minCraLvl = secCraLvl / 60;
			secCraLvl -= minCraLvl * 60;
		} else {
			minCraLvl = 0;
		}
		if (minCraLvl >= 60) {
			hrCraLvl = minCraLvl / 60;
			minCraLvl -= hrCraLvl * 60;
		} else {
			hrCraLvl = 0;
		}
		if (secCraLvl <= 9) {
			secC0 = "0";
		} else {
			secC0 = "";
		}
		if (minCraLvl <= 9) {
			minC0 = "0";
		} else {
			minC0 = "";
		}
		if (hrCraLvl <= 9) {
			hrC0 = "0";
		} else {
			hrC0 = "";
		}
		itemsTL = expToCraLvl / exp;
		Graphics2D g = (Graphics2D) a;
		if (!globalBanking) {
			for (int i = 1; i < pathToFurnace.length; i++) {
				pathToFurnace[i].drawTo(a, pathToFurnace[i - 1]);
			}
			for (int i = 1; i < pathToBank.length; i++) {
				pathToBank[i].drawTo(a, pathToBank[i - 1]);
			}
		}
		int currentCraftingLevel = skills.getCurrentLevel(Skills.CRAFTING);
		int percentToNextCraftingLevel = skills
				.getPercentToNextLevel(Skills.CRAFTING);
		float t = skills.getPercentToNextLevel(Skills.CRAFTING);
		float h = t / 100;

		// NEWPAINT
		g.setColor(new Color(0, 0, 0, 168));
		g.fillRect(5, 70, 200, 16);
		g.setColor(Color.ORANGE);
		g.drawRect(5, 70, 200, 16);
		g.setFont(new Font("System", Font.BOLD, 12));
		g.drawString(scriptName + " v." + version + " by Aut0r", 8, 84);

		g.setColor(new Color(0, 0, 0, 168));
		g.fillRect(5, 90, 200, 70);
		g.setColor(Color.ORANGE);
		g.drawRect(5, 90, 200, 70);
		g.setFont(new Font("System", Font.PLAIN, 10));
		g.setColor(Color.WHITE);
		g.drawString("Time Running: " + hh + hours + ":" + mm + minutes + ":"
				+ ss + seconds, 8, 102);
		g.drawString(itemName + "s " + action + ": " + itemsMade + " ("
				+ cutPerHour + " p/h)", 8, 113);
		g.drawString("Profit: " + profit + "k (" + profitPerHour + "k p/h)", 8,
				124);
		g.drawString("Xp/Lvls Gained: " + gainedXp + " xp (" + gainedLvl
				+ " lvls)", 8, 135);
		g.drawString("Xp/Hr: " + expPerHour + " p/h", 8, 146);
		g.drawString("Status: " + status, 8, 157);
		// Progress Bar
		g.setColor(new Color(0, 0, 0, 168));
		g.fillRect(5, 153 + 11, 200, 22);
		g.setColor(Color.ORANGE);
		g.drawRect(5, 153 + 11, 200, 22);

		g.setColor(new Color(254, 110, 0, 128));
		g.fillRect(8, 155 + 13, (int) (h * 194), 14);
		g.setColor(Color.ORANGE);
		g.drawRect(8, 155 + 13, 194, 14);
		g.setColor(Color.WHITE);
		g.setFont(new Font("System", Font.BOLD, 10));
		g.drawString("Crafting " + currentCraftingLevel + "  | "
				+ percentToNextCraftingLevel + "% Lvl  | TTL: [ " + hrC0
				+ hrCraLvl + ":" + minC0 + minCraLvl + ":" + secC0 + secCraLvl
				+ " ]", 9, 179);
		g.setFont(new Font("System", Font.BOLD, 12));
		g.setColor(Color.ORANGE);
		g.drawString("http://aut0rprogramming.net", 350, 333);
	}

	public int getDistance2(RSTile t) {
		return (Math.abs(getMyPlayer().getLocation().getX() - t.getX()) + Math
				.abs(getMyPlayer().getLocation().getY() - t.getY()));
	}

	private boolean tileInNextRange(RSTile t) {
		return calc.distanceBetween(t, getMyPlayer().getLocation()) < nextStep;
	}

	private int nextStep = 12;

	private int step(ArrayList<RSTile> path) {
		if (calc.distanceBetween(getMyPlayer().getLocation(),
				path.get(path.size() - 1)) < 2) {
			return path.size();
		}
		RSTile dest = walking.getDestination();
		int index = -1;
		int shortestDist = 0, dist, shortest = -1;
		if (dest != null) {
			for (int i = 0; i < path.size(); i++) {
				dist = (int) calc.distanceBetween(path.get(i), dest);
				if (shortest < 0 || shortestDist > dist) {
					shortest = i;
					shortestDist = dist;
				}
			}
		}
		for (int i = path.size() - 1; i >= 0; i--) {
			if (tileInNextRange(path.get(i))) {
				index = i;
				break;
			}
		}
		if (index >= 0
				&& (dest == null || (index > shortest) || !getMyPlayer()
						.isMoving())) {
			if (walking.walkTileMM(path.get(index))) {
				waitToMove(random(600, 900));
			}
			nextStep = random(13, 15);
			while (walking.getDestination() != null
					&& (calc.distanceBetween(walking.getDestination(),
							getMyPlayer().getLocation()) >= 5)) {
				sleep(random(50, 100));
			}
			restructPath();
			return index;
		}
		return -1;
	}

	private void restructPath() {
		path = generatePath(structType);
	}

	private ArrayList<RSTile> generatePath(Line[] lines) { // you're generating
															// the path at
															// walking start,
															// you need to
															// gernerate it AS
															// YOU WALK.
		double minStep = 5, maxStep = 10, wander = 3;
		if (lines.length < 2) {
			return null;
		}
		path = new ArrayList<RSTile>();
		Line l1, l2 = lines[0];
		double distFromCenter = random(0, l2.getDistance() + 1);
		RSTile p = l2.translate((int) distFromCenter);
		distFromCenter = l2.getDistance() / 2 - distFromCenter;
		double centerXdist, centerYdist, line1Xdist, line1Ydist, line2Xdist, line2Ydist;
		double line1dist, line2dist, centerDist;
		double x, y;
		double distOnLine, last, cap1, cap2, move;
		double distFromCenterX1, distFromCenterY1, distFromCenterX2, distFromCenterY2;
		double force1, force2, slopeX, slopeY, slopeDist;
		boolean finished;
		int lastX = p.getX(), lastY = p.getY(), curX, curY;
		double dist, xdist, ydist;
		for (int i = 1; i < lines.length; i++) {
			l1 = l2;
			l2 = lines[i];
			centerXdist = l2.getCenterX() - l1.getCenterX();
			centerYdist = l2.getCenterY() - l1.getCenterY();
			centerDist = Math.sqrt(centerXdist * centerXdist + centerYdist
					* centerYdist);
			line1Xdist = l2.getX() - l1.getX();
			line1Ydist = l2.getY() - l1.getY();
			line2Xdist = l2.getX2() - l1.getX2();
			line2Ydist = l2.getY2() - l1.getY2();
			centerXdist /= centerDist;
			centerYdist /= centerDist;
			line1Xdist /= centerDist;
			line1Ydist /= centerDist;
			line2Xdist /= centerDist;
			line2Ydist /= centerDist;
			distOnLine = 0;
			last = 0;
			finished = false;
			while (!finished) {
				distOnLine += random(minStep, maxStep);
				if (distOnLine >= centerDist) {
					distOnLine = centerDist;
					finished = true;
				}
				x = centerXdist * distOnLine + l1.getCenterX();
				y = centerYdist * distOnLine + l1.getCenterY();

				distFromCenterX1 = x - (line1Xdist * distOnLine + l1.getX());
				distFromCenterY1 = y - (line1Ydist * distOnLine + l1.getY());

				distFromCenterX2 = x - (line2Xdist * distOnLine + l1.getX2());
				distFromCenterY2 = y - (line2Ydist * distOnLine + l1.getY2());

				slopeX = distFromCenterX2 - distFromCenterX1;
				slopeY = distFromCenterY2 - distFromCenterY1;
				slopeDist = Math.sqrt(slopeX * slopeX + slopeY * slopeY);
				slopeX /= slopeDist;
				slopeY /= slopeDist;

				line1dist = Math.sqrt(distFromCenterX1 * distFromCenterX1
						+ distFromCenterY1 * distFromCenterY1);
				line2dist = Math.sqrt(distFromCenterX2 * distFromCenterX2
						+ distFromCenterY2 * distFromCenterY2);

				move = (distOnLine - last) / maxStep * wander;

				force1 = line1dist + distFromCenter;
				force2 = line2dist - distFromCenter;

				cap1 = Math.min(move, force1);
				cap2 = Math.min(move, force2);

				if (force1 < 0) {
					distFromCenter -= force1;
				} else if (force2 < 0) {
					distFromCenter += force2;
				} else {
					distFromCenter += random(-cap1, cap2);
				}
				if (finished) {
					RSTile t = l2.translateFromCenter(distFromCenter);
					curX = t.getX();
					curY = t.getY();
				} else {
					curX = (int) Math.round(distOnLine * centerXdist
							+ l1.getCenterX() + distFromCenter * slopeX);
					curY = (int) Math.round(distOnLine * centerYdist
							+ l1.getCenterY() + distFromCenter * slopeY);
				}
				xdist = curX - lastX;
				ydist = curY - lastY;
				dist = Math.sqrt(xdist * xdist + ydist * ydist);
				xdist /= dist;
				ydist /= dist;
				for (int j = 0; j < dist; j++) {
					if (objects.getTopAt(new RSTile((int) Math.round(xdist * j
							+ lastX), (int) Math.round(ydist * j + lastY))) == null) {
						path.add(new RSTile(
								(int) Math.round(xdist * j + lastX), (int) Math
										.round(ydist * j + lastY)));
					} else {
						RSTile ran = null;
						ran = new RSTile(
								(int) Math.round(xdist * j + lastX) + 1,
								(int) Math.round(ydist * j + lastY));
						if (objects.getTopAt(ran) == null) {
							path.add(ran);
							continue;
						}
						ran = new RSTile(
								(int) Math.round(xdist * j + lastX) - 1,
								(int) Math.round(ydist * j + lastY));
						if (objects.getTopAt(ran) == null) {
							path.add(ran);
							continue;
						}
						ran = new RSTile((int) Math.round(xdist * j + lastX),
								(int) Math.round(ydist * j + lastY) + 1);
						if (objects.getTopAt(ran) == null) {
							path.add(ran);
							continue;
						}
						ran = new RSTile((int) Math.round(xdist * j + lastX),
								(int) Math.round(ydist * j + lastY) - 1);
						if (objects.getTopAt(ran) == null) {
							path.add(ran);
							continue;
						}
						ran = new RSTile(
								(int) Math.round(xdist * j + lastX) + 1,
								(int) Math.round(ydist * j + lastY) + 1);
						if (objects.getTopAt(ran) == null) {
							path.add(ran);
							continue;
						}
						ran = new RSTile(
								(int) Math.round(xdist * j + lastX) + 1,
								(int) Math.round(ydist * j + lastY) - 1);
						if (objects.getTopAt(ran) == null) {
							path.add(ran);
							continue;
						}
						ran = new RSTile(
								(int) Math.round(xdist * j + lastX) - 1,
								(int) Math.round(ydist * j + lastY) + 1);
						if (objects.getTopAt(ran) == null) {
							path.add(ran);
							continue;
						}
						ran = new RSTile(
								(int) Math.round(xdist * j + lastX) - 1,
								(int) Math.round(ydist * j + lastY) - 1);
						if (objects.getTopAt(ran) == null) {
							path.add(ran);
							continue;
						}
					}
				}
				last = distOnLine;
				lastX = curX;
				lastY = curY;
			}
		}
		return cutUp(path);
	}

	public ArrayList<RSTile> cutUp(ArrayList<RSTile> tiles) {
		path = new ArrayList<RSTile>();
		int index = 0;
		while (index < tiles.size()) {
			path.add(tiles.get(index));
			index += random(8, 12);
		}
		if (!path.get(path.size() - 1).equals(tiles.get(tiles.size() - 1))) {
			path.add(tiles.get(tiles.size() - 1));
		}

		return path;
	}

	private class Line {

		private final int x, y, xdist, ydist, x2, y2, centerX, centerY;
		private final RSTile t1, t2;
		private final double dist;

		public Line(int x1, int y1, int x2, int y2) {

			t1 = new RSTile(x1, y1);
			t2 = new RSTile(x2, y2);
			x = x1;
			y = y1;
			this.x2 = x2;
			this.y2 = y2;
			xdist = x2 - x1;
			ydist = y2 - y1;
			centerX = x + (int) (0.5 * xdist);
			centerY = y + (int) (0.5 * ydist);
			dist = Math.sqrt(xdist * xdist + ydist * ydist);
		}

		public int getCenterX() {
			return centerX;
		}

		public int getCenterY() {
			return centerY;
		}

		public RSTile getTile1() {
			return t1;
		}

		public RSTile getTile2() {
			return t2;
		}

		public void drawTo(Graphics g, Line line) {
			if (!calc.tileOnMap(t1) || !calc.tileOnMap(t2)) {
				return;
			}
			if (calc.tileOnMap(line.getTile1())
					&& calc.tileOnMap(line.getTile2())) {
				Point p1 = calc.tileToMinimap(t1);
				Point p2 = calc.tileToMinimap(t2);
				Point p3 = calc.tileToMinimap(line.getTile2());
				Point p4 = calc.tileToMinimap(line.getTile1());
				GeneralPath path = new GeneralPath();
				path.moveTo(p1.x, p1.y);
				path.lineTo(p2.x, p2.y);
				path.lineTo(p3.x, p3.y);
				path.lineTo(p4.x, p4.y);
				path.closePath();
				g.setColor(transGreen);
				((Graphics2D) g).fill(path);
				((Graphics2D) g).draw(path);
			}
			Point last = null, p;
			g.setColor(Color.BLACK);// where do you walk.
			for (RSTile t : path) {
				if (calc.tileOnMap(t)) {
					p = calc.tileToMinimap(t);
					if (last != null) {
						g.drawLine(p.x, p.y, last.x, last.y);
					}
					last = p;
				} else {
					last = null;
				}
			}
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public int getX2() {
			return x2;
		}

		public int getY2() {
			return y2;
		}

		public double getDistance() {
			return dist;
		}

		public RSTile translate(double length) {
			return new RSTile((int) Math.round(length * (xdist / dist)) + x,
					(int) Math.round(length * (ydist / dist)) + y);
		}

		public RSTile translateFromCenter(double length) {
			return new RSTile((int) Math.round(centerX - (xdist / dist)
					* length), (int) Math.round(centerY - (ydist / dist)
					* length));
		}
	}

	public class GUI extends JFrame {

		private static final long serialVersionUID = 1L;

		public GUI() {
			initComponents();
		}

		private void initComponents() {
			dialogPane = new JPanel();
			contentPanel = new JPanel();
			tabbedPane2 = new JTabbedPane();
			panel1 = new JPanel();
			loc = new JComboBox();
			label1 = new JLabel();
			label2 = new JLabel();
			type = new JComboBox();
			craftObj = new JComboBox();
			craftingType = new JComboBox();
			mouseSpeed = new JComboBox();
			panel2 = new JPanel();
			label3 = new JLabel();
			label4 = new JLabel();
			label5 = new JLabel();
			label6 = new JLabel();
			label7 = new JLabel();
			buttonBar = new JPanel();
			okButton = new JButton();
			cancelButton = new JButton();

			// Title
			setTitle(scriptName + " v." + version);
			Container contentPane = getContentPane();
			contentPane.setLayout(new BorderLayout());

			{
				dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
				dialogPane.setLayout(new BorderLayout());

				{
					contentPanel.setLayout(null);

					{

						// @START - TAB 1
						{
							panel1.setLayout(null);
							// @START CRAFTINGTYPE
							craftingType
									.setModel(new javax.swing.DefaultComboBoxModel(
											new String[] { "",
													"Gold Jewellery",
													"Silver Jewellery",
													"Cut Gems" }));
							panel1.add(craftingType);
							craftingType.setBounds(5, 30, 115,
									type.getPreferredSize().height);
							craftingType
									.addActionListener(new ActionListener() {
										@Override
										public void actionPerformed(
												final ActionEvent evt) {
											final String co = (String) craftingType
													.getSelectedItem();
											if (co == "Gold Jewellery") {
												craftObj.setModel(new javax.swing.DefaultComboBoxModel(
														new String[] { "Gold",
																"Sapphire",
																"Emerald",
																"Ruby",
																"Diamond" }));
												type.setModel(new javax.swing.DefaultComboBoxModel(
														new String[] { "",
																"Ring",
																"Necklace",
																"Bracelet",
																"Amulet" }));
												loc.setModel(new DefaultComboBoxModel(
														new String[] {
																"Al Kharid",
																"Edgeville",
																"Neitiznot(not operational)" }));
												// @START - COMBOBOX - TYPE
												panel1.add(type);
												type.setBounds(
														170,
														70,
														115,
														type.getPreferredSize().height);
												// @END - COMBOBOX - TYPE
												label6.setText("Make what?");
												label6.setFont(new Font(
														"Tahoma", Font.BOLD, 11));
												panel1.add(label6);
												label6.setBounds(170, 45, 120,
														35);
											} else if (co == "Silver Jewellery") {
												craftObj.setModel(new javax.swing.DefaultComboBoxModel(
														new String[] { "Silver" }));
												type.setModel(new javax.swing.DefaultComboBoxModel(
														new String[] { "",
																"Holy Symbol",
																"Tiara" }));
												loc.setModel(new DefaultComboBoxModel(
														new String[] {
																"Al Kharid",
																"Edgeville",
																"Neitiznot(not operational)" }));
												// @START - COMBOBOX - TYPE
												panel1.add(type);
												type.setBounds(
														170,
														70,
														115,
														type.getPreferredSize().height);
												// @END - COMBOBOX - TYPE
												label6.setText("Make what?");
												label6.setFont(new Font(
														"Tahoma", Font.BOLD, 11));
												panel1.add(label6);
												label6.setBounds(170, 45, 120,
														35);
											} else if (co == "Cut Gems") {
												craftObj.setModel(new javax.swing.DefaultComboBoxModel(
														new String[] {
																"Sapphire",
																"Emerald",
																"Ruby",
																"Diamond",
																"Dragonstone" }));
												loc.setModel(new DefaultComboBoxModel(
														new String[] { "Any Location" }));
											}
										}
									});
							// @START CRAFTOBJ
							panel1.add(craftObj);
							craftObj.setBounds(170, 30, 115,
									type.getPreferredSize().height);

							// @START - COMBOBOX - LOCATION
							panel1.add(loc);
							loc.setBounds(5, 70, 115, 20);
							// @END - COMBOBOX - LOCATION

							// @START - COMBOBOX - MOUSESPEED
							mouseSpeed
									.setModel(new javax.swing.DefaultComboBoxModel(
											new String[] { "Slow",
													"Slow-Medium",
													"Medium(Recommended)",
													"Medium-Fast(Recommended)",
													"Fast**", "Super Fast**" }));
							panel1.add(mouseSpeed);
							mouseSpeed.setBounds(170, 110, 115,
									mouseSpeed.getPreferredSize().height);
							// @END - COMBOBOX - MOUSESPEED

							// @START LABELS(PANEL 1)
							label7.setText("What to Craft?");
							label7.setFont(new Font("Tahoma", Font.BOLD, 11));
							panel1.add(label7);
							label7.setBounds(5, 5, 100, 35);

							label1.setText("Where?");
							label1.setFont(new Font("Tahoma", Font.BOLD, 11));
							panel1.add(label1);
							label1.setBounds(5, 45, 100, 35);

							label2.setText("Type?");
							label2.setFont(new Font("Tahoma", Font.BOLD, 11));
							panel1.add(label2);
							label2.setBounds(170, 5, 120, 35);

							label5.setText("Mouse Speed?");
							label5.setFont(new Font("Tahoma", Font.BOLD, 11));
							panel1.add(label5);
							label5.setBounds(170, 85, 120, 35);
							// @END LABELS(PANEL 1)
							{ // compute preferred size
								Dimension preferredSize = new Dimension();
								for (int i = 0; i < panel1.getComponentCount(); i++) {
									Rectangle bounds = panel1.getComponent(i)
											.getBounds();
									preferredSize.width = Math
											.max(bounds.x + bounds.width,
													preferredSize.width);
									preferredSize.height = Math.max(bounds.y
											+ bounds.height,
											preferredSize.height);
								}
								Insets insets = panel1.getInsets();
								preferredSize.width += insets.right;
								preferredSize.height += insets.bottom;
								panel1.setMinimumSize(preferredSize);
								panel1.setPreferredSize(preferredSize);
							}
						}
						tabbedPane2.addTab("Info", panel1);

						// ======== panel2 ========
						{
							panel2.setLayout(null);

							// ---- label3 ----
							label3.setText("http://aut0rprogramming.net");
							label3.setFont(new Font("Cambria", Font.BOLD, 12));
							panel2.add(label3);
							label3.setBounds(5, 5,
									label3.getPreferredSize().width, 15);

							// ---- label4 ----
							label4.setText("Ver." + version);
							label4.setFont(new Font("Cambria", Font.BOLD, 10));
							panel2.add(label4);
							label4.setBounds(5, 15,
									label4.getPreferredSize().width, 20);

							{ // compute preferred size
								Dimension preferredSize = new Dimension();
								for (int i = 0; i < panel2.getComponentCount(); i++) {
									Rectangle bounds = panel2.getComponent(i)
											.getBounds();
									preferredSize.width = Math
											.max(bounds.x + bounds.width,
													preferredSize.width);
									preferredSize.height = Math.max(bounds.y
											+ bounds.height,
											preferredSize.height);
								}
								Insets insets = panel2.getInsets();
								preferredSize.width += insets.right;
								preferredSize.height += insets.bottom;
								panel2.setMinimumSize(preferredSize);
								panel2.setPreferredSize(preferredSize);
							}
						}
						tabbedPane2.addTab("Credits", panel2);

					}
					contentPanel.add(tabbedPane2);
					tabbedPane2.setBounds(5, 0, 315, 220);

					{ // compute preferred size
						Dimension preferredSize = new Dimension();
						for (int i = 0; i < contentPanel.getComponentCount(); i++) {
							Rectangle bounds = contentPanel.getComponent(i)
									.getBounds();
							preferredSize.width = Math.max(bounds.x
									+ bounds.width, preferredSize.width);
							preferredSize.height = Math.max(bounds.y
									+ bounds.height, preferredSize.height);
						}
						Insets insets = contentPanel.getInsets();
						preferredSize.width += insets.right;
						preferredSize.height += insets.bottom;
						contentPanel.setMinimumSize(preferredSize);
						contentPanel.setPreferredSize(preferredSize);
					}
				}
				dialogPane.add(contentPanel, BorderLayout.CENTER);

				// ======== buttonBar ========
				{
					buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
					buttonBar.setLayout(new GridBagLayout());
					((GridBagLayout) buttonBar.getLayout()).columnWidths = new int[] {
							0, 85, 80 };
					((GridBagLayout) buttonBar.getLayout()).columnWeights = new double[] {
							1.0, 0.0, 0.0 };

					// ---- okButton ----
					okButton.setText("Start " + scriptName);
					buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1,
							0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0,
							0));
					okButton.addActionListener(new java.awt.event.ActionListener() {

						@Override
						public void actionPerformed(
								java.awt.event.ActionEvent evt) {
							StartActionPerformed(evt);
						}
					});

					// ---- cancelButton ----
					cancelButton.setText("Cancel");
					buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1,
							1, 0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,
							0));
					cancelButton
							.addActionListener(new java.awt.event.ActionListener() {

								@Override
								public void actionPerformed(
										java.awt.event.ActionEvent evt) {
									CancelActionPerformed(evt);
								}
							});
				}
				dialogPane.add(buttonBar, BorderLayout.SOUTH);
			}
			contentPane.add(dialogPane, BorderLayout.CENTER);
			pack();
			setLocationRelativeTo(getOwner());
			// //GEN-END:initComponents
		}

		private void StartActionPerformed(java.awt.event.ActionEvent evt) {
			String location = (String) loc.getSelectedItem();
			String uo = (String) type.getSelectedItem();
			String co = (String) craftingType.getSelectedItem();
			String go = (String) craftObj.getSelectedItem();
			String speed = (String) mouseSpeed.getSelectedItem();

			if (speed.equals("Slow")) {
				speedMouse = random(10, 11);
			} else if (speed.equals("Slow-Medium")) {
				speedMouse = random(7, 10);
			} else if (speed.equals("Medium(Recommended)")) {
				speedMouse = random(6, 7);
			} else if (speed.equals("Medium-Fast(Recommended)")) {
				speedMouse = random(5, 6);
			} else if (speed.equals("Fast**")) {
				speedMouse = random(3, 5);
			} else if (speed.equals("Super Fast**")) {
				speedMouse = random(2, 3);
			}
			if (location.contains("harid")) {
				pathToFurnace = new Line[] { new Line(3275, 3172, 3279, 3174),
						new Line(3280, 3183, 3282, 3187),
						new Line(3275, 3185, 3275, 3187) };
				pathToBank = new Line[] { new Line(3280, 3183, 3282, 3187),
						new Line(3275, 3172, 3279, 3174),
						new Line(3269, 3164, 3269, 3170) };
				AREAS = new RSArea[] {
						new RSArea(new RSTile(3269, 3164), new RSTile(3271,
								3170)),
						new RSArea(new RSTile(3274, 3184), new RSTile(3277,
								3188)) };
				bankID = 35647;
				smithID = 11666;
				craftLoc = "Al Kharid";
			} else if (location.contains("Edgeville")) {
				pathToFurnace = new Line[] { new Line(3103, 3498, 3105, 3500),
						new Line(3108, 3500, 3109, 3502) };
				pathToBank = new Line[] { new Line(3103, 3498, 3105, 3500),
						new Line(3096, 3496, 3098, 3497) };
				AREAS = new RSArea[] {
						new RSArea(new RSTile(3095, 3496), new RSTile(3098,
								3498)),
						new RSArea(new RSTile(3107, 3498), new RSTile(3113,
								3503)) };
				bankID = 26972;
				smithID = 26814;
				craftLoc = "Edgeville";
			} else if (location.contains("Neitiznot")) {
				pathToFurnace = new Line[] { new Line(2335, 3803, 2337, 3804),
						new Line(2341, 3803, 2341, 3805),
						new Line(2343, 3810, 2345, 3810) };
				pathToBank = new Line[] { new Line(2341, 3803, 2341, 3805),
						new Line(2335, 3803, 2337, 3804),
						new Line(2335, 3807, 2338, 3807) };
				AREAS = new RSArea[] {
						new RSArea(new RSTile(2334, 3805), new RSTile(2339,
								3808)),
						new RSArea(new RSTile(2343, 3809), new RSTile(2345,
								3811)) };
				bankID = 21301;
				smithID = 21303;
				craftLoc = "Neitiznot";
			} else if (location.contains("Location")) {
				globalBanking = true;
			}
			if (co.equals("Gold Jewellery")) {
				barName = "Gold bar";
				barID = goldBarID;
				action = "crafted";
				if (go.equals("Gold")) {
					if (uo.equals("Ring")) {// Gold Ring
						ifParent = 446;
						ifChild = 82;
						itemID = 1635;
						mouldID = 1592;
						exp = 15;
						itemName = "Gold ring";
					} else if (uo.equals("Necklace")) {// Gold Necklace
						ifParent = 446;
						ifChild = 68;
						itemID = 1654;
						mouldID = 1597;
						exp = 20;
						itemName = "Gold necklace";
					} else if (uo.equals("Bracelet")) {// Gold Bracelet
						ifParent = 446;
						ifChild = 33;
						itemID = 11069;
						mouldID = 11065;
						exp = 25;
						itemName = "Gold bracelet";
					} else if (uo.equals("Amulet")) {// Gold Amulet
						ifParent = 446;
						ifChild = 53;
						itemID = 1673;
						mouldID = 1595;
						exp = 30;
						itemName = "Gold amulet";
					}
				} else if (go.equals("Sapphire")) {
					if (uo.equals("Ring")) {// Sapphire Ring
						ifParent = 446;
						ifChild = 84;
						itemID = 1637;
						enchantedID = 2550;
						mouldID = 1592;
						gem = true;
						gemID = sapphire;
						component = 29;
						exp = 40;
						itemName = "Saphire ring";
						enchantedName = "Ring of recoil";
						gemName = "Sapphire";
						spellName = "1 Enchant";
					} else if (uo.equals("Necklace")) {// Sapphire Necklace
						ifParent = 446;
						ifChild = 70;
						itemID = 1656;
						enchantedID = 3853;
						gem = true;
						gemID = sapphire;
						mouldID = 1597;
						component = 29;
						exp = 55;
						enchantedName = "Games necklace (8)";
						gemName = "Sapphire";
						spellName = "1 Enchant";
						itemName = "Sapphire necklace";
					} else if (uo.equals("Bracelet")) {// Sapphire Bracelet
						ifParent = 446;
						ifChild = 35;
						itemID = 11072;
						enchantedID = 11074;
						gem = true;
						gemID = sapphire;
						mouldID = 11065;
						component = 29;
						exp = 60;
						enchantedName = "Bracelet of clay";
						gemName = "Sapphire";
						spellName = "1 Enchant";
						itemName = "Sapphire Bracelet";
					} else if (uo.equals("Amulet")) {// Sapphire Amulet
						ifParent = 446;
						ifChild = 55;
						itemID = 1675;
						gem = true;
						gemID = sapphire;
						mouldID = 1595;
						exp = 65;
						// spellName = "0"; Coming Soon.
						enchantedName = "Amulet of magic";
						gemName = "Sapphire";
						itemName = "Sapphire amulet";
					}
				} else if (go.equals("Emerald")) {
					if (uo.equals("Ring")) {// Emerald Ring
						ifParent = 446;
						ifChild = 86;
						itemID = 1639;
						enchantedID = 2552;
						gem = true;
						gemID = emerald;
						mouldID = 1592;
						component = 41;
						exp = 55;
						enchantedName = "Ring of duelling (8)";
						gemName = "Emerald";
						spellName = "2 Enchant";
						itemName = "Emerald ring";
					} else if (uo.equals("Necklace")) {// Emerald Necklace
						ifParent = 446;
						ifChild = 72;
						itemID = 1658;
						enchantedID = 5521;
						gem = true;
						gemID = emerald;
						mouldID = 1597;
						component = 41;
						exp = 60;
						enchantedName = "Binding necklace";
						gemName = "Emerald";
						spellName = "2 Enchant";
						itemName = "Emerald necklace";
					} else if (uo.equals("Bracelet")) {// Emerald Bracelet
						ifParent = 446;
						ifChild = 37;
						itemID = 11076;
						enchantedID = 11079;
						gem = true;
						gemID = emerald;
						mouldID = 11065;
						component = 41;
						exp = 65;
						enchantedName = "Castle wars brace (3)";
						gemName = "Emerald";
						spellName = "2 Enchant";
						itemName = "Emerald bracelet";
					} else if (uo.equals("Amulet")) {// Emerald Amulet
						ifParent = 446;
						ifChild = 57;
						itemID = 1677;
						gem = true;
						gemID = emerald;
						mouldID = 1595;
						exp = 70;
						// spellName = "0"; Coming Soon
						enchantedName = "Amulet of defence";
						gemName = "Emerald";
						itemName = "Emerald amulet";
					}
				} else if (go.equals("Ruby")) {
					if (uo.equals("Ring")) {// Ruby Ring
						ifParent = 446;
						ifChild = 88;
						itemID = 1641;
						enchantedID = 2568;
						mouldID = 1592;
						gem = true;
						gemID = ruby;
						component = 53;
						exp = 70;
						enchantedName = "Ring of forging";
						gemName = "Ruby";
						spellName = "3 Enchant";
						itemName = "Ruby Ring";
					} else if (uo.equals("Necklace")) {// Ruby Necklace
						ifParent = 446;
						ifChild = 74;
						itemID = 1660;
						enchantedID = 11194;
						gem = true;
						gemID = ruby;
						mouldID = 1597;
						component = 53;
						exp = 75;
						enchantedName = "Digsite pendant (5)";
						gemName = "Ruby";
						spellName = "3 Enchant";
						itemName = "Ruby necklace";
					} else if (uo.equals("Bracelet")) {// Ruby Bracelet
						ifParent = 446;
						ifChild = 39;
						itemID = 11085;
						enchantedID = 11088;
						gem = true;
						gemID = ruby;
						mouldID = 11065;
						component = 53;
						exp = 80;
						enchantedName = "Inoculation brace";
						gemName = "Ruby";
						spellName = "3 Enchant";
						itemName = "Ruby bracelet";
					} else if (uo.equals("Amulet")) {// Ruby Amulet
						ifParent = 446;
						ifChild = 59;
						itemID = 1679;
						gem = true;
						gemID = ruby;
						mouldID = 1595;
						exp = 85;
						enchantedName = "Amulet of strength";
						// spellName = "0"; Comming Soon
						gemName = "Ruby";
						itemName = "Ruby amulet";
					}
				} else if (go.equals("Diamond")) {
					if (uo.equals("Ring")) {// Diamond Ring
						ifParent = 446;
						ifChild = 90;
						itemID = 1643; // MUST DO
						enchantedID = 2570;
						gem = true;
						gemID = diamond;
						mouldID = 1592;
						component = 61;
						exp = 85;
						enchantedName = "Ring of life";
						gemName = "Diamond";
						spellName = "4 Enchant";
						itemName = "Diamond Ring";
					} else if (uo.equals("Necklace")) {// Diamond Necklace
						ifParent = 446;
						ifChild = 76;
						itemID = 1662;
						enchantedID = 11090;
						gem = true;
						gemID = diamond;
						mouldID = 1597;
						component = 61;
						exp = 90;
						enchantedName = "Phoenix necklace";
						gemName = "Diamond";
						spellName = "4 Enchant";
						itemName = "Diamond necklace";
					} else if (uo.equals("Bracelet")) {// Diamond Bracelet
						ifParent = 446;
						ifChild = 41;
						itemID = 11092;
						enchantedID = 11095;
						mouldID = 11065;
						component = 61;
						exp = 95;
						enchantedName = "Forinthry brace (5)";
						gemName = "Diamond";
						spellName = "4 Enchant";
						itemName = "Diamond bracelet";
					} else if (uo.equals("Amulet")) {// Diamond Amulet
						ifParent = 446;
						ifChild = 61;
						itemID = 1681;
						gem = true;
						gemID = diamond;
						mouldID = 1595;
						exp = 100;
						// spellName = "0"; Coming Soon.
						enchantedName = "Amulet of power";
						gemName = "Diamond";
						itemName = "Diamond amulet";
					}
				}
			} else if (co.equals("Silver Jewellery")) {
				action = "crafted";
				if (go.equals("Silver")) {
					barName = "Silver bar";
					if (uo.equals("Holy Symbol")) {
						ifParent = 438;
						ifChild = 16;
						itemID = 1714;
						barID = silverBarID;
						mouldID = 1599;
						exp = 50;
						itemName = "Unstrung symbol";
					} else if (uo.equals("Tiara")) {
						ifParent = 438;
						ifChild = 44;
						itemID = 5525;
						barID = silverBarID;
						mouldID = 5523;
						exp = 52;
						itemName = "Tiara";
					}
				}
			} else if (co.equals("Cut Gems")) {
				ifParent = 905;
				ifChild = 14;
				chiselID = 1755;
				action = "cut";
				if (go.equals("Sapphire")) {
					barName = "Uncut sapphire";
					gemName = itemName = "Sapphire";
					gemID = itemID = sapphire;
					uncutGemID = barID = uncutSapphire;
					exp = 50;
				} else if (go.equals("Emerald")) {
					barName = "Uncut emerald";
					gemName = itemName = "Emerald";
					gemID = itemID = emerald;
					uncutGemID = barID = uncutEmerald;
					exp = 67;
				} else if (go.equals("Ruby")) {
					barName = "Uncut ruby";
					gemName = itemName = "Ruby";
					gemID = itemID = ruby;
					uncutGemID = barID = uncutRuby;
					exp = 85;
				} else if (go.equals("Diamond")) {
					barName = "Uncut diamond";
					gemName = itemName = "Diamond";
					gemID = itemID = diamond;
					uncutGemID = barID = uncutDiamond;
					exp = 107;
				} else if (go.equals("Dragonstone")) {
					barName = "Uncut dragonstone";
					gemName = itemName = "Dragonstone";
					gemID = itemID = dragonstone;
					uncutGemID = barID = uncutDragonstone;
					exp = 137;
				}
			}
			log("[GUI] Making " + itemName + "s at: " + location + "!");
			this.setVisible(false);

		}

		private void CancelActionPerformed(java.awt.event.ActionEvent evt) {
			canceled = true;
			this.setVisible(false);
		}

		private JPanel dialogPane;
		private JPanel contentPanel;
		private JTabbedPane tabbedPane2;
		private JPanel panel1;
		private JComboBox loc;
		private JLabel label1;
		private JLabel label2;
		private JComboBox type;
		private JComboBox mouseSpeed;
		private JComboBox craftingType;
		private JComboBox craftObj;
		private JPanel panel2;
		private JLabel label3;
		private JLabel label4;
		private JLabel label5;
		private JLabel label6;
		private JLabel label7;
		private JPanel buttonBar;
		private JButton okButton;
		private JButton cancelButton;
	}

	public class CameraRotateThread extends Thread {
		@Override
		public void run() {
			char LR = KeyEvent.VK_RIGHT;
			if (random(0, 2) == 0) {
				LR = KeyEvent.VK_LEFT;
			}
			keyboard.pressKey(LR);
			try {
				Thread.sleep(random(450, 2600));
			} catch (final Exception ignored) {
			}
			keyboard.releaseKey(LR);
		}
	}

	public class CameraHeightThread extends Thread {

		@Override
		public void run() {
			char UD = KeyEvent.VK_UP;
			if (random(0, 2) == 0) {
				UD = KeyEvent.VK_DOWN;
			}
			keyboard.pressKey(UD);
			try {
				Thread.sleep(random(450, 1700));
			} catch (final Exception ignored) {
			}
			keyboard.releaseKey(UD);
		}
	}
}