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

package org.netbeans.modules.cnd.asm.core.assistance;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.netbeans.modules.cnd.asm.model.lang.BitWidth;
import org.netbeans.modules.cnd.asm.model.lang.Register;

// Mock-object for test purposes 

class MockRegister implements Register {
    
    private static final List<MockRegister> regs;
    
    static  {
        regs = new LinkedList<MockRegister>();              
    }
        
    public static Register getRegister(int num) {
        for (MockRegister reg : regs) {
            if (reg.num == num) {
                return reg;
            }
        }
        
        MockRegister reg = new MockRegister(num); 
        regs.add(reg);
        
        return reg;
    }
    
    private static final List<Register> empty = Collections.<Register>emptyList();
    private int num;
        
    protected  MockRegister(int i) {
    }

    public List<Register> getChildren() {
        return empty;
    }

    public Register getDirectParent() {
        return null;
    }

    public String getName() {
        return "";
    }

    public BitWidth getBitWidth() {
        return BitWidth.BYTE;
    }

    public String getProperty(String prop) {
        return "";
    }    
}
