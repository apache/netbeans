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

package org.netbeans.modules.editor.settings.storage.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.settings.storage.ApiAccessor;
import org.netbeans.modules.editor.settings.storage.SettingsType;
import org.netbeans.modules.editor.settings.storage.StorageImpl;
import org.netbeans.modules.editor.settings.storage.spi.StorageDescription;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author vita
 */
public final class EditorSettingsStorage <K extends Object, V extends Object> {

    public static final String PROP_DATA = "EditorSettingsStorage.PROP_DATA"; //NOI18N
    
    private static RequestProcessor RP = new RequestProcessor(EditorSettingsStorage.class);
    
    public static <K extends Object, V extends Object> EditorSettingsStorage<K, V> get(String settingsTypeId) {
        EditorSettingsStorage<K, V> ess = EditorSettingsStorage.<K, V>find(settingsTypeId);
        assert ess != null : "Invalid settings type Id: '" + settingsTypeId + "'"; //NOI18N
        return ess;
    }
    
   @SuppressWarnings("unchecked")
    public static <K extends Object, V extends Object> EditorSettingsStorage<K, V> find(String settingsTypeId) {
        StorageDescription<K, V> sd = SettingsType.<K, V>find(settingsTypeId);
        // must not cache in a static cache, cache can be found in [contextual] Lookup.
        return Lookup.getDefault().lookup(StorageImpl.StorageCache.class).createStorage(sd);
    }
    
    public Map<K, V> load(MimePath mimePath, String profile, boolean defaults) throws IOException {
        return storageImpl.load(mimePath, profile, defaults);
    }
    
    public void save(final MimePath mimePath, final String profile, final boolean defaults, final Map<K, V> data) throws IOException {
        if (SwingUtilities.isEventDispatchThread()) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        storageImpl.save(mimePath, profile, defaults, data);
                    } catch (IOException ioe) {
                        Logger.getLogger(EditorSettingsStorage.class.getName()).log(Level.WARNING, null, ioe);
                    }
                }
            });
        } else {
            storageImpl.save(mimePath, profile, defaults, data);
        }
    }

    public void delete(MimePath mimePath, String profile, boolean defaults) throws IOException {
        storageImpl.delete(mimePath, profile, defaults);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        PCS.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        PCS.removePropertyChangeListener(l);
    }
    
    // ------------------------------------------
    // private implementation
    // ------------------------------------------

    private final PropertyChangeSupport PCS = new PropertyChangeSupport(this);
    private final StorageImpl<K, V> storageImpl;
    
    private EditorSettingsStorage(StorageDescription<K, V> storageDescription) {
        this.storageImpl = new StorageImpl<K, V>(storageDescription, new Callable<Void>() {
            public Void call() {
                PCS.firePropertyChange(PROP_DATA, null, null);
                return null;
            }
        });
    }
    
    static {
        ApiAccessor.register(new ApiAccessor() {
            @Override
            public <K,V> EditorSettingsStorage<K,V> createSettingsStorage(StorageDescription<K, V> storageDescription) {
                return new EditorSettingsStorage<>(storageDescription);
            }
        });
    }
}
