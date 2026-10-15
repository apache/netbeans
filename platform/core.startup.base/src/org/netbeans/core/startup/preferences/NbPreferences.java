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

package org.netbeans.core.startup.preferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Radek Matous
 */
public abstract class NbPreferences extends AbstractPreferences implements  ChangeListener {
    private static Preferences USER_ROOT;
    private static Preferences SYSTEM_ROOT;
    private ThreadLocal<Boolean> localThread = new ThreadLocal<Boolean>();
    private ArrayList<String> keyEntries = new ArrayList<String>();
    /*private*/ HashMap<String, ArrayList<String>> cachedKeyValues = new HashMap<String, ArrayList<String>>();
    
    /*private*/EditableProperties properties;
    /*private*/FileStorage fileStorage;

    private static final RequestProcessor RP = new RequestProcessor();
    /*private*/final RequestProcessor.Task flushTask = RP.create(new Runnable() {
        @Override
        public void run() {
            fileStorage.runAtomic(new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        try {
                            flushSpi();
                        } catch (BackingStoreException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            });
        }
    },true);
    
    
    public static Preferences userRootImpl() {
        if (USER_ROOT == null) {
            USER_ROOT = new NbPreferences.UserPreferences();
        }
        assert USER_ROOT != null;
        return USER_ROOT;
    }
    
    public static Preferences systemRootImpl() {
        if (SYSTEM_ROOT == null) {
            SYSTEM_ROOT = new NbPreferences.SystemPreferences();
        }
        assert SYSTEM_ROOT != null;
        return SYSTEM_ROOT;
    }

    private NbPreferences(boolean user) {
        super(null, "");
        fileStorage = getFileStorage(absolutePath());
        fileStorage.attachChangeListener(this);
    }
    
    /** Creates a new instance of PreferencesImpl */
    private  NbPreferences(NbPreferences parent, String name)  {
        super(parent, name);
        fileStorage = getFileStorage(absolutePath());
        newNode = !fileStorage.existsNode();
        fileStorage.attachChangeListener(this);
    }
        
    @Override
    protected final String getSpi(String key) {
        return getProperty(key);
    }

    private String getProperty(String key) {
        synchronized (lock) {
            return properties().getProperty(key);
        }
    }
    
    @Override
    protected final String[] childrenNamesSpi() throws BackingStoreException {
        //TODO: cache it if necessary
        return fileStorage.childrenNames();
    }
    
    @Override
    protected final String[] keysSpi() throws BackingStoreException {
        return getKeysSpi();
    }

    private String[] getKeysSpi() throws BackingStoreException {
        synchronized (lock) {
            Set<String> keySet = properties().keySet();
            return keySet.toArray(new String[0]);
        }
    }
    
    @Override
    protected final void putSpi(String key, String value) {
        putProperty(key, value);
        if (Boolean.TRUE.equals(localThread.get())) {
            return;
        }        
        fileStorage.markModified();
        asyncInvocationOfFlushSpi();
    }

    private void putProperty(String key, String value) {
        synchronized (lock) {
            properties().put(key, value);
        }
    }
    
    @Override
    public void put(String key, String value) {
        put(key, value, false);
    }
    
    public void put(String key, String value, boolean triggeredByStateChangedEvent) {
        String oldValue = getSpi(key);
        if (value.equals(oldValue)) {return;}
        try {
            if (super.isRemoved()) {
                return;
            }
            ArrayList<String> cachedValues = cachedKeyValues.get(key);
            if (cachedValues == null) {
                cachedValues = new ArrayList<String>();
            }
            if (triggeredByStateChangedEvent) {
                if (cachedValues.contains(value)) {
                    return;
                }
            } else {
                cachedValues.add(value);
                // The last 100 values added to cache should make it safe to handle any
                // delayed file change event that would set an old value to some property
                if(cachedValues.size() > 1000) {
                    cachedValues.subList(0, 900).clear();
                }
                cachedKeyValues.put(key, cachedValues);
            }
            super.put(key, value);
        } catch (IllegalArgumentException iae) {
            if (iae.getMessage().contains("too long")) {
                // Not for us!
                putSpi(key, value);
            } else {
                throw iae;
            }
        }
    }
    
    @Override
    protected final void removeSpi(String key) {
        removeProperty(key);
        keyEntries.remove(key);
        cachedKeyValues.remove(key);
        if (Boolean.TRUE.equals(localThread.get())) {
            return;
        }
        fileStorage.markModified();
        asyncInvocationOfFlushSpi();
    }

    private void removeProperty(String key) {
        synchronized (lock) {
            properties().remove(key);
        }
    }
    
    @Override
    protected final void removeNodeSpi() throws BackingStoreException {
        try {
            fileStorage.removeNode();
        } catch (IOException ex) {
            throw new BackingStoreException(ex);
        }
    }
    
