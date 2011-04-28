package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.*;

import java.awt.*;

/*
 * Updated by Iscream Feb 04,10
 * Updated by Iscream Feb 20,10
 * Updated by Iscream Mar 01,10
 * Updated by Arbiter Sep 20,10
 * Updated by Arbiter Sep 23,10
 */
@ScriptManifest(authors = {"Arbiter", "PwnZ", "Megaalgos", "Taha", "Fred", "Poxer", "Iscream"}, name = "Exam",
		version = 2.1)
public class Exam extends Random {
	public class NextObjectQuestion {
		int One = -1;
		int Two = -1;
		int Three = -1;

		public NextObjectQuestion() {
		}

		public boolean arrayContains(final int[] arr, final int i) {
			boolean returnt = false;
			for (final int num : arr) {
				if (num == i) {
					returnt = true;
				}
			}

			return returnt;
		}

		public boolean clickAnswer() {
			int[] Answers;
			if ((Answers = returnAnswer()) == null) {
				return false;
			}

			for (int i = 10; i <= 13; i++) {
				if (arrayContains(Answers, interfaces.get(nextObjectInterface).
						getComponent(i).getComponentID())) {
					return interfaces.get(nextObjectInterface).getComponent(i).doClick();
				}
			}

			return false;
		}

		public boolean getObjects() {
			One = -1;
			Two = -1;
			Three = -1;
			One = interfaces.get(nextObjectInterface).getComponent(6).getComponentID();
			Two = interfaces.get(nextObjectInterface).getComponent(7).getComponentID();
			Three = interfaces.get(nextObjectInterface).getComponent(8).getComponentID();

			return (One != -1) && (Two != -1) && (Three != -1);
		}

		public void guess() {
			final int[] objects = new int[4];
			objects[0] = interfaces.get(nextObjectInterface).getComponent(10).getComponentID();
			objects[1] = interfaces.get(nextObjectInterface).getComponent(11).getComponentID();
			objects[2] = interfaces.get(nextObjectInterface).getComponent(12).getComponentID();
			objects[3] = interfaces.get(nextObjectInterface).getComponent(13).getComponentID();

			int lowest = 120;
			int click = 10;
			int compare = 0;
			if (compare <= 10) {
				interfaces.get(nextObjectInterface).getComponent(random(10, 13)).doClick();
				return;
			}

			for (int i = 0; i < objects.length; i++) {
				if (Math.abs(objects[i] - compare) <= lowest) {
					lowest = Math.abs(objects[i] - compare);
				}
				click = 10 + i;
			}
			interfaces.get(nextObjectInterface).getComponent(click).doClick();
		}

		public int[] returnAnswer() {
			final int[] count = new int[items.length];
			int firstcard = 0;
			int secondcard = 0;
			int thirdcard = 0;

			for (int i = 0; i < count.length; i++) {
				count[i] = 0;
			}
			// Will verify that all IDs are IDs which we currently have
			for (int[] item : items) {
				for (int anItem : item) {
					if (anItem == One) {
						firstcard = 1;
					}
					if (anItem == Two) {
						secondcard = 1;
					}
					if (anItem == Three) {
						thirdcard = 1;
					}
				}
			}
			if (firstcard == 0) {
				log.severe("The first object is a new Object");
				log.severe("The Missing Object ID is :" + Integer.toString(One));
			}
			if (secondcard == 0) {
				log.severe("The second object is a new Object");
				log.severe("The Missing Object ID is :"
						+ Integer.toString(Two));
			}
			if (thirdcard == 0) {
				log.severe("The third object is a new Object");
				log.severe("The Missing Object ID is :"
						+ Integer.toString(Three));
			}

			for (int i = 0; i < items.length; i++) {
				for (int j = 0; j < items[i].length; j++) {
					if (items[i][j] == One) {
						count[i]++;
					}
					if (items[i][j] == Two) {
						count[i]++;
					}
					if (items[i][j] == Three) {
						count[i]++;
					}
					if (count[i] >= 2) {
						log.info("Answer Type Found!");
						return items[i];
					}
				}
			}

			return null;
		}
	}

	public class SimilarObjectQuestion {
		final String question;
		final int[] Answers;

		public SimilarObjectQuestion(final String q, final int[] Answers) {
			question = q.toLowerCase();
			this.Answers = Answers;
		}

