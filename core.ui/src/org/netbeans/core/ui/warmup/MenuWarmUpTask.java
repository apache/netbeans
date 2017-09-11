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

package org.netbeans.core.ui.warmup;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import static org.netbeans.core.ui.warmup.Bundle.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor.Message;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * A menu preheating task. It is referenced from the layer and may be performed
 * by the core after the startup.
 * 
 * Plus hooked WindowListener on main window (see {@link NbWindowsAdapter})
 */
@ServiceProvider(service=Runnable.class, path="WarmUp")
public final class MenuWarmUpTask implements Runnable {

    private Component[] comps;
    
    /** Actually performs pre-heat.
     */
    @Override
    public void run() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    Frame main = WindowManager.getDefault().getMainWindow();
                    
                    assert main != null;
                    main.addWindowListener(new NbWindowsAdapter());
                    
                    if (main instanceof JFrame) {
                        comps = ((JFrame) main).getJMenuBar().getComponents();
                    }
                }
            });
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return;
        } catch (Exception e) { // bail out!
            return;
        }


        if (comps != null) {
            walkMenu(comps);
            comps = null;
        }

        // tackle the Tools menu now? How?
    }

    private void walkMenu(Component[] items) {
        for (int i=0; i<items.length; i++) {
            if (! (items[i] instanceof JMenu)) continue;
            try {
                Class<?> cls = items[i].getClass();
                Method m = cls.getDeclaredMethod("doInitialize");
                m.setAccessible(true);
                m.invoke(items[i]);
                walkMenu(((JMenu)items[i]).getMenuComponents()); // recursive?
            } catch (Exception e) {// do nothing, it may happen for user-provided menus
            }
        }
    }

    /**
     * After activation of window is refreshed filesystem but only if switching
     * from an external application.
     */ 
    private static class NbWindowsAdapter extends WindowAdapter
    implements Runnable, Cancellable {
        private static final RequestProcessor rp = new RequestProcessor ("Refresh-After-WindowActivated", 1, true);//NOI18N
        private RequestProcessor.Task task;
        private AtomicBoolean goOn;
        private static final Logger UILOG = Logger.getLogger("org.netbeans.ui.focus"); // NOI18N
        private static final Logger LOG = Logger.getLogger("org.netbeans.core.ui.focus"); // NOI18N
        private boolean warnedNoRefresh;

        @Override
        public void windowActivated(WindowEvent e) {
            // proceed only if switching from external application
            if (e.getOppositeWindow() == null) {
                synchronized (rp) {
                    if (task != null) {
                        LOG.fine("Scheduling task after activation");
                        task.schedule(1500);
                        task = null;
                    } else {
                        LOG.fine("Activation without prepared refresh task");
                    }
                }
            }
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
            // proceed only if switching to external application
            if (e.getOppositeWindow() == null) {
                synchronized (rp) {
                    if (task != null) {
                        task.cancel();
                    } else {
                        task = rp.create(this);
                    }
                    LOG.fine("Window deactivated, preparing refresh task");
                }
                if (UILOG.isLoggable(Level.FINE)) {
                    LogRecord r = new LogRecord(Level.FINE, "LOG_WINDOW_DEACTIVATED"); // NOI18N
                    r.setResourceBundleName("org.netbeans.core.ui.warmup.Bundle"); // NOI18N
                    r.setResourceBundle(NbBundle.getBundle(MenuWarmUpTask.class)); // NOI18N
                    r.setLoggerName(UILOG.getName());
                    UILOG.log(r);
                }
            }
        }

        private static boolean isNoRefresh() {
            if (Boolean.getBoolean("netbeans.indexing.noFileRefresh")) {
                return true;
            }
            return NbPreferences.root().node("org/openide/actions/FileSystemRefreshAction").getBoolean("manual", false); // NOI18N
        }

        @Messages({
            "MSG_Refresh=Checking for external changes",
            "# default delay is 10s. Increase to Integer.MAX_VALUE to effectively disable", "#NOI18N", "MSG_RefreshDelay=10000",
            "MSG_Refresh_Suspend=Suspended"
        })
        @Override
        public void run() {
            if (isNoRefresh()) {
                if (!warnedNoRefresh) {
                    LOG.info("External Changes Refresh on focus gain disabled"); // NOI18N
                    warnedNoRefresh = true;
                }
                LOG.fine("Refresh disabled, aborting");
                return; // no file refresh
            }
            final ProgressHandle h = ProgressHandleFactory.createHandle(MSG_Refresh(), this, null);
            if (!LOG.isLoggable(Level.FINE)) {
                int delay = Integer.parseInt(MSG_RefreshDelay());
                h.setInitialDelay(delay);
            }
            h.start();
            Runnable run = null;
            class HandleBridge extends ActionEvent implements Runnable {
                private FileObject previous;
                private long next;

                public HandleBridge(Object source) {
                    super (source, 0, "");
                }

                @Override
                public void setSource(Object newSource) {
                    if (newSource instanceof Object[]) {
                        long now = System.currentTimeMillis();
                        boolean again = now > next;
                        Object[] arr = (Object[])newSource;
                        if (arr.length >= 3 &&
                            arr[0] instanceof Integer &&
                            arr[1] instanceof Integer &&
                            arr[2] instanceof FileObject
                        ) {
                            if (! (getSource() instanceof Object[])) {
                                h.switchToDeterminate((Integer)arr[1]);
                                LOG.log(Level.FINE, "First refresh progress event delivered: {0}/{1} where {2}, goOn: {3}", arr);
                            }
                            if ((Integer)arr[0] < (Integer)arr[1]) {
                                h.progress((Integer)arr[0]);
                            }
                            FileObject fo = (FileObject)arr[2];
                            if (previous != fo.getParent() && again) {
                                previous = fo.getParent();
                                if (previous != null) {
                                    h.progress(previous.getPath());
                                }
                                next = now + 500;
                            }
                            super.setSource(newSource);
                        }
                        if (arr.length >= 4 && arr[3] instanceof AtomicBoolean) {
                            goOn = (AtomicBoolean)arr[3];
                        }
                        if (arr.length >= 5 && arr[4] == null && again) {
                            arr[4] = Utilities.actionsGlobalContext().lookup(FileObject.class);
                            LOG.log(Level.FINE, "Preferring {0}", arr[4]);
                        }
                    }
                }

                @Override
                public void run() {
                    if (EventQueue.isDispatchThread()) {
                        try {
                            h.suspend(MSG_Refresh_Suspend());
                        } catch (Throwable t) {
                            // ignore any errors
                        }
                    } else {
                        EventQueue.invokeLater(this);
                    }
                }
            }
            HandleBridge handleBridge = new HandleBridge(this);
            // preinitialize
            handleBridge.run();

            try {
                FileObject udFo = FileUtil.toFileObject(Places.getUserDirectory());
                if (udFo != null) {
                    udFo = udFo.getFileSystem().getRoot();
                }
                if (udFo != null) {
                    run = (Runnable)udFo.getAttribute("refreshSlow"); // NOI18N
                }
            } catch (Exception ex) {
                LOG.log(Level.FINE, "Error getting refreshSlow", ex); // NOI18N
            }
            long now = System.currentTimeMillis();
            try {
                if (run == null) {
                    LOG.fine("Starting classical refresh");
                    FileUtil.refreshAll();
                } else {
                    // connect the bridge with the masterfs's RefreshSlow
                    if (run instanceof AtomicBoolean) {
                        goOn = (AtomicBoolean) run;
                        LOG.fine("goOn controller registered");
                    }
                    LOG.fine("Starting slow refresh");
                    run.equals(handleBridge);
                    run.run();
                }
                long took = System.currentTimeMillis() - now;
                if (UILOG.isLoggable(Level.FINE)) {
                    LogRecord r = new LogRecord(Level.FINE, "LOG_WINDOW_ACTIVATED"); // NOI18N
                    r.setParameters(new Object[] { took });
                    r.setResourceBundleName("org.netbeans.core.ui.warmup.Bundle"); // NOI18N
                    r.setResourceBundle(NbBundle.getBundle(MenuWarmUpTask.class)); // NOI18N
                    r.setLoggerName(UILOG.getName());
                    UILOG.log(r);
                }
                LOG.log(Level.FINE, "Refresh done in {0} ms", took);
                AtomicBoolean ab = goOn;
                if (ab == null || ab.get()) {
                    long sfs = System.currentTimeMillis();
                    FileUtil.getConfigRoot().getFileSystem().refresh(true);
                    LOG.log(Level.FINE, "SystemFileSystem refresh done {0} ms", (System.currentTimeMillis() - sfs));
                } else {
                    LOG.fine("Skipping SystemFileSystem refresh");
                }
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                h.finish();
            }
        }
        private int counter;
        @Override
        public boolean cancel() {
            synchronized(rp) {
                if (task != null) {
                    task.cancel();
                }
                if (goOn != null) {
                    goOn.set(false);
                    LOG.log(Level.FINE, "Signaling cancel to {0}", System.identityHashCode(goOn));
                } else {
                    LOG.log(Level.FINE, "Cannot signal cancel, goOn is null");
                }
            }

            ++counter;

            if (UILOG.isLoggable(Level.FINE)) {
                LogRecord r = new LogRecord(Level.FINE, "LOG_WINDOW_REFRESH_CANCEL"); // NOI18N
                r.setParameters(new Object[]{counter});
                r.setResourceBundleName("org.netbeans.core.ui.warmup.Bundle"); // NOI18N
                r.setResourceBundle(NbBundle.getBundle(MenuWarmUpTask.class)); // NOI18N
                r.setLoggerName(UILOG.getName());
                UILOG.log(r);
            }

            return true;
        }
    } // end of NbWindowsAdapter
}
