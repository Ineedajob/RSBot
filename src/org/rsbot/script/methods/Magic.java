package org.rsbot.script.methods;

import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSInterface;

/**
 * Magic tab and spell related operations.
 */
public class Magic extends MethodProvider {

	/**
	 * Provides Magic Book(s) Information.
	 *
	 * @author Jacmob, Aut0r
	 */
	public static enum Book {

		MODERN(192), ANCIENT(193), LUNAR(430);

		private int id;

		Book(int id) {
			this.id = id;
		}

		public int getInterfaceID() {
			return id;
		}

	}

	// Buttons
	public static final int INTERFACE_DEFENSIVE_STANCE = 2;
	public static final int INTERFACE_SHOW_COMBAT_SPELLS = 7;
	public static final int INTERFACE_SHOW_TELEPORT_SPELLS = 9;
	public static final int INTERFACE_SHOW_MISC_SPELLS = 11;
	public static final int INTERFACE_SHOW_SKILL_SPELLS = 13;
	public static final int INTERFACE_SORT_BY_LEVEL = 15;
	public static final int INTERFACE_SORT_BY_COMBAT = 16;
	public static final int INTERFACE_SORT_BY_TELEPORTS = 17;

	// Normal spells
	public static final int SPELL_HOME_TELEPORT = 24;
	public static final int SPELL_WIND_STRIKE = 25;
	public static final int SPELL_CONFUSE = 26;
	public static final int SPELL_ENCHANT_CROSSBOW_BOLT = 27;
	public static final int SPELL_WATER_STRIKE = 28;
	public static final int SPELL_LVL1_ENCHANT = 29;
	public static final int SPELL_EARTH_STRIKE = 30;
	public static final int SPELL_WEAKEN = 31;
	public static final int SPELL_FIRE_STRIKE = 32;
	public static final int SPELL_BONES_TO_BANANAS = 33;
	public static final int SPELL_WIND_BOLT = 34;
	public static final int SPELL_CURSE = 35;
	public static final int SPELL_BIND = 36;
	public static final int SPELL_MOBILISING_ARMIES_TELEPORT = 37;
	public static final int SPELL_LOW_LEVEL_ALCHEMY = 38;
	public static final int SPELL_WATER_BOLT = 39;
	public static final int SPELL_VARROCK_TELEPORT = 40;
	public static final int SPELL_LVL2_ENCHANT = 41;
	public static final int SPELL_EARTH_BOLT = 42;
	public static final int SPELL_LUMBRIDGE_TELEPORT = 43;
	public static final int SPELL_TELEKINETIC_GRAB = 44;
	public static final int SPELL_FIRE_BOLT = 45;
	public static final int SPELL_FALADOR_TELEPORT = 46;
	public static final int SPELL_CRUMBLE_UNDEAD = 47;
	public static final int SPELL_TELEPORT_TO_HOUSE = 48;
	public static final int SPELL_WIND_BLAST = 49;
	public static final int SPELL_SUPERHEAT_ITEM = 50;
	public static final int SPELL_CAMELOT_TELEPORT = 51;
	public static final int SPELL_WATER_BLAST = 52;
	public static final int SPELL_LVL3_ENCHANT = 53;
	public static final int SPELL_IBAN_BLAST = 54;
	public static final int SPELL_SNARE = 55;
	public static final int SPELL_MAGIC_DART = 56;
	public static final int SPELL_ARDOUGNE_TELEPORT = 57;
	public static final int SPELL_EARTH_BLAST = 58;
	public static final int SPELL_HIGH_LEVEL_ALCHEMY = 59;
	public static final int SPELL_CHARGE_WATER_ORB = 60;
	public static final int SPELL_LVL4_ENCHANT = 61;
	public static final int SPELL_WATCHTOWER_TELEPORT = 62;
	public static final int SPELL_FIRE_BLAST = 63;
	public static final int SPELL_CHARGE_EARTH_ORB = 64;
	public static final int SPELL_BONES_TO_PEACHES = 65;
	public static final int SPELL_SARADOMIN_STRIKE = 66;
	public static final int SPELL_CLAWS_OF_GUTHIX = 67;
	public static final int SPELL_FLAMES_OF_ZAMORAK = 68;
	public static final int SPELL_TROLLHEIM_TELEPORT = 69;
	public static final int SPELL_WIND_WAVE = 70;
	public static final int SPELL_CHARGE_FIRE_ORB = 71;
	public static final int SPELL_APE_ATOL_TELEPORT = 72;
	public static final int SPELL_WATER_WAVE = 73;
	public static final int SPELL_CHARGE_AIR_ORB = 74;
	public static final int SPELL_VULNERABILITY = 75;
	public static final int SPELL_LVL5_ENCHANT = 76;
	public static final int SPELL_EARTH_WAVE = 77;
	public static final int SPELL_ENFEEBLE = 78;
	public static final int SPELL_TELEOTHER_LUMBRIDGE = 79;
	public static final int SPELL_FIRE_WAVE = 80;
	public static final int SPELL_ENTANGLE = 81;
	public static final int SPELL_STUN = 82;
	public static final int SPELL_CHARGE = 83;
	public static final int SPELL_WIND_SURGE = 84;
	public static final int SPELL_TELEOTHER_FALADOR = 85;
	public static final int SPELL_TELEPORT_BLOCK = 86;
	public static final int SPELL_WATER_SURGE = 87;
	public static final int SPELL_LVL6_ENCHANT = 88;
	public static final int SPELL_TELEOTHER_CAMELOT = 89;
	public static final int SPELL_EARTH_SURGE = 90;
	public static final int SPELL_FIRE_SURGE = 91;

