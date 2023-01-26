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

package org.netbeans.modules.maven.configurations;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import org.codehaus.plexus.util.StringUtils;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.ProjectProfileHandler;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import static org.netbeans.modules.maven.configurations.ConfigurationPersistenceUtils.*;
import org.netbeans.modules.maven.customizer.CustomizerProviderImpl;
import org.netbeans.modules.maven.execute.ActionNameProvider;
import org.netbeans.modules.maven.execute.DefaultActionGoalProvider;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionProfile;
import org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider;
import org.netbeans.modules.maven.spi.actions.MavenActionsProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
/**
 * WARNING: this class shall in no way use project.getLookup() as it's called
 * in the critical loop (getOriginalMavenproject
 * @author mkleint
 */
public class M2ConfigProvider implements ProjectConfigurationProvider<M2Configuration> {

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final NbMavenProjectImpl project;
    private final M2Configuration DEFAULT;
    
    // next five guarded by this
    private SortedSet<M2Configuration> provided = null;
    private SortedSet<M2Configuration> profiles = null;
    private SortedSet<M2Configuration> shared = null;
    private SortedSet<M2Configuration> nonshared = null;
    private M2Configuration active;
    private String initialActive;
    
    /**
     * Allows a temporary configuration override for a specific execution.
     */
    private final ThreadLocal<M2Configuration> activeOverride = new ThreadLocal<>();
    private final AtomicBoolean initialActiveLoaded = new AtomicBoolean(false);
    private final AuxiliaryConfiguration aux;
    private final ProjectProfileHandler profileHandler;
    private final PropertyChangeListener propertyChange;


    private static final RequestProcessor RP = new RequestProcessor(M2ConfigProvider.class.getName(),10);
    
