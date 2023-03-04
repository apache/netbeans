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
package org.netbeans.modules.javascript.bower.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.bower.exec.BowerExecutable;
import org.netbeans.modules.javascript.bower.file.BowerJson;
import org.netbeans.modules.javascript.bower.util.BowerUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

@ActionID(id = "org.netbeans.modules.javascript.bower.ui.actions.BowerInstallAction", category = "Build")
@ActionRegistration(displayName = "#BowerInstallAction.name", lazy = false)
@ActionReferences({
    @ActionReference(path = "Editors/text/bower+x-json/Popup", position = 905),
    @ActionReference(path = "Loaders/text/bower+x-json/Actions", position = 155),
    @ActionReference(path = "Projects/org-netbeans-modules-web-clientproject/Actions", position = 175),
    @ActionReference(path = "Projects/org-netbeans-modules-php-project/Actions", position = 115),
    @ActionReference(path = "Projects/org-netbeans-modules-web-project/Actions", position = 660),
    @ActionReference(path = "Projects/org-netbeans-modules-maven/Actions", position = 760)
})
@NbBundle.Messages("BowerInstallAction.name=Bower Install")
public final class BowerInstallAction extends AbstractAction implements ContextAwareAction {

    private static final RequestProcessor RP = new RequestProcessor(BowerInstallAction.class);

    private final Project project;


    public BowerInstallAction() {
        this(null);
    }

    public BowerInstallAction(Project project) {
        this.project = project;
        setEnabled(project != null);
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        putValue(Action.NAME, Bundle.BowerInstallAction_name());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        assert project != null;
        final BowerExecutable bower = BowerExecutable.getDefault(project, true);
        if (bower == null) {
            return;
        }
        RP.post(new Runnable() {
            @Override
            public void run() {
                BowerUtils.logUsageBowerInstall();
                bower.install();
            }
        });
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        Project contextProject = context.lookup(Project.class);
        BowerJson bowerJson = null;
        if (contextProject != null) {
            // project action
            bowerJson = new BowerJson(contextProject.getProjectDirectory());
        } else {
            // package.json directly
            FileObject file = context.lookup(FileObject.class);
            if (file == null) {
                DataObject dataObject = context.lookup(DataObject.class);
                if (dataObject != null) {
                    file = dataObject.getPrimaryFile();
                }
            }
            if (file != null) {
                bowerJson = new BowerJson(file.getParent());
            }
        }
        if (bowerJson == null) {
            return this;
        }
        if (!bowerJson.exists()) {
            return this;
        }
        if (bowerJson.getDependencies().isEmpty()) {
            return this;
        }
        return new BowerInstallAction(contextProject != null ? contextProject : FileOwnerQuery.getOwner(Utilities.toURI(bowerJson.getFile())));
    }

}
