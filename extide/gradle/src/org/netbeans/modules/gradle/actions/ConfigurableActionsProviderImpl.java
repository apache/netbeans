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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.Project;
import static org.netbeans.modules.gradle.api.NbGradleProject.PROP_PROJECT_INFO;
import org.netbeans.modules.gradle.api.execute.ActionMapping;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.modules.gradle.execute.ConfigurableActionProvider;
import org.netbeans.modules.gradle.execute.GradleExecAccessor;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.netbeans.modules.gradle.spi.actions.GradleActionsProvider;
import org.netbeans.modules.gradle.spi.actions.ProjectActionMappingProvider;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ProxyLookup;
import org.xml.sax.SAXException;

/**
 * Extended implementation of action provider. This service is quite tangled with {@link ProjectConfigurationProvider}: individual participating
 * {@link GradleActionProvider}s may provide builtin {@link GradleExecConfiguration}s - this information is served from this impl to GradleProjectConfigProvider.
 * And action definition files are loaded for defined configurations, especially the user-defined, which is managed by the {@link ProjectConfigurationProvider} implementation.
 * So these two impls listen for each other firing their own change events + guard against loops.
 * <p/>
 * @author sdedic
 */
@ProjectServiceProvider(service = { ConfigurableActionProvider.class, ProjectActionMappingProvider.class }, projectType = "org-netbeans-modules-gradle")
public class ConfigurableActionsProviderImpl implements ProjectActionMappingProvider, ConfigurableActionProvider {
    private static final Logger LOG = Logger.getLogger(ConfigurableActionsProviderImpl.class.getName());
    
    private static final RequestProcessor ACTIONS_REFRESH_RP = new RequestProcessor(ConfigurableActionsProviderImpl.class); // NOI18N
    
    private final Project project;
    private final FileObject projectDirectory;

