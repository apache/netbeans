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
package org.netbeans.modules.cloud.common.spi.support.ui;

import java.awt.Component;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 */
public class CloudResourcesWizardPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    private CloudResourcesWizardComponent component;
    private List<ServerResourceDescriptor> resources;
    
    public static final String PROP_SERVER_RESOURCES = "server-resources";
    
    private final String[] names;
    
    private final int step;
	
    public CloudResourcesWizardPanel(String[] names, int step) {
        this.names = names.clone();
        this.step = step;
    }
    
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new CloudResourcesWizardComponent(resources);
            component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, names);
            component.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, step);
        }
        return component;
    }
    
    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        resources = (List<ServerResourceDescriptor>)settings.getProperty(PROP_SERVER_RESOURCES);
        assert resources != null;
        settings.getProperty(WizardDescriptor.PROP_CONTENT_DATA);
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

}
