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

package org.netbeans.modules.cnd.refactoring.support;

import org.netbeans.modules.cnd.api.model.CsmField;

/**
 * utilities to create declarations
 */
public final class DeclarationGenerator {

    public static final String INLINE_PROPERTY = "inline_method"; // NOI18N
    public static final String INSERT_CODE_INLINE_PROPERTY = "insert_code_inline_method"; // NOI18N

    public enum Kind {
        INLINE_DEFINITION,
        INLINE_DEFINITION_MAKRED_INLINE, // definition with "inline" keyword
        DECLARATION,
        EXTERNAL_DEFINITION
    }
    
    private DeclarationGenerator() {
    }

    public static String createGetter(CsmField field, String gName, Kind kind) {
        StringBuilder out = new StringBuilder();
        // type information is the first
        if (field.isStatic()) {
            out.append("static "); //NOI18N
        }
        if (kind == Kind.INLINE_DEFINITION_MAKRED_INLINE) {
            out.append("inline "); //NOI18N
        }
        out.append(field.getType().getText()).append(" "); //NOI18N
        // add name
        if (kind == Kind.EXTERNAL_DEFINITION) {
            // external definition needs class prefix
            out.append(field.getContainingClass().getName()).append("::"); // NOI18N
        }
        out.append(gName).append("() "); // NOI18N
        if (!field.isStatic()) {
            out.append("const "); // NOI18N
        }
        if (kind == Kind.DECLARATION) {
            out.append(";"); //NOI18N
        } else {
            out.append("{ ").append("return ").append(field.getName()).append(";}"); // NOI18N
        }
        return out.toString();
    }

    public static String createSetter(CsmField field, String sName, Kind kind) {
        StringBuilder out = new StringBuilder();
        CharSequence fldName = field.getName();
        String paramName = GeneratorUtils.stripFieldPrefix(fldName.toString());
        CharSequence clsName = field.getContainingClass().getName();
        out.append("\n"); // NOI18N
        // type information is the first
        if (field.isStatic()) {
            out.append("static "); //NOI18N
        }
        if (kind == Kind.INLINE_DEFINITION_MAKRED_INLINE) {
            out.append("inline "); //NOI18N
        }
        out.append("void "); //NOI18N
        // add name
        if (kind == Kind.EXTERNAL_DEFINITION) {
            // external definition needs class prefix
            out.append(clsName).append("::"); // NOI18N
        }
        out.append(sName).append("("); // NOI18N
        // add parameter
        out.append(field.getType().getText());
        out.append(" ").append(paramName);// NOI18N
        out.append(")"); // NOI18N
        if (kind == Kind.DECLARATION) {
            out.append(";"); //NOI18N
        } else {
            out.append("{ ");// NOI18N
            // check for name collisions
            if (paramName.contentEquals(fldName)) {
                if (field.isStatic()) {
                    out.append(clsName).append("::"); // NOI18N
                } else {
                    out.append("this->");// NOI18N
                }
            }
            out.append(fldName).append("=").append(paramName).append(";}"); // NOI18N
        }
        return out.toString();
    }

}
