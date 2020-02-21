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
import org.netbeans.modules.cnd.apt.support.APTToken;

/**
 * base class for nodes handling tokens and children
 */
public abstract class APTTokenAndChildBasedNode extends APTTokenBasedNode 
                                                implements APTNodeBuilder, Serializable {
    private static final long serialVersionUID = 1564950303841807099L;
    transient private APT child;
    
    /** Copy constructor */
    /**package*/APTTokenAndChildBasedNode(APTTokenAndChildBasedNode orig) {
        super(orig);
        // clear tree structure information
        this.child = null;
    }
    
    /** Constructor for serialization */
    protected APTTokenAndChildBasedNode() {
    }
    
    /** Creates a new instance of APTTokenAndChildBasedNode */
    public APTTokenAndChildBasedNode(APTToken token) {
        super(token);
    }


    @Override
    public APT getFirstChild() {
        return child;
    } 

    ////////////////////////////////////////////////////////////////////////////
    // implementation details
    
    @Override
    public final void setFirstChild(APT child) {
        assert (child != null) : "why added null child?"; // NOI18N
        assert (this.child == null) : "why do you change immutable APT?"; // NOI18N
        this.child = child;
    }
    
    @Override
    public APTBaseNode getNode() {
        return this;
    }
}
