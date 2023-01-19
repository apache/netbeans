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

package org.netbeans.insane.impl;

import java.lang.reflect.Field;
import java.util.*;
import javax.swing.BoundedRangeModel;

import org.netbeans.insane.scanner.ObjectMap;
import org.netbeans.insane.scanner.ScannerUtils;
import org.netbeans.insane.scanner.Visitor;
import org.netbeans.insane.live.*;
import org.netbeans.insane.scanner.Filter;

/**
 * Implementation class, don't use directly.
 *
 * @author nenik
 */
public class LiveEngine implements ObjectMap, Visitor {
    
    private final IdentityHashMap<Object,Object> objects = new IdentityHashMap<>();
    private final Map<Object, String> rest = new IdentityHashMap<>();
    
    private BoundedRangeModel progress;
    private int objCount;
    private int objExpected;  
    private int objStep;
    private Filter filter = ScannerUtils.skipNonStrongReferencesFilter();
    
    public LiveEngine() {}

    public LiveEngine(BoundedRangeModel progress) {
        this.progress = progress;
    }

    public LiveEngine(BoundedRangeModel progress, Filter filter) {
        this.progress = progress;
        if (filter != null) {
            this.filter = ScannerUtils.compoundFilter(new Filter[] {filter, this.filter});
        }
    }

    //--------------------------------------------
    // ObjectMap-like interface. We don't provide IDs though (returns null)
    @Override
    public boolean isKnown(Object o) {
        return objects.containsKey(o);
    }
    
    @Override
    public String getID(Object o) {
        objects.put(o, null); // mark as known
        return null; // null - if somebody really uses it, fails quickly
    }

    //--------------------------------------------
    // Visitor interface
    @Override
    public void visitClass(Class<?> cls) {}
    @Override
    public void visitObject(ObjectMap map, Object object) {
        if (progress != null) {
            objCount++;
            if ((((objCount % objStep) == 0)) && objCount < objExpected)
                progress.setValue(objCount);
        }
    }

    @Override
    public void visitArrayReference(ObjectMap map, Object from, Object to, int index) {
        visitRef(from, to, null);
    }

    @Override
    public void visitObjectReference(ObjectMap map, Object from, Object to, java.lang.reflect.Field ref) {
        visitRef(from, to, ref);
    }

    @Override
    public void visitStaticReference(ObjectMap map, Object to, java.lang.reflect.Field ref) {
        visitRef(null, to, ref);
    }

    //---------------------------------------------
    // Implementation
    private void visitRef(Object from, Object to, Field field) {
        addIncommingRef(to, from, field);
        if (rest.containsKey(to)) {
            rest.remove(to);
            if (rest.isEmpty()) {
                throw new ObjectFoundException();
            }
        }
    }

    
    private void addIncommingRef (Object to, Object from, Field f) {
        // wrap statics with special (private) object
        Object save = from != null ? from : Root.createStatic(f, to);

        Object entry = objects.get(to);
        if (entry == null) {
            if (save instanceof Object[]) {
                entry= new Object[] { save };
            } else {
                entry = save;
            }
        } else if (! (entry instanceof Object[])) {
                entry = new Object[] { entry, save };
        } else {
            int origLen = ((Object[])entry).length;
            Object[] ret = new Object[origLen + 1];
            System.arraycopy(entry, 0, ret, 0, origLen);
            ret[origLen] = save;
            entry = ret;
        }
        objects.put(to, entry);
    }

    private Iterable<Object> getIncomingRefs(Object to) {
        Object oo = objects.get(to);
        if (oo instanceof Object[]) {
            return Arrays.asList((Object[])oo);
        } else {
            return oo != null ? Collections.singleton(oo) : Collections.emptyList();
        }
    }
        
    
    public Map<Object,Path> trace(Collection<Object> objs, Set<Object> roots) {
        try {
            return traceImpl(objs, roots);
        } catch (CancelException ex) {
            return null;
        }
    }

    private Map<Object,Path> traceImpl(Collection<Object> objs, Set<Object> roots) {
        if (progress != null) {
            long usedMemory = Utils.getUsedMemory();
            objExpected = (int)(usedMemory / 50);
            objStep = objExpected / 200; // plan for 200 updates
            // cover only 90%
            progress.setRangeProperties(0, 0, 0, 10*objExpected/9, false);
        }
                
        for (Object o: objs ) rest.put(o, "");
        
        Map<Object,Boolean> s = new IdentityHashMap<>();
        for (Object o : ScannerUtils.interestingRoots()) {
            s.put(o, true);
        }
        if (roots != null) {
            for (Object o : roots) {
                s.put(roots, true);
            }
        }
        try {
            InsaneEngine iEngine = new InsaneEngine(this, filter, this, true);
            iEngine.traverse(s.keySet());
        } catch (CancelException ex) {
            throw ex;
        } catch (ObjectFoundException ex) {
        } catch (Exception e){
            e.printStackTrace();
        }
        
        if (progress != null) {
            progress.setValue(objExpected); // should move the mark to 90%
        }

        Map<Object,Path> result = new IdentityHashMap<>();
        
        // split last 10% of progress equally among found objects
        int found = objs.size() - rest.size();
        int base = objExpected;
        int step = found > 0 ? objExpected/9/found : 0;
        
        for(Object obj : objs) {
            if (!rest.containsKey(obj)) { // not found
                Path toObj = findRoots(obj, s.keySet());
                if (toObj != null) {
                    result.put(obj, toObj);
                }
                if (progress != null) {
                    base += step;
                    progress.setValue(base);
                }
            }
        }

        return result;
    }

    private Path findRoots(Object obj, Set<?> roots) {
        Set<Path> visited = new HashSet<>();
        Path last = Utils.createPath(obj, null);

        List<Path> queue = new LinkedList<>();
        queue.add(last);
        visited.add(last);

        while (!queue.isEmpty()) {
            Path act = queue.remove(0);
            Object item = act.getObject();

            if (roots.contains(item)) {
                return act; // XXX provide RootPath wrapper
            }

            // follow incomming
            for(Object o : getIncomingRefs(item)) {
                Path prev = Utils.createPath(o, act);
                if (o instanceof Root) return prev;

                if (!visited.contains(prev)) { // add to the queue if not new
                    visited.add(prev);
                    queue.add(prev);
                }
            }
        }
        return null; // Not found
    }

    
}    
