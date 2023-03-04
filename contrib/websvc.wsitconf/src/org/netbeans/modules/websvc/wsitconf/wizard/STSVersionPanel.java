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

package org.netbeans.modules.websvc.wsitconf.wizard;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;

/**
 * FinishableProxyWizardPanel.java - used decorator pattern to enable to finish 
 * the original wizard panel, that is not finishable
 * 
 *
 * @author mkuchtiak
 */
public class STSVersionPanel implements WizardDescriptor.Panel {
    
    private WizardDescriptor wizard;
    private Project project;

    private STSVersionPanelUI ui = null;
    
    /** Creates a new instance of ProxyWizardPanel */
    public STSVersionPanel(WizardDescriptor wiz) {
        wizard = wiz;
        this.project = Templates.getProject(wizard);
        ui = new STSVersionPanelUI(project);
    }

    public void addChangeListener(javax.swing.event.ChangeListener l) {
    }

    public void removeChangeListener(javax.swing.event.ChangeListener l) {
    }

    public void storeSettings(Object settings) {
        wizard.putProperty(WizardProperties.VERSION, ui.getVersion());
    }

    public void readSettings(Object settings) {}

    public boolean isValid() {
        return true;
    }

    public java.awt.Component getComponent() {
        ui.setVisible(true);
        return ui;
    }

    public org.openide.util.HelpCtx getHelp() {
        return wizard.getHelpCtx();
    }
    
}
