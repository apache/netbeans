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

package org.netbeans.modules.j2ee.earproject.ui;

import org.netbeans.api.project.Project;
import org.netbeans.modules.javaee.project.api.ant.ui.logicalview.AbstractLogicalViewProvider.LogicalViewRootNode;
import org.netbeans.modules.javaee.project.api.ant.ui.logicalview.AbstractLogicalViewProvider2;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Support for creating logical views.
 */
public class J2eeArchiveLogicalViewProvider extends AbstractLogicalViewProvider2 {
    
    private final EarProject project;

    public J2eeArchiveLogicalViewProvider(EarProject project, UpdateHelper helper,
            PropertyEvaluator evaluator, ReferenceHelper resolver, J2eeModuleProvider provider) {
        super(project, helper, evaluator, resolver, provider);
        this.project = project;
    }

    @Override
    public Node createLogicalView() {
        return new LogicalViewRootNode("Projects/org-netbeans-modules-j2ee-earproject/Nodes",
                    "org-netbeans-modules-j2ee-earproject",
                    "org/netbeans/modules/j2ee/earproject/ui/resources/projectIcon.gif",
                    NbBundle.getMessage(J2eeArchiveLogicalViewProvider.class, "HINT_project_root_node"),
                    J2eeArchiveLogicalViewProvider.class);
    }
    
    private static final String[] BREAKABLE_PROPERTIES = new String[] {
        EarProjectProperties.DEBUG_CLASSPATH,
        ProjectProperties.ENDORSED_CLASSPATH,
        EarProjectProperties.JAR_CONTENT_ADDITIONAL,
    };
    
    @Override
    public String[] getBreakableProperties() {
        return BREAKABLE_PROPERTIES.clone();
    }

    @Override
    protected void setServerInstance(Project project, UpdateHelper helper, String serverInstanceID) {
        EarProjectProperties.setServerInstance((EarProject)project, helper, serverInstanceID);
    }
    
}
