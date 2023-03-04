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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

class ClusterizeWizardPanel2
implements WizardDescriptor.Panel<Clusterize>, PropertyChangeListener {
    private Component component;
    Clusterize settings;
    private boolean valid;
    private ChangeListener listener;

    ClusterizeWizardPanel2() {
    }

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new ClusterizeVisualPanel2(this);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.apisupport.project.ui.customizer.ClusterizeWizardPanel2");
    }

    void setValid(boolean valid) {
        this.valid = valid;
        ChangeListener l = this.listener;
        if (l != null) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
        assert this.listener == null;
        this.listener = l;
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        if (l == this.listener) {
            this.listener = null;
        }
    }

    @Override
    public void readSettings(Clusterize settings) {
        this.settings = settings;
        settings.modules.addPropertyChangeListener(this);
        propertyChange(null);
    }

    @Override
    public void storeSettings(Clusterize settings) {
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (settings == null) {
            setValid(false);
            return;
        }
        ClusterizeInfo ci = settings.modules;
        setValid(ci.getSelectedFilesCount() > 0);
        if (!isValid()) {
            settings.wizardDescriptor.getNotificationLineSupport().setErrorMessage(
                NbBundle.getMessage(ClusterizeWizardPanel2.class, "MSG_ClusterizeNothingSelected")
            );
        } else {
            settings.wizardDescriptor.getNotificationLineSupport().clearMessages();
        }
    }
}

