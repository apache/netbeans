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
package org.netbeans.modules.javascript.nodejs.ui.wizard;

import java.awt.Component;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

abstract class BaseWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {

    @StaticResource
    protected static final String NODEJS_PROJECT_ICON = "org/netbeans/modules/javascript/nodejs/ui/resources/new-nodejs-project.png"; // NOI18N

    protected volatile WizardDescriptor wizardDescriptor;

    private int index;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;


    abstract String getWizardTitle();

    abstract WizardDescriptor.Panel<WizardDescriptor>[] createPanels();

    abstract String[] createSteps();

    abstract void uninitializeInternal();

    @Override
    public final void initialize(WizardDescriptor wizard) {
        wizardDescriptor = wizard;
        // #245975
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                initialize();
            }
        });
    }

    final void initialize() {
        assert EventQueue.isDispatchThread();
        index = 0;
        panels = createPanels();
        String[] steps = createSteps();
        // XXX should be lazy
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            assert steps[i] != null : "Missing name for step: " + i;
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                // name
                jc.setName(steps[i]);
            }
        }
    }

    @Override
    public final void uninitialize(WizardDescriptor wizard) {
        panels = null;
        uninitializeInternal();
    }

    @Override
    public final Set<FileObject> instantiate() throws IOException {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        wizardDescriptor.putProperty("NewProjectWizard_Title", getWizardTitle()); // NOI18N
        return panels[index];
    }

    @NbBundle.Messages({
        "# {0} - current step index",
        "# {1} - number of steps",
        "BaseWizardIterator.name={0} of {1}"
    })
    @Override
    public String name() {
        return Bundle.BaseWizardIterator_name(index + 1, panels.length);
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
    public void addChangeListener(ChangeListener l) {
        // noop
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        // noop
    }

}
