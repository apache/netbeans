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
 * Symbol table types (st_info)
 *
 */
public enum STT {
    STT_NOTYPE(0, "NOTYPE"), /* Type not specified */ // NOI18N
    STT_OBJECT(1, "OBJECT"), /* Associated with a data object */ // NOI18N
    STT_FUNC(2, "FUNC"), /* Associated with a function */ // NOI18N
    STT_SECTION(3, "SECTION"), /* Associated with a section */ // NOI18N
    STT_FILE(4, "FILE"), /* Associated with a file name */ // NOI18N
    STT_COMMON(5, "COMMON"), /* Uninitialised common block */ // NOI18N
    STT_TLS(6, "TLS"), /* Thread local data object */ // NOI18N
    STT_NUM(7, "NUM"), // NOI18N
    STT_LOOS(10, "LOOS"), /* Operating system specific range */ // NOI18N
    STT_HIOS(12, "HIOS"), // NOI18N
    STT_LOPROC(13, "LOPROC"), /* Processor-specific range */// NOI18N
    STT_HIPROC(15, "HIPROC"); // NOI18N
    
    private static final HashMap<Integer, STT> hashmap = new HashMap<Integer, STT>();
    private final int value;
    private final String name;
    
    static {
        for (STT elem : values()) {
            hashmap.put(elem.value, elem);
        }
    }
    
    STT(int value, String name) {
        this.value = value;
        this.name = name;
    }
    
    public static STT get(int val) {
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
