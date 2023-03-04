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
package org.netbeans.modules.php.editor.api;

import org.netbeans.modules.csl.api.ElementKind;

public enum PhpElementKind {

    INDEX, PROGRAM, INCLUDE,
    IFACE, CLASS, USE_ALIAS,
    METHOD, FIELD, TYPE_CONSTANT,
    VARIABLE, CONSTANT, FUNCTION,
    NAMESPACE_DECLARATION, USE_STATEMENT, GROUP_USE_STATEMENT, CONSTRUCTOR,
    TRAIT, TRAIT_CONFLICT_RESOLUTION, TRAIT_METHOD_ALIAS, EMPTY,
    ENUM, ENUM_CASE;

    public final ElementKind getElementKind() {
        ElementKind result;
        switch (this) {
            case CLASS:
                result = ElementKind.CLASS;
                break;
            case TYPE_CONSTANT:
                result = ElementKind.CONSTANT;
                break;
            case CONSTANT:
                result = ElementKind.CONSTANT;
                break;
            case FIELD:
                result = ElementKind.FIELD;
                break;
            case FUNCTION:
                result = ElementKind.METHOD;
                break;
            case IFACE:
                result = ElementKind.INTERFACE;
                break;
            case METHOD:
                result = ElementKind.METHOD;
                break;
            case VARIABLE:
                result = ElementKind.VARIABLE;
                break;
            case NAMESPACE_DECLARATION:
                result = ElementKind.PACKAGE;
                break;
            case ENUM_CASE:
                result = ElementKind.CONSTANT;
                break;
            default:
                result = ElementKind.OTHER;
        }
        return result;
    }
}
