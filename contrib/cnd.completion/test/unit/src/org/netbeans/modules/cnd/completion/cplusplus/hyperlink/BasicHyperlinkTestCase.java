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

package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

import org.junit.Test;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelFileFilter;
import org.netbeans.modules.cnd.modelimpl.test.ProjectBasedTestCase.SimpleFileFilter;

/**
 *
 *
 */
public class BasicHyperlinkTestCase extends HyperlinkBaseTestCase {

    public BasicHyperlinkTestCase(String testName) {
        super(testName, true);
    }

    @Override
    protected TraceModelFileFilter getTraceModelFileFilter() {
        String simpleName = SimpleFileFilter.testNameToFileName(getName());
        switch (simpleName) {
            case "IZ157907":
                return new SimpleFileFilter("fun_macro_and_name");
            case "IZ139600":
            case "VarInFunWithInitalization":
            case "ParamWithoutSpace":
            case "FileLocalVariable":
            case "FuncParamUsage":
            case "ForLoopLocalVarsUsage":
            case "NameWithUnderscore":
            case "SameNameDiffScope":
            case "GlobalVar":
                return new SimpleFileFilter("main");
            case "FuncUsage":
            case "KRFuncParamUsage":
            case "KRFooDeclDefUsageH":
            case "KRFooDeclDefUsageC":
            case "KRFuncParamDecl":
            case "KRFooDeclDefUsage":
                return new SimpleFileFilter("kr");
            case "FuncLocalVarsUsage":
                return new SimpleFileFilter("main","kr");
            case "ConstParameter":
                return new SimpleFileFilter("const");
            case "StaticFunctions":
                return new SimpleFileFilter("static_function");
            case "StringInMacroParams":
                return new SimpleFileFilter("string_in_macro_params");
            case "StaticConstInNamespace":
                return new SimpleFileFilter("IZ141765_static_const_in_nsp");
            case "StaticVariable":
                return new SimpleFileFilter("static_variable");
            case "StaticFunctionInHeader":
                return new SimpleFileFilter("IZ141601_static_fun_in_hdr");
            case "Bug202191":
                return new SimpleFileFilter("bug201237_2");
            case "TemplateParameterBeforeFunction":
                return new SimpleFileFilter("template_parameter2");
            case "TemplateParameter":
                return new SimpleFileFilter("template_parameter");
            case "Bug191305":
                return new SimpleFileFilter("bug191198");
            default:
                return new SimpleFileFilter(simpleName); 
        }
    }

    public void test239814() throws Exception {
        // #239814 - When clicks "Find usages" shows error alert "Cannot refactor here".
        performTest("iz239814.cpp", 11, 10, "iz239814.cpp", 8, 1);
        performTest("iz239814.cpp", 11, 20, "iz239814.cpp", 3, 5);
    }
    
    public void test229003() throws Exception {
        // #229003 - inaccuracy tests: Perl project has unresolved identifiers
        
        performTest("iz229003.cpp", 11, 20, "iz229003.cpp", 9, 5);
        performTest("iz229003.cpp", 12, 20, "iz229003.cpp", 9, 5);
        performTest("iz229003.cpp", 12, 40, "iz229003.cpp", 10, 5);
        performTest("iz229003.cpp", 12, 50, "iz229003.cpp", 6, 1);
        performTest("iz229003.cpp", 12, 60, "iz229003.cpp", 5, 1);
        
        // fun-ptr param parameters
        performTest("iz229003.cpp", 15, 35, "iz229003.cpp", 15, 27);
        performTest("iz229003.cpp", 15, 60, "iz229003.cpp", 15, 52);
        performTest("iz229003.cpp", 15, 90, "iz229003.cpp", 15, 78);
        
    }
    
    public void test192897() throws Exception {
        // #192897 -  unstable LiteSQL accuracy test
        performTest("iz192897.h", 23, 20, "iz192897.h", 6, 13);
        performTest("iz192897.cc", 10, 15, "iz192897.h", 8, 13);
        performTest("iz192897.h", 24, 20, "iz192897.h", 9, 13);
        performTest("iz192897.cc", 11, 20, "iz192897.h", 9, 13);        
    }
    
    public void test191446_2() throws Exception {
        // #191446 -  no code assistance for elementes #include'ed in namespace body
        performTest("iz191446_2.cc", 24, 30, "iz191446_2.h", 18, 5);
        performTest("iz191446_2.cc", 15, 34, "iz191446_2.cc", 13, 17);
        performTest("iz191446_2.cc", 24, 10, "iz191446_2.cc", 23, 9);
        performTest("iz191446_2.cc", 17, 25, "iz191446_2.cc", 12, 17);
        performTest("iz191446_2.cc", 15, 20, "iz191446_2.cc", 14, 17);
    }
    
    public void test191446() throws Exception {
        // #191446 -  no code assistance for elementes #include'ed in namespace body
        performTest("iz191446.cc", 7, 25, "iz191446.h", 5, 1);
        performTest("iz191446.cc", 8, 20, "iz191446.h", 9, 9);
    }
    
    public void test191457() throws Exception {
        // #191457: Parser error in hashtable.cc (ccfe)
        performTest("iz191457.cc", 9, 10, "iz191457.cc", 15, 1);
        performTest("iz191457.cc", 15, 15, "iz191457.cc", 9, 9);
        performTest("iz191457.cc", 16, 10, "iz191457.cc", 7, 9);
        performTest("iz191457.cc", 17, 10, "iz191457.cc", 8, 9);
    }
    
    public void testIZ157907() throws Exception {
        // IZ#157907: False positive recognition of macro
        performTest("fun_macro_and_name.c", 6, 5, "fun_macro_and_name.c", 6, 3); // PREFIX as name of typedef
        performTest("fun_macro_and_name.c", 10, 10, "fun_macro_and_name.c", 6, 3); // PREFIX as name of typedef

        performTest("fun_macro_and_name.c", 1, 10, "fun_macro_and_name.c", 1, 1); // PREFIX as name of macro with params
        performTest("fun_macro_and_name.c", 8, 15, "fun_macro_and_name.c", 1, 1); // PREFIX as name of macro with params
    }

    public void testIZ151061() throws Exception {
        // IZ#151061: code model inaccuracy on VLC's is above boundary
        performTest("iz151061.c", 6, 10, "iz151061.c", 2, 5);
        performTest("iz151061.c", 7, 10, "iz151061.c", 2, 5);
        performTest("iz151061.c", 24, 20, "iz151061.c", 2, 5);

        performTest("iz151061.c", 17, 15, "iz151061.c", 13, 9);
        performTest("iz151061.c", 18, 15, "iz151061.c", 13, 9);
        performTest("iz151061.c", 22, 15, "iz151061.c", 13, 9);
    }

    public void testIZ146392() throws Exception {
        // IZ#146392: regression: some declaration statements are not rendered any more
        performTest("iz146392.cc", 4, 25, "iz146392.cc", 4, 22);
        performTest("iz146392.cc", 6, 15, "iz146392.cc", 4, 22);
    }

    public void testIZ139600() throws Exception {
        performTest("main.cc", 35, 15, "main.cc", 35, 5); // funPtr in int (*funPtr)();
    }

    public void testVarInFunWithInitalization() throws Exception {
        performTest("main.cc", 19, 10, "main.cc", 19, 5); // iiii in int iiii = fun(null, null);
    }

    public void testParamWithoutSpace() throws Exception {
        performTest("main.cc", 18, 17, "main.cc", 18, 10); // aaa in void foo(char* aaa, char**bbb)
        performTest("main.cc", 18, 28, "main.cc", 18, 21); // bbb in void foo(char* aaa, char**bbb)
    }

    public void testFileLocalVariable() throws Exception {
        performTest("main.cc", 15, 12, "main.cc", 15, 1); // VALUE in const int VALUE = 10;
        performTest("main.cc", 16, 30, "main.cc", 15, 1); // VALUE in const int VALUE_2 = 10 + VALUE;
        performTest("main.cc", 16, 12, "main.cc", 16, 1); // VALUE_2 in const int VALUE_2 = 10 + VALUE;
    }

