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
package org.netbeans.api.java.source;

import java.util.Map;

/**
 * Debug helper to display the modifier flags of JCTrees.
 *
 * The bit patterns were copied from {@code com.sun.tools.javac.code.Flags.}
 * Duplicate values are commented out of best effort basis.
 *
 * @author homberghp
 */
public class FlagsMap {

    public static final Map<Long, String> map = Map.ofEntries(
            Map.entry(1L, "PUBLIC"),
            Map.entry(1L << 1, "PRIVATE"),
            Map.entry(1L << 2, "PROTECTED"),
            Map.entry(1L << 3, "STATIC"),
            Map.entry(1L << 4, "FINAL"),
            Map.entry(1L << 5, "SYNCHRONIZED"),
            Map.entry(1L << 6, "VOLATILE"),
            Map.entry(1L << 7, "TRANSIENT"),
            Map.entry(1L << 8, "NATIVE"),
            Map.entry(1L << 9, "INTERFACE"),
            Map.entry(1L << 10, "ABSTRACT"),
            Map.entry(1L << 11, "STRICTFP"),
            Map.entry(1L << 12, "SYNTHETIC"),
            Map.entry(1L << 13, "ANNOTATION"),
            Map.entry(1L << 14, "ENUM"),
            Map.entry(1L << 15, "MANDATED"),
//            Map.entry(0x0fffL, "StandardFlags"),
//            Map.entry(0x0020L, "ACC_SUPER"),
//            Map.entry(0x0040L, "ACC_BRIDGE"),
//            Map.entry(0x0080L, "ACC_VARARGS"),
//            Map.entry(0x8000L, "ACC_MODULE"),
            Map.entry(1L << 17, "DEPRECATED"),
            Map.entry(1L << 18, "HASINIT"),
            Map.entry(1L << 20, "BLOCK"),
            Map.entry(1L << 22, "NOOUTERTHIS"),
            Map.entry(1L << 23, "EXISTS"),
//            Map.entry(1L << 24, "COMPOUND"),
            Map.entry(1L << 25, "CLASS_SEEN"),
            Map.entry(1L << 26, "SOURCE_SEEN"),
            Map.entry(1L << 27, "LOCKED"),
            Map.entry(1L << 28, "UNATTRIBUTED"),
            Map.entry(1L << 29, "ANONCONSTR"),
//            Map.entry(1L << 29, "SUPER_OWNER_ATTRIBUTED"),
            Map.entry(1L << 30, "ACYCLIC"),
            Map.entry(1L << 31, "BRIDGE"),
            Map.entry(1L << 33, "PARAMETER"),
            Map.entry(1L << 34, "VARARGS"),
            Map.entry(1L << 35, "ACYCLIC_ANN"),
            Map.entry(1L << 36, "GENERATEDCONSTR"),
            Map.entry(1L << 37, "HYPOTHETICAL"),
            Map.entry(1L << 38, "PROPRIETARY"),
            Map.entry(1L << 39, "UNION"),
            Map.entry(1L << 40, "RECOVERABLE"),
            Map.entry(1L << 41, "EFFECTIVELY_FINAL"),
            Map.entry(1L << 42, "CLASH"),
            Map.entry(1L << 43, "DEFAULT"),
            Map.entry(1L << 44, "AUXILIARY"),
            Map.entry(1L << 45, "NOT_IN_PROFILE"),
//            Map.entry(1L << 45, "BAD_OVERRIDE"),
            Map.entry(1L << 46, "SIGNATURE_POLYMORPHIC"),
            Map.entry(1L << 47, "THROWS"),
            Map.entry(1L << 48, "POTENTIALLY_AMBIGUOUS"),
            Map.entry(1L << 49, "LAMBDA_METHOD"),
            Map.entry(1L << 50, "TYPE_TRANSLATED"),
//            Map.entry(1L << 51, "MODULE"),
            Map.entry(1L << 52, "AUTOMATIC_MODULE"),
//            Map.entry(1L << 52, "HAS_RESOURCE"),
//            Map.entry(1L << 52, "NAME_FILLED"),
//            Map.entry(1L << 53, "SYSTEM_MODULE"),
            Map.entry(1L << 53, "VALUE_BASED"),
            Map.entry(1L << 54, "DEPRECATED_ANNOTATION"),
            Map.entry(1L << 55, "DEPRECATED_REMOVAL"),
            Map.entry(1L << 56, "PREVIEW_API"),
            Map.entry(1L << 57, "ANONCONSTR_BASED"),
//            Map.entry(1L << 17, "BODY_ONLY_FINALIZE"),
            Map.entry(1L << 58, "PREVIEW_REFLECTIVE"),
            Map.entry(1L << 59, "MATCH_BINDING"),
            Map.entry(1L << 60, "MATCH_BINDING_TO_OUTER"),
            Map.entry(1L << 61, "RECORD"),
            Map.entry(1L << 51, "COMPACT_RECORD_CONSTRUCTOR"),
            Map.entry(1L << 24, "GENERATED_MEMBER"),
            Map.entry(1L << 62, "SEALED"),
            Map.entry(1L << 63, "NON_SEALED")
    );


    public static String toString(long flags){
        StringBuilder sb = new StringBuilder();
        int shift =0;
        long mask=  1L << shift;
        while (flags > 0L){
            long key=flags &mask;
            if (key !=0) {
                sb.append(map.get(key)+" ");
            }
            flags &= ~(mask);
            shift++;
            mask=  1L << shift;
        }
        return sb.toString().trim();
    }

    public static void main(String[] args) {
        String result =  toString(12L);
        System.out.println("result = " + result);
    }
}
