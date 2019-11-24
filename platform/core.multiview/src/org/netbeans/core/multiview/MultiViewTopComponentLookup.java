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
import org.openide.util.Lookup;
import java.util.*;
import javax.swing.ActionMap;

import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

class MultiViewTopComponentLookup extends Lookup {

    private final MyProxyLookup proxy;
    private final InitialProxyLookup initial;
    
    public MultiViewTopComponentLookup(ActionMap initialObject) {
        super();
        // need to delegate in order to get the correct Lookup.Templates that refresh..
        initial = new InitialProxyLookup(initialObject);
        proxy = new MyProxyLookup(initial);
    }
    
    
    public void setElementLookup(Lookup look) {
        proxy.setElementLookup(look);
        initial.refreshLookup();
    }
    
    @Override
    public <T> Item<T> lookupItem(Template<T> template) {
        Lookup.Item<T> retValue;
        if (template.getType() == ActionMap.class || (template.getId() != null && template.getId().equals("javax.swing.ActionMap"))) {
            return initial.lookupItem(template);
        }
        // do something here??
        retValue = super.lookupItem(template);
        return retValue;
    }    
    
     
    @Override
    public <T> T lookup(Class<T> clazz) {
        if (clazz == ActionMap.class) {
            return initial.lookup(clazz);
        }
        T retValue;
        
        retValue = proxy.lookup(clazz);
        return retValue;
    }
    
    @Override
    public <T> Lookup.Result<T> lookup(Lookup.Template<T> template) {
        if (template.getType() == ActionMap.class || (template.getId() != null && template.getId().equals("javax.swing.ActionMap"))) {
            return initial.lookup(template);
        }
        Lookup.Result<T> retValue;
        retValue = proxy.lookup(template);
        retValue = new ExclusionResult<>(retValue);
        return retValue;
    }

    boolean isInitialized() {
        return proxy.isInitialized();
    }
    
    /**
     * A lookup result excluding some instances.
     */
    private static final class ExclusionResult<T> extends Lookup.Result<T> implements LookupListener {
        
        private final Lookup.Result<T> delegate;
        private final List<LookupListener> listeners = new ArrayList<>();
        private Collection lastResults;
        
        public ExclusionResult(Lookup.Result<T> delegate) {
            this.delegate = delegate;
        }
        
        @Override
        public Collection<T> allInstances() {
            // this shall remove duplicates??
            return new HashSet<>(delegate.allInstances());
        }
        
        @Override
        public Set<Class<? extends T>> allClasses() {
            return delegate.allClasses(); // close enough
        }
        
        @Override
        public Collection<? extends Item<T>> allItems() {
            // remove duplicates..
            Set<? extends Item<T>> s = new HashSet<>(delegate.allItems());
            Iterator<? extends Item<T>> it = s.iterator();
            Set<T> instances = new HashSet();
            while (it.hasNext()) {
                Lookup.Item<T> i = it.next();
                if (instances.contains(i.getInstance())) {
                    it.remove();
                } else {
                    instances.add(i.getInstance());
                }
            }
            return s;
        }
        
        @Override
        public void addLookupListener(LookupListener l) {
            synchronized (listeners) {
                if (listeners.isEmpty()) {
                    if (lastResults == null) {
                        lastResults = allInstances();
                    }
                    delegate.addLookupListener(this);
                }
                listeners.add(l);
            }
        }
        
        @Override
        public void removeLookupListener(LookupListener l) {
            synchronized (listeners) {
                listeners.remove(l);
                if (listeners.isEmpty()) {
                    delegate.removeLookupListener(this);
                    lastResults = null;
                }
            }
        }
        
        @Override
        public void resultChanged(LookupEvent ev) {
            synchronized (listeners) {
                Collection current = allInstances();
                boolean equal = lastResults != null && current != null && current.containsAll(lastResults) && lastResults.containsAll(current);
                if (equal) {
                    // the merged list is the same, ignore...
                    return ;
                }
                lastResults = current;
            }
                
            LookupEvent ev2 = new LookupEvent(this);
            LookupListener[] ls;
            synchronized (listeners) {
                ls = listeners.toArray(new LookupListener[listeners.size()]);
            }
            for (int i = 0; i < ls.length; i++) {
                ls[i].resultChanged(ev2);
            }
        }
        
    }
    
    private static class MyProxyLookup extends ProxyLookup {
        private final Lookup initialLookup;
        public MyProxyLookup(Lookup initial) {
            super(new Lookup[] {initial});
            initialLookup = initial;
        }

        public void setElementLookup(Lookup look) {
            final Lookup[] arr = getLookups();
            if (arr.length == 2 && look == arr[1]) {
                return;
            }
            setLookups(new Lookup[] {initialLookup, look});
        }

        private boolean isInitialized() {
            return getLookups().length == 2;
        }
    }
    
    static class InitialProxyLookup extends ProxyLookup {
        private final ActionMap initObject;
        public InitialProxyLookup(ActionMap obj) {
            super(new Lookup[] {Lookups.fixed(new Object[] {new LookupProxyActionMap(obj)})});
            initObject = obj;
        }

        public void refreshLookup() {
            setLookups(new Lookup[] {Lookups.fixed(new Object[] {new LookupProxyActionMap(initObject)})});
        }
        
    }
    
    /**
     * A proxy ActionMap that delegates to the original one, used because of #47991
     * non private because of tests..
     */
    static class LookupProxyActionMap extends ActionMap  {
        private final ActionMap map;
        public LookupProxyActionMap(ActionMap original) {
            map = original;
        }
        
        @Override
        public void setParent(ActionMap map) {
            this.map.setParent(map);
        }
        
        
        @Override
        public ActionMap getParent() {
            return map.getParent();
        }
        
        @Override
        public void put(Object key, Action action) {
            map.put(key, action);
        }
        
        @Override
        public Action get(Object key) {
            return map.get(key);
        }
        
        @Override
        public void remove(Object key) {
            map.remove(key);
        }
        
        @Override
        public void clear() {
            map.clear();
        }
        
        @Override
        public Object[] keys() {
            return map.keys();
        }
        
        @Override
        public int size() {
            return map.size();
        }
        
        @Override
        public Object[] allKeys() {
            return map.allKeys();
        }
        
    }
}
