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
package org.netbeans.modules.gradle.configurations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.modules.gradle.execute.GradleExecAccessor;
import org.netbeans.modules.gradle.execute.ProjectConfigurationUpdater;
import org.netbeans.modules.gradle.execute.ProjectConfigurationSupport;
import org.openide.util.Lookup;

/**
 * Working mutabel copy of a configuration set, using in project's Customzier. Can be created for
 * a Lookup instance (i.e. the Lookup passed around by Project Customizer); will keep a reference in a global Map,
 * and allows to erase the reference on customizer close.
 * <p/>
 * This allows to share the {@link ConfigurationShapshot} between individual panels in the project, that can share
 * <b>active configuration</b> and <b>newly added</b> configurations.
 * @author sdedic
 */
public class ConfigurationSnapshot {
    private static final String USER_PREFIX = "user-"; // NOI18N
    
    /**
     * Updater that handles loading and saving of configurations.
     */
    private final ProjectConfigurationUpdater updater;
    
    private Set<String> fixedIds = Collections.emptySet();
    
    /**
     * List of edited configurations.
     */
    private List<GradleExecConfiguration>   configurations  = Collections.emptyList();
    
    /**
     * Modified configurations.
     */
    private Set<String>    changedConfigs = Collections.emptySet();
    
    /**
     * Shraed configuration IDs.
     */
    private Set<String>    sharedConfigs = Collections.emptySet();
    
    private boolean defaultIsOverriden;
    
    private boolean modified;
    
    /**
     * Fixed configurations that are also present in shared/nonshared = user-customized ones.
     */
    private Set<GradleExecConfiguration>    fixedOverrides = Collections.emptySet();
    
    private GradleExecConfiguration activeConfiguration;

    public ConfigurationSnapshot(ProjectConfigurationUpdater updater) {
        this.updater = updater;
        reload();
    }

    public GradleExecConfiguration getActiveConfiguration() {
        return activeConfiguration;
    }

    public void setActiveConfiguration(GradleExecConfiguration ac) {
        // maybe the original instance, so find a copy
        for (GradleExecConfiguration c : configurations) {
            if ((c.isDefault() && ac == null) ||
                c.equals(ac)) {
                activeConfiguration = c;
                return;
            }
        }
    }
    
    public boolean isOverriden(GradleExecConfiguration cfg) {
        return fixedOverrides.contains(cfg);
    }
    
    private void reload() {
        Collection<GradleExecConfiguration> shared = updater.getSharedConfigurations();
        Collection<GradleExecConfiguration> nonshared = updater.getPrivateConfigurations();
        
        changedConfigs = new HashSet<>();
        sharedConfigs = new HashSet<>();
        fixedIds = updater.getFixedConfigurations().stream().map(GradleExecConfiguration::getId).collect(Collectors.toSet());
        
        GradleExecConfiguration defConfig = null;
        
        configurations = new ArrayList<>();
        for (GradleExecConfiguration c : updater.getConfigurations()) {
            configurations.add(GradleExecAccessor.instance().copy(c));
            if (c.isDefault()) {
                defConfig = c;
            }
            if (shared.contains(c) || c.isDefault()) {
                sharedConfigs.add(c.getId());
            } 
        }
        fixedOverrides = new HashSet<>();
        fixedOverrides.addAll(shared);
        fixedOverrides.addAll(nonshared);
        fixedOverrides.retainAll(updater.getFixedConfigurations());
        
        defaultIsOverriden = shared.contains(defConfig) || nonshared.contains(defConfig);
        modified = false;
        setActiveConfiguration(activeConfiguration);
    }

    public boolean isModified() {
        return modified;
    }
    
    public List<GradleExecConfiguration> getConfigurations() {
        return configurations;
    }
    
    public GradleExecConfiguration createNew(String id) {
        int maxId = -1;
        // find the highest user-number
        for (GradleExecConfiguration c : configurations) {
            if (c.getId().equalsIgnoreCase(id)) {
                throw new IllegalArgumentException();
            }
            if (c.getId().startsWith(USER_PREFIX)) {
                try {
                    maxId = Math.max(maxId, Integer.parseInt(c.getId().substring(USER_PREFIX.length())));
                } catch (NumberFormatException ex) {
                    // ignore, nit a number
                }
            }
        }
        if (maxId < 1) {
            maxId = 1;
        } else {
            maxId++;
        }
        return GradleExecAccessor.instance().create(id != null ? id : USER_PREFIX + maxId, null, null, null);
    }
    
