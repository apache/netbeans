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
import org.netbeans.modules.cnd.apt.structure.APTEndif;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * #endif directive implementation
 */
public final class APTEndifNode extends APTTokenBasedNode 
                                implements APTNodeBuilder, APTEndif, Serializable {
    private static final long serialVersionUID = 6797353042752788870L;
    
    private int endOffset = 0;

    /** Copy constructor */
    /**package*/APTEndifNode(APTEndifNode orig) {
        super(orig);
    }
    
    /** Constructor for serialization */
    protected APTEndifNode() {
    }
    
    /** Creates a new instance of APTEndifNode */
    public APTEndifNode(APTToken token) {
        super(token);
    }    
    
    @Override
    public final int getType() {
        return APT.Type.ENDIF;
    }
    
    @Override
    public APT getFirstChild() {
        // #endif doesn't have subtree
        return null;
    }

    @Override
    public boolean accept(APTFile curFile,APTToken token) {
        assert (token != null);
        int ttype = token.getType();
        assert (!APTUtils.isEOF(ttype)) : "EOF must be handled in callers"; // NOI18N
        // eat all till END_PREPROC_DIRECTIVE        
        if (APTUtils.isEndDirectiveToken(ttype)) {
            endOffset = token.getOffset();
            return false;
        } else {
            return true;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // implementation details
      
    @Override
    public void setFirstChild(APT child) {
        // do nothing
        assert (false) : "endif doesn't support children"; // NOI18N
    }
    
    @Override
    public APTBaseNode getNode() {
        return this;
    }
}
