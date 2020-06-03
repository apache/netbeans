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

import org.netbeans.modules.cnd.modelimpl.trace.TraceModelFileFilter;

/**
 *
 *
 */
public class TemplateSpecializationsTestCase extends HyperlinkBaseTestCase {

    public TemplateSpecializationsTestCase(String testName) {
        super(testName);
    }
    
    @Override
    protected TraceModelFileFilter getTraceModelFileFilter() {
        String simpleName = SimpleFileFilter.testNameToFileName(getName());
        switch (simpleName) {
            case "FriendTemplateFun":
                return new SimpleFileFilter("iz157359"); 
            case "IZ144156_func_spec_tpl_params":
                return new SimpleFileFilter("template_fun_spec"); 
            case "IZ143977_Parm_in_Loki_0":
                return new SimpleFileFilter("iz143977_0"); 
            case "IZ143977_Parm_in_Loki_2":
                return new SimpleFileFilter("iz143977_2"); 
            case "IZ143977_Parm_in_Loki_3":
                return new SimpleFileFilter("iz143977_3"); 
            case "IZ144156_func_partial_spec_pointer":
            case "IZ144156_func_full_spec_char":
            case "IZ144156_func_partial_spec_pair":
            case "IZ144156_func_spec_main":
            case "IZ144156_func_full_spec_pair_char":
                return new SimpleFileFilter("template_fun_spec"); 
            case "IZ103462_1":
                return new SimpleFileFilter("iz103462_first_and_second_1"); 
            default:
                return new SimpleFileFilter(simpleName); 
        }
    }

    public void testFriendTemplateFun() throws Exception {
        // IZ#157359: IDE highlights protected field as wrong
        super.performTest("iz157359.cc", 15, 15, "iz157359.cc", 8, 3);
        super.performTest("iz157359.cc", 15, 25, "iz157359.cc", 8, 3);
    }

    public void testIZ143611_inherited_spec_field() throws Exception {
        performTest("iz143611_inherited_spec_field.cc", 21, 15, "iz143611_inherited_spec_field.cc", 7, 5); // param_t
        performTest("iz143611_inherited_spec_field.cc", 26, 15, "iz143611_inherited_spec_field.cc", 11, 5); // param_int
        performTest("iz143611_inherited_spec_field.cc", 31, 15, "iz143611_inherited_spec_field.cc", 15, 5); // param_char_int
    }
    
    public void testIZ144156_func_partial_spec_pointer() throws Exception {
        performTest("template_fun_spec.cc", 12, 33, "template_fun_spec.cc", 40, 1); // partial spec. for T*
        performTest("template_fun_spec.cc", 40, 33, "template_fun_spec.cc", 12, 1); // and back
    }
    
    public void testIZ144156_func_full_spec_char() throws Exception {
        performTest("template_fun_spec.cc", 18, 26, "template_fun_spec.cc", 50, 1); // full spec. for char
        performTest("template_fun_spec.cc", 50, 26, "template_fun_spec.cc", 18, 1); // and back
    }
    
    public void testIZ143977_Parm_in_Loki_0() throws Exception {
        performTest("iz143977_0.cc", 17, 43, "iz143977_0.cc", 7, 9);
        performTest("iz143977_0.cc", 18, 43, "iz143977_0.cc", 7, 9);
        performTest("iz143977_0.cc", 28, 42, "iz143977_0.cc", 6, 9);
    }
    
    public void testIZ143977_Parm_in_Loki_2() throws Exception {
        performTest("iz143977_2.cc", 9, 36, "iz143977_2.cc", 5, 9);
    }
    
    public void testIZ143977_Parm_in_Loki_3() throws Exception {
        performTest("iz143977_3.cc", 20, 36, "iz143977_3.cc", 8, 9);
        performTest("iz143977_3.cc", 21, 36, "iz143977_3.cc", 12, 9);
    }
    
    public void testIZ103462_1() throws Exception {
        // IZ#103462: Errors in template typedef processing:   'first' and 'second' are missed in Code Completion listbox
        performTest("iz103462_first_and_second_1.cc", 21, 16, "iz103462_first_and_second_1.cc", 3, 5);
    }

