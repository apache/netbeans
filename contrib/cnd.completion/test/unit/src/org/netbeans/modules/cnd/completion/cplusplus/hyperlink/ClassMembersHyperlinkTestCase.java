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
import org.netbeans.modules.cnd.modelimpl.test.ProjectBasedTestCase.SimpleFileFilter;

/**
 *
 *
 */
public class ClassMembersHyperlinkTestCase extends HyperlinkBaseTestCase {
    public ClassMembersHyperlinkTestCase(String testName) {
        super(testName, true);
    }
    
    @Override
    protected TraceModelFileFilter getTraceModelFileFilter() {
        String simpleName = SimpleFileFilter.testNameToFileName(getName());
        switch (simpleName) {
            case "Operators":
            case "ClassNameInFuncsParams":
            case "FriendOperatorHyperlink":
            case "Constructors":
            case "ClassMethodRetClassAPtr":
            case "ClassMethodRetClassARef":
            case "PrivateMethods":
            case "PublicMethods":
            case "1":
            case "2":
            case "3":
            case "4":
            case "5":
            case "MyInnerInt1":
            case "ClassMethodRetMyInt":
            case "InitList":
            case "ClassNameInFuncRetType":
            case "Destructors":
            case "ProtectedMethods":
            case "StaticFields":
                return new SimpleFileFilter("ClassA"); 
            case "InnerSelfDeclaration":
            case "Overloads":
            case "FunParamInHeader":
            case "StringFuncsParams":
                return new SimpleFileFilter("ClassB"); 
            case "ConstructorInitializerListInHeader":
            case "ConstructorInitializerListInSource":
                return new SimpleFileFilter("ClassA", "ClassB"); 
            case "OperatorsInBaseClasses":
                return new SimpleFileFilter("iz147312_operators_in_base_cls"); 
            case "StdVector":
                return new SimpleFileFilter("IZ141105_std_vector"); 
            case "SameName":
            case "IZ136894":
            case "IZ137483":
            case "IZ145617":
            case "IZ145828":
            case "MainParamsUsing":
            case "GlobalFunctionGo":
                return new SimpleFileFilter("main"); 
            case "IZ137798":
            case "IZ137799":
            case "NestedStructAndVar":
                return new SimpleFileFilter("IZ137799and137798");
            case "IZ138902":
            case "IZ144880":
            case "IZ145230":
            case "IZ145822":
            case "IZ146030_5":
                return new SimpleFileFilter("useenumerators");
            case "IZ144731":
                return new SimpleFileFilter("iz145077");
            case "GoToDeclarationForTemplateMethods":
            case "GoToDefinitionForTemplateMethods":
                return new SimpleFileFilter("templateMethods");
            case "ClassUsageAfterDereferrencedObjects":
            case "ClassMembersUsageAfterDereferrencedClass":
            case "187254":
                return new SimpleFileFilter("ClassNameAfterDeref");
            case "ClassFwdTemplateParameters":
            case "NestedTemplateClassTemplateParameters":
            case "TemplateParameters":
            case "RenamedTemplateParameters":
            case "ConstInTemplateParameters":
            case "TemplateParameterPriority":
            case "TypenameInTemplateParameterDeclaration":
            case "TemplateParamsInNestedClasses":
                return new SimpleFileFilter("templateParameters");
            case "ClassNameCollision":
                return new SimpleFileFilter("iz156123");
            case "FriendFuncHyperlink":
            case "FromMainToClassDecl":
            case "Includes":
                return new SimpleFileFilter("ClassA","main");
            case "CastsAndPtrs":
                return new SimpleFileFilter("ClassB","main");
            case "DerefArrayByArrow":
                return new SimpleFileFilter("iz149783");
            case "IZ146030_3":
                return new SimpleFileFilter("accessMember"); 
            case "IZ148223":
                return new SimpleFileFilter("operators_hyperlink"); 
            case "IZ223046_overloads":
                return new SimpleFileFilter("iz223046"); 
            case "InnerTypePriority":
                return new SimpleFileFilter("IZ144050"); 
            case "MethodPrefix":
                return new SimpleFileFilter("IZ125760"); 
            default:
                return new SimpleFileFilter(simpleName); 
        }
    }

    public void testIZ241212() throws Exception {
        // #241212 - Wrong follow link & call graph for C++ this pointer and overloaded functions
        performTest("iz241212.cpp", 18, 25, "iz241212.cpp", 9, 9);
        performTest("iz241212.cpp", 19, 25, "iz241212.cpp", 12, 9);
    }
    
    public void testIZ151584() throws Exception {
        // IZ#151584:
        performTest("iz151584.cpp", 2, 10, "iz151584.cpp", 2, 5);
        performTest("iz151584.cpp", 4, 15, "iz151584.cpp", 4, 9);
        performTest("iz151584.cpp", 6, 20, "iz151584.cpp", 6, 13);
    }

    public void testIZ162280() throws Exception {
        // IZ#162280: Inaccuracy tests: regression in Boost and Vlc
        performTest("iz162280.cpp", 33, 20, "iz162280.cpp", 30, 9);
        performTest("iz162280.cpp", 33, 30, "iz162280.cpp", 30, 9);
        performTest("iz162280.cpp", 35, 15, "iz162280.cpp", 30, 9);
        performTest("iz162280.cpp", 35, 25, "iz162280.cpp", 27, 9);
    }

    public void testIZ149685() throws Exception {
        // IZ#149685: Multi-level class declarations not recognized by ide
        performTest("iz149685.cpp", 8, 30, "iz149685.cpp", 6, 13);
        performTest("iz149685.cpp", 19, 15, "iz149685.cpp", 6, 13);
    }

    public void testIZ155578() throws Exception {
        // IZ155578: Overloaded functions with tpedef'ed argument types break Code Assistance
        performTest("iz155578.cpp", 6, 20, "iz155578.cpp", 6, 5);
        performTest("iz155578.cpp", 7, 20, "iz155578.cpp", 7, 5);
    }

    public void testIZ159307() throws Exception {
        // IZ#159307: Wrong recognition of local constructor as global function
        performTest("iz159307.cpp", 13, 12, "iz159307.cpp", 7, 9);
        performTest("iz159307.cpp", 13, 30, "iz159307.cpp", 9, 9);
        performTest("iz159307.cpp", 14, 15, "iz159307.cpp", 1, 1);
    }

    public void testIZ159156() throws Exception {
        // IZ#159156: Wrong type resolving scope sequence in Resolver3
        performTest("iz159156.cpp", 18, 18, "iz159156.cpp", 2, 5);
    }

    public void testIZ148223() throws Exception {
        // IZ#148223: IDE can't recognize overloaded operator&&
        performTest("operators_hyperlink.cpp", 65, 21, "operators_hyperlink.cpp", 6, 9); // cc in (a1 && b1).cc()
        performTest("operators_hyperlink.cpp", 66, 21, "operators_hyperlink.cpp", 6, 9); // cc in (a1 && e1).cc()
        performTest("operators_hyperlink.cpp", 67, 21, "operators_hyperlink.cpp", 14, 9); // c1 in (e1 && d1)->c1()
    }

    public void testIZ157837() throws Exception {
        // IZ#157837: incorrect deref of function-type fields
        performTest("iz157837.cc", 23, 35, "iz157837.cc", 7, 9); // me_value
        performTest("iz157837.cc", 24, 35, "iz157837.cc", 7, 9); // me_value
        performTest("iz157837.cc", 25, 35, "iz157837.cc", 8, 9); // me_lookup
        performTest("iz157837.cc", 25, 55, "iz157837.cc", 12, 9);// mp_value
        performTest("iz157837.cc", 24, 45, "iz157837.cc", 3, 9); // value
    }

    public void testClassNameCollision() throws Exception {
        // IZ#156123: Resolve class from current namespace at first
        performTest("iz156123.cc", 21, 20, "iz156123.cc", 12, 9); // Field in "Database::Field fld;"
        performTest("iz156123.cc", 22, 15, "iz156123.cc", 14, 13); // name in "fld.name = 1;"
    }

    public void testDerefArrayByArrow() throws Exception {
        //IZ#149783: IDE does not recognize array as pointer
        performTest("iz149783.c", 9, 10, "iz149783.c", 2, 5);
        performTest("iz149783.c", 10, 15, "iz149783.c", 2, 5);
        performTest("iz149783.c", 11, 15, "iz149783.c", 2, 5);
        // IZ#151609: Unresolved struct array member
        performTest("iz149783.c", 13, 22, "iz149783.c", 2, 5);
    }

    public void testOperatorsInBaseClasses() throws Exception {
        // IZ#147312: Code completion issue with operator-> and operator*
        performTest("iz147312_operators_in_base_cls.cc", 85, 10, // o.myMethod1();
                    "iz147312_operators_in_base_cls.cc", 7, 5);
        performTest("iz147312_operators_in_base_cls.cc", 88, 20, // sp.get()->myMethod1();
                "iz147312_operators_in_base_cls.cc", 7, 5);
        performTest("iz147312_operators_in_base_cls.cc", 89, 15, // sp->myMethod1();
                "iz147312_operators_in_base_cls.cc", 7, 5);
        performTest("iz147312_operators_in_base_cls.cc", 90, 15, // (*sp).myMethod1();
                "iz147312_operators_in_base_cls.cc", 7, 5);
        performTest("iz147312_operators_in_base_cls.cc", 93, 20, // tp.get()->myMethod1();
                "iz147312_operators_in_base_cls.cc", 7, 5);
        performTest("iz147312_operators_in_base_cls.cc", 94, 15, // tp->myMethod1();
                "iz147312_operators_in_base_cls.cc", 7, 5);
        performTest("iz147312_operators_in_base_cls.cc", 95, 15, // (*tp).myMethod1();
                "iz147312_operators_in_base_cls.cc", 7, 5);
        performTest("iz147312_operators_in_base_cls.cc", 98, 20, // s2p.get()->myMethod1();
                "iz147312_operators_in_base_cls.cc", 7, 5);
        performTest("iz147312_operators_in_base_cls.cc", 99, 15, // s2p->myMethod1();
                "iz147312_operators_in_base_cls.cc", 7, 5);
        performTest("iz147312_operators_in_base_cls.cc", 100, 15, // (*s2p).myMethod1();
                "iz147312_operators_in_base_cls.cc", 7, 5);
        performTest("iz147312_operators_in_base_cls.cc", 103, 20, // t2p.get()->myMethod1();
                "iz147312_operators_in_base_cls.cc", 7, 5);
        performTest("iz147312_operators_in_base_cls.cc", 104, 15, // t2p->myMethod1();
                "iz147312_operators_in_base_cls.cc", 7, 5);
        performTest("iz147312_operators_in_base_cls.cc", 105, 15, // (*t2p).myMethod1();
                "iz147312_operators_in_base_cls.cc", 7, 5);
    }

