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
    private static volatile boolean policyAdvanced;

    public static void advancePolicy() {
        if (policyAdvanced) {
            return;
        }
        policyAdvanced = true;
    }
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
