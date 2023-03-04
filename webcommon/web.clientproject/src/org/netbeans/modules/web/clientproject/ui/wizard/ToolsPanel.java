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
package org.netbeans.modules.web.clientproject.ui.wizard;

import java.awt.Component;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectUtils;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class ToolsPanel implements WizardDescriptor.FinishablePanel<WizardDescriptor> {

    private final CreateProjectUtils.Tools tools;
    // @GuardedBy("EDT") - not possible, wizard support calls store() method in EDT as well as in a background thread
    private volatile Tools panel;


    public ToolsPanel(CreateProjectUtils.Tools tools) {
        assert tools != null;
        this.tools = tools;
    }

    public CreateProjectUtils.Tools getTools() {
        return tools;
    }

    @Override
    public Component getComponent() {
        return getPanel();
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.web.clientproject.ui.wizard.ToolsPanel"); // NOI18N
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        getPanel().setNpmEnabled(tools.isNpm());
        getPanel().setBowerEnabled(tools.isBower());
        getPanel().setGruntEnabled(tools.isGrunt());
        getPanel().setGulpEnabled(tools.isGulp());
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        tools.setNpm(getPanel().isNpmEnabled());
        tools.setBower(getPanel().isBowerEnabled());
        tools.setGrunt(getPanel().isGruntEnabled());
        tools.setGulp(getPanel().isGulpEnabled());
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        getPanel().addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        getPanel().removeChangeListener(listener);
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

    private Tools getPanel() {
        // assert EventQueue.isDispatchThread(); - not possible, see comment above (@GuardedBy())
        if (panel == null) {
            panel = new Tools();
        }
        return panel;
    }

}