    public void testFuncParamUsage() throws Exception {
        performTest("main.cc", 3, 15, "main.cc", 2, 9); // aa in 'int kk = aa + bb;'
        performTest("main.cc", 3, 20, "main.cc", 2, 17); // bb in 'int kk = aa + bb;'
    }

    public void testFuncUsage() throws Exception {
        performTest("kr.c", 6, 13, "kr.c", 9, 1); // foo in "return foo(kk) + boo(kk);"
        performTest("kr.c", 6, 23, "kr.c", 17, 1); // boo in "return foo(kk) + boo(kk);"
    }

    public void testFuncLocalVarsUsage() throws Exception {
        performTest("main.cc", 5, 20, "main.cc", 3, 5); // kk in "for (int ii = kk; ii > 0; ii--) {"
        performTest("main.cc", 6, 10, "main.cc", 4, 5); // res in "res *= ii;"
        performTest("main.cc", 8, 13, "main.cc", 4, 5); // res in "return res;"
        performTest("kr.c", 6, 17, "kr.c", 5, 5); // first kk in "return foo(kk) + boo(kk);"
        performTest("kr.c", 6, 27, "kr.c", 5, 5); // second kk in "return foo(kk) + boo(kk);"
    }

    public void testForLoopLocalVarsUsage() throws Exception {
        performTest("main.cc", 5, 24, "main.cc", 5, 10); // second ii in "for (int ii = kk; ii > 0; ii--) {"
        performTest("main.cc", 5, 32, "main.cc", 5, 10); // third ii in "for (int ii = kk; ii > 0; ii--) {"
        performTest("main.cc", 6, 17, "main.cc", 5, 10); // ii in "res *= ii;"
    }

    public void testNameWithUnderscore() throws Exception {
        performTest("main.cc", 12, 6, "main.cc", 11, 1); // method_name_with_underscore();
    }

    public void testSameNameDiffScope() throws Exception {
        // IZ#131560: Hyperlink does not distinguish variables with the same names within function body
        // function parameter
        performTest("main.cc", 22, 30, "main.cc", 22, 24); // name in void sameNameDiffScope(int name) {
        performTest("main.cc", 23, 10, "main.cc", 22, 24); // name in if (name++) {
        performTest("main.cc", 26, 17, "main.cc", 22, 24); // name in } else if (name++) {
        performTest("main.cc", 26, 17, "main.cc", 22, 24); // name in name--;

        // local variable
        performTest("main.cc", 24, 17, "main.cc", 24, 9); // name in name--;
        performTest("main.cc", 25, 10, "main.cc", 24, 9); // name in name--;

        // second local variable
        performTest("main.cc", 27, 17, "main.cc", 27, 9); // name in name--;
        performTest("main.cc", 28, 17, "main.cc", 27, 9); // name in name--;
    }

    public void testGlobalVar() throws Exception {
        // IZ#132295: Hyperlink does not  distinguish local variable and global
        // variable if they has same name

        // local variable
        performTest("main.cc", 33, 24, "main.cc", 32, 5);
        performTest("main.cc", 34, 36, "main.cc", 32, 5);

        // global variable
        performTest("main.cc", 33, 14, "main.cc", 38, 1);
        performTest("main.cc", 34, 12, "main.cc", 38, 1);
        performTest("main.cc", 34, 28, "main.cc", 38, 1);
    }

    public void testConstParameter() throws Exception {
        // IZ#76032: ClassView component doubles function in some cases
        // (partial fix: made const parameters resolve correctly)
        performTest("const.cc", 5, 44, "const.cc", 1, 1);
        performTest("const.cc", 5, 50, "const.cc", 2, 5);
    }

    ////////////////////////////////////////////////////////////////////////////
    // K&R style

    public void testKRFuncParamUsage() throws Exception {
        performTest("kr.c", 12, 15, "kr.c", 10, 1); // index in 'return index;'
    }

    public void testKRFooDeclDefUsageH() throws Exception {
        // See IZ116715
        performTest("kr.h", 2, 6, "kr.h", 9, 1); // int foo(); -> int foo(index)
        performTest("kr.h", 9, 6, "kr.h", 2, 1); // int foo(index) -> int foo();
        performTest("kr.h", 15, 6, "kr.h", 17, 1); // int boo(); -> int boo(int i)
        performTest("kr.h", 17, 6, "kr.h", 15, 1); // int boo(int i) -> int boo();
    }

    public void testKRFooDeclDefUsageC() throws Exception {
        // See IZ116715
        performTest("kr.c", 2, 6, "kr.c", 9, 1); // int foo(); -> int foo(index)
        performTest("kr.c", 9, 6, "kr.c", 2, 1); // int foo(index) -> int foo();
        performTest("kr.c", 15, 6, "kr.c", 17, 1); // int boo(); -> int boo(int i)
        performTest("kr.c", 17, 6, "kr.c", 15, 1); // int boo(int i) -> int boo();
    }

    public void testStaticVariable() throws Exception {
        // See IZ136481
        performTest("static_variable.c", 5, 16, "static_variable.h", 2, 1);
        performTest("static_variable.c", 6, 15, "static_variable.h", 1, 1);
        // See IZ151730: Unresolved static variable in header included after its definition
        performTest("static_variable.h", 2, 40, "static_variable.c", 1, 1);
    }

    public void testStaticFunctions() throws Exception {
        // IZ#151751: Unresolved usage of function name as pointer for static member initialization
        performTest("static_function.c", 26, 10, "static_function.c", 17, 1);
        performTest("static_function.c", 11, 30, "static_function.c", 2, 1);
    }

    public void testIZ131555() throws Exception {
        for (int i = 5; i <=13; i++ ) {
            performTest("IZ131555.c", i, 16, "IZ131555.c", 2, 5);
        }
    }

    public void testIZ136730() throws Exception {
        performTest("IZ136730.c", 2, 11, "IZ136730.c", 3, 1);
    }

    public void testTemplateParameter() throws Exception {
        performTest("template_parameter.cc", 2, 13, "template_parameter.cc", 1, 17);
        performTest("template_parameter.cc", 3, 13, "template_parameter.cc", 1, 17);
        performTest("template_parameter.cc", 6, 19, "template_parameter.cc", 1, 17);
        performTest("template_parameter.cc", 7, 14, "template_parameter.cc", 1, 17);
        performTest("template_parameter.cc", 7, 12, "template_parameter.cc", 1, 29);
        performTest("template_parameter.cc", 7, 26, "template_parameter.cc", 1, 10);
        performTest("template_parameter.cc", 8, 11, "template_parameter.cc", 1, 10);
    }

    public void testTemplateParameterBeforeFunction() throws Exception {
        // IZ#138099 : unresolved identifier for functions' template parameter
        performTest("template_parameter2.cc", 1, 18, "template_parameter2.cc", 1, 11);
        performTest("template_parameter2.cc", 4, 22, "template_parameter2.cc", 4, 15);
        performTest("template_parameter2.cc", 4, 66, "template_parameter2.cc", 4, 15);
        performTest("template_parameter2.cc", 5, 15, "template_parameter2.cc", 5, 14);
        performTest("template_parameter2.cc", 5, 41, "template_parameter2.cc", 5, 14);
        performTest("template_parameter2.cc", 8, 20, "template_parameter2.cc", 8, 10);
        performTest("template_parameter2.cc", 8, 46, "template_parameter2.cc", 8, 10);
        performTest("template_parameter2.cc", 9, 20, "template_parameter2.cc", 9, 10);
        performTest("template_parameter2.cc", 9, 46, "template_parameter2.cc", 9, 10);
        performTest("template_parameter2.cc", 11, 11, "template_parameter2.cc", 11, 10);
        performTest("template_parameter2.cc", 11, 55, "template_parameter2.cc", 11, 10);
        performTest("template_parameter2.cc", 13, 17, "template_parameter2.cc", 13, 10);
        performTest("template_parameter2.cc", 13, 29, "template_parameter2.cc", 13, 22);
        performTest("template_parameter2.cc", 13, 33, "template_parameter2.cc", 13, 22);
    }

