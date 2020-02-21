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

package org.netbeans.modules.cnd.api.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public interface CsmFunction extends CsmFunctional, CsmOffsetableDeclaration, CsmScope {
    
    public enum OperatorKind {
        COMMA(",", true), //NOI18N
        NOT("!", false), // NOI18N 
        NOT_EQ("!=", true), // NOI18N 
        MOD("%", true), // NOI18N 
        MOD_EQ("%=", true), // NOI18N 
        AND("&", true), // NOI18N 
        ADDRESS("&", false), // NOI18N
        AND_AND("&&", true), // NOI18N
        AND_EQ("&=", true), // NOI18N
        FUN_CALL("()", null), // NOI18N
        CAST("()", false), // NOI18N
        MUL("*", true), // NOI18N
        POINTER("*", false), // NOI18N
        MUL_EQ("*=", true), // NOI18N
        PLUS("+", true), // NOI18N
        PLUS_EQ("+=", true), // NOI18N
        PLUS_UNARY("+", false), // NOI18N
        PLUS_PLUS("++", false), // NOI18N
        MINUS("-", true), // NOI18N
        MINUS_EQ("-=", true), // NOI18N
        MINUS_UNARY("-", false), // NOI18N
        MINUS_MINUS("--", false), // NOI18N
        ARROW("->", true), // NOI18N
        ARROW_MBR("->*", true), // NOI18N
        DIV("/", true), // NOI18N
        DIV_EQ("/=", true), // NOI18N
        LESS("<", true), // NOI18N
        LEFT_SHIFT("<<", true), // NOI18N // often as serialize
        LEFT_SHIFT_EQ("<<=", true), // NOI18N
        LESS_EQ("<=", true), // NOI18N
        EQ("=", true), // NOI18N
        EQ_EQ("==", true), // NOI18N
        GREATER(">", true), // NOI18N
        GREATER_EQ(">=", true), // NOI18N
        RIGHT_SHIFT(">>", true), // NOI18N
        RIGHT_SHIFT_EQ(">>=", true), // NOI18N
        ARRAY("[]", null), // NOI18N
        XOR("^", true), // NOI18N
        XOR_EQ("^=", true), // NOI18N
        OR("|", true), // NOI18N
        OR_EQ("|=", true), // NOI18N
        OR_OR("||", true), // NOI18N
        TILDE("~", false), // NOI18N
        DELETE("delete", null), // NOI18N
        NEW("new", null), // NOI18N
        CONVERSION("", false), // NOI18N
        NONE("", null);
        
        private final String img;
        private final Boolean binary;
        private OperatorKind(String img, Boolean binary) {
            this.img = img;
            this.binary = binary;
        }
        
        public String getImage() {
            return img;
        }
        
        public Boolean isBinary() {
            return binary;
        }
        
        private static boolean inited = false;
        private static Map<String, OperatorKind> binaryMap = new HashMap<String, OperatorKind>();
        private static Map<String, OperatorKind> unaryMap = new HashMap<String, OperatorKind>();

        public static OperatorKind getKindByImage(String image, boolean binary) {
            boolean wasInited = inited;
            if (!wasInited) {
                synchronized (CsmFunction.class) {
                    if (!inited) {
                        for (OperatorKind kind : OperatorKind.values()) {
                            String img = kind.getImage();
                            if (img.length() > 0) {
                                if (kind.isBinary() != null && kind.isBinary()) {
                                    binaryMap.put(img, kind);
                                } else {
                                    unaryMap.put(img, kind);
                                }
                            }
                        }
                        inited = true;
                    }
                }
            }
            OperatorKind kind = binary ? binaryMap.get(image) : unaryMap.get(image);
            if (kind == null) {
                kind = NONE;
            }
            return kind;
        }
    }

    /** Gets this function's declaration text */
    CharSequence getDeclarationText();

    /**
     * Gets this function definition
     * TODO: describe getDefiition==this ...
     */
    CsmFunctionDefinition getDefinition();

    /** Returns this function declaration */
    CsmFunction getDeclaration();
    
    boolean isOperator();
    
    OperatorKind getOperatorKind();
    
    boolean isInline();

    boolean isStatic();

    CsmFunctionParameterList  getParameterList();
   
}
