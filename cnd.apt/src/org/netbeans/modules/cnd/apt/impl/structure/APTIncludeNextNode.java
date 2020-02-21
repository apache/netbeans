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
import org.netbeans.modules.cnd.apt.structure.APTIncludeNext;
import org.netbeans.modules.cnd.apt.support.APTToken;

/**
 * #include_next directive implementation
 */
public final class APTIncludeNextNode extends APTIncludeBaseNode 
                                        implements APTIncludeNext, Serializable {
    private static final long serialVersionUID = -1545162066559611779L;
    
    /** Copy constructor */
    /**package*/ APTIncludeNextNode(APTIncludeNextNode orig) {
        super(orig);
    }
    
    /** Constructor for serialization */
    protected APTIncludeNextNode() {
    }
    
    /** Creates a new instance of APTIncludeNextNode */
    public APTIncludeNextNode(APTToken token) {
        super(token);
    } 
    
    @Override
    public final int getType() {
        return APT.Type.INCLUDE_NEXT;
    }    
}
