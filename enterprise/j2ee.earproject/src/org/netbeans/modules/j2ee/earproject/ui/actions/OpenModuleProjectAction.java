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

package org.netbeans.modules.j2ee.earproject.ui.actions;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.earproject.ui.ModuleNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

public class OpenModuleProjectAction extends CookieAction {
    private static final long serialVersionUID = 1L;
    
    protected Class[] cookieClasses() {
        return new Class[] { ModuleNode.class };
    }
    
    protected int mode() {
        return CookieAction.MODE_ALL;
    }
    
    public void performAction(Node[] nodes) {
        Project projects[] = new Project[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            ClassPathSupport.Item vcpi = nodes[i].getCookie(ModuleNode.class).getVCPI();
            if (ClassPathSupport.Item.TYPE_ARTIFACT == vcpi.getType()) {
                projects[i] = vcpi.getArtifact().getProject();
            } else {
                continue;
            }
        }
        Set<Project> validProjects = new HashSet<Project>();
        for (int i = 0; i < nodes.length; i++) {
            if (ProjectManager.getDefault().isValid(projects[i])) {
                validProjects.add(projects[i]);
            } // XXX else make project broken?
        }
        if (!validProjects.isEmpty()) {
            OpenProjects.getDefault().open(projects,false);
        }
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    public String getName() {
        return NbBundle.getMessage(OpenModuleProjectAction.class, "LBL_OpenProject");
    }
    
    @Override
    protected boolean asynchronous() {
        return true;
    }
    
}
