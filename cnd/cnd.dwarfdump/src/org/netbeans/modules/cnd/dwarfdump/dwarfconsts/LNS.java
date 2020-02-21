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
public enum LNS {
    DW_LNS_extended_op(0x00),
    DW_LNS_copy(0x01),
    DW_LNS_advance_pc(0x02),
    DW_LNS_advance_line(0x03),
    DW_LNS_set_file(0x04),
    DW_LNS_set_column(0x05),
    DW_LNS_negate_stmt(0x06),
    DW_LNS_set_basic_block(0x07),
    DW_LNS_const_add_pc(0x08),
    DW_LNS_fixed_advance_pc(0x09),
    DW_LNS_set_prologue_end(0x0a),
    DW_LNS_set_epilogue_begin(0x0b),
    DW_LNS_set_isa(0x0c);
    
    private final int value;
    private static final HashMap<Integer, LNS> hashmap = new HashMap<Integer, LNS>();
    
    static {
        for (LNS elem : values()) {
            hashmap.put(elem.value, elem);
        }
    }
    
    LNS(int value) {
        this.value = value;
    }
    
    public static LNS get(int val) {
        return hashmap.get(val);
    }
    
    public int value() {
        return value;
    }
}