    public void testTemplateParamsInNestedClasses() throws Exception {
        // IZ#144881: template parameter is not resolved in nested class

        performTest("templateParameters.h", 104, 9, "templateParameters.h", 100, 10);// _Tp
        performTest("templateParameters.h", 105, 9, "templateParameters.h", 100, 10);// _Tp
        performTest("templateParameters.h", 106, 9, "templateParameters.h", 100, 10);// _Tp

        performTest("templateParameters.h", 103, 25, "templateParameters.h", 100, 24);// _Alloc
        performTest("templateParameters.h", 109, 15, "templateParameters.h", 100, 24);// _Alloc
    }

    public void testPtrOperator() throws Exception {
        // noIZ:fixed ptr operator handling
        performTest("checkPtrOperator.cc", 16, 15, "checkPtrOperator.cc", 11, 9);
    }

    public void testIZ146030_3() throws Exception {
        // IZ#146030: set of problems for declarations in Loki
        // usecase 3)
        performTest("accessMember.cc", 9, 37, "accessMember.cc", 4, 5);
        performTest("accessMember.cc", 10, 45, "accessMember.cc", 4, 5);
        performTest("accessMember.cc", 14, 32, "accessMember.cc", 5, 5);
        performTest("accessMember.cc", 15, 33, "accessMember.cc", 5, 5);
    }

    public void testIZ146030_5() throws Exception {
        // IZ#146030: set of problems for declarations in Loki
        // usecase 5)
        performTest("useenumerators.cc", 55, 20, "useenumerators.cc", 52, 9);
        performTest("useenumerators.cc", 56, 20, "useenumerators.cc", 52, 9);
    }

    public void testIZ138902() throws Exception {
        // IZ#138902: No completion and hyperl ink  to enumerator in structure init
        performTest("useenumerators.cc", 48, 35, "useenumerators.cc", 43, 19);
    }

    public void testIZ145828() throws Exception {
        // IZ#145828: & breaks completion in some expressions
        performTest("main.cc", 91, 25, "main.cc", 83, 5);
        performTest("main.cc", 92, 16, "main.cc", 84, 5);
        performTest("main.cc", 93, 24, "main.cc", 85, 5);
    }

    public void testIZ144880() throws Exception {
        // IZ#144880: enumerators in template arguments are not resolved
        performTest("useenumerators.cc", 33, 12, "useenumerators.cc", 32, 12);
        performTest("useenumerators.cc", 38, 12, "useenumerators.cc", 37, 12);
    }
    public void testIZ145617() throws Exception {
        // IZ#145617: IDE highlights code with 'sizeof' in array as wrong
        performTest("main.cc", 79, 70, "main.cc", 59, 5);
    }

    public void testIZ145230() throws Exception {
        // IZ#145230:Various C++ expressions don't resolve
        // usage of enumerators
        performTest("useenumerators.cc", 4, 20, "useenumerators.cc", 1, 8);
        performTest("useenumerators.cc", 16, 40, "useenumerators.cc", 11, 5);
        performTest("useenumerators.cc", 19, 35, "useenumerators.cc", 11, 5);
    }

    public void testIZ145822() throws Exception {
        // IZ#145230:unresolved members of typedefed class
        performTest("useenumerators.cc", 26, 20, "useenumerators.cc", 26, 5);
        performTest("useenumerators.cc", 40, 10, "useenumerators.cc", 26, 5);
    }

    public void testIZ144731() throws Exception {
        // IZ#144731: function(a->m_obj ? a->m_obj : a->m_obj);
        performTest("iz145077.cc", 132, 30, "iz145077.cc", 118, 5);
    }

    public void testClassUsageAfterDereferrencedObjects() throws Exception {
        // IZ#145230:Various C++ expressions don't resolve
        performTest("ClassNameAfterDeref.cc", 22, 18, "ClassNameAfterDeref.cc", 2, 5);
        performTest("ClassNameAfterDeref.cc", 23, 18, "ClassNameAfterDeref.cc", 2, 5);
        performTest("ClassNameAfterDeref.cc", 24, 10, "ClassNameAfterDeref.cc", 2, 5);
        performTest("ClassNameAfterDeref.cc", 25, 10, "ClassNameAfterDeref.cc", 2, 5);
        performTest("ClassNameAfterDeref.cc", 32, 16, "ClassNameAfterDeref.cc", 2, 5);
        performTest("ClassNameAfterDeref.cc", 34, 16, "ClassNameAfterDeref.cc", 2, 5);
        performTest("ClassNameAfterDeref.cc", 35, 16, "ClassNameAfterDeref.cc", 2, 5);
        performTest("ClassNameAfterDeref.cc", 38, 16, "ClassNameAfterDeref.cc", 2, 5);
        performTest("ClassNameAfterDeref.cc", 39, 16, "ClassNameAfterDeref.cc", 2, 5);
    }

    public void testClassMembersUsageAfterDereferrencedClass() throws Exception {
        // IZ#145230:Various C++ expressions don't resolve
        performTest("ClassNameAfterDeref.cc", 22, 25, "ClassNameAfterDeref.cc", 8, 9);
        performTest("ClassNameAfterDeref.cc", 23, 25, "ClassNameAfterDeref.cc", 9, 9);
        performTest("ClassNameAfterDeref.cc", 24, 16, "ClassNameAfterDeref.cc", 5, 9);
        performTest("ClassNameAfterDeref.cc", 25, 16, "ClassNameAfterDeref.cc", 8, 9);
        performNullTargetTest("ClassNameAfterDeref.cc", 27, 20);
        performTest("ClassNameAfterDeref.cc", 32, 22, "ClassNameAfterDeref.cc", 5, 9);
        performTest("ClassNameAfterDeref.cc", 33, 15, "ClassNameAfterDeref.cc", 16, 9);
        performTest("ClassNameAfterDeref.cc", 34, 22, "ClassNameAfterDeref.cc", 6, 9);
        performNullTargetTest("ClassNameAfterDeref.cc", 35, 24);
        performTest("ClassNameAfterDeref.cc", 36, 15, "ClassNameAfterDeref.cc", 6, 9);
        performNullTargetTest("ClassNameAfterDeref.cc", 37, 17);
        performTest("ClassNameAfterDeref.cc", 38, 25, "ClassNameAfterDeref.cc", 5, 9);
        performNullTargetTest("ClassNameAfterDeref.cc", 39, 25);
    }

    public void test187254() throws Exception {
        // #187254 -  unresolved full method name
        performTest("ClassNameAfterDeref.cc", 43, 20, "ClassNameAfterDeref.cc", 12, 5);
        performTest("ClassNameAfterDeref.cc", 44, 20, "ClassNameAfterDeref.cc", 12, 5);
        performTest("ClassNameAfterDeref.cc", 43, 25, "ClassNameAfterDeref.cc", 16, 9);
        performTest("ClassNameAfterDeref.cc", 44, 25, "ClassNameAfterDeref.cc", 15, 9);
    }
    
    public void testClassFwdTemplateParameters() throws Exception {
        // template parameters of class member forward template class declaration
        performTest("templateParameters.h", 36, 23, "templateParameters.h", 36, 13);
        performTest("templateParameters.h", 37, 40, "templateParameters.h", 37, 13);
        performTest("templateParameters.h", 38, 30, "templateParameters.h", 38, 13);
        performTest("templateParameters.h", 39, 40, "templateParameters.h", 39, 13);
        performTest("templateParameters.h", 40, 40, "templateParameters.h", 40, 13);
        performTest("templateParameters.h", 41, 40, "templateParameters.h", 41, 13);

        // template parameters of global forward template class declaration
        performTest("templateParameters.h", 48, 24, "templateParameters.h", 48, 10);
        performTest("templateParameters.h", 48, 34, "templateParameters.h", 48, 27);
        performTest("templateParameters.h", 48, 45, "templateParameters.h", 48, 37);
    }

    public void testNestedTemplateClassTemplateParameters() throws Exception {
        performTest("templateParameters.h", 21, 50, "templateParameters.h", 21, 15); // test for ThreadingModel
        performTest("templateParameters.h", 26, 45, "templateParameters.h", 21, 15); // test for ThreadingModel
        performTest("templateParameters.h", 28, 30, "templateParameters.h", 21, 15); // test for ThreadingModel

        performTest("templateParameters.h", 22, 22, "templateParameters.h", 22, 15); // test for MX
        performTest("templateParameters.h", 26, 71, "templateParameters.h", 22, 15); // test for MX
        performTest("templateParameters.h", 28, 55, "templateParameters.h", 22, 15); // test for MX

        performTest("templateParameters.h", 25, 25, "templateParameters.h", 25, 19); // test for P
        performTest("templateParameters.h", 26, 66, "templateParameters.h", 25, 19); // test for P
        performTest("templateParameters.h", 28, 50, "templateParameters.h", 25, 19); // test for P
    }

