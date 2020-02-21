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

import junit.framework.AssertionFailedError;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelFileFilter;

/**
 *
 *155513
 */
public class InstantiationHyperlinkTestCase extends HyperlinkBaseTestCase {
    public InstantiationHyperlinkTestCase(String testName) {
        super(testName, true);
    }
    
    @Override
    protected TraceModelFileFilter getTraceModelFileFilter() {
        String simpleName = SimpleFileFilter.testNameToFileName(getName());
        switch (simpleName) {
            case "CyclicTypedef":
                return new SimpleFileFilter("cyclic_typedef"); 
            case "GccVector":
                return new SimpleFileFilter("iz146697"); 
            default:
                return new SimpleFileFilter(simpleName); 
        }
    }

    public void test159679() throws Exception {
        // IZ159679: regression on Boost: resolver prefers global variable to local typedef
        performTest("iz159679.cpp", 12, 25, "iz159679.cpp", 11, 5);
        performTest("iz159679.cpp", 12, 35, "iz159679.cpp", 3, 5);
        performTest("iz159679.cpp", 12, 40, "iz159679.cpp", 6, 5);
        performTest("iz159679.cpp", 12, 45, "iz159679.cpp", 12, 5);
    }

    public void test154777() throws Exception {
        // IZ154777: Unresolved inner type of specialization
        performTest("iz154777.cpp", 16, 19, "iz154777.cpp", 10, 9); // DD in CC<int>::DD::dType j;
        performTest("iz154777.cpp", 16, 24, "iz154777.cpp", 11, 13); // dType in CC<int>::DD::dType j;
        performTest("iz154777.cpp", 17, 15, "iz154777.cpp", 3, 9); // method in j.method();
    }

    public void testClassForward() throws Exception {
        // IZ144869 : fixed instantiation of class forward declaration
        performTest("classForward.h", 21, 12, "classForward.h", 16, 5);
    }

    public void testCyclicTypedef() throws Exception {
        // IZ148453 : Highlighting thread hangs on boost
        try {
            performTest("cyclic_typedef.cc", 25, 66, "cyclic_typedef.cc", 25, 66);
        } catch (AssertionFailedError e) {
            // it's ok: it won't find: it just shouldn't hang
        }
    }
    
    public void testGccVector() throws Exception {
        // IZ144869 : fixed instantiation of class forward declaration
        performTest("iz146697.cc", 41, 20, "iz146697.cc", 34, 5);
    }

    public void test153986() throws Exception {
        // MYSTL case of IZ#153986: code completion of iterators and of the [] operator
        performTest("iz153986.cc", 18, 15, "iz153986.cc", 9, 9);
        performTest("iz153986.cc", 18, 30, "iz153986.cc", 4, 9);
    }

    public void test159068() throws Exception {
        // IZ#159068 : Unresolved ids in instantiations after &
        performTest("iz159068.cc", 4, 27, "iz159068.cc", 2, 5);
    }

    public void test159054() throws Exception {
        // IZ#159054 : Unresolved id in case of reference to template as return type
        performTest("iz159054.cc", 9, 17, "iz159054.cc", 3, 5);
        performTest("iz159054.cc", 15, 17, "iz159054.cc", 4, 5);
    }

    public void test151194() throws Exception {
        // IZ#151194 : Unresolved template instantiation with template as parameter
        performTest("iz151194.cpp", 32, 12, "iz151194.cpp", 3, 5);
    }

    public void test154792() throws Exception {
        // IZ#154792 : Completion fails on question mark
        performTest("iz154792.cpp", 6, 34, "iz154792.cpp", 2, 5);
    }

    public void test151619() throws Exception {
        // IZ#151619 : completion parser fails on complex template instantiation
        performTest("iz151619.cpp", 6, 14, "iz151619.cpp", 3, 5);
        performTest("iz151619.cpp", 7, 14, "iz151619.cpp", 3, 5);
    }

    public void test147518() throws Exception {
        // IZ#147518 : Code completion issue with template specialization
        performTest("iz147518.cpp", 61, 21, "iz147518.cpp", 41, 5);
        performTest("iz147518.cpp", 49, 17, "iz147518.cpp", 7, 5);
        performTest("iz147518.cpp", 50, 17, "iz147518.cpp", 7, 5);
        performTest("iz147518.cpp", 53, 17, "iz147518.cpp", 25, 5);
        performTest("iz147518.cpp", 54, 17, "iz147518.cpp", 25, 5);
        performTest("iz147518.cpp", 57, 17, "iz147518.cpp", 16, 5);
        performTest("iz147518.cpp", 58, 17, "iz147518.cpp", 16, 5);
    }

