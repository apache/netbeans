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
package org.netbeans.modules.css.prep.editor.model;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.openide.filesystems.FileObject;

/**
 * Resolved element.
 * 
 * Can hold model and parser result!
 *
 * @author marekfukala
 */
public class CPElement {

    private CPElementHandle handle;
    private OffsetRange range; 
    private OffsetRange scope;

    public CPElement(CPElementHandle handle, OffsetRange range, OffsetRange scope) {
        this.handle = handle;
        this.range = range;
        this.scope = scope;
    }
    
    public String getName() {
        return getHandle().getName();
    }
    
    public CPElementType getType() {
        return getHandle().getType();
    }
    
    public FileObject getFile() {
        return getHandle().getFile();
    }

    /**
     * range of the element itself.
     */
    public OffsetRange getRange() {
        return range;
    }

    /**
     * range of the element scope.
     * 
     * null means no scope 
     */
    @CheckForNull
    public OffsetRange getScope() {
        return scope;
    }
    
    void setScope(OffsetRange scope) {
        this.scope = scope;
    }

    public CPElementHandle getHandle() {
        return handle;
    }
    
    public static Collection<CPElementHandle> toHandles(Collection<CPElement> elements) {
        Collection<CPElementHandle> handles = new ArrayList<>();
        for(CPElement e : elements) {
            handles.add(e.getHandle());
        }
        return handles;
    }
    
}