    public void testIZ131625() throws Exception {
        performTest("IZ131625.cc",  4, 11, "IZ131625.cc", 10, 1);
        performTest("IZ131625.cc",  7, 23, "IZ131625.cc", 10, 1);
        performTest("IZ131625.cc",  7, 23, "IZ131625.cc", 10, 1);
        performTest("IZ131625.cc", 14, 35, "IZ131625.cc", 12, 3);
        performTest("IZ131625.cc", 18, 24, "IZ131625.cc", 10, 1);
        performTest("IZ131625.cc", 20,  3, "IZ131625.cc", 10, 1);
        performTest("IZ131625.cc", 21, 12, "IZ131625.cc", 13, 3);
        performTest("IZ131625.cc", 22, 11, "IZ131625.cc", 13, 3);
        performTest("IZ131625.cc", 10, 20, "IZ131625.cc",   4, 3);
    }

    public void testIZ136146() throws Exception {
        performTest("IZ136146.cc", 20, 10, "IZ136146.cc", 15, 5);
        performTest("IZ136146.cc", 21, 12, "IZ136146.cc", 15, 5);
    }

    public void testIZ132903() throws Exception {
        performTest("IZ132903.cc", 16, 10, "IZ132903.cc",  9, 5);
    }

    public void testIZ136167() throws Exception {
        performTest("IZ136167.cc", 21, 13, "IZ136167.cc",  3, 5);
    }

    public void testIZ138833() throws Exception {
        performTest("IZ138833.cc", 4, 17, "IZ138833.cc",  3, 5);
    }

    public void testIZ138905() throws Exception {
        // IZ#138905 : IDE highlights 'a1' as invalid identifier (struct {...} a1;)
        performTest("IZ138905.cc", 4, 4, "IZ138905.cc", 4, 3);
        performTest("IZ138905.cc", 9, 4, "IZ138905.cc", 9, 3);
        performTest("IZ138905.cc", 12, 18, "IZ138905.cc", 12, 17);
    }

    public void testIZ139056() throws Exception {
        // IZ#139056 : using directive affects only single namespace definition
        performTest("IZ139056.cc", 10, 8, "IZ139056.cc", 2, 5);
        performTest("IZ139056.cc", 10, 24, "IZ139056.cc", 2, 5);
        performTest("IZ139056.cc", 15, 8, "IZ139056.cc", 2, 5);
        performTest("IZ139056.cc", 15, 24, "IZ139056.cc", 2, 5);
    }

    public void testIZ139141() throws Exception {
        // IZ#139141 : unable to resolve constructor of nested structure
        performTest("IZ139141.cc", 7, 6, "IZ139141.cc", 7, 5);
        performTest("IZ139141.cc", 8, 6, "IZ139141.cc", 8, 5);
    }

    public void testIZ139618() throws Exception {
        // IZ#139618 : Syntax hightlighting failure for unnamed union
        performTest("IZ139618.cc", 2, 11, "IZ139618.cc", 2, 9);
        performTest("IZ139618.cc", 2, 15, "IZ139618.cc", 2, 14);
        performTest("IZ139618.cc", 3, 13, "IZ139618.cc", 3, 5);
        performTest("IZ139618.cc", 8, 16, "IZ139618.cc", 8, 9);
        performTest("IZ139618.cc", 9, 15, "IZ139618.cc", 9, 9);
        performTest("IZ139618.cc", 11, 7, "IZ139618.cc", 8, 9);
        performTest("IZ139618.cc", 12, 6, "IZ139618.cc", 9, 9);
        performTest("IZ139618.cc", 12, 19, "IZ139618.cc", 10, 7);
        performTest("IZ139618.cc", 12, 22, "IZ139618.cc", 9, 9);
    }

    public void testIZ139693() throws Exception {
        // IZ#139693 : function-local typedefs are not resolved
        performTest("IZ139693.cc", 2, 21, "IZ139693.cc", 2, 5);
        performTest("IZ139693.cc", 3, 9, "IZ139693.cc", 2, 5);
        performTest("IZ139693.cc", 4, 26, "IZ139693.cc", 2, 5);
    }

    public void testIZ139409() throws Exception {
        // IZ#139409 : Labels highlighted as errors
        performTest("IZ139409.cc", 1, 8, "IZ139409.cc", 1, 1);
        performTest("IZ139409.cc", 3, 17, "IZ139409.cc", 1, 1);
        performTest("IZ139409.cc", 4, 7, "IZ139409.cc", 4, 5);
        performTest("IZ139409.cc", 6, 16, "IZ139409.cc", 4, 5);
        performNullTargetTest("IZ139409.cc", 8, 11);
    }

    public void testIZ139784() throws Exception {
        // IZ#139784 : last unnamed enum overrides previous ones
        performTest("IZ139784.cc", 2, 13, "IZ139784.cc", 2, 12);
        performTest("IZ139784.cc", 2, 21, "IZ139784.cc", 2, 20);
        performTest("IZ139784.cc", 3, 13, "IZ139784.cc", 3, 12);
        performTest("IZ139784.cc", 3, 18, "IZ139784.cc", 2, 12);
        performTest("IZ139784.cc", 3, 26, "IZ139784.cc", 3, 25);
        performTest("IZ139784.cc", 3, 31, "IZ139784.cc", 2, 20);
        performTest("IZ139784.cc", 4, 16, "IZ139784.cc", 2, 12);
        performTest("IZ139784.cc", 5, 16, "IZ139784.cc", 2, 20);
        performTest("IZ139784.cc", 6, 16, "IZ139784.cc", 3, 12);
        performTest("IZ139784.cc", 7, 16, "IZ139784.cc", 3, 25);
    }

    public void testIZ139058() throws Exception {
        // IZ#139058 : unresolved identifiers in statement "this->operator std::string()"
        performTest("IZ139058.cc", 7, 65, "IZ139058.cc", 1, 1);
        performTest("IZ139058.cc", 7, 75, "IZ139058.cc", 2, 5);
    }

    public void testIZ139143() throws Exception {
        // IZ#139143 : unresolved identifiers in "(*cur.object).*cur.creator"
        performTest("IZ139143.cc", 9, 9, "IZ139143.cc", 8, 5);
        performTest("IZ139143.cc", 9, 14, "IZ139143.cc", 4, 5);
        performTest("IZ139143.cc", 9, 24, "IZ139143.cc", 8, 5);
        performTest("IZ139143.cc", 9, 29, "IZ139143.cc", 5, 5);
        performTest("IZ139143.cc", 10, 11, "IZ139143.cc", 8, 5);
        performTest("IZ139143.cc", 10, 18, "IZ139143.cc", 4, 5);
        performTest("IZ139143.cc", 10, 23, "IZ139143.cc", 8, 5);
        performTest("IZ139143.cc", 10, 28, "IZ139143.cc", 5, 5);
    }

    public void testIZ140111() throws Exception {
        // IZ#140111 : unresolved identifier in declaration "TCHAR c;"
        performTest("IZ140111.cc", 3, 10, "IZ140111.cc", 3, 1);
        performTest("IZ140111.cc", 4, 8, "IZ140111.cc", 4, 1);
        performTest("IZ140111.cc", 7, 14, "IZ140111.cc", 7, 5);
        performTest("IZ140111.cc", 8, 12, "IZ140111.cc", 8, 5);
        performTest("IZ140111.cc", 12, 14, "IZ140111.cc", 12, 5);
        performTest("IZ140111.cc", 13, 12, "IZ140111.cc", 13, 5);
        performTest("IZ140111.cc", 14, 8, "IZ140111.cc", 14, 5);
    }

    public void testIZ140589() throws Exception {
        // IZ#140589 : template class member is not resolved when parentheses are used
        performTest("IZ140589.cc", 8, 38, "IZ140589.cc", 3, 5);
        performTest("IZ140589.cc", 9, 38, "IZ140589.cc", 3, 5);
    }

    public void testIZ138683() throws Exception {
        // IZ#138683 : function typedef are not recognized
        performTest("IZ138683.cc", 4, 24, "IZ138683.cc", 2, 1);
    }

