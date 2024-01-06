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
import java.util.Collection;
import java.util.List;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.CodeUtils;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class Type {

    public enum Kind {
        NONE(""), // NOI18N
        NORMAL(""), // NOI18N e.g. MyClass
        NULLABLE("?"), // NOI18N e.g. ?Nullable
        UNION(SEPARATOR), // e.g. A|B|C
        INTERSECTION(SEPARATOR_INTERSECTION), // e.g. A&B&C
        DNF(""), // NOI18N e.g. (A&B)|(X&Y)|Z
        ;

        private final String sign;

        private Kind(String sing) {
            this.sign = sing;
        }

        public String getSign() {
            return sign;
        }

        public static Kind fromTypes(@NullAllowed String types) {
            Kind kind = NORMAL;
            if (StringUtils.isEmpty(types)) {
                kind = NONE;
            } else if (types.contains(SEPARATOR) && types.contains(SEPARATOR_INTERSECTION)) {
                kind = DNF;
            } else if (types.contains(SEPARATOR)) {
                kind = UNION;
            } else if (types.contains(SEPARATOR_INTERSECTION)) {
                kind = INTERSECTION;
            } else if (types.contains(NULLABLE.getSign())) {
                kind = NULLABLE;
            }
            return kind;
        }

    }

    private Type() {
    }

    public static final String SEPARATOR_INTERSECTION = "&"; // NOI18N
    public static final String SEPARATOR = "|"; // NOI18N
    public static final String STRING = "string"; //NOI18N
    public static final String REAL = "real"; //NOI18N
    public static final String INT = "int"; //NOI18N
    public static final String INTEGER = "integer"; //NOI18N
    public static final String BOOL = "bool"; //NOI18N
    public static final String BOOLEAN = "boolean"; //NOI18N
    public static final String TRUE = "true"; //NOI18N
    public static final String FALSE = "false"; //NOI18N
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
    public static final String STATIC = "static"; //NOI18N NETBEANS-4443 PHP 8.0
    public static final String NEVER = "never"; //NOI18N NETBEANS-5599 PHP 8.1

    private static final List<String> TYPES_FOR_EDITOR = Arrays.asList(ARRAY, CALLABLE, ITERABLE, BOOL, FLOAT, INT, STRING, OBJECT, NULL, FALSE, MIXED, TRUE);
    private static final List<String> TYPES_FOR_RETURN_TYPE = Arrays.asList(ARRAY, CALLABLE, ITERABLE, BOOL, FLOAT, INT, STRING, VOID, OBJECT, NULL, FALSE, MIXED, NEVER, TRUE);
    private static final List<String> TYPES_FOR_FIELD_TYPE = Arrays.asList(ARRAY, ITERABLE, BOOL, FLOAT, INT, STRING, OBJECT, SELF, PARENT, NULL, FALSE, MIXED, TRUE); // PHP 7.4 Typed Properties 2.0
    private static final List<String> TYPES_FOR_CONST_TYPE = Arrays.asList(ARRAY, ITERABLE, BOOL, FLOAT, INT, STRING, OBJECT, STATIC, SELF, PARENT, NULL, FALSE, MIXED, TRUE); // PHP 8.3 Typed class constants
    private static final List<String> SPECIAL_TYPES_FOR_TYPE = Arrays.asList(SELF, PARENT);
    private static final List<String> TYPES_FOR_PHP_DOC = Arrays.asList(STRING, INTEGER, INT, BOOLEAN, BOOL, FLOAT, DOUBLE, OBJECT, MIXED, ARRAY,
            RESOURCE, VOID, NULL, CALLBACK, CALLABLE, ITERABLE, FALSE, TRUE, SELF);
    private static final List<String> MIXED_TYPE = Arrays.asList(ARRAY, BOOL, CALLABLE, INT, FLOAT, NULL, OBJECT, /*RESOURCE, */STRING);
    private static final List<String> TYPES_FOR_BACKING_TYPE = Arrays.asList(INT, STRING);

    public static boolean isPrimitive(String typeName) {
        boolean retval = false;
        if (BOOL.equals(typeName) || INT.equals(typeName)
                || INTEGER.equals(typeName) || FLOAT.equals(typeName) || REAL.equals(typeName)
                || ARRAY.equals(typeName) || OBJECT.equals(typeName) || MIXED.equals(typeName)
                || NUMBER.equals(typeName) || CALLBACK.equals(typeName) || RESOURCE.equals(typeName)
                || DOUBLE.equals(typeName) || STRING.equals(typeName) || NULL.equals(typeName)
                || VOID.equals(typeName) || CALLABLE.equals(typeName) || ITERABLE.equals(typeName)
                || FALSE.equals(typeName) || STATIC.equals(typeName) || NEVER.equals(typeName)
                || TRUE.equals(typeName)) {
            retval = true;
        }
        return retval;
    }

    public static boolean isPrimitiveAlias(String typeName) {
        boolean retval = false;
        if (BOOLEAN.equals(typeName)) {
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
                || NULL.equals(typeName)
                || STATIC.equals(typeName)
                || NEVER.equals(typeName);
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
     * Get special types for the inside of the type. They are "self" and
     * "parent".
     *
     * @return special types for the inside of the type
     */
    public static List<String> getSpecialTypesForType() {
        return SPECIAL_TYPES_FOR_TYPE;
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

    /**
     * Get valid types for the const type. This does not contain "void", "callable", and "never".
     *
     * @return valid types for the const type
     */
    public static List<String> getTypesForConstType() {
        return TYPES_FOR_CONST_TYPE;
    }

    public static List<String> getTypesForPhpDoc() {
        return TYPES_FOR_PHP_DOC;
    }

    /**
     * Get valid types for the backing type. "int" and "string" are available.
     *
     * @return valid types for the backing type
     */
    public static List<String> getTypesForBackingType() {
        return TYPES_FOR_BACKING_TYPE;
    }

    /**
     * Create types separated by "|". e.g. int|folat|NamespaceName
     *
     * @param types types
     * @return types separated by "|"
     */
    public static String asUnionType(Collection<String> types) {
        return StringUtils.implode(types, SEPARATOR);
    }

    /**
     * Create types separated by "&". e.g. T1&T2&T3
     *
     * @param types types
     * @return types separated by "&"
     */
    public static String asIntersectionType(Collection<String> types) {
        return StringUtils.implode(types, SEPARATOR_INTERSECTION);
    }

    public static List<String> getMixedType() {
        return MIXED_TYPE;
    }

    /**
     * Get the type separator.
     *
     * @param isIntersection
     * @return "&" if it's intersection type, otherwise "|"
     */
    public static String getTypeSeparator(boolean isIntersection) {
        return isIntersection ? SEPARATOR_INTERSECTION : SEPARATOR;
    }

    /**
     * Get all types from the declared type.
     *
     * @param declaredType the declared type. can be {@code null} e.g. (X&Y)|Z
     * @return all type names, if it's a nullable type, the type name with nullable type prefix("?")
     */
    public static String[] splitTypes(@NullAllowed String declaredType) {
        if (!StringUtils.hasText(declaredType)) {
            return new String[0];
        }
        String type = declaredType.trim();
        if (type.startsWith("(")) { // NOI18N
            type = type.substring(1);
        }
        if (type.endsWith(")")) { // NOI18N
            type = type.substring(0, type.length() - 1);
        }
        return CodeUtils.SPLIT_TYPES_PATTERN.split(type.replace(" ", "")); // NOI18N
    }

    /**
     * Convert the type declaration to the type template. e.g.
     * <pre>
     * - Type1|Type2|Type3 -> %s|%s|%s
     * - Type1&Type2&Type3 -> %s&%s&%s
     * - (Type1&Type2)|Type3 -> (%s&%s)|%s
     * - ?Type1 -> ?%s
     * </pre>
     *
     * @param typeDeclaration the type declaration (e.g.
     * {@code (X&Y)|Z, X|Y|Z, ?Nullable})
     * @return the type template (e.g. {@code (%s&%s)|%s, %s|%s|%s, ?%s})
     */
    public static String toTypeTemplate(String typeDeclaration) {
        return CodeUtils.TYPE_NAMES_IN_TYPE_DECLARATION_PATTERN.matcher(typeDeclaration.replace(" ", "")).replaceAll("%s"); // NOI18N
    }

    /**
     * Check whether a declaration type is a union type.
     *
     * @param typeDeclaration a type declaration
     * @return {@code true} if it's a union type, {@code false} otherwise
     */
    public static boolean isUnionType(@NullAllowed String typeDeclaration) {
        return typeDeclaration != null
                && typeDeclaration.contains(SEPARATOR);
    }

    /**
     * Check whether a declaration type is an intersection type.
     *
     * @param typeDeclaration a type declaration
     * @return {@code true} if it's an intersection type, {@code false}
     * otherwise
     */
    public static boolean isIntersectionType(@NullAllowed String typeDeclaration) {
        return typeDeclaration != null
                && typeDeclaration.contains(SEPARATOR_INTERSECTION);
    }

    /**
     * Check whether a declaration type is a DNF type.
     *
     * @param typeDeclaration a type declaration
     * @return {@code true} if it's a DNF type, {@code false} otherwise
     */
    public static boolean isDNFType(@NullAllowed String typeDeclaration) {
        return isUnionType(typeDeclaration) && isIntersectionType(typeDeclaration);
    }
}
