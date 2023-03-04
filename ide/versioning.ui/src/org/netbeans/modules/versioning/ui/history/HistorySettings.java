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
package org.netbeans.modules.versioning.ui.history;

import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * Manages History Settings
 * 
 * @author Tomas Stupka
 */
public class HistorySettings {
    
    private static final HistorySettings INSTANCE = new HistorySettings();
    
    private static final String PROP_TTL = "timeToLive";                                        // NOI18N  
    public static final String PROP_INCREMENTS = "history.increments";                          // NOI18N  
    private static final String PROP_CLEANUP_LABELED = "noLabelCleanUp";                        // NOI18N  
    private static final String PROP_KEEP_FOREVER = "keepForever";                              // NOI18N  
    public static final String PROP_LOAD_ALL = "history.loadAll";                               // NOI18N  
    public static final String PROP_ALL_MODE = "history.AllMode";                               // NOI18N  
    public static final String PROP_VCS_MODE = "history.VCSMode";                               // NOI18N  
    public static final String PROP_LH_MODE = "history.LHMode";                               // NOI18N  

    /** Creates a new instance of HistorySettings */
    private HistorySettings() {
    }
    
    public static HistorySettings getInstance() {        
        migrate();
        return INSTANCE;
    }
    
    private Preferences getPreferences() {
        return NbPreferences.forModule(HistorySettings.class);
    }

    public void setTTL(int ttl) {
        getPreferences().putInt(PROP_TTL, ttl);
    }
    
    public void setIncrements(int ttl) {
        getPreferences().putInt(PROP_INCREMENTS, ttl);
    }
    
    public int getTTL() {
        return getPreferences().getInt(PROP_TTL, 7);
    }    

    public int getIncrements() {
        int i = getPreferences().getInt(PROP_INCREMENTS, 30);
        return i < 1 ? i = 7 : i;
    }    

    public void setCleanUpLabeled(boolean selected) {
        getPreferences().putBoolean(PROP_CLEANUP_LABELED, selected);
    }
    
    public boolean getCleanUpLabeled() {
        return getPreferences().getBoolean(PROP_CLEANUP_LABELED, true);
    }
    
    public long getTTLMillis() {
        return ((long) getTTL()) * 24 * 60 * 60 * 1000;
    }   

    public boolean getKeepForever() {
        return getPreferences().getBoolean(PROP_KEEP_FOREVER, false);
    }

    public void setKeepForever(boolean forever) {
        getPreferences().putBoolean(PROP_KEEP_FOREVER, forever);
    }
    
    public boolean getLoadAll() {
        return getPreferences().getBoolean(PROP_LOAD_ALL, true);
}

    public void setLoadAll(boolean loadAll) {
        getPreferences().putBoolean(PROP_LOAD_ALL, loadAll);
    }

    public void addPreferenceListener(PreferenceChangeListener l) {
        getPreferences().addPreferenceChangeListener(l);
    }
    
    public void removePreferenceListener(PreferenceChangeListener l) {
        getPreferences().removePreferenceChangeListener(l);
    }

    void setAllMode(String name) {
        getPreferences().put(PROP_ALL_MODE, name);
    }
    
    void setVCSMode(String name) {
        getPreferences().put(PROP_VCS_MODE, name);
    }
    
    void setLHMode(String name) {
        getPreferences().put(PROP_LH_MODE, name);
    }
    
    String getAllMode(String def) {
        return getPreferences().get(PROP_ALL_MODE, def);
    }
    
    String getVCSMode(String def) {
        return getPreferences().get(PROP_VCS_MODE, def);
    }
    
    String getLHMode(String def) {
        return getPreferences().get(PROP_LH_MODE, def);
    }

    private static void migrate() { 
        // migrate pre 7.2 settings 
        String prevPath = "org/netbeans/modules/localhistory"; // NOI18N
        try {
            if(!NbPreferences.root().nodeExists(prevPath)) {
                return;
            }
            Preferences prev = NbPreferences.root().node(prevPath);
            Preferences cur =  NbPreferences.forModule(HistorySettings.class);
            String[] keys = prev.keys();
            for (String key : keys) {
                String value = prev.get(key, null);
                if(value != null && cur.get(key, null) == null) {
                    cur.put(key, value);
                }
            }
            prev.removeNode();
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
        
    }    
}
