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

package org.netbeans.core;

import java.awt.EventQueue;
import java.awt.SecondaryLoop;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.startup.ModuleSystem;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default implementation of the lifecycle manager interface that knows
 * how to save all modified DataObject's, and to exit the IDE safely.
 */
@ServiceProvider(
    service=LifecycleManager.class, 
    supersedes="org.netbeans.core.startup.ModuleLifecycleManager"
)
public final class NbLifecycleManager extends LifecycleManager {
    static final Logger LOG = Logger.getLogger(NbLifecycleManager.class.getName());
    
    /** @GuardedBy("NbLifecycleManager.class") */
    private static CountDownLatch onExit;
    private volatile SecondaryLoop sndLoop;
    private volatile boolean isExitOnEventQueue;
    
    @Override
    public void saveAll() {
        ArrayList<DataObject> bad = new ArrayList<>();
        DataObject[] modifs = DataObject.getRegistry().getModified();
        if (modifs.length == 0) {
            // Do not show MSG_AllSaved
            return;
        }
        for (DataObject dobj : modifs) {
            try {
                SaveCookie sc = dobj.getLookup().lookup(SaveCookie.class);
                if (sc != null) {
                    StatusDisplayer.getDefault().setStatusText(
                            NbBundle.getMessage(NbLifecycleManager.class, "CTL_FMT_SavingMessage", dobj.getName()));
                    sc.save();
                }
            } catch (IOException ex) {
                Logger.getLogger(NbLifecycleManager.class.getName()).log(Level.WARNING, null, ex);
                bad.add(dobj);
            }
        }
        NotifyDescriptor descriptor;
        //recode this part to show only one dialog?
        for (DataObject badDO : bad) {
            descriptor = new NotifyDescriptor.Message(
                    NbBundle.getMessage(NbLifecycleManager.class, "CTL_Cannot_save", badDO.getPrimaryFile().getName()));
            DialogDisplayer.getDefault().notify(descriptor);
        }
        // notify user that everything is done
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(NbLifecycleManager.class, "MSG_AllSaved"));
    }

    @Override
    public void exit() {
        // #37160 So there is avoided potential clash between hiding GUI in AWT
        // and accessing AWTTreeLock from saving routines (winsys).
        exit(0);
    }
    
    private boolean blockForExit(CountDownLatch[] arr) {
        synchronized (NbLifecycleManager.class) {
            if (onExit != null) {
                arr[0] = onExit;
                LOG.log(Level.FINE, "blockForExit, already counting down {0}", onExit);
                return true;
            }
            arr[0] = onExit = new CountDownLatch(1) {
                @Override
                public void countDown() {
                    super.countDown();
                    SecondaryLoop d = sndLoop;
                    LOG.log(Level.FINE, "countDown for {0}, hiding {1}, by {2}",
                        new Object[] { this, d, Thread.currentThread() }
                    );
                    if (d != null) {
                        while (!d.exit()) {
                            LOG.log(Level.FINE, "exit before enter, try again");
                        }
                    }
                }
            };
            LOG.log(Level.FINE, "blockForExit, new {0}", onExit);
            return false;
        }
    }
    
    private void finishExitState(CountDownLatch cdl, boolean clean) {
        LOG.log(Level.FINE, "finishExitState {0} clean: {1}", new Object[]{Thread.currentThread(), clean});
        if (EventQueue.isDispatchThread()) {
            while (cdl.getCount() > 0) {
                boolean prev = isExitOnEventQueue;
                if (!prev) {
                    isExitOnEventQueue = true;
                    try {
                        LOG.log(Level.FINE, "waiting in EDT: {0} own: {1}", new Object[]{onExit, cdl});
                        if (cdl.await(5, TimeUnit.SECONDS)) {
                            LOG.fine("wait is over, return");
                            return;
                        }
                    } catch (InterruptedException ex) {
                        LOG.log(Level.FINE, null, ex);
                    }
                }
                SecondaryLoop sl = Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();
                try {
                    sndLoop = sl;
                    LOG.log(Level.FINE, "Showing dialog: {0}", sl);
                    sl.enter();
                } finally {
                    LOG.log(Level.FINE, "Disposing dialog: {0}", sndLoop);
                    sndLoop = null;
                    isExitOnEventQueue = prev;
                }
            }
        }
        LOG.log(Level.FINE, "About to block on {0}", cdl);
        try {
            cdl.await();
        } catch (InterruptedException ex) {
            LOG.log(Level.FINE, null, ex);
        } finally {
            if (clean) {
                LOG.log(Level.FINE, "Cleaning {0} own {1}", new Object[] { onExit, cdl });
                synchronized (NbLifecycleManager.class) {
                    assert cdl == onExit;
                    onExit = null;
                }
            }
        }
        LOG.fine("End of finishExitState");
    }
    
    @Override
    public void exit(int status) {
        LOG.log(Level.FINE, "Initiating exit with status {0}", status);
        if (EventQueue.isDispatchThread()) {
            if (isExitOnEventQueue) {
                LOG.log(Level.FINE, "Already in process of exiting {0}, return", isExitOnEventQueue);
                return;
            } else {
                isExitOnEventQueue = true;
            }
        }
        try {
            CountDownLatch[] cdl = { null };
            if (blockForExit(cdl)) {
                finishExitState(cdl[0], false);
                return;
            }
            NbLifeExit action = new NbLifeExit(0, status, cdl[0]);
            Mutex.EVENT.readAccess(action);
            finishExitState(cdl[0], true);
        } catch (Error | RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error during shutdown", ex);
            throw ex;
        } finally {
            if (EventQueue.isDispatchThread()) {
                isExitOnEventQueue = false;
            }
        }
    }
    
    public static synchronized boolean isExiting() {
        return onExit != null;
    }

    @Override
    public void markForRestart() throws UnsupportedOperationException {
        ModuleSystem.markForRestart();
    }
}
