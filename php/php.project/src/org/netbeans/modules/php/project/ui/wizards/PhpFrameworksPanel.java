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

package org.netbeans.modules.php.project.ui.wizards;

import java.awt.Component;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.framework.PhpFrameworks;
import org.netbeans.modules.php.spi.framework.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.framework.PhpModuleExtender;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class PhpFrameworksPanel implements WizardDescriptor.Panel<WizardDescriptor>,
        WizardDescriptor.FinishablePanel<WizardDescriptor>, ChangeListener {

    static final String VALID = "PhpFrameworksPanel.valid"; // NOI18N // used in the previous steps while validating
    static final String EXTENDERS = "frameworks"; // NOI18N

    private final String[] steps;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private PhpFrameworksPanelVisual frameworksPanel = null;
    private WizardDescriptor descriptor = null;

    public PhpFrameworksPanel(String[] steps) {
        this.steps = steps.clone();
    }

    String[] getSteps() {
        return steps;
    }

    @Override
    public Component getComponent() {
        if (frameworksPanel == null) {
            frameworksPanel = new PhpFrameworksPanelVisual(this, createExtenders());
        }
        return frameworksPanel;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.php.project.ui.wizards.PhpFrameworksPanel"); // NOI18N
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        getComponent();
        descriptor = settings;

        frameworksPanel.addPhpFrameworksListener(this);
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        getComponent();
        frameworksPanel.removePhpFrameworksListener(this);

        descriptor.putProperty(EXTENDERS, frameworksPanel.getSelectedExtenders());
    }

    @Override
    public boolean isValid() {
        getComponent();
        descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, " "); // NOI18N

        // validate frameworks
        String error = null;
        String warning = null;
        PhpModuleExtender visibleExtender = frameworksPanel.getSelectedVisibleExtender();
        if (visibleExtender != null) {
            if (!visibleExtender.isValid()) {
                error = visibleExtender.getErrorMessage();
            }
            warning = visibleExtender.getWarningMessage();
        }
        Set<PhpFrameworkProvider> invalidFrameworks = new HashSet<>();
        for (Entry<PhpFrameworkProvider, PhpModuleExtender> entry : frameworksPanel.getSelectedExtenders().entrySet()) {
            PhpModuleExtender extender = entry.getValue();
            if (extender != null
                    && !extender.isValid()) {
                PhpFrameworkProvider frameworkProvider = entry.getKey();
                if (error == null) {
                    error = NbBundle.getMessage(PhpFrameworksPanel.class, "MSG_InvalidFramework", frameworkProvider.getName());
                }
                invalidFrameworks.add(frameworkProvider);
            }
        }
        frameworksPanel.markInvalidFrameworks(invalidFrameworks);

        if (error != null) {
            descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, error);
            descriptor.putProperty(VALID, false);
            return false;
        } else if (warning != null) {
            descriptor.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, warning);
        }

        descriptor.putProperty(VALID, true);
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

    @Override
    public boolean isFinishPanel() {
        return NewPhpProjectWizardIterator.areAllStepsValid(descriptor);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }

    final void fireChangeEvent() {
        changeSupport.fireChange();
    }

    private Map<PhpFrameworkProvider, PhpModuleExtender> createExtenders() {
        Map<PhpFrameworkProvider, PhpModuleExtender> extenders = new LinkedHashMap<>();
        for (PhpFrameworkProvider provider : PhpFrameworks.getFrameworks()) {
            PhpModuleExtender extender = provider.createPhpModuleExtender(null);
            if (extender != null) {
                extenders.put(provider, extender);
            }
        }
        return extenders;
    }
}
