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
package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb;

import java.awt.Component;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class MdbPropertiesPanel implements WizardDescriptor.FinishablePanel {

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final WizardDescriptor wizardDescriptor;
    private MdbPropertiesPanelVisual panel;

    /**
     * Creates new MdbPropertiesPanel.
     *
     * @param wizardDescriptor parent wizard descriptor
     */
    public MdbPropertiesPanel(WizardDescriptor wizardDescriptor) {
        this.wizardDescriptor = wizardDescriptor;
    }

    @Override
    public boolean isFinishPanel() {
        return isValid();
    }

    @Override
    public Component getComponent() {
        if (panel == null) {
            Project project = Templates.getProject(wizardDescriptor);
            J2eeProjectCapabilities j2eeProjectCapabilities = J2eeProjectCapabilities.forProject(project);
            panel = new MdbPropertiesPanelVisual(j2eeProjectCapabilities);
        }
        return panel;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb.MdbPropertiesPanel");
    }

    @Override
    public void readSettings(Object settings) {
        WizardDescriptor descriptor = (WizardDescriptor) settings;
        panel.read(descriptor);
    }

    @Override
    public void storeSettings(Object settings) {
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    protected final void fireChangeEvent() {
        changeSupport.fireChange();
    }

    public Map<String, String> getProperties() {
        return panel.getProperties();
    }

}
