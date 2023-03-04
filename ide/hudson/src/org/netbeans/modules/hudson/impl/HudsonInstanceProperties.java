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

package org.netbeans.modules.hudson.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import static org.netbeans.modules.hudson.constants.HudsonInstanceConstants.*;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * Instance properties for Hudson instance
 *
 * @author Michal Mocnak
 */
public class HudsonInstanceProperties extends HashMap<String,String> {
    
    private static final RequestProcessor RP = new RequestProcessor(
            HudsonInstanceProperties.class);
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    public HudsonInstanceProperties(String name, String url, String sync) {
        put(INSTANCE_NAME, name);
        put(INSTANCE_URL, url);
        put(INSTANCE_SYNC, sync);
    }
    
    public HudsonInstanceProperties(Map<String,String> properties) {
        super(properties);
    }

    @Override
    public final synchronized String put(String key, String value) {
        String o = super.put(key, value);
        pcs.firePropertyChange(key, o, value);
        if (key.equals(INSTANCE_NAME)) {
            loadPreferences();
        }
        updatePreferences(key);
        return o;
    }
    
    @Override
    public synchronized String remove(Object key) {
        String o = super.remove((String) key);
        pcs.firePropertyChange((String) key, o, null);
        updatePreferences((String) key);
        return o;
    }

    public final boolean isPersisted() {
        String pers = get(INSTANCE_PERSISTED);
        return pers == null || TRUE.equals(pers);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    public List<PropertyChangeListener> getCurrentListeners() {
        return Arrays.asList(pcs.getPropertyChangeListeners());
    }

    public static List<String> split(String prop) {
        if (prop != null && prop.trim().length() > 0) {
            String[] escaped = prop.split("(?<!/)/(?!/)");              //NOI18N
            List<String> list = new ArrayList<String>(escaped.length);
            for (String e : escaped) {
                list.add(e.replace("//", "/"));                         //NOI18N
            }
            return list;
        } else {
            return Collections.<String>emptyList();
        }
    }

    public static String join(List<String> pieces) {
        StringBuilder b = new StringBuilder();
        for (String piece : pieces) {
            assert !piece.startsWith("/") //NOI18N
                    && !piece.endsWith("/") : piece;                    //NOI18N
            String escaped = piece.replace("/", "//");                  //NOI18N
            if (b.length() > 0) {
                b.append('/');
            }
            b.append(escaped);
        }
        return b.toString();
    }

    /**
     * Get Preferences that this properties use as persistent storage.
     */
    public Preferences getPreferences() {
        String nodeName = getNodeName();
        if (nodeName != null) {
            return HudsonManagerImpl.instancePrefs().node(nodeName);
        } else {
            return null;
        }
    }

    /**
     * Check if there are existing preferences for this properties.
     */
    private boolean hasPreferences() {
        String nodeName = getNodeName();
        if (nodeName != null) {
            try {
                return HudsonManagerImpl.instancePrefs().nodeExists(nodeName);
            } catch (BackingStoreException ex) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Get Preferences node name for this properties instance.
     */
    private String getNodeName() {
        String name = get(INSTANCE_NAME);
        if (name != null && !name.isEmpty()) {
            return HudsonManagerImpl.simplifyServerLocation(name, true);
        } else {
            return null;
        }
    }

    /**
     * Update persistent preferences in a background thread.
     */
    private void updatePreferences(final String... keys) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                Preferences prefs = getPreferences();
                if (prefs != null) {
                    for (String key : keys) {
                        String val = get(key);
                        if (val == null) {
                            prefs.remove(key);
                        } else {
                            prefs.put(key, val);
                        }
                    }
                }
            }
        });
    }

    /**
     * Load preferences in background thread.
     */
    private void loadPreferences() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                if (hasPreferences()) {
                    Preferences prefs = getPreferences();
                    if (prefs != null) {
                        try {
                            String[] keys = prefs.keys();
                            for (String key : keys) {
                                if (INSTANCE_NAME.equals(key)
                                        || INSTANCE_URL.equals(key)) {
                                    continue;
                                }
                                String val = prefs.get(key, null);
                                if (val != null) {
                                    put(key, val);
                                }
                            }
                        } catch (BackingStoreException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        });
    }
}
