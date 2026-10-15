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

package org.netbeans.modules.editor.settings.storage.preferences;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.settings.storage.api.EditorSettingsStorage;
import org.netbeans.modules.editor.settings.storage.api.OverridePreferences;
import org.netbeans.modules.editor.settings.storage.spi.TypedValue;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author vita
 */
public final class PreferencesImpl extends AbstractPreferences implements PreferenceChangeListener, OverridePreferences {

    // the constant bellow is used in o.n.e.Settings!!
    private static final String JAVATYPE_KEY_PREFIX = "nbeditor-javaType-for-legacy-setting_"; //NOI18N
    
    public static synchronized PreferencesImpl get(MimePath mimePath) {
          // must not access static-variable cache directly.
          return (PreferencesImpl)DEFAULT_FACTORY.createPreferences(mimePath);
    }

    // ---------------------------------------------------------------------
    // Preferences API
    // ---------------------------------------------------------------------
    
    public @Override String absolutePath() {
        return SLASH;
    }

    public @Override String[] childrenNames() throws BackingStoreException {
        return EMPTY_ARRAY;
    }

    public @Override boolean isUserNode() {
        return true;
    }

    public @Override String name() {
        return EMPTY;
    }

    public @Override Preferences node(String path) {
        if (path.length() == 0 || path.equals(SLASH)) {
            return this;
        } else {
            throw new IllegalStateException("Editor Preferences does not support children nodes."); //NOI18N
        }
    }

    public @Override boolean nodeExists(String path) throws BackingStoreException {
        if (path.length() == 0 || path.equals(SLASH)) {
            return true;
        } else {
            return false;
        }
    }

    public @Override Preferences parent() {
        return null;
    }

    public @Override void removeNode() throws BackingStoreException {
        throw new IllegalStateException("Can't remove the root!"); //NOI18N
    }

    public @Override final void sync() throws BackingStoreException {
        flushTask.waitFinished();
        super.sync();
    }
    
    public @Override void put(String key, String value) {
        if (putValueJavaType.get() == null) {
            putValueJavaType.set(String.class.getName());
        }
        try {
            synchronized(lock) {
                if (key != null && value != null && value.equals(getSpi(key))) {
                    return;
                } else {
                    super.put(key, value);
                }
            }
        } finally {
            if (putValueJavaType.get().equals(String.class.getName())) {
                putValueJavaType.remove();
            }
        }
    }
    
    public @Override boolean isOverriden(String key) {
        synchronized (lock) {
            String bareKey;
            if (key.startsWith(JAVATYPE_KEY_PREFIX)) {
                bareKey = key.substring(JAVATYPE_KEY_PREFIX.length());
            } else {
                bareKey = key;
            }
            return getLocal().containsKey(bareKey);
        }
    }

    public @Override void remove(String key) {
        synchronized(lock) {
            String bareKey;
            boolean removeValue;

            if (key.startsWith(JAVATYPE_KEY_PREFIX)) {
                bareKey = key.substring(JAVATYPE_KEY_PREFIX.length());
                removeValue = false;
            } else {
                bareKey = key;
                removeValue = true;
            }

            if (getLocal().containsKey(bareKey)) {
                if (removeValue) {
                    getLocal().remove(bareKey);
                } else {
                    getLocal().get(bareKey).setJavaType(null);
                }

                firePreferenceChange(key, null);
                asyncInvocationOfFlushSpi();
            }
        }
    }

    public @Override void putInt(String key, int value) {
        putValueJavaType.set(Integer.class.getName());
        try {
            super.putInt(key, value);
        } finally {
            putValueJavaType.remove();
        }
    }

    public @Override void putLong(String key, long value) {
        putValueJavaType.set(Long.class.getName());
        try {
            super.putLong(key, value);
        } finally {
            putValueJavaType.remove();
        }
    }
    
    public @Override void putBoolean(String key, boolean value) {
        putValueJavaType.set(Boolean.class.getName());
        try {
            super.putBoolean(key, value);
        } finally {
            putValueJavaType.remove();
        }
    }
    
    public @Override void putFloat(String key, float value) {
        putValueJavaType.set(Float.class.getName());
        try {
            super.putFloat(key, value);
        } finally {
            putValueJavaType.remove();
        }
    }

    public @Override void putDouble(String key, double value) {
        putValueJavaType.set(Double.class.getName());
        try {
            super.putDouble(key, value);
        } finally {
            putValueJavaType.remove();
        }
    }

    public @Override void putByteArray(String key, byte[] value) {
        putValueJavaType.set(value.getClass().getName());
        try {
            super.putByteArray(key, value);
        } finally {
            putValueJavaType.remove();
        }
    }

