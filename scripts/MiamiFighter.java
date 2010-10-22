import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.*;
import org.rsbot.script.wrappers.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.ImageObserver;
import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


@ScriptManifest(website = "Wtf zal wel",
		authors = {"DutchSniper"},
		keywords = "Combat",
		name = "MiamiFighter",
		version = 1.00,
		description = "Settings in GUI"
)
public class MiamiFighter extends Script implements PaintListener, MouseListener {

	//------------------------------------------------------------
	// Part 1.1.1 Basic Things - System Variables
	//------------------------------------------------------------
	FightingSystem fightingSystem = new FightingSystem();
	HealingSystem healingSystem = new HealingSystem();
	LootingSystem lootingSystem = new LootingSystem();
	AntiBanSystem antiBanSystem = new AntiBanSystem();
	ControlSystem1 controlSystem1 = new ControlSystem1();
	ControlSystem2 controlSystem2 = new ControlSystem2();

	InfoDatabase info = new InfoDatabase();
	MethodCollection method = new MethodCollection();
	SettingDatabase setting = new SettingDatabase();
	PathMakerLibrary pathLibrary = new PathMakerLibrary();

	ExtraSystem extraSystem = new ExtraSystem();
	MiamiFighterGUI gui;

	boolean lostConnection = false;

	//------------------------------------------------------------
	// Part 1.3 Basic Things - Loop
	//------------------------------------------------------------
	@Override
	public int loop() {
		if (!game.isLoggedIn() || gui.isActive() || gui.isVisible()) {
			lostConnection = true;
			return 0;
		} else if (lostConnection) {
			lostConnection = false;
			return 5000;
		}
		if (!controlSystem1.controlSystemLoop()) {
			return 0;
		}

		if (!healingSystem.healingSystemLoop()) {
			return 0;
		}

		if (!controlSystem2.controlSystem2Loop()) {
			return 0;
		}

		if (!lootingSystem.lootingSystemLoop()) {
			return 0;
		}

		if (!antiBanSystem.antiBanSystemLoop()) {
			return 0;
		}

		if (!fightingSystem.fightingSystemLoop()) {
			return 0;
		}

		return 0;
	}

	@Override
	public boolean onStart() {
		gui = new MiamiFighterGUI();
		gui.setVisible(true);
		return true;
	}

	@Override
	public void onFinish() {
		String url = "http://gametuning.net76.net/MiamiFighter/GeneralThings.php?SecondsRunning=" +
				Math.round((System.currentTimeMillis() - controlSystem1.lastTimeUpdated) / 1000) +
				"&MoneyGained=" + (extraSystem.inventoryItemCheck.howMuchMoneyIsMade() - controlSystem1.lastTimeMoneyUpdate) +
				"&XPGained=" + (extraSystem.miamiPaint.totalXPGained - controlSystem1.lastTimeXPGained);
		try {
			new URL(url).openStream();
		} catch (Exception e) {
		}
	}

	//------------------------------------------------------------
	// Part 2.1 Systems - Fighting
	//------------------------------------------------------------
	public class FightingSystem {
		RSNPC monster = null;
		boolean specialIsNow = false;

		public boolean fightingSystemLoop() {
			if (!fightingSystemSpecialAttack()) {
				return false;
			}
			if (!fightingSystemFight()) {
				return false;
			}
			return true;
		}


		public boolean fightingSystemFight() {
			if ((!(getMyPlayer().getInteracting() instanceof RSNPC)) && (!getMyPlayer().isInCombat() || setting.multiCombat)) {
				monster = getNearestMonster(setting.arrayOfMonsters);
				if (monster != null) {
					try {
						for (Monster tempMonster : setting.arrayOfMonsters) {
							if (tempMonster.getID() != monster.getID()) {
								continue;
							} else {
								if (!calc.tileOnScreen(monster.getLocation())) {
									method.walkToTileCombatZone(monster.getLocation());
									return false;
								}
								if (!monster.doAction("Attack " + monster.getName() + " (Level: " + monster.getLevel() + ")")) {
									return false;
								}
								if (random(0, 2) == 0) {
									camera.setAngle(method.getAngleToTile(monster.getLocation()) + random(-10, +10));
									sleep(random(100, 150));
								}
								sleep(random(1500, 2000));
								while (getMyPlayer().isMoving()) {
									if (method.getLifePoints() < info.eatAt) {
										return false;
									}
									sleep(random(85, 150));
								}
								sleep(random(1200, 1500));
								return true;
							}
						}
					} catch (Exception e) {
						return false;
					}
				}
			} else {
				sleep(150, 250);
			}
			return true;
		}

		public RSNPC getNearestMonster(Monster... IDs) {
			int Dist = 99;
			RSNPC closest = null;
			for (RSNPC aMonster : npcs.getAll()) {
				if (!method.isInCombatZone(aMonster.getLocation())) {
					continue;
				}
				try {
					if (aMonster.getInteracting() != null) {
						if (aMonster.getInteracting().equals(getMyPlayer()) && getMyPlayer().isInCombat()) {
							if (!setting.multiCombat) {
								return aMonster;
							}
							int distance = calc.distanceTo(aMonster);
							if (distance < Dist) {
								Dist = distance;
								closest = aMonster;
							}
							continue;
						}
					}
				} catch (Exception ignored) {
				}
				if (aMonster.isInCombat())
					continue;
				for (Monster id : IDs) {
					if (id.getID() != aMonster.getID()) {
						continue;
					}
					int distance = calc.distanceTo(aMonster);
					if (distance < Dist) {
						Dist = distance;
						closest = aMonster;
					}
					break;
				}
			}

			return closest;
		}

		public boolean fightingSystemSpecialAttack() {
			if (setting.useSpecial) {
				if (!setting.useSecondaryWeaponSpecial) {
					if (settings.getSetting(300) >= setting.atWhatPercentToUseSpecial * 10 && settings.getSetting(Settings.SETTING_SPECIAL_ATTACK_ENABLED) == 0) {
						if (game.getCurrentTab() != Game.TAB_ATTACK) {
							game.openTab(Game.TAB_ATTACK);
							sleep(random(300, 900));
						}
						interfaces.getComponent(884, 4).doAction("");
						sleep(random(200, 500));
					}
				} else {
					if (settings.getSetting(Settings.SETTING_SPECIAL_ATTACK_ENABLED) == 0 && settings.getSetting(300) >= setting.atWhatPercentToUseSpecial) {
						if (inventory.contains(setting.secondaryWeaponID)) {
							inventory.getItem(setting.secondaryWeaponID).doAction("");
							sleep(random(400, 600));
						}
						if (game.getCurrentTab() != Game.TAB_ATTACK) {
							game.openTab(Game.TAB_ATTACK);
							sleep(random(300, 900));
						}
						if (info.styleSecondaryWeaponColor != 0) {
							if (interfaces.getComponent(884, info.styleSecondaryWeaponColor).getBackgroundColor() != 654) {
								interfaces.getComponent(884, info.styleSecondaryWeaponClick).doAction("");
								sleep(random(400, 500));
							}
						}
						interfaces.getComponent(884, 4).doAction("");
						sleep(random(200, 500));

						if (game.getCurrentTab() != Game.TAB_INVENTORY) {
							game.openTab(Game.TAB_INVENTORY);
							sleep(random(300, 900));
						}
						specialIsNow = true;
					} else if (settings.getSetting(300) <= setting.atWhatPercentToUseSpecial) {
						if (inventory.contains(setting.primaryWeaponID)) {
							inventory.getItem(setting.primaryWeaponID).doAction("");
							sleep(random(400, 600));
						}
						if (info.stylePrimaryWeaponColor != 0) {
							if (interfaces.getComponent(884, info.stylePrimaryWeaponColor).getBackgroundColor() != 654) {
								if (game.getCurrentTab() != Game.TAB_ATTACK) {
									game.openTab(Game.TAB_ATTACK);
									sleep(random(300, 900));
								}
								interfaces.getComponent(884, info.stylePrimaryWeaponClick).doAction("");
								sleep(random(400, 500));
							}
						}
						specialIsNow = false;
					}
				}
			}
			return true;
		}
	}

	//------------------------------------------------------------
	// Part 2.2 Systems - Healing
	//------------------------------------------------------------
	public class HealingSystem {
		boolean healingSystemStart = true;

		boolean guthansOn = false;

		//--------------------------------------------------------
		// Class Part 2.1.1 Systems - Healing - Loop
		//--------------------------------------------------------
		public boolean healingSystemLoop() {
			if (healingSystemStart) {
				healingSystemStart();
				healingSystemStart = false;
			}
			if (setting.useFood || setting.useB2P) {
				if (!healingSystemUsingFood()) {
					return false;
				}
			}
			if (setting.useGuthans) {
				if (!healingSystemGuthans()) {
					return false;
				}
			}
			if (inventory.containsOneOf(info.allPots)) {
				if (!healingSystemPotions()) {
					return false;
				}
			}
			return true;
		}

		//--------------------------------------------------------
		// Class Part 2.1.2 Systems - Healing - Start
		//--------------------------------------------------------
		public void healingSystemStart() {

		}

		//--------------------------------------------------------
		// Class Part 2.1.3 Systems - Healing - Using Food
		//--------------------------------------------------------
		public boolean healingSystemUsingFood() {
			if (method.getLifePoints() < info.eatAt) {
				for (int a : info.foodID) {
					if (inventory.contains(a)) {
						inventory.getItem(a).doAction("Eat");
						sleep(random(1500, 2000));
						info.eatAt = random(info.eatAtMin, info.eatAtMax);
						return false;
					}
				}
			}
			return true;
		}

		//--------------------------------------------------------
		// Class Part 2.1.4 Systems - Healing - Potions
		//--------------------------------------------------------
		public boolean healingSystemPotions() {
			int currentAttLevel = skills.getCurrentLevel(Skills.ATTACK);
			int realAttLevel = skills.getRealLevel(Skills.ATTACK);
			int currentDefLevel = skills.getCurrentLevel(Skills.DEFENSE);
			int realDefLevel = skills.getRealLevel(Skills.DEFENSE);
			int currentStrLevel = skills.getCurrentLevel(Skills.STRENGTH);
			int realStrLevel = skills.getRealLevel(Skills.STRENGTH);
			int currentRangeLVL = skills.getCurrentLevel(Skills.RANGE);
			int realRangeLVL = method.getRealLevel(Skills.RANGE);
			int prayNow = skills.getCurrentLevel(Skills.PRAYER);

			if (currentAttLevel <= realAttLevel && currentStrLevel <= realStrLevel) {
				for (int pot : info.combatPots) {
					if (inventory.contains(pot)) {
						inventory.getItem(pot).doAction("Drink");
						sleep(1200, 1500);
						return false;
					}
				}
				for (int pot : info.overloadPots) {
					if (inventory.contains(pot)) {
						inventory.getItem(pot).doAction("Drink");
						sleep(1200, 1500);
						return false;
					}
				}
			}
			if (currentAttLevel <= realAttLevel) {
				for (int pot : info.attackPotsAll) {
					if (inventory.contains(pot)) {
						inventory.getItem(pot).doAction("Drink");
						sleep(1200, 1500);
						return false;
					}
				}
			}
			if (currentStrLevel <= realStrLevel) {
				for (int pot : info.strengthPotsAll) {
					if (inventory.contains(pot)) {
						inventory.getItem(pot).doAction("Drink");
						sleep(1200, 1500);
						return false;
					}
				}
			}
			if (currentDefLevel <= realDefLevel) {
				for (int pot : info.defencePotsAll) {
					if (inventory.contains(pot)) {
						inventory.getItem(pot).doAction("Drink");
						sleep(1200, 1500);
						return false;
					}
				}
			}
			if (prayNow <= random(5, 12)) {
				for (int pot : info.prayerPots) {
					if (inventory.contains(pot)) {
						inventory.getItem(pot).doAction("Drink");
						sleep(1200, 1500);
						return false;
					}
				}
			}
			if (currentRangeLVL <= realRangeLVL) {
				for (int pot : info.rangedPotsAll) {
					if (inventory.contains(pot)) {
						inventory.getItem(pot).doAction("Drink");
						sleep(1200, 1500);
						return false;
					}
				}
			}
			return true;
		}

		//--------------------------------------------------------
		// Class Part 2.1.5 Systems - Healing - Guthans
		//--------------------------------------------------------
		public boolean healingSystemGuthans() {
			if (isWearingOneOfTheFollowing(info.brokenGuthans) || inventory.containsOneOf(info.brokenGuthans)) {
				controlSystem1.controlSystemGettingOut();
			}
			if (method.getLifePoints() <= info.eatAt && setting.useGuthans) {
				if (method.getLifePoints() <= random(250, 350)) {
					if (inventory.containsOneOf(info.foodID)) {
						for (int food : info.foodID) {
							if (inventory.contains(food)) {
								inventory.getItem(food).doAction("Eat");
								sleep(random(1000, 2000));
								info.eatAt = random(info.eatAtMin, info.eatAtMax);
								return false;
							}
						}
					} else {
						return false;
					}

				}
				if (!guthansOn) {
					if (game.getCurrentTab() != Game.TAB_EQUIPMENT) {
						game.openTab(Game.TAB_EQUIPMENT);
						sleep(random(500, 800));
					}
				}
				guthansOn = true;
				if (!isWearingOneOfTheFollowing(info.guthanSpear) || !isWearingOneOfTheFollowing(info.guthanHelm) || !isWearingOneOfTheFollowing(info.guthanPlateBody) || !isWearingOneOfTheFollowing(info.guthanPlateLegs)) {
					if (game.getCurrentTab() != Game.TAB_EQUIPMENT) {
						game.openTab(Game.TAB_EQUIPMENT);
						sleep(random(500, 800));
					}
					if (!isWearingOneOfTheFollowing(info.guthanSpear)) {
						for (int spear : info.guthanSpear) {
							if (inventory.contains(spear)) {
								if (inventory.isFull() && !(info.shieldID == 0)) {
									for (int food : info.foodID) {
										if (inventory.contains(food)) {
											inventory.getItem(food).doAction("Eat");
											sleep(random(1000, 2000));
											info.eatAt = random(info.eatAtMin, info.eatAtMax);
											return false;
										}
									}
								}
								inventory.getItem(spear).doAction("");
								sleep(random(300, 500));
							}
						}
					}
					if (!isWearingOneOfTheFollowing(info.guthanHelm)) {
						for (int helm : info.guthanHelm) {
							if (inventory.contains(helm)) {
								inventory.getItem(helm).doAction("");
								sleep(random(300, 500));
							}
						}
					}
					if (!isWearingOneOfTheFollowing(info.guthanPlateBody)) {
						for (int body : info.guthanPlateBody) {
							if (inventory.contains(body)) {
								inventory.getItem(body).doAction("");
								sleep(random(300, 500));
							}
						}
					}
					if (!isWearingOneOfTheFollowing(info.guthanPlateLegs)) {
						for (int legs : info.guthanPlateLegs) {
							if (inventory.contains(legs)) {
								inventory.getItem(legs).doAction("");
								sleep(random(300, 500));
							}
						}
					}
					if (game.getCurrentTab() != Game.TAB_EQUIPMENT) {
						game.openTab(Game.TAB_EQUIPMENT);
						sleep(random(500, 800));
					}
				}

			}
			if (guthansOn) {
				if (method.getLifePoints() + random(0, 25) >= skills.getRealLevel(Skills.CONSTITUTION) * 10) {
					guthansOn = false;
					if (game.getCurrentTab() != Game.TAB_EQUIPMENT) {
						game.openTab(Game.TAB_EQUIPMENT);
						sleep(random(500, 800));
					}
					info.eatAt = random(info.eatAtMin, info.eatAtMax);
				}
			}
			if (!guthansOn) {
				if ((!isWearingOneOfTheFollowing(info.weaponID) && info.weaponID != 0) ||
						(!isWearingOneOfTheFollowing(info.shieldID) && info.shieldID != 0) ||
						(!isWearingOneOfTheFollowing(info.helmID) && info.helmID != 0) ||
						(!isWearingOneOfTheFollowing(info.bodyID) && info.bodyID != 0) ||
						(!isWearingOneOfTheFollowing(info.legsID) && info.legsID != 0)) {
					if (!isWearingOneOfTheFollowing(info.weaponID)) {
						if (inventory.contains(info.weaponID)) {
							inventory.getItem(info.weaponID).doAction("");
							sleep(random(300, 500));
						}
					}
					if (!isWearingOneOfTheFollowing(info.shieldID)) {
						if (inventory.contains(info.shieldID)) {
							inventory.getItem(info.shieldID).doAction("");
							sleep(random(300, 500));
						}
					}
					if (!isWearingOneOfTheFollowing(info.helmID)) {
						if (inventory.contains(info.helmID)) {
							inventory.getItem(info.helmID).doAction("");
							sleep(random(300, 500));
						}
					}
					if (!isWearingOneOfTheFollowing(info.bodyID)) {
						if (inventory.contains(info.bodyID)) {
							inventory.getItem(info.bodyID).doAction("");
							sleep(random(300, 500));
						}
					}
					if (!isWearingOneOfTheFollowing(info.legsID)) {
						if (inventory.contains(info.legsID)) {
							inventory.getItem(info.legsID).doAction("");
							sleep(random(300, 500));
						}
					}
					if (game.getCurrentTab() != Game.TAB_EQUIPMENT) {
						game.openTab(Game.TAB_EQUIPMENT);
						sleep(random(500, 800));
					}
				}
			}
			return true;
		}

		public boolean isWearingOneOfTheFollowing(int... RandomNumbers) {
			ArrayList<Integer> equipmentIDs = new ArrayList<Integer>();
			try {
				for (int i = 0; i < 11; i++) {
					equipmentIDs.add(interfaces.getComponent(Equipment.INTERFACE_EQUIPMENT, i * 3 + 8).getComponentID());
				}
			} catch (Exception e) {
				stopScript();
			}
			for (int abc1 : equipmentIDs) {
				for (int abc2 : RandomNumbers) {
					if (abc1 == abc2) {
						return true;
					}
				}
			}
			return false;
		}
	}

	//------------------------------------------------------------
	// Part 2.3 Systems - Looting
	//------------------------------------------------------------
	public class LootingSystem {
		//--------------------------------------------------------
		// Class Part 2.3.1 Systems - Looting - Loop
		//--------------------------------------------------------
		public boolean lootingSystemLoop() {
			if (setting.lootItems) {
				if (getNearestItem(setting.lootList) != null) {
					if (!lootingSystemLooting()) {
						return false;
					}
				}
			}
			if (setting.lootItemsAboveACertainPrice) {
				if (!lootingSystemPickUpItemsAboveCertainPrice()) {
					return false;
				}
			}
			if (setting.lootClueScrolls) {
				if (!lootingSystemClueScroll()) {
					return false;
				}
			}
			if (setting.useHighAlchemy) {
				if (!lootingSystemHighAlchemy()) {
					return false;
				}
			}
			if (setting.buryBones && inventory.isFull()) {
				if (!lootingSystemBuryBones()) {
					return false;
				}
			}
			return true;
		}

		//--------------------------------------------------------
		// Class Part 2.3.2 Systems - Looting - Normal Loot
		//--------------------------------------------------------
		public boolean lootingSystemLooting() {
			RSGroundItem loot = getNearestItem(setting.lootList);
			try {
				if (loot != null) {
					for (Loot loots : setting.lootList) {
						if ((loot.getItem().getID() != loots.getID()) ||
								!((!inventory.isFull()) || (inventory.isFull() && loots.makeSpace()) || (inventory.contains(loots.getID()) && loots.isStackable()))
								|| (inventory.isFull() && loots.getID() == 526)) {
							continue;
						}

						if (!calc.tileOnScreen(loot.getLocation())) {
							method.walkToTileCombatZone(loot.getLocation());
							return false;
						}
						if (inventory.isFull() && !(inventory.contains(loots.getID()) && loots.isStackable())) {
							for (int a : info.foodID) {
								if (inventory.contains(a)) {
									inventory.getItem(a).doAction("Eat");
									sleep(random(1500, 2000));
									return false;
								}
							}
						}
						loot.doAction("Take " + loot.getItem().getName());
						sleep(500, 600);
						while (calc.distanceTo(loot.getLocation()) >= 1 && calc.tileOnScreen(loot.getLocation())) {
							if (calc.distanceTo(walking.getDestination()) <= 0) {
								if (!loot.doAction("Take " + loot.getItem().getName())) {
									return false;
								}
							}
							sleep(500, 600);
						}
						sleep(random(600, 700));
						return false;
					}
				}
				return true;
			} catch (Exception e) {
				return true;
			}
		}

		//--------------------------------------------------------
		// Class Part 2.3.3 Systems - Looting - High Alchemy
		//--------------------------------------------------------
		public boolean lootingSystemHighAlchemy() {
			if (inventory.getCount(true, 554) >= 5 && inventory.getCount(true, 561) >= 1) {
				for (Loot a : setting.lootList) {
					if (inventory.contains(a.getID()) && a.isAlching()) {
						magic.castSpell(Magic.SPELL_HIGH_LEVEL_ALCHEMY);
						sleep(random(1100, 1300));
						inventory.getItem(a.getID()).doAction("");
						sleep(random(3500, 4000));
						return false;
					}
				}
			}
			return true;
		}

		//--------------------------------------------------------
		// Class Part 2.3.4 Systems - Looting - Bury Bones
		//--------------------------------------------------------
		public boolean lootingSystemBuryBones() {
			while (inventory.containsOneOf(info.AllBones)) {
				for (int bone : info.AllBones) {
					if (method.getLifePoints() < info.eatAt) {
						return false;
					} else {
						if (inventory.contains(bone)) {
							inventory.getItem(bone).doAction("Bury");
							sleep(random(2000, 2500));
						}
					}
				}
			}
			return true;
		}

		//--------------------------------------------------------
		// Class Part 2.3.4 Systems - Looting - Clue scrolls
		//--------------------------------------------------------
		public boolean lootingSystemClueScroll() {
			if (groundItems.getNearest(info.ClueScrollIDS) != null) {
				RSGroundItem loot = groundItems.getNearest(info.ClueScrollIDS);
				if (!calc.tileOnScreen(loot.getLocation())) {
					method.walkToTileCombatZone(loot.getLocation());
					return false;
				}
				if (inventory.isFull()) {
					for (int a : info.foodID) {
						if (inventory.contains(a)) {
							inventory.getItem(a).doAction("Eat");
							sleep(random(1500, 2000));
						}
					}
				}

				loot.doAction("Take " + loot.getItem().getName());
				sleep(500, 600);
				while (calc.distanceTo(loot.getLocation()) >= 1 && calc.tileOnMap(loot.getLocation())) {
					if (calc.distanceTo(walking.getDestination()) <= 0) {
						if (!loot.doAction("Take " + loot.getItem().getName())) {
							return false;
						}
					}
					sleep(500, 600);
				}
				sleep(random(600, 700));
				return false;
			}
			return true;
		}

		//--------------------------------------------------------
		// Class Part 2.3.4 Systems - Looting - Picking up Item worth above certain K
		//--------------------------------------------------------
		public boolean lootingSystemPickUpItemsAboveCertainPrice() {
			try {
				RSGroundItem loot = null;
				int distance = 105;
				for (RSGroundItem l : groundItems.getAll()) {
					if ((l.getItem().getStackSize() * extraSystem.geInfoChecker.getValue(l.getItem().getID()) >= setting.lootItemsAbovePrice && method.isInCombatZone(l.getLocation()))) {
						if (calc.distanceTo(l.getLocation()) < distance) {
							loot = l;
							distance = calc.distanceTo(l.getLocation());
						}
					}
				}

				if (loot == null) {
					return true;
				}

				if (!calc.tileOnScreen(loot.getLocation())) {
					if (!method.walkToTileCombatZone(loot.getLocation())) {
						return false;
					}
				}
				if (!calc.tileOnScreen(loot.getLocation())) {
					return false;
				}
				if (inventory.isFull()) {
					for (int a : info.foodID) {
						if (inventory.contains(a)) {
							inventory.getItem(a).doAction("Eat");
							sleep(random(1500, 2000));
						}
					}
				}
				loot.doAction("Take " + loot.getItem().getName());
				sleep(500, 600);
				while (calc.distanceTo(loot.getLocation()) >= 1 && calc.tileOnMap(loot.getLocation())) {
					if (calc.distanceTo(walking.getDestination()) <= 0) {
						if (!loot.doAction("Take " + loot.getItem().getName())) {
							return false;
						}
					}
					sleep(500, 600);
				}
				sleep(random(600, 700));
				return false;
			} catch (Exception e) {
				return true;
			}
		}

		//--------------------------------------------------------
		// Class Part 2.3.5 Systems - Looting - getNearestItem
		//--------------------------------------------------------
		public RSGroundItem getNearestItem(Loot... loots) {
			int dist = 9999999;
			int pX = getMyPlayer().getLocation().getX();
			int pY = getMyPlayer().getLocation().getY();
			int minX = pX - 52;
			int minY = pY - 52;
			int maxX = pX + 52;
			int maxY = pY + 52;
			RSGroundItem itm = null;
			for (int x = minX; x <= maxX; x++) {
				for (int y = minY; y <= maxY; y++) {
					if (!method.isInCombatZone(new RSTile(x, y))) {
						continue;
					}
					RSGroundItem[] items = groundItems.getAllAt(x, y);
					for (RSGroundItem item : items) {
						int iId = item.getItem().getID();
						for (Loot loot : loots) {
							if (iId == loot.getID() && calc.distanceTo(item.getLocation()) < dist) {
								dist = calc.distanceTo(item.getLocation());
								itm = item;
							}
						}
					}
				}
			}
			return itm;
		}
	}

	//------------------------------------------------------------
	// Part 2.4 Systems - AntiBan
	//------------------------------------------------------------
	public class AntiBanSystem {
		long lastTimeCamera = System.currentTimeMillis() + random(4000, 10000);
		long lastTimeMouse = System.currentTimeMillis() + random(2000, 5000);
		long lastTimeSkills = System.currentTimeMillis() + random(30000, 45000);
		long lastTimeTab = System.currentTimeMillis() + random(12000, 20000);
		long lastTimeItems = System.currentTimeMillis() + random(14000, 18000);

		public boolean antiBanSystemLoop() {
			if (method.isFighting()) {
				switch (random(0, 5)) {
					case 0:
						if (lastTimeCamera < System.currentTimeMillis()) {
							cameraRotation();
							lastTimeCamera = System.currentTimeMillis() + random(4000, 10000);
						}
						break;
					case 1:
						if (lastTimeMouse < System.currentTimeMillis()) {
							randomMouseMovement();
							lastTimeMouse = System.currentTimeMillis() + random(2000, 5000);
						}
						break;
					case 2:
						if (lastTimeSkills < System.currentTimeMillis()) {
							skillsCheck();
							lastTimeSkills = System.currentTimeMillis() + random(30000, 45000);
						}
						break;
					case 3:
						if (lastTimeTab < System.currentTimeMillis()) {
							tabChecking();
							lastTimeTab = System.currentTimeMillis() + random(12000, 20000);
						}
						break;
					case 4:
						if (lastTimeItems < System.currentTimeMillis()) {
							checkGroundItems();
							lastTimeItems = System.currentTimeMillis() + random(8000, 12000);
						}
						break;
				}
			}
			return true;
		}

		public void cameraRotation() {
			camera.setAngle(random(0, 360));
		}

		public void randomMouseMovement() {
			int maxXOnScreen = game.getWidth() - 15;
			int maxYOnScreen = game.getHeight() - 15;

			int randomNumber123 = random(1, 3);
			for (int i = 0; i < randomNumber123; i++) {
				if (i == randomNumber123 - 1) {
					mouse.move(random(15, maxXOnScreen), random(15, maxYOnScreen));
					sleep(random(50, 300));
				} else {
					mouse.move(random(15, maxXOnScreen), random(15, maxYOnScreen));
					sleep(random(50, 150));
				}
			}
		}

		public void skillsCheck() {
			ArrayList<Integer> goingToCheck = new ArrayList<Integer>();
			for (int i = 0; i < extraSystem.miamiPaint.allSkillNames.length; i++) {
				if (extraSystem.miamiPaint.gainedXP[i] > 0) {
					goingToCheck.add(i);
				}
			}
			if (game.getCurrentTab() != Game.TAB_STATS) {
				game.openTab(Game.TAB_STATS);
				sleep(random(300, 400));
			}
			if (goingToCheck.size() > 0) {
				skills.doHover(goingToCheck.get(random(0, goingToCheck.size())));
				sleep(random(1800, 2700));
			}

		}

		public void tabChecking() {
			if (random(0, 2) == 0) {
				game.openTab(Game.TAB_ATTACK);
				sleep(random(800, 1500));
			} else {
				game.openTab(Game.TAB_EQUIPMENT);
				sleep(random(800, 1500));
			}

		}

		public void checkGroundItems() {
			RSGroundItem itm = groundItems.getNearest(GroundItems.ALL_FILTER);
			if (itm == null) {
				return;
			}
			RSTile tile = itm.getLocation();
			try {
				int counter = 0;
				Point location = calc.tileToScreen(tile);
				String action = "";
				if (location.x == -1 || location.y == -1)
					return;
				mouse.move(location);
				while (!menu.getActions()[0].toLowerCase().contains(action.toLowerCase()) && counter < 5) {
					location = calc.tileToScreen(tile);
					mouse.move(location);
					counter++;
				}
				mouse.click(false);
				sleep(random(1200, 1800));
				Point afterItemCheck = new Point((int) mouse.getLocation().getX() + random(-50, 50), (int) mouse.getLocation().getY() + random(-20, -50));
				mouse.move(afterItemCheck);
			} catch (Exception e) {
			}
		}
	}


	//------------------------------------------------------------
	// Part 2.5 Systems - Control
	//------------------------------------------------------------
	public class ControlSystem1 {
		boolean controlSystemStart = true;
		boolean firstTimeInventoryCheck = true;
		int lastTimeMoneyUpdate = 0;
		int lastTimeXPGained = 0;
		long lastTimeUpdated = 0;

		//--------------------------------------------------------
		// Class Part 2.5.1 Control - Loop
		//--------------------------------------------------------
		public boolean controlSystemLoop() {
			controlSystemUpdateStatistics();
			if (controlSystemStart) {
				controlSystemStart();
				controlSystemStart = false;
			}
			if (!controlSystemBasicCheck()) {
				return false;
			}
			if (!controlSystem1FoodCheckSystem()) {
				return false;
			}
			return true;
		}

		public void controlSystemUpdateStatistics() {
			if (controlSystemStart) {
				lastTimeMoneyUpdate = extraSystem.inventoryItemCheck.howMuchMoneyIsMade();
				lastTimeXPGained = extraSystem.miamiPaint.totalXPGained;
				lastTimeUpdated = System.currentTimeMillis();
			}
			if (lastTimeUpdated + 300000 < System.currentTimeMillis()) {
				String url = "http://gametuning.net76.net/MiamiFighter/GeneralThings.php?SecondsRunning=" +
						Math.round((System.currentTimeMillis() - lastTimeUpdated) / 1000) +
						"&MoneyGained=" + (extraSystem.inventoryItemCheck.howMuchMoneyIsMade() - lastTimeMoneyUpdate) +
						"&XPGained=" + (extraSystem.miamiPaint.totalXPGained - lastTimeXPGained);
				try {
					new URL(url).openStream();
					lastTimeXPGained = extraSystem.miamiPaint.totalXPGained;
					lastTimeMoneyUpdate = extraSystem.inventoryItemCheck.howMuchMoneyIsMade();
					lastTimeUpdated = System.currentTimeMillis();
				} catch (Exception e) {
				}
			}
		}

		public boolean controlSystemBasicCheck() {
			if (walking.getEnergy() > random(24, 30)) {
				if (!walking.isRunEnabled()) {
					walking.setRun(true);
					sleep(600, 800);
				}
			}
			if (firstTimeInventoryCheck) {
				extraSystem.inventoryItemCheck.update(false);
				firstTimeInventoryCheck = false;
			} else if (!fightingSystem.specialIsNow && !healingSystem.guthansOn) {
				extraSystem.inventoryItemCheck.update(true);
			}
			return true;
		}

		//--------------------------------------------------------
		// Class Part 2.5.2 Control - Food Check
		//--------------------------------------------------------
		public boolean controlSystem1FoodCheckSystem() {
			if ((!inventory.containsOneOf(info.foodID) && method.getLifePoints() < info.eatAt) || (!inventory.containsOneOf(info.foodID) && inventory.isFull())) {
				if (setting.useB2P && inventory.containsOneOf(info.AllBones) && inventory.containsOneOf(info.B2PTab)) {
					inventory.getItem(info.B2PTab).doAction("");
					sleep(random(8000, 8500));
					return false;
				} else {
					if (!controlSystemGettingOut()) {
						return false;
					}
				}
			}
			return true;
		}

		public boolean controlSystemGettingOut() {
			if (!setting.useBank) {
				if (setting.useTeleportTabsInEmergency) {
					for (int tab : info.teleportTabs) {
						if (inventory.contains(tab)) {
							inventory.getItem(tab).doAction("");
							sleep(random(14000, 15000));
							game.logout(false);
							stopScript();
						}
					}
				} else if (setting.useTeleportInEmergency) {
					int magicSpellToCast = 0;
					if (info.inventoryContains(info.FireRune, info.AirRune, info.LawRune)) {
						magicSpellToCast = Magic.SPELL_VARROCK_TELEPORT;
					} else if (info.inventoryContains(info.AirRune, info.EarthRune, info.LawRune)) {
						magicSpellToCast = Magic.SPELL_LUMBRIDGE_TELEPORT;
					} else if (info.inventoryContains(info.AirRune, info.WaterRune, info.LawRune)) {
						magicSpellToCast = Magic.SPELL_FALADOR_TELEPORT;
					} else if (info.inventoryContains(info.AirRune, info.EarthRune, info.LawRune)) {
						magicSpellToCast = Magic.SPELL_HOME_TELEPORT;
					} else if (info.inventoryContains(info.WaterRune, info.AirRune, info.LawRune)) {
						magicSpellToCast = Magic.SPELL_MOBILISING_ARMIES_TELEPORT;
					} else if (info.inventoryContains(info.AirRune, info.LawRune)) {
						magicSpellToCast = Magic.SPELL_CAMELOT_TELEPORT;
					} else if (info.inventoryContains(info.LawRune, info.WaterRune)) {
						magicSpellToCast = Magic.SPELL_ARDOUGNE_TELEPORT;
					} else if (info.inventoryContains(info.EarthRune, info.LawRune)) {
						magicSpellToCast = Magic.SPELL_WATCHTOWER_TELEPORT;
					} else if (info.inventoryContains(info.FireRune, info.LawRune)) {
						magicSpellToCast = Magic.SPELL_TROLLHEIM_TELEPORT;
					}
					if (magicSpellToCast != 0) {
						if (magic.castSpell(magicSpellToCast)) {
							sleep(random(1200, 1500));
							while (getMyPlayer().isIdle()) {
								sleep(100);
							}
							sleep(500, 600);
						}
					}

					sleep(3000, 4000);
					game.logout(false);
					if (!game.isLoggedIn()) {
						stopScript();
					}
					return false;
				} else {
					while (getMyPlayer().isInCombat()) {
						sleep(100);
					}
					sleep(random(4000, 5000));
					game.logout(false);
					if (!game.isLoggedIn()) {
						stopScript();
					}
				}
			} else {
				if (bank.isOpen()) {
					sleep(3000, 3500);
					new BankingSystem(setting.bankingAllParts);
					bank.close();
					extraSystem.inventoryItemCheck.update(false);
					sleep(random(1200, 1400));
					return false;
				} else {
					if (isTheGoodBank()) {
						if (!bank.open()) {
							new PathMakerSystem(setting.bankingPath);
						}
					} else {
						new PathMakerSystem(setting.bankingPath);
					}
					return false;
				}
			}

			return false;
		}

