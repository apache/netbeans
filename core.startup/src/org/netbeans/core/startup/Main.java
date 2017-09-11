/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.core.startup;

import java.beans.Introspector;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.ProxyURLStreamHandlerFactory;
import org.netbeans.Stamps;
import org.netbeans.Util;
import org.netbeans.core.startup.layers.SystemFileSystem;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.UserCancelException;
import org.openide.util.Utilities;

/**
 * Main class for NetBeans when run in GUI mode.
 */
public final class Main extends Object {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    /** module subsystem */
    private static ModuleSystem moduleSystem;
    /** module subsystem is fully ready */
    private static boolean moduleSystemInitialized;

  /** Prints the text to splash screen or to status line, if available.
   */
  public static void setStatusText (String msg) {
        Splash.getInstance().print (msg);
        if (moduleSystemInitialized) {
            CoreBridge.getDefault().setStatusText(msg);
        }
  }
  
  /** Starts TopThreadGroup which properly overrides uncaughtException
   * Further - new thread in the group execs main
   */
  public static void main (String[] argv) throws Exception {
    TopThreadGroup tg = new TopThreadGroup ("IDE Main", argv); // NOI18N - programatic name
    StartLog.logStart ("Forwarding to topThreadGroup"); // NOI18N
    tg.start ();
    StartLog.logProgress ("Main.main finished"); // NOI18N
  }


  /** Initializes default stream factory */
  public static void initializeURLFactory () {
      ProxyURLStreamHandlerFactory.register();
  }
  
  /**
   * Sets up the custom font size and theme url for the plaf library to
   * process.
   */
    public static void initUICustomizations() {
      if (!CLIOptions.isGui ()) {
          return;
      }
    
      URL themeURL = null;
      boolean wantTheme = Boolean.getBoolean ("netbeans.useTheme") ||
          CLIOptions.uiClass != null && CLIOptions.uiClass.getName().indexOf("MetalLookAndFeel") >= 0;

      try {
          if (wantTheme) {
              //Put a couple things into UIDefaults for the plaf library to process if it wants
               FileObject fo = FileUtil.getConfigFile("themes.xml"); //NOI18N
               if (fo == null) {            
                    // File on SFS failed --> try to load from a jar from path
                    // /org/netbeans/core/startup/resources/themes.xml
                    try {
                        themeURL = new URL("nbresloc:/org/netbeans/core/startup/resources/themes.xml"); //NOI18N
                        // check whether the file is there:
                        themeURL.openStream().close();
                    } catch (IOException ex) {
                        themeURL = null;
                    }
               } else {
                   themeURL = fo.toURL();
               }
          }
      } finally {
          CoreBridge.getDefault ().initializePlaf(CLIOptions.uiClass, CLIOptions.getFontSize(), themeURL);
      }
      if (CLIOptions.getFontSize() > 0 && "GTK".equals(UIManager.getLookAndFeel().getID())) { //NOI18N
          Util.err.warning(NbBundle.getMessage(Main.class,
          "GTK_FONTSIZE_UNSUPPORTED")); //NOI18N
      }
      StartLog.logProgress("Fonts updated"); // NOI18N
  }
    /** Get and initialize module subsystem.  */
    public static ModuleSystem getModuleSystem() {
        return getModuleSystem(true);
    }
    
    /** Get and possibly initialize module subsystem.  
     * @param init <code>true</code> to initialize the system if it has not been initialized yet
     * @return the module system or <code>null</code> (if the initialization is not requested)
     * @since 1.47
     */
    public static ModuleSystem getModuleSystem(boolean init) {
        synchronized (Main.class) {
            if (moduleSystem != null) {
                return moduleSystem;
            }
            if (!init) {
                return null;
            }

            StartLog.logStart ("Modules initialization"); // NOI18N
            try {
                moduleSystem = new ModuleSystem();
                moduleSystem.init(FileUtil.getConfigRoot().getFileSystem());
                SystemFileSystem.registerMutex(moduleSystem.getManager().mutex());
            } catch (IOException ioe) {
                // System will be screwed up.
                throw (IllegalStateException) new IllegalStateException("Module system cannot be created").initCause(ioe); // NOI18N
            }
            StartLog.logProgress ("ModuleSystem created"); // NOI18N
        }

        moduleSystem.loadBootModules();
        moduleSystem.readList();
        moduleSystem.restore();
        StartLog.logEnd ("Modules initialization"); // NOI18N

        moduleSystemInitialized = true;
        
        return moduleSystem;
    }
    
