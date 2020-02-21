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

package org.netbeans.modules.cnd.asm.model.util;

import org.openide.util.Lookup;

import org.netbeans.modules.cnd.asm.model.AsmState;
import org.netbeans.modules.cnd.asm.model.lang.AsmElement;
import org.netbeans.modules.cnd.asm.model.lang.impl.LeafAsmElement;
import org.openide.util.Pair;

public class EmptyAsmState implements AsmState,
                              AsmFakeable {

    private static final AsmElement EMPTY = 
                        new LeafAsmElement() { };
    
    public AsmElement getElements() {
        return EMPTY;
    }

    public Pair<AsmElement, AsmElement> resolveLink(int pos) {
        return null;
    }

    public Lookup getServices() {
        return Lookup.EMPTY;
    }    
}
