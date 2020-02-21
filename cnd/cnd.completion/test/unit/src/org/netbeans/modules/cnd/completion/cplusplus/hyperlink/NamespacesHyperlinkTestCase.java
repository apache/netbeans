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
public class NamespacesHyperlinkTestCase extends HyperlinkBaseTestCase {

    public NamespacesHyperlinkTestCase(String testName) {
        super(testName, true);
        //System.setProperty("cnd.modelimpl.trace.registration", "true");
    }
    
    @Override
    protected TraceModelFileFilter getTraceModelFileFilter() {
        String simpleName = SimpleFileFilter.testNameToFileName(getName());
        switch (simpleName) {
            case "228949_BaseClassFromAnotherNS":
                return new SimpleFileFilter("bug228949"); 
            case "UsingNS1":
            case "UsingNS1S2":
            case "UsingDirectivesS1":
            case "UsingDirectivesS1S2":
            case "UsingCout":
            case "UsingNS2":
            case "UsingDirectivesS2":
            case "NestedTypesOfTemplatedClass":
            case "MainDefFQN":
            case "UsingNS2_2":
                return new SimpleFileFilter("main", "file"); 
            case "UsingDirectives":
                return new SimpleFileFilter("main"); 
            case "S1FooDefS1Decls":
            case "S2BooDefS1Decls":
            case "S1FooDefS2":
            case "S2BooDefS2Decls":
            case "DeclsFromHeader":
            case "ClassS1":
            case "ClassS2":
            case "S2BooDefFQN":
            case "S1FooDefFQN":
                return new SimpleFileFilter("file"); 
            case "ScopeInTypeAfterConst":
                return new SimpleFileFilter("boost_in_type_after_scope"); 
            case "Bug219398":
            case "TypeIdName":
                return new SimpleFileFilter("typeid", "typeinfo"); 
            case "228949_UsingNsInOtherHeader":
            case "228949_DeferWhenUsingNsInOtherHeader":
                return new SimpleFileFilter("bug228949"); 
            case "UsingInOtherNsDef":
                return new SimpleFileFilter("using_in_ns"); 
            case "ChildNamespaces":
            case "ChildNamespaces2":
            case "ChildNamespaces3":
                return new SimpleFileFilter("child_ns"); 
            case "DoubleUsing":
                return new SimpleFileFilter("iz207841"); 
            default:
                return new SimpleFileFilter(simpleName); 
        }
    }

    public void test228949_UsingNsInOtherHeader() throws Exception {
        // #228949 - inaccuracy tests: Clucene project has unresolved identifiers 
        performTest("bug228949.cpp", 16, 10, "bug228949StdHeader.h", 16, 5);
        performTest("bug228949.h", 20, 10, "bug228949StdHeader.h", 16, 5);
        performTest("bug228949.h", 25, 10, "bug228949StdHeader.h", 16, 5);
        performTest("bug228949.h", 20, 20, "bug228949StdHeader.h", 12, 5);
        performTest("bug228949.h", 25, 20, "bug228949StdHeader.h", 12, 5);
    }
    
    public void test228949_DeferWhenUsingNsInOtherHeader() throws Exception {
        // #228949 - inaccuracy tests: Clucene project has unresolved identifiers 
        // size
        performTest("bug228949.cpp", 17, 20, "bug228949StdHeader.h", 20, 9);
        performTest("bug228949.cpp", 23, 17, "bug228949StdHeader.h", 20, 9);
        performTest("bug228949.h", 30, 28, "bug228949StdHeader.h", 20, 9);
        
        // get
        performTest("bug228949.cpp", 23, 10, "bug228949.h", 25, 9);
        // length
        performTest("bug228949.h", 30, 40, "bug228949StdHeader.h", 13, 9);
        performTest("bug228949.cpp", 21, 20, "bug228949StdHeader.h", 13, 9);
    }
    
