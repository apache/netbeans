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

package org.netbeans.modules.j2ee.ejbjarproject.ui;


import org.netbeans.api.project.Project;
import org.netbeans.modules.javaee.project.api.ant.ui.logicalview.AbstractLogicalViewProvider2;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.nodes.*;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.openide.util.NbBundle;

/**
 * Support for creating logical views.
 * @author Petr Hrebejk
 */
public class EjbJarLogicalViewProvider extends AbstractLogicalViewProvider2 {

    private final EjbJarProject project;

    public EjbJarLogicalViewProvider(EjbJarProject project, UpdateHelper updateHelper,
            PropertyEvaluator evaluator, SubprojectProvider spp,
            ReferenceHelper resolver, J2eeModuleProvider provider) {
        super(project, updateHelper, evaluator, resolver, provider);
        this.project = project;
    }

    public Node createLogicalView() {
        return new LogicalViewRootNode("Projects/org-netbeans-modules-j2ee-ejbjarproject/Nodes",
                    "org-netbeans-modules-j2ee-ejbjarproject",
                    "org/netbeans/modules/j2ee/ejbjarproject/ui/resources/ejbjarProjectIcon.gif",
                    NbBundle.getMessage(EjbJarLogicalViewProvider.class, "HINT_project_root_node"),
                    EjbJarLogicalViewProvider.class);
    }

    private static final String[] BREAKABLE_PROPERTIES = new String[] {
        ProjectProperties.JAVAC_CLASSPATH,
        EjbJarProjectProperties.DEBUG_CLASSPATH,
        ProjectProperties.RUN_TEST_CLASSPATH,
        EjbJarProjectProperties.DEBUG_TEST_CLASSPATH,
        ProjectProperties.ENDORSED_CLASSPATH,
        ProjectProperties.JAVAC_TEST_CLASSPATH,
    };

    @Override
    public String[] getBreakableProperties() {
        return createListOfBreakableProperties(project.getSourceRoots(), project.getTestSourceRoots(), BREAKABLE_PROPERTIES);
    }

    @Override
    protected void setServerInstance(Project project, UpdateHelper helper, String serverInstanceID) {
        EjbJarProjectProperties.setServerInstance((EjbJarProject)project, helper.getAntProjectHelper(), serverInstanceID);
    }

}
