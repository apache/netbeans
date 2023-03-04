/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.core;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.netbeans.core.startup.CLIOptions;
import org.netbeans.core.startup.MainLookup;
import org.netbeans.core.startup.RunLevel;
import org.netbeans.core.startup.Splash;
import org.netbeans.core.startup.StartLog;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * GUI-oriented NetBeans startup logic.
 */
@ServiceProvider(service=RunLevel.class)
public class GuiRunLevel implements RunLevel {
    
    public GuiRunLevel() {
        MainLookup.started();
    }
    
    /** Initialization of the manager.
    */
    public @Override void run() {
        // -----------------------------------------------------------------------------------------------------
        // 10. Loader pool loading
        try {
            NbLoaderPool.load();
        } catch (IOException ioe) {
            Logger.getLogger(GuiRunLevel.class.getName()).log(Level.INFO, null, ioe);
        }
        StartLog.logProgress ("LoaderPool loaded"); // NOI18N
        Splash.getInstance().increment(10);

        NbLoaderPool.installationFinished ();
        StartLog.logProgress ("LoaderPool notified"); // NOI18N
        Splash.getInstance().increment(10);

        if (CLIOptions.isGui()) {
            //---------------------------------------------------------------------------------------------------------
            // initialize main window AFTER the setup wizard is finished

            initializeMainWindow ();
            StartLog.logProgress ("Main window initialized"); // NOI18N
            Splash.getInstance().increment(1);
        } else {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    maybeDie(null);
                }
            });
        }

        NbLifecycleManager.advancePolicy();
        NbAuthenticator.install();

        StartLog.logProgress ("Security managers installed"); // NOI18N
        Splash.getInstance().increment(1);
    }


    /** Method to initialize the main window.
    */
    private void initializeMainWindow() {
        StartLog.logStart ("Main window initialization"); //NOI18N

        TimableEventQueue.initialize();
        
        // -----------------------------------------------------------------------------------------------------
        // 11. Initialization of main window
        StatusDisplayer.getDefault().setStatusText (NbBundle.getMessage (GuiRunLevel.class, "MSG_MainWindowInit"));

        // force to initialize timer
        // sometimes happened that the timer thread was initialized under
        // a TaskThreadGroup
        // such task never ends or, if killed, timer is over
        Timer timerInit = new Timer(0, new java.awt.event.ActionListener() {
              public @Override void actionPerformed(java.awt.event.ActionEvent ev) { }
        });
        timerInit.setRepeats(false);
        timerInit.start();
        Splash.getInstance().increment(10);
        StartLog.logProgress ("Timer initialized"); // NOI18N

    // -----------------------------------------------------------------------------------------------------
    // 14. Open main window
        StatusDisplayer.getDefault().setStatusText (NbBundle.getMessage (GuiRunLevel.class, "MSG_WindowShowInit"));

        // Starts GUI components to be created and shown on screen.
        // I.e. main window + current workspace components.



        // Access winsys from AWT thread only. In this case main thread wouldn't harm, just to be kosher.
        final WindowSystem windowSystem = Lookup.getDefault().lookup(WindowSystem.class);
        if (windowSystem != null) {
            windowSystem.init();
        }
        SwingUtilities.invokeLater(new InitWinSys(windowSystem));
        StartLog.logEnd ("Main window initialization"); //NOI18N
    }

    private static void waitForMainWindowPaint() {
        // Waits for notification about processed paint event for main window
        // require modified java.awt.EventQueue to run succesfully
        Runnable r = new Runnable() {
          public @Override void run() {
              try {
                  Class<?> clz = Class.forName("org.netbeans.modules.performance.guitracker.LoggingRepaintManager"); // NOI18N
                  Method m = clz.getMethod("measureStartup"); // NOI18N
                  Object o = m.invoke(null);
                  endOfStartupMeasuring(o);
              } catch (ClassNotFoundException e) {
                  StartLog.logProgress(e.toString());
              } catch (NoSuchMethodException e) {
                  StartLog.logProgress(e.toString());
        //              } catch (InterruptedException e) {
        //                  StartLog.logProgress(e.toString());
              } catch (IllegalAccessException e) {
                  StartLog.logProgress(e.toString());
              } catch (java.lang.reflect.InvocationTargetException e) {
                  StartLog.logProgress(e.toString());
              }
          }
        };
        new Thread(r).start();
    }
    private static void endOfStartupMeasuring(Object o) {
      StartLog.logProgress("Startup memory and time measured"); // NOI18N
      maybeDie(o);
    }

    private static void maybeDie(Object o) {
        // finish starting
        if (System.getProperty("netbeans.kill") != null) {
            org.netbeans.TopSecurityManager.exit(5);
        }

        // close IDE
        if (System.getProperty("netbeans.close") != null) {
            if (Boolean.getBoolean("netbeans.warm.close")) {
                try {
                    // Do other stuff related to startup, to measure the effect.
                    // synchronous
                    MainLookup.warmUp(0).waitFinished(); // synchronous
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (o != null) {
                StartLog.logMeasuredStartupTime((Long) o);                
            }
            org.openide.LifecycleManager.getDefault().exit();
        }
    }

    private class InitWinSys implements Runnable {

        private final WindowSystem windowSystem;
        private int phase;

        public InitWinSys(WindowSystem windowSystem) {
            this.windowSystem = windowSystem;
        }

        public @Override void run() {
            StartLog.logProgress("Window system initialization");
            if (phase == 0) {
                if (windowSystem != null) {
                    windowSystem.load();
                    StartLog.logProgress("Window system loaded");
                } else {
                    Logger.getLogger(GuiRunLevel.class.getName()).log(Level.WARNING, "Module org.netbeans.core.windows missing, cannot start window system");
                }
                phase = 1;
                SwingUtilities.invokeLater(this);
                return;
            }
            
            if (phase == 1) {
                if (windowSystem != null) {
                    if (StartLog.willLog()) {
                        waitForMainWindowPaint();
                    }
                    windowSystem.show();
                }
                StartLog.logProgress("Window system shown");
                if (!StartLog.willLog()) {
                    maybeDie(null);
                }
                if (System.getProperty("netbeans.warmup.skip") == null && System.getProperty("netbeans.close") == null) {
                    WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                        @Override
                        public void run() {
                            MainLookup.warmUp(1500);
                        }
                    });
                }
                return;
            }
            assert false : "Wrong phase " + phase;
        }
    }

}