    public void testTemplateParameters() throws Exception {
        performTest("templateParameters.h", 1, 23, "templateParameters.h", 1, 10); // test for L
        performTest("templateParameters.h", 2, 25, "templateParameters.h", 1, 10); // test for L
        performTest("templateParameters.h", 5, 57, "templateParameters.h", 1, 10); // test for L
        performTest("templateParameters.h", 8, 34, "templateParameters.h", 1, 10); // test for L

        performTest("templateParameters.h", 1, 32, "templateParameters.h", 1, 26); // test for T
        performTest("templateParameters.h", 2, 28, "templateParameters.h", 1, 26); // test for T
        performTest("templateParameters.h", 2, 83, "templateParameters.h", 1, 26); // test for T
        performTest("templateParameters.h", 5, 54, "templateParameters.h", 1, 26); // test for T
        performTest("templateParameters.h", 13, 40, "templateParameters.h", 1, 26); // test for T
        performTest("templateParameters.h", 13, 63, "templateParameters.h", 1, 26); // test for T

        performTest("templateParameters.h", 1, 57, "templateParameters.h", 1, 35); // test for C
        performTest("templateParameters.h", 11, 9, "templateParameters.h", 1, 35); // test for C
        performTest("templateParameters.h", 13, 61, "templateParameters.h", 1, 35); // test for C
    }

    public void testRenamedTemplateParameters() throws Exception {
        // IZ 138903 : incorrect parsing of template function
        performTest("templateParameters.h", 89, 18, "templateParameters.h", 82, 1);
        performTest("templateParameters.h", 89, 23, "templateParameters.h", 83, 1);
        performTest("templateParameters.h", 89, 39, "templateParameters.h", 84, 1);
        performTest("templateParameters.h", 89, 43, "templateParameters.h", 85, 1);
        performTest("templateParameters.h", 89, 47, "templateParameters.h", 86, 1);
        performTest("templateParameters.h", 90, 6, "templateParameters.h", 82, 1);
    }

    public void testConstInTemplateParameters() throws Exception {
        // IZ#156679 : Constant in template is highlighted as invalid identifier
        performTest("templateParameters.h", 129, 9, "templateParameters.h", 125, 20);
    }

    public void testSameName() throws Exception {
        performTest("main.cc", 53, 10, "main.cc", 51, 1); //sameValue(  in sameValue(sameValue - 1);
        performTest("main.cc", 53, 20, "main.cc", 51, 16); //sameValue-1  in sameValue(sameValue - 1);
    }

    public void testInnerSelfDeclaration() throws Exception {
        performTest("ClassB.h", 8, 20, "ClassB.h", 8, 17); // "MEDIUM" in enum type { MEDIUM,  HIGH };
        performTest("ClassB.h", 8, 28, "ClassB.h", 8, 26); // "HIGH" in enum type { MEDIUM,  HIGH };
        performTest("ClassB.h", 8, 12, "ClassB.h", 8, 5); // "type" in enum type { MEDIUM,  HIGH };
        performTest("ClassB.h", 30, 15, "ClassB.h", 30, 5); // "myPtr" in void* myPtr;
    }

    public void testOverloads() throws Exception {
        performTest("ClassB.h", 34, 15, "ClassB.h", 34, 5); // setDescription in void setDescription(const char* description);
        performTest("ClassB.h", 36, 15, "ClassB.h", 36, 5); // setDescription in void setDescription(const char* description, const char* vendor, int type, int category, int units);
        performTest("ClassB.h", 38, 15, "ClassB.h", 38, 5); // setDescription in void setDescription(const ClassB& obj);
    }

    public void testFunParamInHeader() throws Exception {
        performTest("ClassB.h", 34, 40, "ClassB.h", 34, 25); // description in void setDescription(const char* description);
        performTest("ClassB.h", 16, 30, "ClassB.h", 16, 23); //"type1" in ClassB(int type1, int type2 = HIGH);
        performTest("ClassB.h", 16, 20, "ClassB.h", 16, 12); //"type2" in ClassB(int type1, int type2 = HIGH);
        performTest("ClassB.cc", 5, 22, "ClassB.cc", 5, 16); // type1 in ClassB::ClassB(int type1, int type2 /* = HIGH*/) :
        performTest("ClassB.cc", 5, 35, "ClassB.cc", 5, 27); // type2 in ClassB::ClassB(int type1, int type2 /* = HIGH*/) :
    }

    public void testConstructorInitializerListInHeader() throws Exception {
        performTest("ClassB.h", 13, 42, "ClassB.h", 13, 12); // second "type" in ClassB(int type = MEDIUM) : ClassA(type), myType2(HIGH) {
        performTest("ClassB.h", 13, 25, "ClassB.h", 8, 17); // "MEDIUM" in ClassB(int type = MEDIUM) : ClassA(type), myType2(HIGH) {
        performTest("ClassB.h", 13, 35, "ClassA.cc", 12, 1); // "ClassA" in ClassB(int type = MEDIUM) : ClassA(type), myType2(HIGH) {
        performTest("ClassB.h", 13, 50, "ClassB.h", 27, 5); // "myType2" in ClassB(int type = MEDIUM) : ClassA(type), myType2(HIGH) {
        performTest("ClassB.h", 13, 56, "ClassB.h", 8, 26); // "HIGH" in ClassB(int type = MEDIUM) : ClassA(type), myType2(HIGH) {
    }

    public void testConstructorInitializerListInSource() throws Exception {
        performTest("ClassB.cc", 6, 5, "ClassA.cc", 12, 1); // "ClassA" in ClassA(type1), myType2(type2), myType1(MEDIUM)
        performTest("ClassB.cc", 6, 10, "ClassB.cc", 5, 16); // "type1" in ClassA(type1), myType2(type2), myType1(MEDIUM)
        performTest("ClassB.cc", 6, 20, "ClassB.h", 27, 5); // "myType2" in ClassA(type1), myType2(type2), myType1(MEDIUM)
        performTest("ClassB.cc", 6, 25, "ClassB.cc", 5, 27); // "type2" in ClassA(type1), myType2(type2), myType1(MEDIUM)
        performTest("ClassB.cc", 6, 35, "ClassB.h", 26, 5); // "myType1" in ClassA(type1), myType2(type2), myType1(MEDIUM)
        performTest("ClassB.cc", 6, 45, "ClassB.h", 8, 17); // "MEDIUM" in ClassA(type1), myType2(type2), myType1(MEDIUM)
    }

    public void testClassNameInFuncsParams() throws Exception {
        performTest("ClassA.h", 12, 25, "ClassA.h", 2, 1); //ClassA in void publicFoo(ClassA a);
        performTest("ClassA.h", 13, 30, "ClassA.h", 2, 1); //ClassA in void publicFoo(const ClassA &a)
        performTest("ClassA.h", 23, 30, "ClassA.h", 2, 1); //ClassA in void void protectedFoo(const ClassA* const ar[]);
        performTest("ClassA.h", 31, 30, "ClassA.h", 2, 1); //ClassA in void privateFoo(const ClassA *a);
        performTest("ClassA.h", 52, 35, "ClassA.h", 2, 1); //second ClassA in  ClassA& operator= (const ClassA& obj);
    }

    public void testClassNameInFuncRetType() throws Exception {
        performTest("ClassA.h", 52, 10, "ClassA.h", 2, 1); //first ClassA in  ClassA& operator= (const ClassA& obj);
    }

    public void testStringFuncsParams() throws Exception {
        performTest("ClassB.cc", 8, 10, "ClassB.h", 20, 5); // "method" in method("string");
        performTest("ClassB.cc", 9, 10, "ClassB.h", 24, 5); // "method" in method("string", "string");
    }

    public void testCastsAndPtrs() throws Exception {
        performTest("main.cc", 45, 20, "ClassB.h", 30, 5); // myPtr in ((ClassB)*a).*myPtr;
        performTest("main.cc", 46, 21, "ClassB.h", 30, 5); // myPtr in ((ClassB*)a)->*myPtr;
        performTest("main.cc", 47, 20, "ClassB.h", 31, 5); // myVal in ((ClassB)*a).myVal;
        performTest("main.cc", 48, 20, "ClassB.h", 31, 5); // myVal in ((ClassB*)a)->myVal;
    }

    public void testFromMainToClassDecl() throws Exception {
        performTest("main.cc", 21, 6, "ClassA.h", 2, 1);
    }

    public void testPublicMethods() throws Exception {
        // declaration do definition
        performTest("ClassA.h", 9, 11, "ClassA.cc", 24, 1); // void publicFoo();
        performTest("ClassA.h", 10, 11, "ClassA.cc", 27, 1); // void publicFoo(int a);
        performTest("ClassA.h", 11, 11, "ClassA.cc", 30, 1); // void publicFoo(int a, double b);
        //TODO: performTest("ClassA.h", 12, 11, "ClassA.cc", 33, 1); // void publicFoo(ClassA a);
        //TODO: performTest("ClassA.h", 13, 11, "ClassA.cc", 36, 1); // void publicFoo(const ClassA &a);
        performTest("ClassA.h", 15, 18, "ClassA.cc", 39, 12); // static void publicFooSt();

        // definition to declaration
        performTest("ClassA.cc", 24, 15, "ClassA.h", 9, 5); // void ClassA::publicFoo()
        performTest("ClassA.cc", 27, 15, "ClassA.h", 10, 5); // void ClassA::publicFoo(int a)
        performTest("ClassA.cc", 30, 15, "ClassA.h", 11, 5); // void ClassA::publicFoo(int a, double b)
        //TODO: performTest("ClassA.cc", 33, 15, "ClassA.h", 12, 5); // void ClassA::publicFoo(ClassA a)
        //TODO: performTest("ClassA.cc", 36, 15, "ClassA.h", 13, 5); // void ClassA::publicFoo(const ClassA &a)
        performTest("ClassA.cc", 39, 30, "ClassA.h", 15, 5); // /*static*/ void ClassA::publicFooSt()
    }

