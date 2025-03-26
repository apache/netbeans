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
package org.netbeans.modules.php.project.connections;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.util.StringUtils;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * Configuration manager which is able to maintain any number of sets of key-value pairs.
 * Caller has to provide the config map (one can use {@link #createEmptyConfigs()} method).
 * It is caller's responsibility to save configurations.
 * <p>
 * Deleted {@link Configuration} remains in map with the value equals to <code>null</code>.
 * @author Radek Matous, Tomas Mysik
 */
public final class ConfigManager {
    static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());
    public static final String PROP_DISPLAY_NAME = "$label"; // NOI18N

    private final Map<String/*|null*/, Map<String, String/*|null*/>/*|null*/> configs;
    // error messages for configruations
    private final Map<String/*|null*/, String/*|null*/> configErrors = new HashMap<>();
    private final ConfigProvider configProvider;
    private final String[] propertyNames;
    private final ChangeSupport changeSupport;

    private volatile String currentConfig;

    public ConfigManager(ConfigProvider configProvider) {
        this(configProvider, null);
    }

    public ConfigManager(ConfigProvider configProvider, String currentConfig) {
        this.configProvider = configProvider;
        changeSupport = new ChangeSupport(this);
        configs = createEmptyConfigs();
        configs.putAll(configProvider.getConfigs());
        this.currentConfig = currentConfig;

        List<String> tmp = new ArrayList<>(Arrays.asList(configProvider.getConfigProperties()));
        tmp.add(PROP_DISPLAY_NAME);
        propertyNames = tmp.toArray(new String[0]);
    }

    /**
     * Suitable for creating new, empty map for configurations. The default configuration is already present.
     * @return empty map for configurations.
     */
    public static Map<String, Map<String, String>> createEmptyConfigs() {
        Map<String, Map<String, String>> configs = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1 != null ? (s2 != null ? s1.compareTo(s2) : 1) : (s2 != null ? -1 : 0);
            }
        });
        // the default config has to be there even if it is not used
        configs.put(null, null);
        return configs;
    }

    /**
     * {@link Comparator} suitable for {@link Configuration configuration} ordering according to
     * the display name (locale-sensitive string comparison).
     * @return {@link Comparator} for {@link Configuration configuration} ordering.
     */
    public static Comparator<Configuration> getConfigurationComparator() {
        return new Comparator<Configuration>() {
            Collator coll = Collator.getInstance();
            @Override
            public int compare(Configuration c1, Configuration c2) {
                String lbl1 = c1.getDisplayName();
                String lbl2 = c2.getDisplayName();
                return coll.compare(lbl1, lbl2);
            }
        };
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    // configs are reseted to their original state (discards changes in memory)
    public synchronized void reset() {
        configs.clear();
        configErrors.clear();
        configs.putAll(configProvider.getConfigs());
    }

    public synchronized boolean exists(String name) {
        return configs.containsKey(name) && configs.get(name) != null;
    }

    public synchronized Configuration createNew(String name, String displayName) {
        assert !exists(name);
        configs.put(name, new HashMap<String, String>());
        Configuration retval  = new Configuration(name);
        if (!name.equals(displayName)) {
            retval.putValue(PROP_DISPLAY_NAME, displayName);
        }
        markAsCurrentConfiguration(name);
        return retval;
    }

    public synchronized Collection<String> configurationNames() {
        return configs.keySet();
    }

    public synchronized Configuration currentConfiguration() {
        if (exists(currentConfig)) {
            return new Configuration(currentConfig);
        }
        // #176670
        LOGGER.log(Level.WARNING, "Missing configuration \"{0}\" found - perhaps deleted?", currentConfig);
        return createNew(currentConfig, currentConfig);
    }

    public Configuration defaultConfiguration() {
        return new Configuration();
    }

    public synchronized Configuration configurationFor(String name) {
        return new Configuration(name);
    }

    public void markAsCurrentConfiguration(String currentConfig) {
        synchronized (this) {
            assert configs.containsKey(currentConfig);
            this.currentConfig = currentConfig;
        }
        changeSupport.fireChange();
    }

    private String[] getPropertyNames() {
        return propertyNames;
    }

    private Map<String, String/*|null*/> getProperties(String config) {
        return configs.get(config);
    }

    public static String encode(String input) {
        return encodeDecode(input);
    }

    public static String decode(String input) {
        return encodeDecode(input);
    }

    private static String encodeDecode(String input) {
        if (input == null) {
            return null;
        }
        return rot13coder(input);
    }

    // not secure - just not to be obvious at first glance
    static String rot13coder(String input) {
        char[] out = new char[input.length()];
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c >= 'a' && c <= 'm') {
                c += 13;
            } else if (c >= 'n' && c <= 'z') {
                c -= 13;
            } else if (c >= 'A' && c <= 'M') {
                c += 13;
            } else if (c >= 'A' && c <= 'Z') {
                c -= 13;
            }
            out[i] = c;
        }
        return String.valueOf(out);
    }

    public final class Configuration {
        private final String name;

        private Configuration() {
            this(null);
        }

        private Configuration(String name) {
            if (name != null && name.trim().length() == 0) {
                name = null;
            }
            assert configs.containsKey(name) : "Unknown configuration: " + name;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            String retval = getValue(PROP_DISPLAY_NAME);
            retval = retval != null ? retval : getName();
            return retval != null ? retval : NbBundle.getMessage(ConfigManager.class, "LBL_DefaultConfiguration");
        }

        public void setDisplayName(String displayName) {
            if (!StringUtils.hasText(displayName)
                    || displayName.equals(name)) {
                putValue(PROP_DISPLAY_NAME, null);
            } else {
                putValue(PROP_DISPLAY_NAME, displayName);
            }
        }

        public boolean isDefault() {
            return name == null;
        }

        public void delete() {
            synchronized (ConfigManager.this) {
                // just "mark as deleted" (null) to be able to remove property file etc.
                //configs.remove(getName());
                //configErrors.remove(getName());
                configs.put(getName(), null);
                configErrors.put(getName(), null);
                markAsCurrentConfiguration(null);
            }
        }

        private boolean isDeleted() {
            return configs.get(getName()) == null;
        }

        public String getValue(String propertyName, boolean decode) {
            String value = getValue(propertyName);
            if (decode && value != null) {
                value = rot13coder(value);
            }
            return value;
        }

        public String getValue(String propertyName) {
            assert Arrays.asList(getPropertyNames()).contains(propertyName) : "Unknown property: " + propertyName;
            //assert !isDeleted();
            synchronized (ConfigManager.this) {
                return !isDeleted() ?  getProperties(getName()).get(propertyName) : null;
            }
        }

        public void putValue(String propertyName, String value, boolean encode) {
            if (encode && value != null) {
                value = rot13coder(value);
            }
            putValue(propertyName, value);
        }

        public void putValue(String propertyName, String value) {
            assert Arrays.asList(getPropertyNames()).contains(propertyName) : "Unknown property: " + propertyName;
            assert !isDeleted();
            synchronized (ConfigManager.this) {
                getProperties(getName()).put(propertyName, value);
            }
        }

        public String[] getPropertyNames() {
            synchronized (ConfigManager.this) {
                return ConfigManager.this.getPropertyNames();
            }
        }

        /**
         * Get the error message for the configuration.
         * @return the error message for the configuration.
         * @see #setErrorMessage(java.lang.String)
         */
        public String getErrorMessage() {
            return configErrors.get(name);
        }

        /**
         * Set the error message for the configuration. The message should be internalized. The configuration is then invalid.
         * Valid configuration can be set using <code>null</code>.
         * @param errorMessage the error message for the configuration or <code>null</code> to set the configuration as valid.
         */
        public void setErrorMessage(String errorMessage) {
            configErrors.put(name, errorMessage);
        }

        /**
         * Return <code>true</code> if the configuration is valid (it means that no error message is set).
         * @return
         */
        public boolean isValid() {
            return configErrors.get(name) == null;
        }
    }

    /**
     * Configuration provider for {@link ConfigManager configuration manager}.
     */
    public interface ConfigProvider {

        /**
         * Get all names of the properties which can be defined in each configuration.
         * @return an array of property names.
         */
        String[] getConfigProperties();

        /**
         * Get all the configurations the configuration manager should operate with.
         * @return all the configurations.
         */
        Map<String/*|null*/, Map<String, String/*|null*/>/*|null*/> getConfigs();
    }
}
