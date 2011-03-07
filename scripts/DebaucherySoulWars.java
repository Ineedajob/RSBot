import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSGroundItem;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSModel;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSPath;
import org.rsbot.script.wrappers.RSPlayer;
import org.rsbot.script.wrappers.RSTile;

@SuppressWarnings({ "unused", "serial" })
@ScriptManifest(authors = "Debauchery", name = "DebaucherySoulWars", version = 0.33, description = "")
public class DebaucherySoulWars extends Script implements MouseListener,
		MessageListener, PaintListener {

	private final int randomTeamID = 42031, blueBarrierID = 42029,
			bandageID = 14640, pyresID = 8598, jelliesID = 8599,
			redBarrierID = 42030, bonesID = 14638;
	private int zeal, won, lost, drew, blueShouts, redShouts, kicked,
			maxiumTimeForBreak, maxiumTimeUntillBreak, minimiumTimeForBreak,
			minimiumTimeUntillBreak;
	private final int[] BarrierID = { 42013, 42014, 42015, 42016, 42017, 42018 },
			bandageTableID = { 42023, 42024 },
			barricadeID = { 8600 },
			blueAvatarID = { 8597 }, redAvatarID = { 8596 }, fragmentID = {
					14646, 15792 }, arrowsID = { 9242, 13280, 9142, 864, 863 };
	private String result, task, breakHandlerStatus;
	private boolean randomTeam, lastWonTeam, lastLostTeam, redTeam, blueTeam,
			clanChatTeam, takeBreak, attackAvatar, attackPyres, attackJellies,
			getSupplies, healOthers, pickUpBones, attackPlayers, randomStrat,
			pureMode, blueLast, redLast, inClan, buryAtGrave, pickUpArrows,
			weaponSpec, quickPrayer, takingBreak, startedBreak, stoppedBreak,
			startScript, hide;
	private SWGUI gui;
	private Pitch pitch;
	private Angle angle;
	private ExtraAntiban extraAntiban;
	private BreakHandler breakHandler;
	private Timer run;
	private RSTile tempTile;
	private Strategies current;
	RSPath path;
	public Rectangle hideRect = new Rectangle(504, 209, 14, 14);

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Start">
	@Override
	public boolean onStart() {
		gui = new SWGUI();
		gui.setVisible(true);
		while (!startScript) {
			sleep(10);
		}
		gui.setVisible(false);
		run = new Timer(0);
		mouse.setSpeed(random(8, 10));
		angle = new Angle();
		pitch = new Pitch();
		pitch.start();
		angle.start();
		if (takeBreak) {
			breakHandler = new BreakHandler();
			breakHandler.start();
		}
		if (clanChatTeam) {
			inClan = clanChat.isInChannel();
			if (!inClan) {
				clanChat.join(clanChat.getLastChannel());
				inClan = clanChat.isInChannel();
			}
		}
		combat.setAutoRetaliate((pureMode ? false : true));
		extraAntiban = new ExtraAntiban();
		extraAntiban.start();
		return true;
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Loop">
	@Override
	public int loop() {
		path = null;
		tempTile = null;
		if ((getLocation().equals(Location.OUTSIDE)
				|| game.getClientState() == Game.INDEX_LOBBY_SCREEN || !game
				.isLoggedIn()) && takingBreak) {
			task = "Waiting in lobby.";
			if (!interfaces.get(Game.INTERFACE_LOGOUT_LOBBY).isValid()) {
				sleep(1000, 12000);
				game.logout(true);
			}
			startedBreak = true;
			stoppedBreak = false;
			env.disbleRandoms();
			inClan = false;
		} else {
			env.enableRandoms();
			startedBreak = false;
			stoppedBreak = true;
			if (game.isLoggedIn()
					&& game.getClientState() != Game.INDEX_LOBBY_SCREEN) {
				if (clanChatTeam) {
					if (!inClan) {
						if (clanChat.isInChannel()) {
							clanChat.join(clanChat.getLastChannel());
						}
						inClan = clanChat.isInChannel();
					}
				}
				if (interfaces.get(Game.INTERFACE_LEVEL_UP).isValid()) {
					if (interfaces.canContinue()) {
						interfaces.clickContinue();
					}
					sleep(100, 600);
				}
				if (getMyPlayer().getTeam() == 1) {
					redLast = false;
					blueLast = true;
				} else if (getMyPlayer().getTeam() == 2) {
					redLast = true;
					blueLast = false;
				}
				if (getLocation().equals(Location.OUTSIDE)) {
					if (interfaces.getComponent(243, 4).isValid()
							&& interfaces.getComponent(243, 4).containsText(
									"You were removed from the game due")) {
						result = "drewLast";
						kicked += 1;
						interfaces.clickContinue();
					}
					current = null;
					task = "Joining Team.";
					joinTeam();
					if (waitTime() > 0) {
						sleep(waitTime() * 50000);
					}
				} else if (getLocation().equals(Location.RED_WAITING)
						|| getLocation().equals(Location.BLUE_WAITING)) {
					current = null;
					task = "Waiting for next game.";
					waitingAntiban();
				} else if (getLocation().equals(Location.RED_SPAWN)
						|| getLocation().equals(Location.BLUE_SPAWN)
						|| getLocation().equals(Location.EAST_GRAVE)
						|| getLocation().equals(Location.WEST_GRAVE)) {
					current = null;
					task = "Leaving grave or spawn.";
					leave();
					sleep(1000, 2000);
					if (!listenTime()) {
						blueShouts = 0;
						redShouts = 0;
					}
				} else {
					if (game.getClientState() == Game.INDEX_LOBBY_SCREEN
							|| !game.isLoggedIn()) {
						return 1;
					}
					if (barricade()) {
						return 1;
					}
					if (current == null) {
						current = getStrategies();
					} else {
						switch (current) {
						case PICKUP_BONES:
							task = "Picking up bones.";
							bonesStrat();
							break;
						case HEAL_PLAYERS:
							task = "Healing others.";
							healPlayersStrat();
							break;
						case ATTACK_AVATAR:
							if (getActivityBarPercent() < 25) {
								if (pickUpBones) {
									task = "Picking Bones(Low activity).";
									bonesStrat();
									break;
								}
							} else {
								task = "Attack avatar.";
								avatarStrat();
							}
							break;
						case ATTACK_PYRES:
							if (getActivityBarPercent() < 25) {
								if (pickUpBones) {
									task = "Picking Bones(Low activity).";
									bonesStrat();
									break;
								}
							} else {
								task = "Attacking Pyres.";
								pyresStrat();
							}
							break;
						case ATTACK_JELLIES:
							if (getActivityBarPercent() < 25) {
								if (pickUpBones) {
									task = "Picking Bones(Low activity).";
									bonesStrat();
									break;
								}
							} else {
								task = "Attacking Jellies.";
								jelliesStrat();
							}
							break;
						case ATTACK_PLAYERS:
							if (getActivityBarPercent() < 25) {
								if (pickUpBones) {
									task = "Picking Bones(Low activity).";
									bonesStrat();
									break;
								}
							} else {
								task = "Attacking Players.";
								playersStrat();
							}
							break;
						}
					}
				}
			}
		}
		return 1;
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Methods">
	public int findClan() {
		sleep(10000, 15000);
		int blueUsers = 0;
		int redUsers = 0;
		String[] clanUsers;
		try {
			clanUsers = clanChat.getChannelUsers();
			for (int x = 0; x < clanUsers.length; x++) {
				RSPlayer temp = players.getNearest(clanUsers[x]);
				if (temp != null) {
					RSTile tempTile = temp.getLocation();
					if (tempTile != null) {
						if (Location.BLUE_WAITING.containsTile(tempTile)) {
							blueUsers++;
						} else if (Location.RED_WAITING.containsTile(tempTile)) {
							redUsers++;
						}
					}
				}
			}
			if (blueUsers > redUsers) {
				return 1;
			} else if (redUsers > blueUsers) {
				return 2;
			}
		} catch (Exception e) {
		}
		return 0;
	}

	public int waitTime() {
		try {
			if (interfaces.getComponent(211, 1).isValid()
					&& interfaces.getComponent(211, 1).containsText(
							"You left a game of Soul Wars early")) {
				String getLine = interfaces.getComponent(211, 1).getText();
				String subLine = getLine.substring(
						getLine.indexOf("wait ") + 5,
						getLine.indexOf(" minutes"));
				return Integer.parseInt(subLine);
			}
		} catch (Exception e) {
		}
		return 0;
	}

	private RSTile divideTile(RSTile tile) {
		RSTile loc = getMyPlayer().getLocation();
		return new RSTile((loc.getX() + 4 * tile.getX()) / 5,
				(loc.getY() + 4 * tile.getY()) / 5);
	}

	private int getOwnAvatarHealth() {
		String s = null;
		try {
			if (getMyPlayer().getTeam() == 1) {
				RSComponent avatar = interfaces.getComponent(836, 11);
				if (avatar != null) {
					s = avatar.getText();
				}
			} else {
				RSComponent avatar = interfaces.getComponent(836, 15);
				if (avatar != null) {
					s = avatar.getText();
				}
			}
			if (s.equals("---")) {
				return 100;
			}
			s.replace("%", "");
			try {
				return Integer.parseInt(s.trim());
			} catch (Exception e) {
			}
		} catch (Exception e) {
			return 100;
		}
		return 100;
	}

	private int getOpponentAvatarLevel() {
		String s = null;
		try {
			if (getMyPlayer().getTeam() == 1) {
				s = interfaces.get(836).getComponent(14).getText();
				if (!s.equals("---")) {
					return Integer.parseInt(s);
				}
			} else {
				s = interfaces.get(836).getComponent(10).getText();
				if (!s.equals("---")) {
					return Integer.parseInt(s);
				}
			}
		} catch (Exception e) {
			return 100;
		}
		return 100;
	}

	private int getActivityBarPercent() {
		RSComponent c = interfaces.get(836).getComponent(56);
		if (c != null && c.isValid() && c.getRelativeY() > -1) {
			return ((c.getHeight() * 100) / 140);
		}
		return 0;
	}

	private boolean controlsWesternGraveyard() {
		Color color = getColor(272, 25);
		if (color != null) {
			if (getMyPlayer().getTeam() == 1) {
				return (color.getBlue() > color.getRed() ? true : false);
			} else {
				return (color.getRed() > color.getBlue() ? true : false);
			}
		}
		return false;
	}

	private boolean controlsEasternGraveyard() {
		Color color = getColor(352, 25);
		if (color != null) {
			if (getMyPlayer().getTeam() == 1) {
				return (color.getBlue() > color.getRed() ? true : false);
			} else {
				return (color.getRed() > color.getBlue() ? true : false);
			}
		}
		return false;
	}

	private int chooseTeam() {
		if (clanChatTeam) {
			if (blueShouts != redShouts) {
				if (blueShouts > redShouts) {
					return 1;
				} else {
					return 2;
				}
			} else {
				return findClan();
			}
		} else if (lastWonTeam || lastLostTeam) {
			if (result != null) {
				if (result.equals("drawLast")) {
					return 0;
				} else if (result.equals("wonLast")) {
					if (blueLast) {
						if (lastWonTeam) {
							return 1;
						} else if (lastLostTeam) {
							return 2;
						}
					} else {
						if (lastWonTeam) {
							return 2;
						} else if (lastLostTeam) {
							return 1;
						}
					}
				} else if (result.equals("lostLast")) {
					if (redLast) {
						if (lastWonTeam) {
							return 1;
						} else if (lastLostTeam) {
							return 2;
						}
					} else {
						if (lastWonTeam) {
							return 2;
						} else if (lastLostTeam) {
							return 1;
						}
					}
				}
			}
		} else if (redTeam) {
			return 2;
		} else if (blueTeam) {
			return 1;
		} else if (randomTeam) {
			return 0;
		}
		return 0;
	}

	private void joinTeam() {
		String s = interfaces.get(199).getComponent(0).getText();
		if (interfaces.get(211).getComponent(3).isValid()
				&& interfaces.get(211).getComponent(3) != null) {
			interfaces.get(211).getComponent(3).doClick();
			sleep(1200, 2000);
		} else if (interfaces.get(228).getComponent(2).isValid()
				&& interfaces.get(228).getComponent(2) != null) {
			interfaces.get(228).getComponent(2).doClick();
			sleep(1200, 2000);
		} else {
			switch (chooseTeam()) {
			case 0:
				// random
				RSObject randomTeam = objects.getNearest(randomTeamID);
				if (randomTeam != null) {
					RSTile randomTeamTile = randomTeam.getLocation();
					if (randomTeamTile != null) {
						if (randomTeam.isOnScreen()) {
							switch (random(0, 2)) {
							case 0:
								if (getLocation().equals(Location.OUTSIDE)) {
									if (randomTeam.doAction("Join-team")) {
										sleep(1000, 1300);
									}
								}
								break;
							// Join-team
							case 1:
								RSModel mod = randomTeam.getModel();
								if (mod != null) {
									Point p = mod.getPoint();
									if (p != null) {
										mouse.hop(p);
										if (getLocation().equals(
												Location.OUTSIDE)) {
											mouse.click(true);
											sleep(1000, 1300);
										}
									}
								}
								break;
							}
						} else if (!randomTeam.isOnScreen()
								&& calc.distanceTo(randomTeamTile) <= 4) {
							camera.getObjectAngle(randomTeam);
						} else {
							RSTile closestTileToRandomTeam = walking
									.getClosestTileOnMap(randomTeamTile);
							if (closestTileToRandomTeam != null) {
								walking.walkTileMM(closestTileToRandomTeam);
								sleep(400, 800);
							}
						}
					}
				}
				break;
			case 1:
				// blue
				RSObject blueBarrier = objects.getNearest(blueBarrierID);
				if (blueBarrier != null) {
					RSTile blueBarrierTile = blueBarrier.getLocation();
					if (blueBarrierTile != null) {
						if (blueBarrier.isOnScreen()) {
							switch (random(0, 2)) {
							case 0:
								RSModel mod = blueBarrier.getModel();
								if (mod != null) {
									Point p = mod.getPoint();
									if (p != null) {
										mouse.hop(p);
										if (getLocation().equals(
												Location.OUTSIDE)) {
											mouse.click(true);
											sleep(800, 1000);
										}
									}
								}
								break;
							case 1:
								if (getLocation().equals(Location.OUTSIDE)) {
									blueBarrier.doAction("Pass");
								}
								sleep(800, 1000);
								break;
							}
						} else if (!blueBarrier.isOnScreen()
								&& calc.distanceTo(blueBarrierTile) <= 4) {
							camera.getObjectAngle(blueBarrier);
							camera.setPitch(true);
						} else {
							RSTile closestTileToBlueBarrier = walking
									.getClosestTileOnMap(blueBarrierTile);
							if (closestTileToBlueBarrier != null) {
								walking.walkTileMM(closestTileToBlueBarrier);
								sleep(400, 800);
							}
						}
					}
				}
				break;
			case 2:
				// red
				RSObject redBarrier = objects.getNearest(redBarrierID);
				if (redBarrier != null) {
					RSTile redBarrierTile = redBarrier.getLocation();
					if (redBarrierTile != null) {
						if (redBarrier.isOnScreen()) {
							switch (random(0, 2)) {
							case 0:
								RSModel mod = redBarrier.getModel();
								if (mod != null) {
									Point p = mod.getPoint();
									if (p != null) {
										mouse.hop(p);
										if (getLocation().equals(
												Location.OUTSIDE)) {
											mouse.click(true);
										}
										sleep(800, 1000);
									}
								}
								break;
							case 1:
								if (getLocation().equals(Location.OUTSIDE)) {
									if (redBarrier.doAction("Pass")) {
										sleep(800, 1000);
									}
								}
								break;
							}
						} else if (!redBarrier.isOnScreen()
								&& calc.distanceTo(redBarrierTile) <= 4) {
							camera.getObjectAngle(redBarrier);
							camera.setPitch(true);
						} else {
							RSTile closestTileToRedBarrier = walking
									.getClosestTileOnMap(redBarrierTile);
							if (closestTileToRedBarrier != null) {
								walking.walkTileMM(closestTileToRedBarrier);
								sleep(400, 800);
							}
						}
					}
				}
				break;
			}
		}

	}

	private void waitingAntiban() {
		switch (random(0, 7)) {
		case 0:
			sleep(600, 700);
			break;
		case 1:
			mouse.moveRandomly(200);
			break;
		case 2:
			camera.moveRandomly(random(1000, 1200));
			break;
		case 3:
			mouse.moveOffScreen();
			sleep(1200, 2000);
			break;
		case 4:
			sleep(800, 1000);
			break;
		case 5:
			if (getLocation().equals(Location.RED_WAITING)) {
				RSTile center = Location.RED_WAITING.getRSArea()
						.getCentralTile();
				if (center != null && calc.distanceTo(center) > 2) {
					walking.walkTileMM(center, 2, 2);
				}
			} else if (getLocation().equals(Location.BLUE_WAITING)) {
				RSTile center = Location.BLUE_WAITING.getRSArea()
						.getCentralTile();
				if (center != null && calc.distanceTo(center) > 2) {
					walking.walkTileMM(center, 2, 2);
				}
			}
			break;
		case 6:
			sleep(200);
			break;
		}
	}

	private void leave() {
		try {
			RSObject Barrier = objects.getNearest(BarrierID);
			if (getLocation().equals(Location.RED_SPAWN)
					|| getLocation().equals(Location.BLUE_SPAWN)
					|| getLocation().equals(Location.EAST_GRAVE)
					|| getLocation().equals(Location.WEST_GRAVE)) {
				if (getMyPlayer().getNPCID() == 8623) {
					if (random(1, 100) < 25) {
						if (Barrier != null) {
							walking.walkTileMM(Barrier.getLocation());
							sleep(600, 1000);
						}
					}
					if (random(1, 100) < 10) {
						if (getLocation().equals(Location.RED_SPAWN)
								|| getLocation().equals(Location.BLUE_SPAWN)
								|| getLocation().equals(Location.EAST_GRAVE)
								|| getLocation().equals(Location.WEST_GRAVE)) {
							Barrier.doAction("Pass");
						}
					}
					sleep(100, 600);
				} else if (Barrier != null) {
					if (!Barrier.isOnScreen()
							&& calc.distanceTo(Barrier.getLocation()) < 2) {
						camera.turnToObject(Barrier);
					}
					if (!Barrier.isOnScreen()) {
						walking.walkTileMM(Barrier.getLocation());
					}
					if (Barrier.isOnScreen()
							&& getMyPlayer().getAnimation() != 1) {
						RSModel mod = Barrier.getModel();
						camera.turnToObject(Barrier);
						sleep(2000, 4000);
						switch (random(0, 5)) {
						case 2:
							if (mod != null) {
								Point p = mod.getPoint();
								if (p != null) {
									if (getLocation()
											.equals(Location.RED_SPAWN)
											|| getLocation().equals(
													Location.BLUE_SPAWN)
											|| getLocation().equals(
													Location.EAST_GRAVE)
											|| getLocation().equals(
													Location.WEST_GRAVE)) {
										mouse.hop(p);
										mouse.click(true);
									}
								}
							}
							break;
						default:
							if (getLocation().equals(Location.RED_SPAWN)
									|| getLocation()
											.equals(Location.BLUE_SPAWN)
									|| getLocation()
											.equals(Location.EAST_GRAVE)
									|| getLocation()
											.equals(Location.WEST_GRAVE)) {
								Barrier.doAction("Pass");
							}
							break;
						}
					}
				}
			}
			sleep(1000, 2000);
		} catch (Exception e) {
		}
	}

	private boolean getSupplies() {
		if (inventory.containsOneOf(bandageID)) {
			return true;
		}
		RSObject bandages = objects.getNearest(bandageTableID);
		int supplyNum = random(28, 100);
		if (bandages != null && getMyPlayer().getInteracting() == null) {
			if (!bandages.isOnScreen()) {
				camera.turnToObject(bandages);
			}
			if (bandages.isOnScreen()) {
				if (bandages.doAction("Take-x")) {
					sleep(2500, 3000);
					if (calc.distanceTo(bandages) <= 1
							&& getMyPlayer().getInteracting() == null) {
						if (interfaces.getComponent(752, 5) != null
								&& !interfaces.getComponent(752, 5).getText()
										.contains(String.valueOf(supplyNum))) {
							keyboard.sendText(String.valueOf(supplyNum), false);
							sleep(1000, 1400);
						}
						if (interfaces.getComponent(752, 5) != null
								&& interfaces.getComponent(752, 5).getText()
										.contains(String.valueOf(supplyNum))) {
							keyboard.sendText("", true);
							sleep(1800, 2000);
						}
					}
				}
			} else {
				if (!getMyPlayer().isMoving()) {
					walking.walkTileMM(walking.getClosestTileOnMap(bandages
							.getLocation()));
					sleep(1000, 2000);
				}
			}
		}
		return false;
	}

	private RSPlayer teamMate() {
		try {
			RSPlayer[] validPlayers = players.getAll();
			for (RSPlayer player : validPlayers) {
				if (player != null && player.isOnScreen()) {
					if (getMyPlayer().getTeam() == player.getTeam()) {
						if (player.getHPPercent() < 90) {
							return player;
						}
					}
				}
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	private void healOthers() {
		camera.setPitch(true);
		try {
			if (inventory.getSelectedItem() != null
					&& inventory.getSelectedItem().getID() == bandageID) {
				RSItem bandage = inventory.getItem(bandageID);
				if (bandage != null) {
					bandage.doClick(true);
				}
			} else {
				switch (random(1, 3)) {
				case 1:
					if (teamMate() != null) {
						RSModel model = teamMate().getModel();
						if (model != null) {
							Point p = model.getPoint();
							if (p != null) {
								mouse.hop(p);
								mouse.click(true);
								sleep(3000, 5000);
							}
						}
					}
					break;
				case 2:
					opponent().doAction("Heal " + teamMate().getName());
					sleep(3000, 5000);
					break;
				}
			}
		} catch (Exception e) {
		}
	}

	private RSPlayer opponent() {
		try {
			RSPlayer[] validPlayers = players.getAll();
			for (RSPlayer player : validPlayers) {
				if (player != null && player.isOnScreen()) {
					if (getMyPlayer().getTeam() != player.getTeam()) {
						if (player.getHPPercent() > 30) {
							return player;
						}
					}
				}
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	private void attack() {
		camera.setPitch(true);
		try {
			RSPlayer opp = opponent();
			if (opp != null && Location.OBELISK.containsTile(opp.getLocation())) {
				if (getMyPlayer().getInteracting() == null) {
					switch (random(1, 3)) {
					case 1:
						if (opp != null) {
							RSModel model = opp.getModel();
							if (model != null) {
								Point p = model.getPoint();
								if (p != null) {
									mouse.hop(p);
									mouse.click(true);
									sleep(3000, 5000);
								}
							}
						}
						break;
					case 2:
						opp.doAction("Attack");
						sleep(3000, 5000);
						break;
					}
				}
			} else {
				if (pickUpBones) {
					if (getActivityBarPercent() < 100 || buryAtGrave) {
						RSGroundItem bones = groundItems.getNearest(bonesID);
						bones.doAction("Take " + bones.getItem().getName());
						return;
					}
				}
				pickUpArrows();
				if (!getMyPlayer().isMoving()
						|| calc.distanceTo(walking.getDestination()) < 5) {
					walking.walkTileMM(walking
							.getClosestTileOnMap((getMyPlayer().getTeam() == 1 ? new RSTile(
									1901, 3231) : new RSTile(1871, 3233))));
					sleep(1000, 3000);
				}
			}
		} catch (Exception e) {
		}
	}

	private void attackAvatar() {
		if (getMyPlayer().getTeam() == 1) {
			RSNPC avatar = npcs.getNearest(redAvatarID);
			if (avatar != null) {
				if (avatar.isOnScreen()) {
					if (getMyPlayer().getInteracting() == null
							|| !getMyPlayer().getInteracting().getName()
									.equals(avatar.getName())) {
						avatar.doAction("Attack " + avatar.getName());
						sleep(3000, 5000);
					}
				} else if (avatar.isOnScreen() && calc.distanceTo(avatar) < 4) {
					camera.getCharacterAngle(avatar);
				} else {
					RSTile avatarLoc = avatar.getLocation();
					if (avatarLoc != null) {
						if (calc.distanceTo(walking.getDestination()) < 4
								|| !getMyPlayer().isMoving()) {
							walking.walkTileMM(walking
									.getClosestTileOnMap(avatarLoc));
						}
					}
				}
			}
		} else {
			RSNPC avatar = npcs.getNearest(blueAvatarID);
			if (avatar != null) {
				if (avatar.isOnScreen()) {
					if (getMyPlayer().getInteracting() == null
							|| !getMyPlayer().getInteracting().getName()
									.equals(avatar.getName())) {
						avatar.doAction("Attack");
						sleep(3000, 5000);
					}
				} else if (avatar.isOnScreen() && calc.distanceTo(avatar) < 4) {
					camera.getCharacterAngle(avatar);
				} else {
					RSTile avatarLoc = avatar.getLocation();
					if (avatarLoc != null) {
						if (calc.distanceTo(walking.getDestination()) < 4
								|| !getMyPlayer().isMoving()) {
							walking.walkTileMM(walking
									.getClosestTileOnMap(avatarLoc));
						}
					}
				}
			}
		}
	}

	private void buryBones() {
		if (getActivityBarPercent() < random(60, 80)) {
			if (inventory.containsOneOf(bonesID)) {
				RSItem inventBones = inventory.getItem(bonesID);
				if (inventBones != null) {
					inventBones.doAction("Bury");
					sleep(700, 1200);
				}
			}
		}
	}

	public void destroyBones() {
		if (inventory.containsOneOf(bonesID)) {
			RSItem inventBones = inventory.getItem(bonesID);
			if (inventBones != null) {
				inventBones.doAction("Destroy");
				sleep(700, 1200);
				if (interfaces.get(94).isValid()) {
					RSComponent tick = interfaces.get(94).getComponent(3);
					tick.doAction("Continue");
				}
				sleep(700, 1200);
			}
		}
	}

	private boolean barricade() {
		try {
			RSNPC barricade = npcs.getNearest(barricadeID);
			if (barricade != null) {
				RSTile barricadeLoc = barricade.getLocation();
				if (barricadeLoc != null) {
					if (calc.distanceTo(barricadeLoc) <= 2) {
						if (getMyPlayer().getInteracting().equals(barricade)) {
							return true;
						} else {
							barricade.doAction("Attack " + barricade.getName());
							sleep(3000, 5000);
							return true;
						}
					}
				}
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	private void attackJellies() {
		RSNPC jellies = npcs.getNearest(jelliesID);
		if (jellies != null) {
			if (jellies != null) {
				camera.setPitch(true);
				if (calc.distanceTo(jellies) > 4 && !jellies.isOnScreen()) {
					walking.walkTileMM(walking.getClosestTileOnMap(jellies
							.getLocation()));
				}
				if (!jellies.isOnScreen()) {
					camera.turnToCharacter(jellies);
				}
				if (jellies.isOnScreen()
						&& (getMyPlayer().getInteracting() == null || !getMyPlayer()
								.getInteracting().getName()
								.equals(jellies.getName()))) {
					jellies.doAction("Attack");
					sleep(3000, 5000);
				}
			}
		}
	}

	private void attackPyres() {
		RSNPC pyres = npcs.getNearest(pyresID);
		if (pyres != null) {
			if (pyres != null) {
				camera.setPitch(true);
				if (calc.distanceTo(pyres) > 4 && !pyres.isOnScreen()) {
					walking.walkTileMM(walking.getClosestTileOnMap(pyres
							.getLocation()));
				}
				if (!pyres.isOnScreen()) {
					camera.turnToCharacter(pyres);
				}
				if (pyres.isOnScreen()
						&& (getMyPlayer().getInteracting() == null || !getMyPlayer()
								.getInteracting().getName()
								.equals(pyres.getName()))) {
					pyres.doAction("Attack");
					sleep(3000, 5000);
				}
			}
		}
	}

	private void heal() {
		if (getMyPlayer().getHPPercent() < 60) {
			RSItem bandage = inventory.getItem(bandageID);
			if (bandage != null) {
				bandage.doAction("Heal");
				sleep(200, 400);
			}
		}
	}

	private void qPrayer() {
		if (quickPrayer) {
			if (getMyPlayer().getHPPercent() < random(65, 90)) {
				if (!combat.isQuickPrayerOn() && combat.getPrayerPoints() > 1) {
					interfaces.getComponent(749, 2).doClick();
				}
			}
		}
	}

	private void weaponSpec() {
		if (weaponSpec) {
			if (getMyPlayer().getInteracting() != null) {
				if (settings.getSetting(301) != 1
						&& settings.getSetting(300) == 1000) {
					if (game.getCurrentTab() != Game.TAB_ATTACK) {
						game.openTab(Game.TAB_ATTACK);
						sleep(random(300, 900));
					}
					mouse.click(645 + random(0, 4), 425 + random(0, 4), true);
				}
			}
		}
	}

	public void pickUpArrows() {
		if (pickUpArrows) {
			RSGroundItem arrow = null;
			for (int x = 0; arrow == null || x < arrowsID.length; x++) {
				arrow = groundItems.getNearest(arrowsID[x]);
				if (arrow != null) {
					arrow.doAction("Take " + arrow.getItem().getName());
					return;
				}
			}
		}
	}

	private Location nearestPyres() {
		int distanceToNorth = (calc.distanceTo(Location.NORTHWEST_PRYES
				.getRSArea().getNearestTile(getMyPlayer().getLocation())));
		int distanceToSouth = (calc.distanceTo(Location.SOUTHEAST_PYRES
				.getRSArea().getNearestTile(getMyPlayer().getLocation())));
		return (distanceToSouth > distanceToNorth ? Location.NORTHWEST_PRYES
				: Location.SOUTHEAST_PYRES);
	}

	private Location nearestJellies() {
		int distanceToNorth = (calc.distanceTo(Location.NORTH_JELLIES
				.getRSArea().getNearestTile(getMyPlayer().getLocation())));
		int distanceToSouth = (calc.distanceTo(Location.SOUTH_JELLIES
				.getRSArea().getNearestTile(getMyPlayer().getLocation())));
		return (distanceToSouth > distanceToNorth ? Location.NORTH_JELLIES
				: Location.SOUTH_JELLIES);
	}

	private Location nearestSupplies() {
		int distanceToBlue = (calc.distanceTo(Location.BLUE_SUPPLIES
				.getRSArea().getNearestTile(getMyPlayer().getLocation())));
		int distanceToRed = (calc.distanceTo(Location.RED_SUPPLIES.getRSArea()
				.getNearestTile(getMyPlayer().getLocation())));
		return (distanceToRed > distanceToBlue ? Location.BLUE_SUPPLIES
				: Location.RED_SUPPLIES);
	}

	private Location nearestOwnedGrave() {
		if (controlsWesternGraveyard() && controlsEasternGraveyard()) {
			int distanceToEast = (calc.distanceTo(Location.EAST_GRAVE_OUT
					.getRSArea().getNearestTile(getMyPlayer().getLocation())));
			int distanceToWest = (calc.distanceTo(Location.WEST_GRAVE_OUT
					.getRSArea().getNearestTile(getMyPlayer().getLocation())));
			return (distanceToEast < distanceToWest ? Location.EAST_GRAVE_OUT
					: Location.WEST_GRAVE_OUT);
		} else if (controlsWesternGraveyard()) {
			return Location.WEST_GRAVE_OUT;
		} else if (controlsEasternGraveyard()) {
			return Location.EAST_GRAVE_OUT;
		}
		return null;
	}

	private boolean listenTime() {
		if (getLocation().equals(Location.OUTSIDE)) {
			return true;
		} else {
			String time = interfaces.get(836).getComponent(27).getText();
			if (time == null
					|| (time != null && (time.equals("7 mins")
							|| time.equals("6 mins") || time.equals("5 mins")
							|| time.equals("4 mins") || time.equals("3 mins")
							|| time.equals("2 mins") || time.equals("1 min")))) {
				return true;
			} else {
				return false;
			}
		}
	}

	public Color getColor(int x, int y) {
		try {
			BufferedImage image = env.takeScreenshot(false);
			int c = image.getRGB(x, y);
			return new Color(c);
		} catch (Exception e) {
			return null;
		}
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Strategies">
	private void playersStrat() {
		if (getLocation().equals(Location.OBELISK)) {
			heal();
			qPrayer();
			attack();
			weaponSpec();
		} else {
			if (!inventory.containsOneOf(bandageID)
					&& (getSupplies || healOthers)) {
				if (getLocation().equals(nearestSupplies())) {
					getSupplies();
				} else {
					walkTo(nearestSupplies());
				}
			} else {
				walkTo(Location.OBELISK);
			}
		}
	}

	private void pyresStrat() {
		if (getLocation().equals(nearestPyres())) {
			heal();
			qPrayer();
			attackPyres();
			weaponSpec();
		} else {
			if (!inventory.containsOneOf(bandageID)
					&& (getSupplies || healOthers)) {
				if (getLocation().equals(nearestSupplies())) {
					getSupplies();
				} else {
					walkTo(nearestSupplies());
				}
			} else {
				walkTo(nearestPyres());
			}
		}
	}

	private void jelliesStrat() {
		if (getLocation().equals(nearestJellies())) {
			heal();
			qPrayer();
			attackJellies();
			weaponSpec();

		} else {
			if (!inventory.containsOneOf(bandageID)
					&& (getSupplies || healOthers)) {
				if (getLocation().equals(nearestSupplies())) {
					getSupplies();

				} else {
					walkTo(nearestSupplies());
				}
			} else {
				walkTo(nearestJellies());
			}
		}
	}

	private void healPlayersStrat() {
		try {
			if (!inventory.containsOneOf(bandageID)) {
				if (getLocation().equals(nearestSupplies())) {
					getSupplies();
				} else {
					walkTo(nearestSupplies());
				}
			} else {
				if (getLocation().equals(Location.OBELISK)) {
					if (getActivityBarPercent() < random(70, 80)) {
						healOthers();
					} else {
						pickUpArrows();
					}
				} else {
					walkTo(Location.OBELISK);
				}
			}
		} catch (Exception e) {
		}
	}

	private void bonesStrat() {
		try {
			if (buryAtGrave
					&& (getLocation().equals(Location.EAST_GRAVE_OUT) || getLocation()
							.equals(Location.WEST_GRAVE_OUT))
					&& inventory.containsOneOf(bonesID)) {
				if (getActivityBarPercent() < 70) {
					buryBones();
				} else {
					RSGroundItem bones = groundItems.getNearest(bonesID);
					if (bones != null) {
						if (bones.isOnScreen() && !inventory.isFull()) {
							bones.doAction("Take " + bones.getItem().getName());
							return;
						}
					}
				}
			} else {
				if (buryAtGrave && inventory.isFull()) {
					if (nearestOwnedGrave() != null) {
						walkTo(nearestOwnedGrave());
						return;
					}
				}
				RSGroundItem bones = groundItems.getNearest(bonesID);
				if (bones != null) {
					if (calc.distanceTo(bones.getLocation()) < 5
							&& !inventory.isFull()) {
						if (getActivityBarPercent() < random(50, 80)
								|| buryAtGrave) {
							bones.doAction("Take " + bones.getItem().getName());
							return;
						}
					}
				}
				if (getLocation().equals(Location.OBELISK)) {
					bones = groundItems.getNearest(bonesID);
					if (bones != null) {
						if (!bones.isOnScreen()) {
							camera.turnToTile(bones.getLocation());
							if (!bones.isOnScreen()) {
								walking.walkTileMM(bones.getLocation());
								sleep(1000, 1200);
							}
						} else {
							if (!inventory.isFull()) {
								if (getActivityBarPercent() < 70) {
									bones.doAction("Take "
											+ bones.getItem().getName());
								} else {
									pickUpArrows();
								}
							} else {
								if (!buryAtGrave && nearestOwnedGrave() != null) {
									destroyBones();
								}
							}
						}
					}
				} else {
					walkTo(Location.OBELISK);
				}
			}
		} catch (Exception e) {
		}
	}

	private void avatarStrat() {
		if (getLocation().equals(Location.OBELISK)) {
			heal();
			qPrayer();
			attackAvatar();
			weaponSpec();
		} else {
			if (!inventory.containsOneOf(bandageID)
					&& (getSupplies || healOthers)) {
				if (getLocation().equals(nearestSupplies())) {
					getSupplies();
				} else {
					walkTo(nearestSupplies());
				}
			} else {
				walkTo((getMyPlayer().getTeam() == 1 ? Location.RED_AVATAR
						: Location.BLUE_AVATAR));
			}
		}

	}

	private enum Strategies {

		ATTACK_PLAYERS, ATTACK_PYRES, ATTACK_JELLIES, HEAL_PLAYERS, PICKUP_BONES, ATTACK_AVATAR;
	}

	private Strategies getStrategies() {
		if (randomStrat) {
			switch (random(0, 5)) {
			case 0:
				if (pickUpBones) {
					return Strategies.PICKUP_BONES;
				}
			case 1:
				if (healOthers) {
					return Strategies.HEAL_PLAYERS;
				}
			case 2:
				if (skills.getCurrentLevel(Skills.SLAYER) >= 30) {
					return Strategies.ATTACK_PYRES;
				}
			case 3:
				if (skills.getCurrentLevel(Skills.SLAYER) >= 52) {
					return Strategies.ATTACK_JELLIES;
				}
			case 4:
				return Strategies.ATTACK_PLAYERS;
			default:
				return Strategies.ATTACK_PLAYERS;
			}
		}
		if (pureMode) {
			if (pickUpBones && healOthers) {
				switch (random(0, 2)) {
				case 0:
					return Strategies.PICKUP_BONES;
				case 1:
					return Strategies.HEAL_PLAYERS;
				}
			}
			if (pickUpBones) {
				return Strategies.PICKUP_BONES;
			}
			if (healOthers) {
				return Strategies.HEAL_PLAYERS;
			}
		}
		if (getActivityBarPercent() < 70) {
			if (pickUpBones && healOthers) {
				switch (random(0, 2)) {
				case 0:
					return Strategies.PICKUP_BONES;
				case 1:
					return Strategies.HEAL_PLAYERS;
				}
			}
			if (pickUpBones) {
				return Strategies.PICKUP_BONES;

			}
			if (healOthers) {
				return Strategies.HEAL_PLAYERS;

			}
		} else {
			if (getOpponentAvatarLevel() < skills.getRealLevel(Skills.SLAYER)
					&& attackAvatar) {
				return Strategies.ATTACK_AVATAR;

			}
			if (attackPyres) {
				return Strategies.ATTACK_PYRES;

			} else if (attackJellies) {
				return Strategies.ATTACK_JELLIES;

			} else if (attackPlayers) {
				return Strategies.ATTACK_PLAYERS;

			}
		}
		return null;

	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Location">
	private enum Location {

		RED_WAITING(new RSArea(new RSTile(1909, 3167), new RSTile(1899, 3156))), BLUE_WAITING(
				new RSArea(new RSTile(1880, 3167), new RSTile(1869, 3157))), OUTSIDE(
				new RSArea(new RSTile(1918, 3187), new RSTile(1862, 3149))), RED_SPAWN(
				new RSArea(new RSTile(1951, 3234), new RSTile(1958, 3244))), BLUE_SPAWN(
				new RSArea(new RSTile(1816, 3220), new RSTile(1823, 3230))), EAST_GRAVE(
				new RSArea(new RSTile(1932, 3244), new RSTile(1934, 3246))), WEST_GRAVE(
				new RSArea(new RSTile(1841, 3217), new RSTile(1843, 3219))), EAST_GRAVE_OUT(
				new RSArea(new RSTile(1939, 3252), new RSTile(1926, 3239))), WEST_GRAVE_OUT(
				new RSArea(new RSTile(1848, 3225), new RSTile(1836, 3212))), RED_SUPPLIES(
				new RSArea(new RSTile(1977, 3213), new RSTile(1961, 3203))), BLUE_SUPPLIES(
				new RSArea(new RSTile(1812, 3261), new RSTile(1795, 3250))), RED_AVATAR(
				new RSArea(new RSTile(1976, 3261), new RSTile(1959, 3244))), BLUE_AVATAR(
				new RSArea(new RSTile(1816, 3220), new RSTile(1798, 3202))), SOUTHEAST_PYRES(
				new RSArea(new RSTile(1938, 3222), new RSTile(1917, 3204))), NORTHWEST_PRYES(
				new RSArea(new RSTile(1856, 3258), new RSTile(1834, 3238))), NORTH_JELLIES(
				new RSArea(new RSTile(1906, 3263), new RSTile(1869, 3248))), SOUTH_JELLIES(
				new RSArea(new RSTile(1899, 3214), new RSTile(1873, 3199))), OBELISK(
				new RSArea(new RSTile(1901, 3241), new RSTile(1872, 3221))), OTHER(
				new RSArea(new RSTile(0, 0), new RSTile(0, 0)));
		private final RSArea area;

		Location(RSArea area) {
			this.area = area;
		}

		RSArea getRSArea() {
			return area;
		}

		boolean containsTile(RSTile tile) {
			return this.area.contains(tile);
		}
	}

	private Location getLocation() {
		RSTile player = getMyPlayer().getLocation();
		for (Location loc : Location.values()) {
			if (loc.containsTile(player)) {
				return loc;
			}
		}
		return Location.OTHER;
	}

	private void walkTo(Location loc) {
		long startTime = System.currentTimeMillis();
		try {
			path = walking.getPath(loc.getRSArea().getCentralTile());
			tempTile = loc.getRSArea().getCentralTile();
			while (path.getNext() == null) {
				if (game.getClientState() == Game.INDEX_LOBBY_SCREEN
						|| !game.isLoggedIn()
						|| getLocation().equals(Location.OUTSIDE)
						|| getLocation().equals(Location.RED_SPAWN)
						|| getLocation().equals(Location.BLUE_SPAWN)
						|| getLocation().equals(Location.EAST_GRAVE)
						|| getLocation().equals(Location.WEST_GRAVE)) {
					return;
				}
				tempTile = divideTile(tempTile);
				path = walking.getPath(tempTile);
				if (path.getNext() == null) {
					if (startTime + 2000 < System.currentTimeMillis()
							&& getMyPlayer().isMoving()) {
						walking.walkTileMM(walking.getClosestTileOnMap(loc
								.getRSArea().getCentralTile()));
					}
				}
			}
			if (path.getNext() != null) {
				path.traverse();
			}
		} catch (Exception e) {
		}
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="GUI">
	public class SWGUI extends javax.swing.JFrame {

		public SWGUI() {
			initComponents();
		}

		private void initComponents() {

			jLabel1 = new javax.swing.JLabel();
			jLabel2 = new javax.swing.JLabel();
			jTabbedPane1 = new javax.swing.JTabbedPane();
			jPanel1 = new javax.swing.JPanel();
			jLabel3 = new javax.swing.JLabel();
			jSeparator1 = new javax.swing.JSeparator();
			jLabel4 = new javax.swing.JLabel();
			ChooseTeam = new javax.swing.JComboBox();
			jLabel5 = new javax.swing.JLabel();
			ChooseActivity = new javax.swing.JComboBox();
			AttackAvatar = new javax.swing.JCheckBox();
			PickUpBones = new javax.swing.JCheckBox();
			HealOthers = new javax.swing.JCheckBox();
			Start = new javax.swing.JButton();
			GetSupplies = new javax.swing.JCheckBox();
			info = new javax.swing.JButton();
			donate = new javax.swing.JButton();
			BuryAtGrave = new javax.swing.JCheckBox();
			PickUpArrows = new javax.swing.JCheckBox();
			QuickPrayer = new javax.swing.JCheckBox();
			WeponSpec = new javax.swing.JCheckBox();
			jPanel3 = new javax.swing.JPanel();
			jLabel13 = new javax.swing.JLabel();
			jPanel2 = new javax.swing.JPanel();
			TakeBreak = new javax.swing.JCheckBox();
			jLabel6 = new javax.swing.JLabel();
			jLabel7 = new javax.swing.JLabel();
			MaxiumTimeUntillBreak = new javax.swing.JTextField();
			MinimiumTimeUntillBreak = new javax.swing.JTextField();
			jLabel8 = new javax.swing.JLabel();
			MinimiumTimeForBreak = new javax.swing.JTextField();
			jLabel9 = new javax.swing.JLabel();
			MaxiumTimeForBreak = new javax.swing.JTextField();
			jLabel10 = new javax.swing.JLabel();
			jLabel11 = new javax.swing.JLabel();
			jLabel12 = new javax.swing.JLabel();

			setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
			setTitle("SwiftSoulWars");
			setAlwaysOnTop(true);
			setBackground(java.awt.SystemColor.activeCaption);
			setCursor(new java.awt.Cursor(java.awt.Cursor.CROSSHAIR_CURSOR));
			setForeground(java.awt.Color.gray);
			setResizable(false);

			jLabel1.setText("<html><center><img src=\"http://img13.imageshack.us/img13/2519/sswz.png\"/></center></html>");

			jLabel2.setFont(new java.awt.Font("Tahoma", 0, 10));
			jLabel2.setText("Made/updated by Debauchery");

			jLabel3.setFont(new java.awt.Font("Tahoma", 1, 18));
			jLabel3.setText("Please fill in all areas;");

			jLabel4.setFont(new java.awt.Font("Tahoma", 0, 14));
			jLabel4.setText("Choose team:");

			ChooseTeam.setModel(new javax.swing.DefaultComboBoxModel(
					new String[] { "Random", "Red", "Blue", "Last Won",
							"Last Lost", "Clan Chat" }));
			ChooseTeam.addActionListener(new java.awt.event.ActionListener() {

				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					ChooseTeamActionPerformed(evt);
				}
			});

			jLabel5.setFont(new java.awt.Font("Tahoma", 0, 14));
			jLabel5.setText("Choose Activity: ");

			ChooseActivity.setModel(new javax.swing.DefaultComboBoxModel(
					new String[] { "Random", "Attack Players", "Attack Pyres",
							"Attack Jellies", "Pure Mode" }));
			ChooseActivity
					.addActionListener(new java.awt.event.ActionListener() {

						@Override
						public void actionPerformed(
								java.awt.event.ActionEvent evt) {
							ChooseActivityActionPerformed(evt);
						}
					});

			AttackAvatar.setText("Attack Avatar");
			AttackAvatar.addActionListener(new java.awt.event.ActionListener() {

				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					AttackAvatarActionPerformed(evt);
				}
			});

			PickUpBones.setText("Pick Up Bones");
			PickUpBones.addActionListener(new java.awt.event.ActionListener() {

				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					PickUpBonesActionPerformed(evt);
				}
			});

			HealOthers.setText("Heal Others");
			HealOthers.addActionListener(new java.awt.event.ActionListener() {

				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					HealOthersActionPerformed(evt);
				}
			});

			Start.setFont(new java.awt.Font("Tahoma", 0, 24));
			Start.setText("Start");
			Start.addActionListener(new java.awt.event.ActionListener() {

				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					StartActionPerformed(evt);
				}
			});

			GetSupplies.setText("Get Supplies");
			GetSupplies.addActionListener(new java.awt.event.ActionListener() {

				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					GetSuppliesActionPerformed(evt);
				}
			});

			info.setText("Infomation");
			info.addActionListener(new java.awt.event.ActionListener() {

				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					infoActionPerformed(evt);
				}
			});

			donate.setText("Donate");
			donate.addActionListener(new java.awt.event.ActionListener() {

				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					donateActionPerformed(evt);
				}
			});

			BuryAtGrave.setText("Bury At Grave");
			BuryAtGrave.addActionListener(new java.awt.event.ActionListener() {

				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					BuryAtGraveActionPerformed(evt);
				}
			});

			PickUpArrows.setText("Pick Up Arrows");
			PickUpArrows.addActionListener(new java.awt.event.ActionListener() {

				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					PickUpArrowsActionPerformed(evt);
				}
			});

			QuickPrayer.setText("Quick Prayer");
			QuickPrayer.addActionListener(new java.awt.event.ActionListener() {

				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					QuickPrayerActionPerformed(evt);
				}
			});

			WeponSpec.setText("Weapon Spec");
			WeponSpec.addActionListener(new java.awt.event.ActionListener() {

				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					WeponSpecActionPerformed(evt);
				}
			});

			javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(
					jPanel1);
			jPanel1.setLayout(jPanel1Layout);
			jPanel1Layout
					.setHorizontalGroup(jPanel1Layout
							.createParallelGroup(
									javax.swing.GroupLayout.Alignment.LEADING)
							.addGroup(
									jPanel1Layout
											.createSequentialGroup()
											.addComponent(jLabel3)
											.addContainerGap(107,
													Short.MAX_VALUE))
							.addComponent(jSeparator1,
									javax.swing.GroupLayout.DEFAULT_SIZE, 303,
									Short.MAX_VALUE)
							.addGroup(
									jPanel1Layout
											.createSequentialGroup()
											.addContainerGap()
											.addGroup(
													jPanel1Layout
															.createParallelGroup(
																	javax.swing.GroupLayout.Alignment.LEADING)
															.addGroup(
																	jPanel1Layout
																			.createSequentialGroup()
																			.addGroup(
																					jPanel1Layout
																							.createParallelGroup(
																									javax.swing.GroupLayout.Alignment.LEADING)
																							.addComponent(
																									info,
																									javax.swing.GroupLayout.DEFAULT_SIZE,
																									101,
																									Short.MAX_VALUE)
																							.addComponent(
																									donate,
																									javax.swing.GroupLayout.Alignment.TRAILING,
																									javax.swing.GroupLayout.DEFAULT_SIZE,
																									101,
																									Short.MAX_VALUE))
																			.addGap(8,
																					8,
																					8)
																			.addComponent(
																					Start,
																					javax.swing.GroupLayout.PREFERRED_SIZE,
																					174,
																					javax.swing.GroupLayout.PREFERRED_SIZE))
															.addGroup(
																	jPanel1Layout
																			.createSequentialGroup()
																			.addGroup(
																					jPanel1Layout
																							.createParallelGroup(
																									javax.swing.GroupLayout.Alignment.LEADING)
																							.addComponent(
																									jLabel4)
																							.addComponent(
																									jLabel5))
																			.addPreferredGap(
																					javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																			.addGroup(
																					jPanel1Layout
																							.createParallelGroup(
																									javax.swing.GroupLayout.Alignment.LEADING)
																							.addComponent(
																									ChooseTeam,
																									0,
																									175,
																									Short.MAX_VALUE)
																							.addComponent(
																									ChooseActivity,
																									0,
																									175,
																									Short.MAX_VALUE)))
															.addGroup(
																	jPanel1Layout
																			.createSequentialGroup()
																			.addGroup(
																					jPanel1Layout
																							.createParallelGroup(
																									javax.swing.GroupLayout.Alignment.LEADING)
																							.addGroup(
																									jPanel1Layout
																											.createSequentialGroup()
																											.addComponent(
																													PickUpBones)
																											.addPreferredGap(
																													javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																											.addComponent(
																													PickUpArrows))
																							.addGroup(
																									jPanel1Layout
																											.createSequentialGroup()
																											.addGroup(
																													jPanel1Layout
																															.createParallelGroup(
																																	javax.swing.GroupLayout.Alignment.LEADING)
																															.addComponent(
																																	GetSupplies)
																															.addComponent(
																																	AttackAvatar))
																											.addPreferredGap(
																													javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																											.addGroup(
																													jPanel1Layout
																															.createParallelGroup(
																																	javax.swing.GroupLayout.Alignment.LEADING)
																															.addComponent(
																																	QuickPrayer)
																															.addComponent(
																																	HealOthers))))
																			.addPreferredGap(
																					javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																			.addGroup(
																					jPanel1Layout
																							.createParallelGroup(
																									javax.swing.GroupLayout.Alignment.LEADING)
																							.addComponent(
																									WeponSpec)
																							.addComponent(
																									BuryAtGrave))))
											.addContainerGap()));
			jPanel1Layout
					.setVerticalGroup(jPanel1Layout
							.createParallelGroup(
									javax.swing.GroupLayout.Alignment.LEADING)
							.addGroup(
									jPanel1Layout
											.createSequentialGroup()
											.addComponent(jLabel3)
											.addPreferredGap(
													javax.swing.LayoutStyle.ComponentPlacement.RELATED)
											.addComponent(
													jSeparator1,
													javax.swing.GroupLayout.PREFERRED_SIZE,
													javax.swing.GroupLayout.DEFAULT_SIZE,
													javax.swing.GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(
													javax.swing.LayoutStyle.ComponentPlacement.RELATED)
											.addGroup(
													jPanel1Layout
															.createParallelGroup(
																	javax.swing.GroupLayout.Alignment.BASELINE)
															.addComponent(
																	jLabel4)
															.addComponent(
																	ChooseTeam,
																	javax.swing.GroupLayout.PREFERRED_SIZE,
																	javax.swing.GroupLayout.DEFAULT_SIZE,
																	javax.swing.GroupLayout.PREFERRED_SIZE))
											.addPreferredGap(
													javax.swing.LayoutStyle.ComponentPlacement.RELATED)
											.addGroup(
													jPanel1Layout
															.createParallelGroup(
																	javax.swing.GroupLayout.Alignment.BASELINE)
															.addComponent(
																	jLabel5)
															.addComponent(
																	ChooseActivity,
																	javax.swing.GroupLayout.PREFERRED_SIZE,
																	javax.swing.GroupLayout.DEFAULT_SIZE,
																	javax.swing.GroupLayout.PREFERRED_SIZE))
											.addPreferredGap(
													javax.swing.LayoutStyle.ComponentPlacement.RELATED)
											.addGroup(
													jPanel1Layout
															.createParallelGroup(
																	javax.swing.GroupLayout.Alignment.BASELINE)
															.addComponent(
																	AttackAvatar)
															.addComponent(
																	QuickPrayer)
															.addComponent(
																	WeponSpec))
											.addPreferredGap(
													javax.swing.LayoutStyle.ComponentPlacement.RELATED)
											.addGroup(
													jPanel1Layout
															.createParallelGroup(
																	javax.swing.GroupLayout.Alignment.BASELINE)
															.addComponent(
																	GetSupplies)
															.addComponent(
																	HealOthers))
											.addPreferredGap(
													javax.swing.LayoutStyle.ComponentPlacement.RELATED)
											.addGroup(
													jPanel1Layout
															.createParallelGroup(
																	javax.swing.GroupLayout.Alignment.BASELINE)
															.addComponent(
																	PickUpBones)
															.addComponent(
																	PickUpArrows)
															.addComponent(
																	BuryAtGrave))
											.addGroup(
													jPanel1Layout
															.createParallelGroup(
																	javax.swing.GroupLayout.Alignment.TRAILING)
															.addGroup(
																	jPanel1Layout
																			.createSequentialGroup()
																			.addPreferredGap(
																					javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																					14,
																					Short.MAX_VALUE)
																			.addComponent(
																					info)
																			.addPreferredGap(
																					javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																					8,
																					Short.MAX_VALUE)
																			.addComponent(
																					donate))
															.addGroup(
																	jPanel1Layout
																			.createSequentialGroup()
																			.addPreferredGap(
																					javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																			.addComponent(
																					Start,
																					javax.swing.GroupLayout.PREFERRED_SIZE,
																					55,
																					javax.swing.GroupLayout.PREFERRED_SIZE)))
											.addContainerGap()));

			jTabbedPane1.addTab("General", jPanel1);

			jLabel13.setFont(new java.awt.Font("Tahoma", 0, 14));
			jLabel13.setText("This features has not been completed");

			javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(
					jPanel3);
			jPanel3.setLayout(jPanel3Layout);
			jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(
					javax.swing.GroupLayout.Alignment.LEADING).addGroup(
					jPanel3Layout.createSequentialGroup().addGap(30, 30, 30)
							.addComponent(jLabel13)
							.addContainerGap(44, Short.MAX_VALUE)));
			jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(
					javax.swing.GroupLayout.Alignment.LEADING).addGroup(
					jPanel3Layout.createSequentialGroup().addContainerGap()
							.addComponent(jLabel13)
							.addContainerGap(204, Short.MAX_VALUE)));

			jTabbedPane1.addTab("Trade in Zeals", jPanel3);

			TakeBreak.setFont(new java.awt.Font("Tahoma", 0, 14));
			TakeBreak.setText("Take Breaks");
			TakeBreak.addActionListener(new java.awt.event.ActionListener() {

				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					TakeBreakActionPerformed(evt);
				}
			});

			jLabel6.setFont(new java.awt.Font("Tahoma", 0, 14));
			jLabel6.setText("Minimium Time Untill Break: ");

			jLabel7.setFont(new java.awt.Font("Tahoma", 0, 14));
			jLabel7.setText("Maxium Time Untill Break:");

			MaxiumTimeUntillBreak.setText("70");
			MaxiumTimeUntillBreak
					.addActionListener(new java.awt.event.ActionListener() {

						@Override
						public void actionPerformed(
								java.awt.event.ActionEvent evt) {
							MaxiumTimeUntillBreakActionPerformed(evt);
						}
					});

			MinimiumTimeUntillBreak.setText("40");
			MinimiumTimeUntillBreak
					.addActionListener(new java.awt.event.ActionListener() {

						@Override
						public void actionPerformed(
								java.awt.event.ActionEvent evt) {
							MinimiumTimeUntillBreakActionPerformed(evt);
						}
					});

			jLabel8.setFont(new java.awt.Font("Tahoma", 0, 14));
			jLabel8.setText("Minimium Time For Break:");

			MinimiumTimeForBreak.setText("15");
			MinimiumTimeForBreak
					.addActionListener(new java.awt.event.ActionListener() {

						@Override
						public void actionPerformed(
								java.awt.event.ActionEvent evt) {
							MinimiumTimeForBreakActionPerformed(evt);
						}
					});

			jLabel9.setFont(new java.awt.Font("Tahoma", 0, 14));
			jLabel9.setText("Maxium Time For Break:");

			MaxiumTimeForBreak.setText("60");
			MaxiumTimeForBreak
					.addActionListener(new java.awt.event.ActionListener() {

						@Override
						public void actionPerformed(
								java.awt.event.ActionEvent evt) {
							MaxiumTimeForBreakActionPerformed(evt);
						}
					});

			jLabel10.setFont(new java.awt.Font("Tahoma", 0, 10));
			jLabel10.setText("Please don't use the times supplied if using them");

			jLabel11.setFont(new java.awt.Font("Tahoma", 0, 10));
			jLabel11.setText("use them as a guide line, but if the same times are used all the");

			jLabel12.setFont(new java.awt.Font("Tahoma", 0, 10));
			jLabel12.setText("time it will become an easy way to detect a bot.");

			javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(
					jPanel2);
			jPanel2.setLayout(jPanel2Layout);
			jPanel2Layout
					.setHorizontalGroup(jPanel2Layout
							.createParallelGroup(
									javax.swing.GroupLayout.Alignment.LEADING)
							.addGroup(
									jPanel2Layout
											.createSequentialGroup()
											.addGroup(
													jPanel2Layout
															.createParallelGroup(
																	javax.swing.GroupLayout.Alignment.LEADING)
															.addGroup(
																	jPanel2Layout
																			.createSequentialGroup()
																			.addContainerGap()
																			.addGroup(
																					jPanel2Layout
																							.createParallelGroup(
																									javax.swing.GroupLayout.Alignment.LEADING)
																							.addComponent(
																									jLabel6)
																							.addComponent(
																									jLabel7)
																							.addComponent(
																									TakeBreak)
																							.addComponent(
																									jLabel8)
																							.addComponent(
																									jLabel9))
																			.addGap(18,
																					18,
																					18)
																			.addGroup(
																					jPanel2Layout
																							.createParallelGroup(
																									javax.swing.GroupLayout.Alignment.LEADING,
																									false)
																							.addComponent(
																									MaxiumTimeForBreak)
																							.addComponent(
																									MaxiumTimeUntillBreak)
																							.addComponent(
																									MinimiumTimeUntillBreak,
																									javax.swing.GroupLayout.DEFAULT_SIZE,
																									82,
																									Short.MAX_VALUE)
																							.addComponent(
																									MinimiumTimeForBreak)))
															.addGroup(
																	jPanel2Layout
																			.createSequentialGroup()
																			.addGap(32,
																					32,
																					32)
																			.addComponent(
																					jLabel12))
															.addGroup(
																	jPanel2Layout
																			.createSequentialGroup()
																			.addContainerGap()
																			.addComponent(
																					jLabel11))
															.addGroup(
																	jPanel2Layout
																			.createSequentialGroup()
																			.addGap(36,
																					36,
																					36)
																			.addComponent(
																					jLabel10)))
											.addContainerGap(21,
													Short.MAX_VALUE)));
			jPanel2Layout
					.setVerticalGroup(jPanel2Layout
							.createParallelGroup(
									javax.swing.GroupLayout.Alignment.LEADING)
							.addGroup(
									jPanel2Layout
											.createSequentialGroup()
											.addContainerGap()
											.addComponent(TakeBreak)
											.addPreferredGap(
													javax.swing.LayoutStyle.ComponentPlacement.RELATED)
											.addGroup(
													jPanel2Layout
															.createParallelGroup(
																	javax.swing.GroupLayout.Alignment.BASELINE)
															.addComponent(
																	jLabel6)
															.addComponent(
																	MinimiumTimeUntillBreak,
																	javax.swing.GroupLayout.PREFERRED_SIZE,
																	javax.swing.GroupLayout.DEFAULT_SIZE,
																	javax.swing.GroupLayout.PREFERRED_SIZE))
											.addPreferredGap(
													javax.swing.LayoutStyle.ComponentPlacement.RELATED)
											.addGroup(
													jPanel2Layout
															.createParallelGroup(
																	javax.swing.GroupLayout.Alignment.BASELINE)
															.addComponent(
																	jLabel7)
															.addComponent(
																	MaxiumTimeUntillBreak,
																	javax.swing.GroupLayout.PREFERRED_SIZE,
																	javax.swing.GroupLayout.DEFAULT_SIZE,
																	javax.swing.GroupLayout.PREFERRED_SIZE))
											.addPreferredGap(
													javax.swing.LayoutStyle.ComponentPlacement.RELATED)
											.addGroup(
													jPanel2Layout
															.createParallelGroup(
																	javax.swing.GroupLayout.Alignment.BASELINE)
															.addComponent(
																	jLabel8)
															.addComponent(
																	MinimiumTimeForBreak,
																	javax.swing.GroupLayout.PREFERRED_SIZE,
																	20,
																	javax.swing.GroupLayout.PREFERRED_SIZE))
											.addPreferredGap(
													javax.swing.LayoutStyle.ComponentPlacement.RELATED)
											.addGroup(
													jPanel2Layout
															.createParallelGroup(
																	javax.swing.GroupLayout.Alignment.BASELINE)
															.addComponent(
																	jLabel9)
															.addComponent(
																	MaxiumTimeForBreak,
																	javax.swing.GroupLayout.PREFERRED_SIZE,
																	javax.swing.GroupLayout.DEFAULT_SIZE,
																	javax.swing.GroupLayout.PREFERRED_SIZE))
											.addPreferredGap(
													javax.swing.LayoutStyle.ComponentPlacement.RELATED,
													38, Short.MAX_VALUE)
											.addComponent(
													jLabel10,
													javax.swing.GroupLayout.PREFERRED_SIZE,
													13,
													javax.swing.GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(
													javax.swing.LayoutStyle.ComponentPlacement.RELATED)
											.addComponent(
													jLabel11,
													javax.swing.GroupLayout.PREFERRED_SIZE,
													13,
													javax.swing.GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(
													javax.swing.LayoutStyle.ComponentPlacement.RELATED)
											.addComponent(jLabel12)
											.addContainerGap()));

			jTabbedPane1.addTab("Break Handler", jPanel2);

			javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
					getContentPane());
			getContentPane().setLayout(layout);
			layout.setHorizontalGroup(layout
					.createParallelGroup(
							javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(
							layout.createSequentialGroup()
									.addGroup(
											layout.createParallelGroup(
													javax.swing.GroupLayout.Alignment.LEADING)
													.addGroup(
															layout.createSequentialGroup()
																	.addGap(91,
																			91,
																			91)
																	.addComponent(
																			jLabel2))
													.addGroup(
															layout.createSequentialGroup()
																	.addContainerGap()
																	.addGroup(
																			layout.createParallelGroup(
																					javax.swing.GroupLayout.Alignment.LEADING)
																					.addComponent(
																							jTabbedPane1,
																							javax.swing.GroupLayout.DEFAULT_SIZE,
																							308,
																							Short.MAX_VALUE)
																					.addComponent(
																							jLabel1,
																							javax.swing.GroupLayout.DEFAULT_SIZE,
																							308,
																							Short.MAX_VALUE))))
									.addContainerGap()));
			layout.setVerticalGroup(layout
					.createParallelGroup(
							javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(
							layout.createSequentialGroup()
									.addComponent(
											jLabel1,
											javax.swing.GroupLayout.PREFERRED_SIZE,
											javax.swing.GroupLayout.DEFAULT_SIZE,
											javax.swing.GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(
											javax.swing.LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(
											jTabbedPane1,
											javax.swing.GroupLayout.DEFAULT_SIZE,
											260, Short.MAX_VALUE)
									.addPreferredGap(
											javax.swing.LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(jLabel2)));

			pack();
		}// </editor-fold>

		// <editor-fold defaultstate="collapsed" desc="GUI Actions">
		private void ChooseTeamActionPerformed(java.awt.event.ActionEvent evt) {
			switch (ChooseTeam.getSelectedIndex()) {
			case 0:
				randomTeam = true;
				redTeam = false;
				blueTeam = false;
				lastWonTeam = false;
				lastLostTeam = false;
				clanChatTeam = false;
				break;
			case 1:
				randomTeam = false;
				redTeam = true;
				blueTeam = false;
				lastWonTeam = false;
				lastLostTeam = false;
				clanChatTeam = false;
				break;
			case 2:
				randomTeam = false;
				redTeam = false;
				blueTeam = true;
				lastWonTeam = false;
				lastLostTeam = false;
				clanChatTeam = false;
				break;
			case 3:
				randomTeam = false;
				redTeam = false;
				blueTeam = false;
				lastWonTeam = true;
				lastLostTeam = false;
				clanChatTeam = false;
				break;
			case 4:
				randomTeam = false;
				redTeam = false;
				blueTeam = false;
				lastWonTeam = false;
				lastLostTeam = true;
				clanChatTeam = false;
				break;
			case 5:
				randomTeam = false;
				redTeam = false;
				blueTeam = false;
				lastWonTeam = false;
				lastLostTeam = false;
				clanChatTeam = true;
				break;
			}
		}

		private void StartActionPerformed(java.awt.event.ActionEvent evt) {
			startScript = true;
			switch (ChooseTeam.getSelectedIndex()) {
			case 0:
				randomTeam = true;
				redTeam = false;
				blueTeam = false;
				lastWonTeam = false;
				lastLostTeam = false;
				clanChatTeam = false;
				break;
			case 1:
				randomTeam = false;
				redTeam = true;
				blueTeam = false;
				lastWonTeam = false;
				lastLostTeam = false;
				clanChatTeam = false;
				break;
			case 2:
				randomTeam = false;
				redTeam = false;
				blueTeam = true;
				lastWonTeam = false;
				lastLostTeam = false;
				clanChatTeam = false;
				break;
			case 3:
				randomTeam = false;
				redTeam = false;
				blueTeam = false;
				lastWonTeam = true;
				lastLostTeam = false;
				clanChatTeam = false;
				break;
			case 4:
				randomTeam = false;
				redTeam = false;
				blueTeam = false;
				lastWonTeam = false;
				lastLostTeam = true;
				clanChatTeam = false;
				break;
			case 5:
				randomTeam = false;
				redTeam = false;
				blueTeam = false;
				lastWonTeam = false;
				lastLostTeam = false;
				clanChatTeam = true;
				break;
			}
			startScript = true;
			healOthers = (HealOthers.isSelected() ? true : false);
			pickUpBones = (PickUpBones.isSelected() ? true : false);
			attackAvatar = (AttackAvatar.isSelected() ? true : false);
			switch (ChooseActivity.getSelectedIndex()) {
			case 0:
				randomStrat = true;
				attackPlayers = false;
				attackPyres = false;
				attackJellies = false;
				pureMode = false;
				break;
			case 1:
				randomStrat = false;
				attackPlayers = true;
				attackPyres = false;
				attackJellies = false;
				pureMode = false;
				break;
			case 2:
				randomStrat = false;
				attackPlayers = false;
				attackPyres = true;
				attackJellies = false;
				pureMode = false;
				break;
			case 3:
				randomStrat = false;
				attackPlayers = false;
				attackPyres = false;
				attackJellies = true;
				pureMode = false;
				break;
			case 4:
				randomStrat = false;
				attackPlayers = false;
				attackPyres = false;
				attackJellies = false;
				pureMode = true;
				break;
			}
			getSupplies = (GetSupplies.isSelected() ? true : false);
			takeBreak = (TakeBreak.isSelected() ? true : false);
			maxiumTimeForBreak = Integer.parseInt(MaxiumTimeForBreak.getText());
			minimiumTimeForBreak = Integer.parseInt(MinimiumTimeForBreak
					.getText());
			maxiumTimeUntillBreak = Integer.parseInt(MaxiumTimeUntillBreak
					.getText());
			minimiumTimeUntillBreak = Integer.parseInt(MinimiumTimeUntillBreak
					.getText());
			pickUpArrows = (PickUpArrows.isSelected() ? true : false);
			buryAtGrave = (BuryAtGrave.isSelected() ? true : false);
			weaponSpec = (WeponSpec.isSelected() ? true : false);
			quickPrayer = (QuickPrayer.isSelected() ? true : false);
		}

		private void HealOthersActionPerformed(java.awt.event.ActionEvent evt) {
			healOthers = (HealOthers.isSelected() ? true : false);
		}

		private void PickUpBonesActionPerformed(java.awt.event.ActionEvent evt) {
			pickUpBones = (PickUpBones.isSelected() ? true : false);
		}

		private void AttackAvatarActionPerformed(java.awt.event.ActionEvent evt) {
			attackAvatar = (AttackAvatar.isSelected() ? true : false);
		}

		private void ChooseActivityActionPerformed(
				java.awt.event.ActionEvent evt) {
			switch (ChooseActivity.getSelectedIndex()) {
			case 0:
				randomStrat = true;
				attackPlayers = false;
				attackPyres = false;
				attackJellies = false;
				pureMode = false;
				break;
			case 1:
				randomStrat = false;
				attackPlayers = true;
				attackPyres = false;
				attackJellies = false;
				pureMode = false;
				break;
			case 2:
				randomStrat = false;
				attackPlayers = false;
				attackPyres = true;
				attackJellies = false;
				pureMode = false;
				break;
			case 3:
				randomStrat = false;
				attackPlayers = false;
				attackPyres = false;
				attackJellies = true;
				pureMode = false;
				break;
			case 4:
				randomStrat = false;
				attackPlayers = false;
				attackPyres = false;
				attackJellies = false;
				pureMode = true;
				break;
			}
		}

		private void GetSuppliesActionPerformed(java.awt.event.ActionEvent evt) {
			getSupplies = (GetSupplies.isSelected() ? true : false);
		}

		private void TakeBreakActionPerformed(java.awt.event.ActionEvent evt) {
			takeBreak = (TakeBreak.isSelected() ? true : false);
		}

		private void MaxiumTimeForBreakActionPerformed(
				java.awt.event.ActionEvent evt) {
			maxiumTimeForBreak = Integer.parseInt(MaxiumTimeForBreak.getText());
		}

		private void MinimiumTimeForBreakActionPerformed(
				java.awt.event.ActionEvent evt) {
			minimiumTimeForBreak = Integer.parseInt(MinimiumTimeForBreak
					.getText());
		}

		private void MaxiumTimeUntillBreakActionPerformed(
				java.awt.event.ActionEvent evt) {
			maxiumTimeUntillBreak = Integer.parseInt(MaxiumTimeUntillBreak
					.getText());
		}

		private void MinimiumTimeUntillBreakActionPerformed(
				java.awt.event.ActionEvent evt) {
			minimiumTimeUntillBreak = Integer.parseInt(MinimiumTimeUntillBreak
					.getText());
		}

		private void infoActionPerformed(java.awt.event.ActionEvent evt) {
			String info = "http://www.powerbot.org/vb/showthread.php?t=560367";
			try {
				java.awt.Desktop.getDesktop().browse(java.net.URI.create(info));
			} catch (Exception e) {
			}
		}

		private void donateActionPerformed(java.awt.event.ActionEvent evt) {
			String donate = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=3QP2NLZS3N65W";
			try {
				java.awt.Desktop.getDesktop().browse(
						java.net.URI.create(donate));
			} catch (Exception e) {
			}
		}

		private void PickUpArrowsActionPerformed(java.awt.event.ActionEvent evt) {
			pickUpArrows = (PickUpArrows.isSelected() ? true : false);
		}

		private void BuryAtGraveActionPerformed(java.awt.event.ActionEvent evt) {
			buryAtGrave = (BuryAtGrave.isSelected() ? true : false);
		}

		private void WeponSpecActionPerformed(java.awt.event.ActionEvent evt) {
			weaponSpec = (WeponSpec.isSelected() ? true : false);
		}

		private void QuickPrayerActionPerformed(java.awt.event.ActionEvent evt) {
			quickPrayer = (QuickPrayer.isSelected() ? true : false);
		}

		// </editor-fold>
		// Variables declaration - do not modify
		private javax.swing.JCheckBox AttackAvatar;
		private javax.swing.JCheckBox BuryAtGrave;
		private javax.swing.JComboBox ChooseActivity;
		private javax.swing.JComboBox ChooseTeam;
		private javax.swing.JCheckBox GetSupplies;
		private javax.swing.JCheckBox HealOthers;
		private javax.swing.JTextField MaxiumTimeForBreak;
		private javax.swing.JTextField MaxiumTimeUntillBreak;
		private javax.swing.JTextField MinimiumTimeForBreak;
		private javax.swing.JTextField MinimiumTimeUntillBreak;
		private javax.swing.JCheckBox PickUpArrows;
		private javax.swing.JCheckBox PickUpBones;
		private javax.swing.JCheckBox QuickPrayer;
		private javax.swing.JButton Start;
		private javax.swing.JCheckBox TakeBreak;
		private javax.swing.JCheckBox WeponSpec;
		private javax.swing.JButton donate;
		private javax.swing.JButton info;
		private javax.swing.JLabel jLabel1;
		private javax.swing.JLabel jLabel10;
		private javax.swing.JLabel jLabel11;
		private javax.swing.JLabel jLabel12;
		private javax.swing.JLabel jLabel13;
		private javax.swing.JLabel jLabel2;
		private javax.swing.JLabel jLabel3;
		private javax.swing.JLabel jLabel4;
		private javax.swing.JLabel jLabel5;
		private javax.swing.JLabel jLabel6;
		private javax.swing.JLabel jLabel7;
		private javax.swing.JLabel jLabel8;
		private javax.swing.JLabel jLabel9;
		private javax.swing.JPanel jPanel1;
		private javax.swing.JPanel jPanel2;
		private javax.swing.JPanel jPanel3;
		private javax.swing.JSeparator jSeparator1;
		private javax.swing.JTabbedPane jTabbedPane1;
		// End of variables declaration
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="MessageListener">
	@Override
	public void messageReceived(MessageEvent e) {
		String message = e.getMessage().toLowerCase();
		if (e.getID() == MessageEvent.MESSAGE_SERVER
				|| e.getID() == MessageEvent.MESSAGE_CLIENT
				|| e.getID() == MessageEvent.MESSAGE_ACTION) {
			if (message.contains("you receive 1 zeal")) {
				lost++;
				result = "lostLast";
				zeal++;
			} else if (message.contains("you receive 2 zeal")) {
				drew++;
				result = "drewLast";
				zeal += 2;
			} else if (message.contains("you receive 3 zeal")) {
				won++;
				result = "wonLast";
				zeal += 3;
			} else if (message.contains("This chat is currently full")) {
				inClan = false;
			} else if (message
					.contains("You have been kicked from the channel")) {
				inClan = false;
				log("You got kicked from clan channel!");
			} else if (message
					.contains("You are temporarily banned from this clan channel")) {
				inClan = false;
				log("You got banned from clan channel!");
			} else if (message
					.contains("You cannot take non-combat items into the arena")) {
				log("You have something that it stopping us from entering a game.");
				game.logout(true);
				stopScript();
			}
		}
		if (clanChatTeam && listenTime()) {
			if (e.getID() == MessageEvent.MESSAGE_CLAN_CHAT) {
				message.replace("-", "");
				message.replace("=", "");
				message.replace(";", "");
				message.replace(":", "");
				message.replace("*", "");
				message.replace("$", "");
				message.replace("£", "");
				message.replace("_", "");
				if (!message.contains("bot") && !message.contains("winnin")
						&& !message.contains("losin")) {
					if (message.contains("blue") || message.contains("b l u e")
							|| message.contains("b  l  u  e")) {
						blueShouts += 1;
					} else if (message.contains("red")
							|| message.contains("r e d")
							|| message.contains("r  e  d")) {
						redShouts += 1;
					}
				}
			}
		}
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Paint">
	private Image getImage(String url) {
		try {
			return ImageIO.read(new URL(url));

		} catch (IOException e) {
			return null;

		}
	}

	private final Image img1 = getImage("http://img43.imageshack.us/img43/5479/logoym.png");
	private final Image hideImg = getImage("http://www.authorstream.com/images/close_icon.gif");

	@Override
	public void onRepaint(Graphics g1) {
		Graphics2D g = (Graphics2D) g1;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		if (!hide) {
			hideRect = new Rectangle(503, 210, 14, 14);
			g.setColor(new Color(0, 0, 0, 100));
			g.fillRect(0, 208, 519, 130);
			g.setColor(new Color(0, 0, 0));
			g.setStroke(new BasicStroke(1));
			g.drawRect(0, 208, 519, 130);
			g.drawImage(img1, 2, 209, null);
			g.setFont(new Font("SansSerif", 0, 15));
			g.setColor(new Color(255, 255, 255, 200));
			g.drawString(
					"Version: "
							+ getClass().getAnnotation(ScriptManifest.class)
									.version(), 119, 332);
			g.setFont(new Font("SansSerif", 0, 10));
			g.drawString("Time:  " + run.toElapsedString(), 300, 219);
			g.drawString("Current Task:  " + task, 300, 234);
			g.drawString("Zeal:  " + zeal, 300, 251);
			g.drawString("Won:  " + won, 300, 268);
			g.drawString("Lost:  " + lost, 300, 285);
			g.drawString("Drew:  " + drew, 300, 302);
			g.drawString("Kicked:  " + kicked, 300, 319);
			if (clanChatTeam) {
				g.drawString("Clan Team Calls", 420, 285);
				g.drawString("Blue:  " + blueShouts, 420, 302);
				g.drawString("Red:  " + redShouts, 420, 319);
			}
			if (takeBreak) {
				g.drawString("" + breakHandlerStatus, 300, 336);
			}
			g.drawImage(hideImg, 504, 210, null);
		} else {
			hideRect = new Rectangle(503, 324, 14, 14);
			g.setColor(new Color(0, 0, 0, 100));
			g.fillRect(368, 322, 150, 16);
			g.setColor(new Color(0, 0, 0));
			g.setStroke(new BasicStroke(1));
			g.drawRect(368, 322, 150, 16);
			g.drawImage(hideImg, 503, 324, null);
			g.setColor(new Color(255, 255, 255, 200));
			g.setFont(new Font("SansSerif", 0, 10));
			g.drawString("Time: " + run.toElapsedString() + " | Zeal: " + zeal,
					372, 335);
		}
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="MouseListener">
	@Override
	public void mouseClicked(MouseEvent e) {
		Point p = e.getPoint();
		if (hideRect.contains(p)) {
			hide = (hide ? false : true);
		}
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

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Antiban">
	class Angle extends Thread {

		private volatile boolean stop = false;
		private volatile boolean pause = false;

		@Override
		public void run() {
			try {
				while (!stop) {
					if (takingBreak) {
						while (takingBreak) {
							Thread.sleep(random(25, 150));
						}
					}
					if (pause) {
						while (pause) {
							Thread.sleep(random(25, 150));
						}
					}
					if (random(1, 15000) == 342) {
						char key = 0;
						if (random(1, 5) == 2) {
							key = KeyEvent.VK_RIGHT;
						}
						if (random(1, 5) == 2) {
							key = KeyEvent.VK_LEFT;
						}
						final long endTime = System.currentTimeMillis()
								+ random(250, 1200);
						if (key != 0) {
							keyboard.pressKey(key);
							while (System.currentTimeMillis() < endTime) {
								if (pause) {
									break;
								}
								Thread.sleep(random(5, 15));
							}
							keyboard.releaseKey(key);
						}
						Thread.sleep(random(random(6000, 8000),
								random(19000, 20000)));
					}
				}
			} catch (Exception e) {
			}
		}
	}

	class Pitch extends Thread {

		private volatile boolean stop = false;
		private volatile boolean pause = false;

		@Override
		public void run() {
			try {
				while (!stop) {
					if (takingBreak) {
						while (takingBreak) {
							Thread.sleep(random(25, 150));
						}
					}
					if (pause) {
						while (pause) {
							Thread.sleep(random(25, 150));
						}
					}
					if (random(1, 15000) == 342) {
						char key = 0;
						if (camera.getPitch() < random(random(50, 86), 100)) {
							key = KeyEvent.VK_UP;
						}
						if (camera.getPitch() >= random(random(50, 86), 100)) {
							key = KeyEvent.VK_DOWN;
						}
						if (key != 0) {
							keyboard.pressKey(key);
							final long endTime = System.currentTimeMillis()
									+ random(random(50, 150), 500);
							while (System.currentTimeMillis() < endTime) {
								if (pause) {
									break;
								} else if (camera.getPitch() == 0) {
									if (key == KeyEvent.VK_DOWN) {
										break;
									}
								} else if (camera.getPitch() == 100) {
									if (key == KeyEvent.VK_UP) {
										break;
									}
								}
								Thread.sleep(random(5, 15));
							}
							keyboard.releaseKey(key);
						}
						Thread.sleep(random(random(6000, 8000),
								random(19000, 20000)));
					}
				}
			} catch (Exception e) {
			}
		}
	}

	class ExtraAntiban extends Thread {

		private volatile boolean stop = false;
		private volatile boolean pause = false;

		@Override
		public void run() {
			try {
				while (!stop) {
					if (takingBreak) {
						while (takingBreak) {
							Thread.sleep(random(25, 150));
						}
					}
					switch (random(0, 200)) {
					case 3:
						camera.moveRandomly(random(100, 900));
						break;
					case 70:
						mouse.moveOffScreen();
						Thread.sleep(random(600, 900));
					case 100:
						mouse.moveSlightly();
						break;
					case 130:
						skills.doHover(Skills.CONSTRUCTION);
						Thread.sleep(200, 500);
						break;
					case 180:
						camera.setNorth();
						break;
					case 200:
						RSPlayer player = players.getNearest(random(40, 132));
						if (player != null && player.isOnScreen()) {
							RSModel mod = player.getModel();
							if (mod != null) {
								Point p = mod.getPoint();
								if (p != null) {
									mouse.move(p, 10);
									mouse.click(false);
								}
							}
						}
						break;
					default:
						Thread.sleep(random(random(6000, 8000),
								random(19000, 20000)));
						break;
					}
				}
			} catch (Exception e) {
			}
		}
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Break Handler">
	class BreakHandler extends Thread {

		private volatile boolean stop = false;
		private volatile boolean pause = false;

		@Override
		public void run() {
			try {
				while (!stop) {
					if (takeBreak) {
						long takingBreakIn = random(
								minimiumTimeUntillBreak * 60000,
								maxiumTimeUntillBreak * 60000);
						long ranFor = System.currentTimeMillis();
						while ((ranFor + takingBreakIn) > System
								.currentTimeMillis() && !stop) {
							Thread.sleep(1);
							breakHandlerStatus = "Taking break in:  "
									+ Timer.format((ranFor + takingBreakIn)
											- System.currentTimeMillis());
						}
						takingBreak = true;
						while (!startedBreak && !stop) {
							Thread.sleep(1);
							breakHandlerStatus = "Taking break after this game";
						}
						long takingBreakFor = random(
								minimiumTimeForBreak * 60000,
								maxiumTimeForBreak * 60000);
						long breakingFor = System.currentTimeMillis();
						while ((breakingFor + takingBreakFor) > System
								.currentTimeMillis() && !stop) {
							Thread.sleep(1);
							breakHandlerStatus = "Taking break for:  "
									+ Timer.format(((breakingFor + takingBreakFor) - System
											.currentTimeMillis()));
						}
						takingBreak = false;
						while (!stoppedBreak && !stop) {
							Thread.sleep(1);
							breakHandlerStatus = "Logging back in hopefully...";
						}
					}
				}
			} catch (Exception e) {
			}
		}
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Finish">
	@Override
	public void onFinish() {
		angle.stop = true;
		pitch.stop = true;
		extraAntiban.stop = true;
		if (breakHandler != null) {
			breakHandler.stop = true;
		}
		log("Thanks for using DebaucherySoulWars");
	}
	// </editor-fold>

	/*
	 * TODO; - Optional trade in zeals.
	 */
}