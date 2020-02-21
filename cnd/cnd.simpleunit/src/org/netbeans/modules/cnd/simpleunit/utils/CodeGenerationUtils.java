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
