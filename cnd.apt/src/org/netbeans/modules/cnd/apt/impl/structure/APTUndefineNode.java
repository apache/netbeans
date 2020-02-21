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
import org.netbeans.modules.cnd.apt.structure.APTUndefine;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * #undef directive implementation
 */
public final class APTUndefineNode extends APTMacroBaseNode 
                                    implements APTNodeBuilder, APTUndefine, Serializable {
    private static final long serialVersionUID = 3929923839413486096L;
    
    /** Copy constructor */
    /**package*/ APTUndefineNode(APTUndefineNode orig) {
        super(orig);
    }
    
    /** constructor for serialization **/
    protected APTUndefineNode() {
    }
    
    /** Creates a new instance of APTUndefineNode */
    public APTUndefineNode(APTToken token) {
        super(token);
    }
    
    @Override
    public boolean accept(APTFile curFile, APTToken token) {
        int ttype = token.getType();
        super.accept(curFile, token);
        return !APTUtils.isEndDirectiveToken(ttype);
    }
    
    @Override
    public final int getType() {
        return APT.Type.UNDEF;
    }
    
    @Override
    public APTBaseNode getNode() {
        return this;
    }
}
