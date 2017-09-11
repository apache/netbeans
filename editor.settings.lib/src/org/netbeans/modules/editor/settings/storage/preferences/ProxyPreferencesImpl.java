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

package org.netbeans.modules.editor.settings.storage.preferences;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.xml.bind.DatatypeConverter;
import org.netbeans.modules.editor.settings.storage.api.OverridePreferences;
import org.netbeans.modules.editor.settings.storage.spi.TypedValue;
import org.openide.util.WeakListeners;

/**
 * Preferences impl that stores changes locally, and propagates them upon flush().
 * The implementation is an adapted (former) implementation from org.netbeans.modules.options.indentation.ProxyPreferences.
 * The original was moved here, and adapted to work with 'diamond' double defaulting: 1st default is
 * the persistent Preferences object, where the changes will be finally propagated. The 2nd default
 * is the Preferences object for the MIMEtype parent (if it exists). Keys that do not exist
 * in the stored Preferences should delegate to the MIME parent. During editing, the MIME parent Preferences
 * may get also changed, so we cannot rely on delegation between stored Mime Preferences, but must
 * inject an additional path - see {@link InheritedPreferences}.
 *
 * @author sdedic
 * @author vita
 */
public final class ProxyPreferencesImpl extends Preferences implements PreferenceChangeListener, NodeChangeListener, 
        OverridePreferences {

    /**
     * Inherited preferences, for the case that key does not exist at our Node.
     * Special handling for diamond inheritance. 
     */
    private Preferences inheritedPrefs;
    
    public static ProxyPreferencesImpl getProxyPreferences(Object token, Preferences delegate) {
        return Tree.getTree(token, delegate).get(null, delegate.name(), delegate); //NOI18N
    }
    
    public boolean isDirty() {
        synchronized (tree.treeLock()) {
            return !(data.isEmpty() && removedKeys.isEmpty() && children.isEmpty() && removedChildren.isEmpty()) || removed;
        }
    }

    @Override
    public void put(String key, String value) {
        _put(key, value, String.class.getName());
    }

    @Override
    public String get(String key, String def) {
        synchronized (tree.treeLock()) {
            checkNotNull(key, "key"); //NOI18N
            checkRemoved();
            
            if (removedKeys.contains(key)) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Key '" + key + "' removed, using default '" + def + "'"); //NOI18N
                }
                // removes will be flushed to the preferences, but now we need to see the defaults
                // that WILL become effective after flush of this object.
                if (inheritedPrefs != null) {
                    return inheritedPrefs.get(key, def);
                } else {
                    return def;
                }
            } else {
                TypedValue typedValue = data.get(key);
                if (typedValue != null) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Key '" + key + "' modified, local value '" + typedValue.getValue() + "'"); //NOI18N
                    }
                    return typedValue.getValue();
                } else if (delegate != null) {
                    String value = delegate.get(key, def);
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Key '" + key + "' undefined, original value '" + value + "'"); //NOI18N
                    }
                    return value;
                } else {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Key '" + key + "' undefined, '" + name + "' is a new node, using default '" + def + "'"); //NOI18N
                    }
                    return def;
                }
            }
        }
    }

    @Override
    public void remove(String key) {
        EventBag<PreferenceChangeListener, PreferenceChangeEvent> bag = null;
        
        // do not try to remove a non-existent key; it could lead to ugly 'remove' entries persisted in settings.
        if (get(key, null) == null) {
            return;
        }
        synchronized (tree.treeLock()) {
            checkNotNull(key, "key"); //NOI18N
            checkRemoved();
            if (removedKeys.add(key)) {
                data.remove(key);
                bag = new EventBag<PreferenceChangeListener, PreferenceChangeEvent>();
                bag.addListeners(prefListeners);
                if (inheritedPrefs != null) {
                    bag.addEvent(new PreferenceChangeEvent(this, key, 
                            inheritedPrefs.get(key, null)));
                } else {
                    bag.addEvent(new PreferenceChangeEvent(this, key, null));
                }
            }
        }

        if (bag != null) {
            firePrefEvents(Collections.singletonList(bag));
        }
    }

    @Override
    public void clear() throws BackingStoreException {
        EventBag<PreferenceChangeListener, PreferenceChangeEvent> bag = new EventBag<PreferenceChangeListener, PreferenceChangeEvent>();
        
        synchronized (tree.treeLock()) {
            checkRemoved();

            // Determine modified or added keys
            Set<String> keys = new HashSet<String>();
            keys.addAll(data.keySet());
            keys.removeAll(removedKeys);
            if (!keys.isEmpty()) {
                for(String key : keys) {
                    String value = delegate == null ? null : delegate.get(key, null);
                    bag.addEvent(new PreferenceChangeEvent(this, key, value));
                }
            }

            // Determine removed keys
            if (delegate != null) {
                for(String key : removedKeys) {
                    String value = delegate.get(key, null);
                    if (value != null) {
                        bag.addEvent(new PreferenceChangeEvent(this, key, value));
                    }
                }
            }

            // Initialize bag's listeners
            bag.addListeners(prefListeners);

            // Finally, remove the data
            data.clear();
            removedKeys.clear();
        }
        
        firePrefEvents(Collections.singletonList(bag));
    }

    @Override
    public void putInt(String key, int value) {
        _put(key, Integer.toString(value), Integer.class.getName());
    }

    @Override
    public int getInt(String key, int def) {
        String value = get(key, null);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
                // ignore
            }
        }
        return def;
    }

    @Override
    public void putLong(String key, long value) {
        _put(key, Long.toString(value), Long.class.getName());
    }

    @Override
    public long getLong(String key, long def) {
        String value = get(key, null);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException nfe) {
                // ignore
            }
        }
        return def;
    }

    @Override
    public void putBoolean(String key, boolean value) {
        _put(key, Boolean.toString(value), Boolean.class.getName());
    }

    @Override
    public boolean getBoolean(String key, boolean def) {
        String value = get(key, null);
        if (value != null) {
            return Boolean.parseBoolean(value);
        } else {
            return def;
        }
    }

    @Override
    public void putFloat(String key, float value) {
        _put(key, Float.toString(value), Float.class.getName());
    }

    @Override
    public float getFloat(String key, float def) {
        String value = get(key, null);
        if (value != null) {
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException nfe) {
                // ignore
            }
        }
        return def;
    }

    @Override
    public void putDouble(String key, double value) {
        _put(key, Double.toString(value), Double.class.getName());
    }

    @Override
    public double getDouble(String key, double def) {
        String value = get(key, null);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException nfe) {
                // ignore
            }
        }
        return def;
    }

    @Override
    public void putByteArray(String key, byte[] value) {
        _put(key, DatatypeConverter.printBase64Binary(value), value.getClass().getName());
    }

    @Override
    public byte[] getByteArray(String key, byte[] def) {
        String value = get(key, null);
        if (value != null) {
            byte [] decoded = DatatypeConverter.parseBase64Binary(value);
            if (decoded != null) {
                return decoded;
            }
        }
        return def;
    }

    @Override
    public String[] keys() throws BackingStoreException {
        synchronized (tree.treeLock()) {
            checkRemoved();
            HashSet<String> keys = new HashSet<String>();
            if (delegate != null) {
                keys.addAll(Arrays.asList(delegate.keys()));
            }
            keys.addAll(data.keySet());
            keys.removeAll(removedKeys);
            return keys.toArray(new String [keys.size()]);
        }
    }

    @Override
    public String[] childrenNames() throws BackingStoreException {
        synchronized (tree.treeLock()) {
            checkRemoved();
            HashSet<String> names = new HashSet<String>();
            if (delegate != null) {
                names.addAll(Arrays.asList(delegate.childrenNames()));
            }
            names.addAll(children.keySet());
            names.removeAll(removedChildren);
            return names.toArray(new String [names.size()]);
        }
    }

    @Override
    public Preferences parent() {
        synchronized (tree.treeLock()) {
            checkRemoved();
            return parent;
        }
    }

    @Override
    public Preferences node(String pathName) {
        Preferences node;
        LinkedList<EventBag<NodeChangeListener, NodeChangeEvent>> events = new LinkedList<EventBag<NodeChangeListener, NodeChangeEvent>>();

        synchronized (tree.treeLock()) {
            checkNotNull(pathName, "pathName"); //NOI18N
            checkRemoved();
            node = node(pathName, true, events);
        }

        fireNodeEvents(events);
        return node;
    }

    @Override
    public boolean nodeExists(String pathName) throws BackingStoreException {
        synchronized (tree.treeLock()) {
            if (pathName.length() == 0) {
                return !removed;
            } else {
                checkRemoved();
                return node(pathName, false, null) != null;
            }
        }
    }

    @Override
    public void removeNode() throws BackingStoreException {
        synchronized (tree.treeLock()) {
            checkRemoved();
            ProxyPreferencesImpl p = parent;
            if (p != null) {
                p.removeChild(this);
            } else {
                throw new UnsupportedOperationException("Can't remove the root."); //NOI18N
            }
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String absolutePath() {
        synchronized (tree.treeLock()) {
            ProxyPreferencesImpl pp = parent;
            if (pp != null) {
                if (pp.parent == null) {
                    // pp is the root, we don't want two consecutive slashes in the path
                    return "/" + name(); //NOI18N
                } else {
                    return pp.absolutePath() + "/" + name(); //NOI18N
                }
            } else {
                return "/"; //NOI18N
            }
        }
    }

    @Override
    public boolean isUserNode() {
        synchronized (tree.treeLock()) {
            if (delegate != null) {
                return delegate.isUserNode();
            } else {
                ProxyPreferencesImpl pp = parent;
                if (pp != null) {
                    return pp.isUserNode();
                } else {
                    return true;
                }
            }
        }
    }

    @Override
    public String toString() {
        return (isUserNode() ? "User" : "System") + " Preference Node: " + absolutePath(); //NOI18N
    }

    @Override
    public void flush() throws BackingStoreException {
        synchronized (tree.treeLock()) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Flushing " + absolutePath());
            }

            checkRemoved();
            for(ProxyPreferencesImpl pp : children.values()) {
                pp.flush();
            }

            if (delegate == null) {
                ProxyPreferencesImpl proxyRoot = parent.node("/", false, null); //NOI18N
                assert proxyRoot != null : "Root must always exist"; //NOI18N

                Preferences delegateRoot = proxyRoot.delegate;
                assert delegateRoot != null : "Root must always have its corresponding delegate"; //NOI18N

                Preferences nueDelegate = delegateRoot.node(absolutePath());
                changeDelegate(nueDelegate);
            }

            delegate.removeNodeChangeListener(weakNodeListener);
            delegate.removePreferenceChangeListener(weakPrefListener);
            try {
                // remove all removed children
                for(String childName : removedChildren) {
                    if (delegate.nodeExists(childName)) {
                        delegate.node(childName).removeNode();
                    }
                }

                // write all valid key-value pairs
                for(String key : data.keySet()) {
                    if (!removedKeys.contains(key)) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("Writing " + absolutePath() + "/" + key + "=" + data.get(key));
                        }
                        
                        TypedValue typedValue = data.get(key);
                        if (String.class.getName().equals(typedValue.getJavaType())) {
                            delegate.put(key, typedValue.getValue());

                        } else if (Integer.class.getName().equals(typedValue.getJavaType())) {
                            delegate.putInt(key, Integer.parseInt(typedValue.getValue()));

                        } else if (Long.class.getName().equals(typedValue.getJavaType())) {
                            delegate.putLong(key, Long.parseLong(typedValue.getValue()));

                        } else if (Boolean.class.getName().equals(typedValue.getJavaType())) {
                            delegate.putBoolean(key, Boolean.parseBoolean(typedValue.getValue()));

                        } else if (Float.class.getName().equals(typedValue.getJavaType())) {
                            delegate.putFloat(key, Float.parseFloat(typedValue.getValue()));

                        } else if (Double.class.getName().equals(typedValue.getJavaType())) {
                            delegate.putDouble(key, Double.parseDouble(typedValue.getValue()));

                        } else {
                            delegate.putByteArray(key, DatatypeConverter.parseBase64Binary(typedValue.getValue()));
                        }
                    }
                }
                data.clear();

                // remove all removed keys
                for(String key : removedKeys) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Removing " + absolutePath() + "/" + key);
                    }
                    delegate.remove(key);
                }
                removedKeys.clear();
            } finally {
                delegate.addNodeChangeListener(weakNodeListener);
                delegate.addPreferenceChangeListener(weakPrefListener);
            }
        }        
    }

    @Override
    public void sync() throws BackingStoreException {
        ArrayList<EventBag<PreferenceChangeListener, PreferenceChangeEvent>> prefEvents = new ArrayList<EventBag<PreferenceChangeListener, PreferenceChangeEvent>>();
        ArrayList<EventBag<NodeChangeListener, NodeChangeEvent>> nodeEvents = new ArrayList<EventBag<NodeChangeListener, NodeChangeEvent>>();

        synchronized (tree.treeLock()) {
            _sync(prefEvents, nodeEvents);
        }

        fireNodeEvents(nodeEvents);
        firePrefEvents(prefEvents);
    }

    @Override
    public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
        synchronized (tree.treeLock()) {
            prefListeners.add(pcl);
        }
    }

    @Override
    public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
        synchronized (tree.treeLock()) {
            prefListeners.remove(pcl);
        }
    }

    @Override
    public void addNodeChangeListener(NodeChangeListener ncl) {
        synchronized (tree.treeLock()) {
            nodeListeners.add(ncl);
        }
    }

    @Override
    public void removeNodeChangeListener(NodeChangeListener ncl) {
        synchronized (tree.treeLock()) {
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
        String nValue = evt.getNewValue();
        String k = evt.getKey();
        synchronized (tree.treeLock()) {
            if (removed || data.containsKey(k)) {
                return;
            }
            if (removedKeys.contains(k)) {
                if (inheritedPrefs == null) {
                    return;
                } else {
                    // if removed && there are inherited preferences, we must report the 'new value'
                    // from the inherited prefs, as the override in our preferences is not in effect now.
                    nValue = inheritedPrefs.get(k, null);
                    if (nValue == null) {
                         // even inherited preferences does not contain a value, we've already removed it -> no event needed.
                        return;
                    }
                }
            }
            listeners = prefListeners.toArray(new PreferenceChangeListener[prefListeners.size()]);
        }

        PreferenceChangeEvent myEvt = null;
        for(PreferenceChangeListener l : listeners) {
            if (myEvt == null) {
                myEvt = new PreferenceChangeEvent(this, k, nValue);
            }
            l.preferenceChange(myEvt);
        }
    }

    // ------------------------------------------------------------------------
    // NodeChangeListener implementation
    // ------------------------------------------------------------------------

    public void childAdded(NodeChangeEvent evt) {
        NodeChangeListener [] listeners;
        Preferences childNode;

        synchronized (tree.treeLock()) {
            String childName = evt.getChild().name();
            if (removed || removedChildren.contains(childName)) {
                return;
            }

            childNode = children.get(childName);
            if (childNode != null) {
                // swap delegates
                ((ProxyPreferencesImpl) childNode).changeDelegate(evt.getChild());
            } else {
                childNode = node(evt.getChild().name());
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

        synchronized (tree.treeLock()) {
            String childName = evt.getChild().name();
            if (removed || removedChildren.contains(childName)) {
                return;
            }

            childNode = children.get(childName);
            if (childNode != null) {
                // swap delegates
                ((ProxyPreferencesImpl) childNode).changeDelegate(null);
            } else {
                // nobody has accessed the child yet
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
    
    // ------------------------------------------------------------------------
    // Other public implementation
    // ------------------------------------------------------------------------

    /**
     * Destroys whole preferences tree as if called on the root.
     */
    public void destroy() {
        synchronized (tree.treeLock()) {
            tree.destroy();
        }
    }
    
    public void silence() {
        synchronized (tree.treeLock()) {
            noEvents = true;
        }
    }
    
    public void noise() {
        synchronized (tree.treeLock()) {
            noEvents = false;
        }
    }

    @Override
    public boolean isOverriden(String key) {
        if (data.containsKey(key)) {
            return true;
        }
        // if the storage delegate overrides the key, return also true.
        if (delegate instanceof OverridePreferences) {
            return ((OverridePreferences)delegate).isOverriden(key) && !removedKeys.contains(key);
        }
        return false;
    }
    
    // ------------------------------------------------------------------------
    // private implementation
    // ------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(ProxyPreferencesImpl.class.getName());
    
    private final ProxyPreferencesImpl parent;
    private final String name;
    private Preferences delegate;
    private final Tree tree;
    private boolean removed;
    
    private final Map<String, TypedValue> data = new HashMap<String, TypedValue>();
    private final Set<String> removedKeys = new HashSet<String>();
    private final Map<String, ProxyPreferencesImpl> children = new HashMap<String, ProxyPreferencesImpl>();
    private final Set<String> removedChildren = new HashSet<String>();

    private boolean noEvents = false;
    private PreferenceChangeListener weakPrefListener;
    private final Set<PreferenceChangeListener> prefListeners = new HashSet<PreferenceChangeListener>();
    private NodeChangeListener weakNodeListener;
    private final Set<NodeChangeListener> nodeListeners = new HashSet<NodeChangeListener>();

    private ProxyPreferencesImpl(ProxyPreferencesImpl parent, String name, Preferences delegate, Tree tree) {
        assert name != null;
        
        this.parent = parent;
        this.name = name;
        this.delegate = delegate;
        if (delegate instanceof InheritedPreferences) {
            this.inheritedPrefs = ((InheritedPreferences)delegate).getParent();
        }
        if (delegate != null) {
            assert name.equals(delegate.name());

            weakPrefListener = WeakListeners.create(PreferenceChangeListener.class, this, delegate);
            delegate.addPreferenceChangeListener(weakPrefListener);
            
            weakNodeListener = WeakListeners.create(NodeChangeListener.class, this, delegate);
            delegate.addNodeChangeListener(weakNodeListener);
        }
        this.tree = tree;
    }

    private void _put(String key, String value, String javaType) {
        EventBag<PreferenceChangeListener, PreferenceChangeEvent> bag = null;

        synchronized (tree.treeLock()) {
            checkNotNull(key, "key"); //NOI18N
            checkNotNull(value, "value"); //NOI18N
            checkRemoved();
            
            String orig = get(key, null);
            if (orig == null || !orig.equals(value)) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Overwriting '" + key + "' = '" + value + "'"); //NOI18N
                }
                
                data.put(key, new TypedValue(value, javaType));
                removedKeys.remove(key);
                
                bag = new EventBag<PreferenceChangeListener, PreferenceChangeEvent>();
                bag.addListeners(prefListeners);
                bag.addEvent(new PreferenceChangeEvent(this, key, value));
            }
        }

        if (bag != null) {
            firePrefEvents(Collections.singletonList(bag));
        }
    }

    private ProxyPreferencesImpl node(String pathName, boolean create, List<EventBag<NodeChangeListener, NodeChangeEvent>> events) {
        if (pathName.length() > 0 && pathName.charAt(0) == '/') { //NOI18N
            // absolute path, if this is not the root then find the root
            // and pass the call to it
            if (parent != null) {
                Preferences root = this;
                while (root.parent() != null) {
                    root = root.parent();
                }
                return ((ProxyPreferencesImpl) root).node(pathName, create, events);
            } else {
                // this is the root, change the pathName to a relative path and proceed
                pathName = pathName.substring(1);
            }
        }

        if (pathName.length() > 0) {
            String childName;
            String pathFromChild;

            int idx = pathName.indexOf('/'); //NOI18N
            if (idx != -1) {
                childName = pathName.substring(0, idx);
                pathFromChild = pathName.substring(idx + 1);
            } else {
                childName = pathName;
                pathFromChild = null;
            }

            ProxyPreferencesImpl child = children.get(childName);
            if (child == null) {
                if (removedChildren.contains(childName) && !create) {
                    // this child has been removed
                    return null;
                }
                
                Preferences childDelegate = null;
                try {
                    if (delegate != null && delegate.nodeExists(childName)) {
                        childDelegate = delegate.node(childName);
                    }
                } catch (BackingStoreException bse) {
                    // ignore
                }

                if (childDelegate != null || create) {
                    child = tree.get(this, childName, childDelegate);
                    children.put(childName, child);
                    removedChildren.remove(childName);

                    // fire event if we really created the new child node
                    if (childDelegate == null) {
                        EventBag<NodeChangeListener, NodeChangeEvent> bag = new EventBag<NodeChangeListener, NodeChangeEvent>();
                        bag.addListeners(nodeListeners);
                        bag.addEvent(new NodeChangeEventExt(this, child, false));
                        events.add(bag);
                    }
                } else {
                    // childDelegate == null && !create
                    return null;
                }
            } else {
                assert !child.removed;
            }

            return pathFromChild != null ? child.node(pathFromChild, create, events) : child;
        } else {
            return this;
        }
    }

    private void addChild(ProxyPreferencesImpl child) {
        ProxyPreferencesImpl pp = children.get(child.name());
        if (pp == null) {
            children.put(child.name(), child);
        } else {
            assert pp == child;
        }
    }
    
    private void removeChild(ProxyPreferencesImpl child) {
        assert child != null;
        assert children.get(child.name()) == child;

        child.nodeRemoved();
        children.remove(child.name());
        removedChildren.add(child.name());
    }
    
    private void nodeRemoved() {
        for(ProxyPreferencesImpl pp : children.values()) {
            pp.nodeRemoved();
        }

        data.clear();
        removedKeys.clear();
        children.clear();
        removedChildren.clear();
        tree.removeNode(this);
        
        removed = true;
    }
    
    private void checkNotNull(Object paramValue, String paramName) {
        if (paramValue == null) {
            throw new NullPointerException("The " + paramName + " must not be null");
        }
    }

    private void checkRemoved() {
        if (removed) {
            throw new IllegalStateException("The node '" + this + " has already been removed."); //NOI18N
        }
    }

    private void changeDelegate(Preferences nueDelegate) {
        if (delegate != null) {
            try {
                if (delegate.nodeExists("")) { //NOI18N
                    assert weakPrefListener != null;
                    assert weakNodeListener != null;
                    delegate.removePreferenceChangeListener(weakPrefListener);
                    delegate.removeNodeChangeListener(weakNodeListener);
                }
            } catch (BackingStoreException bse) {
                LOG.log(Level.WARNING, null, bse);
            }
        }

        delegate = nueDelegate;
        weakPrefListener = null;
        weakNodeListener = null;
        
        if (delegate != null) {
            weakPrefListener = WeakListeners.create(PreferenceChangeListener.class, this, delegate);
            delegate.addPreferenceChangeListener(weakPrefListener);
            
            weakNodeListener = WeakListeners.create(NodeChangeListener.class, this, delegate);
            delegate.addNodeChangeListener(weakNodeListener);
        }
    }

    private void _sync(
        List<EventBag<PreferenceChangeListener, PreferenceChangeEvent>> prefEvents, 
        List<EventBag<NodeChangeListener, NodeChangeEvent>> nodeEvents
    ) {
        // synchronize all children firts
        for(ProxyPreferencesImpl pp : children.values()) {
            pp._sync(prefEvents, nodeEvents);
        }

        // report all new children as removed
        EventBag<NodeChangeListener, NodeChangeEvent> nodeBag = new EventBag<NodeChangeListener, NodeChangeEvent>();
        nodeBag.addListeners(nodeListeners);

        for(ProxyPreferencesImpl pp : children.values()) {
            if (pp.delegate == null) {
                // new node that does not have corresponding node in the original hierarchy
                nodeBag.addEvent(new NodeChangeEventExt(this, pp, true));
            }
        }

        if (!nodeBag.getEvents().isEmpty()) {
            nodeEvents.add(nodeBag);
        }

        // report all modified keys
        if (delegate != null) {
            EventBag<PreferenceChangeListener, PreferenceChangeEvent> prefBag = new EventBag<PreferenceChangeListener, PreferenceChangeEvent>();
            prefBag.addListeners(prefListeners);
            prefEvents.add(prefBag);

            for(String key : data.keySet()) {
                prefBag.addEvent(new PreferenceChangeEvent(this, key, delegate.get(key, data.get(key).getValue())));
            }
        } // else there is no corresponding node in the orig hierarchy and this node
          // will be reported as removed

        // erase modified data
        for(NodeChangeEvent nce : nodeBag.getEvents()) {
            children.remove(nce.getChild().name());
        }
        data.clear();
    }

    private void firePrefEvents(List<EventBag<PreferenceChangeListener, PreferenceChangeEvent>> events) {
        if (noEvents) {
            return;
        }
        
        for(EventBag<PreferenceChangeListener, PreferenceChangeEvent> bag : events) {
            for(PreferenceChangeEvent event : bag.getEvents()) {
                for(PreferenceChangeListener l : bag.getListeners()) {
                    try {
                        l.preferenceChange(event);
                    } catch (Throwable t) {
                        LOG.log(Level.WARNING, null, t);
                    }
                }
            }
        }
    }

    private void fireNodeEvents(List<EventBag<NodeChangeListener, NodeChangeEvent>> events) {
        if (noEvents) {
            return;
        }
        
        for(EventBag<NodeChangeListener, NodeChangeEvent> bag : events) {
            for(NodeChangeEvent event : bag.getEvents()) {
                for(NodeChangeListener l : bag.getListeners()) {
                    try {
                        if ((event instanceof NodeChangeEventExt) && ((NodeChangeEventExt) event).isRemovalEvent()) {
                            l.childRemoved(event);
                        } else {
                            l.childAdded(event);
                        }
                    } catch (Throwable t) {
                        LOG.log(Level.WARNING, null, t);
                    }
                }
            }
        }
    }

    /* test */ static final class Tree {

        public static Tree getTree(Object token, Preferences prefs) {
            synchronized (trees) {
                // find all trees for the token
                Map<Preferences, Tree> forest = trees.get(token);
                if (forest == null) {
                    forest = new HashMap<Preferences, Tree>();
                    trees.put(token, forest);
                }

                // find the tree for the prefs' root
                Preferences root = prefs.node("/"); //NOI18N
                Tree tree = forest.get(root);
                if (tree == null) {
                    tree = new Tree(token, root);
                    forest.put(root, tree);
                }

                return tree;
            }
        }

        /* test */ static final Map<Object, Map<Preferences, Tree>> trees = new WeakHashMap<Object, Map<Preferences, Tree>>();

        private final Preferences root;
        private final Reference<?> tokenRef;
        private final Map<String, ProxyPreferencesImpl> nodes = new HashMap<String, ProxyPreferencesImpl>();
        
        private Tree(Object token, Preferences root) {
            this.root = root;
            this.tokenRef = new WeakReference<Object>(token);
        }

        public Object treeLock() {
            return this;
        }

        public ProxyPreferencesImpl get(ProxyPreferencesImpl parent, String name, Preferences delegate) {
            if (delegate != null) {
                assert name.equals(delegate.name());

                if (parent == null) {
                    Preferences parentDelegate = delegate.parent();
                    if (parentDelegate != null) {
                        parent = get(null, parentDelegate.name(), parentDelegate);
                    } // else delegate is the root
                } else {
                    // sanity check
                    assert parent.delegate == delegate.parent();
                }
            }

            String absolutePath;
            if (parent == null) {
                absolutePath = "/"; //NOI18N
            } else if (parent.parent() == null) {
                absolutePath = "/" + name; //NOI18N
            } else {
                absolutePath = parent.absolutePath() + "/" + name; //NOI18N
            }

            ProxyPreferencesImpl node = nodes.get(absolutePath);
            if (node == null) {
                node = new ProxyPreferencesImpl(parent, name, delegate, this);
                nodes.put(absolutePath, node);

                if (parent != null) {
                    parent.addChild(node);
                }
            } else {
                assert !node.removed;
            }

            return node;
        }

        public void removeNode(ProxyPreferencesImpl node) {
            String path = node.absolutePath();
            assert nodes.containsKey(path);
            ProxyPreferencesImpl pp = nodes.remove(path);
        }

        public void destroy() {
            synchronized (trees) {
                Object token = tokenRef.get();
                if (token != null) {
                    trees.remove(token);
                } // else the token has been GCed and therefore is not even in the trees map
            }
        }
    } // End of Tree class

    private static final class EventBag<L, E extends EventObject> {
        private final Set<L> listeners = new HashSet<L>();
        private final Set<E> events = new HashSet<E>();

        public EventBag() {
        }

        public Set<? extends L> getListeners() {
            return listeners;
        }

        public Set<? extends E> getEvents() {
            return events;
        }

        public void addListeners(Collection<? extends L> l) {
            listeners.addAll(l);
        }

        public void addEvent(E event) {
            events.add(event);
        }
    } // End of EventBag class

    private static final class NodeChangeEventExt extends NodeChangeEvent {
        private final boolean removal;
        public NodeChangeEventExt(Preferences parent, Preferences child, boolean removal) {
            super(parent, child);
            this.removal = removal;
        }

        public boolean isRemovalEvent() {
            return removal;
        }
    } // End of NodeChangeEventExt class
}