    public void test228949_BaseClassFromAnotherNS() throws Exception {
        // str
        performTest("bug228949.h", 35, 27, "bug228949.h", 13, 9);
        performTest("bug228949.cpp", 29, 18, "bug228949.h", 13, 9);
        
        // BBB228949
        performTest("bug228949.cpp", 28, 10, "bug228949.h", 12, 5);
        performTest("bug228949.h", 22, 30, "bug228949.h", 12, 5);
        performTest("bug228949.h", 34, 18, "bug228949.h", 12, 5);
    }
    
    public void test228950() throws Exception {
        // #228950 - inaccuracy tests: LiteSQL project has unresolved identifiers
        
        // size()
        performTest("iz228950.cpp", 10, 20, "bug228950_Included.h", 6, 9); 
        performTest("iz228950.cpp", 19, 15, "bug228950_Included.h", 6, 9); 
        // Field::typeOther
        performTest("iz228950.cpp", 9, 10, "bug228950_Included.h", 10, 5); 
        performTest("iz228950.cpp", 9, 20, "bug228950_Included.h", 12, 9); 
        // Field::typeOther
        performTest("iz228950.cpp", 18, 9, "bug228950_Included.h", 10, 5); 
        performTest("iz228950.cpp", 18, 15, "bug228950_Included.h", 12, 9); 
        // main3()
        performTest("iz228950.cpp", 19, 25, "iz228950.cpp", 8, 5); 
    }
    
    public void testDoubleUsing() throws Exception {
        // #207841 - double "using namespace" breaks code model
        performTest("iz207841.cpp", 27, 7, "iz207841.cpp", 13, 9); 
        performTest("iz207841.cpp", 31, 7, "iz207841.cpp", 18, 13); 
        performTest("iz207841.cpp", 36, 12, "iz207841.cpp", 26, 1); 
        performTest("iz207841.cpp", 42, 10, "iz207841.cpp", 30, 1); 
        performTest("iz207841.cpp", 44, 10, "iz207841.cpp", 26, 1); 
    }
    
    public void testScopeInTypeAfterConst() throws Exception {
        performTest("boost_in_type_after_scope.cpp", 14, 15, "boost_in_type_after_scope.cpp", 1, 1);
        performTest("boost_in_type_after_scope.cpp", 14, 25, "boost_in_type_after_scope.cpp", 2, 5);
        performTest("boost_in_type_after_scope.cpp", 14, 35, "boost_in_type_after_scope.cpp", 3, 9);
    }

    public void testTypeIdName() throws Exception {
        // IZ#162160: typeid(obj).name is not resolved
        performTest("typeid.cpp", 24, 25, "typeinfo.h", 21, 5);
        performTest("typeid.cpp", 25, 30, "typeinfo.h", 21, 5);
        performTest("typeid.cpp", 25, 40, "typeinfo.h", 21, 5);
    }

    public void testUsingInOtherNsDef() throws Exception {
        // IZ#159223: Unresolved ids from namespace with usings
        performTest("using_in_ns.cpp", 11, 24, "using_in_ns.cpp", 3, 9); // AA in struct B : public AA {
        performTest("using_in_ns.cpp", 11, 24, "using_in_ns.cpp", 3, 9); // TTT in TTT t;
        // IZ#159308: Unresolved using of using in nested namespace
        performTest("using_in_ns.cpp", 24, 20, "using_in_ns.cpp", 18, 9); // AAA in using N2::AAA;
        performTest("using_in_ns.cpp", 26, 15, "using_in_ns.cpp", 18, 9); // AAA in AAA a;
    }
    
    public void testS1FooDefFQN() throws Exception {
        performTest("file.cc", 9, 10, "file.cc", 4, 1); // S1 in S1::foo();
        performTest("file.cc", 9, 14, "file.cc", 7, 5); // foo in S1::foo();
        performTest("file.cc", 10, 10, "file.cc", 4, 1); // S1 in S1::var1();
        performTest("file.cc", 10, 14, "file.cc", 5, 5); // var1 in S1::var1;

        performTest("file.cc", 14, 10, "file.cc", 4, 1); // S1 in S1::S2::boo();
        performTest("file.cc", 14, 14, "file.cc", 20, 5); // S2 in S1::S2::boo();
        performTest("file.cc", 14, 18, "file.cc", 23, 9); // boo in S1::S2::boo();
        performTest("file.cc", 15, 10, "file.cc", 4, 1); // S1 in S1::S2::var2();
        performTest("file.cc", 15, 14, "file.cc", 20, 5); // S2 in S1::S2::var2();
        performTest("file.cc", 15, 18, "file.cc", 21, 9); // var2 in S1::S2::var2();
    }