	// Ancient spells
	public static final int SPELL_ICE_RUSH = 20;
	public static final int SPELL_ICE_BLITZ = 21;
	public static final int SPELL_ICE_BURST = 22;
	public static final int SPELL_ICE_BARRAGE = 23;
	public static final int SPELL_BLOOD_RUSH = 24;
	public static final int SPELL_BLOOD_BLITZ = 25;
	public static final int SPELL_BLOOD_BURST = 26;
	public static final int SPELL_BLOOD_BARRAGE = 27;
	public static final int SPELL_SMOKE_RUSH = 28;
	public static final int SPELL_SMOKE_BLITZ = 29;
	public static final int SPELL_SMOKE_BURST = 30;
	public static final int SPELL_SMOKE_BARRAGE = 31;
	public static final int SPELL_SHADOW_RUSH = 32;
	public static final int SPELL_SHADOW_BLITZ = 33;
	public static final int SPELL_SHADOW_BURST = 34;
	public static final int SPELL_SHADOW_BARRAGE = 35;
	public static final int SPELL_MIASMIC_RUSH = 36;
	public static final int SPELL_MIASMIC_BLITZ = 37;
	public static final int SPELL_MIASMIC_BURST = 38;
	public static final int SPELL_MIASMIC_BARRAGE = 39;

	public static final int SPELL_PADDEWWA_TELEPORT = 40;
	public static final int SPELL_SENNTISTEN_TELEPORT = 41;
	public static final int SPELL_KHARYRLL_TELEPRT = 42;
	public static final int SPELL_LASSER_TELEPORT = 43;
	public static final int SPELL_DAREEYAK_TELEPORT = 44;
	public static final int SPELL_CARRALLANGER_TELEPORT = 45;
	public static final int SPELL_ANNAKARL_TELEPORT = 46;
	public static final int SPELL_GHORROCK_TELEPORT = 47;
	public static final int SPELL_ANCIENT_HOME_TELEPORT = 48;

