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

import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.css.prep.editor.model.CPElementHandle;
import org.netbeans.modules.css.prep.editor.model.CPElementType;
import org.openide.util.ImageUtilities;

/**
 *
 * @author marekfukala
 */
public class VariableCompletionItem extends CPCompletionItem {

    private static final ImageIcon LOCAL_VAR_ICON = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/css/prep/editor/resources/localVariable.gif")); //NOI18N

    /**
     * 
     * @param elementHandle
     * @param handle
     * @param anchorOffset
     * @param origin Origin is null for current file. File displayname otherwise.
     */
    public VariableCompletionItem(@NonNull ElementHandle elementHandle, @NonNull CPElementHandle handle, int anchorOffset, @NullAllowed String origin) {
        super(elementHandle, handle, anchorOffset, origin);
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.VARIABLE;
    }

    
    @Override
    public ImageIcon getIcon() {
        switch (handle.getType()) {
            case VARIABLE_LOCAL_DECLARATION:
            case VARIABLE_DECLARATION_IN_BLOCK_CONTROL:
                return LOCAL_VAR_ICON;
            default:
                return super.getIcon();
        }
    }

    @Override
    public String getInsertPrefix() {
        return handle.getName();
    }

    @Override
    public String getName() {
        return handle.getName().substring(1); //strip off the leading $ or @ sign
    }
    
    private boolean isGlobalVar() {
        return handle.getType() == CPElementType.VARIABLE_GLOBAL_DECLARATION;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (this.origin != null ? this.origin.hashCode() : 0);
        hash = 89 * hash + getName().hashCode();
        hash = 89 * hash + (isGlobalVar() ? 1 : 0);
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
        final VariableCompletionItem other = (VariableCompletionItem) obj;
        if ((this.origin == null) ? (other.origin != null) : !this.origin.equals(other.origin)) {
            return false;
        }
        if (!getName().equals(other.getName())) {
            return false;
        }
        if(isGlobalVar() != other.isGlobalVar()) {
            return false;
        }
        
        return true;
    }

     @Override
    public int getSortPrioOverride() {
        int prio = 50;
        if (origin == null) {
            prio -= 40; //current file items have precedence
        }

        switch (handle.getType()) {
            case VARIABLE_GLOBAL_DECLARATION:
                prio -= 5;
                break;
            case VARIABLE_DECLARATION_IN_BLOCK_CONTROL:
            case VARIABLE_LOCAL_DECLARATION:
                prio -= 10;
                break;
            default:
        }
        return prio;
    }
    
    
}
