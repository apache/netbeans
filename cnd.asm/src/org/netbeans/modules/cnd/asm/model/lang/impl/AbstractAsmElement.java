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


package org.netbeans.modules.cnd.asm.model.lang.impl;

import java.util.List;

import org.netbeans.modules.cnd.asm.model.lang.AsmElement;
import org.netbeans.modules.cnd.asm.model.lang.AsmOffsetable;

public abstract class AbstractAsmElement implements AsmElement {
    
    private int startOffset;
    private int endOffset;      
       
    abstract public List<AsmElement> getCompounds();
                
    public void setOffset(AsmOffsetable off) {
        startOffset = off.getStartOffset();
        endOffset = off.getEndOffset();
    }        
    
    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }
    
    public void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }
    
    public int getStartOffset() {
        return startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }       
}