    public void testProtectedMethods() throws Exception {
        // declaration do definition
        performTest("ClassA.h", 20, 11, "ClassA.cc", 42, 1); // void protectedFoo();
        performTest("ClassA.h", 21, 11, "ClassA.cc", 45, 1); // void protectedFoo(int a);
        performTest("ClassA.h", 22, 11, "ClassA.cc", 48, 1); // void protectedFoo(int a, double b);
        //TODO: performTest("ClassA.h", 23, 11, "ClassA.cc", 51, 1); // void protectedFoo(const ClassA* const ar[]);
        performTest("ClassA.h", 25, 18, "ClassA.cc", 54, 12); // static void protectedFooSt();

        // definition to declaration
        performTest("ClassA.cc", 42, 15, "ClassA.h", 20, 5); // void ClassA::protectedFoo()
        performTest("ClassA.cc", 45, 15, "ClassA.h", 21, 5); // void ClassA::protectedFoo(int a)
        performTest("ClassA.cc", 48, 15, "ClassA.h", 22, 5); // void ClassA::protectedFoo(int a, double b)
        //TODO: performTest("ClassA.cc", 51, 15, "ClassA.h", 23, 5); // void ClassA::protectedFoo(const ClassA* const ar[])
        performTest("ClassA.cc", 54, 30, "ClassA.h", 25, 5); // /*static*/ void ClassA::protectedFooSt()
    }

    // IZ103915 Hyperlink works wrong with private methods
    public void testPrivateMethods() throws Exception {
        // declaration do definition
        performTest("ClassA.h", 28, 11, "ClassA.cc", 57, 1); // void privateFoo();
        performTest("ClassA.h", 29, 11, "ClassA.cc", 60, 1); // void privateFoo(int a);
        performTest("ClassA.h", 30, 11, "ClassA.cc", 63, 1); // void privateFoo(int a, double b);
        performTest("ClassA.h", 31, 11, "ClassA.cc", 66, 1); // void privateFoo(const ClassA *a);
        performTest("ClassA.h", 33, 18, "ClassA.cc", 69, 12); // static void privateFooSt();

        // definition to declaration
        performTest("ClassA.cc", 57, 15, "ClassA.h", 28, 5); // void ClassA::privateFoo()
        performTest("ClassA.cc", 60, 15, "ClassA.h", 29, 5); // void ClassA::privateFoo(int a)
        performTest("ClassA.cc", 63, 15, "ClassA.h", 30, 5); // void ClassA::privateFoo(int a, double b)
        performTest("ClassA.cc", 66, 15, "ClassA.h", 31, 5); // void ClassA::privateFoo(const ClassA *a)
        performTest("ClassA.cc", 69, 30, "ClassA.h", 33, 5); // /*static*/ void ClassA::privateFooSt()
    }

    public void testInitList() throws Exception {
        performTest("ClassA.cc", 8, 25, "ClassA.h", 46, 5); // privateMemberInt in "ClassA::ClassA() : privateMemberInt(1)"
    }

    public void testConstructors() throws Exception {
        // declaration do definition
        performTest("ClassA.h", 7, 10, "ClassA.cc", 8, 1); // public ClassA();
        performTest("ClassA.h", 18, 10, "ClassA.cc", 12, 1); // protected ClassA(int a);
        performTest("ClassA.h", 27, 10, "ClassA.cc", 16, 1); // private ClassA(int a, double b);

        // definition to declaration
        performTest("ClassA.cc", 8, 10, "ClassA.h", 7, 5); // ClassA::ClassA()
        performTest("ClassA.cc", 12, 10, "ClassA.h", 18, 5); // ClassA::ClassA(int a)
        performTest("ClassA.cc", 16, 10, "ClassA.h", 27, 5); // ClassA::ClassA(int a, double b)
    }

    public void testDestructors() throws Exception {
        // declaration do definition
        performTest("ClassA.h", 4, 15, "ClassA.cc", 20, 1); // ~ClassA() {

        // definition to declaration
        performTest("ClassA.cc", 20, 15, "ClassA.h", 4, 5); // ClassA::~ClassA() {
    }

    public void testIncludes() throws Exception {
        // check #include "ClassA.h" hyperlinks
        performTest("main.cc", 2, 12, "ClassA.h", 1, 1); // start of file ClassA.h
        performTest("ClassA.cc", 2, 12, "ClassA.h", 1, 1); // start of file ClassA.h
    }

    public void testOperators() throws Exception {
        // IZ#87543: Hyperlink doesn't work with overloaded operators

        // declaration do definition
        performTest("ClassA.h", 52, 15, "ClassA.cc", 74, 1); // ClassA& operator= (const ClassA& obj);
        performTest("ClassA.h", 54, 15, "ClassA.cc", 78, 1); // ClassA& operator+ (const ClassA& obj);
        performTest("ClassA.h", 56, 15, "ClassA.cc", 82, 1); // ClassA& operator- (const ClassA& obj);

        // definition to declaration
        performTest("ClassA.cc", 74, 20, "ClassA.h", 52, 5); // ClassA& ClassA::operator= (const ClassA& obj) {
        performTest("ClassA.cc", 78, 20, "ClassA.h", 54, 5); // ClassA& ClassA::operator+ (const ClassA& obj) {
        performTest("ClassA.cc", 82, 20, "ClassA.h", 56, 5); // ClassA& ClassA::operator- (const ClassA& obj) {
    }

    public void testGlobalFunctionGo() throws Exception {
        // IZ#84455 incorrect hyperlinks in case of global functions definition/declaration
        // declaration do definition
        performTest("main.cc", 4, 6, "main.cc", 8, 1); // void go();
        performTest("main.cc", 5, 6, "main.cc", 12, 1); // void go(int a);
        performTest("main.cc", 6, 6, "main.cc", 16, 1); // void go(int a, double b);

        // definition to declaration
        performTest("main.cc", 8, 6, "main.cc", 4, 1); // void go() {
        performTest("main.cc", 12, 6, "main.cc", 5, 1); // void go(int a) {
        performTest("main.cc", 16, 6, "main.cc", 6, 1); // void go(int a, double b) {

        // usage to definition
        performTest("main.cc", 24, 6, "main.cc", 8, 1); // go();
        performTest("main.cc", 25, 6, "main.cc", 12, 1); // go(1);
        performTest("main.cc", 26, 6, "main.cc", 16, 1); // go(i, 1.0);
    }

    public void testMainParamsUsing() throws Exception {
        // IZ#76195: incorrect hyperlink for "argc" in welcome.cc of Welcome project
        // usage to parameter
        performTest("main.cc", 32, 10, "main.cc", 20, 10); // f (argc > 1) {
        performTest("main.cc", 34, 30, "main.cc", 20, 10); // for (int i = 1; i < argc; i++) {
        performTest("main.cc", 35, 35, "main.cc", 20, 20); // cout << i << ": " << argv[i] << "\n";
    }

    public void testClassMethodRetClassAPtr() throws Exception {
        // declaration do definition
        performTest("ClassA.h", 59, 15, "ClassA.cc", 86, 1); // ClassA* classMethodRetClassAPtr();
        // class name in return type to class
        performTest("ClassA.h", 59, 10, "ClassA.h", 2, 1); // ClassA* classMethodRetClassAPtr();

        // definition to declaration
        performTest("ClassA.cc", 86, 20, "ClassA.h", 59, 5); // ClassA* ClassA::classMethodRetClassAPtr() {
        // class name in return type to class
        performTest("ClassA.cc", 86, 5, "ClassA.h", 2, 1); // ClassA* ClassA::classMethodRetClassAPtr() {
        // class name in method name to class
        performTest("ClassA.cc", 86, 10, "ClassA.h", 2, 1); // ClassA* ClassA::classMethodRetClassAPtr() {
    }

    public void testClassMethodRetClassARef() throws Exception {
        // declaration do definition
        performTest("ClassA.h", 60, 20, "ClassA.cc", 90, 1); // const ClassA& classMethodRetClassARef();
        // class name in return type to class
        performTest("ClassA.h", 60, 15, "ClassA.h", 2, 1); // const ClassA& classMethodRetClassARef();

        // definition to declaration
        performTest("ClassA.cc", 90, 25, "ClassA.h", 60, 5); // const ClassA& ClassA::classMethodRetClassARef() {
        // class name in return type to class
        performTest("ClassA.cc", 90, 10, "ClassA.h", 2, 1); // const ClassA& ClassA::classMethodRetClassARef() {
        // class name in method name to class
        performTest("ClassA.cc", 90, 20, "ClassA.h", 2, 1); // const ClassA& ClassA::classMethodRetClassARef() {
    }

    public void testClassMethodRetMyInt() throws Exception {
        // declaration do definition
        performTest("ClassA.h", 64, 20, "ClassA.cc", 94, 1); // myInt classMethodRetMyInt();
        // type name in return type to typedef
        performTest("ClassA.h", 64, 7, "ClassA.h", 1, 1); // myInt classMethodRetMyInt();

        // definition to declaration
        performTest("ClassA.cc", 94, 25, "ClassA.h", 64, 5); // myInt ClassA::classMethodRetMyInt() {
        // type name in return type to typedef
        performTest("ClassA.cc", 94, 5, "ClassA.h", 1, 1); // myInt ClassA::classMethodRetMyInt() {
        // class name in method name to class
        performTest("ClassA.cc", 94, 10, "ClassA.h", 2, 1); // myInt ClassA::classMethodRetMyInt() {
    }

