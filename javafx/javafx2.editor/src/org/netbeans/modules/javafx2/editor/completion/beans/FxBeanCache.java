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
package org.netbeans.modules.javafx2.editor.completion.beans;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.lang.model.element.TypeElement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClassIndexListener;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.RootsEvent;
import org.netbeans.api.java.source.TypesEvent;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * This cache collects {@link FxBean}s for individual classes
 * encountered and introspected by Fx support.
 * The cache is invalidated when the {@link ClasspathInfo} changes. When
 * a class changes, the cache removes data for the class and recursively 
 * for dependencies of that class, so features are analyzed again on
 * the next query.
 * <p/>
 * The cache maintains an instance of {@link ClasspathCache} for each instance
 * of {@link ClasspathInfo}. Within the ClasspathCache, dependencies
 * and class FxBean instances are collected. Each ClasspathCache listens on
 * its underlying ClasspathInfo's ClassIndex for class structural changes.
 * The whole cache listens on individual ClasspathInfos using weak listeners,
 * and when classpath changes, it removes the whole ClasspathCache object.
 * <p/>
 * 
 * @author sdedic
 */
class FxBeanCache implements ChangeListener {
    private final Map<String, Reference<ClasspathCache>>  cache = new HashMap<String, Reference<ClasspathCache>>();
    
    private static volatile FxBeanCache INSTANCE;
    
    static FxBeanCache instance() {
        if (INSTANCE == null) {
            synchronized (FxBeanCache.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FxBeanCache();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        ClasspathInfo source = (ClasspathInfo)e.getSource();
        synchronized (cache) {
            cache.remove(source);
        }
    }
    
    private static void addCp(StringBuilder sb, ClassPath cp) {
        if (cp == null) {
            return;
        }
        sb.append(cp.toString());
    }
    
    private String createKey(ClasspathInfo cp) {
        StringBuilder sb = new StringBuilder();
        addCp(sb, cp.getClassPath(ClasspathInfo.PathKind.BOOT));
        sb.append("/");
        addCp(sb, cp.getClassPath(ClasspathInfo.PathKind.COMPILE));
        sb.append("/");
        addCp(sb, cp.getClassPath(ClasspathInfo.PathKind.SOURCE));
        
        return sb.toString();
    }
    
    public FxBean getBeanInfo(ClasspathInfo cp, String classname) {
        synchronized (cache) {
            Reference<ClasspathCache> c = cache.get(createKey(cp));
            if (c == null) {
                return null;
            }
            
            ClasspathCache cc = c.get();
            if (cc == null) {
                return null;
            }
            return cc.getBean(classname);
        }
    }
    
    public void addBeanInfo(ClasspathInfo cp, FxBean instance, Set<String> parents) {
        ClasspathCache cc = null;
        synchronized (cache) {
            String key = createKey(cp);
            Reference<ClasspathCache> c = cache.get(key);
            
            if (c != null) {
                cc = c.get();
            }
            if (cc == null) {
                cache.put(key, new CacheRef(cc = new ClasspathCache(cp), key));
            }
        }
        cc.addBeanInfo(instance, parents);
    }
    
    private static class CacheRef extends WeakReference<ClasspathCache> implements Runnable {
        private String    refKey;

        public CacheRef(ClasspathCache referent, String key) {
            super(referent, Utilities.activeReferenceQueue());
            this.refKey = key;
        }

        public void run() {
            synchronized (instance().cache) {
                Map m = instance().cache;
                Object o = m.get(refKey);
                if (o == this) {
                    m.remove(refKey);
                }
            }
        }
        
    }
            
    private static class ClasspathCache implements ClassIndexListener {
        private final Map<String, FxBean>     classInfos = new HashMap<String, FxBean>();
        private final Map<String, Object> dependencies = new HashMap<String, Object>();
        
        private ClasspathCache(ClasspathInfo cpInfo) {
            cpInfo.getClassIndex().addClassIndexListener(
                    WeakListeners.create(ClassIndexListener.class, this, cpInfo));
        }

        @Override
        public void typesRemoved(TypesEvent event) {
            clearFrom(event);
        }

        @Override
        public void typesChanged(TypesEvent event) {
            clearFrom(event);
        }

        @Override
        public void rootsAdded(RootsEvent event) {
            clear();
        }

        @Override
        public void rootsRemoved(RootsEvent event) {
            clear();
        }

        @Override
        public void typesAdded(TypesEvent event) {
            // no op
        }
        
        private synchronized void clear() {
            classInfos.clear();
            dependencies.clear();
        }
        
        @SuppressWarnings("unchecked")
        private synchronized void addDependency(String from, String to) {
            Object o = dependencies.get(from);

            Collection<String> deps;
            if (o == null) {
                dependencies.put(from, to);
                return;
            } else if (o instanceof String) {
                if (to.equals(o)) {
                    return;
                }
                deps = new HashSet<String>();
                deps.add((String)o);
                dependencies.put(from, deps);
            } else {
                deps = (Collection<String>)o;
            }
            deps.add(to);
        }
        
        private void clearFrom(TypesEvent event) {
            Collection<String> fqns = new ArrayList<String>();
            for (ElementHandle<TypeElement> t : event.getTypes()) {
                fqns.add(t.getQualifiedName());
            }
            clearFrom(fqns);
        }
        
        public synchronized FxBean getBean(String fqn) {
            return classInfos.get(fqn);
        }
        
        @SuppressWarnings("unchecked")
        public synchronized void clearFrom(Collection<String> roots) {
            Set<String> allDeps = new HashSet<String>();
            Deque<String> process = new LinkedList<String>();
            process.addAll(roots);
            while (!process.isEmpty()) {
                String x = process.removeFirst();
                allDeps.add(x);
                Object deps = dependencies.get(x);
                if (deps == null) {
                    continue;
                }
                if (deps instanceof String) {
                    process.add((String)deps);
                } else {
                    process.addAll((Collection<String>)deps);
                }
            }
            dependencies.keySet().removeAll(allDeps);

            classInfos.keySet().removeAll(allDeps);
        }
        
        public synchronized void addBeanInfo(FxBean info, Set<String> superClasses) {
            String clName = info.getClassName();
            if (classInfos.containsKey(clName)) {
                return;
            }
            classInfos.put(clName, info);

            for (String scn : superClasses) {
                addDependency(scn, clName);
            }
        }
    }
}