    public void testIZ160659() throws Exception {
        // IZ#160659 : Unresolved ids in case of specialization of templated class forward declaration
        performTest("iz160659.cc", 11, 45, "iz160659.cc", 7, 12);
        performTest("iz160659.cc", 25, 51, "iz160659.cc", 21, 16);
    }

    public void testIZ172227() throws Exception {
        // IZ#172227 : Unable to resolve identifier path although code compiles allright
        performTest("iz172227.cc", 14, 9, "iz172227.cc", 2, 5);
        performTest("iz172227.cc", 16, 10, "iz172227.cc", 5, 5);
    }

    public void testBug180828() throws Exception {
        // Bug 180828 : Highlighting bug
        performTest("bug180828.cpp", 7, 44, "bug180828.cpp", 4, 5);
    }

    public void testBug186388() throws Exception {
        // Bug 186388 - Unresolved ids in template specialization function definition
        performTest("bug186388.cpp", 14, 22, "bug186388.cpp", 9, 4);
    }

    public void testBug190668() throws Exception {
        // Bug 190668 - [code model] Lack of support for template specializations
        performTest("bug190668.cpp", 16, 6, "bug190668.cpp", 11, 5);
    }

    public void testBug187258() throws Exception {
        // Bug 187258 - code model does not find template specialization for unsigned type
        performTest("bug187258.cpp", 22, 50, "bug187258.cpp", 14, 5);
        performTest("bug187258.cpp", 21, 45, "bug187258.cpp", 6, 5);
    }

    public void testExplicit_Specializations() throws Exception {
        // Improving specializations
        performTest("explicit_specializations.cpp", 5, 11, "explicit_specializations.cpp", 8, 1);
//        performTest("explicit_specializations.cpp", 8, 69, "explicit_specializations.cpp", 5, 5);
        performTest("explicit_specializations.cpp", 15, 11, "explicit_specializations.cpp", 18, 1);
        performTest("explicit_specializations.cpp", 18, 64, "explicit_specializations.cpp", 15, 5);
        performTest("explicit_specializations.cpp", 22, 56, "explicit_specializations.cpp", 24, 1);
        performTest("explicit_specializations.cpp", 24, 56, "explicit_specializations.cpp", 22, 1);
        
        performTest("explicit_specializations.cpp", 25, 5, "explicit_specializations.cpp", 4, 5);

        performTest("explicit_specializations.cpp", 31, 9, "explicit_specializations.cpp", 24, 1);
        performTest("explicit_specializations.cpp", 34, 9, "explicit_specializations.cpp", 18, 1);
        performTest("explicit_specializations.cpp", 37, 9, "explicit_specializations.cpp", 8, 1);
    }

    public void testIZ144156_func_spec_main() throws Exception {
        performTest("template_fun_spec.cc", 9, 33, "template_fun_spec.cc", 35, 1); // base template
        performTest("template_fun_spec.cc", 35, 33, "template_fun_spec.cc", 9, 1); // and back
    }
    public void testIZ144156_func_partial_spec_pair() throws Exception {
        performTest("template_fun_spec.cc", 15, 33, "template_fun_spec.cc", 45, 1); // partial spec. for pair<T,T>
        performTest("template_fun_spec.cc", 45, 33, "template_fun_spec.cc", 15, 1); // and back
    }
    public void testIZ144156_func_full_spec_pair_char() throws Exception {
        performTest("template_fun_spec.cc", 21, 26, "template_fun_spec.cc", 55, 1); // full spec. for pair<char,char>
        performTest("template_fun_spec.cc", 55, 26, "template_fun_spec.cc", 21, 1); // and back
    }
    
    public void testIZ144156_func_spec_tpl_params() throws Exception {
        performTest("template_fun_spec.cc", 9, 16, "template_fun_spec.cc", 9, 10);
        performTest("template_fun_spec.cc", 9, 36, "template_fun_spec.cc", 9, 10); 
        performTest("template_fun_spec.cc", 35, 16, "template_fun_spec.cc", 35, 10);
        performTest("template_fun_spec.cc", 35, 36, "template_fun_spec.cc", 35, 10); 
    }

    public void testBug185045() throws Exception {
        // Bug 185045 - [code model] Incorrect hyperlink with template specialization function
        performTest("bug185045.cpp", 12, 9, "bug185045.cpp", 7, 1);
    }

