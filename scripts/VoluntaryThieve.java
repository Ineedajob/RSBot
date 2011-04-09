/*
 * @(#)VoluntaryThieve.java	1.13 10/12/17
 * Copyright 2010 vilon@powerbot.org. All rights reserved.
 */

import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.util.Timer;
import org.rsbot.script.util.WindowUtil;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSInterface;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.util.GlobalConfiguration;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.prefs.Preferences;

@ScriptManifest(authors = "vilon", name = "Voluntary Thieve", keywords = "Thieving", version = 1.13, description = "Blackjacks/pickpockets the trainers and volunteers in the Thieves' Guild.")
public final class VoluntaryThieve extends Script implements MouseInputListener, PaintListener, MessageListener {

	/**
	 * The <tt>Action</tt>-class is used to represent an actual in-game action.
	 * Provides the skeleton for performing an action, confirming the action and
	 * finally verify that the result of the action matches the desired result.
	 */
	private static abstract class Action {

		/**
		 * Represents the steps of an action.
		 */
		static final class Steps {

			/**
			 * The constant representing the "PERFORM"-step.
			 */
			private static final int PERFORM = 0;

			/**
			 * The constant representing the "CONFIRM"-step.
			 */
			private static final int CONFIRM = 1;

			/**
			 * The constant representing the "FINISH"-step.
			 */
			private static final int FINISH = 2;

			/**
			 * The maximum times of the steps, in order: PERFORM, CONFIRM, FINISH.
			 */
			private final long[] maximumTimes;

			/**
			 * The starting times of the steps, in order: PERFORM, CONFIRM, FINISH.
			 */
			private final long[] startingTimes = new long[3];

			/**
			 * The current step in the action-process.
			 */
			private int currentStep = PERFORM;

			/**
			 * Creates a new instance representing the steps of an action.
			 *
			 * @param maximumTimes The maximum times of the individual steps,
			 *                     in the order: PERFORM, CONFIRM, FINISH.
			 * @throws IllegalArgumentException If the length of <tt>maximumTimes</tt> wasn't three,
			 *                                  or if any of the maximum times were below zero.
			 */
			private Steps(final long... maximumTimes) throws IllegalArgumentException {
				if (maximumTimes.length != 3) throw new IllegalArgumentException(
						"Invalid argument length, expected 3 was " + maximumTimes.length + ".");
				for (final long maximumTime : maximumTimes)
					if (maximumTime < 0) throw new IllegalArgumentException(
							"Maximum time was below zero: " + maximumTime);
				this.maximumTimes = maximumTimes;
			}

			/**
			 * Gets the current step in the action-process.
			 *
			 * @return The current step in the action-process.
			 */
			private int getCurrent() {
				return currentStep;
			}

			/**
			 * Sets the starting time to that of the systems current time.
			 */
			private void setStartingTime() {
				startingTimes[getCurrent()] = System.currentTimeMillis();
			}

			/**
			 * Gets the starting time of the current step.
			 *
			 * @return The starting time of the current step,
			 *         or <tt>0</tt> if not set.
			 */
			private long getStartingTime() {
				return startingTimes[getCurrent()];
			}

			/**
			 * Checks whether the starting time has been set or not.
			 *
			 * @return <tt>true</tt> if the starting time has been set;
			 *         otherwise <tt>false</tt>.
			 */
			private boolean isStartingTimeSet() {
				return (getStartingTime() > 0);
			}

			/**
			 * Sets the current step to the next step in the set order.
			 *
			 * @throws IllegalStateException If the method is called when the current
			 *                               step is the <tt>FINISH</tt>-step.
			 */
			private void next() throws IllegalStateException {
				if (getCurrent() >= FINISH)
					throw new IllegalStateException(
							"Unable to switch step, current step is: " + getCurrent());
				currentStep++;
			}

			/**
			 * Checks if the maximum time set has passed, i.e.
			 * if it's now obsolete to continue doing the action.
			 *
			 * @return <tt>true</tt> if the maximum time has passed;
			 *         otherwise <tt>false</tt>.
			 * @throws IllegalStateException If the starting time has
			 *                               not been set.
			 */
			private boolean isObsolete() throws IllegalStateException {
				if (!isStartingTimeSet()) throw new IllegalStateException("Starting time has not been set.");
				return (System.currentTimeMillis() - getStartingTime()
						>= maximumTimes[getCurrent()]);
			}
		}

		/**
		 * The states which tells if an action was completed, failed or is still in progress.
		 */
		enum State {

			COMPLETED, FAILED, PROGRESSING
		}

		/**
		 * The user-friendly name of this action.
		 */
		private final String name;

		/**
		 * The identification for this action.
		 */
		private final int id;

		/**
		 * The current step in the action-process.
		 */
		final Steps steps;

		/**
		 * This will contain the final state when the action is done (i.e. failed/completed).
		 */
		private State actionDoneState;

		/**
		 * Creates a new <tt>Action</tt> with the maximum times for the three
		 * steps of the action, namely the perform-, confirm- and finish-step.
		 *
		 * @param name		 The user-friendly name of this action.
		 * @param id		   The identification for this action.
		 * @param maximumTimes The maximum times for the steps, in the order:
		 *                     PERFORM, CONFIRM, FINISH.
		 */
		private Action(final String name, final int id, final long... maximumTimes) {
			this.id = id;
			this.name = name;
			steps = new Steps(maximumTimes);
		}

		/**
		 * Creates a new <tt>Action</tt> with the maximum times for two of the
		 * three steps of the action, namely the perform-, and finish-step. The
		 * confirm-step defaults to completed but can be overridden and will
		 * in that case be called once before failing.
		 *
		 * @param name	The user-friendly name of this action.
		 * @param id	  The identification for this action.
		 * @param perform The maximum time before the perform-step is obsolete.
		 * @param finish  The maximum time before the finish-step is obsolete.
		 */
		private Action(final String name, final int id, final long perform, final long finish) {
			this(name, id, perform, 0, finish);
		}

		/**
		 * Gets the user-friendly name of this action.
		 *
		 * @return The user-friendly name of the action.
		 */
		private String getName() {
			return name;
		}

		/**
		 * Gets the identification for this action.
		 *
		 * @return The identification for this action.
		 */
		private int getId() {
			return id;
		}

		/**
		 * Performs this action. Should be looped as long as the
		 * return-value is <tt>State.PROGRESSING</tt>. No matter how
		 * low a maximum time for a step is set, the corresponding
		 * method will always be called once, before making any conclusions.
		 *
		 * @return One of the following states:
		 *         State.PROGRESSING - The action is still in progress.
		 *         State.COMPLETED - The action completed successfully.
		 *         State.FAILED - The action did not complete successfully.
		 */
		private State doAction() {
			if (actionDoneState != null)
				return actionDoneState;
			if (!steps.isStartingTimeSet())
				steps.setStartingTime();

			State returnState;
			switch (steps.getCurrent()) {
				case Steps.PERFORM:
					if ((returnState = perform()) == State.COMPLETED) {
						steps.next();
						return State.PROGRESSING;
					}
					break;
				case Steps.CONFIRM:
					if ((returnState = confirm()) == State.COMPLETED) {
						steps.next();
						return State.PROGRESSING;
					}
					break;
				case Steps.FINISH:
					if ((returnState = finish()) == State.COMPLETED)
						return (actionDoneState = State.COMPLETED);
					break;
				default:
					throw new AssertionError("Unsupported step: " + steps.getCurrent());
			}

			return (returnState == State.FAILED || steps.isObsolete()) ?
					(actionDoneState = State.FAILED) : State.PROGRESSING;
		}

		/**
		 * Performs the selected action, e.x. clicking on an object.
		 *
		 * @return The <tt>State</tt>, telling if the operation was
		 *         successful or not, or if the action is still in progress.
		 */
		abstract State perform();

		/**
		 * Confirms the selected action, e.x. the click on the object.
		 *
		 * @return The <tt>State</tt>, telling if the operation was
		 *         successful or not, or if the action is still in progress.
		 */
		State confirm() {
			return State.COMPLETED;
		}

		/**
		 * Checks if the actions result matches the desired result of the action.
		 *
		 * @return The <tt>State</tt>, telling if the operation was
		 *         successful or not, or if the action is still in progress.
		 */
		abstract State finish();
	}

	/**
	 * The class <tt>Actions</tt> contains all actions used in this script.
	 */
	private final class Actions {

		/**
		 * Contains all the custom methods being shared among actions.
		 */
		private final class Methods {

			/**
			 * Checks if the the player is logged in and has passed the welcome screen. Also
			 * makes sure that skill levels and such are loaded, so no misreadings happen.
			 *
			 * @return <tt>true</tt> if the player is logged in and has passed the welcome screen;
			 *         otherwise <tt>false</tt>.
			 */
			private boolean isLoggedIn() {
				return (game.isLoggedIn() && skills.getRealLevel(Skills.THIEVING) > 1);
			}

			/**
			 * Gets the identifications for the trainers to use in the current mode. Excludes
			 * trainers that have explicitly been marked as not to be included.
			 *
			 * @return The identifications for the trainers to use.
			 */
			private int[] getTrainers() {
				final int[] allTrainers = options.isBlackjacking ? new int[]{11289, 11291, 11293, 11297} :
						new int[]{11281, 11283, 11285, 11287};

				final List<Integer> validTrainers = new ArrayList<Integer>();
				for (final Integer trainer : allTrainers) {
					final Boolean isIncluded = trainerInclusions.get(trainer);
					if (isIncluded == null || isIncluded)
						validTrainers.add(trainer);
				}

				final int[] resultTrainers = new int[validTrainers.size()];
				for (int i = 0; i < validTrainers.size(); i++)
					resultTrainers[i] = validTrainers.get(i);
				return resultTrainers;
			}

			/**
			 * Gets if the door to the bank is open or closed.
			 *
			 * @return <tt>true</tt> if the door could be found and is open;
			 *         <tt>false</tt> if the door could not be found or is closed.
			 */
			private boolean isDoorOpen() {
				final RSObject door = getOpenDoor();
				return (door != null && door.getID() == Values.OBJECT_DOOR_OPEN);
			}

			/**
			 * Gets the object at the position where the door to the bank should
			 * be if it is currently open.
			 *
			 * @return The <tt>RSObject</tt> at the position of the open door.
			 */
			private RSObject getOpenDoor() {
				return objects.getTopAt(new RSTile(4755 + tileOffset.x, 5795 + tileOffset.y));
			}

			/**
			 * Gets the object at the position where the door to the bank should
			 * be if it is currently closed.
			 *
			 * @return The <tt>RSObject</tt> at the position of the closed door.
			 */
			private RSObject getClosedDoor() {
				return objects.getTopAt(new RSTile(4754 + tileOffset.x, 5795 + tileOffset.y));
			}

			/**
			 * Gets if any of the interfaces for the conversation when luring is valid or not.
			 *
			 * @return <tt>true</tt> if any of the interfaces for the conversation when luring is valid;
			 *         otherwise <tt>false</tt>.
			 */
			private boolean isLureScreenValid() {
				if (hasKnockoutFailed()) return false;
				final int[] screens = {Values.INTERFACE_LURE_FIRST, Values.INTERFACE_LURE_SECOND};
				for (final int screen : screens) {
					final RSInterface screenInterface = interfaces.get(screen);
					if (screenInterface != null && screenInterface.isValid())
						return true;
				}

				return false;
			}