    public void test144869() throws Exception {
        // IZ#144869 : pair members are not resolved when accessed via iterator
        performTest("iz144869.cpp", 282, 10, "iz144869.cpp", 81, 7);
        performTest("iz144869.cpp", 283, 10, "iz144869.cpp", 82, 7);
        performTest("iz144869.cpp", 288, 10, "iz144869.cpp", 81, 7);
        performTest("iz144869.cpp", 289, 10, "iz144869.cpp", 82, 7);
        performTest("iz144869.cpp", 294, 10, "iz144869.cpp", 81, 7);
        performTest("iz144869.cpp", 295, 10, "iz144869.cpp", 82, 7);
    }

    public void test144869_2() throws Exception {
        // IZ#144869 : pair members are not resolved when accessed via iterator
        performTest("iz144869_2.cpp", 32, 7, "iz144869_2.cpp", 6, 5);
        performTest("iz144869_2.cpp", 35, 8, "iz144869_2.cpp", 11, 5);
        performTest("iz144869_2.cpp", 38, 8, "iz144869_2.cpp", 16, 5);
        performTest("iz144869_2.cpp", 41, 8, "iz144869_2.cpp", 26, 5);
    }

    public void test161504() throws Exception {
        // IZ#161504 : Unresolved ids in vector wrapper from Loki
        performTest("iz161504.cpp", 248, 14, "iz161504.cpp", 49, 9);
        performTest("iz161504.cpp", 250, 14, "iz161504.cpp", 49, 9);
        performTest("iz161504.cpp", 252, 14, "iz161504.cpp", 49, 9);
    }

    public void test147518_2() throws Exception {
        // IZ#147518 : Code completion issue with template specialisation
        performTest("iz147518_2.cpp", 12, 34, "iz147518_2.cpp", 7, 9);
        performTest("iz147518_2.cpp", 15, 13, "iz147518_2.cpp", 7, 9);
        performTest("iz147518_2.cpp", 24, 12, "iz147518_2.cpp", 7, 9);
        performTest("iz147518_2.cpp", 22, 12, "iz147518_2.cpp", 14, 9);
        performTest("iz147518_2.cpp", 25, 29, "iz147518_2.cpp", 17, 9);
    }

    public void test161875() throws Exception {
        // IZ#161875 : Regression on specializations with forward classes
        performTest("iz161875.cpp", 7, 7, "iz161875.cpp", 3, 5);
    }

    public void test171848() throws Exception {
        // IZ#171848 : Parser does not properly process iterators
        performTest("iz171848.cpp", 13, 9, "iz171848.cpp", 2, 5);
        performTest("iz171848.cpp", 30, 13, "iz171848.cpp", 19, 9);
    }

    public void test172419() throws Exception {
        // IZ#172419 : Model doesn't recognize class declared through the preprocessor
        performTest("iz172419.cpp", 16, 7, "iz172419.cpp", 2, 5);
    }

    public void testIZ144079() throws Exception {
        // Bug 144079 - Hyperlink from type goes to the main template instead of specialization
        performTest("iz144079.cpp", 10, 12, "iz144079.cpp", 5, 1);
        performTest("iz144079.cpp", 11, 12, "iz144079.cpp", 1, 1);
    }

    public void testBug185657() throws Exception {
        // Bug 185657 - Unresolved ids in two dimensional vector usage
        performTest("bug185657.cpp", 854, 25, "bug185657.cpp", 847, 5);
    }
    
    public void testBug199079() throws Exception {
        // Bug 199079 - Unresolved id in case of nested type specialization
        performTest("bug199079.cpp", 24, 7, "bug199079.cpp", 2, 5);
    }
 
    public void testBug209746() throws Exception {
        // Bug 209746 - Unresolved template parameter    
        performTest("bug209746.cpp", 2, 15, "bug209746.cpp", 2, 10);
    }

    public void testBug203374() throws Exception {
        // Bug 203374 - C++ lexer reports unexpected token: template when initializing static member of nested templates
        performTest("bug203374.cpp", 12, 20, "bug203374.cpp", 12, 11);
    }
    
    public void testBug209929() throws Exception {
        // Bug 209929 - Regression on Boost: parser failed on template parameters
        performTest("bug209929.cpp", 2, 11, "bug209929.cpp", 2, 5);
    }
    