    public void testS2BooDefFQN() throws Exception {
        performTest("file.cc", 25, 14, "file.cc", 4, 1); // S1 in S1::foo();
        performTest("file.cc", 25, 18, "file.cc", 7, 5); // foo in S1::foo();
        performTest("file.cc", 26, 14, "file.cc", 4, 1); // S1 in S1::var1();
        performTest("file.cc", 26, 18, "file.cc", 5, 5); // var1 in S1::var1;

        performTest("file.cc", 30, 14, "file.cc", 4, 1); // S1 in S1::S2::boo();
        performTest("file.cc", 30, 18, "file.cc", 20, 5); // S2 in S1::S2::boo();
        performTest("file.cc", 30, 22, "file.cc", 23, 9); // boo in S1::S2::boo();
        performTest("file.cc", 31, 14, "file.cc", 4, 1); // S1 in S1::S2::var2;
        performTest("file.cc", 31, 18, "file.cc", 20, 5); // S2 in S1::S2::var2;
        performTest("file.cc", 31, 22, "file.cc", 21, 9); // var2 in S1::S2::var2;
    }

    public void testMainDefFQN() throws Exception {
        performTest("main.cc", 6, 6, "file.cc", 4, 1); // S1 in S1::foo();
        performTest("main.cc", 6, 10, "file.cc", 7, 5); // foo in S1::foo();
        performTest("main.cc", 7, 6, "file.cc", 4, 1); // S1 in S1::var1();
        performTest("main.cc", 7, 10, "file.cc", 5, 5); // var1 in S1::var1;

        performTest("main.cc", 8, 6, "file.cc", 4, 1); // S1 in S1::S2::boo();
        performTest("main.cc", 8, 10, "file.cc", 20, 5); // S2 in S1::S2::boo();
        performTest("main.cc", 8, 14, "file.cc", 23, 9); // boo in S1::S2::boo();
        performTest("main.cc", 9, 6, "file.cc", 4, 1); // S1 in S1::S2::var2;
        performTest("main.cc", 9, 10, "file.cc", 20, 5); // S2 in S1::S2::var2;
        performTest("main.cc", 9, 14, "file.cc", 21, 9); // var2 in S1::S2::var2;
    }

    public void testS1FooDefS1Decls() throws Exception {
        performTest("file.cc", 11, 10, "file.cc", 7, 5); // foo();
        performTest("file.cc", 12, 10, "file.cc", 5, 5); // var1
    }

    public void testS2BooDefS1Decls() throws Exception {
        performTest("file.cc", 27, 14, "file.cc", 7, 5); // foo();
        performTest("file.cc", 28, 14, "file.cc", 5, 5); // var1
    }

    public void testS1FooDefS2() throws Exception {
        performTest("file.cc", 16, 10, "file.cc", 20, 5); // S2 in S2::boo();
        performTest("file.cc", 16, 14, "file.cc", 23, 9); // boo in S2::boo();
        performTest("file.cc", 17, 10, "file.cc", 20, 5); // S2 in S2::var2
        performTest("file.cc", 17, 14, "file.cc", 21, 9); // var2 in S2::var2
    }

    public void testS2BooDefS2Decls() throws Exception {
        performTest("file.cc", 32, 14, "file.cc", 20, 5); // S2 in S2::boo();
        performTest("file.cc", 32, 18, "file.cc", 23, 9); // boo in S2::boo();
        performTest("file.cc", 33, 14, "file.cc", 20, 5); // S2 in S2::var2
        performTest("file.cc", 33, 18, "file.cc", 21, 9); // var2 in S2::var2
        performTest("file.cc", 34, 14, "file.cc", 23, 9); // boo
        performTest("file.cc", 35, 14, "file.cc", 21, 9); // var2
    }