    public void testFriendFuncHyperlink() throws Exception {
        // from declaration to definition
        performTest("ClassA.h", 72, 20, "ClassA.cc", 107, 1); // friend void friendFoo();
        // from definition to declaration
        performTest("ClassA.cc", 107, 10, "ClassA.h", 72, 5); // void friendFoo() {
        // from usage to definition
        performTest("main.cc", 17, 10, "ClassA.cc", 107, 1); // friendFoo();
    }

    public void testFriendOperatorHyperlink() throws Exception {
        // from declaration to definition
        performTest("ClassA.h", 69, 25, "ClassA.cc", 102, 1); // friend ostream& operator<< (ostream&, const ClassA&);
        // from definition to declaration
        performTest("ClassA.cc", 102, 15, "ClassA.h", 69, 5); // ostream& operator <<(ostream& output, const ClassA& item) {
    }

    public void testIZ136102() throws Exception {
        // from usage to definition
        performTest("IZ136102.cc", 15, 8, "IZ136102.cc", 6, 12);
    }

    public void testIZ136140() throws Exception {
        // from usage to definition
        performTest("IZ136140.cc", 16, 11, "IZ136140.cc", 11, 5);
        performTest("IZ136140.cc", 17, 12, "IZ136140.cc", 11, 5);
    }

    public void testIZ136894() throws Exception {
        performTest("main.cc", 67, 35, "main.cc", 59, 5); // itd_state in state->ehci_itd_pool_addr->itd_state;
        performTest("main.cc", 68, 35, "main.cc", 59, 5); // itd_state in state->ehci_itd_pool_addr[i].itd_state;
        performTest("main.cc", 70, 19, "main.cc", 59, 5); // itd_state in pool_addr[i].itd_state;
        performTest("main.cc", 71, 35, "main.cc", 59, 5); // itd_state in state->ehci_itd_pool_addr[0].itd_state;
        performTest("main.cc", 72, 19, "main.cc", 59, 5); // itd_state in pool_addr[0].itd_state;
    }

    public void testIZ136975() throws Exception {
        performTest("iz136975.cc", 18, 14, "iz136975.cc", 13, 5); // OP in if (OP::Release(*static_cast<SP*> (this))) {
        performTest("iz136975.cc", 19, 14, "iz136975.cc", 12, 5); // SP in SP::Destroy();
        performTest("iz136975.cc", 18, 39, "iz136975.cc", 12, 5); // SP in if (OP::Release(*static_cast<SP*> (this))) {
        performTest("iz136975.cc", 23, 10, "iz136975.cc", 15, 5); // PointerType in PointerType operator->() {
    }

    public void testIZ137483() throws Exception {
        performTest("main.cc", 75, 39, "main.cc", 75, 34);
        performTest("main.cc", 75, 24, "main.cc", 75, 15);
        performTest("main.cc", 76, 15, "main.cc", 75, 34);
        performTest("main.cc", 77, 18, "main.cc", 75, 15);
    }

    public void testIZ137798() throws Exception {
        performTest("IZ137799and137798.h", 2, 15, "IZ137799and137798.h", 2, 1);
        performTest("IZ137799and137798.h", 19, 15, "IZ137799and137798.h", 2, 1);
        performTest("IZ137799and137798.h", 3, 15, "IZ137799and137798.h", 3, 1);
        performTest("IZ137799and137798.h", 16, 25, "IZ137799and137798.h", 3, 1);
    }

    public void testIZ137799() throws Exception {
        performTest("IZ137799and137798.h", 12, 21, "IZ137799and137798.h", 12, 13);
        performTest("IZ137799and137798.h", 13, 21, "IZ137799and137798.h", 13, 13);
        performTest("IZ137799and137798.h", 14, 21, "IZ137799and137798.h", 14, 13);
        performTest("IZ137799and137798.h", 15, 21, "IZ137799and137798.h", 15, 13);
        performTest("IZ137799and137798.h", 16, 21, "IZ137799and137798.h", 16, 13);
        performTest("IZ137799and137798.h", 17, 21, "IZ137799and137798.h", 17, 13);
        performTest("IZ137799and137798.h", 18, 21, "IZ137799and137798.h", 18, 13);
    }

    public void testNestedStructAndVar() throws Exception {
        performTest("IZ137799and137798.h", 19, 12, "IZ137799and137798.h", 19, 11);
        performTest("IZ137799and137798.h", 11, 17, "IZ137799and137798.h", 11, 9);
    }

    public void testMethodPrefix() throws Exception {
        // IZ#125760: Hyperlink works wrongly if user created
        // method without declaration in class
        performNullTargetTest("IZ125760.cpp", 6, 10);
    }

    public void testStaticFields() throws Exception {
        // IZ114002: Hyperlink does not go from static field definition to its declaration

        // from definition to declaration
        performTest("ClassA.cc", 4, 30, "ClassA.h", 38, 5); // publicMemberStInt in int ClassA::publicMemberStInt = 1;
        performTest("ClassA.cc", 5, 30, "ClassA.h", 43, 5); // protectedMemberStInt in int ClassA::protectedMemberStInt = 2;
        performTest("ClassA.cc", 6, 30, "ClassA.h", 48, 5); // privateMemberStInt in int ClassA::privateMemberStInt = 3;

        // from declaration to definition
        performTest("ClassA.h", 38, 20, "ClassA.cc", 4, 12); // publicMemberStInt in ClassA
        performTest("ClassA.h", 43, 20, "ClassA.cc", 5, 12); // protectedMemberStInt in ClassA
        performTest("ClassA.h", 48, 20, "ClassA.cc", 6, 12); // privateMemberStInt in ClassA

        // from usage to definition
        performTest("ClassA.cc", 108, 25, "ClassA.cc", 4, 12); // publicMemberStInt in int i = ClassA::publicMemberStInt;
    }

    public void testGoToDeclarationForTemplateMethods() throws Exception {
        performTest("templateMethods.cc", 15, 8, "templateMethods.cc", 3, 5); //A in C2
        performTest("templateMethods.cc", 22, 8, "templateMethods.cc", 4, 5); //B in C2
        performTest("templateMethods.cc", 33, 5, "templateMethods.cc", 8, 5); //A in D2
    }

    public void testGoToDefinitionForTemplateMethods() throws Exception {
        performTest("templateMethods.cc", 3, 35, "templateMethods.cc", 12, 1); //A in C2
        performTest("templateMethods.cc", 4, 9, "templateMethods.cc", 20, 1); //B in C2
        performTest("templateMethods.cc", 8, 28, "templateMethods.cc", 31, 1); //A in D2
    }

    public void testIZ143285_nested_classifiers() throws Exception {
        // IZ#143285 Unresolved reference to typedefed class' typedef
        performTest("IZ143285_nested_classifiers.cc", 11, 33, "IZ143285_nested_classifiers.cc", 8, 17);
        performTest("IZ143285_nested_classifiers.cc", 15, 28, "IZ143285_nested_classifiers.cc", 3, 9);
        performTest("IZ143285_nested_classifiers.cc", 16, 16, "IZ143285_nested_classifiers.cc", 7, 13);
    }

    public void testStdVector() throws Exception {
        // IZ#141105 Code model can not resolve type for vector[i]
        performTest("IZ141105_std_vector.cc", 20, 11, "IZ141105_std_vector.cc", 3, 5);
    }

    public void testTemplateParameterPriority() throws Exception {
        // IZ#144050 : inner type should have priority over global one
        performTest("templateParameters.h", 96, 5, "templateParameters.h", 95, 11);
    }

    public void testInnerTypePriority() throws Exception {
        // IZ#144050 : inner type should have priority over global one
        performTest("IZ144050.cc", 8, 43, "IZ144050.cc", 8, 5);
        performTest("IZ144050.cc", 12, 24, "IZ144050.cc", 8, 5);
    }

    public void testIZ144062() throws Exception {
        // IZ#144062 : inner class members are not resolved
        performTest("IZ144062.cc", 3, 13, "IZ144062.cc", 3, 5);
        performTest("IZ144062.cc", 4, 14, "IZ144062.cc", 4, 9);
        performTest("IZ144062.cc", 5, 20, "IZ144062.cc", 5, 9);
        performTest("IZ144062.cc", 6, 20, "IZ144062.cc", 6, 9);
        performTest("IZ144062.cc", 8, 17, "IZ144062.cc", 8, 13);
        performTest("IZ144062.cc", 9, 18, "IZ144062.cc", 9, 13);
        performTest("IZ144062.cc", 10, 11, "IZ144062.cc", 10, 11);
        performTest("IZ144062.cc", 11, 15, "IZ144062.cc", 11, 9);
        performTest("IZ144062.cc", 11, 22, "IZ144062.cc", 11, 18);
        performTest("IZ144062.cc", 12, 17, "IZ144062.cc", 12, 13);
        performTest("IZ144062.cc", 12, 21, "IZ144062.cc", 11, 18);
        performTest("IZ144062.cc", 14, 8, "IZ144062.cc", 14, 7);
        performTest("IZ144062.cc", 15, 16, "IZ144062.cc", 15, 5);
        performTest("IZ144062.cc", 15, 18, "IZ144062.cc", 15, 18);
    }

    public void testIZ144679() throws Exception {
        // IZ#144679 : IDE highlights static constants in class as wrong code
        performTest("IZ144679.cc", 11, 40, "IZ144679.cc", 10, 1);
        performTest("IZ144679.cc", 12, 40, "IZ144679.cc", 11, 1);
    }

