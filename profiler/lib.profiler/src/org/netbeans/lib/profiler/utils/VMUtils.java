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

package org.netbeans.lib.profiler.utils;


/**
 * Utilities for dealing with VM representation of stuff.
 *
 * @author Ian Formanek
 */
public class VMUtils {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    public static final char BOOLEAN = 'Z'; // NOI18N
    public static final char CHAR = 'C'; // NOI18N
    public static final char BYTE = 'B'; // NOI18N
    public static final char SHORT = 'S'; // NOI18N
    public static final char INT = 'I'; // NOI18N
    public static final char LONG = 'J'; // NOI18N
    public static final char FLOAT = 'F'; // NOI18N
    public static final char DOUBLE = 'D'; // NOI18N
    public static final char VOID = 'V'; // NOI18N
    public static final char REFERENCE = 'L'; // NOI18N

    public static final String BOOLEAN_CODE = "Z"; // NOI18N
    public static final String CHAR_CODE = "C"; // NOI18N
    public static final String BYTE_CODE = "B"; // NOI18N
    public static final String SHORT_CODE = "S"; // NOI18N
    public static final String INT_CODE = "I"; // NOI18N
    public static final String LONG_CODE = "J"; // NOI18N
    public static final String FLOAT_CODE = "F"; // NOI18N
    public static final String DOUBLE_CODE = "D"; // NOI18N
    public static final String VOID_CODE = "V"; // NOI18N
    
    public static final String BOOLEAN_STRING = "boolean"; // NOI18N
    public static final String CHAR_STRING = "char"; // NOI18N
    public static final String BYTE_STRING = "byte"; // NOI18N
    public static final String SHORT_STRING = "short"; // NOI18N
    public static final String INT_STRING = "int"; // NOI18N
    public static final String LONG_STRING = "long"; // NOI18N
    public static final String FLOAT_STRING = "float"; // NOI18N
    public static final String DOUBLE_STRING = "double"; // NOI18N
    public static final String VOID_STRING = "void"; // NOI18N

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static String typeToVMSignature(final String type) {
        //    System.err.println("sig for: "+type);
        String ret = type.replace('.', '/'); // NOI18N

        // 1. replace primitive types or surround class name
        if (ret.startsWith(BOOLEAN_STRING)) {
            ret = ret.replace(BOOLEAN_STRING, BOOLEAN_CODE);
        } else if (ret.startsWith(CHAR_STRING)) {
            ret = ret.replace(CHAR_STRING, CHAR_CODE);
        } else if (ret.startsWith(BYTE_STRING)) {
            ret = ret.replace(BYTE_STRING, BYTE_CODE);
        } else if (ret.startsWith(SHORT_STRING)) {
            ret = ret.replace(SHORT_STRING, SHORT_CODE);
        } else if (ret.startsWith(INT_STRING)) {
            ret = ret.replace(INT_STRING, INT_CODE);
        } else if (ret.startsWith(LONG_STRING)) {
            ret = ret.replace(LONG_STRING, LONG_CODE);
        } else if (ret.startsWith(FLOAT_STRING)) {
            ret = ret.replace(FLOAT_STRING, FLOAT_CODE);
        } else if (ret.startsWith(DOUBLE_STRING)) {
            ret = ret.replace(DOUBLE_STRING, DOUBLE_CODE);
        } else if (ret.startsWith(VOID_STRING)) {
            ret = ret.replace(VOID_STRING, VOID_CODE);
        } else {
            // if the remainder is a class, surround it with "L...;"
            final int arIdx = ret.indexOf('['); // NOI18N

            if (arIdx == -1) {
                ret = "L" + ret + ";"; // NOI18N
            } else {
                ret = "L" + ret.substring(0, arIdx) + ";" + ret.substring(arIdx); // NOI18N
            }
        }

        // 2. put all array marks to the beginning in the VM-signature style
        while (ret.endsWith("[]")) { // NOI18N
            ret = "[" + ret.substring(0, ret.length() - 2); // NOI18N
        }

        //    System.err.println("is: "+ret);
        return ret;
    }
    
    public static boolean isVMPrimitiveType(String className) {
        if (className == null || className.length() != 1) return false;
        if (VMUtils.BOOLEAN_CODE.equals(className) || VMUtils.CHAR_CODE.equals(className) || VMUtils.BYTE_CODE.equals(className) ||
            VMUtils.SHORT_CODE.equals(className) || VMUtils.INT_CODE.equals(className) || VMUtils.LONG_CODE.equals(className) ||
            VMUtils.FLOAT_CODE.equals(className) || VMUtils.DOUBLE_CODE.equals(className)) return true;
        return false;
    }
    
    public static boolean isPrimitiveType(String className) {
        if (className == null || className.length() < 1) return false;
        if (VMUtils.BOOLEAN_STRING.equals(className) || VMUtils.CHAR_STRING.equals(className) || VMUtils.BYTE_STRING.equals(className) ||
            VMUtils.SHORT_STRING.equals(className) || VMUtils.INT_STRING.equals(className) || VMUtils.LONG_STRING.equals(className) ||
            VMUtils.FLOAT_STRING.equals(className) || VMUtils.DOUBLE_STRING.equals(className)) return true;
        return false;
    }
}
