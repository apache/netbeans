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

package org.netbeans.modules.cnd.completion.services;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionSupport;
import org.netbeans.modules.cnd.spi.model.services.CsmOverloadingResolverImplementation;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.spi.model.services.CsmOverloadingResolverImplementation.class)
public class CsmOverloadingResolverImpl implements CsmOverloadingResolverImplementation {

    @Override
    public Collection<CsmFunction> resolveOverloading(Collection<CsmFunction> methods, CharSequence instantiationDescriptor, Map<CsmFunction, List<CsmType>> paramTypes) {
        // TODO: make generic expresiion from instantiationDescriptor
        return CompletionSupport.filterMethods(null, methods, paramTypes, null, false, true);
    }
    
}