    public void testIZ145077() throws Exception {
        // IZ#145077: Internal C++ compiler cannot resolve inner classes
        performTest("iz145077.cc", 128, 17, "iz145077.cc", 47, 9);
        performTest("iz145077.cc", 43, 50, "iz145077.cc", 33, 9);
        performTest("iz145077.cc", 44, 60, "iz145077.cc", 112, 5);
        performTest("iz145077.cc", 163, 30, "iz145077.cc", 142, 9);
        performTest("iz145077.cc", 172, 22, "iz145077.cc", 142, 9);
        performTest("iz145077.cc", 164, 30, "iz145077.cc", 143, 9);
        performTest("iz145077.cc", 173, 22, "iz145077.cc", 143, 9);
    }

    public void testIZ145071() throws Exception {
        // IZ#145071 : forward declarations marked as error
        performTest("IZ145071.cc", 4, 23, "IZ145071.cc", 4, 13);
    }

    public void testIZ147795() throws Exception {
        // IZ#147795 : Code completion issue when using '()'
        performTest("IZ147795.cc", 10, 13, "IZ147795.cc", 5, 9);
        performTest("IZ147795.cc", 11, 17, "IZ147795.cc", 5, 9);
        performTest("IZ147795.cc", 12, 15, "IZ147795.cc", 5, 9);
        performTest("IZ147795.cc", 13, 14, "IZ147795.cc", 5, 9);
        performTest("IZ147795.cc", 13, 27, "IZ147795.cc", 5, 9);
    }

    public void testIZ148022() throws Exception {
        // IZ#148022 : Unable to resolve outside definition of a private member class
        performTest("IZ148022.cc", 10, 14, "IZ148022.cc", 5, 9);
        performTest("IZ148022.cc", 11, 14, "IZ148022.cc", 7, 9);
    }

    public void testIZ148929() throws Exception {
        // IZ#148929 : Unable to resolve destructor of a nested class
        performTest("iz148929.cc", 13, 7, "iz148929.cc", 16, 1);
        performTest("iz148929.cc", 16, 9, "iz148929.cc", 13, 5);
    }

    public void testTypenameInTemplateParameterDeclaration() throws Exception {
        // IZ#151957: 9 parser's errors in boost 1.36
        performTest("templateParameters.h", 116, 39, "templateParameters.h", 116, 1);
        performTest("templateParameters.h", 120, 15, "templateParameters.h", 116, 1);
    }

    public void testIZ151955() throws Exception {
        // IZ#151955: java.lang.StackOverflowError in boost 1.36
        performTest("iz151955.cc", 13, 35, "iz151955.cc", 10, 5);
    }

    public void testIZ154112() throws Exception {
        // IZ#154112: Unresolved instantiations of template
        performTest("iz154112.cc", 17, 13, "iz154112.cc", 7, 5);
        performTest("iz154112.cc", 18, 13, "iz154112.cc", 7, 5);
        performTest("iz154112.cc", 19, 19, "iz154112.cc", 13, 5);
        performTest("iz154112.cc", 20, 19, "iz154112.cc", 13, 5);
    }

    public void testIZ154594() throws Exception {
        // IZ#154594: completion fails on expressions with keyword template
        performTest("iz154594.cc", 15, 32, "iz154594.cc", 12, 5);
    }

    public void testIZ154775() throws Exception {
        // IZ#154775: Unresolved inner type of instantiation
        performTest("iz154775.cc", 14, 20, "iz154775.cc", 9, 5);

        performTest("iz154775.cc", 31, 24, "iz154775.cc", 23, 5);
        performTest("iz154775.cc", 32, 24, "iz154775.cc", 27, 5);
    }

    public void testIZ154778() throws Exception {
        //IZ#154778: Completion fails on gt operator
        performTest("iz154778.cc", 9, 18, "iz154778.cc", 5, 5);
    }

    public void testIZ154789() throws Exception {
        //IZ#154789: Completion fails on macros
        performTest("iz154789.cc", 15, 22, "iz154789.cc", 5, 5);
    }

    public void testIZ154781() throws Exception {
        //IZ#154781: Completion fails on const
        performTest("iz154781.cc", 14, 20, "iz154781.cc", 5, 5);
        performTest("iz154781.cc", 15, 20, "iz154781.cc", 5, 5);
    }

    public void testIZ151592() throws Exception {
        //IZ#151592: wrong hyperlink on derived class member
        performTest("iz151592.cc", 9, 14, "iz151592.cc", 9, 5);
        performTest("iz151592.cc", 10, 14, "iz151592.cc", 10, 5);
    }

    public void testIZ148035() throws Exception {
        //IZ#148035 : Code assistance errors from instances of complex template classes
        performTest("iz148035.cc", 20, 16, "iz148035.cc", 13, 5);
    }

    public void testIZ151591() throws Exception {
        // IZ#151591 : Unresolved protected member of parent class in inner class of child class
        performTest("iz151591.cc", 13, 17, "iz151591.cc", 3, 5);
    }

    public void testIZ151763() throws Exception {
        // IZ#151763 : Unresolved usage of operator ()
        performTest("iz151763.cc", 15, 9, "iz151763.cc", 2, 5);
        performTest("iz151763.cc", 18, 11, "iz151763.cc", 2, 5);
    }

    public void testIZ154792() throws Exception {
        // IZ#154792 : Completion fails on question mark
        performTest("iz154792.cc", 10, 32, "iz154792.cc", 5, 9);
    }

    public void testIZ151765() throws Exception {
        // IZ#151765 : Unresolved shifted pointers
        performTest("iz151765.cc", 11, 12, "iz151765.cc", 3, 5);
    }

    public void testIZ160637() throws Exception {
        // IZ#160637 : space between destructor and parens makes destructor unresolved
        performTest("iz160637.cc", 9, 25, "iz160637.cc", 5, 5);
    }

    public void testIZ151043() throws Exception {
        // IZ#151043 : Unresolved dereferencing
        performTest("iz151043.cc", 8, 11, "iz151043.cc", 3, 5);
        performTest("iz151043.cc", 9, 13, "iz151043.cc", 3, 5);
        performTest("iz151043.cc", 10, 25, "iz151043.cc", 3, 5);
    }

    public void testIZ160677() throws Exception {
        // IZ#160677 : Unresolved members of global variables in global context
        performTest("iz160677.cc", 6, 14, "iz160677.cc", 2, 5);
    }

    public void testIZ104943() throws Exception {
        // IZ#104943 : Hyperlink works wrong on overloaded methods with pointer
        performTest("iz104943.cc", 3, 12, "iz104943.cc", 8, 1);
        performTest("iz104943.cc", 4, 12, "iz104943.cc", 12, 1);
        performTest("iz104943.cc", 5, 12, "iz104943.cc", 16, 1);
        performTest("iz104943.cc", 8, 20, "iz104943.cc", 3, 5);
        performTest("iz104943.cc", 12, 20, "iz104943.cc", 4, 5);
        performTest("iz104943.cc", 16, 20, "iz104943.cc", 5, 5);
    }

    public void testIZ104945() throws Exception {
        // IZ#104945 : Hyperlink works wrong on overloaded methods with const
        performTest("iz104945.cc", 3, 12, "iz104945.cc", 10, 1);
        performTest("iz104945.cc", 4, 12, "iz104945.cc", 14, 1);
        performTest("iz104945.cc", 5, 12, "iz104945.cc", 18, 1);
        performTest("iz104945.cc", 6, 12, "iz104945.cc", 22, 1);
        performTest("iz104945.cc", 7, 12, "iz104945.cc", 26, 1);
        performTest("iz104945.cc", 10, 20, "iz104945.cc", 3, 5);
        performTest("iz104945.cc", 14, 20, "iz104945.cc", 4, 5);
        performTest("iz104945.cc", 18, 20, "iz104945.cc", 5, 5);
        performTest("iz104945.cc", 22, 20, "iz104945.cc", 6, 5);
        performTest("iz104945.cc", 26, 20, "iz104945.cc", 7, 5);
    }

    public void testIZ165597() throws Exception {
        // IZ#165597 : Parsing of struct field initializations fails
        performTest("iz165597.cc", 8, 10, "iz165597.cc", 4, 5);
    }

    public void testIZ169305() throws Exception {
        // IZ#169305 : unresolved identifier on class method
        performTest("iz169305.cc", 24, 15, "iz169305.cc", 3, 5);
    }

    public void testIZ151583() throws Exception {
        // IZ#151583 : nested classes should resolve containing class context
        performTest("iz151583.cc", 15, 6, "iz151583.cc", 5, 5);
        performTest("iz151583.cc", 14, 5, "iz151583.cc", 8, 1);
    }

    public void testIZ166897() throws Exception {
        // IZ#166897 : Unable to resolve member variable in typedef function definition
        performTest("iz166897.cc", 12, 19, "iz166897.cc", 5, 5);
    }

    public void testIZ159422() throws Exception {
        // IZ#159422 : Unresolved ids in function definition with field like declaration
        performTest("iz159422.cc", 10, 5, "iz159422.cc", 5, 5);
        performTest("iz159422.cc", 16, 28, "iz159422.cc", 5, 5);
        performTest("iz159422.cc", 16, 22, "iz159422.cc", 4, 5);
        performTest("iz159422.cc", 8, 27, "iz159422.cc", 4, 5);
    }

    public void testIZ231328() throws Exception {
        // IZ#231328 : Error finding fails with function pointers.
        performTest("iz231328.cc", 37, 10, "iz231328.cc", 7, 5);
        performTest("iz231328.cc", 37, 18, "iz231328.cc", 4, 5);
    }
    
    public void testIZ174581() throws Exception {
        // IZ#174581 : template: Unable to resolve identifier
        performTest("iz174581.cc", 24, 17, "iz174581.cc", 4, 5);
    }

    public void testIZ157786() throws Exception {
        // IZ#157786 : No member in class
        performTest("iz157786.cc", 13, 9, "iz157786.cc", 3, 5);
    }

