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
package org.netbeans.modules.cloud.oracle;

import org.netbeans.modules.cloud.oracle.items.OCIItem;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

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

    private static final String TENANCY = "TENANCY";
    private Panel panel;

    public OracleCloudWizardIterator() {
    }

    @Override
    public Set instantiate() throws IOException {
        return Collections.emptySet();
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        wizard.putProperty(TENANCY, 
                CompletableFuture.supplyAsync(() -> OCIManager.getDefault().getTenancy()));
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
    }

    @Override
    public WizardDescriptor.Panel current() {
        if (panel == null) {
            panel = new Panel();
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

    private class Panel implements WizardDescriptor.AsynchronousValidatingPanel<WizardDescriptor> {

        private JTextPane text;
        private boolean valid = false;
        private final JPanel panel;
        private final ChangeSupport changeSupport;

        public Panel() {
            text = new JTextPane();
            text.setContentType("text/html"); //NOI18N
            text.setText(Bundle.MSG_CheckingSetup());
            panel = new JPanel(new BorderLayout());
            text.setEditable(false);
            text.addHyperlinkListener(new HyperlinkListener() {
                @Override
                public void hyperlinkUpdate(HyperlinkEvent hle) {
                    if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
                        Desktop desktop = Desktop.getDesktop();
                        try {
                            desktop.browse(hle.getURL().toURI());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
            panel.add(text, BorderLayout.CENTER);
            panel.setName(Bundle.LBL_OC());
            panel.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, new String[]{Bundle.LBL_OC()});
            panel.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, 0);
            text.setText(Bundle.MSG_CheckingSetup());
            changeSupport = new ChangeSupport(this);
        }

        @Override
        public Component getComponent() {
            return panel;
        }

        @Override
        public HelpCtx getHelp() {
            return null;
        }

        @Override
        public void readSettings(WizardDescriptor settings) {
            Object o = settings.getProperty(TENANCY);
            if (o == null) {
                return;
            }
            CompletionStage<Optional<OCIItem>> cs = (CompletionStage<Optional<OCIItem>>) o;
            cs.thenAccept(t -> {
                if (t.isPresent()) {
                    if (!valid) {
                        valid = true;
                        changeSupport.fireChange();
                    }
                    text.setText(Bundle.MSG_TenancyFound(t.get().getName()));
                } else {
                    text.setText(Bundle.MSG_OCI_Setup(Bundle.URL_OCI_Setup()));
                }
            });
        }

        @Override
        public void storeSettings(WizardDescriptor settings) {
        }

        @Override
        public boolean isValid() {
            return valid;
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
        public void prepareValidation() {
        }

        @Override
        public void validate() throws WizardValidationException {
        }
    }

}
