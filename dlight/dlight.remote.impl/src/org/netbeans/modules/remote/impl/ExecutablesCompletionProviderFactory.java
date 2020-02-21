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

package org.netbeans.modules.remote.impl;

import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.remote.api.ui.AutocompletionProvider;
import org.netbeans.modules.remote.ui.spi.AutocompletionProviderFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * Provides auto-completion options based on $PATH environment variable.
 * Searches for all executables in paths and matches their names for
 * autocompletion options.
 * 
 */

// DISABLED - FindBasedExecutablesCompletionProviderFactory is used instead

//@ServiceProvider(service = AutocompletionProviderFactory.class)
public class ExecutablesCompletionProviderFactory implements AutocompletionProviderFactory {

    public AutocompletionProvider newInstance(ExecutionEnvironment env) {
        try {
            return new Provider(env);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (CancellationException ex) {
            // don't report cancellatoin exception
        }

        return null;
    }

    public boolean supports(final ExecutionEnvironment env) {
        return ConnectionManager.getInstance().isConnectedTo(env) && HostInfoUtils.isHostInfoAvailable(env);
    }

    private final static class Provider implements AutocompletionProvider {

        private final static int numOfScanThreads = 2;
        private final Scanner scanner;
        private Task[] scanningTasks;

        public Provider(final ExecutionEnvironment env) throws IOException, CancellationException {
            List<String> paths = new ArrayList<>();
            HostInfo info = HostInfoUtils.getHostInfo(env);
            String pathList = info.getEnvironment().get("PATH"); // NOI18N

            for (String path : pathList.split(":")) { // NOI18N
                if (!paths.contains(path)) {
                    paths.add(path);
                }
            }

            scanner = new Scanner(env, paths);
            startScan();
        }

        public List<String> autocomplete(final String str) {
            if ("".equals(str)) { // NOI18N
                return Collections.<String>emptyList();
            }

            List<String> result = new ArrayList<>();

            for (String exec : scanner.getExecutables()) {
                if (exec.startsWith(str)) {
                    result.add(exec);
                }
            }

            return result;
        }

        public void startScan() {
            synchronized (this) {
                if (scanner != null && scanningTasks == null) {
                    scanningTasks = new Task[numOfScanThreads];
                    for (int i = 0; i < numOfScanThreads; i++) {
                        scanningTasks[i] = RequestProcessor.getDefault().post(scanner);
                    }
                }
            }
        }
    }

    private final static class Scanner implements Runnable {

        private final Iterator<String> pathsIterator;
        private final Set<String> executables = new HashSet<>();
        private final ExecutionEnvironment env;
        private volatile boolean isInterrupted;

        public Scanner(final ExecutionEnvironment env, final List<String> paths) {
            List<String> pathsCopy = new ArrayList<>(paths);
            pathsIterator = pathsCopy.iterator();
            this.env = env;
        }

        public void stop() {
            isInterrupted = true;
        }

        public void run() {
            isInterrupted = false;
            while (true) {
                if (isInterrupted()) {
                    break;
                }

                String path = null;

                synchronized (pathsIterator) {
                    if (pathsIterator.hasNext()) {
                        path = pathsIterator.next();
                    }
                }

                if (path == null) {
                    break;
                }

                NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
                npb.setExecutable("/bin/ls").setArguments("-1FL", path); // NOI18N

                ProcessUtils.ExitStatus result = ProcessUtils.execute(npb);
                if (result.isOK()) {
                    for (String s : result.getOutputLines()) {
                        if (s.endsWith("*")) { // NOI18N
                            synchronized (executables) {
                                executables.add(s.substring(0, s.length() - 1));
                            }
                        }
                    }
                }
            }
        }

        private boolean isInterrupted() {
            try {
                Thread.sleep(0);
            } catch (InterruptedException ex) {
                isInterrupted = true;
                Thread.currentThread().interrupt();
            }

            isInterrupted |= Thread.currentThread().isInterrupted();
            return isInterrupted;
        }

        private String[] getExecutables() {
            synchronized (executables) {
                return executables.toArray(new String[0]);
            }
        }
    }
}