			/**
			 * Gets if the knock-out attempt has failed and the npc needs to be lured again.
			 *
			 * @return <tt>true</tt> if the knock-out attempt has failed; otherwise <tt>false</tt>.
			 */
			private boolean hasKnockoutFailed() {
				final RSInterface failScreen = interfaces.get(Values.INTERFACE_LURE_FIRST);
				return (failScreen != null && failScreen.isValid() &&
						failScreen.getComponent(4).containsText("divert"));
			}

			/**
			 * Gets if the player is considered to be stunned.
			 *
			 * @return <tt>true</tt> if the player is stunned; otherwise <tt>false</tt>.
			 */
			private boolean isStunned() {
				return (stunnedTimer != null && stunnedTimer.isRunning());
			}

			/**
			 * Gets the appropriate action to do in order to handle the opening of a door.
			 *
			 * @return The appropriate {@link VoluntaryThieve.Action} to take when opening the door.
			 */
			private Action getDoorAction() {
				final RSObject door = methods.getClosedDoor();
				return (door != null && calc.distanceTo(door.getLocation()) < 5) ?
						get(Values.ACTION_BANK_DOOR_OPEN) : get(Values.ACTION_BANK_DOOR_WALK);
			}

			/**
			 * Gets if the player is inside the bank-area.
			 *
			 * @return <tt>true</tt> if the player is in the bank-area;
			 *         otherwise <tt>false</tt>.
			 */
			private boolean isInBank() {
				return new RSArea(new RSTile(4747 + tileOffset.x, 5793 + tileOffset.y), new RSTile(4754 + tileOffset.x, 5797 + tileOffset.y)).
						contains(getMyPlayer().getLocation());
			}

			/**
			 * Gets if the player is located inside the thieves guild or not.
			 *
			 * @return <tt>true</tt> if the player is inside the thieves guild;
			 *         otherwise <tt>false</tt>.
			 */
			private boolean isInGuild() {
				return new RSArea(new RSTile(4745 + tileOffset.x, 5762 + tileOffset.y), new RSTile(4794 + tileOffset.x, 5806 + tileOffset.y)).
						contains(getMyPlayer().getLocation()) && game.getPlane() == 0;
			}

			/**
			 * Gets the offset to use for all locations in the script. This is used because,
			 * depending on how many capers the user has completed, the guild will be placed
			 * differently in the runescape world.
			 *
			 * @return The offset to be used for locations in the script.
			 */
			private Point getOffset() {
				final RSTile playerLocation = getMyPlayer().getLocation();
				final Point offset = new Point();

				if (4617 <= playerLocation.getX() && playerLocation.getX() <= 4666)
					offset.x = -128;

				if (5890 <= playerLocation.getY() && playerLocation.getY() <= 5934)
					offset.y = 128;

				return offset;
			}
		}

		/**
		 * Contains all the values being shared among actions.
		 */
		private final class Values {

			/**
			 * An item identification - gloves of silence.
			 */
			private static final int ITEM_GLOVES_OF_SILENCE = 10075;

			/**
			 * An item identification - rubber blackjack.
			 */
			private static final int ITEM_RUBBER_BLACKJACK = 18644;

			/**
			 * An object identification - the door to the bank when open.
			 */
			private static final int OBJECT_DOOR_OPEN = 52315;

			/**
			 * An object identification - the bank-booth in the guild.
			 */
			private static final int OBJECT_BANK_BOOTH = 52397;

			/**
			 * An interface identification - the first lure conversation screen.
			 */
			private static final int INTERFACE_LURE_FIRST = 64;

			/**
			 * An interface identification - the second lure conversation screen.
			 */
			private static final int INTERFACE_LURE_SECOND = 241;

			/**
			 * An animation identification - the animation when an npc is knocked out.
			 */
			private static final int ANIMATION_KNOCKED_OUT = 12413;

			/**
			 * An identifier for an action - walks to the specified trainer (passed as argument).
			 */
			private static final int ACTION_WALK_TO_TRAINER = 0;

			/**
			 * An identifier for an action - walks to the area with the trainers.
			 */
			private static final int ACTION_WALK_TO_TRAINING_AREA = 1;

			/**
			 * An identifier for an action - pickpockets the specified trainer (passed as argument).
			 */
			private static final int ACTION_PICKPOCKET_TRAINER = 2;

			/**
			 * An identifier for an action - lures the specified trainer (passed as argument).
			 */
			private static final int ACTION_LURE_TRAINER = 3;

			/**
			 * An identifier for an action - lures the specified trainer (passed as argument).
			 */
			private static final int ACTION_LURE_TALK_TRAINER = 4;

			/**
			 * An identifier for an action - knocks-out the specified trainer (passed as argument).
			 */
			private static final int ACTION_KNOCK_TRAINER = 5;

			/**
			 * An identifier for an action - walks to the nearby bank.
			 */
			private static final int ACTION_BANK_WALK = 6;

			/**
			 * An identifier for an action - walks to the nearby bank area.
			 */
			private static final int ACTION_BANK_WALK_AREA = 7;

			/**
			 * An identifier for an action - opens the door at the nearby bank.
			 */
			private static final int ACTION_BANK_DOOR_OPEN = 8;

			/**
			 * An identifier for an action - walks to the door at the nearby bank.
			 */
			private static final int ACTION_BANK_DOOR_WALK = 9;

			/**
			 * An identifier for an action - opens the bank at the nearby bank.
			 */
			private static final int ACTION_BANK_OPEN = 10;

			/**
			 * An identifier for an action - banks at the nearby bank.
			 */
			private static final int ACTION_BANK_BANKING = 11;

			/**
			 * An identifier for an action - waits while being stunned.
			 */
			private static final int ACTION_STUNNED_WAIT = 12;

			/**
			 * An identifier for an action - equips a new pair of gloves.
			 */
			private static final int ACTION_EQUIP_GLOVES = 13;

			/**
			 * An identifier for an action - waits for the player to get into the thieves guild.
			 */
			private static final int ACTION_FAILSAFE_TIMEOUT = 14;

			/**
			 * The amount of actions available (from 0 to <tt>ACTION_COUNT_TOTAL</tt> - 1).
			 */
			private static final int ACTIONS_TOTAL_COUNT = 15;
		}

		/**
		 * Holds all the methods being shared among actions.
		 */
		private final Methods methods = new Methods();

		/**
		 * <tt>true</tt> if the user has gloves equipped.
		 */
		private boolean hasGloves;

		/**
		 * <tt>true</tt> if the player is using gloves; otherwise <tt>false</tt>.
		 */
		private boolean isUsingGloves = true;

		/**
		 * <tt>true</tt> if the equipment has been checked for gloves.
		 */
		private boolean isEquipmentChecked;

		/**
		 * <tt>true</tt> if a pickpocket has been done since the last check.
		 */
		private boolean hasThieved;

		/**
		 * Checks an extra time for npcs on the mainscreen before walking.
		 */
		private boolean isExtraCheckDone;

		/**
		 * When <tt>true</tt> it forces blackjack (knockout) to be performed.
		 */
		private boolean isForcingBlackjack;

		/**
		 * <tt>true</tt> if the npc has been lured; otherwise <tt>false</tt>.
		 */
		private boolean isLured;

		/**
		 * <tt>true</tt> if unable to reach the current trainer; otherwise <tt>false</tt>.
		 */
		private boolean isUnableToReach;

		/**
		 * The identification for the trainer currently being targeted.
		 */
		private int currentTrainerId;

		/**
		 * Keeps track of how long we been stunned for and when the stun ends.
		 */
		private Timer stunnedTimer;

		/**
		 * The offset used for all locations in the script (for all actions).
		 */
		private Point tileOffset;

		/**
		 * Maps the trainers ids against their status (<tt>true</tt> to be included).
		 */
		private final Map<Integer, Boolean> trainerInclusions = new HashMap<Integer, Boolean>();

		/**
		 * Gets the next action to be performed.
		 *
		 * @return The next <tt>Action</tt> to be performed or
		 *         <tt>null</tt> if unable to get the next step.
		 */
		private Action getNext() {
			if (!methods.isInGuild())
				return get(Values.ACTION_FAILSAFE_TIMEOUT);

			if (!options.isBlackjacking && isUsingGloves) {
				if (!isEquipmentChecked) {
					hasGloves = equipment.containsAll(Values.ITEM_GLOVES_OF_SILENCE);
					isEquipmentChecked = true;
				}

				if (!hasGloves) {
					if (inventory.contains(Values.ITEM_GLOVES_OF_SILENCE))
						return get(Values.ACTION_EQUIP_GLOVES);

					if (options.isBanking) {
						currentTrainerId = 0;

						if (!methods.isInBank() && !methods.isDoorOpen())
							return methods.getDoorAction();

						final RSObject bankBooth = objects.getNearest(Values.OBJECT_BANK_BOOTH);
						if (bankBooth == null)
							return get(Values.ACTION_BANK_WALK_AREA);

						if (!bankBooth.isOnScreen())
							return calc.tileOnMap(bankBooth.getLocation()) ?
									get(Values.ACTION_BANK_WALK) : get(Values.ACTION_BANK_WALK_AREA);

						return bank.isOpen() ? get(Values.ACTION_BANK_BANKING)
								: get(Values.ACTION_BANK_OPEN);
					} else {
						isUsingGloves = false;
						log("Won't be using any more gloves of silence.");
					}
				}
			}

			if (methods.isStunned()) {
				currentTrainerId = 0;
				return get(Values.ACTION_STUNNED_WAIT);
			}

			if (methods.isInBank() && !methods.isDoorOpen())
				return methods.getDoorAction();

			final int[] trainerIds = (currentTrainerId != 0) ? new int[]
					{currentTrainerId} : methods.getTrainers();

			if (trainerIds.length == 0)
				return get(Values.ACTION_WALK_TO_TRAINING_AREA);

			final RSNPC nearestTrainer = npcs.getNearest(trainerIds);

			if (nearestTrainer == null)
				return get(Values.ACTION_WALK_TO_TRAINING_AREA);

			if (isUnableToReach) {
				trainerInclusions.put(nearestTrainer.getID(), false);
				isUnableToReach = false;
				currentTrainerId = 0;
				return getNext();
			}

			if (!nearestTrainer.isOnScreen()) {
				if (!isExtraCheckDone) {
					currentTrainerId = 0;
					isExtraCheckDone = true;
					return getNext();
				}

				isExtraCheckDone = false;
				return calc.tileOnMap(nearestTrainer.getLocation()) ? get(Values.ACTION_WALK_TO_TRAINER, nearestTrainer)
						: get(Values.ACTION_WALK_TO_TRAINING_AREA);
			}

			currentTrainerId = nearestTrainer.getID();
			if (options.isBlackjacking && nearestTrainer.getAnimation() != Values.ANIMATION_KNOCKED_OUT) {
				if (!isForcingBlackjack) {
					if (methods.hasKnockoutFailed())
						isLured = false;

					if (!isLured) return methods.isLureScreenValid() ? get(Values.ACTION_LURE_TALK_TRAINER) :
							get(Values.ACTION_LURE_TRAINER, nearestTrainer);
				} else isForcingBlackjack = false;

				return get(Values.ACTION_KNOCK_TRAINER, nearestTrainer);
			}

			return get(Values.ACTION_PICKPOCKET_TRAINER, nearestTrainer);
		}

