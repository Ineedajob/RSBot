package org.rsbot.script.methods;

import org.rsbot.client.Node;
import org.rsbot.client.RSNPCNode;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSInterface;
import org.rsbot.script.wrappers.RSNPC;

/**
 * Summoning related operations.
 *
 * @author Unknown5000
 * @author illusion
 */
public class Summoning extends MethodProvider {

	public static final int INTERFACE_TAB_SUMMONING = 747;
	public static final int INTERFACE_TAB_SUMMONING_CHILD = 2;
	public static final int INTERFACE_DETAILS = 662;
	public static final int INTERFACE_OPTIONS = 880;

	public static final int INTERFACE_FOLLOWER_DETAILS = 9;
	public static final int INTERFACE_SPECIAL_MOVE = 99;
	public static final int INTERFACE_ATTACK = 14;
	public static final int INTERFACE_CALL_FOLLOWER = 17;
	public static final int INTERFACE_DISMISS_FOLLOWER = 18;
	public static final int INTERFACE_TAKE_BOB = 12;
	public static final int INTERFACE_RENEW_FAMILIAR = 13;

	/**
	 * Familiar constants.
	 *
	 * @author illusion
	 */
	public static enum Familiar {

		SPIRIT_WOLF("Spirit Wolf", 1, 6, 3, "Howl", 0), DREADFOWL("Dreadfowl",
				4, 4, 3, "Dreadfowl Strike", 0), SPIRIT_SPIDER(
				"Spirit spider",
				10, 15, 6, "Egg Spawn", 0), THORNY_SNAIL("Thorny snail", 13,
				16, 3, "Slime Spray", 3), GRANITE_CRAB("Granite crab", 16, 18,
				12, "Stony Shell",
				0), MOSQUITO("Mosquito",
				17, 12, 3,
				"Pester",
				0), DESERT_WYRM(
				"Desert wyrm", 18, 19, 6,
				"Electric Lash", 0), SPIRIT_SCORPION("Spirit scorpion", 19, 17,
				6, "Venom Shot", 0), SPIRIT_TZ_KIH("Spirit Tz-Kih", 22, 18, 6,
				"Fireball Assault",
				0), ALBINO_RAT("Albino rat", 23,
				22, 6,
				"Cheese Feast",
				0), SPIRIT_KALPHITE(
				"Spirit kalphite", 25, 22,
				6, "Sandstorm", 6), COMPOST_MOUND("Compost Mound", 28, 24, 12,
				"Generate Compost", 0), GIANT_CHINCHOMPA("Giant chinchompa",
				29, 31, 3, "Explode",
				0), VAMPIRE_BAT(
				"Vampire bat", 31, 33, 4,
				"Vampire Touch", 0), HONEY_BADGER("Honey badger", 32, 25, 12,
				"Insane Ferocity", 0), BEAVER("Beaver", 33, 27, 3, "Multichop",
				0), VOID_RAVAGER("Void ravager", 34, 27,
				3, "Call to Arms",
				0), VOID_SHIFTER(
				"Void shifter", 34, 94, 3, "Call to Arms", 0), VOID_SPINNER(
				"Void spinner", 34, 27, 3, "Call to Arms", 0), VOID_TORCHER(
				"Void torcher", 34, 94, 3, "Call to Arms", 0), BRONZE_MINOTAUR(
				"Bronze minotaur", 36, 30, 6, "Bronze Bull Rush", 0), BULL_ANT(
				"Bull ant", 40, 30, 12, "Unburden", 9), MACAW("Macaw", 41, 31,
				12, "Herbcall", 0), EVIL_TURNIP("Evil turnip", 42, 30, 6,
				"Evil Flames",
				0), SPIRIT_COCKATRICE(
				"Spirit cockatrice", 43,
				36, 3, "Petrifying Gaze", 0), IRON_MINOTAUR("Iron minotaur",
				46, 37, 6, "Iron Bull Rush", 0), PYRELORD("Pyrelord", 46,
				32,
				6, "Immense Heat",
				0), MAGPIE(
				"Magpie", 47, 34, 12,
				"Thieving Fingers", 0), BLOATED_LEECH("Bloated leech", 49, 34,
				6, "Blood Drain", 0), SPIRIT_TERRORBIRD("Spirit terrorbird",
				52, 36, 8, "Tireless Run",
				12), ABYSSAL_PARASITE(
				"Abyssal parasite", 54, 30, 6, "Abyssal Drain", 7), SPIRIT_JELLY(
				"Spirit jelly", 55, 43, 6, "Dissolve", 0), IBIS("Ibis", 56, 38,
				12, "Fish Rain", 0), STEEL_MINOTAUR("Steel minotaur",
				56, 46,
				6,
				"Steel Bull Rush",
				0), SPIRIT_GRAAHK(
				"Spirit graahk", 57,
				49, 3, "Goad", 0), SPIRIT_KYATT("Spirit kyatt", 57, 49, 3,
				"Ambush", 0), SPIRIT_LARUPIA("Spirit larupia", 57, 49, 6,
				"Rending", 0), KARAMTHULHU_OVERLORD(
				"Karamthulhu overlord", 58,
				44, 3, "Doomspere Device", 0), SMOKE_DEVIL("Smoke devil", 61,
				48, 6, "Dust Cloud", 0), ABYSSAL_LURKER("Abyssal lurker", 62,
				41, 20,
				"Abyssal Stealth",
				7), SPIRIT_COBRA(
				"Spirit cobra", 63,
				56, 3, "Ophidian Incubation", 0), STRANGER_PLANT(
				"Stranger plant", 64, 49, 6, "Poisonous Blast", 0), BARKER_TOAD(
				"Barker toad", 66, 8, 6, "Toad Bark", 0), MITHRIL_MINOTAUR(
				"Mithril minotaur", 66, 55, 6, "Mithril Bull Rush", 0), WAR_TORTOISE(
				"War tortoise", 67, 43, 20, "Testudo", 18), BUNYIP("Bunyip",
				68, 44, 3, "Swallow Whole", 0), FRUIT_BAT(
				"Fruit bat", 69, 45,
				6, "Fruitfall", 0), RAVENOUS_LOCUST("Ravenous locust", 70, 24,
				12, "Famine", 0), ARCTIC_BEAR("Arctic bear", 71, 28, 6,
				"Arctic Blast", 0), PHOENIX("Phoenix",
				72, 30,
				12,
				"Rise from the Ashes",
				0), OBSIDIAN_GOLEM(
				"Obsidian golem", 73,
				55, 12, "Volcanic Strength", 0), GRANITE_LOBSTER(
				"Granite lobster", 74, 47, 6, "Crushing Claw", 0), PRAYING_MANTIS(
				"Praying mantis", 75, 69, 6, "Mantis Strike", 0), ADAMANT_MINOTAUR(
				"Adamant minotaur", 76, 66, 6, "Adamant Bull Rush", 0), FORGE_REGENT(
				"Forge regent", 76, 45, 6, "Inferno", 0), TALON_BEAST(
				"Talon beast", 77, 49, 6, "Deadly Claw", 0), GIANT_ENT(
				"Giant ent", 78, 49, 6, "Acorn Missile", 0), FIRE_TITAN(
				"Fire titan", 79, 62, 20, "Titan's Constitution", 0), ICE_TITAN(
				"Ice titan", 79, 64, 20, "Titan's Constitution", 0), MOSS_TITAN(
				"Moss titan", 79, 58, 20, "Titan's Constitution", 0), HYDRA(
				"Hydra", 80, 49, 6, "Regrowth", 0), SPIRIT_DAGANNOTH(
				"Spirit dagannoth", 83, 57, 6, "Spike Shot", 0), LAVA_TITAN(
				"Lava titan", 83, 61, 4, "Ebon Thunder", 0), SWAMP_TITAN(
				"Swamp titan", 85, 56, 6, "Swamp Plague", 0), RUNE_MINOTAUR(
				"Rune minotaur", 86, 151, 6, "Rune Bull Rush", 0), UNICORN_STALLION(
				"Unicorn stallion", 88, 54, 20, "Healing Aura", 0), GEYSER_TITAN(
				"Geyser titan", 89, 69, 6, "Boil", 0), WOLPERTIGER(
				"Wolpertiger", 92, 62, 20, "Magic Focus", 0), ABYSSAL_TITAN(
				"Abyssal titan", 93, 32, 6, "Essence Shipment", 7), IRON_TITAN(
				"Iron titan", 95, 60, 12, "Iron Within", 0), PACK_YAK(
				"Pack yak", 96, 58, 12, "Winter Storage", 30), STEEL_TITAN(
				"Steel titan", 99, 64, 12, "Steel of Legends", 0);

