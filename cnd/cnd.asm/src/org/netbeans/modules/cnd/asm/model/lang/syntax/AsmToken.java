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


package org.netbeans.modules.cnd.asm.model.lang.syntax;

import org.netbeans.modules.cnd.asm.model.lang.AsmOffsetable;

public class AsmToken implements AsmOffsetable { 
    
    private final AsmTokenId id;
    private final int start, end;
    private final String text;
    
    public AsmToken(AsmTokenId id, String text, int start, int end) {
        this.id = id;
        this.text = text;
        this.start = start;
        this.end = end;
    } 
    
    public AsmTokenId getId() {
        return id;        
    }

    public String getText() {
        return text;
    }

    public int getStartOffset() {
        return start;
    }

    public int getEndOffset() {
        return end;
    }               
}
