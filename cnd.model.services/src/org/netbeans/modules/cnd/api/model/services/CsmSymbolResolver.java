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

package org.netbeans.modules.cnd.api.model.services;

import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.spi.model.services.CsmSymbolResolverImplementation;
import org.openide.util.Lookup;

/**
 *
 */
public final class CsmSymbolResolver {
    
    /**
     * Resolves symbol by qualified name or 
     * signature for functions and methods.
     * 
     * Examples: 
     * 1) String "AAA<int>::BBB" will be resolved into symbol BBB 
     *    inside class AAA<int>
     * 2) String "aaa::foo(int)" is a signature and will be resolved into 
     *    function or method 'foo' which takes one integer parameter and 
     *    declared inside namespace or class 'aaa'
     * 3) String "int aaa::foo(int)" is a signature of a template 
     *    function or method 'foo' which takes one integer parameter, 
     *    declared inside namespace or class 'aaa' and returns integer
     * 
     * @param project
     * @param declText
     * 
     * @return all entities which have the same declaration text
     */      
    public static Collection<CsmOffsetable> resolveSymbol(NativeProject project, CharSequence declText) {
        return DEFAULT.resolveSymbol(project, declText);
    }    
    
    /**
     * Resolves symbol by qualified name or 
     * signature for functions and methods.
     * 
     * @param project
     * @param declText
     * 
     * @return all entities which have the same declaration text
     */ 
    public static Collection<CsmOffsetable> resolveSymbol(CsmProject project, CharSequence declText) {
        return DEFAULT.resolveSymbol(project, declText);
    }        
        
    /**
     * Resolves function by name in global namespace.
     * Does not wait until project parse is finished.
     * 
     * @param project
     * @param functionName
     * 
     * @return all function definitions with name
     */    
    public static Collection<CsmOffsetable> resolveGlobalFunction(NativeProject project, CharSequence functionName) {
        return DEFAULT.resolveGlobalFunction(project, functionName);
    }        
    //<editor-fold defaultstate="collapsed" desc="Implementation">

    private static final CsmSymbolResolverImplementation DEFAULT = new Default();
    
    private CsmSymbolResolver() {
        throw new AssertionError("Not instantiable"); // NOI18N
    }
    
    /**
     * Default implementation (just a proxy to a real service)
     */
    private static final class Default implements CsmSymbolResolverImplementation {
        
        private final Lookup.Result<CsmSymbolResolverImplementation> res;
        
        private CsmSymbolResolverImplementation delegate;
        
        
        private Default() {
            res = Lookup.getDefault().lookupResult(CsmSymbolResolverImplementation.class);
        }
        
        private CsmSymbolResolverImplementation getDelegate(){
            CsmSymbolResolverImplementation service = delegate;
            if (service == null) {
                for (CsmSymbolResolverImplementation resolver : res.allInstances()) {
                    service = resolver;
                    break;
                }
                delegate = service;
            }
            return service;
        }
        
        @Override
        public Collection<CsmOffsetable> resolveSymbol(NativeProject project, CharSequence declText) {
            return getDelegate().resolveSymbol(project, declText);
        }
        
        @Override
        public Collection<CsmOffsetable> resolveSymbol(CsmProject project, CharSequence declText) {
            return getDelegate().resolveSymbol(project, declText);
        }

        @Override
        public Collection<CsmOffsetable> resolveGlobalFunction(NativeProject project, CharSequence functionName) {
            return getDelegate().resolveGlobalFunction(project, functionName);
        }
    }
//</editor-fold>
}
