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
package org.netbeans.modules.cnd.cncppunit.codegeneration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.services.CsmIncludeResolver;
import org.netbeans.modules.cnd.simpleunit.utils.CodeGenerationUtils;
import org.netbeans.modules.cnd.utils.FSPath;

/**
 */
public class CUnitCodeGenerator {

    private CUnitCodeGenerator() {
    }

    public static Map<String, Object> generateTemplateParamsForFunctions(String testName, FSPath testFilePath, List<CsmFunction> functions) {
        Map<String, Object> templateParams = new HashMap<String, Object>();

        if (functions != null) {
            StringBuilder testFunctions = new StringBuilder(""); // NOI18N
            StringBuilder testCalls = new StringBuilder(""); // NOI18N
            StringBuilder testIncludes = new StringBuilder(""); // NOI18N

            testCalls.append("if ( "); // NOI18N

            List<String> testFunctionsNames = new ArrayList<String>();
            List<String> addedTestIncludes = new ArrayList<String>();

            int functionNumber = 0;
            for (CsmFunction fun : functions) {

                CsmIncludeResolver inclResolver = CsmIncludeResolver.getDefault();
                String include = inclResolver.getLocalIncludeDerectiveByFilePath(testFilePath, fun);
                if(!include.isEmpty()) {
                    if(!addedTestIncludes.contains(include)) {
                        testIncludes.append(include);
                        testIncludes.append("\n"); // NOI18N
                    }
                    addedTestIncludes.add(include);
                } else {
                    testFunctions.append(CodeGenerationUtils.generateFunctionDeclaration(fun));
                    testFunctions.append("\n\n"); // NOI18N
                }

                String funName = fun.getName().toString();
                String testFunctionName = "test" + // NOI18N
                        Character.toUpperCase(funName.charAt(0))
                        + funName.substring(1);
                if(testFunctionsNames.contains(testFunctionName)) {
                    int i = 0;
                    while(testFunctionsNames.contains(testFunctionName + i)) {
                        i++;
                    }
                    testFunctionName = testFunctionName + i;
                }
                testFunctionsNames.add(testFunctionName);
                testFunctions.append("void ") // NOI18N
                        .append(testFunctionName) // NOI18N
                        .append("() {\n"); // NOI18N
                Collection<CsmParameter> params = fun.getParameters();
                int i = 2;
                for (CsmParameter param : params) {
                    if (!param.isVarArgs()) {
                        testFunctions.append("    "); // NOI18N
                        testFunctions.append(CodeGenerationUtils.generateParameterDeclaration(param, i));
                        testFunctions.append("\n"); // NOI18N
                        i++;
                    }
                }

                testFunctions.append(CodeGenerationUtils.generateFunctionCall(fun));

                testFunctions.append("    if(1 /*check result*/) {\n"); // NOI18N
                testFunctions.append("        CU_ASSERT(0);"); // NOI18N
                testFunctions.append("    }\n"); // NOI18N
                testFunctions.append("}\n\n"); // NOI18N

                if(functionNumber != 0) {
                    testCalls.append("||\n        "); // NOI18N
                }
                testCalls.append("(NULL == CU_add_test(pSuite, \"" + testFunctionName + "\", " + testFunctionName + ")) "); // NOI18N

                functionNumber++;
            }

            testCalls.append(")\n"); // NOI18N
            testCalls.append("    {\n"); // NOI18N
            testCalls.append("        CU_cleanup_registry();\n"); // NOI18N
            testCalls.append("        return CU_get_error();\n"); // NOI18N
            testCalls.append("    }\n"); // NOI18N

            if(functionNumber != 0) {
                templateParams.put("testFunctions", testFunctions.toString()); // NOI18N
                templateParams.put("testCalls", testCalls.toString()); // NOI18N
                templateParams.put("testIncludes", testIncludes.toString()); // NOI18N
            }
        }

        return templateParams;
    }
}
