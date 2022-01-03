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
import org.netbeans.modules.cnd.apt.structure.APTUnknown;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * impl for unknown #-directive with it's content till end of line
 */
public final class APTUnknownNode extends APTStreamBaseNode 
                                    implements APTUnknown, Serializable {
    private static final long serialVersionUID = -6159626009326550770L;
    
    /** Copy constructor */
    /**package*/ APTUnknownNode(APTUnknownNode orig) {
        super(orig);
        assert (false) : "are you sure it's correct to make copy of unknown node?"; // NOI18N
    }
    
    /** constructor for serialization **/
    protected APTUnknownNode() {
    }
    
    /**
     * Creates a new instance of APTUnknownNode
     */
    public APTUnknownNode(APTToken token) {
        super(token);
    }
    
    @Override
    public final int getType() {
        return APT.Type.PREPROC_UNKNOWN;
    }
    
    @Override
    protected boolean validToken(APTToken t) {
        assert (t != null);
        int ttype = t.getType();
        assert (!APTUtils.isEOF(ttype)) : "EOF must be handled in callers"; // NOI18N
        // eat all till END_PREPROC_DIRECTIVE
        return !APTUtils.isEndDirectiveToken(ttype);
    }    
}
