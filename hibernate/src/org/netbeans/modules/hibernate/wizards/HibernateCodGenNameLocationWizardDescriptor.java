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
package org.netbeans.modules.hibernate.wizards;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 * @author Dongmei Cao
 */
public class HibernateCodGenNameLocationWizardDescriptor implements WizardDescriptor.Panel<WizardDescriptor> {

    private JPanel component;
    private String title;

    public HibernateCodGenNameLocationWizardDescriptor(Project project, String title) {
        this.title = title;
    }

    public JPanel getComponent() {
        if (component == null) {
            component = new JPanel();

        }
        return component;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(HibernateCodGenNameLocationWizardDescriptor.class);
    }

    
    public void readSettings(WizardDescriptor settings) {
        settings.putProperty("NewFileWizard_Title", title);
    }

    public boolean isValid() {
        return true;
    }

    public void storeSettings(WizardDescriptor settings) {
    }

    public void stateChanged(ChangeEvent event) {
    }

    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }
}
