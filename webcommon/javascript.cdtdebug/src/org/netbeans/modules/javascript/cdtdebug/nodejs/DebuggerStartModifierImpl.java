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
package org.netbeans.modules.javascript.cdtdebug.nodejs;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.cdtdebug.api.Connector;
import org.netbeans.modules.javascript.nodejs.api.DebuggerOptions;
import org.netbeans.modules.javascript.nodejs.spi.DebuggerStartModifier;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class DebuggerStartModifierImpl implements DebuggerStartModifier {

    private static final Logger LOG = Logger.getLogger(DebuggerStartModifierImpl.class.getName());
    private static final String DEBUG_BRK_COMMAND = "--inspect-brk=%d"; // NOI18N
    private static final String DEBUG_COMMAND = "--inspect=%d"; // NOI18N

    private final List<String> localPaths;
    private final List<String> serverPaths;
    private final List<String> localPathsExclusionFilter;
    private final Project project;
    private final AtomicReference<Future<Integer>> taskRef;
    private boolean processingDone;
    private int port;

    public DebuggerStartModifierImpl(Project project, List<String> localPaths, List<String> serverPaths, List<String> localPathsExclusionFilter, AtomicReference<Future<Integer>> taskRef) {
        this.localPaths = localPaths;
        this.serverPaths = serverPaths;
        this.localPathsExclusionFilter = localPathsExclusionFilter;
        this.project = project;
        this.taskRef = taskRef;
    }

    void connectDebugger() {
        Connector.Properties props = new Connector.Properties("localhost", port, // NOI18N
                null, localPaths, serverPaths, localPathsExclusionFilter);
        try {
            Connector.connect(props, new Runnable() {
                @Override
                public void run() {
                    Future<Integer> task = taskRef.get();
                    assert task != null : project.getProjectDirectory();
                    task.cancel(true);
                }
            });
        } catch (IOException ex) {
            LOG.log(Level.INFO, "cannot run node.js debugger", ex);
            warnCannotDebug(ex);
        }
    }

    @Override
    public List<String> getArguments(Lookup context) {
        this.port = allocateServerPort();
        if (DebuggerOptions.getInstance().isBreakAtFirstLine()) {
            return Collections.singletonList(String.format(DEBUG_BRK_COMMAND, port));
        } else {
            return Collections.singletonList(String.format(DEBUG_COMMAND, port));
        }
    }

    @Override
    public void processOutputLine(String line) {
        if (line != null && line.toLowerCase(Locale.US).startsWith("debugger listening on ")) {
            connectDebugger();
            processingDone = true;
        }
    }

    @Override
    public boolean startProcessingDone() {
        return processingDone;
    }

    private int allocateServerPort() {
        try ( ServerSocket ss = new ServerSocket(0)) {
            ss.setReuseAddress(true);
            return ss.getLocalPort();
        } catch (IOException ex) {
            return 0;
        }
    }

    @NbBundle.Messages({
        "# {0} - reason",
        "warn.debug=Cannot run debugger. Reason:\n\n{0}",
    })
    protected void warnCannotDebug(IOException ex) {
        NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(Bundle.warn_debug(ex), NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notifyLater(descriptor);
    }

}