    public M2ConfigProvider(NbMavenProjectImpl proj, AuxiliaryConfiguration aux, ProjectProfileHandler prof) {
        project = proj;
        this.aux = aux;
        profileHandler = prof;
        DEFAULT = M2Configuration.createDefault(project.getProjectDirectory());           
        active = DEFAULT;
        propertyChange = new PropertyChangeListener() {
            public @Override void propertyChange(PropertyChangeEvent evt) {
                if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                    flush();
                }
            }
        };
    }
    
    private void flush() {
        synchronized (M2ConfigProvider.this) {
            provided = null;
            profiles = null;
            shared = null;
            nonshared = null;
            initialActive = active != null ? active.getId() : null; //#241337
        }
        RP.post(new Runnable() {
            public @Override void run() {
                checkActiveAgainstAll(getConfigurations(), false);
                firePropertyChange();
            }

        });
    }

    public M2Configuration setLocalConfiguration(M2Configuration cfg) {
        M2Configuration old = activeOverride.get();
        activeOverride.set(cfg);
        return old;
    }
    
    private String getInitialActive() {
        if (initialActiveLoaded.compareAndSet(false, true)) {
            initialActive = readActiveConfigurationName(aux); 
        }
        return initialActive;
    }
    
    private ThreadLocal<Boolean> inConfigInit = new ThreadLocal<>();
    
    private void checkActiveAgainstAll(Collection<M2Configuration> confs, boolean async) {
        Boolean b = inConfigInit.get();
        assert b != null || !Thread.holdsLock(this);
        boolean found = false;
        String id;
        synchronized (this) {
            id = internalActive().getId();
        }
        for (M2Configuration conf : confs) {
            if (conf.getId().equals(id)) {
                found = true;
                break;
            }
        }
        if (!found) {
            Runnable dothis = new Runnable() {
                    public @Override void run() {
                        M2Configuration _active;
                        synchronized (M2ConfigProvider.this) {
                            _active = internalActive();
                        }
                        try {
                            doSetActiveConfiguration(DEFAULT, _active);
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                };
            if (async) {
                RP.post(dothis);
            } else {
                dothis.run();
            }
        }
    }
    
    private synchronized Collection<M2Configuration> getConfigurations(boolean skipProfiles) {
        if (profiles == null && !skipProfiles) {
            profiles = createProfilesList();
        }
        skipProfiles |= !Boolean.parseBoolean(NbBundle.getMessage(M2ConfigProvider.class, "ProjectConfigurationProvider.ExportProfiles"));
        if (shared == null) {
            //read from auxconf
            shared = readConfigurations(aux, project.getProjectDirectory(), true);
        }
        if (nonshared == null) {
            //read from auxconf
            nonshared = readConfigurations(aux, project.getProjectDirectory(), false);
        }
        if (provided == null) {
            provided = createProvidedList();
        }
        Collection<M2Configuration> toRet = new TreeSet<M2Configuration>();
        toRet.add(DEFAULT);
        toRet.addAll(shared);
        toRet.addAll(nonshared);
        toRet.addAll(provided);
        if (!skipProfiles) {
            //prevent duplicates in the list
            Iterator<M2Configuration> it = profiles.iterator();
            while (it.hasNext()) {
                M2Configuration c = it.next();
                if (!toRet.contains(c)) {
                    toRet.add(c);
                } else {
                    it.remove();
                }
            }
        }
        return toRet;
        
    }
    
    public @Override synchronized Collection<M2Configuration> getConfigurations() {
        return getConfigurations(false);
    }

    public M2Configuration getDefaultConfig() {
        return DEFAULT;
    }
    
    public synchronized Collection<M2Configuration> getProfileConfigurations() {
        getConfigurations();
        return profiles;
    }
    
    public synchronized Collection<M2Configuration> getSharedConfigurations() {
        getConfigurations();
        return shared;
    }
    
    public synchronized Collection<M2Configuration> getNonSharedConfigurations() {
        getConfigurations();
        return nonshared;
    }
    
    public synchronized Collection<M2Configuration> getProvidedConfigurations() {
        getConfigurations();
        return provided;
    }
    
    public @Override boolean hasCustomizer() {
        return true;
    }

    public @Override void customize() {
        CustomizerProviderImpl prv = project.getLookup().lookup(CustomizerProviderImpl.class);
        prv.showCustomizer(ModelHandle2.PANEL_CONFIGURATION);
    }

    public @Override boolean configurationsAffectAction(String action) {
        return !ActionProvider.COMMAND_DELETE.equals(action) && !ActionProvider.COMMAND_COPY.equals(action) && !ActionProvider.COMMAND_MOVE.equals(action);
    }
    
    private M2Configuration internalActive() {
        M2Configuration c = this.activeOverride.get();
        return c != null ? c : active;
    }

    public @Override M2Configuration getActiveConfiguration() {
        M2Configuration _active;
        Collection<M2Configuration> confs;
        Boolean b = inConfigInit.get();
        _active  = activeOverride.get();
        if (_active != null) {
            // explicit temporary override, skip all the 'is still there' & fire change logic.
            return _active;
        }
        synchronized (this) {
            _active = active;
            confs = getConfigurations(false);
            String initAct = getInitialActive();
            OUTER: if (initAct != null) {
                for (M2Configuration conf : confs) {
                    if (initAct.equals(conf.getId())) {
                        if (_active != conf) {
                            _active = conf;
                            break OUTER;
                        } else {
                            _active = conf;
                        }
                        initAct = null;
                        break;
                    }
                }
                if (b == null && initAct != null) {
                    RP.post(new Runnable() {
                        public @Override void run() {
                            try {
                                doSetActiveConfiguration(DEFAULT, null);
                            } catch (Exception ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    });
                }
                initialActive = null; //here we reset the initial active field value to prevent this block from happening exactly once.
            }
            if (b == null) {
                active = _active;
            }
        }
        checkActiveAgainstAll(confs, true);
        return _active;
    }
    
    public void setConfigurations(List<M2Configuration> shared, List<M2Configuration> nonshared, boolean includeProfiles) {
        writeAuxiliaryData(aux, true, shared);
        writeAuxiliaryData(aux, false, nonshared);
        synchronized (this) {
            this.shared = new TreeSet<M2Configuration>(shared);
            this.nonshared = new TreeSet<M2Configuration>(nonshared);
            //#174637
            if (active != null) {
                if (shared.contains(active)) {
                    M2Configuration newActive = shared.get(shared.indexOf(active));
                    //can have different content
                    active = newActive;
                }
                if (nonshared.contains(active)) {
                    M2Configuration newActive = nonshared.get(nonshared.indexOf(active));
                    //can have different content
                    active = newActive;
                }
            }
            this.profiles = null;
        }
        firePropertyChange();
    }

    public @Override void setActiveConfiguration(M2Configuration configuration) throws IllegalArgumentException, IOException {
        M2Configuration _active;
        synchronized (this) {
            if (active == configuration || (active != null && active.equals(configuration))) {
                return;
            }
            _active = active;
        }
        doSetActiveConfiguration(configuration, _active);
        NbMavenProject.fireMavenProjectReload(project);
    }

    private void doSetActiveConfiguration(M2Configuration newone, M2Configuration old) throws IllegalArgumentException, IOException {
        M2Configuration _active;
        synchronized (this) {
            active = newone;
            writeAuxiliaryData(
                    aux,
                    ACTIVATED, active.getId());
            _active = active;
        }
        assert !Thread.holdsLock(this);
        support.firePropertyChange(PROP_CONFIGURATION_ACTIVE, old, _active);
    }

    private SortedSet<M2Configuration> createProfilesList() {
        List<String> profs = profileHandler.getAllProfiles();
        SortedSet<M2Configuration> config = new TreeSet<M2Configuration>();
//        config.add(DEFAULT);
        for (String prof : profs) {
            M2Configuration c = new M2Configuration(prof, project.getProjectDirectory());
            c.setActivatedProfiles(Collections.singletonList(prof));
            config.add(c);
        }
        return config;
    }
    
    private static void addProfileNames(Set<String> names, Set<M2Configuration> profile) {
        for (M2Configuration c : profile) {
            names.add(c.getId());
        }
    }
    
    private Lookup.Result<MavenActionsProvider> actionProviders;
    
    // @GuardedBy(this)
    private SortedSet<M2Configuration> createProvidedList() {
        SortedSet<M2Configuration> result = new TreeSet<>();
        provided = result;
        
        Boolean b = inConfigInit.get();
        inConfigInit.set(true);
        try {
            Collection<? extends MavenActionsProvider> providers;
            synchronized (this) {
                Lookup.Result<MavenActionsProvider> r = actionProviders;
                if (r == null) {
                    r = project.getLookup().lookupResult(MavenActionsProvider.class);
                }
                providers = new ArrayList<>(r.allInstances());
                // initialize the listener after initial Lookup, otherwise the event may be fired. That could flush() 
                // e.g. in the middle of getConfigurations() processing.
                if (actionProviders == null) {
                    actionProviders = r;
                    actionProviders.addLookupListener(new LookupListener() {
                        @Override
                        public void resultChanged(LookupEvent ev) {
                            flush();
                        }
                    });
                }
            }
            for (MavenActionsProvider p : providers) {
                if (p == this) {
                    continue;
                }
                if (p instanceof AbstractMavenActionsProvider) {
                    List<NetbeansActionProfile> profiles;
                    M2Configuration c = setLocalConfiguration(DEFAULT);
                    try {
                        profiles = ((AbstractMavenActionsProvider) p).getRawMappings().getProfiles();
                        if (profiles.isEmpty()) {
                            continue;
                        }
                    } finally {
                        setLocalConfiguration(c);
                    }
                    Set<String> knownIds = new HashSet<>();
                    addProfileNames(knownIds, this.profiles);
                    addProfileNames(knownIds, this.shared);
                    addProfileNames(knownIds, this.nonshared);

                    for (NetbeansActionProfile prof : profiles) {
                        if (!knownIds.contains(prof.getId())) {
                            M2Configuration cfg = new M2Configuration(prof.getId(), project.getProjectDirectory()) {
                                {
                                    if (p instanceof ActionNameProvider) {
                                        reader = DefaultActionGoalProvider.createI18nReader(((ActionNameProvider)p).getTranslations());
                                    }
                                }
                                @Override
                                public ActionToGoalMapping getRawMappings() {
                                    try (InputStream istm = getActionDefinitionStream()) {
                                        if (istm != null) {
                                            return super.getRawMappings();
                                        }
                                    } catch (IOException ex) {
                                        // silently ignore
                                    }
                                    ActionToGoalMapping m = new ActionToGoalMapping();
                                    ActionToGoalMapping allMappings;
                                    M2Configuration save = setLocalConfiguration(getDefaultConfig());
                                    try {
                                        allMappings = ((AbstractMavenActionsProvider) p).getRawMappings();
                                    } finally {
                                        setLocalConfiguration(save);
                                    }
                                    List<NetbeansActionProfile> profiles = allMappings.getProfiles();
                                    for (NetbeansActionProfile p : profiles) {
                                        if (prof.getId().equals(p.getId())) {
                                            for (NetbeansActionMapping am : p.getActions()) {
                                                m.addAction(am);
                                            }
                                            // default actions are handled in the UI, should not be added
                                            // as they are not customized by this profile (?)
                                        }
                                    }
                                    return m;
                                }
                            };
                            cfg.setDisplayName(prof.getDisplayName());
                            result.add(cfg);
                        }
                    }
                }
            }
            return result;
        } finally {
            inConfigInit.set(b);
        }
    }

    public @Override synchronized void addPropertyChangeListener(PropertyChangeListener lst) {
        if (support.getPropertyChangeListeners().length == 0) {
            project.getProjectWatcher().addPropertyChangeListener(propertyChange);
        }
        support.addPropertyChangeListener(lst);

    }

    public @Override synchronized void removePropertyChangeListener(PropertyChangeListener lst) {
        support.removePropertyChangeListener(lst);
        if (support.getPropertyChangeListeners().length == 0) {
            project.getProjectWatcher().removePropertyChangeListener(propertyChange);
        }
    }


    private void firePropertyChange() {
        support.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATIONS, null, null);
    }
    
    

    public static void writeAuxiliaryData(AuxiliaryConfiguration conf, String property, String value) {
        Element el = conf.getConfigurationFragment(ROOT, NAMESPACE, false);
        if (el == null) {
            el = XMLUtil.createDocument(ROOT, NAMESPACE, null, null).getDocumentElement();
        }
        Element enEl;
        NodeList list = el.getElementsByTagNameNS(NAMESPACE, property);
        if (list.getLength() > 0) {
            enEl = (Element)list.item(0);
        } else {
            enEl = el.getOwnerDocument().createElementNS(NAMESPACE, property);
            el.appendChild(enEl);
        }
        enEl.setTextContent(value);
        conf.putConfigurationFragment(el, false);
    }

    private static void writeAuxiliaryData(AuxiliaryConfiguration conf, boolean shared, List<M2Configuration> configs) {
        if (configs.isEmpty()) {//#226017
            conf.removeConfigurationFragment(ROOT, NAMESPACE, shared);
            return;
        }
        Element el = conf.getConfigurationFragment(ROOT, NAMESPACE, shared);
        if (el == null) {
            el = XMLUtil.createDocument(ROOT, NAMESPACE, null, null).getDocumentElement();
        }
        Element enEl;
        NodeList list = el.getElementsByTagNameNS(NAMESPACE, CONFIGURATIONS);
        if (list.getLength() > 0) {
            enEl = (Element)list.item(0);
            NodeList nl = enEl.getChildNodes();
            int len = nl.getLength();
            for (int i = 0; i < len; i++) {
                enEl.removeChild(nl.item(0));
            }
        } else {
            enEl = el.getOwnerDocument().createElementNS(NAMESPACE, CONFIGURATIONS);
            el.appendChild(enEl);
        }
        for (M2Configuration config : configs) {
            Element child  = enEl.getOwnerDocument().createElementNS(NAMESPACE, CONFIG);
            child.setAttribute(CONFIG_ID_ATTR, config.getId());
            child.setAttribute(CONFIG_PROFILES_ATTR, StringUtils.join(config.getActivatedProfiles().iterator(), " "));
            for (Map.Entry<String,String> entry : config.getProperties().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key != null && value != null) {
                    Element prop  = enEl.getOwnerDocument().createElementNS(NAMESPACE, PROPERTY);
                    prop.setAttribute(PROPERTY_NAME_ATTR, key);
                    prop.setTextContent(value);
                    child.appendChild(prop);
                }
            }
            enEl.appendChild(child);
        }
        conf.putConfigurationFragment(el, shared);
    }

}