    public void testLabels() throws Exception {
        // IZ#141135 : Labels within code bocks are unresolved
        performTest("labels.cc", 3, 12, "labels.cc", 4, 5);
        performTest("labels.cc", 8, 12, "labels.cc", 10, 9);
        performTest("labels.cc", 15, 12, "labels.cc", 19, 9);
        performTest("labels.cc", 24, 12, "labels.cc", 26, 9);
        performTest("labels.cc", 31, 12, "labels.cc", 33, 9);
        performTest("labels.cc", 38, 12, "labels.cc", 40, 9);
        performTest("labels.cc", 45, 12, "labels.cc", 47, 9);
        performTest("labels.cc", 57, 19, "labels.cc", 54, 13);
    }

    public void testStaticConstInNamespace() throws Exception {
        // IZ141765 static const in namespace definition is unresolved
        performTest("IZ141765_static_const_in_nsp.cc", 7, 48, "IZ141765_static_const_in_nsp.h", 3, 17);
        performTest("IZ141765_static_const_in_nsp.cc", 9, 48, "IZ141765_static_const_in_nsp.h", 4, 17);
    }

    public void testStaticFunctionInHeader() throws Exception {
        // IZ141601 A static function defined in a header and used in a source file is unresolved
        performTest("IZ141601_static_fun_in_hdr.c", 4, 8, "IZ141601_static_fun_in_hdr.h", 2, 1);
    }

    public void testIZ141842() throws Exception {
        // IZ#141842 : If template parameter declared as a template class, its usage is unresolved
        performTest("IZ141842.cc", 9, 13, "IZ141842.cc", 5, 5);
        performTest("IZ141842.cc", 13, 5, "IZ141842.cc", 5, 5);
        performTest("IZ141842.cc", 14, 5, "IZ141842.cc", 5, 5);
    }

    public void testIZ137897() throws Exception {
        // IZ#137897 : parameters of function pointer are not resolved
        performTest("IZ137897.cc", 1, 24, "IZ137897.cc", 1, 15);
        performTest("IZ137897.cc", 1, 43, "IZ137897.cc", 1, 31);
        performTest("IZ137897.cc", 2, 26, "IZ137897.cc", 2, 16);
        performTest("IZ137897.cc", 2, 43, "IZ137897.cc", 2, 34);
        performTest("IZ137897.cc", 3, 30, "IZ137897.cc", 3, 24);
    }

    public void testIZ143226() throws Exception {
        // IZ#143226 : Incorrect error in the editor
        performTest("IZ143226.cc", 3, 6, "IZ143226.cc", 2, 5);
        performTest("IZ143226.cc", 3, 18, "IZ143226.cc", 2, 5);
    }

    public void testIZ144154() throws Exception {
        // IZ#144154 : nested typedef "type" is unresolved in Boost
        performTest("IZ144154.cc", 24, 49, "IZ144154.cc", 12, 9);
        performTest("IZ144154.cc", 57, 52, "IZ144154.cc", 31, 5);
    }

    public void testIZ144360() throws Exception {
        // IZ#144360 : unable to resolve typedef-ed class member in loki
        performTest("IZ144360.cc", 12, 22, "IZ144360.cc", 12, 9);
        performTest("IZ144360.cc", 13, 9, "IZ144360.cc", 12, 9);
        performTest("IZ144360.cc", 13, 15, "IZ144360.cc", 7, 9);
    }

    public void test186780() throws Exception {
        // #186780 -  Random resolving of variable type  
        performTest("IZ186780.cc", 16, 30, "IZ186780.cc", 7, 1);
        performTest("IZ186780.cc", 16, 50, "IZ186780.cc", 3, 5);
        performTest("IZ186780.cc", 17, 40, "IZ186780.cc", 4, 5);
        performTest("IZ186780.c", 3, 15, "IZ186780.c", 3, 1);
        performTest("IZ186780.c", 5, 15, "IZ186780.c", 3, 1);
    }
    
    public void testIZ140795() throws Exception {
        // IZ#140795 : Usage of enumerators of nested enums
        // of the template specializations are unresolved
        performTest("IZ140795.cc", 8, 30, "IZ140795.cc", 4, 16);
        performTest("IZ140795.cc", 9, 29, "IZ140795.cc", 4, 16);
        performTest("IZ140795.cc", 10, 30, "IZ140795.cc", 4, 16);
        performTest("IZ140795.cc", 11, 34, "IZ140795.cc", 4, 16);
        performTest("IZ140795.cc", 12, 36, "IZ140795.cc", 4, 16);
        performTest("IZ140795.cc", 13, 37, "IZ140795.cc", 4, 16);
        performTest("IZ140795.cc", 14, 43, "IZ140795.cc", 4, 16);
    }

    public void testIZ140757() throws Exception {
        // IZ#140757 : Template parameter in the definition of the static
        // template class field is highlighted as an error
        performTest("IZ140757.cc", 17, 12, "IZ140757.cc", 17, 5);
        performTest("IZ140757.cc", 18, 29, "IZ140757.cc", 18, 5);
        performTest("IZ140757.cc", 19, 29, "IZ140757.cc", 19, 5);
        performTest("IZ140757.cc", 20, 36, "IZ140757.cc", 20, 5);
        performTest("IZ140757.cc", 21, 12, "IZ140757.cc", 21, 5);
        performTest("IZ140757.cc", 23, 27, "IZ140757.cc", 17, 5);
        performTest("IZ140757.cc", 23, 30, "IZ140757.cc", 18, 5);
        performTest("IZ140757.cc", 23, 33, "IZ140757.cc", 19, 5);
        performTest("IZ140757.cc", 23, 36, "IZ140757.cc", 20, 5);
        performTest("IZ140757.cc", 23, 39, "IZ140757.cc", 21, 5);
        performTest("IZ140757.cc", 24, 22, "IZ140757.cc", 17, 5);
        performTest("IZ140757.cc", 24, 25, "IZ140757.cc", 18, 5);
        performTest("IZ140757.cc", 24, 28, "IZ140757.cc", 19, 5);
        performTest("IZ140757.cc", 24, 31, "IZ140757.cc", 20, 5);
        performTest("IZ140757.cc", 24, 34, "IZ140757.cc", 21, 5);
    }

    public void testIZ144363() throws Exception {
        // IZ#144363 : typename in for-loop leads to unresolved identifier error
        performTest("IZ144363.cc", 17, 48, "IZ144363.cc", 17, 13);
        performTest("IZ144363.cc", 18, 15, "IZ144363.cc", 17, 13);
        performTest("IZ144363.cc", 20, 43, "IZ144363.cc", 9, 5);
    }

    public void testIZ145286() throws Exception {
        // IZ#145286 : const variable declared in "if" condition is not resolved
        performTest("IZ145286.cc", 3, 27, "IZ145286.cc", 3, 13);
        performTest("IZ145286.cc", 4, 14, "IZ145286.cc", 3, 13);
        performTest("IZ145286.cc", 6, 31, "IZ145286.cc", 6, 16);
        performTest("IZ145286.cc", 7, 15, "IZ145286.cc", 6, 16);
        performTest("IZ145286.cc", 9, 29, "IZ145286.cc", 9, 17);
        performTest("IZ145286.cc", 10, 22, "IZ145286.cc", 9, 17);
    }

    public void testNamesakes() throws Exception {
        // IZ#145553 Class in the same namespace should have priority over a global one
        // global
        performTest("iz_145553_namesakes.cc", 14, 26, "iz_145553_namesakes.cc", 1, 1);
        performTest("iz_145553_namesakes.cc", 15, 8, "iz_145553_namesakes.cc", 1, 1);
        performTest("iz_145553_namesakes.cc", 18, 18, "iz_145553_namesakes.cc", 11, 5);
        performTest("iz_145553_namesakes.cc", 19, 12, "iz_145553_namesakes.cc", 1, 1);
        performTest("iz_145553_namesakes.cc", 19, 19, "iz_145553_namesakes.cc", 3, 5);
        performTest("iz_145553_namesakes.cc", 20, 20, "iz_145553_namesakes.cc", 4, 9);
        performTest("iz_145553_namesakes.cc", 22, 22, "iz_145553_namesakes.cc", 14, 1);
        // namespace
        performTest("iz_145553_namesakes.cc", 36, 24, "iz_145553_namesakes.cc", 28, 5);
        performTest("iz_145553_namesakes.cc", 41, 31, "iz_145553_namesakes.cc", 28, 5);
        performTest("iz_145553_namesakes.cc", 42, 10, "iz_145553_namesakes.cc", 28, 5);
        performTest("iz_145553_namesakes.cc", 45, 20, "iz_145553_namesakes.cc", 38, 9);
        performTest("iz_145553_namesakes.cc", 46, 16, "iz_145553_namesakes.cc", 28, 5);
        performTest("iz_145553_namesakes.cc", 47, 23, "iz_145553_namesakes.cc", 31, 13);
        performTest("iz_145553_namesakes.cc", 49, 25, "iz_145553_namesakes.cc", 41, 5);
    }