		/**
		 * Gets the already defined action for the specified identifier, <tt>action</tt>.
		 *
		 * @param action The unique identifier for a predefined action, identifying a valid action.
		 * @param args   Any arguments that should be passed to the action, see identifiers.
		 * @return The already defined action for the specified identifier.
		 * @throws IllegalArgumentException If an invalid action was specified.
		 */
		private Action get(final int action, final Object... args) throws IllegalArgumentException {
			if (0 > action || action >= Values.ACTIONS_TOTAL_COUNT)
				throw new IllegalArgumentException("Invalid action: " + action);

			if (action == Values.ACTION_WALK_TO_TRAINER)
				return new Action("Walking to trainer", Values.ACTION_WALK_TO_TRAINER,
						random(1425, 1635), random(1645, 1850), random(4925, 5135)) {

					RSNPC trainer;

					@Override
					State perform() {
						if (trainer == null) {
							if (args.length != 1 || !(args[0] instanceof RSNPC))
								throw new IllegalArgumentException();
							trainer = (RSNPC) args[0];
						}

						if (!calc.tileOnMap(trainer.getLocation())) return State.FAILED;
						return walking.walkTileMM(trainer.getLocation(), 2, 2) ?
								State.COMPLETED : State.PROGRESSING;
					}

					@Override
					State confirm() {
						return getMyPlayer().isMoving() ?
								State.COMPLETED : State.PROGRESSING;
					}

					@Override
					State finish() {
						if (calc.distanceTo(trainer) > random(4, 7))
							antibans.perform(new int[]{Antibans.MOUSE_MOVE_RANDOMLY}, random(17, 23));
						return trainer.isOnScreen() ?
								State.COMPLETED : State.PROGRESSING;
					}
				};

			if (action == Values.ACTION_PICKPOCKET_TRAINER)
				return new Action("Pickpocketing the trainer", Values.ACTION_PICKPOCKET_TRAINER,
						random(250, 1250), random(515, 725)) {

					RSNPC trainer;

					@Override
					State perform() {
						if (trainer == null) {
							if (args.length != 1 || !(args[0] instanceof RSNPC))
								throw new IllegalArgumentException();
							trainer = (RSNPC) args[0];
						}

						if (!trainer.isOnScreen()) return State.FAILED;
						return trainer.doAction(options.isBlackjacking ? "Loot" : "Pickpocket Pickpocketing") ?
								State.COMPLETED : State.PROGRESSING;
					}

					@Override
					State finish() {
						if (hasThieved) {
							hasThieved = false;
							return State.COMPLETED;
						}

						return State.PROGRESSING;
					}
				};

			if (action == Values.ACTION_STUNNED_WAIT)
				return new Action("Waiting while being stunned", Values.ACTION_STUNNED_WAIT,
						0, random(4820, 5210)) {

					@Override
					State perform() {
						return (stunnedTimer != null) ?
								State.COMPLETED : State.FAILED;
					}

					@Override
					State finish() {
						antibans.perform(new int[]{Antibans.MOUSE_MOVE_RANDOMLY}, 21);
						antibans.perform(new int[]{Antibans.ALL_ANTIBANS}, 65);
						return stunnedTimer.isRunning() ?
								State.PROGRESSING : State.COMPLETED;
					}
				};

			if (action == Values.ACTION_EQUIP_GLOVES)
				return new Action("Equipping a new pair of gloves", Values.ACTION_EQUIP_GLOVES,
						random(2345, 2745), random(3215, 3445)) {

					int inventoryCount;

					@Override
					State perform() {
						if (inventoryCount == 0)
							inventoryCount = inventory.getCount();

						final RSItem gloves = inventory.getItem(Values.ITEM_GLOVES_OF_SILENCE);
						if (gloves == null) return State.FAILED;

						return gloves.doAction("Wear") ?
								State.COMPLETED : State.PROGRESSING;
					}

					@Override
					State finish() {
						if (inventory.getCount() != inventoryCount) {
							hasGloves = true;
							return State.COMPLETED;
						}

						return State.PROGRESSING;
					}
				};

			if (action == Values.ACTION_LURE_TRAINER)
				return new Action("Luring the trainer", Values.ACTION_LURE_TRAINER,
						random(250, 1250), random(1455, 2135)) {

					RSNPC trainer;

					@Override
					State perform() {
						if (trainer == null) {
							if (args.length != 1 || !(args[0] instanceof RSNPC))
								throw new IllegalArgumentException();
							trainer = (RSNPC) args[0];
						}

						return trainer.doAction("Lure") ?
								State.COMPLETED : State.PROGRESSING;
					}

					@Override
					State finish() {
						return methods.isLureScreenValid() ?
								State.COMPLETED : State.PROGRESSING;
					}
				};

			if (action == Values.ACTION_LURE_TALK_TRAINER)
				return new Action("Luring/talking to the trainer", Values.ACTION_LURE_TALK_TRAINER,
						random(5550, 6750), random(3225, 4475)) {

					boolean isFirstDone;

					@Override
					State perform() {
						if (!isFirstDone) {
							final RSInterface secondTalkScreen = interfaces.get(Values.INTERFACE_LURE_SECOND);
							isFirstDone = (secondTalkScreen != null && secondTalkScreen.isValid());
						}

						if (isFirstDone) {
							final RSInterface secondTalkScreen = interfaces.get(Values.INTERFACE_LURE_SECOND);
							if (secondTalkScreen != null && secondTalkScreen.isValid()) {
								if (secondTalkScreen.getComponent(5).doClick())
									return State.COMPLETED;
							}
						} else {
							final RSInterface firstTalkScreen = interfaces.get(Values.INTERFACE_LURE_FIRST);
							if (firstTalkScreen != null && firstTalkScreen.isValid())
								isFirstDone = firstTalkScreen.getComponent(5).doClick();
						}

						return State.PROGRESSING;
					}

					@Override
					State finish() {
						final RSInterface secondTalkScreen = interfaces.get(241);
						if (secondTalkScreen == null || !secondTalkScreen.isValid()) {
							isLured = true;
							return State.COMPLETED;
						}

						return State.PROGRESSING;
					}
				};

			if (action == Values.ACTION_KNOCK_TRAINER)
				return new Action("Knocking the trainer", Values.ACTION_KNOCK_TRAINER,
						random(250, 1250), random(2145, 2625)) {

					RSNPC trainer;

					@Override
					State perform() {
						if (trainer == null) {
							if (args.length != 1 || !(args[0] instanceof RSNPC))
								throw new IllegalArgumentException();
							trainer = (RSNPC) args[0];
						}

						return trainer.doAction("Knock-out") ?
								State.COMPLETED : State.PROGRESSING;
					}

					@Override
					State finish() {
						if (trainer.getAnimation() == Values.ANIMATION_KNOCKED_OUT || methods.isStunned() || isForcingBlackjack) {
							isLured = false;
							return State.COMPLETED;
						}

						return State.PROGRESSING;
					}
				};

			if (action == Values.ACTION_BANK_DOOR_WALK)
				return new Action("Walking to the door", Values.ACTION_BANK_DOOR_WALK,
						random(1245, 1485), random(2145, 2675), random(4595, 5780)) {

					@Override
					State perform() {
						return walking.walkTo(new RSTile(4755 + tileOffset.x, 5795 + tileOffset.y)) ?
								State.COMPLETED : State.PROGRESSING;
					}

					@Override
					State confirm() {
						final RSTile destination = walking.getDestination();
						return (destination != null && getMyPlayer().isMoving()) ?
								State.COMPLETED : State.PROGRESSING;
					}

					@Override
					State finish() {
						final RSTile destination = walking.getDestination();
						if (destination == null) return State.FAILED;

						final RSObject door = methods.getOpenDoor();
						if (door != null && door.isOnScreen())
							return State.COMPLETED;

						if (calc.distanceTo(destination) > random(5, 7))
							antibans.perform(new int[]{Antibans.MOUSE_MOVE_RANDOMLY}, random(17, 23));
						return calc.distanceTo(destination) < random(3, 6) ?
								State.COMPLETED : State.PROGRESSING;
					}
				};

			if (action == Values.ACTION_BANK_DOOR_OPEN)
				return new Action("Opening the door", Values.ACTION_BANK_DOOR_OPEN,
						random(4525, 4895), random(2975, 3225)) {

					boolean isCameraSet;

					@Override
					State perform() {
						final RSObject door = methods.getClosedDoor();
						if (door == null) return State.FAILED;

						if (!door.isOnScreen()) {
							if (!isCameraSet) {
								camera.turnTo(door);
								isCameraSet = true;
							} else return State.FAILED;
						}

						return door.doAction("Open") ?
								State.COMPLETED : State.PROGRESSING;
					}

					@Override
					State finish() {
						return methods.isDoorOpen() ?
								State.COMPLETED : State.PROGRESSING;
					}
				};

			if (action == Values.ACTION_BANK_WALK)
				return new Action("Walking to the bank", Values.ACTION_BANK_WALK,
						random(1425, 1635), random(2345, 2755), random(5255, 5825)) {

					RSObject bankBooth;

					@Override
					State perform() {
						bankBooth = objects.getNearest(Values.OBJECT_BANK_BOOTH);
						if (bankBooth == null || !calc.tileOnMap(bankBooth.getLocation()))
							return State.FAILED;

						return walking.walkTileMM(bankBooth.getLocation(), 2, 2) ?
								State.COMPLETED : State.PROGRESSING;
					}

					@Override
					State confirm() {
						final RSTile destination = walking.getDestination();
						if (destination != null) {
							if (calc.distanceBetween(destination, bankBooth.getLocation()) > 3)
								return State.FAILED;

							if (getMyPlayer().isMoving())
								return State.COMPLETED;
						}

						return State.PROGRESSING;
					}

					@Override
					State finish() {
						if (calc.distanceTo(bankBooth) > random(4, 7))
							antibans.perform(new int[]{Antibans.MOUSE_MOVE_RANDOMLY}, random(17, 23));
						return bankBooth.isOnScreen() ?
								State.COMPLETED : State.PROGRESSING;
					}
				};

			if (action == Values.ACTION_BANK_OPEN)
				return new Action("Opening the bank", Values.ACTION_BANK_OPEN,
						random(1425, 1625), random(2452, 2855)) {

					boolean isCameraSet;

					@Override
					State perform() {
						final RSObject bankBooth = objects.getNearest(Values.OBJECT_BANK_BOOTH);
						if (bankBooth == null) return State.FAILED;

						if (!bankBooth.isOnScreen()) {
							if (!isCameraSet) {
								camera.turnTo(bankBooth);
								isCameraSet = true;
							} else return State.FAILED;
						}

						return bankBooth.doAction("Use-quickly") ?
								State.COMPLETED : State.PROGRESSING;
					}

					@Override
					State finish() {
						return bank.isOpen() ?
								State.COMPLETED : State.PROGRESSING;
					}
				};

			if (action == Values.ACTION_BANK_BANKING)
				return new Action("Banking", Values.ACTION_BANK_BANKING,
						random(6445, 7125), random(2745, 3465)) {

					Timer withdrawTimer;
					boolean hasDeposited;

					@Override
					State perform() {
						if (withdrawTimer == null)
							withdrawTimer = new Timer(random(1045, 1425));
						if (withdrawTimer.isRunning())
							return State.PROGRESSING;

						if (!bank.isOpen())
							return State.FAILED;

						if (!hasDeposited && inventory.getCount() > 0)
							hasDeposited = bank.depositAll();

						final RSItem gloves = bank.getItem(Values.ITEM_GLOVES_OF_SILENCE);
						if (gloves == null) {
							if (bank.close()) {
								log("Character is out of gloves of silence.");
								isUsingGloves = false;
								return State.COMPLETED;
							} else return State.PROGRESSING;
						}

						bank.withdraw(Values.ITEM_GLOVES_OF_SILENCE, 0);
						withdrawTimer = new Timer(random(2545, 2895));
						return State.COMPLETED;
					}

					@Override
					State finish() {
						if (withdrawTimer.isRunning()) {
							if (inventory.contains(Values.ITEM_GLOVES_OF_SILENCE))
								withdrawTimer.setEndIn(0);
							else return State.PROGRESSING;
						}

						if (inventory.contains(Values.ITEM_GLOVES_OF_SILENCE)) {
							return bank.close() ? State.COMPLETED
									: State.PROGRESSING;
						}

						return State.FAILED;
					}
				};

			if (action == Values.ACTION_BANK_WALK_AREA)
				return new Action("Walking to the bank area", Values.ACTION_BANK_WALK_AREA,
						random(1245, 1475), random(2345, 2765), random(4985, 5375)) {

					@Override
					State perform() {
						return walking.walkTo(new RSTile(4747 + tileOffset.x, 5795 + tileOffset.y)) ?
								State.COMPLETED : State.PROGRESSING;
					}

					State confirm() {
						return getMyPlayer().isMoving() ?
								State.COMPLETED : State.PROGRESSING;
					}

					@Override
					State finish() {
						final RSObject bankBooth = objects.getNearest(Values.OBJECT_BANK_BOOTH);
						if (bankBooth == null)
							antibans.perform(new int[]{Antibans.MOUSE_MOVE_RANDOMLY}, random(17, 23));

						return (bankBooth != null && calc.tileOnMap(bankBooth.getLocation())) ?
								State.COMPLETED : State.PROGRESSING;
					}
				};

			if (action == Values.ACTION_WALK_TO_TRAINING_AREA)
				return new Action("Walking to the training area", Values.ACTION_WALK_TO_TRAINING_AREA,
						random(1245, 1475), random(2345, 2765), random(4985, 5375)) {

					@Override
					State perform() {
						return walking.walkTo(new RSTile(4763 + tileOffset.x, 5793 + tileOffset.y)) ?
								State.COMPLETED : State.PROGRESSING;
					}

					State confirm() {
						return getMyPlayer().isMoving() ?
								State.COMPLETED : State.PROGRESSING;
					}

					@Override
					State finish() {
						final RSNPC nearestTrainer = npcs.getNearest(methods.getTrainers());
						if (nearestTrainer == null)
							antibans.perform(new int[]{Antibans.MOUSE_MOVE_RANDOMLY}, random(17, 23));

						return (nearestTrainer != null && calc.tileOnMap(nearestTrainer.getLocation())) ?
								State.COMPLETED : State.PROGRESSING;
					}
				};

			if (action == Values.ACTION_FAILSAFE_TIMEOUT)
				return new Action("Failsafe timeout", Values.ACTION_FAILSAFE_TIMEOUT,
						0, random(7200, 13250)) {

					@Override
					State perform() {
						log.fine("Failsafe timeout initiated, time: " + steps.maximumTimes[Steps.FINISH]);
						return State.COMPLETED;
					}

					@Override
					State finish() {
						return methods.isInGuild() ?
								State.COMPLETED : State.PROGRESSING;
					}
				};

			throw new AssertionError("Unsupported action: " + action);
		}
	}

