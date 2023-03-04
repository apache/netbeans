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
package org.netbeans.modules.css.prep.editor;

import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.css.prep.editor.model.CPElementHandle;

/**
 *
 * @author marekfukala
 */
public class MixinCompletionItem extends CPCompletionItem {
    
    /**
     * 
     * @param elementHandle
     * @param handle
     * @param anchorOffset
     * @param origin Origin is null for current file. File displayname otherwise.
     */
    public MixinCompletionItem(ElementHandle elementHandle, CPElementHandle handle, int anchorOffset, String origin) {
        super(elementHandle, handle, anchorOffset ,origin);
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.METHOD;
    }

    @Override
    public String getInsertPrefix() {
        return handle.getName();
    }

    @Override
    public String getName() {
        return handle.getName();
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (this.origin != null ? this.origin.hashCode() : 0);
        hash = 89 * hash + getName().hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MixinCompletionItem other = (MixinCompletionItem) obj;
        if ((this.origin == null) ? (other.origin != null) : !this.origin.equals(other.origin)) {
            return false;
        }
        if (!getName().equals(other.getName())) {
            return false;
        }
        return true;
    }

    @Override
    public int getSortPrioOverride() {
        int prio = 70;
        if(origin == null) {
            prio -= 40; //current file items have precedence
        }
        return prio;
    }
}
