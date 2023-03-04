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

package org.netbeans.modules.debugger.jpda.heapwalk;

import org.netbeans.lib.profiler.heap.GCRoot;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.HeapSummary;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

import java.util.regex.Pattern;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;

/**
 *
 * @author Martin Entlicher
 */
public class HeapImpl implements Heap {
    
    private JPDADebugger debugger;
    private final Object classesCacheAccessLock = new Object();
    private List<JavaClass> classesCache;
    private long[] instanceTotalCountPtr = new long[] { -1 };
    
    /** Creates a new instance of HeapImpl */
    public HeapImpl(JPDADebugger debugger) {
        this.debugger = debugger;
    }
    
    public JPDADebugger getDebugger() {
        return debugger;
    }

    @Override
    public HeapSummary getSummary() {
        return new DebuggerHeapSummary(debugger, instanceTotalCountPtr);
    }

    public void computeClasses() {
        List<JPDAClassType> allClasses = debugger.getAllClasses();
        long[] counts = debugger.getInstanceCounts(allClasses);
        long sum = 0;
        for (long c : counts) {
            sum += c;
        }
        instanceTotalCountPtr[0] = sum;
        List<JavaClass> javaClasses = new ArrayList<JavaClass>(allClasses.size());
        int i = 0;
        for (JPDAClassType clazz : allClasses) {
            javaClasses.add(new JavaClassImpl(this, clazz, counts[i++]));
        }
        synchronized (classesCacheAccessLock) {
            this.classesCache = javaClasses;
        }
    }

    @Override
    public List<JavaClass> getAllClasses() {
        List<JavaClass> javaClasses;
        synchronized (classesCacheAccessLock) {
            javaClasses = classesCache;
            classesCache = null;
        }
        if (javaClasses != null) {
            return javaClasses;
        } else {
            computeClasses();
            return getAllClasses();
        }
    }
    
    @Override
    public List getBiggestObjectsByRetainedSize(int number) {
        return null;
    }
 
    @Override
    public Instance getInstanceByID(long id) {
         return null;
    }
    
    @Override
    public JavaClass getJavaClassByID(long id) {
        return null;
    }
    
    @Override
    public JavaClass getJavaClassByName(String name) {
        List<JPDAClassType> classes = debugger.getClassesByName(name);
        if (classes.isEmpty()) {
            return null;
        }
        return new JavaClassImpl(this, classes.get(0), classes.get(0).getInstanceCount());
    }

    @Override
    public Collection getJavaClassesByRegExp(String regexp) {
        List<JPDAClassType> allClasses = debugger.getAllClasses();
        Collection<JavaClass> result = new ArrayList<JavaClass>(256);
        Pattern pattern = Pattern.compile(regexp);

        for (JPDAClassType clazz : allClasses) {
            if (pattern.matcher(clazz.getName()).matches()) {
                result.add(new JavaClassImpl(this, clazz, clazz.getInstanceCount()));
            }
        }
        return result;
    }


    @Override
    public Collection getGCRoots() {
        return Collections.emptyList();
    }

    @Override
    public GCRoot getGCRoot(Instance instance) {
        return null;
    }
    
    @Override
    public Properties getSystemProperties() {
        // TODO
        return null;
    }

    @Override
    public Iterator getAllInstancesIterator() {
        List classes = getAllClasses();
        
        if (classes != null && !classes.isEmpty()) {
            return new InstancesIterator(classes);
        }
        return Collections.emptyIterator();
    }

    @Override
    public boolean isRetainedSizeComputed() {
        return true;
    }

    @Override
    public boolean isRetainedSizeByClassComputed() {
        return true;
    }

    private static class InstancesIterator implements Iterator {

        private Iterator clsIt;
        private Iterator instIt;
        
        private InstancesIterator(List classes) {
            clsIt = classes.iterator();
            instIt = ((JavaClass)clsIt.next()).getInstances().iterator();
        }
        
        @Override
        public boolean hasNext() {
            if (instIt.hasNext()) {
                return true;
            }
            if (clsIt.hasNext()) {
                instIt = ((JavaClass)clsIt.next()).getInstances().iterator();
                return hasNext();
            }
            return false;
        }

        @Override
        public Object next() {
            if (hasNext()) return instIt.next();
            throw new NoSuchElementException();
        }     
    }
    
    private static final class DebuggerHeapSummary implements HeapSummary {
        
        private JPDADebugger debugger;
        private long[] instanceTotalCountPtr;
        
        public DebuggerHeapSummary(JPDADebugger debugger, long[] instanceTotalCountPtr) {
            this.debugger = debugger;
            this.instanceTotalCountPtr = instanceTotalCountPtr;
        }
        
        @Override
        public long getTotalLiveBytes() {
            return -1;
        }

        @Override
        public long getTotalLiveInstances() {
            long sum = instanceTotalCountPtr[0];
            if (sum >= 0) {
                return sum;
            }
            long[] counts = debugger.getInstanceCounts(debugger.getAllClasses());
            sum = 0;
            for (long c : counts) {
                sum += c;
            }
            return sum;
        }

        @Override
        public long getTotalAllocatedBytes() {
            return -1L;
        }

        @Override
        public long getTotalAllocatedInstances() {
            return -1L;
        }

        @Override
        public long getTime() {
            return System.currentTimeMillis();
        }
        
    }
    
}
