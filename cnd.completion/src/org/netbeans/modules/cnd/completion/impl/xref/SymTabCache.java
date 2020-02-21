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

package org.netbeans.modules.cnd.completion.impl.xref;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.completion.csm.CompletionResolver.Result;

/**
 *
 *
 */
public final class SymTabCache {

    private final Map<CacheEntry, Result> cache = new HashMap<CacheEntry, Result>();

    /*package-local*/ SymTabCache() {
    }

    private Map<CacheEntry, Result> getCache(){
        return cache;
    }

    public Result get(CacheEntry key){
        return getCache().get(key);
    }

    public void clear(){
        getCache().clear();
    }

    public Result put(CacheEntry key, Result value){
        return getCache().put(key, value);
    }

    public void setScope(CsmUID uid){
        Iterator<CacheEntry> it = getCache().keySet().iterator();
        if (it.hasNext()) {
            CacheEntry entry = it.next();
            if (!entry.getScope().equals(uid)){
                getCache().clear();
            }
        }
    }

    public static final class CacheEntry {
        private final int resolve;
        private final int hide;
        private final String name;
        private final CsmUID scope;

        public CacheEntry(int resolve, int hide, String name, CsmUID scope){
            this.resolve = resolve;
            this.hide = hide;
            this.name = name;
            this.scope = scope;
        }

        public CsmUID getScope(){
            return scope;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CacheEntry)){
                return false;
            }
            CacheEntry o = (CacheEntry) obj;
            return resolve == o.resolve && hide == o.hide &&
                   name.equals(o.name) && scope.equals(o.scope);
        }

        @Override
        public int hashCode() {
            return resolve + 17*(hide + 17*(name.hashCode()+17*scope.hashCode()));
        }
    }
}
