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

package org.netbeans.insane.scanner;

import java.util.*;

/**
 * A visitor implementation that counts occurence and total size of found
 * objects, classified by their class.
 *
 * Usage: use it as a {@link Visitor} for an engine. After the engine finishes,
 * you can query found classes and per class statistics using
 * {@link #getClasses()}, {@link #getCountForClass(java.lang.Class)},
 * {@link #getSizeForClass(java.lang.Class)}, and gobal statistics
 * using {@link #getTotalCount()} and {@link #getTotalSize}.
 *
 * @author Nenik
 */
public class CountingVisitor implements Visitor {
    
    private Map<Class<?>, Info> infoMap = new HashMap<Class<?>, Info>();
    private int count;
    private int size;
    
    /** Creates a new instance of CountingVisitor */
    public CountingVisitor() {
    }
    
    
    public void visitClass(Class<?> cls) {
        infoMap.put(cls, new Info());
    }
    
    public void visitObject(ObjectMap map, Object obj) {
        Info info = infoMap.get(obj.getClass());
        assert info != null : "Engine shall announce the class before instance";
        
        info.count++;
        count++;
        int objSize = ScannerUtils.sizeOf(obj); 
        info.size += objSize;
        size += objSize;
    }
    
    public void visitStaticReference(ObjectMap map, Object to, java.lang.reflect.Field ref) {}
    public void visitObjectReference(ObjectMap map, Object from, Object to, java.lang.reflect.Field ref) {}
    public void visitArrayReference(ObjectMap map, Object from, Object to, int index) {}  

    public Set<Class<?>> getClasses() {
        return Collections.unmodifiableSet(infoMap.keySet());
    }
    
    public int getCountForClass(Class cls) {
        Info info = infoMap.get(cls);
        if (info == null) throw new IllegalArgumentException("Unknown class");
        
        return info.count;
    }
    
    public int getSizeForClass(Class cls) {
        Info info = infoMap.get(cls);
        if (info == null) throw new IllegalArgumentException("Unknown class");
        
        return info.size;
    }
    
    public int getTotalCount() {
        return count;
    }
    
    public int getTotalSize() {
        return size;
    }
    
    
    // A structure holding statistics for one class.
    private static class Info {
        int count;
        int size;
    }
}
