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
package org.netbeans.modules.java.lsp.server.explorer;

import org.netbeans.modules.java.lsp.server.explorer.api.TreeDataListener;
import org.netbeans.modules.java.lsp.server.explorer.api.TreeItemData;
import org.netbeans.modules.java.lsp.server.explorer.api.TreeDataProvider;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Extracts services from node's Lookup; just checks for their presence. Service names are
 * collected to a context value, 
 * @author sdedic
 */
public class NodeLookupContextValues implements TreeDataProvider {
    private static final Logger LOG = Logger.getLogger(NodeLookupContextValues.class.getName());
    private static final String CLASS_PREFIX = "class:"; // NOI18N
    private static final Map<String, String> DEFAULT_PREFIXES = new HashMap<>();
    
    private final Set<Class<?>> watchClasses;
    private final Map<String, String> mapPrefixes;
    private final Map<Class<?>, String> interfaces; 
    private final Map<Node, L> listenerMap = new WeakHashMap<>();
    // @GuardedBy(this)
    private final List<TreeDataListener> listeners = new ArrayList<>();
    
    static {
        DEFAULT_PREFIXES.put("org.openide.cookies.", ""); // NOI18N
        DEFAULT_PREFIXES.put("org.netbeans.api.actions.", ""); // NOI18N
    }
    
    class L extends WeakReference<Node> implements LookupListener {
        final List<Lookup.Result> results;
        
        public L(List<Lookup.Result> results, Node target) {
            super(target);
            this.results = results;
        }
        
        @Override
        public void resultChanged(LookupEvent ev) {
            Node n = get();
            if (n == null) {
                Lookup.Result lr = (Lookup.Result)ev.getSource();
                lr.removeLookupListener(this);
            } else {
                fireNodeChange(n);
            }
        }
    }
    
    public NodeLookupContextValues(Map<Class<?>, String> interfaces, Map<String, String> mapPrefixes, Set<Class<?>> watchClasses) {
        this.interfaces = interfaces;
        this.mapPrefixes = mapPrefixes;
        this.watchClasses = watchClasses;
    }
    
    private String token(Class c) {
        String token = interfaces.get(c);
        if (token == null) {
            token = c.getName();
        }
        String longestPrefix = null;
        for (String s : mapPrefixes.keySet()) {
            if (token.startsWith(s)) {
                if (longestPrefix == null || longestPrefix.length() < s.length()) {
                    longestPrefix = s;
                }
            }
        }
        if (longestPrefix != null) {
            String pref = mapPrefixes.get(longestPrefix);
            token = pref + token.substring(longestPrefix.length());
        }
        return CLASS_PREFIX + token;
    }
    
    public String[] findContextValues(Node n) {
        L listener;
        List<Lookup.Result> results = null;
        synchronized (this) {
            listener = listenerMap.get(n);
            if (listener == null) {
                results = new ArrayList<>();
                listener = new L(results, n);
            }
        }
        List<String> res = new ArrayList<>();
        Lookup nl = n.getLookup();
        for (Class c : interfaces.keySet()) {
            Lookup.Item li;
            if (results == null || !watchClasses.contains(c)) {
                li = nl.lookupItem(new Lookup.Template<>(c));
            } else {
                Lookup.Result lr = nl.lookupResult(c);
                Collection items = lr.allItems();
                results.add(lr);
                li = items.isEmpty() ? null : (Lookup.Item)items.iterator().next();
            }
            if (li != null) {
                res.add(token(c));
            }
        }
        if (results != null && !results.isEmpty()) {
            synchronized (this) {
                if (listenerMap.putIfAbsent(n, listener) == null) {
                    for (Lookup.Result lr : results) {
                        lr.addLookupListener(listener);
                    }
                }
            }
        }
        return res.toArray(new String[0]);
        
    }

    public static NodeLookupContextValues nodeLookup(String[] classes) {
        Set<Class<?>> watch = new HashSet<>();
        Map<String, String> prefixes = new HashMap<>(DEFAULT_PREFIXES);
        Map<Class<?>, String> tokens = new HashMap<>();
        ClassLoader ldr = Lookup.getDefault().lookup(ClassLoader.class);
        for (String s : classes) {
            String prefix = null;
            String toPrefix = null;
            
            String line = s.trim();
            String classToken = line;
            int bracket = line.indexOf('['); // NOI18N
            if (bracket != -1) {
                int endBracket = line.indexOf(']', bracket + 1); // NOI18N
                if (endBracket == -1) {
                    throw new IllegalArgumentException(line);
                } else {
                    prefix = line.substring(bracket + 1, endBracket);
                    int eq = prefix.indexOf('='); // NOI18N
                    if (eq != -1) {
                        prefix = prefix.substring(0, eq);
                        toPrefix = prefix.length() <= eq + 1 ? "" : prefix.substring(eq + 1); // NOI18N
                    } else {
                        toPrefix = ""; // NOI18N
                    }
                    if (line.length() <= endBracket + 1) {
                        line = "";  // NOI18N
                    } else {
                        classToken = line.substring(0, bracket) + line.substring(endBracket + 1);
                        line = line.substring(0, bracket) + prefix + line.substring(endBracket + 1);
                    }
                }
            } else {
                int eq = line.indexOf('='); // NOI18N
                if (eq != -1) {
                    classToken = line.substring(eq + 1);
                    line = line.substring(0, eq);
                }
            }
            if (!line.isEmpty()) {
                boolean enableWatch = line.charAt(0) == '*'; // NOI18N
                if (enableWatch) {
                    line = line.substring(1);
                }
                try {
                    Class clazz = ldr.loadClass(line);
                    tokens.put(clazz, classToken);
                    if (enableWatch) {
                        watch.add(clazz);
                    }
                } catch (ClassNotFoundException ex) {
                    LOG.log(Level.INFO, "Service class {0} not found.", line);
                }
            } else if (toPrefix != null) {
                prefixes.put(prefix, toPrefix);
            }
        }
        return new NodeLookupContextValues(tokens, prefixes, watch);
    }

    @Override
    public TreeItemData createDecorations(Node n, boolean expanded) {
        String[] tokens = findContextValues(n);
        if (tokens != null && tokens.length > 0) {
            return new TreeItemData().setContextValues(tokens);
        } else {
            return null;
        }
    }
    
    private void fireNodeChange(Node n) {
        
    }

    @Override
    public synchronized void addTreeItemDataListener(TreeDataListener l) {
        listeners.add(l);
    }

    @Override
    public synchronized void removeTreeItemDataListener(TreeDataListener l) {
        listeners.remove(l);
    }

    @Override
    public void nodeReleased(Node n) {
        L l;
        synchronized (this) {
            l = listenerMap.remove(n);
        }
        if (l != null) {
            for (Lookup.Result lr : l.results) {
                lr.removeLookupListener(l);
            }
        }
    }
}
