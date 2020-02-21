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

package org.netbeans.modules.cnd.debug;

/**
 *
 */
public interface CndTraceFlags {
    public static final boolean TRACE_SLICE_DISTIBUTIONS = DebugUtils.getBoolean("cnd.slice.trace", false); // NOI18N

    public static final boolean LANGUAGE_FLAVOR_CPP11 = DebugUtils.getBoolean("cnd.language.flavor.cpp11", false); // NOI18N
    public static final boolean LANGUAGE_FLAVOR_CPP14 = DebugUtils.getBoolean("cnd.language.flavor.cpp14", false); // NOI18N
    public static final boolean LANGUAGE_FLAVOR_CPP17 = DebugUtils.getBoolean("cnd.language.flavor.cpp17", false); // NOI18N

    // use of weak refs instead of soft to allow quicker GC
    public static final boolean WEAK_REFS_HOLDERS = DebugUtils.getBoolean("cnd.weak.refs", false); // NOI18N
    public static final boolean WEAK_REFS_HOLDERS_FILE_STATE = DebugUtils.getBoolean("cnd.weak.refs.file.state", false); // NOI18N
    
    // use L1 cache in FileUtils
    public static final boolean L1_CACHE_FILE_UTILS = DebugUtils.getBoolean("cnd.l1.cache.file.utils", true); // NOI18N

    public static final boolean TEXT_INDEX = DebugUtils.getBoolean("cnd.model.text.index", true); // NOI18N
    
    public static final boolean USE_INDEXING_API = DebugUtils.getBoolean("cnd.use.indexing.api", true); // NOI18N
  
    public static final boolean USE_CSM_CACHE = DebugUtils.getBoolean("cnd.use.csm.cache", true); // NOI18N;

    public static final boolean TRACE_CSM_CACHE = DebugUtils.getBoolean("cnd.trace.csm.cache", false); // NOI18N;
}
