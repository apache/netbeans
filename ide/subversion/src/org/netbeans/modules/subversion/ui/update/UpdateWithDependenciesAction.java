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

package org.netbeans.modules.subversion.ui.update;

import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.nodes.Node;
import java.util.*;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.Subversion;

/**
 * Updates selected projects and all projects they depend on.
 *
 * @author Maros Sandor
 */
public class UpdateWithDependenciesAction extends ContextAction {
    
    private boolean running;

    @Override
    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_IN_REPOSITORY;
    }

    @Override
    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED 
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED 
             & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }
    
    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_UpdateWithDependencies";    // NOI18N
    }

    @Override
    protected boolean enable(Node[] nodes) {
        boolean enabled = !running && super.enable(nodes);
        if (enabled) {
            for (int i = 0; i < nodes.length; i++) {
                Node node = nodes[i];
                if (!SvnUtils.isVersionedProject(node, false)) {
                    enabled = false;
                    break;
                }
            }
        }
        return enabled;
    }
    
    @Override
    protected void performContextAction(final Node[] nodes) {
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }
        
        running = true;
        Subversion.getInstance().getParallelRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                try {
                    updateWithDependencies(nodes);
                } finally {
                    running = false;
                }
            }
        });
    }

    private void updateWithDependencies(Node[] nodes) {
        Set<Project> projectsToUpdate = new HashSet<Project>(nodes.length * 2);
        for (Node node : nodes) {
            if (!SvnUtils.isVersionedProject(node, true)) {
                continue;
            }
            Project project =  (Project) node.getLookup().lookup(Project.class);
            projectsToUpdate.add(project);
            //mkleint: see subprojectprovider for official contract, see #210465
            // do we care if all or just the direct subprojects are included?
            SubprojectProvider deps = (SubprojectProvider) project.getLookup().lookup(SubprojectProvider.class);
            if(deps != null) {
                Set<? extends Project> children = deps.getSubprojects();
                for (Project child : children) {
                    if (SvnUtils.isVersionedProject(child, true)) {
                        projectsToUpdate.add(child);
                    }
                }
            }
        }
        Context context = SvnUtils.getProjectsContext(projectsToUpdate.toArray(new Project[0]));
        UpdateAction.performUpdate(context, getContextDisplayName(nodes));
    }
}
