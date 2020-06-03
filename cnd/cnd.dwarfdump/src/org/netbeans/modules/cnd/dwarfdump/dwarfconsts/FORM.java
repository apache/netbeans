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
public enum FORM {
    DW_FORM_addr(0x01),
    DW_FORM_block2(0x03),
    DW_FORM_block4(0x04),
    DW_FORM_data2(0x05),
    DW_FORM_data4(0x06),
    DW_FORM_data8(0x07),
    DW_FORM_string(0x08),
    DW_FORM_block(0x09),
    DW_FORM_block1(0x0a),
    DW_FORM_data1(0x0b),
    DW_FORM_flag(0x0c),
    DW_FORM_sdata(0x0d),
    DW_FORM_strp(0x0e),
    DW_FORM_udata(0x0f),
    DW_FORM_ref_addr(0x10),
    DW_FORM_ref1(0x11),
    DW_FORM_ref2(0x12),
    DW_FORM_ref4(0x13),
    DW_FORM_ref8(0x14),
    DW_FORM_ref_udata(0x15),
    DW_FORM_indirect(0x16),
    /* DWARF 4.  */
    DW_FORM_sec_offset(0x17),
    DW_FORM_exprloc(0x18),
    DW_FORM_flag_present(0x19),
    DW_FORM_sig8(0x20);
    
    private final int value;
    private static final HashMap<Integer, FORM> hashmap = new HashMap<Integer, FORM>();
    
    static {
        for (FORM elem : values()) {
            hashmap.put(elem.value, elem);
        }
    }
    
    FORM(int value) {
        this.value = value;
    }
    
    public static FORM get(int val) {
        return hashmap.get(val);
    }
    
    
    public int value() {
        return value;
    }
}
