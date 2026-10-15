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

package org.netbeans.modules.editor.indent.project;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.openide.util.WeakListeners;

/**
 *
 * @author vita
 */
public final class ProxyPreferences extends AbstractPreferences {

    public ProxyPreferences(Preferences... delegates) {
        this("", null, delegates); //NOI18N
    }

    @Override
    protected void putSpi(String key, String value) {
        checkDelegates();
        for(int i = 0; i < delegates.length; i++) {
            if (delegates[i] != null) {
                delegates[i].put(key, value);
                return;
            }
        }
    }

    @Override
    protected String getSpi(String key) {
        checkDelegates();
        for(int i = 0; i < delegates.length; i++) {
            if (delegates[i] != null) {
                try {
                    String value = delegates[i].get(key, null);
                    if (value != null) {
                        return value;
                    }
                } catch (Exception e) {
                    // mark the delegate as invalid
                    delegates[i] = null;
                }
            }
        }
        return null;
    }

    @Override
    protected void removeSpi(String key) {
        throw new UnsupportedOperationException("Not supported yet."); //NOI18N
    }

    @Override
    protected void removeNodeSpi() throws BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet."); //NOI18N
    }

    @Override
    protected String[] keysSpi() throws BackingStoreException {
        Set<String> keys = new HashSet<String>();
        checkDelegates();
        for(int i = 0; i < delegates.length; i++) {
            if (delegates[i] != null) {
                try {
                    keys.addAll(Arrays.asList(delegates[i].keys()));
                } catch (Exception e) {
                    // mark the delegate as invalid
                    delegates[i] = null;
                }
            }
        }
        return keys.toArray(new String[ 0 ]);
    }

    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException {
//        Set<String> names = new HashSet<String>();
//        for(Preferences d : delegates) {
//            names.addAll(Arrays.asList(d.childrenNames()));
//        }
//        return names.toArray(new String[ names.size() ]);
        return EMPTY;
    }

    @Override
    protected AbstractPreferences childSpi(String name) {
//        Preferences [] nueDelegates = new Preferences[delegates.length];
//        for(int i = 0; i < delegates.length; i++) {
//            nueDelegates[i] = delegates[i].node(name);
//        }
//        return new ProxyPreferences(name, this, nueDelegates);
        throw new UnsupportedOperationException("Not supported yet."); //NOI18N
    }

    @Override
    protected void syncSpi() throws BackingStoreException {
//        delegates[0].sync();
        throw new UnsupportedOperationException("Not supported yet."); //NOI18N
    }

    @Override
    protected void flushSpi() throws BackingStoreException {
//        delegates[0].flush();
        throw new UnsupportedOperationException("Not supported yet."); //NOI18N
    }

    // -----------------------------------------------------------------------
    // private implementation
    // -----------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(ProxyPreferences.class.getName());

    private static final String [] EMPTY = new String[0];
    
    private final Preferences [] delegates;
    private final Preferences [] roots;
    private final String [] paths;
    private final PreferenceChangeListener [] prefTrackers;

    private ProxyPreferences(String name, ProxyPreferences parent, Preferences... delegates) {
        super(parent, name); //NOI18N
        assert delegates.length > 0 : "There must be at least one delegate"; //NOI18N
        this.delegates = delegates;
        this.roots = new Preferences[delegates.length];
        this.paths = new String[delegates.length];
        this.prefTrackers = new PreferenceChangeListener[delegates.length];

        for(int i = 0; i < delegates.length; i++) {
            roots[i] = delegates[i].node("/"); //NOI18N
            paths[i] = delegates[i].absolutePath();
            prefTrackers[i] = new PrefTracker(i);
            delegates[i].addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, prefTrackers[i], delegates[i]));
        }
    }

    private void checkDelegates() {
        for(int i = 0; i < delegates.length; i++) {
            if (delegates[i] != null) {
                try {
                    if (delegates[i].nodeExists("")) { //NOI18N
                        continue;
                    }
                } catch (BackingStoreException bse) {
                    // ignore
                }
                delegates[i] = null;
            }

            assert delegates[i] == null;
            try {
                if (roots[i].nodeExists(paths[i])) {
                    delegates[i] = roots[i].node(paths[i]);
                }
            } catch (BackingStoreException bse) {
                // ignore
            }
        }
    }

    private void firePrefChange(String key, String newValue) {
        try {
            Method m = AbstractPreferences.class.getDeclaredMethod("enqueuePreferenceChangeEvent", String.class, String.class); //NOI18N
            m.setAccessible(true);
            m.invoke(this, key, newValue);
        } catch (Exception e) {
            LOG.log(Level.WARNING, null, e);
        }
    }
    
    private class PrefTracker implements PreferenceChangeListener {

        private final int delegateIdx;

        public PrefTracker(int idx) {
            this.delegateIdx = idx;
        }
        
        public void preferenceChange(PreferenceChangeEvent evt) {
            synchronized (ProxyPreferences.this.lock) {
                if (evt.getKey() != null) {
                    checkDelegates();
                    for(int i = 0; i < delegateIdx; i++) {
                        if (delegates[i] != null && delegates[i].get(evt.getKey(), null) != null) {
                            // ignore
                            return;
                        }
                    }
                }
            }

            firePrefChange(evt.getKey(), evt.getNewValue());
        }

    } // End of PrefTracker class
}
