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
package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.Component;
import java.awt.EventQueue;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

class ClusterizeWizardPanel1 implements
WizardDescriptor.AsynchronousValidatingPanel<Clusterize>, Runnable {
    private ClusterizeVisualPanel1 component;
    Clusterize settings;

    ClusterizeWizardPanel1() {
    }

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new ClusterizeVisualPanel1(this);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.apisupport.project.ui.customizer.ClusterizeVisualPanel1");
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
    }

    @Override
    public void readSettings(Clusterize settings) {
        this.settings = settings;
    }

    @Override
    public void storeSettings(Clusterize settings) {
    }

    @Override
    public void prepareValidation() {
        component.showProgress();
    }

    @Override
    public void validate() throws WizardValidationException {
        settings.scanForJars();
        EventQueue.invokeLater(this);
    }
    @Override
    public void run() {
        component.hideProgress();
    }
}

