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

package org.netbeans.modules.dbschema.jdbcimpl.wizard;

import java.awt.Component;
import javax.swing.event.ChangeListener;
import org.openide.util.HelpCtx;

public class DBSchemaTargetPanel extends DBSchemaPanel {

    private org.openide.WizardDescriptor.Panel panel;

    public DBSchemaTargetPanel() {
    }

    public void setPanel(org.openide.WizardDescriptor.Panel panel) {
        this.panel = panel;
    }

    public DBSchemaTargetPanel getPanel() {
        return this;
    }

    @Override
    public Component getComponent() {
        return panel.getComponent();
    }

    @Override
    public boolean isValid() {
        boolean ret = panel.isValid();
        
        if (ret) {
            org.openide.loaders.TemplateWizard settings = new org.openide.loaders.TemplateWizard();
            String name = settings.getTargetName();
            
            if (name != null)
                if ((name.indexOf("\\") != -1) || (name.indexOf("/") != -1))
                    return false;
        }
        
        return ret;
    }

    @Override
    public void readSettings(Object settings) {
        panel.readSettings(settings);
    }

    @Override
    public void storeSettings(Object settings) {
        panel.storeSettings(settings);
    }

    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public synchronized void addChangeListener(ChangeListener listener) {
        panel.addChangeListener(listener);
    }

    @Override
    public synchronized void removeChangeListener(ChangeListener listener) {
        panel.removeChangeListener(listener);
    }
}
