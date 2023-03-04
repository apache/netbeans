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

package org.netbeans.core.startup;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import org.netbeans.Events;
import org.netbeans.Module;
import org.netbeans.Util;
import static org.netbeans.core.startup.Bundle.*;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;

/** Report events to the performance logger, status text/splash screen,
 * console, and so on.
 * @author Jesse Glick
 */
final class NbEvents extends Events {
    private static final Logger logger = Logger.getLogger(NbEvents.class.getName());

    private int moduleCount;
	
    private int counter;

    /** Handle a logged event.
     * CAREFUL that this is called synchronously, usually within a write
     * mutex or other sensitive environment. So do not call anything
     * blocking (like TM.notify) directly. TM.setStatusText and printing
     * to console are fine, as well as performance logging.
     */
    @Messages({
        "MSG_start_load_boot_modules=Loading core...",
        "MSG_finish_load_boot_modules=Loading core...done.",
        "MSG_start_auto_restore=Loading modules...",
        "MSG_finish_auto_restore=Done loading modules.",
        "MSG_start_enable_modules=Turning on modules...",
        "TEXT_finish_enable_modules=Turning on modules:",
        "# {0} = startlevel",
        "MSG_startlevelinfo=, startlevel={0}",
        "MSG_finish_enable_modules=Turning on modules...done.",
        "MSG_start_disable_modules=Turning off modules...",
        "TEXT_finish_disable_modules=Turning off modules:",
        "MSG_finish_disable_modules=Turning off modules...done.",
        "# {0} - file name ", "TEXT_start_deploy_test_module=Deploying test module in {0} ...",
        "TEXT_finish_deploy_test_module=Finished deploying test module.",
        "MSG_failed_install_new=Warning - could not install some modules:",
        "# {0} - module display name", "MSG_failed_install_new_unexpected=Warning - could not install module {0}",
        "MSG_start_read=Reading module storage...",
        "MSG_finish_read=Reading module storage...done.",
        "MSG_restore=Starting modules...",
        "# #13806: might have been installed before and uninstalled.", "# {0} - module display name", "MSG_install=Installing {0}",
        "# {0} - module display name", "TEXT_install=Installing new module: {0}",
        "# {0} - module display name", "MSG_update=Starting a new version of {0}",
        "# {0} - module display name", "TEXT_update=Updating module: {0}",
        "# {0} - module display name", "MSG_uninstall=Stopping {0}",
        "MSG_load_section=Loading modules...",
        "MSG_load_layers=Loading module services...",
        "# {0} - path to expected module JAR", "TEXT_missing_jar_file=Warning: the module {0} could not be found, ignoring...",
        "# {0} - module display name", "TEXT_cant_delete_enabled_autoload=The module {0} could not be deleted as it was an autoload or eager module and still in use.",
        "# {0} - module display name", "# {1} - property name in XML", "# {2} - value on disk in XML", "# {3} - actual value", "TEXT_misc_prop_mismatch=An attempt was made to change the property {1} of the module {0} in the system/Modules folder.\nThe actual value is \"{3}\" but it was changed on disk to read \"{2}\".\nThis property cannot be changed while the IDE is running, so this attempt had no effect.",
        "# {0} - JAR file name", "TEXT_patch=Module patch or custom extension: {0}"
    })
    @Override protected void logged(final String message, Object[] args) {
        if (message.equals(PERF_TICK)) {
            StartLog.logProgress( (String)args[0]);
        } else if (message.equals(PERF_START)) {
            StartLog.logStart( (String)args[0]);
        } else if (message.equals(PERF_END)) {
            StartLog.logEnd( (String)args[0]);
        } else if (message.equals(START_CREATE_BOOT_MODULE)) {
            org.netbeans.core.startup.Splash.getInstance().increment(1);
        } else if (message.equals(START_LOAD_BOOT_MODULES)) {
            setStatusText(
                MSG_start_load_boot_modules());
            StartLog.logStart("ModuleSystem.loadBootModules"); // NOI18N
        } else if (message.equals(START_LOAD)) {
            StartLog.logStart("NbInstaller.load"); // NOI18N
        } else if (message.equals(FINISH_LOAD_BOOT_MODULES)) {
            setStatusText(
                MSG_finish_load_boot_modules());
            StartLog.logEnd( "ModuleSystem.loadBootModules" ); // NOI18N
        } else if (message.equals(FINISH_LOAD)) {
            StartLog.logEnd("NbInstaller.load"); // NOI18N
        } else if (message.equals(START_AUTO_RESTORE)) {
            Set<?> modules = (Set) args[0];
            if (! modules.isEmpty()) {
                setStatusText(
                    MSG_start_auto_restore());
            }
        } else if (message.equals(FINISH_AUTO_RESTORE)) {
            setStatusText(
                MSG_finish_auto_restore());
        } else if (message.equals(START_ENABLE_MODULES)) {
            setStatusText(
                MSG_start_enable_modules());
        } else if (message.equals(FINISH_ENABLE_MODULES)) {
            List<Module> modules = NbCollections.checkedListByCopy((List) args[0], Module.class, true);
            if (! modules.isEmpty()) {
                logger.log(Level.INFO, TEXT_finish_enable_modules());
                dumpModulesList(modules);
            }
            setStatusText(
                MSG_finish_enable_modules());
            StartLog.logEnd("ModuleManager.enable"); // NOI18N
        } else if (message.equals(START_DISABLE_MODULES)) {
            setStatusText(
                MSG_start_disable_modules());
        } else if (message.equals(FINISH_DISABLE_MODULES)) {
            List<Module> modules = NbCollections.checkedListByCopy((List<?>) args[0], Module.class, true);
            if (! modules.isEmpty()) {
                logger.log(Level.INFO, TEXT_finish_disable_modules());
                dumpModulesList(modules);
            }
            setStatusText(
                MSG_finish_disable_modules());
        } else if (message.equals(START_DEPLOY_TEST_MODULE)) {
            // No need to print anything. ModuleSystem.deployTestModule prints
            // its own stuff (it needs to be printed synchronously to console
            // in order to appear in the output window). But status text is OK.
            // Fix for IZ#81566 - I18N: need to localize status messages for module dev
            String msg = TEXT_start_deploy_test_module((File) args[0]);
            setStatusText( msg ); 
        } else if (message.equals(FINISH_DEPLOY_TEST_MODULE)) {
            // Fix for IZ#81566 - I18N: need to localize status messages for module dev
            setStatusText(
                    TEXT_finish_deploy_test_module());
        } else if (message.equals(FAILED_INSTALL_NEW)) {
            Set<Module> modules = NbCollections.checkedSetByCopy((Set) args[0], Module.class, true);
            {
                StringBuilder buf = new StringBuilder(MSG_failed_install_new());
                NbProblemDisplayer.problemMessagesForModules(buf, modules, false);
                buf.append('\n'); // #123669
                logger.log(Level.INFO, buf.toString());
            }
            {
                StringBuilder buf = new StringBuilder(MSG_failed_install_new());
                NbProblemDisplayer.problemMessagesForModules(buf, modules, true);
                String msg = buf.toString();
                notify(msg, true);
            }
            setStatusText("");
        } else if (message.equals(FAILED_INSTALL_NEW_UNEXPECTED)) {
            Module m = (Module)args[0];
            List<Module> modules = new ArrayList<Module> ();
            modules.add (m);
            modules.addAll (NbCollections.checkedSetByCopy((Set) args[1], Module.class, true));
            // ignore args[2]: InvalidException
            {
                StringBuilder buf = new StringBuilder(MSG_failed_install_new_unexpected(m.getDisplayName()));
                NbProblemDisplayer.problemMessagesForModules(buf, modules, false);
                buf.append('\n');
                logger.log(Level.INFO, buf.toString());
            }

            {
                notify(NbProblemDisplayer.messageForProblem (m, m.getProblems ().iterator ().next (), true), true);
            }
            setStatusText("");
        } else if (message.equals(START_READ)) {
            setStatusText(
                MSG_start_read());
            StartLog.logStart("ModuleList.readInitial"); // NOI18N
        } else if (message.equals(MODULES_FILE_SCANNED)) {
	    moduleCount = (Integer)args[0];
            Splash.getInstance().addToMaxSteps(Math.max(moduleCount + moduleCount/2 - 100, 0));
        } else if (message.equals(MODULES_FILE_PROCESSED)) {
            Splash.getInstance().increment(1);
            if (StartLog.willLog()) {
                StartLog.logProgress("module " + args[0] + " processed"); // NOI18N
            }
        } else if (message.equals(FINISH_READ)) {
	    if (moduleCount < 100) {
		Splash.getInstance().increment(moduleCount - 100);
	    }
            setStatusText(
                MSG_finish_read());
            StartLog.logEnd("ModuleList.readInitial"); // NOI18N
        } else if (message.equals(RESTORE)) {
            // Don't look for display name. Just takes too long.
            setStatusText(
                MSG_restore(/*, ((Module)args[0]).getDisplayName()*/));
	    if (++counter < moduleCount / 2) {
		Splash.getInstance().increment(1);
	    }
        } else if (message.equals(INSTALL)) {
            // Nice to see the real title; not that common, after all.
            setStatusText(
                MSG_install(((Module)args[0]).getDisplayName()));
            logger.log(Level.INFO, TEXT_install(((Module)args[0]).getDisplayName()));
        } else if (message.equals(UPDATE)) {
            setStatusText(
                MSG_update(((Module)args[0]).getDisplayName()));
            logger.log(Level.INFO, TEXT_update(((Module)args[0]).getDisplayName()));
        } else if (message.equals(UNINSTALL)) {
            setStatusText(
                MSG_uninstall(((Module)args[0]).getDisplayName()));
        } else if (message.equals(LOAD_SECTION)) {
            // Again avoid finding display name now.
            setStatusText(
                MSG_load_section(/*, ((Module)args[0]).getDisplayName()*/));
	    if (++counter < moduleCount / 4) {
		Splash.getInstance().increment(1);
	    }
        } else if (message.equals(LOAD_LAYERS)) {
            setStatusText(
                MSG_load_layers());
        } else if (message.equals(WRONG_CLASS_LOADER)) {
            if (! Boolean.getBoolean("netbeans.moduleitem.dontverifyclassloader") && Util.err.isLoggable(Level.WARNING)) { // NOI18N
                Class<?> clazz = (Class) args[1];
                // Message for developers, no need for I18N.
                StringBuilder b = new StringBuilder();
                b.append("The module ").append(((Module) args[0]).getDisplayName()).append(" loaded the class ").append(clazz.getName()).append("\n"); // NOI18N
                b.append("from the wrong classloader. The expected classloader was ").append(args[2]).append("\n"); // NOI18N
                b.append("whereas it was actually loaded from ").append(clazz.getClassLoader()).append("\n"); // NOI18N
                b.append("Usually this means that some classes were in the startup classpath.\n"); // NOI18N
                b.append("To suppress this message, run with: -J-Dnetbeans.moduleitem.dontverifyclassloader=true"); // NOI18N
                Util.err.warning(b.toString());
            }
        } else if (message.equals(EXTENSION_MULTIPLY_LOADED)) {
            // Developer-oriented message, no need for I18N.
            logger.log(Level.WARNING, "The extension {0} may be multiply loaded by modules: {1}; see: http://www.netbeans.org/download/dev/javadoc/org-openide-modules/org/openide/modules/doc-files/classpath.html#class-path", new Object[] {(File) args[0], (Set<?/*File*/>) args[1]});
        } else if (message.equals(MISSING_JAR_FILE)) {
            File jar = (File)args[0];
            Level level = Boolean.FALSE.equals(args[1]) ? Level.FINE : Level.INFO;
            logger.log(level, TEXT_missing_jar_file(jar.getAbsolutePath()));
        } else if (message.equals(CANT_DELETE_ENABLED_AUTOLOAD)) {
            Module m = (Module)args[0];
            logger.log(Level.INFO, TEXT_cant_delete_enabled_autoload(m.getDisplayName()));
        } else if (message.equals(MISC_PROP_MISMATCH)) {
            // XXX does this really need to be logged to the user?
            // Or should it just be sent quietly to the log file?
            Module m = (Module)args[0];
            String prop = (String)args[1];
            Object onDisk = args[2];
            Object inMem = args[3];
            logger.log(Level.INFO, TEXT_misc_prop_mismatch(m.getDisplayName(), prop, onDisk, inMem));
        } else if (message.equals(PATCH)) {
            File f = (File)args[0];
            logger.log(Level.INFO, TEXT_patch(f.getAbsolutePath()));
        }
        // XXX other messages?
    }

