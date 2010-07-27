package org.rsbot.script;

import org.rsbot.Application;
import org.rsbot.bot.Bot;
import org.rsbot.script.randoms.*;
import org.rsbot.util.GlobalConfiguration;
import org.rsbot.util.GlobalFile;
import org.rsbot.util.ScriptClassLoader;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

public class ScriptHandler {
    private static HashMap<Integer, Script> scripts = new HashMap<Integer, Script>();
    private static HashMap<Integer, Thread> scriptThreads = new HashMap<Integer, Thread>();

    public static List<ScriptClassLoader> getLoaders() {
        final List<ScriptClassLoader> loaders = new ArrayList<ScriptClassLoader>();
        final ArrayList<String> paths = new ArrayList<String>(2);
        if (!GlobalConfiguration.RUNNING_FROM_JAR) {
            final String rel = "." + File.separator + GlobalConfiguration.Paths.SCRIPTS_NAME_SRC;
            paths.add(rel);
        } else {
            // Generate the path of the scripts folder in the jar
            final URL version = GlobalConfiguration.class.getClassLoader().getResource(GlobalConfiguration.Paths.Resources.VERSION);
            String p = version.toString().replace("jar:file:", "").replace(GlobalConfiguration.Paths.Resources.VERSION, GlobalConfiguration.Paths.Resources.SCRIPTS);
            try {
                p = URLDecoder.decode(p, "UTF-8");
            } catch (final UnsupportedEncodingException ignored) {
            }
            paths.add(p);
        }
        paths.add(GlobalConfiguration.Paths.getScriptsDirectory());
        paths.add(GlobalConfiguration.Paths.getScriptsPrecompiledDirectory());

        // Add all jar files in the precompiled scripts directory
        final File psdir = new GlobalFile(GlobalConfiguration.Paths.getScriptsPrecompiledDirectory());
        if (psdir.exists()) {
            for (final File file : psdir.listFiles()) {
                if (file.getName().endsWith(".jar!")) {
                    paths.add(file.getPath());
                }
            }
        }

        for (final String path : paths) {
            try {
                final String url = new GlobalFile(path).toURI().toURL().toString();
                loaders.add(new ScriptClassLoader(url, ScriptClassLoader.class.getClassLoader()));
            } catch (final MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return loaders;
    }

    private final ArrayList<Random> randoms = new ArrayList<Random>();
    
    private Bot bot;

    public ScriptHandler(Bot bot) {
    	this.bot = bot;
    }
    
    public void init() {
    	try {
            randoms.add(new LoginBot());
            randoms.add(new BankPins());
            randoms.add(new BeehiveSolver());
            randoms.add(new CapnArnav());
            randoms.add(new Certer());
            randoms.add(new CloseAllInterface());
            randoms.add(new DrillDemon());
            randoms.add(new FreakyForester());
            randoms.add(new FrogCave());
            randoms.add(new GraveDigger());
            randoms.add(new ImprovedRewardsBox());
            randoms.add(new LostAndFound());
            randoms.add(new Maze());
            randoms.add(new Mime());
            randoms.add(new Molly());
            randoms.add(new Exam());
            randoms.add(new Pillory());
            randoms.add(new Pinball());
            randoms.add(new Prison());
            randoms.add(new QuizSolver());
            randoms.add(new SandwhichLady());
            randoms.add(new ScapeRuneIsland());
            randoms.add(new TeleotherCloser());
            randoms.add(new FirstTimeDeath());
            randoms.add(new LeaveSafeArea());
            randoms.add(new SystemUpdate());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    	for (Random r : randoms) {
    		r.init(bot.getMethodContext());
    	}
    }

    private void addScriptToPool(final Script ss, final Thread t) {
        for (int off = 0; off < ScriptHandler.scripts.size(); off++) {
            if (!ScriptHandler.scripts.containsKey(off)) {
                ScriptHandler.scripts.put(off, ss);
                ss.ID = off;
                ScriptHandler.scriptThreads.put(off, t);
                return;
            }
        }
        ss.ID = ScriptHandler.scripts.size();
        ScriptHandler.scripts.put(ScriptHandler.scripts.size(), ss);
        ScriptHandler.scriptThreads.put(ScriptHandler.scriptThreads.size(), t);
    }

    public Collection<Random> getRandoms() {
        return randoms;
    }

    public Map<Integer, Script> getRunningScripts() {
        return Collections.unmodifiableMap(ScriptHandler.scripts);
    }

    public void pauseScript(final int id) {
        final Script s = ScriptHandler.scripts.get(id);
        s.isPaused = !s.isPaused;
    }

    public void removeScript(final int id) {
        if (ScriptHandler.scripts.get(id) == null)
            return;
        ScriptHandler.scripts.get(id).isActive = false;
        ScriptHandler.scripts.remove(id);
        ScriptHandler.scriptThreads.remove(id);
        Application.getGUI().updatePauseButton("Run Script", GlobalConfiguration.Paths.Resources.ICON_PLAY, GlobalConfiguration.Paths.ICON_PLAY);
    }

    public void runScript(final Script ss, final Map<String, String> map) {
    	ss.init(bot.getMethodContext());
        final ScriptManifest prop = ss.getClass().getAnnotation(ScriptManifest.class);
        final Thread t = new Thread(new Runnable() {
            public void run() {
                ss.run(map);
            }
        }, "Script-" + prop.name());
        
        addScriptToPool(ss, t);
        t.start();
    }

    public void stopScript() {
        Thread curThread = Thread.currentThread();
        for (int i = 0; i < ScriptHandler.scripts.size(); i++) {
            if ((ScriptHandler.scripts.get(i) != null) && ScriptHandler.scripts.get(i).isActive) {
                if (ScriptHandler.scriptThreads.get(i) == curThread) {
                    removeScript(i);
                    curThread = null;
                } else {
                    stopScript(i);
                }
            }
        }
        if (curThread == null)
            throw new ThreadDeath();
    }

    @SuppressWarnings("deprecation")
    public void stopScript(final int id) {
        ScriptHandler.scripts.get(id).isActive = false;
        ScriptHandler.scripts.remove(id);
        ScriptHandler.scriptThreads.get(id).stop();
        ScriptHandler.scriptThreads.remove(id);
    }
}