    public void testBug196157() throws Exception {
        // Bug 196157 - Template friend functions highlighting problems 
        performTest("bug196157.cpp", 15, 23, "bug196157.cpp", 10, 5);
    }

    public void testBug195283() throws Exception {
        // Bug 195283 - go to jumps to base template instead of specialization
        performTest("bug195283.cpp", 7, 7, "bug195283.cpp", 4, 1);
    }
    
    public void testBug209513() throws Exception {
        // Bug 209513 - a lot of renderer exceptions in log
        performTest("bug209513.cpp", 4, 37, "bug209513.cpp", 2, 9);
    }

    public void testBug210303() throws Exception {
        // Bug 210303 - Unresolved instantiation
        performTest("bug210303.cpp", 8, 7, "bug210303.cpp", 3, 5);
    }    
    
    public void testBug230585() throws Exception {
        // Bug 230585 - Wrong specialization in case of unnamed built-in type
        performTest("bug230585.cpp", 17, 16, "bug230585.cpp", 10, 9);
    }    
    
    public void testBug230589() throws Exception {
        // Bug 230589 - Wrong specialization when constant is used
        performTest("bug230589.cpp", 22, 25, "bug230589.cpp", 14, 9);
    }    
    
    public void testBug234973() throws Exception { 
        // Bug 234973 - Unresolved identifier in specialization
        performTest("bug234973.cpp", 28, 11, "bug234973.cpp", 14, 9);
        performTest("bug234973.cpp", 31, 11, "bug234973.cpp", 23, 9);
    }
    
    public void testBug235399() throws Exception { 
        // Bug 235399 - No deduction of types from template function calls
        performTest("bug235399.cpp", 19, 18, "bug235399.cpp", 3, 7);
        performTest("bug235399.cpp", 20, 23, "bug235399.cpp", 3, 7);
        performTest("bug235399.cpp", 21, 28, "bug235399.cpp", 3, 7);
        
        performTest("bug235399.cpp", 22, 18, "bug235399.cpp", 7, 7);
        performTest("bug235399.cpp", 23, 23, "bug235399.cpp", 7, 7);
        performTest("bug235399.cpp", 24, 28, "bug235399.cpp", 7, 7);
    }    
    
    public void testBug235829() throws Exception {
        // Bug 235829 - Regression on Loki: unresolved identifier in specialization. 
        performTest("bug235829.cpp", 11, 7, "bug235829.cpp", 7, 5);
    }
    
    public void testBug240123() throws Exception {
        // Bug 240123 - Outer definitions of explicit template specializations of functions ruin whole model in a file 
        performTest("bug240123.cpp", 12, 49, "bug240123.cpp", 6, 13);
    }    
    
    public void testBug246332() throws Exception {
        // Bug 246332 - Code model and completion cannot handle function types in template arguments
        performTest("bug246332.cpp", 25, 45, "bug246332.cpp", 7, 9);
        performTest("bug246332.cpp", 26, 53, "bug246332.cpp", 12, 9);
        performTest("bug246332.cpp", 27, 65, "bug246332.cpp", 12, 9);
        performTest("bug246332.cpp", 28, 36, "bug246332.cpp", 7, 9);
        performTest("bug246332.cpp", 29, 38, "bug246332.cpp", 12, 9);
        performTest("bug246332.cpp", 30, 38, "bug246332.cpp", 12, 9);        
    }
    
    public void testBug246332_1() throws Exception {
        // Bug 246332 - Code model and completion cannot handle function types in template arguments
        performTest("bug246332_1.cpp", 26, 15, "bug246332_1.cpp", 12, 9);
        performTest("bug246332_1.cpp", 27, 15, "bug246332_1.cpp", 7, 9);
        performTest("bug246332_1.cpp", 51, 15, "bug246332_1.cpp", 35, 9);
        performTest("bug246332_1.cpp", 52, 15, "bug246332_1.cpp", 35, 9);        
    }    
    
    public void testBug246463() throws Exception {
        // Bug 246463 - Click to specialization navigates to forward declaration
        performTest("bug246463.cpp", 6, 16, "bug246463.cpp", 5, 5);
    }
    
