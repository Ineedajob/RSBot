import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.Magic;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.util.WindowUtil;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSModel;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSPlayer;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.util.GlobalConfiguration;

@ScriptManifest(authors = { "TerraBubble" }, keywords = "Combat", name = "FOGRunner", version = 1.5, description = "A Fist of Guthix playing script by TerraBubble")
public class FOGRunner extends Script implements PaintListener,
		MessageListener, MouseListener, MouseMotionListener {

	final ScriptManifest properties = getClass().getAnnotation(
			ScriptManifest.class);

	// The GUI
	private FOGRunner.FOGRunnerGUI gui;
	// For Mouse Paint
	public BufferedImage normal = null;
	public BufferedImage clicked = null;
	public BufferedImage upArrow = null;
	public BufferedImage downArrow = null;
	// For Main Game Stats Paint
	public int cornerNum = 5;
	public int statsPaint = 2;
	public double statsAlpha = 1.0;
	public long startTime = System.currentTimeMillis();
	public long runTime = 0;
	public long seconds = 0;
	public long minutes = 0;
	public long hours = 0;
	public int startTokens = 0;
	public int tokensPerHour = 0;
	public int tokensGained = 0;
	public int currentTokens = 0;
	public int bankedTokens = 0;
	public int gamesPlayed = 0;
	public int gamesPerHour = 0;
	public int gamesWon = 0;
	public int gamesLost = 0;
	public int userMouseX = 0;
	public int userMouseY = 0;
	public boolean userMousePressed = false;
	// Extra Game Stats Paint
	public int moreStats = 0;
	public int moreStatsOffset = 0;
	public double[] textAlpha = { 0, 0, 0, 0, 0, 0, 0, 0 };
	public int gamesWonPerHour = 0;
	public int gamesLostPerHour = 0;
	public int averageCharges = 0;
	public int averageChargesOpponent = 0;
	public int totalCharges = 0;
	public int totalChargesO = 0;
	public int startRating = 0;
	public int currentRating = 0;
	public int gainedRating = 0;
	// Status Paint
	public int statusPaint = 2;
	public double statusAlpha = 1.0;
	public String status = "Starting Up...";
	public String overallStatus = "NULL";
	// Player Exp/Levels Paint
	public int expPaint = 2;
	public double expAlpha = 1.0;
	public int[] expYs = { 345, 358, 371, 384, 397, 410, 423 };
	public int nextPlace = 0;
	public int startAttackExp = 0;
	public int startStrengthExp = 0;
	public int startDefenceExp = 0;
	public int startMagicExp = 0;
	public int startRangeExp = 0;
	public int startConstitutionExp = 0;
	public int startAttackLevel = 0;
	public int startStrengthLevel = 0;
	public int startDefenceLevel = 0;
	public int startMagicLevel = 0;
	public int startRangeLevel = 0;
	public int startConstitutionLevel = 0;
	public Rectangle attackBox = new Rectangle(7, 0, 490, 13);
	public Rectangle strengthBox = new Rectangle(attackBox.x, 0,
			attackBox.width, attackBox.height);
	public Rectangle defenceBox = new Rectangle(attackBox.x, 0,
			attackBox.width, attackBox.height);
	public Rectangle magicBox = new Rectangle(attackBox.x, 0, attackBox.width,
			attackBox.height);
	public Rectangle rangeBox = new Rectangle(attackBox.x, 0, attackBox.width,
			attackBox.height);
	public Rectangle constitutionBox = new Rectangle(attackBox.x, 0,
			attackBox.width, attackBox.height);
	public Rectangle expGradientBox = new Rectangle(attackBox.x,
			expYs[nextPlace], attackBox.width, 30);
	// Button Rectangles
	public Rectangle statusButtonRec = new Rectangle(560, 443, 51, 15);
	public Rectangle expButtonRec = new Rectangle(statusButtonRec.x + 57, 443,
			51, 15);
	public Rectangle statsButtonRec = new Rectangle(expButtonRec.x + 57, 443,
			51, 15);
	public Rectangle moreStatsButtonRec = new Rectangle(expButtonRec.x - 10,
			443 - 15, 71, 12);
	// Tile Paint
	public boolean canDrawTheirTile = false;
	public boolean canDrawInfo = false;

	// For GUI
	// Main Things
	public boolean dieScript = false;
	public BufferedImage banner = null;
	public boolean success = false;
	public final File scriptFile = new File(new File(
			GlobalConfiguration.Paths.getScriptsSourcesDirectory()),
			"FOGRunner.java");
	public final File settingsFile = new File(new File(
			GlobalConfiguration.Paths.getSettingsDirectory()), "FOGRunner.txt");
	public final File bannerFile = new File(new File(
			GlobalConfiguration.Paths.getScriptsDirectory()), "FOGRunner.png");
	public boolean loadBannerFromFile = true;
	// Misc Options
	public boolean useAntiBan = true;
	public boolean useBandages = true;
	public boolean usePrayers = false;
	public boolean useAttackPrayers = false;
	public boolean useSkinPrayers = false;
	public boolean useQuickPrayers = false;
	public String quickPrayersLong = "When Hunting";
	public boolean bankTokens = false;
	public boolean search = true;
	public boolean screenshots = true;
	public int tokensBeforeBank = 9000;
	public String tokensBeforeBankS = "9000";
	public int mouseSpeed = 7;
	// When to use teleorb vars
	public String teleorbLong = "When being Hunted";
	public String teleorb = "NULL";
	// Various vars for setting up the items to wield
	public int[] inventoryItemsIDsX = new int[28];
	public int inventoryItemsNumP;
	public int[] inventoryItemsIDsP = new int[28];
	public String[] inventoryItemsNamesP = new String[28];
	public int inventoryItemsNum = 0;
	public int[] inventoryItemsIDs = new int[39];
	public String[] inventoryItemsNames = new String[39];
	public int equipmentItemsNum;
	public int[] equipmentItemsIDsX = new int[11];
	public int[] equipmentItemsIDs = new int[11];
	public String[] equipmentItemsNames = new String[11];

	// ATTACK
	// Arrays containing the inventoryList item index positions for wielding
	// items for each combat style
	public int[] meleeListIDsAdded = new int[11];
	public int[] magicListIDsAdded = new int[11];
	public int[] rangeListIDsAdded = new int[11];
	// Arrays containing the actual inventory IDs for wielding items for each
	// Combat Style
	public int[] meleeEquipment;
	public int[] rangeEquipment;
	public int[] magicEquipment;
	// Whether Combat Styles are active
	public boolean meleeActive = true;
	public boolean magicActive = true;
	public boolean rangeActive = false;
	// Attack Options for each combat style
	public String meleeAttackStyle = "Attack EXP";
	public String magicSpell = "Auto Highest (F2P)";
	public String rangeAttackStyle = "Accurate";
	// First Attack Style to Use
	public String firstStyle = "Melee";
	// The Current Attack Style
	public String currentStyle = "Melee";
	// Items to wield vars
	public int inventoryListSelectedID;
	public String[] startStylesP = new String[3];
	public String[] startStyles1 = new String[1];
	public String[] startStyles2 = new String[2];
	public String[] startStyles3 = new String[3];

	// DEFENCE
	// Arrays containing the inventoryList item index positions for wielding
	// items for each combat style
	public int[] meleeListIDsAddedDefence = new int[11];
	public int[] magicListIDsAddedDefence = new int[11];
	public int[] rangeListIDsAddedDefence = new int[11];
	// Arrays containing the actual inventory IDs for wielding items for each
	// Combat Style
	public int[] meleeEquipmentDefence;
	public int[] rangeEquipmentDefence;
	public int[] magicEquipmentDefence;
	// Whether Combat Styles are active
	public boolean meleeActiveDefence = true;
	public boolean magicActiveDefence = true;
	public boolean rangeActiveDefence = false;
	// Whether to unequip other items in defence
	public boolean meleeUnequip = false;
	public boolean magicUnequip = false;
	public boolean rangeUnequip = false;
	// Defence Style
	public String currentStyleDefence = "Melee";
	public boolean FSGame = false;
	// Items to wield vars
	public int inventoryListSelectedIDDefence;

	// For Script
	// Spellbook Constants
	public final int MODERN_SPELLBOOK = 192;
	public final int ANCIENT_SPELLBOOK = 193;
	public final int SPELL_WIND_RUSH = 98;
	// What triggers the attack style to switch
	public boolean prayerSwitch = false;
	public boolean meleeSafeSpotSwitch = false;
	public boolean randomGameSwitch = true;
	public int nextRandomSwitch = 3;
	public boolean everyGameSwitch = false;
	public boolean hitSwitch = false;
	public int hitSwitchMinHits = 3;
	public int hitSwitchMaxHits = 5;
	public int nextHitSwitch = 4;
	public int hits = 0;
	public int lastKnownHealth = 100;
	public boolean nextRandomSwitchSet = false;
	public boolean justStarted = true;
	public boolean gotRatingInfo = true;
	// Tiles for walking to
	public RSTile entranceBefore = new RSTile(1717, 5598);
	public RSTile entrance = new RSTile(1719, 5599);
	// General bot stuff
	public boolean loginWait = false;
	public boolean meleeTimerStarted = false; // Whether the timer below has
												// started
	public org.rsbot.script.util.Timer meleeTimer = new org.rsbot.script.util.Timer(
			100); // Timer for if melee attacking (for rock bug)
	public boolean initialised = false; // Whether the script has initialised
	public boolean justPlayed = false; // Whether just played a game
	public boolean walkedOnce = false; // Whether you've already walked to a
										// tile in the waiting room
	public boolean stoneHovered = false; // Whether the mouse has already
											// hovered over the stone while
											// waiting
	// For Unequipping Items
	public final int[] eComps = { 8, 11, 14, 17, 20, 23, 26, 29, 32, 35, 38 };
	// Whether you are hunted or not variables
	public int prevHg = -1;
	public int prevHd = -1;
	public boolean roundOneHunted = false;
	public boolean switchingRole = false;
	public boolean hunted = true;
	public boolean hunted2 = true;
	// For doing initial settings at start of a round
	public boolean setVarsRound1 = false;
	public boolean setVarsRound2 = false;
	// booleans for whether the round has started up
	public boolean gone = false;
	public boolean gone2 = false;
	public boolean startedUp = false;
	public boolean startedUp2 = false;
	public int round = 1;
	// Whether antiban should happen
	public boolean antiBanTime = true;
	// 'Go!' detection
	public boolean goReceived = false;
	public org.rsbot.script.util.Timer goTimer = new org.rsbot.script.util.Timer(
			5000);
	public int canGoFailed = 0;
	public boolean overrideGoReceived = false;
	// Timer for waiting in the centre before searching for other player
	public org.rsbot.script.util.Timer t = new org.rsbot.script.util.Timer(8000);
	public org.rsbot.script.util.Timer t2 = new org.rsbot.script.util.Timer(
			30000);
	public org.rsbot.script.util.Timer s = new org.rsbot.script.util.Timer(
			10000);
	public boolean timerStarted = false;
	public int timerToUse = 1;
	// For searching for other player
	public RSTile lastKnownLoc = new RSTile(-1, -1);
	public boolean searching = false;
	public int spawnArea = 0;
	public RSTile centerTile = new RSTile(1663, 5696);
	public final int[] searchTilesXOrig = { 1663, 1677, 1688, 1682, 1659, 1644,
			1637, 1649 };
	public final int[] searchTilesYOrig = { 5717, 5708, 5692, 5677, 5671, 5681,
			5698, 5712 };
	public int[] searchTilesX = { 1663, 1677, 1688, 1682, 1659, 1644, 1637,
			1649 };
	public int[] searchTilesY = { 5717, 5708, 5692, 5677, 5671, 5681, 5698,
			5712 };
	public int searchTileOn = 0;
	// Debug mode boolean
	public boolean dmode = false;
	// For setting prayer protection and attack
	public int prayerlvl;
	public int lastPrayerSet = 0;
	public int lastAttackPrayerSet = 0;
	public int lastSkinPrayerSet = -1;
	// For setting spell
	public boolean setCombatSpells = false;
	// For random usage of teleorb
	public int tRandom = 1;
	// For banking items not allowed in the FOG arena (UBI = Unknown Bad Items)
	public String UBIString = "";
	public boolean bankingUBIs = false;
	public final int[] food = { 1895, 1893, 1891, 4293, 2142, 291, 2140, 3228,
			9980, 7223, 6297, 6293, 6295, 6299, 7521, 9988, 7228, 2878, 7568,
			2343, 1861, 13433, 315, 325, 319, 3144, 347, 355, 333, 339, 351,
			329, 3381, 361, 10136, 5003, 379, 365, 373, 7946, 385, 397, 391,
			3369, 3371, 3373, 2309, 2325, 2333, 2327, 2331, 2323, 2335, 7178,
			7180, 7188, 7190, 7198, 7200, 7208, 7210, 7218, 7220, 2003, 2011,
			2289, 2291, 2293, 2295, 2297, 2299, 2301, 2303, 1891, 1893, 1895,
			1897, 1899, 1901, 7072, 7062, 7078, 7064, 7084, 7082, 7066, 7068,
			1942, 6701, 6703, 7054, 6705, 7056, 7060, 2130, 1985, 1993, 1989,
			1978, 5763, 5765, 1913, 5747, 1905, 5739, 1909, 5743, 1907, 1911,
			5745, 2955, 5749, 5751, 5753, 5755, 5757, 5759, 5761, 2084, 2034,
			2048, 2036, 2217, 2213, 2205, 2209, 2054, 2040, 2080, 2277, 2225,
			2255, 2221, 2253, 2219, 2281, 2227, 2223, 2191, 2233, 2092, 2032,
			2074, 2030, 2281, 2235, 2064, 2028, 2187, 2185, 2229, 6883, 1971,
			4608, 1883, 1885, 1973, 15272, 6962, 1969, 403 }; // IDs of food
																// (not allowed)
	public final int[] otherNAIDs = { 434, 592 }; // IDs of other items not
													// allowed (clay, ash)
	public final int[] NAIDs = concat(food, otherNAIDs);
	// For detecting if you're in waiting room
	public final RSTile waitingRoomTile = new RSTile(1653, 5300);
	// FOG Items
	public int tokenID = 12852;
	public int teleorbID = 12855;
	public int bandagesID = 12853;
	public int[] stoneID = { 12845, 12846, 12847, 12848, 12849 };
	public RSObject stoneObject;

	// Fist of Guthix item class, with all info on an item
	class FOGItem {
		public String name;
		public int ID;
		public int tokens;
		public int price = 0;
		public boolean members;

		public FOGItem(String tehName, int tehID, int tehTokens,
				boolean isMembers) {
			name = tehName;
			ID = tehID;
			tokens = tehTokens;
			price = 0;
			members = isMembers;
		}

		public FOGItem(String tehName, int tehID, int tehTokens, int tehPrice,
				boolean isMembers) {
			name = tehName;
			ID = tehID;
			tokens = tehTokens;
			price = tehPrice;
			members = isMembers;
		}

		public boolean hasGotPrice() {
			if (price != 0) {
				return true;
			}
			return false;
		}

		// Sets the price from the GE database, returns true if successful
		public boolean setPrice() {
			int prePrice = 0;
			try {
				prePrice = grandExchange.lookup(ID).getGuidePrice();
				if (prePrice > 0) {
					price = prePrice;
					return true;
				}
			} catch (NullPointerException e) {
			}
			price = 0;
			return false;
		}

		// Gets the Coins Per Token ratio
		public int getRatio() {
			if (price > 0 && tokens > 0) {
				return (int) Math.round(((double) (price))
						/ ((double) (tokens)));
			} else {
				return 0;
			}
		}
	}

	// FOG Items You Can Purchase & Sell Degraded (IDs at 0 charge)
	// F2P
	public FOGItem druidic_mage_top = new FOGItem("Druidic Mage Top", 12899,
			300, false);
	public FOGItem druidic_mage_bottom = new FOGItem("Druidic Mage Bottom",
			12906, 200, false);
	public FOGItem druidic_mage_hood = new FOGItem("Druidic Mage Hood", 12892,
			100, false);
	public FOGItem combat_robe_top = new FOGItem("Combat Robe Top", 12976, 150,
			false);
	public FOGItem combat_robe_bottom = new FOGItem("Combat Robe Bottom",
			12983, 100, false);
	public FOGItem combat_hood = new FOGItem("Combat Hood", 12969, 50, false);
	public FOGItem green_dhide_coif = new FOGItem("Green D'Hide Coif", 12941,
			150, false);
	public FOGItem bronze_gauntlets = new FOGItem("Bronze Gauntlets", 12986,
			15, false);
	public FOGItem iron_gauntlets = new FOGItem("Iron Gauntlets", 12989, 30,
			false);
	public FOGItem steel_gauntlets = new FOGItem("Steel Gauntlets", 12992, 50,
			false);
	public FOGItem black_gauntlets = new FOGItem("Black Gauntlets", 12995, 75,
			false);
	public FOGItem mithril_gauntlets = new FOGItem("Mithril Gauntlets", 12998,
			100, false);
	public FOGItem adamant_gauntlets = new FOGItem("Adamant Gauntlets", 13001,
			150, false);
	public FOGItem rune_gauntlets = new FOGItem("Rune Gauntlets", 13004, 200,
			false);
	public FOGItem adamant_spikeshield = new FOGItem("Adamant Spikeshield",
			12913, 50, false);
	public FOGItem adamant_berserker_shield = new FOGItem(
			"Adamant Berserker Shield", 12920, 100, false);
	public FOGItem rune_spikeshield = new FOGItem("Rune Spikeshield", 12927,
			200, false);
	public FOGItem rune_berserker_shield = new FOGItem("Rune Berserker Shield",
			12934, 300, false);
	// P2P
	public FOGItem dragon_gauntlets = new FOGItem("Dragon Gauntlets", 13007,
			300, true);
	public FOGItem blue_dhide_coif = new FOGItem("Blue D'Hide Coif", 12948,
			200, true);
	public FOGItem red_dhide_coif = new FOGItem("Red D'Hide Coif", 12955, 300,
			true);
	public FOGItem black_dhide_coif = new FOGItem("Black D'Hide Coif", 12962,
			500, true);
	public FOGItem battle_robe_top = new FOGItem("Battle Robe Top", 12878,
			1500, true);
	public FOGItem battle_robe_bottom = new FOGItem("Battle Robe Bottom",
			12885, 1000, true);
	public FOGItem battle_hood = new FOGItem("Battle Hood", 12871, 250, true);

	public FOGItem[] FOGItemsF2P = { druidic_mage_top, druidic_mage_bottom,
			druidic_mage_hood, combat_robe_top, combat_robe_bottom,
			combat_hood, green_dhide_coif, bronze_gauntlets, iron_gauntlets,
			steel_gauntlets, black_gauntlets, mithril_gauntlets,
			adamant_gauntlets, rune_gauntlets, adamant_spikeshield,
			adamant_berserker_shield, rune_spikeshield, rune_berserker_shield };
	public FOGItem[] FOGItemsP2P = { dragon_gauntlets, blue_dhide_coif,
			red_dhide_coif, black_dhide_coif, battle_robe_top,
			battle_robe_bottom, battle_hood };
	public FOGItem[] FOGItems = concat(FOGItemsF2P, FOGItemsP2P);

	public FOGItem f2pItem;
	public FOGItem p2pItem;

	public int[] v = new int[100];

	@SuppressWarnings("serial")
	class FOGRunnerGUI extends JFrame implements ListSelectionListener,
			ActionListener {

		// For Entire GUI:
		int screenHeight = (int) Toolkit.getDefaultToolkit().getScreenSize()
				.getHeight();
		private JPanel jContentPane = null;
		private JPanel MainTab = null;
		private JPanel OptionsTab = null;
		private JPanel PrayersTab = null;
		private JPanel CombatStylesTab = null;
		private JPanel CombatStylesTabDefence = null;
		private JPanel CombatStylesTabScrollable = null;
		private JPanel CombatStylesTabScrollableDefence = null;
		private JPanel MoneyTab = null;
		private JTabbedPane jTabbedPane = null;
		private JButton Start = null;
		private JButton Cancel = null;
		private JButton Save = null;
		private JButton Load = null;
		private final JLabel statusLabel = new JLabel();

		// For Main Tab:
		private final JLabel imageLabel = new JLabel();
		private final JLabel notesLabel = new JLabel();
		private final JLabel note1 = new JLabel();
		private final JLabel note2 = new JLabel();
		private final JLabel note3 = new JLabel();
		private final JLabel continueNote = new JLabel();
		private final JLabel authorNote = new JLabel();
		private final JLabel versionNote = new JLabel();

		// For Options Tab:
		private JCheckBox chckbxAntiBan;
		private JCheckBox chckbxBandages;
		private JCheckBox chckbxDmode;
		private JCheckBox chckbxSearch;
		private JCheckBox chckbxScreenshots;
		private JCheckBox chckbxBankTokens;
		private final JLabel teleOrbLabel = new JLabel();
		private final JComboBox teleOrbBox = new JComboBox();
		private final JTextArea bankTokensNum = new JTextArea();
		private final JLabel mouseSpeedLabel = new JLabel();
		private final JLabel mouseSpeedLabel2 = new JLabel();
		private final JTextArea mouseSpeedBox = new JTextArea();
		private final JLabel continueNoteOptions = new JLabel();

		// For Prayers Tab
		private JCheckBox chckbxPrayers;
		private JCheckBox chckbxAttackPrayers;
		private JCheckBox chckbxSkinPrayers;
		private JCheckBox chckbxQuickPrayers;
		private final JLabel prayersLabel = new JLabel();
		private final JLabel prayersLabel2 = new JLabel();
		private final JLabel prayersAttackLabel = new JLabel();
		private final JLabel prayersAttackLabel2 = new JLabel();
		private final JLabel prayersSkinLabel = new JLabel();
		private final JLabel prayersSkinLabel2 = new JLabel();
		private final JComboBox quickPrayersBox = new JComboBox();
		private final JLabel continueNotePrayers = new JLabel();

		// For Money Tab
		private final JLabel moneyLabel1 = new JLabel();
		private JPanel itemsList = null;
		private JScrollPane itemScrollPane = null;
		public JLabel[] itemPrices = new JLabel[100];
		public JLabel[] itemRatios = new JLabel[100];

		// For Attack Styles Tab && Defence Styles Tab:
		private final Color enabledColor = new Color(210, 210, 210);
		private final Color disabledColor = new Color(230, 230, 230);

		// ATTACK
		// For Attack Styles tab:
		private final JLabel inventoryNoteText1 = new JLabel();
		private final JLabel inventoryNoteText2 = new JLabel();
		private final JLabel inventoryNoteText3 = new JLabel();
		private final JLabel combatStylesText1 = new JLabel();
		private final JLabel combatStylesText2 = new JLabel();
		private final JLabel combatStylesText3 = new JLabel();
		private final JLabel startCombatStyleText = new JLabel();
		private final JComboBox startCombatStyle = new JComboBox();
		private JCheckBox chckbxFSGame;
		private final JLabel switchTriggersTitle = new JLabel();
		private JCheckBox prayerTrigger;
		private JCheckBox meleeSafeSpotTrigger;
		private JCheckBox everyGameTrigger;
		private JCheckBox randomGameTrigger;
		private JCheckBox hitTrigger;
		private final JTextArea hitTriggerBox1 = new JTextArea();
		private final JLabel hitTriggerText1 = new JLabel();
		private final JTextArea hitTriggerBox2 = new JTextArea();
		private final JLabel hitTriggerText2 = new JLabel();

		// For Inventory List:
		private DefaultListModel model;
		private JLabel inventoryTitleText;
		private JScrollPane scrollPane;
		private JList invList;

		// For Melee Section:
		private JCheckBox chckbxMelee;
		private JPanel meleePanel = null;
		private final Rectangle meleeArea = new Rectangle(285, 87, 430, 117);

		private DefaultListModel meleeModel;
		private JLabel meleeTitleText; // List Stuff
		private JScrollPane meleeScrollPane;
		private JList meleeList;
		private JButton meleeAdd = null;
		private JButton meleeRemove = null;

		private JLabel meleeOptionsTitle;
		private JLabel meleeAttackStyleTitle; // Options Stuff
		private final JComboBox meleeAttackStyleBox = new JComboBox();
		private JCheckBox chckbxMagic;
		private JPanel magicPanel = null;
		private final Rectangle magicArea = new Rectangle(285, 207, 430, 117);

		private DefaultListModel magicModel;
		private JLabel magicTitleText; // List Stuff
		private JScrollPane magicScrollPane;
		private JList magicList;
		private JButton magicAdd = null;
		private JButton magicRemove = null;

		private JLabel magicOptionsTitle;
		private JLabel magicCastSpellTitle; // Options Stuff
		private final JComboBox magicCastSpell = new JComboBox();
		private JCheckBox chckbxRange;
		private JPanel rangePanel = null;
		private final Rectangle rangeArea = new Rectangle(285, 327, 430, 117);

		private DefaultListModel rangeModel;
		private JLabel rangeTitleText; // List Stuff
		private JScrollPane rangeScrollPane;
		private JList rangeList;
		private JButton rangeAdd = null;
		private JButton rangeRemove = null;

		private JLabel rangeOptionsTitle;
		private JLabel rangeAttackStyleTitle; // Options Stuff
		private final JComboBox rangeAttackStyleBox = new JComboBox();
		// DEFENCE
		// For Defence Styles tab:
		private final JLabel inventoryNoteText1Defence = new JLabel();
		private final JLabel inventoryNoteText2Defence = new JLabel();
		private final JLabel inventoryNoteText3Defence = new JLabel();
		private final JLabel combatStylesText1Defence = new JLabel();
		private final JLabel combatStylesText2Defence = new JLabel();
		private final JLabel combatStylesText3Defence = new JLabel();
		private final JLabel stylesNoteDefence1 = new JLabel();
		private final JLabel stylesNoteDefence2 = new JLabel();
		private final JLabel stylesNoteDefence3 = new JLabel();

		// For Inventory List:
		private DefaultListModel modelDefence;
		private JLabel inventoryTitleTextDefence;
		private JScrollPane scrollPaneDefence;
		private JList invListDefence;

		// For Melee Section:
		private JCheckBox chckbxMeleeDefence;
		private JPanel meleePanelDefence = null;
		private final Rectangle meleeAreaDefence = new Rectangle(285, 87, 430,
				117);

		private DefaultListModel meleeModelDefence;
		private JLabel meleeTitleTextDefence; // List Stuff
		private JScrollPane meleeScrollPaneDefence;
		private JList meleeListDefence;
		private JButton meleeAddDefence = null;
		private JButton meleeRemoveDefence = null;

		private JCheckBox chckbxMagicDefence;
		private JPanel magicPanelDefence = null;
		private final Rectangle magicAreaDefence = new Rectangle(285, 207, 430,
				117);

		private DefaultListModel magicModelDefence;
		private JLabel magicTitleTextDefence; // List Stuff
		private JScrollPane magicScrollPaneDefence;
		private JList magicListDefence;
		private JButton magicAddDefence = null;
		private JButton magicRemoveDefence = null;

		private JCheckBox chckbxRangeDefence;
		private JPanel rangePanelDefence = null;
		private final Rectangle rangeAreaDefence = new Rectangle(285, 327, 430,
				117);

		private DefaultListModel rangeModelDefence;
		private JLabel rangeTitleTextDefence; // List Stuff
		private JScrollPane rangeScrollPaneDefence;
		private JList rangeListDefence;
		private JButton rangeAddDefence = null;
		private JButton rangeRemoveDefence = null;

		public FOGRunnerGUI() {
			initialize();
			setVisible(true);
		}

		private void initialize() {
			for (int i = 0; i < FOGItems.length; i++) {
				itemPrices[i] = new JLabel();
				itemPrices[i].setFont(new Font("SansSerif", 0, 12));
				itemPrices[i].setText("Loading...");
				itemRatios[i] = new JLabel();
				itemRatios[i].setFont(new Font("SansSerif", 0, 12));
				itemRatios[i].setText("Loading...");
			}
			if (screenHeight < 750) {
				this.setSize(810, 570);
			} else {
				this.setSize(810, 710);
			}
			this.setContentPane(getJContentPane());
			this.setTitle("FOGRunner by TerraBubble");
			WindowUtil.position(this);
		}

		private JPanel getJContentPane() {
			if (jContentPane == null) {
				jContentPane = new JPanel();
				jContentPane.setLayout(null);
				jContentPane.add(getJTabbedPane(), null);
				jContentPane.add(getStartButton(), null);
				jContentPane.add(getCancelButton(), null);
				jContentPane.add(getSaveButton(), null);
				jContentPane.add(getLoadButton(), null);
				jContentPane.add(getStatusLabel(), null);
			}
			return jContentPane;
		}

		private JTabbedPane getJTabbedPane() {
			if (screenHeight < 750) {
				if (jTabbedPane == null) {
					jTabbedPane = new JTabbedPane();
					jTabbedPane.setBounds(new Rectangle(2, 4, 805, 515));
					jTabbedPane.addTab("Main", null, getMainTab(), null);
					jTabbedPane.addTab("Options", null, getOptionsTab(), null);
					jTabbedPane.addTab("Prayers", null, getPrayersTab(), null);
					jTabbedPane.addTab("Attack Styles (Hybridding)", null,
							getCombatStylesTabScrollable(), null);
					jTabbedPane.addTab("Defence Styles", null,
							getCombatStylesTabScrollableDefence(), null);
					jTabbedPane.addTab("Money", null, getMoneyTab(), null);
				}
			} else {
				if (jTabbedPane == null) {
					jTabbedPane = new JTabbedPane();
					jTabbedPane.setBounds(new Rectangle(2, 4, 805, 641));
					jTabbedPane.addTab("Main", null, getMainTab(), null);
					jTabbedPane.addTab("Options", null, getOptionsTab(), null);
					jTabbedPane.addTab("Prayers", null, getPrayersTab(), null);
					jTabbedPane.addTab("Attack Styles (Hybridding)", null,
							getCombatStylesTab(), null);
					jTabbedPane.addTab("Defence Styles", null,
							getCombatStylesTabDefence(), null);
					jTabbedPane.addTab("Money", null, getMoneyTab(), null);
				}
			}
			return jTabbedPane;
		}

		private JPanel getMoneyTab() {
			if (MoneyTab == null) {
				MoneyTab = new JPanel();
				MoneyTab.setLayout(null);
				if (screenHeight < 750) {
					MoneyTab.setBounds(0, 0, 775, 660);
					MoneyTab.setPreferredSize(new Dimension(775, 700));
				}
				{
					moneyLabel1.setFont(new Font("SansSerif", 0, 12));
					moneyLabel1
							.setText("This tab shows prices and tokens for every FOG reward so you can maximise profit from FOG tokens.");
					moneyLabel1.setBounds(new Rectangle(new Point(30, 25),
							moneyLabel1.getPreferredSize()));
					MoneyTab.add(moneyLabel1);

					int x = 40;
					int y = 82;

					JLabel nameText = new JLabel();
					nameText.setFont(new Font("SansSerif", Font.BOLD, 12));
					nameText.setText("Name");
					nameText.setBounds(new Rectangle(new Point(x + 10, y + 3),
							nameText.getPreferredSize()));
					MoneyTab.add(nameText);
					JPanel nameTitle = new JPanel();
					nameTitle.setLayout(null);
					nameTitle.setBounds(x, y + 1, 181, 18);
					nameTitle.setBackground(enabledColor);
					nameTitle.setBorder(new LineBorder(Color.BLACK));
					MoneyTab.add(nameTitle);

					JLabel tokensText = new JLabel();
					tokensText.setFont(new Font("SansSerif", Font.BOLD, 12));
					tokensText.setText("FOG Tokens");
					tokensText.setBounds(new Rectangle(
							new Point(x + 190, y + 3), tokensText
									.getPreferredSize()));
					MoneyTab.add(tokensText);
					JPanel tokensTitle = new JPanel();
					tokensTitle.setLayout(null);
					tokensTitle.setBounds(x + 180, y + 1, 121, 18);
					tokensTitle.setBackground(enabledColor);
					tokensTitle.setBorder(new LineBorder(Color.BLACK));
					MoneyTab.add(tokensTitle);

					JLabel priceText = new JLabel();
					priceText.setFont(new Font("SansSerif", Font.BOLD, 12));
					priceText.setText("GE Market Price");
					priceText.setBounds(new Rectangle(
							new Point(x + 310, y + 3), priceText
									.getPreferredSize()));
					MoneyTab.add(priceText);
					JPanel priceTitle = new JPanel();
					priceTitle.setLayout(null);
					priceTitle.setBounds(x + 300, y + 1, 121, 18);
					priceTitle.setBackground(enabledColor);
					priceTitle.setBorder(new LineBorder(Color.BLACK));
					MoneyTab.add(priceTitle);

					JLabel ratioText = new JLabel();
					ratioText.setFont(new Font("SansSerif", Font.BOLD, 12));
					ratioText.setText("GP Per Token");
					ratioText.setBounds(new Rectangle(
							new Point(x + 430, y + 3), ratioText
									.getPreferredSize()));
					MoneyTab.add(ratioText);
					JPanel ratioTitle = new JPanel();
					ratioTitle.setLayout(null);
					ratioTitle.setBounds(x + 420, y + 1, 121, 18);
					ratioTitle.setBackground(enabledColor);
					ratioTitle.setBorder(new LineBorder(Color.BLACK));
					MoneyTab.add(ratioTitle);

					JLabel membersText = new JLabel();
					membersText.setFont(new Font("SansSerif", Font.BOLD, 12));
					membersText.setText("Members?");
					membersText.setBounds(new Rectangle(new Point(x + 550,
							y + 3), membersText.getPreferredSize()));
					MoneyTab.add(membersText);
					JPanel membersTitle = new JPanel();
					membersTitle.setLayout(null);
					membersTitle.setBounds(x + 540, y + 1, 155, 18);
					membersTitle.setBackground(enabledColor);
					membersTitle.setBorder(new LineBorder(Color.BLACK));
					MoneyTab.add(membersTitle);

					itemScrollPane = new JScrollPane(getItemsList(),
							JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
							JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
					if (screenHeight < 750) {
						itemScrollPane.setBounds(x, y + 18, 695, 250);
					} else {
						itemScrollPane.setBounds(x, y + 18, 695, 360);
					}
					MoneyTab.add(itemScrollPane);
				}
			}
			return MoneyTab;
		}

		private JPanel getItemsList() {
			if (itemsList == null) {
				itemsList = new JPanel();
				itemsList.setLayout(null);
				itemsList.setBounds(40, 100, 695, 404);
				itemsList.setPreferredSize(new Dimension(695, 410));

				for (int i = 0; i < FOGItems.length; i++) {

					JLabel nameLabel = new JLabel();
					nameLabel.setFont(new Font("SansSerif", 0, 12));
					nameLabel.setText(FOGItems[i].name);
					nameLabel.setBounds(new Rectangle(new Point(3, v[i] + 1),
							nameLabel.getPreferredSize()));
					itemsList.add(nameLabel);

					JLabel tokensLabel = new JLabel();
					tokensLabel.setFont(new Font("SansSerif", 0, 12));
					tokensLabel.setText("" + FOGItems[i].tokens);
					tokensLabel.setBounds(new Rectangle(
							new Point(195, v[i] + 1), tokensLabel
									.getPreferredSize()));
					itemsList.add(tokensLabel);

					itemPrices[i].setBounds(new Rectangle(new Point(315,
							v[i] + 1), itemPrices[i].getPreferredSize()));
					itemsList.add(itemPrices[i]);
					itemRatios[i].setBounds(new Rectangle(new Point(435,
							v[i] + 1), itemRatios[i].getPreferredSize()));
					itemsList.add(itemRatios[i]);

					JLabel membersLabel = new JLabel();
					membersLabel.setFont(new Font("SansSerif", 0, 12));
					if (FOGItems[i].members) {
						membersLabel.setText("Yes");
					} else {
						membersLabel.setText("No");
					}
					membersLabel.setBounds(new Rectangle(new Point(555,
							v[i] + 1), membersLabel.getPreferredSize()));
					itemsList.add(membersLabel);

					JPanel itemPanel = new JPanel();
					itemPanel.setBounds(0, v[i], 695, 17);
					if ((v[i] + 2) % 2 == 0) { // if v is even
						itemPanel.setBackground(disabledColor);
					} else {
						itemPanel.setBackground(enabledColor);
					}
					itemsList.add(itemPanel);
				}
			}
			return itemsList;
		}

		private JPanel getCombatStylesTabScrollable() {
			if (CombatStylesTabScrollable == null) {
				CombatStylesTabScrollable = new JPanel();
				CombatStylesTabScrollable.setLayout(null);
				{
					JScrollPane theScrollPane = new JScrollPane(
							getCombatStylesTab(),
							JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
							JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
					theScrollPane.setBounds(0, 0, 775, 465);
					CombatStylesTabScrollable.add(theScrollPane);
				}
			}
			return CombatStylesTabScrollable;
		}

		private JPanel getCombatStylesTabScrollableDefence() {
			if (CombatStylesTabScrollableDefence == null) {
				CombatStylesTabScrollableDefence = new JPanel();
				CombatStylesTabScrollableDefence.setLayout(null);
				{
					JScrollPane theScrollPane = new JScrollPane(
							getCombatStylesTabDefence(),
							JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
							JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
					theScrollPane.setBounds(0, 1, 775, 465);
					CombatStylesTabScrollableDefence.add(theScrollPane);
				}
			}
			return CombatStylesTabScrollableDefence;
		}

		private JButton getStartButton() {
			if (Start == null) {
				Start = new JButton();
				if (screenHeight < 750) {
					Start.setBounds(new Rectangle(10, 513, 100, 25));
				} else {
					Start.setBounds(new Rectangle(10, 648, 100, 25));
				}
				Start.setText("Start");
				Start.addActionListener(this);
			}
			return Start;
		}

		private JButton getCancelButton() {
			if (Cancel == null) {
				Cancel = new JButton();
				if (screenHeight < 750) {
					Cancel.setBounds(new Rectangle(110, 513, 100, 25));
				} else {
					Cancel.setBounds(new Rectangle(110, 648, 100, 25));
				}
				Cancel.setText("Cancel");
				Cancel.addActionListener(this);
			}
			return Cancel;
		}

		private JButton getSaveButton() {
			if (Save == null) {
				Save = new JButton();
				if (screenHeight < 750) {
					Save.setBounds(new Rectangle(550, 513, 120, 25));
				} else {
					Save.setBounds(new Rectangle(550, 648, 120, 25));
				}
				Save.setText("Save Settings");
				Save.addActionListener(this);
			}
			return Save;
		}

		private JButton getLoadButton() {
			if (Load == null) {
				Load = new JButton();
				if (screenHeight < 750) {
					Load.setBounds(new Rectangle(670, 513, 120, 25));
				} else {
					Load.setBounds(new Rectangle(670, 648, 120, 25));
				}
				Load.setText("Load Settings");
				Load.addActionListener(this);
			}
			return Load;
		}

		private JLabel getStatusLabel() {
			statusLabel.setFont(new Font("SansSerif", 0, 12));
			statusLabel.setText("");
			setStatusLabelBounds();
			return statusLabel;
		}

		private void setStatusLabelBounds() {
			if (screenHeight < 750) {
				statusLabel.setBounds(new Rectangle(
						new Point(405 - (int) (statusLabel.getPreferredSize()
								.getWidth() / 2), 513), statusLabel
								.getPreferredSize()));
			} else {
				statusLabel.setBounds(new Rectangle(
						new Point(405 - (int) (statusLabel.getPreferredSize()
								.getWidth() / 2), 648), statusLabel
								.getPreferredSize()));
			}
		}

		private JPanel getMainTab() {
			if (MainTab == null) {
				MainTab = new JPanel();
				MainTab.setLayout(null);
				{
					if (success) {
						imageLabel.setIcon(new ImageIcon(banner));
					} else {
						imageLabel.setText("Image download error");
					}
					imageLabel.setBounds(new Rectangle(new Point(2, 0),
							imageLabel.getPreferredSize()));
					MainTab.add(imageLabel);
				}
				{
					notesLabel.setFont(new Font("SansSerif", 0, 23));
					notesLabel.setText("Notes:");
					notesLabel.setBounds(new Rectangle(new Point(
							400 - (int) (notesLabel.getPreferredSize()
									.getWidth() / 2), 260), notesLabel
							.getPreferredSize()));
					MainTab.add(notesLabel);
				}
				{
					note1.setFont(new Font("SansSerif", 0, 15));
					note1.setText("Start in the Fist of Guthix Lobby");
					note1.setBounds(new Rectangle(new Point(400 - (int) (note1
							.getPreferredSize().getWidth() / 2), 295), note1
							.getPreferredSize()));
					MainTab.add(note1);
				}
				{
					note2.setFont(new Font("SansSerif", 0, 15));
					note2.setText("XP Paint will appear once you start gaining XP");
					note2.setBounds(new Rectangle(new Point(400 - (int) (note2
							.getPreferredSize().getWidth() / 2), 320), note2
							.getPreferredSize()));
					MainTab.add(note2);
				}
				{
					note3.setFont(new Font("SansSerif", 0, 15));
					note3.setText("Hover over the XP Paint to see details in that skill");
					note3.setBounds(new Rectangle(new Point(400 - (int) (note3
							.getPreferredSize().getWidth() / 2), 345), note3
							.getPreferredSize()));
					MainTab.add(note3);
				}
				{
					continueNote.setFont(new Font("SansSerif", 0, 16));
					continueNote
							.setText("Continue to next tab to set up script");
					continueNote.setBounds(new Rectangle(new Point(
							400 - (int) (continueNote.getPreferredSize()
									.getWidth() / 2), 500), continueNote
							.getPreferredSize()));
					MainTab.add(continueNote);
				}
				{
					authorNote.setFont(new Font("SansSerif", 0, 12));
					authorNote.setText("By TerraBubble");
					authorNote.setBounds(new Rectangle(new Point(
							400 - (int) (authorNote.getPreferredSize()
									.getWidth() / 2), 555), authorNote
							.getPreferredSize()));
					MainTab.add(authorNote);
				}
				{
					versionNote.setFont(new Font("SansSerif", 0, 12));
					versionNote.setText("Version " + getVersion());
					versionNote.setBounds(new Rectangle(new Point(
							400 - (int) (versionNote.getPreferredSize()
									.getWidth() / 2), 572), versionNote
							.getPreferredSize()));
					MainTab.add(versionNote);
				}
			}
			return MainTab;
		}

		private JPanel getOptionsTab() {
			if (OptionsTab == null) {
				OptionsTab = new JPanel();
				OptionsTab.setLayout(null);
				{
					chckbxAntiBan = new JCheckBox("Use AntiBan");
					chckbxAntiBan.addActionListener(this);
					chckbxAntiBan.setBounds(30, 20, 390, 16);
					chckbxAntiBan.setSelected(true);
					OptionsTab.add(chckbxAntiBan);
				}
				{
					chckbxBandages = new JCheckBox("Use Bandages");
					chckbxBandages.addActionListener(this);
					chckbxBandages.setBounds(30, 45, 370, 16);
					chckbxBandages.setSelected(true);
					OptionsTab.add(chckbxBandages);
				}
				{
					teleOrbLabel.setText("Use Tele-Orb: ");
					teleOrbLabel.setBounds(new Rectangle(new Point(57, 75),
							teleOrbLabel.getPreferredSize()));
					OptionsTab.add(teleOrbLabel);
				}
				{
					teleOrbBox.setModel(new DefaultComboBoxModel(new String[] {
							"When being Hunted", "When Hunting",
							"Random (Hunting/Hunted)", "Never" }));
					teleOrbBox.setBounds(new Rectangle(new Point(160, 71),
							teleOrbBox.getPreferredSize()));
					teleOrbBox.addActionListener(this);
					OptionsTab.add(teleOrbBox);
				}
				{
					chckbxBankTokens = new JCheckBox(
							"Bank Tokens When You Have Over ");
					chckbxBankTokens.addActionListener(this);
					chckbxBankTokens.setBounds(30, 110,
							chckbxBankTokens.getPreferredSize().width, 16);
					chckbxBankTokens.setSelected(false);
					OptionsTab.add(chckbxBankTokens);
				}
				{
					bankTokensNum.setBounds(290, 109, 45, 20);
					bankTokensNum.setBorder(new LineBorder(new Color(0, 0, 0,
							255)));
					bankTokensNum.setText("9000");
					OptionsTab.add(bankTokensNum);
				}
				{
					chckbxSearch = new JCheckBox(
							"Search for Opponent when Hunting");
					chckbxSearch.addActionListener(this);
					chckbxSearch.setBounds(30, 137, 370, 16);
					chckbxSearch.setSelected(true);
					OptionsTab.add(chckbxSearch);
				}
				{
					chckbxScreenshots = new JCheckBox(
							"Take Screenshots on Level Ups");
					chckbxScreenshots.addActionListener(this);
					chckbxScreenshots.setBounds(30, 162, 370, 16);
					chckbxScreenshots.setSelected(true);
					OptionsTab.add(chckbxScreenshots);
				}
				{
					mouseSpeedLabel.setText("Mouse Speed: ");
					mouseSpeedLabel.setBounds(new Rectangle(new Point(57, 187),
							mouseSpeedLabel.getPreferredSize()));
					OptionsTab.add(mouseSpeedLabel);
				}
				{
					mouseSpeedBox.setBounds((int) mouseSpeedLabel
							.getPreferredSize().getWidth() + 67, 184, 30, 20);
					mouseSpeedBox.setBorder(new LineBorder(new Color(0, 0, 0,
							255)));
					mouseSpeedBox.setText("7");
					OptionsTab.add(mouseSpeedBox);
				}
				{
					mouseSpeedLabel2
							.setText("(The lower the number, the faster the mouse)");
					mouseSpeedLabel2.setBounds(new Rectangle(
							new Point((int) mouseSpeedLabel.getPreferredSize()
									.getWidth() + 107, 187), mouseSpeedLabel2
									.getPreferredSize()));
					OptionsTab.add(mouseSpeedLabel2);
				}
				{
					chckbxDmode = new JCheckBox("Debug Mode");
					chckbxDmode.addActionListener(this);
					chckbxDmode.setBounds(30, 212, 370, 16);
					chckbxDmode.setSelected(false);
					OptionsTab.add(chckbxDmode);
				}
				{
					continueNoteOptions.setFont(new Font("SansSerif", 0, 16));
					continueNoteOptions
							.setText("Continue to next tab to set up prayers");
					continueNoteOptions.setBounds(new Rectangle(new Point(
							400 - (int) (continueNoteOptions.getPreferredSize()
									.getWidth() / 2), 530), continueNoteOptions
							.getPreferredSize()));
					OptionsTab.add(continueNoteOptions);
				}
			}
			return OptionsTab;
		}

		private JPanel getPrayersTab() {
			if (PrayersTab == null) {
				PrayersTab = new JPanel();
				PrayersTab.setLayout(null);
				{
					chckbxQuickPrayers = new JCheckBox(
							"Use Quick Prayers when: ");
					chckbxQuickPrayers.addActionListener(this);
					chckbxQuickPrayers.setBounds(30, 25,
							chckbxQuickPrayers.getPreferredSize().width, 16);
					chckbxQuickPrayers.setSelected(false);
					PrayersTab.add(chckbxQuickPrayers);
				}
				{
					quickPrayersBox
							.setModel(new DefaultComboBoxModel(new String[] {
									"When Hunting", "When Being Hunted" }));
					quickPrayersBox.setBounds(new Rectangle(new Point(220, 21),
							quickPrayersBox.getPreferredSize()));
					quickPrayersBox.addActionListener(this);
					PrayersTab.add(quickPrayersBox);
				}
				{
					chckbxPrayers = new JCheckBox("Use Protection Prayers");
					chckbxPrayers.addActionListener(this);
					chckbxPrayers.setBounds(30, 60, 370, 16);
					chckbxPrayers.setSelected(false);
					PrayersTab.add(chckbxPrayers);
				}
				{
					prayersLabel
							.setText("This will use 'Protect from ...' prayers automatically depending on the attack style of your");
					prayersLabel.setBounds(new Rectangle(new Point(57, 80),
							prayersLabel.getPreferredSize()));
					PrayersTab.add(prayersLabel);
				}
				{
					prayersLabel2.setText("opponent and your Prayer level.");
					prayersLabel2.setBounds(new Rectangle(new Point(57, 100),
							prayersLabel2.getPreferredSize()));
					PrayersTab.add(prayersLabel2);
				}
				{
					chckbxAttackPrayers = new JCheckBox(
							"Use Attack Multiplier Prayers");
					chckbxAttackPrayers.addActionListener(this);
					chckbxAttackPrayers.setBounds(30, 139, 370, 16);
					chckbxAttackPrayers.setSelected(false);
					PrayersTab.add(chckbxAttackPrayers);
				}
				{
					prayersAttackLabel
							.setText("This will use Attack Mulitplier Prayers (+5%, +10%, 15%) such as Mystic Might, Ultimate Strength");
					prayersAttackLabel.setBounds(new Rectangle(new Point(57,
							159), prayersAttackLabel.getPreferredSize()));
					PrayersTab.add(prayersAttackLabel);
				}
				{
					prayersAttackLabel2
							.setText("and Incredible Reflexes automatically depending on your Prayer level and attack style.");
					prayersAttackLabel2.setBounds(new Rectangle(new Point(57,
							179), prayersAttackLabel2.getPreferredSize()));
					PrayersTab.add(prayersAttackLabel2);
				}
				{
					chckbxSkinPrayers = new JCheckBox(
							"Use 'Skin' Defence Multiplier Prayers");
					chckbxSkinPrayers.addActionListener(this);
					chckbxSkinPrayers.setBounds(30, 218, 400, 16);
					chckbxSkinPrayers.setSelected(false);
					PrayersTab.add(chckbxSkinPrayers);
				}
				{
					prayersSkinLabel
							.setText("This will use 'Skin' Defence Multiplier Prayers (Thick Skin, Rock Skin and Steel Skin)");
					prayersSkinLabel.setBounds(new Rectangle(
							new Point(57, 238), prayersSkinLabel
									.getPreferredSize()));
					PrayersTab.add(prayersSkinLabel);
				}
				{
					prayersSkinLabel2
							.setText("automatically depending on your Prayer level.");
					prayersSkinLabel2.setBounds(new Rectangle(
							new Point(57, 258), prayersSkinLabel2
									.getPreferredSize()));
					PrayersTab.add(prayersSkinLabel2);
				}
				{
					continueNotePrayers.setFont(new Font("SansSerif", 0, 16));
					continueNotePrayers
							.setText("Continue to next two tabs to set up your attack weapons and defence armour");
					continueNotePrayers.setBounds(new Rectangle(new Point(
							400 - (int) (continueNotePrayers.getPreferredSize()
									.getWidth() / 2), 530), continueNotePrayers
							.getPreferredSize()));
					PrayersTab.add(continueNotePrayers);
				}
			}
			return PrayersTab;
		}

		// ATTACK
		private JPanel getMeleePanel() {
			meleePanel = new JPanel();
			meleePanel.setBounds(meleeArea);
			meleePanel.setBackground(enabledColor);
			{
				meleeModel = new DefaultListModel();
				{
					// Melee Check Box
					chckbxMelee = new JCheckBox("Melee");
					chckbxMelee.addActionListener(this);
					chckbxMelee.setBounds(295, 90,
							chckbxMelee.getPreferredSize().width, 16);
					chckbxMelee.setSelected(true);
					CombatStylesTab.add(chckbxMelee);
					// Melee Wield List
					meleeTitleText = new JLabel();
					meleeTitleText.setText("Wield:");
					meleeTitleText.setBounds(new Rectangle(new Point(410, 92),
							meleeTitleText.getPreferredSize()));
					CombatStylesTab.add(meleeTitleText);
					meleeScrollPane = new JScrollPane();
					meleeScrollPane.setBounds(325, 110, 220, 90);
					CombatStylesTab.add(meleeScrollPane);
					meleeScrollPane.setEnabled(true);
					meleeList = new JList(meleeModel);
					meleeScrollPane.setViewportView(meleeList);
					meleeList.addListSelectionListener(this);
					meleeList
							.setBorder(new LineBorder(new Color(255, 255, 255)));
					// Melee Add Button
					meleeAdd = new JButton();
					meleeAdd.setBounds(new Rectangle(285, 125, 40, 28));
					meleeAdd.setText(">");
					meleeAdd.addActionListener(this);
					CombatStylesTab.add(meleeAdd);
					meleeAdd.setEnabled(true);
					// Melee Remove Button
					meleeRemove = new JButton();
					meleeRemove.setBounds(new Rectangle(285, 155, 40, 28));
					meleeRemove.setText("<");
					meleeRemove.addActionListener(this);
					CombatStylesTab.add(meleeRemove);
					meleeRemove.setEnabled(true);
					// Melee Options
					meleeOptionsTitle = new JLabel();
					meleeOptionsTitle.setText("Options:");
					meleeOptionsTitle.setBounds(new Rectangle(
							new Point(600, 92), meleeOptionsTitle
									.getPreferredSize()));
					CombatStylesTab.add(meleeOptionsTitle);
					meleeAttackStyleTitle = new JLabel();
					meleeAttackStyleTitle.setText("Attack Style:");
					meleeAttackStyleTitle
							.setBounds(new Rectangle(new Point(590, 120),
									meleeAttackStyleTitle.getPreferredSize()));
					CombatStylesTab.add(meleeAttackStyleTitle);
					meleeAttackStyleBox.setModel(new DefaultComboBoxModel(
							new String[] { "Attack EXP", "Strength EXP",
									"Defence EXP", "Shared EXP" }));
					meleeAttackStyleBox.setBounds(new Rectangle(new Point(565,
							140), meleeAttackStyleBox.getPreferredSize()));
					meleeAttackStyleBox.addActionListener(this);
					CombatStylesTab.add(meleeAttackStyleBox);
					meleeAttackStyleBox.setEnabled(true);
				}
			}
			return meleePanel;
		}

		private JPanel getMagicPanel() {
			magicPanel = new JPanel();
			magicPanel.setBounds(magicArea);
			magicPanel.setBackground(enabledColor);
			{
				magicModel = new DefaultListModel();
				{
					// Magic Check Box
					chckbxMagic = new JCheckBox("Magic");
					chckbxMagic.addActionListener(this);
					chckbxMagic.setBounds(295, 210,
							chckbxMagic.getPreferredSize().width, 16);
					chckbxMagic.setSelected(true);
					CombatStylesTab.add(chckbxMagic);
					// Magic Wield List
					magicTitleText = new JLabel();
					magicTitleText.setText("Wield:");
					magicTitleText.setBounds(new Rectangle(new Point(410, 212),
							magicTitleText.getPreferredSize()));
					CombatStylesTab.add(magicTitleText);
					magicScrollPane = new JScrollPane();
					magicScrollPane.setBounds(325, 230, 220, 90);
					CombatStylesTab.add(magicScrollPane);
					magicScrollPane.setEnabled(true);
					magicList = new JList(magicModel);
					magicScrollPane.setViewportView(magicList);
					magicList.addListSelectionListener(this);
					magicList
							.setBorder(new LineBorder(new Color(255, 255, 255)));
					// Magic Add Button
					magicAdd = new JButton();
					magicAdd.setBounds(new Rectangle(285, 245, 40, 28));
					magicAdd.setText(">");
					magicAdd.addActionListener(this);
					CombatStylesTab.add(magicAdd);
					magicAdd.setEnabled(true);
					// Magic Remove Button
					magicRemove = new JButton();
					magicRemove.setBounds(new Rectangle(285, 275, 40, 28));
					magicRemove.setText("<");
					magicRemove.addActionListener(this);
					CombatStylesTab.add(magicRemove);
					magicRemove.setEnabled(true);
					// Magic Options
					magicOptionsTitle = new JLabel();
					magicOptionsTitle.setText("Options:");
					magicOptionsTitle.setBounds(new Rectangle(new Point(600,
							212), magicOptionsTitle.getPreferredSize()));
					CombatStylesTab.add(magicOptionsTitle);
					magicCastSpellTitle = new JLabel();
					magicCastSpellTitle.setText("Cast Spell:");
					magicCastSpellTitle.setBounds(new Rectangle(new Point(590,
							240), magicCastSpellTitle.getPreferredSize()));
					CombatStylesTab.add(magicCastSpellTitle);
					magicCastSpell.setModel(new DefaultComboBoxModel(
							new String[] { "Auto Highest (F2P)",
									"Auto Highest (P2P)", "Fire Surge",
									"Earth Surge", "Water Surge", "Wind Surge",
									"Fire Wave", "Earth Wave", "Water Wave",
									"Wind Wave", "Fire Blast", "Earth Blast",
									"Water Blast", "Wind Blast", "Fire Bolt",
									"Earth Bolt", "Water Bolt", "Wind Bolt",
									"Fire Strike", "Earth Strike",
									"Water Strike", "Wind Strike", "Wind Rush",
									"Ice Barrage", "Blood Barrage",
									"Shadow Barrage", "Smoke Barrage",
									"Ice Blitz", "Blood Blitz", "Shadow Blitz",
									"Smoke Blitz", "Ice Burst", "Blood Burst",
									"Shadow Burst", "Smoke Burst", "Ice Rush",
									"Blood Rush", "Shadow Rush", "Smoke Rush",
									"Miasmic Barrage", "Miasmic Blitz",
									"Miasmic Burst", "Miasmic Rush" }));
					magicCastSpell.setBounds(new Rectangle(new Point(545, 260),
							magicCastSpell.getPreferredSize()));
					magicCastSpell.addActionListener(this);
					CombatStylesTab.add(magicCastSpell);
					magicCastSpell.setEnabled(true);
				}
			}
			return magicPanel;
		}

		private JPanel getRangePanel() {
			rangePanel = new JPanel();
			rangePanel.setBounds(rangeArea);
			rangePanel.setBackground(disabledColor);
			{
				rangeModel = new DefaultListModel();
				{
					// Range Check Box
					chckbxRange = new JCheckBox("Range");
					chckbxRange.addActionListener(this);
					chckbxRange.setBounds(295, 330,
							chckbxRange.getPreferredSize().width, 16);
					chckbxRange.setSelected(false);
					CombatStylesTab.add(chckbxRange);
					// Range Wield List
					rangeTitleText = new JLabel();
					rangeTitleText.setText("Wield:");
					rangeTitleText.setBounds(new Rectangle(new Point(410, 332),
							rangeTitleText.getPreferredSize()));
					CombatStylesTab.add(rangeTitleText);
					rangeScrollPane = new JScrollPane();
					rangeScrollPane.setBounds(325, 350, 220, 90);
					CombatStylesTab.add(rangeScrollPane);
					rangeScrollPane.setEnabled(false);
					rangeList = new JList(rangeModel);
					rangeScrollPane.setViewportView(rangeList);
					rangeList.addListSelectionListener(this);
					rangeList
							.setBorder(new LineBorder(new Color(255, 255, 255)));
					// Range Add Button
					rangeAdd = new JButton();
					rangeAdd.setBounds(new Rectangle(285, 365, 40, 28));
					rangeAdd.setText(">");
					rangeAdd.addActionListener(this);
					CombatStylesTab.add(rangeAdd);
					rangeAdd.setEnabled(false);
					// Range Remove Button
					rangeRemove = new JButton();
					rangeRemove.setBounds(new Rectangle(285, 395, 40, 28));
					rangeRemove.setText("<");
					rangeRemove.addActionListener(this);
					CombatStylesTab.add(rangeRemove);
					rangeRemove.setEnabled(false);
					// Range Options
					rangeOptionsTitle = new JLabel();
					rangeOptionsTitle.setText("Options:");
					rangeOptionsTitle.setBounds(new Rectangle(new Point(600,
							332), rangeOptionsTitle.getPreferredSize()));
					CombatStylesTab.add(rangeOptionsTitle);
					rangeAttackStyleTitle = new JLabel();
					rangeAttackStyleTitle.setText("Attack Style:");
					rangeAttackStyleTitle
							.setBounds(new Rectangle(new Point(590, 360),
									rangeAttackStyleTitle.getPreferredSize()));
					CombatStylesTab.add(rangeAttackStyleTitle);
					rangeAttackStyleBox
							.setModel(new DefaultComboBoxModel(new String[] {
									"Accurate", "Rapid", "Long range" }));
					rangeAttackStyleBox.setBounds(new Rectangle(new Point(565,
							380), rangeAttackStyleBox.getPreferredSize()));
					rangeAttackStyleBox.addActionListener(this);
					CombatStylesTab.add(rangeAttackStyleBox);
					rangeAttackStyleBox.setEnabled(false);
				}
			}
			return rangePanel;
		}

		private JPanel getCombatStylesTab() {
			if (CombatStylesTab == null) {
				CombatStylesTab = new JPanel();
				CombatStylesTab.setLayout(null);
				if (screenHeight < 750) {
					CombatStylesTab.setBounds(0, 0, 775, 660);
					CombatStylesTab.setPreferredSize(new Dimension(775, 700));
				}
				{
					// Inventory List
					model = new DefaultListModel();
					{
						// Inventory List Display
						inventoryTitleText = new JLabel();
						inventoryTitleText.setText("Your Inventory:");
						inventoryTitleText
								.setBounds(new Rectangle(new Point(60, 10),
										inventoryTitleText.getPreferredSize()));
						CombatStylesTab.add(inventoryTitleText);
						scrollPane = new JScrollPane();
						scrollPane.setBounds(20, 30, 220, 485);
						CombatStylesTab.add(scrollPane);
						invList = new JList(model);
						scrollPane.setViewportView(invList);
						invList.addListSelectionListener(this);
						invList.setBorder(new LineBorder(new Color(255, 255,
								255)));
					}
					{
						// Inventory List Population with Inventory Items
						int i = 0;
						while (i < inventoryItemsNum) {
							String IDString = Integer
									.toString(inventoryItemsIDs[i]);
							int zerosToAdd = 5 - IDString.length();
							String zeros = "";
							int wey = 0;
							while (wey < zerosToAdd) {
								zeros = zeros + "0";
								wey++;
							}
							zeros = zeros + IDString;
							IDString = zeros;
							model.add(model.getSize(), IDString + " - "
									+ inventoryItemsNames[i]);
							i++;
						}
					}

					// Combat Styles instructions
					combatStylesText1.setFont(new Font("SansSerif", 0, 12));
					combatStylesText2.setFont(new Font("SansSerif", 0, 12));
					combatStylesText3.setFont(new Font("SansSerif", 0, 12));
					combatStylesText1
							.setText("These are the different attack styles the script can use.");
					combatStylesText2
							.setText("The items in the wield boxes will be equipped when using that style.");
					combatStylesText3
							.setText("Select inventory items and use the '>' button to add to the wield items.");
					combatStylesText1.setBounds(new Rectangle(
							new Point(283, 22), combatStylesText1
									.getPreferredSize()));
					combatStylesText2.setBounds(new Rectangle(
							new Point(283, 38), combatStylesText2
									.getPreferredSize()));
					combatStylesText3.setBounds(new Rectangle(
							new Point(283, 54), combatStylesText3
									.getPreferredSize()));
					CombatStylesTab.add(combatStylesText1);
					CombatStylesTab.add(combatStylesText2);
					CombatStylesTab.add(combatStylesText3);

					// Melee Section
					CombatStylesTab.add(getMeleePanel());
					// Magic Section
					CombatStylesTab.add(getMagicPanel());
					// Range Section
					CombatStylesTab.add(getRangePanel());

					// Bottom Options
					startCombatStyleText.setFont(new Font("SansSerif", 0, 13));
					startCombatStyleText.setText("First Attack Style to Use: ");
					startCombatStyleText.setBounds(new Rectangle(new Point(283,
							450), startCombatStyleText.getPreferredSize()));
					CombatStylesTab.add(startCombatStyleText);
					refreshStartStyleMenu();
					startCombatStyle
							.setBounds(new Rectangle(440, 446, 100, 27));
					startCombatStyle.addActionListener(this);
					CombatStylesTab.add(startCombatStyle);

					chckbxFSGame = new JCheckBox(
							"Use this at start of every game");
					chckbxFSGame.addActionListener(this);
					chckbxFSGame.setSelected(false);
					chckbxFSGame.setBounds(new Rectangle(new Point(535, 446),
							chckbxFSGame.getPreferredSize()));
					CombatStylesTab.add(chckbxFSGame);

					switchTriggersTitle.setFont(new Font("SansSerif",
							Font.BOLD, 13));
					switchTriggersTitle.setText("Switch Attack Style...  ");
					switchTriggersTitle.setBounds(new Rectangle(new Point(283,
							470), switchTriggersTitle.getPreferredSize()));
					CombatStylesTab.add(switchTriggersTitle);

					prayerTrigger = new JCheckBox(
							"When opponent uses prayer protection... [ UNDER CONSTRUCTION ]");
					prayerTrigger.addActionListener(this);
					prayerTrigger.setSelected(false);
					prayerTrigger.setEnabled(false);
					prayerTrigger.setBounds(new Rectangle(new Point(280, 488),
							prayerTrigger.getPreferredSize()));
					CombatStylesTab.add(prayerTrigger);
					meleeSafeSpotTrigger = new JCheckBox(
							"When attacking with melee and opponent uses a safespot");
					meleeSafeSpotTrigger.addActionListener(this);
					meleeSafeSpotTrigger.setSelected(false);
					meleeSafeSpotTrigger.setBounds(new Rectangle(new Point(280,
							508), meleeSafeSpotTrigger.getPreferredSize()));
					CombatStylesTab.add(meleeSafeSpotTrigger);
					randomGameTrigger = new JCheckBox(
							"Randomly Every 1-5 FOG Games");
					randomGameTrigger.addActionListener(this);
					randomGameTrigger.setBounds(new Rectangle(new Point(280,
							528), randomGameTrigger.getPreferredSize()));
					randomGameTrigger.setSelected(true);
					CombatStylesTab.add(randomGameTrigger);
					everyGameTrigger = new JCheckBox("Every FOG Game");
					everyGameTrigger.addActionListener(this);
					everyGameTrigger.setBounds(new Rectangle(
							new Point(280, 548), everyGameTrigger
									.getPreferredSize()));
					everyGameTrigger.setSelected(false);
					CombatStylesTab.add(everyGameTrigger);
					hitTrigger = new JCheckBox("Randomly Every ");
					hitTrigger.addActionListener(this);
					hitTrigger.setBounds(new Rectangle(new Point(280, 568),
							hitTrigger.getPreferredSize()));
					hitTrigger.setSelected(false);
					CombatStylesTab.add(hitTrigger);
					hitTriggerBox1.setBounds(410, 572, 20, 20);
					hitTriggerBox1.setBorder(new LineBorder(new Color(0, 0, 0,
							255)));
					hitTriggerBox1.setText("3");
					CombatStylesTab.add(hitTriggerBox1);
					hitTriggerText1.setFont(new Font("SansSerif", 0, 13));
					hitTriggerText1.setText("to");
					hitTriggerText1.setBounds(new Rectangle(
							new Point(435, 573), hitTriggerText1
									.getPreferredSize()));
					CombatStylesTab.add(hitTriggerText1);
					hitTriggerBox2.setBounds(455, 572, 20, 20);
					hitTriggerBox2.setBorder(new LineBorder(new Color(0, 0, 0,
							255)));
					hitTriggerBox2.setText("5");
					CombatStylesTab.add(hitTriggerBox2);
					hitTriggerText2.setFont(new Font("SansSerif", 0, 13));
					hitTriggerText2.setText("Hitsplats on Opponent");
					hitTriggerText2.setBounds(new Rectangle(
							new Point(480, 573), hitTriggerText2
									.getPreferredSize()));
					CombatStylesTab.add(hitTriggerText2);

					inventoryNoteText1.setFont(new Font("SansSerif", 0, 13));
					inventoryNoteText1.setText("This list should have");
					inventoryNoteText1.setBounds(new Rectangle(new Point(26,
							520), inventoryNoteText1.getPreferredSize()));
					CombatStylesTab.add(inventoryNoteText1);
					inventoryNoteText2.setFont(new Font("SansSerif", 0, 13));
					inventoryNoteText2.setText("all your items from your");
					inventoryNoteText2.setBounds(new Rectangle(new Point(26,
							540), inventoryNoteText2.getPreferredSize()));
					CombatStylesTab.add(inventoryNoteText2);
					inventoryNoteText3.setFont(new Font("SansSerif", 0, 13));
					inventoryNoteText3.setText("equipment and inventory.");
					inventoryNoteText3.setBounds(new Rectangle(new Point(26,
							560), inventoryNoteText3.getPreferredSize()));
					CombatStylesTab.add(inventoryNoteText3);
				}
			}
			return CombatStylesTab;
		}

		// DEFENCE
		private JPanel getMeleePanelDefence() {
			meleePanelDefence = new JPanel();
			meleePanelDefence.setBounds(meleeAreaDefence);
			meleePanelDefence.setBackground(enabledColor);
			{
				meleeModelDefence = new DefaultListModel();
				{
					// Melee Check Box
					chckbxMeleeDefence = new JCheckBox("Melee");
					chckbxMeleeDefence.addActionListener(this);
					chckbxMeleeDefence.setBounds(295, 90,
							chckbxMeleeDefence.getPreferredSize().width, 16);
					chckbxMeleeDefence.setSelected(true);
					CombatStylesTabDefence.add(chckbxMeleeDefence);
					// Melee Wield List
					meleeTitleTextDefence = new JLabel();
					meleeTitleTextDefence.setText("Wield:");
					meleeTitleTextDefence
							.setBounds(new Rectangle(new Point(410, 92),
									meleeTitleTextDefence.getPreferredSize()));
					CombatStylesTabDefence.add(meleeTitleTextDefence);
					meleeScrollPaneDefence = new JScrollPane();
					meleeScrollPaneDefence.setBounds(325, 110, 220, 90);
					CombatStylesTabDefence.add(meleeScrollPaneDefence);
					meleeScrollPaneDefence.setEnabled(true);
					meleeListDefence = new JList(meleeModelDefence);
					meleeScrollPaneDefence.setViewportView(meleeListDefence);
					meleeListDefence.addListSelectionListener(this);
					meleeListDefence.setBorder(new LineBorder(new Color(255,
							255, 255)));
					// Melee Add Button
					meleeAddDefence = new JButton();
					meleeAddDefence.setBounds(new Rectangle(285, 125, 40, 28));
					meleeAddDefence.setText(">");
					meleeAddDefence.addActionListener(this);
					CombatStylesTabDefence.add(meleeAddDefence);
					meleeAdd.setEnabled(true);
					// Melee Remove Button
					meleeRemoveDefence = new JButton();
					meleeRemoveDefence
							.setBounds(new Rectangle(285, 155, 40, 28));
					meleeRemoveDefence.setText("<");
					meleeRemoveDefence.addActionListener(this);
					CombatStylesTabDefence.add(meleeRemoveDefence);
					meleeRemoveDefence.setEnabled(true);
				}
			}
			return meleePanelDefence;
		}

		private JPanel getMagicPanelDefence() {
			magicPanelDefence = new JPanel();
			magicPanelDefence.setBounds(magicAreaDefence);
			magicPanelDefence.setBackground(enabledColor);
			{
				magicModelDefence = new DefaultListModel();
				{
					// Magic Check Box
					chckbxMagicDefence = new JCheckBox("Magic");
					chckbxMagicDefence.addActionListener(this);
					chckbxMagicDefence.setBounds(295, 210,
							chckbxMagicDefence.getPreferredSize().width, 16);
					chckbxMagicDefence.setSelected(true);
					CombatStylesTabDefence.add(chckbxMagicDefence);
					// Magic Wield List
					magicTitleTextDefence = new JLabel();
					magicTitleTextDefence.setText("Wield:");
					magicTitleTextDefence
							.setBounds(new Rectangle(new Point(410, 212),
									magicTitleTextDefence.getPreferredSize()));
					CombatStylesTabDefence.add(magicTitleTextDefence);
					magicScrollPaneDefence = new JScrollPane();
					magicScrollPaneDefence.setBounds(325, 230, 220, 90);
					CombatStylesTabDefence.add(magicScrollPaneDefence);
					magicScrollPaneDefence.setEnabled(true);
					magicListDefence = new JList(magicModelDefence);
					magicScrollPaneDefence.setViewportView(magicListDefence);
					magicListDefence.addListSelectionListener(this);
					magicListDefence.setBorder(new LineBorder(new Color(255,
							255, 255)));
					// Magic Add Button
					magicAddDefence = new JButton();
					magicAddDefence.setBounds(new Rectangle(285, 245, 40, 28));
					magicAddDefence.setText(">");
					magicAddDefence.addActionListener(this);
					CombatStylesTabDefence.add(magicAddDefence);
					magicAddDefence.setEnabled(true);
					// Magic Remove Button
					magicRemoveDefence = new JButton();
					magicRemoveDefence
							.setBounds(new Rectangle(285, 275, 40, 28));
					magicRemoveDefence.setText("<");
					magicRemoveDefence.addActionListener(this);
					CombatStylesTabDefence.add(magicRemoveDefence);
					magicRemoveDefence.setEnabled(true);
				}
			}
			return magicPanelDefence;
		}

		private JPanel getRangePanelDefence() {
			rangePanelDefence = new JPanel();
			rangePanelDefence.setBounds(rangeAreaDefence);
			rangePanelDefence.setBackground(disabledColor);
			{
				rangeModelDefence = new DefaultListModel();
				{
					// Range Check Box
					chckbxRangeDefence = new JCheckBox("Range");
					chckbxRangeDefence.addActionListener(this);
					chckbxRangeDefence.setBounds(295, 330,
							chckbxRangeDefence.getPreferredSize().width, 16);
					chckbxRangeDefence.setSelected(false);
					CombatStylesTabDefence.add(chckbxRangeDefence);
					// Range Wield List
					rangeTitleTextDefence = new JLabel();
					rangeTitleTextDefence.setText("Wield:");
					rangeTitleTextDefence
							.setBounds(new Rectangle(new Point(410, 332),
									rangeTitleTextDefence.getPreferredSize()));
					CombatStylesTabDefence.add(rangeTitleTextDefence);
					rangeScrollPaneDefence = new JScrollPane();
					rangeScrollPaneDefence.setBounds(325, 350, 220, 90);
					CombatStylesTabDefence.add(rangeScrollPaneDefence);
					rangeScrollPaneDefence.setEnabled(false);
					rangeListDefence = new JList(rangeModelDefence);
					rangeScrollPaneDefence.setViewportView(rangeListDefence);
					rangeListDefence.addListSelectionListener(this);
					rangeListDefence.setBorder(new LineBorder(new Color(255,
							255, 255)));
					// Range Add Button
					rangeAddDefence = new JButton();
					rangeAddDefence.setBounds(new Rectangle(285, 365, 40, 28));
					rangeAddDefence.setText(">");
					rangeAddDefence.addActionListener(this);
					CombatStylesTabDefence.add(rangeAddDefence);
					rangeAddDefence.setEnabled(false);
					// Range Remove Button
					rangeRemoveDefence = new JButton();
					rangeRemoveDefence
							.setBounds(new Rectangle(285, 395, 40, 28));
					rangeRemoveDefence.setText("<");
					rangeRemoveDefence.addActionListener(this);
					CombatStylesTabDefence.add(rangeRemoveDefence);
					rangeRemoveDefence.setEnabled(false);
				}
			}
			return rangePanelDefence;
		}

		private JPanel getCombatStylesTabDefence() {
			if (CombatStylesTabDefence == null) {
				CombatStylesTabDefence = new JPanel();
				CombatStylesTabDefence.setLayout(null);
				if (screenHeight < 750) {
					CombatStylesTabDefence.setBounds(0, 0, 775, 660);
					CombatStylesTabDefence.setPreferredSize(new Dimension(775,
							700));
				}
				{
					// Inventory List
					modelDefence = new DefaultListModel();
					{
						// Inventory List Display
						inventoryTitleTextDefence = new JLabel();
						inventoryTitleTextDefence.setText("Your Inventory:");
						inventoryTitleTextDefence.setBounds(new Rectangle(
								new Point(60, 10), inventoryTitleTextDefence
										.getPreferredSize()));
						CombatStylesTabDefence.add(inventoryTitleTextDefence);
						scrollPaneDefence = new JScrollPane();
						scrollPaneDefence.setBounds(20, 30, 220, 485);
						CombatStylesTabDefence.add(scrollPaneDefence);
						invListDefence = new JList(modelDefence);
						scrollPaneDefence.setViewportView(invListDefence);
						invListDefence.addListSelectionListener(this);
						invListDefence.setBorder(new LineBorder(new Color(255,
								255, 255)));
					}
					{
						// Inventory List Population with Inventory Items
						int i = 0;
						while (i < inventoryItemsNum) {
							String IDString = Integer
									.toString(inventoryItemsIDs[i]);
							int zerosToAdd = 5 - IDString.length();
							String zeros = "";
							int wey = 0;
							while (wey < zerosToAdd) {
								zeros = zeros + "0";
								wey++;
							}
							zeros = zeros + IDString;
							IDString = zeros;
							modelDefence.add(modelDefence.getSize(), IDString
									+ " - " + inventoryItemsNames[i]);
							i++;
						}
					}

					// Combat Styles instructions
					combatStylesText1Defence.setFont(new Font("SansSerif", 0,
							12));
					combatStylesText2Defence.setFont(new Font("SansSerif", 0,
							12));
					combatStylesText3Defence.setFont(new Font("SansSerif", 0,
							12));
					combatStylesText1Defence
							.setText("These are the different defence styles the script can use.");
					combatStylesText2Defence
							.setText("The items in the wield boxes will be equipped when being attacked by that style.");
					combatStylesText3Defence
							.setText("Select inventory items and use the '>' button to add to the wield items.");
					combatStylesText1Defence.setBounds(new Rectangle(new Point(
							283, 22), combatStylesText1Defence
							.getPreferredSize()));
					combatStylesText2Defence.setBounds(new Rectangle(new Point(
							283, 38), combatStylesText2Defence
							.getPreferredSize()));
					combatStylesText3Defence.setBounds(new Rectangle(new Point(
							283, 54), combatStylesText3Defence
							.getPreferredSize()));
					CombatStylesTabDefence.add(combatStylesText1Defence);
					CombatStylesTabDefence.add(combatStylesText2Defence);
					CombatStylesTabDefence.add(combatStylesText3Defence);

					// Melee Section
					CombatStylesTabDefence.add(getMeleePanelDefence());
					// Magic Section
					CombatStylesTabDefence.add(getMagicPanelDefence());
					// Range Section
					CombatStylesTabDefence.add(getRangePanelDefence());

					// Bottom Note
					stylesNoteDefence1.setFont(new Font("SansSerif", 0, 12));
					stylesNoteDefence1
							.setText("These items will be equipped when being hunted and the enemy attacks you");
					stylesNoteDefence1.setBounds(new Rectangle(new Point(283,
							480), stylesNoteDefence1.getPreferredSize()));
					CombatStylesTabDefence.add(stylesNoteDefence1);
					stylesNoteDefence2.setFont(new Font("SansSerif", 0, 12));
					stylesNoteDefence2
							.setText("with any of the enabled styles above.");
					stylesNoteDefence2.setBounds(new Rectangle(new Point(283,
							496), stylesNoteDefence2.getPreferredSize()));
					CombatStylesTabDefence.add(stylesNoteDefence2);
					stylesNoteDefence3.setFont(new Font("SansSerif", Font.BOLD,
							12));
					stylesNoteDefence3
							.setText("If no items are entered, the script will just wear what it wore last round");
					stylesNoteDefence3.setBounds(new Rectangle(new Point(283,
							520), stylesNoteDefence3.getPreferredSize()));
					CombatStylesTabDefence.add(stylesNoteDefence3);

					inventoryNoteText1Defence.setFont(new Font("SansSerif", 0,
							13));
					inventoryNoteText1Defence.setText("This list should have");
					inventoryNoteText1Defence.setBounds(new Rectangle(
							new Point(26, 520), inventoryNoteText1Defence
									.getPreferredSize()));
					CombatStylesTabDefence.add(inventoryNoteText1Defence);
					inventoryNoteText2Defence.setFont(new Font("SansSerif", 0,
							13));
					inventoryNoteText2Defence
							.setText("all your items from your");
					inventoryNoteText2Defence.setBounds(new Rectangle(
							new Point(26, 540), inventoryNoteText2Defence
									.getPreferredSize()));
					CombatStylesTabDefence.add(inventoryNoteText2Defence);
					inventoryNoteText3Defence.setFont(new Font("SansSerif", 0,
							13));
					inventoryNoteText3Defence
							.setText("equipment and inventory.");
					inventoryNoteText3Defence.setBounds(new Rectangle(
							new Point(26, 560), inventoryNoteText3Defence
									.getPreferredSize()));
					CombatStylesTabDefence.add(inventoryNoteText3Defence);
				}
			}
			return CombatStylesTabDefence;
		}

		@Override
		public void actionPerformed(final ActionEvent arg0) {

			if (arg0.getSource() == chckbxDmode) {
				dmode = chckbxDmode.isSelected();
			}
			if (arg0.getSource() == meleeAttackStyleBox) {
				meleeAttackStyle = meleeAttackStyleBox.getSelectedItem()
						.toString();
			}
			if (arg0.getSource() == rangeAttackStyleBox) {
				rangeAttackStyle = rangeAttackStyleBox.getSelectedItem()
						.toString();
			}
			if (arg0.getSource() == magicCastSpell) {
				magicSpell = magicCastSpell.getSelectedItem().toString();
			}
			if (arg0.getSource() == prayerTrigger) {
				prayerSwitch = prayerTrigger.isSelected();
			}
			if (arg0.getSource() == meleeSafeSpotTrigger) {
				meleeSafeSpotSwitch = meleeSafeSpotTrigger.isSelected();
			}
			if (arg0.getSource() == randomGameTrigger) {
				randomGameSwitch = randomGameTrigger.isSelected();
				if (randomGameTrigger.isSelected()) {
					everyGameTrigger.setSelected(false);
					everyGameSwitch = false;
					hitTrigger.setSelected(false);
					hitSwitch = false;
				}
			}
			if (arg0.getSource() == everyGameTrigger) {
				everyGameSwitch = everyGameTrigger.isSelected();
				if (everyGameTrigger.isSelected()) {
					randomGameTrigger.setSelected(false);
					randomGameSwitch = false;
					hitTrigger.setSelected(false);
					hitSwitch = false;
				}
			}
			if (arg0.getSource() == hitTrigger) {
				hitSwitch = hitTrigger.isSelected();
				if (hitTrigger.isSelected()) {
					randomGameTrigger.setSelected(false);
					randomGameSwitch = false;
					everyGameTrigger.setSelected(false);
					everyGameSwitch = false;
				}
			}

			// ATTACK
			// Melee Item Adding/Removing
			if (arg0.getSource() == meleeAdd) {
				meleeAddMethod();
			}
			if (arg0.getSource() == meleeRemove) {
				try {
					int indRemoving = meleeList.getSelectedIndex();
					while (indRemoving < 10) {
						meleeListIDsAdded[indRemoving] = meleeListIDsAdded[indRemoving + 1];
						indRemoving++;
					}
					meleeListIDsAdded[10] = -1;
					meleeModel.removeElementAt(meleeList.getSelectedIndex());
				} catch (ArrayIndexOutOfBoundsException ignored) {
				}
			}

			// Magic Item Adding/Removing
			if (arg0.getSource() == magicAdd) {
				magicAddMethod();
			}
			if (arg0.getSource() == magicRemove) {
				try {
					int indRemoving = magicList.getSelectedIndex();
					while (indRemoving < 10) {
						magicListIDsAdded[indRemoving] = magicListIDsAdded[indRemoving + 1];
						indRemoving++;
					}
					magicListIDsAdded[10] = -1;
					magicModel.removeElementAt(magicList.getSelectedIndex());
				} catch (ArrayIndexOutOfBoundsException ignored) {
				}
			}

			// Range Item Adding/Removing
			if (arg0.getSource() == rangeAdd) {
				rangeAddMethod();
			}
			if (arg0.getSource() == rangeRemove) {
				try {
					int indRemoving = rangeList.getSelectedIndex();
					while (indRemoving < 10) {
						rangeListIDsAdded[indRemoving] = rangeListIDsAdded[indRemoving + 1];
						indRemoving++;
					}
					rangeListIDsAdded[10] = -1;
					rangeModel.removeElementAt(rangeList.getSelectedIndex());
				} catch (ArrayIndexOutOfBoundsException ignored) {
				}
			}

			// DEFENCE
			// Melee Defence Item Adding/Removing
			if (arg0.getSource() == meleeAddDefence) {
				meleeAddMethodDefence();
			}
			if (arg0.getSource() == meleeRemoveDefence) {
				try {
					int indRemoving = meleeListDefence.getSelectedIndex();
					while (indRemoving < 10) {
						meleeListIDsAddedDefence[indRemoving] = meleeListIDsAddedDefence[indRemoving + 1];
						indRemoving++;
					}
					meleeListIDsAddedDefence[10] = -1;
					meleeModelDefence.removeElementAt(meleeListDefence
							.getSelectedIndex());
				} catch (ArrayIndexOutOfBoundsException ignored) {
				}
			}

			// Magic Item Adding/Removing
			if (arg0.getSource() == magicAddDefence) {
				magicAddMethodDefence();
			}
			if (arg0.getSource() == magicRemoveDefence) {
				try {
					int indRemoving = magicListDefence.getSelectedIndex();
					while (indRemoving < 10) {
						magicListIDsAddedDefence[indRemoving] = magicListIDsAddedDefence[indRemoving + 1];
						indRemoving++;
					}
					magicListIDsAddedDefence[10] = -1;
					magicModelDefence.removeElementAt(magicListDefence
							.getSelectedIndex());
				} catch (ArrayIndexOutOfBoundsException ignored) {
				}
			}

			// Range Item Adding/Removing
			if (arg0.getSource() == rangeAddDefence) {
				rangeAddMethodDefence();
			}
			if (arg0.getSource() == rangeRemoveDefence) {
				try {
					int indRemoving = rangeListDefence.getSelectedIndex();
					while (indRemoving < 10) {
						rangeListIDsAddedDefence[indRemoving] = rangeListIDsAddedDefence[indRemoving + 1];
						indRemoving++;
					}
					rangeListIDsAddedDefence[10] = -1;
					rangeModelDefence.removeElementAt(rangeListDefence
							.getSelectedIndex());
				} catch (ArrayIndexOutOfBoundsException ignored) {
				}
			}

			if (arg0.getSource() == chckbxAntiBan) {
				useAntiBan = chckbxAntiBan.isSelected();
			}
			if (arg0.getSource() == chckbxBandages) {
				useBandages = chckbxBandages.isSelected();
			}
			if (arg0.getSource() == teleOrbBox) {
				teleorbLong = teleOrbBox.getSelectedItem().toString();
			}
			if (arg0.getSource() == chckbxBankTokens) {
				bankTokens = chckbxBankTokens.isSelected();
			}
			if (arg0.getSource() == chckbxSearch) {
				search = chckbxSearch.isSelected();
			}
			if (arg0.getSource() == chckbxScreenshots) {
				screenshots = chckbxScreenshots.isSelected();
			}
			if (arg0.getSource() == chckbxPrayers) {
				usePrayers = chckbxPrayers.isSelected();
			}
			if (arg0.getSource() == chckbxAttackPrayers) {
				useAttackPrayers = chckbxAttackPrayers.isSelected();
			}
			if (arg0.getSource() == chckbxSkinPrayers) {
				useSkinPrayers = chckbxSkinPrayers.isSelected();
			}
			if (arg0.getSource() == chckbxQuickPrayers) {
				useQuickPrayers = chckbxQuickPrayers.isSelected();
			}
			if (arg0.getSource() == quickPrayersBox) {
				quickPrayersLong = quickPrayersBox.getSelectedItem().toString();
			}
			if (arg0.getSource() == startCombatStyle) {
				firstStyle = startCombatStyle.getSelectedItem().toString();
			}
			if (arg0.getSource() == chckbxFSGame) {
				FSGame = chckbxFSGame.isSelected();
			}

			// ATTACK
			if (arg0.getSource() == chckbxMelee) {
				meleeActive = chckbxMelee.isSelected();
				if (!meleeActive && !magicActive && !rangeActive) {
					JOptionPane.showMessageDialog(null,
							"At least one Attack Style must be selected");
					chckbxMelee.setSelected(true);
					meleeActive = true;
				} else {

					meleeScrollPane.setEnabled(chckbxMelee.isSelected());
					meleeAdd.setEnabled(chckbxMelee.isSelected());
					meleeRemove.setEnabled(chckbxMelee.isSelected());
					meleeAttackStyleBox.setEnabled(chckbxMelee.isSelected());
					if (chckbxMelee.isSelected()) {
						startStylesP[0] = "Melee";
						meleePanel.setBackground(enabledColor);
					} else {
						startStylesP[0] = "NULL";
						meleePanel.setBackground(disabledColor);
					}
					refreshStartStyleMenu();
					CombatStylesTab.repaint(meleeArea);
				}
			}
			if (arg0.getSource() == chckbxMagic) {
				magicActive = chckbxMagic.isSelected();
				if (!meleeActive && !magicActive && !rangeActive) {
					JOptionPane.showMessageDialog(null,
							"At least one Attack Style must be selected");
					chckbxMagic.setSelected(true);
					magicActive = true;
				} else {
					magicScrollPane.setEnabled(chckbxMagic.isSelected());
					magicAdd.setEnabled(chckbxMagic.isSelected());
					magicRemove.setEnabled(chckbxMagic.isSelected());
					magicCastSpell.setEnabled(chckbxMagic.isSelected());
					if (chckbxMagic.isSelected()) {
						startStylesP[1] = "Magic";
						magicPanel.setBackground(enabledColor);
					} else {
						startStylesP[1] = "NULL";
						magicPanel.setBackground(disabledColor);
					}
					refreshStartStyleMenu();
					CombatStylesTab.repaint(magicArea);
				}
			}
			if (arg0.getSource() == chckbxRange) {
				rangeActive = chckbxRange.isSelected();
				if (!meleeActive && !magicActive && !rangeActive) {
					JOptionPane.showMessageDialog(null,
							"At least one Attack Style must be selected");
					chckbxRange.setSelected(true);
					rangeActive = true;
				} else {
					rangeScrollPane.setEnabled(chckbxRange.isSelected());
					rangeAdd.setEnabled(chckbxRange.isSelected());
					rangeRemove.setEnabled(chckbxRange.isSelected());
					rangeAttackStyleBox.setEnabled(chckbxRange.isSelected());
					if (chckbxRange.isSelected()) {
						startStylesP[2] = "Range";
						rangePanel.setBackground(enabledColor);
					} else {
						startStylesP[2] = "NULL";
						rangePanel.setBackground(disabledColor);
					}
					refreshStartStyleMenu();
					CombatStylesTab.repaint(rangeArea);
				}
			}

			// DEFENCE
			if (arg0.getSource() == chckbxMeleeDefence) {
				meleeActiveDefence = chckbxMeleeDefence.isSelected();
				meleeScrollPaneDefence.setEnabled(chckbxMeleeDefence
						.isSelected());
				meleeAddDefence.setEnabled(chckbxMeleeDefence.isSelected());
				meleeRemoveDefence.setEnabled(chckbxMeleeDefence.isSelected());
				if (chckbxMeleeDefence.isSelected()) {
					meleePanelDefence.setBackground(enabledColor);
				} else {
					meleePanelDefence.setBackground(disabledColor);
				}
				CombatStylesTabDefence.repaint(meleeAreaDefence);
			}
			if (arg0.getSource() == chckbxMagicDefence) {
				magicActiveDefence = chckbxMagicDefence.isSelected();
				magicScrollPaneDefence.setEnabled(chckbxMagicDefence
						.isSelected());
				magicAddDefence.setEnabled(chckbxMagicDefence.isSelected());
				magicRemoveDefence.setEnabled(chckbxMagicDefence.isSelected());
				if (chckbxMagicDefence.isSelected()) {
					magicPanelDefence.setBackground(enabledColor);
				} else {
					magicPanelDefence.setBackground(disabledColor);
				}
				CombatStylesTabDefence.repaint(magicAreaDefence);
			}
			if (arg0.getSource() == chckbxRangeDefence) {
				rangeActiveDefence = chckbxRangeDefence.isSelected();
				rangeScrollPaneDefence.setEnabled(chckbxRangeDefence
						.isSelected());
				rangeAddDefence.setEnabled(chckbxRangeDefence.isSelected());
				rangeRemoveDefence.setEnabled(chckbxRangeDefence.isSelected());
				if (chckbxRangeDefence.isSelected()) {
					rangePanelDefence.setBackground(enabledColor);
				} else {
					rangePanelDefence.setBackground(disabledColor);
				}
				CombatStylesTabDefence.repaint(rangeAreaDefence);
			}

			if (arg0.getSource() == Save) {
				statusLabel.setText("Saving...");
				setStatusLabelBounds();
				try {

					final BufferedWriter out = new BufferedWriter(
							new FileWriter(settingsFile));

					out.write("FOGRunner Settings - Version:" + getVersion());
					out.newLine();

					// Options Tab
					out.write((chckbxAntiBan.isSelected() ? "true" : "false")
							+ ":"
							+ (chckbxBandages.isSelected() ? "true" : "false")
							+ ":"
							+ teleOrbBox.getSelectedIndex()
							+ ":"
							+ (chckbxBankTokens.isSelected() ? "true" : "false")
							+ ":"
							+ bankTokensNum.getText()
							+ ":"
							+ (chckbxSearch.isSelected() ? "true" : "false")
							+ ":"
							+ (chckbxScreenshots.isSelected() ? "true"
									: "false") + ":" + mouseSpeedBox.getText()
							+ ":"
							+ (chckbxDmode.isSelected() ? "true" : "false"));
					out.newLine();

					// Prayers Tab
					out.write((chckbxQuickPrayers.isSelected() ? "true"
							: "false")
							+ ":"
							+ quickPrayersBox.getSelectedIndex()
							+ ":"
							+ (chckbxPrayers.isSelected() ? "true" : "false")
							+ ":"
							+ (chckbxAttackPrayers.isSelected() ? "true"
									: "false")
							+ ":"
							+ (chckbxSkinPrayers.isSelected() ? "true"
									: "false"));
					out.newLine();

					// Melee Attack
					out.write((chckbxMelee.isSelected() ? "true" : "false")
							+ ":" + meleeAttackStyleBox.getSelectedIndex());
					out.newLine();
					int meleei = 0;
					while (meleei < meleeModel.size() - 1) {
						out.write(meleeModel
								.get(meleei)
								.toString()
								.substring(
										0,
										meleeModel.get(meleei).toString()
												.indexOf(" - "))
								+ ":");
						meleei++;
					}
					if (meleeModel.size() > 0) {
						out.write(meleeModel
								.get(meleei)
								.toString()
								.substring(
										0,
										meleeModel.get(meleei).toString()
												.indexOf(" - ")));
					}
					out.newLine();
					int meleei2 = 0;
					while (meleei2 < meleeModel.size() - 1) {
						out.write(meleeModel
								.get(meleei2)
								.toString()
								.substring(
										meleeModel.get(meleei2).toString()
												.indexOf(" - ") + 3)
								+ ":");
						meleei2++;
					}
					if (meleeModel.size() > 0) {
						out.write(meleeModel
								.get(meleei2)
								.toString()
								.substring(
										meleeModel.get(meleei2).toString()
												.indexOf(" - ") + 3));
					}
					out.newLine();

					// Magic Attack
					out.write((chckbxMagic.isSelected() ? "true" : "false")
							+ ":" + magicCastSpell.getSelectedIndex());
					out.newLine();
					int magici = 0;
					while (magici < magicModel.size() - 1) {
						out.write(magicModel
								.get(magici)
								.toString()
								.substring(
										0,
										magicModel.get(magici).toString()
												.indexOf(" - "))
								+ ":");
						magici++;
					}
					if (magicModel.size() > 0) {
						out.write(magicModel
								.get(magici)
								.toString()
								.substring(
										0,
										magicModel.get(magici).toString()
												.indexOf(" - ")));
					}
					out.newLine();
					int magici2 = 0;
					while (magici2 < magicModel.size() - 1) {
						out.write(magicModel
								.get(magici2)
								.toString()
								.substring(
										magicModel.get(magici2).toString()
												.indexOf(" - ") + 3)
								+ ":");
						magici2++;
					}
					if (magicModel.size() > 0) {
						out.write(magicModel
								.get(magici2)
								.toString()
								.substring(
										magicModel.get(magici2).toString()
												.indexOf(" - ") + 3));
					}
					out.newLine();

					// Range Attack
					out.write((chckbxRange.isSelected() ? "true" : "false")
							+ ":" + rangeAttackStyleBox.getSelectedIndex());
					out.newLine();
					int rangei = 0;
					while (rangei < rangeModel.size() - 1) {
						out.write(rangeModel
								.get(rangei)
								.toString()
								.substring(
										0,
										rangeModel.get(rangei).toString()
												.indexOf(" - "))
								+ ":");
						rangei++;
					}
					if (rangeModel.size() > 0) {
						out.write(rangeModel
								.get(rangei)
								.toString()
								.substring(
										0,
										rangeModel.get(rangei).toString()
												.indexOf(" - ")));
					}
					out.newLine();
					int rangei2 = 0;
					while (rangei2 < rangeModel.size() - 1) {
						out.write(rangeModel
								.get(rangei2)
								.toString()
								.substring(
										rangeModel.get(rangei2).toString()
												.indexOf(" - ") + 3)
								+ ":");
						rangei2++;
					}
					if (rangeModel.size() > 0) {
						out.write(rangeModel
								.get(rangei2)
								.toString()
								.substring(
										rangeModel.get(rangei2).toString()
												.indexOf(" - ") + 3));
					}
					out.newLine();

					// Attack Styles Options
					out.write(startCombatStyle.getSelectedIndex()
							+ ":"
							+ (prayerTrigger.isSelected() ? "true" : "false")
							+ ":"
							+ (everyGameTrigger.isSelected() ? "true" : "false")
							+ ":"
							+ (randomGameTrigger.isSelected() ? "true"
									: "false")
							+ ":"
							+ (hitTrigger.isSelected() ? "true" : "false")
							+ ":"
							+ hitTriggerBox1.getText()
							+ ":"
							+ hitTriggerBox2.getText()
							+ ":"
							+ (meleeSafeSpotTrigger.isSelected() ? "true"
									: "false") + ":"
							+ (chckbxFSGame.isSelected() ? "true" : "false"));
					out.newLine();

					// Melee Defence
					out.write((chckbxMeleeDefence.isSelected() ? "true"
							: "false"));
					out.newLine();
					int meleeDefencei = 0;
					while (meleeDefencei < meleeModelDefence.size() - 1) {
						out.write(meleeModelDefence
								.get(meleeDefencei)
								.toString()
								.substring(
										0,
										meleeModelDefence.get(meleeDefencei)
												.toString().indexOf(" - "))
								+ ":");
						meleeDefencei++;
					}
					if (meleeModelDefence.size() > 0) {
						out.write(meleeModelDefence
								.get(meleeDefencei)
								.toString()
								.substring(
										0,
										meleeModelDefence.get(meleeDefencei)
												.toString().indexOf(" - ")));
					}
					out.newLine();
					int meleeDefencei2 = 0;
					while (meleeDefencei2 < meleeModelDefence.size() - 1) {
						out.write(meleeModelDefence
								.get(meleeDefencei2)
								.toString()
								.substring(
										meleeModelDefence.get(meleeDefencei2)
												.toString().indexOf(" - ") + 3)
								+ ":");
						meleeDefencei2++;
					}
					if (meleeModelDefence.size() > 0) {
						out.write(meleeModelDefence
								.get(meleeDefencei2)
								.toString()
								.substring(
										meleeModelDefence.get(meleeDefencei2)
												.toString().indexOf(" - ") + 3));
					}
					out.newLine();

					// Magic Defence
					out.write((chckbxMagicDefence.isSelected() ? "true"
							: "false"));
					out.newLine();
					int magicDefencei = 0;
					while (magicDefencei < magicModelDefence.size() - 1) {
						out.write(magicModelDefence
								.get(magicDefencei)
								.toString()
								.substring(
										0,
										magicModelDefence.get(magicDefencei)
												.toString().indexOf(" - "))
								+ ":");
						magicDefencei++;
					}
					if (magicModelDefence.size() > 0) {
						out.write(magicModelDefence
								.get(magicDefencei)
								.toString()
								.substring(
										0,
										magicModelDefence.get(magicDefencei)
												.toString().indexOf(" - ")));
					}
					out.newLine();
					int magicDefencei2 = 0;
					while (magicDefencei2 < magicModelDefence.size() - 1) {
						out.write(magicModelDefence
								.get(magicDefencei2)
								.toString()
								.substring(
										magicModelDefence.get(magicDefencei2)
												.toString().indexOf(" - ") + 3)
								+ ":");
						magicDefencei2++;
					}
					if (magicModelDefence.size() > 0) {
						out.write(magicModelDefence
								.get(magicDefencei2)
								.toString()
								.substring(
										magicModelDefence.get(magicDefencei2)
												.toString().indexOf(" - ") + 3));
					}
					out.newLine();

					// Range Defence
					out.write((chckbxRangeDefence.isSelected() ? "true"
							: "false"));
					out.newLine();
					int rangeDefencei = 0;
					while (rangeDefencei < rangeModelDefence.size() - 1) {
						out.write(rangeModelDefence
								.get(rangeDefencei)
								.toString()
								.substring(
										0,
										rangeModelDefence.get(rangeDefencei)
												.toString().indexOf(" - "))
								+ ":");
						rangeDefencei++;
					}
					if (rangeModelDefence.size() > 0) {
						out.write(rangeModelDefence
								.get(rangeDefencei)
								.toString()
								.substring(
										0,
										rangeModelDefence.get(rangeDefencei)
												.toString().indexOf(" - ")));
					}
					out.newLine();
					int rangeDefencei2 = 0;
					while (rangeDefencei2 < rangeModelDefence.size() - 1) {
						out.write(rangeModelDefence
								.get(rangeDefencei2)
								.toString()
								.substring(
										rangeModelDefence.get(rangeDefencei2)
												.toString().indexOf(" - ") + 3)
								+ ":");
						rangeDefencei2++;
					}
					if (rangeModelDefence.size() > 0) {
						out.write(rangeModelDefence
								.get(rangeDefencei2)
								.toString()
								.substring(
										rangeModelDefence.get(rangeDefencei2)
												.toString().indexOf(" - ") + 3));
					}
					// out.newLine();

					out.close();

					statusLabel.setText("Saved");
					setStatusLabelBounds();

				} catch (Exception e) {
					statusLabel.setText("Error - See Log");
					setStatusLabelBounds();
					e.printStackTrace();
				}
			}

			if (arg0.getSource() == Load) {
				statusLabel.setText("Loading...");
				setStatusLabelBounds();
				try {
					final BufferedReader in = new BufferedReader(
							new FileReader(settingsFile));
					String line;
					double settingsVersion;
					String[] options = {};
					String[] prayers = {};

					String[] meleeOptions = {};
					String[] meleeIDsS = {};
					int[] meleeIDs = {};
					String[] meleeNames = {};
					String[] magicOptions = {};
					String[] magicIDsS = {};
					int[] magicIDs = {};
					String[] magicNames = {};
					String[] rangeOptions = {};
					String[] rangeIDsS = {};
					int[] rangeIDs = {};
					String[] rangeNames = {};

					String[] attackStyleOptions = {};

					String meleeOptionsDefence = "";
					String[] meleeIDsDefenceS = {};
					int[] meleeIDsDefence = {};
					String[] meleeNamesDefence = {};
					String magicOptionsDefence = "";
					String[] magicIDsDefenceS = {};
					int[] magicIDsDefence = {};
					String[] magicNamesDefence = {};
					String rangeOptionsDefence = "";
					String[] rangeIDsDefenceS = {};
					int[] rangeIDsDefence = {};
					String[] rangeNamesDefence = {};

					line = in.readLine(); // Line 1
					settingsVersion = Double.parseDouble(line.substring(line
							.indexOf(":") + 1));
					options = in.readLine().split(":"); // Line 2
					prayers = in.readLine().split(":"); // Line 3

					meleeOptions = in.readLine().split(":"); // Line 4
					line = in.readLine(); // Line 5
					if (line.contains(":")) {
						meleeIDsS = line.split(":");
						meleeIDs = sTi(meleeIDsS);
					} else if (!line.isEmpty()) {
						meleeIDsS = new String[1];
						meleeIDsS[0] = line;
						meleeIDs = sTi(meleeIDsS);
					} else {
						meleeIDsS = new String[1];
						meleeIDsS[0] = "";
					}
					line = in.readLine(); // Line 6
					if (line.contains(":")) {
						meleeNames = line.split(":");
					} else if (!line.isEmpty()) {
						meleeNames = new String[1];
						meleeNames[0] = line;
					} else {
						meleeNames = new String[1];
						meleeNames[0] = "";
					}

					magicOptions = in.readLine().split(":"); // Line 7
					line = in.readLine(); // Line 8
					if (line.contains(":")) {
						magicIDsS = line.split(":");
						magicIDs = sTi(magicIDsS);
					} else if (!line.isEmpty()) {
						magicIDsS = new String[1];
						magicIDsS[0] = line;
						magicIDs = sTi(magicIDsS);
					} else {
						magicIDsS = new String[1];
						magicIDsS[0] = "";
					}
					line = in.readLine(); // Line 9
					if (line.contains(":")) {
						magicNames = line.split(":");
					} else if (!line.isEmpty()) {
						magicNames = new String[1];
						magicNames[0] = line;
					} else {
						magicNames = new String[1];
						magicNames[0] = "";
					}

					rangeOptions = in.readLine().split(":"); // Line 10
					line = in.readLine(); // Line 11
					if (line.contains(":")) {
						rangeIDsS = line.split(":");
						rangeIDs = sTi(rangeIDsS);
					} else if (!line.isEmpty()) {
						rangeIDsS = new String[1];
						rangeIDsS[0] = line;
						rangeIDs = sTi(rangeIDsS);
					} else {
						rangeIDsS = new String[1];
						rangeIDsS[0] = "";
					}
					line = in.readLine(); // Line 12
					if (line.contains(":")) {
						rangeNames = line.split(":");
					} else if (!line.isEmpty()) {
						rangeNames = new String[1];
						rangeNames[0] = line;
					} else {
						rangeNames = new String[1];
						rangeNames[0] = "";
					}

					attackStyleOptions = in.readLine().split(":"); // Line 13

					meleeOptionsDefence = in.readLine(); // Line 14
					line = in.readLine(); // Line 15
					if (line.contains(":")) {
						meleeIDsDefenceS = line.split(":");
						meleeIDsDefence = sTi(meleeIDsDefenceS);
					} else if (!line.isEmpty()) {
						meleeIDsDefenceS = new String[1];
						meleeIDsDefenceS[0] = line;
						meleeIDsDefence = sTi(meleeIDsDefenceS);
					} else {
						meleeIDsDefenceS = new String[1];
						meleeIDsDefenceS[0] = "";
					}
					line = in.readLine(); // Line 16
					if (line.contains(":")) {
						meleeNamesDefence = line.split(":");
					} else if (!line.isEmpty()) {
						meleeNamesDefence = new String[1];
						meleeNamesDefence[0] = line;
					} else {
						meleeNamesDefence = new String[1];
						meleeNamesDefence[0] = "";
					}

					magicOptionsDefence = in.readLine(); // Line 17
					line = in.readLine(); // Line 18
					if (line.contains(":")) {
						magicIDsDefenceS = line.split(":");
						magicIDsDefence = sTi(magicIDsDefenceS);
					} else if (!line.isEmpty()) {
						magicIDsDefenceS = new String[1];
						magicIDsDefenceS[0] = line;
						magicIDsDefence = sTi(magicIDsDefenceS);
					} else {
						magicIDsDefenceS = new String[1];
						magicIDsDefenceS[0] = "";
					}
					line = in.readLine(); // Line 19
					if (line.contains(":")) {
						magicNamesDefence = line.split(":");
					} else if (!line.isEmpty()) {
						magicNamesDefence = new String[1];
						magicNamesDefence[0] = line;
					} else {
						magicNamesDefence = new String[1];
						magicNamesDefence[0] = "";
					}

					rangeOptionsDefence = in.readLine(); // Line 20
					line = in.readLine(); // Line 21
					if (line.contains(":")) {
						rangeIDsDefenceS = line.split(":");
						rangeIDsDefence = sTi(rangeIDsDefenceS);
					} else if (!line.isEmpty()) {
						rangeIDsDefenceS = new String[1];
						rangeIDsDefenceS[0] = line;
						rangeIDsDefence = sTi(rangeIDsDefenceS);
					} else {
						rangeIDsDefenceS = new String[1];
						rangeIDsDefenceS[0] = "";
					}
					line = in.readLine(); // Line 22
					if (line != null) {
						if (line.contains(":")) {
							rangeNamesDefence = line.split(":");
						} else if (!line.isEmpty()) {
							rangeNamesDefence = new String[1];
							rangeNamesDefence[0] = line;
						} else {
							rangeNamesDefence = new String[1];
							rangeNamesDefence[0] = "";
						}
					} else {
						rangeNamesDefence = new String[1];
						rangeNamesDefence[0] = "";
					}

					// Setting the Variables
					// Options Tab
					chckbxAntiBan.setSelected(Boolean.parseBoolean(options[0]));
					useAntiBan = chckbxAntiBan.isSelected();
					chckbxBandages
							.setSelected(Boolean.parseBoolean(options[1]));
					useBandages = chckbxBandages.isSelected();
					teleOrbBox.setSelectedIndex(Integer.parseInt(options[2]));
					teleorbLong = teleOrbBox.getSelectedItem().toString();
					chckbxBankTokens.setSelected(Boolean
							.parseBoolean(options[3]));
					bankTokens = chckbxBankTokens.isSelected();
					bankTokensNum.setText(options[4]);
					chckbxSearch.setSelected(Boolean.parseBoolean(options[5]));
					search = chckbxSearch.isSelected();
					chckbxScreenshots.setSelected(Boolean
							.parseBoolean(options[6]));
					screenshots = chckbxScreenshots.isSelected();
					mouseSpeedBox.setText(options[7]);
					chckbxDmode.setSelected(Boolean.parseBoolean(options[8]));
					dmode = chckbxDmode.isSelected();

					// Prayers Tab
					chckbxQuickPrayers.setSelected(Boolean
							.parseBoolean(prayers[0]));
					useQuickPrayers = chckbxQuickPrayers.isSelected();
					quickPrayersBox.setSelectedIndex(Integer
							.parseInt(prayers[1]));
					quickPrayersLong = quickPrayersBox.getSelectedItem()
							.toString();
					chckbxPrayers.setSelected(Boolean.parseBoolean(prayers[2]));
					usePrayers = chckbxPrayers.isSelected();
					chckbxAttackPrayers.setSelected(Boolean
							.parseBoolean(prayers[3]));
					useAttackPrayers = chckbxAttackPrayers.isSelected();
					chckbxSkinPrayers.setSelected(Boolean
							.parseBoolean(prayers[4]));
					useSkinPrayers = chckbxSkinPrayers.isSelected();

					// Attack Styles Tab

					chckbxMelee.setSelected(Boolean
							.parseBoolean(meleeOptions[0]));
					chckbxMagic.setSelected(Boolean
							.parseBoolean(magicOptions[0]));
					chckbxRange.setSelected(Boolean
							.parseBoolean(rangeOptions[0]));
					chckbxMeleeDefence.setSelected(Boolean
							.parseBoolean(meleeOptionsDefence));
					chckbxMagicDefence.setSelected(Boolean
							.parseBoolean(magicOptionsDefence));
					chckbxRangeDefence.setSelected(Boolean
							.parseBoolean(rangeOptionsDefence));

					meleeAttackStyleBox.setSelectedIndex(Integer
							.parseInt(meleeOptions[1]));
					meleeAttackStyle = meleeAttackStyleBox.getSelectedItem()
							.toString();
					magicCastSpell.setSelectedIndex(Integer
							.parseInt(magicOptions[1]));
					magicSpell = magicCastSpell.getSelectedItem().toString();
					rangeAttackStyleBox.setSelectedIndex(Integer
							.parseInt(rangeOptions[1]));
					rangeAttackStyle = rangeAttackStyleBox.getSelectedItem()
							.toString();

					int[] notFoundItems = new int[39];
					String[] notFoundItemsNames = new String[39];
					int notFoundItemsIndex = 0;

					// ATTACK
					int iMelee = 0;
					while (iMelee < meleeIDs.length) {
						if (itemFound(meleeIDs[iMelee])) {
							int invIndex = invIndexOf(meleeIDs[iMelee]);
							invList.setSelectedIndex(invIndex);
							meleeAddMethod();
						} else {
							if (!arrayContains(meleeIDs[iMelee], notFoundItems)) {
								notFoundItems[notFoundItemsIndex] = meleeIDs[iMelee];
								notFoundItemsNames[notFoundItemsIndex] = meleeNames[iMelee];
								notFoundItemsIndex++;
							}
						}
						iMelee++;
					}

					int iMagic = 0;
					while (iMagic < magicIDs.length) {
						if (itemFound(magicIDs[iMagic])) {
							int invIndex = invIndexOf(magicIDs[iMagic]);
							invList.setSelectedIndex(invIndex);
							magicAddMethod();
						} else {
							if (!arrayContains(magicIDs[iMagic], notFoundItems)) {
								notFoundItems[notFoundItemsIndex] = magicIDs[iMagic];
								notFoundItemsNames[notFoundItemsIndex] = magicNames[iMagic];
								notFoundItemsIndex++;
							}
						}
						iMagic++;
					}

					int iRange = 0;
					while (iRange < rangeIDs.length) {
						if (itemFound(rangeIDs[iRange])) {
							int invIndex = invIndexOf(rangeIDs[iRange]);
							invList.setSelectedIndex(invIndex);
							rangeAddMethod();
						} else {
							if (!arrayContains(rangeIDs[iRange], notFoundItems)) {
								notFoundItems[notFoundItemsIndex] = rangeIDs[iRange];
								notFoundItemsNames[notFoundItemsIndex] = rangeNames[iRange];
								notFoundItemsIndex++;
							}
						}
						iRange++;
					}

					// DEFENCE
					int iMeleeDefence = 0;
					while (iMeleeDefence < meleeIDsDefence.length) {
						if (itemFound(meleeIDsDefence[iMeleeDefence])) {
							int invIndex = invIndexOf(meleeIDsDefence[iMeleeDefence]);
							invListDefence.setSelectedIndex(invIndex);
							meleeAddMethodDefence();
						} else {
							if (!arrayContains(meleeIDsDefence[iMeleeDefence],
									notFoundItems)) {
								notFoundItems[notFoundItemsIndex] = meleeIDsDefence[iMeleeDefence];
								notFoundItemsNames[notFoundItemsIndex] = meleeNamesDefence[iMeleeDefence];
								notFoundItemsIndex++;
							}
						}
						iMeleeDefence++;
					}

					int iMagicDefence = 0;
					while (iMagicDefence < magicIDsDefence.length) {
						if (itemFound(magicIDsDefence[iMagicDefence])) {
							int invIndex = invIndexOf(magicIDsDefence[iMagicDefence]);
							invListDefence.setSelectedIndex(invIndex);
							magicAddMethodDefence();
						} else {
							if (!arrayContains(magicIDsDefence[iMagicDefence],
									notFoundItems)) {
								notFoundItems[notFoundItemsIndex] = magicIDsDefence[iMagicDefence];
								notFoundItemsNames[notFoundItemsIndex] = magicNamesDefence[iMagicDefence];
								notFoundItemsIndex++;
							}
						}
						iMagicDefence++;
					}

					int iRangeDefence = 0;
					while (iRangeDefence < rangeIDsDefence.length) {
						if (itemFound(rangeIDsDefence[iRangeDefence])) {
							int invIndex = invIndexOf(rangeIDsDefence[iRangeDefence]);
							invListDefence.setSelectedIndex(invIndex);
							rangeAddMethodDefence();
						} else {
							if (!arrayContains(rangeIDsDefence[iRangeDefence],
									notFoundItems)) {
								notFoundItems[notFoundItemsIndex] = rangeIDsDefence[iRangeDefence];
								notFoundItemsNames[notFoundItemsIndex] = rangeNamesDefence[iRangeDefence];
								notFoundItemsIndex++;
							}
						}
						iRangeDefence++;
					}

					meleeActive = chckbxMelee.isSelected();
					if (!meleeActive && !magicActive && !rangeActive) {
						JOptionPane.showMessageDialog(null,
								"At least one Attack Style must be selected");
						chckbxMelee.setSelected(true);
						meleeActive = true;
					} else {

						meleeScrollPane.setEnabled(chckbxMelee.isSelected());
						meleeAdd.setEnabled(chckbxMelee.isSelected());
						meleeRemove.setEnabled(chckbxMelee.isSelected());
						meleeAttackStyleBox
								.setEnabled(chckbxMelee.isSelected());
						if (chckbxMelee.isSelected()) {
							startStylesP[0] = "Melee";
							meleePanel.setBackground(enabledColor);
						} else {
							startStylesP[0] = "NULL";
							meleePanel.setBackground(disabledColor);
						}
						refreshStartStyleMenu();
						CombatStylesTab.repaint(meleeArea);
					}

					magicActive = chckbxMagic.isSelected();
					if (!meleeActive && !magicActive && !rangeActive) {
						JOptionPane.showMessageDialog(null,
								"At least one Attack Style must be selected");
						chckbxMagic.setSelected(true);
						magicActive = true;
					} else {
						magicScrollPane.setEnabled(chckbxMagic.isSelected());
						magicAdd.setEnabled(chckbxMagic.isSelected());
						magicRemove.setEnabled(chckbxMagic.isSelected());
						magicCastSpell.setEnabled(chckbxMagic.isSelected());
						if (chckbxMagic.isSelected()) {
							startStylesP[1] = "Magic";
							magicPanel.setBackground(enabledColor);
						} else {
							startStylesP[1] = "NULL";
							magicPanel.setBackground(disabledColor);
						}
						refreshStartStyleMenu();
						CombatStylesTab.repaint(magicArea);
					}

					rangeActive = chckbxRange.isSelected();
					if (!meleeActive && !magicActive && !rangeActive) {
						JOptionPane.showMessageDialog(null,
								"At least one Attack Style must be selected");
						chckbxRange.setSelected(true);
						rangeActive = true;
					} else {
						rangeScrollPane.setEnabled(chckbxRange.isSelected());
						rangeAdd.setEnabled(chckbxRange.isSelected());
						rangeRemove.setEnabled(chckbxRange.isSelected());
						rangeAttackStyleBox
								.setEnabled(chckbxRange.isSelected());
						if (chckbxRange.isSelected()) {
							startStylesP[2] = "Range";
							rangePanel.setBackground(enabledColor);
						} else {
							startStylesP[2] = "NULL";
							rangePanel.setBackground(disabledColor);
						}
						refreshStartStyleMenu();
						CombatStylesTab.repaint(rangeArea);
					}

					meleeActiveDefence = chckbxMeleeDefence.isSelected();
					meleeScrollPaneDefence.setEnabled(chckbxMeleeDefence
							.isSelected());
					meleeAddDefence.setEnabled(chckbxMeleeDefence.isSelected());
					meleeRemoveDefence.setEnabled(chckbxMeleeDefence
							.isSelected());
					if (chckbxMeleeDefence.isSelected()) {
						meleePanelDefence.setBackground(enabledColor);
					} else {
						meleePanelDefence.setBackground(disabledColor);
					}
					CombatStylesTabDefence.repaint(meleeAreaDefence);

					magicActiveDefence = chckbxMagicDefence.isSelected();
					magicScrollPaneDefence.setEnabled(chckbxMagicDefence
							.isSelected());
					magicAddDefence.setEnabled(chckbxMagicDefence.isSelected());
					magicRemoveDefence.setEnabled(chckbxMagicDefence
							.isSelected());
					if (chckbxMagicDefence.isSelected()) {
						magicPanelDefence.setBackground(enabledColor);
					} else {
						magicPanelDefence.setBackground(disabledColor);
					}
					CombatStylesTabDefence.repaint(magicAreaDefence);

					rangeActiveDefence = chckbxRangeDefence.isSelected();
					rangeScrollPaneDefence.setEnabled(chckbxRangeDefence
							.isSelected());
					rangeAddDefence.setEnabled(chckbxRangeDefence.isSelected());
					rangeRemoveDefence.setEnabled(chckbxRangeDefence
							.isSelected());
					if (chckbxRangeDefence.isSelected()) {
						rangePanelDefence.setBackground(enabledColor);
					} else {
						rangePanelDefence.setBackground(disabledColor);
					}
					CombatStylesTabDefence.repaint(rangeAreaDefence);

					// Attack Style Options
					startCombatStyle.setSelectedIndex(Integer
							.parseInt(attackStyleOptions[0]));
					firstStyle = startCombatStyle.getSelectedItem().toString();
					prayerTrigger.setSelected(Boolean
							.parseBoolean(attackStyleOptions[1]));
					prayerSwitch = prayerTrigger.isSelected();
					everyGameTrigger.setSelected(Boolean
							.parseBoolean(attackStyleOptions[2]));
					everyGameSwitch = everyGameTrigger.isSelected();
					if (everyGameTrigger.isSelected()) {
						randomGameTrigger.setSelected(false);
						randomGameSwitch = false;
						hitTrigger.setSelected(false);
						hitSwitch = false;
					}
					randomGameTrigger.setSelected(Boolean
							.parseBoolean(attackStyleOptions[3]));
					randomGameSwitch = randomGameTrigger.isSelected();
					if (randomGameTrigger.isSelected()) {
						everyGameTrigger.setSelected(false);
						everyGameSwitch = false;
						hitTrigger.setSelected(false);
						hitSwitch = false;
					}
					hitTrigger.setSelected(Boolean
							.parseBoolean(attackStyleOptions[4]));
					hitSwitch = hitTrigger.isSelected();
					if (hitTrigger.isSelected()) {
						randomGameTrigger.setSelected(false);
						randomGameSwitch = false;
						everyGameTrigger.setSelected(false);
						everyGameSwitch = false;
					}
					hitTriggerBox1.setText(attackStyleOptions[5]);
					hitTriggerBox2.setText(attackStyleOptions[6]);
					// VERSION 1.0+ ONLY [[[
					if (settingsVersion >= 1.0) {
						// MELEE SAFE SPOT TRIGGER OPTION
						if (attackStyleOptions.length > 7) {
							meleeSafeSpotTrigger.setSelected(Boolean
									.parseBoolean(attackStyleOptions[7]));
							meleeSafeSpotSwitch = meleeSafeSpotTrigger
									.isSelected();
						}
						// USE FIRST ATTACK STYLE AT START OF EACH ROUND OPTION
						if (attackStyleOptions.length > 8) {
							chckbxFSGame.setSelected(Boolean
									.parseBoolean(attackStyleOptions[8]));
							FSGame = chckbxFSGame.isSelected();
						}
					}
					// ]]]

					in.close();

					if (notFoundItemsIndex != 0) {
						String notFoundItemsText = "";
						int i = 0;
						while (i < notFoundItemsIndex) {
							notFoundItemsText = notFoundItemsText
									+ Integer.toString(notFoundItems[i])
									+ " - " + notFoundItemsNames[i] + "\n";
							i++;
						}
						JOptionPane
								.showMessageDialog(
										null,
										"These saved wield items were not found in your inventory,\n They will not be wielded:\n\n"
												+ notFoundItemsText);
					}

					statusLabel.setText("Loaded");
					setStatusLabelBounds();

				} catch (FileNotFoundException e) {
					statusLabel.setText("No Settings File");
					setStatusLabelBounds();
				} catch (Exception e) {
					statusLabel.setText("Error - See Log");
					setStatusLabelBounds();
					e.printStackTrace();
				}
			}

			if (arg0.getSource() == Cancel) {
				dieScript = true;
				dispose();
			}

			if (arg0.getSource() == Start) {
				boolean canStart = true;

				String mouseSpeedString = "";
				mouseSpeedString = mouseSpeedBox.getText();
				try {
					mouseSpeed = Integer.parseInt(mouseSpeedString);
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null,
							"Please enter a NUMBER in the mouse speed box");
					canStart = false;
				}
				if (mouseSpeed < 1 || mouseSpeed > 25) {
					JOptionPane
							.showMessageDialog(null,
									"Please enter a valid number in the mouse speed box");
					canStart = false;
				}

				if (bankTokens) {
					tokensBeforeBankS = bankTokensNum.getText();
					try {
						tokensBeforeBank = Integer.parseInt(tokensBeforeBankS);
					} catch (NumberFormatException e) {
						JOptionPane
								.showMessageDialog(null,
										"Please enter a NUMBER in the 'Bank Tokens After' box");
						canStart = false;
					}
					if (tokensBeforeBank < 1) {
						JOptionPane
								.showMessageDialog(null,
										"Please enter a number more than 0 in the 'Bank Tokens After' box");
						canStart = false;
					}
				}

				if (hitSwitch) {
					String hitTriggerBox1Text = hitTriggerBox1.getText();
					String hitTriggerBox2Text = hitTriggerBox2.getText();
					int hitSwitchMinHitsP = 0;
					int hitSwitchMaxHitsP = 0;
					try {
						hitSwitchMinHitsP = Integer
								.parseInt(hitTriggerBox1Text);
						hitSwitchMaxHitsP = Integer
								.parseInt(hitTriggerBox2Text);
						if (hitSwitchMinHits < 0 || hitSwitchMaxHits < 0) {
							JOptionPane
									.showMessageDialog(
											null,
											"Please enter positive numbers in the 'Change Attack Style on Opponent Hitsplats' boxes");
							canStart = false;
						}
						if (canStart) {
							if (hitSwitchMaxHitsP >= hitSwitchMinHitsP) {
								hitSwitchMaxHits = hitSwitchMaxHitsP;
								hitSwitchMinHits = hitSwitchMinHitsP;
							} else {
								hitSwitchMaxHits = hitSwitchMinHitsP;
								hitSwitchMinHits = hitSwitchMaxHitsP;
							}
						}
					} catch (NumberFormatException e) {
						JOptionPane
								.showMessageDialog(null,
										"Please enter NUMBERS in the 'Change Attack Style on Opponent Hitsplats' boxes");
						canStart = false;
					}

				}

				if (canStart) {
					// ATTACK
					if (meleeActive) {
						createMeleeItemsArray();
					}
					if (magicActive) {
						createMagicItemsArray();
					}
					if (rangeActive) {
						createRangeItemsArray();
					}
					// DEFENCE
					if (meleeActiveDefence) {
						createMeleeItemsArrayDefence();
					}
					if (magicActiveDefence) {
						createMagicItemsArrayDefence();
					}
					if (rangeActiveDefence) {
						createRangeItemsArrayDefence();
					}

					if (bankTokens) {
						if (dmode) {
							log("Will bank tokens when over "
									+ tokensBeforeBank);
						}
					}
					dieScript = false;
					dispose();
				}
			}
		}

		public boolean itemFound(int itemID) {
			int i = 0;
			boolean well = false;
			while (i < inventoryItemsIDs.length) {
				if (inventoryItemsIDs[i] == itemID) {
					well = true;
				}
				i++;
			}
			return well;
		}

		public boolean arrayContains(int itemID, int[] containingArray) {
			int i = 0;
			boolean well = false;
			while (i < containingArray.length) {
				if (containingArray[i] == itemID) {
					well = true;
				}
				i++;
			}
			return well;
		}

		public void meleeAddMethod() {
			if (invList.getSelectedIndex() == meleeListIDsAdded[0]
					|| invList.getSelectedIndex() == meleeListIDsAdded[1]
					|| invList.getSelectedIndex() == meleeListIDsAdded[2]
					|| invList.getSelectedIndex() == meleeListIDsAdded[3]
					|| invList.getSelectedIndex() == meleeListIDsAdded[4]
					|| invList.getSelectedIndex() == meleeListIDsAdded[5]
					|| invList.getSelectedIndex() == meleeListIDsAdded[6]
					|| invList.getSelectedIndex() == meleeListIDsAdded[7]
					|| invList.getSelectedIndex() == meleeListIDsAdded[8]
					|| invList.getSelectedIndex() == meleeListIDsAdded[9]
					|| invList.getSelectedIndex() == meleeListIDsAdded[10]) {
			} else if (meleeListIDsAdded[10] != -1) {
				JOptionPane.showMessageDialog(null,
						"You cannot wield more than 11 items...");
			} else {
				String IDString = Integer.toString(inventoryItemsIDs[invList
						.getSelectedIndex()]);
				int zerosToAdd = 5 - IDString.length();
				String zeros = "";
				int wey = 0;
				while (wey < zerosToAdd) {
					zeros = zeros + "0";
					wey++;
				}
				zeros = zeros + IDString;
				IDString = zeros;
				meleeModel.add(meleeModel.size(), IDString + " - "
						+ inventoryItemsNames[invList.getSelectedIndex()]);
				meleeListIDsAdded[meleeModel.size() - 1] = invList
						.getSelectedIndex();
			}
		}

		public void magicAddMethod() {
			if (invList.getSelectedIndex() == magicListIDsAdded[0]
					|| invList.getSelectedIndex() == magicListIDsAdded[1]
					|| invList.getSelectedIndex() == magicListIDsAdded[2]
					|| invList.getSelectedIndex() == magicListIDsAdded[3]
					|| invList.getSelectedIndex() == magicListIDsAdded[4]
					|| invList.getSelectedIndex() == magicListIDsAdded[5]
					|| invList.getSelectedIndex() == magicListIDsAdded[6]
					|| invList.getSelectedIndex() == magicListIDsAdded[7]
					|| invList.getSelectedIndex() == magicListIDsAdded[8]
					|| invList.getSelectedIndex() == magicListIDsAdded[9]
					|| invList.getSelectedIndex() == magicListIDsAdded[10]) {
			} else if (magicListIDsAdded[10] != -1) {
				JOptionPane.showMessageDialog(null,
						"You cannot wield more than 11 items...");
			} else {
				String IDString = Integer.toString(inventoryItemsIDs[invList
						.getSelectedIndex()]);
				int zerosToAdd = 5 - IDString.length();
				String zeros = "";
				int wey = 0;
				while (wey < zerosToAdd) {
					zeros = zeros + "0";
					wey++;
				}
				zeros = zeros + IDString;
				IDString = zeros;
				magicModel.add(magicModel.size(), IDString + " - "
						+ inventoryItemsNames[invList.getSelectedIndex()]);
				magicListIDsAdded[magicModel.size() - 1] = invList
						.getSelectedIndex();
			}
		}

		public void rangeAddMethod() {
			if (invList.getSelectedIndex() == rangeListIDsAdded[0]
					|| invList.getSelectedIndex() == rangeListIDsAdded[1]
					|| invList.getSelectedIndex() == rangeListIDsAdded[2]
					|| invList.getSelectedIndex() == rangeListIDsAdded[3]
					|| invList.getSelectedIndex() == rangeListIDsAdded[4]
					|| invList.getSelectedIndex() == rangeListIDsAdded[5]
					|| invList.getSelectedIndex() == rangeListIDsAdded[6]
					|| invList.getSelectedIndex() == rangeListIDsAdded[7]
					|| invList.getSelectedIndex() == rangeListIDsAdded[8]
					|| invList.getSelectedIndex() == rangeListIDsAdded[9]
					|| invList.getSelectedIndex() == rangeListIDsAdded[10]) {
			} else if (rangeListIDsAdded[10] != -1) {
				JOptionPane.showMessageDialog(null,
						"You cannot wield more than 11 items...");
			} else {
				String IDString = Integer.toString(inventoryItemsIDs[invList
						.getSelectedIndex()]);
				int zerosToAdd = 5 - IDString.length();
				String zeros = "";
				int wey = 0;
				while (wey < zerosToAdd) {
					zeros = zeros + "0";
					wey++;
				}
				zeros = zeros + IDString;
				IDString = zeros;
				rangeModel.add(rangeModel.size(), IDString + " - "
						+ inventoryItemsNames[invList.getSelectedIndex()]);
				rangeListIDsAdded[rangeModel.size() - 1] = invList
						.getSelectedIndex();
			}
		}

		public void meleeAddMethodDefence() {
			if (invListDefence.getSelectedIndex() == meleeListIDsAddedDefence[0]
					|| invListDefence.getSelectedIndex() == meleeListIDsAddedDefence[1]
					|| invListDefence.getSelectedIndex() == meleeListIDsAddedDefence[2]
					|| invListDefence.getSelectedIndex() == meleeListIDsAddedDefence[3]
					|| invListDefence.getSelectedIndex() == meleeListIDsAddedDefence[4]
					|| invListDefence.getSelectedIndex() == meleeListIDsAddedDefence[5]
					|| invListDefence.getSelectedIndex() == meleeListIDsAddedDefence[6]
					|| invListDefence.getSelectedIndex() == meleeListIDsAddedDefence[7]
					|| invListDefence.getSelectedIndex() == meleeListIDsAddedDefence[8]
					|| invListDefence.getSelectedIndex() == meleeListIDsAddedDefence[9]
					|| invListDefence.getSelectedIndex() == meleeListIDsAddedDefence[10]) {
			} else if (meleeListIDsAddedDefence[10] != -1) {
				JOptionPane.showMessageDialog(null,
						"You cannot wield more than 11 items...");
			} else {
				String IDString = Integer
						.toString(inventoryItemsIDs[invListDefence
								.getSelectedIndex()]);
				int zerosToAdd = 5 - IDString.length();
				String zeros = "";
				int wey = 0;
				while (wey < zerosToAdd) {
					zeros = zeros + "0";
					wey++;
				}
				zeros = zeros + IDString;
				IDString = zeros;
				meleeModelDefence.add(
						meleeModelDefence.size(),
						IDString
								+ " - "
								+ inventoryItemsNames[invListDefence
										.getSelectedIndex()]);
				meleeListIDsAddedDefence[meleeModelDefence.size() - 1] = invListDefence
						.getSelectedIndex();
			}
		}

		public void magicAddMethodDefence() {
			if (invListDefence.getSelectedIndex() == magicListIDsAddedDefence[0]
					|| invListDefence.getSelectedIndex() == magicListIDsAddedDefence[1]
					|| invListDefence.getSelectedIndex() == magicListIDsAddedDefence[2]
					|| invListDefence.getSelectedIndex() == magicListIDsAddedDefence[3]
					|| invListDefence.getSelectedIndex() == magicListIDsAddedDefence[4]
					|| invListDefence.getSelectedIndex() == magicListIDsAddedDefence[5]
					|| invListDefence.getSelectedIndex() == magicListIDsAddedDefence[6]
					|| invListDefence.getSelectedIndex() == magicListIDsAddedDefence[7]
					|| invListDefence.getSelectedIndex() == magicListIDsAddedDefence[8]
					|| invListDefence.getSelectedIndex() == magicListIDsAddedDefence[9]
					|| invListDefence.getSelectedIndex() == magicListIDsAddedDefence[10]) {
			} else if (magicListIDsAddedDefence[10] != -1) {
				JOptionPane.showMessageDialog(null,
						"You cannot wield more than 11 items...");
			} else {
				String IDString = Integer
						.toString(inventoryItemsIDs[invListDefence
								.getSelectedIndex()]);
				int zerosToAdd = 5 - IDString.length();
				String zeros = "";
				int wey = 0;
				while (wey < zerosToAdd) {
					zeros = zeros + "0";
					wey++;
				}
				zeros = zeros + IDString;
				IDString = zeros;
				magicModelDefence.add(
						magicModelDefence.size(),
						IDString
								+ " - "
								+ inventoryItemsNames[invListDefence
										.getSelectedIndex()]);
				magicListIDsAddedDefence[magicModelDefence.size() - 1] = invListDefence
						.getSelectedIndex();
			}
		}

		public void rangeAddMethodDefence() {
			if (invListDefence.getSelectedIndex() == rangeListIDsAddedDefence[0]
					|| invListDefence.getSelectedIndex() == rangeListIDsAddedDefence[1]
					|| invListDefence.getSelectedIndex() == rangeListIDsAddedDefence[2]
					|| invListDefence.getSelectedIndex() == rangeListIDsAddedDefence[3]
					|| invListDefence.getSelectedIndex() == rangeListIDsAddedDefence[4]
					|| invListDefence.getSelectedIndex() == rangeListIDsAddedDefence[5]
					|| invListDefence.getSelectedIndex() == rangeListIDsAddedDefence[6]
					|| invListDefence.getSelectedIndex() == rangeListIDsAddedDefence[7]
					|| invListDefence.getSelectedIndex() == rangeListIDsAddedDefence[8]
					|| invListDefence.getSelectedIndex() == rangeListIDsAddedDefence[9]
					|| invListDefence.getSelectedIndex() == rangeListIDsAddedDefence[10]) {
			} else if (rangeListIDsAdded[10] != -1) {
				JOptionPane.showMessageDialog(null,
						"You cannot wield more than 11 items...");
			} else {
				String IDString = Integer
						.toString(inventoryItemsIDs[invListDefence
								.getSelectedIndex()]);
				int zerosToAdd = 5 - IDString.length();
				String zeros = "";
				int wey = 0;
				while (wey < zerosToAdd) {
					zeros = zeros + "0";
					wey++;
				}
				zeros = zeros + IDString;
				IDString = zeros;
				rangeModelDefence.add(
						rangeModelDefence.size(),
						IDString
								+ " - "
								+ inventoryItemsNames[invListDefence
										.getSelectedIndex()]);
				rangeListIDsAddedDefence[rangeModelDefence.size() - 1] = invListDefence
						.getSelectedIndex();
			}
		}

		public int invIndexOf(int itemID) {
			int i = 0;
			boolean found = false;
			while (i < inventoryItemsIDs.length && !found) {
				if (inventoryItemsIDs[i] == itemID) {
					found = true;
				} else {
					i++;
				}
			}
			return i;
		}

		public int[] sTi(String[] sarray) throws Exception { // String Array to
																// Int Array
			if (sarray != null) {
				int intarray[] = new int[sarray.length];
				for (int i = 0; i < sarray.length; i++) {
					intarray[i] = Integer.parseInt(sarray[i]);
				}
				return intarray;
			}
			return null;
		}

		@Override
		public void valueChanged(final ListSelectionEvent arg0) {
			// ATTACK
			if (arg0.getSource() == invList) {
				inventoryListSelectedID = invList.getSelectedIndex();
			}
			// DEFENCE
			if (arg0.getSource() == invListDefence) {
				inventoryListSelectedIDDefence = invListDefence
						.getSelectedIndex();
			}
		}

		// ATTACK
		public void createMeleeItemsArray() {
			int numberOfItems = meleeModel.size();
			meleeEquipment = new int[numberOfItems];
			int i = 0;
			while (i < numberOfItems) {
				meleeEquipment[i] = inventoryItemsIDs[meleeListIDsAdded[i]];
				i++;
			}
		}

		public void createMagicItemsArray() {
			int numberOfItems = magicModel.size();
			magicEquipment = new int[numberOfItems];
			int i = 0;
			while (i < numberOfItems) {
				magicEquipment[i] = inventoryItemsIDs[magicListIDsAdded[i]];
				i++;
			}
		}

		public void createRangeItemsArray() {
			int numberOfItems = rangeModel.size();
			rangeEquipment = new int[numberOfItems];
			int i = 0;
			while (i < numberOfItems) {
				rangeEquipment[i] = inventoryItemsIDs[rangeListIDsAdded[i]];
				i++;
			}
		}

		public void refreshStartStyleMenu() {

			startStyles1[0] = "NULL";
			startStyles2[0] = "NULL";
			startStyles2[1] = "NULL";
			startStyles3[0] = "NULL";
			startStyles3[1] = "NULL";
			startStyles3[2] = "NULL";

			int stylesEnabled = 0;
			int i = 0;
			while (i < 3) {
				if (!startStylesP[i].equals("NULL")) {
					stylesEnabled++;
				}
				i++;
			}
			if (dmode) {
				log("Styles Enabled for Attack: " + stylesEnabled);
			}
			switch (stylesEnabled) {
			case 1: {
				int one = 0;
				while (startStyles1[0].equals("NULL")) {
					startStyles1[0] = startStylesP[one];
					one++;
				}
				startCombatStyle
						.setModel(new DefaultComboBoxModel(startStyles1));
				firstStyle = startStyles1[0];
				break;
			}
			case 2: {
				int two = 0;
				while (startStyles2[0].equals("NULL")) {
					startStyles2[0] = startStylesP[two];
					two++;
				}
				while (startStyles2[1].equals("NULL")) {
					startStyles2[1] = startStylesP[two];
					two++;
				}
				startCombatStyle
						.setModel(new DefaultComboBoxModel(startStyles2));
				firstStyle = startStyles2[0];
				break;
			}
			case 3: {
				startStyles3[0] = startStylesP[0];
				startStyles3[1] = startStylesP[1];
				startStyles3[2] = startStylesP[2];
				startCombatStyle
						.setModel(new DefaultComboBoxModel(startStyles3));
				firstStyle = startStyles3[0];
				break;
			}
			}
		}

		// DEFENCE
		public void createMeleeItemsArrayDefence() {
			int numberOfItems = meleeModelDefence.size();
			meleeEquipmentDefence = new int[numberOfItems];
			int i = 0;
			while (i < numberOfItems) {
				meleeEquipmentDefence[i] = inventoryItemsIDs[meleeListIDsAddedDefence[i]];
				i++;
			}
		}

		public void createMagicItemsArrayDefence() {
			int numberOfItems = magicModelDefence.size();
			magicEquipmentDefence = new int[numberOfItems];
			int i = 0;
			while (i < numberOfItems) {
				magicEquipmentDefence[i] = inventoryItemsIDs[magicListIDsAddedDefence[i]];
				i++;
			}
		}

		public void createRangeItemsArrayDefence() {
			int numberOfItems = rangeModelDefence.size();
			rangeEquipmentDefence = new int[numberOfItems];
			int i = 0;
			while (i < numberOfItems) {
				rangeEquipmentDefence[i] = inventoryItemsIDs[rangeListIDsAddedDefence[i]];
				i++;
			}
		}
	}

	public Point getPlayerScreenLoc() {
		Point thePoint = new Point(245, 156);
		try {
			if (calc.tileOnScreen(getPlayerLocThrow())) {
				thePoint = calc.tileToScreen(getPlayerLocThrow(), 4);
			}
		} catch (NullPointerException ignored) {
		}
		return thePoint;
	}

	public Point getPlayerScreenLocThrow() throws NullPointerException {
		Point thePoint = new Point(245, 156);
		if (calc.tileOnScreen(getPlayerLocThrow())) {
			thePoint = calc.tileToScreen(getPlayerLocThrow(), 4);
		}
		return thePoint;
	}

	public RSTile getPlayerLoc() {
		try {
			return getPlayer().getLocation();
		} catch (NullPointerException ignored) {
		}
		return new RSTile(0, 0);
	}

	public RSTile getPlayerLocThrow() throws NullPointerException {
		RSTile theTile = getPlayerThrow().getLocation();
		return theTile;
	}

	public boolean playerLocKnown() {
		if (getPlayerLoc().getX() == 0 && getPlayerLoc().getY() == 0) {
			return false;
		} else {
			return true;
		}
	}

	public RSPlayer getPlayer() {
		String name = interfaces.get(730).getComponent(18).getText();
		RSPlayer[] array = players.getAll();
		int i = 0;
		while (i < array.length) {
			if (array[i] != null) {
				try {
					if ((array[i].getName()).equals(name)) {
						return array[i];
					}
				} catch (NullPointerException ignored) {
				}
				i++;
			}
		}
		return null;
	}

	public RSPlayer getPlayerThrow() throws NullPointerException {
		String name = interfaces.get(730).getComponent(18).getText();
		RSPlayer[] array = players.getAll();
		int i = 0;
		while (i < array.length) {
			if (array[i] != null) {
				try {
					if ((array[i].getName()).equals(name)) {
						return array[i];
					}
				} catch (NullPointerException ignored) {
				}
				i++;
			}
		}
		throw new NullPointerException();
	}

	public String playerName() {
		return interfaces.get(730).getComponent(18).getText();
	}

	public boolean inLobby() {
		int x = getMyPlayer().getLocation().getX();
		int y = getMyPlayer().getLocation().getY();
		if (x <= 1725 && x >= 1670) {
			if (y <= 5615 && y >= 5585) {
				return true;
			}
		}

		return false;
	}

	public boolean inGame() {
		int x = getMyPlayer().getLocation().getX();
		int y = getMyPlayer().getLocation().getY();
		if (x <= 1710 && x >= 1615) {
			if (y <= 5735 && y >= 5650) {
				return true;
			}
		}

		return false;
	}

	public boolean inWaitingRoom() {
		int x = getMyPlayer().getLocation().getX();
		int y = getMyPlayer().getLocation().getY();
		if (x <= 1660 && x >= 1610) {
			if (y <= 5620 && y >= 5600) {
				return true;
			}
		}

		return false;
	}

	public int getCharges() {
		String chargesTxt = interfaces.get(730).getComponent(17).getText();
		int chargesIndex = chargesTxt.indexOf(": ");
		String finalString = chargesTxt.substring(chargesIndex + 2);
		int finalCharges = Integer.parseInt(finalString);
		return finalCharges;
	}

	public void setNextHitSwitch() {

		int hitSwitchMinHitsBIG = hitSwitchMinHits * 1000;
		int hitSwitchMaxHitsBIG = hitSwitchMaxHits * 1000;
		int nextBig = random(hitSwitchMinHitsBIG, hitSwitchMaxHitsBIG);
		double nextDown = nextBig / 1000.00;
		int next = (int) Math.round(nextDown);

		nextHitSwitch = next;
	}

	public void setRandomStyle() {
		status = "Changing Style...";
		int numActive = 0;
		if (meleeActive) {
			numActive++;
		}
		if (magicActive) {
			numActive++;
		}
		if (rangeActive) {
			numActive++;
		}

		if (numActive == 3) {
			int rand = random(1, 2);
			switch (rand) {
			case 1: {
				if (currentStyle.equals("Melee")) {
					currentStyle = "Magic";
				} else if (currentStyle.equals("Magic")) {
					currentStyle = "Range";
				} else if (currentStyle.equals("Range")) {
					currentStyle = "Melee";
				}
				break;
			}
			case 2: {
				if (currentStyle.equals("Melee")) {
					currentStyle = "Range";
				} else if (currentStyle.equals("Magic")) {
					currentStyle = "Melee";
				} else if (currentStyle.equals("Range")) {
					currentStyle = "Magic";
				}
				break;
			}
			}
		} else if (numActive == 2) {
			if (currentStyle.equals("Melee")) {
				if (magicActive) {
					currentStyle = "Magic";
				} else if (rangeActive) {
					currentStyle = "Range";
				}
			} else if (currentStyle.equals("Magic")) {
				if (rangeActive) {
					currentStyle = "Range";
				} else if (meleeActive) {
					currentStyle = "Melee";
				}
			} else if (currentStyle.equals("Range")) {
				if (meleeActive) {
					currentStyle = "Melee";
				} else if (magicActive) {
					currentStyle = "Magic";
				}
			}
		} else if (numActive == 1) {
			if (dmode) {
				log("Only one Combat Style active. Cannot switch.");
			}
		} else if (numActive == 0) {
			if (dmode) {
				log("ERROR - NO COMBAT STYLES");
			}
		}
		if (numActive > 1) {
			setupStyleEquipment();
			setupStyle();
		}
	}

	public boolean isFOGItem(int id) {
		boolean isIt = false;
		int i = 0;
		while (!isIt && i < FOGItems.length) {
			int i2 = 0;
			while (!isIt && i2 < 7) {
				if (FOGItems[i].ID - i2 == id) {
					isIt = true;
					dlog("Found FOG Item: " + id + ", FOGItems[" + i + "] - "
							+ i2);
				}
				i2++;
			}
			i++;
		}
		return isIt;
	}

	public boolean isFOGItem2(int id) {
		boolean isIt = false;
		int i = 0;
		while (!isIt && i < FOGItems.length) {
			int i2 = 0;
			while (!isIt && i2 < 7) {
				if (FOGItems[i].ID - i2 == id) {
					isIt = true;
				}
				i2++;
			}
			i++;
		}
		return isIt;
	}

	public boolean isFOGItemInInv(int id) {
		int idOn = id;
		int i = 0;
		while (i < FOGItems.length) {
			while (idOn <= FOGItems[i].ID && idOn >= (FOGItems[i].ID - 5)) {
				if (inventory.getCount(false, idOn) > 0) {
					return true;
				} else {
					idOn++;
				}
			}
			i++;
		}
		return false;
	}

	public int wieldFOGItem(int id) {
		int toReturn = -1;
		boolean done = false;
		int idOn = id;
		int i = 0;
		while (!done && i < FOGItems.length) {
			while (!done && idOn <= FOGItems[i].ID
					&& idOn >= (FOGItems[i].ID - 5)) {
				if (inventory.getCount(false, idOn) > 0) {
					if (doActionExtreme(idOn, "Wear")) {
						done = true;
						toReturn = idOn;
						dlog("Wielded (\"Wear\") FOG Item: " + idOn
								+ ", From old id: " + id + ", FOGItems[" + i
								+ "] - ");
					} else {
						if (doActionExtreme(idOn, "Wield")) {
							done = true;
							toReturn = idOn;
							dlog("Wielded (\"Wield\") FOG Item: " + idOn
									+ ", From old id: " + id + ", FOGItems["
									+ i + "] - ");
						} else {
							idOn++;
						}
					}
				} else {
					idOn++;
				}
			}
			i++;
		}
		return toReturn;
	}

	public boolean stringArrayContains(String[] array, String term) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].contains(term)) {
				return true;
			}
		}
		return false;
	}

	public boolean doActionExtreme(int itemID, String action) {
		try {
			RSItem item = inventory.getItem(itemID);
			if (item == null)
				return false;
			int tries = 0;
			while (!item.doAction(action) && tries < 10) {
				tries++;
				if (!menu.contains(action)) {
					if (!hoverItemCentre(item))
						moveMouseUp();
				}
			}
			if (tries < 10)
				return true;
		} catch (NullPointerException e) {
			if (dmode)
				e.printStackTrace();
		}
		return false;
	}

	public void moveMouseUp() {
		int x = (int) mouse.getLocation().getX();
		int y = (int) mouse.getLocation().getY();
		mouse.move(x + random(-10, 10), y - random(20, 80));
	}

	public void moveMouseDown() {
		int x = (int) mouse.getLocation().getX();
		int y = (int) mouse.getLocation().getY();
		mouse.move(x + random(-10, 10), y + random(20, 80));
	}

	public boolean hoverItemCentre(RSItem item) {
		if (item == null || item.getComponent() == null
				|| item.getComponent().getArea() == null
				|| item.getComponent().getArea().x == -1)
			return false;
		Rectangle r1 = item.getComponent().getArea();
		int width = r1.width / 2;
		int height = r1.height / 2;
		int x = r1.x + (width / 2);
		int y = r1.y + (height / 2);
		new Rectangle(x, y, width, height);
		int mx = random(x, x + width);
		int my = random(y, y + height);
		mouse.move(mx, my);
		return true;
	}

	public void setupStyleEquipment() {
		try {
			if (equipmentItemsInInventory()) {
				if (currentStyle.equals("Melee")) {
					int i = 0;
					while (i < meleeEquipment.length) {
						if (inventory.getCount(false, meleeEquipment[i]) > 0) {
							if (!doActionExtreme(meleeEquipment[i], "Wear")) {
								if (!doActionExtreme(meleeEquipment[i], "Wield")) {
									dlog("Unwearable item: "
											+ meleeEquipment[i]);
								}
							}
						} else if (isFOGItem(meleeEquipment[i])
								&& isFOGItemInInv(meleeEquipment[i])) {
							int newID = wieldFOGItem(meleeEquipment[i]);
							if (newID != -1) {
								meleeEquipment[i] = newID;
							} else {
								dlog("Unwearable FOG item: "
										+ meleeEquipment[i]);
							}
						}
						i++;
					}
				} else if (currentStyle.equals("Magic")) {
					int i = 0;
					while (i < magicEquipment.length) {
						if (inventory.getCount(false, magicEquipment[i]) > 0) {
							if (!doActionExtreme(magicEquipment[i], "Wear")) {
								if (!doActionExtreme(magicEquipment[i], "Wield")) {
									dlog("Unwearable item: "
											+ magicEquipment[i]);
								}
							}
						} else if (isFOGItem(magicEquipment[i])
								&& isFOGItemInInv(magicEquipment[i])) {
							int newID = wieldFOGItem(magicEquipment[i]);
							if (newID != -1) {
								magicEquipment[i] = newID;
							} else {
								dlog("Unwearable FOG item: "
										+ magicEquipment[i]);
							}
						}
						i++;
					}
				} else if (currentStyle.equals("Range")) {
					int i = 0;
					while (i < rangeEquipment.length) {
						if (inventory.getCount(false, rangeEquipment[i]) > 0) {
							if (!doActionExtreme(rangeEquipment[i], "Wear")) {
								if (!doActionExtreme(rangeEquipment[i], "Wield")) {
									dlog("Unwearable item: "
											+ rangeEquipment[i]);
								}
							}
						} else if (isFOGItem(rangeEquipment[i])
								&& isFOGItemInInv(rangeEquipment[i])) {
							int newID = wieldFOGItem(rangeEquipment[i]);
							if (newID != -1) {
								rangeEquipment[i] = newID;
							} else {
								dlog("Unwearable FOG item: "
										+ rangeEquipment[i]);
							}
						}
						i++;
					}
				}
			}
		} catch (NullPointerException ignored) {
		}
	}

	public void setupStyle() {
		if (currentStyle.equals("Melee")) {
			if (meleeAttackStyle.equals("Attack EXP")) {
				combat.setFightMode(0);
			} else if (meleeAttackStyle.equals("Strength EXP")) {
				combat.setFightMode(1);
			} else if (meleeAttackStyle.equals("Shared EXP")) {
				combat.setFightMode(2);
			} else if (meleeAttackStyle.equals("Defence EXP")) {
				combat.setFightMode(3);
			}

		} else if (currentStyle.equals("Range")) {
			if (rangeAttackStyle.equals("Accurate")) {
				combat.setFightMode(0);
			} else if (rangeAttackStyle.equals("Rapid")) {
				combat.setFightMode(1);
			} else if (rangeAttackStyle.equals("Long range")) {
				combat.setFightMode(2);
			}
		}
	}

	public void setupStyleDefence() {
		try {
			if (equipmentItemsInInventoryDefence()) {
				if (currentStyleDefence.equals("Melee")) {
					int i = 0;
					while (i < meleeEquipmentDefence.length
							&& getMyPlayer().getHPPercent() > 50 && inGame()) {
						if (inventory.getCount(false, meleeEquipmentDefence[i]) > 0) {
							if (!doActionExtreme(meleeEquipmentDefence[i],
									"Wear")) {
								if (!doActionExtreme(meleeEquipmentDefence[i],
										"Wield")) {
									dlog("Unwearable item: "
											+ meleeEquipmentDefence[i]);
								}
							}
						} else if (isFOGItem(meleeEquipmentDefence[i])
								&& isFOGItemInInv(meleeEquipmentDefence[i])) {
							int newID = wieldFOGItem(meleeEquipmentDefence[i]);
							if (newID != -1) {
								meleeEquipmentDefence[i] = newID;
							} else {
								dlog("Unwearable FOG item: "
										+ meleeEquipmentDefence[i]);
							}
						}
						i++;
					}
				} else if (currentStyleDefence.equals("Magic")) {
					int i = 0;
					while (i < magicEquipmentDefence.length
							&& getMyPlayer().getHPPercent() > 50 && inGame()) {
						if (inventory.getCount(false, magicEquipmentDefence[i]) > 0) {
							if (!doActionExtreme(magicEquipmentDefence[i],
									"Wear")) {
								if (!doActionExtreme(magicEquipmentDefence[i],
										"Wield")) {
									dlog("Unwearable item: "
											+ magicEquipmentDefence[i]);
								}
							}
						} else if (isFOGItem(magicEquipmentDefence[i])
								&& isFOGItemInInv(magicEquipmentDefence[i])) {
							int newID = wieldFOGItem(magicEquipmentDefence[i]);
							if (newID != -1) {
								magicEquipmentDefence[i] = newID;
							} else {
								dlog("Unwearable FOG item: "
										+ magicEquipmentDefence[i]);
							}
						}
						i++;
					}
				} else if (currentStyleDefence.equals("Range")) {
					int i = 0;
					while (i < rangeEquipmentDefence.length
							&& getMyPlayer().getHPPercent() > 50 && inGame()) {
						if (inventory.getCount(false, rangeEquipmentDefence[i]) > 0) {
							if (!doActionExtreme(rangeEquipmentDefence[i],
									"Wear")) {
								if (!doActionExtreme(rangeEquipmentDefence[i],
										"Wield")) {
									dlog("Unwearable item: "
											+ rangeEquipmentDefence[i]);
								}
							}
						} else if (isFOGItem(rangeEquipmentDefence[i])
								&& isFOGItemInInv(rangeEquipmentDefence[i])) {
							int newID = wieldFOGItem(rangeEquipmentDefence[i]);
							if (newID != -1) {
								rangeEquipmentDefence[i] = newID;
							} else {
								dlog("Unwearable FOG item: "
										+ rangeEquipmentDefence[i]);
							}
						}
						i++;
					}
				}
			}
		} catch (NullPointerException ignored) {
		}
	}

	public boolean equipmentItemsInInventory() {
		boolean isThere = false;
		if (currentStyle.equals("Melee")) {
			if (inventory.getCount(false, meleeEquipment) > 0) {
				isThere = true;
			} else {
				int i = 0;
				while (!isThere && i < meleeEquipment.length) {
					if (isFOGItem2(meleeEquipment[i])
							&& isFOGItemInInv(meleeEquipment[i])) {
						isThere = true;
					}
					i++;
				}
			}
		} else if (currentStyle.equals("Magic")) {
			if (inventory.getCount(false, magicEquipment) > 0) {
				isThere = true;
			} else {
				int i = 0;
				while (!isThere && i < magicEquipment.length) {
					if (isFOGItem2(magicEquipment[i])
							&& isFOGItemInInv(magicEquipment[i])) {
						isThere = true;
					}
					i++;
				}
			}
		} else if (currentStyle.equals("Range")) {
			if (inventory.getCount(false, rangeEquipment) > 0) {
				isThere = true;
			} else {
				int i = 0;
				while (!isThere && i < rangeEquipment.length) {
					if (isFOGItem2(rangeEquipment[i])
							&& isFOGItemInInv(rangeEquipment[i])) {
						isThere = true;
					}
					i++;
				}
			}
		}
		return isThere;
	}

	public boolean equipmentItemsInInventoryDefence() {
		boolean isThere = false;
		if (currentStyleDefence.equals("Melee")) {
			if (inventory.getCount(false, meleeEquipmentDefence) > 0) {
				isThere = true;
			} else {
				int i = 0;
				while (!isThere && i < meleeEquipmentDefence.length) {
					if (isFOGItem2(meleeEquipmentDefence[i])
							&& isFOGItemInInv(meleeEquipmentDefence[i])) {
						isThere = true;
					}
					i++;
				}
			}
		} else if (currentStyleDefence.equals("Magic")) {
			if (inventory.getCount(false, magicEquipmentDefence) > 0) {
				isThere = true;
			} else {
				int i = 0;
				while (!isThere && i < magicEquipmentDefence.length) {
					if (isFOGItem2(magicEquipmentDefence[i])
							&& isFOGItemInInv(magicEquipmentDefence[i])) {
						isThere = true;
					}
					i++;
				}
			}
		} else if (currentStyleDefence.equals("Range")) {
			if (inventory.getCount(false, rangeEquipmentDefence) > 0) {
				isThere = true;
			} else {
				int i = 0;
				while (!isThere && i < rangeEquipmentDefence.length) {
					if (isFOGItem2(rangeEquipmentDefence[i])
							&& isFOGItemInInv(rangeEquipmentDefence[i])) {
						isThere = true;
					}
					i++;
				}
			}
		}
		return isThere;
	}

	public int itemsInEquipment() {
		int itemsFound = 0;
		int i = 0;
		while (i < 11) {
			try {
				RSComponent comp = equipment.getInterface().getComponent(
						eComps[i]);
				String[] compActions = comp.getActions();
				if (compActions[0].contains("Remove")) {
					itemsFound++;
				}
			} catch (NullPointerException ignored) {
			}
			i++;
		}
		return itemsFound;
	}

	public void unequipAll() {
		while (game.getCurrentTab() != Game.TAB_EQUIPMENT) {
			game.openTab(Game.TAB_EQUIPMENT);
			sleep(101);
		}
		while (itemsInEquipment() > 0) {
			RSItem[] eItems = equipment.getItems();
			String[] order = { "0", "1", "2", "3", "4", "5", "6", "7", "8",
					"9", "10" };
			List<String> list = Arrays.asList(order);
			Collections.shuffle(list);
			Object[] noOrderObjects = new Object[11];
			String[] noOrderStrings = new String[11];
			noOrderObjects = list.toArray();
			for (int i = 0; i < 11; i++) {
				noOrderStrings[i] = noOrderObjects[i].toString();
			}
			int[] noOrder = new int[11];
			for (int i = 0; i < 11; i++) {
				noOrder[i] = Integer.parseInt(noOrderStrings[i]);
			}
			for (int i = 0; i < 11; i++) {
				try {
					int nextItem = noOrder[i];
					if (equipment.getInterface().getComponent(eComps[nextItem])
							.getActions()[0].contains("Remove")) {
						if (eItems[nextItem].doAction("Remove")) {
							if (itemsInEquipment() < 3) {
								sleep(700);
							}
							sleep(random(50, 150));
						}
					}
				} catch (NullPointerException ignored) {
				}
			}
		}
		game.openTab(Game.TAB_INVENTORY);
		sleep(200);
	}

	public void initialise() {

		if (game.isLoggedIn()) {
			// Player Stats Exp
			startAttackExp = skills.getCurrentExp(Skills.ATTACK);
			startStrengthExp = skills.getCurrentExp(Skills.STRENGTH);
			startDefenceExp = skills.getCurrentExp(Skills.DEFENSE);
			startMagicExp = skills.getCurrentExp(Skills.MAGIC);
			startRangeExp = skills.getCurrentExp(Skills.RANGE);
			startConstitutionExp = skills.getCurrentExp(Skills.CONSTITUTION);
			// Prayer Stats Levels
			startAttackLevel = skills.getRealLevel(Skills.ATTACK);
			startStrengthLevel = skills.getRealLevel(Skills.STRENGTH);
			startDefenceLevel = skills.getRealLevel(Skills.DEFENSE);
			startMagicLevel = skills.getRealLevel(Skills.MAGIC);
			startRangeLevel = skills.getRealLevel(Skills.RANGE);
			startConstitutionLevel = skills.getRealLevel(Skills.CONSTITUTION);
		}

		if (!dieScript) {

			status = "Checking Items...";

			clearItemListArrays();
			startStylesP[0] = "Melee";
			startStylesP[1] = "Magic";
			startStylesP[2] = "NULL";

			clearItemListArraysDefence();

			/*
			 * status = "Unequipping All Items..."; unequipAll(); status =
			 * "Opening GUI...";
			 */

			game.openTab(Game.TAB_INVENTORY);
			sleep(50);
			inventoryItemsNumP = inventory.getCount(false);
			int i = 0;
			while (i < 28) {
				inventoryItemsIDsX[i] = inventory.getItemAt(i).getID();
				i++;
			}
			int i2 = 0;
			int i2Try = 0;
			while (i2 < inventoryItemsNumP) {
				if (inventoryItemsIDsX[i2Try] == -1) {
					i2Try++;
				} else {
					inventoryItemsIDsP[i2] = inventoryItemsIDsX[i2Try];
					int nameStart = inventory.getItemAt(i2Try).getName()
							.indexOf("<col=ff9040>") + 12;
					inventoryItemsNamesP[i2] = inventory.getItemAt(i2Try)
							.getName().substring(nameStart);
					i2++;
					i2Try++;
				}
			}

			int iToAdd = 0;
			while (iToAdd < inventoryItemsNumP) {
				inventoryItemsIDs[iToAdd] = inventoryItemsIDsP[iToAdd];
				inventoryItemsNames[iToAdd] = inventoryItemsNamesP[iToAdd];
				inventoryItemsNum++;
				iToAdd++;
			}

			// Equipment
			game.openTab(Game.TAB_EQUIPMENT);
			sleep(50);
			equipmentItemsNum = equipment.getCount();
			RSItem[] equipmentItems = equipment.getItems();
			int iE = 0;
			while (iE < 11) {
				equipmentItemsIDsX[iE] = equipmentItems[iE].getID();
				iE++;
			}
			int i2E = 0;
			int i2TryE = 0;
			while (i2E < equipmentItemsNum) {
				if (equipmentItemsIDsX[i2TryE] == -1) {
					i2TryE++;
				} else {
					equipmentItemsIDs[i2E] = equipmentItemsIDsX[i2TryE];
					int nameStart = equipmentItems[i2TryE].getName().indexOf(
							"<col=ff9040>") + 12;
					equipmentItemsNames[i2E] = equipmentItems[i2TryE].getName()
							.substring(nameStart);
					i2E++;
					i2TryE++;
				}
			}

			int iToAddE = 0;
			while (iToAddE < equipmentItemsNum) {
				inventoryItemsIDs[iToAdd] = equipmentItemsIDs[iToAddE];
				inventoryItemsNames[iToAdd] = equipmentItemsNames[iToAddE];
				inventoryItemsNum++;
				iToAdd++;
				iToAddE++;
			}
			if (magic.getInterface() == null || !magic.getInterface().isValid()) {
				game.openTab(Game.TAB_MAGIC);
			}
			game.openTab(Game.TAB_INVENTORY);
			status = "Opening GUI...";
			dieScript = true;
			for (int n = 0; n < v.length; n++) {
				v[n] = (17 * n);
			}

			gui = new FOGRunnerGUI();

			status = "Loading FOG Reward Prices...";
			for (int m = 0; m < FOGItems.length; m++) {
				if (gui.isVisible()) {
					FOGItems[m].setPrice();
					if (gui.isActive() || gui.isVisible()) {
						gui.itemPrices[m].setText("" + FOGItems[m].price);
						gui.itemRatios[m].setText("" + FOGItems[m].getRatio());
					}
				} else {
					break;
				}
			}
			if (gui.isVisible()) {
				int[] ratiosP2P = new int[FOGItemsP2P.length];
				for (int j = 0; j < ratiosP2P.length; j++) {
					ratiosP2P[j] = FOGItemsP2P[j].getRatio();
				}
				int[] ratiosF2P = new int[FOGItemsF2P.length];
				for (int l = 0; l < ratiosF2P.length; l++) {
					ratiosF2P[l] = FOGItemsF2P[l].getRatio();
				}

				for (int k = 0; k < FOGItems.length; k++) {
					if (FOGItems[k].members) {
						if (FOGItems[k].getRatio() >= maxInArray(ratiosP2P)) {
							if (gui.isActive()) {
								gui.itemRatios[k].setText(""
										+ FOGItems[k].getRatio()
										+ " - P2P Best");
								gui.itemRatios[k].setFont(new Font("SansSerif",
										Font.BOLD, 12));
								gui.itemRatios[k].setBounds(new Rectangle(
										new Point(435, v[k] + 1),
										gui.itemRatios[k].getPreferredSize()));
							}
							f2pItem = FOGItems[k];
						}
					} else {
						if (FOGItems[k].getRatio() >= maxInArray(ratiosF2P)) {
							if (gui.isActive()) {
								gui.itemRatios[k].setText(""
										+ FOGItems[k].getRatio()
										+ " - F2P Best");
								gui.itemRatios[k].setFont(new Font("SansSerif",
										Font.BOLD, 12));
								gui.itemRatios[k].setBounds(new Rectangle(
										new Point(435, v[k] + 1),
										gui.itemRatios[k].getPreferredSize()));
							}
							p2pItem = FOGItems[k];
						}
					}
				}
			}

			while (gui.isActive() || gui.isVisible()) {
				status = "Waiting for GUI...";
				sleep(50);
			}
		}

		if (!dieScript) {

			startTime = System.currentTimeMillis();
			seconds = 0;
			minutes = 0;
			hours = 0;
			mouse.setSpeed(mouseSpeed);

			try {
				startTokens = inventory.getCount(true, tokenID);
			} catch (NullPointerException ignored) {
			}

			nextPlace = 0;
			// Player Stats Exp
			startAttackExp = skills.getCurrentExp(Skills.ATTACK);
			startStrengthExp = skills.getCurrentExp(Skills.STRENGTH);
			startDefenceExp = skills.getCurrentExp(Skills.DEFENSE);
			startMagicExp = skills.getCurrentExp(Skills.MAGIC);
			startRangeExp = skills.getCurrentExp(Skills.RANGE);
			startConstitutionExp = skills.getCurrentExp(Skills.CONSTITUTION);
			// Prayer Stats Levels
			startAttackLevel = skills.getRealLevel(Skills.ATTACK);
			startStrengthLevel = skills.getRealLevel(Skills.STRENGTH);
			startDefenceLevel = skills.getRealLevel(Skills.DEFENSE);
			startMagicLevel = skills.getRealLevel(Skills.MAGIC);
			startRangeLevel = skills.getRealLevel(Skills.RANGE);
			startConstitutionLevel = skills.getRealLevel(Skills.CONSTITUTION);

			prayerlvl = skills.getRealLevel(Skills.PRAYER);

			if (teleorbLong.equals("When being Hunted")) {
				teleorb = "hunted";
			} else if (teleorbLong.equals("When Hunting")) {
				teleorb = "hunting";
			} else if (teleorbLong.equals("Random (Hunting/Hunted)")) {
				teleorb = "random";
			} else if (teleorbLong.equals("Never")) {
				teleorb = "never";
			} else {
				if (dmode) {
					log("Tele-Orb Mode Error");
				}
			}

			currentStyle = firstStyle;

			log(">>> FOGRUNNER STARTED <<<");
			status = "Equipping Items...";
			setupStyleEquipment();
			setupStyle();
		}

		initialised = true;
	}

	public void initialiseImages() {
		status = "Loading Images...";
		try {
			banner = ImageIO.read(bannerFile);
			success = true;
		} catch (FileNotFoundException e) {
			loadBannerFromFile = false;
		} catch (IOException e) {
			loadBannerFromFile = false;
		}
		if (!loadBannerFromFile) {
			try {
				final URL bannerURL = new URL(
						"http://i55.tinypic.com/288144x.png");
				banner = ImageIO.read(bannerURL);
				success = true;
			} catch (MalformedURLException e) {
				log("Unable to buffer banner.");
			} catch (IOException e) {
				log("Unable to open banner image.");
			}
			if (success) {
				try {
					ImageIO.write(banner, "png", bannerFile);
				} catch (IOException e) {
					log("Save Banner Image Failed");
				}
			}
		}
		try {
			final URL cursorURL = new URL("http://i54.tinypic.com/2unwz85.png");
			final URL clickedURL = new URL("http://i51.tinypic.com/24y1lxe.png");
			final URL upArrowURL = new URL("http://i51.tinypic.com/16ge3no.png");
			final URL downArrowURL = new URL(
					"http://i53.tinypic.com/2j49b0k.png");
			normal = ImageIO.read(cursorURL);
			clicked = ImageIO.read(clickedURL);
			upArrow = ImageIO.read(upArrowURL);
			downArrow = ImageIO.read(downArrowURL);
		} catch (MalformedURLException e) {
			log("Unable to buffer cursor.");
		} catch (IOException e) {
			log("Unable to open cursor image.");
		}
	}

	public double getVersion() {
		return 1.5;
	}

	@Override
	public final boolean onStart() {
		if (!game.isLoggedIn()) {
			env.enableRandom("Login");
			loginWait = true;
			status = "Logging in...";
			log(">>> GUI WILL OPEN AFTER LOGIN <<<");
		}

		initialised = false;
		return true;
	}

	@Override
	public void onRepaint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		// Round 2 detection
		if (inGame()) {
			if (prevHg != -1 || prevHd != -1) {
				if (interfaces.get(730).getComponent(26).getBoundsArrayIndex() != -1
						&& // hunting component index = 26
						interfaces.get(730).getComponent(27)
								.getBoundsArrayIndex() != -1) { // hunted
																// component
																// index = 27
					if (round != 2) {
						canDrawTheirTile = false;
						antiBanTime = false;
						timerStarted = false;
						searchTileOn = 0;
						searching = false;
						stoneHovered = false;
						lastPrayerSet = 0;
						lastAttackPrayerSet = 0;
						lastSkinPrayerSet = -1;
						lastKnownLoc = new RSTile(-1, -1);
						timerToUse = 1;
						meleeTimerStarted = false;
						lastKnownHealth = 100;
						round = 2;
					}
				}
			}
		}

		// Paint Variables
		runTime = System.currentTimeMillis() - startTime;
		seconds = runTime / 1000;
		if (seconds >= 60) {
			minutes = seconds / 60;
			seconds -= (minutes * 60);
		}
		if (minutes >= 60) {
			hours = minutes / 60;
			minutes -= (hours * 60);
		}

		tokensGained = (currentTokens + bankedTokens) - startTokens;
		tokensPerHour = (int) ((3600000.0 / runTime) * tokensGained);
		gamesPerHour = (int) ((3600000.0 / runTime) * gamesPlayed);
		gamesWonPerHour = (int) ((3600000.0 / runTime) * gamesWon);
		gamesLostPerHour = (int) ((3600000.0 / runTime) * gamesLost);
		int ratingPerHour = (int) ((3600000.0 / runTime) * gainedRating);

		// Tile Paint

		// Your Player Tile
		try {
			drawTile(g, getMyPlayer().getLocation(), new Color(0, 255, 0, 80),
					"You", new Color(0, 255, 0, 255));
		} catch (NullPointerException ignored) {
		}

		// Opponent Tile
		if (canDrawTheirTile) {
			try {
				if (getPlayerLocThrow().getX() != 0) {
					lastKnownLoc = getPlayerLocThrow();
					drawTile(g, getPlayerLocThrow(), new Color(255, 0, 0, 80),
							"Opponent", new Color(255, 0, 0, 255));
				}
			} catch (NullPointerException e) {
				canDrawTheirTile = false;
			}
		}

		// Mouse Paint
		if (clicked != null) {
			final int mx = (int) mouse.getLocation().getX();
			final int my = (int) mouse.getLocation().getY();
			final int mpx = (int) mouse.getPressLocation().getX();
			final int mpy = (int) mouse.getPressLocation().getY();
			final long mpt = System.currentTimeMillis() - mouse.getPressTime();
			if (mpt < 1000) {
				g.setColor(Color.RED);
				g.drawLine(mpx - 6, mpy, mpx - 2, mpy);
				g.drawLine(mpx - 5, mpy - 1, mpx - 3, mpy - 1);
				g.drawLine(mpx - 5, mpy + 1, mpx - 3, mpy + 1);
				g.drawLine(mpx + 6, mpy, mpx + 2, mpy);
				g.drawLine(mpx + 5, mpy - 1, mpx + 3, mpy - 1);
				g.drawLine(mpx + 5, mpy + 1, mpx + 3, mpy + 1);
				g.drawLine(mpx, mpy - 6, mpx, mpy - 2);
				g.drawLine(mpx - 1, mpy - 5, mpx - 1, mpy - 3);
				g.drawLine(mpx + 1, mpy - 5, mpx + 1, mpy - 3);
				g.drawLine(mpx, mpy + 6, mpx, mpy + 2);
				g.drawLine(mpx - 1, mpy + 5, mpx - 1, mpy + 3);
				g.drawLine(mpx + 1, mpy + 5, mpx + 1, mpy + 3);
			}
			if (mouse.getPressTime() == -1 || mpt >= 300)
				g.drawImage(normal, mx - 10, my - 6, null);
			if (mpt < 300)
				g.drawImage(clicked, mx - 10, my - 6, null);
		}

		Color statusWhite = new Color(255, 255, 255, (int) (255 * statusAlpha));

		// Status Paint
		if (statusPaint != 0) {
			if (statusPaint == 1) {
				if (statusAlpha < 1.0) {
					statusAlpha += 0.2;
				} else {
					statusPaint = 2;
				}
			} else if (statusPaint == 2) {
				statusAlpha = 1.0;
			} else if (statusPaint == 3) {
				if (statusAlpha > 0.2) {
					statusAlpha -= 0.2;
				} else {
					statusAlpha = 0.0;
					statusPaint = 0;
				}
			}

			statusWhite = new Color(255, 255, 255, (int) (255 * statusAlpha));

			g.setColor(new Color(0, 0, 70, (int) (150 * statusAlpha)));
			g.fillRoundRect(1, 301, 514, 37, cornerNum, cornerNum);
			g.setColor(statusWhite);
			g.drawRoundRect(1, 301, 514, 37, cornerNum, cornerNum);
			g.setFont(new Font("Arial", Font.BOLD, 14));
			g.drawString(properties.name() + " v" + getVersion(), 6, 316);
			g.drawString("Status: ", 150, 316);
			g.setFont(new Font("Arial", Font.PLAIN, 14));
			if (!overallStatus.equals("NULL")) {
				g.drawString(overallStatus + " | " + status, 205, 316);
			} else {
				g.drawString(status, 205, 316);
			}
			if (dmode) {
				String hgS = Integer.toString(interfaces.get(730)
						.getComponent(26).getBoundsArrayIndex());
				String hdS = Integer.toString(interfaces.get(730)
						.getComponent(27).getBoundsArrayIndex());
				int magicIndex = -1;
				if (magic.getInterface() != null
						&& magic.getInterface().isValid()) {
					magicIndex = magic.getInterface().getIndex();
				}
				g.drawString("Magic ID: " + magicIndex + "  B.A.Is: Hunting: "
						+ hgS + " Hunted: " + hdS + "   User Mouse: "
						+ userMouseX + ", " + userMouseY, 2, 299);
			}
			if (inGame()) {
				if (canDrawInfo) {
					try {
						if (dmode) {
							String thing = Integer.toString(getPlayerThrow()
									.getAnimation());
							g.drawString("OP Anim: " + thing, 2, 287);
						}
						g.drawString("Opponent LvL: "
								+ getPlayerThrow().getCombatLevel(), 6, 331);
						drawHealthBar(g, g2d, 150, 332);

						// Setting the last known hit on the opponent
						if (hitSwitch) {
							if (inGame()) {
								if (round == 1) {
									if (!hunted) {
										if (attacking()) {
											if (getPlayerThrow().getHPPercent() < lastKnownHealth) {
												lastKnownHealth = getPlayerThrow()
														.getHPPercent();
												hits++;
											} else if (getPlayerThrow()
													.getHPPercent() > lastKnownHealth) {
												lastKnownHealth = getPlayerThrow()
														.getHPPercent();
											}
										}
									}
								}
								if (round == 2) {
									if (!hunted2) {
										if (attacking()) {
											if (getPlayerThrow().getHPPercent() < lastKnownHealth) {
												lastKnownHealth = getPlayerThrow()
														.getHPPercent();
												hits++;
											} else if (getPlayerThrow()
													.getHPPercent() > lastKnownHealth) {
												lastKnownHealth = getPlayerThrow()
														.getHPPercent();
											}
										}
									}
								}
							}
						}
					} catch (NullPointerException e) {
						canDrawInfo = false;
						if (dmode) {
							log("Null Error: " + e);
							e.printStackTrace();
						}
					}
				}
			}
		}

		Color buttonNormal = new Color(30, 30, 100, 255);
		Color buttonOver = new Color(60, 60, 130, 255);
		Color buttonPressed = new Color(0, 0, 50, 255);
		Color buttonNormalOn = new Color(20, 75, 20, 255);
		Color buttonOverOn = new Color(40, 95, 40, 255);
		Color buttonPressedOn = new Color(0, 60, 0, 255);

		Color buttonNormalS = new Color(30, 30, 100, (int) (255 * statsAlpha));
		Color buttonOverS = new Color(60, 60, 130, (int) (255 * statsAlpha));
		Color buttonPressedS = new Color(0, 0, 50, (int) (255 * statsAlpha));
		Color buttonNormalOnS = new Color(20, 75, 20, (int) (255 * statsAlpha));
		Color buttonOverOnS = new Color(40, 95, 40, (int) (255 * statsAlpha));
		Color buttonPressedOnS = new Color(0, 60, 0, (int) (255 * statsAlpha));

		Color statsWhite = new Color(255, 255, 255, (int) (255 * statsAlpha));

		// Stats Paint
		int[] coords = new int[] { 199, 214, 229, 244, 259, 274, 289, 304, 319,
				334, 349, 364, 379, 394, 409, 424, 439, 454, 469, 484 };
		if (statsPaint != 0) {
			if (statsPaint == 1) {
				if (statsAlpha < 1.0) {
					statsAlpha += 0.2;
				} else {
					statsPaint = 2;
				}
			} else if (statsPaint == 2) {
				statsAlpha = 1.0;
			} else if (statsPaint == 3) {
				if (statsAlpha > 0.2) {
					statsAlpha -= 0.2;
				} else {
					statsAlpha = 0.0;
					statsPaint = 0;
				}
			}

			statsWhite = new Color(255, 255, 255, (int) (255 * statsAlpha));

			if (moreStats == 0) {
				g.setColor(new Color(0, 0, 70, (int) (110 * statsAlpha)));
				g.fillRoundRect(555, 327, 175, 134, cornerNum, cornerNum);
				g.setColor(new Color(255, 255, 255, (int) (255 * statsAlpha)));
				g.drawRoundRect(555, 327, 175, 134, cornerNum, cornerNum);
				g.setFont(new Font("Arial", Font.BOLD, 14));
				g.drawString(properties.name() + " v" + getVersion(), 561,
						coords[9] + 8);
				g.setFont(new Font("Arial", Font.PLAIN, 12));
				g.drawString("Run Time: " + hours + ":" + minutes + ":"
						+ seconds, 561, coords[10] + 8);
				g.setColor(new Color(200, 200, 200, (int) (255 * statsAlpha)));
				g.drawString("-----------------------------------------", 561,
						coords[11] + 3);
				g.setColor(statsWhite);
				g.drawString("Tokens Gained: " + tokensGained, 561, coords[12]);
				g.drawString("Tokens/Hour: " + tokensPerHour, 561, coords[13]);
				g.drawString("Games Played: " + gamesPlayed, 561, coords[14]);
				g.drawString("Won: " + gamesWon + "  Lost: " + gamesLost
						+ "  Ratio: " + getRatio(), 561, coords[15]);
			} else {
				switch (moreStats) {
				case 1:
					if (moreStatsOffset < 120) {
						moreStatsOffset += 5;
					} else {
						moreStats = 2;
					}
					break;
				case 3:
					if (moreStatsOffset > 0) {
						moreStatsOffset -= 5;
					} else {
						moreStats = 0;
					}
					break;
				}
				g.setColor(new Color(0, 0, 70, (int) (110 * statsAlpha)));
				g.fillRoundRect(555, 327 - moreStatsOffset, 175,
						134 + moreStatsOffset, cornerNum, cornerNum);
				g.setColor(statsWhite);
				g.drawRoundRect(555, 327 - moreStatsOffset, 175,
						134 + moreStatsOffset, cornerNum, cornerNum);
				g.setFont(new Font("Arial", Font.BOLD, 14));
				g.drawString(properties.name() + " v" + getVersion(), 561,
						coords[9] + 8 - moreStatsOffset);
				g.setFont(new Font("Arial", Font.PLAIN, 12));
				g.drawString("Run Time: " + hours + ":" + minutes + ":"
						+ seconds, 561, coords[10] + 8 - moreStatsOffset);
				g.setColor(new Color(200, 200, 200, (int) (255 * statsAlpha)));
				g.drawString("-----------------------------------------", 561,
						coords[11] + 3 - moreStatsOffset);
				g.setColor(statsWhite);
				g.drawString("Tokens Gained: " + tokensGained, 561, coords[12]
						- moreStatsOffset);
				g.drawString("Tokens/Hour: " + tokensPerHour, 561, coords[13]
						- moreStatsOffset);
				g.drawString("Games Played: " + gamesPlayed, 561, coords[14]
						- moreStatsOffset);
				g.drawString("Won: " + gamesWon + "  Lost: " + gamesLost
						+ "  Ratio: " + getRatio(), 561, coords[15]
						- moreStatsOffset);
				if (moreStatsOffset >= 115) {
					if (moreStats == 1 || moreStats == 2) {
						if (textAlpha[0] < 1.0) {
							textAlpha[0] += 0.2;
						}
					}
					if (moreStats == 3 || moreStats == 0) {
						if (textAlpha[0] > 0.0) {
							textAlpha[0] -= 0.2;
						}
					}
					g.setColor(new Color(255, 255, 255, getTextAlpha(0)));
					g.drawString("Games/Hour: " + gamesPerHour, 561, coords[8]);
				}
				if (moreStatsOffset >= 105) {
					if (moreStats == 1 || moreStats == 2) {
						if (textAlpha[1] < 1.0) {
							textAlpha[1] += 0.2;
						}
					}
					if (moreStatsOffset <= 115) {
						if (moreStats == 3 || moreStats == 0) {
							if (textAlpha[1] > 0.0) {
								textAlpha[1] -= 0.2;
							}
						}
					}
					g.setColor(new Color(255, 255, 255, getTextAlpha(1)));
					g.drawString("Wins/H: " + gamesWonPerHour + "  "
							+ "Losses/H: " + gamesLostPerHour, 561, coords[9]);
				}
				if (moreStatsOffset >= 90) {
					if (moreStats == 1 || moreStats == 2) {
						if (textAlpha[2] < 1.0) {
							textAlpha[2] += 0.2;
						}
					}
					if (moreStatsOffset <= 105) {
						if (moreStats == 3 || moreStats == 0) {
							if (textAlpha[2] > 0.0) {
								textAlpha[2] -= 0.2;
							}
						}
					}
					g.setColor(new Color(255, 255, 255, getTextAlpha(2)));
					g.drawString("% Games Won: " + ((int) (getRatio2() * 100))
							+ "%", 561, coords[10]);
				}
				if (moreStatsOffset >= 75) {
					if (moreStats == 1 || moreStats == 2) {
						if (textAlpha[3] < 1.0) {
							textAlpha[3] += 0.2;
						}
					}
					if (moreStatsOffset <= 90) {
						if (moreStats == 3 || moreStats == 0) {
							if (textAlpha[3] > 0.0) {
								textAlpha[3] -= 0.2;
							}
						}
					}
					g.setColor(new Color(255, 255, 255, getTextAlpha(3)));
					g.drawString("Average Charges: " + averageCharges, 561,
							coords[11]);
				}
				if (moreStatsOffset >= 60) {
					if (moreStats == 1 || moreStats == 2) {
						if (textAlpha[4] < 1.0) {
							textAlpha[4] += 0.2;
						}
					}
					if (moreStatsOffset <= 75) {
						if (moreStats == 3 || moreStats == 0) {
							if (textAlpha[4] > 0.0) {
								textAlpha[4] -= 0.2;
							}
						}
					}
					g.setColor(new Color(255, 255, 255, getTextAlpha(4)));
					g.drawString("Average Op. Charges: "
							+ averageChargesOpponent, 561, coords[12]);
				}
				if (moreStatsOffset >= 45) {
					if (moreStats == 1 || moreStats == 2) {
						if (textAlpha[5] < 1.0) {
							textAlpha[5] += 0.2;
						}
					}
					if (moreStatsOffset <= 60) {
						if (moreStats == 3 || moreStats == 0) {
							if (textAlpha[5] > 0.0) {
								textAlpha[5] -= 0.2;
							}
						}
					}
					g.setColor(new Color(255, 255, 255, getTextAlpha(5)));
					g.drawString("Current Rating: " + currentRating, 561,
							coords[13]);
				}
				if (moreStatsOffset >= 30) {
					if (moreStats == 1 || moreStats == 2) {
						if (textAlpha[6] < 1.0) {
							textAlpha[6] += 0.2;
						}
					}
					if (moreStatsOffset <= 45) {
						if (moreStats == 3 || moreStats == 0) {
							if (textAlpha[6] > 0.0) {
								textAlpha[6] -= 0.2;
							}
						}
					}
					g.setColor(new Color(255, 255, 255, getTextAlpha(6)));
					g.drawString("Gained Rating: " + gainedRating, 561,
							coords[14]);
				}
				if (moreStatsOffset >= 15) {
					if (moreStats == 1 || moreStats == 2) {
						if (textAlpha[7] < 1.0) {
							textAlpha[7] += 0.2;
						}
					}
					if (moreStatsOffset <= 30) {
						if (moreStats == 3 || moreStats == 0) {
							if (textAlpha[7] > 0.0) {
								textAlpha[7] -= 0.2;
							}
						}
					}
					g.setColor(new Color(255, 255, 255, getTextAlpha(7)));
					g.drawString("Rating/Hour: " + ratingPerHour, 561,
							coords[15]);
				}
			}
		}

		Color expWhite = new Color(255, 255, 255, (int) (255 * expAlpha));

		// Exp Paint
		if (game.isLoggedIn() && initialised && expPaint != 0) {
			if (expPaint == 1) {
				if (expAlpha < 1.0) {
					expAlpha += 0.2;
				} else {
					expPaint = 2;
				}
			} else if (expPaint == 2) {
				expAlpha = 1.0;
			} else if (expPaint == 3) {
				if (expAlpha > 0.2) {
					expAlpha -= 0.2;
				} else {
					expAlpha = 0.0;
					expPaint = 0;
				}
			}

			expWhite = new Color(255, 255, 255, (int) (255 * expAlpha));

			int gainedAttackExp = skills.getCurrentExp(Skills.ATTACK)
					- startAttackExp;
			int gainedStrengthExp = skills.getCurrentExp(Skills.STRENGTH)
					- startStrengthExp;
			int gainedDefenceExp = skills.getCurrentExp(Skills.DEFENSE)
					- startDefenceExp;
			int gainedMagicExp = skills.getCurrentExp(Skills.MAGIC)
					- startMagicExp;
			int gainedRangeExp = skills.getCurrentExp(Skills.RANGE)
					- startRangeExp;
			int gainedConstitutionExp = skills
					.getCurrentExp(Skills.CONSTITUTION) - startConstitutionExp;

			int gainedAttackExpHour = (int) ((3600000.0 / runTime) * gainedAttackExp);
			int gainedStrengthExpHour = (int) ((3600000.0 / runTime) * gainedStrengthExp);
			int gainedDefenceExpHour = (int) ((3600000.0 / runTime) * gainedDefenceExp);
			int gainedMagicExpHour = (int) ((3600000.0 / runTime) * gainedMagicExp);
			int gainedRangeExpHour = (int) ((3600000.0 / runTime) * gainedRangeExp);
			int gainedConstitutionExpHour = (int) ((3600000.0 / runTime) * gainedConstitutionExp);

			int gainedAttackLevel = skills.getRealLevel(Skills.ATTACK)
					- startAttackLevel;
			int gainedStrengthLevel = skills.getRealLevel(Skills.STRENGTH)
					- startStrengthLevel;
			int gainedDefenceLevel = skills.getRealLevel(Skills.DEFENSE)
					- startDefenceLevel;
			int gainedMagicLevel = skills.getRealLevel(Skills.MAGIC)
					- startMagicLevel;
			int gainedRangeLevel = skills.getRealLevel(Skills.RANGE)
					- startRangeLevel;
			int gainedConstitutionLevel = skills
					.getRealLevel(Skills.CONSTITUTION) - startConstitutionLevel;

			Color blackVisible = new Color(0, 0, 0, (int) (200 * expAlpha));
			Color blackInvisible = new Color(0, 0, 0, 0);
			expGradientBox.y = expYs[nextPlace];
			GradientPaint expGradient = new GradientPaint(expGradientBox.x,
					expGradientBox.y, blackVisible, expGradientBox.x,
					expGradientBox.y + expGradientBox.height, blackInvisible,
					false);
			g2d.setPaint(expGradient);
			g2d.fillRect(expGradientBox.x, expGradientBox.y,
					expGradientBox.width, expGradientBox.height);

			Color expDarkGrey = new Color(70, 70, 70, (int) (255 * expAlpha));
			Color expDarkerGrey = new Color(40, 40, 40, (int) (255 * expAlpha));
			Color expBlack = new Color(0, 0, 0, (int) (255 * expAlpha));
			Color expDarkBlue = new Color(0, 0, 150, (int) (255 * expAlpha));
			Color expBrown = new Color(75, 45, 0, (int) (255 * expAlpha));
			Color expDarkRed = new Color(150, 0, 0, (int) (255 * expAlpha));
			Color expGreen = new Color(0, 255, 0, (int) (255 * expAlpha));
			Color expRed = new Color(255, 0, 0, (int) (255 * expAlpha));

			if (gainedAttackExp > 0) {
				if (attackBox.y == 0) {
					attackBox.y = expYs[nextPlace];
					nextPlace++;
				}
				g.setColor(expBlack);
				g.fillRoundRect(attackBox.x, attackBox.y, attackBox.width,
						attackBox.height, 0, 0);
				g.setColor(expWhite);
				g.setFont(new Font("Arial", Font.BOLD, 11));
				g.drawString("Attack", attackBox.x + 3, attackBox.y + 10);
				g.setFont(new Font("Arial", Font.PLAIN, 11));
				g.drawString("XP Gained: " + gainedAttackExp, attackBox.x + 77,
						attackBox.y + 10);
				g.drawString("XP/Hour: " + gainedAttackExpHour,
						attackBox.x + 183, attackBox.y + 10);
				g.drawString("Level: " + skills.getRealLevel(Skills.ATTACK)
						+ "(+" + gainedAttackLevel + ")", attackBox.x + 268,
						attackBox.y + 10);
				if (skills.getRealLevel(Skills.ATTACK) < 99) {
					g.drawString("% to "
							+ (skills.getRealLevel(Skills.ATTACK) + 1) + ":",
							attackBox.x + 343, attackBox.y + 10);
					drawPercentBar(g, g2d, attackBox.x + 385, attackBox.y + 1,
							attackBox.height - 3, expBlack, expWhite, expGreen,
							expRed, skills.getPercentToNextLevel(Skills.ATTACK));
				}
			}

			if (gainedStrengthExp > 0) {
				if (strengthBox.y == 0) {
					strengthBox.y = expYs[nextPlace];
					nextPlace++;
				}
				g.setColor(expDarkerGrey);
				g.fillRoundRect(strengthBox.x, strengthBox.y,
						strengthBox.width, strengthBox.height, 0, 0);
				g.setColor(expWhite);
				g.setFont(new Font("Arial", Font.BOLD, 11));
				g.drawString("Strength", strengthBox.x + 3, strengthBox.y + 10);
				g.setFont(new Font("Arial", Font.PLAIN, 11));
				g.drawString("XP Gained: " + gainedStrengthExp,
						strengthBox.x + 77, strengthBox.y + 10);
				g.drawString("XP/Hour: " + gainedStrengthExpHour,
						strengthBox.x + 183, strengthBox.y + 10);
				g.drawString("Level: " + skills.getRealLevel(Skills.STRENGTH)
						+ "(+" + gainedStrengthLevel + ")",
						strengthBox.x + 268, strengthBox.y + 10);
				if (skills.getRealLevel(Skills.STRENGTH) < 99) {
					g.drawString(
							"% to "
									+ (skills.getRealLevel(Skills.STRENGTH) + 1)
									+ ":", strengthBox.x + 343,
							strengthBox.y + 10);
					drawPercentBar(g, g2d, strengthBox.x + 385,
							strengthBox.y + 1, strengthBox.height - 3,
							expBlack, expWhite, expGreen, expRed,
							skills.getPercentToNextLevel(Skills.STRENGTH));
				}
			}

			if (gainedDefenceExp > 0) {
				if (defenceBox.y == 0) {
					defenceBox.y = expYs[nextPlace];
					nextPlace++;
				}
				g.setColor(expDarkGrey);
				g.fillRoundRect(defenceBox.x, defenceBox.y, defenceBox.width,
						defenceBox.height, 0, 0);
				g.setColor(expWhite);
				g.setFont(new Font("Arial", Font.BOLD, 11));
				g.drawString("Defence", defenceBox.x + 3, defenceBox.y + 10);
				g.setFont(new Font("Arial", Font.PLAIN, 11));
				g.drawString("XP Gained: " + gainedDefenceExp,
						defenceBox.x + 77, defenceBox.y + 10);
				g.drawString("XP/Hour: " + gainedDefenceExpHour,
						defenceBox.x + 183, defenceBox.y + 10);
				g.drawString("Level: " + skills.getRealLevel(Skills.DEFENSE)
						+ "(+" + gainedDefenceLevel + ")", defenceBox.x + 268,
						defenceBox.y + 10);
				if (skills.getRealLevel(Skills.DEFENSE) < 99) {
					g.drawString("% to "
							+ (skills.getRealLevel(Skills.DEFENSE) + 1) + ":",
							defenceBox.x + 343, defenceBox.y + 10);
					drawPercentBar(g, g2d, defenceBox.x + 385,
							defenceBox.y + 1, defenceBox.height - 3, expBlack,
							expWhite, expGreen, expRed,
							skills.getPercentToNextLevel(Skills.DEFENSE));
				}
			}

			if (gainedMagicExp > 0) {
				if (magicBox.y == 0) {
					magicBox.y = expYs[nextPlace];
					nextPlace++;
				}
				g.setColor(expDarkBlue);
				g.fillRoundRect(magicBox.x, magicBox.y, magicBox.width,
						magicBox.height, 0, 0);
				g.setColor(expWhite);
				g.setFont(new Font("Arial", Font.BOLD, 11));
				g.drawString("Magic", magicBox.x + 3, magicBox.y + 10);
				g.setFont(new Font("Arial", Font.PLAIN, 11));
				g.drawString("XP Gained: " + gainedMagicExp, magicBox.x + 77,
						magicBox.y + 10);
				g.drawString("XP/Hour: " + gainedMagicExpHour,
						magicBox.x + 183, magicBox.y + 10);
				g.drawString("Level: " + skills.getRealLevel(Skills.MAGIC)
						+ "(+" + gainedMagicLevel + ")", magicBox.x + 268,
						magicBox.y + 10);
				if (skills.getRealLevel(Skills.MAGIC) < 99) {
					g.drawString("% to "
							+ (skills.getRealLevel(Skills.MAGIC) + 1) + ":",
							magicBox.x + 343, magicBox.y + 10);
					drawPercentBar(g, g2d, magicBox.x + 385, magicBox.y + 1,
							magicBox.height - 3, expBlack, expWhite, expGreen,
							expRed, skills.getPercentToNextLevel(Skills.MAGIC));
				}
			}

			if (gainedRangeExp > 0) {
				if (rangeBox.y == 0) {
					rangeBox.y = expYs[nextPlace];
					nextPlace++;
				}
				g.setColor(expBrown);
				g.fillRoundRect(rangeBox.x, rangeBox.y, rangeBox.width,
						rangeBox.height, 0, 0);
				g.setColor(expWhite);
				g.setFont(new Font("Arial", Font.BOLD, 11));
				g.drawString("Range", rangeBox.x + 3, rangeBox.y + 10);
				g.setFont(new Font("Arial", Font.PLAIN, 11));
				g.drawString("XP Gained: " + gainedRangeExp, rangeBox.x + 77,
						rangeBox.y + 10);
				g.drawString("XP/Hour: " + gainedRangeExpHour,
						rangeBox.x + 183, rangeBox.y + 10);
				g.drawString("Level: " + skills.getRealLevel(Skills.RANGE)
						+ "(+" + gainedRangeLevel + ")", rangeBox.x + 268,
						rangeBox.y + 10);
				if (skills.getRealLevel(Skills.RANGE) < 99) {
					g.drawString("% to "
							+ (skills.getRealLevel(Skills.RANGE) + 1) + ":",
							rangeBox.x + 343, rangeBox.y + 10);
					drawPercentBar(g, g2d, rangeBox.x + 385, rangeBox.y + 1,
							rangeBox.height - 3, expBlack, expWhite, expGreen,
							expRed, skills.getPercentToNextLevel(Skills.RANGE));
				}
			}

			if (gainedConstitutionExp > 0) {
				if (constitutionBox.y == 0) {
					constitutionBox.y = expYs[nextPlace];
					nextPlace++;
				}
				g.setColor(expDarkRed);
				g.fillRoundRect(constitutionBox.x, constitutionBox.y,
						constitutionBox.width, constitutionBox.height, 0, 0);
				g.setColor(expWhite);
				g.setFont(new Font("Arial", Font.BOLD, 11));
				g.drawString("Constitution", constitutionBox.x + 3,
						constitutionBox.y + 10);
				g.setFont(new Font("Arial", Font.PLAIN, 11));
				g.drawString("XP Gained: " + gainedConstitutionExp,
						constitutionBox.x + 77, constitutionBox.y + 10);
				g.drawString("XP/Hour: " + gainedConstitutionExpHour,
						constitutionBox.x + 183, constitutionBox.y + 10);
				g.drawString(
						"Level: " + skills.getRealLevel(Skills.CONSTITUTION)
								+ "(+" + gainedConstitutionLevel + ")",
						constitutionBox.x + 268, constitutionBox.y + 10);
				if (skills.getRealLevel(Skills.CONSTITUTION) < 99) {
					g.drawString(
							"% to "
									+ (skills.getRealLevel(Skills.CONSTITUTION) + 1)
									+ ":", constitutionBox.x + 343,
							constitutionBox.y + 10);
					drawPercentBar(g, g2d, constitutionBox.x + 385,
							constitutionBox.y + 1, constitutionBox.height - 3,
							expBlack, expWhite, expGreen, expRed,
							skills.getPercentToNextLevel(Skills.CONSTITUTION));
				}
			}

			// Mouse Over Boxes

			if (gainedAttackExp > 0) {
				if (mouseInArea(attackBox)) {
					drawExpInfo(g, g2d, expBlack, expWhite, expBlack, expGreen,
							expRed, "Attack", Skills.ATTACK, gainedAttackExp,
							gainedAttackLevel);
				}
			}

			if (gainedStrengthExp > 0) {
				if (mouseInArea(strengthBox)) {
					drawExpInfo(g, g2d, expBlack, expWhite, expDarkerGrey,
							expGreen, expRed, "Strength", Skills.STRENGTH,
							gainedStrengthExp, gainedStrengthLevel);
				}
			}

			if (gainedDefenceExp > 0) {
				if (mouseInArea(defenceBox)) {
					drawExpInfo(g, g2d, expBlack, expWhite, expDarkGrey,
							expGreen, expRed, "Defence", Skills.DEFENSE,
							gainedDefenceExp, gainedDefenceLevel);
				}
			}

			if (gainedMagicExp > 0) {
				if (mouseInArea(magicBox)) {
					drawExpInfo(g, g2d, expBlack, expWhite, expDarkBlue,
							expGreen, expRed, "Magic", Skills.MAGIC,
							gainedMagicExp, gainedMagicLevel);
				}
			}

			if (gainedRangeExp > 0) {
				if (mouseInArea(rangeBox)) {
					drawExpInfo(g, g2d, expBlack, expWhite, expBrown, expGreen,
							expRed, "Range", Skills.RANGE, gainedRangeExp,
							gainedRangeLevel);
				}
			}

			if (gainedConstitutionExp > 0) {
				if (mouseInArea(constitutionBox)) {
					drawExpInfo(g, g2d, expBlack, expWhite, expDarkRed,
							expGreen, expRed, "Constitution",
							Skills.CONSTITUTION, gainedConstitutionExp,
							gainedConstitutionLevel);
				}
			}
		}

		// Status Button
		if (statusPaint == 1 || statusPaint == 2) {
			if (mouseInArea(statusButtonRec)) {
				if (userMousePressed) {
					drawButton(g, statusButtonRec, buttonPressedOn, "Status",
							Color.WHITE);
				} else {
					drawButton(g, statusButtonRec, buttonOverOn, "Status",
							Color.WHITE);
				}
			} else {
				drawButton(g, statusButtonRec, buttonNormalOn, "Status",
						Color.WHITE);
			}
		} else {
			if (mouseInArea(statusButtonRec)) {
				if (userMousePressed) {
					drawButton(g, statusButtonRec, buttonPressed, "Status",
							Color.WHITE);
				} else {
					drawButton(g, statusButtonRec, buttonOver, "Status",
							Color.WHITE);
				}
			} else {
				drawButton(g, statusButtonRec, buttonNormal, "Status",
						Color.WHITE);
			}
		}
		// Exp Button
		if (expPaint == 1 || expPaint == 2) {
			if (mouseInArea(expButtonRec)) {
				if (userMousePressed) {
					drawButton(g, expButtonRec, buttonPressedOn, "Exp",
							Color.WHITE);
				} else {
					drawButton(g, expButtonRec, buttonOverOn, "Exp",
							Color.WHITE);
				}
			} else {
				drawButton(g, expButtonRec, buttonNormalOn, "Exp", Color.WHITE);
			}
		} else {
			if (mouseInArea(expButtonRec)) {
				if (userMousePressed) {
					drawButton(g, expButtonRec, buttonPressed, "Exp",
							Color.WHITE);
				} else {
					drawButton(g, expButtonRec, buttonOver, "Exp", Color.WHITE);
				}
			} else {
				drawButton(g, expButtonRec, buttonNormal, "Exp", Color.WHITE);
			}
		}
		// Stats Button
		if (statsPaint == 1 || statsPaint == 2) {
			if (mouseInArea(statsButtonRec)) {
				if (userMousePressed) {
					drawButton(g, statsButtonRec, buttonPressedOn, "Stats",
							Color.WHITE);
				} else {
					drawButton(g, statsButtonRec, buttonOverOn, "Stats",
							Color.WHITE);
				}
			} else {
				drawButton(g, statsButtonRec, buttonNormalOn, "Stats",
						Color.WHITE);
			}
		} else {
			if (mouseInArea(statsButtonRec)) {
				if (userMousePressed) {
					drawButton(g, statsButtonRec, buttonPressed, "Stats",
							Color.WHITE);
				} else {
					drawButton(g, statsButtonRec, buttonOver, "Stats",
							Color.WHITE);
				}
			} else {
				drawButton(g, statsButtonRec, buttonNormal, "Stats",
						Color.WHITE);
			}
		}
		// More Stats Button
		if (statsPaint == 1 || statsPaint == 2 || statsPaint == 3) {
			if (moreStats == 1 || moreStats == 2) {
				if (mouseInArea(moreStatsButtonRec)) {
					if (userMousePressed) {
						drawButton(g, moreStatsButtonRec, buttonPressedOnS,
								"Less", statsWhite, -1, statsWhite);
					} else {
						drawButton(g, moreStatsButtonRec, buttonOverOnS,
								"Less", statsWhite, -1, statsWhite);
					}
				} else {
					drawButton(g, moreStatsButtonRec, buttonNormalOnS, "Less",
							statsWhite, -1, statsWhite);
				}
				if (downArrow != null) {
					if (statsAlpha > 0.5) {
						g.drawImage(downArrow, moreStatsButtonRec.x + 4,
								moreStatsButtonRec.y + 3, null);
						g.drawImage(downArrow,
								moreStatsButtonRec.x + moreStatsButtonRec.width
										- downArrow.getWidth() - 4,
								moreStatsButtonRec.y + 3, null);
					}
				}
			} else if (moreStats == 0 || moreStats == 3) {
				if (mouseInArea(moreStatsButtonRec)) {
					if (userMousePressed) {
						drawButton(g, moreStatsButtonRec, buttonPressedS,
								"More", statsWhite, -1, statsWhite);
					} else {
						drawButton(g, moreStatsButtonRec, buttonOverS, "More",
								statsWhite, -1, statsWhite);
					}
				} else {
					drawButton(g, moreStatsButtonRec, buttonNormalS, "More",
							statsWhite, -1, statsWhite);
				}
				if (downArrow != null) {
					if (statsAlpha > 0.5) {
						g.drawImage(upArrow, moreStatsButtonRec.x + 4,
								moreStatsButtonRec.y + 3, null);
						g.drawImage(upArrow, moreStatsButtonRec.x
								+ moreStatsButtonRec.width - upArrow.getWidth()
								- 4, moreStatsButtonRec.y + 3, null);
					}
				}
			}
		}
	}

	public int getTTLSecs(int skill, int gainedExp) {
		int seconds = (int) (skills.getExpToNextLevel(skill) / ((1000.0 / runTime) * gainedExp));
		if (seconds >= 60) {
			int minutes = seconds / 60;
			seconds -= (minutes * 60);
		}
		return seconds;
	}

	public int getTTLMins(int skill, int gainedExp) {
		int minutes = 0;
		int seconds = (int) (skills.getExpToNextLevel(skill) / ((1000.0 / runTime) * gainedExp));
		if (seconds >= 60) {
			minutes = seconds / 60;
			seconds -= (minutes * 60);
		}
		if (minutes >= 60) {
			int hours = minutes / 60;
			minutes -= (hours * 60);
		}
		return minutes;
	}

	public int getTTLHours(int skill, int gainedExp) {
		int hours = 0;
		int minutes = 0;
		int seconds = (int) (skills.getExpToNextLevel(skill) / ((1000.0 / runTime) * gainedExp));
		if (seconds >= 60) {
			minutes = seconds / 60;
			seconds -= (minutes * 60);
		}
		if (minutes >= 60) {
			hours = minutes / 60;
			minutes -= (hours * 60);
		}
		return hours;
	}

	public String getTTL(int skill, int gainedExp) {
		return "" + getTTLHours(skill, gainedExp) + ":"
				+ getTTLMins(skill, gainedExp) + ":"
				+ getTTLSecs(skill, gainedExp);
	}

	public int getTextAlpha(int index) {
		return (int) (((int) (textAlpha[index] * 255)) * statsAlpha);
	}

	public void drawPercentBar(Graphics g, Graphics2D g2d, int x, int y,
			int height, Color black, Color white, Color green, Color red,
			int percent) {
		g.setColor(red);
		g.fillRoundRect(x, y, 100, height, 11, 11);
		g.setColor(green);
		g.fillRoundRect(x, y, percent, height, 11, 11);
		GradientPaint gradient = new GradientPaint(x, y, white, x, y
				+ (int) (height / 1.5), new Color(255, 255, 255, 0), false);
		g2d.setPaint(gradient);
		g2d.fillRoundRect(x, y, 100, height, 11, 11);
		g.setColor(black);
		g.setFont(new Font("Arial", Font.PLAIN, 10));
		g.drawString("" + percent + "%", x + 10, y + height - 1);
		g.drawRoundRect(x, y, 100, height, 11, 11);
		g.drawRoundRect(x, y, percent, height, 11, 11);
	}

	public void drawExpInfo(Graphics g, Graphics2D g2d, Color black,
			Color white, Color background, Color green, Color red,
			String skillName, int skill, int gainedExp, int gainedLevel) {
		int boxX = userMouseX;
		int boxY = userMouseY - 112;
		int width = 130;
		int height = userMouseY - boxY;
		int[] ys = { 3, 15, 27, 39, 51, 63, 75, 87, 99 };
		for (int i = 0; i < ys.length; i++) {
			ys[i] = ys[i] + boxY + 9;
		}
		for (int i = 1; i < ys.length; i++) {
			ys[i] += 1;
		}
		int textX = boxX + 5;
		int expPerHour = (int) ((3600000.0 / runTime) * gainedExp);
		g.setColor(background);
		g.fillRoundRect(boxX, boxY, width, height, cornerNum, cornerNum);
		GradientPaint gradient = new GradientPaint(boxX, boxY, white, boxX,
				boxY + 7, new Color(255, 255, 255, 0), false);
		g2d.setPaint(gradient);
		g2d.fillRoundRect(boxX, boxY, width, height, cornerNum, cornerNum);
		g.setColor(black);
		g.drawRoundRect(boxX, boxY, width, height, cornerNum, cornerNum);
		g.setColor(white);
		Font theFont = new Font("Arial", Font.BOLD, 10);
		FontMetrics theFontMetrics = g.getFontMetrics(theFont);
		int titleWidth = theFontMetrics.stringWidth(skillName);
		int titleX = boxX + (((width / 2)) - ((titleWidth / 2)));
		g.setFont(theFont);
		g.drawString(skillName, titleX, ys[0]);
		g.drawString("Current XP: " + skills.getCurrentExp(skill), textX, ys[1]);
		g.drawString("Gained XP: " + gainedExp, textX, ys[2]);
		g.drawString("XP/Hour: " + expPerHour, textX, ys[3]);
		g.drawString("Current LvL: " + skills.getRealLevel(skill), textX, ys[4]);
		g.drawString("Gained LvLs: " + gainedLevel, textX, ys[5]);
		g.drawString("XP to LvL: " + skills.getExpToNextLevel(skill), textX,
				ys[6]);
		g.drawString("TTL: " + getTTL(skill, gainedExp), textX, ys[7]);
		int barX = boxX + (((width / 2)) - 50);
		drawPercentBar(g, g2d, barX, ys[8] - 9, 10, black, white, green, red,
				skills.getPercentToNextLevel(skill));
	}

	public void drawButton(Graphics g, Rectangle area, Color color,
			String text, Color textColor) {
		drawButton(g, area, color, text, textColor, 0, Color.WHITE);
	}

	public void drawButton(Graphics g, Rectangle area, Color color,
			String text, Color textColor, int textOffsetY) {
		drawButton(g, area, color, text, textColor, textOffsetY, Color.WHITE);
	}

	public void drawButton(Graphics g, Rectangle area, Color color,
			String text, Color textColor, int textOffsetY, Color borderColor) {
		g.setColor(color);
		g.fillRoundRect(area.x, area.y, area.width, area.height, 8, 8);
		g.setColor(borderColor);
		g.drawRoundRect(area.x, area.y, area.width, area.height, 8, 8);
		g.setColor(textColor);
		Font theFont = new Font("Arial", Font.PLAIN, 12);
		FontMetrics theFontMetrics = g.getFontMetrics(theFont);
		int textWidth = theFontMetrics.stringWidth(text);
		int textX = area.x + (((area.width / 2)) - ((textWidth / 2)));
		g.setFont(theFont);
		g.drawString(text, textX, area.y + 12 + textOffsetY);
	}

	public void drawHealthBarHunted(Graphics g, Graphics2D g2d, int X, int Y)
			throws NullPointerException {
		g.setFont(new Font("Arial", Font.PLAIN, 14));
		g.setColor(new Color(255, 255, 255, (int) (255 * statusAlpha)));
		g.drawString("Your Health: ", X, Y);
		g.setFont(new Font("Arial", Font.PLAIN, 10));
		if (status.equals("Dying...")) {
			g.setColor(new Color(255, 0, 0, (int) (255 * statusAlpha)));
			g.fillRoundRect(X + 100, Y - 10, 200, 10, 11, 11);
			GradientPaint gradient = new GradientPaint(X + 100, Y - 10,
					new Color(255, 255, 255, (int) (255 * statusAlpha)),
					X + 100, Y + ((int) (10 / 1.5)) - 10, new Color(255, 255,
							255, 0), false);
			g2d.setPaint(gradient);
			g2d.fillRoundRect(X + 100, Y - 10, 200, 10, 11, 11);
			g.setColor(new Color(0, 0, 0, (int) (255 * statusAlpha)));
			g.drawString("0%", X + 115, Y - 1);
			g.drawRoundRect(X + 100, Y - 10, 200, 10, 11, 11);
		} else {
			g.setColor(new Color(255, 0, 0, (int) (255 * statusAlpha)));
			g.fillRoundRect(X + 100, Y - 10, 200, 10, 11, 11);
			g.setColor(new Color(0, 255, 0, (int) (255 * statusAlpha)));
			g.fillRoundRect(X + 100, Y - 10, getMyPlayer().getHPPercent() * 2,
					10, 11, 11);
			GradientPaint gradient = new GradientPaint(X + 100, Y - 10,
					new Color(255, 255, 255, (int) (255 * statusAlpha)),
					X + 100, Y + ((int) (10 / 1.5)) - 10, new Color(255, 255,
							255, 0), false);
			g2d.setPaint(gradient);
			g2d.fillRoundRect(X + 100, Y - 10, 200, 10, 11, 11);
			g.setColor(new Color(0, 0, 0, (int) (255 * statusAlpha)));
			g.drawString("" + getMyPlayer().getHPPercent() + "%", X + 115,
					Y - 1);
			g.drawRoundRect(X + 100, Y - 10, 200, 10, 11, 11);
			g.drawRoundRect(X + 100, Y - 10, getMyPlayer().getHPPercent() * 2,
					10, 11, 11);
		}
	}

	public void drawHealthBarHunting(Graphics g, Graphics2D g2d, int X, int Y)
			throws NullPointerException {
		g.setFont(new Font("Arial", Font.PLAIN, 14));
		g.setColor(new Color(255, 255, 255, (int) (255 * statusAlpha)));
		g.drawString("Opponent's Health: ", X, Y);
		g.setFont(new Font("Arial", Font.PLAIN, 10));
		if (status.equals("Opponent Killed")) {
			g.setColor(new Color(255, 0, 0, (int) (255 * statusAlpha)));
			g.fillRoundRect(X + 130, Y - 10, 200, 10, 11, 11);
			GradientPaint gradient = new GradientPaint(X + 130, Y - 10,
					new Color(255, 255, 255, (int) (255 * statusAlpha)),
					X + 130, Y + ((int) (10 / 1.5)) - 10, new Color(255, 255,
							255, 0), false);
			g2d.setPaint(gradient);
			g2d.fillRoundRect(X + 130, Y - 10, 200, 10, 11, 11);
			g.setColor(new Color(0, 0, 0, (int) (255 * statusAlpha)));
			g.drawString("0%", X + 145, Y - 1);
			g.drawRoundRect(X + 130, Y - 10, 200, 10, 11, 11);
		} else {
			g.setColor(new Color(255, 0, 0, (int) (255 * statusAlpha)));
			g.fillRoundRect(X + 130, Y - 10, 200, 10, 11, 11);
			g.setColor(new Color(0, 255, 0, (int) (255 * statusAlpha)));
			g.fillRoundRect(X + 130, Y - 10,
					getPlayerThrow().getHPPercent() * 2, 10, 11, 11);
			GradientPaint gradient = new GradientPaint(X + 130, Y - 10,
					new Color(255, 255, 255, (int) (255 * statusAlpha)),
					X + 130, Y + ((int) (10 / 1.5)) - 10, new Color(255, 255,
							255, 0), false);
			g2d.setPaint(gradient);
			g2d.fillRoundRect(X + 130, Y - 10, 200, 10, 11, 11);
			g.setColor(new Color(0, 0, 0, (int) (255 * statusAlpha)));
			g.drawString("" + getPlayerThrow().getHPPercent() + "%", X + 145,
					Y - 1);
			g.drawRoundRect(X + 130, Y - 10, 200, 10, 11, 11);
			g.drawRoundRect(X + 130, Y - 10,
					getPlayerThrow().getHPPercent() * 2, 10, 11, 11);
		}
	}

	public void drawHealthBar(Graphics g, Graphics2D g2d, int X, int Y)
			throws NullPointerException {
		if (round == 1) {
			if (hunted) {
				drawHealthBarHunted(g, g2d, X, Y);
			} else {
				drawHealthBarHunting(g, g2d, X, Y);
			}
		} else if (round == 2) {
			if (hunted2) {
				drawHealthBarHunted(g, g2d, X, Y);
			} else {
				drawHealthBarHunting(g, g2d, X, Y);
			}
		}
	}

	public void drawTile(final Graphics g, final RSTile tile,
			final Color tileColor, final String caption,
			final Color captionColor) {
		final RSTile tx = new RSTile(tile.getX() + 1, tile.getY());
		final RSTile ty = new RSTile(tile.getX(), tile.getY() + 1);
		final RSTile txy = new RSTile(tile.getX() + 1, tile.getY() + 1);
		final Point pn = calc.tileToScreen(tile, 0, 0, 0);
		final Point px = calc.tileToScreen(tx, 0, 0, 0);
		final Point py = calc.tileToScreen(ty, 0, 0, 0);
		final Point pxy = calc.tileToScreen(txy, 0, 0, 0);
		if (pn.x != -1 && pn.y != -1 && px.x != -1 && px.y != -1 && py.x != -1
				&& py.y != -1 && pxy.x != -1 && pxy.y != -1) {
			g.setColor(new Color(0, 0, 0, 255));
			g.drawPolygon(new int[] { py.x, pxy.x, px.x, pn.x }, new int[] {
					py.y, pxy.y, px.y, pn.y }, 4);
			g.setColor(tileColor);
			g.fillPolygon(new int[] { py.x, pxy.x, px.x, pn.x }, new int[] {
					py.y, pxy.y, px.y, pn.y }, 4);
			g.setColor(captionColor);
			g.setFont(new Font("Arial", Font.BOLD, 11));
			g.drawString(caption,
					Math.max(Math.max(pn.x, px.x), Math.max(py.x, pxy.x)) + 5,
					Math.min(Math.min(pn.y, px.y), Math.min(py.y, pxy.y)) + 15);
		}
	}

	public boolean doModelAction(RSObject obj, String action) {
		try {
			RSModel model = obj.getModel();
			if (model != null) {
				if (obj.isOnScreen()) {
					Point p = model.getPoint();
					if (p.getX() != -1 && p.getY() != -1) {
						int tries = 0;
						boolean done = false;
						while (!done && tries < 10) {
							mouse.move(model.getPoint(), 0, 0);
							if (menu.doAction(action)) {
								done = true;
							}
							tries++;
						}
						if (tries < 10) {
							return true;
						} else {
							dlog("Object doAction failed");
							return false;
						}
					} else {
						dlog("Object point not on screen");
						return false;
					}
				} else {
					dlog("Object not on screen");
					return false;
				}
			} else {
				dlog("Object does not have model");
				return false;
			}
		} catch (NullPointerException fail) {
		}
		return false;
	}

	public void getStone() {
		status = "Getting Stone...";
		try {
			boolean walkToStone = false;
			stoneObject = objects.getNearest(30143);
			stoneObject.getLocation();
			RSTile nextToStoneTile = new RSTile(stoneObject.getLocation()
					.getX() + 1, stoneObject.getLocation().getY());
			if (stoneObject.isOnScreen()) {
				while (inventory.getCount(stoneID) < 1 && !walkToStone
						&& inGame() && !dieScript && !switchingRole) {
					if (!doModelAction(stoneObject, "Take-stone")) {
						try {
							stoneObject.doHover();
						} catch (NullPointerException ignored) {
							if (dmode) {
								log("stoneObject doHover fail, NullPointerException");
							}
						} catch (ArrayIndexOutOfBoundsException ignored) {
							if (dmode) {
								log("stoneObject doHover fail, ArrayIndexOutOfBoundsException");
							}
						}
						if (!doModelAction(stoneObject, "Take-stone")) {
							walkToStone = true;
						} else {
							sleep(1000);
							while (getMyPlayer().isMoving()) {
								sleep(1);
							}
						}
					} else {
						sleep(1000);
						while (getMyPlayer().isMoving()) {
							sleep(1);
						}
					}
				}
			} else {
				walkToStone = true;
			}
			if (walkToStone && !switchingRole) {
				while (calc.distanceTo(nextToStoneTile) > 2 && inGame()) {
					walking.walkTileOnScreen(nextToStoneTile);
					sleep(random(500, 1000));
				}
			}
			while (inventory.getCount(stoneID) < 1 && inGame() && !dieScript
					&& !switchingRole) {
				if (!doModelAction(stoneObject, "Take-stone")) {
					try {
						stoneObject.doHover();
					} catch (NullPointerException ignored) {
						if (dmode) {
							log("stoneObject doHover fail, NullPointerException");
						}
					} catch (ArrayIndexOutOfBoundsException ignored) {
						if (dmode) {
							log("stoneObject doHover fail, ArrayIndexOutOfBoundsException");
						}
					}
					if (!doModelAction(stoneObject, "Take-stone")) {
						walkToStone = true;
					} else {
						sleep(1000);
						while (getMyPlayer().isMoving()) {
							sleep(1);
						}
					}
				} else {
					sleep(1000);
					while (getMyPlayer().isMoving()) {
						sleep(1);
					}
				}
			}
			if (switchingRole) {
				switchRole();
			}
		} catch (NullPointerException e) {
			if (dmode) {
				log("Could not get stone");
			}
		}
	}

	public void getStoneSwitch() {
		status = "Getting Stone...";
		try {
			boolean walkToStone = false;
			stoneObject = objects.getNearest(30143);
			stoneObject.getLocation();
			RSTile nextToStoneTile = new RSTile(stoneObject.getLocation()
					.getX() + 1, stoneObject.getLocation().getY());
			if (stoneObject.isOnScreen()) {
				while (inventory.getCount(stoneID) < 1 && !walkToStone
						&& inGame() && !dieScript) {
					if (!stoneObject.doClick()) {
						try {
							stoneObject.doHover();
						} catch (NullPointerException ignored) {
							if (dmode) {
								log("stoneObject doHover fail, NullPointerException");
							}
						} catch (ArrayIndexOutOfBoundsException ignored) {
							if (dmode) {
								log("stoneObject doHover fail, ArrayIndexOutOfBoundsException");
							}
						}
						if (!stoneObject.doClick()) {
							walkToStone = true;
						} else {
							sleep(1500);
						}
					} else {
						sleep(1500);
					}
				}
			} else {
				walkToStone = true;
			}
			if (walkToStone) {
				while (calc.distanceTo(stoneObject.getLocation()) > 3
						&& inGame()) {
					walking.walkTileOnScreen(nextToStoneTile);
					sleep(random(500, 1000));
				}
			}
			while (inventory.getCount(stoneID) < 1 && inGame()) {
				try {
					stoneObject.doHover();
					stoneObject.doClick();
				} catch (NullPointerException ignored) {
					if (dmode) {
						log("stoneObject doHover/doClick fail, NullPointerException");
					}
				} catch (ArrayIndexOutOfBoundsException ignored) {
					if (dmode) {
						log("stoneObject doHover/doClick fail, ArrayIndexOutOfBoundsException");
					}
				}
				sleep(2000);
			}
		} catch (NullPointerException e) {
			if (dmode) {
				log("Could not get stone");
			}
		}
	}

	public void wieldStone() {
		try {
			status = "Wielding Stone...";
			int tries = 0;
			while (!inventory.getItem(stoneID).doAction("Wield") && tries < 10
					&& inGame() && !dieScript) {
				tries++;
				mouse.moveSlightly();
			}
		} catch (NullPointerException ignored) {
		}
	}

	public void setSpell() {
		status = "Setting Spell...";
		game.openTab(Game.TAB_MAGIC);
		sleep(50);
		try {
			if (!setCombatSpells
					&& magic.getInterface().getIndex() == MODERN_SPELLBOOK) {
				int tries = 0;
				while (!setCombatSpells && tries < 15) {
					setCombatSpells = magic.getInterface()
							.getComponent(Magic.INTERFACE_SORT_BY_COMBAT)
							.doAction("");
					tries++;
				}
			} else {
				setCombatSpells = true;
			}
		} catch (NullPointerException e) {
			setCombatSpells = true;
		}

		if (magicSpell.equals("Auto Highest (F2P)")) {
			autoCastHighestF2P();
		} else if (magicSpell.equals("Auto Highest (P2P)")) {
			autoCastHighestP2P();
		} else if (magicSpell.equals("Fire Surge")) {
			autoCast(Magic.SPELL_FIRE_SURGE);
		} else if (magicSpell.equals("Earth Surge")) {
			autoCast(Magic.SPELL_EARTH_SURGE);
		} else if (magicSpell.equals("Water Surge")) {
			autoCast(Magic.SPELL_WATER_SURGE);
		} else if (magicSpell.equals("Wind Surge")) {
			autoCast(Magic.SPELL_WIND_SURGE);
		} else if (magicSpell.equals("Fire Wave")) {
			autoCast(Magic.SPELL_FIRE_WAVE);
		} else if (magicSpell.equals("Earth Wave")) {
			autoCast(Magic.SPELL_EARTH_WAVE);
		} else if (magicSpell.equals("Water Wave")) {
			autoCast(Magic.SPELL_WATER_WAVE);
		} else if (magicSpell.equals("Wind Wave")) {
			autoCast(Magic.SPELL_WIND_WAVE);
		} else if (magicSpell.equals("Fire Blast")) {
			autoCast(Magic.SPELL_FIRE_BLAST);
		} else if (magicSpell.equals("Earth Blast")) {
			autoCast(Magic.SPELL_EARTH_BLAST);
		} else if (magicSpell.equals("Water Blast")) {
			autoCast(Magic.SPELL_WATER_BLAST);
		} else if (magicSpell.equals("Wind Blast")) {
			autoCast(Magic.SPELL_WIND_BLAST);
		} else if (magicSpell.equals("Fire Bolt")) {
			autoCast(Magic.SPELL_FIRE_BOLT);
		} else if (magicSpell.equals("Earth Bolt")) {
			autoCast(Magic.SPELL_EARTH_BOLT);
		} else if (magicSpell.equals("Water Bolt")) {
			autoCast(Magic.SPELL_WATER_BOLT);
		} else if (magicSpell.equals("Wind Bolt")) {
			autoCast(Magic.SPELL_WIND_BOLT);
		} else if (magicSpell.equals("Fire Strike")) {
			autoCast(Magic.SPELL_FIRE_STRIKE);
		} else if (magicSpell.equals("Earth Strike")) {
			autoCast(Magic.SPELL_EARTH_STRIKE);
		} else if (magicSpell.equals("Water Strike")) {
			autoCast(Magic.SPELL_WATER_STRIKE);
		} else if (magicSpell.equals("Wind Strike")) {
			autoCast(Magic.SPELL_WIND_STRIKE);
		} else if (magicSpell.equals("Wind Rush")) {
			autoCast(SPELL_WIND_RUSH);
		} else if (magicSpell.equals("Ice Barrage")) {
			autoCast(Magic.SPELL_ICE_BARRAGE);
		} else if (magicSpell.equals("Blood Barrage")) {
			autoCast(Magic.SPELL_BLOOD_BARRAGE);
		} else if (magicSpell.equals("Shadow Barrage")) {
			autoCast(Magic.SPELL_SHADOW_BARRAGE);
		} else if (magicSpell.equals("Smoke Barrage")) {
			autoCast(Magic.SPELL_SMOKE_BARRAGE);
		} else if (magicSpell.equals("Ice Blitz")) {
			autoCast(Magic.SPELL_ICE_BLITZ);
		} else if (magicSpell.equals("Blood Blitz")) {
			autoCast(Magic.SPELL_BLOOD_BLITZ);
		} else if (magicSpell.equals("Shadow Blitz")) {
			autoCast(Magic.SPELL_SHADOW_BLITZ);
		} else if (magicSpell.equals("Smoke Blitz")) {
			autoCast(Magic.SPELL_SMOKE_BLITZ);
		} else if (magicSpell.equals("Ice Burst")) {
			autoCast(Magic.SPELL_ICE_BURST);
		} else if (magicSpell.equals("Blood Burst")) {
			autoCast(Magic.SPELL_BLOOD_BURST);
		} else if (magicSpell.equals("Shadow Burst")) {
			autoCast(Magic.SPELL_SHADOW_BURST);
		} else if (magicSpell.equals("Smoke Burst")) {
			autoCast(Magic.SPELL_SMOKE_BURST);
		} else if (magicSpell.equals("Ice Rush")) {
			autoCast(Magic.SPELL_ICE_RUSH);
		} else if (magicSpell.equals("Blood Rush")) {
			autoCast(Magic.SPELL_BLOOD_RUSH);
		} else if (magicSpell.equals("Shadow Rush")) {
			autoCast(Magic.SPELL_SHADOW_RUSH);
		} else if (magicSpell.equals("Smoke Rush")) {
			autoCast(Magic.SPELL_SMOKE_RUSH);
		} else if (magicSpell.equals("Miasmic Barrage")) {
			autoCast(Magic.SPELL_MIASMIC_BARRAGE);
		} else if (magicSpell.equals("Miasmic Blitz")) {
			autoCast(Magic.SPELL_MIASMIC_BLITZ);
		} else if (magicSpell.equals("Miasmic Burst")) {
			autoCast(Magic.SPELL_MIASMIC_BURST);
		} else if (magicSpell.equals("Miasmic Rush")) {
			autoCast(Magic.SPELL_MIASMIC_RUSH);
		}
	}

	public boolean autoCastHighestF2P() {
		int ML = skills.getCurrentLevel(Skills.MAGIC);
		if (ML >= 59) {
			return autoCast(Magic.SPELL_FIRE_BLAST);
		} else if (ML >= 53) {
			return autoCast(Magic.SPELL_EARTH_BLAST);
		} else if (ML >= 47) {
			return autoCast(Magic.SPELL_WATER_BLAST);
		} else if (ML >= 41) {
			return autoCast(Magic.SPELL_WIND_BLAST);
		} else if (ML >= 35) {
			return autoCast(Magic.SPELL_FIRE_BOLT);
		} else if (ML >= 29) {
			return autoCast(Magic.SPELL_EARTH_BOLT);
		} else if (ML >= 23) {
			return autoCast(Magic.SPELL_WATER_BOLT);
		} else if (ML >= 17) {
			return autoCast(Magic.SPELL_WIND_BOLT);
		} else if (ML >= 13) {
			return autoCast(Magic.SPELL_FIRE_STRIKE);
		} else if (ML >= 9) {
			return autoCast(Magic.SPELL_EARTH_STRIKE);
		} else if (ML >= 5) {
			return autoCast(Magic.SPELL_WATER_STRIKE);
		} else {
			return autoCast(Magic.SPELL_WIND_STRIKE);
		}
	}

	public boolean autoCastHighestP2P() {
		final int ML = skills.getCurrentLevel(Skills.MAGIC);
		final int spellbook = magic.getInterface().getIndex();

		if (spellbook == MODERN_SPELLBOOK) {
			if (ML >= 95) {
				return autoCast(Magic.SPELL_FIRE_SURGE);
			} else if (ML >= 90) {
				return autoCast(Magic.SPELL_EARTH_SURGE);
			} else if (ML >= 85) {
				return autoCast(Magic.SPELL_WATER_SURGE);
			} else if (ML >= 81) {
				return autoCast(Magic.SPELL_WIND_SURGE);
			} else if (ML >= 75) {
				return autoCast(Magic.SPELL_FIRE_WAVE);
			} else if (ML >= 70) {
				return autoCast(Magic.SPELL_EARTH_WAVE);
			} else if (ML >= 65) {
				return autoCast(Magic.SPELL_WATER_WAVE);
			} else if (ML >= 62) {
				return autoCast(Magic.SPELL_WIND_WAVE);
			} else if (ML >= 59) {
				return autoCast(Magic.SPELL_FIRE_BLAST);
			} else if (ML >= 53) {
				return autoCast(Magic.SPELL_EARTH_BLAST);
			} else if (ML >= 47) {
				return autoCast(Magic.SPELL_WATER_BLAST);
			} else if (ML >= 41) {
				return autoCast(Magic.SPELL_WIND_BLAST);
			} else if (ML >= 35) {
				return autoCast(Magic.SPELL_FIRE_BOLT);
			} else if (ML >= 29) {
				return autoCast(Magic.SPELL_EARTH_BOLT);
			} else if (ML >= 23) {
				return autoCast(Magic.SPELL_WATER_BOLT);
			} else if (ML >= 17) {
				return autoCast(Magic.SPELL_WIND_BOLT);
			} else if (ML >= 13) {
				return autoCast(Magic.SPELL_FIRE_STRIKE);
			} else if (ML >= 9) {
				return autoCast(Magic.SPELL_EARTH_STRIKE);
			} else if (ML >= 5) {
				return autoCast(Magic.SPELL_WATER_STRIKE);
			} else {
				return autoCast(Magic.SPELL_WIND_STRIKE);
			}
		} else if (spellbook == ANCIENT_SPELLBOOK) {
			if (ML >= 94) {
				return autoCast(Magic.SPELL_ICE_BARRAGE);
			} else if (ML >= 92) {
				return autoCast(Magic.SPELL_BLOOD_BARRAGE);
			} else if (ML >= 88) {
				return autoCast(Magic.SPELL_SHADOW_BARRAGE);
			} else if (ML >= 86) {
				return autoCast(Magic.SPELL_SMOKE_BARRAGE);
			} else if (ML >= 82) {
				return autoCast(Magic.SPELL_ICE_BLITZ);
			} else if (ML >= 80) {
				return autoCast(Magic.SPELL_BLOOD_BLITZ);
			} else if (ML >= 76) {
				return autoCast(Magic.SPELL_SHADOW_BLITZ);
			} else if (ML >= 74) {
				return autoCast(Magic.SPELL_SMOKE_BLITZ);
			} else if (ML >= 70) {
				return autoCast(Magic.SPELL_ICE_BURST);
			} else if (ML >= 68) {
				return autoCast(Magic.SPELL_BLOOD_BURST);
			} else if (ML >= 64) {
				return autoCast(Magic.SPELL_SHADOW_BURST);
			} else if (ML >= 62) {
				return autoCast(Magic.SPELL_SMOKE_BURST);
			} else if (ML >= 58) {
				return autoCast(Magic.SPELL_ICE_RUSH);
			} else if (ML >= 56) {
				return autoCast(Magic.SPELL_BLOOD_RUSH);
			} else if (ML >= 52) {
				return autoCast(Magic.SPELL_SHADOW_RUSH);
			} else if (ML >= 50) {
				return autoCast(Magic.SPELL_SMOKE_RUSH);
			} else {
				return false; // Player does not have requirement to use the
								// ancient spellbook they have? Impossible.
			}
		} else {
			dlog("Spellbook error - GET OFF LUNAR!");
		}
		return false;
	}

	public boolean autoCast(int spell) {
		try {
			int i = 0;
			int thisRound = round;
			if (spell != 90) {
				while (!magic.autoCastSpell(spell) && i < 10 && inGame()
						&& round == thisRound) {
					sleep(200);
					i++;
				}
			} else {
				while (!magic.autoCastSpell(89) && i < 10 && inGame()
						&& round == thisRound) {
					sleep(200);
					i++;
				}
			}
			if (i == 10) {
				return false;
			} else {
				return true;
			}
		} catch (NullPointerException e) {
			return false;
		}
	}

	public boolean attacking() {
		boolean well = false;
		try {
			if (getMyPlayer().getInteracting() instanceof RSPlayer && inGame()) {
				if (!getPlayer().isMoving()) {
					if (getPlayer().isInCombat()) {
						well = true;
					}
				} else {
					well = true;
				}
			}
		} catch (NullPointerException e) {
			well = false;
		}
		return well;
	}

	public void attack() {
		status = "Attacking...";
		try {
			mouse.setSpeed(3);
			mouse.move(getPlayerScreenLocThrow());
			mouse.move(getPlayerScreenLocThrow());
			if (calc.tileOnScreen(getPlayerLocThrow())) {
				mouse.click(false);
				sleep(350, 500);
				mouse.setSpeed(10);
				if (menu.isOpen()) {
					if (menu.contains("Attack " + playerName())) {
						if (menu.doAction("Attack " + playerName())) {
							sleep(random(1700, 2200));
							mouse.moveSlightly();
						} else {
							while (menu.isOpen()) {
								mouse.moveSlightly();
							}
						}
					} else {
						while (menu.isOpen()) {
							mouse.moveSlightly();
						}
					}
				}
			}
			mouse.setSpeed(mouseSpeed);
			if (switchingRole) {
				switchRole();
			}
		} catch (NullPointerException ignored) {
			dlog("Attack Player failed");
		}
		mouse.setSpeed(mouseSpeed);
	}

	public boolean atCenter() {
		if (calc.distanceTo(centerTile) > 1) {
			return false;
		}
		return true;
	}

	public boolean opponentAtCenter() throws NullPointerException {
		if (calc.distanceBetween(getPlayerLocThrow(), centerTile) > 4) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	public void search() {
		status = "Searching for Opponent...";
		antiBanTime = false;
		RSTile nextTile = new RSTile(searchTilesX[searchTileOn],
				searchTilesY[searchTileOn]);
		RSTile[] tilePath = walking.findPath(nextTile);
		walking.walkPathMM(tilePath);
		sleep(random(200, 700));
		if (getMyPlayer().isMoving() || searchTileOn == 0) {
			s.reset();
		}
		if (!s.isRunning()) {
			dlog("been standing still too long, cancelling search");
			timerStarted = false;
			searchTileOn = 0;
			searching = false;
			antiBanTime = true;
		}
		if (calc.distanceBetween(getMyPlayer().getLocation(), nextTile) < 11.0) {
			searchTileOn++;
		}
		if (searchTileOn == 8) {
			timerStarted = false;
			searchTileOn = 0;
			searching = false;
			antiBanTime = true;
		}
	}

	public void useTeleOrb() {
		status = "Using TeleOrb...";
		if (inventory.getCount(false, teleorbID) > 0) {
			RSItem tItem = inventory.getItem(teleorbID);
			boolean clicked = false;
			while (!clicked && inGame()) {
				if (tItem.doClick(true)) {
					clicked = true;
					sleep(random(3000, 3500));
				} else {
					sleep(random(50, 200));
				}
			}
		} else {
			if (dmode) {
				log("No TeleOrb to use");
			}
		}
	}

	public void setSearchTiles() {
		int x = getMyPlayer().getLocation().getX();
		int y = getMyPlayer().getLocation().getY();
		if (x >= 1652 && x <= 1675 && y >= 5654 && y <= 5668) {
			spawnArea = 0;
		} else if (x >= 1627 && x <= 1646 && y >= 5662 && y <= 5682) {
			spawnArea = 1;
		} else if (x >= 1620 && x <= 1637 && y >= 5685 && y <= 5710) {
			spawnArea = 2;
		} else if (x >= 1629 && x <= 1649 && y >= 5713 && y <= 5731) {
			spawnArea = 3;
		} else if (x >= 1651 && x <= 1675 && y >= 5720 && y <= 5735) {
			spawnArea = 4;
		} else if (x >= 1677 && x <= 1695 && y >= 5706 && y <= 5723) {
			spawnArea = 5;
		} else if (x >= 1687 && x <= 1708 && y >= 5686 && y <= 5704) {
			spawnArea = 6;
		} else if (x >= 1678 && x <= 1706 && y >= 5665 && y <= 5684) {
			spawnArea = 7;
		}
		if (dmode) {
			log("Spawn Area: " + Integer.toString(spawnArea));
		}
		// Set search tiles X
		int i = 0;
		int origNum = spawnArea;
		while (i < 8) {
			if (origNum > 7) {
				origNum = 0;
			}
			searchTilesX[i] = searchTilesXOrig[origNum];
			i++;
			origNum++;
		}
		// Set search tiles Y
		int i2 = 0;
		int origNum2 = spawnArea;
		while (i2 < 8) {
			if (origNum2 > 7) {
				origNum2 = 0;
			}
			searchTilesY[i2] = searchTilesYOrig[origNum2];
			i2++;
			origNum2++;
		}
		if (dmode) {
			log("Search Tiles Set...");
		}
	}

	public void startup() throws NullPointerException {
		if (dmode) {
			log("Starting up...");
			String logLoc = Integer
					.toString(getMyPlayer().getLocation().getX())
					+ ", "
					+ Integer.toString(getMyPlayer().getLocation().getY());
			log("Spawn pos: " + logLoc);
		}
		if (round == 1) {
			if (hunted) {
				getStone();
				if (hunted) {
					wieldStone();
					if (teleorb.equals("hunted")) {
						useTeleOrb();
					} else if (teleorb.equals("random")) {
						if (tRandom > 500) {
							useTeleOrb();
						}
					}
				}
			} else {
				setSearchTiles();
				if (FSGame)
					currentStyle = firstStyle;
				setupStyleEquipment();
				setupStyle();
				if (currentStyle.equals("Magic"))
					setSpell();
				if (teleorb.equals("hunting")) {
					useTeleOrb();
				} else if (teleorb.equals("random")) {
					if (tRandom <= 500) {
						useTeleOrb();
					}
				}
			}
			startedUp = true;
		} else if (round == 2) {
			if (hunted2) {
				getStone();
				if (hunted2) {
					wieldStone();
					if (teleorb.equals("hunted")) {
						useTeleOrb();
					} else if (teleorb.equals("random")) {
						if (tRandom > 500) {
							useTeleOrb();
						}
					}
				}
			} else {
				setSearchTiles();
				if (FSGame) {
					currentStyle = firstStyle;
				}
				setupStyleEquipment();
				setupStyle();
				if (currentStyle.equals("Magic")) {
					setSpell();
				}
				if (teleorb.equals("hunting")) {
					useTeleOrb();
				} else if (teleorb.equals("random")) {
					if (tRandom <= 500) {
						useTeleOrb();
					}
				}
			}
			startedUp2 = true;
		}
	}

	public boolean playerGoneThroughPortal() {
		boolean found = false;
		final int x = lastKnownLoc.getX();
		final int y = lastKnownLoc.getY();
		final RSArea nwHouse = new RSArea(1649, 5701, 1653, 5705);
		final RSArea neHouse = new RSArea(1665, 5702, 1669, 5706);
		final RSArea seHouse = new RSArea(1674, 5686, 1678, 5690);
		final RSArea swHouse = new RSArea(1654, 5681, 1658, 5685);

		if (nwHouse.contains(x, y) || neHouse.contains(x, y)
				|| seHouse.contains(x, y) || swHouse.contains(x, y)) {
			found = true;
		}
		return found;
	}

	public int getItemIDT(String name) {
		RSItem[] items = inventory.getItems();
		int slot = -1;
		for (int i = 0; i < items.length; i++) {
			if (items[i].getName().contains(name)) {
				slot = items[i].getID();
			}
		}
		return slot;
	}

	@SuppressWarnings("deprecation")
	public void bankUBIs() {
		status = "Banking Disallowed Items";
		if (dmode) {
			log("Disallowed item in inventory, banking...");
		}
		int itemIndex = UBIString.indexOf("arena:") + 7;
		String itemString = UBIString.substring(itemIndex);
		if (dmode) {
			log("item name string: \"" + itemString + "\"");
		}
		int itemID = getItemIDT(itemString);
		if (dmode) {
			log("ID is " + itemID);
		}
		RSTile bankerCloseTile = new RSTile(1704, 5599);
		RSTile[] bankerCloseTilePath = walking.findPath(bankerCloseTile);
		while (calc.distanceTo(bankerCloseTile) > 3) {
			if (dmode) {
				log("Walking to tile close to banker");
			}
			walking.walkPathMM(bankerCloseTilePath);
			sleep(random(1000, 2000));
		}
		RSNPC banker = npcs.getNearest(7605);
		while (!banker.isOnScreen() || banker.isMoving()) {
			if (dmode) {
				log("Walking to banker location");
			}
			RSTile bankerTile = banker.getLocation();
			walking.walkPathMM(walking.findPath(bankerTile));
			sleep(random(500, 1500));
		}
		while (!bank.isOpen()) {
			if (dmode) {
				log("clicking on banker...");
			}
			banker.doAction("Bank Banker");
			sleep(1500);
		}
		while (inventory.getCount(itemID) != 0) {
			try {
				bank.deposit(itemID, 0);
				sleep(500);
				if (inventory.getCount(itemID) > 0) {
					sleep(random(800, 1100));
				}
			} catch (NullPointerException ignored) {
				if (dmode) {
					log("bad item null exception...");
				}
			}
		}
		if (bank.isOpen()) {
			if (random(1, 2) == 2) {
				bank.close();
			}
		}
		bankingUBIs = false;
	}

	@SuppressWarnings("deprecation")
	public void bankTokensM() {
		status = "Banking Tokens";
		int tokensToBank = inventory.getCount(true, tokenID);
		while (inventory.getCount(true, tokenID) > 0) {
			RSNPC banker = npcs.getNearest(7605);
			while (!banker.isOnScreen() || banker.isMoving()) {
				RSTile bankerTile = banker.getLocation();
				walking.walkPathMM(walking.findPath(bankerTile));
				sleep(random(500, 1500));
			}
			while (!bank.isOpen()) {
				banker.doAction("Bank Banker");
				sleep(1500);
			}
			while (inventory.getCount(true, tokenID) > 0) {
				try {
					bank.deposit(tokenID, 0);
					sleep(500);
					if (inventory.getCount(true, tokenID) > 0) {
						sleep(random(800, 1100));
					}
				} catch (NullPointerException ignored) {
				}
			}
			bankedTokens += tokensToBank;
			if (bank.isOpen()) {
				if (random(100, 200) > 100) {
					bank.close();
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public int loop() {
		try {
			if (!initialised) {
				initialiseImages();
				if (loginWait) {
					status = "Waiting for Login...";
					sleep(4000, 5000);
				}
				loginWait = false;
				initialise();
			}
			if (dieScript) {
				log("Stopping Script...");
				stopScript();
			} else {
				if (camera.getPitch() < 100) {
					while (camera.getPitch() < 100) {
						char keyUp = KeyEvent.VK_UP;
						keyboard.pressKey(keyUp);
						sleep(2000);
						keyboard.releaseKey(keyUp);
					}
				}
				if (game.getCurrentTab() != Game.TAB_INVENTORY) {
					game.openTab(Game.TAB_INVENTORY);
					mouse.moveSlightly();
				}
				if (switchingRole) {
					switchRole();
				}
				if (walking.getEnergy() > random(35, 50)) {
					walking.setRun(true);
					sleep(1000);
				}
				if (walking.getEnergy() < 1) {
					walking.setRun(false);
					sleep(1000);
				}
				if (useAntiBan) {
					if (antiBanTime) {
						antiBan();
					}
				}
				try {
					currentTokens = inventory.getCount(true, tokenID);
				} catch (NullPointerException ignored) {
				}

				if (inLobby()) {
					overallStatus = "In Lobby";
					canDrawInfo = false;
					canDrawTheirTile = false;
					antiBanTime = true;
					walkedOnce = false;
					if (justStarted) {
						if (randomGameSwitch) {
							if (!nextRandomSwitchSet) {
								int properRandomHigh = random(1000, 5000);
								int aThousand = 1000;
								double smaller = (double) properRandomHigh
										/ (double) aThousand;
								int properRandom = (int) Math.round(smaller);
								if (dmode) {
									log("Next style switch: " + properRandom);
								}
								nextRandomSwitch = properRandom;
								nextRandomSwitchSet = true;
							}
						}
						sleep(250);
						try {
							startRating = Integer
									.parseInt(interfaces
											.get(731)
											.getComponent(7)
											.getText()
											.substring(
													interfaces.get(731)
															.getComponent(7)
															.getText()
															.indexOf(":") + 2));
							currentRating = Integer
									.parseInt(interfaces
											.get(731)
											.getComponent(7)
											.getText()
											.substring(
													interfaces.get(731)
															.getComponent(7)
															.getText()
															.indexOf(":") + 2));
						} catch (StringIndexOutOfBoundsException e) {
							if (dmode) {
								e.printStackTrace();
							}
						} catch (NumberFormatException e) {
							if (dmode) {
								e.printStackTrace();
							}
						}
						gainedRating = 0;
						justStarted = false;
					}
					if (justPlayed) {
						if (dmode) {
							log("In Lobby");
						}
						if (everyGameSwitch) {
							setRandomStyle();
						} else if (randomGameSwitch) {
							if (nextRandomSwitch == gamesPlayed) {
								setRandomStyle();
								mouse.moveSlightly();
								nextRandomSwitchSet = false;
							}
							if (!nextRandomSwitchSet) {
								int properRandomHigh = random(1000, 5000);
								int aThousand = 1000;
								double smaller = (double) properRandomHigh
										/ (double) aThousand;
								int properRandom = (int) Math.round(smaller);
								if (dmode) {
									log("Next style switch: " + properRandom);
								}
								nextRandomSwitch = gamesPlayed + properRandom;
								nextRandomSwitchSet = true;
							}
						}
						sleep(1500, 2500);
						justPlayed = false;
						gotRatingInfo = false;
					}
					if (!gotRatingInfo) {
						try {
							currentRating = Integer
									.parseInt(interfaces
											.get(731)
											.getComponent(7)
											.getText()
											.substring(
													interfaces.get(731)
															.getComponent(7)
															.getText()
															.indexOf(":") + 2));
							gainedRating = currentRating - startRating;
							gotRatingInfo = true;
							dlog("Got Rating Stats");
						} catch (StringIndexOutOfBoundsException e) {
							if (dmode) {
								e.printStackTrace();
							}
						} catch (NumberFormatException e) {
							if (dmode) {
								e.printStackTrace();
							}
						}
					}
					if (bankTokens) {
						if (inventory.getCount(true, tokenID) > tokensBeforeBank) {
							bankTokensM();
							mouse.moveSlightly();
						}
					}
					while (inventory.getCount(NAIDs) > 0) {
						status = "Banking Disallowed Items";
						RSNPC banker = npcs.getNearest(7605);
						while (!banker.isOnScreen() || banker.isMoving()) {
							RSTile bankerTile = banker.getLocation();
							walking.walkPathMM(walking.findPath(bankerTile));
							sleep(random(500, 1500));
						}
						while (!bank.isOpen()) {
							banker.doAction("Bank Banker");
							mouse.moveSlightly();
							sleep(1500);
						}
						for (int i = 0; i < NAIDs.length; i++) {
							while (inventory.getCount(NAIDs[i]) > 0) {
								try {
									bank.deposit(NAIDs[i], 0);
									mouse.moveSlightly();
									sleep(500);
									if (inventory.getCount(NAIDs[i]) > 0) {
										sleep(random(800, 1100));
									}
								} catch (NullPointerException ignored) {
								}
							}
						}
						if (bank.isOpen()) {
							if (random(1000, 2000) > 1000) {
								bank.close();
							}
						}
					}
					if (!calc.tileOnScreen(entrance)) {
						status = "Walking to Waiting Room...";
						if (!dieScript) {
							walking.walkPathMM(walking.findPath(entranceBefore));
							mouse.moveSlightly();
							sleep(random(600, 1000));
						}
					} else {
						if (bank.isOpen()) {
							bank.close();
						}
						if (!getMyPlayer().isMoving()) {
							status = "Going into Waiting Room...";
							mouse.setSpeed(3);
							RSObject entranceObject = objects.getNearest(30224);
							mouse.move(calc.tileToScreen(entrance, 1));
							int timesTried = 0;
							while (!entranceObject.doAction("Go-through")
									&& timesTried < 10 && inLobby()) {
								sleep(400);
								timesTried++;
							}
							mouse.setSpeed(mouseSpeed);
							mouse.moveSlightly();
							int millisWaiting = 0;
							while (inLobby() && !bankingUBIs) {
								sleep(10);
								millisWaiting += 10;
								if (millisWaiting > 1000) {
									turnCamera();
									break;
								}
							}
						}
						if (bankingUBIs) {
							bankUBIs();
							mouse.moveSlightly();
						}
					}
				}

				if (inWaitingRoom()) {
					if (!walkedOnce) {
						prevHd = -1;
						prevHg = -1;
						canDrawInfo = false;
						canDrawTheirTile = false;
						roundOneHunted = false;
						startedUp = false;
						startedUp2 = false;
						gone = false;
						gone2 = false;
						hunted = false;
						hunted2 = false;
						setVarsRound1 = false;
						setVarsRound2 = false;
						round = 1;
						timerStarted = false;
						searching = false;
						searchTileOn = 0;
						walkedOnce = true;
						antiBanTime = true;
						canGoFailed = 0;
						overrideGoReceived = false;
						meleeTimerStarted = false;
						stoneHovered = false;
						lastKnownHealth = 100;
						lastKnownLoc = new RSTile(-1, -1);
						timerToUse = 1;
						lastPrayerSet = 0;
						lastAttackPrayerSet = 0;
						lastSkinPrayerSet = -1;
						tRandom = random(0, 1000);
						overallStatus = "In Waiting Room";
						status = "Waiting...";
						sleep(random(825, 1225));
						walking.walkTileOnScreen(new RSTile(random(1650, 1652),
								random(5598, 5604)));
						sleep(500);
					}
				}

				// THE GAME
				// YOU JUST LOST THE GAME
				// =P 123456789

				if (inGame()) {
					justPlayed = true;
					if (round == 1) {
						if (!setVarsRound1) {
							goTimer.reset();
							prevHg = interfaces.get(730).getComponent(26)
									.getBoundsArrayIndex();
							prevHd = interfaces.get(730).getComponent(27)
									.getBoundsArrayIndex();
							setVarsRound1 = true;
						}
						if (!gone) {
							overallStatus = "In Game";
							status = "Waiting for 'Go!'";
							antiBanTime = true;
							canDrawInfo = false;
							canDrawTheirTile = false;
							if (!goReceived && !overrideGoReceived) {
								if (!goTimer.isRunning()) {
									overrideGoReceived = true;
								}
							}
							if (goReceived || overrideGoReceived) {
								if (overrideGoReceived) {
									if (dmode) {
										log("goReceived overriden, going...");
									}
								} else {
									if (dmode) {
										log("goReceived true, going...");
									}
								}
								gone = true;
								goReceived = false;
								overrideGoReceived = false;
							}
						}
						if (gone) {
							if (!startedUp) {
								prevHg = interfaces.get(730).getComponent(26)
										.getBoundsArrayIndex();
								prevHd = interfaces.get(730).getComponent(27)
										.getBoundsArrayIndex();
								if (prevHg != -1) {
									hunted = false;
									roundOneHunted = false;
									overallStatus = "Round 1, Hunting";
									if (dmode) {
										log("Hunting, Round 1");
									}
								} else {
									hunted = true;
									roundOneHunted = true;
									overallStatus = "Round 1, Being Hunted";
									if (dmode) {
										log("Being Hunted, Round 1");
									}
								}
								canGoFailed = 0;
								overrideGoReceived = false;
								if (dmode) {
									log("Round 1 Starting Up...");
								}
								antiBanTime = false;
								if (hitSwitch && !hunted) {
									setNextHitSwitch();
								}
								startup();
							}
							if (hunted) {
								if (!atCenter()) {
									status = "Running to Center";
									antiBanTime = false;
									RSTile[] tilePath = walking
											.findPath(centerTile);
									walking.walkPathMM(tilePath);
									sleep(random(600, 1200));
								} else {
									status = "At Center";
									antiBanTime = true;
								}
								if (playerLocKnown()) {
									canDrawInfo = true;
									canDrawTheirTile = true;
									if (getMyPlayer().isInCombat()) {
										setPrayer();
										if (playerLocKnown())
											setDefenceStyle();
										setSkinPrayer();
										if (useQuickPrayers) {
											if (quickPrayersLong
													.equals("When Being Hunted")) {
												setQuickPrayers(true);
											}
										}
									} else {
										turnOffPrayer();
									}
								} else {
									canDrawInfo = false;
									canDrawTheirTile = false;
								}
								if (getMyPlayer().getHPPercent() < 50) {
									if (useBandages) {
										if (inventory.getCount(false,
												bandagesID) > 0) {
											status = "Using Bandages...";
											dlog("Using Bandages...");
											doActionExtreme(bandagesID, "Heal");
										}
									}
								}
								if (getMyPlayer().getHPPercent() < 1) {
									status = "Dying...";
									antiBanTime = false;
									if (dmode) {
										log("Died");
									}
									stoneHovered = false;
									lastPrayerSet = 0;
									lastAttackPrayerSet = 0;
									lastSkinPrayerSet = -1;
									lastKnownHealth = 100;
									round = 2;
									lastKnownLoc = new RSTile(-1, -1);
									timerToUse = 1;
									canDrawTheirTile = false;
									meleeTimerStarted = false;
									while (playerLocKnown()) {
										timerStarted = false;
										sleep(1);
									}
								}
							} else {
								if (playerLocKnown()) {
									canDrawInfo = true;
									canDrawTheirTile = true;
									timerStarted = false;
									searchTileOn = 0;
									searching = false;
									if (calc.distanceBetween(getMyPlayer()
											.getLocation(), getPlayerLoc()) > 4.3
											&& !attacking()) {
										status = "Running to Opponent...";
										antiBanTime = false;
										walking.walkPathMM(walking
												.findPath(getPlayerLoc()));
										if (currentStyle.equals("Magic")
												|| currentStyle.equals("Range")) {
											try {
												if (calc.distanceBetween(
														getMyPlayer()
																.getLocation(),
														getPlayerLocThrow()) < 15.0) {
													char keyDown = KeyEvent.VK_DOWN;
													keyboard.pressKey(keyDown);
													camera.turnToCharacter(getPlayerThrow());
													int waitTime = 0;
													while (camera.getPitch() > 5
															&& waitTime < 1000) {
														waitTime++;
														sleep(1);
													}
													keyboard.releaseKey(keyDown);
													if (calc.tileOnScreen(getPlayerLocThrow())) {
														attack();
													}
												}
											} catch (NullPointerException ignored) {
											}
										}
										int sleepTime = 0;
										while (playerLocKnown()
												&& calc.distanceBetween(
														getMyPlayer()
																.getLocation(),
														getPlayerLoc()) > 4.3
												&& sleepTime < random(300, 400)) {
											sleep(1);
											sleepTime++;
										}
									}
									if (playerLocKnown()
											&& (calc.distanceBetween(
													getMyPlayer().getLocation(),
													getPlayerLoc()) <= 4.3 || attacking())) {
										if (getPlayer().getHPPercent() > 0) {
											if (!attacking()) {
												antiBanTime = false;
												attack();
											} else {
												if (currentStyle
														.equals("Melee")) { // MELEE
																			// GLITCH
																			// BUG
																			// FIX
																			// 12345
													if (calc.distanceTo(getPlayerLoc()) > 1
															&& !getMyPlayer()
																	.isMoving()
															&& !getPlayer()
																	.isMoving()) {
														if (meleeSafeSpotSwitch) {
															setRandomStyle();
															if (currentStyle
																	.equals("Magic")) {
																setSpell();
															}
														} else {
															if (!meleeTimerStarted) {
																meleeTimer
																		.reset();
																meleeTimerStarted = true;
															} else {
																if (!meleeTimer
																		.isRunning()) {
																	attack();
																}
																meleeTimerStarted = false;
															}
														}
													} else {
														meleeTimerStarted = false;
													}
												} else {
													meleeTimerStarted = false;
												}
												setAttackPrayer();
												if (useQuickPrayers) {
													if (quickPrayersLong
															.equals("When Hunting")) {
														setQuickPrayers(true);
													}
												}
												if (currentStyle
														.equals("Magic")) {
													if (!maging()) {
														setSpell();
													}
												}
												if (hitSwitch) {
													if (hits >= nextHitSwitch) {
														hits = 0;
														setNextHitSwitch();
														setRandomStyle();
														if (currentStyle
																.equals("Magic")) {
															setSpell();
														}
													}
												}
												status = "Attacking Opponent";
												antiBanTime = true;
											}
										} else {
											status = "Opponent Killed";
											if (dmode) {
												log("Opponent Killed");
											}
											round = 2;
											while (playerLocKnown()) {
												lastKnownHealth = 100;
												antiBanTime = false;
												timerStarted = false;
												searchTileOn = 0;
												searching = false;
												stoneHovered = false;
												lastPrayerSet = 0;
												lastAttackPrayerSet = 0;
												lastSkinPrayerSet = -1;
												sleep(1);
												lastKnownLoc = new RSTile(-1,
														-1);
												timerToUse = 1;
												meleeTimerStarted = false;
											}
										}
									}
								} else {
									canDrawInfo = false;
									canDrawTheirTile = false;
									setQuickPrayers(false);
									if (playerGoneThroughPortal()) {
										timerToUse = 2;
									} else {
										timerToUse = 1;
									}
									if (!atCenter() && !searching) {
										status = "Running to Center";
										antiBanTime = false;
										RSTile[] tilePath = walking
												.findPath(centerTile);
										walking.walkPathMM(tilePath);
										sleep(random(600, 1200));
										timerStarted = false;
									} else {
										status = "Waiting at Center";
										if (search) {
											if (!searching) {
												if (!timerStarted) {
													if (timerToUse == 1) {
														t.reset();
													} else {
														t2.reset();
													}
													timerStarted = true;
												}
												if (timerToUse == 1) {
													if (!t.isRunning()) {
														if (dmode) {
															log("Searching for player...");
														}
														searching = true;
														timerStarted = false;
													}
												} else {
													if (!t2.isRunning()) {
														if (dmode) {
															log("Searching for player...");
														}
														searching = true;
														timerStarted = false;
													}
												}
											} else {
												search();
											}
										}
										antiBanTime = true;
									}
								}
								setupStyleEquipment();
							}
						}
					} else if (round == 2) {
						if (!setVarsRound2) {
							goTimer.reset();
							setVarsRound2 = true;
						}
						if (!gone2) {
							overallStatus = "In Game";
							status = "Waiting for 'Go!'";
							canDrawInfo = false;
							canDrawTheirTile = false;
							antiBanTime = true;
							if (!goReceived && !overrideGoReceived) {
								if (!goTimer.isRunning()) {
									overrideGoReceived = true;
								}
							}
							if (goReceived || overrideGoReceived) {
								if (overrideGoReceived) {
									if (dmode) {
										log("goReceived overriden, going");
									}
								} else {
									if (dmode) {
										log("goReceived is true, going");
									}
								}
								gone2 = true;
								goReceived = false;
								overrideGoReceived = false;
							}
						}
						if (gone2) {
							if (!startedUp2) {
								if (roundOneHunted) {
									hunted2 = false;
									overallStatus = "Round 2, Hunting";
									if (dmode) {
										log("Hunting, Round 2");
									}
								} else {
									hunted2 = true;
									overallStatus = "Round 2, Being Hunted";
									if (dmode) {
										log("Being Hunted, Round 2");
									}
								}
								canGoFailed = 0;
								overrideGoReceived = false;
								if (dmode) {
									log("Round 2 Starting Up...");
								}
								antiBanTime = false;
								if (hitSwitch && !hunted2) {
									setNextHitSwitch();
								}
								startup();
							}
							if (hunted2) {
								if (!atCenter()) {
									status = "Running to Center...";
									antiBanTime = false;
									RSTile[] tilePath = walking
											.findPath(centerTile);
									walking.walkPathMM(tilePath);
									sleep(random(600, 1200));
								} else {
									status = "At Center";
									antiBanTime = true;
								}
								if (playerLocKnown()) {
									canDrawInfo = true;
									canDrawTheirTile = true;
									if (getMyPlayer().isInCombat()) {
										setPrayer();
										if (playerLocKnown())
											setDefenceStyle();
										setSkinPrayer();
										if (useQuickPrayers) {
											if (quickPrayersLong
													.equals("When Being Hunted")) {
												setQuickPrayers(true);
											}
										}
									} else {
										turnOffPrayer();
									}
								} else {
									canDrawInfo = false;
									canDrawTheirTile = false;
								}
								if (getMyPlayer().getHPPercent() < 50) {
									if (useBandages) {
										if (inventory.getCount(false,
												bandagesID) > 0) {
											status = "Using Bandages...";
											dlog("Using Bandages...");
											doActionExtreme(bandagesID, "Heal");
										}
									}
								}
								if (getMyPlayer().getHPPercent() < 1) {
									status = "Dying...";
									if (dmode) {
										log("Died");
									}
									canDrawTheirTile = false;
									justPlayed = true;
									sleep(3500);
								}
							} else {
								if (playerLocKnown()) {
									canDrawInfo = true;
									canDrawTheirTile = true;
									timerStarted = false;
									searchTileOn = 0;
									searching = false;
									if (calc.distanceBetween(getMyPlayer()
											.getLocation(), getPlayerLoc()) > 4.3
											&& !attacking()) {
										status = "Running to Opponent";
										antiBanTime = false;
										walking.walkPathMM(walking
												.findPath(getPlayerLoc()));
										if (currentStyle.equals("Magic")
												|| currentStyle.equals("Range")) {
											try {
												if (calc.distanceBetween(
														getMyPlayer()
																.getLocation(),
														getPlayerLocThrow()) < 15.0) {
													char keyDown = KeyEvent.VK_DOWN;
													keyboard.pressKey(keyDown);
													camera.turnToCharacter(getPlayerThrow());
													int waitTime = 0;
													while (camera.getPitch() > 5
															&& waitTime < 1000) {
														waitTime++;
														sleep(1);
													}
													keyboard.releaseKey(keyDown);
													if (calc.tileOnScreen(getPlayerLocThrow())) {
														attack();
													}
												}
											} catch (NullPointerException ignored) {
											}
										}
										int sleepTime = 0;
										while (playerLocKnown()
												&& calc.distanceBetween(
														getMyPlayer()
																.getLocation(),
														getPlayerLoc()) > 4.3
												&& sleepTime < random(300, 400)) {
											sleep(1);
											sleepTime++;
										}
									}
									if (playerLocKnown()
											&& (calc.distanceBetween(
													getMyPlayer().getLocation(),
													getPlayerLoc()) <= 4.3 || attacking())) {
										if (getPlayer().getHPPercent() > 0) {
											if (!attacking()) {
												antiBanTime = false;
												attack();
											} else {
												if (currentStyle
														.equals("Melee")) { // MELEE
																			// GLITCH
																			// BUG
																			// FIX
																			// 12345
													if (calc.distanceTo(getPlayerLoc()) > 1
															&& !getMyPlayer()
																	.isMoving()
															&& !getPlayer()
																	.isMoving()) {
														if (meleeSafeSpotSwitch) {
															setRandomStyle();
															if (currentStyle
																	.equals("Magic")) {
																setSpell();
															}
														} else {
															if (!meleeTimerStarted) {
																meleeTimer
																		.reset();
																meleeTimerStarted = true;
															} else {
																if (!meleeTimer
																		.isRunning()) {
																	attack();
																}
																meleeTimerStarted = false;
															}
														}
													} else {
														meleeTimerStarted = false;
													}
												} else {
													meleeTimerStarted = false;
												}
												setAttackPrayer();
												if (useQuickPrayers) {
													if (quickPrayersLong
															.equals("When Hunting")) {
														setQuickPrayers(true);
													}
												}
												if (currentStyle
														.equals("Magic")) {
													if (!maging()) {
														setSpell();
													}
												}
												if (hitSwitch) {
													if (hits >= nextHitSwitch) {
														hits = 0;
														setNextHitSwitch();
														setRandomStyle();
														if (currentStyle
																.equals("Magic")) {
															setSpell();
														}
													}
												}
												status = "Attacking Opponent";
												antiBanTime = true;
											}
										} else {
											status = "Opponent Killed";
											if (dmode) {
												log("Opponent Killed");
											}
											justPlayed = true;
											sleep(3500);
										}
									}
								} else {
									canDrawInfo = false;
									canDrawTheirTile = false;
									setQuickPrayers(false);
									if (playerGoneThroughPortal()) {
										timerToUse = 2;
									} else {
										timerToUse = 1;
									}
									if (!atCenter() && !searching) {
										status = "Running to Center...";
										antiBanTime = false;
										RSTile[] tilePath = walking
												.findPath(centerTile);
										walking.walkPathMM(tilePath);
										sleep(random(600, 1200));
										timerStarted = false;
									} else {
										status = "Waiting at Center";
										if (search) {
											if (!searching) {
												if (!timerStarted) {
													if (timerToUse == 1) {
														t.reset();
													} else {
														t2.reset();
													}
													timerStarted = true;
												}
												if (timerToUse == 1) {
													if (!t.isRunning()) {
														if (dmode) {
															log("Searching for player...");
														}
														searching = true;
														timerStarted = false;
													}
												} else {
													if (!t2.isRunning()) {
														if (dmode) {
															log("Searching for player...");
														}
														searching = true;
														timerStarted = false;
													}
												}
											} else {
												search();
											}
										}
										antiBanTime = true;
									}
								}
								setupStyleEquipment();
							}
						}
					}
				}
			}
		} catch (NullPointerException e) {
			dlog("MAIN LOOP NULLPOINTER");
			if (dmode)
				e.printStackTrace();
		}
		return 0;
	}

	public double round2(double value) {
		double result = value * 100;
		int resultint = (int) result;
		double resultintdub = resultint;
		double finalresult = resultintdub / 100;
		return finalresult;
	}

	public double getRatio() {
		double ratio = 666;
		double gamesWonDouble = gamesWon;
		double gamesLostDouble = gamesLost;
		if (gamesWon != 0 && gamesLost != 0) {
			ratio = round2(gamesWonDouble / gamesLostDouble);
		} else {
			if (gamesLost == 0 && gamesWon != 0) {
				ratio = gamesWonDouble;
			} else if (gamesLost != 0 && gamesWon == 0) {
				ratio = 0;
			} else if (gamesLost == 0 && gamesWon == 0) {
				ratio = 0;
			}
		}
		return ratio;
	}

	public double getRatio2() {
		double ratio = 666;
		double gamesWonDouble = gamesWon;
		double gamesPlayedDouble = gamesPlayed;
		if (gamesWon != 0 && gamesPlayed != 0) {
			ratio = round2(gamesWonDouble / gamesPlayedDouble);
		} else {
			if (gamesPlayed == 0 && gamesWon != 0) {
				ratio = gamesWonDouble;
			} else if (gamesPlayed != 0 && gamesWon == 0) {
				ratio = 0;
			} else if (gamesPlayed == 0 && gamesWon == 0) {
				ratio = 0;
			}
		}
		return ratio;
	}

	@Override
	public void onFinish() {
		status = "Stopping Script...";
		log("Gained " + tokensGained + " Tokens in " + hours + ":" + minutes
				+ ":" + seconds + " - " + tokensPerHour + " Tokens Per Hour");
		log("Games Played: " + gamesPlayed + ", Won: " + gamesWon + ", Lost: "
				+ gamesLost + ", W/L Ratio: " + getRatio());
		log("Thank You for using FOGRunner by TerraBubble");
	}

	@Override
	public void messageReceived(MessageEvent e) {
		final String messageString = e.getMessage();
		String messageStringLower = messageString.toLowerCase();

		switch (e.getID()) {
		case MessageEvent.MESSAGE_SERVER: {
			if (messageString.contains("You've just advanced")) {
				if (screenshots) {
					env.saveScreenshot(true);
					log("Congrats on level up! Screenshot taken =D");
					sleep(random(1500, 2500));
					if (interfaces.canContinue()) {
						interfaces.clickContinue();
					}
				}
			}
			if (messageString.contains("current role")) {
				if (inGame()) {
					if (!switchingRole) {
						switchingRole = true;
					}
				}
			}
			if (messageStringLower.contains("you won")) {
				gamesPlayed++;
				gamesWon++;
				String chargesString = messageStringLower.substring(
						messageStringLower.indexOf("you had") + 8,
						messageStringLower.indexOf("charges") - 1);
				String chargesStringO = messageStringLower.substring(
						messageStringLower.indexOf("opponent had") + 13,
						messageStringLower.lastIndexOf("."));
				if (dmode) {
					log("Strings: You: " + chargesString + " Opponent: "
							+ chargesStringO);
				}
				int charges = Integer.parseInt(chargesString);
				int chargesO = Integer.parseInt(chargesStringO);
				if (dmode) {
					log("Ints: You: " + charges + " Opponent: " + chargesO);
				}
				totalCharges += charges;
				totalChargesO += chargesO;
				averageCharges = (totalCharges / gamesPlayed);
				averageChargesOpponent = (totalChargesO / gamesPlayed);
			}
			if (messageStringLower.contains("you lost")) {
				gamesPlayed++;
				gamesLost++;
				String chargesString = messageStringLower.substring(
						messageStringLower.indexOf("you had") + 8,
						messageStringLower.indexOf("charges") - 1);
				String chargesStringO = messageStringLower.substring(
						messageStringLower.indexOf("opponent had") + 13,
						messageStringLower.lastIndexOf("."));
				if (dmode) {
					log("Strings: You: " + chargesString + " Opponent: "
							+ chargesStringO);
				}
				int charges = Integer.parseInt(chargesString);
				int chargesO = Integer.parseInt(chargesStringO);
				if (dmode) {
					log("Ints: You: " + charges + " Opponent: " + chargesO);
				}
				totalCharges += charges;
				totalChargesO += chargesO;
				averageCharges = (totalCharges / gamesPlayed);
				averageChargesOpponent = (totalChargesO / gamesPlayed);
			}

			if (messageString.contains("The following item is not allowed")) {
				if (inLobby()) {
					bankingUBIs = true;
					UBIString = messageString;
				}
			}
		}
		case MessageEvent.MESSAGE_CHAT: {
			if (e.getSender().equals(getMyPlayer().getName())) {
				if (messageStringLower.contains("1")) {
					goReceived = true;
					status = "'1' Received";
				}
			}
		}
		}
	}

	@SuppressWarnings("deprecation")
	public void switchRole() {
		status = "Switching Role...";
		if (round == 1) {
			if (hunted) {
				hunted = false;
				roundOneHunted = false;
			} else {
				hunted = true;
				roundOneHunted = true;
			}
		} else if (round == 2) {
			if (hunted2) {
				hunted2 = false;
			} else {
				hunted2 = true;
			}
		}

		boolean goEdge = true;
		try {
			RSObject stoneObjectTest = objects.getNearest(30143);
			if (stoneObjectTest != null) {
				goEdge = false;
			}
		} catch (NullPointerException e) {
		}

		if (goEdge) {
			RSTile edgeTile = new RSTile(1693, 5675);
			while (calc.distanceTo(edgeTile) > 5 && inGame()) {
				walking.walkPathMM(walking.findPath(edgeTile));
			}
		} else {
			RSObject stoneObjectS = objects.getNearest(30143);
			RSTile nextToStoneTileS = new RSTile(stoneObjectS.getLocation()
					.getX() + 1, stoneObjectS.getLocation().getY());
			while (calc.distanceTo(nextToStoneTileS) > 5 && inGame()) {
				walking.walkPathMM(walking.findPath(nextToStoneTileS));
				sleep(random(500, 1000));
			}
		}
		// Setup Again:

		if (dmode) {
			log("Switching Role...");
		}
		if (round == 1) {
			if (hunted) {
				getStoneSwitch();
				wieldStone();
				if (teleorb.equals("hunted")) {
					useTeleOrb();
				} else if (teleorb.equals("random")) {
					if (tRandom > 500) {
						useTeleOrb();
					}
				}
			} else {
				setSearchTiles();
				setupStyleEquipment();
				setupStyle();
				if (currentStyle.equals("Magic")) {
					setSpell();
				}
				if (teleorb.equals("hunting")) {
					useTeleOrb();
				} else if (teleorb.equals("random")) {
					if (tRandom <= 500) {
						useTeleOrb();
					}
				}
			}
		} else if (round == 2) {
			if (hunted2) {
				getStoneSwitch();
				wieldStone();
				if (teleorb.equals("hunted")) {
					useTeleOrb();
				} else if (teleorb.equals("random")) {
					if (tRandom > 500) {
						useTeleOrb();
					}
				}
			} else {
				setSearchTiles();
				setupStyleEquipment();
				setupStyle();
				if (currentStyle.equals("Magic")) {
					setSpell();
				}
				if (teleorb.equals("hunting")) {
					useTeleOrb();
				} else if (teleorb.equals("random")) {
					if (tRandom <= 500) {
						useTeleOrb();
					}
				}
			}
		}
		switchingRole = false;
	}

	// ATTACK
	public void clearItemListArrays() {

		meleeListIDsAdded[0] = -1;
		meleeListIDsAdded[1] = -1;
		meleeListIDsAdded[2] = -1;
		meleeListIDsAdded[3] = -1;
		meleeListIDsAdded[4] = -1;
		meleeListIDsAdded[5] = -1;
		meleeListIDsAdded[6] = -1;
		meleeListIDsAdded[7] = -1;
		meleeListIDsAdded[8] = -1;
		meleeListIDsAdded[9] = -1;
		meleeListIDsAdded[10] = -1;

		magicListIDsAdded[0] = -1;
		magicListIDsAdded[1] = -1;
		magicListIDsAdded[2] = -1;
		magicListIDsAdded[3] = -1;
		magicListIDsAdded[4] = -1;
		magicListIDsAdded[5] = -1;
		magicListIDsAdded[6] = -1;
		magicListIDsAdded[7] = -1;
		magicListIDsAdded[8] = -1;
		magicListIDsAdded[9] = -1;
		magicListIDsAdded[10] = -1;

		rangeListIDsAdded[0] = -1;
		rangeListIDsAdded[1] = -1;
		rangeListIDsAdded[2] = -1;
		rangeListIDsAdded[3] = -1;
		rangeListIDsAdded[4] = -1;
		rangeListIDsAdded[5] = -1;
		rangeListIDsAdded[6] = -1;
		rangeListIDsAdded[7] = -1;
		rangeListIDsAdded[8] = -1;
		rangeListIDsAdded[9] = -1;
		rangeListIDsAdded[10] = -1;
	}

	// DEFENCE
	public void clearItemListArraysDefence() {

		meleeListIDsAddedDefence[0] = -1;
		meleeListIDsAddedDefence[1] = -1;
		meleeListIDsAddedDefence[2] = -1;
		meleeListIDsAddedDefence[3] = -1;
		meleeListIDsAddedDefence[4] = -1;
		meleeListIDsAddedDefence[5] = -1;
		meleeListIDsAddedDefence[6] = -1;
		meleeListIDsAddedDefence[7] = -1;
		meleeListIDsAddedDefence[8] = -1;
		meleeListIDsAddedDefence[9] = -1;
		meleeListIDsAddedDefence[10] = -1;

		magicListIDsAddedDefence[0] = -1;
		magicListIDsAddedDefence[1] = -1;
		magicListIDsAddedDefence[2] = -1;
		magicListIDsAddedDefence[3] = -1;
		magicListIDsAddedDefence[4] = -1;
		magicListIDsAddedDefence[5] = -1;
		magicListIDsAddedDefence[6] = -1;
		magicListIDsAddedDefence[7] = -1;
		magicListIDsAddedDefence[8] = -1;
		magicListIDsAddedDefence[9] = -1;
		magicListIDsAddedDefence[10] = -1;

		rangeListIDsAddedDefence[0] = -1;
		rangeListIDsAddedDefence[1] = -1;
		rangeListIDsAddedDefence[2] = -1;
		rangeListIDsAddedDefence[3] = -1;
		rangeListIDsAddedDefence[4] = -1;
		rangeListIDsAddedDefence[5] = -1;
		rangeListIDsAddedDefence[6] = -1;
		rangeListIDsAddedDefence[7] = -1;
		rangeListIDsAddedDefence[8] = -1;
		rangeListIDsAddedDefence[9] = -1;
		rangeListIDsAddedDefence[10] = -1;
	}

	public boolean maging() {
		int anim = getMyPlayer().getAnimation();
		if (anim == 14221 // Staff + Wind
				|| anim == 14220 // Staff + Water
				|| anim == 14222 // Staff + Earth
				|| anim == 14223 // Staff + Fire
				|| anim == 10546 // No Staff + Wind
				|| anim == 10542 // No Staff + Water
				|| anim == 14209 // No Staff + Earth
				|| anim == 2791 // No Staff + Fire
				|| anim == 1978 // Ancient Blitz or Rush Spell
				|| anim == 1979 // Ancient Burst or Barrage Spell
				|| anim == -1 // Idle
		) {
			return true;
		}
		return false;
	}

	public void setDefenceStyle() {
		boolean canSetup = false;
		int anim = -1;
		try {
			anim = getPlayerThrow().getAnimation();
		} catch (NullPointerException e) {
			anim = -1;
		}
		if (anim == 14221 // Staff + Wind
				|| anim == 14220 // Staff + Water
				|| anim == 14222 // Staff + Earth
				|| anim == 14223 // Staff + Fire
				|| anim == 10546 // No Staff + Wind
				|| anim == 10542 // No Staff + Water
				|| anim == 14209 // No Staff + Earth
				|| anim == 2791 // No Staff + Fire
				|| anim == 1978 // Ancient Blitz or Rush Spell
				|| anim == 1979 // Ancient Burst or Barrage Spell
		) {
			if (magicActiveDefence) {
				currentStyleDefence = "Magic";
				canSetup = true;
			}
		} else if (anim == 426 // All bows and all arrows
				|| anim == 4230 // Crossbow
				|| anim == 2075 // Some crossbow-like thing not sure...
		) {
			if (rangeActiveDefence) {
				currentStyleDefence = "Range";
				canSetup = true;
			}
		} else if (anim != -1 // Idle
				&& anim != 9012 // Using TeleOrb
				&& anim != 710 // Other Magic Spells to lower stats/freeze
				&& anim != 716 && anim != 717 && anim != 718
				&& anim != 1161
				&& anim != 1163 && anim != 1164
				&& anim != 1165
				&& anim != 12806 // Defencive Stance, Possibly Ancient Prayer of
									// Opponent
		) {
			if (meleeActiveDefence) {
				currentStyleDefence = "Melee";
				canSetup = true;
			}
		}
		if (canSetup) {
			setupStyleDefence();
			game.openTab(Game.TAB_INVENTORY);
		}
	}

	public void setQuickPrayers(boolean on) {
		if (skills.getCurrentLevel(Skills.PRAYER) > 0) {
			if (on) {
				if (interfaces.get(Game.INTERFACE_PRAYER_ORB).getComponent(1)
						.containsAction("Turn quick prayers on")) {
					int thisRound = round;
					int tries = 0;
					boolean prayerSet = false;
					while (!prayerSet && tries < 10 && inGame()
							&& round == thisRound) {
						if (interfaces.get(Game.INTERFACE_PRAYER_ORB)
								.getComponent(1)
								.doAction("Turn quick prayers on")) {
							prayerSet = true;
						}
						tries++;
					}
				}
			} else if (!on) {
				if (interfaces.get(Game.INTERFACE_PRAYER_ORB).getComponent(1)
						.containsAction("Turn prayers off")) {
					int thisRound = round;
					int tries = 0;
					boolean prayerSet = false;
					while (!prayerSet && tries < 10 && inGame()
							&& round == thisRound) {
						if (interfaces.get(Game.INTERFACE_PRAYER_ORB)
								.getComponent(1).doAction("Turn prayers off")) {
							prayerSet = true;
						}
						tries++;
					}
				}
			}
		}
	}

	public void turnOffPrayer() {
		if (lastPrayerSet == 17) {
			status = "Turning Off Prayer...";
			game.openTab(Game.TAB_PRAYER);
			mouse.move(645, 340);
			sleep(101);
			if (setPrayer(17, false)) {
				lastPrayerSet = 0;
			}
		} else if (lastPrayerSet == 18) {
			status = "Turning Off Prayer...";
			game.openTab(Game.TAB_PRAYER);
			mouse.move(683, 340);
			sleep(101);
			if (setPrayer(18, false)) {
				lastPrayerSet = 0;
			}
		} else if (lastPrayerSet == 19) {
			status = "Turning Off Prayer...";
			game.openTab(Game.TAB_PRAYER);
			mouse.move(718, 340);
			sleep(101);
			if (setPrayer(19, false)) {
				lastPrayerSet = 0;
			}
		}
		game.openTab(Game.TAB_INVENTORY);
	}

	public void turnOffAttackPrayer() {
		if (lastAttackPrayerSet == 1 || lastAttackPrayerSet == 2
				|| lastAttackPrayerSet == 3 || lastAttackPrayerSet == 4
				|| lastAttackPrayerSet == 6 || lastAttackPrayerSet == 7
				|| lastAttackPrayerSet == 11 || lastAttackPrayerSet == 12
				|| lastAttackPrayerSet == 14 || lastAttackPrayerSet == 15
				|| lastAttackPrayerSet == 20 || lastAttackPrayerSet == 21) {
			status = "Turning Off Attack Prayer...";
			game.openTab(Game.TAB_PRAYER);
			int thisRound = round;
			int tries = 0;
			boolean prayerSet = false;
			while (!prayerSet && tries < 10 && inGame() && round == thisRound) {
				if (setPrayer(lastAttackPrayerSet, false)) {
					prayerSet = true;
					lastAttackPrayerSet = 0;
				}
				tries++;
			}
			game.openTab(Game.TAB_INVENTORY);
		}
	}

	public void setAttackPrayer() {
		prayerlvl = skills.getRealLevel(Skills.PRAYER);
		if (useAttackPrayers) {
			// prayer 1 = Burst of Strength +5% Strength
			// prayer 2 = Clarity of Thought +5% Attack
			// prayer 3 = Sharp Eye +5% Ranged
			// prayer 4 = Mystic Will +5% Magic
			// prayer 6 = Superhuman Strength +10% Strength
			// prayer 7 = Improved Reflexes +10% Attack
			// prayer 11 = Hawk Eye +10% Ranged
			// prayer 12 = Mystic Lore +10% Magic
			// prayer 14 = Ultimate Strength +15% Strength
			// prayer 15 = Incredible Reflexes +15% Attack
			// prayer 20 = Eagle Eye +15% Ranged
			// prayer 21 = Mystic Might +15% Magic
			if (currentStyle.equals("Melee")) {
				if (meleeAttackStyle.equals("Strength EXP")) {
					if (prayerlvl >= 31) {
						if (lastAttackPrayerSet != 14) {
							status = "Setting Attack Prayer...";
							int thisRound = round;
							game.openTab(Game.TAB_PRAYER);
							mouse.setSpeed(mouseSpeed);
							sleep(101);
							int tries = 0;
							boolean prayerSet = false;
							while (!prayerSet && tries < 10 && inGame()
									&& round == thisRound) {
								if (setPrayer(14, true)) {
									prayerSet = true;
									lastAttackPrayerSet = 14;
								}
								tries++;
							}
							mouse.setSpeed(mouseSpeed);
						}
					} else if (prayerlvl >= 13) {
						if (lastAttackPrayerSet != 6) {
							status = "Setting Attack Prayer...";
							int thisRound = round;
							game.openTab(Game.TAB_PRAYER);
							mouse.setSpeed(mouseSpeed);
							sleep(101);
							int tries = 0;
							boolean prayerSet = false;
							while (!prayerSet && tries < 10 && inGame()
									&& round == thisRound) {
								if (setPrayer(6, true)) {
									prayerSet = true;
									lastAttackPrayerSet = 6;
								}
								tries++;
							}
							mouse.setSpeed(mouseSpeed);
						}
					} else if (prayerlvl >= 4) {
						if (lastAttackPrayerSet != 1) {
							status = "Setting Attack Prayer...";
							int thisRound = round;
							game.openTab(Game.TAB_PRAYER);
							mouse.setSpeed(mouseSpeed);
							sleep(101);
							int tries = 0;
							boolean prayerSet = false;
							while (!prayerSet && tries < 10 && inGame()
									&& round == thisRound) {
								if (setPrayer(1, true)) {
									prayerSet = true;
									lastAttackPrayerSet = 1;
								}
								tries++;
							}
							mouse.setSpeed(mouseSpeed);
						}
					}
				} else if (meleeAttackStyle.equals("Attack EXP")) {
					if (prayerlvl >= 34) {
						if (lastAttackPrayerSet != 15) {
							status = "Setting Attack Prayer...";
							int thisRound = round;
							game.openTab(Game.TAB_PRAYER);
							mouse.setSpeed(mouseSpeed);
							sleep(101);
							int tries = 0;
							boolean prayerSet = false;
							while (!prayerSet && tries < 10 && inGame()
									&& round == thisRound) {
								if (setPrayer(15, true)) {
									prayerSet = true;
									lastAttackPrayerSet = 15;
								}
								tries++;
							}
							mouse.setSpeed(mouseSpeed);
						}
					} else if (prayerlvl >= 16) {
						if (lastAttackPrayerSet != 7) {
							status = "Setting Attack Prayer...";
							int thisRound = round;
							game.openTab(Game.TAB_PRAYER);
							mouse.setSpeed(mouseSpeed);
							sleep(101);
							int tries = 0;
							boolean prayerSet = false;
							while (!prayerSet && tries < 10 && inGame()
									&& round == thisRound) {
								if (setPrayer(7, true)) {
									prayerSet = true;
									lastAttackPrayerSet = 7;
								}
								tries++;
							}
							mouse.setSpeed(mouseSpeed);
						}
					} else if (prayerlvl >= 7) {
						if (lastAttackPrayerSet != 2) {
							status = "Setting Attack Prayer...";
							int thisRound = round;
							game.openTab(Game.TAB_PRAYER);
							mouse.setSpeed(mouseSpeed);
							sleep(101);
							int tries = 0;
							boolean prayerSet = false;
							while (!prayerSet && tries < 10 && inGame()
									&& round == thisRound) {
								if (setPrayer(2, true)) {
									prayerSet = true;
									lastAttackPrayerSet = 2;
								}
								tries++;
							}
							mouse.setSpeed(mouseSpeed);
						}
					}
				}
			} else if (currentStyle.equals("Magic")) {
				if (prayerlvl >= 45) {
					if (lastAttackPrayerSet != 21) {
						status = "Setting Attack Prayer...";
						int thisRound = round;
						game.openTab(Game.TAB_PRAYER);
						mouse.setSpeed(mouseSpeed);
						sleep(101);
						int tries = 0;
						boolean prayerSet = false;
						while (!prayerSet && tries < 10 && inGame()
								&& round == thisRound) {
							if (setPrayer(21, true)) {
								prayerSet = true;
								lastAttackPrayerSet = 21;
							}
							tries++;
						}
						mouse.setSpeed(mouseSpeed);
					}
				} else if (prayerlvl >= 27) {
					if (lastAttackPrayerSet != 12) {
						status = "Setting Attack Prayer...";
						int thisRound = round;
						game.openTab(Game.TAB_PRAYER);
						mouse.setSpeed(mouseSpeed);
						sleep(101);
						int tries = 0;
						boolean prayerSet = false;
						while (!prayerSet && tries < 10 && inGame()
								&& round == thisRound) {
							if (setPrayer(12, true)) {
								prayerSet = true;
								lastAttackPrayerSet = 12;
							}
							tries++;
						}
						mouse.setSpeed(mouseSpeed);
					}
				} else if (prayerlvl >= 9) {
					if (lastAttackPrayerSet != 4) {
						status = "Setting Attack Prayer...";
						int thisRound = round;
						game.openTab(Game.TAB_PRAYER);
						mouse.setSpeed(mouseSpeed);
						sleep(101);
						int tries = 0;
						boolean prayerSet = false;
						while (!prayerSet && tries < 10 && inGame()
								&& round == thisRound) {
							if (setPrayer(4, true)) {
								prayerSet = true;
								lastAttackPrayerSet = 4;
							}
							tries++;
						}
						mouse.setSpeed(mouseSpeed);
					}
				}
			} else if (currentStyle.equals("Range")) {
				if (prayerlvl >= 44) {
					if (lastAttackPrayerSet != 20) {
						status = "Setting Attack Prayer...";
						int thisRound = round;
						game.openTab(Game.TAB_PRAYER);
						mouse.setSpeed(mouseSpeed);
						sleep(101);
						int tries = 0;
						boolean prayerSet = false;
						while (!prayerSet && tries < 10 && inGame()
								&& round == thisRound) {
							if (setPrayer(20, true)) {
								prayerSet = true;
								lastAttackPrayerSet = 20;
							}
							tries++;
						}
						mouse.setSpeed(mouseSpeed);
					}
				} else if (prayerlvl >= 26) {
					if (lastAttackPrayerSet != 11) {
						status = "Setting Attack Prayer...";
						int thisRound = round;
						game.openTab(Game.TAB_PRAYER);
						mouse.setSpeed(mouseSpeed);
						sleep(101);
						int tries = 0;
						boolean prayerSet = false;
						while (!prayerSet && tries < 10 && inGame()
								&& round == thisRound) {
							if (setPrayer(11, true)) {
								prayerSet = true;
								lastAttackPrayerSet = 11;
							}
							tries++;
						}
						mouse.setSpeed(mouseSpeed);
					}
				} else if (prayerlvl >= 8) {
					if (lastAttackPrayerSet != 3) {
						status = "Setting Attack Prayer...";
						int thisRound = round;
						game.openTab(Game.TAB_PRAYER);
						mouse.setSpeed(mouseSpeed);
						sleep(101);
						int tries = 0;
						boolean prayerSet = false;
						while (!prayerSet && tries < 10 && inGame()
								&& round == thisRound) {
							if (setPrayer(3, true)) {
								prayerSet = true;
								lastAttackPrayerSet = 3;
							}
							tries++;
						}
						mouse.setSpeed(mouseSpeed);
					}
				}
			}
		}
	}

	public void setPrayer() {
		prayerlvl = skills.getRealLevel(Skills.PRAYER);
		if (usePrayers) {
			// prayer 17 = Protect from Magic
			// prayer 18 = Protect from Missiles
			// prayer 19 = Protect from Melee
			int anim;
			try {
				anim = getPlayer().getAnimation();
			} catch (NullPointerException e) {
				anim = -1;
			}
			if (anim == 14221 // Staff + Wind
					|| anim == 14220 // Staff + Water
					|| anim == 14222 // Staff + Earth
					|| anim == 14223 // Staff + Fire
					|| anim == 10546 // No Staff + Wind
					|| anim == 10542 // No Staff + Water
					|| anim == 14209 // No Staff + Earth
					|| anim == 2791 // No Staff + Fire
					|| anim == 1978 // Ancient Blitz or Rush Spell
					|| anim == 1979 // Ancient Burst or Barrage Spell
			) {
				if (prayerlvl >= 37 && lastPrayerSet != 17) {
					status = "Setting Prayer...";
					game.openTab(Game.TAB_PRAYER);
					mouse.setSpeed(mouseSpeed);
					mouse.move(645, 340);
					sleep(101);
					int thisRound = round;
					int tries = 0;
					boolean prayerSet = false;
					while (!prayerSet && tries < 10 && inGame()
							&& round == thisRound) {
						if (setPrayer(17, true)) {
							prayerSet = true;
							lastPrayerSet = 17;
						}
						tries++;
					}
					mouse.setSpeed(mouseSpeed);
				}
			} else if (anim == 426 // All bows and all arrows
					|| anim == 4230 // Crossbow
					|| anim == 2075 // Some crossbow-like thing not sure...
			) {
				if (prayerlvl >= 40 && lastPrayerSet != 18) {
					status = "Setting Prayer...";
					game.openTab(Game.TAB_PRAYER);
					mouse.setSpeed(mouseSpeed);
					sleep(101);
					mouse.move(683, 340);
					int thisRound = round;
					int tries = 0;
					boolean prayerSet = false;
					while (!prayerSet && tries < 10 && inGame()
							&& round == thisRound) {
						if (setPrayer(18, true)) {
							prayerSet = true;
							lastPrayerSet = 18;
						}
						tries++;
					}
					mouse.setSpeed(mouseSpeed);
				}
			} else if (anim != -1 // Idle
					&& anim != 9012 // Using TeleOrb
					&& anim != 710 // Magic Spells to lower stats/freeze
					&& anim != 716 && anim != 717
					&& anim != 718
					&& anim != 1161 && anim != 1163
					&& anim != 1164
					&& anim != 1165 && anim != 12806 // Defencive Stance,
														// Possibly Ancient
														// Prayer of Opponent
			) {
				if (prayerlvl >= 43 && lastPrayerSet != 19) {
					status = "Setting Prayer...";
					game.openTab(Game.TAB_PRAYER);
					mouse.setSpeed(mouseSpeed);
					sleep(101);
					mouse.move(718, 340);
					int thisRound = round;
					int tries = 0;
					boolean prayerSet = false;
					while (!prayerSet && tries < 10 && inGame()
							&& round == thisRound) {
						if (setPrayer(19, true)) {
							prayerSet = true;
							lastPrayerSet = 19;
						}
						tries++;
					}
					mouse.setSpeed(mouseSpeed);
				}
			}
		}
	}

	public void setSkinPrayer() {
		prayerlvl = skills.getRealLevel(Skills.PRAYER);
		if (useSkinPrayers) {
			// prayer 0 = Thick Skin +5% Defence
			// prayer 5 = Rock Skin +10% Defence
			// prayer 13 = Steel Skin +15% Defence
			if (prayerlvl >= 28) {
				if (lastSkinPrayerSet != 13) {
					status = "Setting Defence Prayer...";
					game.openTab(Game.TAB_PRAYER);
					mouse.setSpeed(mouseSpeed);
					sleep(101);
					int thisRound = round;
					int tries = 0;
					boolean prayerSet = false;
					while (!prayerSet && tries < 10 && inGame()
							&& round == thisRound) {
						if (setPrayer(13, true)) {
							prayerSet = true;
							lastSkinPrayerSet = 13;
						}
						tries++;
					}
					mouse.setSpeed(mouseSpeed);
				}
			} else if (prayerlvl >= 10) {
				if (lastSkinPrayerSet != 5) {
					status = "Setting Defence Prayer...";
					game.openTab(Game.TAB_PRAYER);
					mouse.setSpeed(mouseSpeed);
					sleep(101);
					int thisRound = round;
					int tries = 0;
					boolean prayerSet = false;
					while (!prayerSet && tries < 10 && inGame()
							&& round == thisRound) {
						if (setPrayer(5, true)) {
							prayerSet = true;
							lastSkinPrayerSet = 5;
						}
						tries++;
					}
					mouse.setSpeed(mouseSpeed);
				}
			} else {
				if (lastSkinPrayerSet != 0) {
					status = "Setting Defence Prayer...";
					game.openTab(Game.TAB_PRAYER);
					mouse.setSpeed(mouseSpeed);
					sleep(101);
					int thisRound = round;
					int tries = 0;
					boolean prayerSet = false;
					while (!prayerSet && tries < 10 && inGame()
							&& round == thisRound) {
						if (setPrayer(0, true)) {
							prayerSet = true;
							lastSkinPrayerSet = 0;
						}
						tries++;
					}
					mouse.setSpeed(mouseSpeed);
				}
			}
		}
	}

	public void turnOffSkinPrayer() {
		if (lastSkinPrayerSet == 0 || lastSkinPrayerSet == 5
				|| lastSkinPrayerSet == 13) {
			status = "Turning Off Defence Prayer...";
			game.openTab(Game.TAB_PRAYER);
			sleep(101);
			int thisRound = round;
			int tries = 0;
			boolean prayerSet = false;
			while (!prayerSet && tries < 10 && inGame() && round == thisRound) {
				if (setPrayer(lastSkinPrayerSet, false)) {
					prayerSet = true;
					lastSkinPrayerSet = -1;
				}
				tries++;
			}
			game.openTab(Game.TAB_INVENTORY);
		}
	}

	public void turnCamera() {
		final char[] LR = new char[] { KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT };
		final char[] UD = new char[] { KeyEvent.VK_DOWN, KeyEvent.VK_UP };
		final char[] LRUD = new char[] { KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT,
				KeyEvent.VK_UP, KeyEvent.VK_DOWN };
		final int random2 = random(0, 2);
		final int random1 = random(0, 2);
		final int random4 = random(0, 4);

		if (random(0, 3) == 0) {
			keyboard.pressKey(LR[random1]);
			sleep(random(250, 500));
			keyboard.pressKey(UD[random2]);
			sleep(random(250, 500));
			keyboard.releaseKey(UD[random2]);
			sleep(random(250, 500));
			keyboard.releaseKey(LR[random1]);
		} else {
			keyboard.pressKey(LRUD[random4]);
			sleep(random(250, 800));
			keyboard.releaseKey(LRUD[random4]);
		}
		if (camera.getPitch() < 100) {
			while (camera.getPitch() < 100) {
				char keyUp = KeyEvent.VK_UP;
				keyboard.pressKey(keyUp);
				sleep(1500);
				keyboard.releaseKey(keyUp);
			}
		}
	}

	public void lookAtLevels() {
		if (inLobby() || inWaitingRoom()) {
			game.openTab(Game.TAB_STATS);
			sleep(random(400, 750));
			if (currentStyle.equals("Melee")) {
				int rand = random(1, 400);
				if (rand > 0 && rand < 101) {
					skills.doHover(Skills.INTERFACE_ATTACK);
				} else if (rand > 100 && rand < 201) {
					skills.doHover(Skills.INTERFACE_STRENGTH);
				} else if (rand > 200 && rand < 301) {
					skills.doHover(Skills.INTERFACE_DEFENSE);
				} else if (rand > 300 && rand < 401) {
					skills.doHover(Skills.INTERFACE_CONSTITUTION);
				}
			} else if (currentStyle.equals("Magic")) {
				int rand = random(1, 200);
				if (rand > 0 && rand < 101) {
					skills.doHover(Skills.INTERFACE_MAGIC);
				} else if (rand > 100 && rand < 201) {
					skills.doHover(Skills.INTERFACE_CONSTITUTION);
				}
			} else if (currentStyle.equals("Range")) {
				int rand = random(1, 200);
				if (rand > 0 && rand < 101) {
					skills.doHover(Skills.INTERFACE_RANGE);
				} else if (rand > 100 && rand < 201) {
					skills.doHover(Skills.INTERFACE_CONSTITUTION);
				}
			}
			sleep(random(1200, 1750));
			game.openTab(Game.TAB_INVENTORY);
		}
	}

	public void antiBan() {
		switch (random(1, 50)) {
		case 1:
			turnCamera();
			break;
		case 2:
			turnCamera();
			break;
		case 3:
			turnCamera();
			break;
		case 4:
			turnCamera();
			break;
		case 5:
			mouse.move(random(50, 700), random(50, 450));
			break;
		case 6:
			mouse.move(random(50, 700), random(50, 450));
			break;
		case 7:
			mouse.move(random(50, 700), random(50, 450));
			break;
		case 8:
			mouse.move(random(50, 700), random(50, 450));
			break;
		case 9:
			lookAtLevels();
			break;
		}
	}

	public boolean mouseInArea(Rectangle rec) {
		return mouseInArea(rec.x, rec.y, rec.width, rec.height);
	}

	public boolean mouseInArea(int x, int y, int w, int h) {
		int x2 = x + w;
		int y2 = y + h;
		if (userMouseX >= x && userMouseX <= x2 && userMouseY >= y
				&& userMouseY <= y2) {
			return true;
		}
		return false;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			userMousePressed = true;
			break;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			userMousePressed = false;
			if (mouseInArea(statusButtonRec)) {
				if (statusPaint == 1 || statusPaint == 2) {
					statusPaint = 3;
				} else {
					statusPaint = 1;
				}
			} else if (mouseInArea(expButtonRec)) {
				if (expPaint == 1 || expPaint == 2) {
					expPaint = 3;
				} else {
					expPaint = 1;
				}
			} else if (mouseInArea(statsButtonRec)) {
				if (statsPaint == 1 || statsPaint == 2) {
					statsPaint = 3;
				} else {
					statsPaint = 1;
				}
			} else if (mouseInArea(moreStatsButtonRec)) {
				if (statsPaint == 1 || statsPaint == 2) {
					if (moreStats == 0 || moreStats == 3) {
						moreStats = 1;
					} else if (moreStats == 2 || moreStats == 1) {
						moreStats = 3;
					}
				}
			}
			break;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		userMouseX = e.getX();
		userMouseY = e.getY();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	int[] concat(int[] A, int[] B) {
		int[] C = new int[A.length + B.length];
		System.arraycopy(A, 0, C, 0, A.length);
		System.arraycopy(B, 0, C, A.length, B.length);
		return C;
	}

	FOGItem[] concat(FOGItem[] A, FOGItem[] B) {
		FOGItem[] C = new FOGItem[A.length + B.length];
		System.arraycopy(A, 0, C, 0, A.length);
		System.arraycopy(B, 0, C, A.length, B.length);
		return C;
	}

	public int maxInArray(int[] array) {
		int max = 0;
		for (int i = 0; i < array.length; i++) {
			max = Math.max(max, array[i]);
		}
		return max;
	}

	public boolean hoverMenuIndex(int menuIndex) {
		if (!menu.isOpen())
			return false;
		try {
			String[] itemStringsArray = menu.getItems();
			if (itemStringsArray.length <= menuIndex)
				return false;
			Point menuLocation = menu.getLocation();
			int i = random(4, itemStringsArray[menuIndex].length() * 4);
			int j = 21 + (15 * menuIndex) + random(3, 12);
			mouse.move((int) menuLocation.getX() + i, (int) menuLocation.getY()
					+ j, 2, 2);
			if (!menu.isOpen())
				return false;
			return true;
		} catch (Exception e) {
			if (dmode)
				e.printStackTrace();
		}
		return false;
	}

	public void dlog(String s) {
		if (dmode) {
			log(s);
		}
	}

	// Sets a prayer - returns false instead of throwing an
	// ArrayIndexOutOfBoundsException at you
	public boolean setPrayer(int pray, boolean activate) {
		RSComponent prayerComp = interfaces.getComponent(271, 7);
		RSComponent[] prayerComps = prayerComp.getComponents();
		int tries = 0;
		while (prayerComps.length <= pray && tries < 75) {
			prayerComp = interfaces.getComponent(271, 7);
			prayerComps = prayerComp.getComponents();
			tries++;
			sleep(1);
		}
		if (prayerComps.length > pray) {
			return prayerComps[pray].doAction(activate ? "Activate"
					: "Deactivate");
		} else {
			if (dmode) {
				log("Prayer Array Too Short");
			}
		}
		return false;
	}
}