    public void add(GradleExecConfiguration c) {
        for (GradleExecConfiguration e : configurations) {
            if (e.getId().equals(c.getId())) {
                throw new IllegalArgumentException("ID exists");
            }
        }
        configurations.add(c);
        changedConfigs.add(c.getId());
        modified = true;
    }
    
    public boolean isFixed(GradleExecConfiguration c) {
        return fixedIds.contains(c.getId());
    }
    
    public void removeConfiguration(GradleExecConfiguration c) {
        if (configurations.remove(c)) {
            changedConfigs.remove(c.getId());
            sharedConfigs.remove(c.getId());
            modified = true;
        }
    }
    
    public void setShared(GradleExecConfiguration c, boolean shared) {
        if (isShared(c) == shared) {
            return;
        }
        if (shared) {
            sharedConfigs.add(c.getId());
        } else {
            sharedConfigs.remove(c.getId());
        }
        changedConfigs.add(c.getId());
        modified = true;
    }
    
    public boolean isShared(GradleExecConfiguration c) {
        return sharedConfigs.contains(c.getId());
    }
    
    public boolean updateConfiguration(GradleExecConfiguration c, String dispName, Map<String, String> properties, String args) {
        boolean ch = !(
                    Objects.equals(dispName, c.getName()) &&
                    Objects.equals(properties, c.getProjectProperties()) &&
                    Objects.equals(args, c.getCommandLineArgs())
                );
        if (ch) {
            GradleExecAccessor.instance().update(c, dispName, properties, args);
            changedConfigs.add(c.getId());
            modified = true;
        }
        return ch;
    }
    
    public static ConfigurationSnapshot createSnapshot(ProjectConfigurationUpdater updater) {
        ConfigurationSnapshot sn = new ConfigurationSnapshot(updater);
        return sn;
    }
    
    private static final Map<Lookup, ConfigurationSnapshot> projectSnapshots = new HashMap<>();
    
    public static ConfigurationSnapshot forProject(Lookup context, Project project, Consumer<Runnable> unregisterHook) {
        ConfigurationSnapshot snap;
        synchronized (projectSnapshots) {
            snap = projectSnapshots.get(context);
            if (snap != null) {
                return snap;
            }
            
            GradleExecConfiguration active = ProjectConfigurationSupport.getEffectiveConfiguration(project, context);
            ProjectConfigurationUpdater upd = project.getLookup().lookup(ProjectConfigurationUpdater.class);
            snap = createSnapshot(upd);
            snap.setActiveConfiguration(active);
            projectSnapshots.put(context, snap);
        }
        unregisterHook.accept(() -> removeSnapshot(context));
        return snap;
    }
    
    private static void removeSnapshot(Lookup context) {
        synchronized (projectSnapshots) {
            projectSnapshots.remove(context);
        }
    }
    
    public void save() throws IOException {
        if (!isModified()) {
            return;
        }
        List<GradleExecConfiguration> sharedConfigsToSave = new ArrayList<>();
        List<GradleExecConfiguration> privateConfigsToSave = new ArrayList<>();
        
        
        for (GradleExecConfiguration cfg : configurations) {
            if (fixedIds.contains(cfg.getId())) {
                // do not save fixed conf as private / shared unless really modified, or 
                // it was already customized.
                boolean shouldOverride = fixedOverrides.contains(cfg) || changedConfigs.contains(cfg.getId());
                if (!shouldOverride) {
                    continue;
                }
            }
            if (isShared(cfg)) {
                sharedConfigsToSave.add(cfg);
            } else {
                privateConfigsToSave.add(cfg);
            }
        }
        updater.setConfigurations(sharedConfigsToSave, privateConfigsToSave);
        // reset the metadata:
        reload();
    }
    
    public GradleExecConfiguration revert(GradleExecConfiguration cfg) {
        if (!isOverriden(cfg)) {
            return cfg;
        }
        int idx = configurations.indexOf(cfg);
        if (idx == -1) {
            return cfg;
        }

        GradleExecConfiguration found = null;
        for (GradleExecConfiguration orig : updater.getFixedConfigurations()) {
            if (orig.getId().equals(cfg.getId())) {
                found = orig;
                break;
            }
        }
        if (found == null) {
            return cfg;
        }
        changedConfigs.remove(cfg.getId());
        sharedConfigs.remove(cfg.getId());
        fixedOverrides.remove(cfg);
        GradleExecConfiguration copyOfOrig = GradleExecAccessor.instance().copy(found);
        configurations.set(idx, copyOfOrig);
        modified = true;
        return copyOfOrig;
    }
}
