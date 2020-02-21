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
 * value of DW_AT_virtuality
 *
 */
public enum VIRTUALITY {
    DW_VIRTUALITY_none(0x0, "none"), // NOI18N
    DW_VIRTUALITY_virtual(0x1, "virtual"), // NOI18N
    DW_VIRTUALITY_pure_virtual(0x2, "pure_virtual"); // NOI18N

    private static final HashMap<Integer, VIRTUALITY> hashmap = new HashMap<Integer, VIRTUALITY>();
    private final int value;
    private final String name;

    static {
        for (VIRTUALITY elem : values()) {
            hashmap.put(elem.value, elem);
        }
    }

    VIRTUALITY(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static VIRTUALITY get(int val) {
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
