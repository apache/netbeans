/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