    public void testIZ145071() throws Exception {
        // IZ#145071 : forward declarations marked as error
        performTest("IZ145071.cc", 2, 20, "IZ145071.cc", 2, 9);
    }

    public void testIZ136731() throws Exception {
        // IZ#136731 : No hyper link on local extern function
        performTest("IZ136731_local_extern_function.cc", 4, 18, "IZ136731_local_extern_function.cc", 3, 16);
        performTest("IZ136731_local_extern_function.cc", 3, 40, "IZ136731_local_extern_function.cc", 3, 32);
    }

    public void testIZ146464() throws Exception {
        // IZ#146464 : IDE can't find 'wchar_t' identifier in C projects
        performTest("IZ146464.c", 1, 16, "IZ146464.c", 1, 1); // NOI18N
        performTest("IZ146464.c", 2, 5, "IZ146464.c", 1, 1); // NOI18N
        performTest("IZ146464.c", 2, 23, "IZ146464.c", 1, 1); // NOI18N
    }

    public void testIZ147627() throws Exception {
        // IZ#147627 : IDE highlights code with 'i' in 'for' as wrong
        performTest("IZ147627.cc", 6, 18, "IZ147627.cc", 6, 14); // NOI18N
        performTest("IZ147627.cc", 7, 23, "IZ147627.cc", 6, 14); // NOI18N
        performTest("IZ147627.cc", 7, 28, "IZ147627.cc", 6, 14); // NOI18N
        performTest("IZ147627.cc", 8, 18, "IZ147627.cc", 8, 14); // NOI18N
        performTest("IZ147627.cc", 9, 23, "IZ147627.cc", 8, 14); // NOI18N
        performTest("IZ147627.cc", 9, 28, "IZ147627.cc", 8, 14); // NOI18N
    }

    public void testIZ147632() throws Exception {
        // IZ#147632 : IDE highlights global variable in 'if' as wrong
        performTest("IZ147632.cc", 8, 16, "IZ147632.cc", 1, 1);
        performTest("IZ147632.cc", 8, 25, "IZ147632.cc", 3, 5);
        performTest("IZ147632.cc", 10, 25, "IZ147632.cc", 3, 5);
        performTest("IZ147632.cc", 12, 25, "IZ147632.cc", 3, 5);
    }

    public void testIZ152875() throws Exception {
        // IZ#152875 : No mark occurrences in macros actual parameters
        performTest("IZ152875.cc", 12, 26, "IZ152875.cc", 9, 24);
        performTest("IZ152875.cc", 12, 43, "IZ152875.cc", 9, 39);
        performTest("IZ152875.cc", 12, 53, "IZ152875.cc", 10, 5);
    }

    public void testIZ153761() throws Exception {
        // IZ#153761 : regression in python
        performTest("IZ153761.cc", 16, 18, "IZ153761.cc", 16, 13);
        performTest("IZ153761.cc", 19, 18, "IZ153761.cc", 19, 13);
        performTest("IZ153761.cc", 22, 18, "IZ153761.cc", 22, 13);
        performTest("IZ153761.cc", 25, 18, "IZ153761.cc", 25, 13);
        performTest("IZ153761.cc", 28, 18, "IZ153761.cc", 28, 13);
        performTest("IZ153761.cc", 31, 18, "IZ153761.cc", 31, 13);
        performTest("IZ153761.cc", 35, 18, "IZ153761.cc", 35, 13);
        performTest("IZ153761.cc", 38, 18, "IZ153761.cc", 38, 13);
        performTest("IZ153761.cc", 41, 18, "IZ153761.cc", 41, 13);
        performTest("IZ153761.cc", 44, 14, "IZ153761.cc", 43, 9);
    }

    public void testKRFuncParamDecl() throws Exception {
        performTest("kr.c", 9, 10, "kr.c", 10, 1); // index in 'int foo(index)'
        performTest("kr.c", 21, 13, "kr.c", 22, 8); // index in 'int foo(index)'
        performTest("kr.c", 21, 17, "kr.c", 22, 12); // index in 'int foo(index)'
    }

    public void testKRFooDeclDefUsage() throws Exception {
        // See IZ116715
        performTest("kr.c", 2, 6, "kr.c", 9, 1); // int foo(); -> int foo(index)
        performTest("kr.c", 9, 6, "kr.c", 2, 1); // int foo(index) -> int foo();
        performTest("kr.c", 15, 6, "kr.c", 17, 1); // int boo(); -> int boo(int i)
        performTest("kr.c", 17, 6, "kr.c", 15, 1); // int boo(int i) -> int boo();
    }

    public void testIZ151705() throws Exception {
        // IZ#151705 : Unresolved ids in function call in case of empty macro
        performTest("IZ151705.cc", 9, 15, "IZ151705.cc", 6, 1);
    }

    public void testIZ151045() throws Exception {
        // IZ#151045 : Unresolved cast to macro type
        performTest("IZ151045.cc", 11, 21, "IZ151045.cc", 3, 5);
    }

    public void testIZ158816() throws Exception {
        // IZ#158816 : No hyperlink for ids after short macros
        performTest("IZ158816.cc", 8, 16, "IZ158816.cc", 2, 5);
    }

    public void testIZ150884() throws Exception {
        // IZ#150884 : Unresolved elements in local definition of type
        performTest("IZ150884.cc", 3, 11, "IZ150884.cc", 3, 9);
        performTest("IZ150884.cc", 3, 28, "IZ150884.cc", 3, 24);
        performTest("IZ150884.cc", 3, 54, "IZ150884.cc", 3, 51);
        performTest("IZ150884.cc", 9, 20, "IZ150884.cc", 4, 7);
        performTest("IZ150884.cc", 12, 17, "IZ150884.cc", 3, 9);
        performTest("IZ150884.cc", 14, 39, "IZ150884.cc", 3, 24);
        performTest("IZ150884.cc", 16, 19, "IZ150884.cc", 3, 51);
    }

    public void testIZ151588() throws Exception {
        // IZ#151588 : Unresolved element of array in case of complex index
        performTest("IZ151588.cc", 11, 26, "IZ151588.cc", 3, 5);
    }

    public void testIZ161901() throws Exception {
        // IZ#161901 : unresolved friend class forward
        performTest("IZ161901.cc", 3, 22, "IZ161901.cc", 3, 5);
    }

    public void testIZ169750() throws Exception {
        // IZ#169750 : Unresolved id in the case variable declared in while
        performTest("IZ169750.cc", 5, 37, "IZ169750.cc", 5, 12);
    }

    public void testIZ165961() throws Exception {
        // IZ#165961 : Unresolved ids in construction with macros
        performTest("IZ165961.cc", 9, 27, "IZ165961.cc", 9, 10);
    }

    public void testIZ165976() throws Exception {
        // IZ#165976 : Unresolved array element in case of complicated index
        performTest("IZ165976.cc", 15, 56, "IZ165976.cc", 4, 3);
    }

    public void testIZ173311() throws Exception {
        // IZ#173311 : Unresolved ids in function typedef
        performTest("IZ173311.cc", 2, 30, "IZ173311.cc", 2, 26);
    }

    public void testIZ145071_2() throws Exception {
        // IZ#145071 : forward declarations in function body marked as error
        performTest("IZ145071_2.cc", 2, 12, "IZ145071_2.cc", 2, 5);
        performTest("IZ145071_2.cc", 6, 20, "IZ145071_2.cc", 6, 13);
        performTest("IZ145071_2.cc", 10, 12, "IZ145071_2.cc", 10, 5);
        performTest("IZ145071_2.cc", 11, 20, "IZ145071_2.cc", 10, 5);
    }