		public boolean accept() {
			return interfaces.getComponent(relatedCardsInterface, 26).doClick();
		}

		public boolean activateCondition() {
			if (!interfaces.get(relatedCardsInterface).isValid()) {
				return false;
			}

			if (interfaces.getComponent(relatedCardsInterface, 25).getText().toLowerCase()
					.contains(question)) {
				log.info("Question keyword: " + question);
				return true;
			}

			return false;
		}

		public boolean clickObjects() {
			int count = 0;
			for (int i = 42; i <= 56; i++) {
				for (final int answer : Answers) {
					if (interfaces.getComponent(relatedCardsInterface, i).getComponentID() == answer) {
						if (interfaces.getComponent(relatedCardsInterface, i).doClick()) {
							sleep(random(600, 1000));
						}
						count++;
						if (count >= 3) {
							return true;
						}
					}
				}
			}
			log.info("returns false");
			return false;
		}
	}

	public static final int nextObjectInterface = 103;
	public static final int relatedCardsInterface = 559;

	public static final int[] Ranged = {11539, 11540, 11541, 11614, 11615, 11633};

	public static final int[] Cooking = {11526, 11529, 11545, 11549, 11550, 11555, 11560,
			11563, 11564, 11607, 11608, 11616, 11620, 11621, 11622, 11623,
			11628,

			11629, 11634, 11639, 11641, 11649, 11624};

	public static final int[] Fishing = {11527, 11574, 11578, 11580, 11599, 11600, 11601,
			11602, 11603,

			11604, 11605, 11606, 11625};

	public static final int[] Combat = {11528, 11531, 11536, 11537, 11579, 11591, 11592,
			11593, 11597, 11627, 11631, 11635, 11636, 11638, 11642, 11648,
			11617};

	public static final int[] Farming = {11530, 11532, 11547, 11548, 11554, 11556, 11571,
			11581, 11586, 11610, 11645};

	public static final int[] Magic = {11533, 11534, 11538, 11562, 11567, 11582};

	public static final int[] Firemaking = {11535, 11551, 11552, 11559, 11646};

	public static final int[] Hats = {11540, 11557, 11558, 11560, 11570, 11619, 11626,
			11630, 11632, 11637, 11654};
	public static final int[] Pirate = {11570, 11626, 11558};

	public static final int[] Jewellery = {11572, 11576, 11652};

	public static final int[] Jewellery2 = {11572, 11576, 11652};

	public static final int[] Drinks = {11542, 11543, 11544, 11644, 11647};

	public static final int[] Woodcutting = {11573, 11595};

	public static final int[] Boots = {11561, 11618, 11650, 11651};

	public static final int[] Crafting = {11546, 11553, 11565, 11566, 11568, 11569, 11572,
			11575, 11576, 11577, 11581, 11583, 11584, 11585, 11643, 11652,
			11653};

	public static final int[] Mining = {11587, 11588, 11594, 11596, 11598, 11609, 11610};

	public static final int[] Smithing = {11611, 11612, 11613};
	public static final int[][] items = {Ranged, Cooking, Fishing, Combat, Farming, Magic,
			Firemaking, Hats, Drinks, Woodcutting, Boots, Crafting, Mining,
			Smithing};

