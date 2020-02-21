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
package org.netbeans.modules.cnd.makeproject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class ConfigurationDescriptorProviderImpl extends ConfigurationDescriptorProvider{
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    // for unit tests only
    public ConfigurationDescriptorProviderImpl(FileObject projectDirectory) {
        this(null, projectDirectory);
    }

    public ConfigurationDescriptorProviderImpl(Project project, FileObject projectDirectory) {
        super(project, projectDirectory);
    }

    @Override
    protected MakeConfigurationDescriptor getConfigurationDescriptorImpl() {
        return super.getConfigurationDescriptorImpl();
    }
    
    protected void addConfigurationDescriptorListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
    
    protected void removeConfigurationDescriptorListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
    @Override
    protected void fireConfigurationDescriptorLoaded() {
        MakeConfigurationDescriptor makeConfigurationDescriptor = getConfigurationDescriptorImpl();
        
        if (makeConfigurationDescriptor.getState() != ConfigurationDescriptor.State.BROKEN) {  // IZ 122372 // IZ 182321
            pcs.firePropertyChange(ConfigurationDescriptorProvider.PROP_CONFIGURATIONS_LOADED, null, makeConfigurationDescriptor);
            pcs.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATIONS, null, makeConfigurationDescriptor.getConfs().getConfigurations());
            pcs.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE, null, makeConfigurationDescriptor.getConfs().getActive());
        } else {
            // notify problem
            pcs.firePropertyChange(MakeProjectConfigurationProvider.PROP_CONFIGURATIONS_BROKEN, null, ConfigurationDescriptor.State.BROKEN);
        }
    }

    @Override
    protected void opening(Interrupter interrupter) {
        super.opening(interrupter);
    }
}