    public void testDeclsFromHeader() throws Exception {
        performTest("file.h", 6, 17, "file.cc", 5, 5); // extern int var1;
        performTest("file.h", 7, 11, "file.cc", 7, 5); // void foo();
        performTest("file.h", 9, 22, "file.cc", 21, 9); // extern int var2;
        performTest("file.h", 10, 15, "file.cc", 23, 9); // void boo();
    }

    public void testClassS1() throws Exception {
        performTest("file.cc", 39, 14, "file.h", 18, 5); // clsS1 s1;
        performTest("file.cc", 40, 20, "file.cc", 59, 5); // clsS1pubFun in s1.clsS1pubFun();
        performTest("file.cc", 52, 10, "file.h", 18, 5); // clsS1 s1;
        performTest("file.cc", 53, 15, "file.cc", 59, 5); // clsS1pubFun in s1.clsS1pubFun();
        performTest("file.cc", 59, 14, "file.h", 18, 5); // clsS1 in void clsS1::clsS1pubFun() {
        performTest("file.cc", 59, 20, "file.h", 20, 9); // clsS1pubFun in void clsS1::clsS1pubFun() {
        performTest("file.h", 20, 20, "file.cc", 59, 5); // void clsS1pubFun();
    }

    public void testClassS2() throws Exception {
        performTest("file.cc", 42, 14, "file.h", 12, 9); // clsS2 s2;
        performTest("file.cc", 43, 20, "file.cc", 46, 9); // clsS2pubFun in s2.clsS2pubFun();
        performTest("file.cc", 55, 14, "file.h", 12, 9); // clsS2 s2;
        performTest("file.cc", 46, 18, "file.h", 12, 9); // clsS2 in void clsS2::clsS2pubFun() {
        performTest("file.cc", 46, 25, "file.h", 14, 13); // clsS2pubFun in void clsS2::clsS2pubFun() {
        performTest("file.h", 14, 25, "file.cc", 46, 9); // void clsS2pubFun();
    }

    public void testUnnamed() throws Exception {
        performTest("unnamed.cc", 5, 6, "unnamed.h", 16, 5);//    funFromUnnamed();
        performTest("unnamed.cc", 6, 6, "unnamed.h", 11, 5);//    unnamedAInt = 10;
        performTest("unnamed.cc", 7, 6, "unnamed.h", 7, 5);//    ClUnnamedA in ClUnnamedA cl;
        performTest("unnamed.cc", 8, 10, "unnamed.h", 9, 9);//    funFromClassA in cl.funFromClassA();
        performTest("unnamed.cc", 9, 6, "unnamed.h", 13, 5);//    funDefFromUnnamed();

        performTest("unnamed.h", 6, 12, "unnamed.h", 16, 5);//    void funDefFromUnnamed();
    }

    public void testUsingNS1() throws Exception {
        performTest("main.cc", 15, 6, "file.cc", 5, 5); //var1 = 10;
        performTest("main.cc", 16, 6, "file.cc", 7, 5); //foo();
        performTest("main.cc", 17, 6, "file.h", 18, 5); //clsS1 in clsS1 c1;
        performTest("main.cc", 18, 10, "file.cc", 59, 5); //clsS1pubFun in c1.clsS1pubFun();
    }

    public void testUsingNS1S2() throws Exception {
        performTest("main.cc", 23, 6, "file.cc", 21, 9); //var2 = 10;
        performTest("main.cc", 24, 6, "file.cc", 23, 9); //boo();
        performTest("main.cc", 25, 6, "file.h", 12, 9); //clsS2 in clsS2 c2;
        performTest("main.cc", 26, 10, "file.cc", 46, 9); //clsS2pubFun in c2.clsS2pubFun();
    }

