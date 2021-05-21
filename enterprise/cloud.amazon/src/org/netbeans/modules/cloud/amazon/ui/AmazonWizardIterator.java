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
package org.netbeans.modules.cloud.amazon.ui;

import org.netbeans.modules.cloud.common.spi.support.ui.CloudResourcesWizardPanel;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cloud.amazon.AmazonInstance;
import org.netbeans.modules.cloud.amazon.AmazonInstanceManager;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.ChangeSupport;

/**
 *
 */
public class AmazonWizardIterator implements WizardDescriptor.InstantiatingIterator {

    private ChangeSupport listeners;
    private WizardDescriptor wizard;
    private AmazonWizardPanel panel;
    private CloudResourcesWizardPanel panel2;
    boolean first = true;

    public AmazonWizardIterator() {
        listeners = new ChangeSupport(this);
    }
    
    public static final String PROP_DISPLAY_NAME = "ServInstWizard_displayName"; // NOI18N

    @Override
    public Set instantiate() throws IOException {
        String keyId = (String)wizard.getProperty(AmazonWizardPanel.KEY_ID);
        assert keyId != null;
        String key = (String)wizard.getProperty(AmazonWizardPanel.KEY);
        assert key != null;
        String name = (String)wizard.getProperty(PROP_DISPLAY_NAME);
        assert name != null;
        String regionUrl = (String)wizard.getProperty(AmazonWizardPanel.REGION);
        String regionCode = (String)wizard.getProperty(AmazonWizardPanel.CODE);
        
        AmazonInstanceManager.getDefault().add(new AmazonInstance(name, keyId, key, regionUrl, regionCode));
        
        return Collections.emptySet();
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        panel = null;
    }

    @Override
    public Panel current() {
        if (first) {
            if (panel == null) {
                panel = new AmazonWizardPanel();
            }
            return panel;
        } else {
            if (panel2 == null) {
                panel2 = new CloudResourcesWizardPanel(AmazonWizardPanel.getPanelContentData(), 1);
            }
            return panel2;
        }
    }

    @Override
    public String name() {
        return "Amazon";
    }

    @Override
    public boolean hasNext() {
        return first;
    }

    @Override
    public boolean hasPrevious() {
        return !first;
    }

    @Override
    public void nextPanel() {
        first = false;
    }

    @Override
    public void previousPanel() {
        first = true;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        listeners.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listeners.removeChangeListener(l);
    }
    
}