    private void asyncInvocationOfFlushSpi() {
        if (!fileStorage.isReadOnly()) {
            flushTask.schedule(200);
        }
    }
    
    @Override
    protected  void flushSpi() throws BackingStoreException {
        try {
            synchronized (lock) {
                fileStorage.save(properties());
            }
        } catch (IOException ex) {
            throw new BackingStoreException(ex);
        }
    }
    
    @Override
    protected void syncSpi() throws BackingStoreException {
        if (properties != null) {            
            try {
                putAllProperties(fileStorage.load(), true);
                
            } catch (IOException ex) {
                throw new BackingStoreException(ex);
            }
        }
    }

    private EditableProperties properties()  {
        if (properties == null) {
            properties = new EditableProperties(true);
            //properties.putAll(loadDefaultProperties());
            try {
                putAllProperties(fileStorage.load(), false);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return properties;
    }

    private void putAllProperties(EditableProperties props, boolean clear) {
        synchronized (lock) {
            if (clear) {
                properties().clear();
            }
            properties().putAll(props);
        }
    }

    public @Override final void removeNode() throws BackingStoreException {
        if (fileStorage.isReadOnly()) {
            throw new BackingStoreException("Unsupported operation: read-only storage");//NOI18N
        } else {
            if (super.isRemoved()) {
                return;
            }
            clearProperties();
            super.removeNode();
        }
    }

    private void clearProperties() {
        synchronized (lock) {
            properties().clear();
            keyEntries.clear();
            cachedKeyValues.clear();
        }
    }
    
    public @Override final void flush() throws BackingStoreException {
        if (fileStorage.isReadOnly()) {
            throw new BackingStoreException("Unsupported operation: read-only storage");//NOI18N
        } else {
            super.flush();
            cachedKeyValues.clear();
        }
    }
    
    public @Override final void sync() throws BackingStoreException {
        if (fileStorage.isReadOnly()) {
            throw new BackingStoreException("Unsupported operation: read-only storage");//NOI18N
        } else {
            if (super.isRemoved()) {
                return;
            }
            flushTask.waitFinished();
            super.sync();
            cachedKeyValues.clear();
        }
    }

    public @Override void stateChanged(ChangeEvent e) {
        synchronized(lock){
            Boolean previewState = localThread.get();
            EditableProperties ep = null;
            ArrayList<String> entries2add = new ArrayList<String>();
            try {
                localThread.set(Boolean.TRUE);
                ep = fileStorage.load();
                Iterator<Entry<String, String>> iter = ep.entrySet().iterator();
                while(iter.hasNext()) {
                    Entry entry = iter.next();
                    if (keyEntries.isEmpty() || keyEntries.contains(entry.getKey().toString())) {
                        put(entry.getKey().toString(), entry.getValue().toString(), true);
                        entries2add.add(entry.getKey().toString());
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
		ArrayList<String> entries2remove = new ArrayList<String>();
                for(String key : keyEntries) {
                    if (!entries2add.contains(key)) {
                        if (super.isRemoved()) {
                            continue;
                        }
			entries2remove.add(key);
                    }
                }
		for(String key : entries2remove) {
		    remove(key);
		}
                keyEntries.clear();
                keyEntries.addAll(entries2add);
                localThread.set(previewState);
            }
        }
    }

    protected abstract FileStorage getFileStorage(String absolutePath);

    public static class UserPreferences extends NbPreferences {
        public UserPreferences() {
            super(true);
        }
        
        /** Creates a new instance */
        private UserPreferences(NbPreferences parent, String name)  {
            super(parent, name);
        }
        
        @Override
        protected AbstractPreferences childSpi(String name) {
            return new UserPreferences(this, name);
        }

        @Override
        protected NbPreferences.FileStorage getFileStorage(String absolutePath) {
            // work with user files
            return PropertiesStorage.instance(FileUtil.getConfigRoot(), absolutePath());
        }
    }
    
    private static final class SystemPreferences extends NbPreferences {
        private SystemPreferences() {
            super(false);
        }
        
        private SystemPreferences(NbPreferences parent, String name) {
            super(parent, name);
        }
        
        @Override
        protected AbstractPreferences childSpi(String name) {
            return new SystemPreferences(this, name);
        }

        @Override
        protected NbPreferences.FileStorage getFileStorage(String absolutePath) {
            // work with system files
            return PropertiesStorage.instanceReadOnly(FileUtil.getSystemConfigRoot(), absolutePath());            
        }
    }
    
    interface FileStorage {
        boolean isReadOnly();
        String[] childrenNames();
        boolean existsNode();
        void removeNode() throws IOException;
        void markModified();
        EditableProperties load() throws IOException;
        void save(final EditableProperties properties) throws IOException;
        void runAtomic(Runnable run);
        void attachChangeListener(ChangeListener changeListener);

    }
}