    public void testUsingDirectivesS1() throws Exception {
        performTest("main.cc", 31, 6, "file.h", 18, 5); //clsS1 in clsS1 c1;
        performTest("main.cc", 33, 6, "file.cc", 5, 5); //var1 = 10;
        performTest("main.cc", 35, 6, "file.cc", 7, 5); //foo();
    }

    public void testUsingDirectivesS1S2() throws Exception {
        performTest("main.cc", 40, 6, "file.h", 12, 9); //clsS2 in clsS2 c2;
        performTest("main.cc", 42, 6, "file.cc", 21, 9); //var2 = 10;
        performTest("main.cc", 44, 6, "file.cc", 23, 9); //boo();
    }

    public void testUsingCout() throws Exception {
        performTest("main.cc", 69, 10, "file.cc", 63, 5); //myCout in S1::myCout;
        performTest("main.cc", 70, 20, "file.cc", 63, 5); //myCout in using S1::myCout;
        performTest("main.cc", 71, 6, "file.cc", 63, 5); //myCout;
    }

    public void testUsingNS2() throws Exception {
        // IZ#106772: incorrect resolving of using directive
        performTest("main.cc", 51, 6, "file.cc", 21, 9); //var2 = 10;
        performTest("main.cc", 52, 6, "file.cc", 23, 9); //boo();
        performTest("main.cc", 53, 6, "file.h", 12, 9); //clsS2 in clsS2 c2;
    }

    public void testUsingDirectivesS2() throws Exception {
        // IZ#106772: incorrect resolving of using directive
        performTest("main.cc", 61, 6, "file.h", 12, 9); //clsS2 in clsS2 c2;
        performTest("main.cc", 63, 6, "file.cc", 21, 9); //var2 = 10;
        performTest("main.cc", 65, 6, "file.cc", 23, 9); //boo();
    }

    public void testNestedTypesOfTemplatedClass() throws Exception {
        // IZ#135999: string:: code completion doesn't work
        performTest("main.cc", 75, 20, "file.h", 26, 9);
        performTest("main.cc", 77, 15, "file.h", 26, 9);
    }

    public void testUsingDirectives() throws Exception {
        // IZ#144982: std class members are not resolved in litesql
        performTest("main.cc", 94, 12, "main.cc", 83, 9);
        performTest("main.cc", 102, 13, "main.cc", 83, 9);
    }

    public void testChildNamespaces() throws Exception {
        // IZ#145148: forward class declaration is not replaced by real declaration in some cases
        performTest("child_ns.cc", 9, 12, "child_ns.cc", 4, 13);
    }

    public void testChildNamespaces2() throws Exception {
        // IZ#145148: forward class declaration is not replaced by real declaration in some cases
        performTest("child_ns.cc", 18, 16, "child_ns.cc", 15, 20);
    }
    
    
    public void testChildNamespaces3() throws Exception {
        // IZ 145142 : unable to resolve declaration imported from child namespace
        performTest("child_ns.cc", 38, 17, "child_ns.cc", 30, 5);
        performTest("child_ns.cc", 39, 17, "child_ns.cc", 30, 5);
        performTest("child_ns.cc", 40, 17, "child_ns.cc", 25, 5);
        performTest("child_ns.cc", 41, 17, "child_ns.cc", 25, 5);

        performTest("child_ns.cc", 55, 23, "child_ns.cc", 50, 9);
    }
    
    public void testIZ145071() throws Exception {
        // IZ#145071 : forward declarations marked as error
        performTest("IZ145071.cc", 3, 21, "IZ145071.cc", 3, 13);
    }

    public void testIZ155148() throws Exception {
        // IZ#155148: Unresolved namespace alias
        performTest("iz155148.cc", 12, 13, "iz155148.cc", 5, 5);
    }
    
    public void testIZ145142() throws Exception {
        // IZ#145142 : unable to resolve declaration imported from child namespace
        performTest("iz145142.cc", 16, 35, "iz145142.cc", 4, 13);
    }