    /** Is used to find out whether the system has already been fully initialized
     * or not yet. In case you need to access module system that is just being
     * initialized consider using {@link #getModuleSystem(boolean)}.
     * 
     * @return true if changes in the lookup shall mean real changes, false if it just
     *   the first initalization
     */
    public static boolean isInitialized() {
        return moduleSystemInitialized;
    }
    
  
  /**
  * @exception SecurityException if it is called multiple times
  */
  static void start (String[] args) throws SecurityException {
    StartLog.logEnd ("Forwarding to topThreadGroup"); // NOI18N
    StartLog.logStart ("Preparation"); // NOI18N

    // just setup some reasonable values for this deprecated property
    // 6.2 seems to be like the right version as that is the last one
    // that ever saw openide
    System.setProperty ("org.openide.specification.version", "6.2"); // NOI18N
    System.setProperty ("org.openide.version", "deprecated"); // NOI18N
    System.setProperty ("org.openide.major.version", "IDE/1"); // NOI18N

    // In the past we derived ${jdk.home} from ${java.home} by appending
    // "/.." to the end of ${java.home} assuming that JRE is under JDK
    // directory.  It does not always work.  On MacOS X JDK and JRE files
    // are mixed together, thus ${jdk.home} == ${java.home}.  In several
    // Linux distros JRE and JDK are installed at the same directory level
    // with ${jdk.home}/jre a symlink to ${java.home}, which means
    // ${java.home}/.. != ${jdk.home}.
    //
    // Now the launcher can set ${jdk.home} explicitly because it knows
    // best where the JDK is.

    String jdkHome = System.getProperty("jdk.home");  // NOI18N

    if (jdkHome == null) {
        jdkHome = System.getProperty("java.home");  // NOI18N

        if (!Utilities.isMac()) {
            jdkHome += File.separator + "..";  // NOI18N
        }

        System.setProperty("jdk.home", jdkHome);  // NOI18N
    }

    // initialize the URL factory
    initializeURLFactory();
  
    CLIOptions.initialize();
    StartLog.logProgress ("Command line parsed"); // NOI18N


// 5. initialize GUI 
    //Bugfix #35919: Log message to console when initialization of local
    //graphics environment fails eg. due to incorrect value of $DISPLAY
    //on X Windows (Linux, Solaris). In such case IDE will not start
    //so we must inform user about error.
      
    if (CLIOptions.isGui ()) {
        try {
            java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
        } catch (java.lang.InternalError exc) {
            String s = NbBundle.getMessage(Main.class, "EXC_GraphicsStartFails1", exc.getMessage());
            System.out.println(s);
            s = NbBundle.getMessage(Main.class, "EXC_GraphicsStartFails2", CLIOptions.getUserDir() + "/var/log/messages.log");
            System.out.println(s);
            throw exc;
        }
    }
    
    

    InstalledFileLocatorImpl.prepareCache();

    try {
        if (!Boolean.getBoolean("netbeans.full.hack") && !Boolean.getBoolean("netbeans.close")) {
	    // -----------------------------------------------------------------------------------------------------
	    // License check
            if (!handleLicenseCheck()) {
                File userdir = new File(CLIOptions.getUserDir()); // #145936
                // #189656: be conservative about what we delete.
                deleteRec(new File(userdir, "var/cache")); // NOI18N
                deleteRec(new File(userdir, "var/log")); // NOI18N
                rm(new File(userdir, "var")); // NOI18N
                rm(new File(userdir, "config")); // NOI18N
                rm(userdir); // NOI18N
                TopLogging.exit(0);
            }
	    // -----------------------------------------------------------------------------------------------------
	    // Upgrade
            if (!handleImportOfUserDir ()) {
                TopLogging.exit(0);
            }
        }
    } catch (Exception e) {
        Exceptions.printStackTrace(e);
    }
    StartLog.logProgress ("License check performed and upgrade wizard consulted"); // NOI18N

    //
    // 8.5 - we can show the splash only after the upgrade wizard finished
    //

    Splash.getInstance().setRunning(true);

    // -----------------------------------------------------------------------------------------------------

    Splash.getInstance().print(NbBundle.getMessage(Main.class, "MSG_IDEInit"));

    
    // -----------------------------------------------------------------------------------------------------
    // 9. Modules
    
    assert Repository.getDefault() instanceof NbRepository : "Has to be NbRepository: " + Repository.getDefault(); // NOI18N
    getModuleSystem ();
    
    // property editors are registered in modules, so wait a while before loading them
    CoreBridge.getDefault().registerPropertyEditors();
    StartLog.logProgress ("PropertyEditors registered"); // NOI18N
    
    // clean up the cache before --install, --update CLI options are processed
    InstalledFileLocatorImpl.discardCache();

    org.netbeans.Main.finishInitialization();
    StartLog.logProgress("Ran any delayed command-line options"); // NOI18N
    
    for (RunLevel level : Lookup.getDefault().lookupAll(RunLevel.class)) {
        level.run();
    }

    Splash.getInstance().setRunning(false);
    Splash.getInstance().dispose();
    StartLog.logProgress ("Splash hidden"); // NOI18N
    StartLog.logEnd ("Preparation"); // NOI18N
    
    updateAllResources();
    // start to store all caches after 15s
    Stamps.getModulesJARs().flush(15000);
    // initialize life-cycle manager
    LifecycleManager.getDefault();
  }
    private static void deleteRec(File f) throws IOException {
        if (f.isDirectory()) {
            File[] kids = f.listFiles();
            if (kids == null) {
                Logger.getLogger(Main.class.getName()).log(Level.WARNING, "Could not list: {0}", f);
            } else {
                for (File kid : kids) {
                    deleteRec(kid);
                }
            }
        }
        rm(f);
    }
    private static void rm(File f) {
        if (f.exists() && !f.delete()) {
            Logger.getLogger(Main.class.getName()).log(Level.WARNING, "Failed to delete {0}", f);
        }
    }
  
