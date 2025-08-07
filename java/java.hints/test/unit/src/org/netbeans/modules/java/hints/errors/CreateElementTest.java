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
package org.netbeans.modules.java.hints.errors;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.modules.java.hints.infrastructure.HintsTestBase;
import org.netbeans.modules.java.hints.spiimpl.JavaFixImpl;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Lahoda
 */
public class CreateElementTest extends HintsTestBase {

    /** Creates a new instance of CreateElementTest */
    public CreateElementTest(String name) {
        super(name);
    }

    public void testBinaryOperator() throws Exception {
        Set<String> golden = new HashSet<String>(Arrays.asList(
            "CreateFieldFix:p:org.netbeans.test.java.hints.BinaryOperator:int:[private, static]",
            "AddParameterOrLocalFix:p:int:PARAMETER",
            "AddParameterOrLocalFix:p:int:LOCAL_VARIABLE"
        ));

        performTestAnalysisTest("org.netbeans.test.java.hints.BinaryOperator", 218, golden);
        performTestAnalysisTest("org.netbeans.test.java.hints.BinaryOperator", 255, golden);
        performTestAnalysisTest("org.netbeans.test.java.hints.BinaryOperator", 294, golden);
        performTestAnalysisTest("org.netbeans.test.java.hints.BinaryOperator", 333, golden);
    }

    public void testEnhancedForLoop() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.EnhancedForLoop", 186, new HashSet<String>(Arrays.asList(
            "CreateFieldFix:u:org.netbeans.test.java.hints.EnhancedForLoop:java.lang.Iterable<java.lang.String>:[private, static]",
            "AddParameterOrLocalFix:u:java.lang.Iterable<java.lang.String>:PARAMETER",
            "AddParameterOrLocalFix:u:java.lang.Iterable<java.lang.String>:LOCAL_VARIABLE"
        )));