	public final SimilarObjectQuestion[] simObjects = {
			new SimilarObjectQuestion("I never leave the house without some sort of jewellery.", Jewellery),
			new SimilarObjectQuestion("There is no better feeling than", Jewellery2),
			new SimilarObjectQuestion("I'm feeling dehydrated", Drinks),
			new SimilarObjectQuestion("All this work is making me thirsty", Drinks),
			new SimilarObjectQuestion("quenched my thirst", Drinks),
			new SimilarObjectQuestion("light my fire", Firemaking),
			new SimilarObjectQuestion("fishy", Fishing),
			new SimilarObjectQuestion("fishing for answers", Fishing),
			new SimilarObjectQuestion("fish out of water", Drinks),
			new SimilarObjectQuestion("strange headgear", Hats),
			new SimilarObjectQuestion("tip my hat", Hats),
			new SimilarObjectQuestion("thinking cap", Hats),
			new SimilarObjectQuestion("wizardry here", Magic),
			new SimilarObjectQuestion("rather mystical", Magic),
			new SimilarObjectQuestion("abracada", Magic),
			new SimilarObjectQuestion("hide one's face", Hats),
			new SimilarObjectQuestion("shall unmask", Hats),
			new SimilarObjectQuestion("hand-to-hand", Combat),
			new SimilarObjectQuestion("melee weapon", Combat),
			new SimilarObjectQuestion("prefers melee", Combat),
			new SimilarObjectQuestion("me hearties", Pirate),
			new SimilarObjectQuestion("puzzle for landlubbers", Pirate),
			new SimilarObjectQuestion("mighty pirate", Pirate),
			new SimilarObjectQuestion("mighty archer", Ranged),
			new SimilarObjectQuestion("as an arrow", Ranged),
			new SimilarObjectQuestion("Ranged attack", Ranged),
			new SimilarObjectQuestion("shiny things", Crafting),
			new SimilarObjectQuestion("igniting", Firemaking),
			new SimilarObjectQuestion("sparks from my synapses.", Firemaking),
			new SimilarObjectQuestion("fire.", Firemaking),
			new SimilarObjectQuestion("disguised", Hats),
			// added diguised Feb 04,2010


			// Default questions just incase the bot gets stuck
			new SimilarObjectQuestion("range", Ranged),
			new SimilarObjectQuestion("arrow", Ranged),
			new SimilarObjectQuestion("drink", Drinks),
			new SimilarObjectQuestion("logs", Firemaking),
			new SimilarObjectQuestion("light", Firemaking),
			new SimilarObjectQuestion("headgear", Hats),
			new SimilarObjectQuestion("hat", Hats),
			new SimilarObjectQuestion("cap", Hats),
			new SimilarObjectQuestion("mine", Mining),
			new SimilarObjectQuestion("mining", Mining),
			new SimilarObjectQuestion("ore", Mining),
			new SimilarObjectQuestion("fish", Fishing),
			new SimilarObjectQuestion("fishing", Fishing),
			new SimilarObjectQuestion("thinking cap", Hats),
			new SimilarObjectQuestion("cooking", Cooking),
			new SimilarObjectQuestion("cook", Cooking),
			new SimilarObjectQuestion("bake", Cooking),
			new SimilarObjectQuestion("farm", Farming),
			new SimilarObjectQuestion("farming", Farming),
			new SimilarObjectQuestion("cast", Magic),
			new SimilarObjectQuestion("magic", Magic),
			new SimilarObjectQuestion("craft", Crafting),
			new SimilarObjectQuestion("boot", Boots),
			new SimilarObjectQuestion("chop", Woodcutting),
			new SimilarObjectQuestion("cut", Woodcutting),
			new SimilarObjectQuestion("tree", Woodcutting),

	};

	public RSObject door = null;

	@Override
	public boolean activateCondition() {
		door = null;
		return npcs.getNearest("Mr. Mordaut") != null;
	}

	/*
			  * Don't use this with any other monster.I edited for this script only cause
			  * Mr. Mordaunt doesn't move
			  */
	public boolean clickCharacter(final RSCharacter c, final String action) {
		try {
			Point screenLoc;
			screenLoc = c.getScreenLocation();

			if (!c.isValid() || !calc.pointOnScreen(screenLoc)) {
				System.out.println("Not on screen " + action);
				return false;
			}

			mouse.move(screenLoc);

			screenLoc = c.getScreenLocation();

			final String[] items = menu.getItems();

			if (items[0].toLowerCase().contains(action.toLowerCase())) {
				mouse.click(screenLoc, true);
				return true;
			} else {
				mouse.click(screenLoc, false);
				return menu.doAction(action);
			}

		} catch (final NullPointerException ignored) {
		}
		return true;
	}

	// My clickObject, like clickCharacter, and faster than atObject.
	public boolean clickObject(final RSObject c, final String action) {
		try {
			Point screenLoc;
			int X = (int) calc.tileToScreen(c.getLocation()).getX();
			int Y = (int) calc.tileToScreen(c.getLocation()).getY();

			screenLoc = new Point(X, Y);

			if ((c == null) || !calc.pointOnScreen(screenLoc)) {
				log("Not on screen " + action);
				return false;
			}

			mouse.move(screenLoc);

			X = (int) calc.tileToScreen(c.getLocation()).getX();
			Y = (int) calc.tileToScreen(c.getLocation()).getY();
			screenLoc = new Point(X, Y);
			if (!mouse.getLocation().equals(screenLoc)) {
				return false;
			}

			final String[] items = menu.getItems();
			if (items.length <= 1) {
				return false;
			}
			if (items[0].toLowerCase().contains(action.toLowerCase())) {
				mouse.click(screenLoc, true);
				return true;
			} else {
				mouse.click(screenLoc, false);
				return menu.doAction(action);
			}
		} catch (final NullPointerException ignored) {
		}
		return true;
	}

