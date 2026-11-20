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

package org.netbeans.modules.generic.project.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.generic.project.ui.wizard.GpWizardIterator;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import static org.netbeans.modules.generic.project.ui.actions.Bundle.*;

/**
 * Context action for folder nodes: turns a folder which is not a project
 * yet into a <em>local</em> generic project — recognition by the FileObject
 * attribute only, no marker file in the working tree — and opens it in the
 * Projects tab.
 * <p>
 * Registered on {@code Loaders/folder/any/Actions}, hence available on any
 * folder node (e.g. in the Favorites tab or beneath an open project).
 * Mirrors the inverse action {@code OpenProjectFolderAction} of the
 * projectui module.
 *
 * @author mbien
 */
@Messages("GpOpenAsProjectAction.LBL_action=Open as Generic Project")
@ActionID(category = "Project", id = "org.netbeans.modules.generic.project.ui.actions.GpOpenAsProjectAction")
@ActionRegistration(displayName = "#GpOpenAsProjectAction.LBL_action", lazy = false)
@ActionReference(path = "Loaders/folder/any/Actions", position = 110)
public final class GpOpenAsProjectAction extends AbstractAction implements ContextAwareAction {

    private static final Logger LOG = Logger.getLogger(GpOpenAsProjectAction.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(GpOpenAsProjectAction.class);

    public GpOpenAsProjectAction() {
        super(GpOpenAsProjectAction_LBL_action());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Cannot be invoked without any context.
        assert false;
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new ContextAction(context);
    }

    private static final class ContextAction extends AbstractAction {

        private final Lookup context;

        public ContextAction(Lookup context) {
            super(GpOpenAsProjectAction_LBL_action());
            this.context = context;
            checkEnabled(context);
        }

        private void checkEnabled(Lookup context1) {
            for (DataFolder d : context1.lookupAll(DataFolder.class)) {
                if (isCandidate(d.getPrimaryFile())) {
                    return;
                }
            }
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Run asynch so that the UI is not blocked; the open
            // shows a progress dialog.
            RP.post(() -> {
                for (DataFolder d : context.lookupAll(DataFolder.class)) {
                    FileObject fo = d.getPrimaryFile();
                    if (isCandidate(fo)) {
                        try {
                            adopt(fo);
                            return;
                        } catch (IOException x) {
                            LOG.log(Level.WARNING, "Cannot open folder as a generic project: " + fo, x); // NOI18N
                        }
                    }
                }
            });
        }

        /** A folder on the local file system which is not a project yet. */
        private static boolean isCandidate(FileObject fo) {
            File f = FileUtil.toFile(fo);
            return f != null && f.isDirectory() && !ProjectManager.getDefault().isProject(fo);
        }

        /**
         * Marks the folder as a local generic project (attribute only, no
         * marker file) and opens the resulting project.
         */
        private static void adopt(FileObject fo) throws IOException {
            File dir = FileUtil.normalizeFile(FileUtil.toFile(fo));
            GpWizardIterator.createProject(dir, true);
            // The folder may be cached as a non-project from the
            // isProject() check done to enable this action.
            ProjectManager.getDefault().clearNonProjectCache();
            Project p = ProjectManager.getDefault().findProject(fo);
            if (p == null) {
                throw new IOException("Project not found in " + dir); // NOI18N
            }
            OpenProjects.getDefault().open(new Project[]{p}, false, true);
            File parent = dir.getParentFile();
            if (parent != null && parent.exists()) {
                ProjectChooser.setProjectsFolder(parent);
            }
        }

    }

}
