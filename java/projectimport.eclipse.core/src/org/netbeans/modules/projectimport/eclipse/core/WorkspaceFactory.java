/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Able to load and fill up an <code>EclipseWorkspace</code> from an Eclipse
 * workspace directory using a .workspace and .classpath file and eventually
 * passed workspace. It is also able to load a basic information from workspace.
 *
 * @author mkrauskopf
 */
public final class WorkspaceFactory {

    private static Map<File, WeakReference<Workspace>> cache = new HashMap<File, WeakReference<Workspace>>();
            
    /** singleton */
    private static WorkspaceFactory instance = new WorkspaceFactory();
    
    private WorkspaceFactory() {/*empty constructor*/}
    
    public static WorkspaceFactory getInstance() {
        return instance;
    }
    
    public void resetCache() {
        cache = new HashMap<File, WeakReference<Workspace>>();
    }
    /**
     * Loads a workspace contained in the given <code>workspaceDir</code>.
     *
     * @throws InvalidWorkspaceException if workspace in the given
     *     <code>workspaceDir</code> is not a valid Eclipse workspace.
     */
    public Workspace load(File workspaceDir) throws ProjectImporterException {
        WeakReference<Workspace> wr = cache.get(workspaceDir);
        Workspace w = wr != null ? wr.get() : null;
        if (w == null) {
            Workspace workspace = Workspace.createWorkspace(workspaceDir);
            if (workspace != null) {
                WorkspaceParser parser = new WorkspaceParser(workspace);
                parser.parse();
                cache.put(workspaceDir, new WeakReference<Workspace>(workspace));
                w = workspace;
            }
        }
        return w;
    }
}
