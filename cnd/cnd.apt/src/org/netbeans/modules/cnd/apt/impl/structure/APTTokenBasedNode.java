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
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * base impl for nodes with associated token
 */
public abstract class APTTokenBasedNode extends APTBaseNode 
                                        implements Serializable {
    private static final long serialVersionUID = -1540565849389504039L;
    // seems, all will have sibling and token, but not all childs
    transient private APT next;
    private APTToken token;
    
    /** Copy constructor */
    /**package*/APTTokenBasedNode(APTTokenBasedNode orig) {
        super(orig);
        this.token = orig.token;
        // clear tree structure information
        this.next = null;
    }
    
    /** constructor for serialization **/
    protected APTTokenBasedNode() {
    }
    
    /** Creates a new instance of APTTokenBasedNode */
    protected APTTokenBasedNode(APTToken token) {
        this.token = token;
    }
    
    @Override
    public APTToken getToken() {
        return token;
    }   
                
    @Override
    public int getOffset() {
        if (token != null && token != APTUtils.EOF_TOKEN) {
            return token.getOffset();
        }
        return 0;
    }
    
    @Override
    public int getEndOffset() {
        if (token != null && token != APTUtils.EOF_TOKEN) {
            return token.getEndOffset();
        }
        return 0;        
    }
    
    @Override
    public abstract APT getFirstChild();
    
    @Override
    public APT getNextSibling() {
        return next;
    }       
        
    @Override
    public String getText() {
        return "TOKEN{" + (getToken()!= null ? getToken().toString() : "") + "}"; // NOI18N
    }
    ////////////////////////////////////////////////////////////////////////////
    // implementation details
    
    /** 
     * sets next sibling element
     */
    @Override
    public final void setNextSibling(APT next) {
        assert (next != null) : "null sibling, what for?"; // NOI18N
        assert (this.next == null) : "why do you change immutable APT?"; // NOI18N
        this.next = next;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final APTTokenBasedNode other = (APTTokenBasedNode) obj;
        return getOffset() == other.getOffset() &&
                getEndOffset() == other.getEndOffset() &&
                getType() == other.getType() &&
                getToken().equals(other.getToken());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + getOffset();
        hash = 29 * hash + getEndOffset();
        hash = 29 * hash + getType();
        hash = 29 * hash + getToken().hashCode();
        return hash;
    }

    

    
    @Override
    public abstract void setFirstChild(APT child);
}