    public void testBug246803() throws Exception {
        // Bug 246803 - Bad support of unnamed template parameters
        performTest("bug246803.cpp", 8, 25, "bug246803.cpp", 2, 26);
        performTest("bug246803.cpp", 14, 14, "bug246803.cpp", 9, 9);
        performTest("bug246803.cpp", 39, 15, "bug246803.cpp", 29, 9);
        performTest("bug246803.cpp", 41, 15, "bug246803.cpp", 34, 9);
        performTest("bug246803.cpp", 43, 15, "bug246803.cpp", 24, 9);
        performTest("bug246803.cpp", 58, 15, "bug246803.cpp", 48, 9);
        performTest("bug246803.cpp", 60, 15, "bug246803.cpp", 53, 9);
    }    
    
    public void testBug246643() throws Exception {
        // Bug 246643 - Exception: SUID: STRUCT CFieldCallback[ 3388:7/123851 - 3405:8/124571 ][ParseDecl.cpp 3388:7-3405:8], STRUCT CFieldCallback[ 3388:7/123851 - 3405:8/124571 ][ParseDecl.cpp 3388:7-3405:8]
        performTest("bug246643.cpp", 18, 47, "bug246643.cpp", 3, 9);
        performTest("bug246643.cpp", 21, 19, "bug246643.cpp", 6, 13);
    }
    
    public void testBug255475() throws Exception {
        // Bug 255475 - inaccuracy tests: regression in LiteSQL
        performTest("bug255475.cpp", 16, 58, "bug255475.cpp", 11, 9);
        performTest("bug255475.cpp", 17, 20, "bug255475.cpp", 6, 9);
    }
    
    public void testBug256700() throws Exception {
        // Bug 256700 - errors in stlport
        performTest("bug256700.cpp", 41, 61, "bug256700.cpp", 8, 11);
        performTest("bug256700.cpp", 42, 14, "bug256700.cpp", 25, 9);
        performTest("bug256700.cpp", 43, 61, "bug256700.cpp", 9, 11);
        performTest("bug256700.cpp", 44, 15, "bug256700.cpp", 25, 9);
    }
    
    public void testBug216095() throws Exception {
        // Bug 216095 - Not able to resolve some identifiers from heavily templated C++ libraries 
        performTest("bug216095.cpp", 18, 14, "bug216095.cpp", 13, 9);
    }
    
    public void testBug262586() throws Exception {
        // Bug 262586 - Warnings in editor (check C++11 STL API) 
        performTest("bug262586.cpp", 64, 17, "bug262586.cpp", 59, 9);
        performTest("bug262586.cpp", 82, 20, "bug262586.cpp", 59, 9);
    }
    
    public void testBug269272() throws Exception {
        // Bug 269272 - Wrong name lookup results when template parameters are omitted
        performTest("bug269272.cpp", 19, 43, "bug269272.cpp", 11, 9);
        performTest("bug269272.cpp", 29, 43, "bug269272.cpp", 11, 9);
        performTest("bug269272.cpp", 16, 21, "bug269272.cpp", 24, 5);
        performTest("bug269272.cpp", 26, 21, "bug269272.cpp", 14, 5);
        performTest("bug269272.cpp", 19, 25, "bug269272.cpp", 14, 5);
        performTest("bug269272.cpp", 29, 29, "bug269272.cpp", 24, 5);
    }
    
    public static class Failed extends HyperlinkBaseTestCase {

        @Override
        protected Class<?> getTestCaseDataClass() {
            return TemplateSpecializationsTestCase.class;
        }

        public void testExplicitSpecializations2() throws Exception {
            // Improving specializations
            performTest("explicit_specializations.cpp", 8, 69, "explicit_specializations.cpp", 5, 5);
        }

        public void testIZ143977_Parm_in_Loki_1() throws Exception {
            performTest("iz143977_1.cc", 45, 33, "iz143977_1.cc", 11, 9);
            performTest("iz143977_1.cc", 46, 33, "iz143977_1.cc", 12, 9);
        }

        public void testIZ143977_Parm_in_Loki_4() throws Exception {
            performTest("iz143977_3.cc", 22, 36, "iz143977_3.cc", 8, 9);
            performTest("iz143977_3.cc", 23, 36, "iz143977_3.cc", 12, 9);
        }

        public Failed(String testName) {
            super(testName, true);
        }
    }
    
}
