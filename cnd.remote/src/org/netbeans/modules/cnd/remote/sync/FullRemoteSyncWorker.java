/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.sync;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.cnd.spi.remote.setup.support.RemoteSyncNotifier;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 */
/*package-local*/ class FullRemoteSyncWorker implements RemoteSyncWorker {

    private final ExecutionEnvironment executionEnvironment;
    private final FileSystem sourceFileSystem;
    private final PrintWriter out;
    
    private final PrintWriter err;
    private static final RequestProcessor RP = new RequestProcessor("FullRemoteSyncWoker", 1); // NOI18N
    
    public FullRemoteSyncWorker(ExecutionEnvironment executionEnvironment, PrintWriter out, PrintWriter err, List<FSPath> files) {
        this.executionEnvironment = executionEnvironment;
        this.sourceFileSystem = (files.isEmpty()) ? FileSystemProvider.getFileSystem(ExecutionEnvironmentFactory.getLocal()) : files.get(0).getFileSystem();
        this.out = out;
        this.err = err;
    }

    @Override
    public boolean startup(Map<String, String> env2add) {

        if (SyncUtils.isDoubleRemote(executionEnvironment, sourceFileSystem)) {
            RemoteSyncNotifier.getInstance().warnDoubleRemote(executionEnvironment, sourceFileSystem);
            return false;
        }

        try {
            // call FileSystemProvider.waitWrites
            // and print "waiting..." message in the case it does not return within half a second

            final String hostName = RemoteUtil.getDisplayName(executionEnvironment);
            final Object lock = new Object();
            final AtomicReference<Boolean> waiting = new AtomicReference<>(true);
            if (out != null) {
                RequestProcessor.Task task = RP.create(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (lock) {
                            if (waiting.get().booleanValue()) {                                
                                out.println(NbBundle.getMessage(getClass(), "FULL_Synchronizing_Message", hostName));
                            }
                        }
                    }
                });
                task.schedule(500);
            }
            List<String> failedList = new ArrayList<>();
            boolean written = FileSystemProvider.waitWrites(executionEnvironment, failedList);
            synchronized (lock) {
                waiting.set(false);
            }
            if( written ) {
                return true;
            } else {
                if (err != null) {
                    StringBuilder failedText = new StringBuilder();
                    for (String file : failedList) {
                        if (failedText.length() > 0) {
                            failedText.append('\n');
                        }
                        failedText.append(file);
                    }
                    err.println(NbBundle.getMessage(getClass(), "FULL_Failed_Message", hostName, failedText.toString()));
                }
                return false;
            }
        } catch (InterruptedException ex) {
            // don't report InterruptedException
            return false;
        }
    }

    @Override
    public void shutdown() {
    }

    @Override
    public boolean cancel() {
        return false;
    }
}