    /** Print a nonempty list of modules to console (= log file).
     * @param modules the modules
     */
    private void dumpModulesList(Collection<Module> modules) {
        if (modules.isEmpty()) {
            throw new IllegalArgumentException();
        }
        StringBuilder buf = new StringBuilder(modules.size() * 100 + 1);
        String lineSep = System.getProperty("line.separator");
        for (Module m : modules) {
            buf.append('\t'); // NOI18N
            buf.append(m.getCodeName());
            buf.append(" ["); // NOI18N
            SpecificationVersion sv = m.getSpecificationVersion();
            if (sv != null) {
                buf.append(sv);
            }
            String iv = m.getImplementationVersion();
            if (iv != null) {
                buf.append(' '); // NOI18N
                buf.append(iv);
            }
            String bv = m.getBuildVersion();
            if (bv != null && !bv.equals (iv)) {
                buf.append(' '); // NOI18N
                buf.append(bv);
            }
            buf.append(']'); // NOI18N
            int startlevel = m.getStartLevel();
            if (startlevel != -1) {
                buf.append(MSG_startlevelinfo(startlevel));
            }
            // #32331: use platform-specific newlines
            buf.append(lineSep);
        }
        logger.log(Level.INFO, buf.toString());
    }
    
    private void notify(String text, boolean warn) {
        if (GraphicsEnvironment.isHeadless() || Boolean.getBoolean("netbeans.full.hack")) { // NOI18N
            // #21773: interferes with automated GUI testing.
            logger.log(Level.WARNING, "{0}\n", text);
        } else {
            // Normal - display dialog.
            new Notifier(text, warn).show();
        }
    }
    private static final class Notifier implements Runnable {
        private static boolean showDialog = true;
        
