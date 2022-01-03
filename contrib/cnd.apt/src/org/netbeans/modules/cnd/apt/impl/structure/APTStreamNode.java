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
import org.netbeans.modules.cnd.apt.structure.APTStream;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * implementation of APTStream
 */
public final class APTStreamNode extends APTStreamBaseNode 
                                    implements APTStream, Serializable {
    private static final long serialVersionUID = -6247236916195389448L;
    
    /** Copy constructor */
    /**package*/ APTStreamNode(APTStreamNode orig) {
        super(orig);
        assert (false) : "are you sure it's correct to make copy of stream node?"; // NOI18N
    }
    
    /** Constructor for serialization **/
    protected APTStreamNode() {
    }
    
    /** Creates a new instance of APTStreamNode */
    public APTStreamNode(APTToken token) {
        super(token);
        assert (validToken(token)) : "must init only from valid tokens"; // NOI18N
    }
    
    @Override
    public final int getType() {
        return APT.Type.TOKEN_STREAM;
    }    
    
    @Override
    protected boolean validToken(APTToken t) {
        if (t == null) {
            return false;
        }
        int ttype = t.getType();
        assert (!APTUtils.isEOF(ttype)) : "EOF must be handled in callers"; // NOI18N
        return !APTUtils.isPreprocessorToken(ttype);
    }    
}
