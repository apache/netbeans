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
package org.netbeans.modules.java.lsp.server.protocol;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lahvac
 */
public final class WorkspaceServiceImpl implements WorkspaceService, LanguageClientAware {

    private NbCodeLanguageClient client;

    public WorkspaceServiceImpl() {
    }

    @Override
    public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
        switch (params.getCommand()) {
            case Server.GRAALVM_PAUSE_SCRIPT:
                ActionsManager am = DebuggerManager.getDebuggerManager().getCurrentEngine().getActionsManager();
                am.doAction("pauseInGraalScript");
                return CompletableFuture.completedFuture(true);
            case Server.JAVA_BUILD_WORKSPACE:
                for (Project prj : OpenProjects.getDefault().getOpenProjects()) {
                    ActionProvider ap = prj.getLookup().lookup(ActionProvider.class);
                    if (ap != null && ap.isActionEnabled(ActionProvider.COMMAND_BUILD, Lookups.fixed())) {
                        ap.invokeAction(ActionProvider.COMMAND_REBUILD, Lookups.fixed());
                    }
                }
                return CompletableFuture.completedFuture(true);
            default:
                throw new UnsupportedOperationException("Command not supported: " + params.getCommand());
        }
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams arg0) {
        //TODO: no real configuration right now
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams arg0) {
        //TODO: not watching files for now
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = (NbCodeLanguageClient)client;
    }
}
