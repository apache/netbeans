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

package org.netbeans.modules.j2ee.deployment.plugins.spi;

import org.openide.util.Lookup;

/**
 * interface for inclusion of 3rd party content in
 * {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform}'s lookup.
 * Typically, if the serverplugin type allows composition of lookup from
 * multiple sources, it will make a layer location public where 3rd parties
 * will register implementations of this interface.
 * @author phejl, mkleint
 * @since 1.50
 */
public interface LookupProvider {
    
    /**
     * implementations will be asked to create their additional project lookup based on the baseContext
     * passed as parameter. The content of baseLookup is undefined on this level, is a contract
     * of the actual serverplugin type. Each implementation is only asked once for it's lookup
     * for a given platform instance at the time when platform's lookup is being created.
     * @param baseContext implementation shall decide what to return for a given platform instance based on context
     *  passed in.
     * @return a {@link org.openide.util.Lookup} instance that is to be added to the platform's lookup, never null.
     */ 
    Lookup createAdditionalLookup(Lookup baseContext);
}
