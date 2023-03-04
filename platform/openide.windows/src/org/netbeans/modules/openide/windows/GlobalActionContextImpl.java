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

package org.netbeans.modules.openide.windows;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.KeyboardFocusManager;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/** An interface that can be registered in a lookup by subsystems
 * wish to provide a global context actions should react to.
 *
 * @author Jaroslav Tulach
*/
@org.openide.util.lookup.ServiceProvider(service=org.openide.util.ContextGlobalProvider.class)
public final class GlobalActionContextImpl extends Object
implements ContextGlobalProvider, Lookup.Provider, java.beans.PropertyChangeListener, Runnable {
    /** registry to work with */
    private TopComponent.Registry registry;
    
    public GlobalActionContextImpl () {
        this (TopComponent.getRegistry());
    }
    
    public GlobalActionContextImpl (TopComponent.Registry r) {
        this.registry = r;
        if (EventQueue.isDispatchThread()) {
            run();
        } else {
            EventQueue.invokeLater(this);
        }
    }
    
    @Override
    public void run() {
        KeyboardFocusManager m = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        m.removePropertyChangeListener("permanentFocusOwner", this); // NOI18N
        m.addPropertyChangeListener("permanentFocusOwner", this); // NOI18N
        setFocusOwner(m.getPermanentFocusOwner());
    }
    
    /** we also manage the current focus owner */
    private static Reference<Component> focusOwner;
    /** the lookup to temporarily use */
    private static volatile Lookup temporary;
    /** Temporarily provides different action map in the lookup.
     */
    public static void blickActionMap(ActionMap map) {
        blickActionMap(map, null);
    }
    private static void blickActionMap(final ActionMap map, final Component[] focus) {
        if (EventQueue.isDispatchThread()) {
            blickActionMapImpl(map, focus);
        } else {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    blickActionMapImpl(map, focus);
                }
            });
        }
    }

    static void blickActionMapImpl(ActionMap map, Component[] focus) {
        assert EventQueue.isDispatchThread();
        Object obj = Lookup.getDefault ().lookup (ContextGlobalProvider.class);
        if (obj instanceof GlobalActionContextImpl) {
            GlobalActionContextImpl g = (GlobalActionContextImpl)obj;
            
            Lookup[] arr = {
                map == null ? Lookup.EMPTY : Lookups.singleton (map),
                Lookups.exclude (g.getLookup (), new Class[] { javax.swing.ActionMap.class }),
            };
            
            Lookup originalLkp = g.getLookup();
            Lookup prev = temporary;
            try {
                temporary = new ProxyLookup (arr);
                Lookup actionsGlobalContext = Utilities.actionsGlobalContext();
                Object q = actionsGlobalContext.lookup (javax.swing.ActionMap.class);
                assert q == map : dumpActionMapInfo(map, q, prev, temporary, actionsGlobalContext, originalLkp);
                if (focus != null) {
                    setFocusOwner(focus[0]);
                }
            } finally {
                temporary = prev;
                // fire the changes about return of the values back
                org.openide.util.Utilities.actionsGlobalContext ().lookup (javax.swing.ActionMap.class);
            }
        }
    }

    private static String dumpActionMapInfo(ActionMap map, Object q, Lookup prev, Lookup now,
            Lookup globalContext, Lookup originalLkp) {
        StringBuilder sb = new StringBuilder();
        sb.append("We really get map from the lookup. Map: ").append(map) // NOI18N
            .append(" returned: ").append(q); // NOI18N
        sb.append("\nprev: ").append(prev == null ? "null prev" : prev.lookupAll(Object.class)); //NOI18N
        sb.append("\nnow : ").append(now == null ? "null now" : now.lookupAll(Object.class)); //NOI18N
        sb.append("\nglobal ctx : ").append(globalContext == null ? "null" : globalContext.lookupAll(Object.class)); //NOI18N
        sb.append("\noriginal lkp : ").append(originalLkp == null ? "null" : originalLkp.lookupAll(Object.class)); //NOI18N
        return sb.toString();
    }
    
    private static void setFocusOwner(Component focus) {
        focusOwner = new WeakReference<Component>(focus);
    }
    public static Component findFocusOwner() {
        if (focusOwner == null) {
            Utilities.actionsGlobalContext();
            if (focusOwner == null) {
                // give up
                setFocusOwner(null);
            }
        }
        return focusOwner.get();
    }
    
    /** Let's create the proxy listener that delegates to currently 
     * selected top component.
     */
    public Lookup createGlobalContext() {
        registry.addPropertyChangeListener(this);
        return org.openide.util.lookup.Lookups.proxy(this);
    }
    
    /** The current component lookup */
    public Lookup getLookup() {
        Lookup l = temporary;
        if (l != null) {
            return l;
        }
        
        TopComponent tc = registry.getActivated();
        return tc == null ? Lookup.EMPTY : tc.getLookup();
    }
    
    /** Requests refresh of our lookup everytime component is chagned.
     */
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (TopComponent.Registry.PROP_ACTIVATED.equals (evt.getPropertyName())) {
            org.openide.util.Utilities.actionsGlobalContext ().lookup (javax.swing.ActionMap.class);
        }
        if ("permanentFocusOwner".equals(evt.getPropertyName())) {
            Component[] arr = { (Component)evt.getNewValue() };
            if (arr[0] instanceof AbstractButton) {
                Action a = ((AbstractButton)arr[0]).getAction();
                if (a instanceof ContextAwareAction) {
                    // ignore focus change into a button with our action
                    return;
                }
            }
            blickActionMap(null, arr);
        }
    }
    
}