    @Override
    public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
        try {
            super.removePreferenceChangeListener(pcl);
        } catch (IllegalArgumentException e) {
            // ignore, see #143581
        }
    }

    @Override
    public void removeNodeChangeListener(NodeChangeListener ncl) {
        try {
            super.removeNodeChangeListener(ncl);
        } catch (IllegalArgumentException e) {
            // ignore, see #143581
        }
    }
    
    // ---------------------------------------------------------------------
    // AbstractPreferences SPI
    // ---------------------------------------------------------------------

    @Override
    protected AbstractPreferences getChild(String nodeName) throws BackingStoreException {
        throw new IllegalStateException("Should never be called."); //NOI18N
    }

    @Override
    protected boolean isRemoved() {
        boolean superRemoved = super.isRemoved();
        assert superRemoved == false : "super.isRemoved() should always == false"; //NOI18N
        return superRemoved;
    }
    
    protected @Override void removeNodeSpi() throws BackingStoreException {
        throw new IllegalStateException("Should never be called."); //NOI18N
    }

    protected @Override String[] childrenNamesSpi() throws BackingStoreException {
        throw new IllegalStateException("Should never be called."); //NOI18N
    }

    protected @Override AbstractPreferences childSpi(String name) {
        throw new IllegalStateException("Should never be called."); //NOI18N
    }

    protected @Override void putSpi(String key, String value) {
        // This hack is here for refiring preferenceChange events from 'inherited'.
        // Comparing the key strings by == is ok here. Please see firePreferenceChange()
        // for details.
        if (refiringChangeKey.get() != key) {
            if (!key.startsWith(JAVATYPE_KEY_PREFIX)) {
                getLocal().put(key, new TypedValue(value, putValueJavaType.get()));
                asyncInvocationOfFlushSpi();
            } else {
                String bareKey = key.substring(JAVATYPE_KEY_PREFIX.length());
                if (getLocal().containsKey(bareKey)) {
                    getLocal().get(bareKey).setJavaType(value);
                    asyncInvocationOfFlushSpi();
                } else {
                    Preferences inheritedPrefs = getInherited();
                    assert inheritedPrefs != null : mimePath;
                    inheritedPrefs.put(key, value);
                }
            }
        }
    }

    protected @Override String getSpi(String key) {
        String bareKey;
        boolean returnValue;
        
        if (key.startsWith(JAVATYPE_KEY_PREFIX)) {
            bareKey = key.substring(JAVATYPE_KEY_PREFIX.length());
            returnValue = false;
        } else {
            bareKey = key;
            returnValue = true;
        }
        
        if (getLocal().containsKey(bareKey)) {
            TypedValue typedValue = getLocal().get(bareKey);
            return returnValue ? typedValue.getValue() : typedValue.getJavaType();
        } else {
            Preferences inheritedPrefs = getInherited();
            return inheritedPrefs != null ? inheritedPrefs.get(key, null) : null;
        }
    }

    protected @Override void removeSpi(String key) {
        throw new IllegalStateException("Should never be called!"); //NOI18N
    }

    protected @Override String[] keysSpi() throws BackingStoreException {
        Set<String> keys;
        Preferences prefs = getInherited();
                
        if (prefs != null) {
            keys = new HashSet<String>();
            keys.addAll(Arrays.asList(prefs.keys()));
            keys.addAll(getLocal().keySet());
        } else {
            keys = getLocal().keySet();
        }

        return keys.toArray(new String [0]);
    }

    protected @Override void syncSpi() throws BackingStoreException {
        local = null;
    }

    protected @Override void flushSpi() throws BackingStoreException {
        // This should normally be true when invoked from flushTask, but
        // clients can generally get this node and call flush() on it without
        // changing anything.
        if (local != null) {
            try {
                storage.save(MimePath.parse(mimePath), null, false, local);
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, "Can't save editor preferences for '" + mimePath + "'", ioe); //NOI18N
            }
        }
    }

    // ---------------------------------------------------------------------
    // PreferenceChangeListener implementation
    // ---------------------------------------------------------------------
    
    public void preferenceChange(PreferenceChangeEvent evt) {
        synchronized (lock) {
            if (local != null && local.containsKey(evt.getKey())) {
                // ignore
                return;
            }
        }
        
        firePreferenceChange(evt.getKey(), evt.getNewValue());
    }
    
    // ---------------------------------------------------------------------
    // private implementation
    // ---------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(PreferencesImpl.class.getName());
    
    private static final Map<MimePath, PreferencesImpl> INSTANCES =
        new WeakHashMap<MimePath, PreferencesImpl>();

    private static final String SLASH = "/"; //NOI18N
    private static final String EMPTY = ""; //NOI18N
    private static final String [] EMPTY_ARRAY = new String [0];
    
    private static final RequestProcessor RP = new RequestProcessor();
    private final RequestProcessor.Task flushTask = RP.create(
        new Runnable() {
            public void run() {
                synchronized(lock) {
                    try {
                        flushSpi();
                    } catch (BackingStoreException ex) {
                        LOG.log(Level.WARNING, null, ex);
                    }
                }
            }
        },
        true // initially finished
    );

    private boolean noEnqueueMethodAvailable = false;
    private final ThreadLocal<String> refiringChangeKey = new ThreadLocal<String>();
    private final ThreadLocal<String> putValueJavaType = new ThreadLocal<String>();
    
    private final String mimePath;
    private final EditorSettingsStorage<String, TypedValue> storage;
    private final PropertyChangeListener storageTracker = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt == null || EditorSettingsStorage.PROP_DATA.equals(evt.getPropertyName())) {
//                Map<String, TypedValue> added = new HashMap<String, TypedValue>();
//                Map<String, TypedValue> removed = new HashMap<String, TypedValue>();
                
                synchronized (lock) {
                    if (local == null) {
                        // the data has not been read yet
                        return;
                    }
                    
//                    Map<String, TypedValue> oldLocal = local;
                    
                    // re-read the data
                    local = null;
                    getLocal();
//                    
//                    // figure out what changed
//                    Utils.<String, TypedValue>diff(oldLocal, local, added, removed);
                }
                
//                // fire the changes
//                for(String key : added.keySet()) {
//                    TypedValue value = added.get(key);
//                    firePreferenceChange(key, value.getValue());
//                }
//                
//                for(String key : removed.keySet()) {
//                    TypedValue value = removed.get(key);
//                    firePreferenceChange(key, value.getValue());
//                }
                
                firePreferenceChange(null, null);
            }
        }
    };
    
    private Map<String, TypedValue> local = null;
    private Preferences inherited = null;
    private final PreferencesFactory factory;
    
    private PreferencesImpl(String mimePath) {
        this(DEFAULT_FACTORY, mimePath);
    }
    
    public PreferencesImpl(PreferencesFactory factory, String mimePath) {
        super (null, EMPTY);
        this.factory = factory;
        this.mimePath = mimePath;
        this.storage = EditorSettingsStorage.<String, TypedValue>get(PreferencesStorage.ID);
        this.storage.addPropertyChangeListener(WeakListeners.propertyChange(storageTracker, this.storage));
    }

    private Map<String, TypedValue> getLocal() {
        if (local == null) {
            try {
                local = new HashMap<String, TypedValue>(storage.load(MimePath.parse(mimePath), null, false));
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, "Can't load editor preferences for '" + mimePath + "'", ioe); //NOI18N
                local = new HashMap<String, TypedValue>();
            }
        }
        return local;
    }
    
    private Preferences getInherited() {
        if (inherited == null && mimePath.length() > 0) {
            String type = MimePath.parse(mimePath).getInheritedType();
            if (type != null) {
                inherited = factory.createPreferences(MimePath.parse(type));
            } else {
                inherited = factory.createPreferences(MimePath.EMPTY);
            }
            
            inherited.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, inherited));
        }
        
        return inherited;
    }

    private void asyncInvocationOfFlushSpi() {
        flushTask.schedule(200);
    }

    // XXX: we probably should not extends AbstractPreferences and do it all ourselfs
    // including the firing of events. For the events delivery we could just reuse common
    // RequestProcessor threads.
    private void firePreferenceChange(String key, String newValue) {
        if (!noEnqueueMethodAvailable) {
            try {
                Method enqueueMethod = AbstractPreferences.class.getDeclaredMethod("enqueuePreferenceChangeEvent", String.class, String.class); //NOI18N
                enqueueMethod.setAccessible(true);
                enqueueMethod.invoke(this, key, newValue);
                return;
            } catch (NoSuchMethodException nsme) {
                noEnqueueMethodAvailable = true;
            } catch (Exception e) {
                LOG.log(Level.WARNING, null, e);
            }
        }
        
        if (key != null && newValue != null) {
            refiringChangeKey.set(key);
            try {
                put(key, newValue);
            } finally {
                refiringChangeKey.remove();
            }
        } else {
            assert false : "Can't fire preferenceChange event for null key or value, no enqueuePreferenceChangeEvent available"; //NOI18N
        }
    }
    
    /**
     * The default preferences factory. To be GC-compatible with older NB versions, preferences cache
     * is kept in a static variable.
     */
    private static final PreferencesFactory DEFAULT_FACTORY = new PreferencesFactory() {
        @Override
        public Preferences createPreferences(MimePath mimePath) {
            synchronized (INSTANCES) {
                PreferencesImpl prefs = INSTANCES.get(mimePath);

                if (prefs == null) {
                    prefs = new PreferencesImpl(this, mimePath.getPath());
                    INSTANCES.put(mimePath, prefs);
                }
                return prefs;
            }
        }
    };
    
    /**
     * Provides cahce for preferences for individual MIME paths. Preferences cannot be
     * cached in a static variable, since they hold per-execution data which depends
     * on the Lookup contents. The default PreferencesFactory is provided in the system Lookup
     * and can be overriden in a specialized local Lookup.
     */
    public interface PreferencesFactory {
          /**
           * Creates or acquires Preferences instance for the given MIME path.
           * 
           * @param path mime path
           * @return Preferences instance.
           */
          public Preferences createPreferences(MimePath path);
    }
}
