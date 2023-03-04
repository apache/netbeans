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

package org.netbeans.modules.gradle.javaee.web.newproject;

import java.awt.Component;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 * @author Laszlo Kishalmi
 */
public class ServerSelectionPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    ServerSelectionPanelVisual component;
    final J2eeModule.Type type;
    public static final String PROP_SERVER = "platformId"; //NOI18N
    public static final String PROP_PROFILE = "profileId"; //NOI18N

    public ServerSelectionPanel(J2eeModule.Type type) {
        this.type = type;
    }

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new ServerSelectionPanelVisual(type);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        component.read(settings);
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        component.write(settings);
    }

    @Override
    public boolean isValid() {
        return component.isValidSelection();
    }


    @Override
    public void addChangeListener(ChangeListener l) {
        component.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        component.removeChangeListener(l);
    }

}