	/**
	 * Provides special actions that are intended to minimize the risk of a ban.
	 */
	private final class Antibans {

		/**
		 * Antiban identifier - moves the mouse randomly across the mainscreen.
		 */
		private static final int MOUSE_MOVE_RANDOMLY = 0;

		/**
		 * Antiban identifier - moves the camera slightly.
		 */
		private static final int CAMERA_MOVE_SLIGHTLY = 1;

		/**
		 * Antiban identifier - hovers the thieving skill.
		 */
		private static final int SKILLS_HOVER_THIEVING = 2;

		/**
		 * Antiban identifier - selects a random tab.
		 */
		private static final int TABS_SELECT_RANDOM = 3;

		/**
		 * Antiban identifier - includes all the possible antibans.
		 */
		private static final int ALL_ANTIBANS = 4;

		/**
		 * The antiban that is currently being performed. A value of <tt>0</tt>
		 * means that no antiban is currently performing.
		 */
		private int currentAntiban;

		/**
		 * A timer for the antibans to be able to do actions over time.
		 */
		private final Timer timer = new Timer(0);

		/**
		 * A counter for the antibans to be able to do multiple actions.
		 */
		private int counter;

		/**
		 * Generates a random number, and if it's the <tt>1/probability</tt>,
		 * it performs a randomly selected antiban from the <tt>selection</tt>.
		 *
		 * @param selection   The identifiers of the possible antibans to perform. To include
		 *                    all antibans, use only the value of <tt>ALL_ANTIBANS</tt>.
		 * @param probability The probability for an antiban to be performed, read as
		 *                    "1 in probability", where probability is the specified value.
		 *                    The minimum allowed probability is 1.
		 * @return <tt>true</tt> if an antiban was, and still is, being performed;
		 *         otherwise <tt>false</tt>.
		 * @throws IllegalArgumentException If the selection is null or if the probability is below one.
		 *                                  Also if an invalid antiban was found in the selection.
		 */
		private boolean perform(final int[] selection, final int probability) throws IllegalArgumentException {
			if (currentAntiban == 0) {
				if (selection == null) throw new IllegalArgumentException("The selection of antibans is null.");
				if (probability < 1) throw new IllegalArgumentException(
						"The probability is below one: " + probability);

				if (selection.length == 0 || random(0, probability) != 0)
					return false;

				currentAntiban = (selection.length == 1 && selection[0] == ALL_ANTIBANS) ?
						random(0, ALL_ANTIBANS) : selection[random(0, selection.length)];
				if (0 > currentAntiban || currentAntiban >= ALL_ANTIBANS)
					throw new IllegalArgumentException("Invalid antiban in selection: " + currentAntiban);
				timer.setEndIn(counter = 0);
			}

			final int mouseSpeed = mouse.getSpeed();
			mouse.setSpeed(random(4, 11));

			switch (currentAntiban) {
				case MOUSE_MOVE_RANDOMLY:
					if (timer.isRunning()) break;
					timer.setEndIn(random(755, 2345));

					if (++counter < random(2, 5))
						mouse.move(random(5, game.getWidth() - 253), random(5, game.getHeight() - 169));
					else currentAntiban = 0;
					break;
				case CAMERA_MOVE_SLIGHTLY:
					camera.setAngle(camera.getAngle() + random(-80, 80));
					currentAntiban = 0;
					break;
				case SKILLS_HOVER_THIEVING:
					if (timer.isRunning()) break;
					if (counter == 0) {
						skills.doHover(Skills.INTERFACE_THIEVING);
						timer.setEndIn(random(1735, 2865));
						counter++;
					} else currentAntiban = 0;
					break;
				case TABS_SELECT_RANDOM:
					final int[] tabs = {Game.TAB_ACHIEVEMENTS, Game.TAB_ATTACK, Game.TAB_CLAN, Game.TAB_CONTROLS,
							Game.TAB_EQUIPMENT, Game.TAB_FRIENDS, Game.TAB_IGNORE, Game.TAB_INVENTORY, Game.TAB_MAGIC,
							Game.TAB_MUSIC, Game.TAB_NOTES, Game.TAB_OPTIONS, Game.TAB_PRAYER, Game.TAB_QUESTS,
							Game.TAB_STATS, Game.TAB_SUMMONING};

					game.openTab(tabs[random(0, tabs.length)]);
					currentAntiban = 0;
					break;
				default:
					throw new AssertionError("Unsupported antiban in selection: " + currentAntiban);
			}

			mouse.setSpeed(mouseSpeed);
			return (currentAntiban != 0);
		}
	}

	/**
	 * Certain conditions, set by the user, that stops the script.
	 */
	private final class Conditions {

		/**
		 * The <tt>Condition</tt>-class provides conditions with specific methods.
		 */
		private abstract class Condition {

			abstract boolean isMet();

			abstract String getMessage();
		}

		/**
		 * The conditions being checked/used throughout this script.
		 */
		private final Condition[] conditions;

		/**
		 * Initializes a new <tt>Conditions</tt>-instance with the
		 * already predefined conditions.
		 */
		private Conditions() {
			conditions = new Condition[]{
					new Condition() {

						@Override
						boolean isMet() {
							return (options.maximumTime > 0 && System.currentTimeMillis() -
									progress.startingTime >= options.maximumTime);
						}

						@Override
						String getMessage() {
							return "The maximum time to run has been reached.";
						}
					},

					new Condition() {

						@Override
						boolean isMet() {
							return (options.maximumPickpockets > 0 && progress.pickpocketCount
									>= options.maximumPickpockets);
						}

						@Override
						String getMessage() {
							return "The maximum pickpockets to perform has been reached.";
						}
					},

					new Condition() {

						@Override
						boolean isMet() {
							return (options.maximumLevels > 0 && skills.getRealLevel(progress.skill) -
									Skills.getLevelAt(progress.startingExp) >= options.maximumLevels);
						}

						@Override
						String getMessage() {
							return "The maximum levels to gain has been reached.";
						}
					}
			};
		}

		/**
		 * Gets if any of the conditions in this script has been met and if
		 * that is the case - returns the message associated with that condition.
		 *
		 * @return The message (String) associated with the {@link Condition} that
		 *         has been met; or <tt>null</tt> if no condition has been met.
		 */
		private String getAnyMet() {
			for (Condition condition : conditions)
				if (condition.isMet())
					return condition.getMessage();
			return null;
		}
	}

	/**
	 * The <tt>Configuration</tt>-class is responsible for storing/retrieving configurations.
	 */
	private final class Configuration {

		/**
		 * The path-name of the node to use for storing/retrieving configuration details.
		 */
		private static final String NODE_PATH_NAME = "/net/vilon/VoluntaryThieve";

		/**
		 * A configuration key - checks if the user has allowed storage of configuration.
		 */
		private static final String KEY_ROOT_ALLOW_STORAGE = NODE_PATH_NAME + "/AllowStorage";

		/**
		 * A configuration key - checks if the user has allowed updates to be checked/downloaded.
		 */
		private static final String KEY_ALLOW_UPDATES = "AllowUpdates";

		/**
		 * The <tt>Preferences</tt> used for handling configurations.
		 */
		private Preferences preferences;