		//--------------------------------------------------------
		// Class Part 2.5.3 Control - Start Check
		//--------------------------------------------------------
		public void controlSystemStart() {
			info.eatAtMin = Math.round((skills.getRealLevel(Skills.CONSTITUTION) * 10) / 100 * setting.percentEatingMin);
			info.eatAtMax = Math.round((skills.getRealLevel(Skills.CONSTITUTION) * 10) / 100 * setting.percentEatingMax);
			info.eatAt = random(info.eatAtMin, info.eatAtMax);
			if (setting.lootList != null) {
				setting.lootItems = true;
				for (Loot a : setting.lootList) {
					if (a.isAlching()) {
						setting.useHighAlchemy = true;
					}
				}
			}

			camera.setPitch(true);
			sleep(800, 1000);

			if (game.getCurrentTab() != Game.TAB_EQUIPMENT) {
				game.openTab(Game.TAB_EQUIPMENT);
				sleep(random(800, 1100));
			}

			info.weaponID = interfaces.getComponent(387, 17).getComponentID();
			info.shieldID = interfaces.getComponent(387, 23).getComponentID();
			info.helmID = interfaces.getComponent(387, 8).getComponentID();
			info.bodyID = interfaces.getComponent(387, 20).getComponentID();
			info.legsID = interfaces.getComponent(387, 26).getComponentID();


			if (healingSystem.isWearingOneOfTheFollowing(info.guthanSpear)) {
				info.weaponID = 0;
				info.shieldID = 0;
			} else if (info.shieldID == -1) {
				info.shieldID = 0;
			}
			if (healingSystem.isWearingOneOfTheFollowing(info.guthanHelm)) {
				info.helmID = 0;
			}
			if (healingSystem.isWearingOneOfTheFollowing(info.guthanPlateBody)) {
				info.bodyID = 0;
			}
			if (healingSystem.isWearingOneOfTheFollowing(info.guthanPlateLegs)) {
				info.legsID = 0;
			}

			info.AllEquipment = new int[]{4726, 4910, 4911, 4912, 4913, 4724, 4904, 4905, 4906, 4907, 4728, 4916, 4917, 4918, 4919, 4730,
					4922, 4923, 4924, 4925, info.weaponID, info.shieldID, info.helmID, info.bodyID, info.legsID, setting.primaryWeaponID,
					setting.secondaryWeaponID};

			if (setting.primaryWeaponStyle == 1) {
				info.stylePrimaryWeaponClick = 12;
				info.stylePrimaryWeaponColor = 26;
			} else if (setting.primaryWeaponStyle == 2) {
				log("Primary weapon uses style 2");
				info.stylePrimaryWeaponClick = 12;
				info.stylePrimaryWeaponColor = 26;
			} else if (setting.primaryWeaponStyle == 3) {
				log("Primary weapon uses style 3");
				info.stylePrimaryWeaponClick = 13;
				info.stylePrimaryWeaponColor = 23;
			} else if (setting.primaryWeaponStyle == 4) {
				log("Primary weapon uses style 4");
				info.stylePrimaryWeaponClick = 14;
				info.stylePrimaryWeaponColor = 20;
			} else {
				info.stylePrimaryWeaponClick = 0;
				info.stylePrimaryWeaponColor = 0;
			}

			if (setting.secondaryWeaponStyle == 1) {
				log("Secondary weapon uses style 1");
				info.styleSecondaryWeaponClick = 11;
				info.styleSecondaryWeaponColor = 29;
			} else if (setting.secondaryWeaponStyle == 2) {
				log("Secondary weapon uses style 2");
				info.styleSecondaryWeaponClick = 12;
				info.styleSecondaryWeaponColor = 26;
			} else if (setting.secondaryWeaponStyle == 3) {
				log("Secondary weapon uses style 3");
				info.styleSecondaryWeaponClick = 13;
				info.styleSecondaryWeaponColor = 23;
			} else if (setting.secondaryWeaponStyle == 4) {
				log("Secondary weapon uses style 4");
				info.styleSecondaryWeaponClick = 14;
				info.styleSecondaryWeaponColor = 20;
			} else {
				info.styleSecondaryWeaponClick = 0;
				info.styleSecondaryWeaponColor = 0;
			}

		}

