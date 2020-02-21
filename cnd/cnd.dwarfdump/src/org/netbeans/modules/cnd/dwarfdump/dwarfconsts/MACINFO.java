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
public enum MACINFO {
    DW_MACINFO_define(0x1),
    DW_MACINFO_undef(0x2),
    DW_MACINFO_start_file(0x3),
    DW_MACINFO_end_file(0x4),
    DW_MACRO_define_indirect(0x05), // Extension for .debug_macro section
    DW_MACRO_undef_indirect(0x06), // Extension for .debug_macro section
    DW_MACRO_transparent_include(0x07), // Extension for .debug_macro section
    // dwarf4
    DW_MACRO_define_indirect_alt(0x08),
    DW_MACRO_undef_indirect_alt(0x09),
    DW_MACRO_transparent_include_alt(0x0a),
    
    DW_MACINFO_vendor_ext(0xff);

    private static final HashMap<Integer, MACINFO> hashmap = new HashMap<Integer, MACINFO>();
    private final int value;
    
    static {
        for (MACINFO elem : values()) {
            hashmap.put(elem.value, elem);
        }
    }

    MACINFO(int value) {
        this.value = value;
    }
    
    public static MACINFO get(int val) {
        return hashmap.get(val);
    }
    
    
    public int value() {
        return value;
    }
}