	@Override
	public int loop() {
		final RSNPC mordaut = npcs.getNearest("Mr. Mordaut");
		if (mordaut == null) {
			return -1;
		}

		if (getMyPlayer().isMoving() || (getMyPlayer().getAnimation() != -1)) {
			return random(800, 1200);
		}

		if (door != null) {
			if (calc.distanceTo(door) > 3) {
				walking.getPath(door.getLocation()).traverse();
				sleep(random(1400, 2500));
			}
			if (!calc.tileOnScreen(door.getLocation())) {
				walking.walkTileMM(door.getLocation());
				sleep(random(1400, 2500));
			}
			if (door.getID() == 2188) {
				camera.setCompass('w');
			}
			if (door.getID() == 2193) {
				camera.setCompass('e');
			}
			if (door.getID() == 2189) {
				camera.setCompass('w');
			}
			if (door.getID() == 2192) {
				camera.setCompass('n');
			}
			clickObject(door, "Open");
			return random(500, 1000);
		}
		final RSComponent inter = searchInterfacesText("To exit,");
		if (inter != null) {
			if (inter.getText().toLowerCase().contains("red")) {
				door = objects.getNearest(2188);
			}
			if (inter.getText().toLowerCase().contains("green")) {
				door = objects.getNearest(2193);
			}
			if (inter.getText().toLowerCase().contains("blue")) {
				door = objects.getNearest(2189);
			}
			if (inter.getText().toLowerCase().contains("purple")) {
				door = objects.getNearest(2192);
			}
			return random(500, 1000);
		}
		if (!interfaces.get(nextObjectInterface).isValid() && !getMyPlayer().isMoving()
				&& !interfaces.get(relatedCardsInterface).isValid() && !interfaces.canContinue()
				&& door == null) {
			if (calc.distanceTo(mordaut) > 4) {
				walking.getPath(mordaut.getLocation()).traverse();
			}
			if (!calc.tileOnScreen(mordaut.getLocation())) {
				walking.walkTileMM(mordaut.getLocation());
			}
			clickCharacter(mordaut, "Talk-to");
			return (random(1500, 1700));
		}
		if (interfaces.get(nextObjectInterface).isValid()) {
			log.info("Question Type: Next Object");
			final NextObjectQuestion noq = new NextObjectQuestion();
			if (noq.getObjects()) {
				if (noq.clickAnswer()) {
					return random(800, 1200);
				} else {
					noq.guess();
					return random(800, 1200);
				}
			} else {
				log.info("Could not find get object. Making educated guess.");
				noq.guess();
				return random(800, 1200);
			}
		}

		if (interfaces.get(relatedCardsInterface).isValid()) {
			log.info("Question Type: Similar Objects");
			int z = 0;
			for (final SimilarObjectQuestion obj : simObjects) {
				if (obj.activateCondition()) {
					z = 1;
					if (obj.clickObjects()) {
						obj.accept();
					}
				}
			}
			if (z == 0) {
				log.severe("This is a new question.");
				log.severe("Please post this on the forums.");
				log.severe("The Missing Question is :");
				log(interfaces.get(nextObjectInterface).getComponent(25).getText().toLowerCase());
			}
			return random(800, 1200);
		}

		if (interfaces.clickContinue()) {
			return random(800, 3500);
		}

		return random(800, 1200);
	}

	public RSComponent searchInterfacesText(final String string) {
		final RSInterface[] inters = interfaces.getAll();
		for (final RSInterface inter : inters) {
			for (final RSComponent interfaceChild : inter.getComponents()) {
				if (interfaceChild.getText().toLowerCase().contains(
						string.toLowerCase())) {
					return interfaceChild;
				}
			}
		}

		return null;
	}
}