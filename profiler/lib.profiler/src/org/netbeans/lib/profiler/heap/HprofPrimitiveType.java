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

package org.netbeans.lib.profiler.heap;

import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author Tomas Hurka
 */
class HprofPrimitiveType implements PrimitiveType {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final Map<Integer, Type> PRIMITIVE_TYPE_MAP;

    static {
        PRIMITIVE_TYPE_MAP = new HashMap<>(10);
        PRIMITIVE_TYPE_MAP.put(HprofHeap.BOOLEAN, new HprofPrimitiveType("boolean")); //NOI18N
        PRIMITIVE_TYPE_MAP.put(HprofHeap.CHAR, new HprofPrimitiveType("char")); //NOI18N
        PRIMITIVE_TYPE_MAP.put(HprofHeap.FLOAT, new HprofPrimitiveType("float")); //NOI18N
        PRIMITIVE_TYPE_MAP.put(HprofHeap.DOUBLE, new HprofPrimitiveType("double")); //NOI18N
        PRIMITIVE_TYPE_MAP.put(HprofHeap.BYTE, new HprofPrimitiveType("byte")); //NOI18N
        PRIMITIVE_TYPE_MAP.put(HprofHeap.SHORT, new HprofPrimitiveType("short")); //NOI18N
        PRIMITIVE_TYPE_MAP.put(HprofHeap.INT, new HprofPrimitiveType("int")); //NOI18N
        PRIMITIVE_TYPE_MAP.put(HprofHeap.LONG, new HprofPrimitiveType("long")); //NOI18N
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
        return (Type) PRIMITIVE_TYPE_MAP.get((int) type);
    }
}
