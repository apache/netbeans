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

package org.netbeans.modules.options.indentation;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.modules.editor.settings.storage.api.MemoryPreferences;
import org.openide.util.WeakListeners;

/**
 * This class has been obsoleted by {@link MemoryPreferences}
 * 
 * @author vita
 * @deprecated Please use {@link MemoryPreferences} API instead
 */
@Deprecated
public final class ProxyPreferences extends Preferences implements PreferenceChangeListener, NodeChangeListener {
    
    private final MemoryPreferences delegateRoot;
    private Preferences       delegate;
    private boolean noEvents;
    
    public static ProxyPreferences getProxyPreferences(Object token, Preferences delegate) {
        return new ProxyPreferences(null, MemoryPreferences.get(token, delegate), null);
    }

    public void silence() {
        synchronized (delegate) {
            this.noEvents = true;
        }
    }

    public void destroy() {
        delegateRoot.destroy();
    }

    @Override
    public void put(String key, String value) {
        delegate.put(key, value);
    }

    @Override
    public String get(String key, String def) {
        return delegate.get(key, def);
    }

    @Override
    public void remove(String key) {
        delegate.remove(key);
    }

    @Override
    public void clear() throws BackingStoreException {
        delegate.clear();
    }

    @Override
    public void putInt(String key, int value) {
        delegate.putInt(key, value);
    }

    @Override
    public int getInt(String key, int def) {
        return delegate.getInt(key, def);
    }

    @Override
    public void putLong(String key, long value) {
        delegate.putLong(key, value);
    }

    @Override
    public long getLong(String key, long def) {
        return delegate.getLong(key, def);
    }

    @Override
    public void putBoolean(String key, boolean value) {
        delegate.putBoolean(key, value);
    }

    @Override
    public boolean getBoolean(String key, boolean def) {
        return delegate.getBoolean(key, def);
    }

    @Override
    public void putFloat(String key, float value) {
        delegate.putFloat(key, value);
    }

    @Override
    public float getFloat(String key, float def) {
        return delegate.getFloat(key, def);
    }

    @Override
    public void putDouble(String key, double value) {
        delegate.putDouble(key, value);
    }

    @Override
    public double getDouble(String key, double def) {
        return delegate.getDouble(key, def);
    }

    @Override
    public void putByteArray(String key, byte[] value) {
        delegate.putByteArray(key, value);
    }

    @Override
    public byte[] getByteArray(String key, byte[] def) {
        return delegate.getByteArray(key, def);
    }

    @Override
    public String[] keys() throws BackingStoreException {
        return delegate.keys();
    }

    @Override
    public String[] childrenNames() throws BackingStoreException {
        return delegate.childrenNames();
    }

    @Override
    public Preferences parent() {
        return parent;
    }

    @Override
    public Preferences node(String pathName) {
        synchronized (this) {
            Preferences pref = children.get(pathName);
            if (pref != null) {
                return pref;
            }
        }
        Preferences pref = delegate.node(pathName);
        ProxyPreferences result;
        
        synchronized (this) {
            result = children.get(pathName);
            if (result != null) {
                return result;
            }
            result = new ProxyPreferences(this, delegateRoot, pref);
            children.put(pathName, result);
            return result;
        }
    }

    @Override
    public boolean nodeExists(String pathName) throws BackingStoreException {
        return delegate.nodeExists(pathName);
    }

    @Override
    public void removeNode() throws BackingStoreException {
        delegate.removeNode();
        if (parent != null) {
            parent.nodeRemoved(this);
        }
    }

    @Override
    public String name() {
        return delegate.name();
    }

    @Override
    public String absolutePath() {
        return delegate.absolutePath();
    }

    @Override
    public boolean isUserNode() {
        return delegate.isUserNode();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public void flush() throws BackingStoreException {
        delegate.flush();
    }

    @Override
    public void sync() throws BackingStoreException {
        delegate.sync();
    }
    
    @Override
    public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
        synchronized (this) {
            prefListeners.add(pcl);
        }
    }

