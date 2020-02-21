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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.makeproject.api.BuildActionsProvider.OutputStreamHandler;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandler;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.Env;
import org.netbeans.modules.cnd.remote.support.RemoteLogger;
import org.netbeans.modules.cnd.remote.support.RemoteProjectSupport;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 *
 */
/* package-local */
class RemoteBuildProjectActionHandler implements ProjectActionHandler {

    private ProjectActionHandler delegate;
    private ProjectActionEvent pae;
    private ExecutionEnvironment execEnv;
    private final List<ExecutionListener> listeners = new CopyOnWriteArrayList<>();
    private final CountDownLatch delegateExecutionFinished = new CountDownLatch(1);

    private PrintWriter out;
    private PrintWriter err;

    private static final String testWorkerRunningProp = "cnd.remote.sync.worker.running"; // for tests only // NOI18N

    /* package-local */
    RemoteBuildProjectActionHandler() {
    }

    @Override
    public void init(ProjectActionEvent pae, ProjectActionEvent[] paes, Collection<OutputStreamHandler> outputHandlers) {
        this.pae = pae;
        this.delegate = RemoteBuildProjectActionHandlerFactory.createDelegateHandler(pae);
        this.delegate.init(pae, paes, outputHandlers);
        this.execEnv = pae.getConfiguration().getDevelopmentHost().getExecutionEnvironment();
    }

    @Override
    public void addExecutionListener(ExecutionListener l) {
        delegate.addExecutionListener(l);
        listeners.add(l);
    }

    @Override
    public void removeExecutionListener(ExecutionListener l) {
        delegate.removeExecutionListener(l);
        listeners.remove(l);
    }

    @Override
    public boolean canCancel() {
        return delegate.canCancel();
    }

    @Override
    public void cancel() {
        delegate.cancel();
    }

    @Override
    public void execute(InputOutput io) {

        if (execEnv.isLocal()) {
            assert true;
            delegate.execute(io);
            return;
        } else {
            try {
                ConnectionManager.getInstance().connectTo(execEnv);
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
                if (io != null) {
                    io.getErr().printf("%s%n", ex.getMessage()); //NOI18N
                }
                delegate.cancel();
                return;
            } catch (CancellationException ex) {
                // don't log CancellationException
                delegate.cancel();
                return;
            }
        }

        if (io != null) {
            err = io.getErr();
            out = io.getOut();
        }

        if (!RemoteProjectSupport.projectExists(pae.getProject())) {
            delegate.cancel();
            return;
        }

        FileObject privProjectStorage = RemoteProjectSupport.getPrivateStorage(pae.getProject());
        MakeConfiguration conf = pae.getConfiguration();
        AtomicReference<String> runDir = new AtomicReference<>();
        List<FSPath> sourceDirs = RemoteProjectSupport.getProjectSourceDirs(pae.getProject(), conf, runDir);

        RemoteSyncFactory syncFactory = conf.getRemoteSyncFactory();
        final RemoteSyncWorker worker = (syncFactory == null) ? null :
                syncFactory.createNew(execEnv, out, err, privProjectStorage, runDir.get(), sourceDirs,
                        RemoteProjectSupport.getBuildResults(conf));
        CndUtils.assertTrue(worker != null, "RemoteSyncWorker shouldn't be null"); //NOI18N
        if (worker == null) {
            delegate.execute(io);
            return;
        }

        Map<String, String> env2add = new HashMap<>();
        System.setProperty(testWorkerRunningProp, "true"); // NOI18N
        if (worker.startup(env2add)) {
            final ExecutionListener listener = new ExecutionListener() {
                @Override
                public void executionStarted(int pid) {
                }
                @Override
                public void executionFinished(int rc) {
                    try {
                        worker.shutdown();
                        delegate.removeExecutionListener(this);
                        System.setProperty(testWorkerRunningProp, "false"); // NOI18N
                    } finally {
                        delegateExecutionFinished.countDown();
                    }
                }
            };
            delegate.addExecutionListener(listener);
            Env env = pae.getProfile().getEnvironment();
            for (Map.Entry<String, String> entry : env2add.entrySet()) {
                if (RemoteUtil.LOGGER.isLoggable(Level.FINE)) {
                    RemoteUtil.LOGGER.fine(String.format("\t%s=%s", entry.getKey(), entry.getValue()));
                }
                env.putenv(entry.getKey(), entry.getValue());
            }
            delegate.execute(io);
            try {
                // we need to ensure that worker.shutdown() is called before execute() returns
                // see issue #257565 - FileAlreadyLockedException
                delegateExecutionFinished.await();
            } catch (InterruptedException ex) {
                RemoteLogger.getInstance().log(Level.FINE, "That's just FYI: interrupted", ex);
            }
        } else {
            System.setProperty(testWorkerRunningProp, "false"); // NOI18N
            for (ExecutionListener l : listeners) {
                l.executionFinished(-8);
            }
            if (err != null) {
                err.printf("%s%n", NbBundle.getMessage(getClass(), "MSG_Build_Failed"));
            }
        }
    }

    /**
     * For test purposes: wait until workers finished
     * @param timeout timeout IN SECONDS
     */
    /* package */ static void testWaitWorkerFinished(int timeout) throws TimeoutException, InterruptedException {
        long end = System.currentTimeMillis() + timeout * 1000;
        while (Boolean.getBoolean(testWorkerRunningProp)) {
            long rest = end - System.currentTimeMillis();
            if (rest < 0) {
                throw new TimeoutException();
            }
            RemoteUtil.LOGGER.finest("Waiting until sync worker is finished");
            Thread.sleep(rest < 200 ? rest : 200);
        }
    }
}
