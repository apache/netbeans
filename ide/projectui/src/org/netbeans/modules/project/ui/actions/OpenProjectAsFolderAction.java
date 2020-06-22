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
package org.netbeans.modules.project.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.project.ProjectManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import static org.netbeans.modules.project.ui.actions.Bundle.*;
import org.openide.awt.DynamicMenuContent;
import org.openide.loaders.DataFolder;
import org.openide.util.actions.Presenter;

/**
 *
 * @author lkishalmi
 */
@ActionID(id = "org.netbeans.modules.project.ui.actions.OpenProjectAsFolderAction", category = "Project")
@ActionRegistration(displayName = "#OpenProjectAsFolderAction.LBL_action", lazy=false)
@ActionReference(path = "Loaders/folder/any/Actions", position = 101)
@NbBundle.Messages("OpenProjectAsFolderAction.LBL_action=Open Project of Folder")
public class OpenProjectAsFolderAction  extends AbstractAction implements ContextAwareAction {

    public OpenProjectAsFolderAction() {
        super(OpenProjectAsFolderAction_LBL_action());
    }

    public @Override void actionPerformed(ActionEvent e) {
        // Cannot be invoked without any context.
        assert false;
    }

    @Override
    public Action createContextAwareInstance(Lookup ctx) {
        return new CustomPopupAction(ctx);
    }

    private final class OpenAsProjectAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {

        }

    }

    private final class CustomPopupAction extends AbstractAction implements Presenter.Popup {

        private final Lookup context;
        private ProjectManager.Result[] projects;

        public CustomPopupAction(Lookup context) {
            super(OpenProjectAsFolderAction_LBL_action());
            this.context = context;

            projects = new ProjectManager.Result[0];
            for (DataFolder d : context.lookupAll(DataFolder.class)) {
                projects = ProjectManager.getDefault().checkProject(d.getPrimaryFile());
                if (projects.length > 0) {
                    break;
                }
            }
            if (projects.length <= 1) {
                putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
                setEnabled(false);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }

        @Override
        public JMenuItem getPopupPresenter() {
            if (projects.length <=1) return null;
            
            final JMenu menu = new JMenu(OpenProjectAsFolderAction_LBL_action());

            for (ProjectManager.Result project : projects) {
                if (project.getProjectType() != null) {
                    JMenuItem item = new JMenuItem("Open as: ");
                    item.setText(project.getProjectType());
                    menu.add(item);
                }
            }
            return menu;
        }
    }
}
