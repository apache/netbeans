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
package org.netbeans.modules.cnd.lsp.server;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.SwingPropertyChangeSupport;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider.LanguageServerDescription;
import org.netbeans.modules.lsp.client.spi.ServerRestarter;

/**
 * The unique instance of the clangd LSP server, serving many projects at once.
 *
 * @author antonio
 */
public final class ClangdProcess {

    private static final Logger LOG = Logger.getLogger(ClangdProcess.class.getName());

    private static final ClangdProcess INSTANCE = new ClangdProcess();

    /**
     * Returns the ClangdProcess shared among different Projects.
     *
     * @return The running instance of ClangdProcess, or null.
     */
    public static final ClangdProcess getInstance() {
        return INSTANCE;
    }

    public static final String PROP_STATE = "state"; // NOI18N

    private final class ClangdProcessWatcher extends Thread {

        private final Process process;

        public ClangdProcessWatcher(Process process) {
            super("clangd process watcher"); // NOI18N
            this.process = process;
        }

        @Override
        public void run() {
            try {
                if (process.isAlive()) {
                    int exitCode = process.waitFor();
                    LOG.log(Level.INFO, "clangd process exited with code {0}", exitCode);
                    fireStateChange(LSPServerState.STOPPED);
                }
            } catch (InterruptedException e) {
                LOG.log(Level.INFO, "clangd process exited with error {0}:{1}", new Object[] { e.getMessage(), e.getClass().getName() });
                fireStateChange(LSPServerState.ERROR);
            }
        }
    }

    private final SwingPropertyChangeSupport propertyChangeSupport;
    private Process process;
    private LanguageServerDescription languageServerDescription;
    private LSPServerState serverState;
    private ServerRestarter serverRestarter;
    private ClangdProcessWatcher processWatcher;

    private ClangdProcess() {
        this.propertyChangeSupport = new SwingPropertyChangeSupport(this, true);
        this.serverState = LSPServerState.STOPPED;
    }

    /**
     * Returns the state of clangd.
     *
     * @return The state of the clangd process.
     */
    public LSPServerState getState() {
        return serverState;
    }

    public String[] getCommands() {
        ClangdOptions options = ClangdOptions.load();
        File clangd = options.getClangdExecutable();
        ArrayList<String> commands = new ArrayList<>();
        commands.add(clangd.getAbsolutePath());
        commands.add("--clang-tidy"); // NOI18N
        commands.add("--completion-style=detailed"); // NOI18N
        commands.add("--offset-encoding=utf-8"); // NOI18N
        commands.addAll(options.getCommandLineOptions());
        return commands.toArray(new String[commands.size()]);
    }

    public void start(ServerRestarter serverRestarter) throws IOException {
        this.serverRestarter = serverRestarter;
        if (this.process != null && this.process.isAlive() && serverRestarter != null) {
            fireStateChange(LSPServerState.STOPPED);
            serverRestarter.restart();
        }
        ClangdOptions options = ClangdOptions.load();
        if (options.isMisconfigured()) {
            fireStateChange(LSPServerState.MISCONFIGURED);
            File clangd = options.getLogFile();
            throw new IOException(String.format("Cannot execute clangd at path: %s", clangd == null ? "[NOT SET]" : clangd.getAbsolutePath()));
        }
        String[] commands = getCommands();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            File logFile = options.getLogFile();
            if (logFile != null) {
                processBuilder.redirectError(logFile);
            } else {
                processBuilder.redirectError(Redirect.INHERIT);
            }
            this.process = processBuilder.start();
            LOG.log(Level.INFO, String.format("Started clangd with arguments: %s",
                    String.join(" ", commands)));
            this.languageServerDescription = LanguageServerDescription.create(process.getInputStream(), process.getOutputStream(), process);
            fireStateChange(LSPServerState.RUNNING);
            this.processWatcher = new ClangdProcessWatcher(process);
            this.processWatcher.start();
        } catch (Throwable e) {
            LOG.log(Level.SEVERE, String.format("Failed to start LSP Server: %s:%s",
                    e.getMessage(), e.getClass().getName()), e);
            fireStateChange(LSPServerState.ERROR);
            throw e;
        }
    }

    public LanguageServerDescription getLanguageServerDescription() {
        return languageServerDescription;
    }

    /**
     * Only LSPServerSupport is expected to be a listener of this class. Other
     * objects should listen to LSPServerSupport in the project's lookup.
     *
     * @param propertyChangeListener
     */
    void addPropertyChangeListener(LSPServerSupport propertyChangeListener) {
        propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
    }

    /**
     * Only LSPServerSupport is expected to be a listener of this class. Other
     * objects should listen to LSPServerSupport in the project's lookup.
     *
     * @param propertyChangeListener
     */
    void removePropertyChangeListener(LSPServerSupport propertyChangeListener) {
        propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
    }

    private void fireStateChange(LSPServerState newState) {
        LSPServerState oldState = this.serverState;
        if (oldState != newState) {
            this.serverState = newState;
            LOG.log(Level.INFO, "Clangd server state changed from {0} to {1}", new Object[] { oldState, newState});
            propertyChangeSupport.firePropertyChange(PROP_STATE, oldState, newState);
        }
    }

    public void restart() {
        if (this.serverRestarter != null) {
            this.serverRestarter.restart();
        }
    }

}