    public void testIZ175123() throws Exception {
        // IZ#175123 : Pointer to const parsed incorrectly in some cases
        performTest("IZ175123.cc", 4, 21, "IZ175123.cc", 4, 9);
    }

    public void testStringInMacroParams() throws Exception {
        // Unresolved macro with string in params
        performTest("string_in_macro_params.cc", 7, 31, "string_in_macro_params.cc", 1, 1);
    }

    public void testIZ175877() throws Exception {
        // IZ#175877 : Error at processing #define func(args....)
        performTest("IZ175877.cc", 12, 6, "IZ175877.cc", 5, 3);
    }

    public void testIZ182152() throws Exception {
        // Bug 182152 - variable names in prototypes are unresolved in ide display
        performTest("IZ182152.cc", 3, 66, "IZ182152.cc", 3, 52);
    }

    public void testIZ154779() throws Exception {
        // Bug 154779 - Completion fails on preprocessor statements
        performTest("IZ154779.cc", 12, 10, "IZ154779.cc", 2, 5);
    }

    public void testIZ144535() throws Exception {
        // Bug 144535 - wrong error highlighting for inner structure
        performTest("IZ144535.c", 9, 31, "IZ144535.c", 3, 5);
        performTest("IZ144535.c", 10, 10, "IZ144535.c", 4, 9);
    }

    public void testIZ155577() throws Exception {
        // Bug 155577 - Code Assistance has problems with #include directives in class definitions
        performTest("IZ155577.cc", 8, 12, "IZ155577.h", 2, 1);
    }

    public void testBug190127() throws Exception {
        // Bug 190127 - Extern declarations without return type are not supported
        performTest("bug190127.cpp", 14, 40, "bug190127.cpp", 9, 13);
    }

    public void testBug189838() throws Exception {
        // Bug 189838 - C++ parser complains about function call on temporary object, if * is used in object constructor
        performTest("bug189838.cpp", 12, 32, "bug189838.cpp", 6, 5);
    }

    public void testBug191083() throws Exception {
        // Bug 191083 - Parser errors in fe_tab.h (ir2hf)
        performTest("bug191083.cpp", 4, 20, "bug191083.cpp", 1, 1);
    }

    public void testBug191081() throws Exception {
        // Bug 191081 - Parser errors in opt_set.c (CC)
        performTest("bug191081.cpp", 8, 33, "bug191081.cpp", 2, 9);
    }

    public void testBug191198() throws Exception {
        // #191198 -  Parser error in buf.c
        performTest("bug191198.c", 9, 35, "bug191198.c", 2, 9);
        performTest("bug191198.c", 9, 45, "bug191198.c", 3, 9);
        performTest("bug191198.c", 15, 35, "bug191198.c", 2, 9);
        performTest("bug191198.c", 15, 45, "bug191198.c", 3, 9);        
    }
    
    public void testBug191305() throws Exception {
        performTest("bug191198.c", 22, 40, "bug191198.c", 3, 9);
        performTest("bug191198.c", 23, 26, "bug191198.c", 2, 9);
        performTest("bug191198.c", 23, 40, "bug191198.c", 3, 9);
    }
    
    public void testBug191200() throws Exception {
        // #191200 -  Parser errors in val_tables.c
        performTest("bug191200.c", 14, 25, "bug191200.c", 8, 5);
        performTest("bug191200.c", 16, 25, "bug191200.c", 8, 5);
        performTest("bug191200.c", 23, 25, "bug191200.c", 8, 5);
        performTest("bug191200.c", 15, 25, "bug191200.c", 7, 5);
        performTest("bug191200.c", 22, 25, "bug191200.c", 7, 5);
        performTest("bug191200.c", 24, 25, "bug191200.c", 7, 5);
        performTest("bug191200.c", 24, 15, "bug191200.c", 6, 5);
        performTest("bug191200.c", 17, 15, "bug191200.c", 6, 5);
        performTest("bug191200.c", 15, 40, "bug191200.c", 3, 3);
        performTest("bug191200.c", 16, 40, "bug191200.c", 3, 3);
        performTest("bug191200.c", 23, 40, "bug191200.c", 3, 3);
        performTest("bug191200.c", 24, 40, "bug191200.c", 3, 3);
    }

    public void testBug191314() throws Exception {
        // Bug 191314 - last unresolved identifier in ir2hf
        performTest("bug191314.c", 2, 56, "bug191314.c", 2, 47);
    }

    public void testBug190127$2() throws Exception {
        // Bug 190127 - Extern declarations without return type are not supported
        performTest("bug190127.c", 4, 12, "bug190127.c", 1, 1);
    }

    public void testBug141302() throws Exception {
        // Bug 141302 - Add to keywords C++ alternative tokens
        performTest("bug141302.cpp", 8, 10, "bug141302.cpp", 2, 5);
    }

    public void testBug188925() throws Exception {
        // Bug 188925 - unable to resolve identifier in templates
        performTest("bug188925.cpp", 40, 24, "bug188925.cpp", 26, 13);
    }

    public void testBug159328() throws Exception {
        // Bug 159328 - Unresolved static cast to template
        performTest("bug159328.cpp", 9, 42, "bug159328.cpp", 5, 5);
    }

    public void testBug192967() throws Exception {
        // Bug 192967 - dereference of return type of ternary operator is not resolved
        performTest("bug192967.cpp", 8, 41, "bug192967.cpp", 3, 5);
        performTest("bug192967.cpp", 21, 41, "bug192967.cpp", 14, 5);
    }
        
    public void testBug190885() throws Exception {
        // Bug 190885 - created unittest++ project, unable to resolve identifiers error
        performTest("bug190885.cpp", 4, 10, "bug190885.cpp", 4, 5);
        performTest("bug190885.cpp", 7, 10, "bug190885.cpp", 7, 5);
    }

    public void testBug188305() throws Exception {
        // Bug 188305 - c++ parser complains about struct instance declaration
        performTest("bug188305.cpp", 6, 16, "bug188305.cpp", 6, 7);
    }

    public void testBug76172() throws Exception {
        // Bug 76172 - parser failed on forward function declaration without explicit return type
        performTest("bug76172.cpp", 2, 2, "bug76172.cpp", 2, 1);
        performTest("bug76172.cpp", 3, 2, "bug76172.cpp", 3, 1);
    }

    public void testBug194453() throws Exception {
        // Bug 194453 - Static C Structure initialization incorrectly reports as erroneous syntax
        performTest("bug194453.cpp", 9, 62, "bug194453.cpp", 7, 6);
    }

    public void testBug188270() throws Exception {
        // Bug 188270 - Unable to resolve identifier in nested structs (C)
        performTest("bug188270.cpp", 13, 12, "bug188270.cpp", 2, 5);
    }

    public void testBug189039() throws Exception {
        // Bug 189039 - Unresolved unnamed enum constant in unnamed struct
        performTest("bug189039.cpp", 11, 62, "bug189039.cpp", 4, 5);
    }

    public void testBug195307() throws Exception {
        // Bug 195307 - Unresolved function parameters in function pointer with composed return type
        performTest("bug195307.cpp", 6, 47, "bug195307.cpp", 6, 43);
    }

    public void testBug196966() throws Exception {
        // Bug 196966 - volatile bitfield in structure incorrectly reported as an error
        performTest("bug196966.cpp", 2, 21, "bug196966.cpp", 2, 5);
    }    

    public void testBug151199() throws Exception {
        // Bug 151199 - Unresolved parameter of pointer to function type used as template parameter
        performTest("bug151199.cpp", 5, 26, "bug151199.cpp", 5, 22);
    }    

    public void testBug198823() throws Exception {
        // Bug 198823 - Wrong recognition of function instead of variable
        performTest("bug198823.cpp", 17, 10, "bug198823.cpp", 3, 5);
    }    

    public void testBug200115() throws Exception {
        // Bug 200115 - Unresolved ids on ternary operator
        performTest("bug200115.c", 11, 48, "bug200115.c", 2, 25);
    }    

    public void testBug200140() throws Exception {
        // Bug 200140 - Unresolved ids on ternary operator 2
        performTest("bug200140.c", 43, 30, "bug200140.c", 24, 9);
        performTest("bug200140.c", 44, 82, "bug200140.c", 24, 9);
    }    