        private boolean warn;
        private String text;
        private static RequestProcessor RP = new RequestProcessor("Notify About Module System"); // NOI18N
        
        public Notifier(String text, boolean type) {
            this.warn = type;
            this.text = text;
        }
        void show() {
            if (showDialog) {
                showDialog = false;
                if (EventQueue.isDispatchThread()) {
                    run();
                } else {
                    RP.post(this, 0, Thread.MIN_PRIORITY).waitFinished ();
                }
            }
        }

        @Messages({
            "MSG_warning=Warning",
            "MSG_info=Information",
            "MSG_continue=Disable Modules and Continue",
            "MSG_exit=Exit"
        })
        @Override public void run() {
            int type = warn ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE;
            String msg = warn ? MSG_warning() : MSG_info();

            Splash out = Splash.getInstance();
            final Component c = out.getComponent() == null ? null : out.getComponent();
            try {
                UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName ());
            } catch (ClassNotFoundException ex) {
                logger.log(Level.INFO, null, ex);
            } catch (InstantiationException ex) {
                logger.log(Level.INFO, null, ex);
            } catch (IllegalAccessException ex) {
                logger.log(Level.INFO, null, ex);
            } catch (UnsupportedLookAndFeelException ex) {
                logger.log(Level.INFO, null, ex);
            }
            JTextPane tp = new JTextPane ();
            tp.setContentType("text/html"); // NOI18N
            text = text.replace ("\n", "<br>"); // NOI18N