    /**
     * Listener for project reloads. Will reload the actions if the project information change.
     */
    final PropertyChangeListener pcl = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                reload();
            }
        }
    };

    /**
     * Multiplexing listener for individual action files and the project directory.
     */
    final FileChangeListener fcl = new FileChangeAdapter() {
        @Override
        public void fileRenamed(FileRenameEvent fe) {
            actionFileChanged(fe.getFile(), fe.getName(), false);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            actionFileChanged(fe.getFile(), null, true);
        }

        @Override
        public void fileChanged(FileEvent fe) {
            actionFileChanged(fe.getFile(), null, false);
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            actionFileChanged(fe.getFile(), null, false);
        }
    };
    
    /**
     * Configuration provider. Initialized on the first read by {@link #conf()}.
     */
    private ProjectConfigurationProvider<GradleExecConfiguration> confProvider;

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
    private final List<ChangeListener> listeners = new ArrayList<>();
    
    // @GuardedBy(this)
    private Map<String, GradleExecConfiguration> configurations = new HashMap<>();
    
    // @GuardedBy(this)
    private Set<String> actionIDs = new HashSet<>();
    
    public ConfigurableActionsProviderImpl(Project project, Lookup l) {
        this.project = project;
        this.projectDirectory = project.getProjectDirectory();
        
        FileChangeListener wl =  WeakListeners.create(FileChangeListener.class, fcl, this.projectDirectory);
        projectDirectory.addFileChangeListener(wl);
        
        LOG.log(Level.FINER, "Initializing ConfigurableAP for {0}", project);
    }
    
    void setConfigurationProvider(ProjectConfigurationProvider p) {
        this.confProvider = p;
        p.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                configurationsChanged();
            }
        });
    }
    
    private ProjectConfigurationProvider<GradleExecConfiguration> conf() {
        if (confProvider != null) {
            return confProvider;
        }
        synchronized (this) {
            confProvider = project.getLookup().lookup(ProjectConfigurationProvider.class);
            setConfigurationProvider(confProvider);
            return confProvider;
        }
    }

    @Override
    public ActionMapping findMapping(String action) {
        ActionData ad = getActionData(GradleExecConfiguration.DEFAULT);
        return ad == null ? null : ad.mappings.get(action);
    }

    @Override
    public Set<String> customizedActions() {
        ActionData ad = getActionData(GradleExecConfiguration.DEFAULT);
        return ad == null ? Collections.emptySet() : ad.customizedMappings.keySet();
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
        LOG.log(Level.FINER, "Reloading configuration; old IDs: {0}", confIds);
        Map<String, ActionData> map = updateCache(serial.incrementAndGet(), confIds);
        List<GradleExecConfiguration> lst = new ArrayList<>(map.size());
        for (ActionData ad : map.values()) {
            // do not reflect back user-custom configurations.
            if (ad.fromProvider) {
                lst.add(ad.cfg);
            }
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
                        m = ad.getAction(action);
                    }
                    if (m == null) {
                        m = getActionData(GradleExecConfiguration.DEFAULT).getAction(action);
                    }
                    return m;
                }

                @Override
                public Set<String> customizedActions() {
                    ActionData ad = getActionData(configurationId);
                    return ad == null ? Collections.emptySet() : ad.customizedMappings.keySet();
                }
            };
        }
    }

    @Override
    public ActionMapping findDefaultMapping(String configurationId, String action) {
        Map<String, ActionData> snap = null;
        
        synchronized (this) {
            snap = cache;
        }
        if (snap == null) {
            LOG.log(Level.FINER, "Reloading configuration");
            snap = updateCache(serial.incrementAndGet(), null);
        }
        ActionData ad = snap.get(configurationId);
        if (ad == null) {
            ad = snap.get(GradleExecConfiguration.DEFAULT);
        }
        return ad == null ? null : ad.getAction(action);
    }
    
    
    
    private Collection<? extends GradleActionsProvider> providers() {
        if (providers != null) {
            return providers.allInstances();
        }
        LOG.log(Level.FINER, "Initializing providers lookup for: {0}", project);
        Lookup combined = new ProxyLookup(Lookup.getDefault(), project.getLookup());
        Lookup.Result<GradleActionsProvider> result = combined.lookupResult(GradleActionsProvider.class);
        Collection<? extends GradleActionsProvider> lst;
        
        synchronized (this) {
            if (providers != null) {
                return providers.allInstances();
            } else {
                LOG.log(Level.FINER, "Attaching provider listener");
                result.addLookupListener(new LookupListener() {
                    @Override
                    public void resultChanged(LookupEvent ev) {
                        synchronized (ConfigurableActionsProviderImpl.this) {
                            if (providers != result) {
                                return;
                            }
                        }
                        LOG.log(Level.FINER, "Action providers change for {0}, refreshing", project);
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
    
    /**
     * Stamp guarding against paralel obsolete overwrites.
     */
    private final AtomicInteger serial = new AtomicInteger(0);
    
    private void configurationsChanged() {
        Collection<? extends GradleExecConfiguration> confs = conf().getConfigurations();
        Set<String> ids = new HashSet<>();
        confs.forEach(c -> ids.add(c.getId()));
        synchronized (this) {
            if (cache != null && cache.keySet().equals(ids)) {
                LOG.log(Level.FINER, "Configuration set did not change - stop.");
                return;
            }
            
        }
        refresh();
        LOG.log(Level.FINER, "Different configuraitons set, reloading");
    }
    
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
        Collection<? extends GradleExecConfiguration> configs = conf().getConfigurations();
        Map<String, ActionData> snap;
        GradleExecConfiguration foundConfig = null;
        synchronized (this) {
            snap = cache;
            if (snap != null) {
                ActionData ad = cache.get(id);
                if (ad != null) {
                    return ad;
                }
                foundConfig = configs.stream().filter(c -> c.getId().equals(id)).findAny().orElse(null);
                if (foundConfig == null) {
                    return null;
                }
                // may need a refresh ... some custom config is not in yet:
            }
        }
        if (snap != null) {
            // try to load customized actions now
            Map<String, ActionMapping> map = loadCustomActions(id);
            synchronized (this) {
                if (snap == cache) {
                    ActionData ad = cache.get(id);
                    if (ad != null) {
                        return ad;
                    }
                    ad = new ActionData(false, foundConfig);
                    ad.customizedMappings = map;
                    FileObject f = ActionPersistenceUtils.findActionsFile(projectDirectory, id);
                    if (f != null) {
                        ad.listener = WeakListeners.create(FileChangeListener.class, fcl, f);
                        f.addFileChangeListener(ad.listener);
                    }
                    cache.put(id, ad);
                    return ad;
                } else {
                    return cache.get(id);
                }
            }
        } else {
            return updateCache(serial.incrementAndGet(), null).get(id);
        }
    }
    
    private Map<String, ActionData> updateCache(int serial, Set<String> oldConfigs) {
        LOG.log(Level.FINER, "Updating action cache for {0}, serial {1}, oldConfigs {2}", new Object[] { project, serial, oldConfigs });
        Refresher r = new Refresher();
        r.run();
        
        List<ChangeListener> ll;
        Map<String, ActionData> oldCache;
        Map<String, ActionData> newCache;
        Set<String> toRemove;
        Set<String> toAdd;
        
        synchronized (this) {
            if (serial != this.serial.get()) {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "Reloaded serial {0}-{1} does not match current {2}, stop.", new Object[] { project, serial, this.serial.get() });
                }
                return r.actionMap;
            }
            oldCache = this.cache;
            this.cache = newCache = r.actionMap;
            Map<String, GradleExecConfiguration> confs = new HashMap<>();
            for (String id : newCache.keySet()) {
                confs.put(id, newCache.get(id).cfg);
            }
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "Project {0} got configurations: {1}", new Object[] { project, confs.keySet() });
            }
            this.configurations = confs;
            this.actionIDs = r.actionIDs;
            if (newCache.keySet().equals(oldConfigs)) {
                // no change, no configurations added/removed.
                LOG.log(Level.FINER, "No configuraiton change - stop.");
                return cache;
            }
            ll = new ArrayList<>(listeners);
            
            toRemove = new HashSet<>(oldCache == null ? Collections.emptySet() : oldCache.keySet());
            toRemove.removeAll(newCache.keySet());
            
            toAdd = new HashSet<>(newCache.keySet());
            if (oldCache != null) {
                toAdd.remove(oldCache.keySet());
            }
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "Removed config: {0}, added config: {1}", new Object[] { toRemove, toAdd });
            }
            // update the set of listeners
            for (String s : toAdd) {
                FileObject f = ActionPersistenceUtils.findActionsFile(projectDirectory, s);
                if (f != null) {
                    FileChangeListener w = WeakListeners.create(FileChangeListener.class, fcl, f);
                    r.actionMap.get(s).listener = w;
                    f.addFileChangeListener(w);
                }
            }
            for (String s : toRemove) {
                FileObject f = ActionPersistenceUtils.findActionsFile(projectDirectory, s);
                ActionData d = oldCache.get(s);
                if (d != null && d.listener != null) {
                    if (f != null) {
                        f.removeFileChangeListener(d.listener);
                    } else {
                        d.listener = null;
                    }
                }
            }

            if (oldConfigs == null) {
                return cache;
            }
            if (listeners.isEmpty()) {
                return cache;
            }
        }
        LOG.log(Level.FINER, "Firing config/action change events");
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
    
    private Map<String, ActionMapping> loadCustomizedDefaultConfig() {
        FileObject defaultConfigFile = project.getProjectDirectory().getFileObject(GradleFiles.GRADLE_PROPERTIES_NAME);
        if (defaultConfigFile == null) {
            return Collections.emptyMap();
        }
        Map<String, ActionMapping> mapping = new HashMap<>();
        // update just the action mapping
        try (InputStream is = defaultConfigFile.getInputStream()) {
            Properties props = new Properties();
            props.load(is);
            Set<ActionMapping> actions =  ActionMappingPropertyReader.loadMappings(props);
            for (ActionMapping am : actions) {
                mapping.put(am.getName(), am);
            }
        } catch (IOException ex) {
            // log
        }
        return mapping;
    }

    private Map<String, ActionMapping> loadCustomActions(String id) {
        if (GradleExecConfiguration.DEFAULT.equals(id)) {
            return loadCustomizedDefaultConfig();
        }
        FileObject configFile = ActionPersistenceUtils.findActionsFile(projectDirectory, id);
        if (configFile == null || !configFile.isValid() || !configFile.isData()) {
            return Collections.emptyMap();
        }
        Map<String, ActionMapping> mapping = new HashMap<>();
        // update just the action mapping
        try (InputStream is = configFile.getInputStream()) {
            Map<String, ActionMapping> map = new HashMap<>();
            Set<ActionMapping> actions = ActionMappingScanner.loadMappings(is);
            for (ActionMapping am : actions) {
                mapping.put(am.getName(), am);
            }
        } catch (IOException | SAXException | ParserConfigurationException ex) {
            // log
        }
        return mapping;
    }

    class Refresher implements Runnable {
        Map<String, GradleExecConfiguration> config = new HashMap<>();
        Set<String> actionIDs = new HashSet<>();
        Map<String, ActionData> actionMap = new HashMap<>();
        ActionData currentData;
        
        public void run() {
            Collection<? extends GradleActionsProvider> lst = providers();
            for (GradleActionsProvider p : lst) {
                actionIDs.addAll(p.getSupportedActions());
                
                Set<ActionMapping> defaultMappings;
                
                Map<GradleExecConfiguration, Set<ActionMapping>> mapp = new HashMap<>();
                try (InputStream istm = p.defaultActionMapConfig()) {
                    if (istm == null) {
                        continue;
                    }
                    defaultMappings = ActionMappingScanner.loadMappings(istm, mapp);
                } catch (IOException | ParserConfigurationException | SAXException ex) {
                    continue;
                }
                
                
                ActionData ad = actionMap.get(GradleExecConfiguration.DEFAULT);
                currentData = ad;
                if (ad == null) {
                    ad = new ActionData(true, GradleExecAccessor.createDefault());
                    actionMap.put(GradleExecConfiguration.DEFAULT, ad);
                }
                
                for (ActionMapping m : defaultMappings) {
                    ad.mappings.putIfAbsent(m.getName(), m);
                }
                ad.customizedMappings = loadCustomActions(ad.cfg.getId());
                
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
                        ad = new ActionData(true, existing);
                        actionMap.put(existing.getId(), ad);
                    }
                    for (ActionMapping m : mapp.get(c)) {
                        ad.mappings.putIfAbsent(m.getName(), m);
                    }
                    ad.customizedMappings = loadCustomActions(ad.cfg.getId());
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
    
    private static class ActionData {
        private final GradleExecConfiguration cfg;
        private final Map<String, ActionMapping>  mappings = new HashMap<>();
        private final boolean fromProvider;
        
        // @GuardedBy(ConfigurableActionProvider.this)
        private FileChangeListener listener;
        
        // @GuardedBy(ConfigurableActionProvider.this)
        private Map<String, ActionMapping>  customizedMappings = new HashMap<>();

        public ActionData(boolean provided, GradleExecConfiguration cfg) {
            this.fromProvider = provided;
            this.cfg = cfg;
        }
        
        ActionMapping getAction(String id) {
            ActionMapping am = customizedMappings.get(id);
            return am != null ? am : mappings.get(id);
        }
    }
    
    private void reload() {
        refresh();
    }
    
    /**
     * Informs that action file has been changed, created or renamed. On rename, originalName
     * should be non-null.
     * @param af
     * @param originalName 
     */
    private void actionFileChanged(FileObject af, String originalName, boolean delete) {
        actionFileChanged0(af, originalName, delete);
    }
    
    private boolean actionFileChanged0(FileObject af, String originalName, boolean delete) {
        String name = af.getName();
        if (af.getParent() != project.getProjectDirectory()) {
            return false;
        }
        
        String selectedId;
        ActionData dataHolder = null;
        boolean reloadCache = false;
        synchronized (this) {
            if (GradleFiles.GRADLE_PROPERTIES_NAME.equals(af.getNameExt())) {
                selectedId = GradleExecConfiguration.DEFAULT;
            } else if (!name.startsWith(ActionPersistenceUtils.NBACTIONS_CONFIG_PREFIX)) {
                selectedId = name.substring(ActionPersistenceUtils.NBACTIONS_CONFIG_PREFIX.length());
            } else {
                return false;
            }
            if (cache != null) {
                dataHolder = cache.get(selectedId);
                if (dataHolder == null) {
                    // no known configuration, go away; configurations ought to
                    // be loaded first.
                    return false;
                }
                
                // the simplest thing is delete: just nix the custom actions
                if (delete) {
                    dataHolder.customizedMappings = Collections.emptyMap();
                    return true;
                }
            } else {
                reloadCache = true;
            }
        }
        
        if (reloadCache) {
            updateCache(serial.incrementAndGet(), null);
            return false;
        }
        Map<String, ActionMapping> map = loadCustomActions(selectedId);
        synchronized (this) {
            if (cache == null) {
                return false;
            }
            ActionData check = cache.get(selectedId);
            if (check != dataHolder) {
                return false;
            }
            if (originalName != null) {
                // delete from the original name:
                String renamedId = null;
                if (GradleFiles.GRADLE_PROPERTIES_NAME.equals(originalName)) {
                    renamedId = GradleExecConfiguration.DEFAULT;
                } else if (originalName.startsWith(ActionPersistenceUtils.NBACTIONS_CONFIG_PREFIX) && originalName.endsWith(ActionPersistenceUtils.NBACTIONS_XML_EXT)) {
                    renamedId = originalName.substring(ActionPersistenceUtils.NBACTIONS_CONFIG_PREFIX.length(), originalName.length() - ActionPersistenceUtils.NBACTIONS_XML_EXT.length());
                }
                if (renamedId != null) {
                    ActionData toRemove = cache.get(renamedId);
                    if (toRemove != null) {
                        toRemove.customizedMappings = Collections.emptyMap();
                    }
                }
            }
            check.customizedMappings = map;
        }
        return true;
    }
}
