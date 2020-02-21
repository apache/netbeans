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

package org.netbeans.modules.cnd.api.model.xref;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.openide.util.Lookup;

/**
 * entry point to resolve usages of types
 */
public abstract class CsmTypeHierarchyResolver {
    /** A dummy resolver that never returns any results.
     */
    private static final CsmTypeHierarchyResolver EMPTY = new Empty();
    
    /** default instance */
    private static CsmTypeHierarchyResolver defaultResolver;
    
    protected CsmTypeHierarchyResolver() {
    }
    
    /** Static method to obtain the resolver.
     * @return the resolver
     */
    public static CsmTypeHierarchyResolver getDefault() {
        /*no need for sync synchronized access*/
        if (defaultResolver != null) {
            return defaultResolver;
        }
        defaultResolver = Lookup.getDefault().lookup(CsmTypeHierarchyResolver.class);
        return defaultResolver == null ? EMPTY : defaultResolver;
    }
    
    /**
     * Get subtypes for referenced class.
     * Return collection of class references that direct or inderect extend referenced class.
     */
    public abstract Collection<CsmReference> getSubTypes(CsmClass referencedClass, boolean directSubtypesOnly);
    
    //
    // Implementation of the default resolver
    //
    private static final class Empty extends CsmTypeHierarchyResolver {
        Empty() {
        }

        @Override
        public Collection<CsmReference> getSubTypes(CsmClass referencedClass, boolean directSubtypesOnly) {
            return Collections.<CsmReference>emptyList();
        }
    }    
}
