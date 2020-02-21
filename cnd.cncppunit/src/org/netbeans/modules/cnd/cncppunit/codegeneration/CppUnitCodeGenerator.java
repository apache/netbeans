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
public class CppUnitCodeGenerator {

    private CppUnitCodeGenerator() {
    }

    public static Map<String, Object> generateTemplateParamsForFunctions(String testClassName, FSPath testFilePath, List<CsmFunction> functions) {
        Map<String, Object> templateParams = new HashMap<String, Object>();

        if (functions != null) {
            StringBuilder testFunctions = new StringBuilder(""); // NOI18N
            StringBuilder testInits = new StringBuilder(""); // NOI18N
            StringBuilder testDecls = new StringBuilder(""); // NOI18N
            StringBuilder testIncludes = new StringBuilder(""); // NOI18N

            List<String> testFunctionsNames = new ArrayList<String>();
            List<String> addedTestIncludes = new ArrayList<String>();

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
                    int i = 2;
                    while(testFunctionsNames.contains(testFunctionName + i)) {
                        i++;
                    }
                    testFunctionName = testFunctionName + i;
                }
                testFunctionsNames.add(testFunctionName);
                testFunctions.append("void ") // NOI18N
                        .append(testClassName) // NOI18N
                        .append("::") // NOI18N
                        .append(testFunctionName) // NOI18N
                        .append("() {\n"); // NOI18N
                Collection<CsmParameter> params = fun.getParameters();
                int i = 0;
                for (CsmParameter param : params) {
                    if (!param.isVarArgs()) {
                        testFunctions.append("    "); // NOI18N
                        testFunctions.append(CodeGenerationUtils.generateParameterDeclaration(param, i));
                        testFunctions.append("\n"); // NOI18N
                        i++;
                    }
                }

                testFunctions.append(CodeGenerationUtils.generateFunctionCall(fun));

                testFunctions.append("    if(true /*check result*/) {\n"); // NOI18N
                testFunctions.append("        CPPUNIT_ASSERT(false);\n"); // NOI18N
                testFunctions.append("    }\n"); // NOI18N
                
                testFunctions.append("}\n\n"); // NOI18N

                testDecls.append("    void " + testFunctionName + "();\n"); // NOI18N

                testInits.append("    CPPUNIT_TEST(" + testFunctionName + ");\n"); // NOI18N

            }

            templateParams.put("testFunctions", testFunctions.toString()); // NOI18N
            templateParams.put("testDecls", testDecls.toString()); // NOI18N
            templateParams.put("testInits", testInits.toString()); // NOI18N
            templateParams.put("testIncludes", testIncludes.toString()); // NOI18N
        }

        return templateParams;
    }
}
