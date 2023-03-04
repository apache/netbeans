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

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;

import org.netbeans.modules.form.palette.PaletteUtils;
import org.openide.*;

import org.netbeans.modules.form.project.ClassSource;
import org.netbeans.modules.nbform.project.ClassSourceResolver;

/**
 * A wizard allowing the user to add components to palette from a JAR file,
 * library, or a project. This class manages the whole wizard depending on the
 * type of source the user wants to choose from. There are three steps in the
 * wizard - selecting source, selecting components, and selecting palette
 * category.
 *
 * @author Tomas Pavek
 */

class AddToPaletteWizard extends WizardDescriptor {

    ATPWizardIterator wizardIterator;

    private List<? extends ClassSource.Entry> selectedFiles;
    private BeanInstaller.ItemInfo[] selectedBeans;
    private String selectedCategory;
    private Class<? extends ClassSource.Entry> sourceType;

    private java.awt.Dialog dialog;

    // ---------

    public AddToPaletteWizard() {
        this(new ATPWizardIterator());
    }

    private AddToPaletteWizard(ATPWizardIterator iterator) {
        wizardIterator = iterator;

        putProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
        putProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
        putProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N

        setTitle(PaletteUtils.getBundleString("CTL_AddToPaletteWizard_Title")); // NOI18N
        setTitleFormat(new java.text.MessageFormat("{0}")); // NOI18N
    }

    public boolean show(Class<? extends ClassSource.Entry> sourceType) {
        String firstStep_key;
        this.sourceType = sourceType;
        if (sourceType == ClassSourceResolver.JarEntry.class)
            firstStep_key = "CTL_SelectJAR_Step"; // NOI18N
        else if (sourceType == ClassSourceResolver.LibraryEntry.class)
            firstStep_key = "CTL_SelectLibrary_Step"; // NOI18N
        else if (sourceType == ClassSourceResolver.ProjectEntry.class)
            firstStep_key = "CTL_SelectProject_Step"; // NOI18N
        else
            throw new IllegalArgumentException();

        putProperty(WizardDescriptor.PROP_CONTENT_DATA,  // NOI18N
                    new String[] { PaletteUtils.getBundleString(firstStep_key),
                                   PaletteUtils.getBundleString("CTL_SelectBeans_Step"), // NOI18N
                                   PaletteUtils.getBundleString("CTL_SelectCategory_Step") }); // NOI18N

        wizardIterator.setSourceType(sourceType);
        setPanelsAndSettings(wizardIterator, this);
        updateState();

        if (dialog == null)
            dialog = DialogDisplayer.getDefault().createDialog(this);
        dialog.setVisible(true);
        dialog.dispose();

        return getValue() == FINISH_OPTION;
    }

    // -------

    void stepToNext() {
        if (wizardIterator.hasNext()) {
            wizardIterator.nextPanel();
            updateState();
        }
    }

    void setJARFiles(List<? extends ClassSource.Entry> files) {
        selectedFiles = files;
    }

    /** @return the JAR files representing the selected source in the first
     * step of the wizard (i.e. a JAR file directly, library, or project) */
    List<? extends ClassSource.Entry> getJARFiles() {
        return selectedFiles;
    }

    void setSelectedBeans(BeanInstaller.ItemInfo[] beans) {
        selectedBeans = beans;
    }

    BeanInstaller.ItemInfo[] getSelectedBeans() {
        return selectedBeans;
    }

    void setSelectedCategory(String name) {
        selectedCategory = name;
    }

    String getSelectedCategory() {
        return selectedCategory;
    }
    
    Class<? extends ClassSource.Entry> getSourceType() {
        return sourceType;
    }

    // -------

    /** Wizard iterator implementation for Add to Palette wizard */
    static class ATPWizardIterator implements WizardDescriptor.Iterator<AddToPaletteWizard> {

        List<WizardDescriptor.Panel<AddToPaletteWizard>> panels = new ArrayList<WizardDescriptor.Panel<AddToPaletteWizard>>();
        int stage;

        void setSourceType(Class<? extends ClassSource.Entry> sourceType) {
            panels.clear();
            if (sourceType == ClassSourceResolver.JarEntry.class)
                panels.add(new ChooseJARWizardPanel());
            else if (sourceType == ClassSourceResolver.LibraryEntry.class)
                panels.add(new ChooseLibraryWizardPanel());
            else if (sourceType == ClassSourceResolver.ProjectEntry.class)
                panels.add(new ChooseProjectWizardPanel());
            else
                throw new IllegalArgumentException();

            panels.add(new ChooseBeansWizardPanel());
            panels.add(new ChooseCategoryWizardPanel());

            stage = 1;
        }

        static int getPanelsCount() {
            return 3;
        }

        // ------
        // WizardDescriptor.Iterator implementation

        @Override
        public WizardDescriptor.Panel<AddToPaletteWizard> current() {
            return panels.get(stage - 1);
        }

        @Override
        public boolean hasNext() {
            return stage < getPanelsCount();
        }

        @Override
        public boolean hasPrevious() {
            return stage > 1;
        }

        @Override
        public java.lang.String name() {
            return ""; // NOI18N
        }

        @Override
        public void nextPanel() {
            if (stage < getPanelsCount())
                stage++;
        }

        @Override
        public void previousPanel() {
            if (stage > 1)
                stage--;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
        }
    }
}
