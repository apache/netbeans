/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.core.startup;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.MissingResourceException;
import org.netbeans.CLIHandler;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Handler for core options.
 * @author Jaroslav Tulach
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.CLIHandler.class)
public class CLIOptions extends CLIHandler {

    /** directory for modules */
    static final String DIR_MODULES = "modules"; // NOI18N

    static boolean defaultsLoaded = false; // package private for testing
    
    /** The flag whether to create the log - can be set via -nologging
    * command line option */
    protected static boolean noLogging = false;

    /** The flag whether to show the Splash screen on the startup */
    private static boolean noSplash;
    
    /* The class of the UIManager to be used for netbeans - can be set by command-line argument -ui <class name> */
    protected static Class uiClass;
    
    static String uiClassName;
    
    /* The size of the fonts in the UI - 0 pt, the default value is set in NbTheme (for Metal L&F), for other L&Fs is set
       in the class Main. The value can be changed in Themes.xml in system directory or by command-line argument -fontsize <size> */
    private static int uiFontSize = 0;
    private static boolean fallbackToMemory;

    /** The netbeans home dir - acquired from property netbeans.home */
    private static String homeDir;
    /** The netbeans user dir - acquired from property netbeans.user */
    private static String userDir;
    /** The netbeans system dir - ${netbeans.user}/system */
    private static String systemDir;
    private static File cacheDir;

    /**
     * Create a default handler.
     */
    public CLIOptions() {
        super(WHEN_BOOT);
    }
    
    protected int cli(Args arguments) {
        return cli(arguments.getArguments());
    }

    private static boolean gui = true;
    
    static boolean isFallbackToMemory() {
        return fallbackToMemory;
    }
    
    static void fallbackToMemory() {
        fallbackToMemory = true;
    }
    
    /** Checks whether we are supposed to use GUI features or not.
     */
    public static boolean isGui () {
        return gui && !GraphicsEnvironment.isHeadless();
    }
    
    private static boolean isOption (String value, String optionName) {
        if (value == null) return false;
        
        if (value.startsWith ("--") && optionName.length() > 1) {
            return value.substring (2).equals (optionName);
        } else if (value.startsWith ("-") && optionName.length() == 1) {
            return value.substring (1).contains(optionName);
        }
        return false;
    }
    