		/**
		 * Sets to <tt>true</tt> if the user has denied storage of preferences once.
		 */
		private boolean hasDeniedOnce;

		/**
		 * Returns the value associated with the specified key in the preference
		 * node. Returns the specified default if there is no value associated
		 * with the key, the backing store is inaccessible, or if the user has
		 * explicitly denied any storage of configuration details.
		 *
		 * @param key Key whose associated value is to be returned.
		 * @param def The default value to return if unable to retrieve a stored value.
		 * @return the value associated with the specified key in the preference
		 *         node. Returns the specified default if there is no value associated
		 *         with the key, the backing store is inaccessible, or if the user has
		 *         explicitly denied any storage of configuration details.
		 */
		private String get(final String key, final String def) {
			if (!initialize()) return def;
			return preferences.get(key, def);
		}

		/**
		 * Associates the specified value with the specified key in the
		 * preference node. Only stores the value if the permission has been
		 * given by the user.
		 *
		 * @param key   Key with which the specified value is to be associated.
		 * @param value The value to be associated with the specified key.
		 */
		private void put(final String key, final String value) {
			if (!initialize()) return;
			preferences.put(key, value);
		}

		/**
		 * Tries to initialize the preferences-object, based on permission
		 * from the user. Returns if the preferences object is initialized.
		 *
		 * @return <tt>true</tt> if the preferences object is initialized;
		 *         otherwise <tt>false</tt>.
		 */
		private boolean initialize() {
			if (preferences == null && !confirmStorage())
				return false;

			if (preferences == null)
				preferences = Preferences.userRoot().node(NODE_PATH_NAME);
			return true;
		}

		/**
		 * Removes the whole associated preference-node, including the key allowing storage.
		 */
		private void removeAll() {
			Preferences currentPreferences = Preferences.userRoot().node("");
			currentPreferences.remove(KEY_ROOT_ALLOW_STORAGE);

			currentPreferences = Preferences.userRoot().node(NODE_PATH_NAME);
			try {
				currentPreferences.removeNode();
			} catch (final Exception ignored) {
			}

			preferences = null;
			hasDeniedOnce = false;
		}

		/**
		 * Asks the user to confirm/allow that configuration can be
		 * stored on the individuals system. If the user has denied
		 * storage once, the user won't be asked again.
		 *
		 * @return <tt>true</tt> if storage of configuration data is
		 *         allowed by the user; otherwise <tt>false</tt>.
		 */
		private boolean confirmStorage() {
			if (hasDeniedOnce) return false;

			final Preferences defaultNode = Preferences.userRoot().node("");
			if (Boolean.parseBoolean(defaultNode.get(KEY_ROOT_ALLOW_STORAGE, "false")))
				return true;

			final int storageAnswer = WindowUtil.showConfirmDialog("Do you give your permission to let the script " +
					"store configuration data?\nThis is used to save your selected options for later usage.", WindowUtil.YES_NO_CANCEL);

			if (storageAnswer == WindowUtil.YES_OPTION) {
				defaultNode.put(KEY_ROOT_ALLOW_STORAGE, "true");
				return true;
			}

			hasDeniedOnce = (storageAnswer == WindowUtil.NO_OPTION);
			return false;
		}
	}

	/**
	 * The graphical user interface for this script.
	 */
	private final class GraphicalInterface extends javax.swing.JFrame {

		/**
		 * GUI
		 */
		private static final long serialVersionUID = 1L;
		/**
		 * <tt>true</tt> means the script should start; otherwise <tt>false</tt>.
		 */
		private boolean shouldStart;

		/**
		 * Creates a new instance of the graphical interface.
		 */
		private GraphicalInterface() {

			/* Sets the look and feel of the frame to system default. */
			try {
				javax.swing.UIManager.setLookAndFeel(
						javax.swing.UIManager.getSystemLookAndFeelClassName());
			} catch (Exception ignored) {
			}

			/* Initialize the frames components. */
			initComponents();
			initSettings();
		}

