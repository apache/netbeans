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
package org.netbeans.modules.fish.payara.micro.project.ui;

import java.awt.Component;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.validation.adapters.WizardDescriptorAdapter;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.openide.WizardDescriptor;
import static org.openide.util.NbBundle.getMessage;

/**
 * Base abstract class for all types of Maven enterprise projects.
 * Encapsulates some Wizard related stuffs and few methods common for every project type
 *
 * @author Martin Janicek
 */
public abstract class BaseWizardIterator implements WizardDescriptor.BackgroundInstantiatingIterator {

    protected WizardDescriptor descriptor;
    private int index;
    private WizardDescriptor.Panel[] panels;

    protected abstract WizardDescriptor.Panel[] createPanels(ValidationGroup validationGroup);

    public BaseWizardIterator() {
    }

    @Override
    public void initialize(WizardDescriptor descriptor) {
        this.descriptor = descriptor;
        this.index = 0;
        panels = createPanels(ValidationGroup.create(new WizardDescriptorAdapter(descriptor)));
        updateSteps();
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
        this.descriptor.putProperty("projdir", null); //NOI18N
        this.descriptor.putProperty("name", null); //NOI18N
        this.descriptor = null;
        panels = null;
    }

    @Override
    public String name() {
        return getMessage(BaseWizardIterator.class, "LBL_NameFormat", index + 1, panels.length);
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
    }

    private void updateSteps() {
        // Make sure list of steps is accurate.
        String[] steps = new String[panels.length];
        String[] basicOnes = new String[]{
            getMessage(BaseWizardIterator.class, "LBL_MavenProjectSettings"),
            getMessage(BaseWizardIterator.class, "LBL_PayaraMicroSettings")
        };
        System.arraycopy(basicOnes, 0, steps, 0, basicOnes.length);
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (i >= basicOnes.length || steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) {
                // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", i); //NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); //NOI18N
            }
        }
    }

}