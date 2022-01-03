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

import java.util.Stack;
import org.netbeans.modules.cnd.api.model.CsmFile;

/**
 *
 */

/**
 * Represents a point in file
 */
public final class CsmResolveContext {
    
    private final CsmFile file;
    
    private final int offset;
    
    
    public static CsmResolveContext create(CsmFile file, int offset) {
        return new CsmResolveContext(file, offset);
    }    
    
    /**
     * Finds last created and registered resolve context (This is done in CsmCompletionQuery)
     * 
     * @return last resolve context if there is one or null
     */
    public static CsmResolveContext getLast() {
        CsmResolveContext context;
        Stack<CsmResolveContext> contexts = (Stack<CsmResolveContext>) CsmCacheManager.get(CsmResolveContext.class);
        context = (contexts != null && !contexts.empty()) ? contexts.peek() : null;
        return context;
    }
    
    public CsmFile getFile() {
        return file;
    }
        
    public int getOffset() {
        return offset;
    }
    
    
    private CsmResolveContext(CsmFile file, int offset) {
        this.file = file;
        this.offset = offset;
    }
}
