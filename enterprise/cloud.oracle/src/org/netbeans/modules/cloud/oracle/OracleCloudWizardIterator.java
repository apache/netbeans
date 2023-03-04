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
package org.netbeans.modules.cloud.oracle;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "LBL_OC=Oracle Cloud",
    "URL_OCI_Setup=https://docs.oracle.com/en-us/iaas/Content/API/Concepts/sdkconfig.htm",
    "MSG_OCI_Setup=No Oracle Cloud configuration was found. Plase follow <a href=\"{0}\">the steps here</a>",
    "MSG_CheckingSetup=Checking Oracle Cloud Setup...",
    "MSG_TenancyFound=Found a tenancy <br/><b>{0}</b>"
})
public class OracleCloudWizardIterator implements WizardDescriptor.AsynchronousInstantiatingIterator {
    private static final RequestProcessor RP = new RequestProcessor(OracleCloudWizardIterator.class);
    private static final String TENANCY = "TENANCY";
    private Panel panel;

    // @GuardedBy(this)
    private CompletableFuture<List<OCIProfile>> profiles = new CompletableFuture<>();
    
    public OracleCloudWizardIterator() {
    }

    @Override
    public Set instantiate() throws IOException {
        for (OCIProfile p : panel.ui.getSelectedProfiles()) {
            OCIManager.getDefault().addConnectedProfile(p);
        }
        return Collections.emptySet();
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        RP.post(() -> {
            List<OCIProfile> list = new ArrayList<>();
            try {
                for (OCIProfile p : OCIManager.getDefault().listProfiles(null)) {
                    if (p.getTenancy().isPresent()) {
                        list.add(p);
                    }
                }
            } catch (IOException ex) {
                profiles.completeExceptionally(ex);
                return;
            }
            list.removeAll(OCIManager.getDefault().getConnectedProfiles());
            profiles.complete(list);
        });
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
    }
    
    @Override
    public WizardDescriptor.Panel current() {
        if (panel == null) {
            panel = new Panel();
            profiles.thenAccept((l) -> {
                // doh: no Swing EDT executor available...
                SwingUtilities.invokeLater(() -> updateProfilesUI(l));
            }).exceptionally(ex -> {
                panel.ui.showErrorMessage(ex.getLocalizedMessage());
                return null;
            });
        }
        return panel;
    }

    @Override
    public String name() {
        return Bundle.LBL_OC();
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public void nextPanel() {
    }

    @Override
    public void previousPanel() {
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }
    
    private void updateProfilesUI(List<OCIProfile> profiles) {
        if (panel == null) {
            return;
        }
        if (profiles.isEmpty()) {
            panel.ui.showErrorMessage(Bundle.MSG_OCI_Setup(Bundle.URL_OCI_Setup()));
        } else {
            panel.ui.setProfiles(profiles);
            // select by default
            panel.ui.setSelectedProfiles(profiles);
        }
    }

    static class Panel implements WizardDescriptor.Panel, PropertyChangeListener {
        private ChangeSupport changeSupport;
        private ConnectProfilePanel ui = new ConnectProfilePanel();
        
        @Override
        public Component getComponent() {
            return ui;
        }

        @Override
        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
        }

        @Override
        public void readSettings(Object settings) {
        }

        @Override
        public void storeSettings(Object settings) {
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            if (changeSupport == null) {
                changeSupport = new ChangeSupport(this);
                ui.addPropertyChangeListener(this);
            }
            changeSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            if (changeSupport != null) {
                changeSupport.removeChangeListener(l);
            }
        }

        @Override
        public boolean isValid() {
            return ui.isContentValid();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("contentValid".equals(evt.getPropertyName())) {
                if (changeSupport != null) {
                    changeSupport.fireChange();
                }
            }
        }
    }
}
