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

package org.netbeans.modules.gradle.newproject;

import java.awt.Component;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class ProjectAttriburesPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    WizardDescriptor.Panel<WizardDescriptor> bottomPanel;

    public ProjectAttriburesPanel(WizardDescriptor.Panel<WizardDescriptor> bottomPanel) {
        this.bottomPanel = bottomPanel;
    }

    public ProjectAttriburesPanel() {
        this(null);
    }


    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private ProjectAttributesPanelVisual component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public ProjectAttributesPanelVisual getComponent() {
        if (component == null) {
            Component bottom = bottomPanel != null ? bottomPanel.getComponent() : null;
            component = new ProjectAttributesPanelVisual(this, bottom);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return true;
        // If it depends on some condition (form filled out...) and
        // this condition changes (last form field filled in...) then
        // use ChangeSupport to implement add/removeChangeListener below.
        // WizardDescriptor.ERROR/WARNING/INFORMATION_MESSAGE will also be useful.
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {
        getComponent().read(wiz);
        if (bottomPanel != null) {
            bottomPanel.readSettings(wiz);
        }
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        getComponent().write(wiz);
        if (bottomPanel != null) {
            bottomPanel.storeSettings(wiz);
        }
    }

}
