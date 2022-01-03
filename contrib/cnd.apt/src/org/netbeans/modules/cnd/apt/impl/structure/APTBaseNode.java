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

package org.netbeans.modules.cnd.apt.impl.structure;

import java.io.Serializable;
import org.netbeans.modules.cnd.apt.structure.APT;

/**
 * base APT node impl
 */
public abstract class APTBaseNode implements APT, Serializable {
    private static final long serialVersionUID = -7790789617759717719L;
    
    /** Copy constructor */
    /**package*/APTBaseNode(APTBaseNode orig) {
    }
    
    /** Creates a new instance of APTBaseNode */
    protected APTBaseNode() {
    }    
    
    @Override
    public String toString() {
        return getText();
    }

    /**Add a node to the end of the child list for this node */
    protected final void addChild(APT node) {
        if (node == null) {
            return;
        }
        APT t = getFirstChild();
        if (t != null) {
            while (t.getNextSibling() != null) {
                t = t.getNextSibling();
            }
            ((APTBaseNode)t).setNextSibling(node);
        } else {
            setFirstChild(node);
        }
    }     
    
    ////////////////////////////////////////////////////////////////////////////
    // implementation details
    
    /** 
     * sets next sibling element
     */
    @Override
    public abstract void setNextSibling(APT next);
    
    /** 
     * sets first child element
     */
    @Override
    public abstract void setFirstChild(APT child);        
}
