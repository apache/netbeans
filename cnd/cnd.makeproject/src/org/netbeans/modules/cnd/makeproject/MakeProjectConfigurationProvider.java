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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collection;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.MakeCustomizerProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.spi.project.ProjectConfigurationProvider;

public class MakeProjectConfigurationProvider implements ProjectConfigurationProvider<Configuration>, PropertyChangeListener {

    /**
     * Property name of the set of configurations.
     * Use it when firing a change in the set of configurations.
     */
    static final String PROP_CONFIGURATIONS_BROKEN = "brokenConfigurations"; // NOI18N
    private final Project project;
    private final ConfigurationDescriptorProviderImpl projectDescriptorProvider;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public MakeProjectConfigurationProvider(Project project, ConfigurationDescriptorProviderImpl projectDescriptorProvider, PropertyChangeListener info) {
        this.project = project;
        this.projectDescriptorProvider = projectDescriptorProvider;
        this.pcs.addPropertyChangeListener(info);
        projectDescriptorProvider.addConfigurationDescriptorListener(this);
        projectDescriptorProvider.getConfigurationDescriptorImpl().getConfs().addPropertyChangeListener(this);
    }

    @Override
    public Collection<Configuration> getConfigurations() {
        return projectDescriptorProvider.getConfigurationDescriptorImpl().getConfs().getConfigurations();
    }

    @Override
    public Configuration getActiveConfiguration() {
        return projectDescriptorProvider.getConfigurationDescriptorImpl().getConfs().getActive();
    }

    @Override
    public void setActiveConfiguration(Configuration configuration) throws IllegalArgumentException, IOException {
        projectDescriptorProvider.getConfigurationDescriptorImpl().getConfs().setActive(configuration);
    }

    public void registerPropertyChangeListener(PropertyChangeListener lst) {
        pcs.addPropertyChangeListener(lst);
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener lst) {
        pcs.addPropertyChangeListener(lst);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener lst) {
        pcs.removePropertyChangeListener(lst);
    }

    @Override
    public boolean hasCustomizer() {
        if (projectDescriptorProvider.gotDescriptor() && projectDescriptorProvider.getConfigurationDescriptor() != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void customize() {
        MakeCustomizerProvider makeCustomizer = project.getLookup().lookup(MakeCustomizerProvider.class);
        makeCustomizer.showCustomizer("Build"); // NOI18N
    }

    @Override
    public boolean configurationsAffectAction(String command) {
        return false;
    /*
    return command.equals(ActionProvider.COMMAND_RUN) ||
    command.equals(ActionProvider.COMMAND_BUILD) ||
    command.equals(ActionProvider.COMMAND_CLEAN) ||
    command.equals(ActionProvider.COMMAND_DEBUG);
     */
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        assert pcs != null;
        pcs.firePropertyChange(evt);
    }
}