    public void testBug200141() throws Exception {
        // Bug 200141 - Unresolved ids in initializers
        performTest("bug200141.c", 9, 41, "bug200141.c", 2, 5);
        performTest("bug200141.c", 10, 41, "bug200141.c", 2, 5);
        performTest("bug200141.c", 11, 41, "bug200141.c", 2, 5);
    }    

    public void testBug201237() throws Exception {
        // Bug 201237 - Regression in CLucene (ternary operator)
        performTest("bug201237.cpp", 19, 63, "bug201237.cpp", 5, 5);
    }    

    public void testBug200675() throws Exception {
        // Bug 200675 - code model fails to see local variables inside some functions generated by macros
        performTest("bug200675.cpp", 4, 9, "bug200675.cpp", 4, 5);
    }    

    public void testBug201237_2() throws Exception {
        // Bug 201237 - Regression in CLucene (ternary operator)
        performTest("bug201237_2.cpp", 15, 44, "bug201237_2.cpp", 3, 5);
    }    
    
    public void testBug202191() throws Exception {
        // Bug #202191  -  incorrect detection of overridden function
        performTest("bug201237_2.cpp", 16, 20, "bug201237_2.cpp", 3, 5);
        performTest("bug201237_2.cpp", 16, 8, "bug201237_2.cpp", 6, 5);
        performTest("bug201237_2.cpp", 17, 8, "bug201237_2.cpp", 9, 5);
    }

    public void testBug207843() throws Exception {
        // Bug 207843 - incorrect find usages result
        performTest("bug207843.cpp", 12, 11, "bug207843.cpp", 12, 5);
    }    
    
    public void testBug210186() throws Exception {
        // Bug 210186 - Unresolved variable like expression declaration
        performTest("bug210186.cpp", 7, 17, "bug210186.cpp", 7, 5);
    }

    public void testBug211265() throws Exception {
        // Bug 211265 -  Typedef has priority on local class
        performTest("bug211265.cpp", 21, 6, "bug211265.cpp", 15, 5);
        performTest("bug211265.cpp", 22, 10, "bug211265.cpp", 16, 9);
        performTest("bug211265.cpp", 10, 9, "bug211265.cpp", 3, 9);
    }

    public void testBug211534() throws Exception {
        // Bug 211534 - Code model does not handle some implicit type conversions
        performTest("bug211534.cpp", 236, 59, "bug211534.cpp", 36, 5);
    }
    
    @Test
    public void testBug211971() throws Exception {
        // Bug 211971 - Incorrect mark occurrences if namespace and class have the same name
        performTest("bug211971.cc", 1, 15, "bug211971.cc", 1, 1);
        performTest("bug211971.cc", 7, 5, "bug211971.cc", 1, 1);
        performTest("bug211971.cc", 7, 15, "bug211971.cc", 2, 3);
        performTest("bug211971.cc", 7, 25, "bug211971.cc", 3, 7);
    }
    
    public void testBug161749() throws Exception {
        // Bug 161749 - problems with restrict, __restrict and __restrict__
        performTest("bug161749.c", 1, 34, "bug161749.c", 1, 19);
        performTest("bug161749.c", 2, 34, "bug161749.c", 2, 19);

        performTest("bug161749.c", 4, 40, "bug161749.c", 4, 19);
        performTest("bug161749.c", 5, 40, "bug161749.c", 5, 19);

        performTest("bug161749.c", 7, 42, "bug161749.c", 7, 19);
        performTest("bug161749.c", 8, 42, "bug161749.c", 8, 19);

        performTest("bug161749.c", 10, 44, "bug161749.c", 10, 19);
        performTest("bug161749.c", 11, 44, "bug161749.c", 11, 19);
    }
    
    public void testBug210996() throws Exception {
        // Bug 210996 - forward class is colored as field
        performTest("bug210996.cpp", 5, 13, "bug210996.cpp", 8, 1);
    }    

    public void testBug200171() throws Exception {
        // Bug 200171 - Unresolved forward class declaration
        performTest("bug200171.c", 2, 17, "bug200171.c", 2, 5);
    }    
    
    public void testTwoMacros() throws Exception {
        // two macros with the same name
        performTest("twoMacros1.h", 1, 9, "twoMacros1.c", 1, 1);
        performTest("twoMacros2.h", 1, 9, "twoMacros2.c", 1, 1);
    }    

    public void testBug216965() throws Exception {
        // Bug 216965 - Unresolved identifier when using constant unsigned indices
        performTest("bug216965.cpp", 8, 26, "bug216965.cpp", 3, 5);
        performTest("bug216965.cpp", 9, 27, "bug216965.cpp", 3, 5);
    }    

    public void testBug220310() throws Exception {
        // Bug 220310 - Incorrect find usages results
        performTest("bug220310.cpp", 4, 39, "bug220310.cpp", 4, 21);
    }    

    public void testBug220680() throws Exception {
        // Bug 220680 - C11 parser error for _Noreturn
        performTest("bug220680.c", 1, 17, "bug220680.c", 1, 1);
    }    

    public void testBug223298() throws Exception {
        // Bug 223298 - Wrong recognition of function
        performTest("bug223298.cpp", 10, 10, "bug223298.cpp", 6, 1);
    }    

    public void testBug223298$2() throws Exception {
        // Bug 223298 - Wrong recognition of function
        performTest("bug223298.c", 10, 10, "bug223298.c", 6, 1);
    }
    
    public void testBug233280() throws Exception {
        // Bug 233280 - Symbol navigation works incorrectly when a function call is passed through a macro
        performTest("bug233280.c", 23, 20, "bug233280.c", 6, 1);
    }
    
    public void testBug233836() throws Exception { 
        // Bug 233836 - Templated typedef breaks resolving in if() statement object declaration
        performTest("bug233836.cpp", 7, 20, "bug233836.cpp", 7, 12);        
    }
    
    public void testBug228953() throws Exception { 
        // Bug 228953 - inaccuracy tests: Python project has unresolved identifiers
        performTest("bug228953.cpp", 7, 18, "bug228953.cpp", 3, 7);
    }    
    
    public void testParensInFunctionParameters() throws Exception { 
        // Bug 228953 - inaccuracy tests: Python project has unresolved identifiers
        performTest("parensInFunctionParameters.cpp", 3, 26, "parensInFunctionParameters.cpp", 3, 21);
        performTest("parensInFunctionParameters.cpp", 5, 43, "parensInFunctionParameters.cpp", 5, 33);
        performTest("parensInFunctionParameters.cpp", 5, 87, "parensInFunctionParameters.cpp", 5, 76);
    }        
    
    public void testBug235462() throws Exception { 
        // Bug 235462 - Wrong navigation to overloaded functions
        performTest("bug235462.cpp", 85, 9, "bug235462.cpp", 15, 5);
        performTest("bug235462.cpp", 86, 9, "bug235462.cpp", 20, 5);
        performTest("bug235462.cpp", 87, 9, "bug235462.cpp", 25, 5);
        performTest("bug235462.cpp", 88, 9, "bug235462.cpp", 30, 5);
        performTest("bug235462.cpp", 89, 9, "bug235462.cpp", 35, 5);        
        performTest("bug235462.cpp", 98, 9, "bug235462.cpp", 39, 5);
        performTest("bug235462.cpp", 99, 9, "bug235462.cpp", 43, 5);
        performTest("bug235462.cpp", 100, 9, "bug235462.cpp", 47, 5);
        performTest("bug235462.cpp", 101, 9, "bug235462.cpp", 51, 5);
        performTest("bug235462.cpp", 103, 9, "bug235462.cpp", 55, 5);
        performTest("bug235462.cpp", 104, 9, "bug235462.cpp", 59, 5);
        performTest("bug235462.cpp", 105, 9, "bug235462.cpp", 63, 5);
        performTest("bug235462.cpp", 106, 9, "bug235462.cpp", 67, 5);
        performTest("bug235462.cpp", 107, 9, "bug235462.cpp", 72, 5);
        performTest("bug235462.cpp", 109, 9, "bug235462.cpp", 25, 5);
    }      
    