    /** Loads a class from available class loaders. */
    private static final Class getKlass(String cls) {
        try {
            ClassLoader loader;
            ModuleSystem ms = moduleSystem;
            if (ms != null) {
                loader = ms.getManager ().getClassLoader ();
            } else {
                loader = Main.class.getClassLoader ();
            }
            
            return Class.forName(cls, false, loader);
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.getLocalizedMessage());
        }
    }

    /** Does import of userdir. Made non-private just for testing purposes.
     *
     * @return true if the execution should continue or false if it should
     *     stop
     */
    static boolean handleImportOfUserDir () {
        class ImportHandler implements Runnable {
            private File installed = new File (new File (CLIOptions.getUserDir (), "var"), "imported"); // NOI18N
            private String classname;
            private boolean executedOk; 
            
            public boolean shouldDoAnImport () {
                classname = System.getProperty ("netbeans.importclass"); // NOI18N
                
                return classname != null && !installed.exists ();
            }
            
            
            public void run() {
                // This module is included in our distro somewhere... may or may not be turned on.
                // Whatever - try running some classes from it anyway.
                try {
                    Class<?> clazz = getKlass (classname);
                
                    // Method showMethod = wizardClass.getMethod( "handleUpgrade", new Class[] { Splash.SplashOutput.class } ); // NOI18N
                    Method showMethod = clazz.getMethod( "main", String[].class ); // NOI18N
                    showMethod.invoke (null, new Object[] {
                        new String[0]
                    });
                    executedOk = true;
                } catch (InvocationTargetException ex) {
                    // canceled by user, all is fine
                    if (ex.getTargetException() instanceof UserCancelException) {
                        executedOk = true;
                    } else {
                        LOG.log(Level.WARNING, null, ex);
                    }
                } catch (Exception e) {
                    // If exceptions are thrown, notify them - something is broken.
                    LOG.log(Level.WARNING, null, e);
                } catch (LinkageError e) {
                    // These too...
                    LOG.log(Level.WARNING, null, e);
                }
            }
            
            
            public boolean canContinue () {
                if (shouldDoAnImport ()) {
                    try {
                        SwingUtilities.invokeAndWait (this);
                        if (executedOk) {
                            // if the import went fine, then we are fine
                            // just create the file
                            installed.getParentFile ().mkdirs ();
                            installed.createNewFile ();
                            return true;
                        } else {
                            return false;
                        }
                    } catch (IOException ex) {
                        // file was not created a bit of problem but go on
                        LOG.log(Level.WARNING, null, ex);
                        return true;
                    } catch (java.lang.reflect.InvocationTargetException ex) {
                        return false;
                    } catch (InterruptedException ex) {
                        LOG.log(Level.WARNING, null, ex);
                        return false;
                    }
                } else {
                    // if there is no need to upgrade that every thing is good
                    return true;
                }
            }
        }
        
        
        ImportHandler handler = new ImportHandler ();
        
        return handler.canContinue ();
    }
    
    /** Displays license to user to accept if necessary. Made non-private just for testing purposes.
     *
     * @return true if the execution should continue or false if it should
     * stop
     */
    static boolean handleLicenseCheck () {
        class LicenseHandler implements Runnable {
            private String classname;
            private boolean executedOk; 
            
            /** Checks if licence was accepted already or not. */
            public boolean shouldDisplayLicense () {
                if (licenseFileExists()) return false;
                classname = System.getProperty("netbeans.accept_license_class"); // NOI18N
                return (classname != null);
            }
            
            @Override
            public void run() {
                // This module is included in our distro somewhere... may or may not be turned on.
                // Whatever - try running some classes from it anyway.
                try {
                    Class<?> clazz = getKlass (classname);
                
                    Method showMethod = clazz.getMethod("showLicensePanel"); // NOI18N
                    showMethod.invoke (null, new Object [] {});
                    executedOk = true;
                    //User accepted license => create file marker in userdir
                    File f = new File(new File(CLIOptions.getUserDir(), "var"), "license_accepted"); // NOI18N
                    if (!f.exists()) {
                        f.getParentFile().mkdirs();
                        try {
                            f.createNewFile();
                            if (!licenseFileExists()) {
                                throw new IOException("InstalledFileLocator can't find " + f); // NOI18N
                            }
                        } catch (IOException exc) {
                            LOG.log(Level.WARNING, null, exc);
                        }
                    }
                } catch (InvocationTargetException ex) {
                    // canceled by user, all is fine
                    if (ex.getTargetException() instanceof UserCancelException) {
                        executedOk = false;
                    } else {
                        LOG.log(Level.WARNING, null, ex);
                    }
                } catch (Exception ex) {
                    // If exceptions are thrown, notify them - something is broken.
                    LOG.log(Level.WARNING, null, ex);
                } catch (LinkageError ex) {
                    // These too...
                    LOG.log(Level.WARNING, null, ex);
                }
            }
            
            public boolean canContinue () {
                if (shouldDisplayLicense()) {
                    try {
                        SwingUtilities.invokeAndWait(this);
                        if (executedOk) {
                            return true;
                        } else {
                            return false;
                        }
                    } catch (java.lang.reflect.InvocationTargetException ex) {
                        return false;
                    } catch (InterruptedException ex) {
                        LOG.log(Level.WARNING, null, ex);
                        return false;
                    }
                } else {
                    // if there is no need to upgrade that every thing is good
                    return true;
                }
            }

            private boolean licenseFileExists() {
                for (File cluster : InstalledFileLocatorImpl.computeDirs()) {
                    File f = new File(new File(cluster, "var"), "license_accepted"); // NOI18N
                    if (f.exists()) {
                        return true;
                    }
                }
                return false;
            }
        }
                
        LicenseHandler handler = new LicenseHandler ();
        
        return handler.canContinue ();
    }

    static boolean updateAllResources() {
        String value = System.getProperty("org.netbeans.core.update.all.resources", "always");
        if (!"never".equals(value)) { // NOI18N
            if ("missing".equals(value)) { // NOI18N
                if (!org.netbeans.JarClassLoader.isArchivePopulated()) {
                    org.netbeans.JarClassLoader.saveArchive();
                    return true;
                }
            } else {
                assert "always".equals(value); // NOI18N
                org.netbeans.JarClassLoader.saveArchive();
                return true;
            }
        }
        return false;
    }
}
