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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.ChangeListener;
import org.netbeans.modules.form.palette.PaletteUtils;
import org.openide.WizardDescriptor;
import org.openide.explorer.*;
import org.openide.util.ChangeSupport;

/**
 * The third panel in the wizard for adding new components to the palette.
 * Lets the user choose the palette category where to add the selected
 * components.
 *
 * @author Tomas Pavek
 */

class ChooseCategoryWizardPanel implements WizardDescriptor.FinishablePanel<AddToPaletteWizard> {

    private CategorySelector categorySelector;

    private final ChangeSupport cs = new ChangeSupport(this);

    // ----------
    // WizardDescriptor.Panel implementation

    @Override
    public java.awt.Component getComponent() {
        if (categorySelector == null) { // create the UI component for the wizard step
            categorySelector = new CategorySelector();

            // wizard API: set the caption and index of this panel
            categorySelector.setName(PaletteUtils.getBundleString("CTL_SelectCategory_Caption")); // NOI18N
            categorySelector.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, // NOI18N
                                               Integer.valueOf(2));

            categorySelector.getExplorerManager().addPropertyChangeListener(
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent ev) {
                        if (ExplorerManager.PROP_SELECTED_NODES.equals(ev.getPropertyName()))
                            cs.fireChange();
                    }
                });
        }

        return categorySelector;
    }

    @Override
    public org.openide.util.HelpCtx getHelp() {
        // PENDING
        return new org.openide.util.HelpCtx("beans.adding"); // NOI18N
    }

    @Override
    public boolean isValid() {
        return categorySelector != null
               && categorySelector.getSelectedCategory() != null;
    }

    @Override
    public void readSettings(AddToPaletteWizard settings) {
    }

    @Override
    public void storeSettings(AddToPaletteWizard settings) {
        if (categorySelector != null)
            settings.setSelectedCategory(categorySelector.getSelectedCategory());
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        cs.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }

    // WizardDescriptor.FinishablePanel implementation
    @Override
    public boolean isFinishPanel() {
        return true;
    }

}
