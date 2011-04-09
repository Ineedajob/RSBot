import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.script.wrappers.Web;

import javax.swing.*;

/**
 * @author Timer
 */
@ScriptManifest(authors = {"Timer"}, name = "Web Testster", description="Tests the web walking, input a tile like ####,####", keywords = "Development")
public class WebTest extends Script {

	private Web web = null;

	private RSTile tile;

	public boolean onStart() {
		final String a = JOptionPane.showInputDialog(null, "What tile?");
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
		}
		return 0;
	}

}