    @Override
    public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
        synchronized (this) {
            prefListeners.remove(pcl);
        }
    }

    @Override
    public void addNodeChangeListener(NodeChangeListener ncl) {
        synchronized (this) {
            nodeListeners.add(ncl);
        }
    }

    @Override
    public void removeNodeChangeListener(NodeChangeListener ncl) {
        synchronized (this) {
            nodeListeners.remove(ncl);
        }
    }

    @Override
    public void exportNode(OutputStream os) throws IOException, BackingStoreException {
        throw new UnsupportedOperationException("exportNode not supported");
    }

    @Override
    public void exportSubtree(OutputStream os) throws IOException, BackingStoreException {
        throw new UnsupportedOperationException("exportSubtree not supported");
    }

    // ------------------------------------------------------------------------
    // PreferenceChangeListener implementation
    // ------------------------------------------------------------------------

    public void preferenceChange(PreferenceChangeEvent evt) {
        PreferenceChangeListener [] listeners;
        synchronized (this) {
            if (noEvents) {
                return;
            }
            listeners = prefListeners.toArray(new PreferenceChangeListener[0]);
        }

        PreferenceChangeEvent myEvt = null;
        for(PreferenceChangeListener l : listeners) {
            if (myEvt == null) {
                myEvt = new PreferenceChangeEvent(this, evt.getKey(), evt.getNewValue());
            }
            l.preferenceChange(myEvt);
        }
    }
    
    private synchronized void changeDelegate(Preferences del) {
        this.delegate = del;
    }
    
    private synchronized void nodeRemoved(ProxyPreferences child) {
        children.values().remove(child);
    }

    // ------------------------------------------------------------------------
    // NodeChangeListener implementation
    // ------------------------------------------------------------------------

    public void childAdded(NodeChangeEvent evt) {
        NodeChangeListener [] listeners;
        Preferences childNode;

        synchronized (this) {
            String childName = evt.getChild().name();
            childNode = children.get(childName);
            if (childNode != null) {
                // swap delegates
                ((ProxyPreferences) childNode).changeDelegate(evt.getChild());
            } else {
                childNode = node(evt.getChild().name());
            }
            if (noEvents) {
                return;
            }
            listeners = nodeListeners.toArray(new NodeChangeListener[0]);
        }

        NodeChangeEvent myEvt = null;
        for(NodeChangeListener l : listeners) {
            if (myEvt == null) {
                myEvt = new NodeChangeEvent(this, childNode);
            }
            l.childAdded(evt);
        }
    }

    public void childRemoved(NodeChangeEvent evt) {
        NodeChangeListener [] listeners;
        Preferences childNode;

        synchronized (this) {
            String childName = evt.getChild().name();

            childNode = children.get(childName);
            if (childNode == null) {
                // nobody has accessed the child yet
                return;
            }
            
            if (noEvents) {
                return;
            }
            listeners = nodeListeners.toArray(new NodeChangeListener[0]);
        }

        NodeChangeEvent myEvt = null;
        for(NodeChangeListener l : listeners) {
            if (myEvt == null) {
                myEvt = new NodeChangeEvent(this, childNode);
            }
            l.childAdded(evt);
        }
    }
    
    private final ProxyPreferences parent;
    private final Map<String, ProxyPreferences> children = new HashMap<String, ProxyPreferences>();

    private PreferenceChangeListener weakPrefListener;
    private final Set<PreferenceChangeListener> prefListeners = new HashSet<PreferenceChangeListener>();
    private NodeChangeListener weakNodeListener;
    private final Set<NodeChangeListener> nodeListeners = new HashSet<NodeChangeListener>();

    private ProxyPreferences(ProxyPreferences parent, MemoryPreferences memoryPref, Preferences delegate) {
        this.parent = parent;
        this.delegateRoot = memoryPref;
        this.delegate = delegate == null ? memoryPref.getPreferences() : delegate;
        weakPrefListener = WeakListeners.create(PreferenceChangeListener.class, this, delegate);
        this.delegate.addPreferenceChangeListener(weakPrefListener);
        weakNodeListener = WeakListeners.create(NodeChangeListener.class, this, delegate);
        this.delegate.addNodeChangeListener(weakNodeListener);
    }
}
