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

package org.netbeans.modules.web.project.ui;

import org.openide.filesystems.FileObject;
import org.openide.nodes.*;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javaee.project.api.ant.ui.logicalview.AbstractLogicalViewProvider.LogicalViewRootNode;
import org.netbeans.modules.javaee.project.api.ant.ui.logicalview.AbstractLogicalViewProvider2;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.web.clientproject.api.remotefiles.RemoteFilesNodeFactory;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.openide.util.NbBundle;

/**
 * Support for creating logical views.
 */
public class WebLogicalViewProvider extends AbstractLogicalViewProvider2 {

    public WebLogicalViewProvider(WebProject project, UpdateHelper helper,
            PropertyEvaluator evaluator, ReferenceHelper resolver, J2eeModuleProvider j2eeModuleProvider) {
        super(project, helper, evaluator, resolver, j2eeModuleProvider);
    }

    @Override
    public Node createLogicalView() {
        return new LogicalViewRootNode("Projects/org-netbeans-modules-web-project/Nodes",
                    "org-netbeans-modules-web-project",
                    "org/netbeans/modules/web/project/ui/resources/webProjectIcon.gif",
                    NbBundle.getMessage(WebLogicalViewProvider.class, "HINT_project_root_node"),
                    WebLogicalViewProvider.class);
    }

    @Override
    protected Node findPath(Node root, Project proj, FileObject fo) {
        Node node = super.findPath(root, proj, fo);
        if (node != null) {
            return node;
        }
        return findNodeInDocBase(root, fo, WebProjectProperties.WEB_DOCBASE_DIR);
    }

    private static final String[] BREAKABLE_PROPERTIES = new String[] {
        ProjectProperties.JAVAC_CLASSPATH,
        WebProjectProperties.DEBUG_CLASSPATH,
        ProjectProperties.RUN_TEST_CLASSPATH,
        WebProjectProperties.DEBUG_TEST_CLASSPATH,
        ProjectProperties.JAVAC_TEST_CLASSPATH,
        WebProjectProperties.WAR_CONTENT_ADDITIONAL,
        ProjectProperties.ENDORSED_CLASSPATH,
        WebProjectProperties.WEB_DOCBASE_DIR
    };

    @Override
    public String[] getBreakableProperties() {
        return createListOfBreakableProperties(((WebProject)getProject()).getSourceRoots(), ((WebProject)getProject()).getTestSourceRoots(), BREAKABLE_PROPERTIES);
    }

    @Override
    protected void setServerInstance(Project project, UpdateHelper helper, String serverInstanceID) {
        WebProjectProperties.setServerInstance((WebProject)project, helper, serverInstanceID);
    }

    @NodeFactory.Registration(projectType="org-netbeans-modules-web-project",position=149)
    public static NodeFactory createRemoteFiles() {
        return RemoteFilesNodeFactory.createRemoteFilesNodeFactory();
    }

}
