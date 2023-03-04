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

package org.netbeans.modules.j2ee.core.api.support.wizard;

import java.awt.Component;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Parameters;

/**
 * A {@link org.openide.WizardDescriptor.Panel} which delegates to another panel.
 * It can be used to add further validation to e.g. a panel returned by
 * <code>JavaTemplates.createPackageChooser()</code>.
 *
 * <p>This class currently only implements {@link org.openide.WizardDescriptor.Panel}
 * and {@link org.openide.WizardDescriptor.FinishablePanel}. It will not delegate
 * methods in other subinterfaces of {@link org.openide.WizardDescriptor.Panel}.</p>
 *
 * @param  <Data> the type of the object representing the wizard state.
 *
 * @author Andrei Badea
 */
public class DelegatingWizardDescriptorPanel<Data> implements WizardDescriptor.FinishablePanel<Data> {

    private final WizardDescriptor.Panel<Data> delegate;

    private WizardDescriptor wizardDescriptor;
    private Project project;

    /**
     * Create a new instance of DelegatingWizardDescriptorPanel.
     * 
     * @param delegate the panel to wrap; must not be null.
     */
    public DelegatingWizardDescriptorPanel(WizardDescriptor.Panel<Data> delegate) {
        Parameters.notNull("delegate", delegate); //NOI18N
        this.delegate = delegate;
    }

    /**
     * @see org.openide.WizardDescriptor.Panel#getComponent()
     */
    public Component getComponent() {
        return delegate.getComponent();
    }

    /**
     * @see org.openide.WizardDescriptor.Panel#getHelp()
     */
    public HelpCtx getHelp() {
        return delegate.getHelp();
    }

    /**
     * @see org.openide.WizardDescriptor.Panel#readSettings(Object)
     */
    public void readSettings(Data settings) {
        if (wizardDescriptor == null) {
            wizardDescriptor = (WizardDescriptor)settings;
            project = Templates.getProject((WizardDescriptor)settings);
        }
        delegate.readSettings(settings);
    }

    /**
     * @see org.openide.WizardDescriptor.Panel#storeSettings(Object)
     */
    public void storeSettings(Data settings) {
        delegate.storeSettings(settings);
    }

    /**
     * @see org.openide.WizardDescriptor.Panel#isValid()
     */
    public boolean isValid() {
        return delegate.isValid();
    }

    /**
     * @see org.openide.WizardDescriptor.Panel#addChangeListener(ChangeListener)
     */
    public void addChangeListener(ChangeListener l) {
        delegate.addChangeListener(l);
    }

    /**
     * @see org.openide.WizardDescriptor.Panel#removeListener(ChangeListener)
     */
    public void removeChangeListener(ChangeListener l) {
        delegate.removeChangeListener(l);
    }

    /**
     * @return true if the wrapped panel is a <code>FinishablePanel</code>
     * and is finish panel, false otherwise.
     * @see org.openide.WizardDescriptor.FinishablePanel#isFinishPanel()
     */
    public boolean isFinishPanel() {
        if (delegate instanceof WizardDescriptor.FinishablePanel) {
            return ((WizardDescriptor.FinishablePanel)delegate).isFinishPanel();
        }
        return false;
    }

    /**
     * @return the wizard descriptor passed to this panel or null 
     * if none was passed.
     */
    protected WizardDescriptor getWizardDescriptor() {
        return wizardDescriptor;
    }

    /**
     * @return the project in which the panel is invoked, possibly null.
     */
    protected Project getProject() {
        return project;
    }
}