		private final String name;
		private final int requiredLevel;
		private final int time;
		private final int requiredSpecialPoints;
		private final String scrollName;
		private final int bobSpace;
		private RSNPC npcObject;

		Familiar(String name, int requiredLevel, int time, int sp,
		         String scrollName, int space) {
			this.name = name;
			this.requiredLevel = requiredLevel;
			this.time = time;
			this.requiredSpecialPoints = sp;
			this.scrollName = scrollName;
			this.bobSpace = space;
		}

		public int getRequiredLevel() {
			return requiredLevel;
		}

		public int getTime() {
			return time;
		}

		public String getName() {
			return name;
		}

		public String getScrollName() {
			return scrollName;
		}

		public int getRequiredSpecialPoints() {
			return requiredSpecialPoints;
		}

		public int getInventorySpace() {
			return bobSpace;
		}

		public boolean canStore() {
			return bobSpace != 0;
		}

		public void setNPCObject(RSNPC npc) {
			this.npcObject = npc;
		}

		public RSNPC getNPC() {
			return npcObject;
		}

	}

	public Summoning(final MethodContext ctx) {
		super(ctx);
	}

	/**
	 * Returns the number of summoning points left.
	 *
	 * @return The number of summoning points left.
	 */
	public int getSummoningPoints() {
		return Integer.parseInt(methods.interfaces.getComponent(
				INTERFACE_TAB_SUMMONING, 5).getText());
	}

