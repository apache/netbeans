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
package org.netbeans.modules.web.clientproject.ui.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ui.wizard.CreateSiteTemplate;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

@Messages("CTL_CreateSiteTemplateAction=Save as Template")
    @ActionID(id = "org.netbeans.modules.web.clientproject.ui.action.CreateSiteTemplateAction", category = "Project")
    @ActionRegistration(displayName = "#CTL_CreateSiteTemplateAction", lazy = false)
    @ActionReference(path = "Projects/org-netbeans-modules-web-clientproject/Actions", position = 3080)
public final class CreateSiteTemplateAction extends AbstractAction implements ContextAwareAction {

    private final ClientSideProject project;


    public CreateSiteTemplateAction() {
        this(null, false);
    }

    public CreateSiteTemplateAction(ClientSideProject p) {
        this(p, true);
    }

    private CreateSiteTemplateAction(ClientSideProject project, boolean enabled) {
        this.project = project;
        setEnabled(enabled);
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        putValue(Action.NAME, Bundle.CTL_CreateSiteTemplateAction());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        assert project != null;
        CreateSiteTemplate.showWizard(project);
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        Project proj = actionContext.lookup(Project.class);
        if (proj == null) {
            return this;
        }
        ClientSideProject clientSideProject = proj.getLookup().lookup(ClientSideProject.class);
        if (clientSideProject != null
                && clientSideProject.isHtml5Project()) {
            return new CreateSiteTemplateAction(clientSideProject);
        }
        return this;
    }

}
