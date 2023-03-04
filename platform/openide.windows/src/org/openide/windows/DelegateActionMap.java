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
package org.openide.windows;

import java.awt.Component;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import org.netbeans.modules.openide.windows.GlobalActionContextImpl;


// This is almost copy of org.openide.util.UtilitiesCompositeActionMap.

/** ActionMap that delegates to current action map of provided component.
 * Used in <code>TopComopnent</code> lookup.
 * <p><b>Note: This action map is 'passive', i.e putting new mappings
 * into it makes no effect. Could be changed later.</b>
 *
 * @author Peter Zavadsky
 */
final class DelegateActionMap extends ActionMap {
    private Reference<JComponent> component;
    private ActionMap delegate;

    public DelegateActionMap(JComponent c) {
        setComponent(c);
    }

    public DelegateActionMap(TopComponent c, ActionMap delegate) {
        setComponent(c);
        this.delegate = delegate;
    }

    @Override
    public int size() {
        return keys().length;
    }

    @Override
    public Action get(Object key) {
        ActionMap m;

        if (delegate == null) {
            JComponent comp = getComponent();
            if (comp == null) {
                m = null;
            } else {
                m = comp.getActionMap();
            }
        } else {
            m = delegate;
        }

        if (m != null) {
            Action a = m.get(key);

            if (a != null) {
                return a;
            }
        }

        Component owner = GlobalActionContextImpl.findFocusOwner();
        Action found = null;

        while ((owner != null) && (owner != getComponent())) {
            if ((found == null) && (owner instanceof JComponent)) {
                m = ((JComponent) owner).getActionMap();

                if (m != null) {
                    found = m.get(key);
                }
            }

            owner = owner.getParent();
        }

        return (owner == getComponent()) ? found : null;
    }

    @Override
    public Object[] allKeys() {
        return keys(true);
    }

    @Override
    public Object[] keys() {
        return keys(false);
    }

    private Object[] keys(boolean all) {
        Set<Object> keys = new HashSet<Object>();

        
        ActionMap m;

        if (delegate == null) {
            JComponent comp = getComponent();
            if (comp == null) {
                m = null;
            } else {
                m = comp.getActionMap();
            }
        } else {
            m = delegate;
        }

        if (m != null) {
            List<Object> l;

            if (all) {
                Object[] allKeys = m.allKeys();
                if( null == allKeys ) {
                    l = Collections.emptyList();
                } else {
                    l = Arrays.asList(m.allKeys());
                }
            } else {
                l = Arrays.asList(m.keys());
            }

            keys.addAll(l);
        }
        
        Component owner = GlobalActionContextImpl.findFocusOwner();
        List<JComponent> hierarchy = new ArrayList<JComponent>();
        while ((owner != null) && (owner != getComponent())) {
            if (owner instanceof JComponent) {
                hierarchy.add((JComponent)owner);
            }
            owner = owner.getParent();
        }
        if (owner == getComponent()) {
            for (JComponent c : hierarchy) {
                ActionMap am = c.getActionMap();
                if (am == null) {
                    continue;
                }
                Object[] fk = all ? am.allKeys() : am.keys();
                if (fk != null) {
                    keys.addAll(Arrays.asList(fk));
                }
            }
        }
        return keys.toArray();
    }

    // 
    // Not implemented
    //
    @Override
    public void remove(Object key) {
        if (delegate != null) {
            delegate.remove(key);
        }
    }

    @Override
    public void setParent(ActionMap map) {
        if (delegate != null) {
            delegate.setParent(map);
            GlobalActionContextImpl.blickActionMap(new ActionMap());
        }
    }

    @Override
    public void clear() {
        if (delegate != null) {
            delegate.clear();
        }
    }

    @Override
    public void put(Object key, Action action) {
        if (delegate != null) {
            delegate.put(key, action);
        }
    }

    @Override
    public ActionMap getParent() {
        return (delegate == null) ? null : delegate.getParent();
    }

    @Override
    public String toString() {
        return super.toString() + " for " + this.getComponent();
    }

    JComponent getComponent() {
        return component.get();
    }

    private void setComponent(JComponent component) {
        this.component = new WeakReference<JComponent>(component);
    }
}
