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

package org.netbeans.modules.viewmodel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.netbeans.spi.viewmodel.Models;

/**
 * Ugly class, that takes care that the expansion state is always managed for the object under the given node.
 * This is necessary for trees that have equal nodes under various branches, or for recursive trees.
 * 
 * @author Martin
 */
public class DefaultTreeExpansionManager {
    
    private static Map<Models.CompoundModel, DefaultTreeExpansionManager> managers = new WeakHashMap<Models.CompoundModel, DefaultTreeExpansionManager>();

    private Object currentChildren;
    private Map<Object, Set<Object>> expandedNodes = new HashMap<Object, Set<Object>>();
    
    public static synchronized DefaultTreeExpansionManager get(Models.CompoundModel model) {
        if (model == null) throw new NullPointerException();
        DefaultTreeExpansionManager manager = managers.get(model);
        if (manager == null) {
            manager = new DefaultTreeExpansionManager();
            managers.put(model, manager);
        }
        return manager;
    }
    
    public static synchronized void copyExpansions(Models.CompoundModel oldCM, Models.CompoundModel newCM) {
        DefaultTreeExpansionManager oldManager = get(oldCM);
        DefaultTreeExpansionManager newManager = get(newCM);
        Map<Object, Set<Object>> expandedNodes;
        synchronized (oldManager) {
            expandedNodes = new java.util.HashMap<Object, Set<Object>>(oldManager.expandedNodes);
        }
        synchronized (newManager) {
            newManager.expandedNodes.putAll(expandedNodes);
        }
    }
    
    private DefaultTreeExpansionManager() {}
    
    /** Must be called before every query, external synchronization with the model call is required. */
    public void setChildrenToActOn(Object ch) {
        currentChildren = ch;
    }
    
    /** External synchronization with currentNode required. */
    public synchronized boolean isExpanded(Object child) {
        if (currentChildren == null) throw new NullPointerException("Call setChildrenToActOn() before!!!");
        try {
            Set<Object> expanded = expandedNodes.get(currentChildren);
            if (expanded != null && expanded.contains(child)) {
                return true;
            }
            return false;
        } finally {
            currentChildren = null;
        }
    }

    public synchronized void setExpanded(Object child) {
        if (currentChildren == null) throw new NullPointerException("Call setChildrenToActOn() before!!!");
        try {
            Set<Object> expanded = expandedNodes.get(currentChildren);
            if (expanded == null) {
                expanded = Collections.newSetFromMap(new WeakHashMap<>());
                expandedNodes.put(currentChildren, expanded);
            }
            expanded.add(child);
        } finally {
            currentChildren = null;
        }
    }

    public synchronized void setCollapsed(Object child) {
        if (currentChildren == null) throw new NullPointerException("Call setChildrenToActOn() before!!!");
        try {
            Set<Object> expanded = expandedNodes.get(currentChildren);
            if (expanded != null) {
                expanded.remove(child);
            }
        } finally {
            currentChildren = null;
        }
    }

}
