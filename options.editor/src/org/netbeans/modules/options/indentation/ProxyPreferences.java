/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
            listeners = prefListeners.toArray(new PreferenceChangeListener[prefListeners.size()]);
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
            listeners = nodeListeners.toArray(new NodeChangeListener[nodeListeners.size()]);
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
            listeners = nodeListeners.toArray(new NodeChangeListener[nodeListeners.size()]);
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
