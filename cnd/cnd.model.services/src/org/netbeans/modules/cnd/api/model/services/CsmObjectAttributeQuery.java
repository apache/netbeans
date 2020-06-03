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

import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class CsmObjectAttributeQuery {
    /** A dummy query that never returns any results.
     */
    private static final CsmObjectAttributeQuery EMPTY = new Empty();
    
    /** default instance */
    private static CsmObjectAttributeQuery defaultQuery;
    
    protected CsmObjectAttributeQuery() {
    }
    
    /** Static method to obtain the auery.
     * @return the query
     */
    public static CsmObjectAttributeQuery getDefault() {
        /*no need for sync synchronized access*/
        if (defaultQuery != null) {
            return defaultQuery;
        }
        defaultQuery = Lookup.getDefault().lookup(CsmObjectAttributeQuery.class);
        return defaultQuery == null ? EMPTY : defaultQuery;
    }

    public abstract int getLeftBracketOffset(CsmNamespaceDefinition nsd);
    
    private static final class Empty extends CsmObjectAttributeQuery {
        @Override
        public int getLeftBracketOffset(CsmNamespaceDefinition nsd) {
            return nsd.getStartOffset();
        }
    }
}