		/**
		 * Initializes the components of this frame. Mainly generated
		 * from a graphical user interface designer.
		 */
		private void initComponents() {
			generalLabel = new javax.swing.JLabel();
			generalSeparator = new javax.swing.JSeparator();
			modeLabel = new javax.swing.JLabel();
			modeComboBox = new javax.swing.JComboBox();
			getNewGlovesCheckBox = new javax.swing.JCheckBox();
			maxHoursSpinner = new javax.swing.JSpinner();
			maximumRuntimeLabel = new javax.swing.JLabel();
			stopConditionsSeparator = new javax.swing.JSeparator();
			stopConditionsLabel = new javax.swing.JLabel();
			maxLevelsLabel = new javax.swing.JLabel();
			maxHoursLabel = new javax.swing.JLabel();
			maxMinutesSpinner = new javax.swing.JSpinner();
			maxPickpocketsLabel = new javax.swing.JLabel();
			maxLevelsSpinner = new javax.swing.JSpinner();
			maxPickpocketsSpinner = new javax.swing.JSpinner();
			maxSecondsLabel = new javax.swing.JLabel();
			maxMinutesLabel = new javax.swing.JLabel();
			maxSecondsSpinner = new javax.swing.JSpinner();
			otherLabel = new javax.swing.JLabel();
			otherSeparator = new javax.swing.JSeparator();
			enableDebugCheckBox = new javax.swing.JCheckBox();
			startButton = new javax.swing.JButton();
			saveButton = new javax.swing.JButton();
			mainMenuBar = new javax.swing.JMenuBar();
			fileMenu = new javax.swing.JMenu();
			saveMenuItem = new javax.swing.JMenuItem();
			exitSeparator = new javax.swing.JPopupMenu.Separator();
			exitMenuItem = new javax.swing.JMenuItem();
			editMenu = new javax.swing.JMenu();
			resetMenuItem = new javax.swing.JMenuItem();
			runMenu = new javax.swing.JMenu();
			startMenuItem = new javax.swing.JMenuItem();
			helpMenu = new javax.swing.JMenu();
			aboutMenuItem = new javax.swing.JMenuItem();

			setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
			setTitle(scriptManifest.name() + " v" + scriptManifest.version());
			setResizable(false);
			addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					exitActionPerformed(null);
				}
			});

			generalLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
			generalLabel.setText("General");
			generalLabel.setName("generalLabel");

			generalSeparator.setName("generalSeparator");

			modeLabel.setText("Mode:");
			modeLabel.setName("modeLabel");

			modeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Blackjack", "Pickpocket"}));
			modeComboBox.setName("modeComboBox");
			modeComboBox.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
						final boolean selected = (modeComboBox.getSelectedIndex() != 0);
						getNewGlovesCheckBox.setEnabled(selected);
						if (!selected) getNewGlovesCheckBox.setSelected(selected);
					}
				}
			});

			getNewGlovesCheckBox.setText("Get new gloves when run out");
			getNewGlovesCheckBox.setBorder(null);
			getNewGlovesCheckBox.setName("getNewGlovesCheckBox");
			getNewGlovesCheckBox.setEnabled(false);

			maxHoursSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
			maxHoursSpinner.setName("maxHoursSpinner");

			maximumRuntimeLabel.setText("Maximum runtime:");
			maximumRuntimeLabel.setName("maximumRuntimeLabel");

			stopConditionsSeparator.setName("stopConditionsSeparator");

			stopConditionsLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
			stopConditionsLabel.setText("Stop Conditions");
			stopConditionsLabel.setName("stopConditionsLabel");

			maxLevelsLabel.setText("Maximum levels:");
			maxLevelsLabel.setName("maxLevelsLabel");

			maxHoursLabel.setText("H:");
			maxHoursLabel.setName("maxHoursLabel");

			maxMinutesSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 59, 1));
			maxMinutesSpinner.setName("maxMinutesSpinner");

			maxPickpocketsLabel.setText("Maximum pickpockets:");
			maxPickpocketsLabel.setName("maxPickpocketsLabel");

			maxLevelsSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 99, 1));
			maxLevelsSpinner.setName("maxLevelsSpinner");

			maxPickpocketsSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
			maxPickpocketsSpinner.setName("maxPickpocketsSpinner");

			maxSecondsLabel.setText("S");
			maxSecondsLabel.setName("maxSecondsLabel");

			maxMinutesLabel.setText("M:");
			maxMinutesLabel.setName("maxMinutesLabel");

			maxSecondsSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 59, 1));
			maxSecondsSpinner.setName("maxSecondsSpinner");

			otherLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
			otherLabel.setText("Other");
			otherLabel.setName("otherLabel");

			otherSeparator.setName("otherSeparator");

			enableDebugCheckBox.setText("Enable debug");
			enableDebugCheckBox.setBorder(null);
			enableDebugCheckBox.setName("enableDebugCheckBox");

			startButton.setText("Start");
			startButton.setName("startButton");
			startButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					startActionPerformed(evt);
				}
			});

			saveButton.setText("Save");
			saveButton.setName("saveButton");
			saveButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					saveActionPerformed(evt);
				}
			});

			mainMenuBar.setName("mainMenuBar");

			fileMenu.setText("File");
			fileMenu.setName("fileMenu");
			fileMenu.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					saveActionPerformed(evt);
				}
			});

			saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
			saveMenuItem.setText("Save");
			saveMenuItem.setName("saveMenuItem");
			fileMenu.add(saveMenuItem);

			exitSeparator.setName("exitSeparator");
			fileMenu.add(exitSeparator);

			exitMenuItem.setText("Exit");
			exitMenuItem.setName("exitMenuItem");
			exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					exitActionPerformed(evt);
				}
			});
			fileMenu.add(exitMenuItem);

			mainMenuBar.add(fileMenu);

			editMenu.setText("Edit");
			editMenu.setName("editMenu");

			resetMenuItem.setText("Reset Permissions");
			resetMenuItem.setName("resetMenuItem");
			resetMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					resetMenuItemActionPerformed(evt);
				}
			});
			editMenu.add(resetMenuItem);

			mainMenuBar.add(editMenu);

			runMenu.setText("Run");
			runMenu.setName("runMenu");

			startMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
			startMenuItem.setText("Start");
			startMenuItem.setName("startMenuItem");
			startMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					startActionPerformed(evt);
				}
			});
			runMenu.add(startMenuItem);

			mainMenuBar.add(runMenu);

			helpMenu.setText("Help");
			helpMenu.setName("helpMenu");

			aboutMenuItem.setText("About");
			aboutMenuItem.setName("aboutMenuItem");
			aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					aboutMenuItemActionPerformed(evt);
				}
			});
			helpMenu.add(aboutMenuItem);

			mainMenuBar.add(helpMenu);

			setJMenuBar(mainMenuBar);

			javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
			getContentPane().setLayout(layout);
			layout.setHorizontalGroup(
					layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
							.addGroup(layout.createSequentialGroup()
									.addContainerGap()
									.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
											.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
													.addComponent(generalSeparator, javax.swing.GroupLayout.Alignment.LEADING)
													.addComponent(generalLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
											.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
													.addComponent(stopConditionsSeparator)
													.addComponent(stopConditionsLabel))
											.addGroup(layout.createSequentialGroup()
													.addGap(10, 10, 10)
													.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
															.addComponent(getNewGlovesCheckBox)
															.addGroup(layout.createSequentialGroup()
																	.addComponent(modeLabel)
																	.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																	.addComponent(modeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
											.addGroup(layout.createSequentialGroup()
													.addGap(10, 10, 10)
													.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
															.addGroup(layout.createSequentialGroup()
																	.addComponent(maxPickpocketsLabel)
																	.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																	.addComponent(maxPickpocketsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
															.addGroup(layout.createSequentialGroup()
																	.addComponent(maximumRuntimeLabel)
																	.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																	.addComponent(maxHoursSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
																	.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																	.addComponent(maxHoursLabel)
																	.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																	.addComponent(maxMinutesSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
																	.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																	.addComponent(maxMinutesLabel)
																	.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																	.addComponent(maxSecondsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
																	.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																	.addComponent(maxSecondsLabel))
															.addGroup(layout.createSequentialGroup()
																	.addComponent(maxLevelsLabel)
																	.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																	.addComponent(maxLevelsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)))))
									.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
							.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
									.addContainerGap(157, Short.MAX_VALUE)
									.addComponent(saveButton)
									.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(startButton)
									.addContainerGap())
							.addGroup(layout.createSequentialGroup()
									.addContainerGap()
									.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
											.addGroup(layout.createSequentialGroup()
													.addGap(10, 10, 10)
													.addComponent(enableDebugCheckBox))
											.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
													.addComponent(otherSeparator)
													.addComponent(otherLabel)))
									.addContainerGap(184, Short.MAX_VALUE))
			);
			layout.setVerticalGroup(
					layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
							.addGroup(layout.createSequentialGroup()
									.addContainerGap()
									.addComponent(generalLabel)
									.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(generalSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 1, javax.swing.GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
									.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
											.addComponent(modeLabel)
											.addComponent(modeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
									.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(getNewGlovesCheckBox)
									.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
									.addComponent(stopConditionsLabel)
									.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(stopConditionsSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 1, javax.swing.GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
									.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
											.addComponent(maximumRuntimeLabel)
											.addComponent(maxHoursSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
											.addComponent(maxHoursLabel)
											.addComponent(maxMinutesSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
											.addComponent(maxMinutesLabel)
											.addComponent(maxSecondsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
											.addComponent(maxSecondsLabel))
									.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
									.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
											.addComponent(maxPickpocketsLabel)
											.addComponent(maxPickpocketsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
									.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
									.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
											.addComponent(maxLevelsLabel)
											.addComponent(maxLevelsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
									.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
									.addComponent(otherLabel)
									.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(otherSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 1, javax.swing.GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(enableDebugCheckBox)
									.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
											.addComponent(startButton)
											.addComponent(saveButton))
									.addContainerGap())
			);

			pack();
		}

		/**
		 * Initializes the settings of the components. Tries to load
		 * saved settings that the user has saved.
		 */
		private void initSettings() {
			final Component[] components = getContentPane().getComponents();
			for (final Map.Entry<String, String> entry : getOptions().entrySet()) {
				final String storedValue = configuration.get(entry.getKey(), null);

				if (storedValue != null) {
					for (Component component : components) {
						if (entry.getKey().equals(component.getName())) {
							if (component instanceof JComboBox)
								((JComboBox) component).setSelectedItem(storedValue);
							else if (component instanceof JSpinner) {
								try {
									((JSpinner) component).setValue(Integer.parseInt(storedValue));
								} catch (NumberFormatException ignored) {
								}
							} else if (component instanceof JCheckBox)
								((JCheckBox) component).setSelected(Boolean.valueOf(storedValue));
							else throw new AssertionError(entry.getKey() + ": " + storedValue);
							break;
						}
					}
				}
			}
		}

		/**
		 * Gets the options the user has selected in the graphical interface.
		 *
		 * @return A <tt>Map</tt> containing the selected options, where the
		 *         components have been mapped against their respective values.
		 */
		private Map<String, String> getOptions() {
			Map<String, String> options = new java.util.HashMap<String, String>();
			options.put(modeComboBox.getName(), modeComboBox.getSelectedItem().toString());
			options.put(getNewGlovesCheckBox.getName(), String.valueOf(getNewGlovesCheckBox.isSelected()));
			options.put(maxHoursSpinner.getName(), maxHoursSpinner.getValue().toString());
			options.put(maxMinutesSpinner.getName(), maxMinutesSpinner.getValue().toString());
			options.put(maxSecondsSpinner.getName(), maxSecondsSpinner.getValue().toString());
			options.put(maxPickpocketsSpinner.getName(), maxPickpocketsSpinner.getValue().toString());
			options.put(maxLevelsSpinner.getName(), maxLevelsSpinner.getValue().toString());
			options.put(enableDebugCheckBox.getName(), String.valueOf(enableDebugCheckBox.isSelected()));
			return options;
		}

		/**
		 * Gets if the user has chosen to start the script or not.
		 *
		 * @return <tt>true</tt> if the script should start; otherwise <tt>false</tt>.
		 * @throws IllegalStateException If the frame is still visible.
		 */
		private boolean shouldStart() throws IllegalStateException {
			if (isVisible()) throw new IllegalStateException();
			return shouldStart;
		}

		/**
		 * Saves the user-selected options using the configuration-instance.
		 *
		 * @param evt The semantic event which indicates that a component-
		 *            defined action occurred.
		 */
		private void saveActionPerformed(java.awt.event.ActionEvent evt) {
			for (final Map.Entry<String, String> entry : getOptions().entrySet())
				configuration.put(entry.getKey(), entry.getValue());
		}

		/**
		 * Closes the frame and sets the <tt>shouldStart</tt>-flag.
		 *
		 * @param evt The semantic event which indicates that a component-
		 *            defined action occurred.
		 */
		private void startActionPerformed(java.awt.event.ActionEvent evt) {
			shouldStart = true;
			exitActionPerformed(evt);
		}

		/**
		 * Shows a basic informational message in an about-box.
		 *
		 * @param evt The semantic event which indicates that a component-
		 *            defined action occurred.
		 */
		private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
			JOptionPane.showMessageDialog(this, scriptManifest.name() + " v" +
					scriptManifest.version() + " by vilon.\n" + "Visit http://www.powerbot.org/" +
					" for more information.", "About", JOptionPane.INFORMATION_MESSAGE);
		}

		/**
		 * Resets any given permissions and thereby deletes all saved configurations.
		 *
		 * @param evt The semantic event which indicates that a component-
		 *            defined action occurred.
		 */
		private void resetMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
			configuration.removeAll();
		}

		/**
		 * Closes the frame without setting the <tt>shouldStart</tt>-flag.
		 *
		 * @param evt The semantic event which indicates that a component-
		 *            defined action occurred.
		 */
		private void exitActionPerformed(java.awt.event.ActionEvent evt) {
			setVisible(false);
			dispose();
		}

		/* Declaration of the frames components. */
		private javax.swing.JMenuItem aboutMenuItem;
		private javax.swing.JMenu editMenu;
		private javax.swing.JCheckBox enableDebugCheckBox;
		private javax.swing.JMenuItem exitMenuItem;
		private javax.swing.JPopupMenu.Separator exitSeparator;
		private javax.swing.JMenu fileMenu;
		private javax.swing.JLabel generalLabel;
		private javax.swing.JSeparator generalSeparator;
		private javax.swing.JCheckBox getNewGlovesCheckBox;
		private javax.swing.JMenu helpMenu;
		private javax.swing.JMenuBar mainMenuBar;
		private javax.swing.JLabel maxHoursLabel;
		private javax.swing.JSpinner maxHoursSpinner;
		private javax.swing.JLabel maxLevelsLabel;
		private javax.swing.JSpinner maxLevelsSpinner;
		private javax.swing.JLabel maxMinutesLabel;
		private javax.swing.JSpinner maxMinutesSpinner;
		private javax.swing.JLabel maxPickpocketsLabel;
		private javax.swing.JSpinner maxPickpocketsSpinner;
		private javax.swing.JLabel maxSecondsLabel;
		private javax.swing.JSpinner maxSecondsSpinner;
		private javax.swing.JLabel maximumRuntimeLabel;
		private javax.swing.JComboBox modeComboBox;
		private javax.swing.JLabel modeLabel;
		private javax.swing.JLabel otherLabel;
		private javax.swing.JSeparator otherSeparator;
		private javax.swing.JMenuItem resetMenuItem;
		private javax.swing.JMenu runMenu;
		private javax.swing.JButton saveButton;
		private javax.swing.JMenuItem saveMenuItem;
		private javax.swing.JButton startButton;
		private javax.swing.JMenuItem startMenuItem;
		private javax.swing.JLabel stopConditionsLabel;
		private javax.swing.JSeparator stopConditionsSeparator;
	}

	/**
	 * The options provided by the script, that the user is free to modify.
	 */
	private final class Options {

		/**
		 * <tt>true</tt> if blackjacking; otherwise <tt>false</tt>.
		 */
		private final boolean isBlackjacking;

		/**
		 * <tt>true</tt> if banking for new gloves; otherwise <tt>false</tt>.
		 */
		private final boolean isBanking;

		/**
		 * The maximum time to run for before stopping. <tt>0</tt> if not used.
		 */
		private final long maximumTime;

		/**
		 * The maximum successful pickpockets before stopping. <tt>0</tt> if not used.
		 */
		private final int maximumPickpockets;

		/**
		 * The maximum levels to gain before stopping. <tt>0</tt> if not used.
		 */
		private final int maximumLevels;

		/**
		 * <tt>true</tt> if debug is enabled; otherwise <tt>false</tt>.
		 */
		private final boolean isDebugEnabled;

		/**
		 * Creates a new <tt>Options</tt>-class using the passed arguments.
		 *
		 * @param args The arguments passed from a graphical user interface. The
		 *             arguments should always be pre-verified, as no type- or null-checks are done.
		 */
		private Options(final Map<String, String> args) {

			/* Get the general options and store them. */
			isBlackjacking = args.get("modeComboBox").equals("Blackjack");
			isBanking = (!isBlackjacking && Boolean.valueOf(args.get("getNewGlovesCheckBox")));

			/* Read the maximum runtime as entered by the user. */
			long maximumTime = 0;
			final String[] maxLabels = {"maxHoursSpinner", "maxMinutesSpinner", "maxSecondsSpinner"};
			for (int n = 0; n < maxLabels.length; n++)
				maximumTime += Integer.parseInt(args.get(maxLabels[n])) * 1000 * Math.pow(60, 2 - n);
			this.maximumTime = maximumTime;

			/* Read maximum pickpockets and levels. */
			maximumPickpockets = Integer.valueOf(args.get("maxPickpocketsSpinner"));
			maximumLevels = Integer.valueOf(args.get("maxLevelsSpinner"));

			/* Get if debug is enabled, and set log-level. */
			if (isDebugEnabled = Boolean.valueOf(args.get("enableDebugCheckBox")))
				log.setLevel(Level.FINE);
			else log.setLevel(Level.CONFIG);

			/* Set other script options. */
			mouse.setSpeed(5);
		}

		/**
		 * Prints a status-message in the bots log window. This
		 * should normally be called in <code>onStart()</code>.
		 */
		private void print() {

			/* Check if any stop conditions have been set. */
			final boolean isUsingMaximumTime = maximumTime > 0, isUsingMaximumLevels =
					maximumLevels > 0, isUsingMaximumPickpockets = maximumPickpockets > 0;

			/* Print a message confirming the stop conditions, if any was set. */
			if (isUsingMaximumTime || isUsingMaximumLevels || isUsingMaximumPickpockets) {
				String printMessage = isUsingMaximumTime ? "run a maximum " + Timer.format(maximumTime) : "";
				if (isUsingMaximumLevels) printMessage += ((printMessage.length() > 0) ? " or " : "") +
						"gain " + maximumLevels + " levels";
				if (isUsingMaximumPickpockets) printMessage += ((printMessage.length() > 0) ? " or " : "") +
						"perform " + maximumPickpockets + (options.isBlackjacking ? " loots" : " pickpockets");
				log.config("Will " + printMessage + " before stopping.");
			}

			/* Print information about blackjacking/banking. */
			log.config((isBlackjacking ? "Blackjack" : "Pickpocket") +
					" mode is enabled" + (isBanking ? " and will bank for new gloves." : "."));

			/* Show if debug mode was set to enabled. */
			if (isDebugEnabled) log.config("Debug mode is enabled.");
		}
	}

	/**
	 * The <tt>Progress</tt>-class is responsible for statistics, and displaying them.
	 */
	private final class Progress {

		/**
		 * The integer representing the skill being trained in the script.
		 */
		private final int skill = Skills.THIEVING;

		/**
		 * The experience in the skill being trained, when starting.
		 */
		private final int startingExp;

		/**
		 * The systems time, in milliseconds, when starting.
		 */
		private final long startingTime;

		/**
		 * The amount of successfully performed pickpockets/loots.
		 */
		private int pickpocketCount;

		/**
		 * The amount of failed pickpockets/loots.
		 */
		private int pickpocketFailCount;

		/**
		 * The amount of gloves of silence that have worn out.
		 */
		private int glovesUsedCount;

		/**
		 * The amount of times that the script has banked.
		 */
		private int timesBankedCount;

		/**
		 * The amount of times that npcs have been knocked out with a blackjack.
		 */
		private int blackjackKnockCount;

		/**
		 * The amount of times that the player has failed at blackjacking (knocking out).
		 */
		private int blackjackFailCount;

		/**
		 * The {@link java.awt.Rectangle} representing the experience-progress-bar.
		 */
		private Rectangle experienceProgressBar;

		/**
		 * <tt>true</tt> if the real mouse is over the experience-bar; otherwise <tt>false</tt>.
		 */
		private boolean isHovering;

		/**
		 * <tt>true</tt> if the hover-mode is to be toggled; otherwise <tt>false</tt>.
		 */
		private boolean isToggled;

		/**
		 * The <tt>NumberFormat</tt> used for formatting numbers in outputs.
		 */
		private final NumberFormat numberFormat = NumberFormat.getInstance();

		/**
		 * Creates a new <tt>Progress</tt>-instance using the already specified skill.
		 * This should only be called once logged in and all stats have been loaded.
		 */
		private Progress() {
			startingExp = skills.getCurrentExp(skill);
			startingTime = System.currentTimeMillis();
		}

		/**
		 * Paints a progress-report on the specified graphics-object.
		 *
		 * @param g The graphics-object to paint on. Should always be the
		 *          graphics object of the Runescape-client.
		 */
		private void paint(final Graphics g) {

			/* Cast object for rendering extended graphics and turn on antialiasing for text. */
			final Graphics2D graphics2d = (Graphics2D) g;
			final Composite originalComposite = graphics2d.getComposite();
			graphics2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			/* Declare the starting position (x, y) and height for the black background. */
			final int height = 20, startX = 4, startY = game.getHeight() - 165 - height;

			/* Draw the black transparent bar, used as background in the progress-report. */
			graphics2d.setPaint(Color.BLACK);
			graphics2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65F));
			graphics2d.fill(new Rectangle(startX, startY, game.getWidth() - 253, height));
			graphics2d.setComposite(originalComposite);

			/* Declare the padding size and width for the experience progress-bar. */
			final int paddingSize = 2, progressWidth = 100;
			if (experienceProgressBar == null)
				experienceProgressBar = new Rectangle(startX + paddingSize,
						startY + paddingSize, progressWidth, height - 2 * paddingSize - 1);

			/* Draw the experience progress-bar outline on the black bar. */
			graphics2d.setColor(Color.WHITE);
			graphics2d.draw(experienceProgressBar);

			/* Determine percent to next level and select color to use for the content. */
			final int percentToNextLevel = skills.getPercentToNextLevel(skill);
			if (percentToNextLevel < 30) graphics2d.setColor(Color.RED);
			else graphics2d.setColor((percentToNextLevel < 70) ? Color.YELLOW : Color.GREEN);

			/* Draw the content of the experience progress-bar. */
			final int innerProgressWidth = (int) Math.round(((percentToNextLevel / 100D) * progressWidth));
			graphics2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.50F));
			graphics2d.fillRect(startX + paddingSize + 1, startY + paddingSize + 1,
					innerProgressWidth, height - 2 * paddingSize - 2);
			graphics2d.setComposite(originalComposite);

			/* The statistics to be printed right of the experience progress-bar. */
			String drawingString;
			int adjustmentX = 0;

			/* Get statistics that are used in more then one mode. */
			final long timeRunning = System.currentTimeMillis() - startingTime;
			final int currentExp = skills.getCurrentExp(skill),
					gainedExp = currentExp - startingExp;
			final double expPerMillis = gainedExp * (1.0D / timeRunning);

			/* Check if the user is hovering the experience progress-bar. */
			if (isHovering) {

				/* Show the correct graphics. */
				if (isToggled) {

					/* Calculate statistics common for both modes. */
					final int pickpocketRatio = (int) Math.round(pickpocketCount * (100D /
							(pickpocketCount + pickpocketFailCount))),
							pickpocketsPerHour = (int) Math.round(pickpocketCount * (3600000D / timeRunning)),
							knockoutRatio = (int) Math.round(blackjackKnockCount * (100D /
									(blackjackKnockCount + blackjackFailCount)));

					/* Show different strings for different modes. */
					if (options.isBlackjacking) {
						final int blackjacksPerHour = (int) Math.round(blackjackKnockCount * (3600000D / timeRunning));

						drawingString = "SL: " + pickpocketCount + " (" + pickpocketRatio + " %) | LH: " + pickpocketsPerHour +
								" | SK: " + blackjackKnockCount + " (" + knockoutRatio + " %) | FK: " + blackjackFailCount + " | KH: " + blackjacksPerHour;
					} else {
						final int glovesPerHour = (int) Math.round(glovesUsedCount * (3600000D / timeRunning));

						drawingString = "SP: " + pickpocketCount + " (" + pickpocketRatio + " %) | FP: " + pickpocketFailCount +
								" | PH: " + pickpocketsPerHour + " | GU: " + glovesUsedCount + " | GH: " + glovesPerHour +
								(options.isBanking ? " | TB: " + timesBankedCount : "");
					}

					adjustmentX = -1;
				} else {

					/* Calculate statistics that differ from other modes. */
					final int expToNextLevel = skills.getExpToNextLevel(skill),
							expAtNextLevel = currentExp + expToNextLevel,
							timeToNextLevel = (int) Math.round(expToNextLevel / expPerMillis);

					/* Set the statistics that will be shown on the black background. */
					drawingString = "EP: " + currentExp + "/" + expAtNextLevel + " (" + percentToNextLevel + " %) | ER: " +
							expToNextLevel + " | TL: " + Timer.format(timeToNextLevel);
					adjustmentX = -1;
				}
			} else {

				/* Calculate information for display on the black bar. */
				final int realLevel = skills.getRealLevel(skill),
						gainedLevels = realLevel - Skills.getLevelAt(startingExp),
						expPerHour = (int) Math.round(expPerMillis * 3600000D);

				/* Set the statistics that will be shown on the black background. */
				drawingString = "TR: " + Timer.format(timeRunning) + " | CL: " +
						realLevel + " | LG: " + gainedLevels + " | EG: " + gainedExp + " | EH: " + expPerHour;
			}

			/* Show the previously calculated statistics on the black bar. */
			graphics2d.setColor(Color.WHITE);
			graphics2d.setFont(new Font("Consolas", Font.PLAIN, 10));
			graphics2d.drawString(drawingString, startX + paddingSize + progressWidth + 7 + adjustmentX, startY + paddingSize + height / 2 + 1);
		}

		/**
		 * Prints a status-message in the bots log window. This
		 * should normally be called in <code>onFinish()</tt>.
		 * Does only print a message if the user is logged in.
		 */
		private void print() {
			if (!actions.methods.isLoggedIn()) return;

			/* Get the information that is to be printed before finish. */
			final long timeRunning = System.currentTimeMillis() - startingTime;
			final int gainedExp = skills.getCurrentExp(skill) - startingExp,
					gainedLevels = skills.getRealLevel(skill) - Skills.getLevelAt(startingExp);

			/* Print the collected information on a single line, for the user to review. */
			String print = "Gained " + numberFormat.format(gainedExp) + " exp (" + gainedLevels + " " +
					(gainedLevels == 1 ? "level" : "levels") + ") in " +
					Timer.format(timeRunning) + ", performing " +
					numberFormat.format(pickpocketCount) + " ";

			if (options.isBlackjacking) print += (pickpocketCount == 1) ? "loot" : "loots";
			else print += (pickpocketCount == 1) ? "pickpocket" : "pickpockets";
			log(print + ".");
		}

		/**
		 * Processes the {@link java.awt.event.MouseEvent} occurring when
		 * the user presses the mouse.
		 *
		 * @param e The {@link java.awt.event.MouseEvent} occurring when
		 *          the user presses the mouse.
		 */
		private void processMousePressed(final MouseEvent e) {
			if (isHovering) isToggled = !isToggled;
		}

		/**
		 * Processes the {@link java.awt.event.MouseEvent} occurring when
		 * the user moves the mouse.
		 *
		 * @param e The {@link java.awt.event.MouseEvent} occurring when
		 *          the user moves the mouse.
		 */
		private void processMouseMoved(final MouseEvent e) {
			isHovering = (experienceProgressBar != null &&
					experienceProgressBar.contains(e.getPoint()));
			if (!isHovering) isToggled = false;
		}
	}

	/**
	 * The class responsible for updating the script.
	 */
	private final class Update {

		/**
		 * The URL (as a <tt>String</tt>) pointing to the file used for checking updates.
		 */
		private final String URL_VERSION_CHECK = "http://vilon.site90.net/scripts/" +
				VoluntaryThieve.class.getName().toLowerCase() + "/.version";

		/**
		 * The URL (as a <tt>String</tt>) pointing to the file of the latest version of this script.
		 */
		private final String URL_LATEST_VERSION = "http://vilon.site90.net/scripts/" +
				VoluntaryThieve.class.getName().toLowerCase() + "/.latest";

		/**
		 * Gets the latest version from a predefined internet resource.
		 *
		 * @return The latest available version of the script, or <tt>-1</tt>
		 *         if unable to retrieve the latest version.
		 */
		private double getVersion() {
			double latestVersion = -1;
			BufferedReader bufferedReader = null;

			try {
				final URLConnection latestVersionCheck = new URL(URL_VERSION_CHECK).openConnection();
				bufferedReader = new BufferedReader(new InputStreamReader(latestVersionCheck.getInputStream()));
				latestVersion = Double.parseDouble(bufferedReader.readLine());
			} catch (final Exception ignored) {
			} finally {
				if (bufferedReader != null) {
					try {
						bufferedReader.close();
					} catch (final IOException ignored) {
					}
				}
			}

			return latestVersion;
		}

		/**
		 * Downloads any new updates (i.e. the latest version). Handles
		 * all associated user-interactions.
		 *
		 * @return <tt>true</tt> if the latest version was downloaded successfully;
		 *         otherwise <tt>false</tt>.
		 */
		private boolean downloadUpdate() {
			final String savePath = GlobalConfiguration.Paths.getScriptsSourcesDirectory() +
					File.separator + VoluntaryThieve.class.getName();

			BufferedReader bufferedReader = null;
			BufferedWriter bufferedWriter = null;
			boolean isSuccessful = false;

			try {
				URLConnection latestVersion = new URL(URL_LATEST_VERSION).openConnection();
				bufferedReader = new BufferedReader(new InputStreamReader(latestVersion.getInputStream()));
				bufferedWriter = new BufferedWriter(new FileWriter(savePath + ".update"));

				String line;
				while ((line = bufferedReader.readLine()) != null) {
					bufferedWriter.write(line);
					bufferedWriter.newLine();
				}

				isSuccessful = true;
			} catch (final IOException ignored) {
			} catch (final Exception exception) {
				throw new AssertionError(exception.getMessage());
			} finally {
				if (bufferedReader != null) {
					try {
						bufferedReader.close();
					} catch (final IOException ignored) {
					}
				}

				if (bufferedWriter != null) {
					try {
						bufferedWriter.flush();
					} catch (final IOException ignored) {
					}

					try {
						bufferedWriter.close();
					} catch (final IOException ignored) {
					}
				}

				if (!isSuccessful) {
					final File updateFile = new File(savePath + ".update");
					if (updateFile.exists()) {
						if (!updateFile.canWrite() || !updateFile.delete())
							log.warning("Unable to delete: " + savePath + ".update");
					}

					log.warning("Failed when downloading the latest version.");
				}
			}

			if (isSuccessful) {
				final File oldFile = new File(savePath + ".java");
				if (oldFile.exists()) {
					if (!(isSuccessful = oldFile.canWrite() && oldFile.delete()))
						log.warning("Unable to delete: " + oldFile.getPath());
				}

				if (isSuccessful) {
					final File updateFile = new File(savePath + ".update");
					if (isSuccessful = updateFile.renameTo(new File(savePath + ".java"))) {
						log("The latest version has been downloaded successfully.");
						log("Please recompile your scripts and restart the script.");
					} else log.warning("Unable to rename: " + updateFile.getPath());
				}
			}

			if (!isSuccessful) {
				final File updateFile = new File(savePath + ".update");
				if (updateFile.exists()) {
					if (!updateFile.canWrite() || !updateFile.delete())
						log.warning("Unable to delete: " + savePath + ".update");
				}
			}

			return isSuccessful;
		}

		/**
		 * Checks for any updates to this specific script. Handles all
		 * associated user-interactions.
		 *
		 * @return <tt>true</tt> if the latest version was downloaded and
		 *         the script should be stopped (not start); otherwise <tt>false</tt>.
		 */
		private boolean check() {
			String allowUpdates = configuration.get(Configuration.KEY_ALLOW_UPDATES, null);
			if (allowUpdates == null) {
				final int answerAllowUpdates = WindowUtil.showConfirmDialog("Do you give your permission to let the script " +
						"check for updates?", WindowUtil.YES_NO_CANCEL);

				if (answerAllowUpdates == WindowUtil.YES_OPTION) {
					configuration.put(Configuration.KEY_ALLOW_UPDATES, String.valueOf(true));
					allowUpdates = String.valueOf(true);
				} else if (answerAllowUpdates == WindowUtil.NO_OPTION)
					configuration.put(Configuration.KEY_ALLOW_UPDATES, String.valueOf(false));
			}

			if (!Boolean.parseBoolean(allowUpdates))
				return false;

			log("Checking for available updates...");
			final double latestVersion = getVersion();
			if (latestVersion == -1) {
				log.warning("Unable to retrieve information about latest version.");
			} else if (latestVersion <= scriptManifest.version()) {
				log("Script is fully up to date (version " + scriptManifest.version() + ").");
			} else {
				final int answerUpdate = WindowUtil.showConfirmDialog("A new version (v" + latestVersion + ") is available.\n" +
						"Would you like to update now?", WindowUtil.YES_NO_CANCEL);
				if (answerUpdate == WindowUtil.YES_OPTION)
					return downloadUpdate();
			}

			return false;
		}
	}

	/**
	 * The <tt>ScriptManifest</tt> for this script. Used for convenient access to script information.
	 */
	private static final ScriptManifest scriptManifest =
			VoluntaryThieve.class.getAnnotation(ScriptManifest.class);

	/**
	 * Holds all the actions used throughout this script.
	 */
	private final Actions actions = new Actions();

	/**
	 * Holds all the antibans that are used throughout this script.
	 */
	private final Antibans antibans = new Antibans();

	/**
	 * The conditions that stops the script. Usually user-defined.
	 */
	private final Conditions conditions = new Conditions();

	/**
	 * The configuration, used to load and store information/user-options.
	 */
	private final Configuration configuration = new Configuration();

	/**
	 * Holds the options available for the user to customize.
	 */
	private Options options;

	/**
	 * The <tt>Progress</tt> for this script.
	 */
	private Progress progress;

	/**
	 * The current/previous action to be performed.
	 */
	private Action currentAction, previousAction;

	/**
	 * The returned state of the current action.
	 */
	private Action.State actionState;

	/**
	 * <tt>true</tt> if all startup-actions have been performed.
	 */
	private boolean isStartupActionsDone;

	public void messageReceived(final MessageEvent e) {
		if (e.getID() != MessageEvent.MESSAGE_SERVER)
			return;

		final String message = e.getMessage();
		if (message != null) {
			if (message.contains("attempt to"))
				actions.hasThieved = true;
			else if (message.contains("handkerchief"))
				progress.pickpocketCount++;
			else if (message.contains("smack"))
				progress.blackjackKnockCount++;
			else if (message.contains("been stunned") || message.contains("glances")) {
				if (options.isBlackjacking) {
					progress.blackjackFailCount++;
					actions.isForcingBlackjack = true;
				} else {
					progress.pickpocketFailCount++;
					if (actions.stunnedTimer == null)
						actions.stunnedTimer = new Timer(random(4320, 4765));
					else actions.stunnedTimer.setEndIn(random(4320, 4765));
				}
			} else if (message.contains("worn out")) {
				progress.glovesUsedCount++;
				actions.hasGloves = false;
			} else if (message.contains("can't reach")) {
				actions.isUnableToReach = true;
			}
		}
	}

	public void mousePressed(MouseEvent e) {
		if (progress != null && actions.methods.isLoggedIn())
			progress.processMousePressed(e);
	}

	public void mouseMoved(MouseEvent e) {
		if (progress != null && actions.methods.isLoggedIn())
			progress.processMouseMoved(e);
	}

	public void onRepaint(final Graphics render) {
		if (options != null && actions.methods.isLoggedIn()) {
			if (progress == null) progress = new Progress();
			progress.paint(render);
		}
	}

	@Override
	public boolean onStart() {
		if (new Update().check()) return false;

		final GraphicalInterface graphicalInterface = new GraphicalInterface();
		WindowUtil.position(graphicalInterface);
		graphicalInterface.setVisible(true);

		while (graphicalInterface.isVisible()) sleep(1000);
		boolean shouldStart = false;

		try {
			shouldStart = graphicalInterface.shouldStart();
		} catch (IllegalStateException ignored) {
		}
		if (!shouldStart) return false;

		try {
			options = new Options(graphicalInterface.getOptions());
		} catch (IllegalArgumentException exception) {
			log.warning(exception.getMessage());
			return false;
		}

		options.print();
		return true;
	}

	@Override
	public void onFinish() {
		if (progress != null)
			progress.print();
	}

	public int loop() {

		/* Make sure the player is logged in before continuing. */
		if (!actions.methods.isLoggedIn())
			return random(50, 100);

		/* Perform some necessary startup-actions before continuing. */
		if (!isStartupActionsDone) {
			actions.tileOffset = actions.methods.getOffset();
			log.fine("Location offset: " + actions.tileOffset.x + ", " + actions.tileOffset.y);

			camera.setAngle(random(0, 360));
			camera.setPitch(true);
			if (options.isBlackjacking) {
				if (inventory.contains(Actions.Values.ITEM_RUBBER_BLACKJACK))
					inventory.getItem(Actions.Values.ITEM_RUBBER_BLACKJACK).doAction("Wield");
				else if (!equipment.containsAll(Actions.Values.ITEM_RUBBER_BLACKJACK)) {
					log.warning("Character must have a rubber blackjack equipped before starting.");
					return -1;  /* No need to logout. */
				}
			}

			isStartupActionsDone = true;
		}

		/* Check if any stop-conditions have been met; and in that case, stop. */
		if (progress != null) {
			final String conditionMessage = conditions.getAnyMet();
			if (conditionMessage != null) {
				log(conditionMessage);
				stopScript(true);
			}
		}

		/* Initialize the current action if not set. */
		if (currentAction == null && actionState == null)
			currentAction = actions.getNext();

		/* If the current action is null, the script has failed. */
		if (currentAction == null) {
			log.severe("Script failed " + ((previousAction != null) ?
					"when " + previousAction.getName().toLowerCase() : "unexpectedly") + ".");
			stopScript(true);
		}

		/* Get the state returned from the action and get the next action if finished. */
		actionState = currentAction.doAction();
		if (actionState != Action.State.PROGRESSING) {
			log.fine(currentAction.getName() + " " +
					actionState.toString().toLowerCase() + ".");

			/* Clear the list of excluded npcs once we have made a successful pickpocket. */
			if (currentAction.getId() == Actions.Values.ACTION_PICKPOCKET_TRAINER &&
					actionState == Action.State.COMPLETED) {
				actions.trainerInclusions.clear();
			}

			/* Check if the player is no longer in the guild, and the timeout has failed. */
			if (currentAction.getId() == Actions.Values.ACTION_FAILSAFE_TIMEOUT &&
					actionState == Action.State.FAILED) {

				log.warning("The character is not inside the guild.");
				stopScript(true);
			}

			/* Get the next action to perform. */
			previousAction = currentAction;
			currentAction = actions.getNext();

			/* Count the times the character have banked. */
			if (previousAction.getId() == Actions.Values.ACTION_BANK_BANKING &&
					currentAction != null && currentAction.getId() != Actions.Values.ACTION_BANK_BANKING) {
				progress.timesBankedCount++;
			}
		}

		return random(20, 85);
	}

	/* Unused mouse-listeners. */
	public void mouseClicked(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
	}
}