    public void testIZ175231() throws Exception {
        // IZ#175231 : template method is unresolved from template-based operator
        performTest("iz175231.cc", 11, 19, "iz175231.cc", 14, 5);
    }

    public void testIZ179373() throws Exception {
        // Bug#179373: unable to resolve a member of the result of an operator
        performTest("iz179373.cc", 17, 13, "iz179373.cc", 3, 5);
    }

    public void testIZ142674() throws Exception {
        // Bug 142674 - Function-try-catch (C++) in editor shows error
        performTest("iz142674.cc", 6, 9, "iz142674.cc", 3, 5);
    }

    public void testIZ184315() throws Exception {
        // Bug 184315 - unresolved identifier on class method
        performTest("iz184315.cc", 20, 26, "iz184315.cc", 9, 5);
        performTest("iz184315.cc", 21, 26, "iz184315.cc", 9, 5);
        performTest("iz184315.cc", 22, 26, "iz184315.cc", 9, 5);
    }

    public void testIZ179095() throws Exception {
        // Bug 179095 - [code model] Go To declaration doesn't work properly
        performTest("iz179095.cc", 26, 15, "iz179095.cc", 72, 1);
        performTest("iz179095.cc", 72, 25, "iz179095.cc", 26, 5);

        performTest("iz179095.cc", 28, 15, "iz179095.cc", 104, 1);
        performTest("iz179095.cc", 104, 25, "iz179095.cc", 28, 5);

        performTest("iz179095.cc", 32, 15, "iz179095.cc", 75, 1);
        performTest("iz179095.cc", 75, 25, "iz179095.cc", 32, 5);

        performTest("iz179095.cc", 44, 15, "iz179095.cc", 67, 1);
        performTest("iz179095.cc", 67, 25, "iz179095.cc", 44, 5);

        performTest("iz179095.cc", 48, 15, "iz179095.cc", 109, 1);
        performTest("iz179095.cc", 109, 25, "iz179095.cc", 48, 5);

        performTest("iz179095.cc", 48, 15, "iz179095.cc", 109, 1);
        performTest("iz179095.cc", 109, 25, "iz179095.cc", 48, 5);

        performTest("iz179095.cc", 51, 15, "iz179095.cc", 89, 1);
        performTest("iz179095.cc", 89, 25, "iz179095.cc", 51, 5);
    }

    public void test1() throws Exception {
        // TODO: doesn't work yet
        performTest("ClassA.h", 12, 11, "ClassA.cc", 33, 1); // void publicFoo(ClassA a);
    }
    public void test2() throws Exception {
        // TODO: doesn't work yet
        performTest("ClassA.h", 13, 11, "ClassA.cc", 36, 1); // void publicFoo(const ClassA &a);
    }
    public void test4() throws Exception {
        // TODO: doesn't work yet
        performTest("ClassA.cc", 33, 15, "ClassA.h", 12, 5); // void ClassA::publicFoo(ClassA a)
    }
    public void test5() throws Exception {
        // TODO: doesn't work yet
        performTest("ClassA.cc", 36, 15, "ClassA.h", 13, 5); // void ClassA::publicFoo(const ClassA &a)
    }

    public void testMyInnerInt1() throws Exception {
        // type name in return type to typedef
        performTest("ClassA.h", 66, 10, "ClassA.h", 62, 5); // myInnerInt classMethodRetMyInnerInt();
    }

    public void testIZ145037() throws Exception {
        // IZ#145037: "operator string" defintion incorrectly resolved
        performTest("IZ145037_conversion_operators.cc", 20, 22, "IZ145037_conversion_operators.cc", 10, 9);
        performTest("IZ145037_conversion_operators.cc", 38, 22, "IZ145037_conversion_operators.cc", 28, 9);
    }

    public void testBug187272() throws Exception {
        // Bug 187272 - [code model] Parsing bug inside operator() function
        performTest("bug187272.cc", 16, 53, "bug187272.cc", 10, 3);
        performTest("bug187272.cc", 22, 53, "bug187272.cc", 7, 3);
        performTest("bug187272.cc", 18, 17, "bug187272.cc", 12, 3);
        performTest("bug187272.cc", 24, 17, "bug187272.cc", 12, 3);
    }

    public void testBug187254() throws Exception {
        // Bug 187254 - unresolved full method name
        performTest("bug187254.cpp", 14, 24, "bug187254.cpp", 8, 9);
        performTest("bug187254.cpp", 15, 24, "bug187254.cpp", 9, 9);
    }

    public void testBug191026() throws Exception {
        // Bug 191026 - Wrong class resolving
        performTest("bug191026.cpp", 20, 11, "bug191026.cpp", 7, 9);
    }
    
    public void testBug158905() throws Exception {
        // Bug 158905 - Errors in resolving of friend templates
        performTest("bug158905.cpp", 13, 11, "bug158905.cpp", 5, 5);
    }    

    public void testBug197394() throws Exception {
        // Bug 197394 - Resolving member in array of struct fails 
        performTest("bug197394.cpp", 10, 7, "bug197394.cpp", 4, 9);
    }    

    public void testBug200673() throws Exception {
        // Bug 200673 - incorrect navigation between overloaded methods
        performTest("bug200673.cpp", 18, 13, "bug200673.cpp", 22, 1);
        performTest("bug200673.cpp", 22, 27, "bug200673.cpp", 18, 3);
    }

    public void testBug203212() throws Exception {
        // Bug 203212 - Autocompletion does not work if usign namespaces
        performTest("bug203212.cpp", 28, 54, "bug203212.cpp", 21, 1);
    }    

    public void testBug207148() throws Exception {
        // Bug 207148 - C++ structs marked as erroneus code
        performTest("bug207148.cpp", 8, 9, "bug207148.cpp", 2, 4);
    }    

    public void testBug206220() throws Exception {
        // Bug 206220 - friend and nested class
        performTest("bug206220.cpp", 14, 17, "bug206220.cpp", 4, 5);
    }    

    public void testBug206460() throws Exception {
        // Bug 206460 - unresolved field of structure in case of typedef is based on forward decl and decl has the same name
        performTest("bug206460.cpp", 11, 17, "bug206460.cpp", 4, 5);
    }    

    public void testBug210299() throws Exception {
        // Bug 210299 - Unresolved static field
        performTest("bug210299.cpp", 9, 39, "bug210299.cpp", 3, 3);
    }    

    public void testBug212145() throws Exception {
        // Bug 212145 - Unable to resolve identifier - remote development - C++
        performTest("bug212145.cpp", 9, 11, "bug212145.cpp", 3, 5);
    }    

    public void testBug211033() throws Exception {
        // Bug 211033 - code model incorrectly resolves forward class declarations in case of external class definitions
        performTest("bug211033.cpp", 41, 13, "bug211033.cpp", 31, 5);
        performTest("bug211033.cpp", 46, 13, "bug211033.cpp", 36, 5);
    }    

    public void testBug217994() throws Exception {
        // Bug 217994 - default method parameter construct isn't handled properly
        performTest("bug217994.cpp", 17, 32, "bug217994.cpp", 3, 5);
    }    

    public void testIZ223046_overloads() throws Exception {
        // Bug 217994 - default method parameter construct isn't handled properly
        performTest("iz223046.cpp", 29, 12, "iz223046.cpp", 17, 5);
        performTest("iz223046.cpp", 30, 20, "iz223046.cpp", 17, 5);
        performTest("iz223046.cpp", 31, 24, "iz223046.cpp", 17, 5);

        performTest("iz223046.cpp", 33, 13, "iz223046.cpp", 16, 5);
        performTest("iz223046.cpp", 34, 17, "iz223046.cpp", 16, 5);
        performTest("iz223046.cpp", 35, 25, "iz223046.cpp", 16, 5);
    }

    public void testIZ_223966_overloads_template_spec() throws Exception {
        // Bug 217994 - default method parameter construct isn't handled properly
        performTest("iz_223966_overloads_template_spec.cpp", 14, 7, "iz_223966_overloads_template_spec.cpp", 9, 1);
        performTest("iz_223966_overloads_template_spec.cpp", 15, 7, "iz_223966_overloads_template_spec.cpp", 9, 1);
    }    
    
    public void testBug224062() throws Exception {
        // Bug 224062 - Instead of a class his constructor is defined
        performTest("bug224062.cpp", 16, 42, "bug224062.cpp", 11, 1);
        performTest("bug224062.cpp", 20, 26, "bug224062.cpp", 11, 1);
        performTest("bug224062.cpp", 22, 42, "bug224062.cpp", 11, 1);
        performTest("bug224062.cpp", 25, 22, "bug224062.cpp", 11, 1);
        performTest("bug224062.cpp", 25, 62, "bug224062.cpp", 14, 3);
    }
        
    public void testBug232033() throws Exception {
        // Bug 232033 - StringStream.fail() marked as error.
        performTest("bug232033.cpp", 15, 21, "bug232033.cpp", 3, 9);
    }
    
    public void testBug234667() throws Exception {
        // Bug 234667 - Unresolved method in nested struct
        performTest("bug234667.cpp", 10, 48, "bug234667.cpp", 4, 13);
    }    
    
    public void testBug240016() throws Exception {
        // Bug 240016 - Struct initializer is flagged as error
        performTest("bug240016.cpp", 10, 39, "bug240016.cpp", 10, 5);   
        performTest("bug240016.cpp", 11, 16, "bug240016.cpp", 3, 7);   
        performTest("bug240016.cpp", 11, 34, "bug240016.cpp", 8, 5);   
        performTest("bug240016.cpp", 12, 11, "bug240016.cpp", 4, 7);
        performTest("bug240016.cpp", 13, 10, "bug240016.cpp", 5, 7);
        
        performTest("bug240016.cpp", 25, 14, "bug240016.cpp", 17, 9);
        performTest("bug240016.cpp", 26, 14, "bug240016.cpp", 18, 9);
        performTest("bug240016.cpp", 26, 19, "bug240016.cpp", 22, 9);
    }
    
