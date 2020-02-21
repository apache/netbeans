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
public enum LNE {
    DW_LNE_end_sequence(0x01),
    DW_LNE_set_address(0x02),
    DW_LNE_define_file(0x03),
    DW_LNE_lo_user(0x80),
    DW_LNE_hi_user(0xff),
    DW_LNE_UNDEFUNED(-1);

    private final int value;
    static private final HashMap<Integer, LNE> hashmap = new HashMap<Integer, LNE>();
    
    static {
        for (LNE elem : values()) {
            hashmap.put(elem.value, elem);
        }
    }
    
    LNE(int value) {
        this.value = value;
    }
    
    public static LNE get(int val) {
        LNE res = hashmap.get(val);
        if (res == null) {
            res = DW_LNE_UNDEFUNED;
        }
        return res;
    }
    
    public int value() {
        return value;
    }
}
