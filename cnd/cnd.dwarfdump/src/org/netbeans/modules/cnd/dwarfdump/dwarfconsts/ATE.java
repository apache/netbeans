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

package org.netbeans.modules.cnd.dwarfdump.dwarfconsts;

import java.util.HashMap;

/**
 *
 */
public enum ATE {
    DW_ATE_address(0x1),
    DW_ATE_boolean(0x2),
    DW_ATE_complex_float(0x3),
    DW_ATE_float(0x4),
    DW_ATE_signed(0x5),
    DW_ATE_signed_char(0x6),
    DW_ATE_unsigned(0x7),
    DW_ATE_unsigned_char(0x8),
    DW_ATE_imaginary_float(0x9),
    DW_ATE_lo_user(0x80),
    DW_ATE_SUN_interval_float(0x91),
    DW_ATE_SUN_imaginary_float(0x92),
    DW_ATE_hi_user(0xff);
    
    private static final HashMap<Integer, ATE> hashmap = new HashMap<Integer, ATE>();
    private final int value;
    
    static {
        for (ATE elem : values()) {
            hashmap.put(elem.value, elem);
        }
    }
    
    ATE(int value) {
        this.value = value;
    }
    
    public static ATE get(int val) {
        return hashmap.get(val);
    }
    
    
    public int value() {
        return value;
        
    }
}
