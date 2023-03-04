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

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class PhpExtenderPanel implements WizardDescriptor.Panel<WizardDescriptor>,
        WizardDescriptor.FinishablePanel<WizardDescriptor> {

    static final String VALID = "PhpExtenderPanel.valid"; // NOI18N // used in the previous steps while validating

    private final PhpModuleExtender extender;
    private final String[] steps;
    private final int stepIndex;

    private volatile WizardDescriptor descriptor = null;

    // @GuardedBy("EDT")
    private JComponent component;


    @org.netbeans.api.annotations.common.SuppressWarnings(value = "EI_EXPOSE_REP2", justification = "Intentional") // NOI18N
    public PhpExtenderPanel(PhpModuleExtender extender, String[] steps, int stepIndex) {
        assert extender != null;
        assert steps != null;

        this.extender = extender;
        this.steps = steps;
        this.stepIndex = stepIndex;
    }

    @NbBundle.Messages({
        "# {0} - extender name",
        "PhpExtenderPanel.noUi={0} does not provide any configuration UI.",
    })
    @Override
    public JComponent getComponent() {
        assert EventQueue.isDispatchThread();
        if (component == null) {
            component = extender.getComponent();
            if (component == null) {
                // in fact, should not happen since it does not make much sense in this case
                component = new JPanel(new BorderLayout());
                component.add(new JLabel(Bundle.PhpExtenderPanel_noUi(extender.getDisplayName())), BorderLayout.NORTH);
            }
            // Provide a name in the title bar.
            component.setName(extender.getDisplayName());
            component.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, stepIndex);
            // Step name (actually the whole list for reference).
            component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return extender.getHelp();
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        descriptor = settings;
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        // noop
    }

    @Override
    public boolean isValid() {
        // clean any error
        descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, " "); // NOI18N
        // validate
        boolean valid = extender.isValid();
        String error = extender.getErrorMessage();
        String warning = extender.getWarningMessage();
        if (!valid && error == null) {
            throw new IllegalStateException("Extender " + extender.getIdentifier() + " invalid but error message is null");
        }
        if (valid && error != null) {
            throw new IllegalStateException("Extender " + extender.getIdentifier() + " valid but error message is not null");
        }
        if (error != null) {
            descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, error);
            setValid(false);
            return false;
        } else if (warning != null) {
            descriptor.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, warning);
        }
        setValid(true);
        return true;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        extender.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        extender.removeChangeListener(listener);
    }

    @Override
    public boolean isFinishPanel() {
        return NewPhpProjectWizardIterator.areAllStepsValid(descriptor);
    }

    private void setValid(boolean valid) {
        @SuppressWarnings("unchecked")
        Map<PhpModuleExtender, Boolean> validity = (Map<PhpModuleExtender, Boolean>) descriptor.getProperty(VALID);
        if (validity == null) {
            validity = new ConcurrentHashMap<>();
            descriptor.putProperty(VALID, validity);
        }
        validity.put(extender, valid);
    }

}