            tp.setEditable(false);
            tp.setOpaque (false);
            tp.setEnabled(true);
            tp.addHyperlinkListener(new HyperlinkListener() {
                @Override public void hyperlinkUpdate(HyperlinkEvent hlevt) {
                    if (EventType.ACTIVATED == hlevt.getEventType()) {
                        assert hlevt.getURL() != null;
                        try {
                            Desktop.getDesktop().browse(hlevt.getURL().toURI());
                        } catch (Exception ex) {
                            logger.log(Level.INFO, null, ex);
                        }
                    }
                }
            });

            tp.setText (text);
            
            JComponent sp;
            if (tp.getPreferredSize ().width > 600 || tp.getPreferredSize ().height > 400) {
                tp.setPreferredSize (new Dimension (600, 400));
                sp = new JScrollPane (tp);
            } else {
                sp = tp;
            }
            final JOptionPane op = new JOptionPane (sp, type, JOptionPane.YES_NO_OPTION, null);

            JButton continueButton = new JButton(MSG_continue());
            continueButton.setDisplayedMnemonicIndex (0);
            continueButton.addActionListener (new ActionListener () {
                @Override public void actionPerformed (ActionEvent e) {
                    op.setValue (0);
                }
            });
            JButton exitButton = new JButton(MSG_exit());
            exitButton.addActionListener (new ActionListener () {
                @Override public void actionPerformed (ActionEvent e) {
                    op.setValue (1);
                }
            });

            Object [] options = new JButton [] {continueButton, exitButton};
            op.setOptions (options);
            op.setInitialValue (options [1]);
            JDialog d = op.createDialog (c, msg);
            d.setResizable (true);
            d.pack();
            d.setVisible (true);
            
            Object res = op.getValue ();
            if (res instanceof Integer) {
                int ret = (Integer) res;
                if (ret == 1 || ret == -1) { // exit or close
                    TopLogging.exit(1);
                }
            }
        }
    }
        
    private static void setStatusText (String msg) {
        Main.setStatusText (msg);
    }
}
