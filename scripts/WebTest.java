import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.script.wrappers.Web;

import javax.swing.*;
import java.awt.*;

/**
 * @author Timer, Aut0r
 */
@ScriptManifest(authors = {"Timer, Aut0r"}, name = "Web Tester",
                description = "Tests the web walking, input a tile like ####,####", keywords = "Development")
public class WebTest extends Script {

	private Web web = null;

	private RSTile tile;

	private final Color C = new Color(255, 140, 0);

	public boolean onStart() {
		final String a = JOptionPane.showInputDialog(null,
		                                             "GE Tile (3165, 3484) \nVarrock (3213, 3424) \nFalador (2964, 3380)\nCamalot (2758, 3480) \nSeer Village (2724, 3492) \nCatherby (2809, 3441) \nLumbridge (3219, 3218) \nDraynor Village (3094, 3244) \nEdgeville (3087, 3496) \nPlease enter a tile for me to walk to?");
		String[] bla = a.split(",");
		if (bla.length != 2) {
			return false;
		}
		tile = new RSTile(Integer.parseInt(bla[0].trim()), Integer.parseInt(bla[1].trim()));
		return true;
	}

	@Override
	public int loop() {
		if (web == null) {
			web = walking.getWebPath(tile);
		} else {
			web.traverse();
			sleep(50);//So it doesn't burn out your CPU.
		}
		if (web.atDestination()) {
			log(C, "I have reached my destination thanks for flying Aut0 Timelines!");
		}
		return web.atDestination() ? -1 : 0;
	}

}