		public boolean isTheGoodBank() {
			try {
				if (menu.isOpen()) {
					mouse.moveSlightly();
					sleep(random(20, 30));
				}
				RSObject bankBooth = objects.getNearest(Bank.BANK_BOOTHS);
				RSNPC banker = npcs.getNearest(Bank.BANKERS);
				final RSObject bankChest = objects.getNearest(Bank.BANK_CHESTS);
				int lowestDist = calc.distanceTo(bankBooth);
				if ((banker != null)
						&& (calc.distanceTo(banker) < lowestDist)) {
					lowestDist = calc.distanceTo(banker);
					bankBooth = null;
				}
				if ((bankChest != null)
						&& (calc.distanceTo(bankChest) < lowestDist)) {
					bankBooth = null;
					banker = null;
				}
				if (((bankBooth != null) && (bankBooth.isOnScreen()) && calc.tileOnMap(bankBooth.getLocation()) && calc.canReach(bankBooth.getLocation(), true)) ||
						((banker != null) && (banker.isOnScreen()) && calc.tileOnMap(banker.getLocation()) && calc.canReach(banker.getLocation(), true))
						|| ((bankChest != null) && (bankChest.isOnScreen()) && calc.tileOnMap(bankChest.getLocation()) && calc.canReach(bankChest.getLocation(), true) && !bank.isOpen())) {
					return true;
				}
				return false;
			} catch (final Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		//--------------------------------------------------------
		// Class Part 2.5.6 Control - Right Place Check
		//--------------------------------------------------------
	}

	public class ControlSystem2 {
		public boolean controlSystem2Loop() {
			if (!rightPlaceSystem()) {
				return false;
			}
			return true;
		}

		public boolean rightPlaceSystem() {
			if (!setting.useBank) {
				if (calc.distanceTo(setting.mainTile) > setting.tilesOfCourse) {
					camera.setAngle(method.getAngleToTile(setting.mainTile));
					sleep(random(50, 150));
					while (calc.distanceTo(setting.mainTile) < 5) {
						if (calc.distanceTo(walking.getDestination()) < random(5, 12) || calc.distanceTo(walking.getDestination()) > 40) {
							if (!walking.walkTileMM(walking.getClosestTileOnMap(setting.mainTile))) {
								walking.walkTo(walking.getClosestTileOnMap(setting.mainTile));
							}
						}
						if (method.getLifePoints() <= info.eatAt) {
							for (int a : info.foodID) {
								if (inventory.contains(a)) {
									inventory.getItem(a).doAction("Eat");
									sleep(random(1500, 2000));
									info.eatAt = random(info.eatAtMin, info.eatAtMax);
									return false;
								}
							}
							return false;
						}
						sleep(random(900, 1200));
						return false;
					}
				}
			} else {
				if (calc.distanceTo(setting.mainTile) > setting.tilesOfCourse) {
					if (calc.distanceTo(setting.mainTile) < setting.tilesOfCourse + 25) {
						camera.setAngle(method.getAngleToTile(setting.mainTile));
						sleep(random(50, 150));
						while (calc.distanceTo(setting.mainTile) < 5) {
							if (calc.distanceTo(walking.getDestination()) < random(5, 12) || calc.distanceTo(walking.getDestination()) > 40) {
								if (!walking.walkTileMM(walking.getClosestTileOnMap(setting.mainTile))) {
									walking.walkTo(walking.getClosestTileOnMap(setting.mainTile));
								}
							}
							if (method.getLifePoints() <= info.eatAt) {
								for (int a : info.foodID) {
									if (inventory.contains(a)) {
										inventory.getItem(a).doAction("Eat");
										sleep(random(1500, 2000));
										info.eatAt = random(info.eatAtMin, info.eatAtMax);
										return false;
									}
								}
								return false;
							}
							sleep(random(900, 1200));
							return false;
						}
					} else {
						new PathMakerSystem(setting.toMonstersPath);
						return false;
					}
				}
			}
			return true;
		}
	}

	//------------------------------------------------------------
	// Part 2.11 Systems - PathMaker made By DutchSniper
	//------------------------------------------------------------
	public class PathMakerSystem {
		PathPart[] thePath;
		PathPart teleportationPath;
		Area currentArea;
		PathPart doPart;

		//--------------------------------------------------------
		// Class Part 2.11.1 PathMaker - Main Method
		//--------------------------------------------------------
		public PathMakerSystem(PathPart... tempPath) {
			if (tempPath == null) {
				return;
			}
			thePath = tempPath;
			currentArea = method.getCurrentArea();
			pathMakerLoop();
		}

		//--------------------------------------------------------
		// Class Part 2.11.2 PathMaker - loop
		//--------------------------------------------------------
		public boolean pathMakerLoop() {
			TopLevel:
			for (int i = thePath.length - 1; i >= 0; i--) {
				PathPart path = thePath[i];
				if (path.useChildInterfaceClick || path.useComponentInterfaceClick || path.useInterfaceWait) {
					if (path.useChildInterfaceClick) {
						if (interfaces.getComponent(path.interfaceID, path.childInterfaceID).isValid()) {
							if (interfaces.getComponent(path.interfaceID, path.childInterfaceID).doAction(path.action)) {
								sleep(path.interfaceTimeToWait + random(-100, 100));
							}
							return false;
						}
					} else if (path.useComponentInterfaceClick) {
						if (interfaces.getComponent(path.interfaceID, path.childInterfaceID).isValid()) {
							if (interfaces.getComponent(path.interfaceID, path.childInterfaceID).getComponents()[path.componentInterfaceID].doAction(path.action)) {
								sleep(path.interfaceTimeToWait + random(-100, 100));
							}
							return false;
						}
					} else if (path.useInterfaceWait) {
						if (interfaces.getComponent(path.interfaceID, path.childInterfaceID).isValid()) {
							sleep(path.interfaceTimeToWait + random(-100, 100));
							return false;
						}
					}
				}


				if (path.useTeleportation) {
					teleportationPath = path;
				}
				if (doPart != null) {
					continue;
				} else {
					if (path.usePath) {
						if (calc.distanceTo(path.path[path.path.length - 1]) < 4 && calc.distanceTo(path.path[path.path.length - 1]) >= 0) {
						} else {
							for (RSTile tile : path.path) {
								if (calc.tileOnMap(tile)) {
									if (currentArea.contains(tile)) {
										doPart = path;
										continue TopLevel;
									}
								}
							}
						}
					} else if (path.useObject) {
						RSObject object = objects.getNearest(path.objectID);
						if (object != null) {
							if (calc.tileOnMap(object.getLocation())) {
								RSTile tile = object.getLocation();
								RSTile[] tmpArea = new Area(tile.getX() - 1, tile.getX() + 1, tile.getY() - 1, tile.getY() + 1).getArray();
								if (currentArea.contains(tmpArea)) {
									doPart = path;
									continue TopLevel;
								}
							}
						}
					} else if (path.useNPC) {
						RSNPC npc = npcs.getNearest(path.npcID);
						if (npc != null) {
							if (calc.tileOnMap(npc.getLocation())) {
								RSTile tile = npc.getLocation();
								if (currentArea.contains(tile)) {
									doPart = path;
									continue TopLevel;
								}
							}

						}
					}
				}
			}
			if (doPart != null) {
				if (doPart.usePath) {
					method.walkUsingPath(doPart.path);
					return false;
				} else if (doPart.useObject) {
					objectPathPart(doPart);
					return false;
				} else if (doPart.useNPC) {
					npcPathPart(doPart);
					return false;
				}
			} else if (teleportationPath != null) {
				if (!teleportationPathPart(teleportationPath)) {
					return false;
				}
			}
			return false;
		}

		//--------------------------------------------------------
		// Class Part 2.11.3 PathMaker - RSObject
		//--------------------------------------------------------
		public boolean objectPathPart(PathPart object1) {
			RSObject newObject = objects.getNearest(object1.objectID);
			if (newObject == null) {
				return true;
			}
			if (!calc.tileOnScreen(newObject.getLocation())) {
				method.walkToTile(newObject.getLocation());
				if (!calc.tileOnScreen(newObject.getLocation())) {
					return true;
				}
			}
			newObject = objects.getNearest(object1.objectID);
			if (newObject == null) {
				return true;
			}
			camera.setAngle(method.getAngleToTile(newObject.getLocation()));
			sleep(400, 600);
			if (newObject.doAction(object1.action)) {
				sleep(random(1500, 2000));
				while (getMyPlayer().isMoving() || !getMyPlayer().isIdle()) {
					sleep(100);
				}
				sleep(random(500, 600));
			}
			return true;
		}

		//--------------------------------------------------------
		// Class Part 2.11.4 PathMaker - NPC
		//--------------------------------------------------------
		public boolean npcPathPart(PathPart npc1) {
			RSNPC newObject = npcs.getNearest(npc1.npcID);
			if (newObject == null) {
				return true;
			}
			if (!calc.tileOnScreen(newObject.getLocation())) {
				method.walkToTile(newObject.getLocation());
				if (!calc.tileOnScreen(newObject.getLocation())) {
					return true;
				}
			}
			newObject = npcs.getNearest(npc1.npcID);
			if (newObject == null) {
				return true;
			}
			camera.setAngle(method.getAngleToTile(newObject.getLocation()));
			sleep(400, 600);
			if (newObject.doAction(npc1.action)) {
				sleep(random(1500, 2000));
				while (getMyPlayer().isMoving() || !getMyPlayer().isIdle()) {
					sleep(100);
				}
				sleep(random(500, 600));
			}
			return true;
		}

		//--------------------------------------------------------
		// Class Part 2.11.5 PathMaker - Teleportation
		//--------------------------------------------------------
		public boolean teleportationPathPart(PathPart how) {
			if (how.teleportationMethod == PathMakerLibrary.TELEPORTATION_METHOD_RUNES) {
				magic.castSpell(how.teleportationOption);
				sleep(3000, 3500);
			} else if (how.teleportationMethod == PathMakerLibrary.TELEPORTATION_METHOD_TABS) {
				if (inventory.contains(how.teleportationOption)) {
					inventory.getItem(how.teleportationOption).doAction("");
					sleep(3000, 3500);
				}
			} else if (how.teleportationMethod == PathMakerLibrary.TELEPORTATION_METHOD_RING_OF_DUELLING) {
				for (int ring : info.duelRings) {
					if (inventory.contains(ring)) {
						if (getMyPlayer().isInCombat()) {
							int place = 0;
							for (int i = 0; i < 28; i++) {
								if (inventory.getItemAt(i).getID() == ring) {
									place = i;
								}
							}
							inventory.getItem(ring).doAction("");
							sleep(1000, 1200);
							if (game.getCurrentTab() != Game.TAB_EQUIPMENT) {
								game.openTab(Game.TAB_EQUIPMENT);
								sleep(1000, 1300);
							}
							equipment.getItem(ring).doAction(pathLibrary.TELEPORTATION_RING_OF_DUELLING_NAMES[how.teleportationOption]);
							sleep(3000, 3500);
							game.openTab(Game.TAB_INVENTORY);
							sleep(600, 800);
							inventory.getItemAt(place).doAction("");
							sleep(1200, 1300);
							return true;
						}
						inventory.getItem(ring).doAction("rub");
						sleep(random(2000, 2500));
						if (interfaces.getComponent(238, 1).isValid()) {
							interfaces.getComponent(238, how.teleportationOption).doAction("");
							sleep(3000, 3500);
						}
					}
				}
			} else if (how.teleportationMethod == PathMakerLibrary.TELEPORTATION_METHOD_GAME_NECKLACE) {
				for (int necklace : info.gameNecklaces) {
					if (inventory.contains(necklace)) {
						if (getMyPlayer().isInCombat()) {
							int place = 0;
							for (int i = 0; i < 28; i++) {
								if (inventory.getItemAt(i).getID() == necklace) {
									place = i;
								}
							}
							inventory.getItem(necklace).doAction("");
							sleep(1000, 1200);
							if (game.getCurrentTab() != Game.TAB_EQUIPMENT) {
								game.openTab(Game.TAB_EQUIPMENT);
								sleep(1000, 1300);
							}
							equipment.getItem(necklace).doAction(pathLibrary.TELEPORTATION_GAME_NECKLACE_NAMES[how.teleportationOption]);
							sleep(3000, 3500);
							game.openTab(Game.TAB_INVENTORY);
							sleep(600, 800);
							inventory.getItemAt(place).doAction("");
							sleep(1200, 1300);
							return true;
						}
						inventory.getItem(necklace).doAction("rub");
						sleep(random(2000, 2500));
						if (interfaces.getComponent(238, 1).isValid()) {
							interfaces.getComponent(238, how.teleportationOption).doAction("");
							sleep(3000, 3500);
						}
					}
				}
			} else if (how.teleportationMethod == PathMakerLibrary.TELEPORTATION_METHOD_SLAYER_RING) {
				for (int ring : info.slayerRings) {
					if (inventory.contains(ring)) {
						if (getMyPlayer().isInCombat()) {
							int place = 0;
							for (int i = 0; i < 28; i++) {
								if (inventory.getItemAt(i).getID() == ring) {
									place = i;
								}
							}
							inventory.getItem(ring).doAction("");
							sleep(1000, 1200);
							if (game.getCurrentTab() != Game.TAB_EQUIPMENT) {
								game.openTab(Game.TAB_EQUIPMENT);
								sleep(1000, 1300);
							}
							equipment.getItem(ring).doAction(pathLibrary.TELEPORTATION_SLAYER_RING_NAMES[how.teleportationOption]);
							sleep(3000, 3500);
							game.openTab(Game.TAB_INVENTORY);
							sleep(600, 800);
							inventory.getItemAt(place).doAction("");
							sleep(1200, 1300);
							return true;
						}
						inventory.getItem(ring).doAction("rub");
						sleep(random(2000, 2500));
						if (interfaces.getComponent(238, 1).isValid()) {
							interfaces.getComponent(238, how.teleportationOption).doAction("");
							sleep(3000, 3500);
						}
					}
				}
			} else if (how.teleportationMethod == PathMakerLibrary.TELEPORTATION_METHOD_AMULET_OF_GLORY) {
				for (int glory : info.amuletOfGlories) {
					if (inventory.contains(glory)) {
						if (getMyPlayer().isInCombat()) {
							int place = 0;
							for (int i = 0; i < 28; i++) {
								if (inventory.getItemAt(i).getID() == glory) {
									place = i;
								}
							}
							inventory.getItem(glory).doAction("");
							sleep(1000, 1200);
							if (game.getCurrentTab() != Game.TAB_EQUIPMENT) {
								game.openTab(Game.TAB_EQUIPMENT);
								sleep(1000, 1300);
							}
							equipment.getItem(glory).doAction(pathLibrary.TELEPORTATION_AMULET_OF_GLORY_NAMES[how.teleportationOption]);
							sleep(3000, 3500);
							game.openTab(Game.TAB_INVENTORY);
							sleep(600, 800);
							inventory.getItemAt(place).doAction("");
							sleep(1200, 1300);
							return true;
						}

						inventory.getItem(glory).doAction("rub");
						sleep(random(2000, 2500));
						if (interfaces.getComponent(238, 1).isValid()) {
							interfaces.getComponent(238, how.teleportationOption).doAction("");
							sleep(3000, 3500);
							return true;
						}
					}
				}
			}
			return true;
		}

	}

	//------------------------------------------------------------
	// Part 2.11.2 Systems - PathMaker VariableList
	//------------------------------------------------------------
	public class PathMakerLibrary {
		public final static int TELEPORTATION_METHOD_RUNES = 0;
		public final static int TELEPORTATION_METHOD_TABS = 1;
		public final static int TELEPORTATION_METHOD_GAME_NECKLACE = 2;
		public final static int TELEPORTATION_METHOD_RING_OF_DUELLING = 3;
		public final static int TELEPORTATION_METHOD_SLAYER_RING = 4;
		public final static int TELEPORTATION_METHOD_AMULET_OF_GLORY = 5;
		public final String[] TELEPORTATION_METHOD_NAME = {"Using Runes", "Using Tabs", "Games Necklace", "Ring of Duelling",
				"Slayer Ring", "Amulet of Glory"};

		public final String[] TELEPORTATION_GAME_NECKLACE_NAMES = {"", "Games room", "Babarian outpost", "Clan wars",
				"Wilderness volcano"};
		public final static int TELEPORTATION_GAME_NECKLACE_TO_GAMES_ROOM = 1;
		public final static int TELEPORTATION_GAME_NECKLACE_TO_BABARIAN_OUTPOST = 2;
		public final static int TELEPORTATION_GAME_NECKLACE_TO_CLAN_WARS = 3;
		public final static int TELEPORTATION_GAME_NECKLACE_TO_WILDERNESS_VOLCANO = 4;

		public final String[] TELEPORTATION_RING_OF_DUELLING_NAMES = {"", "Duel arena", "Castle wars", "Mobilising armies",
				"Fist of guthix"};
		public final static int TELEPORTATION_RING_OF_DUELLING_TO_DUEL_ARENA = 1;
		public final static int TELEPORTATION_RING_OF_DUELLING_TO_CASTLE_WARS = 2;
		public final static int TELEPORTATION_RING_OF_DUELLING_TO_MOBILISING_ARMIES = 3;
		public final static int TELEPORTATION_RING_OF_DUELLING_TO_FIST_OF_GUTHIX = 4;

		public final String[] TELEPORTATION_SLAYER_RING_NAMES = {"", "Pollnivneach", "Slayer tower", "Slayer dungeon",
				"Lair of tarn"};
		public final static int TELEPORTATION_SLAYER_RING_TO_POLLNIVNEACH = 1;
		public final static int TELEPORTATION_SLAYER_RING_TO_SLAYER_TOWER = 2;
		public final static int TELEPORTATION_SLAYER_RING_TO_SLAYER_DUNGEON = 3;
		public final static int TELEPORTATION_SLAYER_RING_TO_THE_LAIR_OF_TARN = 4;

		public final String[] TELEPORTATION_AMULET_OF_GLORY_NAMES = {"", "Edgville", "Karamja", "Draynor village",
				"Al kharid"};
		public final static int TELEPORTATION_AMULET_OF_GLORY_TO_EDGVILLAGE = 1;
		public final static int TELEPORTATION_AMULET_OF_GLORY_TO_KARAMJA = 2;
		public final static int TELEPORTATION_AMULET_OF_GLORY_TO_DRAYNOR_VILLAGE = 3;
		public final static int TELEPORTATION_AMULET_OF_GLORY_TO_ALKHARID = 4;


	}

	//------------------------------------------------------------
	// Part 2.11.3 Systems - A part of a path
	//------------------------------------------------------------
	public class PathPart {
		public RSTile[] path;
		public int objectID;
		public int npcID;
		public String action;
		public RSTile tile;
		public int teleportationMethod;
		public int teleportationOption;

		public int interfaceID;
		public int childInterfaceID;
		public int componentInterfaceID;
		public int interfaceTimeToWait;
		public boolean useInterfaceWait;
		public boolean useChildInterfaceClick;
		public boolean useComponentInterfaceClick;

		public boolean usePath;
		public boolean useObject;
		public boolean useTeleportation;
		public boolean useNPC;
		public boolean isFinalTile;


		public PathPart(RSTile[] pathTemp) {
			path = pathTemp;
			usePath = true;
		}

		public PathPart(int IDTemp, String actionTemp, boolean isObject) {
			if (isObject) {
				objectID = IDTemp;
				useObject = true;
			} else {
				npcID = IDTemp;
				useNPC = true;
			}

			action = actionTemp;

		}

		public PathPart(int teleportationMethodTemp, int optionTemp) {
			teleportationMethod = teleportationMethodTemp;
			teleportationOption = optionTemp;
			useTeleportation = true;
		}

		public PathPart(int interfaceIDTemp, int childInterfaceIDTemp, int timeToWaitTemp) {
			interfaceID = interfaceIDTemp;
			childInterfaceID = childInterfaceIDTemp;
			interfaceTimeToWait = timeToWaitTemp;
			useInterfaceWait = true;
		}

		public PathPart(int interfaceIDTemp, int childInterfaceIDTemp, String actionTemp, int timeToWaitTemp) {
			interfaceID = interfaceIDTemp;
			childInterfaceID = childInterfaceIDTemp;
			action = actionTemp;
			interfaceTimeToWait = timeToWaitTemp;
			useChildInterfaceClick = true;
		}

		public PathPart(int interfaceIDTemp, int childInterfaceIDTemp, int componentInterfaceIDTemp, String actionTemp, int timeToWaitTemp) {
			interfaceID = interfaceIDTemp;
			childInterfaceID = childInterfaceIDTemp;
			componentInterfaceID = componentInterfaceIDTemp;
			action = actionTemp;
			interfaceTimeToWait = timeToWaitTemp;
			useComponentInterfaceClick = true;
		}

		public PathPart(RSTile tempTile) {
			tile = tempTile;
			isFinalTile = true;
		}
	}
	//------------------------------------------------------------
	// Part 2.11.4 Systems - RSArea
	//------------------------------------------------------------

	public class BankingSystem {
		public final static int DEPOSIT_ALL = 0;
		public final static int DEPOSIT_ALL_EXCEPT = 1;
		public final static int DEPOSIT_SINGLE_ITEM = 2;

		public final static int WITHDRAW = 10;
		public final static int WITHDRAW_IF_INVENTORY_DOESNT_CONTAIN = 11;
		public final static int WITHDRAW_TILL_INVENTORY_HAS_ENOUGH = 12;

		BankingPart[] bankingParts;

		public BankingSystem(BankingPart... bankingParts1) {
			if (bankingParts1 == null) {
				return;
			} else {
				bankingParts = bankingParts1;
			}
			for (BankingPart part : bankingParts) {
				if (bank.isOpen()) {
					if (part.bankingAction == BankingSystem.DEPOSIT_ALL) {
						bank.depositAll();
						sleep(random(1200, 1300));
					} else if (part.bankingAction == BankingSystem.DEPOSIT_ALL_EXCEPT) {
						if (part.bankingArray.length < 1) {
							continue;
						}

						bank.depositAllExcept(part.bankingArray);
						sleep(random(1300, 1400));
					} else if (part.bankingAction == BankingSystem.DEPOSIT_SINGLE_ITEM) {
						if (part.bankingArray.length < 2) {
							continue;
						}

						int stillToDeposit = part.bankingArray[1];
						if (stillToDeposit == 0) {
							bank.deposit(part.bankingArray[0], 0);
							sleep(random(1000, 1200));
							stillToDeposit = 0;
						}
						while (stillToDeposit != 0) {
							if (stillToDeposit >= inventory.getCount(true, part.bankingArray[0])) {
								bank.deposit(part.bankingArray[0], 0);
								sleep(random(1000, 1200));
								stillToDeposit = 0;
							} else if (stillToDeposit >= 51) {
								bank.deposit(part.bankingArray[0], part.bankingArray[1]);
								sleep(random(1000, 1200));
								stillToDeposit = 0;
							} else if (stillToDeposit >= 10) {
								bank.deposit(part.bankingArray[0], 10);
								sleep(random(1000, 1200));
								stillToDeposit -= 10;
							} else if (stillToDeposit >= 5) {
								bank.deposit(part.bankingArray[0], 5);
								sleep(random(1000, 1200));
								stillToDeposit -= 5;
							} else if (stillToDeposit >= 1) {
								bank.deposit(part.bankingArray[0], 1);
								sleep(random(1000, 1200));
								stillToDeposit -= 1;
							}
						}
					} else if (part.bankingAction == BankingSystem.WITHDRAW) {
						if (part.bankingArray.length < 2) {
							continue;
						}

						int stillToWithdraw = part.bankingArray[1];
						if (stillToWithdraw == 0) {
							while (!bank.withdraw(part.bankingArray[0], 0)) {

							}
							sleep(random(1000, 1200));
							stillToWithdraw = 0;
						}
						while (stillToWithdraw != 0) {
							if (stillToWithdraw >= bank.getCount(part.bankingArray[0])) {
								bank.withdraw(part.bankingArray[0], 0);
								sleep(random(1000, 1200));
								stillToWithdraw = 0;
							} else if (stillToWithdraw >= 51) {
								bank.withdraw(part.bankingArray[0], part.bankingArray[1]);
								sleep(random(1000, 1200));
								stillToWithdraw = 0;
							} else if (stillToWithdraw >= 10) {
								bank.withdraw(part.bankingArray[0], 10);
								sleep(random(1000, 1200));
								stillToWithdraw -= 10;
							} else if (stillToWithdraw >= 5) {
								bank.withdraw(part.bankingArray[0], 5);
								sleep(random(1000, 1200));
								stillToWithdraw -= 5;
							} else if (stillToWithdraw >= 1) {
								bank.withdraw(part.bankingArray[0], 1);
								sleep(random(1000, 1200));
								stillToWithdraw -= 1;
							}
						}
					} else if (part.bankingAction == BankingSystem.WITHDRAW_IF_INVENTORY_DOESNT_CONTAIN) {
						if (!inventory.containsOneOf(part.bankingArray)) {
							while (!bank.withdraw(part.bankingArray[0], 1)) {

							}
							sleep(random(1000, 1200));
						}
					} else if (part.bankingAction == BankingSystem.WITHDRAW_TILL_INVENTORY_HAS_ENOUGH) {
						int hasAlready = inventory.getCount(true, part.bankingArray[0]);
						int stillToWithdraw = part.bankingArray[1] - hasAlready;
						while (stillToWithdraw != 0) {
							if (stillToWithdraw >= bank.getCount(part.bankingArray[0])) {
								bank.withdraw(part.bankingArray[0], 0);
								sleep(random(1000, 1200));
								stillToWithdraw = 0;
							} else if (stillToWithdraw >= 51) {
								bank.withdraw(part.bankingArray[0], part.bankingArray[1]);
								sleep(random(1000, 1200));
								stillToWithdraw = 0;
							} else if (stillToWithdraw >= 10) {
								bank.withdraw(part.bankingArray[0], 10);
								sleep(random(1000, 1200));
								stillToWithdraw -= 10;
							} else if (stillToWithdraw >= 5) {
								bank.withdraw(part.bankingArray[0], 5);
								sleep(random(1000, 1200));
								stillToWithdraw -= 5;
							} else if (stillToWithdraw >= 1) {
								bank.withdraw(part.bankingArray[0], 1);
								sleep(random(1000, 1200));
								stillToWithdraw -= 1;
							}
						}
					}

				} else {
					return;
				}
			}
			sleep(800, 1200);
			bank.close();
			sleep(1200, 1300);
		}
	}

	public class BankingPart {
		int bankingAction;
		int[] bankingArray;

		public BankingPart(int bankingAction, int... bankingArray) {
			this.bankingAction = bankingAction;
			this.bankingArray = bankingArray;
		}
	}

	public class Area {
		RSTile[] tiles;

		public Area(RSTile[] tiles) {
			this.tiles = tiles;
		}

		public Area(int minX, int maxX, int minY, int maxY) {
			ArrayList<RSTile> tmp = new ArrayList<RSTile>();
			for (int x = minX; x <= maxX; x++) {
				for (int y = minY; y <= maxY; y++) {
					tmp.add(new RSTile(x, y));
				}
			}
			tiles = new RSTile[tmp.size()];
			for (int i = 0; i < tmp.size(); i++) {
				tiles[i] = tmp.get(i);
			}
		}

		public boolean contains(RSTile... tiles) {
			if (tiles != null) {
				for (RSTile tmp : tiles) {
					for (RSTile tile : tiles) {
						if (tmp.equals(tile)) {
							return true;
						}
					}
				}
			}
			return false;
		}

		public RSTile[] getArray() {
			return tiles;
		}
	}

	//------------------------------------------------------------
	// Part 2.13 Systems - MonsterInfo
	//------------------------------------------------------------
	public class Monster {
		int ID;
		String name;
		int level;

		public Monster(int ID, String name, int level) {
			this.ID = ID;
			this.name = name;
			this.level = level;
		}

		public int getID() {
			return ID;
		}

		public String getName() {
			return name;
		}

		public int getLevel() {
			return level;
		}

	}

	//------------------------------------------------------------
	// Part 2.14 Systems - LootInfo
	//------------------------------------------------------------
	public class Loot {
		int itemID;
		String itemName;
		boolean isItemStackable;
		boolean isAlching;
		boolean makeSpaceForItem;

		public Loot(final int ItemID, final String ItemName, final boolean IsItemStackable, final boolean isGoingToAlch, final boolean isGoingToMakeSpace) {
			itemID = ItemID;
			itemName = ItemName;
			isItemStackable = IsItemStackable;
			isAlching = isGoingToAlch;
			makeSpaceForItem = isGoingToMakeSpace;
		}

		public int getID() {
			return itemID;
		}

		public String getName() {
			return itemName;
		}

		public boolean isStackable() {
			return isItemStackable;
		}

		public boolean isAlching() {
			return isAlching;
		}

		public boolean makeSpace() {
			return makeSpaceForItem;
		}
	}

	//------------------------------------------------------------
	// Part 2.10 Systems - SmallMethods
	//------------------------------------------------------------
	public class MethodCollection {
		public boolean walkToTileCombatZone(RSTile tile) {
			boolean changedCameras = false;
			try {
				if (tile == null) {
					return true;
				}
				while (calc.distanceTo(tile) >= 4 && calc.distanceTo(tile) <= 40 && canUsePath(tile)) {
					if (calc.distanceTo(tile) > 10) {
						if ((calc.distanceTo(walking.getDestination()) < random(5, 12) || calc.distanceTo(walking.getDestination()) > 40)) {
							if (!walking.walkTileMM(tile)) {
								walking.walkTo(tile);
							}
						}
						sleep(900, 1100);
					} else if ((calc.distanceTo(walking.getDestination()) < random(2, 4) || calc.distanceTo(walking.getDestination()) > 40)) {
						tiles.doAction(getClosestTileOnScreen(tile), "alk");
					}


					if (getLifePoints() <= info.eatAt) {
						return false;
					}
					if (isInCombatZone() && !changedCameras) {
						camera.setAngle(method.getAngleToTile(tile) + random(-10, +10));
						sleep(random(300, 500));
						changedCameras = true;
					} else {
						sleep(900, 1100);
					}
				}
				while (getMyPlayer().isMoving()) {
					sleep(random(85, 125));
				}
				sleep(random(500, 600));
			} catch (Exception e) {
				return false;
			}
			return true;

		}

		public boolean walkToTile(RSTile tile) {
			try {
				if (tile == null) {
					return true;
				}
				while (calc.distanceTo(tile) >= 4 && calc.distanceTo(tile) <= 40 && canUsePath(tile)) {
					if ((calc.distanceTo(walking.getDestination()) < random(5, 12) || calc.distanceTo(walking.getDestination()) > 40) || (calc.distanceBetween(walking.getDestination(), tile) > 10)) {
						if (!walking.walkTileMM(tile)) {
							walking.walkTo(tile);
							continue;
						}
						if (random(0, 15) < 10) {
							camera.setAngle(method.getAngleToTile(tile) + +random(-10, 10));
						}
					}
					if (getLifePoints() <= info.eatAt) {
						return false;
					}
					sleep(900, 1100);
				}
				while (getMyPlayer().isMoving()) {
					sleep(random(85, 125));
				}
				sleep(random(500, 600));
			} catch (Exception e) {
				return false;
			}
			return true;

		}

		public boolean walkUsingPath(RSTile[] path) {
			if (path == null) {
				return false;
			}
			try {
				while (canUsePath(path) && calc.distanceTo(path[path.length - 1]) >= 4) {
					if ((calc.distanceTo(walking.getDestination()) < random(5, 12) || calc.distanceTo(walking.getDestination()) > 40) && !isInSameArea(getMyPlayer().getLocation(), nextTileHome(path, 20))) {
						if (!walking.walkPathMM(path)) {
							walking.walkTo(nextTileHome(path, 20));
							continue;
						}
					} else {
						if (getLifePoints() <= info.eatAt + random(-3, 3)) {
							for (int food : info.foodID) {
								if (inventory.contains(food)) {
									inventory.getItem(food).doAction("Eat");
									sleep(random(700, 1000));
									info.eatAt = random(info.eatAtMin, info.eatAtMax);
								}
							}
						}
						if (random(1, 3) == 1) {
							camera.setAngle(method.getAngleToTile(nextTileHome(path, 20)));
						}
					}
					sleep(random(900, 1200));
				}
				while (getMyPlayer().isMoving()) {
					sleep(random(85, 150));
				}
			} catch (Exception e) {
				return false;
			}
			return true;
		}

		public RSTile getClosestTileOnScreen(RSTile tile) {
			if (!calc.tileOnScreen(tile) && game.isLoggedIn()) {
				RSTile loc = players.getMyPlayer().getLocation();
				RSTile walk = new RSTile((loc.getX() + tile.getX()) / 2, (loc.getY() + tile.getY()) / 2);
				return calc.tileOnScreen(walk) ? walk : getClosestTileOnScreen(walk);
			}
			return tile;
		}

		public RSTile nextTileHome(RSTile path[], int skipDist) {
			if (path == null) {
				return null;
			}
			for (int i = path.length - 1; i >= 0; i--) {
				if (calc.distanceTo(path[i]) <= skipDist) {
					return path[i];
				}
			}
			return null;
		}

		public boolean isInSameArea(RSTile curr, RSTile destination) {
			if (curr == null || destination == null) {
				return false;
			}
			if (calc.distanceBetween(curr, destination) <= 3 && calc.distanceBetween(curr, destination) >= 0) {
				return true;
			}
			return false;
		}

		public int getLifePoints() {
			int hp = combat.getLifePoints();
			return hp <= 0 ? 1000 : hp;
		}

		public boolean isFighting() {
			return getMyPlayer().getInteracting() instanceof RSNPC;
		}

		public boolean canUsePath(RSTile... tiles) {
			if (!game.isLoggedIn()) {
				return false;
			}
			Area area = getCurrentArea();
			for (RSTile tile : tiles) {
				if (calc.tileOnMap(tile)) {
					if (area.contains(tile)) {
						return true;
					}
				}
			}
			return false;
		}

		public boolean isInCombatZone() {
			if (calc.distanceTo(setting.mainTile) <= setting.tilesOfCourse) {
				return true;
			}
			return false;
		}

		public boolean isInCombatZone(RSTile tile) {
			if (calc.distanceBetween(tile, setting.mainTile) <= setting.tilesOfCourse) {
				return true;
			}
			return false;
		}

		public int getAngleToCoordinates(int x, int y) {
			RSTile currentPosition = getMyPlayer().getLocation();
			double angle = 0;
			double width = Math.round(Math.sqrt((x - currentPosition.getX()) * (x - currentPosition.getX())));
			double heigth = Math.round(Math.sqrt((y - currentPosition.getY()) * (y - currentPosition.getY())));
			double sqrtResult = Math.sqrt(width * width + heigth * heigth);
			angle = Math.asin(Math.abs(width) / sqrtResult);
			angle = angle * 90 / 1.5707963267948966;
			int newAngle = (int) Math.round(angle);
			int newAngle1;
			int newAngle2;
			if (y - currentPosition.getY() < 0) {
				newAngle1 = 180 - newAngle;
			} else {
				newAngle1 = newAngle;
			}
			if (x - currentPosition.getX() > 0) {
				newAngle2 = 360 - newAngle1;
			} else {
				newAngle2 = newAngle1;
			}
			return newAngle2;
		}

		public int getAngleToTile(RSTile tile) {
			return getAngleToCoordinates(tile.getX(), tile.getY());
		}

		public Area getCurrentArea() {
			RSTile curr = getMyPlayer().getLocation();
			boolean[][] tiles = new boolean[104][104];
			ArrayList<RSTile> canReach = new ArrayList<RSTile>();
			ArrayList<RSTile> newOnes = new ArrayList<RSTile>();
			canReach.add(curr);
			newOnes.add(curr);
			final int[][] blocks = walking.getCollisionFlags(game.getPlane());
			while (newOnes.size() >= 1) {
				ArrayList<RSTile> newOnesTemp = new ArrayList<RSTile>();
				for (RSTile t : newOnes) {
					RSTile[] tilesGoingToCheck = {new RSTile(t.getX() - 1, t.getY()), new RSTile(t.getX() + 1, t.getY()), new RSTile(t.getX(), t.getY() - 1), new RSTile(t.getX(), t.getY() + 1)};
					for (RSTile tile : tilesGoingToCheck) {
						int i = tile.getX() - game.getBaseX() + 1, j = tile.getY() - game.getBaseY() + 1;
						if (i < 1 || i > 100 || j < 1 || j > 100) {
							continue;
						}
						if (tiles[i][j]) {
							continue;
						}
						final int curBlock = blocks[i][j];

						if ((curBlock & 0x1280100) != 0) {
							continue;
						}
						if (t.getY() < tile.getY()) {
							if (((curBlock & 0x1280120) != 0) ||
									(blocks[i][j - 1] & 0x1280102) != 0) {
								continue;
							}
						} else if (t.getY() > tile.getY()) {
							if (((blocks[i][j + 1] & 0x1280120) != 0) ||
									(curBlock & 0x1280102) != 0) {
								continue;
							}
						} else if (t.getX() < tile.getX()) {
							if (((blocks[i - 1][j] & 0x1280108) != 0)
									|| ((curBlock & 0x1280180) != 0)) {
								continue;
							}
						} else if (t.getX() > tile.getX()) {
							if (((blocks[i + 1][j] & 0x1280180) != 0)
									|| ((curBlock & 0x1280108) != 0)) {
								continue;
							}
						}
						canReach.add(tile);
						newOnesTemp.add(tile);
						tiles[i][j] = true;

					}
				}
				newOnes = newOnesTemp;
			}
			RSTile[] tiles1 = new RSTile[canReach.size()];
			for (int i = 0; i < canReach.size(); i++) {
				tiles1[i] = canReach.get(i);
			}
			return new Area(tiles1);
		}

		public int getRealLevel(final int index) {
			if (Skills.getLevelAt(skills.getCurrentExp(index)) >= 99) {
				return 99;
			}
			return Skills.getLevelAt(skills.getCurrentExp(index));
		}

		public int getValue(String s) {
			if (s == null || s.isEmpty()) {
				return -1;
			}
			String intString = "";
			for (char c : s.toCharArray()) {
				if (c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' ||
						c == '6' || c == '7' || c == '8' || c == '9') {
					intString += c;
				}
			}
			if (!intString.isEmpty()) {
				return Integer.parseInt(intString);
			}
			return -1;
		}

		public String getValueString(String line) {
			String a = "";
			boolean stop = false;
			for (char c : line.toCharArray()) {
				if (c == '(') {
					stop = false;
				} else if (c == '<' || stop) {
					stop = true;
				} else if (c == ',' || c == ')') {
					return a;
				} else if (a == "" && c == ' ') {

				} else {
					a += c;
				}
			}
			return a;
		}

		public String[] getValueLine(String line, String var) {
			ArrayList<String> temp = new ArrayList<String>();
			String s = "";
			String varLine = "";
			boolean stop = false;
			for (char c : line.toCharArray()) {
				if (c == '(') {
					stop = false;
				} else if (c == '<' || stop) {
					if (var != null) {
						if (c == '<') {
							varLine = "<";
						} else {
							varLine += c;
						}

					}
					stop = true;
				} else if (c == ',' || c == ')') {
					if (var != null) {
						if (varLine.contains(var)) {
							temp.add(s);
						}
					} else {
						temp.add(s);
					}
					s = "";
				} else if (c == ' ') {
					if (s.length() > 0) {
						s += c;
					}
				} else {
					s += c;
				}
			}
			String[] array = new String[temp.size()];
			for (int i = 0; i < temp.size(); i++) {
				array[i] = temp.get(i);
			}
			return array;
		}
	}

	//------------------------------------------------------------
	// Part 2.9 Systems - Variables
	//------------------------------------------------------------
	public class InfoDatabase {
		final double scriptVersion = 1.01;
		final int[] foodID = {1895, 1893, 1891, 4293, 2142, 291, 2140, 3228, 9980,
				7223, 6297, 6293, 6295, 6299, 7521, 9988, 7228, 2878, 7568, 2343,
				1861, 13433, 315, 325, 319, 3144, 347, 355, 333, 339, 351, 329,
				3381, 361, 10136, 5003, 379, 365, 373, 7946, 385, 397, 391, 3369,
				3371, 3373, 2309, 2325, 2333, 2327, 2331, 2323, 2335, 7178, 7180,
				7188, 7190, 7198, 7200, 7208, 7210, 7218, 7220, 2003, 2011, 2289,
				2291, 2293, 2295, 2297, 2299, 2301, 2303, 1891, 1893, 1895, 1897,
				1899, 1901, 7072, 7062, 7078, 7064, 7084, 7082, 7066, 7068, 1942,
				6701, 6703, 7054, 6705, 7056, 7060, 2130, 1985, 1993, 1989, 1978,
				5763, 5765, 1913, 5747, 1905, 5739, 1909, 5743, 1907, 1911, 5745,
				2955, 5749, 5751, 5753, 5755, 5757, 5759, 5761, 2084, 2034, 2048,
				2036, 2217, 2213, 2205, 2209, 2054, 2040, 2080, 2277, 2225, 2255,
				2221, 2253, 2219, 2281, 2227, 2223, 2191, 2233, 2092, 2032, 2074,
				2030, 2281, 2235, 2064, 2028, 2187, 2185, 2229, 6883, 1971, 4608,
				1883, 1885, 15272};

		//It's allowed to copy, but give credits to me(DutchSniper).
		public final int[] ClueScrollIDS = {2677, 2678, 2679, 2680, 2681, 2682, 2683, 2684, 2685, 2686, 2687, 2688, 2689, 2690, 2691, 2692, 2693, 2694, 2695, 2696,
				2697, 2698, 2699, 2700, 2701, 2702, 2703, 2704, 2705, 2706, 2707, 2708, 2709, 2710, 2711, 2712, 2713, 2716,
				2719, 2722, 2723, 2725, 2727, 2729, 2731, 2733, 2735, 2737, 2739, 2741, 2743, 2745, 2747, 2773, 2774, 2776, 2778, 2780, 2782, 2783,
				2785, 2786, 2788, 2790, 2792, 2793, 2794, 2796, 2797, 2799, 2801, 2803, 2805, 2807, 2809, 2811, 2813, 2815, 2817, 2819, 2821,
				2823, 2825, 2827, 2829, 2831, 2833, 2835, 2837, 2839, 2841, 2843, 2845, 2847, 2848, 2849, 2851, 2853, 2855, 2856, 2857, 2858, 3490,
				3491, 3492, 3493, 3494, 3495, 3496, 3497, 3498, 3499, 3500, 3501, 3502, 3503, 3504, 3505, 3506, 3507, 3508, 3509, 3510, 3512,
				3513, 3514, 3515, 3516, 3518, 3520, 3522, 3524, 3525, 3526, 3528, 3530, 3532, 3534, 3536, 3538, 3540, 3542, 3544, 3546, 3548, 3550,
				3552, 3554, 3556, 3558, 3560, 3562, 3564, 3566, 3568, 3570, 3572, 3573, 3574, 3575, 3577, 3579, 3580, 3582, 3584, 3586, 3588, 3590,
				3592, 3594, 3596, 3598, 3599, 3601, 3602, 3604, 3605, 3607, 3609, 3610, 3611, 3612, 3613, 3614, 3615, 3616, 3617, 3618, 7236, 7238,
				7239, 7241, 7243, 7245, 7247, 7248, 7249, 7250, 7251, 7252, 7253, 7254, 7255, 7256, 7258, 7260, 7262, 7264, 7266, 7268, 7270, 7272,
				7274, 7276, 7278, 7280, 7282, 7284, 7286, 7288, 7290, 7292, 7294, 7296, 7298, 7300, 7301, 7303, 7304, 7305, 7307, 7309, 7311, 7313,
				7315, 7317, 10180, 10182, 10184, 10186, 10188, 10190, 10192, 10194, 10196, 10198, 10200, 10202, 10204, 10206, 10208, 10210, 10212,
				10214, 10216, 10218, 10220, 10222, 10224, 10226, 10228, 10230, 10232, 10234, 10236, 10238, 10240, 10242, 10244, 10246, 10248, 10250,
				10252, 10254, 10256, 10258, 10260, 10262, 10264, 10266, 10268, 10270, 10272, 10274, 10276, 10278, 13010, 13012, 13014, 13016, 13018,
				13020, 13022, 13024, 13026, 13028, 13030, 13032, 13034, 13036, 13038, 13040, 13041, 13042, 13044, 13046, 13048, 13049, 13050, 13051,
				13053, 13055, 13056, 13058, 13060, 13061, 13063, 13065, 13067, 13068, 13069, 13070, 13071, 13072, 13074, 13075, 13076, 13078, 13079,
				13080};


		final int[] AllBones = {526, 528, 530, 532, 534, 536};
		final int B2PTab = 8015;
		final int[] teleportTabs = {8008, 8009, 8010, 8011, 8013};

		final int[] combatPots = {9745, 9743, 9741, 9739};

		final int[] attackPots = {125, 123, 121, 2428};
		final int[] superAttackPots = {149, 147, 145, 2436};
		final int[] extremeAttackPots = {15311, 15310, 15309, 15308};
		final int[] attackPotsAll = {125, 123, 121, 2428, 149, 147, 145, 2436, 15311, 15310, 15309, 15308};

		final int[] strengthPots = {119, 117, 115, 113};
		final int[] superStrengthPots = {161, 159, 157, 2440};
		final int[] extremeStrengthPots = {15315, 15314, 15313, 15312};
		final int[] strengthPotsAll = {119, 117, 115, 113, 161, 159, 157, 2440, 15315, 15314, 15313, 15312};

		final int[] defencePots = {137, 135, 133, 2432};
		final int[] superDefencePots = {167, 165, 163, 2442};
		final int[] extremeDefensePots = {15319, 15318, 15317, 15316};
		final int[] defencePotsAll = {137, 135, 133, 2432, 167, 165, 163, 2442, 15319, 15318, 15317, 15316};

		final int[] overloadPots = {15335, 15334, 15333, 15332};

		final int[] prayerPots = {143, 141, 139, 2434};

		final int[] rangedPots = {173, 171, 169, 2444};
		final int[] extremeRangedPots = {15327, 15326, 15325, 15324};
		final int[] rangedPotsAll = {173, 171, 169, 2444, 15327, 15326, 15325, 15324};

		final int[] summoningPots = {12146, 12144, 12142, 12140};

		final int[] allPots = {125, 123, 121, 2428, 149, 147, 145, 2436, 15311, 15310, 15309, 15308,
				119, 117, 115, 113, 161, 159, 157, 2440, 15315, 15314, 15313, 15312,
				137, 135, 133, 2432, 167, 165, 163, 2442, 15319, 15318, 15317, 15316,
				15335, 15334, 15333, 15332, 143, 141, 139, 2434, 173, 171, 169, 2444, 15327, 15326, 15325, 15324};

		final int[] guthanSpear = {4726, 4910, 4911, 4912, 4913};
		final int[] guthanHelm = {4724, 4904, 4905, 4906, 4907};
		final int[] guthanPlateBody = {4728, 4916, 4917, 4918, 4919};
		final int[] guthanPlateLegs = {4730, 4922, 4923, 4924, 4925};
		final int[] AllGuthanCodes = {4726, 4910, 4911, 4912, 4913, 4724, 4904, 4905, 4906, 4907, 4728, 4916, 4917, 4918, 4919, 4730, 4922, 4923, 4924, 4925};
		final int[] brokenGuthans = {4908, 4914, 4920, 4926};

		final int[] slayerRings = {13288, 13287, 13286, 13285, 13284, 13283, 13282, 13281};
		final int[] duelRings = {2566, 2564, 2562, 2560, 2558, 2556, 2554, 2552};
		final int[] gameNecklaces = {3853, 3855, 3857, 3859, 3861, 3863, 3865, 3867};
		final int[] amuletOfGlories = {1706, 1708, 1710, 1712};

		final int AirRune = 1, FireRune = 2, EarthRune = 3, WaterRune = 4;

		final int AirRune1 = 556, FireRune1 = 554, EarthRune1 = 557, WaterRune1 = 555, LawRune = 563, DustRune = 4696, LavaRune = 4699,
				MistRune = 4695, MudRune = 4698, SmokeRune = 4697, SteamRune = 4694;


		public boolean inventoryContains(int... checkAllIDS) {
			for (int ID : checkAllIDS) {
				if (ID == 1) {
					if (!inventory.containsOneOf(AirRune1, MistRune, DustRune, SmokeRune)) {
						return false;
					}
				} else if (ID == 2) {
					if (!inventory.containsOneOf(FireRune1, SmokeRune, SteamRune, LavaRune)) {
						return false;
					}
				} else if (ID == 3) {
					if (!inventory.containsOneOf(EarthRune1, DustRune, MudRune, LavaRune)) {
						return false;
					}
				} else if (ID == 4) {
					if (!inventory.containsOneOf(WaterRune1, MistRune, MudRune, SteamRune)) {
						return false;
					}
				} else {
					if (!inventory.contains(ID)) {
						return false;
					}
				}
			}

			return false;
		}

		int weaponID, shieldID, helmID, bodyID, legsID;

		int[] AllEquipment;

		int stylePrimaryWeaponColor, styleSecondaryWeaponColor, stylePrimaryWeaponClick, styleSecondaryWeaponClick;

		int eatAt, eatAtMin, eatAtMax;

		boolean miamiPaintOnlyCountSomeLoot;
		String miamiPaintNameOfScript = "MiamiFighter";
		String miamiPaintVersionOfScript = scriptVersion + "";
		String miamiPaintStatus = "Fighting Jad";
	}

	public class SettingDatabase {
		//------------------------------------------------------------
		// Part 1.1.2 Basic Things - Normal Variables
		//------------------------------------------------------------
		RSTile mainTile = null;
		int tilesOfCourse = 0;

		boolean useFood = false, useB2P = false, useGuthans = false;
		int percentEatingMin = 0, percentEatingMax = 0;


		boolean multiCombat = false, useBank = false, useTeleportTabsInEmergency = false, useTeleportInEmergency = false,
				agressiveArea = false, buryBones = false, useHighAlchemy = false;

		boolean useSpecial = false, useSecondaryWeaponSpecial = false;
		int primaryWeaponID, secondaryWeaponID, atWhatPercentToUseSpecial, primaryWeaponStyle, secondaryWeaponStyle;

		boolean lootItems = false;

		int drinkPotsAbove;

		boolean lootItemsAboveACertainPrice = false;
		int lootItemsAbovePrice = 0;

		boolean lootClueScrolls = false;

		Monster[] arrayOfMonsters = null;
		Loot[] lootList = new Loot[]{};
		PathPart[] bankingPath = null;
		PathPart[] toMonstersPath = null;
		BankingPart[] bankingAllParts = null;
	}

	public class ExtraSystem {
		InventoryItemCheck inventoryItemCheck = new InventoryItemCheck();
		MiamiPaint miamiPaint = new MiamiPaint();

		public class InventoryItemCheck {
			ArrayList<ItemsGotten> itemsYouGot = new ArrayList<ItemsGotten>();
			ArrayList<LootedItem> lootedItems = new ArrayList<LootedItem>();
			ArrayList<Integer> lootNotToCount = new ArrayList<Integer>();
			ArrayList<Integer> onlyLootToCount = new ArrayList<Integer>();

			public final int[] allNumbersNotToCount = {
					-1,
					9745, 9743, 9741, 9739, 125, 123, 121, 2428, 149, 147, 145, 2436, 15311, 15310, 15309, 15308,
					119, 117, 115, 113, 161, 159, 157, 2440, 15315, 15314, 15313, 15312, 137, 135, 133, 2432, 167,
					165, 163, 2442, 15319, 15318, 15317, 15316, 15335, 15334, 15333, 15332,
					143, 141, 139, 2434, 173, 171, 169, 2444, 15327, 15326, 15325, 15324,
					3867, 3865, 3863, 3861, 3859, 3857, 3855, 3853,
					13288, 13287, 13286, 13285, 13284, 13283, 13282, 13281,
					2566, 2564, 2562, 2560, 2558, 2556, 2554, 2552,
					12146, 12144, 12142, 12140


			};

			public InventoryItemCheck() {
				for (int i : allNumbersNotToCount) {
					lootNotToCount.add(i);
				}
				for (int i = 4900; i <= 4998; i++) {
					lootNotToCount.add(i);
				}
				for (int i = 4700; i <= 4759; i++) {
					lootNotToCount.add(i);
				}
			}

			public void update(boolean count) {
				if (!game.isLoggedIn()) {
					return;
				}
				if (game.getCurrentTab() != Game.TAB_INVENTORY) {
					game.openTab(Game.TAB_INVENTORY);
					sleep(500, 700);
				}
				RSComponent invIface = inventory.getInterface();
				if (invIface != null) {
					if (invIface.getComponents().length > 0) {
					} else {
						return;
					}
				} else {
					return;
				}
				for (RSItem tempItem : inventory.getItems()) {
					boolean hasItem = false;
					if (info.miamiPaintOnlyCountSomeLoot) {
						boolean countItems = false;
						for (int i : onlyLootToCount) {
							if (i == tempItem.getID()) {
								countItems = true;
								break;
							}
						}
						if (!countItems) {
							continue;
						}
					}
					for (ItemsGotten anItemYouGot : itemsYouGot) {
						if (anItemYouGot.itemID == tempItem.getID()) {
							hasItem = true;
						}
					}
					if (!hasItem && !intIsOneOfFollowing(tempItem.getID(), lootNotToCount)) {
						itemsYouGot.add(new ItemsGotten(tempItem.getID(), count));
					}
				}
				for (ItemsGotten anItemYouGot : itemsYouGot) {
					anItemYouGot.update(count);
				}
			}

			public int howMuchMoneyIsMade() {
				int money = 0;
				for (ItemsGotten tempItem : itemsYouGot) {
					money += tempItem.getTotalPrice();
				}
				return money;
			}

			public String[] getLastLootedItems() {
				int amountOfItems = 7;
				ArrayList<String> lastLootedNames = new ArrayList<String>();
				for (int i = lootedItems.size() - 1; (i >= lootedItems.size() - amountOfItems && i >= 0); i--) {
					LootedItem last = lootedItems.get(i);
					String name = "";
					boolean save = false;
					for (char c : last.name.toCharArray()) {
						if (c == '<') {
							save = false;
						} else if (c == '>') {
							save = true;
						} else if (save) {
							name += c;
						}
					}
					lastLootedNames.add("Looted: " + name + ", Amount: " + last.amount + ", Price: " + last.getTotalPrice() + " (Each: " + last.getPrice() + "), Looted at: " + last.timeGotten);
				}
				String[] newString = new String[lastLootedNames.size()];
				for (int i = 0; i < lastLootedNames.size(); i++) {
					newString[i] = lastLootedNames.get(i);
				}
				return newString;
			}

			public boolean intIsOneOfFollowing(final int index, ArrayList<Integer> array) {
				for (int i : array) {
					if (i == index) {
						return true;
					}
				}
				return false;
			}

			public class ItemsGotten {
				public int itemID = 0;
				public int itemGotten = 0;
				public int itemQuantityNow = 0;
				public String itemName = null;

				public ItemsGotten(int tempID, boolean count) {
					itemID = tempID;
					itemName = inventory.getItem(itemID).getName();
					itemQuantityNow = inventory.getCount(true, itemID);
					if (count) {
						itemQuantityNow = itemGotten;
					}
				}

				public String getName() {
					return itemName;
				}

				public int getTotalPrice() {
					return geInfoChecker.getValue(itemID) * itemGotten;
				}

				public int getPrice() {
					return geInfoChecker.getValue(itemID);
				}

				public void update(boolean count) {
					if (itemQuantityNow < inventory.getCount(true, itemID)) {
						if (count) {
							itemGotten += inventory.getCount(true, itemID) - itemQuantityNow;

							DateFormat dataFormat = new SimpleDateFormat("HH:mm:ss");
							String timeGotten = dataFormat.format(new Date());
							lootedItems.add(new LootedItem(itemID, itemName, inventory.getCount(true, itemID) - itemQuantityNow, timeGotten));
						}
						itemQuantityNow = inventory.getCount(true, itemID);
					} else if (itemQuantityNow > inventory.getCount(true, itemID)) {
						itemQuantityNow = inventory.getCount(true, itemID);
					}
				}

			}

			public class LootedItem {
				public int itemID;
				public String name = null;
				public int amount;
				public String timeGotten;

				public LootedItem(int id, String tempName, int tempAmount, String timeGottenTemp) {
					itemID = id;
					name = tempName;
					amount = tempAmount;
					timeGotten = timeGottenTemp;
				}

				public int getTotalPrice() {
					return geInfoChecker.getValue(itemID) * amount;
				}

				public int getPrice() {
					return geInfoChecker.getValue(itemID);
				}
			}

		}

		MiamiPaint paint = new MiamiPaint();

		public class MiamiPaint implements ImageObserver {
			public long startTime = System.currentTimeMillis();
			String scriptName = "Miami Script";
			String thisVersion = "1.3.3.7";
			String paintScreen = "General";
			public final String[] allScreens = {"General", "Skills", "Loot", "Nothing"};
			public final int[] allScreenSizes = new int[allScreens.length];


			String[] allSkillNames = {"Attack", "Defence",
					"Strength", "Constitution", "Range", "Prayer", "Magic", "Cooking",
					"Woodcutting", "Fletching", "Fishing", "Firemaking", "Crafting",
					"Smithing", "Mining", "Herblore", "Agility", "Thieving", "Slayer",
					"Farming", "Runecrafting", "Hunter", "Construction", "Summoning",
					"Dungeoneering"};
			int[] startXP = new int[allSkillNames.length];
			int[] gainedXP = new int[allSkillNames.length];
			int totalXPGained;


			public void startSkillXP() {
				for (int i = 0; i < allSkillNames.length; i++) {
					if (game.isLoggedIn() && skills.getCurrentExp(i) > 0) {
						startXP[i] = skills.getCurrentExp(i);
					}
				}
				startTime = System.currentTimeMillis();
			}

			public void updateSkillXP() {
				totalXPGained = 0;
				for (int i = 0; i < allSkillNames.length; i++) {
					if (game.isLoggedIn() && skills.getCurrentExp(i) > 0) {
						if (startXP[i] <= 0) {
							startXP[i] = skills.getCurrentExp(i);
						} else {
							gainedXP[i] = skills.getCurrentExp(i) - startXP[i];
							totalXPGained += gainedXP[i];
						}
					}
				}
			}

			public boolean pointIsInArea(Point p, final int minX, final int maxX, final int minY, final int maxY) {
				if (p.getX() >= minX && p.getX() <= maxX && p.getY() >= minY && p.getY() <= maxY) {
					return true;
				}
				return false;
			}

			public void makePaint(Graphics g) {
				Font originalFont = g.getFont();

				paint.updateSkillXP();
				if (game.isLoggedIn()) {
					if (paint.paintScreen.contains(paint.allScreens[0])) {
						g.setColor(new Color(0, 0, 255, 175));
						RSComponent BackGround = interfaces.getComponent(137, 0);
						g.fill3DRect(BackGround.getAbsoluteX(), BackGround.getAbsoluteY(), BackGround.getWidth(), BackGround.getHeight(), true);
						g.setColor(new Color(0, 0, 0, 170));
						g.fill3DRect(BackGround.getAbsoluteX() + 5, BackGround.getAbsoluteY() + 30, BackGround.getWidth() - 10, BackGround.getHeight() - 30 - 21, true);

						g.setColor(new Color(0, 0, 0, 170));
						g.fill3DRect(BackGround.getAbsoluteX() + 5, BackGround.getAbsoluteY() + 5, BackGround.getWidth() - 10, 20, true);
						g.setColor(new Color(255, 128, 128, 255));
						g.fill3DRect(BackGround.getAbsoluteX() + 6, BackGround.getAbsoluteY() + 6, BackGround.getWidth() - 12, 9, true);
						g.setColor(new Color(255, 0, 0, 255));
						g.fill3DRect(BackGround.getAbsoluteX() + 6, BackGround.getAbsoluteY() + 15, BackGround.getWidth() - 12, 9, true);

						g.setColor(new Color(255, 255, 255, 255));
						g.setFont(new Font(originalFont.getName(), Font.PLAIN, originalFont.getSize() + 3));
						g.drawString(info.miamiPaintNameOfScript + " - Version: " + info.miamiPaintVersionOfScript, BackGround.getAbsoluteX() + 11, BackGround.getAbsoluteY() + 20);
						g.setFont(originalFont);

						long totalTime = System.currentTimeMillis() - paint.startTime;

						long millis = System.currentTimeMillis() - paint.startTime;
						long hours1 = millis / (1000 * 60 * 60);
						millis -= hours1 * (1000 * 60 * 60);
						long minutes1 = millis / (1000 * 60);
						millis -= minutes1 * (1000 * 60);
						long seconds1 = millis / 1000;
						String hours = null, minutes = null, seconds = null;
						if (hours1 < 10) hours = "0" + hours1;
						else hours = "" + hours1;
						if (minutes1 < 10) minutes = "0" + minutes1;
						else minutes = "" + minutes1;
						if (seconds1 < 10) seconds = "0" + seconds1;
						else seconds = "" + seconds1;


						long XPLeft = 0;
						;
						long kXPGotten = 0;
						if (totalTime > 1) {
							XPLeft = Math.round((int) ((long) paint.totalXPGained * 36000 / totalTime));
							kXPGotten = XPLeft / 10;
							XPLeft -= kXPGotten * 10;
						}
						String totalXPPerHour = kXPGotten + "." + XPLeft;
						long moneyMade = inventoryItemCheck.howMuchMoneyIsMade();
						long moneyMadePerHour = 0;
						long kMoneyMadePerHour = 0;
						if (totalTime > 1) {
							moneyMadePerHour = Math.round((int) (moneyMade * 36000 / totalTime));
							kMoneyMadePerHour = moneyMadePerHour / 10;
							moneyMadePerHour -= kMoneyMadePerHour * 10;
						}
						String totalMoneyMadePerHour = kMoneyMadePerHour + "." + moneyMadePerHour;


						g.drawString("Time Running: ", BackGround.getAbsoluteX() + 9, BackGround.getAbsoluteY() + 44);
						g.drawString("Exp Gained: ", BackGround.getAbsoluteX() + 9, BackGround.getAbsoluteY() + 58);
						g.drawString("Exp per Hour: ", BackGround.getAbsoluteX() + 9, BackGround.getAbsoluteY() + 72);
						g.drawString("Money Made: ", BackGround.getAbsoluteX() + 9, BackGround.getAbsoluteY() + 86);

						g.drawString(hours + ":" + minutes + ":" + seconds, BackGround.getAbsoluteX() + 130, BackGround.getAbsoluteY() + 44);
						g.drawString("" + paint.totalXPGained, BackGround.getAbsoluteX() + 130, BackGround.getAbsoluteY() + 58);
						g.drawString(totalXPPerHour + "K Exp/H", BackGround.getAbsoluteX() + 130, BackGround.getAbsoluteY() + 72);
						g.drawString(moneyMade + " (" + totalMoneyMadePerHour + "K P/H)", BackGround.getAbsoluteX() + 130, BackGround.getAbsoluteY() + 86);

						//int xTitleSecondRow = (BackGround.getWidth() - 18)/2 + 10 + BackGround.getAbsoluteX(), xResultsSecondRow = (BackGround.getWidth() - 18)/2 + 130 + BackGround.getAbsoluteX();
						//g.drawString("Status:", xTitleSecondRow, BackGround.getAbsoluteY() + 44);

						//g.drawString(info.miamiPaintStatus, xResultsSecondRow, BackGround.getAbsoluteY() + 44);

					} else if (paint.paintScreen.contains(paint.allScreens[1])) {
						g.setColor(new Color(0, 0, 255, 175));
						RSComponent BackGround = interfaces.getComponent(137, 0);
						g.fill3DRect(BackGround.getAbsoluteX(), BackGround.getAbsoluteY(), BackGround.getWidth(), BackGround.getHeight(), true);


						int BackGroundBovenkant = BackGround.getAbsoluteY();
						int SkillXPBegin = BackGround.getAbsoluteX() + 5;
						long totalTime = Math.round((System.currentTimeMillis() - paint.startTime));
						if (paint.startTime <= 0) {
							totalTime = 0;
						}

						int skillNumber = 0;
						for (int i = 0; i < paint.allSkillNames.length; i++) {
							if (paint.gainedXP[i] <= 0 || totalTime <= 0) {
								continue;
							}

							g.setColor(new Color(0, 0, 0, 170));
							g.fill3DRect(SkillXPBegin, BackGroundBovenkant + 5 + (skillNumber * 21), 452, 16, true);


							double percentTillNextLVLAtt = 4.5 * skills.getPercentToNextLevel(i);
							int percentToNextAttLVL = (int) (Math.round(percentTillNextLVLAtt));
							g.setColor(new Color(255, 128, 128, 255));
							g.fill3DRect(SkillXPBegin + 1, BackGroundBovenkant + 6 + (skillNumber * 21), percentToNextAttLVL, 7, true);
							g.setColor(new Color(255, 0, 0, 255));
							g.fill3DRect(SkillXPBegin + 1, BackGroundBovenkant + 13 + (skillNumber * 21), percentToNextAttLVL, 7, true);

							String name = paint.allSkillNames[i];
							int gainedXPFor = paint.gainedXP[i];
							long millisTillLVL = 0;
							String hoursTillLVL = "0";
							String minutesTillLVL = "0";
							String secondsTillLVL = "0";
							String timeTillLVL = "0:0:0";

							int XPLeft = 0;
							int kXPGotten = 0;
							String skillXPPerHour = null;


							if (totalTime > 1) {

								XPLeft = Math.round(((long) gainedXPFor * 36000 / totalTime));
								kXPGotten = XPLeft / 10;
								XPLeft -= kXPGotten * 10;
								skillXPPerHour = kXPGotten + "." + XPLeft;

								millisTillLVL = Math.round(skills.getExpToNextLevel(i) * (totalTime / gainedXPFor));
								long millis = millisTillLVL;
								long hoursTillLVL1 = millis / (1000 * 60 * 60);
								millis -= hoursTillLVL1 * (1000 * 60 * 60);
								long minutesTillLVL1 = millis / (1000 * 60);
								millis -= minutesTillLVL1 * (1000 * 60);
								long secondsTillLVL1 = millis / 1000;
								if (hoursTillLVL1 < 10) hoursTillLVL = "0" + hoursTillLVL1;
								else hoursTillLVL = "" + hoursTillLVL1;
								if (minutesTillLVL1 < 10) minutesTillLVL = "0" + minutesTillLVL1;
								else minutesTillLVL = "" + minutesTillLVL1;
								if (secondsTillLVL1 < 10) secondsTillLVL = "0" + secondsTillLVL1;
								else secondsTillLVL = "" + secondsTillLVL1;
								timeTillLVL = hoursTillLVL + ":" + minutesTillLVL + ":" + secondsTillLVL;
							}

							int xToStartWritingSkill = SkillXPBegin + 6;
							g.setColor(new Color(255, 255, 255, 255));
							g.drawString(name + " Exp: " + gainedXPFor + " || Avg: " + skillXPPerHour + "K Exp/H || " +
									skills.getPercentToNextLevel(i) + "% || TTL: " + timeTillLVL
									, xToStartWritingSkill, BackGroundBovenkant + 17 + (skillNumber * 21));
							skillNumber++;
						}
					} else if (paint.paintScreen.contains(paint.allScreens[2])) {
						g.setColor(new Color(0, 0, 255, 175));
						RSComponent BackGround = interfaces.getComponent(137, 0);
						g.fill3DRect(BackGround.getAbsoluteX(), BackGround.getAbsoluteY(), BackGround.getWidth(), BackGround.getHeight(), true);
						g.setColor(new Color(0, 0, 0, 170));
						g.fill3DRect(BackGround.getAbsoluteX() + 5, BackGround.getAbsoluteY() + 5, BackGround.getWidth() - 10, BackGround.getHeight() - 5 - 21, true);

						g.setColor(new Color(255, 255, 255, 255));
						int randomNumber = 0;
						for (String s : inventoryItemCheck.getLastLootedItems()) {
							g.drawString(s, BackGround.getAbsoluteX() + 10, BackGround.getAbsoluteY() + 17 + (randomNumber * 14));
							randomNumber++;
						}
					} else if (paint.paintScreen.contains(paint.allScreens[3])) {

					}


					RSComponent BackGround = interfaces.getComponent(137, 0);
					int startOfPaintingY = BackGround.getAbsoluteY() + BackGround.getHeight() - 16;
					int startOfPaintingX = BackGround.getAbsoluteX() + BackGround.getWidth() - (70 * paint.allScreens.length);
					for (int i = 0; i < paint.allScreens.length; i++) {
						g.setColor(new Color(0, 0, 0, 170));
						g.fill3DRect(startOfPaintingX + (i * 70), startOfPaintingY, 70, 16, true);
						if (paint.paintScreen.contains(paint.allScreens[i])) {

							g.setColor(new Color(19, 191, 0, 255));
							g.fill3DRect(startOfPaintingX + 1 + (i * 70), startOfPaintingY + 1, 68, 14, true);

							//g.setFont(new Font(originalFont.getFontName(), Font.BOLD, originalFont.getSize()));
							g.setColor(new Color(255, 255, 255, 255));
							g.drawString(paint.allScreens[i], startOfPaintingX + 6 + (i * 70), startOfPaintingY + 12);
							g.setFont(originalFont);
						} else {
							g.setColor(new Color(255, 255, 255, 255));
							g.drawString(paint.allScreens[i], startOfPaintingX + 6 + (i * 70), startOfPaintingY + 12);
						}
					}


				}
			}

			public boolean imageUpdate(Image img, int infoflags, int x, int y,
									   int width, int height) {
				// TODO Auto-generated method stub
				return false;
			}
		}

		GEInfoChecker geInfoChecker = new GEInfoChecker();

		public class GEInfoChecker {
			ArrayList<GEItemInfo> allItems = new ArrayList<GEItemInfo>();
			ArrayList<Integer> allItemIDs = new ArrayList<Integer>();
			boolean isLoading = false;

			public void update() {
				isLoading = true;
				int itemSize = allItems.size(), itemIDSize = allItemIDs.size();
				for (int i = itemSize; i < itemIDSize; i++) {
					allItems.add(grandExchange.loadItemInfo(allItemIDs.get(i)));
				}
				isLoading = false;
			}

			public int getValue(int ID) {
				if (allItemIDs.size() > allItems.size() && !isLoading) {
					update();
				}
				for (int i = 0; i < allItems.size(); i++) {
					if (allItemIDs.get(i) == ID) {
						return allItems.get(i).getMarketPrice();
					}
				}
				for (int i : allItemIDs) {
					if (i == ID) {
						return 0;
					}
				}
				allItemIDs.add(ID);
				return 0;
			}
		}
	}

	public void onRepaint(Graphics g) {
		extraSystem.miamiPaint.makePaint(g);
	}

	public class MiamiFighterGUI extends JFrame {
		private static final long serialVersionUID = 1L;


		ItemsGUI itemsGUI = new ItemsGUI();

		public class ItemsGUI {
			ArrayList<Loot> tempLoot = new ArrayList<Loot>();

			public void updateScreen() {
				String[] s = new String[tempLoot.size()];
				for (int i = 0; i < tempLoot.size(); i++) {
					Loot l = tempLoot.get(i);
					s[i] = (i + 1) + ". " + l.getName() + " (" + l.getID() + ") Is Stackable: (" + l.isStackable() + ") Make Space: (" + l.makeSpace() + ") High-Alch: (" + l.isAlching() + ")";
				}
				final String[] array = tempLoot.size() > 0 ? s : new String[]{"Nothing"};
				AbstractListModel model = new AbstractListModel() {
					/**
					 *
					 */
					private static final long serialVersionUID = 1L;
					String[] values = array;

					public int getSize() {
						return values.length;
					}

					public Object getElementAt(int i) {
						return values[i];
					}

				};
				list5.setModel(model);

			}

			public void delete(String s) {
				if (s == null || s == "" || s == "Nothing") {
					return;
				}
				for (int i = 0; i < tempLoot.size(); i++) {
					Loot l = tempLoot.get(i);
					if (s.contains((i + 1) + ". " + l.getName() + " (" + l.getID() + ") Is Stackable: (" + l.isStackable() + ") Make Space: (" + l.makeSpace() + ") High-Alch: (" + l.isAlching() + ")")) {
						tempLoot.remove(i);
						break;
					}
				}
				updateScreen();
			}

			public void add(Loot l) {
				tempLoot.add(l);
				updateScreen();
			}

			ItemsGUISettings itemsGUISettings = new ItemsGUISettings();

			public class ItemsGUISettings {
				public void loadItemsSettings(String fileName) {
					ArrayList<Loot> allLoot = new ArrayList<Loot>();
					try {
						FileInputStream fstream = new FileInputStream("MiamiFighter_Settings/Loot_Settings/" + fileName + ".txt");
						DataInputStream in = new DataInputStream(fstream);
						BufferedReader br = new BufferedReader(new InputStreamReader(in));

						String line;
						while ((line = br.readLine()) != null) {
							try {
								int ID = method.getValue(method.getValueLine(line, "<ItemID>")[0]);
								String name = method.getValueLine(line, "<ItemName>")[0];
								boolean stackable = method.getValueLine(line, "<Stackable>")[0].contains("true") ? true : false;
								boolean space = method.getValueLine(line, "<MakeSpace>")[0].contains("true") ? true : false;
								boolean alch = method.getValueLine(line, "<HighAlch>")[0].contains("true") ? true : false;
								allLoot.add(new Loot(ID, name, stackable, alch, space));
							} catch (Exception e) {
							}
						}

						in.close();
					} catch (Exception e) {
					}
					tempLoot = allLoot;
					itemsGUI.updateScreen();
				}

				public void saveLootSettings(String fileName) {
					try {
						ArrayList<String> finalSavingThings = new ArrayList<String>();
						for (Loot l : tempLoot) {
							String s = "";
							s += "<ItemID>(" + l.getID() + ")<ItemName>(" + l.getName() + ")";
							s += "<Stackable>(" + l.isStackable() + ")";
							s += "<MakeSpace>(" + l.makeSpace() + ")";
							s += "<HighAlch>(" + l.isAlching() + ")";

							finalSavingThings.add(s);
						}
						BufferedWriter writer = new BufferedWriter(new FileWriter("MiamiFighter_Settings/Loot_Settings/" + fileName + ".txt"));
						int i = 1;
						for (String s : finalSavingThings) {
							writer.write(s);
							if (i < finalSavingThings.size()) {
								writer.newLine();
							}
							i++;
						}
						writer.close();
					} catch (Exception e) {

					}
				}


			}
		}


		NPCGUI npcGUI = new NPCGUI();

		public class NPCGUI {
			ArrayList<Monster> tempMonsters = new ArrayList<Monster>();
			ArrayList<Monster> tempMonstersCheck = new ArrayList<Monster>();

			public void updateScreen() {
				list1.setModel(npcGUI.getList(false));

				list2.setModel(npcGUI.getList(true));
			}

			public AbstractListModel getList(boolean monstersToKill) {
				ArrayList<Monster> temp = monstersToKill ? tempMonsters : tempMonstersCheck;
				String[] s = new String[temp.size()];
				for (int i = 0; i < temp.size(); i++) {
					s[i] = (i + 1) + ". " + temp.get(i).getName() + " (Level: " + temp.get(i).getLevel() + ") (ID: " + temp.get(i).getID() + ")";
				}
				final String[] array = s.length > 0 ? s : new String[]{"Nothing"};
				return new AbstractListModel() {
					private static final long serialVersionUID = 1L;
					String[] values = array;

					public int getSize() {
						return values.length;
					}

					public Object getElementAt(int i) {
						return values[i];
					}
				};

			}

			public void refresh() {
				tempMonstersCheck.clear();
				TopLevel:
				for (RSNPC m : npcs.getAll()) {
					for (Monster mA : tempMonstersCheck) {
						if (m.getName() == mA.getName() && m.getID() == mA.getID()) {
							continue TopLevel;
						}
					}
					for (Monster mA : tempMonsters) {
						if (m.getName() == mA.getName() && m.getID() == mA.getID()) {
							continue TopLevel;
						}
					}
					tempMonstersCheck.add(new Monster(m.getID(), m.getName(), m.getLevel()));
				}
				updateScreen();
			}

			public void add() {
				String s = (String) list1.getSelectedValue();
				if (s == null || s == "") {
					return;
				}
				int randomNumber = 1;
				TopLevel:
				for (Monster m : tempMonstersCheck) {
					for (Monster mA : tempMonsters) {
						if (m.getName() == mA.getName() && m.getID() == mA.getID()) {
							randomNumber++;
							continue TopLevel;
						}
					}
					if (s.contains(randomNumber + ". " + m.getName() + " (Level: " + m.getLevel() + ") (ID: " + m.getID() + ")")) {
						tempMonsters.add(m);
						tempMonstersCheck.remove(randomNumber - 1);
						updateScreen();
						return;

					}
					randomNumber++;
				}
			}

			public void delete() {
				String s = (String) list2.getSelectedValue();
				if (s == null || s == "") {
					return;
				}
				int randomNumber = 1;
				for (Monster m : tempMonsters) {
					boolean onlyDelete = false;
					for (Monster mA : tempMonstersCheck) {
						if (m.getName() == mA.getName() && m.getID() == mA.getID()) {
							onlyDelete = true;
						}
					}
					if (s.contains(randomNumber + ". " + m.getName() + " (Level: " + m.getLevel() + ") (ID: " + m.getID() + ")")) {
						if (onlyDelete) {
							tempMonsters.remove(randomNumber - 1);

						} else {
							tempMonstersCheck.add(m);
							tempMonsters.remove(randomNumber - 1);
						}
						updateScreen();
						return;
					}
					randomNumber++;
				}
			}

			NPCGUISettings npcGUISettings = new NPCGUISettings();

			public class NPCGUISettings {

				public void loadNPCSettings(String fileName) {
					try {
						FileInputStream fstream = new FileInputStream("MiamiFighter_Settings/NPC_Settings/" + fileName + ".txt");
						DataInputStream in = new DataInputStream(fstream);
						BufferedReader br = new BufferedReader(new InputStreamReader(in));
						ArrayList<Monster> allMonsters = new ArrayList<Monster>();
						String line;
						while ((line = br.readLine()) != null) {
							try {
								int ID = method.getValue(method.getValueLine(line, "<NPCID>")[0]);
								String name = method.getValueLine(line, "<NPCName>")[0];
								int level = method.getValue(method.getValueLine(line, "<NPCLevel>")[0]);
								allMonsters.add(new Monster(ID, name, level));
							} catch (Exception e) {
							}
						}
						tempMonsters = allMonsters;
						tempMonstersCheck = new ArrayList<Monster>();
						npcGUI.updateScreen();
						in.close();
					} catch (Exception e) {

					}
				}

				public void saveNPCSettings(String fileName) {
					try {
						ArrayList<String> finalSavingThings = new ArrayList<String>();
						for (Monster m : tempMonsters) {
							finalSavingThings.add("<NPCID>(" + m.getID() + ")<NPCName>(" + m.getName() + ")<NPCLevel>(" + m.getLevel() + ")");
						}
						BufferedWriter writer = new BufferedWriter(new FileWriter("MiamiFighter_Settings/NPC_Settings/" + fileName + ".txt"));
						int i = 1;
						for (String s : finalSavingThings) {
							writer.write(s);
							if (i < finalSavingThings.size()) {
								writer.newLine();
							}
							i++;
						}
						writer.close();
					} catch (Exception e) {

					}
				}
			}

		}


		PathMakerGUI pathMakerGUI = new PathMakerGUI();

		public class PathMakerGUI {
			ArrayList<PathPart> theTempPath = new ArrayList<PathPart>();

			public void openTeleportationPathPart() {
				new MiamiFighterGUITeleportation().setVisible(true);
			}

			public void openPathMakerPathPart() {
				new MiamiFighterGUIPath().setVisible(true);
			}

			public void openObjectNPCPathPart() {
				new MiamiFighterGUIObjectNPC().setVisible(true);
			}

			public void openInterfacesPathPart() {
				new MiamiFighterGUIInterfaces().setVisible(true);
			}

			public void updateSettingsPathMaker() {
				ArrayList<String> pathMakerNames = new ArrayList<String>();
				pathMakerNames.add("<Default>");
				File folder = new File("MiamiFighter_Settings/PathMaker_Settings/");
				File[] listOfFiles = folder.listFiles();
				if (listOfFiles != null) {
					for (int i = 0; i < listOfFiles.length; i++) {
						if (listOfFiles[i].isFile()) {
							String fullName = listOfFiles[i].getName();
							int charLocation = 0;
							for (int a = 0; a < fullName.toCharArray().length; a++) {
								if (fullName.toCharArray()[a] == '.') {
									charLocation = a;
								}
							}
							String newName = "";
							for (int a = 0; a < fullName.toCharArray().length; a++) {
								if (a < charLocation) {
									newName += fullName.toCharArray()[a];
								}
							}
							pathMakerNames.add(newName);
						}
					}
				}
				String[] finalString = new String[pathMakerNames.size()];
				for (int i = 0; i < pathMakerNames.size(); i++) {
					finalString[i] = pathMakerNames.get(i);
				}
				comboBox4.setModel(new DefaultComboBoxModel(finalString));

				comboBox5.setModel(new DefaultComboBoxModel(finalString));

				comboBox2.setModel(new DefaultComboBoxModel(finalString));
			}

			public void updateScreen() {
				ArrayList<String> s = new ArrayList<String>();
				int randomNumber = 1;
				for (PathPart part : theTempPath) {
					if (part.useTeleportation) {
						s.add("Part " + randomNumber + ": Uses Teleportation Method - " + pathLibrary.TELEPORTATION_METHOD_NAME[part.teleportationMethod] + ", Option: " + part.teleportationOption);
					} else if (part.usePath) {
						String pathS = "";
						int randomNumbera = 1;
						for (RSTile tile : part.path) {
							pathS += " RSTile(" + tile.getX() + ", " + tile.getY() + ")";
							if (randomNumbera > 3) {
								break;
							}
							randomNumbera++;
						}
						s.add("Part " + randomNumber + ": Path -" + pathS);
					} else if (part.useObject) {
						s.add("Part " + randomNumber + ": Object ID - " + part.objectID + ", Action - " + part.action);
					} else if (part.useNPC) {
						s.add("Part " + randomNumber + ": NPC ID - " + part.npcID + ", Action - " + part.action);
					} else if (part.useInterfaceWait) {
						s.add("Part " + randomNumber + ": Wait when Interface(" + part.interfaceID + ", " + part.childInterfaceID +
								") is Valid. Wait for: " + part.interfaceTimeToWait + " seconds.");
					} else if (part.useChildInterfaceClick) {
						s.add("Part " + randomNumber + ": Click when Interface(" + part.interfaceID + ", " + part.childInterfaceID +
								") is Valid. When clicked wait for: " + part.interfaceTimeToWait + " seconds.");
					} else if (part.useComponentInterfaceClick) {
						s.add("Part " + randomNumber + ": Click when Interface(" + part.interfaceID + ", " + part.childInterfaceID +
								", " + part.componentInterfaceID + ") is Valid. When clicked wait for: " + part.interfaceTimeToWait + " seconds.");
					}
					randomNumber++;
				}
				String[] theNewStrings = new String[s.size()];
				for (int i = 0; i < s.size(); i++) {
					theNewStrings[i] = s.get(i);
				}
				final String[] array = theTempPath.size() > 0 ? theNewStrings : new String[]{"Nothing"};
				list3.setModel(new AbstractListModel() {
					private static final long serialVersionUID = 1L;
					String[] values = array;

					public int getSize() {
						return values.length;
					}

					public Object getElementAt(int i) {
						return values[i];
					}
				});
			}


			public void addPathPart(PathPart part) {
				theTempPath.add(part);
				updateScreen();
			}

			public void delete() {
				String s = (String) list3.getSelectedValue();
				if (s == null || s == "") {
					return;
				}
				for (int i = 1; i <= theTempPath.size(); i++) {
					if (s.contains("Part " + i + ":")) {
						theTempPath.remove(i - 1);
						break;
					}
				}
				updateScreen();
			}


			public class MiamiFighterGUITeleportation extends JFrame {
				private static final long serialVersionUID = 1L;

				public MiamiFighterGUITeleportation() {
					initComponents();
				}

				private void button24ActionPerformed(ActionEvent e) {
					int teleportingMethod = 0;
					int teleportationOption = 0;
					String s = (String) comboBox7.getSelectedItem();
					if (s.equals("Teleport using runes")) {
						teleportingMethod = PathMakerLibrary.TELEPORTATION_METHOD_RUNES;
					} else if (s.equals("Teleport using tabs")) {
						teleportingMethod = PathMakerLibrary.TELEPORTATION_METHOD_TABS;
					} else if (s.equals("Game Necklace")) {
						teleportingMethod = PathMakerLibrary.TELEPORTATION_METHOD_GAME_NECKLACE;
					} else if (s.equals("Ring of Duelling")) {
						teleportingMethod = PathMakerLibrary.TELEPORTATION_METHOD_RING_OF_DUELLING;
					} else if (s.equals("Slayer ring")) {
						teleportingMethod = PathMakerLibrary.TELEPORTATION_METHOD_SLAYER_RING;
					} else if (s.equals("Amulet of Glory")) {
						teleportingMethod = PathMakerLibrary.TELEPORTATION_METHOD_AMULET_OF_GLORY;
					} else {
						log("Must select method");
						return;
					}
					teleportationOption = method.getValue(spinner1.getValue().toString());
					if (teleportationOption == 0) {
						log("You forgot the option");
						return;
					}
					addPathPart(new PathPart(teleportingMethod, teleportationOption));
					dispose();
					return;

				}

				private void initComponents() {
					// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
					scrollPane24 = new JScrollPane();
					panel25 = new JPanel();
					scrollPane25 = new JScrollPane();
					textArea1 = new JTextArea();
					scrollPane31 = new JScrollPane();
					panel30 = new JPanel();
					button24 = new JButton();
					comboBox7 = new JComboBox();
					textField38 = new JTextField();
					textField39 = new JTextField();
					spinner1 = new JSpinner();

					//======== this ========
					setTitle("Path Part - Teleportation:");
					setResizable(false);
					Container contentPane = getContentPane();
					contentPane.setLayout(null);

					//======== scrollPane24 ========
					{
						scrollPane24.setForeground(UIManager.getColor("Button.background"));
						scrollPane24.setBorder(new TitledBorder(null, "Path Part - Teleportation:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
								new Font("Calibri", Font.PLAIN, 12), Color.blue));

						//======== panel25 ========
						{
							panel25.setLayout(null);

							//======== scrollPane25 ========
							{

								//---- textArea1 ----
								textArea1.setText("Teleportation Options:\nGame Necklace To Games Room = 1;\nGame Necklace To Babarian Outpost = 2;\nGame Necklace To Clan Wars = 3;\nGame Necklace To Wilderness Volcano = 4;\n\nRing of Duelling To Duel Arena = 1;\nRing of Duelling To Castle Wars = 2;\nRing of Duelling To Mobilising Armies = 3;\nRing of Duelling To Fist of Guthix = 4;\n\nSlayer Ring To Pollnivneach = 1;\nSlayer Ring To Slayer Tower = 2;\nSlayer Ring To Slayer Dungeon = 3;\nSlayer Ring To The Lair of Tarn = 4;\n\nAmulet of Glory To Edgvillage\nAmulet of Glory To Karamja\nAmulet of Glory To Draynor Village\nAmulet of Glory To Alkharid\n\nNormal Spells\nSpell Home teleport = 24\nSpell Mobilising armies teleport = 37\nSpell Varrock teleport = 40\nSpell Lumbridge teleport = 43\nSpell Falador teleport = 46\nSpell Teleport to House = 48\nSpell Camelot teleport = 51\nSpell Ardougne teleport = 57\nSpell Watchtower teleport = 62\nSpell Trollheim teleport = 69\nSpell Ape Atol teleport = 72\n\nAncient Spells:\nSpell Ancient Paddewwa Teleport = 40;\nSpell Ancient Senntisten Teleport = 41;\nSpell Ancient Kharyll Teleport = 42;\nSpell Ancient Lasser Teleport = 43;\nSpell Ancient Dareyak Teleport = 44;\nSpell Ancient Carrallanger teleport = 45;\nSpell Ancient Annakarl teleport = 46;\nSpell Ancient Ghorrock teleport = 47;\nSpell Ancient Home teleport = 48;\n\nSpell Lunar Barbarian teleport = 22\nSpell Lunar Home teleport = 38\nSpell Lunar Fishing guild teleport = 39\nSpell Lunar Khazard teleport = 40\nSpell Lunar Moonclan teleport = 42\nSpell Lunar Catherby teleport = 43\nSpell Lunar Waterbirth teleport = 46\nSpell Lunar Ice plateau teleport = 50");
								textArea1.setEditable(false);
								scrollPane25.setViewportView(textArea1);
							}
							panel25.add(scrollPane25);
							scrollPane25.setBounds(10, 5, 360, 105);

							//======== scrollPane31 ========
							{
								scrollPane31.setForeground(UIManager.getColor("Button.background"));
								scrollPane31.setBorder(new TitledBorder(null, "Add Teleport:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										new Font("Calibri", Font.PLAIN, 12), Color.blue));

								//======== panel30 ========
								{
									panel30.setLayout(null);

									//---- button24 ----
									button24.setText("Add Teleport path part");
									button24.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											button24ActionPerformed(e);
										}
									});
									panel30.add(button24);
									button24.setBounds(155, 55, 160, 25);

									//---- comboBox7 ----
									comboBox7.setModel(new DefaultComboBoxModel(new String[]{
											"Teleportation Method: ",
											"Teleport using runes",
											"Teleport using tabs",
											"Game Necklace",
											"Ring of Duelling",
											"Slayer ring",
											"Amulet of Glory"
									}));
									panel30.add(comboBox7);
									comboBox7.setBounds(155, 5, 160, 20);

									//---- textField38 ----
									textField38.setText("Teleportation Method:");
									textField38.setBackground(new Color(240, 240, 240));
									textField38.setCaretColor(new Color(240, 240, 240));
									textField38.setBorder(null);
									textField38.setEditable(false);
									textField38.setFont(new Font("Calibri", Font.BOLD, 13));
									panel30.add(textField38);
									textField38.setBounds(5, 5, 140, 20);

									//---- textField39 ----
									textField39.setText("Teleportation Option:");
									textField39.setBackground(new Color(240, 240, 240));
									textField39.setCaretColor(new Color(240, 240, 240));
									textField39.setBorder(null);
									textField39.setEditable(false);
									textField39.setFont(new Font("Calibri", Font.BOLD, 13));
									panel30.add(textField39);
									textField39.setBounds(5, 30, 135, 20);
									panel30.add(spinner1);
									spinner1.setBounds(155, 30, 160, spinner1.getPreferredSize().height);

									{ // compute preferred size
										Dimension preferredSize = new Dimension();
										for (int i = 0; i < panel30.getComponentCount(); i++) {
											Rectangle bounds = panel30.getComponent(i).getBounds();
											preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
											preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
										}
										Insets insets = panel30.getInsets();
										preferredSize.width += insets.right;
										preferredSize.height += insets.bottom;
										panel30.setMinimumSize(preferredSize);
										panel30.setPreferredSize(preferredSize);
									}
								}
								scrollPane31.setViewportView(panel30);
							}
							panel25.add(scrollPane31);
							scrollPane31.setBounds(5, 110, 370, 115);

							{ // compute preferred size
								Dimension preferredSize = new Dimension();
								for (int i = 0; i < panel25.getComponentCount(); i++) {
									Rectangle bounds = panel25.getComponent(i).getBounds();
									preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
									preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
								}
								Insets insets = panel25.getInsets();
								preferredSize.width += insets.right;
								preferredSize.height += insets.bottom;
								panel25.setMinimumSize(preferredSize);
								panel25.setPreferredSize(preferredSize);
							}
						}
						scrollPane24.setViewportView(panel25);
					}
					contentPane.add(scrollPane24);
					scrollPane24.setBounds(5, 5, 390, 255);

					contentPane.setPreferredSize(new Dimension(415, 300));
					pack();
					setLocationRelativeTo(getOwner());
					// JFormDesigner - End of component initialization  //GEN-END:initComponents
				}

				// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
				private JScrollPane scrollPane24;
				private JPanel panel25;
				private JScrollPane scrollPane25;
				private JTextArea textArea1;
				private JScrollPane scrollPane31;
				private JPanel panel30;
				private JButton button24;
				private JComboBox comboBox7;
				private JTextField textField38;
				private JTextField textField39;
				private JSpinner spinner1;
				// JFormDesigner - End of variables declaration  //GEN-END:variables
			}

			public class MiamiFighterGUIPath extends JFrame {
				private static final long serialVersionUID = 1L;
				ArrayList<RSTile> path = new ArrayList<RSTile>();

				private void updateScreen() {
					String[] s = new String[path.size()];
					for (int i = 0; i < path.size(); i++) {
						s[i] = (i + 1) + ". RSTile(" + path.get(i).getX() + ", " + path.get(i).getY() + ")";
					}
					final String[] array = s.length > 0 ? s : new String[]{"Nothing"};
					list6.setModel(new AbstractListModel() {
						private static final long serialVersionUID = 1L;
						String[] values = array;

						public int getSize() {
							return values.length;
						}

						public Object getElementAt(int i) {
							return values[i];
						}
					});
				}

				private void delete() {
					String s = (String) list6.getSelectedValue();
					if (s == null || s == "") {
						return;
					}
					for (int i = 0; i < path.size(); i++) {
						if (s.contains((i + 1) + ". RSTile(" + path.get(i).getX() + ", " + path.get(i).getY() + ")")) {
							path.remove(i);
							break;
						}
					}
					updateScreen();
				}

				private void addTile() {
					if (game.isLoggedIn()) {
						path.add(getMyPlayer().getLocation());
						updateScreen();
					} else {
						log("You have to log in");
					}
				}

				private void clearPath() {
					path.clear();
					updateScreen();
				}

				public MiamiFighterGUIPath() {
					initComponents();
				}

				private void button25ActionPerformed(ActionEvent e) {
					addTile();
				}

				private void button28ActionPerformed(ActionEvent e) {
					if (path.size() <= 0) {
						log("No tiles are in your  path");
						return;
					}
					RSTile[] submitPath = new RSTile[path.size()];
					for (int t = 0; t < path.size(); t++) {
						submitPath[t] = path.get(t);
					}
					addPathPart(new PathPart(submitPath));
					dispose();
				}

				private void button29ActionPerformed(ActionEvent e) {
					clearPath();
				}

				private void list6ValueChanged(ListSelectionEvent e) {
					delete();
				}

				private void initComponents() {
					// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
					scrollPane32 = new JScrollPane();
					panel31 = new JPanel();
					scrollPane36 = new JScrollPane();
					list6 = new JList();
					scrollPane34 = new JScrollPane();
					panel32 = new JPanel();
					button25 = new JButton();
					textField41 = new JTextField();
					scrollPane35 = new JScrollPane();
					panel34 = new JPanel();
					button28 = new JButton();
					textField42 = new JTextField();
					textField43 = new JTextField();
					button29 = new JButton();
					textArea17 = new JTextArea();

					//======== this ========
					setResizable(false);
					setTitle("Path Part - Path");
					Container contentPane = getContentPane();
					contentPane.setLayout(null);

					//======== scrollPane32 ========
					{
						scrollPane32.setForeground(UIManager.getColor("Button.background"));
						scrollPane32.setBorder(new TitledBorder(null, "Path Part - Path", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
								new Font("Calibri", Font.PLAIN, 12), Color.blue));

						//======== panel31 ========
						{
							panel31.setLayout(null);

							//======== scrollPane36 ========
							{
								scrollPane36.setForeground(UIManager.getColor("Button.background"));
								scrollPane36.setBorder(new TitledBorder(null, "The Actual Path", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										new Font("Calibri", Font.PLAIN, 12), Color.blue));

								//---- list6 ----
								list6.setBackground(UIManager.getColor("Button.background"));
								list6.setModel(new AbstractListModel() {

									private static final long serialVersionUID = 1L;

									String[] values = {
											"Nothing"
									};

									public int getSize() {
										return values.length;
									}

									public Object getElementAt(int i) {
										return values[i];
									}
								});
								list6.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								list6.addListSelectionListener(new ListSelectionListener() {
									public void valueChanged(ListSelectionEvent e) {
										list6ValueChanged(e);
									}
								});
								scrollPane36.setViewportView(list6);
							}
							panel31.add(scrollPane36);
							scrollPane36.setBounds(285, 0, 225, 200);

							//======== scrollPane34 ========
							{
								scrollPane34.setForeground(UIManager.getColor("Button.background"));
								scrollPane34.setBorder(new TitledBorder(null, "Add Tile:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										new Font("Calibri", Font.PLAIN, 12), Color.blue));

								//======== panel32 ========
								{
									panel32.setLayout(null);

									//---- button25 ----
									button25.setText("Add Tile");
									button25.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											button25ActionPerformed(e);
										}
									});
									panel32.add(button25);
									button25.setBounds(145, 0, 100, 25);

									//---- textField41 ----
									textField41.setText("Add current tile:");
									textField41.setBackground(new Color(240, 240, 240));
									textField41.setCaretColor(new Color(240, 240, 240));
									textField41.setBorder(null);
									textField41.setEditable(false);
									textField41.setFont(new Font("Calibri", Font.BOLD, 13));
									panel32.add(textField41);
									textField41.setBounds(5, 5, 110, 20);

									{ // compute preferred size
										Dimension preferredSize = new Dimension();
										for (int i = 0; i < panel32.getComponentCount(); i++) {
											Rectangle bounds = panel32.getComponent(i).getBounds();
											preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
											preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
										}
										Insets insets = panel32.getInsets();
										preferredSize.width += insets.right;
										preferredSize.height += insets.bottom;
										panel32.setMinimumSize(preferredSize);
										panel32.setPreferredSize(preferredSize);
									}
								}
								scrollPane34.setViewportView(panel32);
							}
							panel31.add(scrollPane34);
							scrollPane34.setBounds(0, 140, 280, 60);

							//======== scrollPane35 ========
							{
								scrollPane35.setForeground(UIManager.getColor("Button.background"));
								scrollPane35.setBorder(new TitledBorder(null, "General:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										new Font("Calibri", Font.PLAIN, 12), Color.blue));

								//======== panel34 ========
								{
									panel34.setLayout(null);

									//---- button28 ----
									button28.setText("Add Path part");
									button28.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											button28ActionPerformed(e);
										}
									});
									panel34.add(button28);
									button28.setBounds(150, 50, 100, 25);

									//---- textField42 ----
									textField42.setText("Add Path part:");
									textField42.setBackground(new Color(240, 240, 240));
									textField42.setCaretColor(new Color(240, 240, 240));
									textField42.setBorder(null);
									textField42.setEditable(false);
									textField42.setFont(new Font("Calibri", Font.BOLD, 13));
									panel34.add(textField42);
									textField42.setBounds(10, 50, 110, 25);

									//---- textField43 ----
									textField43.setText("Clear current path:");
									textField43.setBackground(new Color(240, 240, 240));
									textField43.setCaretColor(new Color(240, 240, 240));
									textField43.setBorder(null);
									textField43.setEditable(false);
									textField43.setFont(new Font("Calibri", Font.BOLD, 13));
									panel34.add(textField43);
									textField43.setBounds(10, 80, 110, 25);

									//---- button29 ----
									button29.setText("Clear path");
									button29.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											button29ActionPerformed(e);
										}
									});
									panel34.add(button29);
									button29.setBounds(150, 80, 100, 25);

									//---- textArea17 ----
									textArea17.setBackground(UIManager.getColor("Button.background"));
									textArea17.setEditable(false);
									textArea17.setFont(new Font("Calibri", Font.PLAIN, 13));
									textArea17.setText("Here you can create a Path. NOTE: If you \nadded a wrong tile, you must clear your path\nand start over.");
									panel34.add(textArea17);
									textArea17.setBounds(5, -5, 255, 55);

									{ // compute preferred size
										Dimension preferredSize = new Dimension();
										for (int i = 0; i < panel34.getComponentCount(); i++) {
											Rectangle bounds = panel34.getComponent(i).getBounds();
											preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
											preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
										}
										Insets insets = panel34.getInsets();
										preferredSize.width += insets.right;
										preferredSize.height += insets.bottom;
										panel34.setMinimumSize(preferredSize);
										panel34.setPreferredSize(preferredSize);
									}
								}
								scrollPane35.setViewportView(panel34);
							}
							panel31.add(scrollPane35);
							scrollPane35.setBounds(0, 0, 280, 140);

							{ // compute preferred size
								Dimension preferredSize = new Dimension();
								for (int i = 0; i < panel31.getComponentCount(); i++) {
									Rectangle bounds = panel31.getComponent(i).getBounds();
									preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
									preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
								}
								Insets insets = panel31.getInsets();
								preferredSize.width += insets.right;
								preferredSize.height += insets.bottom;
								panel31.setMinimumSize(preferredSize);
								panel31.setPreferredSize(preferredSize);
							}
						}
						scrollPane32.setViewportView(panel31);
					}
					contentPane.add(scrollPane32);
					scrollPane32.setBounds(5, 5, 525, 235);

