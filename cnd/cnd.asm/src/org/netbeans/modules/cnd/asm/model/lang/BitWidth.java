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


package org.netbeans.modules.cnd.asm.model.lang;

import java.util.Map;
import java.util.WeakHashMap;
 
public class BitWidth {
    private final int width;
    
    public final static BitWidth BIT; 
    public final static BitWidth BYTE; 
    public final static BitWidth WORD; 
    public final static BitWidth DWORD; 
    public final static BitWidth QWORD;    
    
    private final static Map<BitWidth, Boolean> widths;
    
    static 
    {
        widths = new WeakHashMap<BitWidth, Boolean>();
        
        BIT = getBitWidth(1);
        BYTE = getBitWidth(8);
        WORD = getBitWidth(16);
        DWORD = getBitWidth(32);
        QWORD = getBitWidth(64);                                
    }
    
    private BitWidth(int width) {
        this.width = width; 
    }
    
    public int getWidth() {
        return width;
    }
       
    public static BitWidth getBitWidth(int width) {
        for (BitWidth el : widths.keySet()) {
            if (el.getWidth() == width) {
                return el;
            }
        }
        
        BitWidth res = new BitWidth(width);
        widths.put(res, true);
        return res;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BitWidth)) {
            return false;
        }
        
        return width == ((BitWidth) o).getWidth();
    }
        
    @Override
    public int hashCode() {
        return width;
    }
}