    public void testIZ150915() throws Exception {
        // IZ#150915 : Unresolved duplicate of a static function
        performTest("iz150915_2.cc", 2, 26, "iz150915_2.cc", 2, 5);
        performTest("iz150915_2.cc", 8, 25, "iz150915_2.cc", 2, 5);
        // File iz150915_1.cc is not referenced here, but it is important part of the test.
        // We make sure that hyperlink does not link to iz150915_1.cc.
    }

    public void testIZ159242() throws Exception {
        // IZ#159242 : Unresolved using of variable from unnamed namespace
        performTest("iz159242.cc", 33, 7, "iz159242.cc", 19, 9);
    }

    public void testIZ159243() throws Exception {
        // IZ#159243 : Unresolved usage of id from namespace alias
        performTest("iz159243.cc", 10, 6, "iz159243.cc", 3, 9);
        performTest("iz159243.cc", 25, 7, "iz159243.cc", 17, 13);
    }

    public void testIZ172596() throws Exception {
        // IZ#172596 : variable highlighted as unused and also undefined
        performTest("iz172596.cc", 17, 35, "iz172596.cc", 6, 21);
        performTest("iz172596.cc", 37, 27, "iz172596.cc", 26, 9);
    }

    public void testBug201811() throws Exception {
        // Bug 201811 - Code assistance unabled to parse boost::tr1::shared_ptr references
        performTest("bug201811.cpp", 24, 8, "bug201811.cpp", 2, 5);
        performTest("bug201811.cpp", 25, 8, "bug201811.cpp", 4, 9);
    }

    public void testBug199689() throws Exception {
        // Bug 199689 - Code assist shows wrong warning when defining a class with Namespace::Class style
        performTest("bug199689.cpp", 2, 18, "bug199689.cpp", 5, 1);
    }
    
    public void testUsingNS2_2() throws Exception {
        performTest("main.cc", 54, 10, "file.cc", 46, 9); //clsS2pubFun in c2.clsS2pubFun();
    }
        
    public void testBug219546() throws Exception {
        // Bug 219546 - using directive with leading :: not working
        performTest("bug219546.cpp", 19, 8, "bug219546.cpp", 8, 5);
    }

    public void testBug220614() throws Exception {
        // Bug 220614 - Wrong unable to resolve identifier indication - unnamed namespaces
        performTest("bug220614.cpp", 15, 21, "bug220614.cpp", 9, 5);
        performTest("bug220614.cpp", 16, 21, "bug220614.cpp", 3, 5);
        performTest("bug220614.cpp", 22, 34, "bug220614.cpp", 13, 5);
    }
    
    public void testBug226516() throws Exception {
        // Bug 226516 - Problem with namespace nesting and navigation between them
        performTest("bug226516.cpp", 13, 42, "bug226516.cpp", 2, 1);
        performTest("bug226516.cpp", 13, 58, "bug226516.cpp", 3, 1);
        performTest("bug226516.cpp", 18, 39, "bug226516.cpp", 13, 5);
        
        performTest("bug226516.cpp", 18, 39, "bug226516.cpp", 13, 5);
        performTest("bug226516.cpp", 18, 55, "bug226516.cpp", 5, 9);
        
        performTest("bug226516.cpp", 25, 29, "bug226516.cpp", 2, 1);
        performTest("bug226516.cpp", 25, 46, "bug226516.cpp", 3, 1);
        
        performTest("bug226516.cpp", 41, 40, "bug226516.cpp", 23, 1);
        performTest("bug226516.cpp", 41, 55, "bug226516.cpp", 24, 1);
        performTest("bug226516.cpp", 41, 55, "bug226516.cpp", 24, 1);
        
        performTest("bug226516.cpp", 43, 11, "bug226516.cpp", 11, 1);
        performTest("bug226516.cpp", 43, 28, "bug226516.cpp", 12, 1);
        performTest("bug226516.cpp", 43, 42, "bug226516.cpp", 15, 5);        
        
        performNullTargetTest("bug226516.cpp", 51, 25);

        performTest("bug226516.cpp", 53, 13, "bug226516.cpp", 32, 1);
        performTest("bug226516.cpp", 53, 28, "bug226516.cpp", 33, 5);
    }
    
