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
package org.netbeans.modules.nbcode.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cloud.oracle.assets.OpenProjectsFinder;
import org.netbeans.modules.java.lsp.server.LspServerState;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Horvath
 */
@ServiceProvider(service = OpenProjectsFinder.class, position = 1000)
public class LspOpenProjectsFinder extends OpenProjectsFinder {

    @Override
    public CompletableFuture<Project[]> findOpenProjects() {
        LspServerState state = Lookup.getDefault().lookup(LspServerState.class);
        return state.openedProjects();
    }
    
    @Override
    public CompletableFuture<Project[]> findTopLevelProjects() {
        CompletableFuture<Project[]> openFuture = Lookup.getDefault().lookup(LspServerState.class).openedProjects();
        List<FileObject> workspaceFolders = Lookup.getDefault().lookup(LspServerState.class).getClientWorkspaceFolders();
        return openFuture.thenApply(open -> {
            List<Project> result = new ArrayList<> ();
            for (int i = 0; i < open.length; i++) {
                if (workspaceFolders.contains(open[i].getProjectDirectory())) {
                    result.add(open[i]);
                }
            }
            return result.toArray(new Project[0]);
        });
        
    }

}
