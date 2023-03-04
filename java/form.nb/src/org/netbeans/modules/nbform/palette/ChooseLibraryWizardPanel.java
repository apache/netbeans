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

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryChooser;
import org.netbeans.modules.form.palette.PaletteUtils;
import org.netbeans.modules.nbform.project.ClassSourceResolver;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;

/**
 * The first panel in the wizard for adding new components to the palette from
 * a library. In this panel the user chooses a library from available libraries
 * installed in the IDE.
 *
 * @author Tomas Pavek, Jesse Glick
 */
class ChooseLibraryWizardPanel implements WizardDescriptor.Panel<AddToPaletteWizard> {

    private LibraryChooser.Panel librarySelector;
    private Component librarySelectorComponent;

//    private AddToPaletteWizard wizard;

    private final ChangeSupport cs = new ChangeSupport(this);

    // ----------
    // WizardDescriptor.Panel implementation

    @Override
    public java.awt.Component getComponent() {
        if (librarySelectorComponent == null) {
            librarySelector = LibraryChooser.createPanel(null, null);
            librarySelectorComponent = librarySelector.getVisualComponent();

            // wizard API: set the caption and index of this panel
            librarySelectorComponent.setName(
                PaletteUtils.getBundleString("CTL_SelectLibrary_Caption")); // NOI18N
            ((JComponent) librarySelectorComponent).putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, // NOI18N
                                              Integer.valueOf(0));

            librarySelector.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    cs.fireChange();
                }
            });
                    }

        return librarySelectorComponent;
    }

    @Override
    public org.openide.util.HelpCtx getHelp() {
        // PENDING
        return new org.openide.util.HelpCtx("beans.adding"); // NOI18N
    }

    @Override
    public boolean isValid() {
        return librarySelector != null
               && !librarySelector.getSelectedLibraries().isEmpty();
    }

    @Override
    public void readSettings(AddToPaletteWizard settings) {
//        wizard = settings;
    }

    @Override
    public void storeSettings(AddToPaletteWizard settings) {
        if (librarySelector != null) { // create the UI component for the wizard step
            List<ClassSourceResolver.LibraryEntry> entries = new ArrayList<ClassSourceResolver.LibraryEntry>();
            for (Library lib : librarySelector.getSelectedLibraries()) {
                entries.add(new ClassSourceResolver.LibraryEntry(lib));
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
