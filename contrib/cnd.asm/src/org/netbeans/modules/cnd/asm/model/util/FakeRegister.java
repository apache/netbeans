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

import java.util.List;

import org.netbeans.modules.cnd.asm.model.lang.BitWidth;
import org.netbeans.modules.cnd.asm.model.lang.Register;

public final class FakeRegister implements Register,
                                    AsmFakeable {
    
    private final BitWidth width;
    private final Register parent;
    
    public FakeRegister(BitWidth width, Register parent) {
        this.parent = parent;
        this.width = width;        
    }    
    
    public FakeRegister(BitWidth width) {
        this(width, null);
    }

    public BitWidth getBitWidth() {
        return width;
    }

    public List<Register> getChildren() {
        return  null;        
    }

    public Register getDirectParent() {
        return parent;
    }

    public String getName() {
        return "Fake Register"; // NOI18N
    }    

    public String getProperty(String prop) {
        return null;
    }
}
