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
package org.netbeans.modules.editor.mimelookup;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.openide.util.Lookup;

/**
 * Caching of MimeLookups. Since MimePath is a global object, it cannot cache potentially
 * execution-specific data, such as the concrete MimeLookup. Instead, the MimePath looks up
 * the intrinsic data in the cache, which can be potentially execution-specific.
 * <p/>
 * MimePath obtains the MimeLookupCacheSPI from the default Lookup supposing that if some
 * local execution context is active, the infrastructure placed a separate MimeLookupCacheSPI
 * implementation into it.
 * <p/>
 * @author sdedic
 */
public abstract class MimeLookupCacheSPI {
    /**
     * Obtains a MIME-specific Lookup from the cache. If the Lookup does not exist,
     * it is created. The cache implementor is responsible for synchronization, only one
     * Lookup instance can be returned from the cache for the given execution context and MimePath.
     * @param mp MimePath to select the Lookup contents
     * @return Lookup instance
     */
    public abstract Lookup   getLookup(MimePath mp);
}
