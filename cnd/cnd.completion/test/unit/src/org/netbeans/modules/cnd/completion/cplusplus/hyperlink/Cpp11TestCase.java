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
 */
public class Cpp11TestCase extends HyperlinkBaseTestCase {

    public Cpp11TestCase(String testName) {
        super(testName, true);
    }

    @Override
    protected TraceModelFileFilter getTraceModelFileFilter() {
        String simpleName = SimpleFileFilter.testNameToFileName(getName());
        switch (simpleName) {
            case "ExtEnumDefition212843":
                return new SimpleFileFilter("iz212843");
            case "StronglyTypedEnumerations":
                return new SimpleFileFilter("enum");
            case "RangeBasedForLoop":
                return new SimpleFileFilter("rangefor");
            case "ExtMemberEnumEnumerators212124":
                return new SimpleFileFilter("iz212124");
            default:
                return new SimpleFileFilter(simpleName); 
        }
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.modelimpl.tracemodel.project.name", "DummyProject"); // NOI18N
        System.setProperty("parser.report.errors", "true");
        System.setProperty("antlr.exceptions.hideExpectedTokens", "true");
        System.setProperty("cnd.language.flavor.cpp11", "true");         
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.setProperty("cnd.language.flavor.cpp11", "false");
    }

    public void test229025() throws Exception {
        // #229025 - Editor cannot find static member of rvalue reference specialized template        
        performTest("bug229025.cpp", 8, 20, "bug229025.cpp", 3, 36);
        performTest("bug229025.cpp", 9, 20, "bug229025.cpp", 4, 40);
        performTest("bug229025.cpp", 10, 20, "bug229025.cpp", 5, 41);
    }
    
    public void testAuto() throws Exception {
        performTest("auto.cpp", 14, 14, "auto.cpp", 2, 5);
    }

    public void testStronglyTypedEnumerations() throws Exception {
        performTest("enum.cpp", 8, 34, "enum.cpp", 2, 5);
    }

    public void testRangeBasedForLoop() throws Exception {
        performTest("rangefor.cpp", 4, 9, "rangefor.cpp", 3, 9);
    }

    public void testBug210019() throws Exception {
        // Bug 210019 - Unresolved variadic template parameter
        performTest("bug210019.cpp", 2, 50, "bug210019.cpp", 2, 10);
        performTest("bug210019.cpp", 2, 54, "bug210019.cpp", 2, 18);
    }

    public void testBug210191() throws Exception {
        // Bug 210191 - Unresolved class members in lambdas
        performTest("bug210191.cpp", 7, 28, "bug210191.cpp", 3, 5);
        // Bug 210887 -  regression in inaccuracy tests (dbx projectl)
        performTest("bug210191.cpp", 31, 12, "bug210191.cpp", 26, 9);
        performTest("bug210191.cpp", 27, 20, "bug210191.cpp", 26, 50);
    }
    
    public void testBug210192() throws Exception {
        // Bug 210192 - Unresolved template functions
        performTest("bug210192.cpp", 5, 36, "bug210192.cpp", 1, 1);
    }    
    
    public void testBug210194() throws Exception {
        // Bug 210194 - Unresolved instantiations with function pointers
        performTest("bug210194.cpp", 19, 77, "bug210194.cpp", 6, 3);
        performTest("bug210194.cpp", 20, 82, "bug210194.cpp", 6, 3);
        performTest("bug210194.cpp", 21, 88, "bug210194.cpp", 6, 3);
        performTest("bug210194.cpp", 19, 93, "bug210194.cpp", 16, 3);
        performTest("bug210194.cpp", 20, 98, "bug210194.cpp", 16, 3);
        performTest("bug210194.cpp", 21, 105, "bug210194.cpp", 16, 3);        
    }    
    
    public void testBug210257() throws Exception {
        // Bug 210257 - Ellipsis breaks hyperlink
        performTest("bug210257.cpp", 9, 52, "bug210257.cpp", 6, 3);
    }    
    
