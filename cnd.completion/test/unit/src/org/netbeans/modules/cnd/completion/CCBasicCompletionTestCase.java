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

package org.netbeans.modules.cnd.completion;

import java.util.Arrays;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionBaseTestCase;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelFileFilter;

/**
 * NOTE: DO NOT ADD NEW TEST METHODS. 
 * IT IS SENSITIVE TO GLOBAL SYMBOLS AND A LOT OF GOLDEN FILES NEED TO BE CHANGED.
 *
 */
public class CCBasicCompletionTestCase extends CompletionBaseTestCase {

    /**
     * Creates a new instance of CCBasicCompletionTestCase
     */
    public CCBasicCompletionTestCase(String testName) {
        super(testName, true);
    }

    @Override
    protected TraceModelFileFilter getTraceModelFileFilter() {
        final String testName = getName();
        if (Arrays.asList("test208053").contains(testName)) {
            return new TraceModelFileFilter() {

                @Override
                public boolean isProjectFile(String filename) {
                    return filename.contains(testName);
                }
            };
        }
        return null;
    }

    public void test208053() throws Exception {
        // IZ#208053: Method completion replaces uint64_t as unsigned long int
        String text = "template <typename T> MyClass<T>::";
        super.performTest("test208053.cpp", 9, 1, text);
    }
    
    public void test142903_1() throws Exception {
        // IZ#142903: Code completion does not work immediately after "{" or "}"
        super.performTest("file.cc", 44, 35);
    }
    
    public void test142903_2() throws Exception {
        // IZ#142903: Code completion does not work immediately after "{" or "}"
        super.performTest("file.cc", 46, 6);
    }

    public void testIZ109010() throws Exception {
        // IZ#109010: Code completion listbox doesn't appear after "flag ? static_cast<int>(remainder) :" expression
        super.performTest("file.cc", 45, 54);
    }
    
    public void testIZ131568() throws Exception {
        // IZ#131568: Completion doubles some static functions
        super.performTest("iz131568.cc", 4, 5, "Re");
    }

    public void testIZ131283() throws Exception {
        // IZ#131283: Code Completion works wrongly in some casses
        super.performTest("file.h", 28, 9, "st");
    }

    public void testIZ119041() throws Exception {
        super.performTest("file.cc", 9, 5, "str[]", -1);
    }

    public void testNoAbbrevInCompletion() throws Exception {
        super.performTest("file.cc", 43, 5, "a.f");
    }
    
    public void testEmptyDerefCompletion_1() throws Exception {
        super.performTest("file.cc", 39, 5, " &");
    }
    
    public void testEmptyDerefCompletion_2() throws Exception {
        super.performTest("file.cc", 39, 5, "pointer = &");
    }
    
    public void testEmptyDerefCompletion_3() throws Exception {
        super.performTest("file.cc", 39, 5, "foo(&)", -1);
    }    

    public void testEmptyPtrCompletion_1() throws Exception {
        super.performTest("file.cc", 39, 5, "*");
    }
    
    public void testEmptyPtrCompletion_2() throws Exception {
        super.performTest("file.cc", 39, 5, "pointer = *");
    }
    
    public void testEmptyPtrCompletion_3() throws Exception {
        super.performTest("file.cc", 39, 5, "foo(*)", -1);
    }    

    public void testCompletionInEmptyFile() throws Exception {
        super.performTest("empty.cc", 1,1);
    }

    public void testRecoveryBeforeFoo() throws Exception {
        super.performTest("file.cc", 43, 5, "a.");
    }

    public void testExtraDeclarationOnTypeInsideFun() throws Exception {
        super.performTest("file.cc", 39, 5, "p");
    }

    public void testSwitchCaseVarsInCompound() throws Exception {
        super.performTest("file.cc", 24, 13);
    }

    public void testSwitchCaseVarsNotIncCompound() throws Exception {
        super.performTest("file.cc", 28, 13);
    }

    public void testSwitchCaseVarsAfterCompoundAndNotCompoundInDefault() throws Exception {
        super.performTest("file.cc", 32, 13);
    }

