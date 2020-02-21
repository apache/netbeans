/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.simpleunit.utils;

import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;

/**
 */
public class CodeGenerationUtils {

    private CodeGenerationUtils() {
    }

    public static String generateFunctionDeclaration(CsmFunction fun) {
        String returnType = fun.getReturnType().getText().toString();
        StringBuilder functionDecl = new StringBuilder(""); // NOI18N
        functionDecl.append(returnType).append(" ") // NOI18N
                .append(fun.getQualifiedName()).append("("); // NOI18N
        Collection<CsmParameter> params = fun.getParameters();
        int i = 0;
        for (CsmParameter param : params) {
            if (i != 0) {
                functionDecl.append(", "); // NOI18N
            }
            functionDecl.append(param.getText().toString()); // NOI18N
            i++;
        }
        functionDecl.append(");"); // NOI18N
        return functionDecl.toString();
    }

    public static String generateParameterDeclaration(CsmParameter param, int paramNumber) {
        StringBuilder paramDecl = new StringBuilder(""); // NOI18N
        paramDecl.append(generateCanonicalParameterTypeAndName(param, paramNumber))
                .append(";"); // NOI18N
        return paramDecl.toString();
    }
    
    private static String generateCanonicalParameterTypeAndName(CsmParameter param, int paramNumber) {
        String paramName = param.getName().toString();
        if(paramName == null || paramName.isEmpty()) {
            paramName = "p" + paramNumber; // NOI18N
        }
        StringBuilder paramTypeAndName = new StringBuilder(""); // NOI18N
        String paramType = param.getType().getCanonicalText().toString();
        paramTypeAndName.append(paramType) // NOI18N
               .append(" ").append(paramName); // NOI18N
        return paramTypeAndName.toString();
    }

    public static String generateFunctionCall(CsmFunction fun) {
        StringBuilder functionCall = new StringBuilder(""); // NOI18N
        String returnType = fun.getReturnType().getText().toString();
        if (CsmKindUtilities.isMethod(fun)) {
            CsmMethod method = (CsmMethod) CsmBaseUtilities.getFunctionDeclaration(fun);
            CsmClass cls = method.getContainingClass();
            if (cls != null) {
                String clsName = cls.getName().toString();
                String clsVarName =
                        Character.toLowerCase(clsName.charAt(0))
                        + clsName.substring(1);
                clsVarName = (clsVarName.equals(clsName)) ? '_' + clsVarName : clsVarName; // NOI18N
                functionCall.append("    ") // NOI18N
                        .append(cls.getQualifiedName()) // NOI18N
                        .append(" ") // NOI18N
                        .append(clsVarName); // NOI18N
                if (!CsmKindUtilities.isConstructor(method)) {
                    functionCall.append(";\n"); // NOI18N
                    functionCall.append("    ") // NOI18N
                            .append(((!"void".equals(returnType)) ? returnType + " result = " : "")) // NOI18N
                            .append(clsVarName) // NOI18N
                            .append(".") // NOI18N
                            .append(method.getName()); // NOI18N
                }
            }
        } else {
            functionCall.append("    ").append(((!"void".equals(returnType)) ? returnType + " result = " : "")) // NOI18N
                    .append(fun.getName()); // NOI18N
        }
        int i = 0;
        functionCall.append("("); // NOI18N
        Collection<CsmParameter> params = fun.getParameters();
        for (CsmParameter param : params) {
            if (!param.isVarArgs()) {
                if (i != 0) {
                    functionCall.append(", "); // NOI18N
                }
                String paramName = param.getName().toString();
                functionCall.append(((paramName != null && !paramName.isEmpty()) ? paramName : "p" + i)); // NOI18N
                i++;
            }
        }
        functionCall.append(");\n"); // NOI18N
        return functionCall.toString();
    }
}