//        performTestAnalysisTest("org.netbeans.test.java.hints.EnhancedForLoop", 244, new HashSet<String>(Arrays.asList(
//                "CreateFieldFix:u:org.netbeans.test.java.hints.EnhancedForLoop:java.lang.Iterable<java.util.List<? extends java.lang.String>>:[private, static]",
//                "AddParameterOrLocalFix:u:java.lang.Iterable<java.util.List<? extends java.lang.String>>:PARAMETER",
//                "AddParameterOrLocalFix:u:java.lang.Iterable<java.util.List<? extends java.lang.String>>:LOCAL_VARIABLE"
//        )));
    }

    public void testArrayAccess() throws Exception {
        Set<String> simpleGoldenWithLocal = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:x:org.netbeans.test.java.hints.ArrayAccess:int[]:[private, static]",
                "AddParameterOrLocalFix:x:int[]:PARAMETER",
                "AddParameterOrLocalFix:x:int[]:LOCAL_VARIABLE"
                ));

        performTestAnalysisTest("org.netbeans.test.java.hints.ArrayAccess", 170, simpleGoldenWithLocal);
        performTestAnalysisTest("org.netbeans.test.java.hints.ArrayAccess", 188, simpleGoldenWithLocal);

        Set<String> simpleGoldenWithoutLocal = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:x:org.netbeans.test.java.hints.ArrayAccess:int[]:[private]"
                ));

        performTestAnalysisTest("org.netbeans.test.java.hints.ArrayAccess", 262, simpleGoldenWithoutLocal);
        performTestAnalysisTest("org.netbeans.test.java.hints.ArrayAccess", 283, simpleGoldenWithoutLocal);

        Set<String> indexGoldenWithLocal = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:u:org.netbeans.test.java.hints.ArrayAccess:int:[private, static]",
                "AddParameterOrLocalFix:u:int:PARAMETER",
                "AddParameterOrLocalFix:u:int:LOCAL_VARIABLE"
                ));

        performTestAnalysisTest("org.netbeans.test.java.hints.ArrayAccess", 335, indexGoldenWithLocal);
        performTestAnalysisTest("org.netbeans.test.java.hints.ArrayAccess", 377, indexGoldenWithLocal);

        Set<String> indexGoldenWithoutLocal = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:u:org.netbeans.test.java.hints.ArrayAccess:int:[private]"
                ));

        performTestAnalysisTest("org.netbeans.test.java.hints.ArrayAccess", 359, indexGoldenWithoutLocal);
        performTestAnalysisTest("org.netbeans.test.java.hints.ArrayAccess", 401, indexGoldenWithoutLocal);

        performTestAnalysisTest("org.netbeans.test.java.hints.ArrayAccess", 442, new HashSet<String>(Arrays.asList(
                "CreateFieldFix:s:org.netbeans.test.java.hints.ArrayAccess:java.lang.Object[][]:[private]"
        )));
    }

    public void testAssignment() throws Exception {
        Set<String> golden = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:x:org.netbeans.test.java.hints.Assignment:int:[private, static]",
                "AddParameterOrLocalFix:x:int:PARAMETER",
                "AddParameterOrLocalFix:x:int:LOCAL_VARIABLE"
                ));
        performTestAnalysisTest("org.netbeans.test.java.hints.Assignment", 174, golden);
        performTestAnalysisTest("org.netbeans.test.java.hints.Assignment", 186, golden);
    }

    public void testVariableDeclaration() throws Exception {
        Set<String> golden = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:x:org.netbeans.test.java.hints.VariableDeclaration:int:[private, static]",
                "AddParameterOrLocalFix:x:int:PARAMETER",
                "AddParameterOrLocalFix:x:int:LOCAL_VARIABLE"
                ));

        performTestAnalysisTest("org.netbeans.test.java.hints.VariableDeclaration", 186, golden);
    }

    public void testAssert() throws Exception {
        Set<String> goldenC = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:c:org.netbeans.test.java.hints.Assert:boolean:[private, static]",
                "AddParameterOrLocalFix:c:boolean:PARAMETER",
                "AddParameterOrLocalFix:c:boolean:LOCAL_VARIABLE"
                ));

        performTestAnalysisTest("org.netbeans.test.java.hints.Assert", 159, goldenC);

        Set<String> goldenS = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:s:org.netbeans.test.java.hints.Assert:java.lang.Object:[private, static]",
                "AddParameterOrLocalFix:s:java.lang.Object:PARAMETER",
                "AddParameterOrLocalFix:s:java.lang.Object:LOCAL_VARIABLE"
                ));

        performTestAnalysisTest("org.netbeans.test.java.hints.Assert", 163, goldenS);
    }

    public void testParenthesis() throws Exception {
        Set<String> goldenC = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:x:org.netbeans.test.java.hints.Parenthesis:int[][]:[private]"
        ));

        performTestAnalysisTest("org.netbeans.test.java.hints.Parenthesis", 203, goldenC);
    }

    public void testIfAndLoops() throws Exception {
        Set<String> simple = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:a:org.netbeans.test.java.hints.IfAndLoops:boolean:[private, static]",
                "AddParameterOrLocalFix:a:boolean:PARAMETER",
                "AddParameterOrLocalFix:a:boolean:LOCAL_VARIABLE"
        ));

        performTestAnalysisTest("org.netbeans.test.java.hints.IfAndLoops", 194, simple);
        performTestAnalysisTest("org.netbeans.test.java.hints.IfAndLoops", 247, simple);
        performTestAnalysisTest("org.netbeans.test.java.hints.IfAndLoops", 309, simple);
        performTestAnalysisTest("org.netbeans.test.java.hints.IfAndLoops", 368, simple);

        Set<String> complex = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:a:org.netbeans.test.java.hints.IfAndLoops:boolean[]:[private]"
        ));

        performTestAnalysisTest("org.netbeans.test.java.hints.IfAndLoops", 214, complex);
        performTestAnalysisTest("org.netbeans.test.java.hints.IfAndLoops", 270, complex);
        performTestAnalysisTest("org.netbeans.test.java.hints.IfAndLoops", 336, complex);
        performTestAnalysisTest("org.netbeans.test.java.hints.IfAndLoops", 395, complex);
    }

    public void testTarget() throws Exception {
        Set<String> simple = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:a:org.netbeans.test.java.hints.Target:int:[private, static]",
                "AddParameterOrLocalFix:a:int:PARAMETER",
                "AddParameterOrLocalFix:a:int:LOCAL_VARIABLE"
        ));

        performTestAnalysisTest("org.netbeans.test.java.hints.Target", 186, simple);

        Set<String> complex = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:a:org.netbeans.test.java.hints.Target:int:[private]"
        ));

        performTestAnalysisTest("org.netbeans.test.java.hints.Target", 203, complex);
        performTestAnalysisTest("org.netbeans.test.java.hints.Target", 224, complex);
        performTestAnalysisTest("org.netbeans.test.java.hints.Target", 252, complex);
    }

    public void testMemberSelect() throws Exception {
        Set<String> simpleWithStatic = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:a:org.netbeans.test.java.hints.MemberSelect:int:[private, static]"
        ));
        Set<String> simple = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:a:org.netbeans.test.java.hints.MemberSelect:int:[private]"
        ));

        performTestAnalysisTest("org.netbeans.test.java.hints.MemberSelect", 236, simpleWithStatic);
        performTestAnalysisTest("org.netbeans.test.java.hints.MemberSelect", 268, simpleWithStatic);

        performTestAnalysisTest("org.netbeans.test.java.hints.MemberSelect", 290, simple);
        performTestAnalysisTest("org.netbeans.test.java.hints.MemberSelect", 311, simple);
    }

    public void testSimple() throws Exception {
        Set<String> simpleJLOWithLocal = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:e:org.netbeans.test.java.hints.Simple:java.lang.Object:[private, static]",
                "AddParameterOrLocalFix:e:java.lang.Object:PARAMETER",
                "AddParameterOrLocalFix:e:java.lang.Object:LOCAL_VARIABLE"
        ));
        Set<String> simpleJLO = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:e:org.netbeans.test.java.hints.Simple:java.lang.Object:[private]"
        ));
        Set<String> simpleJLEWithLocal = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:e:org.netbeans.test.java.hints.Simple:java.lang.Exception:[private, static]",
                "AddParameterOrLocalFix:e:java.lang.Exception:PARAMETER",
                "AddParameterOrLocalFix:e:java.lang.Exception:LOCAL_VARIABLE"
        ));
        Set<String> simpleJLE = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:e:org.netbeans.test.java.hints.Simple:java.lang.Exception:[private]"
        ));

        performTestAnalysisTest("org.netbeans.test.java.hints.Simple", 192, simpleJLEWithLocal);
        performTestAnalysisTest("org.netbeans.test.java.hints.Simple", 211, simpleJLE);

        performTestAnalysisTest("org.netbeans.test.java.hints.Simple", 245, simpleJLOWithLocal);
        performTestAnalysisTest("org.netbeans.test.java.hints.Simple", 275, simpleJLO);

        performTestAnalysisTest("org.netbeans.test.java.hints.Simple", 302, simpleJLOWithLocal);
        performTestAnalysisTest("org.netbeans.test.java.hints.Simple", 342, simpleJLO);
    }

    public void testUnary() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.Simple", 382, Collections.singleton("CreateFieldFix:i:org.netbeans.test.java.hints.Simple:int:[private]"));

        performTestAnalysisTest("org.netbeans.test.java.hints.Simple", 398, Collections.singleton("CreateFieldFix:b:org.netbeans.test.java.hints.Simple:byte:[private]"));
        performTestAnalysisTest("org.netbeans.test.java.hints.Simple", 409, Collections.singleton("CreateFieldFix:b:org.netbeans.test.java.hints.Simple:byte:[private]"));

        performTestAnalysisTest("org.netbeans.test.java.hints.Simple", 415, Collections.singleton("CreateFieldFix:l:org.netbeans.test.java.hints.Simple:int:[private]"));
    }

    public void testTypevarsAndEnums() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.TypevarsAndErrors", 221, new HashSet<String>(Arrays.asList(
                "CreateFieldFix:c1:org.netbeans.test.java.hints.TypevarsAndErrors:T:[private]"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.TypevarsAndErrors", 243, new HashSet<String>(Arrays.asList(
                "CreateFieldFix:c2:org.netbeans.test.java.hints.TypevarsAndErrors:java.lang.Class<T>:[private]"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.TypevarsAndErrors", 265, Collections.<String>emptySet());
        performTestAnalysisTest("org.netbeans.test.java.hints.TypevarsAndErrors", 287, Collections.<String>emptySet());
    }

    public void testReturn() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.Return", 164, new HashSet<String>(Arrays.asList(
                "AddParameterOrLocalFix:l:int:LOCAL_VARIABLE",
                "AddParameterOrLocalFix:l:int:PARAMETER",
                "CreateFieldFix:l:org.netbeans.test.java.hints.Return:int:[private]"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.Return", 220, new HashSet<String>(Arrays.asList(
                "AddParameterOrLocalFix:l:java.util.List:LOCAL_VARIABLE",
                "AddParameterOrLocalFix:l:java.util.List:PARAMETER",
                "CreateFieldFix:l:org.netbeans.test.java.hints.Return:java.util.List:[private]"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.Return", 284, new HashSet<String>(Arrays.asList(
                "AddParameterOrLocalFix:l:java.util.List<java.lang.String>:LOCAL_VARIABLE",
                "AddParameterOrLocalFix:l:java.util.List<java.lang.String>:PARAMETER",
                "CreateFieldFix:l:org.netbeans.test.java.hints.Return:java.util.List<java.lang.String>:[private]"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.Return", 340, Collections.<String>emptySet());
    }

    public void test92419() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.Bug92419", 123, new HashSet<String>(Arrays.asList(
                "CreateClass:org.netbeans.test.java.hints.XXXX:[]:CLASS",
		"CreateInnerClass:org.netbeans.test.java.hints.Bug92419.XXXX:[private, static]:CLASS"
        )));
    }

    public void testConditionalExpression() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.CondExpression", 203, new HashSet<String>(Arrays.asList(
                "AddParameterOrLocalFix:b:boolean:LOCAL_VARIABLE",
                "AddParameterOrLocalFix:b:boolean:PARAMETER",
                "CreateFieldFix:b:org.netbeans.test.java.hints.CondExpression:boolean:[private]"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.CondExpression", 235, new HashSet<String>(Arrays.asList(
                "AddParameterOrLocalFix:b:boolean:LOCAL_VARIABLE",
                "AddParameterOrLocalFix:b:boolean:PARAMETER",
                "CreateFieldFix:b:org.netbeans.test.java.hints.CondExpression:boolean:[private]"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.CondExpression", 207, new HashSet<String>(Arrays.asList(
                "AddParameterOrLocalFix:d:java.lang.CharSequence:LOCAL_VARIABLE",
                "AddParameterOrLocalFix:d:java.lang.CharSequence:PARAMETER",
                "CreateFieldFix:d:org.netbeans.test.java.hints.CondExpression:java.lang.CharSequence:[private]"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.CondExpression", 243, new HashSet<String>(Arrays.asList(
                "AddParameterOrLocalFix:d:java.lang.CharSequence:LOCAL_VARIABLE",
                "AddParameterOrLocalFix:d:java.lang.CharSequence:PARAMETER",
                "CreateFieldFix:d:org.netbeans.test.java.hints.CondExpression:java.lang.CharSequence:[private]"
        )));
    }

    public void testArrayInitializer() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.ArrayInitializer", 210, new HashSet<String>(Arrays.asList(
                "AddParameterOrLocalFix:f:java.io.File:LOCAL_VARIABLE",
                "AddParameterOrLocalFix:f:java.io.File:PARAMETER",
                "CreateFieldFix:f:org.netbeans.test.java.hints.ArrayInitializer:java.io.File:[private]"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.ArrayInitializer", 248, new HashSet<String>(Arrays.asList(
                "AddParameterOrLocalFix:f:java.io.File:LOCAL_VARIABLE",
                "AddParameterOrLocalFix:f:java.io.File:PARAMETER",
                "CreateFieldFix:f:org.netbeans.test.java.hints.ArrayInitializer:java.io.File:[private]"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.ArrayInitializer", 281, new HashSet<String>(Arrays.asList(
                "AddParameterOrLocalFix:i:int:LOCAL_VARIABLE",
                "AddParameterOrLocalFix:i:int:PARAMETER",
                "CreateFieldFix:i:org.netbeans.test.java.hints.ArrayInitializer:int:[private]"
        )));
    }

    public void test105415() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.Bug105415", 138, Collections.<String>emptySet());
    }

    public void test112846() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.Bug112846", 152, new HashSet<String>(Arrays.asList(
                "AddParameterOrLocalFix:xxx:double[]:LOCAL_VARIABLE",
                "AddParameterOrLocalFix:xxx:double[]:PARAMETER",
                "CreateFieldFix:xxx:org.netbeans.test.java.hints.Bug112846:double[]:[private]"
        )));
    }

    public void test111048() throws Exception {
	// do not offer to create method in non-writable file/class
	performTestAnalysisTest("org.netbeans.test.java.hints.Bug111048", 202, Collections.<String>emptySet());
	// but do it in writable
	performTestAnalysisTest("org.netbeans.test.java.hints.Bug111048", 231, new HashSet<String>(Arrays.asList(
		"CreateMethodFix:contains(java.lang.String string)boolean:org.netbeans.test.java.hints.Bug111048"
        )));
	// do not offer to create field/inner class in non-writable file/class
	performTestAnalysisTest("org.netbeans.test.java.hints.Bug111048", 261, Collections.<String>emptySet());
	performTestAnalysisTest("org.netbeans.test.java.hints.Bug111048", 301, new HashSet<String>(Arrays.asList(
		"CreateInnerClass:org.netbeans.test.java.hints.Bug111048.fieldOrClass:[private]:CLASS",
		"CreateFieldFix:fieldOrClass:org.netbeans.test.java.hints.Bug111048:java.lang.Object:[private]"
        )));
    }

    public void test117431() throws Exception {
        //do not offer same hint more times for a same unknown variable
        performTestAnalysisTest("org.netbeans.test.java.hints.Bug117431", 155, new HashSet<String>(Arrays.asList(
		"AddParameterOrLocalFix:ii:int:PARAMETER",
		"CreateFieldFix:ii:org.netbeans.test.java.hints.Bug117431:int:[private, static]",
                "AddParameterOrLocalFix:ii:int:LOCAL_VARIABLE",
                "AddParameterOrLocalFix:ii:int:OTHER"
        )));
        //but do offer for a different one
        performTestAnalysisTest("org.netbeans.test.java.hints.Bug117431", 219, new HashSet<String>(Arrays.asList(
                "AddParameterOrLocalFix:kk:int:PARAMETER",
                "CreateFieldFix:kk:org.netbeans.test.java.hints.Bug117431:int:[private, static]",
                "AddParameterOrLocalFix:kk:int:LOCAL_VARIABLE"
        )));
    }

    public void testMethodArgument() throws Exception {
        //do not offer same hint more times for a same unknown variable
        performTestAnalysisTest("org.netbeans.test.java.hints.MethodArgument", 217, new HashSet<String>(Arrays.asList(
		"AddParameterOrLocalFix:xx:int:PARAMETER",
		"CreateFieldFix:xx:org.netbeans.test.java.hints.MethodArgument:int:[private, static]",
                "AddParameterOrLocalFix:xx:int:LOCAL_VARIABLE"
        )));
    }

    public void testConstructorArgument() throws Exception {
        //do not offer same hint more times for a same unknown variable
        performTestAnalysisTest("org.netbeans.test.java.hints.ConstructorArgument", 181, new HashSet<String>(Arrays.asList(
		"AddParameterOrLocalFix:xx:int:PARAMETER",
		"CreateFieldFix:xx:org.netbeans.test.java.hints.ConstructorArgument:int:[private, static]",
                "AddParameterOrLocalFix:xx:int:LOCAL_VARIABLE"
        )));
    }

    public void testEnumConstant() throws Exception {
        //test hint creating a new enum constant
        performTestAnalysisTest("org.netbeans.test.java.hints.EnumConstant", 118, new HashSet<String>(Arrays.asList(
                "CreateEnumConstant:D:org.netbeans.test.java.hints.EnumConstant.Name:org.netbeans.test.java.hints.EnumConstant.Name"
                )));
    }

    public void test180111() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.Bug180111", 163, new HashSet<String>(Arrays.asList(
                "CreateMethodFix:create()void:org.netbeans.test.java.hints.Bug180111"
        )));
    }

    public void test190447a() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.Bug190447", 107, new HashSet<String>(Arrays.asList(
                "CreateFieldFix:t:org.netbeans.test.java.hints.Bug190447:java.lang.Iterable<? extends java.lang.String>:[private]",
                "AddParameterOrLocalFix:t:java.lang.Iterable<? extends java.lang.String>:LOCAL_VARIABLE",
                "AddParameterOrLocalFix:t:java.lang.Iterable<? extends java.lang.String>:PARAMETER"
        )));
    }

    public void test190447b() throws Exception {
        performTest("org.netbeans.test.java.hints.Bug190447",
                    "Local Variable",
                    6, 8);
    }
    
    public void test190447c() throws Exception {
        performTest("org.netbeans.test.java.hints.Bug190447",
                    "Local Variable",
                    7, 8);
    }

    public void test190447d() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.Bug190447", 157, new HashSet<String>(Arrays.asList(
                "CreateFieldFix:t3:org.netbeans.test.java.hints.Bug190447:java.lang.Iterable<? extends E>:[private]",
                "AddParameterOrLocalFix:t3:java.lang.Iterable<? extends E>:LOCAL_VARIABLE",
                "AddParameterOrLocalFix:t3:java.lang.Iterable<? extends E>:PARAMETER"
        )));
    }

    public void test189687() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.Bug189687", 83, new HashSet<String>(Arrays.asList(
                "CreateFieldFix:ii:org.netbeans.test.java.hints.Bug189687:int:[private]",
                "AddParameterOrLocalFix:ii:int:LOCAL_VARIABLE"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.Bug189687", 119, new HashSet<String>(Arrays.asList(
                "CreateFieldFix:ii:org.netbeans.test.java.hints.Bug189687:int:[private, static]",
                "AddParameterOrLocalFix:ii:int:LOCAL_VARIABLE"
        )));
    }

    public void test194625() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.CreateConstructor", 160, new HashSet<String>(Arrays.asList(
                "CreateConstructorFix:(java.lang.String string):org.netbeans.test.java.hints.CreateConstructor"
        )));
    }
    
    protected void performTestAnalysisTest(String className, int offset, Set<String> golden) throws Exception {
        prepareTest(className);

        DataObject od = DataObject.find(info.getFileObject());
        EditorCookie ec = (EditorCookie) od.getLookup().lookup(EditorCookie.class);

        Document doc = ec.openDocument();

        List<Fix> fixes = CreateElement.analyze(info, offset);
        Set<String> real = new HashSet<String>();

        for (Fix f : fixes) {
            if (f instanceof CreateFieldFix) {
                real.add(((CreateFieldFix) f).toDebugString(info));
                continue;
            }
            if (f instanceof JavaFixImpl && ((JavaFixImpl) f).jf instanceof AddParameterOrLocalFix) {
                real.add(((AddParameterOrLocalFix) ((JavaFixImpl) f).jf).toDebugString(info));
                continue;
            }
	    if (f instanceof CreateMethodFix) {
                real.add(((CreateMethodFix) f).toDebugString(info));
                continue;
	    }
	    if (f instanceof CreateClassFix) {
		real.add(((CreateClassFix) f).toDebugString(info));
		continue;
	    }

            if (f instanceof CreateEnumConstant) {
                real.add(((CreateEnumConstant) f).toDebugString(info));
                continue;
            }

            fail("Fix of incorrect type: " + f.getClass());
        }

        assertEquals(golden, real);
    }

    @Override
    protected String testDataExtension() {
        return "org/netbeans/test/java/hints/CreateElementTest/";
    }

    @Override
    protected boolean createCaches() {
        return false;
    }

    static {
        NbBundle.setBranding("test");
    }    
}
