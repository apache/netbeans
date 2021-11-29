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
package org.netbeans.modules.java.lsp.server.explorer;

import org.netbeans.modules.java.lsp.server.explorer.api.ExplorerManagerFactory;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.explorer.ExplorerManager;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
@ServiceProvider(path = "Explorers/" + ProjectExplorer.ID_PROJECT_LOGICAL_VIEW, service = ExplorerManagerFactory.class)
public class ProjectExplorer implements ExplorerManagerFactory {
    static final String ID_PROJECT_LOGICAL_VIEW = "foundProjects"; // NOI18N
    
    private static final RequestProcessor PROJECT_INIT_RP = new RequestProcessor(ProjectExplorer.class.getName());
    
    @Override
    public CompletionStage<ExplorerManager> createManager(String id, Lookup context) {
        if (!ID_PROJECT_LOGICAL_VIEW.equals(id)) {
            return null;
        }
        return CompletableFuture.supplyAsync(() -> OpenProjects.getDefault().createLogicalView(), PROJECT_INIT_RP);
    }
}