    public void testBug239348() throws Exception { 
        // Bug 239348 - Hyperlink to "private" typedef struct does not work
        performTest("bug239348.h", 6, 11, "bug239348.cpp", 5, 3);
        performTest("bug239348.h", 7, 19, "bug239348.cpp", 5, 3);
        performTest("bug239348.h", 7, 22, "bug239348.cpp", 5, 3);
        performTest("bug239348.h", 10, 6, "bug239348.cpp", 5, 3);
    }     
    
    public void testBug238041() throws Exception {
        // Bug 238041 - Function inside a function (in C) breaks parser 
        performTest("bug238041.c", 4, 11, "bug238041.c", 4, 1);
        performTest("bug238041.c", 5, 11, "bug238041.c", 5, 5);
        performTest("bug238041.c", 6, 17, "bug238041.c", 5, 20);
        performTest("bug238041.c", 6, 21, "bug238041.c", 4, 16);
        performTest("bug238041.c", 7, 15, "bug238041.c", 2, 1);
        performTest("bug238041.c", 10, 14, "bug238041.c", 5, 5);
    }
    
    public void testBug240446() throws Exception {
        // Bug 240446 - Wrong priority of local variable. 
        performTest("bug240446.cpp", 20, 22, "bug240446.cpp", 10, 9);
        performTest("bug240446.cpp", 24, 22, "bug240446.cpp", 4, 9);
    }
    
    public void testBug239739() throws Exception {
        // Bug 239739 - regression in inaccuracy tests
        performTest("bug239739.cpp", 12, 12, "bug239739.cpp", 6, 9);
        performTest("bug239739.cpp", 13, 14, "bug239739.cpp", 6, 9);
    }
    
    public void testBug240482() throws Exception {
        // Bug 240482 - parser errors appears if file has 'bool' variable 
        performTest("bug240482.c", 3, 25, "bug240482.c", 3, 16);
    }    
    
    public void testBug242284() throws Exception {
        // Bug 240482 - parser errors appears if file has 'bool' variable 
        performTest("bug242284.cpp", 7, 37, "bug242284.cpp", 5, 17);
        performTest("bug242284.cpp", 13, 37, "bug242284.cpp", 10, 17);
        performTest("bug242284.cpp", 15, 21, "bug242284.cpp", 7, 13);
        performTest("bug242284.cpp", 15, 26, "bug242284.cpp", 13, 13);        
    }    
    
    public void testBug243594() throws Exception {
        performTest("bug243594.cpp", 17, 16, "bug243594.cpp", 4, 9);
    }
    
    public void testBug248502() throws Exception {
        performTest("bug248502.cpp", 24, 27, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 25, 27, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 26, 29, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 27, 28, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 28, 27, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 29, 27, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 29, 27, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 30, 29, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 31, 28, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 32, 48, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 33, 48, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 34, 50, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 35, 49, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 36, 48, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 37, 48, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 38, 50, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 39, 49, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 40, 38, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 41, 38, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 42, 40, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 43, 39, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 44, 38, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 45, 38, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 46, 40, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 47, 39, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 48, 41, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 49, 41, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 50, 43, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 51, 42, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 52, 41, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 53, 41, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 54, 43, "bug248502.cpp", 3, 9);
        performTest("bug248502.cpp", 55, 42, "bug248502.cpp", 3, 9);
    }
    
    public void testBug249752_1() throws Exception {
        performTest("bug249752_1.cpp", 5, 56, "bug249752_1.cpp", 5, 15);
    }
    
    public void testBug246684() throws Exception {
        performTest("bug246684.cpp", 5, 29, "bug246684.cpp", 1, 1);
        performTest("bug246684.cpp", 6, 33, "bug246684.cpp", 1, 1);
        performTest("bug246684.cpp", 7, 39, "bug246684.cpp", 1, 1);
    }
    
    public void testBug255900() throws Exception {
        // Bug 255900 - Unresolved static function in generated header file
        performTest("bug255900.h", 2, 9, "bug255900.cpp", 2, 1);
        performTest("bug255900.h", 3, 9, "bug255900.cpp", 3, 1);
        performTest("bug255900.h", 4, 9, "bug255900.cpp", 1, 1);
    }
    
    public void testBug256296() throws Exception {
        // Bug 256296 - Additional parens in expression break parser
        performTest("bug256296.cpp", 8, 32, "bug256296.cpp", 3, 9);
    }
    
    public void testBug257031() throws Exception {
        // Bug 257031 - Unresolved identifiers in ternary operator (
        performTest("bug257031.cpp", 10, 29, "bug257031.cpp", 2, 20);
        performTest("bug257031.cpp", 10, 34, "bug257031.cpp", 2, 20);
        performTest("bug257031.cpp", 10, 39, "bug257031.cpp", 2, 28);
        performTest("bug257031.cpp", 15, 47, "bug257031.cpp", 14, 13);
        performTest("bug257031.cpp", 15, 52, "bug257031.cpp", 14, 21);
    }
    
    public void testBug257647() throws Exception {        
        // Bug 257647 - C pointer arithmetic and invalid hints in fprintf
        performTest("bug257647.cpp", 10, 12, "bug257647.cpp", 5, 5);
        performTest("bug257647.cpp", 11, 12, "bug257647.cpp", 5, 5);
        performTest("bug257647.cpp", 12, 12, "bug257647.cpp", 5, 5);
        performTest("bug257647.cpp", 13, 12, "bug257647.cpp", 3, 5);
    }
    
    public void testBug258143() throws Exception {
        // Bug 258143 - Printf and double + int
        performTest("bug258143.cpp", 67, 10, "bug258143.cpp", 32, 3);
        performTest("bug258143.cpp", 72, 6, "bug258143.cpp", 4, 3);
        performTest("bug258143.cpp", 73, 6, "bug258143.cpp", 4, 3);
        performTest("bug258143.cpp", 74, 6, "bug258143.cpp", 4, 3);
        performTest("bug258143.cpp", 75, 6, "bug258143.cpp", 41, 3);
        performTest("bug258143.cpp", 76, 6, "bug258143.cpp", 37, 3);
        performTest("bug258143.cpp", 77, 6, "bug258143.cpp", 32, 3);
        performTest("bug258143.cpp", 78, 6, "bug258143.cpp", 32, 3);
        performTest("bug258143.cpp", 79, 6, "bug258143.cpp", 28, 3);
        performTest("bug258143.cpp", 80, 6, "bug258143.cpp", 24, 3);
        performTest("bug258143.cpp", 81, 6, "bug258143.cpp", 24, 3);
        performTest("bug258143.cpp", 82, 6, "bug258143.cpp", 20, 3);
        performTest("bug258143.cpp", 83, 6, "bug258143.cpp", 16, 3);
        performTest("bug258143.cpp", 84, 6, "bug258143.cpp", 16, 3);
        performTest("bug258143.cpp", 85, 6, "bug258143.cpp", 16, 3);
        performTest("bug258143.cpp", 90, 14, "bug258143.cpp", 32, 3);
    }
    
    public void testBug258511() throws Exception {
        // Bug 258511 - Erroneous mismatching argument types "int" conversion specifier "s" hint 
        performTest("bug258511.cpp", 7, 10, "bug258511.cpp", 2, 3);
    }
    
    public void testBug267382() throws Exception {
        // Bug 267382 - Out of class function definition is not recognized under certain conditions
        performTest("bug267382.cpp", 7, 29, "bug267382.cpp", 17, 5);
        performTest("bug267382.cpp", 17, 41, "bug267382.cpp", 7, 13);
        performTest("bug267382.cpp", 15, 40, "bug267382.cpp", 6, 13);
    }
    
    public void testBug267275() throws Exception {
        // Bug 267275 - Не корректное сообщение об ошибке "Невозможно разрешить идентификатор"
        performTest("bug267275.cpp", 9, 44, "bug267275.cpp", 3, 9);
    }
    
    public static class Failed extends HyperlinkBaseTestCase {

        @Override
        protected Class<?> getTestCaseDataClass() {
            return BasicHyperlinkTestCase.class;
        }

        public Failed(String testName) {
            super(testName, true);
        }

    }
    
}