    public void testBug210291() throws Exception {
        // Bug 210291 - Unresolved ids in instantiations
        performTest("bug210291.cpp", 13, 59, "bug210291.cpp", 8, 5);
    }

    public void testExtEnumDefition212843() throws Exception {
        // #212843 - external enum declaration can not resolve initializer constants
        performTest("iz212843.cpp", 5, 42, "iz212843.cpp", 2, 12);
        performTest("iz212843.cpp", 6, 16, "iz212843.cpp", 10, 5);
        performTest("iz212843.cpp", 7, 21, "iz212843.cpp", 14, 5);
        performTest("iz212843.cpp", 10, 16, "iz212843.cpp", 6, 9);
        performTest("iz212843.cpp", 11, 25, "iz212843.cpp", 5, 16);
        performTest("iz212843.cpp", 14, 21, "iz212843.cpp", 7, 9);
        performTest("iz212843.cpp", 14, 37, "iz212843.cpp", 2, 12);
        performTest("iz212843.cpp", 14, 46, "iz212843.cpp", 5, 16);
        performTest("iz212843.cpp", 14, 55, "iz212843.cpp", 5, 26);
    }

    public void testExtMemberEnumEnumerators212124() throws Exception {
        // #212124 - C++11 enum forwards
        // correctly resolve externally defined class member forward enums' enumerators
        performTest("iz212124.cpp", 18, 55, "iz212124.cpp", 15, 46);
        performTest("iz212124.cpp", 19, 55, "iz212124.cpp", 15, 67);
        performTest("iz212124.cpp", 20, 55, "iz212124.cpp", 15, 88);
        
        performTest("iz212124.cpp", 22, 55, "iz212124.cpp", 16, 46);
        performTest("iz212124.cpp", 23, 55, "iz212124.cpp", 16, 67);
        performTest("iz212124.cpp", 24, 55, "iz212124.cpp", 16, 88);

        performTest("iz212124.cpp", 25, 45, "iz212124.cpp", 16, 46);
        performTest("iz212124.cpp", 26, 45, "iz212124.cpp", 16, 67);
        performTest("iz212124.cpp", 27, 45, "iz212124.cpp", 16, 88);
    }

    public void testBug210303() throws Exception {
        // Bug 210303 - Unresolved instantiation
        performTest("bug210303.cpp", 18, 11, "bug210303.cpp", 11, 9);
    }

    public void testBug214111() throws Exception {
        // Bug 214111 - No code completion for auto variable
        performTest("bug214111.cpp", 48, 23, "bug214111.cpp", 4, 5);
    }

    public void testBug215662() throws Exception {
        // Bug 215662 - c++11 auto and lambda parameter types not recognized
        performTest("bug215662.cpp", 7, 29, "bug215662.cpp", 7, 17);
        performTest("bug215662.cpp", 7, 96, "bug215662.cpp", 4, 20);
    }
    
    public void testBug218848() throws Exception {
        // Bug 218848 - auto in for loop produces not recognized
        performTest("bug218848.cpp", 256, 15, "bug218848.cpp", 248, 9);
        performTest("bug218848.cpp", 260, 15, "bug218848.cpp", 248, 9);
    }        
    
    public void testBug232383() throws Exception {
        // Bug 232383 - auto const & identifier cannot be resolved
        performTest("bug232383.cpp", 31, 20, "bug232383.cpp", 3, 9);
    }    
    
    public void testBug225611() throws Exception {
        // Bug 225611 - decltype, unable to resolve identifier 
        performTest("bug225611.cpp", 17, 12, "bug225611.cpp", 8, 9);
        
        performTest("bug225611.cpp", 20, 23, "bug225611.cpp", 7, 9);        
        performTest("bug225611.cpp", 21, 12, "bug225611.cpp", 3, 9);
        
        performTest("bug225611.cpp", 23, 27, "bug225611.cpp", 7, 9);        
        performTest("bug225611.cpp", 24, 12, "bug225611.cpp", 3, 9);        
    }  
    
