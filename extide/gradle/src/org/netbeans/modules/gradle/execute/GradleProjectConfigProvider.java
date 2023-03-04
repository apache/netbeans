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
package org.netbeans.modules.gradle.execute;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gradle.api.execute.ActionMapping;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.modules.gradle.configurations.ConfigurationsPanelProvider;
import org.netbeans.modules.gradle.spi.actions.ProjectActionMappingProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
@ProjectServiceProvider(service = {
    ProjectConfigurationProvider.class,
    ProjectConfigurationUpdater.class
    }, projectType = "org-netbeans-modules-gradle"
)
public class GradleProjectConfigProvider implements 
        ProjectConfigurationProvider<GradleExecConfiguration>, ProjectConfigurationUpdater, ChangeListener {
    private final PropertyChangeSupport supp = new PropertyChangeSupport(this);
    private final Project project;

    /**
     * List of available configurations. The {@link GradleExecConfiguration#DEFAULT} is always present
     * and is the first one in the list.
     */
    // GuardedBy(this)
    private List<GradleExecConfiguration> configurations = null;
    
    // @GuardedBy(this)
    private Map<String, GradleExecConfiguration> sharedConfigs = new HashMap<>();

    // @GuardedBy(this)
    private Map<String, GradleExecConfiguration> privateConfigs = new HashMap<>();
    /**
     * ID of the active configuration. It is persisted in project's auxiliary props 
     * on change automatically.
     */
    // @GuardedBy(this)
    private String activeConfigId;
    
    // @GuardedBy(this)
    private AuxiliaryConfiguration aux;
    
    private ConfigurableActionProvider configProvider;

    public GradleProjectConfigProvider(Project project) {
        this.project = project;
    }
    
    // for testing only
    public void setConfigurableProvider(ConfigurableActionProvider provider) {
        configProvider = provider;
        provider.addChangeListener(this);
    }
 
    @Override
    public Collection<GradleExecConfiguration> getConfigurations() {
        synchronized (this) {
            if (configurations != null) {
                return Collections.unmodifiableCollection(configurations);
            }
        }
        return refreshConfigurations();
    }
    
    private synchronized AuxiliaryConfiguration aux() {
        if (aux == null) {
            aux = ProjectUtils.getAuxiliaryConfiguration(project);
        }
        return aux;
    }
    
    @Override
    public GradleExecConfiguration getActiveConfiguration() {
        GradleExecConfiguration cfg = ProjectConfigurationSupport.getExplicitConfiguration(project, Lookup.EMPTY);
        if (cfg != null) {
            return cfg;
        }
        String activeId;
        
        Collection<GradleExecConfiguration> confs = getConfigurations();
        synchronized (this) {
            if (activeConfigId == null) {
                activeConfigId = ConfigPersistenceUtils.readActiveConfiguration(aux());
            }
            activeId = activeConfigId;
        }
        
        GradleExecConfiguration def = null;
                
        for (GradleExecConfiguration c : confs) {
            String cid = c.getId();
            if (GradleExecConfiguration.DEFAULT.equals(cid)) {
                def = c;
            }
            if (activeId.equals(c.getId())) {
                return c;
            }
        }
        return def;
    }
    
    private GradleExecConfiguration getDefaultConfiguration() {
        Collection<GradleExecConfiguration> confs = getConfigurations();
        return confs.iterator().next();
    }

    @Override
    public void setActiveConfiguration(GradleExecConfiguration configuration) throws IllegalArgumentException, IOException {
        GradleExecConfiguration d = getDefaultConfiguration();
        GradleExecConfiguration a = getActiveConfiguration();
        if (configuration == null) {
            configuration = d;
        } else {
            List<GradleExecConfiguration> confs = new ArrayList<>(getConfigurations());
            int idx = confs.indexOf(configuration);
            if (idx == -1) {
                throw new IllegalArgumentException("Unknown config: " + configuration);
            }
            configuration = confs.get(idx);
        }
        String id;
        boolean empty;
        synchronized (this) {
            if (a == configuration) {
                // no change.
                return;
            }
            if (d == configuration) {
                id = GradleExecConfiguration.DEFAULT;
            } else {
                id = configuration.getId();
            }
            this.activeConfigId = id;
            empty = sharedConfigs.isEmpty() && privateConfigs.isEmpty();
        }
        ConfigPersistenceUtils.writeActiveConfiguration(aux(), id, empty);
        supp.firePropertyChange(PROP_CONFIGURATION_ACTIVE, a, configuration);
    }

    @Override
    public boolean hasCustomizer() {
        return project.getLookup().lookup(CustomizerProvider2.class) != null;
    }

    @Override
    public void customize() {
        CustomizerProvider2 provider = project.getLookup().lookup(CustomizerProvider2.class);
        provider.showCustomizer(ConfigurationsPanelProvider.PANEL_CONFIGURATIONS, null);
    }

    @Override
    public boolean configurationsAffectAction(String command) {
        switch (command) {
            case ActionProvider.COMMAND_DELETE:
            case ActionProvider.COMMAND_COPY:
            case ActionProvider.COMMAND_MOVE:
            case ActionProvider.COMMAND_RENAME:
                
                return false;
        }
        return true;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener lst) {
        supp.addPropertyChangeListener(lst);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener lst) {
        supp.removePropertyChangeListener(lst);
    }
    
    /**
     * Determines if the configuration is shared. The default configuration is
     * always shared, as its actions are persisted in gradle.properties.
     * @param c configuration
     * @return true, if shared.
     */
    public boolean isSharedConfiguration(GradleExecConfiguration c) {
        if (GradleExecConfiguration.DEFAULT.equals(c.getId())) {
            return true;
        }
        synchronized (this) {
            return sharedConfigs.get(c.getId()) != null;
        }
    }
    
    /**
     * Updates the list of configurations. It may add/remove/move shared &lt-> unshared. Will fire appropriate
     * events if the list of configurations changes. Obtains {@link ProjectManager#mutex}.
     * @param shared list of all shared configurations to save.
     * @param nonShared  list of all unshared (private) configurations to save.
     */
    public void setConfigurations(List<GradleExecConfiguration> shared, List<GradleExecConfiguration> nonShared) {
        GradleExecConfiguration a = getActiveConfiguration();
        String aid = a == null ? null : a.getId();
        ProjectManager.mutex(false, project). writeAccess(() -> {
            
            ConfigPersistenceUtils.writeConfigurations(shared, aux, null, true);
            ConfigPersistenceUtils.writeConfigurations(nonShared, aux, aid, false);
            
            Map<String, GradleExecConfiguration> mapShared = new LinkedHashMap<>();
            Map<String, GradleExecConfiguration> mapPrivate = new LinkedHashMap<>();
            shared.forEach(c -> mapShared.put(c.getId(), c));
            nonShared.forEach(c -> mapPrivate.put(c.getId(), c));
            Map<String, GradleExecConfiguration> newCfg = buildConfigurations(mapShared, mapPrivate);
            updateConfigurations(-1, mapShared, mapPrivate, newCfg);
        });
    }
    
    /**
     * Guards configuration refresh
     */
    private AtomicInteger serial = new AtomicInteger(0);
    
    private List<GradleExecConfiguration> refreshConfigurations() {
        int stamp = serial.incrementAndGet();
        Map<String, GradleExecConfiguration> newShared = new LinkedHashMap<>();
        Map<String, GradleExecConfiguration> newPrivate = new LinkedHashMap<>();
        ConfigPersistenceUtils.readConfigurations(newShared, aux(), true);
        ConfigPersistenceUtils.readConfigurations(newPrivate,aux(), false);
        return ProjectManager.mutex(false, project). readAccess(() -> {
            Map<String, GradleExecConfiguration> newCfg = buildConfigurations(newShared, newPrivate);
            return updateConfigurations(stamp, newShared, newPrivate, newCfg);
        });
    }
    
    private List<GradleExecConfiguration> updateConfigurations(int stamp, 
            Map<String, GradleExecConfiguration> newShared,
            Map<String, GradleExecConfiguration> newPrivate,
            Map<String, GradleExecConfiguration> newCfg) {
        List<GradleExecConfiguration> lst;
        boolean changed;
        
        final List<GradleExecConfiguration> old;
        synchronized (this) {
            old = this.configurations;
            lst = new ArrayList<>();
            lst.add(newCfg.remove(GradleExecConfiguration.DEFAULT));
            lst.addAll(newCfg.values());
            if (stamp < 0 || stamp == serial.get()) {
                changed = configurations != null && !configurations.equals(lst);
                this.configurations = lst;
                this.sharedConfigs = newShared;
                this.privateConfigs = newPrivate;
            } else {
                changed = false;
            }
        }
        if (changed) {
            ProjectManager.mutex(false, project).postWriteRequest(() -> {
                supp.firePropertyChange(PROP_CONFIGURATIONS, old, lst);
            });
        }
        return lst;
    }

    @Override
    public Collection<GradleExecConfiguration> getSharedConfigurations() {
        return sharedConfigs.values();
    }

    @Override
    public Collection<GradleExecConfiguration> getPrivateConfigurations() {
        return privateConfigs.values();
    }
    
    /**
     * Reads a list of configurations from the providers and from the disk
     * files.
     * @return 
     */
    private Map<String, GradleExecConfiguration> buildConfigurations(Map<String, GradleExecConfiguration> sharedConf, Map<String, GradleExecConfiguration> privateConf) {
        Map<String, GradleExecConfiguration> result = new LinkedHashMap<>();
        result.putAll(sharedConf);
        result.putAll(privateConf);
        for (GradleExecConfiguration c : getFixedConfigurations()) {
            result.putIfAbsent(c.getId(), c);
        }
        return result;
    }
    
    public Collection<GradleExecConfiguration> getFixedConfigurations() {
        Collection<GradleExecConfiguration> result = new LinkedHashSet<>();
        if (configProvider == null) {
            ConfigurableActionProvider p = project.getLookup().lookup(ConfigurableActionProvider.class);
            if (p == null) {
                // configurae null provider.
                p = new ConfigurableActionProvider() {
                    @Override
                    public ActionMapping findDefaultMapping(String configurationId, String action) {
                        return null;
                    }

                    @Override
                    public void addChangeListener(ChangeListener l) {
                    }

                    @Override
                    public void removeChangeListener(ChangeListener l) {
                    }

                    @Override
                    public List<GradleExecConfiguration> findConfigurations() {
                        return Collections.emptyList();
                    }

                    @Override
                    public ProjectActionMappingProvider findActionProvider(String configurationId) {
                        return null;
                    }
                };
            }
            synchronized (this) {
                if (configProvider == null) {
                    setConfigurableProvider(p);
                }
            }
        }
        boolean defPresent = false;
        for (GradleExecConfiguration c : configProvider.findConfigurations()) {
            // rely on that equals on id
            defPresent |= GradleExecConfiguration.DEFAULT.equals(c.getId());
            result.add(c);
        }
        if (!defPresent) {
            result.add(GradleExecAccessor.createDefault());
        }
        return result;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        refreshConfigurations();
    }
}
