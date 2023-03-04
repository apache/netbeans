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
package org.netbeans.modules.java.hints.spiimpl.refactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        Preferences prefs = NbPreferences.forModule(this.getClass());
        try {
            String[] configList = prefs.childrenNames();
            //fix sorting for JDK migrators
            List<String> sl = Arrays.asList(configList);
            final String exp = "([0-9]+)$"; //NOI18N

            sl.sort(new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    Pattern pattern = Pattern.compile(exp);
                    Matcher m1 = pattern.matcher(s1);
                    if (m1.find()) {
                        Matcher m2 = pattern.matcher(s2);
                        if (m2.find()) {
                            String part_s1 = s1.substring(0, m1.start());
                            String part_s2 = s2.substring(0, m2.start());
                            if (part_s1.equals(part_s2)) {
                                int val1 = Integer.parseInt(m1.group());
                                int val2 = Integer.parseInt(m2.group());
                                return val1 - val2;
                            }
                        }
                    }
                    return s1.compareTo(s2);
                }
            });
            for (String kid : sl.toArray(configList)) {
                if (kid.startsWith(RULE_PREFIX)) {
                    Preferences p = NbPreferences.forModule(this.getClass()).node(kid);
                    String displayName = p.get("display.name", "unknown");
                    create(kid.substring(RULE_PREFIX.length()), displayName);
                }
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
        int configurationsVersion = prefs.getInt(KEY_CONFIGURATIONS_VERSION, 0);
        if (configs.isEmpty()) {
            create("default", NbBundle.getMessage(ConfigurationsManager.class, "DN_Default"));
            Configuration jdk7 = create("jdk7", NbBundle.getMessage(ConfigurationsManager.class, "DN_ConvertToJDK7"));
            jdk7.enable("Javac_canUseDiamond");
            jdk7.enable("org.netbeans.modules.java.hints.jdk.ConvertToStringSwitch");
            jdk7.enable("org.netbeans.modules.java.hints.jdk.ConvertToARM");
            jdk7.enable("org.netbeans.modules.java.hints.jdk.JoinCatches");
            // #215546 - requires user inspection
            // jdk7.enable("org.netbeans.modules.java.hints.jdk.UseSpecificCatch");
            //jdk7.enable("java.util.Objects");
        }
        if (configurationsVersion < 1 && !configurationExists("organizeImports")) {
            Configuration organizeImports = create("organizeImports", NbBundle.getMessage(ConfigurationsManager.class, "DN_OrganizeImports"));
            organizeImports.enable("org.netbeans.modules.java.hints.OrganizeImports");
        }
        prefs.putInt(KEY_CONFIGURATIONS_VERSION, CURRENT_CONFIGURATIONS_VERSION);
    }

    private boolean configurationExists(String id) {
        for (Configuration c : configs) {
            if (id.equals(c.id())) return true;
        }

        return false;
    }
    
    public Configuration create(String id, String displayName) {
        assert !id.startsWith(RULE_PREFIX);
        Configuration config = new Configuration(RULE_PREFIX + id, displayName);
        configs.add(config);
        changeSupport.fireChange();
        return config;
    }
    
    //TODO: copied from HintsSettings - would be better to have it on one place:
    private static final String PREFERENCES_LOCATION = "org/netbeans/modules/java/hints";
    
    public Configuration duplicate(Configuration orig, String id, String displayName) {
        assert !id.startsWith(RULE_PREFIX);
        Configuration config = new Configuration(RULE_PREFIX + id, displayName);
        configs.add(config);
        
        Preferences oldOne = NbPreferences.root().node(PREFERENCES_LOCATION).node(orig.id());
        Preferences newOne = NbPreferences.root().node(PREFERENCES_LOCATION).node(config.id());
        try {
            for (String name:oldOne.childrenNames()) {
                Preferences node = oldOne.node(name);
                for (String key: node.keys()) {
                    String old = node.get(key, null);
                    newOne.node(name).put(key, old);
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
    
    
}