    public void testDecltypes1() throws Exception {
        // Static asserts for decltypes 
        performTest("decltypes1.cpp", 19, 57, "decltypes1.cpp", 11, 5);      
        performTest("decltypes1.cpp", 21, 46, "decltypes1.cpp", 11, 5);
        performTest("decltypes1.cpp", 23, 52, "decltypes1.cpp", 11, 5);
        performTest("decltypes1.cpp", 25, 61, "decltypes1.cpp", 11, 5);
    }
    
    public void testDecltypes2() throws Exception {
        // Static asserts for decltypes 
        performTest("decltypes2.cpp", 14, 56, "decltypes2.cpp", 9, 9);      
    }    
    
    public void testBug224399() throws Exception {
        // Bug 224399 - Error parsing decltype in function arguments 
        performTest("bug224399.cpp", 14, 9, "bug224399.cpp", 4, 5);      
    }
    
    public void testBug224032() throws Exception {
        // Bug 224032 - decltype. Unexpected tokens.
        performTest("bug224032.cpp", 13, 22, "bug224032.cpp", 5, 5);
        performTest("bug224032.cpp", 14, 39, "bug224032.cpp", 7, 3);
        performTest("bug224032.cpp", 17, 32, "bug224032.cpp", 8, 5);
        performTest("bug224032.cpp", 18, 32, "bug224032.cpp", 5, 5);
    }
    
    
    public void testBug234640() throws Exception {
        // Bug 234640 - parser fails on decltype
        performTest("bug234640.cpp", 12, 12, "bug234640.cpp", 3, 9);
    }    
    
    public void testBug235044() throws Exception {
        // Bug 235044 - Unresolved identifier in template type alias (C++11)
        performTest("bug235044.cpp", 35, 12, "bug235044.cpp", 4, 9);
        performTest("bug235044.cpp", 38, 12, "bug235044.cpp", 4, 9);
        performTest("bug235044.cpp", 41, 12, "bug235044.cpp", 4, 9);
        performTest("bug235044.cpp", 44, 12, "bug235044.cpp", 4, 9);
        performTest("bug235044.cpp", 47, 12, "bug235044.cpp", 4, 9);
    }
    
    public void bug235076() throws Exception {
        // Bug 235076 - Unresolved parameters in type alias for function with more than one parameter
        performTest("bug235076.cpp", 3, 34, "bug235076.cpp", 3, 30);
        performTest("bug235076.cpp", 3, 41, "bug235076.cpp", 3, 37);
        performTest("bug235076.cpp", 6, 43, "bug235076.cpp", 5, 15);
        performTest("bug235076.cpp", 9, 31, "bug235076.cpp", 8, 15);
    }
    
    public void bug235229() throws Exception {
        // Bug 235229 - Errors during parse of lambda function call as default parameter 
        performTest("bug235229.cpp", 2, 15, "bug235229.cpp", 2, 5);
        performTest("bug235229.cpp", 2, 27, "bug235229.cpp", 2, 21);
        performTest("bug235229.cpp", 2, 40, "bug235229.cpp", 2, 36);
        performTest("bug235229.cpp", 2, 50, "bug235229.cpp", 2, 36);
    }
    
    public void bug235120() throws Exception {
        // Bug 235120 - SFINAE type deduction failure.
        performTest("bug235120.cpp", 31, 20, "bug235120.cpp", 3, 9);
        performTest("bug235120.cpp", 32, 20, "bug235120.cpp", 7, 9);
        performTest("bug235120.cpp", 33, 20, "bug235120.cpp", 3, 9);
        performTest("bug235120.cpp", 34, 20, "bug235120.cpp", 7, 9);
        performTest("bug235120.cpp", 38, 51, "bug235120.cpp", 3, 9);
        
        performTest("bug235120.cpp", 72, 15, "bug235120.cpp", 43, 9);
        performTest("bug235120.cpp", 74, 15, "bug235120.cpp", 53, 9);
        
        performTest("bug235120.cpp", 93, 14, "bug235120.cpp", 81, 9);
        performTest("bug235120.cpp", 122, 59, "bug235120.cpp", 99, 9);        
        performTest("bug235120.cpp", 123, 27, "bug235120.cpp", 99, 9);
    }
    
