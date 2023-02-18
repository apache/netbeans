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
import org.netbeans.modules.gradle.api.GradleBaseProject;
import static org.netbeans.modules.gradle.api.NbGradleProject.PROP_PROJECT_INFO;
import org.netbeans.modules.gradle.api.execute.ActionMapping;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.modules.gradle.execute.ConfigurableActionProvider;
import org.netbeans.modules.gradle.execute.GradleExecAccessor;
import org.netbeans.modules.gradle.execute.ProjectConfigurationSupport;
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
 * Caching here is designed as follows: Once data is loaded, it is valid until a refresh. Customized actions are loaded lazily, on query for a specific configuration, or
 * during the refresh. Once the customizations are loaded, the service listens on the file - if the content changes, customizations are invalidated and loaded during next query.
 * Refresh is caused by a change to the set of configurations and to the set of {@link GradleActionProvider}s - which may occur e.g. when the project is reloaded and 
 * new set of plugins is detected.
 * 
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
     * Listener for individual action files and the project directory.
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
    
    // @GuardedBy(this)
    private Set<String> plugins;
    
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
    
    private String effectiveConfig() {
        return ProjectConfigurationSupport.getEffectiveConfiguration(project, Lookup.EMPTY).getId();
    }
    
    ActionMapping findMapping(String action, String cfgId) {
        ActionData ad = getActionData(cfgId);
        ActionMapping m = null;
        if (ad != null) {
            m = ad.getAction(action);
        }
        if (m == null && !GradleExecConfiguration.DEFAULT.equals(cfgId)) {
            m = getActionData(GradleExecConfiguration.DEFAULT).getAction(action);
        }
        return m;
    }
    
    Set<String> customizedActions(String cfgId) {
        ActionData ad = getActionData(cfgId);
        if (ad == null) {
            return Collections.emptySet();
        }
        return new HashSet<>(ad.customizedMappings.keySet());
    }

    @Override
    public ActionMapping findMapping(String action) {
        return findMapping(action, effectiveConfig());
    }

    @Override
    public Set<String> customizedActions() {
        return customizedActions(effectiveConfig());
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
        return new ProjectActionMappingProvider() {
            @Override
            public ActionMapping findMapping(String action) {
                return ConfigurableActionsProviderImpl.this.findMapping(action, configurationId);
            }

            @Override
            public Set<String> customizedActions() {
                return ConfigurableActionsProviderImpl.this.customizedActions(configurationId);
            }
        };
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
        if (ad != null) {
            ActionMapping result = ad.getDefaultAction(action);
            if (result != null) {
                return result;
            }
        }
        ActionData def = snap.get(GradleExecConfiguration.DEFAULT);
        if (def != ad && def != null) {
            return def.getDefaultAction(action);
        }
        return null;
    }
    
    private synchronized Set<String> getPlugins() {
        synchronized(this) {
            if (plugins == null) {
                GradleBaseProject gbp = GradleBaseProject.get(project);
                plugins = gbp.getPlugins();
            }
            return plugins;
        }
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
            plugins = null;
            confIds = cache == null ? null : cache.keySet();
            cache = null;
            if (pendingTask != null) {
                pendingTask.cancel();
            }
            pendingTask = ACTIONS_REFRESH_RP.post(() -> {
                synchronized (this) {
                    pendingTask = null;
                }
                updateCache(serial.incrementAndGet(), confIds);
            });
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
                    if (ad.customizedMappings != null) {
                        return ad;
                    }
                } else {
                    foundConfig = configs.stream().filter(c -> c.getId().equals(id)).findAny().orElse(null);
                    if (foundConfig == null) {
                        return null;
                    }
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
                        if (ad.customizedMappings != null) {
                            return ad;
                        } else {
                            ad.customizedMappings = map;
                        }
                    } else {
                        ad = new ActionData(false, foundConfig);
                        cache.put(id, ad);
                    }
                    ad.customizedMappings = map;
                    FileObject f = ActionPersistenceUtils.findActionsFile(projectDirectory, id);
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.log(Level.FINER, "Adding listener for: {0} - {1}", new Object[] { id, f });
                    }
                    ad.attach(projectDirectory, f, fcl);
                    return ad;
                } else {
                    return cache.get(id);
                }
            }
        } else {
            return updateCache(serial.incrementAndGet(), null).get(id);
        }
    }
    
    /**
     * Refreshes using Refresher, then atomically updates the cache and updates file listeners.
     * @return new configuration Map.
     */
    private Map<String, ActionData> updateCache(int serial, Set<String> oldConfigs) {
        LOG.log(Level.FINER, "Updating action cache for {0}, serial {1}, oldConfigs {2}", new Object[] { project, serial, oldConfigs });
        Refresher r = new Refresher();
        r.run();
        
        List<ChangeListener> ll;
        Map<String, ActionData> oldCache;
        Map<String, ActionData> newCache;
        
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

            newCache.forEach((k, v) -> confs.put(k, v.cfg));

            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "Project {0} got configurations: {1}", new Object[] { project, confs.keySet() });
            }
            this.configurations = confs;
            this.actionIDs = r.actionIDs;
            
            if (oldCache != null) {
                for (ActionData ad : oldCache.values()) {
                    ad.detach();
                }
            }
            for (ActionData ad : newCache.values()) {
                ad.attach(projectDirectory, null, fcl);
            }
            if (newCache.keySet().equals(oldConfigs)) {
                // no change, no configurations added/removed.
                LOG.log(Level.FINER, "No configuraiton change - stop.");
                return cache;
            }
            ll = new ArrayList<>(listeners);
            
            if (LOG.isLoggable(Level.FINER)) {
                Set<String> toRemove;
                Set<String> toAdd;
                toRemove = new HashSet<>(oldCache == null ? Collections.emptySet() : oldCache.keySet());
                toRemove.removeAll(newCache.keySet());

                toAdd = new HashSet<>(newCache.keySet());
                if (oldCache != null) {
                    toAdd.remove(oldCache.keySet());
                }
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "Removed config: {0}, added config: {1}", new Object[] { toRemove, toAdd });
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
                LOG.log(Level.FINER, "Loading defaults from provider {0}", p);
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
                    ad.customizedMappings = loadCustomActions(ad.cfg.getId());
                    LOG.log(Level.FINER, "Loaded customizations for <default>: {0}", ad.customizedMappings.keySet());
                }
                Set<String> newEntries = new HashSet<>();
                for (ActionMapping m : defaultMappings) {
                    ad.mappings.add(m);
                    newEntries.add(m.getName());
                }
                LOG.log(Level.FINER, "Loaded actions: {0}", newEntries);
                
                for (Map.Entry<GradleExecConfiguration, Set<ActionMapping>> it : mapp.entrySet()) {
                    GradleExecConfiguration c = it.getKey();

                    LOG.log(Level.FINER, "Loading config {0}", c.getId());
                    GradleExecConfiguration existing = config.get(c.getId());
                    if (existing != null) {
                        LOG.log(Level.FINER, "Merging {0} with {1}", new Object[] { existing, c });
                        existing = mergeConfigurations(existing, c);
                    } else {
                        existing = c;
                    }
                    config.put(existing.getId(), existing);
                    
                    ad = actionMap.get(existing.getId());
                    if (ad == null) {
                        ad = new ActionData(true, existing);
                        actionMap.put(existing.getId(), ad);
                        ad.customizedMappings = loadCustomActions(ad.cfg.getId());
                        LOG.log(Level.FINER, "Loaded customizations for config {1}: {0}", new Object[] { ad.customizedMappings.keySet(), existing.getId() });
                    }
                    newEntries = new HashSet<>();
                    for (ActionMapping m : it.getValue()) {
                        ad.mappings.add(m);
                        newEntries.add(m.getName());
                    }
                    LOG.log(Level.FINER, "Loaded config actions: {0}", newEntries);
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
    
    private class ActionData {
        private final GradleExecConfiguration cfg;
        private final List<ActionMapping>  mappings = new ArrayList<>();
        private final boolean fromProvider;
        private FileObject monitoringFile;
        
        // @GuardedBy(ConfigurableActionProvider.this)
        private FileChangeListener listener;
        
        // @GuardedBy(ConfigurableActionProvider.this)
        private Map<String, ActionMapping>  customizedMappings = null;
        
        public ActionData(boolean provided, GradleExecConfiguration cfg) {
            this.fromProvider = provided;
            this.cfg = cfg;
        }
        
        ActionMapping getAction(String id) {
            ActionMapping result = customizedMappings.get(id);
            if (result != null) {
                return result;
            } else {
                return getDefaultAction(id);
            }
        }
        
        ActionMapping getDefaultAction(String id) {
            ActionMapping result = null;
            for (ActionMapping mapping : mappings) {
                if (mapping.getName().equals(id) && mapping.isApplicable(getPlugins())) {
                    if (result == null || result.compareTo(mapping) < 0) {
                        result = mapping;
                    }
                }
            }
            return result;
        }
        
        // @GuardedBy(ConfigurableActionProvider.this)
        void clearCustomMappings() {
            customizedMappings = Collections.emptyMap();
            detach();
        }
        
        void attach(FileObject projectDir, FileObject f, FileChangeListener fcl) {
            if (listener != null) {
                return;
            }
            if (f == null) {
                f = ActionPersistenceUtils.findActionsFile(projectDir, cfg.getId());
                if (f == null) {
                    return;
                }
            }
            LOG.log(Level.FINER, "Started to monitor {0}", f);
            monitoringFile = f;
            listener = WeakListeners.create(FileChangeListener.class, fcl, f);
            f.addFileChangeListener(listener);
        }
        
        void detach() {
            if (monitoringFile == null) {
                listener = null;
                return;
            }
            if (listener != null) {
                LOG.log(Level.FINER, "Stopped monitoring {0}", monitoringFile);
                monitoringFile.removeFileChangeListener(listener);
            }
            monitoringFile = null;
            listener = null;
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
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Action file changed: {0}, orig: {1}, deleted: {2}", new Object[] { af.getNameExt(), originalName, delete });
        }
        String name = af.getName();
        if (af.getParent() != project.getProjectDirectory()) {
            return false;
        }
        
        String selectedId = findConfigurationId(af.getNameExt());
        ActionData dataHolder = null;
        synchronized (this) {
            if (cache != null && selectedId != null) {
                dataHolder = cache.get(selectedId);
                LOG.log(Level.FINER, "selectedId: {0}, holder: {1}", new Object[] { selectedId, dataHolder });
                if (dataHolder == null) {
                    // no known configuration, go away; configurations ought to
                    // be loaded first.
                    return false;
                }
                // just clear, will be loaded on first query.
                LOG.log(Level.FINER, "Invalidating holder");
                if (delete) {
                    dataHolder.clearCustomMappings();
                } else {
                    dataHolder.customizedMappings = null;
                    dataHolder.attach(projectDirectory, af, fcl);
                }
            }
        }
        return true;
    }
    
    private String findConfigurationId(String fileNameExt) {
        if (GradleFiles.GRADLE_PROPERTIES_NAME.equals(fileNameExt)) {
            return GradleExecConfiguration.DEFAULT;
        } else if (fileNameExt.startsWith(ActionPersistenceUtils.NBACTIONS_CONFIG_PREFIX) && fileNameExt.endsWith(ActionPersistenceUtils.NBACTIONS_XML_EXT)) {
            return fileNameExt.substring(ActionPersistenceUtils.NBACTIONS_CONFIG_PREFIX.length(), fileNameExt.length() - ActionPersistenceUtils.NBACTIONS_XML_EXT.length());
        }
        return null;
    }
}
