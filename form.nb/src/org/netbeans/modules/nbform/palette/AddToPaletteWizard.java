/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
