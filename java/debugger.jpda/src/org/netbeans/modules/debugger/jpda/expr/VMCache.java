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
package org.netbeans.modules.debugger.jpda.expr;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

/**
 * A cache of artefacts in the target VM.
 *
 * @author martin
 */
public class VMCache {

    private static final int MAX_ENCLOSING_TYPES = 100;

    private final JPDADebuggerImpl debugger;
    private final Map<String, ReferenceType> cachedClasses = new HashMap<>();
    private final Map<CEncl, ReferenceType> enclosingTypes = new LinkedHashMap<CEncl, ReferenceType>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<CEncl, ReferenceType> eldest) {
            return size() > MAX_ENCLOSING_TYPES;
        }
    };

    public VMCache(JPDADebuggerImpl debugger) {
        this.debugger = debugger;
        debugger.addPropertyChangeListener(JPDADebugger.PROP_CLASSES_FIXED,
                                           event -> reset());
    }

    private void reset() {
        synchronized (cachedClasses) {
            cachedClasses.clear();
        }
        synchronized (enclosingTypes) {
            enclosingTypes.clear();
        }
    }

    /**
     * Get a cached version of a basic Java class, which is used often.
     * Use for Java platform classes only.
     * @param name the class name
     * @return the reference type or <code>null</code>
     */
    ReferenceType getClass(String name) {
        ReferenceType rt;
        synchronized (cachedClasses) {
            rt = cachedClasses.get(name);
            if (rt == null) {
                rt = loadClass(name);
                if (rt != null) {
                    cachedClasses.put(name, rt);
                }
            }
        }
        return rt;
    }
    
    private ReferenceType loadClass(String name) {
        VirtualMachine vm = debugger.getVirtualMachine();
        if (vm == null) {
            return null;
        }
        List<ReferenceType> stringClasses = vm.classesByName(name);
        if (stringClasses.isEmpty()) {
            return null;
        }
        return stringClasses.get(0);
    }

    /**
     * Get a cached type of name 'name' that encloses 'type'.
     * @return The cached type, or <code>null</code>.
     */
    ReferenceType getEnclosingType(ReferenceType type, String name) {
        CEncl classEnclosing = new CEncl(type, name);
        ReferenceType enclosingType;
        synchronized (enclosingTypes) {
            enclosingType = enclosingTypes.get(classEnclosing);
        }
        return enclosingType;
    }

    /**
     * Set an enclosing type of name 'name' that encloses 'type'.
     */
    void setEnclosingType(ReferenceType type, String name, ReferenceType enclosingType) {
        CEncl classEnclosing = new CEncl(type, name);
        synchronized (enclosingTypes) {
            enclosingTypes.put(classEnclosing, enclosingType);
        }
    }

    private static class CEncl {

        final ReferenceType type;
        final String enclName;

        CEncl(ReferenceType type, String enclName) {
            this.type = type;
            this.enclName = enclName;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.type, this.enclName);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CEncl other = (CEncl) obj;
            if (!Objects.equals(this.enclName, other.enclName)) {
                return false;
            }
            if (!Objects.equals(this.type, other.type)) {
                return false;
            }
            return true;
        }

    }
}
