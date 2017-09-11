/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.core;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.CLIHandler;
import org.netbeans.TopSecurityManager;
import org.netbeans.core.startup.CLIOptions;
import org.netbeans.core.startup.Main;
import org.netbeans.core.startup.layers.SessionManager;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class NbLifeExit implements Runnable {
    private static final RequestProcessor RP = new RequestProcessor("Nb Exit"); // NOI18N
    
    private final int type;
    private final int status;
    private final Future<Boolean> waitFor;
    private final CountDownLatch onExit;

    NbLifeExit(int type, int status, CountDownLatch onExit) {
        this(type, status, null, onExit);
    }

    private NbLifeExit(int type, int status, Future<Boolean> waitFor, CountDownLatch onExit) {
        this.type = type;
        this.status = status;
        this.waitFor = waitFor;
        this.onExit = onExit;
        NbLifecycleManager.LOG.log(
            Level.FINE, "NbLifeExit({0}, {1}, {2}, {3}) = {4}", new Object[]{
                type, status, waitFor, onExit, this
            }
        );
    }

    @Override
    public void run() {
        NbLifecycleManager.LOG.log(Level.FINE, "{0}.run()", this);
        switch (type) {
            case 0:
                doExit(status);
                break;
            case 1:
                doStopInfra(status);
                break;
            case 2:
                int s = 3;
                try {
                    if (waitFor != null && waitFor.get()) {
                        s = 4;
                    }
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
                Mutex.EVENT.readAccess(new NbLifeExit(s, status, null, onExit));
                break;
            case 3:
            case 4:
                doApproved(type == 4, status);
                break;
            case 5:
                try {
                    final boolean doExit = !Boolean.getBoolean("netbeans.close.no.exit"); // NOI18N
                    NbLifecycleManager.LOG.log(Level.FINE, "Calling exit: {0}", doExit); // NOI18N
                    if (doExit) {
                        TopSecurityManager.exit(status);
                    }
                } finally {
                    NbLifecycleManager.LOG.log(Level.FINE, "After exit!"); // NOI18N
                    onExit.countDown();
                }
                break;
            default:
                throw new IllegalStateException("Type: " + type); // NOI18N
        }
    }

    private void doExit(int status) {
        // save all open files
        Future<Boolean> res;
        if (System.getProperty("netbeans.close") != null || ExitDialog.showDialog()) { // NOI18N
            res = Main.getModuleSystem().shutDownAsync(new NbLifeExit(1, status, null, onExit));
        } else {
            res = null;
        }
        RP.post(new NbLifeExit(2, status, res, onExit));
    }

    private void doStopInfra(int status) {
        CLIHandler.stopServer();
        final WindowSystem windowSystem = Lookup.getDefault().lookup(WindowSystem.class);
        boolean gui = CLIOptions.isGui();
        if (windowSystem != null && gui) {
            windowSystem.hide();
            windowSystem.save();
        }
        if (Boolean.getBoolean("netbeans.close.when.invisible")) { // NOI18N
            // hook to permit perf testing of time to *apparently* shut down
            try {
                TopSecurityManager.exit(status);
            } finally {
                onExit.countDown();
            }
        }
        
    }

    private void doApproved(boolean isApproved, int status) throws ThreadDeath {
        if (isApproved) {
            try {
                try {
                    NbLoaderPool.store();
                } catch (IOException ioe) {
                    Logger.getLogger(NbLifecycleManager.class.getName()).log(Level.WARNING, null, ioe);
                }
                //#46940 -saving just once..
                //                        // save window system, [PENDING] remove this after the winsys will
                //                        // persist its state automaticaly
                //                        if (windowSystem != null) {
                //                            windowSystem.save();
                //                        }
                SessionManager.getDefault().close();
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
                // Do not let problems here prevent system shutdown. The module
                // system is down; the IDE cannot be used further.
                Exceptions.printStackTrace(t);
            }
            // #37231 Someone (e.g. Jemmy) can install its own EventQueue and then
            // exit is dispatched through that proprietary queue and it
            // can be refused by security check. So, we need to replan
            // to RequestProcessor to avoid security problems.
            Task exitTask = new Task(new NbLifeExit(5, status, null, onExit));
            RP.post(exitTask);
        } else {
            // end of exit
            onExit.countDown();
        }
    }
    
} // end of ExitActions
