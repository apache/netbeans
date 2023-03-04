/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.lib.profiler.heap;

import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author Tomas Hurka
 */
class HprofPrimitiveType implements PrimitiveType {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static Map primitiveTypeMap;

    static {
        primitiveTypeMap = new HashMap(10);
        primitiveTypeMap.put(Integer.valueOf(HprofHeap.BOOLEAN), new HprofPrimitiveType("boolean")); //NOI18N
        primitiveTypeMap.put(Integer.valueOf(HprofHeap.CHAR), new HprofPrimitiveType("char")); //NOI18N
        primitiveTypeMap.put(Integer.valueOf(HprofHeap.FLOAT), new HprofPrimitiveType("float")); //NOI18N
        primitiveTypeMap.put(Integer.valueOf(HprofHeap.DOUBLE), new HprofPrimitiveType("double")); //NOI18N
        primitiveTypeMap.put(Integer.valueOf(HprofHeap.BYTE), new HprofPrimitiveType("byte")); //NOI18N
        primitiveTypeMap.put(Integer.valueOf(HprofHeap.SHORT), new HprofPrimitiveType("short")); //NOI18N
        primitiveTypeMap.put(Integer.valueOf(HprofHeap.INT), new HprofPrimitiveType("int")); //NOI18N
        primitiveTypeMap.put(Integer.valueOf(HprofHeap.LONG), new HprofPrimitiveType("long")); //NOI18N
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private String typeName;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    private HprofPrimitiveType(String name) {
        typeName = name;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public String getName() {
        return typeName;
    }

    static Type getType(byte type) {
        return (Type) primitiveTypeMap.get(Integer.valueOf(type));
    }
}
