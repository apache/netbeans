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
public enum LANG {
    DW_LANG_C89(0x0001, "C 89"), // NOI18N
    DW_LANG_C(0x0002, "C"), // NOI18N
    DW_LANG_Ada83(0x0003, "Ada 83"), // NOI18N
    DW_LANG_C_plus_plus(0x0004, "C++"), // NOI18N
    DW_LANG_Cobol74(0x0005, "Cobol 74"), // NOI18N
    DW_LANG_Cobol85(0x0006, "Cobol 85"), // NOI18N
    DW_LANG_Fortran77(0x0007, "Fortran 77"), // NOI18N
    DW_LANG_Fortran90(0x0008, "Fortran 90"), // NOI18N
    DW_LANG_Pascal83(0x0009, "Pascal 83"), // NOI18N
    DW_LANG_Modula2(0x000a, "Modula 2"), // NOI18N
    DW_LANG_Java(0x000b, "Java"), // NOI18N
    DW_LANG_C99(0x000c, "C 99"), // NOI18N
    DW_LANG_Ada95(0x000d, "Ada"), // NOI18N
    DW_LANG_Fortran95(0x000e, "Fortran 95"), // NOI18N
    DW_LANG_PLI(0x000f, "PL/I"), // NOI18N
    DW_LANG_ObjC(0x0010, "Objective-C"), // NOI18N
    DW_LANG_ObjC_plus_plus(0x0011, "Objective-C++"), // NOI18N
    DW_LANG_UPC(0x0012, "Unified Parallel C"), // NOI18N
    DW_LANG_D(0x0013, "D"), // NOI18N
    DW_LANG_lo_user(0x8000, null), // NOI18N
    DW_LANG_SUN_Assembler(0x9001, "Assembler"), // NOI18N
    DW_LANG_hi_user(0xffff, null); // NOI18N
    
/* What about dwarf 3.0 ?
    See http://dwarf.freestandards.org/Dwarf3Std.php
*/    
    
    private final int value;
    private final String name;
    static private final HashMap<Integer, LANG> hashmap = new HashMap<Integer, LANG>();
    
    static {
        for (LANG elem : values()) {
            hashmap.put(elem.value, elem);
        }
    }
    
    LANG(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static LANG get(int val) {
        return hashmap.get(val);
    }
    
    public int value() {
        return value;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
