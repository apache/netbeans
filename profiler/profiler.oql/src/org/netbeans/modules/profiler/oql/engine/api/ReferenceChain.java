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
package org.netbeans.modules.profiler.oql.engine.api;

import java.lang.ref.WeakReference;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;

/**
 * Represents a chain of references to some target object
 *
 * @author      Bill Foote
 */
final public class ReferenceChain {
    private WeakReference<Object> obj;	// Object referred to
    ReferenceChain next;	// Next in chain
    private Heap heap;
    private long id;
    private char type;
    
    private static char TYPE_INSTANCE = 0;
    private static char TYPE_CLASS = 1;
    
    public ReferenceChain(Heap heap, Object obj, ReferenceChain next) {
        this.obj = new WeakReference<>(obj);
        this.next = next;
        this.heap = heap;
        
        if (obj instanceof Instance) {
            type = TYPE_INSTANCE;
            id = ((Instance)obj).getInstanceId();
        } else if (obj instanceof JavaClass) {
            type = TYPE_CLASS;
            id = ((JavaClass)obj).getJavaClassId();
        }
    }

    public Object getObj() {
        Object o = obj.get();
        if (o == null) {
            if (type == TYPE_INSTANCE) {
                o = heap.getInstanceByID(id);
            } else if (type == TYPE_CLASS) {
                o = heap.getJavaClassByID(id);
            }
            obj = new WeakReference<>(o);
        }
        return o;
    }

    public ReferenceChain getNext() {
        return next;
    }
    
    public boolean contains(Object obj) {
        ReferenceChain tmp = this;
        while (tmp != null) {
            if (tmp.getObj().equals(obj)) return true;
            tmp = tmp.next;
        }
        return false;
    }

    public int getDepth() {
        int count = 1;
        ReferenceChain tmp = next;
        while (tmp != null) {
            count++;
            tmp = tmp.next;
        }
        return count;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (obj instanceof Instance) {
            Instance inst = (Instance)obj;
            sb.append(inst.getJavaClass().getName()).append("#").append(inst.getInstanceNumber());
        } else if (obj instanceof JavaClass) {
            sb.append("class of ").append(((JavaClass)obj).getName());
        }
        sb.append(next != null ? ("->" + next.toString()) : "");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ReferenceChain other = (ReferenceChain) obj;
        if (this.obj != other.obj && (this.obj == null || !this.obj.equals(other.obj))) {
            return false;
        }
        if (this.next != other.next && (this.next == null || !this.next.equals(other.next))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + (this.obj != null ? this.obj.hashCode() : 0);
        hash = 79 * hash + (this.next != null ? this.next.hashCode() : 0);
        return hash;
    }
}