    public void bug235120_2() throws Exception {
        // Bug 235120 - SFINAE type deduction failure.
        performTest("bug235120_2.cpp", 53, 17, "bug235120_2.cpp", 7, 9);
        performTest("bug235120_2.cpp", 55, 17, "bug235120_2.cpp", 11, 9);
    }    
    
    public void bug238413() throws Exception {
        // Bug 238413 - C++11 inline namespaces are not supported
        performTest("bug238413.cpp", 12, 24, "bug238413.cpp", 6, 15);
        performTest("bug238413.cpp", 13, 18, "bug238413.cpp", 6, 15);
        performTest("bug238413.cpp", 14, 13, "bug238413.cpp", 6, 15);
        
        performTest("bug238413.cpp", 29, 23, "bug238413.cpp", 21, 15);
        
        performTest("bug238413.cpp", 47, 15, "bug238413.cpp", 36, 19);
        performTest("bug238413.cpp", 49, 15, "bug238413.cpp", 36, 19);
        performTest("bug238413.cpp", 50, 12, "bug238413.cpp", 41, 15);
        performTest("bug238413.cpp", 55, 13, "bug238413.cpp", 45, 7);
    }

    public void bug224031() throws Exception {
        // Bug 224031 - typedef auto Fun(int a) -> decltype(a + a); unexpected token: ( 
        performTest("bug224031.cpp", 3, 19, "bug224031.cpp", 3, 5);
        performTest("bug224031.cpp", 3, 41, "bug224031.cpp", 3, 22);
        performTest("bug224031.cpp", 3, 45, "bug224031.cpp", 3, 22);
           
        performTest("bug224031.cpp", 5, 34, "bug224031.cpp", 5, 14);
        performTest("bug224031.cpp", 5, 38, "bug224031.cpp", 5, 14);

        performTest("bug224031.cpp", 7, 34, "bug224031.cpp", 7, 14);
        performTest("bug224031.cpp", 7, 38, "bug224031.cpp", 7, 14);        
        
        performTest("bug224031.cpp", 13, 16, "bug224031.cpp", 11, 5);        
        performTest("bug224031.cpp", 13, 26, "bug224031.cpp", 13, 5);
        performTest("bug224031.cpp", 13, 46, "bug224031.cpp", 13, 32);
    
        performTest("bug224031.cpp", 15, 45, "bug224031.cpp", 15, 32);        
        performTest("bug224031.cpp", 15, 56, "bug224031.cpp", 15, 51);        
        performTest("bug224031.cpp", 15, 65, "bug224031.cpp", 15, 60);        
    }    
    
    public void bug238913() throws Exception {
        // Bug 238913 - Function types are not supported
        performTest("bug238913.cpp", 20, 14, "bug238913.cpp", 4, 9);
        performTest("bug238913.cpp", 23, 17, "bug238913.cpp", 4, 9);
        performTest("bug238913.cpp", 26, 13, "bug238913.cpp", 4, 9);
        performTest("bug238913.cpp", 29, 13, "bug238913.cpp", 4, 9);
        performTest("bug238913.cpp", 33, 15, "bug238913.cpp", 33, 7);
    }
    
    public void testBug238847_2() throws Exception {
        // Bug 238913 - Unable to deduce type through uniqe_ptr and decltype
        performTest("bug238847_2.cpp", 16, 15, "bug238847_2.cpp", 4, 9);
    }    
    
