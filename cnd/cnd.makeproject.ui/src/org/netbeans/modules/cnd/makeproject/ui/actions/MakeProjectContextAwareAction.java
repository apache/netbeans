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
package org.netbeans.modules.cnd.makeproject.ui.actions;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.openide.awt.DynamicMenuContent;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;

/**
 *
 */
public abstract class MakeProjectContextAwareAction extends NodeAction {
    
    
    public MakeProjectContextAwareAction() {
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, Boolean.TRUE);
    }
    
    protected final Project[] getProjects(Node[] activatedNodes) {
        List<Project> projects = new ArrayList(activatedNodes.length);
        for (Node node : activatedNodes) {
            Project p = getProject(node);
            if (p != null) {
                projects.add(p);
            }
        }
        return projects.toArray(new Project[projects.size()]);
    }

    protected final Project getProject(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return null;
        }
        return getProject(activatedNodes[0]);
    }

    private Project getProject(Node node) {
        Object project = node.getValue("Project"); // NOI18N
        if (project == null || (!(project instanceof Project))) {
            return null;
        }
        return (Project) project;
    }
    
}