	/**
	 * Returns the current summoning level.
	 *
	 * @return The current summoning level.
	 */
	public int getLevel() {
		return methods.skills.getRealLevel(Skills.SUMMONING);
	}

	/**
	 * Checks whether you have a familiar.
	 *
	 * @return <tt>true</tt> if you have a familiar.
	 */
	public boolean isFamiliarSummoned() {
		return methods.interfaces.getComponent(INTERFACE_TAB_SUMMONING, 3)
				.getBackgroundColor() == 1802;
	}

	/**
	 * Does a action in the summoning skill bubble.
	 *
	 * @return <tt>true</tt> if action is performed.
	 */
	public boolean doAction(String act) {
		return methods.interfaces.get(INTERFACE_TAB_SUMMONING)
				.getComponent(INTERFACE_TAB_SUMMONING_CHILD).isValid()
				&& methods.interfaces.get(INTERFACE_TAB_SUMMONING)
				.getComponent(INTERFACE_TAB_SUMMONING_CHILD)
				.doAction(act);
	}

	/**
	 * Presses attack in the summoning tab.
	 *
	 * @return <tt>true</tt> if the action was performed.
	 */
	public boolean doAttack() {
		return methods.interfaces.getComponent(INTERFACE_TAB_SUMMONING,
				INTERFACE_TAB_SUMMONING_CHILD).doAction("Attack");
	}

	/**
	 * Casts the familiar's attack.
	 *
	 * @return <tt>true</tt> if the action was performed.
	 */
	public boolean doCast() {
		return isFamiliarSummoned()
				&& methods.inventory.getItemID(getFamiliar().getScrollName()) != -1
				&& methods.interfaces.getComponent(INTERFACE_TAB_SUMMONING,
				INTERFACE_TAB_SUMMONING_CHILD).doAction("Cast");

	}

	/**
	 * Presses cancel in the summoning tab.
	 *
	 * @return <tt>true</tt> if the action was performed.
	 */
	public boolean doCancel() {
		return methods.interfaces.getComponent(INTERFACE_TAB_SUMMONING,
				INTERFACE_TAB_SUMMONING_CHILD).doAction("Cancel");
	}

	/**
	 * Renews the familiar from the summoning tab.
	 *
	 * @return <tt>true</tt> if the action was performed.
	 */
	public boolean doRenewFamiliar() {
		return isFamiliarSummoned()
				&& methods.inventory.getItemID(getFamiliar().getName()
				+ " pouch") != -1
				&& methods.interfaces.getComponent(INTERFACE_TAB_SUMMONING,
				INTERFACE_TAB_SUMMONING_CHILD).doAction(
				"Renew Familiar");
	}