    public final int cli(String[] args) {
        // let's go through the command line
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                continue;
            }
            boolean used = true;
            if (isOption (args[i], "nogui")) { // NOI18N
                gui = false;
            } else if (isOption (args[i], "nosplash")) { // NOI18N
                noSplash = true;
            } else if (isOption (args[i], "noinfo")) { // NOI18N
                // obsolete switch, ignore
            } else if (isOption (args[i], "nologging")) { // NOI18N
                noLogging = true;
            } else if (isOption (args[i], "userdir")) { // NOI18N
                args[i] = null;
                try {
                    String v = args[++i];
                    if (!v.equals(/*Places.MEMORY*/"memory")) {
                        v = FileUtil.normalizeFile(new File(v)).getPath();
                    }
                    userDir = v;
                    System.setProperty("netbeans.user", v);
                } catch(ArrayIndexOutOfBoundsException e) {
                    System.err.println(getString("ERR_UserDirExpected"));
                    return 2;
                }
            } else if (isOption(args[i], "cachedir")) { // NOI18N
                args[i] = null;
                try {
                    cacheDir = FileUtil.normalizeFile(new File(args[++i]));
                } catch(ArrayIndexOutOfBoundsException e) {
                    System.err.println(getString("ERR_UserDirExpected"));
                    return 2;
                }
            } else if (isOption (args[i], "ui") || isOption (args[i], "laf")) { // NOI18N
                args[i] = null;
                try {
                    String ui = args[++i];
                    //Translate L&F ID into L&F class for known IDs
                    if ("Metal".equals(ui)) {
                        ui = "javax.swing.plaf.metal.MetalLookAndFeel";
                    } else if ("GTK".equals(ui)) {
                        ui = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
                    } else if ("Nimbus".equals(ui)) {
                        ui = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
                    } else if ("Windows".equals(ui)) {
                        ui = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
                    } else if ("Aqua".equals(ui)) {
                        ui = "com.apple.laf.AquaLookAndFeel";
                    }
                    uiClassName = ui;
                    uiClass = Class.forName(ui);
                } catch(ArrayIndexOutOfBoundsException e) {
                    System.err.println(getString("ERR_UIExpected"));
                    return 2;
                } catch (ClassNotFoundException e2) {
                    // ignore, will be reported later. Maybe.
                }
            } else if (isOption (args[i], "fontsize")) { // NOI18N
                args[i] = null;
                try {
                    uiFontSize = Integer.parseInt(args[++i]);
                } catch(ArrayIndexOutOfBoundsException e) {
                    System.err.println(getString("ERR_FontSizeExpected"));
                    return 2;
                } catch (NumberFormatException e2) {
                    System.err.println(getString("ERR_BadFontSize"));
                    return 1;
                }
            } else if (isOption (args[i], "locale")) { // NOI18N
                args[i] = null;
                try {
                    String localeParam = args[++i];
                    String language;
                    String country = ""; // NOI18N
                    String variant = ""; // NOI18N
                    int index1 = localeParam.indexOf(":"); // NOI18N
                    if (index1 == -1)
                        language = localeParam;
                    else {
                        language = localeParam.substring(0, index1);
                        int index2 = localeParam.indexOf(":", index1+1); // NOI18N
                        if (index2 != -1) {
                            country = localeParam.substring(index1+1, index2);
                            variant = localeParam.substring(index2+1);
                        }
                        else
                            country = localeParam.substring(index1+1);
                    }
                    Locale.setDefault(new Locale(language, country, variant));
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.err.println(getString("ERR_LocaleExpected"));
                    return 2;
                }

            } else if (isOption (args[i], "branding")) { // NOI18N
                args[i] = null;
                if (++i == args.length) {
                    System.err.println(getString("ERR_BrandingNeedsArgument"));
                    return 2;
                }
                String branding = args[i];
                if (branding.equals("-")) branding = null; // NOI18N
                try {
                    NbBundle.setBranding(branding);
                } catch (IllegalArgumentException iae) {
                    iae.printStackTrace();
                    return 1;
                }
            } else {
                used = false;
            }
            if (used) {
                args[i] = null;
            }
        }
        
        return 0;
    }
    
    /** Initializes logging etc.
     */
    public static void initialize() {
        TopLogging.initialize();
        StartLog.logProgress("TopLogging initialized"); // NOI18N
    }
    
    protected void usage(PrintWriter w) {
        w.println("Core options:");
        w.println("  --laf <LaF classname> use given LookAndFeel class instead of the default");
        w.println("  --fontsize <size>     set the base font size of the user interface, in points");
        w.println("  --locale <language[:country[:variant]]> use specified locale");
        w.println("  --userdir <path>      use specified directory to store user settings");
        w.println("  --cachedir <path>     use specified directory to store user cache, must be different from userdir");
        w.println("  --nosplash            do not show the splash screen");
        w.println("");
//   \  --branding <token>    use specified branding (- for default)
//   
//   \  --nologging           do not create the log file\n\
//   \  --nogui               just start up internals, do not show GUI
    }
    
    private static String getString (String key) {
        return NbBundle.getMessage (CLIOptions.class, key);
    }
    
    //
    // Directory functions
    //
    

    /** Directory to place logs into logging.
    */
    public static String getLogDir () {
        return new File (new File (getUserDir (), "var"), "log").toString ();
    }

    /** Tests need to clear some static variables.
     */
    static final void clearForTests () {
        homeDir = null;
        userDir = null;
    }

    /** Getter for home directory. */
    public static String getHomeDir () {
        if (homeDir == null) {
            homeDir = System.getProperty ("netbeans.home");
        }
        return homeDir;
    }

    /** Getter for user home directory. */
    public static String getUserDir () {
        if (userDir == null || !userDir.equals(System.getProperty("netbeans.user"))) {
            userDir = System.getProperty ("netbeans.user");
            
            if ("memory".equals (userDir)) { // NOI18N
                return "memory"; // NOI18N
            }
            
            if (userDir == null) {
                if (homeDir == null) {
                    return "memory"; // NOI18N
                }
                System.err.println(NbBundle.getMessage(CLIOptions.class, "ERR_no_user_directory"));
                Thread.dumpStack(); // likely to happen from misbehaving unit tests, etc.
                TopLogging.exit(1);
            }

            // #11735, #21085: avoid relative user dirs, or ../ seqs
            File userDirF = FileUtil.normalizeFile(new File(userDir));

            String _homeDir = getHomeDir();
            if (_homeDir != null) {
                File homeDirF = FileUtil.normalizeFile(new File(_homeDir));
                if ((userDirF.getAbsolutePath() + File.separatorChar).startsWith(homeDirF.getParentFile().getAbsolutePath() + File.separatorChar)) {
                    System.err.println(NbBundle.getMessage(CLIOptions.class, "ERR_user_dir_is_inside_home", userDirF, homeDirF.getParentFile()));
                    TopLogging.exit(1);
                }
            }

            userDir = userDirF.getPath();
            System.setProperty("netbeans.user", userDir); // NOI18N
            
            File systemDirFile = new File(userDirF, NbRepository.CONFIG_FOLDER);
            makedir (systemDirFile);
            systemDir = systemDirFile.getAbsolutePath ();
    //        makedir(new File(userDirF, DIR_MODULES)); // NOI18N
        }
        return userDir;
    }
    
    public static File getCacheDir() {
        return cacheDir;
    }

    private static void makedir (File f) {
        if (f.isFile ()) {
            Object[] arg = new Object[] {f};
            System.err.println (NbBundle.getMessage (CLIOptions.class, "CTL_CannotCreate_text", arg));
            org.netbeans.NbExit.exit(6);
        }
        if (! f.exists ()) {
            if (! f.mkdirs ()) {
                Object[] arg = new Object[] {f};
                System.err.println (NbBundle.getMessage (CLIOptions.class, "CTL_CannotCreateSysDir_text", arg));
                org.netbeans.NbExit.exit(7);
            }
        }
    }
    
    /** System directory getter.
    */
    protected static String getSystemDir () {
        getUserDir ();
        return systemDir;
    }

    //
    // other getters
    //
    
    private static void initDefaults() {
	if (!defaultsLoaded) {
	    if (CLIOptions.uiFontSize == 0) {
		String key = "";
		try {
		    key = NbBundle.getMessage (Main.class, "CTL_globalFontSize"); //NOI18N
		} catch (MissingResourceException mre) {
		    //Key not found, nothing to do
		}
		if (key.length() > 0) {
		    try {
			CLIOptions.uiFontSize = Integer.parseInt(key);
		    } catch (NumberFormatException exc) {
			//Incorrect value, nothing to do
		    }
		}
	    }
	    if (!noSplash) {
		// was not overriden from command line - read brandable setting
		String value = NbBundle.getMessage(CLIOptions.class, "SplashOnByDefault");
		noSplash = !Boolean.parseBoolean(value);
	    }
	    
	    defaultsLoaded = true;
	}
    }
    public static int getFontSize () {
	initDefaults();
        return uiFontSize;
    }

    static boolean isNoSplash() {
	initDefaults();
        return noSplash;
    }
}