    public void testCompletionOnEmptyInGlobal() throws Exception {
        super.performTest("file.cc", 1, 1);
    }

    public void testCompletionOnEmptyInClassFunction() throws Exception {
        super.performTest("file.cc", 7, 1);
    }

    public void testCompletionOnEmptyInGlobFunction() throws Exception {
        super.performTest("file.cc", 19, 1);
    }

    public void testCompletionInsideInclude() throws Exception {
        // IZ#98530]  Completion list appears in #include directive
        super.performTest("file.cc", 2, 11); // completion inside #include "file.c"
    }

    public void testCompletionInsideString() throws Exception {
        // no completion inside string
        super.performTest("file.cc", 8, 18); // completion inside strings
    }

    public void testCompletionInsideChar() throws Exception {
        // no completion inside char literal
        super.performTest("file.cc", 14, 15); // completion inside chars
    }

    public void testGlobalCompletionInGlobal() throws Exception {
        super.performTest("file.cc", 47, 1, "::");
    }

    public void testGlobalCompletionInClassFunction() throws Exception {
        super.performTest("file.cc", 7, 1, "::");
    }

    public void testGlobalCompletionInGlobFunction() throws Exception {
        super.performTest("file.cc", 19, 1, "::");
    }

    public void testCompletionInConstructor() throws Exception {
        super.performTest("file.h", 20, 9);
    }

    public void testProtectedMethodByClassPrefix() throws Exception {
        super.performTest("file.h", 23, 9, "B::");
    }

    public void testGlobalMethodByScopePrefix() throws Exception {
        super.performTest("file.cc", 9, 26);
    }
    ////////////////////////////////////////////////////////////////////////////
    // tests for incomplete or incorrect constructions

    public void testErrorCompletion1() throws Exception {
        super.performTest("file.cc", 5, 1, "->");
    }

    public void testErrorCompletion2() throws Exception {
        super.performTest("file.cc", 5, 1, ".");
    }

    public void testErrorCompletion3() throws Exception {
        super.performTest("file.cc", 5, 1, ".->");
    }

    public void testErrorCompletion4() throws Exception {
        super.performTest("file.cc", 5, 1, "::.");
    }

    public void testErrorCompletion5() throws Exception {
        super.performTest("file.cc", 5, 1, "*:");
    }

    public void testErrorCompletion6() throws Exception {
        super.performTest("file.cc", 5, 1, ":");
    }

    public void testErrorCompletion7() throws Exception {
        super.performTest("file.cc", 5, 1, "->");
    }

    public void testErrorCompletion8() throws Exception {
        super.performTest("file.cc", 5, 1, "#inc");
    }

    public void testErrorCompletion9() throws Exception {
        super.performTest("file.cc", 5, 1, "#");
    }

    public void testErrorCompletion10() throws Exception {
        // IZ#77774: Code completion list appears, when include header file
        super.performTest("file.cc", 1, 1, "#include \"file.");
    }

    public void testErrorCompletionInFun1() throws Exception {
        super.performTest("file.cc", 7, 1, "->");
    }

    public void testErrorCompletionInFun2() throws Exception {
        super.performTest("file.cc", 7, 1, ".");
    }

    public void testErrorCompletionInFun3() throws Exception {
        super.performTest("file.cc", 7, 1, ".->");
    }

    public void testErrorCompletionInFun4() throws Exception {
        super.performTest("file.cc", 7, 1, "::.");
    }

    public void testErrorCompletionInFun5() throws Exception {
        super.performTest("file.cc", 7, 1, "*:");
    }

    public void testErrorCompletionInFun6() throws Exception {
        super.performTest("file.cc", 7, 1, ":");
    }

    public void testErrorCompletionInFun7() throws Exception {
        super.performTest("file.cc", 7, 1, "->");
    }

    public  void testCompletionAfterSpace() throws Exception {
        super.performTest("file.cc", 7, 5, "A::                 f");
    }

    public  void testCompletionAfterSpace2() throws Exception {
        super.performTest("file.cc", 43, 5, "a    .     ");
    }

