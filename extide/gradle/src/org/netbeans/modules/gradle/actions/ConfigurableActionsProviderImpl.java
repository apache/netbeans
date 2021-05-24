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
package org.netbeans.modules.gradle.actions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.execute.ActionMapping;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.modules.gradle.execute.ConfigurableActionProvider;
import org.netbeans.modules.gradle.execute.GradleExecAccessor;
import org.netbeans.modules.gradle.spi.actions.GradleActionsProvider;
import org.netbeans.modules.gradle.spi.actions.ProjectActionMappingProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.xml.sax.SAXException;

/**
 *
 * @author sdedic
 */
@ProjectServiceProvider(service = { ConfigurableActionProvider.class, ProjectActionMappingProvider.class }, projectType = "org-netbeans-modules-gradle")
public class ConfigurableActionsProviderImpl implements ProjectActionMappingProvider, ConfigurableActionProvider {
    private static final Logger LOG = Logger.getLogger(ConfigurableActionsProviderImpl.class.getName());
    
    private static final RequestProcessor ACTIONS_REFRESH_RP = new RequestProcessor(ConfigurableActionsProviderImpl.class); // NOI18N
    
    private final Project project;
    
    private List<ChangeListener> listeners = new ArrayList<>();
    
    /**
     * Changeable list of providers. Collected from the project (first) and the default
     * Lookup (last).
     */
    // @GuardedBy(this)
    private Lookup.Result<GradleActionsProvider> providers;
    
    /**
     * Cached actions.
     */
    // @GuardedBy(this)
    private Map<String, ActionData> cache = null;
    
    // @GuardedBy(this)
    private Map<String, GradleExecConfiguration> configurations = new HashMap<>();
    
    // @GuardedBy(this)
    private Set<String> actionIDs = new HashSet<>();

    public ConfigurableActionsProviderImpl(Project project) {
        this.project = project;
    }

    @Override
    public ActionMapping findMapping(String action) {
        ActionData ad = getActionData(GradleExecConfiguration.DEFAULT);
        return ad == null ? null : ad.mappings.get(action);
    }

    @Override
    public Set<String> customizedActions() {
        return Collections.emptySet();
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        synchronized (this) {
            listeners.add(l);
        }
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        synchronized (this) {
            listeners.remove(l);
        }
    }
    
    @Override
    public List<GradleExecConfiguration> findConfigurations() {
        Set<String> confIds;
        synchronized (this) {
            if (cache != null) {
                return new ArrayList<>(configurations.values());
            }
            confIds = cache == null ? null : cache.keySet();
        }
        Map<String, ActionData> map = updateCache(serial.incrementAndGet(), confIds);
        List<GradleExecConfiguration> lst = new ArrayList<>(map.size());
        for (ActionData ad : map.values()) {
            lst.add(ad.cfg);
        }
        return lst;
    }

    @Override
    public ProjectActionMappingProvider findActionProvider(String configurationId) {
        if (configurationId == null || GradleExecConfiguration.DEFAULT.equals(configurationId)) {
            return this;
        } else {
            return new ProjectActionMappingProvider() {
                @Override
                public ActionMapping findMapping(String action) {
                    ActionData ad = getActionData(configurationId);
                    ActionMapping m = null;
                    
                    if (ad != null) {
                        m = ad.mappings.get(action);
                    }
                    if (m == null) {
                        m = getActionData(GradleExecConfiguration.DEFAULT).mappings.get(action);
                    }
                    return m;
                }

                @Override
                public Set<String> customizedActions() {
                    return Collections.emptySet();
                }
            };
        }
    }
    
    private Collection<? extends GradleActionsProvider> providers() {
        if (providers != null) {
            return providers.allInstances();
        }
        Lookup combined = new ProxyLookup(Lookup.getDefault(), project.getLookup());
        Lookup.Result<GradleActionsProvider> result = combined.lookupResult(GradleActionsProvider.class);
        Collection<? extends GradleActionsProvider> lst;
        
        synchronized (this) {
            if (providers != null) {
                return providers.allInstances();
            } else {
                result.addLookupListener(new LookupListener() {
                    @Override
                    public void resultChanged(LookupEvent ev) {
                        synchronized (ConfigurableActionsProviderImpl.this) {
                            if (providers != result) {
                                return;
                            }
                        }
                        refresh();
                    }
                });
                lst = result.allInstances();
                providers = result;
            }
        }
        return lst;
    }
    
    // @GuardedBy(this)
    private RequestProcessor.Task pendingTask;
    private AtomicInteger serial = new AtomicInteger(0);
    
