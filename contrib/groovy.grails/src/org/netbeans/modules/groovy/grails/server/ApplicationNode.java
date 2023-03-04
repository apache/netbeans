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

package org.netbeans.modules.groovy.grails.server;

import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.groovy.grails.api.GrailsConstants;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Petr Hejl
 */
public class ApplicationNode extends AbstractNode {

    public ApplicationNode(final Project project, final Process process) {
        super(Children.LEAF);
        GrailsProjectConfig config = GrailsProjectConfig.forProject(project);
        ProjectInformation info = project.getLookup().lookup(ProjectInformation.class);
        setDisplayName(NbBundle.getMessage(
                GrailsInstance.class, "ApplicationNode.displayName", info.getDisplayName(), config.getPort()));
        setIconBaseWithExtension(GrailsConstants.GRAILS_ICON_16x16);

        getCookieSet().add(new ProcessCookie() {

            public Process getProcess() {
                return process;
            }
        });
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {SystemAction.get(StopAction.class)};
    }

    private static class StopAction extends NodeAction {

        public StopAction() {
            super();
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            for (Node node : activatedNodes) {
                ProcessCookie cookie = node.getCookie(ProcessCookie.class);
                if (cookie == null) {
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void performAction(Node[] activatedNodes) {
            for (Node node : activatedNodes) {
                ProcessCookie cookie = node.getCookie(ProcessCookie.class);
                if (cookie != null) {
                    cookie.getProcess().destroy();
                }
            }
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(ApplicationNode.class, "ApplicationNode.stopActionName");
        }

        @Override
        protected boolean asynchronous() {
            return false;
        }

    }
}
