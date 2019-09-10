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
package org.netbeans.modules.php.editor.model.impl;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class Type {

    private Type() {
    }

    public static final String STRING = "string"; //NOI18N
    public static final String REAL = "real"; //NOI18N
    public static final String INT = "int"; //NOI18N
    public static final String INTEGER = "integer"; //NOI18N
    public static final String BOOL = "bool"; //NOI18N
    public static final String BOOLEAN = "boolean"; //NOI18N
    public static final String ARRAY = "array"; //NOI18N
    public static final String NULL = "null"; //NOI18N
    public static final String FLOAT = "float"; //NOI18N
    public static final String DOUBLE = "double"; //NOI18N
    public static final String OBJECT = "object"; //NOI18N
    public static final String NUMBER = "number"; //NOI18N
    public static final String CALLBACK = "callback"; //NOI18N
    public static final String CALLABLE = "callable"; //NOI18N
    public static final String ITERABLE = "iterable"; //NOI18N
    public static final String RESOURCE = "resource"; //NOI18N
    public static final String VOID = "void"; //NOI18N
    public static final String MIXED = "mixed"; //NOI18N
    public static final String SELF = "self"; //NOI18N
    public static final String PARENT = "parent"; //NOI18N

    private static final List<String> TYPES_FOR_EDITOR = Arrays.asList(ARRAY, CALLABLE, ITERABLE, BOOL, FLOAT, INT, STRING, OBJECT);
    private static final List<String> TYPES_FOR_RETURN_TYPE = Arrays.asList(ARRAY, CALLABLE, ITERABLE, BOOL, FLOAT, INT, STRING, VOID, OBJECT);
    private static final List<String> TYPES_FOR_FIELD_TYPE = Arrays.asList(ARRAY, ITERABLE, BOOL, FLOAT, INT, STRING, OBJECT, SELF, PARENT); // PHP 7.4 Typed Properties 2.0
    private static final List<String> TYPES_FOR_PHP_DOC = Arrays.asList(STRING, INTEGER, INT, BOOLEAN, BOOL, FLOAT, DOUBLE, OBJECT, MIXED, ARRAY,
            RESOURCE, VOID, NULL, CALLBACK, CALLABLE, ITERABLE, "false", "true", "self"); // NOI18N


    public static boolean isPrimitive(String typeName) {
        boolean retval = false;
        if (BOOL.equals(typeName) || BOOLEAN.equals(typeName) || INT.equals(typeName)
                || INTEGER.equals(typeName) || FLOAT.equals(typeName) || REAL.equals(typeName)
                || ARRAY.equals(typeName) || OBJECT.equals(typeName) || MIXED.equals(typeName)
                || NUMBER.equals(typeName) || CALLBACK.equals(typeName) || RESOURCE.equals(typeName)
                || DOUBLE.equals(typeName) || STRING.equals(typeName) || NULL.equals(typeName)
                || VOID.equals(typeName) || CALLABLE.equals(typeName) || ITERABLE.equals(typeName)) {
            retval = true;
        }
        return retval;
    }

    public static boolean isArray(String typeName) {
        boolean result = false;
        if (ARRAY.equals(typeName) || (typeName != null && typeName.contains("[") && typeName.contains("]"))) { //NOI18N
            result = true;
        }
        return result;
    }

    /**
     * Check whether propety type is invalid ({@code null}, {@code void}).
     *
     * @param typeName type name
     * @return {@code true} if type is invalid, otherwise {@code false}
     */
    public static boolean isInvalidPropertyType(String typeName) {
        return VOID.equals(typeName)
                || NULL.equals(typeName);
    }

    /**
     * Get valid types for the "editor". It means all the types
     * that are valid to be used in source code (like "int" for PHP 7 etc.).
     * <p>
     * This method will be changed in the future (PHP 7.1 will have "void"
     * as a return type but it will not be a valid scalar type).
     * @return valid types for the "editor"
     */
    public static List<String> getTypesForEditor() {
        return TYPES_FOR_EDITOR;
    }

    /**
     * Get valid types for the Return Type. This contains "void".
     *
     * @return valid types for the Return Type
     */
    public static List<String> getTypesForReturnType() {
        return TYPES_FOR_RETURN_TYPE;
    }

    /**
     * Get valid types for the field type. This does not contain "void" and "callable".
     *
     * @return valid types for the field type
     */
    public static List<String> getTypesForFieldType() {
        return TYPES_FOR_FIELD_TYPE;
    }

    public static List<String> getTypesForPhpDoc() {
        return TYPES_FOR_PHP_DOC;
    }

}
