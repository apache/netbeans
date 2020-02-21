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
package org.netbeans.modules.cnd.refactoring.plugins;

/**
 * Filters used by Csm Find Usages plugins.
 *
 */
public enum CsmWhereUsedFilters {
    COMMENTS("filter-comments"), // NOI18N
    DEAD_CODE("filter-deadcode"), // NOI18N
    MACROS("filter-macros"), // NOI18N
    READ("filter-read"), // NOI18N
    WRITE("filter-write"), // NOI18N
    READ_WRITE("filter-readwrite"), // NOI18N
    DECLARATIONS("filter-declarations"), // NOI18N
    SCOPE("filter-scope"); // NOI18N
    
    private final String key;

    private CsmWhereUsedFilters(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
    
}
