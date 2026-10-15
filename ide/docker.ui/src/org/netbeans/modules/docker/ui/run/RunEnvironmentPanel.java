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
package org.netbeans.modules.docker.ui.run;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import static java.util.stream.Collectors.toSet;

public class RunEnvironmentPanel implements WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {

    @SuppressWarnings("this-escape")
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private RunEnvironmentVisual component;

    private WizardDescriptor wizard;

    public RunEnvironmentPanel() {
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public RunEnvironmentVisual getComponent() {
        if (component == null) {
            component = new RunEnvironmentVisual();
            component.addChangeListener(this);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx("help.key.here");
    }

    @NbBundle.Messages({
        "MSG_EmptyKey=Key not specified",
        "# {0} - Keys that were specified multiple times",
        "MSG_DuplicateKeys=Duplicate keys: {0}",
        "# {0} - Keys that contained spaces",
        "MSG_SpaceInKey=Key contains space: {0}",
        "MSG_EnvFileNotFound=.env File not found"
    })
    @Override
    public boolean isValid() {
        // clear the error message
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);
        wizard.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, null);

        List<EnvEntry> mapping = component.getEnvironment();
        for (EnvEntry m : mapping) {
            if (m.key().isBlank()) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.MSG_EmptyKey());
                return false;
            }
        }

        List<String> duplicateEntries = mapping
                .stream()
                .collect(Collectors.toMap(e -> e.key(), e -> 1, (v1, v2) -> v1 + v2))
                .entrySet()
                .stream()
                .filter(e -> e.getValue() > 1)
                .map(e -> e.getKey())
                .toList();

        if(! duplicateEntries.isEmpty()) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.MSG_DuplicateKeys(duplicateEntries));
            return false;
        }

        Set<String> entriesWithSpace = mapping
                .stream()
                .map(e -> e.key())
                .filter(key -> key.matches(".*?\s+.*"))
                .collect(toSet());

        if(! entriesWithSpace.isEmpty()) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.MSG_SpaceInKey(entriesWithSpace));
            return false;
        }

        String envFilePath = component.getEnvFilePath();

        if (envFilePath != null && !envFilePath.isBlank()) {
            File envFileFile = new File(envFilePath);
            if (!envFileFile.canRead()) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.MSG_EnvFileNotFound());
                return false;
            }
        }

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
    public void readSettings(WizardDescriptor wiz) {
        if (wizard == null) {
            wizard = wiz;
        }

        @SuppressWarnings("unchecked")
        List<EnvEntry> envEntries = (List<EnvEntry>) wiz.getProperty(RunTagWizard.ENV_ENTRIES);
        component.setEnvironment(envEntries == null ? new ArrayList<>() : envEntries);
        @SuppressWarnings("unchecked")
        String envFile = (String) wiz.getProperty(RunTagWizard.ENV_FILE);
        component.setEnvFilePath(envFile == null ? "" : envFile);


        // XXX revalidate; is this bug?
        changeSupport.fireChange();
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        wiz.putProperty(RunTagWizard.ENV_ENTRIES, component.getEnvironment());
        if(component.getEnvFilePath() != null && ! component.getEnvFilePath().isBlank()) {
            wiz.putProperty(RunTagWizard.ENV_FILE, component.getEnvFilePath());
        } else {
            wiz.putProperty(RunTagWizard.ENV_FILE, null);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }
}
