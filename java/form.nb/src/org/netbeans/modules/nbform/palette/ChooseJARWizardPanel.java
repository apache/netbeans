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

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.BorderFactory;
import javax.swing.event.*;
import java.awt.event.*;
import java.beans.*;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.form.palette.PaletteUtils;

import org.netbeans.modules.nbform.project.ClassSourceResolver;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;

/**
 * The first panel in the wizard for adding new components to the palette from
 * a JAR. In this panel (as the first step) the user chooses the JAR file.
 * The alternative first steps are ChooseLibraryWizardPanel and
 * ChooseProjectWizardPanel.
 *
 * @author Tomas Pavek
 */

class ChooseJARWizardPanel implements WizardDescriptor.Panel<AddToPaletteWizard> {

    private JFileChooser fileChooser;
    private static String lastDirectoryUsed;

    private AddToPaletteWizard wizard;

    private final ChangeSupport cs = new ChangeSupport(this);

    // ----------
    // WizardDescriptor.Panel implementation

    @Override
    public java.awt.Component getComponent() {
        if (fileChooser == null) { // create the UI component for the wizard step
            fileChooser = new JFileChooser(lastDirectoryUsed);
            fileChooser.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

            // wizard API: set the caption and index of this panel
            fileChooser.setName(PaletteUtils.getBundleString("CTL_SelectJAR_Caption")); // NOI18N
            fileChooser.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, // NOI18N
                                          Integer.valueOf(0));

            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(true);
            fileChooser.setControlButtonsAreShown(false);
            fileChooser.setMultiSelectionEnabled(true);

            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory()
                           || f.getName().toLowerCase().endsWith(".jar"); // NOI18N
                }
                @Override
                public String getDescription() {
                    return PaletteUtils.getBundleString("CTL_JarArchivesMask"); // NOI18N
                }
            });

            fileChooser.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    if (JFileChooser.APPROVE_SELECTION.equals(ev.getActionCommand()))
                        wizard.stepToNext();
                    else if (JFileChooser.CANCEL_SELECTION.equals(ev.getActionCommand()))
                        fileChooser.getTopLevelAncestor().setVisible(false);
                }
            });

            fileChooser.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent ev) {
                    if (JFileChooser.SELECTED_FILES_CHANGED_PROPERTY
                                        .equals(ev.getPropertyName()))
                        cs.fireChange();
                }
            });

            fileChooser.getAccessibleContext().setAccessibleDescription(PaletteUtils.getBundleString("CTL_SelectJAR_Step")); // NOI18N
        }

        return fileChooser;
    }

    @Override
    public org.openide.util.HelpCtx getHelp() {
        // PENDING
        return new org.openide.util.HelpCtx("beans.adding"); // NOI18N
    }

    @Override
    public boolean isValid() {
        if (fileChooser != null && fileChooser.getSelectedFiles().length > 0) {
            lastDirectoryUsed = fileChooser.getCurrentDirectory().getAbsolutePath();
            return true;
        }
        return false;
    }

    @Override
    public void readSettings(AddToPaletteWizard settings) {
        wizard = settings;
    }

    @Override
    public void storeSettings(AddToPaletteWizard settings) {
        if (fileChooser != null) {
            List<ClassSourceResolver.JarEntry> entries = new ArrayList<ClassSourceResolver.JarEntry>();
            for (File jar : fileChooser.getSelectedFiles()) {
                entries.add(new ClassSourceResolver.JarEntry(jar));
            }
            settings.setJARFiles(entries);
        }
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