    public void testBug209950() throws Exception {
        // Bug 209950 - error: Empty function name
        performTest("bug209950.cpp", 9, 37, "bug209950.cpp", 3, 3);
    }

    public void testBug211983() throws Exception {
        // Bug 211983 - NumberFormatException: For input string: "1U"
        performTest("bug211983.cpp", 10, 7, "bug211983.cpp", 5, 5);
    }

    public void testBug213282() throws Exception {
        // Bug 213282 - template meta programming makes code completion slow
        performTest("bug213282.cpp", 60, 10, "bug213282.cpp", 29, 5);
    }
    
    public void testBug230079() throws Exception {
        // Bug 230079 - Unable to resolve type in case of dereferencing template function return type
        performTest("bug230079.cpp", 14, 40, "bug230079.cpp", 5, 7);
    }
    
    public void testBug230570() throws Exception {
        // Bug 230570 - Wrong specialization in case of indirect template parameters binding
        performTest("bug230570.cpp", 78, 40, "bug230570.cpp", 4, 9);
    }
    
    public void testNestedTemplateEntities() throws Exception {
        // Unexpected tokens and unresolved identifiers in case of deep nested structs
        performTest("deepNestedTemplateEntities.cpp", 21, 21, "deepNestedTemplateEntities.cpp", 21, 11);
        performTest("deepNestedTemplateEntities.cpp", 22, 6, "deepNestedTemplateEntities.cpp", 21, 11);
        
        performTest("deepNestedTemplateEntities.cpp", 21, 67, "deepNestedTemplateEntities.cpp", 21, 57);
        performTest("deepNestedTemplateEntities.cpp", 22, 24, "deepNestedTemplateEntities.cpp", 21, 57);
        
        performTest("deepNestedTemplateEntities.cpp", 26, 21, "deepNestedTemplateEntities.cpp", 26, 11);
        performTest("deepNestedTemplateEntities.cpp", 27, 10, "deepNestedTemplateEntities.cpp", 26, 11);
        
        performTest("deepNestedTemplateEntities.cpp", 26, 67, "deepNestedTemplateEntities.cpp", 26, 57);
        performTest("deepNestedTemplateEntities.cpp", 27, 36, "deepNestedTemplateEntities.cpp", 26, 57);        
    }
    
    public void testBug232530() throws Exception {
        // Bug 232530 - "Empty class specialization name" in messages log
        performTest("bug232530.cpp", 9, 19, "bug232530.cpp", 4, 9);
        
        performTest("bug232530.cpp", 8, 37, "bug232530.cpp", 8, 27);
        performTest("bug232530.cpp", 9, 24, "bug232530.cpp", 8, 27);
        performTest("bug232530.cpp", 9, 34, "bug232530.cpp", 8, 27);
        
        performTest("bug232530.cpp", 9, 39, "bug232530.cpp", 9, 27);
    }
    
    public void testBug235447() throws Exception {
        // Bug 235447 - regression in inaccuracy tests ("variadic template" suite)
        performTest("bug235447.cpp", 9, 28, "bug235447.cpp", 12, 9);
    }    
    
    public void testBug243083() throws Exception {
        // Bug 243083 -  unresolved method after dereferencing template based interator
        performTest("bug243083.cpp", 38, 13, "bug243083.cpp", 29, 9);
        performTest("bug243083.cpp", 39, 16, "bug243083.cpp", 29, 9);
        performTest("bug243083.cpp", 47, 43, "bug243083.cpp", 46, 7);
    }        
    
    public void testBug243083_1() throws Exception {
        // Bug 243083 -  unresolved method after dereferencing template based interator
        performTest("bug243083_1.cpp", 25, 16, "bug243083_1.cpp", 9, 9);
        performTest("bug243083_1.cpp", 27, 16, "bug243083_1.cpp", 14, 9);
        performTest("bug243083_1.cpp", 29, 16, "bug243083_1.cpp", 19, 9);
    }      
    
    public static class Failed extends HyperlinkBaseTestCase {

        @Override
        protected Class<?> getTestCaseDataClass() {
            return InstantiationHyperlinkTestCase.class;
        }

        public Failed(String testName) {
            super(testName);
        }

        public void testBug228146() throws Exception {
            performTest("bug228146.cpp", 20, 5, "bug228146.cpp", 15, 1); // clsS2pubFun in s2.clsS2pubFun();
        }
        
    }    
    
}
