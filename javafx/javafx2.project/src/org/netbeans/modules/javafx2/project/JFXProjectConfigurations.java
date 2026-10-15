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
package org.netbeans.modules.javafx2.project;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Project configurations maintenance class
 * 
 * Getter/Setter naming conventions:
 * "Property" in method name -> method deals with single properties in configuration given by method parameter config
 * "Default" in method name -> method deals with properties in default configuration
 * "Active" in method name -> method deals with properties in currently chosen configuration
 * "Transparent" in method name -> method deals with property in configuration fiven by method parameter config if
 *     exists, or with property in default configuration otherwise. This is to provide simple access to
 *     union of default and non-default properties that are to be presented to users in non-default configurations
 *
 * @author Petr Somol
 */
public class JFXProjectConfigurations {
    
    private static final Logger LOG = Logger.getLogger(JFXProjectConfigurations.class.getName());

    public static final String APPLICATION_ARGS = ProjectProperties.APPLICATION_ARGS;
    public static final String DEFAULT_CONFIG_NAME = "default"; //NOI18N
    
    public static final String APP_PARAM_PREFIX = "javafx.param."; // NOI18N
    public static final String APP_PARAM_SUFFIXES[] = new String[] { "name", "value", "hidden" }; // NOI18N
    public static final String APP_PARAM_CONNECT_SIGN = "="; // NOI18N

    public static final String APP_MANIFEST_PREFIX = "javafx.manifest.entry."; // NOI18N
    public static final String APP_MANIFEST_SUFFIXES[] = new String[] { "name", "value", "hidden" }; // NOI18N
    public static final String APP_MANIFEST_CONNECT_SIGN = ": "; // NOI18N

    private static final String MULTI_PROPERTY_STRING = "MultiProperty"; //NOI18N
    private static final String MULTI_PROPERTY_EMPTY = "empty"; // NOI18N
    public static final String APP_MULTIPROP_HIDDEN_TRUE = "true"; // NOI18N
    
    // folders and files
    public static final String PROJECT_CONFIGS_DIR = "nbproject/configs"; // NOI18N
    public static final String PROJECT_PRIVATE_CONFIGS_DIR = "nbproject/private/configs"; // NOI18N
    public static final String PROPERTIES_FILE_EXT = "properties"; // NOI18N
    // the following should be J2SEConfigurationProvider.CONFIG_PROPS_PATH which is now inaccessible from here
    public static final String CONFIG_PROPERTIES_FILE = "nbproject/private/config.properties"; // NOI18N    

    public static String getSharedConfigFilePath(final @NonNull String config)
    {
        return PROJECT_CONFIGS_DIR + "/" + config + "." + PROPERTIES_FILE_EXT; // NOI18N
    }

    public static String getPrivateConfigFilePath(final @NonNull String config)
    {
        return PROJECT_PRIVATE_CONFIGS_DIR + "/" + config + "." + PROPERTIES_FILE_EXT; // NOI18N
    }

    private Map<String/*|null*/,Map<String,String/*|null*/>/*|null*/> RUN_CONFIGS;
    private MultiProperty appParams;
    private MultiProperty appManifestEntries;
            
    private Set<String> ERASED_CONFIGS;
    private BoundedPropertyGroups groups = new BoundedPropertyGroups();
    private String active;
    
    private FileObject projectDir;

    // list of all properties related to project configurations (excluding application parameter properties that are handled separately)
    private List<String> PROJECT_PROPERTIES = new ArrayList<String>();
    // list of those properties that should be stored in private.properties instead of project.properties
    private List<String> PRIVATE_PROPERTIES = new ArrayList<String>();
    // list of properties that, if set, should later not be overriden by changes in default configuration
    // (useful for keeping pre-defined configurations that do not change unexpectedly after changes in default config)
    // Note that the standard behavior is: when setting a default property, the property is checked in all configs
    // and reset if its value in any non-def config is equal to that in default config
    private List<String> STATIC_PROPERTIES = new ArrayList<String>();
    // defaults if missing - on read, substitute missing property values by those registered here
    private Map<String, String> DEFAULT_IF_MISSING = new HashMap<String, String>();
    // on save remove the following props from file if they are empty
    private List<String> CLEAN_EMPTY_PROJECT_PROPERTIES = new ArrayList<String>();
    private List<String> CLEAN_EMPTY_PRIVATE_PROPERTIES = new ArrayList<String>();