    public void testBug240723() throws Exception {
        // Bug 240723 - auto return type and const noexcept in C++11 code highlighted as error
        performTest("bug240723_c.cpp", 15, 42, "bug240723_c.cpp", 14, 9);
        performTest("bug240723_c.cpp", 27, 42, "bug240723_c.cpp", 26, 9);
    }        
    
    public void testBug243598() throws Exception {
        // Bug 243598 - C++11 thread_local variables highlighted as Unable to resolve identifier
        performTest("bug243598.cpp", 5, 17, "bug243598.cpp", 4, 9);
    }    
    
    public void testBug243600() throws Exception {
        // Bug 243600 - static_cast<int> do not work as casting to int
        performTest("bug243600.cpp", 14, 31, "bug243600.cpp", 7, 9);
        performTest("bug243600.cpp", 15, 37, "bug243600.cpp", 7, 9);
        performTest("bug243600.cpp", 16, 70, "bug243600.cpp", 7, 9);
        performTest("bug243600.cpp", 17, 50, "bug243600.cpp", 7, 9);
    }        
    
    public void testBug243171() throws Exception {
        // Bug 243171 - No code completion for auto variable
        performTest("bug243171.cpp", 26, 16, "bug243171.cpp", 3, 9);
        performTest("bug243171.cpp", 29, 16, "bug243171.cpp", 3, 9);
        performTest("bug243171.cpp", 32, 16, "bug243171.cpp", 3, 9);
        performTest("bug243171.cpp", 35, 16, "bug243171.cpp", 3, 9);
        performTest("bug243171.cpp", 38, 16, "bug243171.cpp", 3, 9);
    }    
    
    public void testBug244177() throws Exception {
        // Bug 244177 - Unresolved decltype inside decltype
        performTest("bug244177.cpp", 12, 20, "bug244177.cpp", 3, 9);
    }        
    
    public void testBug246349() throws Exception {
        // Bug 246349 - Unresolved symbols inside lamda defined in constructor initializer list
        performTest("bug246349.cpp", 9, 45, "bug246349.cpp", 9, 37);
        performTest("bug246349.cpp", 9, 63, "bug246349.cpp", 9, 37);
    }            
    
    public void testBug247751() throws Exception {
        performTest("bug247751.cpp", 8, 7, "bug247751.cpp", 2, 5);
        performTest("bug247751.cpp", 13, 11, "bug247751.cpp", 2, 5);
        performTest("bug247751.cpp", 14, 11, "bug247751.cpp", 2, 5);
        performTest("bug247751.cpp", 15, 16, "bug247751.cpp", 2, 5);
    }
    
    public void testBug238688() throws Exception {
        performTest("bug238688.cpp", 22, 15, "bug238688.cpp", 3, 9);
        performTest("bug238688.cpp", 24, 15, "bug238688.cpp", 7, 9);
    }    
    
    public void testBug248624() throws Exception {
        performTest("bug248624.cpp", 46, 78, "bug248624.cpp", 42, 9);
        performTest("bug248624.cpp", 47, 77, "bug248624.cpp", 42, 9);
        performTest("bug248624.cpp", 48, 78, "bug248624.cpp", 42, 9);
    }        
    
    public void testBug250270() throws Exception {
        performTest("bug250270.cpp", 7, 11, "bug250270.cpp", 6, 7);
        performTest("bug250270.cpp", 8, 13, "bug250270.cpp", 3, 5);
    }  
    
