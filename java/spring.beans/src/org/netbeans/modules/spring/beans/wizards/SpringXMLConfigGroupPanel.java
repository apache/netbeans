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

package org.netbeans.modules.spring.beans.wizards;

import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.spring.api.beans.ConfigFileGroup;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class SpringXMLConfigGroupPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    public static final String CONFIG_FILE_GROUPS = "configFileGroups"; // NOI18N

    private SpringXMLConfigGroupVisual component;
    private List<ConfigFileGroup> configFileGroups;

    public SpringXMLConfigGroupPanel(List<ConfigFileGroup> configFileGroups) {
        this.configFileGroups = configFileGroups;
    }

    public SpringXMLConfigGroupVisual getComponent() {
        if (component == null) {
            component = new SpringXMLConfigGroupVisual(configFileGroups);
        }
        return component;
    }

    public HelpCtx getHelp() {
        return null;
    }

    public void readSettings(WizardDescriptor settings) {
    }

    public void storeSettings(WizardDescriptor settings) {
        settings.putProperty(CONFIG_FILE_GROUPS, getComponent().getSelectedConfigFileGroups());
    }

    public boolean isValid() {
        return true;
    }

    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

}
