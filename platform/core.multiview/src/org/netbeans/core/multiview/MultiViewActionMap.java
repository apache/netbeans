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

package org.netbeans.core.multiview;


import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.openide.windows.TopComponent;


/** ActionMap that delegates to current action map of provided component and dynamically also the current element.
 * Used in <code>MultiViewTopComopnent</code> lookup.
 *
 * @author Milos Kleint
 */
final class MultiViewActionMap extends ActionMap {
    private ActionMap delegate;
    private ActionMap topComponentMap;
    private TopComponent component;
    
    private boolean preventRecursive = false;
    private Object LOCK = new Object();

    public MultiViewActionMap(TopComponent tc, ActionMap tcMap) {
        topComponentMap = tcMap;
        component = tc;
    }
    
    public void setDelegateMap(ActionMap map) {
        delegate = map;
    }

    @Override
    public int size() {
        return keys ().length;
    }

    @Override
    public Action get(Object key) {
        // the multiview's action map first.. for stuff like the closewindow and clonewindow from TopComponent.initActionMap
        javax.swing.ActionMap m = topComponentMap;
        if (m != null) {
            Action a = m.get (key);
            if (a != null) {
                return a;
            }
        }
        // delegate then
        m = delegate;
        if (m != null) {
            //this is needed because of Tc's DelegateActionMap which traverses up the component hierarchy.
            // .. results in calling this method again and again and again. -> stackoverflow.
            // this should break the evil cycle.
            synchronized (LOCK) {
                if (preventRecursive) {
                    preventRecursive = false;
                    return null;
                }
                Action a;
                preventRecursive = true;
                try {
                    a = m.get (key);
                } finally {
                    preventRecursive = false;
                }
                if (a != null) {
                    return a;
                }
            }
        }
        
        java.awt.Component owner = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        Action found = null;
        try {
            preventRecursive = true;
            while (owner != null && owner != component) {
                if (found == null && (owner instanceof JComponent)) {
                    m = ((JComponent)owner).getActionMap ();
                    if (m != null) {
                        if( m instanceof MultiViewActionMap && ((MultiViewActionMap)m).preventRecursive ) {
                            break;
                        }
                        found = m.get (key);
                    }
                }
                owner = owner.getParent ();
            }
        } finally {
            preventRecursive = false;
        }
        
        return owner == component ? found : null;
    }

    @Override
    public Object[] allKeys() {
        return keys (true);
    }

    @Override
    public Object[] keys() {
        return keys (false);
    }


    private Object[] keys(boolean all) {
        Set<Object> keys = new HashSet<>();

        if (delegate != null) {
            Object[] delegateKeys;
            if (all) {
                delegateKeys = delegate.allKeys();
            } else {
                delegateKeys = delegate.keys();
            }
            if( null != delegateKeys ) {
                keys.addAll(Arrays.asList(delegateKeys));
            }
        }
        
        if (topComponentMap != null) {
            java.util.List l;

            if (all) {
                l = Arrays.asList (topComponentMap.allKeys ());
            } else {
                l = Arrays.asList (topComponentMap.keys ());
            }

            keys.addAll (l);
        }

        return keys.toArray();
    }

    // 
    // Not implemented
    //
    @Override
    public void remove(Object key) {
        topComponentMap.remove(key);
    }

    @Override
    public void setParent(ActionMap map) {
        topComponentMap.setParent(map);
    }

    @Override
    public void clear() {
        topComponentMap.clear();
    }

    @Override
    public void put(Object key, Action action) {
        topComponentMap.put (key, action);
    }

    @Override
    public ActionMap getParent() {
        return topComponentMap.getParent();
    }
 
}    
