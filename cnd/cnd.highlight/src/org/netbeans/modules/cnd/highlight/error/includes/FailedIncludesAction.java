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
package org.netbeans.modules.cnd.highlight.error.includes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.highlight.error.BadgeProvider;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 */
public class FailedIncludesAction extends NodeAction {
    
    public FailedIncludesAction(){
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
 
    private String i18n(String id) {
        return NbBundle.getMessage(FailedIncludesAction.class,id);
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        List<NativeProject> projects = getNativeProjects(activatedNodes);
        Set<CsmUID<CsmFile>> set;
        if( projects == null || projects.size() != 1) {
            List<CsmProject> csmProjects = getCsmProjects(activatedNodes);
            if( csmProjects == null || csmProjects.size() != 1) {
                return;
            }
            CsmProject csmProject = csmProjects.get(0);
            set = BadgeProvider.getInstance().getFailedFiles(csmProject);
        } else {
            NativeProject nativeProject = projects.get(0);
            set = BadgeProvider.getInstance().getFailedFiles(nativeProject);
        }
        ErrorIncludeDialog.showErrorIncludeDialog(set);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        List<NativeProject> projects = getNativeProjects(activatedNodes);
        if( projects == null || projects.size() != 1) {
            List<CsmProject> csmProjects = getCsmProjects(activatedNodes);
            if( csmProjects == null || csmProjects.size() != 1) {
                return false;
            }
            return BadgeProvider.getInstance().hasFailedFiles(csmProjects.get(0));
        }
        return BadgeProvider.getInstance().hasFailedFiles(projects.get(0));
    }
    
    private List<NativeProject> getNativeProjects(Node[] nodes) {
        List<NativeProject> projects = new ArrayList<>();
        for (int i = 0; i < nodes.length; i++) {
            Project project = nodes[i].getLookup().lookup(Project.class);
            if(project == null) {
                return null;
            }
            NativeProject nativeProject = project.getLookup().lookup(NativeProject.class);
            if(nativeProject == null) {
                return null;
            }
            projects.add(nativeProject);
        }
        return projects;
    }

    private List<CsmProject> getCsmProjects(Node[] nodes) {
        List<CsmProject> projects = new ArrayList<>();
        for (int i = 0; i < nodes.length; i++) {
            CsmProject project = nodes[i].getLookup().lookup(CsmProject.class);
            if(project == null) {
                return null;
            }
            projects.add(project);
        }
        return projects;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public String getName() {
        return i18n("ErrorIncludeMenu_Title"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
