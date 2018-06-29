/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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

    private static final List<String> TYPES_FOR_EDITOR = Arrays.asList(ARRAY, CALLABLE, ITERABLE, BOOL, FLOAT, INT, STRING, OBJECT);
    private static final List<String> TYPES_FOR_RETURN_TYPE = Arrays.asList(ARRAY, CALLABLE, ITERABLE, BOOL, FLOAT, INT, STRING, VOID, OBJECT);
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

    public static List<String> getTypesForPhpDoc() {
        return TYPES_FOR_PHP_DOC;
    }

}