    public void testCompletionInEmptyUsrInclude() throws Exception {
        super.performTest("file.cc", 1, 1, "#include \"\"", -1);
    }

    public void testCompletionInEmptySysInclude() throws Exception {
        super.performTest("file.cc", 1, 1, "#include <>", -1);
    }

    ////////////////////////////////////////////////////////////////////////////
    // tests for static function completion
    // IZ#126622 : Static function is missed in Code Completion listbox

    public void testCompletionForStaticFunctions1() throws Exception {
        super.performTest("static.cc", 18, 5);
    }

    public void testCompletionForStaticFunctions2() throws Exception {
        super.performTest("static.cc", 18, 5, "func");
    }

    public void testCompletionForStaticFunctions3() throws Exception {
        super.performTest("static.cc", 18, 5, "b");
    }

    public void testCompletionForStaticFunctions4() throws Exception {
        super.performTest("static.cc", 23, 1);
    }

    public void testRestrictPointers1() throws Exception {
        super.performTest("restrict.cc", 15, 5);
    }

    public void testRestrictPointers2() throws Exception {
        super.performTest("restrict.c", 15, 5);
    }

    public void testLocalEnumerators() throws Exception {
        super.performTest("local_enumerators.cc", 4, 14);
    }

    ////////////////////////////////////////////////////////////////////////////
    // tests for cast completion
    // IZ#92198 : Code completion works wrong with static_cast expression
    public void testCast1() throws Exception {
        super.performTest("cast.cc", 9, 23);
    }

    public void testCast2() throws Exception {
        super.performTest("cast.cc", 10, 34);
    }

    public void testCast3() throws Exception {
        super.performTest("cast.cc", 19, 21);
    }

    public void testCast4() throws Exception {
        super.performTest("cast.cc", 20, 33);
    }

    // IZ#148011 : Unable to resolve identifier when strings are involved
    public void testStringLiteral() throws Exception {
        super.performTest("iz148011.cc", 12, 25);
    }

    public void testIZ159423() throws Exception {
        // IZ#159423: Code completion doesn't display macros
        performTest("check_macro.cpp", 11, 16);
        performTest("check_macro.cpp", 11, 17);
    }

    public void testIZ159424_1() throws Exception {
        // IZ#159423: Code Completion works wrongly in macros
        performTest("check_macro.cpp", 13, 14);
    }
    
    public void testIZ159424_2() throws Exception {
        // IZ#159423: Code Completion works wrongly in macros
        performTest("check_macro.cpp", 13, 18, ".v");
    }    

    public void testIZ166620() throws Exception {
        // IZ#166620 : Code comlpetion does not show local variable inside for operator
        performTest("iz166620.cc", 6, 24);
    }

    public void testIZ173049() throws Exception {
        // IZ#173049 : Incorrect type resolution for template class functions
        performTest("iz173049.cc", 16, 16);
    }

    public void testBug188804() throws Exception {
        // Bug 188804 - No completion after for
        performTest("bug188804.cpp", 6, 5, "for");
    }
    
    public void testBug188804_2() throws Exception {
        // Bug 188804 - No completion after for
        performTest("bug188804.cpp", 6, 5, "if");
    }

    public void testBug206234() throws Exception {
        // Bug 206234 - Code Completation in constructor
        performTest("bug206234.cpp", 13, 37);
    }
    
    public void testBug214650() throws Exception {
        // Bug 214650 - Function declaration code completion messes up with const arguments
        performTest("bug214650.cpp", 6, 5, "void te");
    }   
    
    public void testBug235102_cc_1() throws Exception {
        // Bug 235102 - 5% inaccuracy in LLVM
        performTest("bug235102_cc.cpp", 15, 42);
    }       
    
    public void testBug235102_cc_2() throws Exception {
        // Bug 235102 - 5% inaccuracy in LLVM
        performTest("bug235102_cc.cpp", 15, 43);
    }           
    
    public void testSpecializationQualifiers_0() throws Exception {
        // No bug
        performTest("spec_quals_cc_0.cpp", 19, 13, "var.");
    }
}