    private void refresh() {
        Set<String> confIds;
        synchronized (this) {
            confIds = cache == null ? null : cache.keySet();
            cache = null;
            if (pendingTask != null) {
                pendingTask.cancel();
            }
            pendingTask = ACTIONS_REFRESH_RP.post(() -> updateCache(serial.incrementAndGet(), confIds));
        }
    }
    
    private ActionData getActionData(String id) {
        Set<String> confIds;
        synchronized (this) {
            if (cache != null) {
                return cache.get(id);
            }
            confIds = cache == null ? null : cache.keySet();
        }
        return updateCache(serial.incrementAndGet(), confIds).get(id);
    }
    
    private Map<String, ActionData> updateCache(int serial, Set<String> oldConfigs) {
        Refresher r = new Refresher();
        r.run();
        
        List<ChangeListener> ll;
        synchronized (this) {
            if (serial != this.serial.get()) {
                return r.actionMap;
            }
            this.cache = r.actionMap;
            Map<String, GradleExecConfiguration> confs = new HashMap<>();
            for (String id : cache.keySet()) {
                confs.put(id, cache.get(id).cfg);
            }
            this.configurations = confs;
            this.actionIDs = r.actionIDs;
            if (oldConfigs == null || r.actionMap.keySet().equals(oldConfigs)) {
                return cache;
            }
            if (listeners.isEmpty()) {
                return cache;
            }
            ll = new ArrayList<>(listeners);
        }
        ChangeEvent e = new ChangeEvent(this);
        ll.forEach(l -> l.stateChanged(e));
        return r.actionMap;
    }
    
    private static String[] maybeEmpty(String args) {
        if (args == null) {
            return new String[0];
        } else {
            return BaseUtilities.parseParameters(args);
        }
    }
    
    class Refresher implements Runnable {
        Map<String, GradleExecConfiguration> config = new HashMap<>();
        Set<String> actionIDs = new HashSet<>();
        Map<String, ActionData> actionMap = new HashMap<>();
        
        public void run() {
            Collection x = Lookups.forPath("Projects/org-netbeans-modules-gradle/Plugins/io.micronaut.application/Lookup").lookupAll(Object.class);
            Collection<? extends GradleActionsProvider> lst = providers();
            for (GradleActionsProvider p : lst) {
                actionIDs.addAll(p.getSupportedActions());
                
                Set<ActionMapping> defaultMappings;
                
                Map<GradleExecConfiguration, Set<ActionMapping>> mapp = new HashMap<>();
                try (InputStream istm = p.defaultActionMapConfig()) {
                    defaultMappings = ActionMappingScanner.loadMappings(istm, mapp);
                } catch (IOException | ParserConfigurationException | SAXException ex) {
                    continue;
                }
                
                
                ActionData ad = actionMap.get(GradleExecConfiguration.DEFAULT);
                if (ad == null) {
                    ad = new ActionData(GradleExecAccessor.createDefault());
                    actionMap.put(GradleExecConfiguration.DEFAULT, ad);
                }
                for (ActionMapping m : defaultMappings) {
                    ad.mappings.putIfAbsent(m.getName(), m);
                }
                for (GradleExecConfiguration c : mapp.keySet()) {
                    GradleExecConfiguration existing = config.get(c.getId());
                    if (existing != null) {
                        existing = mergeConfigurations(existing, c);
                    } else {
                        existing = c;
                    }
                    config.put(existing.getId(), existing);
                    
                    ad = actionMap.get(existing.getId());
                    if (ad == null) {
                        ad = new ActionData(existing);
                        actionMap.put(existing.getId(), ad);
                    }
                    for (ActionMapping m : mapp.get(c)) {
                        ad.mappings.putIfAbsent(m.getName(), m);
                    }
                }
            }
        }
        
        private GradleExecConfiguration mergeConfigurations(GradleExecConfiguration one, GradleExecConfiguration two) {
            String dispName = one.getName();
            Map<String, String> props = new HashMap<>(one.getProjectProperties());
            props.putAll(two.getProjectProperties());
            String args = String.join(" ", one.getCommandLineArgs(), two.getCommandLineArgs());
            
            String[] params = maybeEmpty(one.getCommandLineArgs());
            String[] params2 = maybeEmpty(two.getCommandLineArgs());
            String[] all = new String[params.length + params2.length];
            System.arraycopy(params, 0, all, 0, params.length);
            System.arraycopy(params2, 0, all, params.length, params2.length);
            
            return GradleExecAccessor.instance().update(one, dispName, props, args);
        }
    }
    
    private static final ActionData NONE = new ActionData(null);
    
    private static class ActionData {
        private final GradleExecConfiguration cfg;
        private final Map<String, ActionMapping>  mappings = new HashMap<>();

        public ActionData(GradleExecConfiguration cfg) {
            this.cfg = cfg;
        }
    }
    
}