    public void testIZ241651() throws Exception {
        // IZ 241651 - Unresolved members via typedefs
        performTest("iz241651.cpp", 16, 35, "iz241651.cpp", 9, 9);   
        performTest("iz241651.cpp", 17, 70, "iz241651.cpp", 5, 9);   
        performTest("iz241651.cpp", 18, 45, "iz241651.cpp", 5, 9);   
        performTest("iz241651.cpp", 19, 49, "iz241651.cpp", 5, 9);   
        
        performTest("iz241651.cpp", 17, 40, "iz241651.cpp", 2, 5);   
        performTest("iz241651.cpp", 18, 36, "iz241651.cpp", 12, 5);   
        performTest("iz241651.cpp", 19, 37, "iz241651.cpp", 4, 5);   
        performTest("iz241651.cpp", 20, 36, "iz241651.cpp", 15, 9);   
    }
    
    public void testBug242417() throws Exception {
        // Bug 242417 - Typedefs are not syntactically equivalent of their underlining type 
        performTest("bug242417.cpp", 12, 14, "bug242417.cpp", 18, 5);
        performTest("bug242417.cpp", 18, 23, "bug242417.cpp", 12, 9);
        performTest("bug242417.cpp", 19, 17, "bug242417.cpp", 14, 9);
        performTest("bug242417.cpp", 23, 13, "bug242417.cpp", 26, 5);
        performTest("bug242417.cpp", 26, 21, "bug242417.cpp", 23, 9);
        performTest("bug242417.cpp", 37, 15, "bug242417.cpp", 42, 5);
        performTest("bug242417.cpp", 42, 34, "bug242417.cpp", 37, 13);
        performTest("bug242417.cpp", 51, 13, "bug242417.cpp", 54, 5);
        performTest("bug242417.cpp", 55, 14, "bug242417.cpp", 50, 9);
        performTest("bug242417.cpp", 61, 17, "bug242417.cpp", 65, 5);
        performTest("bug242417.cpp", 65, 44, "bug242417.cpp", 61, 13);
    }
    
    public void testBug244524() throws Exception  {
        // Bug 244524 - Unexpected token {
        performTest("bug244524.cpp", 15, 28, "bug244524.cpp", 15, 5);
        performTest("bug244524.cpp", 17, 15, "bug244524.cpp", 11, 9);
        performTest("bug244524.cpp", 18, 18, "bug244524.cpp", 3, 9);
        performTest("bug244524.cpp", 20, 14, "bug244524.cpp", 12, 9);
        performTest("bug244524.cpp", 21, 18, "bug244524.cpp", 7, 9);
        performTest("bug244524.cpp", 25, 14, "bug244524.cpp", 11, 9);
        performTest("bug244524.cpp", 26, 18, "bug244524.cpp", 3, 9);
        performTest("bug244524.cpp", 28, 15, "bug244524.cpp", 12, 9);
        performTest("bug244524.cpp", 29, 18, "bug244524.cpp", 7, 9);
        performTest("bug244524.cpp", 33, 14, "bug244524.cpp", 11, 9);
        performTest("bug244524.cpp", 34, 18, "bug244524.cpp", 3, 9);
        performTest("bug244524.cpp", 36, 14, "bug244524.cpp", 12, 9);
        performTest("bug244524.cpp", 37, 18, "bug244524.cpp", 7, 9);        
    }
    
    public void testBug235102_3() throws Exception {
        // Bug 235102 - 5% inaccuracy in LLVM
        performTest("bug235102_3_AAA.cpp", 8, 12, "bug235102_3_AAA.cpp", 3, 5);
        performTest("bug235102_3_BBB.cpp", 8, 12, "bug235102_3_BBB.cpp", 3, 5);
    }
    
    public void testBug252581() throws Exception {
        // Bug 252581 - Methods declared by 'using' statement in a public section are not listed
        performTest("bug252581.cpp", 21, 16, "bug252581.cpp", 6, 9);
    }
    
    public void testBug255328() throws Exception {
        // Bug 255328 - inaccuracy tests: C struct initialization
        performTest("bug255328.cpp", 15, 39, "bug255328.cpp", 6, 4);
        performTest("bug255328.cpp", 16, 25, "bug255328.cpp", 6, 4);
        performTest("bug255328.cpp", 17, 42, "bug255328.cpp", 6, 4);
        performTest("bug255328.cpp", 17, 27, "bug255328.cpp", 2, 5);
    }
    
    public void testBug246680() throws Exception {
        // Bug 246680 - C99: nested c99 initializer (2)
        performTest("bug246680.c", 11, 21, "bug246680.c", 6, 7);
        performTest("bug246680.c", 11, 27, "bug246680.c", 5, 11);
        performTest("bug246680.c", 11, 33, "bug246680.c", 4, 17);
    }
    
    public void testBug255903() throws Exception {
        // Bug 255903 - unresolved enumerators in struct initializer
        performTest("bug255903.c", 13, 16, "bug255903.c", 9, 5);
        performTest("bug255903.c", 14, 20, "bug255903.c", 3, 5);
        performTest("bug255903.c", 15, 20, "bug255903.c", 3, 5);
        performTest("bug255903.c", 16, 14, "bug255903.c", 8, 7);
        performTest("bug255903.c", 16, 34, "bug255903.c", 7, 9);
    }
    
    public void testBug255898() throws Exception {
        // Bug 255898 - unresolved enumerator from unnamed enum
        performTest("bug255898.c", 4, 17, "bug255898.h", 3, 9);
    }
    
    public void testBug262407() throws Exception {
        // Bug 262407 - unable to resolve type in aligned_storage
        performTest("bug262407.cpp", 7, 59, "bug262407.cpp", 4, 9);
    }
    
    public void testBug242719() throws Exception {
        // Bug 242719 - Wrong resolving of member operator plus
        performTest("bug242719.cpp", 24, 18, "bug242719.cpp", 6, 9);
        performTest("bug242719.cpp", 25, 18, "bug242719.cpp", 14, 9);
    }
    
    public void testBug257822() throws Exception {
        // Bug 257822 - Unresolved identifier in designated initializer of compound literal
        performTest("bug257822.c", 8, 8, "bug257822.c", 3, 5);
        performTest("bug257822.c", 18, 8, "bug257822.c", 3, 5);
        performTest("bug257822.c", 23, 8, "bug257822.c", 3, 5);
        performTest("bug257822.c", 26, 44, "bug257822.c", 3, 5);
    }
    
    public void testBug257821() throws Exception {
        // Bug 257821 - Unresolved identifier in designated initializer following sizeof a typedef
        performTest("bug257821.c", 13, 8, "bug257821.c", 7, 5);
        performTest("bug257821.c", 19, 11, "bug257821.c", 2, 5);
    }
    
    public void testBug269201() throws Exception {
        // Bug 269201 - Unnamed inner structures and unions in C11 language or as an extension in previous standards 
        performTest("bug269201.c", 18, 13, "bug269201.c", 3, 9);
        performTest("bug269201.c", 19, 13, "bug269201.c", 4, 9);
        performTest("bug269201.c", 20, 13, "bug269201.c", 7, 9);
        performTest("bug269201.c", 21, 13, "bug269201.c", 8, 9);
        performTest("bug269201.c", 22, 14, "bug269201.c", 11, 9);
    }
    
    public static class Failed extends HyperlinkBaseTestCase {

        @Override
        protected Class<?> getTestCaseDataClass() {
            return ClassMembersHyperlinkTestCase.class;
        }

        public Failed(String testName) {
            super(testName);
        }

        public void allFailedTests() throws Exception {
            // TODO: doesn't work yet
            performTest("ClassA.h", 12, 11, "ClassA.cc", 33, 1); // void publicFoo(ClassA a);
            performTest("ClassA.h", 13, 11, "ClassA.cc", 36, 1); // void publicFoo(const ClassA &a);

            performTest("ClassA.h", 23, 11, "ClassA.cc", 51, 1); // void protectedFoo(const ClassA* const ar[]);

            performTest("ClassA.cc", 33, 15, "ClassA.h", 12, 5); // void ClassA::publicFoo(ClassA a)
            performTest("ClassA.cc", 36, 15, "ClassA.h", 13, 5); // void ClassA::publicFoo(const ClassA &a)

            performTest("ClassA.cc", 51, 15, "ClassA.h", 23, 5); // void ClassA::protectedFoo(const ClassA* const ar[])
        }

        public void test3() throws Exception {
            // TODO: doesn't work yet
            performTest("ClassA.h", 23, 11, "ClassA.cc", 51, 1); // void protectedFoo(const ClassA* const ar[]);
        }

        public void test6() throws Exception {
            // TODO: doesn't work yet
            performTest("ClassA.cc", 51, 15, "ClassA.h", 23, 5); // void ClassA::protectedFoo(const ClassA* const ar[])
        }

        public void testMyInnerInt2() throws Exception {
            // type name in return type to typedef
            performTest("ClassA.cc", 98, 5, "ClassA.h", 62, 5); // myInnerInt ClassA::classMethodRetMyInnerInt() {
        }

        public void testOverloadFuncs() throws Exception {
            performTest("ClassB.h", 18, 15, "ClassB.h", 18, 5); //void method(int a);
            performTest("ClassB.h", 20, 15, "ClassB.h", 20, 5); //void method(const char*);
            performTest("ClassB.h", 12, 15, "ClassB.h", 22, 5); //void method(char*, double);
            performTest("ClassB.h", 24, 15, "ClassB.h", 24, 5); //void method(char*, char*);
        }
    }

}
