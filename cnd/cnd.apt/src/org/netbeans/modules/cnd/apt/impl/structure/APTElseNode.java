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
import org.netbeans.modules.cnd.apt.structure.APTElse;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * #else directive implementation
 */
public final class APTElseNode extends APTTokenAndChildBasedNode 
                                implements APTElse, Serializable {
    private static final long serialVersionUID = 8350237885177577182L;
    
    /** Copy constructor */
    /**package*/APTElseNode(APTElseNode orig) {
        super(orig);
    }
    
    /** Constructor for serialization */
    protected APTElseNode() {
    }
    
    /** Creates a new instance of APTElseNode */
    public APTElseNode(APTToken token) {
        super(token);
    }
    
    @Override
    public final int getType() {
        return APT.Type.ELSE;
    }
    
    @Override
    public boolean accept(APTFile curFile,APTToken token) {
        assert (token != null);
        int ttype = token.getType();
        assert (!APTUtils.isEOF(ttype)) : "EOF must be handled in callers"; // NOI18N
        // eat all till END_PREPROC_DIRECTIVE
        return !APTUtils.isEndDirectiveToken(ttype);
    }
    
}
