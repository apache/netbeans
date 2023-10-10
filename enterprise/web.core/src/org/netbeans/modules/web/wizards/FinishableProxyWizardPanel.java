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

package org.netbeans.modules.web.wizards;

import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.HelpCtx;

/**
 * FinishableProxyWizardPanel.java - used decorator pattern to enable to finish
 * the original wizard panel, that is not finishable
 *
 *
 * @author mkuchtiak
 */
public class FinishableProxyWizardPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {

    private final WizardDescriptor.Panel original;
    private final HelpCtx helpCtx;
    private boolean isOriginallyValid = true;

    public FinishableProxyWizardPanel(Panel original, HelpCtx helpCtx) {
        this.original = original;
        this.helpCtx = helpCtx;
    }
    
    public FinishableProxyWizardPanel(Panel original, HelpCtx helpCtx, 
            boolean isValid ) 
    {
        this.original = original;
        this.helpCtx = helpCtx;
        isOriginallyValid = isValid;
    }
    
    public FinishableProxyWizardPanel(WizardDescriptor.Panel original) {
        this(original, null);
    }

    @Override
    public void addChangeListener(javax.swing.event.ChangeListener l) {
        original.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(javax.swing.event.ChangeListener l) {
        original.removeChangeListener(l);
    }

    @Override
    public void storeSettings(Object settings) {
        original.storeSettings(settings);
    }

    @Override
    public void readSettings(Object settings) {
        original.readSettings(settings);
    }

    @Override
    public boolean isValid() {
        if ( !isOriginallyValid ){
            return false;
        }
        return original.isValid();
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

    @Override
    public java.awt.Component getComponent() {
        return original.getComponent();
    }

    @Override
    public org.openide.util.HelpCtx getHelp() {
        if (helpCtx != null) {
            return helpCtx;
        }
        return original.getHelp();
    }
    
}