    private Comparator<String> getComparator() {
        return new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1 != null ? (s2 != null ? s1.compareTo(s2) : 1) : (s2 != null ? -1 : 0);
            }
        };
    }

    JFXProjectConfigurations(final @NonNull FileObject projectDirFO) {
        projectDir = projectDirFO;
        reset();
    }

    public void registerProjectProperties(String[] props) {
        if(props != null) {
            PROJECT_PROPERTIES.addAll(Arrays.asList(props));
        }
    }            
    public void registerPrivateProperties(String[] props) {
        if(props != null) {
            PRIVATE_PROPERTIES.addAll(Arrays.asList(props));
        }
    }
    public void registerStaticProperties(String[] props) {
        if(props != null) {
            STATIC_PROPERTIES.addAll(Arrays.asList(props));
        }
    }
    public void registerDefaultsIfMissing(Map<String, String> defaults) {
        if(defaults != null) {
            DEFAULT_IF_MISSING.putAll(defaults);
        }
    }
    public void registerCleanEmptyProjectProperties(String[] props) {
        if(props != null) {
            CLEAN_EMPTY_PROJECT_PROPERTIES.addAll(Arrays.asList(props));
        }
    }
    public void registerCleanEmptyPrivateProperties(String[] props) {
        if(props != null) {
            CLEAN_EMPTY_PRIVATE_PROPERTIES.addAll(Arrays.asList(props));
        }
    }
    public void resetProjectProperties() {
        PROJECT_PROPERTIES.clear();
    }
    public void resetPrivateProperties() {
        PRIVATE_PROPERTIES.clear();
    }
    public void resetStaticProperties() {
        STATIC_PROPERTIES.clear();
    }
    public void resetDefaultsIfMissing() {
        DEFAULT_IF_MISSING.clear();
    }
    public void resetCleanEmptyProjectProperties() {
        CLEAN_EMPTY_PROJECT_PROPERTIES.clear();
    }
    public void resetCleanEmptyPrivateProperties() {
        CLEAN_EMPTY_PRIVATE_PROPERTIES.clear();
    }

    public void reset() {
        RUN_CONFIGS = new TreeMap<String,Map<String,String>>(getComparator());
        ERASED_CONFIGS = null;
        appParams = new MultiProperty(APP_PARAM_PREFIX, APP_PARAM_SUFFIXES, APP_PARAM_CONNECT_SIGN);
        appManifestEntries = new MultiProperty(APP_MANIFEST_PREFIX, APP_MANIFEST_SUFFIXES, APP_MANIFEST_CONNECT_SIGN);
    }

    private boolean configNameWrong(String config) {
        return config !=null && config.contains(DEFAULT_CONFIG_NAME); //NOI18N
    }

    public final void defineGroup(String groupName, Collection<String> props) {
        groups.defineGroup(groupName, props);
    }

    public final void clearGroup(String groupName) {
        groups.clearGroup(groupName);
    }

    public final void clearAllGroups() {
        groups.clearAllGroups();
    }

    public boolean isBound(String prop) {
        return groups.isBound(prop);
    }

    public Collection<String> getBoundedProperties(String prop) {
        return groups.getBoundedProperties(prop);
    }

    //==========================================================

    public String getActive() {
        return active;
    }
    public void setActive(String config) {
        assert !configNameWrong(config);
        active = config;
    }

    //==========================================================

    public boolean hasConfig(String config) {
        assert !configNameWrong(config);
        return RUN_CONFIGS.containsKey(config);
    }

    public boolean isConfigEmpty(String config) {
        assert !configNameWrong(config);
        Map<String,String/*|null*/> configMap = getConfig(config);
        if(configMap != null) {
            return configMap.isEmpty();
        }
        return true;
    }

    public boolean isDefaultConfigEmpty() {
        return isConfigEmpty(null);
    }

    public boolean isActiveConfigEmpty() {
        return isConfigEmpty(getActive());
    }

    //----------------------------------------------------------

    public Set<String> getConfigNames() {
        return Collections.unmodifiableSet(RUN_CONFIGS.keySet());
    }

    private Map<String,String/*|null*/> getConfigUnmodifyable(String config) {
        assert !configNameWrong(config);
        return Collections.unmodifiableMap(RUN_CONFIGS.get(config));
    }

    private Map<String,String/*|null*/> getDefaultConfigUnmodifyable() {
        return getConfigUnmodifyable(null);
    }

    private Map<String,String/*|null*/> getActiveConfigUnmodifyable() {
        return getConfigUnmodifyable(getActive());
    }

    private Map<String,String/*|null*/> getConfig(String config) {
        assert !configNameWrong(config);
        return RUN_CONFIGS.get(config);
    }

    private Map<String,String/*|null*/> getDefaultConfig() {
        return getConfig(null);
    }

    private Map<String,String/*|null*/> getActiveConfig() {
        return getConfig(getActive());
    }

    private Map<String,String/*|null*/> getConfigNonNull(String config) {
        assert !configNameWrong(config);
        Map<String,String/*|null*/> configMap = getConfig(config);
        if(configMap == null) {
            configMap = new TreeMap<String,String>(getComparator());
            RUN_CONFIGS.put(config, configMap);
        }
        return configMap;
    }

    private Map<String,String/*|null*/> getDefaultConfigNonNull() {
        return getConfigNonNull(null);
    }

    private Map<String,String/*|null*/> getActiveConfigNonNull() {
        return getConfigNonNull(getActive());
    }

    //----------------------------------------------------------

    /**
     * Adds new and replaces existing properties
     * @param config
     * @param props 
     */
    public void addToConfig(String config, Map<String,String/*|null*/> props) {
        assert !configNameWrong(config);
        Map<String,String/*|null*/> configMap = getConfig(config);
        if(configMap == null) {
            configMap = new TreeMap<String,String>(getComparator());
            RUN_CONFIGS.put(config, configMap);
        }
        configMap.putAll(props);
    }

    public void addToDefaultConfig(Map<String,String/*|null*/> props) {
        addToConfig(null, props);
    }

    public void addToActiveConfig(Map<String,String/*|null*/> props) {
        addToConfig(getActive(), props);
    }

    public void addToConfig(String config, EditableProperties props) {
        assert !configNameWrong(config);
        addToConfig(config, new HashMap<String,String>(props));
    }

    public void addToDefaultConfig(EditableProperties props) {
        addToConfig(null, props);
    }

    public void addToActiveConfig(EditableProperties props) {
        addToConfig(getActive(), props);
    }

    //----------------------------------------------------------

    public void eraseConfig(String config) {
        assert !configNameWrong(config);
        assert config != null; // erasing default config not allowed
        RUN_CONFIGS.remove(config);
        if(ERASED_CONFIGS == null) {
            ERASED_CONFIGS = new HashSet<String>();
        }
        ERASED_CONFIGS.add(config);
    }

    //==========================================================

    /**
     * Returns true if property name is defined in configuration config, false otherwise
     * @param config
     * @param name
     * @return 
     */
    public boolean isPropertySet(String config, @NonNull String prop) {
        assert !configNameWrong(config);
        Map<String,String/*|null*/> configMap = getConfig(config);
        if(configMap != null) {
            return configMap.containsKey(prop);
        }
        return false;
    }

    public boolean isDefaultPropertySet(@NonNull String prop) {
        return isPropertySet(null, prop);
    }

    public boolean isActivePropertySet(@NonNull String prop) {
        return isPropertySet(getActive(), prop);
    }

    /**
     * Returns true if bounded properties exist for prop and at least
     * one of them is set. This is to be used in updateProperty() to
     * indicate that an empty property needs to be stored to editable properties
     * 
     * @param config
     * @param prop
     * @return 
     */
    private boolean isBoundedToNonemptyProperty(String config, String prop) {
        assert !configNameWrong(config);
        for(String name : groups.getBoundedProperties(prop)) {
            if(isPropertySet(config, name)) {
                return true;
            }
        }
        return false;
    }

    //----------------------------------------------------------

    /**
     * Returns property value from configuration config if defined, null otherwise
     * @param config
     * @param name
     * @return 
     */
    public String getProperty(String config, @NonNull String prop) {
        assert !configNameWrong(config);
        Map<String,String/*|null*/> configMap = getConfig(config);
        if(configMap != null) {
            return configMap.get(prop);
        }
        return null;
    }

    public String getDefaultProperty(@NonNull String prop) {
        return getProperty(null, prop);
    }

    public String getActiveProperty(@NonNull String prop) {
        return getProperty(getActive(), prop);
    }

    /**
     * Returns property value from configuration config (if exists), or
     * value from default config (if exists) otherwise
     * 
     * @param config
     * @param name
     * @return 
     */
    public String getPropertyTransparent(String config, @NonNull String prop) {
        assert !configNameWrong(config);
        Map<String,String/*|null*/> configMap = getConfig(config);
        String value = null;
        if(configMap != null) {
            value = configMap.get(prop);
            if(value == null && config != null) {
                return getDefaultProperty(prop);
            }
        }
        return value;
    }

    public String getActivePropertyTransparent(@NonNull String prop) {
        return getPropertyTransparent(getActive(), prop);
    }

    //----------------------------------------------------------

    public void setProperty(String config, @NonNull String prop, String value) {
        setPropertyImpl(config, prop, value);
        solidifyBoundedGroups(config, prop);
        if(config == null) {
            for(String c: getConfigNames()) {
                if(c != null && JFXProjectProperties.isEqualText(getProperty(c, prop), value) && !STATIC_PROPERTIES.contains(prop) && isBoundedPropertiesEraseable(c, prop)) {
                    eraseProperty(c, prop);
                }
            }
        }
    }

    private void setPropertyImpl(String config, @NonNull String prop, String value) {
        assert !configNameWrong(config);
        Map<String,String/*|null*/> configMap = getConfigNonNull(config);
        configMap.put(prop, value);            
    }

    public void setDefaultProperty(@NonNull String prop, String value) {
        setProperty(null, prop, value);
    }

    public void setActiveProperty(@NonNull String prop, String value) {
        setProperty(getActive(), prop, value);
    }

    public void setPropertyTransparent(String config, @NonNull String prop, String value) {
        assert !configNameWrong(config);
        if(config != null && JFXProjectProperties.isEqualText(getDefaultProperty(prop), value) && (!STATIC_PROPERTIES.contains(prop) || !isPropertySet(config, prop)) && isBoundedPropertiesEraseable(config, prop)) {
            eraseProperty(config, prop);
        } else {
            setProperty(config, prop, value);
        }
    }

    public void setActivePropertyTransparent(@NonNull String prop, String value) {
        setPropertyTransparent(getActive(), prop, value);
    }

    //----------------------------------------------------------

    /**
     * In non-default configurations if prop is not set, then
     * this method sets it to a value taken from default config.
     * The result is transparent to getPropertyTransparent(), which
     * returns the same value before and after solidifyProperty() call.
     * 
     * @param config
     * @param prop
     * @return false if property had existed in config, true if it had been set by this method
     */
    public boolean solidifyProperty(String config, @NonNull String prop) {
        if(!isPropertySet(config, prop)) {
            if(config != null) {
                setPropertyImpl(config, prop, getDefaultProperty(prop));
            } else {
                setPropertyImpl(null, prop, ""); // NOI18N
            }
            return true;
        }
        return false;
    }

    /**
     * Solidifies all properties that are in any bounded group with the 
     * property prop
     * 
     * @param config
     * @param prop
     * @return false if nothing was solidified, true otherwise
     */
    private boolean solidifyBoundedGroups(String config, @NonNull String prop) {
        boolean solidified = false;
        for(String name : groups.getBoundedProperties(prop)) {
            solidified |= solidifyProperty(config, name);
        }
        return solidified;
    }

    //----------------------------------------------------------

    public void eraseProperty(String config, @NonNull String prop) {
        assert !configNameWrong(config);
        Map<String,String/*|null*/> configMap = getConfig(config);
        if(configMap != null) {
            configMap.remove(prop);
            configMap.keySet().removeAll(groups.getBoundedProperties(prop));
        }
    }

    public void eraseDefaultProperty(@NonNull String prop) {
        eraseProperty(null, prop);
    }

    public void eraseActiveProperty(@NonNull String prop) {
        eraseProperty(getActive(), prop);
    }

    /**
     * Returns true if property prop and all properties bounded to it
     * can be erased harmlessly, i.e., to ensure that getPropertyTransparent()
     * returns for each of them the same value before and after erasing
     * 
     * @param prop
     * @return 
     */
    private boolean isBoundedPropertiesEraseable(String config, String prop) {
        assert !configNameWrong(config);
        if(config == null) {
            return false;
        }
        boolean canErase = true;
        for(String name : groups.getBoundedProperties(prop)) {
            if((isPropertySet(config, name) && !JFXProjectProperties.isEqualText(getDefaultProperty(name), getProperty(config, name))) || STATIC_PROPERTIES.contains(name)) {
                canErase = false;
                break;
            }
        }
        return canErase;
    }
    
    //==========================================================
    // public proxies to access application parameters. May not cover
    // the whole of Multiproperty; add missing if needed
    
    /**
     * Proxy
     * @param config
     * @param name
     * @return 
     */
    public boolean hasParamTransparent(String config, @NonNull String name) {
        return appParams.hasEntryTransparent(config, name);
    }
    
    /**
     * Proxy
     * @param name
     * @return 
     */
    public boolean hasDefaultParamTransparent(@NonNull String name) {
        return appParams.hasDefaultEntryTransparent(name);
    }
    
    /**
     * Proxy
     * @param name
     * @return 
     */
    public boolean hasActiveParamTransparent(@NonNull String name) {
        return appParams.hasActiveEntryTransparent(name);
    }
    
    /**
     * Proxy
     * @param config
     * @param name
     * @param value
     * @return 
     */
    public boolean hasParamTransparent(String config, @NonNull String name, @NonNull String value) {
        return appParams.hasEntryTransparent(config, name, value);
    }
    
    /**
     * Proxy
     * @param name
     * @param value
     * @return 
     */
    public boolean hasDefaultParamTransparent(@NonNull String name, @NonNull String value) {
        return appParams.hasDefaultEntryTransparent(name, value);
    }
    
    /**
     * Proxy
     * @param name
     * @param value
     * @return 
     */
    public boolean hasActiveParamTransparent(@NonNull String name, @NonNull String value) {
        return appParams.hasActiveEntryTransparent(name, value);
    }
    
    /**
     * Proxy
     * @param config
     * @param name
     * @return 
     */
    public boolean hasParamValueTransparent(String config, @NonNull String name) {
        return appParams.hasEntryValueTransparent(config, name);
    }
    
    /**
     * Proxy
     * @param name
     * @return 
     */
    public boolean hasDefaultParamValueTransparent(@NonNull String name) {
        return appParams.hasDefaultEntryValueTransparent(name);
    }
    
    /**
     * Proxy
     * @param name
     * @return 
     */
    public boolean hasActiveParamValueTransparent(@NonNull String name) {
        return appParams.hasActiveEntryValueTransparent(name);
    }
    
    /**
     * Proxy
     * @param config
     * @param name
     * @return 
     */
    public Map<String, String> getParamTransparent(String config, @NonNull String name) {
        return appParams.getEntryTransparent(config, name);
    }
    
    /**
     * Proxy
     * @param name
     * @return 
     */
    public Map<String, String> getDefaultParamTransparent(@NonNull String name) {
        return appParams.getDefaultEntryTransparent(name);
    }

    /**
     * Proxy
     * @param name
     * @return 
     */
    public Map<String, String> getActiveParamTransparent(@NonNull String name) {
        return appParams.getActiveEntryTransparent(name);
    }
        
    /**
     * Proxy
     * @param config
     * @param name
     * @return 
     */
    public String getParamValueTransparent(String config, @NonNull String name) {
        return appParams.getEntryValueTransparent(config, name);
    }
    
    /**
     * Proxy
     * @param name
     * @return 
     */
    public String getDefaultParamValueTransparent(@NonNull String name) {
        return appParams.getDefaultEntryValueTransparent(name);
    }

    /**
     * Proxy
     * @param name
     * @return 
     */
    public String getActiveParamValueTransparent(@NonNull String name) {
        return appParams.getActiveEntryValueTransparent(name);
    }
    
    /**
     * Proxy
     * @param config
     * @return 
     */
    public List<Map<String,String/*|null*/>> getParamsTransparent(String config) {
        return appParams.getEntriesTransparent(config);
    }
    
    /**
     * Proxy
     * @return 
     */
    public List<Map<String,String/*|null*/>> getDefaultParamsTransparent() {
        return appParams.getDefaultEntriesTransparent();
    }
    
    /**
     * Proxy
     * @return 
     */
    public List<Map<String,String/*|null*/>> getActiveParamsTransparent() {
        return appParams.getActiveEntriesTransparent();
    }
    
    /**
     * Proxy
     * @param config
     * @param name 
     */
    public void addParamTransparent(String config, @NonNull String name){
        appParams.addEntryTransparent(config, name);
    }
    
    /**
     * Proxy
     * @param name 
     */
    public void addDefaultParamTransparent(@NonNull String name) {
        appParams.addDefaultEntryTransparent(name);
    }
    
    /**
     * Proxy
     * @param name 
     */
    public void addActiveParamTransparent(@NonNull String name) {
        appParams.addActiveEntryTransparent(name);
    }
    
    /**
     * Proxy
     * @param config
     * @param name
     * @param value 
     */
    public void addParamTransparent(String config, @NonNull String name, @NonNull String value) {
        appParams.addEntryTransparent(config, name, value);
    }
    
    /**
     * Proxy
     * @param name
     * @param value 
     */
    public void addDefaultParamTransparent(@NonNull String name, @NonNull String value) {
        appParams.addDefaultEntryTransparent(name, value);
    }

    /**
     * Proxy
     * @param name
     * @param value 
     */
    public void addActiveParamTransparent(@NonNull String name, @NonNull String value) {
        appParams.addActiveEntryTransparent(name, value);
    }

    /**
     * Proxy
     * @param config
     * @param params 
     */
    public void setParamsTransparent(String config, List<Map<String,String/*|null*/>>/*|null*/ params) {
        appParams.setEntriesTransparent(config, params);
    }

    /**
     * Proxy
     * @param params 
     */
    public void setDefaultParamsTransparent(List<Map<String,String/*|null*/>>/*|null*/ params) {
        appParams.setDefaultEntriesTransparent(params);
    }

    /**
     * Proxy
     * @param params 
     */
    public void setActiveParamsTransparent(List<Map<String,String/*|null*/>>/*|null*/ params) {
        appParams.setActiveEntriesTransparent(params);
    }

    /**
     * Proxy
     * @param config
     * @param name 
     */
    public void eraseParamTransparent(String config, @NonNull String name) {
        appParams.eraseEntryTransparent(config, name);
    }
    
    /**
     * Proxy
     * @param name 
     */
    public void eraseDefaultParamTransparent(@NonNull String name) {
        appParams.eraseDefaultEntryTransparent(name);
    }

    /**
     * Proxy
     * @param name 
     */
    public void eraseActiveParamTransparent(@NonNull String name) {
        appParams.eraseActiveEntryTransparent(name);
    }

    /**
     * Proxy
     * @param config 
     */
    public void eraseParamsTransparent(String config) {
        appParams.eraseEntriesTransparent(config);
    }
    
    /**
     * Proxy
     */
    public void eraseDefaultParamsTransparent() {
        appParams.eraseDefaultEntriesTransparent();
    }

    /**
     * Proxy
     */
    public void eraseActiveParamsTransparent() {
        appParams.eraseActiveEntriesTransparent();
    }

    /**
     * Proxy
     * @param config
     * @param commandLine
     * @return 
     */
    public String getParamsTransparentAsString(String config, boolean commandLine) {
        return appParams.getEntriesTransparentAsString(config, commandLine);
    }
    
    /**
     * Proxy
     * @param commandLine
     * @return 
     */
    public String getDefaultParamsTransparentAsString(boolean commandLine) {
        return appParams.getDefaultEntriesTransparentAsString(commandLine);
    }

    /**
     * Proxy
     * @param commandLine
     * @return 
     */
    public String getActiveParamsTransparentAsString(boolean commandLine) {
        return appParams.getActiveEntriesTransparentAsString(commandLine);
    }

    //----------------------------------------------------------
    // primarily for testing purposes

    public boolean hasActiveParam(@NonNull String name) {
        return appParams.hasActiveEntry(name);
    }
    
    public String paramsToString() {
        return appParams.toString();
    }
    
    public String getParamsAsString(String config, boolean commandLine) {
        return appParams.getEntriesAsString(config, commandLine);
    }
    
    public String getDefaultParamsAsString(boolean commandLine) {
        return appParams.getDefaultEntriesAsString(commandLine);
    }
    
    public String getActiveParamsAsString(boolean commandLine) {
        return appParams.getActiveEntriesAsString(commandLine);
    }


    //==========================================================
    // public proxies to access custom manifest entries. May not cover
    // the whole of Multiproperty; add missing if needed
    
    /**
     * Proxy
     * @param config
     * @param name
     * @return 
     */
    public boolean hasManifestEntryTransparent(String config, @NonNull String name) {
        return appManifestEntries.hasEntryTransparent(config, name);
    }
    
    /**
     * Proxy
     * @param name
     * @return 
     */
    public boolean hasDefaultManifestEntryTransparent(@NonNull String name) {
        return appManifestEntries.hasDefaultEntryTransparent(name);
    }
    
    /**
     * Proxy
     * @param name
     * @return 
     */
    public boolean hasActiveManifestEntryTransparent(@NonNull String name) {
        return appManifestEntries.hasActiveEntryTransparent(name);
    }
    
    /**
     * Proxy
     * @param config
     * @param name
     * @param value
     * @return 
     */
    public boolean hasManifestEntryTransparent(String config, @NonNull String name, @NonNull String value) {
        return appManifestEntries.hasEntryTransparent(config, name, value);
    }
    
    /**
     * Proxy
     * @param name
     * @param value
     * @return 
     */
    public boolean hasDefaultManifestEntryTransparent(@NonNull String name, @NonNull String value) {
        return appManifestEntries.hasDefaultEntryTransparent(name, value);
    }
    
    /**
     * Proxy
     * @param name
     * @param value
     * @return 
     */
    public boolean hasActiveManifestEntryTransparent(@NonNull String name, @NonNull String value) {
        return appManifestEntries.hasActiveEntryTransparent(name, value);
    }
    
    /**
     * Proxy
     * @param config
     * @param name
     * @return 
     */
    public boolean hasManifestEntryValueTransparent(String config, @NonNull String name) {
        return appManifestEntries.hasEntryValueTransparent(config, name);
    }
    
    /**
     * Proxy
     * @param name
     * @return 
     */
    public boolean hasDefaultManifestEntryValueTransparent(@NonNull String name) {
        return appManifestEntries.hasDefaultEntryValueTransparent(name);
    }
    
    /**
     * Proxy
     * @param name
     * @return 
     */
    public boolean hasActiveManifestEntryValueTransparent(@NonNull String name) {
        return appManifestEntries.hasActiveEntryValueTransparent(name);
    }
    
    /**
     * Proxy
     * @param config
     * @param name
     * @return 
     */
    public Map<String, String> getManifestEntryTransparent(String config, @NonNull String name) {
        return appManifestEntries.getEntryTransparent(config, name);
    }
    
    /**
     * Proxy
     * @param name
     * @return 
     */
    public Map<String, String> getDefaultManifestEntryTransparent(@NonNull String name) {
        return appManifestEntries.getDefaultEntryTransparent(name);
    }

    /**
     * Proxy
     * @param name
     * @return 
     */
    public Map<String, String> getActiveManifestEntryTransparent(@NonNull String name) {
        return appManifestEntries.getActiveEntryTransparent(name);
    }
        
    /**
     * Proxy
     * @param config
     * @param name
     * @return 
     */
    public String getManifestEntryValueTransparent(String config, @NonNull String name) {
        return appManifestEntries.getEntryValueTransparent(config, name);
    }
    
    /**
     * Proxy
     * @param name
     * @return 
     */
    public String getDefaultManifestEntryValueTransparent(@NonNull String name) {
        return appManifestEntries.getDefaultEntryValueTransparent(name);
    }

    /**
     * Proxy
     * @param name
     * @return 
     */
    public String getActiveManifestEntryValueTransparent(@NonNull String name) {
        return appManifestEntries.getActiveEntryValueTransparent(name);
    }
    
    /**
     * Proxy
     * @param config
     * @return 
     */
    public List<Map<String,String/*|null*/>> getManifestEntriesTransparent(String config) {
        return appManifestEntries.getEntriesTransparent(config);
    }
    
    /**
     * Proxy
     * @return 
     */
    public List<Map<String,String/*|null*/>> getDefaultManifestEntriesTransparent() {
        return appManifestEntries.getDefaultEntriesTransparent();
    }
    
    /**
     * Proxy
     * @return 
     */
    public List<Map<String,String/*|null*/>> getActiveManifestEntriesTransparent() {
        return appManifestEntries.getActiveEntriesTransparent();
    }
    
    /**
     * Proxy
     * @param config
     * @param name 
     */
    public void addManifestEntryTransparent(String config, @NonNull String name){
        appManifestEntries.addEntryTransparent(config, name);
    }
    
    /**
     * Proxy
     * @param name 
     */
    public void addDefaultManifestEntryTransparent(@NonNull String name) {
        appManifestEntries.addDefaultEntryTransparent(name);
    }
    
    /**
     * Proxy
     * @param name 
     */
    public void addActiveManifestEntryTransparent(@NonNull String name) {
        appManifestEntries.addActiveEntryTransparent(name);
    }
    
    /**
     * Proxy
     * @param config
     * @param name
     * @param value 
     */
    public void addManifestEntryTransparent(String config, @NonNull String name, @NonNull String value) {
        appManifestEntries.addEntryTransparent(config, name, value);
    }
    
    /**
     * Proxy
     * @param name
     * @param value 
     */
    public void addDefaultManifestEntryTransparent(@NonNull String name, @NonNull String value) {
        appManifestEntries.addDefaultEntryTransparent(name, value);
    }

    /**
     * Proxy
     * @param name
     * @param value 
     */
    public void addActiveManifestEntryTransparent(@NonNull String name, @NonNull String value) {
        appManifestEntries.addActiveEntryTransparent(name, value);
    }

    /**
     * Proxy
     * @param config
     * @param params 
     */
    public void setManifestEntriesTransparent(String config, List<Map<String,String/*|null*/>>/*|null*/ params) {
        appManifestEntries.setEntriesTransparent(config, params);
    }

    /**
     * Proxy
     * @param params 
     */
    public void setDefaultManifestEntriesTransparent(List<Map<String,String/*|null*/>>/*|null*/ params) {
        appManifestEntries.setDefaultEntriesTransparent(params);
    }

    /**
     * Proxy
     * @param params 
     */
    public void setActiveManifestEntriesTransparent(List<Map<String,String/*|null*/>>/*|null*/ params) {
        appManifestEntries.setActiveEntriesTransparent(params);
    }

    /**
     * Proxy
     * @param config
     * @param name 
     */
    public void eraseManifestEntryTransparent(String config, @NonNull String name) {
        appManifestEntries.eraseEntryTransparent(config, name);
    }
    
    /**
     * Proxy
     * @param name 
     */
    public void eraseDefaultManifestEntryTransparent(@NonNull String name) {
        appManifestEntries.eraseDefaultEntryTransparent(name);
    }

    /**
     * Proxy
     * @param name 
     */
    public void eraseActiveManifestEntryTransparent(@NonNull String name) {
        appManifestEntries.eraseActiveEntryTransparent(name);
    }

    /**
     * Proxy
     * @param config 
     */
    public void eraseManifestEntriesTransparent(String config) {
        appManifestEntries.eraseEntriesTransparent(config);
    }
    
    /**
     * Proxy
     */
    public void eraseDefaultManifestEntriesTransparent() {
        appManifestEntries.eraseDefaultEntriesTransparent();
    }

    /**
     * Proxy
     */
    public void eraseActiveManifestEntriesTransparent() {
        appManifestEntries.eraseActiveEntriesTransparent();
    }

    /**
     * Proxy
     * @param config
     * @return 
     */
    public String getManifestEntriesTransparentAsString(String config) {
        return appManifestEntries.getEntriesTransparentAsString(config, false);
    }
    
    /**
     * Proxy
     * @return 
     */
    public String getDefaultManifestEntriesTransparentAsString() {
        return appManifestEntries.getDefaultEntriesTransparentAsString(false);
    }

    /**
     * Proxy
     * @return 
     */
    public String getActiveManifestEntriesTransparentAsString() {
        return appManifestEntries.getActiveEntriesTransparentAsString(false);
    }

    //----------------------------------------------------------
    // primarily for testing purposes

    public boolean hasActiveManifestEntry(@NonNull String name) {
        return appManifestEntries.hasActiveEntry(name);
    }
    
    public String manifestEntriesToString() {
        return appManifestEntries.toString();
    }
    
    public int getNoOfManifestEntries(String config) {
        return appManifestEntries.getNoOfEntries(config);
    }
    
    public int getNoOfDefaultManifestEntries() {
        return appManifestEntries.getNoOfDefaultEntries();
    }
    
    public int getNoOfActiveManifestEntries() {
        return appManifestEntries.getNoOfActiveEntries();
    }


    //==========================================================

    /**
     * Reads configuration properties from project properties files
     * (modified from "A mess." from J2SEProjectProperties)"
     */
    public void read() {
    //Map<String/*|null*/,Map<String,String>> readRunConfigs() {
        reset();
        // read project properties
        readDefaultConfig(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        // overwrite by project private properties
        readDefaultConfig(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        // set properties that were not set but should have a value
        addDefaultsIfMissing();
        // add project properties read from config files
        readNonDefaultConfigs(PROJECT_CONFIGS_DIR, true);
        // add/overwrite project properties read from private config files
        readNonDefaultConfigs(PROJECT_PRIVATE_CONFIGS_DIR, false);
    }

    private void readDefaultConfig(String propsFile) {
        EditableProperties ep = null;
        try {
            ep = JFXProjectUtils.readFromFile(projectDir, propsFile);
        } catch (IOException ex) {
            // can be ignored
        }
        if(ep != null) {
            for (String prop : PROJECT_PROPERTIES) {
                String v = ep.getProperty(prop);
                if (v != null) {
                    setDefaultProperty(prop, v);
                }
            }
        }
        appParams.extractDefaultEntries(ep);
        appManifestEntries.extractDefaultEntries(ep);
    }

    private void addDefaultsIfMissing() {
        for(String prop : DEFAULT_IF_MISSING.keySet()) {
            if(!isDefaultPropertySet(prop)) {
                setDefaultProperty(prop, DEFAULT_IF_MISSING.get(prop));
            }
        }
    }

    private void readNonDefaultConfigs(String subDir, boolean createIfNotExists) {
        FileObject configsFO = projectDir.getFileObject(subDir); // NOI18N
        if (configsFO != null) {
            for (FileObject kid : configsFO.getChildren()) {
                if (!kid.hasExt(PROPERTIES_FILE_EXT)) { // NOI18N
                    continue;
                }
                Map<String,String> c = getConfig(kid.getName());
                if (c == null && !createIfNotExists) {
                    continue;
                }
                EditableProperties cep = null;
                try {
                    cep = JFXProjectUtils.readFromFile( kid );
                } catch (IOException ex) {
                    // can be ignored
                }
                addToConfig(kid.getName(), cep);
                appParams.extractEntries(cep, kid.getName());
                appManifestEntries.extractEntries(cep, kid.getName());
            }
        }
    }

    //----------------------------------------------------------

    public void readActive() {
        try {
            setActive(JFXProjectUtils.readFromFile(projectDir, CONFIG_PROPERTIES_FILE).getProperty(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG));
        } catch(IOException e) {
            LOG.log(Level.WARNING, "Failed to read active configuration from {0}.", CONFIG_PROPERTIES_FILE); // NOI18N
        }
    }

    public void storeActive() throws IOException {
        String configPath = CONFIG_PROPERTIES_FILE;
        if (active == null) {
            try {
                JFXProjectUtils.deleteFile(projectDir, configPath);
            } catch (IOException ex) {
            }
        } else {
            final EditableProperties configProps = JFXProjectUtils.readFromFile(projectDir, configPath);
            configProps.setProperty(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG, active);
            JFXProjectUtils.saveToFile(projectDir, configPath, configProps);
        }
    }

    //----------------------------------------------------------

    /**
    * Gathers application parameters to one property APPLICATION_ARGS
    * to be passed to run/debug target in build-impl.xml when Run as Standalone
    * 
    * @param config
    * @param ep editable properties to which to store the generated property
    * @return true if properties have been edited
    */
    private boolean storeParamsAsCommandLine(String config, EditableProperties projectProperties) {
        assert !configNameWrong(config);
        String params = appParams.getEntriesTransparentAsString(config, true, true);
        if(config != null) {
            if(JFXProjectProperties.isEqualText(params, appParams.getDefaultEntriesAsString(true, true))) {
                params = null;
            }
        }
        if (!Utilities.compareObjects(params, projectProperties.getProperty(APPLICATION_ARGS))) {
            if (params != null && params.length() > 0) {
                projectProperties.setProperty(APPLICATION_ARGS, params);
                projectProperties.setComment(APPLICATION_ARGS, new String[]{"# " + NbBundle.getMessage(JFXProjectConfigurations.class, "COMMENT_app_args")}, false); // NOI18N
            } else {
                projectProperties.remove(APPLICATION_ARGS);
            }
            return true;
        }
        return false;
    }

    private boolean storeDefaultParamsAsCommandLine(EditableProperties projectProperties) {
        return storeParamsAsCommandLine(null, projectProperties);
    }

    /**
     * Stores/updates configuration properties and parameters to EditableProperties in case of default
     * config, or directly to project properties files in case of non-default configs.
     * (modified from "A royal mess." from J2SEProjectProperties)"
     */
    public void store(EditableProperties projectProperties, EditableProperties privateProperties) throws IOException {

        for (String name : PROJECT_PROPERTIES) {
            String value = getDefaultProperty(name);
            updateProperty(name, value, projectProperties, privateProperties, isBoundedToNonemptyProperty(null, name));
        }
        List<String> paramNamesUsed = new ArrayList<String>();
        List<String> manifestEntryNamesUsed = new ArrayList<String>();
        appParams.updateDefaultEntryProperties(projectProperties, privateProperties, paramNamesUsed);
        appManifestEntries.updateDefaultEntryProperties(projectProperties, privateProperties, manifestEntryNamesUsed);
        storeDefaultParamsAsCommandLine(privateProperties);

        for (Map.Entry<String,Map<String,String>> entry : RUN_CONFIGS.entrySet()) {
            String config = entry.getKey();
            if (config == null) {
                continue;
            }
            String sharedPath = getSharedConfigFilePath(config);
            String privatePath = getPrivateConfigFilePath(config);
            Map<String,String> configProps = entry.getValue();
            if (configProps == null) {
                try {
                    JFXProjectUtils.deleteFile(projectDir, sharedPath);
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, "Failed to delete file: {0}", sharedPath); // NOI18N
                }
                try {
                    JFXProjectUtils.deleteFile(projectDir, privatePath);
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, "Failed to delete file: {0}", privatePath); // NOI18N
                }
                continue;
            }
            final EditableProperties sharedCfgProps = JFXProjectUtils.readFromFile(projectDir, sharedPath);
            final EditableProperties privateCfgProps = JFXProjectUtils.readFromFile(projectDir, privatePath);
            boolean privatePropsChanged = false;

            for (Map.Entry<String,String> prop : configProps.entrySet()) {
                String name = prop.getKey();
                String value = prop.getValue();
                String defaultValue = getDefaultProperty(name);
                boolean storeIfEmpty = (defaultValue != null && defaultValue.length() > 0) || isBoundedToNonemptyProperty(config, name);
                privatePropsChanged |= updateProperty(name, value, sharedCfgProps, privateCfgProps, storeIfEmpty);
            }

            cleanPropertiesIfEmpty(CLEAN_EMPTY_PROJECT_PROPERTIES.toArray(new String[0]), 
                    config, sharedCfgProps);
            privatePropsChanged |= cleanPropertiesIfEmpty(CLEAN_EMPTY_PRIVATE_PROPERTIES.toArray(new String[0]), 
                    config, privateCfgProps);
            privatePropsChanged |= appParams.updateEntryProperties(config, sharedCfgProps, privateCfgProps, paramNamesUsed);
            privatePropsChanged |= appManifestEntries.updateEntryProperties(config, sharedCfgProps, privateCfgProps, manifestEntryNamesUsed);
            privatePropsChanged |= storeParamsAsCommandLine(config, privateCfgProps);

            JFXProjectUtils.saveToFile(projectDir, sharedPath, sharedCfgProps);    //Make sure the definition file is always created, even if it is empty.
            if (privatePropsChanged) {                              //Definition file is written, only when changed
                JFXProjectUtils.saveToFile(projectDir, privatePath, privateCfgProps);
            }
        }
        if(ERASED_CONFIGS != null) {
            for (String entry : ERASED_CONFIGS) {
                if(!RUN_CONFIGS.containsKey(entry)) {
                    // config has been erased, and has not been recreated
                    String sharedPath = getSharedConfigFilePath(entry);
                    String privatePath = getPrivateConfigFilePath(entry);
                    try {
                        JFXProjectUtils.deleteFile(projectDir, sharedPath);
                    } catch (IOException ex) {
                        LOG.log(Level.WARNING, "Failed to delete file: {0}", sharedPath); // NOI18N
                    }
                    try {
                        JFXProjectUtils.deleteFile(projectDir, privatePath);
                    } catch (IOException ex) {
                        LOG.log(Level.WARNING, "Failed to delete file: {0}", privatePath); // NOI18N
                    }
                }
            }
        }
    }

    //----------------------------------------------------------

    /**
    * Updates the value of existing property in editable properties if value differs.
    * If value is not set or is set empty, removes property from editable properties
    * unless storeEmpty==true, in which case the property is preserved and set to empty
    * in editable properties.
    * 
    * @param name property to be updated
    * @param value new property value
    * @param projectProperties project editable properties
    * @param privateProperties private project editable properties
    * @param storeEmpty true==keep empty properties in editable properties, false==remove empty properties
    * @return true if private properties have been edited
    */
    private boolean updateProperty(@NonNull String name, String value, @NonNull EditableProperties projectProperties, @NonNull EditableProperties privateProperties, boolean storeEmpty) {
        boolean changePrivate = PRIVATE_PROPERTIES.contains(name) || privateProperties.containsKey(name);
        EditableProperties ep = changePrivate ? privateProperties : projectProperties;
        if(changePrivate) {
            projectProperties.remove(name);
        }
        if (!Utilities.compareObjects(value, ep.getProperty(name))) {
            if (value != null && (value.length() > 0 || storeEmpty)) {
                ep.setProperty(name, value);
            } else {
                ep.remove(name);
            }
            return changePrivate;
        }
        return false;
    }

    /**
    * Updates the value of existing property in editable properties if value differs.
    * If value is not set or is set empty, removes property from editable properties.
    *
    * @param name property to be updated
    * @param value new property value
    * @param projectProperties project editable properties
    * @param privateProperties private project editable properties
    */
    private boolean updateProperty(@NonNull String name, String value, @NonNull EditableProperties projectProperties, @NonNull EditableProperties privateProperties) {
        return updateProperty(name, value, projectProperties, privateProperties, false);
    }

    /**
     * If property not present in config configuration, remove it from editable properties.
     * This is to propagate property deletions in config to property files
     * @param name
     * @param config
     * @param ep
     * @return true if properties have been edited
     */
    private boolean cleanPropertyIfEmpty(@NonNull String name, String config, @NonNull EditableProperties ep) {
        if(!isPropertySet(config, name)) {
            ep.remove(name);
            return true;
        }
        return false;
    }

    private boolean cleanPropertiesIfEmpty(@NonNull String[] names, String config, @NonNull EditableProperties ep) {
        boolean updated = false;
        for(String name : names) {
            updated |= cleanPropertyIfEmpty(name, config, ep);
        }
        return updated;
    }

    //----------------------------------------------------------

    /**
     * For properties registered in bounded groups special
     * handling is to be followed. Either all bounded properties
     * must exist or none of bounded properties must exist
     * in project configuration. The motivation is to enable
     * treating all Preloader related properties is one pseudo-property
     */
    private class BoundedPropertyGroups {

        Map<String, Set<String>> groups = new HashMap<String, Set<String>>();

        public void defineGroup(String groupName, Collection<String> props) {
            Set<String> group = new HashSet<String>();
            group.addAll(props);
            groups.put(groupName, group);
        }

        public void clearGroup(String groupName) {
            groups.remove(groupName);
        }

        public void clearAllGroups() {
            groups.clear();
        }

        /**
         * Returns true if property prop is bound with any other properties
         * @return 
         */
        public boolean isBound(String prop) {
            for(Map.Entry<String, Set<String>> entry : groups.entrySet()) {
                Set<String> group = entry.getValue();
                if(group != null && group.contains(prop) && group.size() > 1) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Returns collection of all properties from any group of which
         * property prop is member. prop is not included in result.
         * @param prop
         * @return 
         */
        public Collection<String> getBoundedProperties(String prop) {
            Set<String> bounded = new HashSet<String>();
            for(Map.Entry<String, Set<String>> entry : groups.entrySet()) {
                Set<String> group = entry.getValue();
                if(group != null && group.contains(prop)) {
                    bounded.addAll(group);
                }
            }
            bounded.remove(prop);
            return bounded;
        }
    }

    //----------------------------------------------------------

    /**
     * Project properties maintenance class. Handles properties that may have multiple
     * instances, like FX Application parameters, or custom manifest entries
     */
    private final class MultiProperty {
    
        private Map<String/*|null*/,List<Map<String,String/*|null*/>>/*|null*/> APP_MULTIPROPS;
        private String prefix;
        private String suffixes[];
        private String connectSign;

        public MultiProperty(@NonNull String prefix, @NonNull String suffixes[], @NonNull String connectSign) {
            assert suffixes.length == 3; // need "name" and "value" and "hidden"
            this.prefix = prefix;
            this.suffixes = suffixes;
            this.connectSign = connectSign;
            reset();
        }
        
        public void reset() {
            APP_MULTIPROPS = new TreeMap<String,List<Map<String,String>>>(getComparator());
        }


        //==========================================================
        
        /**
         * Returns true if the key "hidden" is set to "true" in this entry.
         * Hiding is used to record entry deletion (e.g., entry or custom manifest entry)
         * in non-default configuration while keeping the entry in default config
         * 
         * @param entry
         * @return 
         */
        private boolean isEntryHidden(Map<String,String> entry) {
            if(entry != null && JFXProjectProperties.isTrue(entry.get(suffixes[2]))) {
                return true;
            }
            return false;
        }
        
        private boolean isEntryHidden(String config, @NonNull String name) {
            Map<String, String> entry = getEntry(config, name);
            if(entry != null) {
                return isEntryHidden(entry);
            }
            return false;
        }
        
        private void hideEntry(@NonNull Map<String,String> entry) {
            assert entry != null;
            entry.put(suffixes[2], APP_MULTIPROP_HIDDEN_TRUE); // NOI18N
        }
        
        private void unhideEntry(@NonNull Map<String,String> entry) {
            assert entry != null;
            entry.remove(suffixes[2]);
        }

        private void hideEntry(String config, String name) {
            Map<String, String> map = getEntry(config, name);
            if(map != null) {
                hideEntry(map);
            }
        }
        
        private void unhideEntry(String config, String name) {
            Map<String, String> map = getEntry(config, name);
            if(map != null) {
                unhideEntry(map);
            }
        }

        /**
         * Removes those entries from list that are marked as hidden
         * 
         * @param entries
         * @return filtered list
         */
        private List<Map<String,String/*|null*/>> removeHiddenEntries(@NonNull List<Map<String,String/*|null*/>> entries) {
            List<Map<String,String/*|null*/>> filtered = new ArrayList<Map<String,String>>();
            if(entries != null) {
                for(Map<String,String> map : entries) {
                    if(!isEntryHidden(map)) {
                        filtered.add(map);
                    }
                }
            }
            return filtered;
        }
        
        
        //==========================================================

        private String getEntryName(Map<String,String> entry) {
            return entry != null ? entry.get(suffixes[0]) : null;
        }

        private String getEntryValue(Map<String,String> entry) {
            return entry != null ? entry.get(suffixes[1]) : null;
        }
        
        private void setEntryName(Map<String,String> entry, String name) {
            if(entry != null) {
                entry.put(suffixes[0], name);
            }
        }

        private void setEntryValue(Map<String,String> entry, String value) {
            if(entry != null) {
                entry.put(suffixes[1], value);
            }
        }
        
        //==========================================================

        /**
         * Returns true if entry named name is present in configuration
         * config in any form - with value or without value
         * @param config
         * @param name
         * @return 
         */
        private boolean hasEntry(String config, @NonNull String name) {
            assert !configNameWrong(config);
            return getEntry(config, name) != null;
        }

        private boolean hasDefaultEntry(@NonNull String name) {
            return hasEntry(null, name);
        }

        private boolean hasActiveEntry(@NonNull String name) {
            return hasEntry(getActive(), name);
        }

        public boolean hasEntryTransparent(String config, @NonNull String name) {
            assert !configNameWrong(config);
            return getEntryTransparent(config, name) != null;
        }

        public boolean hasDefaultEntryTransparent(@NonNull String name) {
            return hasEntryTransparent((String)null, name);
        }

        public boolean hasActiveEntryTransparent(@NonNull String name) {
            return hasEntryTransparent(getActive(), name);
        }

        //----------------------------------------------------------

        /**
         * Returns true if exactly the entry with name name and value value
         * is present in configuration config
         * 
         * @param config
         * @param name
         * @param value
         * @return 
         */
        private boolean hasEntry(String config, @NonNull String name, @NonNull String value) {
            assert !configNameWrong(config);
            String v = getEntryValue(config, name);
            return JFXProjectProperties.isEqualText(v, value);
        }

        private boolean hasDefaultEntry(@NonNull String name, @NonNull String value) {
            return hasEntry(null, name, value);
        }

        private boolean hasActiveEntry(@NonNull String name, @NonNull String value) {
            return hasEntry(getActive(), name, value);
        }

        public boolean hasEntryTransparent(String config, @NonNull String name, @NonNull String value) {
            assert !configNameWrong(config);
            String v = getEntryValueTransparent(config, name);
            return JFXProjectProperties.isEqualText(v, value);
        }
        
        public boolean hasDefaultEntryTransparent(@NonNull String name, @NonNull String value) {
            return hasEntryTransparent((String)null, name, value);
        }

        public boolean hasActiveEntryTransparent(@NonNull String name, @NonNull String value) {
            return hasEntryTransparent(getActive(), name, value);
        }

        //----------------------------------------------------------
        // note that these do not search for concrete value, they
        // search for entry named name and ask whether such
        // entry has any value
        
        private boolean hasEntryValue(Map<String,String> entry) {
            return entry != null && entry.containsKey(suffixes[1]);
        }
        
        private boolean hasEntryValue(String config, @NonNull String name) {
            assert !configNameWrong(config);
            Map<String, String> entry = getEntry(config, name);
            return hasEntryValue(entry);
        }

        private boolean hasDefaultEntryValue(@NonNull String name) {
            return hasEntryValue(null, name);
        }

        private boolean hasActiveEntryValue(@NonNull String name) {
            return hasEntryValue(getActive(), name);
        }

        public boolean hasEntryValueTransparent(String config, @NonNull String name) {
            assert !configNameWrong(config);
            Map<String, String> entry = getEntry(config, name);
            if(config != null && entry == null) {
                return hasEntryValueTransparent((String)null, name);
            }
            return isEntryHidden(entry) ? false : hasEntryValue(entry);
        }

        public boolean hasDefaultEntryValueTransparent(@NonNull String name) {
            return hasEntryValueTransparent((String)null, name);
        }

        public boolean hasActiveEntryValueTransparent(@NonNull String name) {
            return hasEntryValueTransparent(getActive(), name);
        }

        //----------------------------------------------------------

        /**
         * Returns entry as map if exists in configuration config, null otherwise
         * 
         * @param config
         * @param name
         * @return 
         */
        private Map<String, String> getEntry(String config, @NonNull String name) {
            assert !configNameWrong(config);
            return getEntry(getEntries(config), name);
        }

        private Map<String, String> getDefaultEntry(@NonNull String name) {
            return getEntry((String)null, name);
        }

        private Map<String, String> getActiveEntry(@NonNull String name) {
            return getEntry(getActive(), name);
        }

        public Map<String, String> getEntryTransparent(String config, @NonNull String name) {
            assert !configNameWrong(config);
            Map<String, String> entry = getEntry(config, name);
            if(config != null && entry == null) {
                return getEntryTransparent((String)null, name);
            }
            return isEntryHidden(entry) ? null : entry;
        }

        public Map<String, String> getDefaultEntryTransparent(@NonNull String name) {
            return getEntryTransparent((String)null, name);
        }

        public Map<String, String> getActiveEntryTransparent(@NonNull String name) {
            return getEntryTransparent(getActive(), name);
        }

        //----------------------------------------------------------

        /**
         * Note that returned null is ambiguous - may mean that there
         * was no value defined or that it was defined and its value was null.
         * To check this ask has*EntryValue*()
         * 
         * @param config
         * @param name
         * @return 
         */
        private String getEntryValue(String config, @NonNull String name) {
            assert !configNameWrong(config);
            Map<String,String/*|null*/> entry = getEntry(config, name);
            if(entry != null) {
                return getEntryValue(entry);
            }
            return null;
        }

        /**
         * Note that returned null is ambiguous - may mean that there
         * was no value defined or that it was defined and its value was null.
         * To check this ask has*EntryValue*()
         * 
         * @param name
         * @return 
         */
        private String getDefaultEntryValue(@NonNull String name) {
            return getEntryValue((String)null, name);
        }

        /**
         * Note that returned null is ambiguous - may mean that there
         * was no value defined or that it was defined and its value was null.
         * To check this ask has*EntryValue*()
         * 
         * @param name
         * @return 
         */
        private String getActiveEntryValue(@NonNull String name) {
            return getEntryValue(getActive(), name);
        }

        /**
         * Note that returned null is ambiguous - may mean that there
         * was no value defined or that it was defined and its value was null.
         * To check this ask has*EntryValue*()
         * 
         * @param config
         * @param name
         * @return 
         */
        public String getEntryValueTransparent(String config, @NonNull String name) {
            assert !configNameWrong(config);
            Map<String, String> entry = getEntry(config, name);
            if(config != null && entry == null) {
                return getEntryValueTransparent((String)null, name);
            }
            return isEntryHidden(entry) ? null : getEntryValue(entry);
        }

        /**
         * Note that returned null is ambiguous - may mean that there
         * was no value defined or that it was defined and its value was null.
         * To check this ask has*EntryValue*()
         * 
         * @param name
         * @return 
         */
        public String getDefaultEntryValueTransparent(@NonNull String name) {
            return getEntryValueTransparent((String)null, name);
        }

        /**
         * Note that returned null is ambiguous - may mean that there
         * was no value defined or that it was defined and its value was null.
         * To check this ask has*EntryValue*()
         * 
         * @param name
         * @return 
         */
        public String getActiveEntryValueTransparent(@NonNull String name) {
            return getEntryValueTransparent(getActive(), name);
        }

        //----------------------------------------------------------

        private @NonNull List<Map<String,String/*|null*/>> getEntries(String config) {
            assert !configNameWrong(config);
            return APP_MULTIPROPS.get(config);
        }

        private List<Map<String,String/*|null*/>> getDefaultEntries() {
            return APP_MULTIPROPS.get(null);
        }

        private List<Map<String,String/*|null*/>> getActiveEntries() {
            return APP_MULTIPROPS.get(getActive());
        }

        /**
        * Returns (copy of) list of default entries if config==default or
        * union of default config and current config entries otherwise
        * (excludes hidden entries)
        * 
        * @param config current config
        * @return union of default and current entries
        */
        public List<Map<String,String/*|null*/>> getEntriesTransparent(String config) {
            assert !configNameWrong(config);
            if(config == null) {
                return removeHiddenEntries(getDefaultEntries());
            }
            List<Map<String,String/*|null*/>> union = new ArrayList<Map<String,String>>();
            // create marker set - identify entries to be added from default and current configs
            Set<String> markerDefault = new HashSet<String>();
            Set<String> markerConfig = new HashSet<String>();
            List<Map<String,String/*|null*/>> defaultEntries = getDefaultEntries();
            if(defaultEntries != null) {
                for(Map<String,String> map : defaultEntries) {
                    if(!isEntryHidden(map)) {
                        markerDefault.add(getEntryName(map));
                    }
                }
            }
            List<Map<String,String/*|null*/>> configEntries = getEntries(config);
            if(configEntries != null) {
                for(Map<String,String> map : configEntries) {
                    if(!isEntryHidden(map)) {
                        markerConfig.add(getEntryName(map));
                    } else {
                        markerDefault.remove(getEntryName(map));
                    }
                }
            }
            // copy entries from default and current config based on marker
            if(defaultEntries != null) {
                for(Map<String,String> map : defaultEntries) {
                    String name = getEntryName(map);
                    if(markerDefault.contains(name) && !markerConfig.contains(name)) {
                        union.add(createEntry(name, getEntryValue(map)));
                    }
                }
            }
            if(configEntries != null) {
                for(Map<String,String> map : configEntries) {
                    String name = getEntryName(map);
                    if(markerConfig.contains(name)) {
                        union.add(createEntry(name, getEntryValue(map)));
                    }
                }
            }
            return union;
        }
        
        public List<Map<String,String/*|null*/>> getDefaultEntriesTransparent() {
            return getEntriesTransparent((String)null);
        }

        public List<Map<String,String/*|null*/>> getActiveEntriesTransparent() {
            return getEntriesTransparent(getActive());
        }

        //----------------------------------------------------------

        /**
        * Gathers all entries applicable to config configuration to one String
        * 
        * @param commandLine if true, formats output as if to be passed on command line, otherwise prouces comma separated list
        * @return a String containing all entries as if passed as command line parameters
        */
        private String getEntriesAsString(String config, boolean commandLine) {
            return getEntriesAsString(getEntries(config), commandLine);
        }

        private String getDefaultEntriesAsString(boolean commandLine, boolean quoteEntries) {
            return getEntriesAsString(getDefaultEntries(), commandLine, quoteEntries);
        }

        private String getDefaultEntriesAsString(boolean commandLine) {
            return getEntriesAsString(getDefaultEntries(), commandLine);
        }

        private String getActiveEntriesAsString(boolean commandLine) {
            return getEntriesAsString(getActiveEntries(), commandLine);
        }

        public String getEntriesTransparentAsString(String config, boolean commandLine) {
            assert !configNameWrong(config);
            return getEntriesAsString(getEntriesTransparent(config), commandLine);
        }

        public String getEntriesTransparentAsString(String config, boolean commandLine, boolean quoteParams) {
            assert !configNameWrong(config);
            return getEntriesAsString(getEntriesTransparent(config), commandLine, quoteParams);
        }

        public String getDefaultEntriesTransparentAsString(boolean commandLine) {
            return getEntriesAsString(getDefaultEntriesTransparent(), commandLine);
        }

        public String getActiveEntriesTransparentAsString(boolean commandLine) {
            return getEntriesAsString(getActiveEntriesTransparent(), commandLine);
        }

        private String getEntriesAsString(List<Map<String,String/*|null*/>> props, boolean commandLine) {
            return getEntriesAsString(props, commandLine, false);
        }

        private String getEntriesAsString(List<Map<String,String/*|null*/>> props, boolean commandLine, boolean quoteParams) {
            StringBuilder sb = new StringBuilder();
            if(props != null) {
                int index = 0;
                for(Map<String,String> m : props) {
                    String name = getEntryName(m);
                    String value = getEntryValue(m);
                    if(name != null && name.length() > 0 && !isEntryHidden(m)) {
                        if(sb.length() > 0) {
                            if(!commandLine) {
                                sb.append(","); // NOI18N
                            }
                            sb.append(" "); // NOI18N
                        }
                        if(value != null && value.length() > 0) {
                            if(commandLine) {
                                sb.append("--"); // NOI18N
                            }
                            if (quoteParams) {
                                sb.append("'").append(name).append("'");
                            } else {
                                sb.append(name);
                            }
                            if(commandLine) {
                                sb.append("="); // NOI18N
                            } else {
                                sb.append(connectSign); // NOI18N
                            }
                            if (quoteParams) {
                                sb.append("'").append(value).append("'");
                            } else {
                                sb.append(value);
                            }
                        } else {
                            if (quoteParams) {
                                sb.append("'").append(name).append("'");
                            } else {
                                sb.append(name);
                            }
                        }
                        index++;
                    }
                }
            }
            return sb.toString();
        }

        public int getNoOfEntries(String config) {
            assert !configNameWrong(config);
            return getNoOfEntries(getEntriesTransparent(config));
        }

        public int getNoOfDefaultEntries() {
            return getNoOfEntries(getDefaultEntriesTransparent());
        }

        public int getNoOfActiveEntries() {
            return getNoOfEntries(getActiveEntriesTransparent());
        }

        private int getNoOfEntries(List<Map<String,String/*|null*/>> props)
        {
            int sum = 0;
            if(props != null) {
                for(Map<String,String> m : props) {
                    String name = getEntryName(m);
                    if(name != null && name.length() > 0 && !isEntryHidden(m)) {
                        sum++;
                    }
                }
            }
            return sum;
        }
        
        //----------------------------------------------------------

        private Map<String, String> createEntry(@NonNull String name) {
            Map<String, String> prop = new TreeMap<String,String>(getComparator());
            setEntryName(prop, name);
            return prop;
        }

        private Map<String, String> createEntry(@NonNull String name, String value) {
            Map<String, String> prop = new TreeMap<String,String>(getComparator());
            setEntryName(prop, name);
            setEntryValue(prop, value);
            return prop;
        }
        
        private Map<String, String> copyEntry(@NonNull Map<String, String> entry) {
            Map<String, String> newEntry = new HashMap<String, String>();
            for(String name : suffixes) {
                String value = entry.get(name);
                if(value != null && !value.isEmpty()) {
                    newEntry.put(name, value);
                }
            }
            return newEntry;
        }

        //----------------------------------------------------------

        /**
         * Add (or replace if present) entry 
         * to configuration config
         */
        private void addEntry(String config, @NonNull Map<String,String> entry) {
            assert !configNameWrong(config);
            List<Map<String,String/*|null*/>> props = getEntries(config);
            if(props == null) {
                props = new ArrayList<Map<String,String/*|null*/>>();
                APP_MULTIPROPS.put(config, props);
            } else {
                eraseEntry(props, entry);
            }
            props.add(entry);
        }
        
        private void addDefaultEntry(@NonNull Map<String,String> entry) {
            addEntry((String)null, entry);
        }
        
        private void addActiveEntry(@NonNull Map<String,String> entry) {
            addEntry(getActive(), entry);
        }

        //----------------------------------------------------------

        /**
         * Add (or replace if present) valueless entry (e.g., run argument)
         * to configuration config
         */
        private Map<String, String> addEntry(String config, @NonNull String name) {
            assert !configNameWrong(config);
            List<Map<String,String/*|null*/>> props = getEntries(config);
            if(props == null) {
                props = new ArrayList<Map<String,String/*|null*/>>();
                APP_MULTIPROPS.put(config, props);
            } else {
                eraseEntry(props, name);
            }
            Map<String, String> newEntry = createEntry(name);
            props.add(newEntry);
            return newEntry;
        }

        private Map<String, String> addDefaultEntry(@NonNull String name) {
            return addEntry((String)null, name);
        }

        private Map<String, String> addActiveEntry(@NonNull String name) {
            return addEntry(getActive(), name);
        }

        public Map<String, String> addEntryTransparent(String config, @NonNull String name) {
            assert !configNameWrong(config);
            if(config == null) {                
                for(String c: getConfigNames()) {
                    final Map<String,String> entry = getEntry(c, name);
                    if(c != null &&  entry != null && getEntryValue(entry) == null && !isEntryHidden(entry)) {
                        eraseEntry(c, name);
                    }
                }
                return addDefaultEntry(name);
            } else {
                if(hasDefaultEntryTransparent(name) && !hasDefaultEntryValueTransparent(name)) {
                    eraseEntry(config, name);
                    return null;
                } else {
                    return addEntry(config, name);
                }
            }
        }

        public Map<String, String> addDefaultEntryTransparent(@NonNull String name) {
            return addEntryTransparent((String)null, name);
        }

        public Map<String, String> addActiveEntryTransparent(@NonNull String name) {
            return addEntryTransparent(getActive(), name);
        }

        //----------------------------------------------------------

        /**
         * Add (or replace if present) named entry (i.e., having a value)
         * to configuration config
         */
        private Map<String, String> addEntry(String config, @NonNull String name, String value) {
            assert !configNameWrong(config);
            List<Map<String,String/*|null*/>> props = getEntries(config);
            if(props == null) {
                props = new ArrayList<Map<String,String/*|null*/>>();
                APP_MULTIPROPS.put(config, props);
            } else {
                eraseEntry(props, name);
            }
            Map<String, String> newEntry = createEntry(name, value);
            props.add(newEntry);
            return newEntry;
        }

        private Map<String, String> addDefaultEntry(@NonNull String name, String value) {
            return addEntry(null, name, value);
        }

        private Map<String, String> addActiveEntry(@NonNull String name, String value) {
            return addEntry(getActive(), name, value);
        }

        public Map<String, String> addEntryTransparent(String config, @NonNull String name, String value) {
            assert !configNameWrong(config);
            if(config == null) {                
                for(String c: getConfigNames()) {
                    final Map<String, String> entry = getEntry(c, name);
                    if(c != null && entry != null && JFXProjectProperties.isEqualText(getEntryValue(entry), value) && !isEntryHidden(entry) ) {
                        eraseEntry(c, name);
                    }
                }
                return addDefaultEntry(name, value);
            } else {
                if(hasDefaultEntryTransparent(name, value)) {
                    eraseEntry(config, name);
                    return null;
                } else {
                    return addEntry(config, name, value);
                }
            }
        }

        public Map<String, String> addDefaultEntryTransparent(@NonNull String name, String value) {
            return addEntryTransparent((String)null, name, value);
        }

        public Map<String, String> addActiveEntryTransparent(@NonNull String name, String value) {
            return addEntryTransparent(getActive(), name, value);
        }

        //----------------------------------------------------------

        /**
        * Updates entries; if config==default, then simply updates default entries,
        * otherwise updates entries in current config so that only those different
        * from those in default config are stored.
        * 
        * @param config
        * @param entries 
        */
        public void setEntriesTransparent(String config, List<Map<String,String/*|null*/>>/*|null*/ entries) {
            assert !configNameWrong(config);
            if(config == null) {
                List<Map<String,String>> newDefault = new ArrayList<Map<String, String>>();
                Set<String> toClean = new HashSet<String>();
                if(APP_MULTIPROPS.get(null) != null) {
                    for(Map<String,String> entry : APP_MULTIPROPS.get(null)) {
                        if(isEntryHidden(entry)) {
                            newDefault.add(entry);
                        } else {
                            toClean.add(getEntryName(entry));
                        }
                    }
                }
                APP_MULTIPROPS.put(null, newDefault);
                if(entries != null) {
                    for(Map<String, String> entry : entries) {
                        String name = getEntryName(entry);
                        toClean.remove(name);
                        Map<String, String> added;
                        if(hasEntryValue(entry)) {
                            added = addDefaultEntryTransparent(name, getEntryValue(entry));
                        } else {
                            added = addDefaultEntryTransparent(name);
                        }
                        if(isEntryHidden(entry)) {
                            hideEntry(added);
                        }
                    }
                }
                for(String name : toClean) {
                    eraseNonDefaultEntries(name, true);
                }
            } else {
                List<Map<String,String/*|null*/>> reduct = new ArrayList<Map<String,String/*|null*/>>();
                if(entries != null) {
                    List<Map<String,String/*|null*/>> def = JFXProjectUtils.copyList(getDefaultEntriesTransparent());
                    for(Map<String,String> map : entries) {
                        String name = getEntryName(map);
                        String value = getEntryValue(map);
                        Map<String, String> defEntry = getDefaultEntryTransparent(name);
                        if(defEntry != null) {
                            String defValue = getEntryValue(defEntry);
                            if( !JFXProjectProperties.isEqualText(value, defValue) ) {
                                reduct.add(JFXProjectUtils.copyMap(map));
                            }
                            def.remove(defEntry);
                        } else {
                            if(!isEntryHidden(map)) {
                                reduct.add(JFXProjectUtils.copyMap(map));
                            }
                        }
                    }
                    for(Map<String,String> map : def) { //def cannot be null
                        Map<String,String> defCopy = JFXProjectUtils.copyMap(map);
                        hideEntry(defCopy);
                        reduct.add(defCopy);
                    }
                }
                APP_MULTIPROPS.put(config, reduct);
            }
        }

        public void setDefaultEntriesTransparent(List<Map<String,String/*|null*/>>/*|null*/ entries) {
            setEntriesTransparent((String)null, entries);
        }

        public void setActiveEntriesTransparent(List<Map<String,String/*|null*/>>/*|null*/ entries) {
            setEntriesTransparent(getActive(), entries);
        }

        //----------------------------------------------------------

        private void eraseEntry(String config, @NonNull String name) {
            assert !configNameWrong(config);
            eraseEntry(getEntries(config), name);
        }

        private void eraseDefaultEntry(@NonNull String name) {
            eraseEntry((String)null, name);
        }

        private void eraseActiveEntry(@NonNull String name) {
            eraseEntry(getActive(), name);
        }
        
        private void eraseNonDefaultEntries(@NonNull String name, boolean hidden) {
            for(String c: getConfigNames()) {
                final Map<String, String> entry = getEntry(c, name);
                if(c != null && entry != null && isEntryHidden(entry)==hidden ) {
                    eraseEntry(c, name);
                }
            }
        }

        public void eraseEntryTransparent(String config, @NonNull String name) {
            assert !configNameWrong(config);
            if(config == null) {                
                eraseNonDefaultEntries(name, true);
                eraseDefaultEntry(name); // do not hide (though that should work too)
            } else {
                if(hasDefaultEntryTransparent(name)) {
                    if(hasEntry(config, name)) {
                        hideEntry(config, name);
                    } else {
                        Map<String, String> toHide = addEntry(config, name);
                        hideEntry(toHide);
                    }
                } else {
                    eraseEntry(config, name);
                }
            }
        }

        public void eraseDefaultEntryTransparent(@NonNull String name) {
            eraseEntryTransparent((String)null, name);
        }

        public void eraseActiveEntryTransparent(@NonNull String name) {
            eraseEntryTransparent(getActive(), name);
        }

        private void eraseEntries(String config) {
            assert !configNameWrong(config);
            APP_MULTIPROPS.remove(config);
        }

        private void eraseDefaultEntries() {
            eraseEntries(null);
        }

        private void eraseActiveEntries() {
            eraseEntries(getActive());
        }

        public void eraseEntriesTransparent(String config) {
            assert !configNameWrong(config);
            if(config == null) {               
                // erase all equal hidden entries in nondefault configs
                if(APP_MULTIPROPS.get(null) != null) {
                    for(Map<String, String> defEntry : APP_MULTIPROPS.get(null)) {
                        if(!isEntryHidden(defEntry)) {
                            String name = getEntryName(defEntry);
                            for(String c: getConfigNames()) {
                                final Map<String, String> entry = getEntry(c, name);
                                if(c != null && entry != null && isEntryHidden(entry) ) {
                                    eraseEntry(c, name);
                                }
                            }
                        }
                    }
                }
                eraseDefaultEntries(); // do not hide (though that should work too)
            } else {
                List<Map<String,String>> hidden = new ArrayList<Map<String, String>>();
                List<Map<String,String>> configEntries = getEntries(config);
                if(configEntries != null) {
                    for(Map<String, String> map : configEntries) {
                        String name = getEntryName(map);
                        if(hasDefaultEntryTransparent(name)) {
                            Map<String, String> h = JFXProjectUtils.copyMap(map);
                            hideEntry(h);
                            hidden.add(h);
                        }
                    }
                }
                APP_MULTIPROPS.put(config, hidden);
            }
        }

        public void eraseDefaultEntriesTransparent() {
            eraseEntriesTransparent((String)null);
        }

        public void eraseActiveEntriesTransparent() {
            eraseEntriesTransparent(getActive());
        }

        //==========================================================

        /**
        * If entryName exists in entries, returns the map representing it
        * Returns null if entry does not exist.
        * 
        * @param entries list of application entries (each stored in a map in keys 'name' and 'value'
        * @param entryName entry to be searched for
        * @return entry if found, null otherwise
        */
        private Map<String, String> getEntry(List<Map<String, String>> entries, String entryName) {
            if(entries != null && entryName != null) {
                for(Map<String, String> map : entries) {
                    String name = getEntryName(map);
                    if(name != null && name.equals(entryName)) {
                        return map;
                    }
                }
            }
            return null;
        }

        private void eraseEntry(List<Map<String, String>> entries, String entryName) {
            if(entries != null && entryName != null) {
                Map<String, String> toErase = null;
                for(Map<String, String> map : entries) {
                    String name = getEntryName(map);
                    if(name != null && name.equals(entryName)) {
                        toErase = map;
                        break;
                    }
                }
                if(toErase != null) {
                    entries.remove(toErase);
                }
            }
        }
        
        /**
         * Erases entry from entries that has name equal to that in 'entry'
         * @param entries
         * @param entry 
         */
        private void eraseEntry(List<Map<String, String>> entries, Map<String,String> entry) {
            if(entries != null && entry != null) {
                String name = getEntryName(entry);
                eraseEntry(entries, name);
            }
        }

        //----------------------------------------------------------

        private boolean isEntryNameProperty(@NonNull String prop) {
            return prop != null && prop.startsWith(prefix) && prop.endsWith(suffixes[0]);
        }

        private boolean isEntryValueProperty(@NonNull String prop) {
            return prop != null && prop.startsWith(prefix) && prop.endsWith(suffixes[1]);
        }

        private boolean isEntryHiddenProperty(@NonNull String prop) {
            return prop != null && prop.startsWith(prefix) && prop.endsWith(suffixes[2]);
        }

        private String getEntryValueProperty(String entryNameProperty) {
            if(entryNameProperty != null && isEntryNameProperty(entryNameProperty)) {
                return entryNameProperty.replace(suffixes[0], suffixes[1]);
            }
            return null;
        }

        private String getEntryHiddenProperty(String entryNameProperty) {
            if(entryNameProperty != null && isEntryNameProperty(entryNameProperty)) {
                return entryNameProperty.replace(suffixes[0], suffixes[2]);
            }
            return null;
        }

        private String getEntryNameProperty(int index) {
            return prefix + index + "." + suffixes[0]; // NOI18N
        }

        private String getEntryValueProperty(int index) {
            return prefix + index + "." + suffixes[1]; // NOI18N
        }

        private String getEntryHiddenProperty(int index) {
            return prefix + index + "." + suffixes[2]; // NOI18N
        }

        private boolean isFreeEntryPropertyIndex(int index, @NonNull EditableProperties ep) {
            return !ep.containsKey(getEntryNameProperty(index));
        }

        private int getFreeEntryPropertyIndex(int start, @NonNull EditableProperties ep, @NonNull EditableProperties pep, List<String> propNamesUsed) {
            int index = (start >= 0) ? start : 0;
            while(index >= 0) {
                if(isFreeEntryPropertyIndex(index, ep) && isFreeEntryPropertyIndex(index, pep) && (propNamesUsed == null || !propNamesUsed.contains(getEntryNameProperty(index)))) {
                    break;
                }
                index++;
            }
            return (index >= 0) ? index : 0;
        }

        /**
         * Adds/updates properties representing entries in editable properties
         * 
         * @param config
         * @param projectProperties project.properties to update
         * @param privateProperties private.properties to update
         * @param propNamesUsed possibly nonempty list of property names to be avoided (to prevent duplication across various configurations etc)
         * @return true if private properties have been updated
         */
        private boolean updateEntryProperties(String config, @NonNull EditableProperties projectProperties, @NonNull EditableProperties privateProperties, @NonNull List<String> propNamesUsed) {
            assert !configNameWrong(config);
            boolean privateUpdated = false;
            List<Map<String, String>> reduce = JFXProjectUtils.copyList(getEntries(config));
            // remove properties with indexes used before (to be replaced later by new unique property names)
            for(String prop : propNamesUsed) {
                if(prop != null && prop.length() > 0) {
                    projectProperties.remove(prop);
                    projectProperties.remove(getEntryValueProperty(prop));
                    projectProperties.remove(getEntryHiddenProperty(prop));
                    privateProperties.remove(prop);
                    privateProperties.remove(getEntryValueProperty(prop));
                    privateProperties.remove(getEntryHiddenProperty(prop));
                }
            }
            // delete those private entry properties not present in config
            cleanEntryPropertiesIfEmpty(config, privateProperties);
            // and log usage of the remaining private entry properties
            for(String prop : privateProperties.keySet()) {
                if(isEntryNameProperty(prop)) {
                    propNamesUsed.add(prop);
                }
            }
            // update private properties
            List<Map<String, String>> toEraseList = new LinkedList<Map<String, String>>();
            for(Map<String, String> map : reduce) { // reduce cannot be null
                if(updateEntryPropertyIfExists(map, privateProperties, true)) {
                    toEraseList.add(map);
                    privateUpdated = true;
                }
            }

            reduce.removeAll(toEraseList);

            // delete those nonprivate prop properties not present in reduce
            cleanEntryPropertiesNotListed(reduce, projectProperties);
            // and log usage of the remaining private entry properties
            for(String prop : projectProperties.keySet()) {
                if(isEntryNameProperty(prop)) {
                    propNamesUsed.add(prop);
                }
            }
            // now create new nonprivate prop properties
            int index = 0;
            for(Map<String, String> map : reduce) {
                String name = getEntryName(map);
                if(name != null && name.length() > 0 && !updateEntryPropertyIfExists(map, projectProperties, false)) {
                    index = getFreeEntryPropertyIndex(index, projectProperties, privateProperties, propNamesUsed);
                    exportEntryProperty(map, getEntryNameProperty(index), getEntryValueProperty(index), getEntryHiddenProperty(index), projectProperties);
                    propNamesUsed.add(getEntryNameProperty(index));
                }
            }
            return privateUpdated;
        }

        private boolean updateDefaultEntryProperties(@NonNull EditableProperties projectProperties, @NonNull EditableProperties privateProperties, List<String> propNamesUsed) {
            return updateEntryProperties(null, projectProperties, privateProperties, propNamesUsed);
        }

        /**
        * Searches in properties for entry named 'name'. If found, updates
        * all existing entry properties (for 'name' and 'value' and possibly 'hidden') and returns
        * true, otherwise returns false.
        * 
        * @param name entry name
        * @param value entry value
        * @param properties editable properties in which to search for updateable entries
        * @param storeEmpty true==keep empty properties in editable properties, false==remove empty properties
        * @return true if updated existing property, false otherwise
        */
        private boolean updateEntryPropertyIfExists(Map<String, String> entry, EditableProperties ep, boolean storeEmpty) {
            if(entry != null) {
                String name = getEntryName(entry);
                if(name != null && !name.isEmpty()) {
                    for(String prop : ep.keySet()) {
                        if(isEntryNameProperty(prop)) {
                            if(JFXProjectProperties.isEqualText(name, ep.get(prop))) {
                                String propVal = getEntryValueProperty(prop);
                                String value = getEntryValue(entry);
                                if (value != null && (value.length() > 0 || storeEmpty)) {
                                    ep.setProperty(propVal, value);
                                } else {
                                    ep.remove(propVal);
                                }
                                String propHid = getEntryHiddenProperty(prop);
                                if(isEntryHidden(entry)) {
                                    ep.setProperty(propHid, APP_MULTIPROP_HIDDEN_TRUE);
                                } else {
                                    ep.remove(propHid);
                                }
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }

        /**
        * Remove from ep all prop related properties that represent
        * entries not present in config
        * 
        * @param ep editable properties
        */
        private void cleanEntryPropertiesIfEmpty(String config, EditableProperties ep) {
            assert !configNameWrong(config);
            List<String> toRemove = new LinkedList<String>();
            for(String prop : ep.keySet()) {
                if(isEntryNameProperty(prop)) {
                    String name = ep.get(prop);
                    if(!hasEntry(config, name)) {
                        toRemove.add(prop);
                    }
                }
            }
            for(String prop : toRemove) {
                ep.remove(prop);
                ep.remove(getEntryValueProperty(prop));
                ep.remove(getEntryHiddenProperty(prop));
            }
        }

        /**
        * Remove from ep all entry related properties that represent
        * entries not present in 'entries'
        * 
        * @param ep editable properties
        */
        private void cleanEntryPropertiesNotListed(List<Map<String, String>> entries, EditableProperties ep) {
            List<String> toRemove = new LinkedList<String>();
            for(String name : ep.keySet()) {
                if(isEntryNameProperty(name)) {
                    boolean inProps = false;
                    if(entries != null) {
                        for(Map<String,String> map : entries) {
                            String prop = getEntryName(map);
                            if(JFXProjectProperties.isEqualText(name, prop)) {
                                inProps = true;
                                break;
                            }
                        }
                    }
                    if(!inProps) {
                        toRemove.add(name);
                    }
                }
            }
            for(String prop : toRemove) {
                ep.remove(prop);
                ep.remove(getEntryValueProperty(prop));
                ep.remove(getEntryHiddenProperty(prop));
            }
        }

        /**
        * Store one entry to editable properties (effectively as two or three properties,
        * one for name, second for value, third to mark hidden properties), index is used to distinguish among
        * entry-property instances
        * 
        * @param entry property to be stored in editable properties
        * @param newPropName name of property to store entry name
        * @param newPropValue name of property to store entry value
        * @param ep editable properties to which prop is to be stored
        */
        private void exportEntryProperty(@NonNull Map<String, String> entry, String newPropName, String newPropValue, String newPropHidden, @NonNull EditableProperties ep) {
            String name = getEntryName(entry);
            String value = getEntryValue(entry);
            if(name != null) {
                ep.put(newPropName, name);
                if(value != null && value.length() > 0) {
                    ep.put(newPropValue, value);
                }
                if(isEntryHidden(entry)) {
                    ep.put(newPropHidden, APP_MULTIPROP_HIDDEN_TRUE);
                }
            }
        }

        // -------------------------------------------------------------------
        
        /**
        * Extract from editable properties all properties depicting entries
        * and store them as such in 'entries'. If such exist in 'entries', then override their values.
        * 
        * @param ep editable properties to extract from
        * @param props application entries to add to / update in
        */
        private void extractEntries(@NonNull EditableProperties ep, String config) {
            if(ep != null) {
                for(String prop : ep.keySet()) {
                    if(isEntryNameProperty(prop)) {
                        String name = ep.getProperty(prop);
                        if(name != null) {
                            String value = ep.getProperty(getEntryValueProperty(prop));
                            String hidden = ep.getProperty(getEntryHiddenProperty(prop));
                            Map<String,String> map = createEntry(name);
                            if(value != null) {
                                setEntryValue(map, value);
                            }
                            if(hidden != null && JFXProjectProperties.isTrue(hidden)) {
                                hideEntry(map);
                            }
                            addEntry(config, map);
                        }
                    }
                }
            }
        }

        private void extractDefaultEntries(@NonNull EditableProperties ep) {
            extractEntries(ep, null);
        }

        private void extractActiveEntries(@NonNull EditableProperties ep) {
            extractEntries(ep, getActive());
        }
        
        /**
         * Returns dump of APP_MULTIPROPS. Useful for testing.
         * @return string representation of complete class contents
         */
        public String toString() {
            StringBuilder sb = new StringBuilder(MULTI_PROPERTY_STRING); // getClass().getName()); // NOI18N
            sb.append(":"); // NOI18N
            List<String> keys = new ArrayList<String>();
            keys.addAll(APP_MULTIPROPS.keySet());
            keys.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    if(o1 == null) {
                        if(o2 == null) {
                            return 0;
                        }
                        return -1;
                    }
                    if(o2 == null) {
                        return 1;
                    }
                    return o1.compareTo(o2);
                }
            });
            if(keys == null || keys.isEmpty()) {
                sb.append(" "); //NOI18N 
                sb.append(MULTI_PROPERTY_EMPTY);
            } else {
                for(String configName : keys) {
                    sb.append(" {"); // NOI18N
                    sb.append(configName);
                    sb.append("}"); // NOI18N
                    List<Map<String,String>> configList = new ArrayList<Map<String,String>>(APP_MULTIPROPS.get(configName));
                    configList.sort(new Comparator<Map<String,String>>() {
                        @Override
                        public int compare(Map<String, String> o1, Map<String, String> o2) {
                            String n1 = getEntryName(o1);
                            String n2 = getEntryName(o2);
                            if(n1 == null && n2 != null) {
                                return -1;
                            } else if (n1 != null && n2 == null) {
                                return 1;
                            }
                            if((n1 == null && n2 == null) || n1.compareTo(n2) == 0) {
                                String v1 = getEntryValue(o1);
                                String v2 = getEntryValue(o2);
                                if(v1 == null && v2 != null) {
                                    return -1;
                                } else if (v1 != null && v2 == null) {
                                    return 1;
                                } else if (v1 == null && v2 == null) {
                                    return 0;
                                }
                                return v1.compareTo(v2);
                            }
                            return n1.compareTo(n2);
                        }
                    });
                    for(Map<String,String> map : configList) {
                        for(int i=0; i<suffixes.length; i++) {
                            String s = map.get(suffixes[i]);
                            if(s != null) {
                                sb.append(" "); // NOI18N
                                sb.append(suffixes[i]);
                                sb.append("="); // NOI18N
                                sb.append(s);
                            }
                        }
                    }
                }
            }
            return sb.toString();
        }

    }
    
}
