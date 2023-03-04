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
package org.netbeans.modules.javascript.nodejs.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.exec.NpmExecutable;
import org.netbeans.modules.javascript.nodejs.util.NodeJsUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

@ActionID(id = "org.netbeans.modules.javascript.nodejs.ui.actions.NpmInstallAction", category = "Build")
@ActionRegistration(displayName = "#NpmInstallAction.name", lazy = false)
@ActionReferences({
    @ActionReference(path = "Editors/text/package+x-json/Popup", position = 906),
    @ActionReference(path = "Loaders/text/package+x-json/Actions", position = 156),
    @ActionReference(path = "Projects/org-netbeans-modules-web-clientproject/Actions", position = 170),
    @ActionReference(path = "Projects/org-netbeans-modules-php-project/Actions", position = 110),
    @ActionReference(path = "Projects/org-netbeans-modules-web-project/Actions", position = 650),
    @ActionReference(path = "Projects/org-netbeans-modules-maven/Actions", position = 750),
})
@NbBundle.Messages("NpmInstallAction.name=npm Install")
public final class NpmInstallAction extends AbstractAction implements ContextAwareAction {

    private static final RequestProcessor RP = new RequestProcessor(NpmInstallAction.class);

    private final Project project;


    public NpmInstallAction() {
        this(null);
    }

    public NpmInstallAction(Project project) {
        this.project = project;
        setEnabled(project != null);
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        putValue(Action.NAME, Bundle.NpmInstallAction_name());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        assert project != null;
        final NpmExecutable npm = NpmExecutable.getDefault(project, true);
        if (npm == null) {
            return;
        }
        RP.post(new Runnable() {
            @Override
            public void run() {
                NodeJsUtils.logUsageNpmInstall();
                npm.install();
            }
        });
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new NpmInstallAction(NodeJsUtils.getPackageJsonProject(context));
    }

}
