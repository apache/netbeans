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
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.spi.model.services.CsmOverloadingResolverImplementation;
import org.openide.util.Lookup;

/**
 *
 */
public final class CsmOverloadingResolver {
    
    /**
     * Determines what method is the best match for the given instantiation descriptor and paramTypes
     * 
     * @param methods
     * @param instantiationDescriptor
     * @param paramTypes
     * 
     * @return best matches
     */
    public static Collection<CsmFunction> resolveOverloading(Collection<CsmFunction> methods, CharSequence instantiationDescriptor, Map<CsmFunction, List<CsmType>> paramTypes) {
        return DEFAULT.resolveOverloading(methods, instantiationDescriptor, paramTypes);
    }
    
//<editor-fold defaultstate="collapsed" desc="Implementation">
    
    private static final CsmOverloadingResolverImplementation DEFAULT = new Default();
    
    private CsmOverloadingResolver() {
        throw new AssertionError("Not instantiable"); // NOI18N
    }
    
    /**
     * Default implementation (just a proxy to a real service)
     */
    private static final class Default implements CsmOverloadingResolverImplementation {
        
        private final Lookup.Result<CsmOverloadingResolverImplementation> res;
        
        private CsmOverloadingResolverImplementation delegate;
        
        
        private Default() {
            res = Lookup.getDefault().lookupResult(CsmOverloadingResolverImplementation.class);
        }
        
        private CsmOverloadingResolverImplementation getDelegate(){
            CsmOverloadingResolverImplementation service = delegate;
            if (service == null) {
                for (CsmOverloadingResolverImplementation resolver : res.allInstances()) {
                    service = resolver;
                    break;
                }
                delegate = service;
            }
            return service;
        }
        
        @Override
        public Collection<CsmFunction> resolveOverloading(Collection<CsmFunction> methods, CharSequence instantiationDescriptor, Map<CsmFunction, List<CsmType>> paramTypes) {
            return getDelegate().resolveOverloading(methods, instantiationDescriptor, paramTypes);
        }
    }
//</editor-fold>    
}
