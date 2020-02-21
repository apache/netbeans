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
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTToken;

/**
 * base implementation for APTContainer
 * - manage children
 */
public abstract class APTContainerNode extends APTBaseNode implements Serializable {
    private static final long serialVersionUID = 8306600642459659574L;
    transient private APT child;   

    /** Copy constructor */
    /**package*/ APTContainerNode(APTContainerNode orig) {
        super(orig);
        // clear tree structure information
        this.child = null;
    }
    
    protected APTContainerNode() {
    }
    
    @Override
    public APT getFirstChild() {
        return child;
    }     

    @Override
    public boolean accept(APTFile curFile,APTToken t) {
        return false;
    }    
    
    @Override
    public APTToken getToken() {
        return null;
    } 
    
    ////////////////////////////////////////////////////////////////////////////
    // implementation details
    
    @Override
    public final void setFirstChild(APT child) {
        assert (child != null) : "why added null child?"; // NOI18N
        assert (this.child == null) : "why do you change immutable APT?"; // NOI18N
        this.child = child;
    }    
}