    public void testBug227045() throws Exception {
        // Bug 226516 - Anonymous namespace class methods not found
        performTest("bug227045.cpp", 19, 36, "bug227045.cpp", 12, 1);
    }    
    
    public void testBug219398() throws Exception {
        // Bug 219398 - "Unable to resolve identifier name" mark happened and source code format is wrong in c++ project
        performTest("typeid.cpp", 39, 61, "typeinfo.h", 21, 5);
    }            
    
    public void test231548() throws Exception {
        performTest("231548.cc", 22, 5, "231548.cc", 11, 3);
        performTest("231548.cc", 22, 16, "231548.cc", 2, 5);
        performTest("231548.cc", 23, 5, "231548.cc", 12, 3);
        performTest("231548.cc", 23, 8, "231548.cc", 14, 7);
        performNullTargetTest("231548.cc", 24, 5);
    }
    
    public void testBug235102_ns() throws Exception {
        performTest("bug235102_ns.cpp", 17, 20, "bug235102_ns.cpp", 4, 13);
        performTest("bug235102_ns.cpp", 37, 20, "bug235102_ns.cpp", 24, 13);
    }

    public void testBug235102_ns_2() throws Exception {
        performTest("bug235102_ns_2.cpp", 8, 12, "bug235102_ns_2.hpp", 12, 13);
    }    

    public void testBug243087() throws Exception {
        // Bug 243087 - static variables are unresolved when accessed with scope ("::")
        performTest("bug243087.cpp", 4, 20, "bug243087.cpp", 1, 1);
    }        
    
    public void testBug249613() throws Exception {
        // Bug 249613 - Code assistance fails for some inline namespaces
        performTest("bug249613.cpp", 22, 30, "bug249613.cpp", 5, 17);
        performTest("bug249613.cpp", 23, 30, "bug249613.cpp", 14, 17);
        performTest("bug249613.cpp", 24, 25, "bug249613.cpp", 9, 13);
        performTest("bug249613.cpp", 25, 25, "bug249613.cpp", 17, 13);
    }        
    
    public void testBug251256() throws Exception {
        // Bug 251256 - Unable to resolve static functions dereferenced with ::
        performTest("bug251256.cpp", 10, 10, "bug251256.cpp", 1, 1);
        performTest("bug251256.cpp", 11, 10, "bug251256.cpp", 3, 1);
        performTest("bug251256.cpp", 12, 10, "bug251256.cpp", 6, 5);
    }
    
    public void testBug254671() throws Exception {
        // Bug 254671 - IDE can't parse STL (default compiler in Ubuntu 15.10)
        performTest("bug254671.cpp", 16, 8, "bug254671.cpp", 10, 5);
    }
    
    public void testBug256058_2() throws Exception {
        // Bug 256058 - Unresolved items in editor of C++ Project With Existing Sources
        performTest("bug256058_2.cpp", 16, 14, "bug256058_2.cpp", 9, 17);
    }
    
    public void testBug257187() throws Exception {
        // Bug 257187 - FindUsages resolving regression
        performTest("bug257187.cpp", 10, 11, "bug257187.cpp", 9, 20);
    }
    
    public static class Failed extends HyperlinkBaseTestCase {

        @Override
        protected Class<?> getTestCaseDataClass() {
            return NamespacesHyperlinkTestCase.class;
        }

        public Failed(String testName) {
            super(testName);
        }

        public void testClassS2FunInFunS1() throws Exception {
            performTest("file.cc", 56, 20, "file.h", 14, 13); // clsS2pubFun in s2.clsS2pubFun();
        }
        
        public void testNestedNamespaceAliases() throws Exception {
            performTest("nsaliasfail.cpp", 12, 41, "file.h", 3, 5); // clsS2pubFun in s2.clsS2pubFun();
        }        
        
    }
}