    public void testBug251181() throws Exception {
        performTest("bug251181.cpp", 10, 31, "bug251181.cpp", 3, 9);
        performTest("bug251181.cpp", 12, 29, "bug251181.cpp", 4, 13);
        performTest("bug251181.cpp", 15, 31, "bug251181.cpp", 3, 9);
        performTest("bug251181.cpp", 16, 22, "bug251181.cpp", 4, 13);
        
        performTest("bug251181.cpp", 57, 26, "bug251181.cpp", 48, 15);
        performTest("bug251181.cpp", 58, 26, "bug251181.cpp", 49, 15);
        performTest("bug251181.cpp", 59, 26, "bug251181.cpp", 50, 15);
        performTest("bug251181.cpp", 60, 26, "bug251181.cpp", 51, 15);
        
        performTest("bug251181.cpp", 79, 26, "bug251181.cpp", 70, 15);
        performTest("bug251181.cpp", 80, 26, "bug251181.cpp", 71, 15);
        performTest("bug251181.cpp", 81, 26, "bug251181.cpp", 72, 15);
        performTest("bug251181.cpp", 82, 26, "bug251181.cpp", 73, 15);
    }  
    
    public void testBug251305() throws Exception {
        // Bug 251305 - StackOverflowError at org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter.UIDtoCsmObject
        performTest("bug251305.cpp", 4, 55, "bug251305.cpp", 4, 24);
        performTest("bug251305.cpp", 4, 61, "bug251305.cpp", 4, 9);
    }
    
    public void testBug249463() throws Exception {
        // Bug 249463 - Code Assistance fails on auto type when used outside of namespace in C++11
        performTest("bug249463.cpp", 17, 25, "bug249463.cpp", 3, 9);
        performTest("bug249463.cpp", 18, 27, "bug249463.cpp", 3, 9);
    }
    
    public void testBug255064() throws Exception {
        // Bug 255064 - Forward strongly typed enum hides its definition
        performTest("bug255064.cpp", 9, 45, "bug255064.cpp", 3, 9);
    }
    
    public void testBug244177_2() throws Exception {
        // Bug 244177 - Unresolved decltype inside decltype
        performTest("bug244177_2.cpp", 4, 30, "bug244177_2.h", 3, 9);
    }
    
    public void testBug256058() throws Exception {
        // Bug 256058 - Unresolved items in editor of C++ Project With Existing Sources
        performTest("bug256058.cpp", 84, 12, "bug256058.cpp", 15, 9);
        performTest("bug256058.cpp", 85, 22, "bug256058.cpp", 22, 13);
        performTest("bug256058.cpp", 88, 12, "bug256058.cpp", 75, 9);
        performTest("bug256058.cpp", 91, 12, "bug256058.cpp", 71, 9);
        performTest("bug256058.cpp", 94, 12, "bug256058.cpp", 38, 13);
        performTest("bug256058.cpp", 97, 12, "bug256058.cpp", 52, 9);
        performTest("bug256058.cpp", 98, 12, "bug256058.cpp", 30, 13);
        performTest("bug256058.cpp", 101, 12, "bug256058.cpp", 38, 13);
        performTest("bug256058.cpp", 104, 12, "bug256058.cpp", 60, 9);
        performTest("bug256058.cpp", 107, 12, "bug256058.cpp", 47, 13);
    }
    
    public void testBug256739() throws Exception {
        // Bug 256739 - IDE cannot find type of (*this) in initializer
        performTest("bug256739.cpp", 15, 25, "bug256739.cpp", 13, 9);
    }
    
    public void testBug257616() throws Exception {
        // Bug 257616 - Lambdas inside if and while statements are not recognized
        performTest("bug257616.cpp", 9, 46, "bug257616.cpp", 9, 30);
        performTest("bug257616.cpp", 10, 51, "bug257616.cpp", 10, 34);
        performTest("bug257616.cpp", 10, 56, "bug257616.cpp", 8, 9);
    }
    
