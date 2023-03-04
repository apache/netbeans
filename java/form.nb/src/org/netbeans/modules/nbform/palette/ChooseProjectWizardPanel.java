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

package org.netbeans.modules.nbform.palette;

import java.io.*;
import javax.swing.JFileChooser;
import javax.swing.BorderFactory;
import javax.swing.event.*;
import java.beans.*;
import java.util.*;

import org.openide.WizardDescriptor;
import org.openide.ErrorManager;
import org.openide.filesystems.*;
import org.netbeans.api.project.*;
import org.netbeans.modules.form.palette.PaletteUtils;
import org.netbeans.modules.nbform.project.ClassSourceResolver;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.util.ChangeSupport;

/**
 * The first panel in the wizard for adding new components to the palette from
 * a project. In this panel the user chooses a project (as project folder
 * using the project chooser UI).
 *
 * @author Tomas Pavek
 */

class ChooseProjectWizardPanel implements WizardDescriptor.Panel<AddToPaletteWizard> {

    private JFileChooser projectChooser;
    private static String lastDirectoryUsed;

    private final ChangeSupport cs = new ChangeSupport(this);

    // ----------
    // WizardDescriptor.Panel implementation

    @Override
    public java.awt.Component getComponent() {
        if (projectChooser == null) { // create the UI component for the wizard step
            projectChooser = ProjectChooser.projectChooser();
            projectChooser.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

            // wizard API: set the caption and index of this panel
            projectChooser.setName(PaletteUtils.getBundleString("CTL_SelectProject_Caption")); // NOI18N
            projectChooser.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, // NOI18N
                                             Integer.valueOf(0));

            if (lastDirectoryUsed != null)
                projectChooser.setCurrentDirectory(new File(lastDirectoryUsed));
            projectChooser.setControlButtonsAreShown(false);

            projectChooser.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent ev) {
                    String propName = ev.getPropertyName();
                    if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(propName)
                         || JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(propName))
                        cs.fireChange();
                }
            });
        }

        return projectChooser;
    }

    @Override
    public org.openide.util.HelpCtx getHelp() {
        // PENDING
        return new org.openide.util.HelpCtx("beans.adding"); // NOI18N
    }

    @Override
    public boolean isValid() {
        if (projectChooser != null) {
            File file = projectChooser.getSelectedFile();
            if (file != null) {
                FileObject projectDir = FileUtil.toFileObject(FileUtil.normalizeFile(file));
                if (projectDir != null) {
                    try {
                        Project project = ProjectManager.getDefault()
                                                   .findProject(projectDir);
                        if (project != null) { // it is a project directory
                            lastDirectoryUsed = projectChooser.getCurrentDirectory()
                                                               .getAbsolutePath();
                            return true;
                        }
                    }
                    catch (IOException ex) {} // ignore
                }
            }
        }
        return false;
    }

    @Override
    public void readSettings(AddToPaletteWizard settings) {
    }

    @Override
    public void storeSettings(AddToPaletteWizard settings) {
        if (projectChooser == null)
            return;

        File file = projectChooser.getSelectedFile();
        if (file == null) {
            return;
        }
        FileObject projectDir = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        if (projectDir == null) {
            return;
        }

        Project project = null;
        try {
            project = ProjectManager.getDefault().findProject(projectDir);
        }
        catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        if (project == null)
            return;

        List<ClassSourceResolver.ProjectEntry> entries = new ArrayList<ClassSourceResolver.ProjectEntry>();
        entries.add(new ClassSourceResolver.ProjectEntry(project));
        settings.setJARFiles(entries);
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        cs.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }

}