					contentPane.setPreferredSize(new Dimension(545, 275));
					pack();
					setLocationRelativeTo(getOwner());
					// JFormDesigner - End of component initialization  //GEN-END:initComponents
				}

				// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
				private JScrollPane scrollPane32;
				private JPanel panel31;
				private JScrollPane scrollPane36;
				private JList list6;
				private JScrollPane scrollPane34;
				private JPanel panel32;
				private JButton button25;
				private JTextField textField41;
				private JScrollPane scrollPane35;
				private JPanel panel34;
				private JButton button28;
				private JTextField textField42;
				private JTextField textField43;
				private JButton button29;
				private JTextArea textArea17;
				// JFormDesigner - End of variables declaration  //GEN-END:variables
			}

			public class MiamiFighterGUIObjectNPC extends JFrame {
				private static final long serialVersionUID = 1L;

				public MiamiFighterGUIObjectNPC() {
					initComponents();
				}

				private void button26ActionPerformed(ActionEvent e) {
					addPathPart(new PathPart(method.getValue(spinner1.getValue().toString()), textField45.getText(), true));
					dispose();
				}

				private void button27ActionPerformed(ActionEvent e) {
					addPathPart(new PathPart(method.getValue(spinner2.getValue().toString()), textField49.getText(), false));
					dispose();
				}


				private void initComponents() {
					// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
					scrollPane26 = new JScrollPane();
					panel26 = new JPanel();
					textArea16 = new JTextArea();
					scrollPane27 = new JScrollPane();
					panel27 = new JPanel();
					button26 = new JButton();
					textField45 = new JTextField();
					textField47 = new JTextField();
					textField48 = new JTextField();
					spinner1 = new JSpinner();
					scrollPane28 = new JScrollPane();
					panel28 = new JPanel();
					button27 = new JButton();
					textField49 = new JTextField();
					textField51 = new JTextField();
					textField52 = new JTextField();
					spinner2 = new JSpinner();

					//======== this ========
					setResizable(false);
					setTitle("Path Part - Object/NPC");
					Container contentPane = getContentPane();
					contentPane.setLayout(null);

					//======== scrollPane26 ========
					{
						scrollPane26.setForeground(UIManager.getColor("Button.background"));
						scrollPane26.setBorder(new TitledBorder(null, "Path Part - Object/NPC:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
								new Font("Calibri", Font.PLAIN, 12), Color.blue));

						//======== panel26 ========
						{
							panel26.setLayout(null);

							//---- textArea16 ----
							textArea16.setBackground(UIManager.getColor("Button.background"));
							textArea16.setEditable(false);
							textArea16.setFont(new Font("Calibri", Font.PLAIN, 13));
							textArea16.setText("Here you can add Objects and NPCs to your path. \nNOTE: An Action is like \"Use-quickly Bankbooth\" etc.");
							panel26.add(textArea16);
							textArea16.setBounds(5, 0, 290, 35);

							//======== scrollPane27 ========
							{
								scrollPane27.setForeground(UIManager.getColor("Button.background"));
								scrollPane27.setBorder(new TitledBorder(null, "Object", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										new Font("Calibri", Font.PLAIN, 12), Color.blue));

								//======== panel27 ========
								{
									panel27.setLayout(null);

									//---- button26 ----
									button26.setText("Add Object");
									button26.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											button26ActionPerformed(e);
										}
									});
									panel27.add(button26);
									button26.setBounds(105, 50, 160, 20);
									panel27.add(textField45);
									textField45.setBounds(105, 25, 160, 20);

									//---- textField47 ----
									textField47.setText("Object ID:");
									textField47.setBackground(new Color(240, 240, 240));
									textField47.setCaretColor(new Color(240, 240, 240));
									textField47.setBorder(null);
									textField47.setEditable(false);
									textField47.setFont(new Font("Calibri", Font.BOLD, 13));
									panel27.add(textField47);
									textField47.setBounds(10, 0, 90, 20);

									//---- textField48 ----
									textField48.setText("Object action:");
									textField48.setBackground(new Color(240, 240, 240));
									textField48.setCaretColor(new Color(240, 240, 240));
									textField48.setBorder(null);
									textField48.setEditable(false);
									textField48.setFont(new Font("Calibri", Font.BOLD, 13));
									panel27.add(textField48);
									textField48.setBounds(10, 25, 90, 20);

									//---- spinner1 ----
									spinner1.setModel(new SpinnerNumberModel(0, 0, 1000000, 1));
									panel27.add(spinner1);
									spinner1.setBounds(105, 0, 160, spinner1.getPreferredSize().height);

									{ // compute preferred size
										Dimension preferredSize = new Dimension();
										for (int i = 0; i < panel27.getComponentCount(); i++) {
											Rectangle bounds = panel27.getComponent(i).getBounds();
											preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
											preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
										}
										Insets insets = panel27.getInsets();
										preferredSize.width += insets.right;
										preferredSize.height += insets.bottom;
										panel27.setMinimumSize(preferredSize);
										panel27.setPreferredSize(preferredSize);
									}
								}
								scrollPane27.setViewportView(panel27);
							}
							panel26.add(scrollPane27);
							scrollPane27.setBounds(5, 40, 295, 105);

							//======== scrollPane28 ========
							{
								scrollPane28.setForeground(UIManager.getColor("Button.background"));
								scrollPane28.setBorder(new TitledBorder(null, "NPC", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										new Font("Calibri", Font.PLAIN, 12), Color.blue));

								//======== panel28 ========
								{
									panel28.setLayout(null);

									//---- button27 ----
									button27.setText("Add NPC");
									button27.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											button27ActionPerformed(e);
										}
									});
									panel28.add(button27);
									button27.setBounds(105, 50, 160, 20);
									panel28.add(textField49);
									textField49.setBounds(105, 25, 160, 20);

									//---- textField51 ----
									textField51.setText("NPC ID:");
									textField51.setBackground(new Color(240, 240, 240));
									textField51.setCaretColor(new Color(240, 240, 240));
									textField51.setBorder(null);
									textField51.setEditable(false);
									textField51.setFont(new Font("Calibri", Font.BOLD, 13));
									panel28.add(textField51);
									textField51.setBounds(10, 0, 90, 20);

									//---- textField52 ----
									textField52.setText("NPC action:");
									textField52.setBackground(new Color(240, 240, 240));
									textField52.setCaretColor(new Color(240, 240, 240));
									textField52.setBorder(null);
									textField52.setEditable(false);
									textField52.setFont(new Font("Calibri", Font.BOLD, 13));
									panel28.add(textField52);
									textField52.setBounds(10, 25, 90, 20);

									//---- spinner2 ----
									spinner2.setModel(new SpinnerNumberModel(0, 0, 100000, 1));
									panel28.add(spinner2);
									spinner2.setBounds(105, 0, 160, spinner2.getPreferredSize().height);

									{ // compute preferred size
										Dimension preferredSize = new Dimension();
										for (int i = 0; i < panel28.getComponentCount(); i++) {
											Rectangle bounds = panel28.getComponent(i).getBounds();
											preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
											preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
										}
										Insets insets = panel28.getInsets();
										preferredSize.width += insets.right;
										preferredSize.height += insets.bottom;
										panel28.setMinimumSize(preferredSize);
										panel28.setPreferredSize(preferredSize);
									}
								}
								scrollPane28.setViewportView(panel28);
							}
							panel26.add(scrollPane28);
							scrollPane28.setBounds(5, 150, 295, 105);

							{ // compute preferred size
								Dimension preferredSize = new Dimension();
								for (int i = 0; i < panel26.getComponentCount(); i++) {
									Rectangle bounds = panel26.getComponent(i).getBounds();
									preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
									preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
								}
								Insets insets = panel26.getInsets();
								preferredSize.width += insets.right;
								preferredSize.height += insets.bottom;
								panel26.setMinimumSize(preferredSize);
								panel26.setPreferredSize(preferredSize);
							}
						}
						scrollPane26.setViewportView(panel26);
					}
					contentPane.add(scrollPane26);
					scrollPane26.setBounds(5, 5, 315, 285);

					contentPane.setPreferredSize(new Dimension(335, 320));
					pack();
					setLocationRelativeTo(getOwner());
					// JFormDesigner - End of component initialization  //GEN-END:initComponents
				}

				// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
				private JScrollPane scrollPane26;
				private JPanel panel26;
				private JTextArea textArea16;
				private JScrollPane scrollPane27;
				private JPanel panel27;
				private JButton button26;
				private JTextField textField45;
				private JTextField textField47;
				private JTextField textField48;
				private JSpinner spinner1;
				private JScrollPane scrollPane28;
				private JPanel panel28;
				private JButton button27;
				private JTextField textField49;
				private JTextField textField51;
				private JTextField textField52;
				private JSpinner spinner2;
				// JFormDesigner - End of variables declaration  //GEN-END:variables
			}


			public class MiamiFighterGUIInterfaces extends JFrame {
				private static final long serialVersionUID = 1L;

				public MiamiFighterGUIInterfaces() {
					initComponents();
				}

				private void button32ActionPerformed(ActionEvent e) {
					try {
						int a = method.getValue(spinner1.getValue().toString());
						int b = method.getValue(spinner11.getValue().toString());
						int c = method.getValue(spinner12.getValue().toString());

						addPathPart(new PathPart(a, b, c));
						dispose();
					} catch (Exception avsadf) {
						log("You forgot something");
					}
				}

				private void button33ActionPerformed(ActionEvent e) {
					try {
						int a = method.getValue(spinner2.getValue().toString());
						int b = method.getValue(spinner3.getValue().toString());
						int c = method.getValue(spinner5.getValue().toString());
						String action = textField28.getText();


						addPathPart(new PathPart(a, b, action, c));
						dispose();
					} catch (Exception avsadf) {
						log("You forgot something");
					}
				}

				private void button34ActionPerformed(ActionEvent e) {
					try {
						int a = method.getValue(spinner6.getValue().toString());
						int b = method.getValue(spinner7.getValue().toString());
						int c = method.getValue(spinner8.getValue().toString());
						int d = method.getValue(spinner10.getValue().toString());
						String action = textField29.getText();

						addPathPart(new PathPart(a, b, d, action, c));
						dispose();
					} catch (Exception avsadf) {
						log("You forgot something");
					}
				}

				private void initComponents() {
					// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
					scrollPane37 = new JScrollPane();
					panel35 = new JPanel();
					textArea18 = new JTextArea();
					scrollPane38 = new JScrollPane();
					panel36 = new JPanel();
					textField72 = new JTextField();
					button33 = new JButton();
					textField83 = new JTextField();
					textField84 = new JTextField();
					textField85 = new JTextField();
					spinner2 = new JSpinner();
					spinner3 = new JSpinner();
					spinner5 = new JSpinner();
					textField28 = new JTextField();
					scrollPane40 = new JScrollPane();
					panel38 = new JPanel();
					textField79 = new JTextField();
					textField80 = new JTextField();
					button32 = new JButton();
					textField81 = new JTextField();
					spinner1 = new JSpinner();
					spinner11 = new JSpinner();
					spinner12 = new JSpinner();
					scrollPane39 = new JScrollPane();
					panel37 = new JPanel();
					textField73 = new JTextField();
					button34 = new JButton();
					textField90 = new JTextField();
					textField91 = new JTextField();
					textField92 = new JTextField();
					textField97 = new JTextField();
					spinner6 = new JSpinner();
					spinner7 = new JSpinner();
					spinner8 = new JSpinner();
					spinner10 = new JSpinner();
					textField29 = new JTextField();

					//======== this ========
					setTitle("Path Part - Interfaces");
					Container contentPane = getContentPane();
					contentPane.setLayout(null);

					//======== scrollPane37 ========
					{
						scrollPane37.setForeground(UIManager.getColor("Button.background"));
						scrollPane37.setBorder(new TitledBorder(null, "Path Part - Interfaces:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
								new Font("Calibri", Font.PLAIN, 12), Color.blue));

						//======== panel35 ========
						{
							panel35.setLayout(null);

							//---- textArea18 ----
							textArea18.setBackground(UIManager.getColor("Button.background"));
							textArea18.setEditable(false);
							textArea18.setFont(new Font("Calibri", Font.PLAIN, 13));
							textArea18.setText("Here you can add Objects and NPCs to your path. \nNOTE: An Action is like \"Use-quickly Bankbooth\" etc.");
							panel35.add(textArea18);
							textArea18.setBounds(5, 0, 290, 35);

							//======== scrollPane38 ========
							{
								scrollPane38.setForeground(UIManager.getColor("Button.background"));
								scrollPane38.setBorder(new TitledBorder(null, "Click interface if is valid", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										new Font("Calibri", Font.PLAIN, 12), Color.blue));

								//======== panel36 ========
								{
									panel36.setLayout(null);

									//---- textField72 ----
									textField72.setText("Action:");
									textField72.setBackground(new Color(240, 240, 240));
									textField72.setCaretColor(new Color(240, 240, 240));
									textField72.setBorder(null);
									textField72.setEditable(false);
									textField72.setFont(new Font("Calibri", Font.BOLD, 13));
									panel36.add(textField72);
									textField72.setBounds(5, 50, 90, 20);

									//---- button33 ----
									button33.setText("Add Event:");
									button33.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											button33ActionPerformed(e);
										}
									});
									panel36.add(button33);
									button33.setBounds(5, 100, 160, 20);

									//---- textField83 ----
									textField83.setText("Time to wait(Seconds):");
									textField83.setBackground(new Color(240, 240, 240));
									textField83.setCaretColor(new Color(240, 240, 240));
									textField83.setBorder(null);
									textField83.setEditable(false);
									textField83.setFont(new Font("Calibri", Font.BOLD, 13));
									panel36.add(textField83);
									textField83.setBounds(5, 75, 155, 20);

									//---- textField84 ----
									textField84.setText("Child Interface ID:");
									textField84.setBackground(new Color(240, 240, 240));
									textField84.setCaretColor(new Color(240, 240, 240));
									textField84.setBorder(null);
									textField84.setEditable(false);
									textField84.setFont(new Font("Calibri", Font.BOLD, 13));
									panel36.add(textField84);
									textField84.setBounds(5, 25, 110, 20);

									//---- textField85 ----
									textField85.setText("Interface ID:");
									textField85.setBackground(new Color(240, 240, 240));
									textField85.setCaretColor(new Color(240, 240, 240));
									textField85.setBorder(null);
									textField85.setEditable(false);
									textField85.setFont(new Font("Calibri", Font.BOLD, 13));
									panel36.add(textField85);
									textField85.setBounds(5, 0, 110, 20);
									panel36.add(spinner2);
									spinner2.setBounds(180, 0, 90, 20);
									panel36.add(spinner3);
									spinner3.setBounds(180, 25, 90, 20);
									panel36.add(spinner5);
									spinner5.setBounds(180, 75, 90, 20);
									panel36.add(textField28);
									textField28.setBounds(180, 50, 90, 20);

									{ // compute preferred size
										Dimension preferredSize = new Dimension();
										for (int i = 0; i < panel36.getComponentCount(); i++) {
											Rectangle bounds = panel36.getComponent(i).getBounds();
											preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
											preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
										}
										Insets insets = panel36.getInsets();
										preferredSize.width += insets.right;
										preferredSize.height += insets.bottom;
										panel36.setMinimumSize(preferredSize);
										panel36.setPreferredSize(preferredSize);
									}
								}
								scrollPane38.setViewportView(panel36);
							}
							panel35.add(scrollPane38);
							scrollPane38.setBounds(5, 170, 295, 150);

							//======== scrollPane40 ========
							{
								scrollPane40.setForeground(UIManager.getColor("Button.background"));
								scrollPane40.setBorder(new TitledBorder(null, "Wait if interface is valid:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										new Font("Calibri", Font.PLAIN, 12), Color.blue));

								//======== panel38 ========
								{
									panel38.setLayout(null);

									//---- textField79 ----
									textField79.setText("Interface ID:");
									textField79.setBackground(new Color(240, 240, 240));
									textField79.setCaretColor(new Color(240, 240, 240));
									textField79.setBorder(null);
									textField79.setEditable(false);
									textField79.setFont(new Font("Calibri", Font.BOLD, 13));
									panel38.add(textField79);
									textField79.setBounds(5, 0, 110, 20);

									//---- textField80 ----
									textField80.setText("Child Interface ID:");
									textField80.setBackground(new Color(240, 240, 240));
									textField80.setCaretColor(new Color(240, 240, 240));
									textField80.setBorder(null);
									textField80.setEditable(false);
									textField80.setFont(new Font("Calibri", Font.BOLD, 13));
									panel38.add(textField80);
									textField80.setBounds(5, 25, 110, 20);

									//---- button32 ----
									button32.setText("Add Event:");
									button32.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											button32ActionPerformed(e);
										}
									});
									panel38.add(button32);
									button32.setBounds(0, 75, 160, 20);

									//---- textField81 ----
									textField81.setText("Time to wait(Seconds):");
									textField81.setBackground(new Color(240, 240, 240));
									textField81.setCaretColor(new Color(240, 240, 240));
									textField81.setBorder(null);
									textField81.setEditable(false);
									textField81.setFont(new Font("Calibri", Font.BOLD, 13));
									panel38.add(textField81);
									textField81.setBounds(5, 50, 155, 20);
									panel38.add(spinner1);
									spinner1.setBounds(180, 0, 90, spinner1.getPreferredSize().height);
									panel38.add(spinner11);
									spinner11.setBounds(180, 25, 90, 20);
									panel38.add(spinner12);
									spinner12.setBounds(180, 50, 90, 20);

									{ // compute preferred size
										Dimension preferredSize = new Dimension();
										for (int i = 0; i < panel38.getComponentCount(); i++) {
											Rectangle bounds = panel38.getComponent(i).getBounds();
											preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
											preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
										}
										Insets insets = panel38.getInsets();
										preferredSize.width += insets.right;
										preferredSize.height += insets.bottom;
										panel38.setMinimumSize(preferredSize);
										panel38.setPreferredSize(preferredSize);
									}
								}
								scrollPane40.setViewportView(panel38);
							}
							panel35.add(scrollPane40);
							scrollPane40.setBounds(5, 40, 295, 130);

							//======== scrollPane39 ========
							{
								scrollPane39.setForeground(UIManager.getColor("Button.background"));
								scrollPane39.setBorder(new TitledBorder(null, "Click interface if is valid", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										new Font("Calibri", Font.PLAIN, 12), Color.blue));

								//======== panel37 ========
								{
									panel37.setLayout(null);

									//---- textField73 ----
									textField73.setText("Action:");
									textField73.setBackground(new Color(240, 240, 240));
									textField73.setCaretColor(new Color(240, 240, 240));
									textField73.setBorder(null);
									textField73.setEditable(false);
									textField73.setFont(new Font("Calibri", Font.BOLD, 13));
									panel37.add(textField73);
									textField73.setBounds(5, 75, 90, 20);

									//---- button34 ----
									button34.setText("Add Event:");
									button34.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											button34ActionPerformed(e);
										}
									});
									panel37.add(button34);
									button34.setBounds(10, 125, 160, 20);

									//---- textField90 ----
									textField90.setText("Time to wait(Seconds):");
									textField90.setBackground(new Color(240, 240, 240));
									textField90.setCaretColor(new Color(240, 240, 240));
									textField90.setBorder(null);
									textField90.setEditable(false);
									textField90.setFont(new Font("Calibri", Font.BOLD, 13));
									panel37.add(textField90);
									textField90.setBounds(5, 100, 155, 20);

									//---- textField91 ----
									textField91.setText("Child Interface ID:");
									textField91.setBackground(new Color(240, 240, 240));
									textField91.setCaretColor(new Color(240, 240, 240));
									textField91.setBorder(null);
									textField91.setEditable(false);
									textField91.setFont(new Font("Calibri", Font.BOLD, 13));
									panel37.add(textField91);
									textField91.setBounds(5, 25, 110, 20);

									//---- textField92 ----
									textField92.setText("Interface ID:");
									textField92.setBackground(new Color(240, 240, 240));
									textField92.setCaretColor(new Color(240, 240, 240));
									textField92.setBorder(null);
									textField92.setEditable(false);
									textField92.setFont(new Font("Calibri", Font.BOLD, 13));
									panel37.add(textField92);
									textField92.setBounds(5, 0, 110, 20);

									//---- textField97 ----
									textField97.setText("Component interface ID:");
									textField97.setBackground(new Color(240, 240, 240));
									textField97.setCaretColor(new Color(240, 240, 240));
									textField97.setBorder(null);
									textField97.setEditable(false);
									textField97.setFont(new Font("Calibri", Font.BOLD, 13));
									panel37.add(textField97);
									textField97.setBounds(5, 50, 155, 20);
									panel37.add(spinner6);
									spinner6.setBounds(180, 0, 90, 20);
									panel37.add(spinner7);
									spinner7.setBounds(180, 25, 90, 20);
									panel37.add(spinner8);
									spinner8.setBounds(180, 50, 90, 20);
									panel37.add(spinner10);
									spinner10.setBounds(180, 100, 90, 20);
									panel37.add(textField29);
									textField29.setBounds(180, 75, 90, 20);

									{ // compute preferred size
										Dimension preferredSize = new Dimension();
										for (int i = 0; i < panel37.getComponentCount(); i++) {
											Rectangle bounds = panel37.getComponent(i).getBounds();
											preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
											preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
										}
										Insets insets = panel37.getInsets();
										preferredSize.width += insets.right;
										preferredSize.height += insets.bottom;
										panel37.setMinimumSize(preferredSize);
										panel37.setPreferredSize(preferredSize);
									}
								}
								scrollPane39.setViewportView(panel37);
							}
							panel35.add(scrollPane39);
							scrollPane39.setBounds(5, 320, 295, 175);

							{ // compute preferred size
								Dimension preferredSize = new Dimension();
								for (int i = 0; i < panel35.getComponentCount(); i++) {
									Rectangle bounds = panel35.getComponent(i).getBounds();
									preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
									preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
								}
								Insets insets = panel35.getInsets();
								preferredSize.width += insets.right;
								preferredSize.height += insets.bottom;
								panel35.setMinimumSize(preferredSize);
								panel35.setPreferredSize(preferredSize);
							}
						}
						scrollPane37.setViewportView(panel35);
					}
					contentPane.add(scrollPane37);
					scrollPane37.setBounds(5, 5, 315, 525);

					contentPane.setPreferredSize(new Dimension(335, 565));
					pack();
					setLocationRelativeTo(getOwner());
					// JFormDesigner - End of component initialization  //GEN-END:initComponents
				}

				// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
				private JScrollPane scrollPane37;
				private JPanel panel35;
				private JTextArea textArea18;
				private JScrollPane scrollPane38;
				private JPanel panel36;
				private JTextField textField72;
				private JButton button33;
				private JTextField textField83;
				private JTextField textField84;
				private JTextField textField85;
				private JSpinner spinner2;
				private JSpinner spinner3;
				private JSpinner spinner5;
				private JTextField textField28;
				private JScrollPane scrollPane40;
				private JPanel panel38;
				private JTextField textField79;
				private JTextField textField80;
				private JButton button32;
				private JTextField textField81;
				private JSpinner spinner1;
				private JSpinner spinner11;
				private JSpinner spinner12;
				private JScrollPane scrollPane39;
				private JPanel panel37;
				private JTextField textField73;
				private JButton button34;
				private JTextField textField90;
				private JTextField textField91;
				private JTextField textField92;
				private JTextField textField97;
				private JSpinner spinner6;
				private JSpinner spinner7;
				private JSpinner spinner8;
				private JSpinner spinner10;
				private JTextField textField29;
				// JFormDesigner - End of variables declaration  //GEN-END:variables
			}

			MiamiFighterGUIPathMakerSettings miamiFighterGuiPathMakerSettings = new MiamiFighterGUIPathMakerSettings();

			public class MiamiFighterGUIPathMakerSettings {
				public void savePathMakerSettings(String nameOfFile) {
					ArrayList<String> finalSavingThings = new ArrayList<String>();
					for (PathPart part : theTempPath) {
						String s = "";
						if (part.usePath) {
							s += "<Path>";
							for (RSTile t : part.path) {
								s += "<Tile>(" + t.getX() + ", " + t.getY() + ")";
							}

						} else if (part.useInterfaceWait || part.useChildInterfaceClick || part.useComponentInterfaceClick) {
							s += "<Interface><InterfaceID>(" + part.interfaceID + ")<ChildInterfaceID>(" + part.childInterfaceID + ")";
							if (part.useComponentInterfaceClick) {
								s += "<ComponentInterfaceID>(" + part.componentInterfaceID + ")<Action>(" + part.action + ")<Wait>(" + part.interfaceTimeToWait + ")";
							} else if (part.useChildInterfaceClick) {
								s += "<Action>(" + part.action + ")<Wait>(" + part.interfaceTimeToWait + ")";
							} else if (part.useInterfaceWait) {
								s += "<Wait>(" + part.interfaceTimeToWait + ")";
							}
						} else if (part.useNPC) {
							s += "<NPC>";
							s += "<ID>(" + part.npcID + ")<Action>(" + part.action + ")";
						} else if (part.useObject) {
							s += "<Object>";
							s += "<ID>(" + part.objectID + ")<Action>(" + part.action + ")";
						} else if (part.useTeleportation) {
							s += "<Teleportation><Method>(" + part.teleportationMethod + ")<Option>(" + part.teleportationOption + ")";
						}
						finalSavingThings.add(s);
					}
					try {

						BufferedWriter writer = new BufferedWriter(new FileWriter("MiamiFighter_Settings/PathMaker_Settings/" + nameOfFile + ".txt"));
						int i = 1;
						for (String s : finalSavingThings) {
							writer.write(s);
							if (i < finalSavingThings.size()) {
								writer.newLine();
							}
							i++;
						}
						writer.close();
					} catch (Exception e) {

					}

				}

				public PathPart[] loadPathParts(String nameOfFile) {
					try {
						FileInputStream fstream = new FileInputStream("MiamiFighter_Settings/PathMaker_Settings/" + nameOfFile + ".txt");
						DataInputStream in = new DataInputStream(fstream);
						BufferedReader br = new BufferedReader(new InputStreamReader(in));

						ArrayList<PathPart> thePath = new ArrayList<PathPart>();
						String line;
						while ((line = br.readLine()) != null) {
							try {
								if (line.contains("<Path>")) {
									String[] array = method.getValueLine(line, "<Tile>");
									RSTile[] path = new RSTile[array.length / 2];
									for (int i = 0; i + 1 < array.length; i += 2) {
										path[i / 2] = new RSTile(method.getValue(array[i]), method.getValue(array[i + 1]));
									}
									PathPart tempPart = new PathPart(path);
									if (tempPart != null) {
										thePath.add(tempPart);
									}
								} else if (line.contains("<Object>")) {
									PathPart tempPart = new PathPart(method.getValue(method.getValueLine(line, "<ID>")[0]), method.getValueLine(line, "<Action>")[0], true);
									if (tempPart != null) {
										thePath.add(tempPart);
									}
								} else if (line.contains("<NPC>")) {
									PathPart tempPart = new PathPart(method.getValue(method.getValueLine(line, "<ID>")[0]), method.getValueLine(line, "<Action>")[0], false);
									if (tempPart != null) {
										thePath.add(tempPart);
									}
								} else if (line.contains("<Interface>")) {
									int interfaceID = method.getValue(method.getValueLine(line, "<InterfaceID>")[0]);
									int childInterfaceID = method.getValue(method.getValueLine(line, "<ChildInterfaceID>")[0]);
									int sleep = method.getValue(method.getValueLine(line, "<Wait>")[0]);

									if (method.getValueLine(line, "<ComponentInterfaceID>").length > 1) {
										thePath.add(new PathPart(interfaceID, childInterfaceID, method.getValue(method.getValueLine(line, "<ComponentInterfaceID>")[0]), method.getValueLine(line, "<Action>")[0], sleep));
									} else if (method.getValueLine(line, "<Action>").length > 1) {
										thePath.add(new PathPart(interfaceID, childInterfaceID, method.getValueLine(line, "<Action>")[0], sleep));
									} else {
										thePath.add(new PathPart(interfaceID, childInterfaceID, sleep));
									}
								} else if (line.contains("<Teleportation>")) {
									PathPart tempPart = new PathPart(method.getValue(method.getValueLine(line, "<Method>")[0]), method.getValue(method.getValueLine(line, "<Option>")[0]));
									if (tempPart != null) {
										thePath.add(tempPart);
									}
								}
							} catch (Exception e) {
							}
						}
						//Close the input stream
						in.close();
						PathPart[] returnPath = new PathPart[thePath.size()];
						for (int i = 0; i < thePath.size(); i++) {
							returnPath[i] = thePath.get(i);
						}
						return returnPath;
					} catch (Exception e) {//Catch exception if any
						return null;
					}
				}

				public void loadPathMakerSettings(String nameOfFile) {
					ArrayList<PathPart> path = new ArrayList<PathPart>();
					for (PathPart part : loadPathParts(nameOfFile)) {
						path.add(part);
					}
					theTempPath = path;
					updateScreen();

				}


			}
		}


		BankingGUI bankingGUI = new BankingGUI();

		public class BankingGUI {
			ArrayList<BankingPart> theTempBankingParts = new ArrayList<BankingPart>();
			String walkingToMonsters = "";
			String walkingToBank = "";

			public void openWithdrawingScreen() {
				new MiamiFighterGUIWithdrawing().setVisible(true);
			}

			public void openDepositingScreen() {
				new MiamiFighterGUIDepositing().setVisible(true);
			}

			public void updateSettings() {
				ArrayList<String> pathMakerNames = new ArrayList<String>();
				pathMakerNames.add("<Default>");
				File folder = new File("MiamiFighter_Settings/Banking_Settings/");
				File[] listOfFiles = folder.listFiles();
				if (listOfFiles != null) {
					for (int i = 0; i < listOfFiles.length; i++) {
						if (listOfFiles[i].isFile()) {
							String fullName = listOfFiles[i].getName();
							int charLocation = 0;
							for (int a = 0; a < fullName.toCharArray().length; a++) {
								if (fullName.toCharArray()[a] == '.') {
									charLocation = a;
								}
							}
							String newName = "";
							for (int a = 0; a < fullName.toCharArray().length; a++) {
								if (a < charLocation) {
									newName += fullName.toCharArray()[a];
								}
							}
							pathMakerNames.add(newName);
						}
					}
				}
				String[] finalString = new String[pathMakerNames.size()];
				for (int i = 0; i < pathMakerNames.size(); i++) {
					finalString[i] = pathMakerNames.get(i);
				}
				comboBox3.setModel(new DefaultComboBoxModel(finalString));

			}

			public void updateScreen() {
				if (walkingToMonsters != null && walkingToMonsters != "") {
					comboBox4.setSelectedItem(walkingToMonsters);
				}
				if (walkingToBank != null && walkingToBank != "") {
					comboBox5.setSelectedItem(walkingToBank);
				}
				ArrayList<String> s = new ArrayList<String>();
				int randomNumber = 1;
				for (BankingPart part : theTempBankingParts) {
					if (part.bankingAction == BankingSystem.WITHDRAW) {
						s.add("Part " + randomNumber + ": Withdraw -  " + part.bankingArray[0] + ", amount - " + part.bankingArray[1]);
					} else if (part.bankingAction == BankingSystem.WITHDRAW_IF_INVENTORY_DOESNT_CONTAIN) {
						String numbersWithComma = "";
						for (int i = 1; i < part.bankingArray.length; i++) {
							if (i == 1) {
								numbersWithComma += part.bankingArray[i];
							} else {
								numbersWithComma += ", " + part.bankingArray[i];
							}
						}
						s.add("Part " + randomNumber + ": Withdraw - " + part.bankingArray[0] + ", if inventory doesn't contain one of the following: " + numbersWithComma);
					} else if (part.bankingAction == BankingSystem.DEPOSIT_ALL) {
						s.add("Part " + randomNumber + ": Deposit All");
					} else if (part.bankingAction == BankingSystem.DEPOSIT_ALL_EXCEPT) {
						String numbersWithComma = "";
						for (int i = 0; i < part.bankingArray.length; i++) {
							if (i == 0) {
								numbersWithComma += part.bankingArray[i];
							} else {
								numbersWithComma += ", " + part.bankingArray[i];
							}
						}
						s.add("Part " + randomNumber + ": Deposit All except - " + numbersWithComma);
					} else if (part.bankingAction == BankingSystem.DEPOSIT_SINGLE_ITEM) {
						s.add("Part " + randomNumber + ": Deposit -  " + part.bankingArray[0] + ", amount - " + part.bankingArray[1]);
					}
					randomNumber++;
				}
				String[] theNewStrings = new String[s.size()];
				for (int i = 0; i < s.size(); i++) {
					theNewStrings[i] = s.get(i);
				}
				final String[] array = theNewStrings.length > 0 ? theNewStrings : new String[]{"Nothing"};
				list4.setModel(new AbstractListModel() {
					private static final long serialVersionUID = 1L;
					String[] values = array;

					public int getSize() {
						return values.length;
					}

					public Object getElementAt(int i) {
						return values[i];
					}
				});
			}


			public void addBankingPart(BankingPart part) {
				theTempBankingParts.add(part);
				updateScreen();
			}

			public void delete() {
				String s = (String) list4.getSelectedValue();
				if (s == null || s == "") {
					return;
				}
				for (int i = 1; i <= theTempBankingParts.size(); i++) {
					if (s.contains("Part " + i + ":")) {
						theTempBankingParts.remove(i - 1);
						break;
					}
				}
				updateScreen();
			}


			public class MiamiFighterGUIWithdrawing extends JFrame {
				private static final long serialVersionUID = 1L;

				public MiamiFighterGUIWithdrawing() {
					initComponents();
				}

				private void button30ActionPerformed(ActionEvent e) {
					try {
						int a = Integer.parseInt(textField70.getText());
						int b = Integer.parseInt(textField69.getText());
						addBankingPart(new BankingPart(BankingSystem.WITHDRAW, new int[]{a, b}));
						dispose();
					} catch (Exception absdfs) {
						log("You forgotsome numbers");
					}
				}

				private void button31ActionPerformed(ActionEvent e) {
					ArrayList<Integer> allNumbers = new ArrayList<Integer>();
					String s = textField75.getText();
					if (s == null || s == "") {
						log("You forgot something");
						return;
					}
					String tempNumber = "";
					for (int i = 0; i < s.length(); i++) {
						if (s.charAt(i) == '0' || s.charAt(i) == '1' || s.charAt(i) == '2' || s.charAt(i) == '3' || s.charAt(i) == '4'
								|| s.charAt(i) == '5' || s.charAt(i) == '6' || s.charAt(i) == '7' || s.charAt(i) == '8' || s.charAt(i) == '9') {

							tempNumber += s.charAt(i);
							continue;
						} else if (s.charAt(i) == ',') {
							try {
								allNumbers.add(Integer.parseInt(tempNumber));
								tempNumber = "";
							} catch (Exception sdfs) {

							}
						}
					}
					try {
						int otherNumber = Integer.parseInt(tempNumber);
						allNumbers.add(otherNumber);
					} catch (Exception sfsdf) {
					}
					try {
						int a = Integer.parseInt(textField76.getText());
						int[] newArray = new int[allNumbers.size() + 1];
						newArray[0] = a;
						for (int i = 1; i < allNumbers.size() + 1; i++) {
							newArray[i] = allNumbers.get(i - 1);
						}
						addBankingPart(new BankingPart(BankingSystem.WITHDRAW_IF_INVENTORY_DOESNT_CONTAIN, newArray));
						dispose();
					} catch (Exception adfds) {
						log("You forgot something");
					}
				}

				private void initComponents() {
					// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
					scrollPane41 = new JScrollPane();
					panel39 = new JPanel();
					textArea19 = new JTextArea();
					scrollPane42 = new JScrollPane();
					panel40 = new JPanel();
					button30 = new JButton();
					textField69 = new JTextField();
					textField70 = new JTextField();
					textField71 = new JTextField();
					textField74 = new JTextField();
					scrollPane43 = new JScrollPane();
					panel41 = new JPanel();
					button31 = new JButton();
					textField75 = new JTextField();
					textField76 = new JTextField();
					textField99 = new JTextField();
					textField100 = new JTextField();
					textField101 = new JTextField();

					//======== this ========
					setTitle("Banking Part - Withdrawing");
					Container contentPane = getContentPane();
					contentPane.setLayout(null);

					//======== scrollPane41 ========
					{
						scrollPane41.setForeground(UIManager.getColor("Button.background"));
						scrollPane41.setBorder(new TitledBorder(null, "Banking Part - Withdrawing", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
								new Font("Calibri", Font.PLAIN, 12), Color.blue));

						//======== panel39 ========
						{
							panel39.setLayout(null);

							//---- textArea19 ----
							textArea19.setBackground(UIManager.getColor("Button.background"));
							textArea19.setEditable(false);
							textArea19.setFont(new Font("Calibri", Font.PLAIN, 13));
							textArea19.setText("Here you can add different Banking Parts for the \ncategory withdrawing.");
							panel39.add(textArea19);
							textArea19.setBounds(5, 0, 290, 35);

							//======== scrollPane42 ========
							{
								scrollPane42.setForeground(UIManager.getColor("Button.background"));
								scrollPane42.setBorder(new TitledBorder(null, "Withdraw:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										new Font("Calibri", Font.PLAIN, 12), Color.blue));

								//======== panel40 ========
								{
									panel40.setLayout(null);

									//---- button30 ----
									button30.setText("Add Banking Part");
									button30.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											button30ActionPerformed(e);
										}
									});
									panel40.add(button30);
									button30.setBounds(5, 50, 160, 20);
									panel40.add(textField69);
									textField69.setBounds(180, 25, 85, 20);
									panel40.add(textField70);
									textField70.setBounds(180, 0, 85, 20);

									//---- textField71 ----
									textField71.setText("Item ID:");
									textField71.setBackground(new Color(240, 240, 240));
									textField71.setCaretColor(new Color(240, 240, 240));
									textField71.setBorder(null);
									textField71.setEditable(false);
									textField71.setFont(new Font("Calibri", Font.BOLD, 13));
									panel40.add(textField71);
									textField71.setBounds(10, 0, 90, 20);

									//---- textField74 ----
									textField74.setText("Amount, 0 = all:");
									textField74.setBackground(new Color(240, 240, 240));
									textField74.setCaretColor(new Color(240, 240, 240));
									textField74.setBorder(null);
									textField74.setEditable(false);
									textField74.setFont(new Font("Calibri", Font.BOLD, 13));
									panel40.add(textField74);
									textField74.setBounds(10, 25, 90, 20);

									{ // compute preferred size
										Dimension preferredSize = new Dimension();
										for (int i = 0; i < panel40.getComponentCount(); i++) {
											Rectangle bounds = panel40.getComponent(i).getBounds();
											preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
											preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
										}
										Insets insets = panel40.getInsets();
										preferredSize.width += insets.right;
										preferredSize.height += insets.bottom;
										panel40.setMinimumSize(preferredSize);
										panel40.setPreferredSize(preferredSize);
									}
								}
								scrollPane42.setViewportView(panel40);
							}
							panel39.add(scrollPane42);
							scrollPane42.setBounds(5, 35, 295, 105);

							//======== scrollPane43 ========
							{
								scrollPane43.setForeground(UIManager.getColor("Button.background"));
								scrollPane43.setBorder(new TitledBorder(null, "Withdraw if Inventory doesn't contain:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										new Font("Calibri", Font.PLAIN, 12), Color.blue));

								//======== panel41 ========
								{
									panel41.setLayout(null);

									//---- button31 ----
									button31.setText("Add Banking Part:");
									button31.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											button31ActionPerformed(e);
										}
									});
									panel41.add(button31);
									button31.setBounds(5, 85, 160, 20);
									panel41.add(textField75);
									textField75.setBounds(5, 60, 260, 20);
									panel41.add(textField76);
									textField76.setBounds(180, 0, 85, 20);

									//---- textField99 ----
									textField99.setText("Withdraw 1 of Item ID:");
									textField99.setBackground(new Color(240, 240, 240));
									textField99.setCaretColor(new Color(240, 240, 240));
									textField99.setBorder(null);
									textField99.setEditable(false);
									textField99.setFont(new Font("Calibri", Font.BOLD, 13));
									panel41.add(textField99);
									textField99.setBounds(5, 0, 130, 20);

									//---- textField100 ----
									textField100.setText("If inventory doesn't contain one of the following");
									textField100.setBackground(new Color(240, 240, 240));
									textField100.setCaretColor(new Color(240, 240, 240));
									textField100.setBorder(null);
									textField100.setEditable(false);
									textField100.setFont(new Font("Calibri", Font.BOLD, 13));
									panel41.add(textField100);
									textField100.setBounds(5, 25, 265, 20);

									//---- textField101 ----
									textField101.setText("Item IDs. Example: \"1, 2, 3\" Use comma and space");
									textField101.setBackground(new Color(240, 240, 240));
									textField101.setCaretColor(new Color(240, 240, 240));
									textField101.setBorder(null);
									textField101.setEditable(false);
									textField101.setFont(new Font("Calibri", Font.BOLD, 13));
									panel41.add(textField101);
									textField101.setBounds(5, 40, 275, 20);

									{ // compute preferred size
										Dimension preferredSize = new Dimension();
										for (int i = 0; i < panel41.getComponentCount(); i++) {
											Rectangle bounds = panel41.getComponent(i).getBounds();
											preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
											preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
										}
										Insets insets = panel41.getInsets();
										preferredSize.width += insets.right;
										preferredSize.height += insets.bottom;
										panel41.setMinimumSize(preferredSize);
										panel41.setPreferredSize(preferredSize);
									}
								}
								scrollPane43.setViewportView(panel41);
							}
							panel39.add(scrollPane43);
							scrollPane43.setBounds(5, 140, 295, 135);

							{ // compute preferred size
								Dimension preferredSize = new Dimension();
								for (int i = 0; i < panel39.getComponentCount(); i++) {
									Rectangle bounds = panel39.getComponent(i).getBounds();
									preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
									preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
								}
								Insets insets = panel39.getInsets();
								preferredSize.width += insets.right;
								preferredSize.height += insets.bottom;
								panel39.setMinimumSize(preferredSize);
								panel39.setPreferredSize(preferredSize);
							}
						}
						scrollPane41.setViewportView(panel39);
					}
					contentPane.add(scrollPane41);
					scrollPane41.setBounds(5, 5, 315, 305);

					contentPane.setPreferredSize(new Dimension(335, 350));
					pack();
					setLocationRelativeTo(getOwner());
					// JFormDesigner - End of component initialization  //GEN-END:initComponents
				}

				// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
				private JScrollPane scrollPane41;
				private JPanel panel39;
				private JTextArea textArea19;
				private JScrollPane scrollPane42;
				private JPanel panel40;
				private JButton button30;
				private JTextField textField69;
				private JTextField textField70;
				private JTextField textField71;
				private JTextField textField74;
				private JScrollPane scrollPane43;
				private JPanel panel41;
				private JButton button31;
				private JTextField textField75;
				private JTextField textField76;
				private JTextField textField99;
				private JTextField textField100;
				private JTextField textField101;
				// JFormDesigner - End of variables declaration  //GEN-END:variables
			}

			public class MiamiFighterGUIDepositing extends JFrame {
				private static final long serialVersionUID = 1L;

				public MiamiFighterGUIDepositing() {
					initComponents();
				}

				private void button35ActionPerformed(ActionEvent e) {
					try {
						int a = Integer.parseInt(textField103.getText());
						int b = Integer.parseInt(textField102.getText());
						addBankingPart(new BankingPart(BankingSystem.DEPOSIT_SINGLE_ITEM, new int[]{a, b}));
						dispose();
					} catch (Exception ssdfsde) {
						log("You forgot to put in");
					}
				}

				private void button36ActionPerformed(ActionEvent e) {
					addBankingPart(new BankingPart(BankingSystem.DEPOSIT_ALL, new int[]{}));
					dispose();
				}

				private void button37ActionPerformed(ActionEvent e) {
					ArrayList<Integer> allNumbers = new ArrayList<Integer>();
					String s = textField111.getText();
					if (s == null || s == "") {
						log("You forgot something");
						return;
					}
					String tempNumber = "";
					for (int i = 0; i < s.length(); i++) {
						if (s.charAt(i) == '0' || s.charAt(i) == '1' || s.charAt(i) == '2' || s.charAt(i) == '3' || s.charAt(i) == '4'
								|| s.charAt(i) == '5' || s.charAt(i) == '6' || s.charAt(i) == '7' || s.charAt(i) == '8' || s.charAt(i) == '9') {

							tempNumber += s.charAt(i);
							continue;
						} else if (s.charAt(i) == ',') {
							try {
								allNumbers.add(Integer.parseInt(tempNumber));
								tempNumber = "";
							} catch (Exception sdfs) {

							}
						}
					}
					try {
						int otherNumber = Integer.parseInt(tempNumber);
						allNumbers.add(otherNumber);
					} catch (Exception sfsdf) {
					}
					try {
						int[] newArray = new int[allNumbers.size()];
						for (int i = 0; i < allNumbers.size(); i++) {
							newArray[i] = allNumbers.get(i);
						}
						addBankingPart(new BankingPart(BankingSystem.DEPOSIT_ALL_EXCEPT, newArray));
						dispose();
					} catch (Exception adfds) {
						log("You forgot something");
					}
				}

				private void initComponents() {
					// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
					scrollPane44 = new JScrollPane();
					panel42 = new JPanel();
					textArea20 = new JTextArea();
					scrollPane45 = new JScrollPane();
					panel43 = new JPanel();
					button35 = new JButton();
					textField102 = new JTextField();
					textField103 = new JTextField();
					textField104 = new JTextField();
					textField105 = new JTextField();
					scrollPane46 = new JScrollPane();
					panel44 = new JPanel();
					button36 = new JButton();
					textField109 = new JTextField();
					scrollPane47 = new JScrollPane();
					panel45 = new JPanel();
					button37 = new JButton();
					textField111 = new JTextField();
					textField114 = new JTextField();
					textField115 = new JTextField();

					//======== this ========
					setTitle("Banking Part - Depositing");
					Container contentPane = getContentPane();
					contentPane.setLayout(null);

					//======== scrollPane44 ========
					{
						scrollPane44.setForeground(UIManager.getColor("Button.background"));
						scrollPane44.setBorder(new TitledBorder(null, "Banking Part - Depositing", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
								new Font("Calibri", Font.PLAIN, 12), Color.blue));

						//======== panel42 ========
						{
							panel42.setLayout(null);

							//---- textArea20 ----
							textArea20.setBackground(UIManager.getColor("Button.background"));
							textArea20.setEditable(false);
							textArea20.setFont(new Font("Calibri", Font.PLAIN, 13));
							textArea20.setText("Here you can add different Banking Parts for the \ncategory depositing.");
							panel42.add(textArea20);
							textArea20.setBounds(5, 0, 290, 35);

							//======== scrollPane45 ========
							{
								scrollPane45.setForeground(UIManager.getColor("Button.background"));
								scrollPane45.setBorder(new TitledBorder(null, "Deposit:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										new Font("Calibri", Font.PLAIN, 12), Color.blue));

								//======== panel43 ========
								{
									panel43.setLayout(null);

									//---- button35 ----
									button35.setText("Add Banking Part");
									button35.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											button35ActionPerformed(e);
										}
									});
									panel43.add(button35);
									button35.setBounds(5, 50, 160, 20);
									panel43.add(textField102);
									textField102.setBounds(180, 25, 85, 20);
									panel43.add(textField103);
									textField103.setBounds(180, 0, 85, 20);

									//---- textField104 ----
									textField104.setText("Item ID:");
									textField104.setBackground(new Color(240, 240, 240));
									textField104.setCaretColor(new Color(240, 240, 240));
									textField104.setBorder(null);
									textField104.setEditable(false);
									textField104.setFont(new Font("Calibri", Font.BOLD, 13));
									panel43.add(textField104);
									textField104.setBounds(10, 0, 90, 20);

									//---- textField105 ----
									textField105.setText("Amount, 0 = all:");
									textField105.setBackground(new Color(240, 240, 240));
									textField105.setCaretColor(new Color(240, 240, 240));
									textField105.setBorder(null);
									textField105.setEditable(false);
									textField105.setFont(new Font("Calibri", Font.BOLD, 13));
									panel43.add(textField105);
									textField105.setBounds(10, 25, 90, 20);

									{ // compute preferred size
										Dimension preferredSize = new Dimension();
										for (int i = 0; i < panel43.getComponentCount(); i++) {
											Rectangle bounds = panel43.getComponent(i).getBounds();
											preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
											preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
										}
										Insets insets = panel43.getInsets();
										preferredSize.width += insets.right;
										preferredSize.height += insets.bottom;
										panel43.setMinimumSize(preferredSize);
										panel43.setPreferredSize(preferredSize);
									}
								}
								scrollPane45.setViewportView(panel43);
							}
							panel42.add(scrollPane45);
							scrollPane45.setBounds(5, 35, 295, 105);

							//======== scrollPane46 ========
							{
								scrollPane46.setForeground(UIManager.getColor("Button.background"));
								scrollPane46.setBorder(new TitledBorder(null, "Deposit All:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										new Font("Calibri", Font.PLAIN, 12), Color.blue));

								//======== panel44 ========
								{
									panel44.setLayout(null);

									//---- button36 ----
									button36.setText("Add Banking Part:");
									button36.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											button36ActionPerformed(e);
										}
									});
									panel44.add(button36);
									button36.setBounds(5, 20, 160, 20);

									//---- textField109 ----
									textField109.setText("Uses the button deposit all:");
									textField109.setBackground(new Color(240, 240, 240));
									textField109.setCaretColor(new Color(240, 240, 240));
									textField109.setBorder(null);
									textField109.setEditable(false);
									textField109.setFont(new Font("Calibri", Font.BOLD, 13));
									panel44.add(textField109);
									textField109.setBounds(5, 0, 265, 20);

									{ // compute preferred size
										Dimension preferredSize = new Dimension();
										for (int i = 0; i < panel44.getComponentCount(); i++) {
											Rectangle bounds = panel44.getComponent(i).getBounds();
											preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
											preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
										}
										Insets insets = panel44.getInsets();
										preferredSize.width += insets.right;
										preferredSize.height += insets.bottom;
										panel44.setMinimumSize(preferredSize);
										panel44.setPreferredSize(preferredSize);
									}
								}
								scrollPane46.setViewportView(panel44);
							}
							panel42.add(scrollPane46);
							scrollPane46.setBounds(5, 140, 295, 75);

							//======== scrollPane47 ========
							{
								scrollPane47.setForeground(UIManager.getColor("Button.background"));
								scrollPane47.setBorder(new TitledBorder(null, "Deposit All Except:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										new Font("Calibri", Font.PLAIN, 12), Color.blue));

								//======== panel45 ========
								{
									panel45.setLayout(null);

									//---- button37 ----
									button37.setText("Add Banking Part:");
									button37.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											button37ActionPerformed(e);
										}
									});
									panel45.add(button37);
									button37.setBounds(5, 55, 160, 20);
									panel45.add(textField111);
									textField111.setBounds(5, 30, 260, 20);

									//---- textField114 ----
									textField114.setText("Deposits all items except the folowing item IDS");
									textField114.setBackground(new Color(240, 240, 240));
									textField114.setCaretColor(new Color(240, 240, 240));
									textField114.setBorder(null);
									textField114.setEditable(false);
									textField114.setFont(new Font("Calibri", Font.BOLD, 13));
									panel45.add(textField114);
									textField114.setBounds(5, -5, 265, 20);

									//---- textField115 ----
									textField115.setText("Example: \"1, 2, 3\" Use commas and spaces");
									textField115.setBackground(new Color(240, 240, 240));
									textField115.setCaretColor(new Color(240, 240, 240));
									textField115.setBorder(null);
									textField115.setEditable(false);
									textField115.setFont(new Font("Calibri", Font.BOLD, 13));
									panel45.add(textField115);
									textField115.setBounds(5, 10, 275, 20);

									{ // compute preferred size
										Dimension preferredSize = new Dimension();
										for (int i = 0; i < panel45.getComponentCount(); i++) {
											Rectangle bounds = panel45.getComponent(i).getBounds();
											preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
											preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
										}
										Insets insets = panel45.getInsets();
										preferredSize.width += insets.right;
										preferredSize.height += insets.bottom;
										panel45.setMinimumSize(preferredSize);
										panel45.setPreferredSize(preferredSize);
									}
								}
								scrollPane47.setViewportView(panel45);
							}
							panel42.add(scrollPane47);
							scrollPane47.setBounds(5, 215, 295, 105);

							{ // compute preferred size
								Dimension preferredSize = new Dimension();
								for (int i = 0; i < panel42.getComponentCount(); i++) {
									Rectangle bounds = panel42.getComponent(i).getBounds();
									preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
									preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
								}
								Insets insets = panel42.getInsets();
								preferredSize.width += insets.right;
								preferredSize.height += insets.bottom;
								panel42.setMinimumSize(preferredSize);
								panel42.setPreferredSize(preferredSize);
							}
						}
						scrollPane44.setViewportView(panel42);
					}
					contentPane.add(scrollPane44);
					scrollPane44.setBounds(5, 5, 315, 350);

					contentPane.setPreferredSize(new Dimension(335, 390));
					pack();
					setLocationRelativeTo(getOwner());
					// JFormDesigner - End of component initialization  //GEN-END:initComponents
				}

				// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
				private JScrollPane scrollPane44;
				private JPanel panel42;
				private JTextArea textArea20;
				private JScrollPane scrollPane45;
				private JPanel panel43;
				private JButton button35;
				private JTextField textField102;
				private JTextField textField103;
				private JTextField textField104;
				private JTextField textField105;
				private JScrollPane scrollPane46;
				private JPanel panel44;
				private JButton button36;
				private JTextField textField109;
				private JScrollPane scrollPane47;
				private JPanel panel45;
				private JButton button37;
				private JTextField textField111;
				private JTextField textField114;
				private JTextField textField115;
				// JFormDesigner - End of variables declaration  //GEN-END:variables
			}

			MiamiFighterGUIBankingSettings miamiFighterGUIBankingSettings = new MiamiFighterGUIBankingSettings();

			public class MiamiFighterGUIBankingSettings {
				public void saveSettings(String nameOfFile) {
					ArrayList<String> finalSavingThings = new ArrayList<String>();
					String chosenPathToMonster = (String) comboBox4.getSelectedItem();
					String chosenPathToBank = (String) comboBox5.getSelectedItem();
					if (chosenPathToMonster != null && chosenPathToMonster != "") {
						finalSavingThings.add("<WalkingToMonsters>(" + chosenPathToMonster + ")");
					}
					if (chosenPathToBank != null && chosenPathToBank != "") {
						finalSavingThings.add("<WalkingToBank>(" + chosenPathToBank + ")");
					}
					if (checkBox17.isSelected()) {
						finalSavingThings.add("<UseBank>(" + checkBox17.isSelected() + ")");
					}
					for (BankingPart part : theTempBankingParts) {
						String s = "";
						if (part.bankingAction == BankingSystem.WITHDRAW) {
							s += "<Withdraw>";
						} else if (part.bankingAction == BankingSystem.WITHDRAW_IF_INVENTORY_DOESNT_CONTAIN) {
							s += "<WithdrawIfInventoryDoesntContain>";
						} else if (part.bankingAction == BankingSystem.DEPOSIT_SINGLE_ITEM) {
							s += "<Deposit>";
						} else if (part.bankingAction == BankingSystem.DEPOSIT_ALL) {
							s += "<DepositAll>";
						} else if (part.bankingAction == BankingSystem.DEPOSIT_ALL_EXCEPT) {
							s += "<DepositAllExcept>";
						}
						for (int i = 0; i < part.bankingArray.length; i++) {
							if (i == 0) {
								s += "<BankingArray>(" + part.bankingArray[i];
							} else {
								s += ", " + part.bankingArray[i];
							}
							if (i == part.bankingArray.length - 1) {
								s += ")";
							}

						}
						finalSavingThings.add(s);
					}
					try {
						BufferedWriter writer = new BufferedWriter(new FileWriter("MiamiFighter_Settings/Banking_Settings/" + nameOfFile + ".txt"));
						int i = 1;
						for (String s : finalSavingThings) {
							writer.write(s);
							if (i < finalSavingThings.size()) {
								writer.newLine();
							}
							i++;
						}
						writer.close();
					} catch (Exception e) {

					}
				}

				public void loadSettings(String nameOfFile) {


					try {
						InputStream in = new DataInputStream(new FileInputStream("MiamiFighter_Settings/Banking_Settings/" + nameOfFile + ".txt"));
						BufferedReader br = new BufferedReader(new InputStreamReader(in));


						ArrayList<BankingPart> bankingParts = new ArrayList<BankingPart>();
						String walkingToMonstersTemp = null;
						String walkingToBankTemp = null;
						String line;
						while ((line = br.readLine()) != null) {
							try {
								if (line.contains("<WalkingToMonsters>")) {
									walkingToMonstersTemp = method.getValueLine(line, null)[0];
									continue;
								} else if (line.contains("<WalkingToBank>")) {
									walkingToBankTemp = method.getValueLine(line, null)[0];
									continue;
								} else if (line.contains("<UseBank>")) {
									checkBox17.setSelected(method.getValueLine(line, null)[0].contains("true"));
									continue;
								} else {
									int action = 0;

									if (line.contains("<Withdraw>")) action = BankingSystem.WITHDRAW;
									else if (line.contains("<WithdrawIfInventoryDoesntContain>"))
										action = BankingSystem.WITHDRAW_IF_INVENTORY_DOESNT_CONTAIN;
									else if (line.contains("<Deposit>")) action = BankingSystem.DEPOSIT_SINGLE_ITEM;
									else if (line.contains("<DepositAll>")) action = BankingSystem.DEPOSIT_ALL;
									else if (line.contains("<DepositAllExcept>"))
										action = BankingSystem.DEPOSIT_ALL_EXCEPT;

									int[] array = new int[method.getValueLine(line, "<BankingArray>").length];
									for (int i = 0; i < array.length; i++) {
										array[i] = method.getValue(method.getValueLine(line, "<BankingArray>")[i]);
									}

									bankingParts.add(new BankingPart(action, array));
								}
							} catch (Exception e) {
							}
						}
						theTempBankingParts = bankingParts;


						walkingToMonsters = walkingToMonstersTemp;
						walkingToBank = walkingToBankTemp;
						in.close();
						updateScreen();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}

		GeneralGUI generalGUI = new GeneralGUI();

		public class GeneralGUI {
			RSTile mainTile = new RSTile(0, 0);
			int mainTileX = 0, mainTileY = 0;

			public void setMainTile() {
				if (game.isLoggedIn()) {
					mainTile = getMyPlayer().getLocation();
					String s = "(" + mainTile.getX() + ", " + mainTile.getY() + ")";
					textField40.setText(s);
				}
			}

			public void setMainTile(int x, int y) {
				mainTile = new RSTile(x, y);
				String s = "(" + mainTile.getX() + ", " + mainTile.getY() + ")";
				textField40.setText(s);
			}

			public void onOpeningGUI() {
				new File("MiamiFighter_Settings").mkdir();
				new File("MiamiFighter_Settings/Banking_Settings").mkdir();
				new File("MiamiFighter_Settings/PathMaker_Settings").mkdir();
				new File("MiamiFighter_Settings/General_Settings").mkdir();
				new File("MiamiFighter_Settings/NPC_Settings").mkdir();
				new File("MiamiFighter_Settings/Loot_Settings").mkdir();
				pathMakerGUI.updateSettingsPathMaker();
				bankingGUI.updateSettings();
				generalGUI.updateSettings();

				String url = "http://gametuning.net76.net/MiamiFighter/MiamiFighterVersion.html";
				String str = null;
				try {
					str = new BufferedReader(new InputStreamReader(new URL(url).openStream())).readLine();
				} catch (Exception e) {
				}
				try {
					double latestVersion = Double.parseDouble(str);
					if (latestVersion > info.scriptVersion) {
						textField33.setForeground(new Color(0, 185, 0));
						textField33.setText("False");
					}
				} catch (Exception e) {
				}


			}


			public void updateSettings() {
				ArrayList<String> allNames = new ArrayList<String>();
				allNames.add("<Default>");
				File[] folder1 = new File[]{new File("MiamiFighter_Settings/NPC_Settings/"),
						new File("MiamiFighter_Settings/General_Settings/"),
						new File("MiamiFighter_Settings/Loot_Settings/")};
				for (File folder : folder1) {
					File[] listOfFiles = folder.listFiles();
					if (listOfFiles != null) {
						for (int i = 0; i < listOfFiles.length; i++) {
							if (listOfFiles[i].isFile()) {
								String fullName = listOfFiles[i].getName();
								int charLocation = 0;
								for (int a = 0; a < fullName.toCharArray().length; a++) {
									if (fullName.toCharArray()[a] == '.') {
										charLocation = a;
									}
								}
								String newName = "";
								for (int a = 0; a < fullName.toCharArray().length; a++) {
									if (a < charLocation) {
										newName += fullName.toCharArray()[a];
									}
								}
								boolean add = true;
								for (String s : allNames) {
									if (newName.equals(s)) {
										add = false;
									}
								}
								if (add) {
									allNames.add(newName);
								}
							}
						}
					}
				}
				String[] finalString = new String[allNames.size()];
				for (int i = 0; i < allNames.size(); i++) {
					finalString[i] = allNames.get(i);
				}
				comboBox1.setModel(new DefaultComboBoxModel(finalString));
			}

			public void loadSettings(String fileName) {
				try {
					try {
						npcGUI.npcGUISettings.loadNPCSettings(fileName);
					} catch (Exception e) {
					}
					try {
						itemsGUI.itemsGUISettings.loadItemsSettings(fileName);
					} catch (Exception e) {
					}
					try {
						generalGUI.mainSettings.loadMainSettings(fileName);
					} catch (Exception e) {
					}
					textField5.setText(fileName);
				} catch (Exception e) {
				}
			}

			public void saveSettings(String fileName) {
				npcGUI.npcGUISettings.saveNPCSettings(fileName);
				itemsGUI.itemsGUISettings.saveLootSettings(fileName);
				generalGUI.mainSettings.saveMainSettings(fileName);
				updateSettings();
				comboBox1.setSelectedItem(fileName);
			}

			MainSettings mainSettings = new MainSettings();

			public class MainSettings {
				public void loadMainSettings(String fileName) {
					try {
						FileInputStream fstream = new FileInputStream("MiamiFighter_Settings/General_Settings/" + fileName + ".txt");
						DataInputStream in = new DataInputStream(fstream);
						BufferedReader br = new BufferedReader(new InputStreamReader(in));

						String line;
						int ID;
						String s;
						boolean b;

						while ((line = br.readLine()) != null) {
							try {
								s = method.getValueString(line);
								ID = method.getValue(s);
								b = s.contains("true");

								if (line.contains("<MultiCombat>")) {
									checkBox13.setSelected(b);
								} else if (line.contains("<UseEmergencyTeleport>")) {
									checkBox18.setSelected(b);
								} else if (line.contains("<UseEmergencyTabs>")) {
									checkBox19.setSelected(b);
								} else if (line.contains("<BuryBones>")) {
									checkBox20.setSelected(b);
								} else if (line.contains("<MainTileX>")) {
									mainTileX = ID;
								} else if (line.contains("<MainTileY>")) {
									mainTileY = ID;
								} else if (line.contains("<TilesAroundMainTile>")) {
									spinner5.setValue(ID);
								} else if (line.contains("<HealingMin%>")) {
									spinner3.setValue(ID);
								} else if (line.contains("<HealingMax%>")) {
									spinner4.setValue(ID);
								} else if (line.contains("<UseFood>")) {
									checkBox14.setSelected(b);
								} else if (line.contains("<UseGuthans>")) {
									checkBox21.setSelected(b);
								} else if (line.contains("<UseB2P>")) {
									checkBox15.setSelected(b);
								} else if (line.contains("<DrinkPotAbove>")) {
									spinner2.setValue(ID);
								} else if (line.contains("<UseSpecial>")) {
									checkBox22.setSelected(b);
								} else if (line.contains("<UseSpecialAt>")) {
									comboBox6.setSelectedItem(s);
								} else if (line.contains("<UseSecondaryWeaponSpecial>")) {
									checkBox23.setSelected(b);
								} else if (line.contains("<PrimaryWeaponID>")) {
									spinner15.setValue(ID);
								} else if (line.contains("<SecondaryWeaponID>")) {
									spinner14.setValue(ID);
								} else if (line.contains("<StyleWeaponOne>")) {
									comboBox8.setSelectedItem(s);
								} else if (line.contains("<StyleWeaponTwo>")) {
									comboBox9.setSelectedItem(s);
								} else if (line.contains("<PickupItemsAboveCertainPrice>")) {
									checkBox12.setSelected(b);
								} else if (line.contains("<PickupItemsAbove>")) {
									spinner6.setValue(ID);
								} else if (line.contains("<PickupClueScrolls>")) {
									checkBox1.setSelected(b);
								} else if (line.contains("<BankingName>")) {
									comboBox3.setSelectedItem(s);
								}
							} catch (Exception e) {
							}

						}
						in.close();
						setMainTile(mainTileX, mainTileY);
					} catch (Exception e) {
					}
				}

				public void saveMainSettings(String fileName) {
					try {
						ArrayList<String> s = new ArrayList<String>();
						s.add("<MultiCombat>(" + checkBox13.isSelected() + ")");
						s.add("<UseEmergencyTeleport>(" + checkBox18.isSelected() + ")");
						s.add("<UseEmergencyTabs>(" + checkBox19.isSelected() + ")");
						s.add("<BuryBones>(" + checkBox20.isSelected() + ")");

						s.add("<MainTileX>(" + generalGUI.mainTile.getX() + ")");
						s.add("<MainTileY>(" + generalGUI.mainTile.getY() + ")");
						s.add("<TilesAroundMainTile>(" + spinner5.getValue() + ")");

						s.add("<HealingMin%>(" + spinner3.getValue() + ")");
						s.add("<HealingMax%>(" + spinner4.getValue() + ")");
						s.add("<UseFood>(" + checkBox14.isSelected() + ")");
						s.add("<UseB2P>(" + checkBox21.isSelected() + ")");
						s.add("<UseGuthans>(" + checkBox15.isSelected() + ")");
						s.add("<DrinkPotAbove>(" + spinner2.getValue() + ")");


						s.add("<UseSpecial>(" + checkBox22.isSelected() + ")");
						s.add("<UseSpecialAt>(" + comboBox6.getSelectedItem() + ")");
						s.add("<UseSecondaryWeaponSpecial>(" + checkBox23.isSelected() + ")");
						s.add("<PrimaryWeaponID>(" + spinner15.getValue() + ")");
						s.add("<SecondaryWeaponID>(" + spinner14.getValue() + ")");
						s.add("<StyleWeaponOne>(" + comboBox8.getSelectedItem() + ")");
						s.add("<StyleWeaponTwo>(" + comboBox9.getSelectedItem() + ")");

						s.add("<PickupItemsAboveCertainPrice>(" + checkBox12.isSelected() + ")");
						s.add("<PickupItemsAbove>(" + spinner6.getValue() + ")");
						s.add("<PickupClueScrolls>(" + checkBox1.isSelected() + ")");

						s.add("<BankingName>(" + comboBox3.getSelectedItem() + ")");

						BufferedWriter writer = new BufferedWriter(new FileWriter("MiamiFighter_Settings/General_Settings/" + fileName + ".txt"));
						int i = 1;
						for (String str : s) {
							writer.write(str);
							if (i < s.size()) {
								writer.newLine();
							}
							i++;
						}
						writer.close();
					} catch (Exception e) {

					}
				}
			}


		}

		public void startScript1() {
			if (generalGUI.mainTile.equals(new RSTile(0, 0))) {
				log("No Main tile is set");
				return;
			}
			setting.mainTile = generalGUI.mainTile;
			setting.tilesOfCourse = method.getValue(spinner5.getValue().toString());

			setting.useFood = checkBox14.isSelected();
			setting.useB2P = checkBox21.isSelected();
			setting.useGuthans = checkBox15.isSelected();

			setting.percentEatingMin = method.getValue(spinner3.getValue().toString());
			setting.percentEatingMax = method.getValue(spinner4.getValue().toString());

			setting.drinkPotsAbove = method.getValue(spinner2.getValue().toString());

			Monster[] tempMonsters = new Monster[npcGUI.tempMonsters.size()];
			for (int i = 0; i < npcGUI.tempMonsters.size(); i++) {
				tempMonsters[i] = npcGUI.tempMonsters.get(i);
			}
			setting.arrayOfMonsters = tempMonsters;

			Loot[] tempLoot = new Loot[itemsGUI.tempLoot.size()];
			for (int i = 0; i < itemsGUI.tempLoot.size(); i++) {
				tempLoot[i] = itemsGUI.tempLoot.get(i);
			}
			setting.lootList = tempLoot;
			setting.useBank = checkBox17.isSelected();
			if (setting.useBank) {
				setting.bankingPath = pathMakerGUI.miamiFighterGuiPathMakerSettings.loadPathParts((String) comboBox5.getSelectedItem());
				setting.toMonstersPath = pathMakerGUI.miamiFighterGuiPathMakerSettings.loadPathParts((String) comboBox4.getSelectedItem());

				setting.bankingAllParts = new BankingPart[bankingGUI.theTempBankingParts.size()];
				for (int i = 0; i < bankingGUI.theTempBankingParts.size(); i++) {
					setting.bankingAllParts[i] = bankingGUI.theTempBankingParts.get(i);
				}
			}


			setting.multiCombat = checkBox13.isSelected();
			setting.useTeleportInEmergency = checkBox18.isSelected();
			setting.useTeleportTabsInEmergency = checkBox19.isSelected();
			setting.buryBones = checkBox20.isSelected();

			setting.useSpecial = checkBox22.isSelected();
			setting.atWhatPercentToUseSpecial = method.getValue((String) comboBox6.getSelectedItem());
			setting.useSecondaryWeaponSpecial = checkBox23.isSelected();
			setting.primaryWeaponID = method.getValue(spinner15.getValue().toString());
			setting.secondaryWeaponID = method.getValue(spinner14.getValue().toString());


			setting.primaryWeaponStyle = method.getValue(comboBox8.getSelectedItem().toString()) > 0 ? method.getValue(comboBox8.getSelectedItem().toString()) : 0;
			setting.secondaryWeaponStyle = method.getValue(comboBox9.getSelectedItem().toString()) > 0 ? method.getValue(comboBox9.getSelectedItem().toString()) : 0;


			if (checkBox12.isSelected()) {
				setting.lootItemsAboveACertainPrice = true;
				setting.lootItemsAbovePrice = method.getValue(spinner6.getValue().toString());
			}
			setting.lootClueScrolls = checkBox1.isSelected();


			dispose();
		}

		public MiamiFighterGUI() {
			initComponents();
			generalGUI.onOpeningGUI();
		}

		private void button3ActionPerformed(ActionEvent e) {
			startScript1();
		}

		private void button4ActionPerformed(ActionEvent e) {
			dispose();

		}

		private void button2ActionPerformed(ActionEvent e) {
			String s = textField5.getText();
			if (s == null || "".contains(s)) {
				return;
			}
			generalGUI.saveSettings(s);

		}

		private void button6ActionPerformed(ActionEvent e) {
			npcGUI.refresh();
		}

		private void button5ActionPerformed(ActionEvent e) {
			if (gui.textField8.getText().equals("")) {
				log("You have to fill in a name");
			} else {
				int itemID = method.getValue(spinner7.getValue().toString());
				itemsGUI.add(new Loot(
						itemID,
						gui.textField8.getText(),
						gui.checkBox10.isSelected(),
						gui.checkBox9.isSelected(),
						gui.checkBox11.isSelected())
				);
			}
		}

		private void button20ActionPerformed(ActionEvent e) {
			generalGUI.setMainTile();
		}


		private void button19ActionPerformed(ActionEvent e) {
			bankingGUI.openDepositingScreen();
		}

		private void button22ActionPerformed(ActionEvent e) {
			bankingGUI.openWithdrawingScreen();
		}


		private void button10ActionPerformed(ActionEvent e) {
			pathMakerGUI.openPathMakerPathPart();
		}

		private void button13ActionPerformed(ActionEvent e) {
			pathMakerGUI.openInterfacesPathPart();
		}

		private void button11ActionPerformed(ActionEvent e) {
			pathMakerGUI.openObjectNPCPathPart();
		}

		private void button12ActionPerformed(ActionEvent e) {
			pathMakerGUI.openTeleportationPathPart();
		}

		private void button8ActionPerformed(ActionEvent e) {
			String s = textField22.getText();
			if (s == null || "".contains(s)) {
				return;
			}
			pathMakerGUI.miamiFighterGuiPathMakerSettings.savePathMakerSettings(s);
			pathMakerGUI.updateSettingsPathMaker();
			comboBox2.setSelectedItem(s);
		}

		private void button15ActionPerformed(ActionEvent e) {
			String s = textField28.getText();
			if (s == null || "".contains(s)) {
				return;
			}
			bankingGUI.miamiFighterGUIBankingSettings.saveSettings(s);
			bankingGUI.updateSettings();
			comboBox3.setSelectedItem(s);
		}

		private void list4ValueChanged(ListSelectionEvent e) {
			bankingGUI.delete();
		}

		private void list3ValueChanged(ListSelectionEvent e) {
			pathMakerGUI.delete();
		}

		private void list5ValueChanged(ListSelectionEvent e) {
			itemsGUI.delete((String) list5.getSelectedValue());
		}

		private void list1ValueChanged(ListSelectionEvent e) {
			npcGUI.add();
		}

		private void list2ValueChanged(ListSelectionEvent e) {
			npcGUI.delete();
		}


		private void comboBox1ItemStateChanged(ItemEvent e) {
			String s = (String) comboBox1.getSelectedItem();
			if (s == null || "".equals(s) || s.equals("<Default>")) {
				return;
			}
			generalGUI.loadSettings(s);
		}

		private void comboBox3ItemStateChanged(ItemEvent e) {
			String s = (String) comboBox3.getSelectedItem();
			if (s == null || "".equals(s) || s.equals("<Default>")) {
				return;
			}
			bankingGUI.miamiFighterGUIBankingSettings.loadSettings(s);

			textField28.setText(s);
		}

		private void comboBox2ItemStateChanged(ItemEvent e) {
			String s = (String) comboBox2.getSelectedItem();
			if (s == null || "".equals(s) || s.equals("<Default>")) {
				return;
			}
			pathMakerGUI.miamiFighterGuiPathMakerSettings.loadPathMakerSettings(s);

			textField22.setText(s);
		}

		private void initComponents() {
			// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
			textField1 = new JTextField();
			textField2 = new JTextField();
			tabbedPane1 = new JTabbedPane();
			panel1 = new JPanel();
			scrollPane3 = new JScrollPane();
			scrollPane50 = new JScrollPane();
			textArea2 = new JTextArea();
			scrollPane4 = new JScrollPane();
			panel4 = new JPanel();
			textArea6 = new JTextArea();
			textField5 = new JTextField();
			textField6 = new JTextField();
			textField7 = new JTextField();
			comboBox1 = new JComboBox();
			button2 = new JButton();
			panel2 = new JPanel();
			scrollPane6 = new JScrollPane();
			panel6 = new JPanel();
			textArea7 = new JTextArea();
			scrollPane1 = new JScrollPane();
			list1 = new JList();
			scrollPane2 = new JScrollPane();
			list2 = new JList();
			textField12 = new JTextField();
			button6 = new JButton();
			textField23 = new JTextField();
			panel7 = new JPanel();
			scrollPane11 = new JScrollPane();
			panel11 = new JPanel();
			textArea9 = new JTextArea();
			scrollPane12 = new JScrollPane();
			list5 = new JList();
			scrollPane8 = new JScrollPane();
			panel10 = new JPanel();
			textField8 = new JTextField();
			textField9 = new JTextField();
			textField10 = new JTextField();
			checkBox9 = new JCheckBox();
			checkBox10 = new JCheckBox();
			checkBox11 = new JCheckBox();
			button5 = new JButton();
			spinner7 = new JSpinner();
			scrollPane9 = new JScrollPane();
			panel12 = new JPanel();
			textField15 = new JTextField();
			checkBox12 = new JCheckBox();
			checkBox1 = new JCheckBox();
			spinner6 = new JSpinner();
			panel8 = new JPanel();
			scrollPane7 = new JScrollPane();
			panel9 = new JPanel();
			scrollPane13 = new JScrollPane();
			panel14 = new JPanel();
			textField16 = new JTextField();
			textField18 = new JTextField();
			textField19 = new JTextField();
			checkBox21 = new JCheckBox();
			checkBox15 = new JCheckBox();
			checkBox14 = new JCheckBox();
			spinner3 = new JSpinner();
			spinner4 = new JSpinner();
			textArea24 = new JTextArea();
			spinner2 = new JSpinner();
			textArea23 = new JTextArea();
			scrollPane49 = new JScrollPane();
			panel47 = new JPanel();
			button20 = new JButton();
			textField36 = new JTextField();
			textField40 = new JTextField();
			textField44 = new JTextField();
			spinner5 = new JSpinner();
			scrollPane10 = new JScrollPane();
			panel13 = new JPanel();
			checkBox13 = new JCheckBox();
			checkBox18 = new JCheckBox();
			checkBox19 = new JCheckBox();
			checkBox20 = new JCheckBox();
			panel3 = new JPanel();
			scrollPane48 = new JScrollPane();
			panel46 = new JPanel();
			checkBox22 = new JCheckBox();
			checkBox23 = new JCheckBox();
			textField30 = new JTextField();
			comboBox6 = new JComboBox();
			comboBox8 = new JComboBox();
			comboBox9 = new JComboBox();
			spinner14 = new JSpinner();
			textField35 = new JTextField();
			spinner15 = new JSpinner();
			scrollPane24 = new JScrollPane();
			textArea22 = new JTextArea();
			panel15 = new JPanel();
			scrollPane19 = new JScrollPane();
			panel21 = new JPanel();
			textArea13 = new JTextArea();
			textField26 = new JTextField();
			comboBox3 = new JComboBox();
			button15 = new JButton();
			textField27 = new JTextField();
			textField28 = new JTextField();
			scrollPane20 = new JScrollPane();
			panel22 = new JPanel();
			scrollPane21 = new JScrollPane();
			list4 = new JList();
			scrollPane22 = new JScrollPane();
			panel23 = new JPanel();
			textArea14 = new JTextArea();
			button19 = new JButton();
			button22 = new JButton();
			scrollPane23 = new JScrollPane();
			panel24 = new JPanel();
			textArea15 = new JTextArea();
			comboBox4 = new JComboBox();
			textField31 = new JTextField();
			textField34 = new JTextField();
			comboBox5 = new JComboBox();
			checkBox17 = new JCheckBox();
			panel16 = new JPanel();
			scrollPane14 = new JScrollPane();
			panel17 = new JPanel();
			textField20 = new JTextField();
			comboBox2 = new JComboBox();
			button8 = new JButton();
			textField21 = new JTextField();
			textField22 = new JTextField();
			scrollPane30 = new JScrollPane();
			textArea8 = new JTextArea();
			scrollPane15 = new JScrollPane();
			panel18 = new JPanel();
			scrollPane16 = new JScrollPane();
			list3 = new JList();
			scrollPane17 = new JScrollPane();
			panel20 = new JPanel();
			textArea12 = new JTextArea();
			button10 = new JButton();
			button11 = new JButton();
			button12 = new JButton();
			textField24 = new JTextField();
			textField25 = new JTextField();
			button13 = new JButton();
			scrollPane5 = new JScrollPane();
			panel5 = new JPanel();
			textField4 = new JTextField();
			textField3 = new JTextField();
			textField32 = new JTextField();
			textField33 = new JTextField();
			button3 = new JButton();
			button4 = new JButton();

			//======== this ========
			setTitle("MiamiFighter - Your Bot for easy XP");
			setResizable(false);
			Container contentPane = getContentPane();
			contentPane.setLayout(null);

			//---- textField1 ----
			textField1.setFont(new Font("Calibri", Font.BOLD, 35));
			textField1.setText("MiamiFighterGUI");
			textField1.setBackground(new Color(240, 240, 240));
			textField1.setCaretColor(new Color(240, 240, 240));
			textField1.setBorder(null);
			textField1.setEditable(false);
			contentPane.add(textField1);
			textField1.setBounds(20, 15, 275, 40);

			//---- textField2 ----
			textField2.setFont(new Font("Calibri", Font.BOLD, 35));
			textField2.setText("V 0.1");
			textField2.setBackground(new Color(240, 240, 240));
			textField2.setCaretColor(new Color(240, 240, 240));
			textField2.setBorder(null);
			textField2.setForeground(Color.red);
			textField2.setEditable(false);
			contentPane.add(textField2);
			textField2.setBounds(300, 15, 85, 40);

			//======== tabbedPane1 ========
			{

				//======== panel1 ========
				{
					panel1.setLayout(null);

					//======== scrollPane3 ========
					{
						scrollPane3.setForeground(UIManager.getColor("Button.background"));
						scrollPane3.setBorder(new TitledBorder(null, "News:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
								new Font("Calibri", Font.PLAIN, 12), Color.blue));

						//======== scrollPane50 ========
						{

							//---- textArea2 ----
							textArea2.setText("MiamiFighter V 1.0.0\nBefore using always read the instructions carefully.\n\nUpdate Log:\nVersion 1.0.0 \n    |- Fixed a lot of things, Path maker is 10 times faster now.\n\nVersion 0.1.1 Release\n    |- Took one hell of a time to create.");
							textArea2.setEditable(false);
							textArea2.setWrapStyleWord(true);
							textArea2.setFont(new Font("Calibri", Font.PLAIN, 13));
							scrollPane50.setViewportView(textArea2);
						}
						scrollPane3.setViewportView(scrollPane50);
					}
					panel1.add(scrollPane3);
					scrollPane3.setBounds(5, 5, 795, 230);

					//======== scrollPane4 ========
					{
						scrollPane4.setForeground(UIManager.getColor("Button.background"));
						scrollPane4.setBorder(new TitledBorder(null, "Profiles:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
								new Font("Calibri", Font.PLAIN, 12), Color.blue));
						scrollPane4.setVerifyInputWhenFocusTarget(false);

						//======== panel4 ========
						{
							panel4.setLayout(null);

							//---- textArea6 ----
							textArea6.setBackground(UIManager.getColor("Button.background"));
							textArea6.setText("Here you can save and load your settings to make it easier to change settings. In the future it is possible to download the settings of a \nwebsite, but for now you have to create them yourself.");
							textArea6.setEditable(false);
							textArea6.setFont(new Font("Calibri", Font.PLAIN, 13));
							panel4.add(textArea6);
							textArea6.setBounds(0, 0, 775, 40);
							panel4.add(textField5);
							textField5.setBounds(460, 45, 105, textField5.getPreferredSize().height);

							//---- textField6 ----
							textField6.setText("Save as:");
							textField6.setBackground(new Color(240, 240, 240));
							textField6.setCaretColor(new Color(240, 240, 240));
							textField6.setBorder(null);
							textField6.setEditable(false);
							textField6.setFont(new Font("Calibri", Font.BOLD, 13));
							panel4.add(textField6);
							textField6.setBounds(405, 45, 55, 20);

							//---- textField7 ----
							textField7.setText("Load:");
							textField7.setBackground(new Color(240, 240, 240));
							textField7.setCaretColor(new Color(240, 240, 240));
							textField7.setBorder(null);
							textField7.setEditable(false);
							textField7.setFont(new Font("Calibri", Font.BOLD, 13));
							panel4.add(textField7);
							textField7.setBounds(5, 45, 35, 20);

							//---- comboBox1 ----
							comboBox1.addItemListener(new ItemListener() {
								public void itemStateChanged(ItemEvent e) {
									comboBox1ItemStateChanged(e);
								}
							});
							panel4.add(comboBox1);
							comboBox1.setBounds(40, 45, 175, 20);

							//---- button2 ----
							button2.setText("Save");
							button2.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									button2ActionPerformed(e);
								}
							});
							panel4.add(button2);
							button2.setBounds(570, 45, 60, 20);
						}
						scrollPane4.setViewportView(panel4);
					}
					panel1.add(scrollPane4);
					scrollPane4.setBounds(5, 240, 795, 100);

					{ // compute preferred size
						Dimension preferredSize = new Dimension();
						for (int i = 0; i < panel1.getComponentCount(); i++) {
							Rectangle bounds = panel1.getComponent(i).getBounds();
							preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
							preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
						}
						Insets insets = panel1.getInsets();
						preferredSize.width += insets.right;
						preferredSize.height += insets.bottom;
						panel1.setMinimumSize(preferredSize);
						panel1.setPreferredSize(preferredSize);
					}
				}
				tabbedPane1.addTab("General:", panel1);


				//======== panel2 ========
				{
					panel2.setLayout(null);

					//======== scrollPane6 ========
					{
						scrollPane6.setForeground(UIManager.getColor("Button.background"));
						scrollPane6.setBorder(new TitledBorder(null, "NPCs:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
								new Font("Calibri", Font.PLAIN, 12), Color.blue));

						//======== panel6 ========
						{
							panel6.setLayout(null);

							//---- textArea7 ----
							textArea7.setBackground(UIManager.getColor("Button.background"));
							textArea7.setText("Here you have to set which monsters the bot is going to attack. NOTE: some of the same NPCs have different IDs for example a chicken can\ncan have the forms(1, 2, 3). ");
							textArea7.setEditable(false);
							textArea7.setFont(new Font("Calibri", Font.PLAIN, 13));
							panel6.add(textArea7);
							textArea7.setBounds(5, 0, 770, 40);

							//======== scrollPane1 ========
							{

								//---- list1 ----
								list1.setModel(new AbstractListModel() {
									/**
									 *
									 */
									private static final long serialVersionUID = 1L;
									String[] values = {
											"Nothing"
									};

									public int getSize() {
										return values.length;
									}

									public Object getElementAt(int i) {
										return values[i];
									}
								});
								list1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								list1.addListSelectionListener(new ListSelectionListener() {
									public void valueChanged(ListSelectionEvent e) {
										list1ValueChanged(e);
									}
								});
								scrollPane1.setViewportView(list1);
							}
							panel6.add(scrollPane1);
							scrollPane1.setBounds(5, 60, 285, 250);

							//======== scrollPane2 ========
							{

								//---- list2 ----
								list2.setModel(new AbstractListModel() {
									/**
									 *
									 */
									private static final long serialVersionUID = 1L;
									String[] values = {
											"Nothing"
									};

									public int getSize() {
										return values.length;
									}

									public Object getElementAt(int i) {
										return values[i];
									}
								});
								list2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								list2.addListSelectionListener(new ListSelectionListener() {
									public void valueChanged(ListSelectionEvent e) {
										list2ValueChanged(e);
									}
								});
								scrollPane2.setViewportView(list2);
							}
							panel6.add(scrollPane2);
							scrollPane2.setBounds(440, 60, 315, 250);

							//---- textField12 ----
							textField12.setText("NPCs Nearby");
							textField12.setBackground(new Color(240, 240, 240));
							textField12.setCaretColor(new Color(240, 240, 240));
							textField12.setBorder(null);
							textField12.setEditable(false);
							textField12.setFont(new Font("Calibri", Font.BOLD, 13));
							panel6.add(textField12);
							textField12.setBounds(5, 40, 85, 15);

							//---- button6 ----
							button6.setText("Refresh");
							button6.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									button6ActionPerformed(e);
								}
							});
							panel6.add(button6);
							button6.setBounds(95, 40, 90, 15);

							//---- textField23 ----
							textField23.setText("NPCs attacking:");
							textField23.setBackground(new Color(240, 240, 240));
							textField23.setCaretColor(new Color(240, 240, 240));
							textField23.setBorder(null);
							textField23.setEditable(false);
							textField23.setFont(new Font("Calibri", Font.BOLD, 13));
							panel6.add(textField23);
							textField23.setBounds(440, 40, 85, 15);

							{ // compute preferred size
								Dimension preferredSize = new Dimension();
								for (int i = 0; i < panel6.getComponentCount(); i++) {
									Rectangle bounds = panel6.getComponent(i).getBounds();
									preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
									preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
								}
								Insets insets = panel6.getInsets();
								preferredSize.width += insets.right;
								preferredSize.height += insets.bottom;
								panel6.setMinimumSize(preferredSize);
								panel6.setPreferredSize(preferredSize);
							}
						}
						scrollPane6.setViewportView(panel6);
					}
					panel2.add(scrollPane6);
					scrollPane6.setBounds(5, 5, 795, 340);

					{ // compute preferred size
						Dimension preferredSize = new Dimension();
						for (int i = 0; i < panel2.getComponentCount(); i++) {
							Rectangle bounds = panel2.getComponent(i).getBounds();
							preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
							preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
						}
						Insets insets = panel2.getInsets();
						preferredSize.width += insets.right;
						preferredSize.height += insets.bottom;
						panel2.setMinimumSize(preferredSize);
						panel2.setPreferredSize(preferredSize);
					}
				}
				tabbedPane1.addTab("NPCs:", panel2);


				//======== panel7 ========
				{
					panel7.setLayout(null);

					//======== scrollPane11 ========
					{
						scrollPane11.setForeground(UIManager.getColor("Button.background"));
						scrollPane11.setBorder(new TitledBorder(null, "Looting System:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
								new Font("Calibri", Font.PLAIN, 12), Color.blue));

						//======== panel11 ========
						{
							panel11.setLayout(null);

							//---- textArea9 ----
							textArea9.setBackground(UIManager.getColor("Button.background"));
							textArea9.setText("Here you have to set which items you are looting. NOTE: if choosing \"Pickup everything above ...\" make sure the item is sellable to GE For example:\nblack mask is sellable to GE, but slayer helmet isnt. So, when its not being sold to GE you must put it in the pickup list you made on the left.");
							textArea9.setEditable(false);
							textArea9.setFont(new Font("Calibri", Font.PLAIN, 13));
							panel11.add(textArea9);
							textArea9.setBounds(0, 0, 790, 40);

							//======== scrollPane12 ========
							{
								scrollPane12.setBorder(new TitledBorder(null, "Item list:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										new Font("Calibri", Font.PLAIN, 12), Color.blue));

								//---- list5 ----
								list5.setModel(new AbstractListModel() {
									/**
									 *
									 */
									private static final long serialVersionUID = 1L;
									String[] values = {
											"Nothing"
									};

									public int getSize() {
										return values.length;
									}

									public Object getElementAt(int i) {
										return values[i];
									}
								});
								list5.setBackground(UIManager.getColor("Button.background"));
								list5.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								list5.addListSelectionListener(new ListSelectionListener() {
									public void valueChanged(ListSelectionEvent e) {
										list5ValueChanged(e);
									}
								});
								scrollPane12.setViewportView(list5);
							}
							panel11.add(scrollPane12);
							scrollPane12.setBounds(300, 40, 485, 280);

							//======== scrollPane8 ========
							{
								scrollPane8.setForeground(UIManager.getColor("Button.background"));
								scrollPane8.setBorder(new TitledBorder(null, "Add Item:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										new Font("Calibri", Font.PLAIN, 12), Color.blue));

								//======== panel10 ========
								{
									panel10.setLayout(null);
									panel10.add(textField8);
									textField8.setBounds(90, 5, 80, textField8.getPreferredSize().height);

									//---- textField9 ----
									textField9.setText("Item name:");
									textField9.setBackground(new Color(240, 240, 240));
									textField9.setCaretColor(new Color(240, 240, 240));
									textField9.setBorder(null);
									textField9.setEditable(false);
									textField9.setFont(new Font("Calibri", Font.BOLD, 13));
									panel10.add(textField9);
									textField9.setBounds(5, 5, 85, 20);

									//---- textField10 ----
									textField10.setText("Item ID:");
									textField10.setBackground(new Color(240, 240, 240));
									textField10.setCaretColor(new Color(240, 240, 240));
									textField10.setBorder(null);
									textField10.setEditable(false);
									textField10.setFont(new Font("Calibri", Font.BOLD, 13));
									panel10.add(textField10);
									textField10.setBounds(5, 30, 85, 20);

									//---- checkBox9 ----
									checkBox9.setText("High-Alch this item");
									panel10.add(checkBox9);
									checkBox9.setBounds(5, 50, 165, checkBox9.getPreferredSize().height);

									//---- checkBox10 ----
									checkBox10.setText("Item is stackable");
									panel10.add(checkBox10);
									checkBox10.setBounds(5, 70, 165, 23);

									//---- checkBox11 ----
									checkBox11.setText("Eat food to make space for Item");
									panel10.add(checkBox11);
									checkBox11.setBounds(5, 90, 190, 23);

									//---- button5 ----
									button5.setText("Add Item:");
									button5.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											button5ActionPerformed(e);
										}
									});
									panel10.add(button5);
									button5.setBounds(5, 115, 90, 25);

									//---- spinner7 ----
									spinner7.setModel(new SpinnerNumberModel(0, 0, 100000, 1));
									panel10.add(spinner7);
									spinner7.setBounds(90, 30, 80, spinner7.getPreferredSize().height);

									{ // compute preferred size
										Dimension preferredSize = new Dimension();
										for (int i = 0; i < panel10.getComponentCount(); i++) {
											Rectangle bounds = panel10.getComponent(i).getBounds();
											preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
											preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
										}
										Insets insets = panel10.getInsets();
										preferredSize.width += insets.right;
										preferredSize.height += insets.bottom;
										panel10.setMinimumSize(preferredSize);
										panel10.setPreferredSize(preferredSize);
									}
								}
								scrollPane8.setViewportView(panel10);
							}
							panel11.add(scrollPane8);
							scrollPane8.setBounds(5, 40, 290, 175);

							//======== scrollPane9 ========
							{
								scrollPane9.setForeground(UIManager.getColor("Button.background"));
								scrollPane9.setBorder(new TitledBorder(null, "General:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										new Font("Calibri", Font.PLAIN, 12), Color.blue));

								//======== panel12 ========
								{
									panel12.setLayout(null);

									//---- textField15 ----
									textField15.setText("|- Pickup everything above ... G");
									textField15.setBackground(new Color(240, 240, 240));
									textField15.setCaretColor(new Color(240, 240, 240));
									textField15.setBorder(null);
									textField15.setEditable(false);
									textField15.setFont(new Font("Calibri", Font.PLAIN, 13));
									panel12.add(textField15);
									textField15.setBounds(10, 25, 175, 20);

									//---- checkBox12 ----
									checkBox12.setText("Pickup items above certain price");
									panel12.add(checkBox12);
									checkBox12.setBounds(5, 0, 225, 23);

									//---- checkBox1 ----
									checkBox1.setText("Pickup Clue scrolls");
									panel12.add(checkBox1);
									checkBox1.setBounds(5, 45, 180, checkBox1.getPreferredSize().height);
									panel12.add(spinner6);
									spinner6.setBounds(210, 25, 65, spinner6.getPreferredSize().height);

									{ // compute preferred size
										Dimension preferredSize = new Dimension();
										for (int i = 0; i < panel12.getComponentCount(); i++) {
											Rectangle bounds = panel12.getComponent(i).getBounds();
											preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
											preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
										}
										Insets insets = panel12.getInsets();
										preferredSize.width += insets.right;
										preferredSize.height += insets.bottom;
										panel12.setMinimumSize(preferredSize);
										panel12.setPreferredSize(preferredSize);
									}
								}
								scrollPane9.setViewportView(panel12);
							}
							panel11.add(scrollPane9);
							scrollPane9.setBounds(5, 215, 290, 105);

							{ // compute preferred size
								Dimension preferredSize = new Dimension();
								for (int i = 0; i < panel11.getComponentCount(); i++) {
									Rectangle bounds = panel11.getComponent(i).getBounds();
									preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
									preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
								}
								Insets insets = panel11.getInsets();
								preferredSize.width += insets.right;
								preferredSize.height += insets.bottom;
								panel11.setMinimumSize(preferredSize);
								panel11.setPreferredSize(preferredSize);
							}
						}
						scrollPane11.setViewportView(panel11);
					}
					panel7.add(scrollPane11);
					scrollPane11.setBounds(0, 0, 805, 385);

					{ // compute preferred size
						Dimension preferredSize = new Dimension();
						for (int i = 0; i < panel7.getComponentCount(); i++) {
							Rectangle bounds = panel7.getComponent(i).getBounds();
							preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
							preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
						}
						Insets insets = panel7.getInsets();
						preferredSize.width += insets.right;
						preferredSize.height += insets.bottom;
						panel7.setMinimumSize(preferredSize);
						panel7.setPreferredSize(preferredSize);
					}
				}
				tabbedPane1.addTab("Looting System:", panel7);


				//======== panel8 ========
				{
					panel8.setLayout(null);

					//======== scrollPane7 ========
					{
						scrollPane7.setForeground(UIManager.getColor("Button.background"));
						scrollPane7.setBorder(new TitledBorder(null, "Settings:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
								new Font("Calibri", Font.PLAIN, 12), Color.blue));

						//======== panel9 ========
						{
							panel9.setLayout(null);

							//======== scrollPane13 ========
							{
								scrollPane13.setForeground(UIManager.getColor("Button.background"));
								scrollPane13.setBorder(new TitledBorder(null, "Healing:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										new Font("Calibri", Font.PLAIN, 12), Color.blue));

								//======== panel14 ========
								{
									panel14.setLayout(null);

									//---- textField16 ----
									textField16.setText("Heal between Min %, Max % of maximum LP");
									textField16.setBackground(new Color(240, 240, 240));
									textField16.setCaretColor(new Color(240, 240, 240));
									textField16.setBorder(null);
									textField16.setEditable(false);
									textField16.setFont(new Font("Calibri", Font.PLAIN, 13));
									panel14.add(textField16);
									textField16.setBounds(5, 0, 240, 20);

									//---- textField18 ----
									textField18.setText("Min %:");
									textField18.setBackground(new Color(240, 240, 240));
									textField18.setCaretColor(new Color(240, 240, 240));
									textField18.setBorder(null);
									textField18.setEditable(false);
									textField18.setFont(new Font("Calibri", Font.BOLD, 13));
									panel14.add(textField18);
									textField18.setBounds(10, 20, 50, 20);

									//---- textField19 ----
									textField19.setText("Max %:");
									textField19.setBackground(new Color(240, 240, 240));
									textField19.setCaretColor(new Color(240, 240, 240));
									textField19.setBorder(null);
									textField19.setEditable(false);
									textField19.setFont(new Font("Calibri", Font.BOLD, 13));
									panel14.add(textField19);
									textField19.setBounds(135, 20, 50, 20);

									//---- checkBox21 ----
									checkBox21.setText("Use B2P");
									panel14.add(checkBox21);
									checkBox21.setBounds(5, 60, 185, 23);

									//---- checkBox15 ----
									checkBox15.setText("Use Guthans");
									panel14.add(checkBox15);
									checkBox15.setBounds(5, 80, 185, 23);

									//---- checkBox14 ----
									checkBox14.setText("Use Food");
									panel14.add(checkBox14);
									checkBox14.setBounds(5, 40, 185, 23);

									//---- spinner3 ----
									spinner3.setModel(new SpinnerNumberModel(25, 0, 100, 2));
									panel14.add(spinner3);
									spinner3.setBounds(65, 20, 45, spinner3.getPreferredSize().height);

									//---- spinner4 ----
									spinner4.setModel(new SpinnerNumberModel(75, 0, 100, 2));
									panel14.add(spinner4);
									spinner4.setBounds(190, 20, 45, spinner4.getPreferredSize().height);

									//---- textArea24 ----
									textArea24.setBackground(UIManager.getColor("Button.background"));
									textArea24.setEditable(false);
									textArea24.setFont(new Font("Calibri", Font.BOLD, 13));
									textArea24.setLineWrap(true);
									textArea24.setWrapStyleWord(true);
									textArea24.setText("Drink a zip of your pot if your current level is lower than ... + real level:");
									panel14.add(textArea24);
									textArea24.setBounds(5, 115, 180, 55);

									//---- spinner2 ----
									spinner2.setModel(new SpinnerNumberModel(0, 0, 25, 1));
									panel14.add(spinner2);
									spinner2.setBounds(190, 120, 50, spinner2.getPreferredSize().height);

									//---- textArea23 ----
									textArea23.setBackground(UIManager.getColor("Button.background"));
									textArea23.setEditable(false);
									textArea23.setFont(new Font("Calibri", Font.PLAIN, 13));
									textArea23.setLineWrap(true);
									textArea23.setWrapStyleWord(true);
									textArea23.setText("Drink a zip of your pot, if your level is .... levels above your real Level. Example you fill in pot up 5 levels above real level: (5+99) = 104, so it  drinks a zip of your pot if your current level is lower than 104.");
									panel14.add(textArea23);
									textArea23.setBounds(5, 170, 225, 110);

									{ // compute preferred size
										Dimension preferredSize = new Dimension();
										for (int i = 0; i < panel14.getComponentCount(); i++) {
											Rectangle bounds = panel14.getComponent(i).getBounds();
											preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
											preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
										}
										Insets insets = panel14.getInsets();
										preferredSize.width += insets.right;
										preferredSize.height += insets.bottom;
										panel14.setMinimumSize(preferredSize);
										panel14.setPreferredSize(preferredSize);
									}
								}
								scrollPane13.setViewportView(panel14);
							}
							panel9.add(scrollPane13);
							scrollPane13.setBounds(5, 0, 260, 310);

							//======== scrollPane49 ========
							{
								scrollPane49.setForeground(UIManager.getColor("Button.background"));
								scrollPane49.setBorder(new TitledBorder(null, "Location of Monsters", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										new Font("Calibri", Font.PLAIN, 12), Color.blue));

								//======== panel47 ========
								{
									panel47.setLayout(null);

									//---- button20 ----
									button20.setText("Set Main Tile:");
									button20.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											button20ActionPerformed(e);
										}
									});
									panel47.add(button20);
									button20.setBounds(135, 20, 100, 20);

									//---- textField36 ----
									textField36.setText("Here you have to set your combat zone.");
									textField36.setBackground(new Color(240, 240, 240));
									textField36.setCaretColor(new Color(240, 240, 240));
									textField36.setBorder(null);
									textField36.setEditable(false);
									textField36.setFont(new Font("Calibri", Font.PLAIN, 13));
									panel47.add(textField36);
									textField36.setBounds(0, 0, 225, 15);

									//---- textField40 ----
									textField40.setText("(0, 0)");
									textField40.setBackground(new Color(240, 240, 240));
									textField40.setCaretColor(new Color(240, 240, 240));
									textField40.setBorder(null);
									textField40.setEditable(false);
									textField40.setFont(new Font("Calibri", Font.BOLD, 13));
									panel47.add(textField40);
									textField40.setBounds(0, 20, 130, 20);

									//---- textField44 ----
									textField44.setText("Tiles around Main tile:");
									textField44.setBackground(new Color(240, 240, 240));
									textField44.setCaretColor(new Color(240, 240, 240));
									textField44.setBorder(null);
									textField44.setEditable(false);
									textField44.setFont(new Font("Calibri", Font.BOLD, 13));
									panel47.add(textField44);
									textField44.setBounds(0, 45, 125, 20);

									//---- spinner5 ----
									spinner5.setModel(new SpinnerNumberModel(10, 0, 100, 1));
									panel47.add(spinner5);
									spinner5.setBounds(135, 45, 100, spinner5.getPreferredSize().height);

									{ // compute preferred size
										Dimension preferredSize = new Dimension();
										for (int i = 0; i < panel47.getComponentCount(); i++) {
											Rectangle bounds = panel47.getComponent(i).getBounds();
											preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
											preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
										}
										Insets insets = panel47.getInsets();
										preferredSize.width += insets.right;
										preferredSize.height += insets.bottom;
										panel47.setMinimumSize(preferredSize);
										panel47.setPreferredSize(preferredSize);
									}
								}
								scrollPane49.setViewportView(panel47);
							}
							panel9.add(scrollPane49);
							scrollPane49.setBounds(265, 115, 250, 95);

							//======== scrollPane10 ========
							{
								scrollPane10.setForeground(UIManager.getColor("Button.background"));
								scrollPane10.setBorder(new TitledBorder(null, "General:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										new Font("Calibri", Font.PLAIN, 12), Color.blue));

								//======== panel13 ========
								{
									panel13.setLayout(null);

									//---- checkBox13 ----
									checkBox13.setText("Multi - Combat area");
									panel13.add(checkBox13);
									checkBox13.setBounds(0, 0, 145, 23);

									//---- checkBox18 ----
									checkBox18.setText("Use Emergency Teleport");
									panel13.add(checkBox18);
									checkBox18.setBounds(0, 20, 145, 23);

									//---- checkBox19 ----
									checkBox19.setText("Use Emergency Tab");
									panel13.add(checkBox19);
									checkBox19.setBounds(0, 40, 145, 23);

									//---- checkBox20 ----
									checkBox20.setText("Bury Bones");
									panel13.add(checkBox20);
									checkBox20.setBounds(0, 60, 145, 23);

									{ // compute preferred size
										Dimension preferredSize = new Dimension();
										for (int i = 0; i < panel13.getComponentCount(); i++) {
											Rectangle bounds = panel13.getComponent(i).getBounds();
											preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
											preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
										}
										Insets insets = panel13.getInsets();
										preferredSize.width += insets.right;
										preferredSize.height += insets.bottom;
										panel13.setMinimumSize(preferredSize);
										panel13.setPreferredSize(preferredSize);
									}
								}
								scrollPane10.setViewportView(panel13);
							}
							panel9.add(scrollPane10);
							scrollPane10.setBounds(265, 0, 250, 115);

							{ // compute preferred size
								Dimension preferredSize = new Dimension();
								for (int i = 0; i < panel9.getComponentCount(); i++) {
									Rectangle bounds = panel9.getComponent(i).getBounds();
									preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
									preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
								}
								Insets insets = panel9.getInsets();
								preferredSize.width += insets.right;
								preferredSize.height += insets.bottom;
								panel9.setMinimumSize(preferredSize);
								panel9.setPreferredSize(preferredSize);
							}
						}
						scrollPane7.setViewportView(panel9);
					}
					panel8.add(scrollPane7);
					scrollPane7.setBounds(5, 5, 795, 340);

					{ // compute preferred size
						Dimension preferredSize = new Dimension();
						for (int i = 0; i < panel8.getComponentCount(); i++) {
							Rectangle bounds = panel8.getComponent(i).getBounds();
							preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
							preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
						}
						Insets insets = panel8.getInsets();
						preferredSize.width += insets.right;
						preferredSize.height += insets.bottom;
						panel8.setMinimumSize(preferredSize);
						panel8.setPreferredSize(preferredSize);
					}
				}
				tabbedPane1.addTab("Settings:", panel8);


				//======== panel3 ========
				{
					panel3.setLayout(null);

					//======== scrollPane48 ========
					{
						scrollPane48.setForeground(UIManager.getColor("Button.background"));
						scrollPane48.setBorder(new TitledBorder(null, "Special Attacks", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
								new Font("Calibri", Font.PLAIN, 12), Color.blue));

						//======== panel46 ========
						{
							panel46.setLayout(null);

							//---- checkBox22 ----
							checkBox22.setText("Use Special,  at ... %:");
							panel46.add(checkBox22);
							checkBox22.setBounds(5, 0, 140, 20);

							//---- checkBox23 ----
							checkBox23.setText("Use Secondary weapon");
							panel46.add(checkBox23);
							checkBox23.setBounds(5, 20, 155, 20);

							//---- textField30 ----
							textField30.setText("Secondary weapon ID:");
							textField30.setBackground(new Color(240, 240, 240));
							textField30.setCaretColor(new Color(240, 240, 240));
							textField30.setBorder(null);
							textField30.setEditable(false);
							textField30.setFont(new Font("Calibri", Font.BOLD, 13));
							panel46.add(textField30);
							textField30.setBounds(5, 70, 125, 20);

							//---- comboBox6 ----
							comboBox6.setModel(new DefaultComboBoxModel(new String[]{
									"25%",
									"50%",
									"75%",
									"100%"
							}));
							panel46.add(comboBox6);
							comboBox6.setBounds(170, 5, 55, 20);

							//---- comboBox8 ----
							comboBox8.setModel(new DefaultComboBoxModel(new String[]{
									"First weapon",
									"Style 1",
									"Style 2",
									"Style 3",
									"Style 4"
							}));
							panel46.add(comboBox8);
							comboBox8.setBounds(5, 95, 90, 20);

							//---- comboBox9 ----
							comboBox9.setModel(new DefaultComboBoxModel(new String[]{
									"Secondary weapon",
									"Style 1",
									"Style 2",
									"Style 3",
									"Style 4"
							}));
							panel46.add(comboBox9);
							comboBox9.setBounds(105, 95, 120, 20);

							//---- spinner14 ----
							spinner14.setModel(new SpinnerNumberModel(0, 0, 1000000, 1));
							panel46.add(spinner14);
							spinner14.setBounds(150, 70, 75, 20);

							//---- textField35 ----
							textField35.setText("Primary weapon ID:");
							textField35.setBackground(new Color(240, 240, 240));
							textField35.setCaretColor(new Color(240, 240, 240));
							textField35.setBorder(null);
							textField35.setEditable(false);
							textField35.setFont(new Font("Calibri", Font.BOLD, 13));
							panel46.add(textField35);
							textField35.setBounds(5, 45, 125, 20);

							//---- spinner15 ----
							spinner15.setModel(new SpinnerNumberModel(0, 0, 1000000, 1));
							panel46.add(spinner15);
							spinner15.setBounds(150, 45, 75, 20);

							//======== scrollPane24 ========
							{
								scrollPane24.setViewportBorder(null);
								scrollPane24.setBorder(null);

								//---- textArea22 ----
								textArea22.setBackground(UIManager.getColor("Button.background"));
								textArea22.setText("*MUST READ* Before using SPECIAL. This feature makes it possible to do special attacks on both primary weapons and secondary weapons. If you use your special on your primary weapon, you only have to tick the \"Use special, att ... %\" and select when you want to use your special. If your using a secondary weapon as your special attack you have to fill in the rest. The last option is if you want to change your styles, when switching weapons for special attack. Leave it like this if you dont want that.  Style 1 = Upper left, Style 2 = Upper right, Style 3 = Lower left, Style 4 = Lower right. ");
								textArea22.setEditable(false);
								textArea22.setFont(new Font("Calibri", Font.PLAIN, 13));
								textArea22.setLineWrap(true);
								textArea22.setWrapStyleWord(true);
								scrollPane24.setViewportView(textArea22);
							}
							panel46.add(scrollPane24);
							scrollPane24.setBounds(245, 0, 520, 125);

							{ // compute preferred size
								Dimension preferredSize = new Dimension();
								for (int i = 0; i < panel46.getComponentCount(); i++) {
									Rectangle bounds = panel46.getComponent(i).getBounds();
									preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
									preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
								}
								Insets insets = panel46.getInsets();
								preferredSize.width += insets.right;
								preferredSize.height += insets.bottom;
								panel46.setMinimumSize(preferredSize);
								panel46.setPreferredSize(preferredSize);
							}
						}
						scrollPane48.setViewportView(panel46);
					}
					panel3.add(scrollPane48);
					scrollPane48.setBounds(10, 5, 785, 155);

					{ // compute preferred size
						Dimension preferredSize = new Dimension();
						for (int i = 0; i < panel3.getComponentCount(); i++) {
							Rectangle bounds = panel3.getComponent(i).getBounds();
							preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
							preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
						}
						Insets insets = panel3.getInsets();
						preferredSize.width += insets.right;
						preferredSize.height += insets.bottom;
						panel3.setMinimumSize(preferredSize);
						panel3.setPreferredSize(preferredSize);
					}
				}
				tabbedPane1.addTab("Special Attacks", panel3);


				//======== panel15 ========
				{
					panel15.setLayout(null);

					//======== scrollPane19 ========
					{
						scrollPane19.setForeground(UIManager.getColor("Button.background"));
						scrollPane19.setBorder(new TitledBorder(null, "Info:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
								new Font("Calibri", Font.PLAIN, 12), Color.blue));

						//======== panel21 ========
						{
							panel21.setLayout(null);

							//---- textArea13 ----
							textArea13.setBackground(UIManager.getColor("Button.background"));
							textArea13.setText("Here you can create/edit and save your Banking Method. This Banking system works like this. It will execute every Banking part one by\none. NOTE: Do not make this if youre not banking. And make sure the banking choise in category settings is set to false.");
							textArea13.setEditable(false);
							textArea13.setFont(new Font("Calibri", Font.PLAIN, 13));
							textArea13.setLineWrap(true);
							panel21.add(textArea13);
							textArea13.setBounds(5, 0, 770, 40);

							//---- textField26 ----
							textField26.setText("Load:");
							textField26.setBackground(new Color(240, 240, 240));
							textField26.setCaretColor(new Color(240, 240, 240));
							textField26.setBorder(null);
							textField26.setEditable(false);
							textField26.setFont(new Font("Calibri", Font.BOLD, 13));
							panel21.add(textField26);
							textField26.setBounds(5, 40, 35, 20);

							//---- comboBox3 ----
							comboBox3.addItemListener(new ItemListener() {
								public void itemStateChanged(ItemEvent e) {
									comboBox3ItemStateChanged(e);
								}
							});
							panel21.add(comboBox3);
							comboBox3.setBounds(45, 40, 175, 20);

							//---- button15 ----
							button15.setText("Save");
							button15.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									button15ActionPerformed(e);
								}
							});
							panel21.add(button15);
							button15.setBounds(720, 40, 60, 20);

							//---- textField27 ----
							textField27.setText("Save:");
							textField27.setBackground(new Color(240, 240, 240));
							textField27.setCaretColor(new Color(240, 240, 240));
							textField27.setBorder(null);
							textField27.setEditable(false);
							textField27.setFont(new Font("Calibri", Font.BOLD, 13));
							panel21.add(textField27);
							textField27.setBounds(510, 40, 40, 20);
							panel21.add(textField28);
							textField28.setBounds(555, 40, 160, 20);

							{ // compute preferred size
								Dimension preferredSize = new Dimension();
								for (int i = 0; i < panel21.getComponentCount(); i++) {
									Rectangle bounds = panel21.getComponent(i).getBounds();
									preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
									preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
								}
								Insets insets = panel21.getInsets();
								preferredSize.width += insets.right;
								preferredSize.height += insets.bottom;
								panel21.setMinimumSize(preferredSize);
								panel21.setPreferredSize(preferredSize);
							}
						}
						scrollPane19.setViewportView(panel21);
					}
					panel15.add(scrollPane19);
					scrollPane19.setBounds(0, 0, 795, 90);

					//======== scrollPane20 ========
					{
						scrollPane20.setForeground(UIManager.getColor("Button.background"));
						scrollPane20.setBorder(new TitledBorder(null, "Path maker:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
								new Font("Calibri", Font.PLAIN, 12), Color.blue));

						//======== panel22 ========
						{
							panel22.setLayout(null);

							//======== scrollPane21 ========
							{
								scrollPane21.setForeground(UIManager.getColor("Button.background"));
								scrollPane21.setBorder(new TitledBorder(null, "The Bank parts:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										new Font("Calibri", Font.PLAIN, 12), Color.blue));

								//---- list4 ----
								list4.setBackground(UIManager.getColor("Button.background"));
								list4.setModel(new AbstractListModel() {
									/**
									 *
									 */
									private static final long serialVersionUID = 1L;
									String[] values = {
											"Nothing"
									};

									public int getSize() {
										return values.length;
									}

									public Object getElementAt(int i) {
										return values[i];
									}
								});
								list4.addListSelectionListener(new ListSelectionListener() {
									public void valueChanged(ListSelectionEvent e) {
										list4ValueChanged(e);
									}
								});
								scrollPane21.setViewportView(list4);
							}
							panel22.add(scrollPane21);
							scrollPane21.setBounds(0, 0, 375, 205);

							//======== scrollPane22 ========
							{
								scrollPane22.setForeground(UIManager.getColor("Button.background"));
								scrollPane22.setBorder(new TitledBorder(null, "Make new Banking part:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										new Font("Calibri", Font.PLAIN, 12), Color.blue));

								//======== panel23 ========
								{
									panel23.setLayout(null);

									//---- textArea14 ----
									textArea14.setBackground(UIManager.getColor("Button.background"));
									textArea14.setEditable(false);
									textArea14.setFont(new Font("Calibri", Font.PLAIN, 13));
									textArea14.setText("Click a certain thing to create a Banking part.");
									panel23.add(textArea14);
									textArea14.setBounds(5, 0, 300, 20);

									//---- button19 ----
									button19.setText("Deposit:");
									button19.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											button19ActionPerformed(e);
										}
									});
									panel23.add(button19);
									button19.setBounds(10, 20, 180, 25);

									//---- button22 ----
									button22.setText("Withdraw:");
									button22.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											button22ActionPerformed(e);
										}
									});
									panel23.add(button22);
									button22.setBounds(210, 20, 180, 25);

									{ // compute preferred size
										Dimension preferredSize = new Dimension();
										for (int i = 0; i < panel23.getComponentCount(); i++) {
											Rectangle bounds = panel23.getComponent(i).getBounds();
											preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
											preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
										}
										Insets insets = panel23.getInsets();
										preferredSize.width += insets.right;
										preferredSize.height += insets.bottom;
										panel23.setMinimumSize(preferredSize);
										panel23.setPreferredSize(preferredSize);
									}
								}
								scrollPane22.setViewportView(panel23);
							}
							panel22.add(scrollPane22);
							scrollPane22.setBounds(375, 125, 405, 80);

							//======== scrollPane23 ========
							{
								scrollPane23.setForeground(UIManager.getColor("Button.background"));
								scrollPane23.setBorder(new TitledBorder(null, "Load the Paths:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										new Font("Calibri", Font.PLAIN, 12), Color.blue));

								//======== panel24 ========
								{
									panel24.setLayout(null);

									//---- textArea15 ----
									textArea15.setBackground(UIManager.getColor("Button.background"));
									textArea15.setEditable(false);
									textArea15.setFont(new Font("Calibri", Font.PLAIN, 13));
									textArea15.setText("Load your paths:");
									panel24.add(textArea15);
									textArea15.setBounds(5, 25, 300, 20);
									panel24.add(comboBox4);
									comboBox4.setBounds(215, 45, 175, 20);

									//---- textField31 ----
									textField31.setText("Walking to Monsters:");
									textField31.setBackground(new Color(240, 240, 240));
									textField31.setCaretColor(new Color(240, 240, 240));
									textField31.setBorder(null);
									textField31.setEditable(false);
									textField31.setFont(new Font("Calibri", Font.BOLD, 13));
									panel24.add(textField31);
									textField31.setBounds(5, 45, 190, 20);

									//---- textField34 ----
									textField34.setText("Walking to Bank:");
									textField34.setBackground(new Color(240, 240, 240));
									textField34.setCaretColor(new Color(240, 240, 240));
									textField34.setBorder(null);
									textField34.setEditable(false);
									textField34.setFont(new Font("Calibri", Font.BOLD, 13));
									panel24.add(textField34);
									textField34.setBounds(5, 70, 190, 20);
									panel24.add(comboBox5);
									comboBox5.setBounds(215, 70, 175, 20);

									//---- checkBox17 ----
									checkBox17.setText("Use Bank");
									panel24.add(checkBox17);
									checkBox17.setBounds(5, 0, 100, 23);

									{ // compute preferred size
										Dimension preferredSize = new Dimension();
										for (int i = 0; i < panel24.getComponentCount(); i++) {
											Rectangle bounds = panel24.getComponent(i).getBounds();
											preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
											preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
										}
										Insets insets = panel24.getInsets();
										preferredSize.width += insets.right;
										preferredSize.height += insets.bottom;
										panel24.setMinimumSize(preferredSize);
										panel24.setPreferredSize(preferredSize);
									}
								}
								scrollPane23.setViewportView(panel24);
							}
							panel22.add(scrollPane23);
							scrollPane23.setBounds(375, 0, 405, 120);

							{ // compute preferred size
								Dimension preferredSize = new Dimension();
								for (int i = 0; i < panel22.getComponentCount(); i++) {
									Rectangle bounds = panel22.getComponent(i).getBounds();
									preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
									preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
								}
								Insets insets = panel22.getInsets();
								preferredSize.width += insets.right;
								preferredSize.height += insets.bottom;
								panel22.setMinimumSize(preferredSize);
								panel22.setPreferredSize(preferredSize);
							}
						}
						scrollPane20.setViewportView(panel22);
					}
					panel15.add(scrollPane20);
					scrollPane20.setBounds(0, 90, 795, 235);

					{ // compute preferred size
						Dimension preferredSize = new Dimension();
						for (int i = 0; i < panel15.getComponentCount(); i++) {
							Rectangle bounds = panel15.getComponent(i).getBounds();
							preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
							preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
						}
						Insets insets = panel15.getInsets();
						preferredSize.width += insets.right;
						preferredSize.height += insets.bottom;
						panel15.setMinimumSize(preferredSize);
						panel15.setPreferredSize(preferredSize);
					}
				}
				tabbedPane1.addTab("Banking:", panel15);


				//======== panel16 ========
				{
					panel16.setLayout(null);

					//======== scrollPane14 ========
					{
						scrollPane14.setForeground(UIManager.getColor("Button.background"));
						scrollPane14.setBorder(new TitledBorder(null, "Info:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
								new Font("Calibri", Font.PLAIN, 12), Color.blue));

						//======== panel17 ========
						{
							panel17.setLayout(null);

							//---- textField20 ----
							textField20.setText("Load:");
							textField20.setBackground(new Color(240, 240, 240));
							textField20.setCaretColor(new Color(240, 240, 240));
							textField20.setBorder(null);
							textField20.setEditable(false);
							textField20.setFont(new Font("Calibri", Font.BOLD, 13));
							panel17.add(textField20);
							textField20.setBounds(5, 65, 35, 20);

							//---- comboBox2 ----
							comboBox2.addItemListener(new ItemListener() {
								public void itemStateChanged(ItemEvent e) {
									comboBox2ItemStateChanged(e);
								}
							});
							panel17.add(comboBox2);
							comboBox2.setBounds(40, 65, 175, 20);

							//---- button8 ----
							button8.setText("Save");
							button8.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									button8ActionPerformed(e);
								}
							});
							panel17.add(button8);
							button8.setBounds(720, 65, 60, 20);

							//---- textField21 ----
							textField21.setText("Save:");
							textField21.setBackground(new Color(240, 240, 240));
							textField21.setCaretColor(new Color(240, 240, 240));
							textField21.setBorder(null);
							textField21.setEditable(false);
							textField21.setFont(new Font("Calibri", Font.BOLD, 13));
							panel17.add(textField21);
							textField21.setBounds(510, 65, 40, 20);
							panel17.add(textField22);
							textField22.setBounds(550, 65, 160, 20);

							//======== scrollPane30 ========
							{

								//---- textArea8 ----
								textArea8.setBackground(UIManager.getColor("Button.background"));
								textArea8.setText("***MUST READ**** \nHere you can create/edit and save your paths. This path system works like this. 1. It starts with looking for interfaces(Advanced) and if one of those are valid it will execute that Path part. 2. The bot checks for all Path Part areas and if you are in one of those Path part areas it will execute that Path part. 3. Otherwise it will use the teleport Path part. \nNOTE: You must always insert an area except if you use a teleport Path part. \nNOTE 2: If you use a path let the path walk outside of your Path part area into the next Path part area.\nNOTE 3: If walking to bank you dont have to use the a Object/NPC Path Part to open the bank. The only requirement is that the path leads to a tile, which is closer than 5 tiles to the nearest bank Object/NPC.");
								textArea8.setEditable(false);
								textArea8.setFont(new Font("Calibri", Font.PLAIN, 13));
								textArea8.setLineWrap(true);
								textArea8.setWrapStyleWord(true);
								scrollPane30.setViewportView(textArea8);
							}
							panel17.add(scrollPane30);
							scrollPane30.setBounds(5, 0, 765, 60);

							{ // compute preferred size
								Dimension preferredSize = new Dimension();
								for (int i = 0; i < panel17.getComponentCount(); i++) {
									Rectangle bounds = panel17.getComponent(i).getBounds();
									preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
									preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
								}
								Insets insets = panel17.getInsets();
								preferredSize.width += insets.right;
								preferredSize.height += insets.bottom;
								panel17.setMinimumSize(preferredSize);
								panel17.setPreferredSize(preferredSize);
							}
						}
						scrollPane14.setViewportView(panel17);
					}
					panel16.add(scrollPane14);
					scrollPane14.setBounds(0, 0, 795, 115);

					//======== scrollPane15 ========
					{
						scrollPane15.setForeground(UIManager.getColor("Button.background"));
						scrollPane15.setBorder(new TitledBorder(null, "Path maker:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
								new Font("Calibri", Font.PLAIN, 12), Color.blue));

						//======== panel18 ========
						{
							panel18.setLayout(null);

							//======== scrollPane16 ========
							{
								scrollPane16.setForeground(UIManager.getColor("Button.background"));
								scrollPane16.setBorder(new TitledBorder(null, "The Path parts:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										new Font("Calibri", Font.PLAIN, 12), Color.blue));

								//---- list3 ----
								list3.setBackground(UIManager.getColor("Button.background"));
								list3.setModel(new AbstractListModel() {

									private static final long serialVersionUID = 1L;

									String[] values = {
											"Nothing"
									};

									public int getSize() {
										return values.length;
									}

									public Object getElementAt(int i) {
										return values[i];
									}
								});
								list3.addListSelectionListener(new ListSelectionListener() {
									public void valueChanged(ListSelectionEvent e) {
										list3ValueChanged(e);
									}
								});
								scrollPane16.setViewportView(list3);
							}
							panel18.add(scrollPane16);
							scrollPane16.setBounds(0, 0, 375, 165);

							//======== scrollPane17 ========
							{
								scrollPane17.setForeground(UIManager.getColor("Button.background"));
								scrollPane17.setBorder(new TitledBorder(null, "Make new path part:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										new Font("Calibri", Font.PLAIN, 12), Color.blue));

								//======== panel20 ========
								{
									panel20.setLayout(null);

									//---- textArea12 ----
									textArea12.setBackground(UIManager.getColor("Button.background"));
									textArea12.setEditable(false);
									textArea12.setFont(new Font("Calibri", Font.PLAIN, 13));
									textArea12.setText("Click a certain thing to create a Path part.");
									panel20.add(textArea12);
									textArea12.setBounds(5, 0, 300, 20);

									//---- button10 ----
									button10.setText("Path:");
									button10.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											button10ActionPerformed(e);
										}
									});
									panel20.add(button10);
									button10.setBounds(95, 25, 100, 25);

									//---- button11 ----
									button11.setText("Object/NPC:");
									button11.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											button11ActionPerformed(e);
										}
									});
									panel20.add(button11);
									button11.setBounds(195, 25, 95, 25);

									//---- button12 ----
									button12.setText("Teleportation:");
									button12.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											button12ActionPerformed(e);
										}
									});
									panel20.add(button12);
									button12.setBounds(290, 25, 100, 25);

									//---- textField24 ----
									textField24.setText("Easy:");
									textField24.setBackground(new Color(240, 240, 240));
									textField24.setCaretColor(new Color(240, 240, 240));
									textField24.setBorder(null);
									textField24.setEditable(false);
									textField24.setFont(new Font("Calibri", Font.BOLD, 13));
									panel20.add(textField24);
									textField24.setBounds(10, 25, 70, 25);

									//---- textField25 ----
									textField25.setText("Advanced:");
									textField25.setBackground(new Color(240, 240, 240));
									textField25.setCaretColor(new Color(240, 240, 240));
									textField25.setBorder(null);
									textField25.setEditable(false);
									textField25.setFont(new Font("Calibri", Font.BOLD, 13));
									panel20.add(textField25);
									textField25.setBounds(10, 50, 70, 25);

									//---- button13 ----
									button13.setText("Interfaces");
									button13.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											button13ActionPerformed(e);
										}
									});
									panel20.add(button13);
									button13.setBounds(95, 50, 100, 25);

									{ // compute preferred size
										Dimension preferredSize = new Dimension();
										for (int i = 0; i < panel20.getComponentCount(); i++) {
											Rectangle bounds = panel20.getComponent(i).getBounds();
											preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
											preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
										}
										Insets insets = panel20.getInsets();
										preferredSize.width += insets.right;
										preferredSize.height += insets.bottom;
										panel20.setMinimumSize(preferredSize);
										panel20.setPreferredSize(preferredSize);
									}
								}
								scrollPane17.setViewportView(panel20);
							}
							panel18.add(scrollPane17);
							scrollPane17.setBounds(375, 0, 405, 105);

							{ // compute preferred size
								Dimension preferredSize = new Dimension();
								for (int i = 0; i < panel18.getComponentCount(); i++) {
									Rectangle bounds = panel18.getComponent(i).getBounds();
									preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
									preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
								}
								Insets insets = panel18.getInsets();
								preferredSize.width += insets.right;
								preferredSize.height += insets.bottom;
								panel18.setMinimumSize(preferredSize);
								panel18.setPreferredSize(preferredSize);
							}
						}
						scrollPane15.setViewportView(panel18);
					}
					panel16.add(scrollPane15);
					scrollPane15.setBounds(0, 120, 795, 195);

					{ // compute preferred size
						Dimension preferredSize = new Dimension();
						for (int i = 0; i < panel16.getComponentCount(); i++) {
							Rectangle bounds = panel16.getComponent(i).getBounds();
							preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
							preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
						}
						Insets insets = panel16.getInsets();
						preferredSize.width += insets.right;
						preferredSize.height += insets.bottom;
						panel16.setMinimumSize(preferredSize);
						panel16.setPreferredSize(preferredSize);
					}
				}
				tabbedPane1.addTab("Path Maker:", panel16);

			}
			contentPane.add(tabbedPane1);
			tabbedPane1.setBounds(15, 65, 810, 380);

			//======== scrollPane5 ========
			{
				scrollPane5.setForeground(UIManager.getColor("Button.background"));
				scrollPane5.setBorder(new TitledBorder(null, "Status:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
						new Font("Calibri", Font.PLAIN, 12), Color.blue));

				//======== panel5 ========
				{
					panel5.setLayout(null);

					//---- textField4 ----
					textField4.setText("Latest Version:");
					textField4.setBackground(new Color(240, 240, 240));
					textField4.setCaretColor(new Color(240, 240, 240));
					textField4.setBorder(null);
					textField4.setEditable(false);
					textField4.setFont(new Font("Calibri", Font.PLAIN, 13));
					panel5.add(textField4);
					textField4.setBounds(5, 0, 85, 10);

					//---- textField3 ----
					textField3.setText("Your Version:");
					textField3.setBackground(new Color(240, 240, 240));
					textField3.setCaretColor(new Color(240, 240, 240));
					textField3.setBorder(null);
					textField3.setEditable(false);
					textField3.setFont(new Font("Calibri", Font.PLAIN, 13));
					panel5.add(textField3);
					textField3.setBounds(5, 15, 80, 10);

					//---- textField32 ----
					textField32.setText("0.0.1");
					textField32.setBorder(null);
					textField32.setBackground(new Color(240, 240, 240));
					textField32.setEditable(false);
					textField32.setForeground(Color.gray);
					textField32.setFont(new Font("Calibri", Font.PLAIN, 13));
					panel5.add(textField32);
					textField32.setBounds(95, 15, 35, 10);

					//---- textField33 ----
					textField33.setText("True");
					textField33.setBorder(null);
					textField33.setBackground(new Color(240, 240, 240));
					textField33.setEditable(false);
					textField33.setForeground(new Color(0, 185, 0));
					textField33.setFont(new Font("Calibri", Font.PLAIN, 13));
					panel5.add(textField33);
					textField33.setBounds(95, 0, 35, 10);

					//---- button3 ----
					button3.setText("Start script:");
					button3.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							button3ActionPerformed(e);
						}
					});
					panel5.add(button3);
					button3.setBounds(140, 0, 90, 25);

					//---- button4 ----
					button4.setText("Exit:");
					button4.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							button4ActionPerformed(e);
						}
					});
					panel5.add(button4);
					button4.setBounds(240, 0, 90, 25);

					{ // compute preferred size
						Dimension preferredSize = new Dimension();
						for (int i = 0; i < panel5.getComponentCount(); i++) {
							Rectangle bounds = panel5.getComponent(i).getBounds();
							preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
							preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
						}
						Insets insets = panel5.getInsets();
						preferredSize.width += insets.right;
						preferredSize.height += insets.bottom;
						panel5.setMinimumSize(preferredSize);
						panel5.setPreferredSize(preferredSize);
					}
				}
				scrollPane5.setViewportView(panel5);
			}
			contentPane.add(scrollPane5);
			scrollPane5.setBounds(385, 5, 440, 60);

			contentPane.setPreferredSize(new Dimension(855, 485));
			pack();
			setLocationRelativeTo(getOwner());
			// JFormDesigner - End of component initialization  //GEN-END:initComponents
		}

		// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
		private JTextField textField1;
		private JTextField textField2;
		private JTabbedPane tabbedPane1;
		private JPanel panel1;
		private JScrollPane scrollPane3;
		private JScrollPane scrollPane50;
		private JTextArea textArea2;
		private JScrollPane scrollPane4;
		private JPanel panel4;
		private JTextArea textArea6;
		private JTextField textField5;
		private JTextField textField6;
		private JTextField textField7;
		private JComboBox comboBox1;
		private JButton button2;
		private JPanel panel2;
		private JScrollPane scrollPane6;
		private JPanel panel6;
		private JTextArea textArea7;
		private JScrollPane scrollPane1;
		private JList list1;
		private JScrollPane scrollPane2;
		private JList list2;
		private JTextField textField12;
		private JButton button6;
		private JTextField textField23;
		private JPanel panel7;
		private JScrollPane scrollPane11;
		private JPanel panel11;
		private JTextArea textArea9;
		private JScrollPane scrollPane12;
		private JList list5;
		private JScrollPane scrollPane8;
		private JPanel panel10;
		private JTextField textField8;
		private JTextField textField9;
		private JTextField textField10;
		private JCheckBox checkBox9;
		private JCheckBox checkBox10;
		private JCheckBox checkBox11;
		private JButton button5;
		private JSpinner spinner7;
		private JScrollPane scrollPane9;
		private JPanel panel12;
		private JTextField textField15;
		private JCheckBox checkBox12;
		private JCheckBox checkBox1;
		private JSpinner spinner6;
		private JPanel panel8;
		private JScrollPane scrollPane7;
		private JPanel panel9;
		private JScrollPane scrollPane13;
		private JPanel panel14;
		private JTextField textField16;
		private JTextField textField18;
		private JTextField textField19;
		private JCheckBox checkBox21;
		private JCheckBox checkBox15;
		private JCheckBox checkBox14;
		private JSpinner spinner3;
		private JSpinner spinner4;
		private JTextArea textArea24;
		private JSpinner spinner2;
		private JTextArea textArea23;
		private JScrollPane scrollPane49;
		private JPanel panel47;
		private JButton button20;
		private JTextField textField36;
		private JTextField textField40;
		private JTextField textField44;
		private JSpinner spinner5;
		private JScrollPane scrollPane10;
		private JPanel panel13;
		private JCheckBox checkBox13;
		private JCheckBox checkBox18;
		private JCheckBox checkBox19;
		private JCheckBox checkBox20;
		private JPanel panel3;
		private JScrollPane scrollPane48;
		private JPanel panel46;
		private JCheckBox checkBox22;
		private JCheckBox checkBox23;
		private JTextField textField30;
		private JComboBox comboBox6;
		private JComboBox comboBox8;
		private JComboBox comboBox9;
		private JSpinner spinner14;
		private JTextField textField35;
		private JSpinner spinner15;
		private JScrollPane scrollPane24;
		private JTextArea textArea22;
		private JPanel panel15;
		private JScrollPane scrollPane19;
		private JPanel panel21;
		private JTextArea textArea13;
		private JTextField textField26;
		private JComboBox comboBox3;
		private JButton button15;
		private JTextField textField27;
		private JTextField textField28;
		private JScrollPane scrollPane20;
		private JPanel panel22;
		private JScrollPane scrollPane21;
		private JList list4;
		private JScrollPane scrollPane22;
		private JPanel panel23;
		private JTextArea textArea14;
		private JButton button19;
		private JButton button22;
		private JScrollPane scrollPane23;
		private JPanel panel24;
		private JTextArea textArea15;
		private JComboBox comboBox4;
		private JTextField textField31;
		private JTextField textField34;
		private JComboBox comboBox5;
		private JCheckBox checkBox17;
		private JPanel panel16;
		private JScrollPane scrollPane14;
		private JPanel panel17;
		private JTextField textField20;
		private JComboBox comboBox2;
		private JButton button8;
		private JTextField textField21;
		private JTextField textField22;
		private JScrollPane scrollPane30;
		private JTextArea textArea8;
		private JScrollPane scrollPane15;
		private JPanel panel18;
		private JScrollPane scrollPane16;
		private JList list3;
		private JScrollPane scrollPane17;
		private JPanel panel20;
		private JTextArea textArea12;
		private JButton button10;
		private JButton button11;
		private JButton button12;
		private JTextField textField24;
		private JTextField textField25;
		private JButton button13;
		private JScrollPane scrollPane5;
		private JPanel panel5;
		private JTextField textField4;
		private JTextField textField3;
		private JTextField textField32;
		private JTextField textField33;
		private JButton button3;
		private JButton button4;
		// JFormDesigner - End of variables declaration  //GEN-END:variables
	}

	public void mouseClicked(MouseEvent e) {
		RSComponent BackGround = interfaces.getComponent(137, 0);
		int startOfPaintingY = BackGround.getAbsoluteY() + BackGround.getHeight() - 16;
		int startOfPaintingX = BackGround.getAbsoluteX() + BackGround.getWidth() - (70 * extraSystem.paint.allScreens.length);
		for (int i = 0; i < extraSystem.paint.allScreens.length; i++) {
			if (extraSystem.paint.pointIsInArea(new Point(e.getX(), e.getY()), startOfPaintingX + (70 * i), startOfPaintingX + 70 + (70 * i), startOfPaintingY, startOfPaintingY + 20)) {
				extraSystem.paint.paintScreen = extraSystem.paint.allScreens[i];
			}
		}
	}

	public void mousePressed(MouseEvent e) {

	}

	public void mouseReleased(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}
}