	// Lunar spells
	public static final int SPELL_BARBARIAN_TELEPORT = 22;
	public static final int SPELL_CURE_OTHER = 23;
	public static final int SPELL_FERTILE_SOIL = 24;
	public static final int SPELL_CURE_GROUP = 25;
	public static final int SPELL_NPC_CONTACT = 26;
	public static final int SPELL_ENERGY_TRANSFER = 27;
	public static final int SPELL_MONSTERS_EXAMINE = 28;
	public static final int SPELL_HUMIDIFY = 29;
	public static final int SPELL_HUNTER_KIT = 30;
	public static final int SPELL_STATE_SPY = 31;
	public static final int SPELL_DREAM = 32;
	public static final int SPELL_PLANK_MAKE = 33;
	public static final int SPELL_SPELLBOOK_SWAP = 34;
	public static final int SPELL_MAGIC_IMBUE = 35;
	public static final int SPELL_VENGEANCE = 36;
	public static final int SPELL_BAKE_PIE = 37;
	public static final int SPELL_HOME_TELEPORT_LUNAR = 38;
	public static final int SPELL_FISHING_GUILD_TELEPORT = 39;
	public static final int SPELL_KHAZARD_TELEPORT = 40;
	public static final int SPELL_VENGEANCE_OTHER = 41;
	public static final int SPELL_MOONCLAN_TELEPORT = 42;
	public static final int SPELL_CATHERBY_TELEPORT = 43;
	public static final int SPELL_STRING_JEWELLERY = 44;
	public static final int SPELL_CURE_ME = 45;
	public static final int SPELL_WATERBIRTH_TELEPORT = 46;
	public static final int SPELL_SUPERGLASS_MAKE = 47;
	public static final int SPELL_BOOTS_POTION_SHARE = 48;
	public static final int SPELL_STAT_RESTORE_POT_SHARE = 49;
	public static final int SPELL_ICE_PLATEAU_TELEPORT = 50;
	public static final int SPELL_HEAL_OTHER = 51;
	public static final int SPELL_HEAL_GROUP = 52;
	public static final int SPELL_OURANIA_TELEPORT = 53;
	public static final int SPELL_CURE_PLANT = 54;
	public static final int SPELL_TELE_GROUP_MOONCLAN = 55;
	public static final int SPELL_TELE_GROUP_WATERBIRTH = 56;
	public static final int SPELL_TELE_GROUP_BARBARIAN = 57;
	public static final int SPELL_TELE_GROUP_KHAZARD = 58;
	public static final int SPELL_TELE_GROUP_FISHING_GUILD = 59;
	public static final int SPELL_TELE_GROUP_CATHERBY = 60;


	Magic(final MethodContext ctx) {
		super(ctx);
	}

	/**
	 * Checks whether or not a spell is selected.
	 *
	 * @return <tt>true</tt> if a spell is selected; otherwise <tt>false</tt>.
	 */
	public boolean isSpellSelected() {
		return methods.client.isSpellSelected();
	}

	/**
	 * Determines whether a spell is currently set to autocast.
	 *
	 * @return <tt>true</tt> if autocasting; otherwise <tt>false</tt>.
	 */
	public boolean isAutoCasting() {
		return methods.combat.getFightMode() == 4;
	}

	/**
	 * Clicks a specified spell, opens magic tab if not open and uses interface
	 * of the spell to click it, so it works if the spells are layout in any
	 * sway.
	 *
	 * @param spell The spell to cast.
	 * @return <tt>true</tt> if the spell was clicked; otherwise <tt>false</tt>.
	 */
	public boolean castSpell(final int spell) {
		if (methods.game.getCurrentTab() != Game.TAB_MAGIC) {
			methods.game.openTab(Game.TAB_MAGIC);
			for (int i = 0; i < 100; i++) {
				sleep(20);
				if (methods.game.getCurrentTab() == Game.TAB_MAGIC) {
					break;
				}
			}
			sleep(random(150, 250));
		}
		if (methods.game.getCurrentTab() == Game.TAB_MAGIC) {
			RSInterface inter = getInterface();
			if (inter != null) {
				RSComponent comp = inter.getComponent(spell);
				return comp != null && comp.doAction("Cast");
			}
		}
		return false;
	}

	/**
	 * Auto-casts a spell via the magic tab.
	 *
	 * @param spell The spell to auto-cast.
	 * @return <tt>true</tt> if the "Auto-cast" interface option was clicked;
	 *         otherwise <tt>false</tt>.
	 */
	public boolean autoCastSpell(final int spell) {
		if (methods.settings.getSetting(43) != 4) {
			if (methods.game.getCurrentTab() != Game.TAB_MAGIC) {
				methods.game.openTab(Game.TAB_MAGIC);
				sleep(random(150, 250));
			}
			RSInterface inter = getInterface();
			if (inter != null) {
				RSComponent comp = inter.getComponent(spell);
				return comp != null && comp.doAction("Autocast");
			}
		}
		return false;
	}


	/**
	 * Gets the open magic book interface.
	 *
	 * @return The current magic RSInterface.
	 */
	public RSInterface getInterface() {
		RSInterface inter = methods.interfaces.get(Book.MODERN.getInterfaceID());
		if (!inter.isValid()) {
			inter = methods.interfaces.get(Book.ANCIENT.getInterfaceID());
			if (!inter.isValid()) {
				inter = methods.interfaces.get(Book.LUNAR.getInterfaceID());
				if (!inter.isValid()) {
					return null;
				}
			}
		}
		return inter;
	}

}