	/**
	 * Takes the BoB of the familiar.
	 *
	 * @return <tt>true</tt> if the action was performed.
	 */
	public boolean doTakeBob() {
		return methods.interfaces.getComponent(INTERFACE_TAB_SUMMONING,
				INTERFACE_TAB_SUMMONING_CHILD).doAction("Take BoB");
	}

	/**
	 * Dismisses the familiar from the summoning tab.
	 *
	 * @return <tt>true</tt> if the action was performed.
	 */
	public boolean doDismiss() {
		methods.interfaces.getComponent(INTERFACE_TAB_SUMMONING,
				INTERFACE_TAB_SUMMONING_CHILD).doAction("Dismiss");

		RSComponent dismissInterface = methods.interfaces.getComponent(228, 2);

		while (!dismissInterface.isValid()) {
			sleep(random(400, 600));

			dismissInterface = methods.interfaces.getComponent(228, 2);
		}

		return dismissInterface.doClick();
	}

	/**
	 * Calls the familiar from the summoning tab.
	 *
	 * @return <tt>true</tt> if the action was performed.
	 */
	public boolean doCallFollower() {
		return methods.interfaces.getComponent(INTERFACE_TAB_SUMMONING,
				INTERFACE_TAB_SUMMONING_CHILD).doAction("Call Follower");
	}

	/**
	 * Shows follower details from the summoning tab.
	 *
	 * @return <tt>true</tt> if the action was performed.
	 */
	public boolean doShowDetails() {
		return methods.interfaces.getComponent(INTERFACE_TAB_SUMMONING,
				INTERFACE_TAB_SUMMONING_CHILD).doAction("Follower Details");
	}

	/**
	 * Returns the time left before the familiar vanishes.
	 *
	 * @return The time left before the familiar vanishes.
	 */
	public double getTimeLeft() {
		double res = Double.parseDouble(methods.interfaces.getComponent(
				INTERFACE_DETAILS, 43).getText());

		if (res == 9999.0) {
			doShowDetails();
			sleep(random(400, 600));
			res = Double.parseDouble(methods.interfaces.getComponent(
					INTERFACE_DETAILS, 43).getText());
		}

		return res;
	}

	/**
	 * Sets the left-click option to the given action.
	 *
	 * @param action the action string
	 * @return <tt>true</tt> if action is performed.
	 */
	public boolean setLeftClickOption(String action) {
		RSInterface optionInterface = methods.interfaces.get(INTERFACE_OPTIONS);
		int index = -1;

		for (RSComponent option : optionInterface.getComponents()) {
			if (option != null && option.getText() != null) {
				if (option.containsAction(action)
						|| option.containsText(action)) {
					index = option.getIndex();
				}
			}
		}

		return index != -1 && setLeftClickOption(index);
	}

	/**
	 * Sets the left click option to the given index.
	 *
	 * @param option the option index
	 * @return <tt>true</tt> if action is performed.
	 */
	public boolean setLeftClickOption(int option) {
		methods.interfaces.getComponent(INTERFACE_TAB_SUMMONING,
				INTERFACE_TAB_SUMMONING_CHILD).doAction(
				"Select left-click option");
		sleep(random(300, 400));
		return methods.interfaces.getComponent(INTERFACE_OPTIONS, option)
				.doClick()
				&& methods.interfaces.getComponent(INTERFACE_OPTIONS, 5)
				.doClick();
	}

	/**
	 * Finds your current summoned Familiar.
	 *
	 * @return your current Familiar
	 */
	public Familiar getFamiliar() {
		for (int element : methods.client.getRSNPCIndexArray()) {
			Node node = methods.nodes.lookup(methods.client.getRSNPCNC(),
					element);
			if (node == null || !(node instanceof RSNPCNode)) {
				continue;
			}
			RSNPC npc = new RSNPC(methods, ((RSNPCNode) node).getRSNPC());
			if (npc.getInteracting() != null
					&& npc.getInteracting().equals(
					methods.players.getMyPlayer())) {
				for (Familiar f : Familiar.values()) {
					if (f != null && npc.getName().equals(f.getName())) {
						f.setNPCObject(npc);
						return f;
					}
				}
			}
		}
		return null;

	}

}