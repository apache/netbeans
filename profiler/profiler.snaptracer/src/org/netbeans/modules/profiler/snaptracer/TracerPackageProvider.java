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

package org.netbeans.modules.profiler.snaptracer;

import org.netbeans.modules.profiler.snaptracer.impl.IdeSnapshot;


/**
 * Provider of TracerPackage(s) for a DataSource type.
 *
 * @author Jiri Sedlacek
 */
public abstract class TracerPackageProvider {

    private final Class scope;


    /**
     * Creates new instance of TracerPackageProvider with defined scope.
     *
     * @param scope scope of the provider
     */
    public TracerPackageProvider(Class scope) { this.scope = scope; }


    /**
     * Returns scope of the provider.
     *
     * @return scope of the provider
     */
    public final Class getScope() { return scope; }
    

    /**
     * Returns TracerPackages for the provided target.
     *
     * @return TracerPackages for the provided target
     */
    public abstract TracerPackage[] getPackages(IdeSnapshot snapshot);

}