    public void testBug247031() throws Exception {
        // Bug 247031 - Cannot resolve identifier if uniform initialization syntax is used
        performTest("bug247031.cpp", 43, 39, "bug247031.cpp", 5, 9);
        performTest("bug247031.cpp", 44, 16, "bug247031.cpp", 5, 9);
        performTest("bug247031.cpp", 45, 31, "bug247031.cpp", 5, 9);
        performTest("bug247031.cpp", 46, 34, "bug247031.cpp", 11, 9);
        performTest("bug247031.cpp", 47, 59, "bug247031.cpp", 5, 9);
        performTest("bug247031.cpp", 47, 81, "bug247031.cpp", 5, 9);
        performTest("bug247031.cpp", 48, 25, "bug247031.cpp", 5, 9);
        performTest("bug247031.cpp", 49, 27, "bug247031.cpp", 5, 9);
        performTest("bug247031.cpp", 51, 22, "bug247031.cpp", 17, 13);
        performTest("bug247031.cpp", 52, 37, "bug247031.cpp", 17, 13);
        performTest("bug247031.cpp", 54, 22, "bug247031.cpp", 5, 9);
        performTest("bug247031.cpp", 55, 41, "bug247031.cpp", 11, 9);
        performTest("bug247031.cpp", 56, 48, "bug247031.cpp", 25, 9);
        performTest("bug247031.cpp", 57, 32, "bug247031.cpp", 5, 9);
    }
    
    public void testBug269199() throws Exception {
        // Bug 269199 - Editor ignore C++11 standard in standard headers
        performTest("bug269199.cpp", 10, 18, "bug269199.cpp", 3, 9);
        performTest("bug269199.cpp", 11, 21, "bug269199.cpp", 3, 9);
    }
    
    public void testBug268930() throws Exception {
        // Bug 268930 - C++11: user-defined literals
        performTest("bug268930_hyperlink.cpp", 46, 13, "bug268930_hyperlink.cpp", 34, 5);
        performTest("bug268930_hyperlink.cpp", 47, 15, "bug268930_hyperlink.cpp", 38, 5);
        performTest("bug268930_hyperlink.cpp", 49, 20, "bug268930_hyperlink.cpp", 13, 5);
        performTest("bug268930_hyperlink.cpp", 50, 20, "bug268930_hyperlink.cpp", 17, 5);
        performTest("bug268930_hyperlink.cpp", 51, 20, "bug268930_hyperlink.cpp", 22, 5);
        performTest("bug268930_hyperlink.cpp", 53, 28, "bug268930_hyperlink.cpp", 26, 5);
        performTest("bug268930_hyperlink.cpp", 54, 28, "bug268930_hyperlink.cpp", 30, 5);
        performTest("bug268930_hyperlink.cpp", 55, 13, "bug268930_hyperlink.cpp", 42, 5);
        performTest("bug268930_hyperlink.cpp", 56, 13, "bug268930_hyperlink.cpp", 43, 5);
        performTest("bug268930_hyperlink.cpp", 57, 13, "bug268930_hyperlink.cpp", 42, 5);
        performTest("bug268930_hyperlink.cpp", 56, 27, "bug268930_hyperlink.cpp", 9, 5);
        performTest("bug268930_hyperlink.cpp", 57, 27, "bug268930_hyperlink.cpp", 9, 5);
        performTest("bug268930_hyperlink.cpp", 57, 35, "bug268930_hyperlink.cpp", 6, 9);
        
        performTest("bug268930_hyperlink.cpp", 65, 13, "bug268930_hyperlink.cpp", 43, 5);
        performTest("bug268930_hyperlink.cpp", 66, 13, "bug268930_hyperlink.cpp", 42, 5);
        
        performTest("bug268930_hyperlink.cpp", 49, 11, "bug268930_hyperlink.cpp", 48, 9);
        performTest("bug268930_hyperlink.cpp", 50, 11, "bug268930_hyperlink.cpp", 48, 9);
        performTest("bug268930_hyperlink.cpp", 51, 11, "bug268930_hyperlink.cpp", 48, 9);
        performTest("bug268930_hyperlink.cpp", 54, 13, "bug268930_hyperlink.cpp", 52, 9);
    }
    
    public void testBug268930_adjacent() throws Exception {
        // Bug 268930 - C++11: user-defined literals
        performTest("bug268930_adjacent.cpp", 3, 20, "bug268930_adjacent.cpp", 1, 1);
    }
}
