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

package org.netbeans.modules.gradle.options;

import org.netbeans.modules.gradle.spi.GradleSettings;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.modules.gradle.api.execute.GradleDistributionManager;
import org.netbeans.modules.gradle.api.execute.GradleDistributionManager.GradleDistribution;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author Laszlo Kishalmi
 */
@OptionsPanelController.SubRegistration(
        location = "Java",
        id = "Gradle",
        displayName = "Gradle",
        keywords = "gradle",
        keywordsCategory = "Java/Gradle"
)
public class GradleOptionsController extends OptionsPanelController {

    private SettingsPanel panel;

    @Override
    public void update() {
        getPanel().setValues();
    }

    @Override
    public void applyChanges() {
        getPanel().applyValues();
    }

    @Override
    public void cancel() {
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isChanged() {
        return getPanel().hasChanges();
    }

    @Override
    public JComponent getComponent(Lookup lkp) {
        return getPanel();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pl) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pl) {
    }

    private SettingsPanel getPanel() {
        if (panel == null) {
            panel = new SettingsPanel();
        }
        return panel;
    }
}
