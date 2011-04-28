package org.rsbot.event.impl;

import org.rsbot.bot.Bot;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.wrappers.RSTile;

import java.awt.*;

public class DrawBoundaries implements PaintListener {

	private final Point[][] minimapPoints = new Point[105][105];
	private final Point[][] screenPoints = new Point[105][105];

	private final MethodContext ctx;

	public DrawBoundaries(Bot bot) {
		this.ctx = bot.getMethodContext();
	}

	public void onRepaint(final Graphics render) {
		if (!ctx.game.isLoggedIn()) {
			return;
		}
		final int blocks[][] = ctx.client.getRSGroundDataArray()[ctx.client.getPlane()].getBlocks();
		final int baseX = ctx.client.getBaseX();
		final int baseY = ctx.client.getBaseY();
		for (int i = 0; i < screenPoints.length; i++) {
			for (int j = 0; j < screenPoints[i].length; j++) {
				final int x = i + baseX - 1;
				final int y = j + baseY - 1;
				Point mini = ctx.calc.worldToMinimap(x - 0.5, y - 0.5);
				if ((mini.x == -1) || (mini.y == -1)) {
					mini = null;
				}
				minimapPoints[i][j] = mini;
				Point screen = ctx.calc.tileToScreen(new RSTile(x, y), 0, 0, 0);
				if ((screen.x == -1) || (screen.y == -1)) {
					screen = null;
				}
				screenPoints[i][j] = screen;
			}
		}

		render.setColor(Color.YELLOW);
		for (int i = 1; i < 104; i++) {
			for (int j = 1; j < 104; j++) {
				final int curBlock = blocks[i][j];
				final Point miniBL = minimapPoints[i][j];
				final Point miniBR = minimapPoints[i][j + 1];
				final Point miniTL = minimapPoints[i + 1][j];
				final Point miniTR = minimapPoints[i + 1][j + 1];
				final Point bl = screenPoints[i][j];
				final Point br = screenPoints[i][j + 1];
				final Point tl = screenPoints[i + 1][j];
				final Point tr = screenPoints[i + 1][j + 1];
				if ((curBlock & 0x1280100) != 0) {
					render.setColor(Color.black);
					if ((tl != null) && (br != null) && (tr != null) && (bl != null)) {
						render.fillPolygon(new int[]{bl.x, br.x, tr.x, tl.x}, new int[]{bl.y, br.y, tr.y, tl.y}, 4);
					}
					if ((miniBL != null) && (miniBR != null) && (miniTR != null) && (miniTL != null)) {
						render.fillPolygon(new int[]{miniBL.x, miniBR.x, miniTR.x, miniTL.x},
								new int[]{miniBL.y, miniBR.y, miniTR.y, miniTL.y}, 4);
					}
				}
				if (((blocks[i][j - 1] & 0x1280102) != 0) || ((curBlock & 0x1280120) != 0)) {
					render.setColor(Color.RED);
					if ((tl != null) && (bl != null)) {
						render.drawLine(bl.x, bl.y, tl.x, tl.y);
					}
					if ((miniBL != null) && (miniTL != null)) {
						render.drawLine(miniBL.x, miniBL.y, miniTL.x, miniTL.y);
					}
				}
				if (((blocks[i - 1][j] & 0x1280108) != 0) || ((curBlock & 0x1280180) != 0)) {
					render.setColor(Color.RED);
					if ((br != null) && (bl != null)) {
						render.drawLine(bl.x, bl.y, br.x, br.y);
					}
					if ((miniBR != null) && (miniBL != null)) {
						render.drawLine(miniBL.x, miniBL.y, miniBR.x, miniBR.y);
					}
				}
				/*
														 * render.setColor(Color.cyan); if ((curBlock & (1<<20)) != 0) {
														 * if (miniBL != null && miniBR != null && miniTR != null &&
														 * miniTL != null) { render.fillPolygon(new
														 * int[]{miniBL.x,miniBR.x,miniTR.x,miniTL.x}, new
														 * int[]{miniBL.y,miniBR.y,miniTR.y,miniTL.y},4); } if (tl !=
														 * null && br != null && tr != null && bl != null) {
														 * render.fillPolygon(new int[]{bl.x,br.x,tr.x,tl.x}, new
														 * int[]{bl.y,br.y,tr.y,tl.y},4); } }
														 */
				// Point miniCent = Calculations.worldToMinimap(i+ baseX, j+
				// baseY);
				// Point cent = Calculations.tileToScreen(i+ baseX, j+ baseY,
				// 0.5,0.5, 0);
				/*
														 * if (cent.x != -1 && cent.y != -1) {
														 * render.setColor(Color.yellow); render.drawString("" +
														 * Calculations.getRealDistanceTo(cur.getX()-baseX,
														 * cur.getY()-baseY, i, j, false), (int)cent.getX(),
														 * (int)cent.getY()); }
														 */
			}
		}
		final Point mini = ctx.players.getMyPlayer().getMinimapLocation();
		render.setColor(Color.red);
		render.fillRect((int) mini.getX() - 1, (int) mini.getY() - 1, 2, 2);
	}
}
