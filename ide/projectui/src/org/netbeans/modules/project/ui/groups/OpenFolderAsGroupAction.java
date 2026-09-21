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
package org.netbeans.modules.project.ui.groups;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Project",
        id = "org.netbeans.modules.project.ui.groups.OpenFolderAsGroupAction"
)
@ActionRegistration(
        displayName = "#CTL_OpenFolderAsGroupAction",
        lazy = true, asynchronous = true
)
@ActionReference(path = "Menu/File", position = 1200, separatorAfter = 1250)
@Messages("CTL_OpenFolderAsGroupAction=Open Fol&der as Workspace...")
public final class OpenFolderAsGroupAction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent ev) {
        File dir = showWorkspaceFolderChooser(null, null);
        FileObject folder = FileUtil.toFileObject(dir);
        if (folder != null) {
            DirectoryGroup group = DirectoryGroup.create(folder.getNameExt(), folder);
            Group.setActiveGroup(group, true);
        }
    }

    static File showWorkspaceFolderChooser(java.awt.Component parent, final String hintPath) throws HeadlessException {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        File start = ProjectChooser.getProjectsFolder();
        if (hintPath != null && hintPath.trim().length() > 0) {
            start = new File(hintPath.trim());
        }
        chooser.setCurrentDirectory(start);
        final int result = chooser.showOpenDialog(parent);
        File f = result == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile() : null;
        return f;
    }

}
