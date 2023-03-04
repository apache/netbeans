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
package org.netbeans.modules.analysis;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**XXX: declarative configurations?
 *
 * @author Jan Becicka
 */
public class ConfigurationsManager {
    private static final String RULE_PREFIX = "rule_config_";
    private static final String KEY_CONFIGURATIONS_VERSION = "configurations.version";
    private static final int CURRENT_CONFIGURATIONS_VERSION = 1;

    private ChangeSupport changeSupport = new ChangeSupport(this);

    private ConfigurationsManager() {
        configs = new ArrayList<Configuration>();
        init();
    }
    
    private static ConfigurationsManager instance;
    
    private ArrayList<Configuration> configs;
    
    public static synchronized ConfigurationsManager getDefault() {
        if (instance == null) {
            instance = new ConfigurationsManager();
        }
        return instance;
    }
    
    public Configuration getDefaultConfiguration() {
        return getConfiguration(0);
    }
    
    public List<Configuration> getConfigurations() {
        return Collections.unmodifiableList(configs);
    }
    
    public Configuration getConfiguration(int i) {
        return configs.get(i);
    }
    
    public int size() {
        return configs.size();
    }

    private void init() {
        Preferences prefs = getConfigurationsRoot();
        try {
            for (String kid:prefs.childrenNames()) {
                if (kid.startsWith(RULE_PREFIX)) {
                    Preferences p = prefs.node(kid);
                    String displayName = p.get("display.name", "unknown");
                    create(kid.substring(RULE_PREFIX.length()), displayName);
                }
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (configs.isEmpty()) {
            create("default", NbBundle.getMessage(ConfigurationsManager.class, "DN_Default"));
        }
        prefs.putInt(KEY_CONFIGURATIONS_VERSION, CURRENT_CONFIGURATIONS_VERSION);
    }

    public Configuration create(String id, String displayName) {
        assert !id.startsWith(RULE_PREFIX);
        Configuration config = new Configuration(RULE_PREFIX + id, displayName, null);
        configs.add(config);
        changeSupport.fireChange();
        return config;
    }
    
    public Configuration duplicate(Configuration orig, String id, String displayName) {
        assert !id.startsWith(RULE_PREFIX);
        Configuration config = new Configuration(RULE_PREFIX + id, displayName, null);
        configs.add(config);
        
        Preferences oldOne = orig.getPreferences();
        Preferences newOne = config.getPreferences();
        try {
            List<SimpleEntry<Preferences, Preferences>> todo = new LinkedList<SimpleEntry<Preferences, Preferences>>();
            boolean first = true;
            
            todo.add(new SimpleEntry<Preferences, Preferences>(oldOne, newOne));
            
            while (!todo.isEmpty()) {
                SimpleEntry<Preferences, Preferences> e = todo.remove(0);
                for (String name:e.getKey().childrenNames()) {
                    todo.add(new SimpleEntry<Preferences, Preferences>(e.getKey().node(name), e.getValue().node(name)));
                }
                if (first) {
                    first = false;
                    continue;
                }
                for (String key : e.getKey().keys()) {
                    String old = e.getKey().get(key, null);
                    e.getValue().put(key, old);
                }
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        changeSupport.fireChange();
        return config;
    }
    
    public void remove(Configuration config) {
        configs.remove(config);
        Preferences prefs = NbPreferences.forModule(this.getClass()).node(config.id());
        try {
            prefs.removeNode();
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
        changeSupport.fireChange();
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener( listener );
    }

    public void removeChangeListener(ChangeListener listener ) {
        changeSupport.removeChangeListener( listener );
    }
    
    public static Preferences getConfigurationsRoot() {
        return NbPreferences.forModule(ConfigurationsManager.class).node("configurations");
    }
    
    public Configuration getTemporaryConfiguration() {
        return new Configuration("internal-temporary", "internal-temporary", NbPreferences.forModule(ConfigurationsManager.class).node("internal-temporary"));
    }
    
}
