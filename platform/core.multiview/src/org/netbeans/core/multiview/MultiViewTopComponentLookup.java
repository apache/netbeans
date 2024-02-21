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

    private MyProxyLookup proxy;
    private InitialProxyLookup initial;
    
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
    public Lookup.Item lookupItem(Lookup.Template template) {
        Lookup.Item retValue;
        if (template.getType() == ActionMap.class || (template.getId() != null && template.getId().equals("javax.swing.ActionMap"))) {
            return initial.lookupItem(template);
        }
        // do something here??
        retValue = super.lookupItem(template);
        return retValue;
    }    
    
     
    public Object lookup(Class clazz) {
        if (clazz == ActionMap.class) {
            return initial.lookup(clazz);
        }
        Object retValue;
        
        retValue = proxy.lookup(clazz);
        return retValue;
    }
    
    public Lookup.Result lookup(Lookup.Template template) {
        
        if (template.getType() == ActionMap.class || (template.getId() != null && template.getId().equals("javax.swing.ActionMap"))) {
            return initial.lookup(template);
        }
        Lookup.Result retValue;
        retValue = proxy.lookup(template);
        retValue = new ExclusionResult(retValue);
        return retValue;
    }

    boolean isInitialized() {
        return proxy.isInitialized();
    }
    
    /**
     * A lookup result excluding some instances.
     */
    private static final class ExclusionResult extends Lookup.Result implements LookupListener {
        
        private final Lookup.Result delegate;
        private final List<LookupListener> listeners = new ArrayList<>();
        private Collection lastResults;
        
        public ExclusionResult(Lookup.Result delegate) {
            this.delegate = delegate;
        }
        
        public Collection allInstances() {
            // this shall remove duplicates??
            Set<Lookup.Result> s = new HashSet<>(delegate.allInstances());
            return s;
        }
        
        public Set allClasses() {
            return delegate.allClasses(); // close enough
        }
        
        public Collection allItems() {
            // remove duplicates..
            Set<Lookup.Item> s = new HashSet<>(delegate.allItems());
            Iterator<Lookup.Item> it = s.iterator();
            Set instances = new HashSet<>();
            while (it.hasNext()) {
                Lookup.Item i = it.next();
                if (instances.contains(i.getInstance())) {
                    it.remove();
                } else {
                    instances.add(i.getInstance());
                }
            }
            return s;
        }
        
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
        
        public void removeLookupListener(LookupListener l) {
            synchronized (listeners) {
                listeners.remove(l);
                if (listeners.isEmpty()) {
                    delegate.removeLookupListener(this);
                    lastResults = null;
                }
            }
        }
        
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
                ls = listeners.toArray(new LookupListener[0]);
            }
            for (int i = 0; i < ls.length; i++) {
                ls[i].resultChanged(ev2);
            }
        }
        
    }
    
    private static class MyProxyLookup extends ProxyLookup {
        private Lookup initialLookup;
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
        private ActionMap initObject;
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
        private ActionMap map;
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
