package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSNPC;


//Checked 4/7/10

/**
 * Updated by Arbiter Sep 20, 10: Replaced getModelZoom with getComponentID() and new sets of possible IDs as solutions
 */
@ScriptManifest(authors = {"PwnZ"}, name = "Quiz", version = 1.0)
public class QuizSolver extends Random {

	public class QuizQuestion {
		final int ID_One;
		final int ID_Two;
		final int ID_Three;
		int answer;

		public QuizQuestion(final int One, final int Two, final int Three) {
			ID_One = One;
			ID_Two = Two;
			ID_Three = Three;
		}

		public boolean activateCondition() {
			if (ID_to_Slot(ID_One) != -1) {
				if (ID_to_Slot(ID_Two) != -1) {
					if (ID_to_Slot(ID_Three) != -1) {
						return true;
					}
				}
			}

			return false;
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
			answer = -1;
			int count = 0;

			for (int j = 0; j < items.length; j++) {
				if (arrayContains(items[j], ID_One)) {
					log.info("Slot 1: " + names[j]);
					count++;
				}
				if (arrayContains(items[j], ID_Two)) {
					log.info("Slot 2: " + names[j]);
					count++;
				}
				if (arrayContains(items[j], ID_Three)) {
					log.info("Slot 3: " + names[j]);
					count++;
				}

				if (count >= 2) {
					log.info("Type Found: " + names[j]);
					answer = j;
					break;
				}
			}

			if (answer != -1) {
				int slot;
				if ((slot = findNotInAnswerArray()) != -1) {
					return atSlot(slot);
				} else {
					log.info("findNotInAnswerArray() fail.");
					return false;
				}
			} else {
				log.info("answer fail.");
				return false;
			}

		}

		public int findNotInAnswerArray() {
			if (!arrayContains(items[answer], ID_One)) {
				return 1;
			} else if (!arrayContains(items[answer], ID_Two)) {
				return 2;
			} else if (!arrayContains(items[answer], ID_Three)) {
				return 3;
			} else {
				return -1;
			}
		}
	}

	public final int quizInterface = 191;
	public final int[] Fish = {6190, 6189};
	public final int[] Jewelry = {6198, 6197};
	public final int[] Weapons = {6192, 6194};
	public final int[] Farming = {6195, 6196};
	public final int[][] items = {Fish, Jewelry, Weapons, Farming};

	public final String[] names = {"Fish", "Jewelry", "Weapons", "Farming"};

	@Override
	public boolean activateCondition() {
		final RSNPC quizMaster = npcs.getNearest("Quiz Master");
		return quizMaster != null;
	}

	public void atRandom() {
		atSlot(random(1, 3));
	}

	public boolean atSlot(final int slot) {
		switch (slot) {
			case 1:
				return interfaces.getComponent(quizInterface, 3).doClick();
			case 2:
				return interfaces.getComponent(quizInterface, 4).doClick();
			case 3:
				return interfaces.getComponent(quizInterface, 5).doClick();
			default:
				return false;
		}
	}

	public int ID_to_Slot(final int id) {
		if (Slot_to_ID(1) == id) {
			return 1;
		} else if (Slot_to_ID(2) == id) {
			return 2;
		} else if (Slot_to_ID(3) == id) {
			return 3;
		} else {
			return -1;
		}
	}

	@Override
	public int loop() {
		final RSNPC quizMaster = npcs.getNearest("Quiz Master");
		if (quizMaster == null) {
			return -1;
		}

		if (Slot_to_ID(1) != -1) {
			log.info("Question detected.");
			final QuizQuestion question = new QuizQuestion(Slot_to_ID(1), Slot_to_ID(2), Slot_to_ID(3));
			if (question.clickAnswer()) {
				return random(2200, 3000);
			} else {
				log.info("Trying Random Answer");
				atRandom();
				return random(1200, 2200);
			}
		} else {
			if (interfaces.clickContinue()) {
				return random(800, 1200);
			}
		}
		return random(1200, 2000);
	}

	public int Slot_to_ID(final int slot) {
		switch (slot) {
			case 1:
				//System.out.println(interfaces.get(quizInterface).getComponent(6).getComponentID());
				return interfaces.get(quizInterface).getComponent(6).getComponentID();
			case 2:
				//System.out.println(interfaces.get(quizInterface).getComponent(7).getComponentID());
				return interfaces.get(quizInterface).getComponent(7).getComponentID();
			case 3:
				//System.out.println(interfaces.get(quizInterface).getComponent(8).getComponentID());
				return interfaces.get(quizInterface).getComponent(8).getComponentID();
			default:
				return -1;
		}
	}
}
