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
package org.netbeans.modules.java.editor.base.semantic;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.java.editor.base.semantic.SemanticHighlighterBase.ErrorDescriptionSetter;
import org.netbeans.modules.java.editor.base.semantic.TestBase.Performer;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Lahoda
 */
public class DetectorTest extends TestBase {
    
    public DetectorTest(String testName) {
        super(testName);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        LifecycleManager.getDefault().saveAll();
    }

    public void testUnusedImports() throws Exception {
        performTest("UnusedImports");
    }

    public void testColorings1() throws Exception {
        performTest("Colorings1");
    }

    public void testReadUseInstanceOf() throws Exception {
        performTest("ReadUseInstanceOf");
    }

    public void testReadUseTypeCast() throws Exception {
        performTest("ReadUseTypeCast");
    }

    public void testReadUseArrayIndex() throws Exception {
        performTest("ReadUseArrayIndex");
    }

    public void testReadUseUnaryOperator() throws Exception {
        performTest("ReadUseUnaryOperator");
    }

    public void testReadUseReturn() throws Exception {
        performTest("ReadUseReturn");
    }

    public void testCompoundPackage() throws Exception {
	performTest("CompoundPackage");
    }

    public void testSemanticInnerClasses() throws Exception {
	performTest("SemanticInnerClasses");
    }

    public void testForEach() throws Exception {
	performTest("ForEach");
    }

    public void testWriteUseArgument() throws Exception {
	performTest("WriteUseArgument");
    }

    public void testReturnType() throws Exception {
	performTest("ReturnType");
    }

    public void testFieldByThis1() throws Exception {
	performTest("FieldByThis1");
    }

    public void testFieldByThis2() throws Exception {
        performTest("FieldByThis2");
    }

    public void testWriteUseCatch() throws Exception {
	performTest("WriteUseCatch");
    }

    public void testReadWriteUseArgumentOfAbstractMethod() throws Exception {
	performTest("ReadWriteUseArgumentOfAbstractMethod");
    }

    public void testReadUseExprIsIdent1() throws Exception {
	performTest("ReadUseExprIsIdent1");
    }

    public void testReadUseExprIsIdent2() throws Exception {
	performTest("ReadUseExprIsIdent2");
    }

    public void testReadUseExprIsIdent3() throws Exception {
	performTest("ReadUseExprIsIdent3");
    }

    public void testReadUseExprIsIdent4() throws Exception {
	performTest("ReadUseExprIsIdent4");
    }

    public void testClassUseNewInstance() throws Exception {
	performTest("ClassUseNewInstance");
    }

    public void testExecUseMethodCall() throws Exception {
	performTest("ExecUseMethodCall");
    }

    public void testReadUseArrayInit() throws Exception {
	performTest("ReadUseArrayInit");
    }

    public void testReadUseNewArrayIndex() throws Exception {
	performTest("ReadUseNewArrayIndex");
    }

    public void testUsages2() throws Exception {
        performTest("Usages2");
    }

    public void testCommentedGenerics() throws Exception {
        performTest("CommentedGenerics");
    }

    public void DISABLEDtestRetentionPolicy() throws Exception {
        performTest("RetentionPolicyTest");
    }

    public void testSimpleGeneric() throws Exception {
        performTest("SimpleGeneric");
    }

    public void testReadUseMathSet() throws Exception {
        performTest("ReadUseMathSet");
    }

    public void testReadUseMathSet2() throws Exception {
        performTest("ReadUseMathSet2");
    }

    public void testReadUseTernaryOperator() throws Exception {
        performTest("ReadUseTernaryOperator");
    }

    public void testUseInGenerics() throws Exception {
        performTest("UseInGenerics");
    }

    public void testFieldIsWritten1() throws Exception {
        performTest("FieldIsWritten1");
    }

    public void testFieldIsWritten2() throws Exception {
        performTest("FieldIsWritten2");
    }

    public void testConstructorsAreMethods() throws Exception {
        performTest("ConstructorsAreMethods");
    }

    public void testConstructorsAreMethods2() throws Exception {
        performTest("ConstructorsAreMethods2");
    }

    public void testDoubleBrackets() throws Exception {
        performTest("DoubleBrackets");
    }

    public void testConstructorsAreMethods3() throws Exception {
        performTest("ConstructorsAreMethods3");
    }

    public void testMethodWithArrayAtTheEnd() throws Exception {
        performTest("MethodWithArrayAtTheEnd");
    }

    public void testReadUseAssert() throws Exception {
        performTest("ReadUseAssert");
    }

    public void testSuperIsKeyword() throws Exception {
        performTest("SuperIsKeyword");
    }

    public void testNewArrayIsClassUse() throws Exception {
        performTest("NewArrayIsClassUse");
    }

    public void testNotKeywords() throws Exception {
        performTest("NotKeywords");
    }

    public void testArrayThroughInitializer() throws Exception {
        performTest("ArrayThroughInitializer");
    }

    public void testReadUseAssert2() throws Exception {
        performTest("ReadUseAssert2");
    }

    public void testConstructorUsedBySuper1() throws Exception {
        performTest("ConstructorUsedBySuper1");
    }

    public void testConstructorUsedBySuper2() throws Exception {
        performTest("ConstructorUsedBySuper2");
    }

    public void testConstructorUsedByThis() throws Exception {
        performTest("ConstructorUsedByThis");
    }

    public void testEnums() throws Exception {
        performTest("Enums");
    }

    public void testReadUseThrow() throws Exception {
        performTest("ReadUseThrow");
    }

    public void testGenericBoundIsClassUse() throws Exception {
        performTest("GenericBoundIsClassUse");
    }

    public void testParameterNames() throws Exception {
        setShowPrependedText(true);
        setInlineHints(true, false, false);
        performTest("Test.java",
                    "package test;" +
                    "public class Test {" +
                    "    public Test(String param1, int param2, int param3, float param4, double param5, Object... param6) {" +
                    "    }" +
                    "    public void api(String param1, int param2, int param3, float param4, double param5, Object... param6) {" +
                    "    }" +
                    "    private int getValue() {" +
                    "        return -1;" +
                    "    }" +
                    "    private void test() {" +
                    "        String param5 = \"\"" +
                    "        new Test(\"\", 2, getValue(), 1.0f, Math.PI)" +
                    "        new Test(\"\", 2, getValue(), 1.0f, Math.PI, null)" +
                    "        new Test(\"\", 2, getValue(), 1.0f, Math.PI, param5, null)" +
                    "        api(\"\", 2, getValue(), 1.0f, Math.PI);" +
                    "        api(\"\", 2, getValue(), 1.0f, Math.PI, null);" +
                    "        api(\"\", 2, getValue(), 1.0f, Math.PI, param5, null);" +
                    "    }" +
                    "}",
                    "[PUBLIC, CLASS, DECLARATION], 0:26-0:30\n" +
                    "[PUBLIC, CONSTRUCTOR, DECLARATION], 0:43-0:47\n" +
                    "[PUBLIC, CLASS], 0:48-0:54\n" +
                    "[PARAMETER, DECLARATION], 0:55-0:61\n" +
                    "[PARAMETER, DECLARATION], 0:67-0:73\n" +
                    "[PARAMETER, DECLARATION], 0:79-0:85\n" +
                    "[PARAMETER, DECLARATION], 0:93-0:99\n" +
                    "[PARAMETER, DECLARATION], 0:108-0:114\n" +
                    "[PUBLIC, CLASS], 0:116-0:122\n" +
                    "[PARAMETER, DECLARATION], 0:126-0:132\n" +
                    "[PUBLIC, METHOD, DECLARATION], 0:156-0:159\n" +
                    "[PUBLIC, CLASS], 0:160-0:166\n" +
                    "[PARAMETER, DECLARATION], 0:167-0:173\n" +
                    "[PARAMETER, DECLARATION], 0:179-0:185\n" +
                    "[PARAMETER, DECLARATION], 0:191-0:197\n" +
                    "[PARAMETER, DECLARATION], 0:205-0:211\n" +
                    "[PARAMETER, DECLARATION], 0:220-0:226\n" +
                    "[PUBLIC, CLASS], 0:228-0:234\n" +
                    "[PARAMETER, DECLARATION], 0:238-0:244\n" +
                    "[PRIVATE, METHOD, DECLARATION], 0:268-0:276\n" +
                    "[PRIVATE, METHOD, UNUSED, DECLARATION], 0:320-0:324\n" +
                    "[PUBLIC, CLASS], 0:336-0:342\n" +
                    "[LOCAL_VARIABLE, DECLARATION], 0:343-0:349\n" +
                    "[PUBLIC, CONSTRUCTOR], 0:366-0:370\n" +
                    "[param1:], 0:371-0:372\n" +
                    "[param2:], 0:375-0:376\n" +
                    "[param3:], 0:378-0:379\n" +
                    "[PRIVATE, METHOD], 0:378-0:386\n" +
                    "[param4:], 0:390-0:391\n" +
                    "[param5:], 0:396-0:397\n" +
                    "[PUBLIC, CLASS], 0:396-0:400\n" +
                    "[STATIC, PUBLIC, FIELD], 0:401-0:403\n" +
                    "[PUBLIC, CONSTRUCTOR], 0:416-0:420\n" +
                    "[param1:], 0:421-0:422\n" +
                    "[param2:], 0:425-0:426\n" +
                    "[param3:], 0:428-0:429\n" +
                    "[PRIVATE, METHOD], 0:428-0:436\n" +
                    "[param4:], 0:440-0:441\n" +
                    "[param5:], 0:446-0:447\n" +
                    "[PUBLIC, CLASS], 0:446-0:450\n" +
                    "[STATIC, PUBLIC, FIELD], 0:451-0:453\n" +
                    "[param6:], 0:455-0:456\n" +
                    "[PUBLIC, CONSTRUCTOR], 0:472-0:476\n" +
                    "[param1:], 0:477-0:478\n" +
                    "[param2:], 0:481-0:482\n" +
                    "[param3:], 0:484-0:485\n" +
                    "[PRIVATE, METHOD], 0:484-0:492\n" +
                    "[param4:], 0:496-0:497\n" +
                    "[param5:], 0:502-0:503\n" +
                    "[PUBLIC, CLASS], 0:502-0:506\n" +
                    "[STATIC, PUBLIC, FIELD], 0:507-0:509\n" +
                    "[param6:], 0:511-0:512\n" +
                    "[LOCAL_VARIABLE], 0:511-0:517\n" +
                    "[param6:], 0:519-0:520\n" +
                    "[PUBLIC, METHOD], 0:532-0:535\n" +
                    "[param1:], 0:536-0:537\n" +
                    "[param2:], 0:540-0:541\n" +
                    "[param3:], 0:543-0:544\n" +
                    "[PRIVATE, METHOD], 0:543-0:551\n" +
                    "[param4:], 0:555-0:556\n" +
                    "[param5:], 0:561-0:562\n" +
                    "[PUBLIC, CLASS], 0:561-0:565\n" +
                    "[STATIC, PUBLIC, FIELD], 0:566-0:568\n" +
                    "[PUBLIC, METHOD], 0:578-0:581\n" +
                    "[param1:], 0:582-0:583\n" +
                    "[param2:], 0:586-0:587\n" +
                    "[param3:], 0:589-0:590\n" +
                    "[PRIVATE, METHOD], 0:589-0:597\n" +
                    "[param4:], 0:601-0:602\n" +
                    "[param5:], 0:607-0:608\n" +
                    "[PUBLIC, CLASS], 0:607-0:611\n" +
                    "[STATIC, PUBLIC, FIELD], 0:612-0:614\n" +
                    "[param6:], 0:616-0:617\n" +
                    "[PUBLIC, METHOD], 0:630-0:633\n" +
                    "[param1:], 0:634-0:635\n" +
                    "[param2:], 0:638-0:639\n" +
                    "[param3:], 0:641-0:642\n" +
                    "[PRIVATE, METHOD], 0:641-0:649\n" +
                    "[param4:], 0:653-0:654\n" +
                    "[param5:], 0:659-0:660\n" +
                    "[PUBLIC, CLASS], 0:659-0:663\n" +
                    "[STATIC, PUBLIC, FIELD], 0:664-0:666\n" +
                    "[param6:], 0:668-0:669\n" +
                    "[LOCAL_VARIABLE], 0:668-0:674\n" +
                    "[param6:], 0:676-0:677\n");
    }

    @RandomlyFails
    public void testBLE91246() throws Exception {
        final boolean wasThrown[] = new boolean[1];
        Logger.getLogger(Utilities.class.getName()).addHandler(new Handler() {
            public void publish(LogRecord lr) {
                if (lr.getThrown() != null && lr.getThrown().getClass() == BadLocationException.class) {
                    wasThrown[0] = true;
                }
            }
            public void close() {}
            public void flush() {}
        });
        performTest("BLE91246");

        assertFalse("BLE was not thrown", wasThrown[0]);
    }

    public void testArrayAccess() throws Exception {
        performTest("ArrayAccess");
    }

    public void test88119() throws Exception {
        performTest("package-info");
    }

    public void test111113() throws Exception {
        performTest("UnusedImport111113");
    }

    public void test89356() throws Exception {
        performTest("SerialVersionUID89356");
    }

    public void testFullMemberSelect109886() throws Exception {
	performTest("FullMemberSelect109886");
    }

    public void testMultiFields116520a() throws Exception {
	performTest("MultiFields");
    }

    public void testMultiFields116520b() throws Exception {
	performTest("MultiFields");
    }

    public void testUnusedParameters() throws Exception {
	performTest("UnusedParameters");
    }

    public void testUsedInFor() throws Exception {
	performTest("UsedInFor");
    }

    public void testCastIsClassUse() throws Exception {
	performTest("CastIsClassUse");
    }

    public void testWildcardBoundIsClassUse() throws Exception {
	performTest("WildcardBoundIsClassUse");
    }

    public void testStaticImport128662() throws Exception {
	performTest("StaticImport128662");
    }

    public void testUsedImport129988() throws Exception {
	performTest("UsedImport129988");
    }

    public void testUsedImport132980() throws Exception {
	performTest("UsedImport132980");
    }

    public void testUsedImport159773() throws Exception {
	performTest("UsedImport159773");
    }

    public void testReadUse132342() throws Exception {
	performTest("ReadUse132342");
    }

    public void testRecursiveExecutionIsNotUse() throws Exception {
	performTest("RecursiveExecutionIsNotUse");
    }

    public void testDeprecatedClassDeprecatesConstructor() throws Exception {
        performTest("DeprecatedClassDeprecatesConstructor");
    }

    public void testAttributeDefaultValue() throws Exception {
        performTest("AttributeDefaultValue");
    }

    public void testWriteThroughThis() throws Exception {
        performTest("WriteThroughThis");
    }

    public void testTwoPackagePrivateConstructors() throws Exception {
        performTest("TwoPackagePrivateConstructors");
    }
    
    public void testUnary220003() throws Exception {
        performTest("Unary220003");
    }
    
    public void testConstructorParamIsUsed220117() throws Exception {
        performTest("ConstructorParamIsUsed");
    }

    //Support for exotic identifiers has been removed 6999438
    public void REMOVEDtestExoticIdentifiers() throws Exception {
        setSourceLevel("1.7");
        performTest("ExoticIdentifier");
    }

    public void testStaticImport189226() throws Exception {
        performTest("StaticImport189226");
    }

    public void testReadUseElseTernary191230() throws Exception {
        performTest("ReadUseElseTernary191230");
    }

    public void testImportDisambiguation203874() throws Exception {
        performTest("ImportDisambiguation");
    }

    public void testLambdaAndFunctionType() throws Exception {
        setSourceLevel("1.8");
        performTest("LambdaAndFunctionType");
    }

    public void testExtensionMethod() throws Exception {
        setSourceLevel("1.8");
        performTest("ExtensionMethod");
    }

    public void testMemberReference() throws Exception {
        setSourceLevel("1.8");
        performTest("MemberReference");
    }

    public void testIncDecReading230408() throws Exception {
        performTest("IncDecReading230408");
    }

    public void testRecord1() throws Exception {
        setSourceLevel("16");
        performTest("Record",
                    "public record Test(String s) {}\n" +
                    "class T {\n" +
                    "    public String g(Test t) {\n" +
                    "        return t.s();\n" +
                    "    }\n" +
                    "}\n",
                    "[KEYWORD], 0:7-0:13",
                    "[PUBLIC, RECORD, DECLARATION], 0:14-0:18",
                    "[PUBLIC, CLASS], 0:19-0:25",
                    "[PUBLIC, RECORD_COMPONENT, DECLARATION], 0:26-0:27",
                    "[PACKAGE_PRIVATE, CLASS, UNUSED, DECLARATION], 1:6-1:7",
                    "[PUBLIC, CLASS], 2:11-2:17",
                    "[PUBLIC, METHOD, DECLARATION], 2:18-2:19",
                    "[PUBLIC, RECORD], 2:20-2:24",
                    "[PARAMETER, DECLARATION], 2:25-2:26",
                    "[PARAMETER], 3:15-3:16",
                    "[PUBLIC, METHOD], 3:17-3:18");
    }

    public void testRecord2() throws Exception {
        setSourceLevel("16");
        performTest("Records",
                    "public class Records {\n" +
                    "    public interface Super {}\n" +
                    "    public record Foo1(String i, String j) implements Super { }\n" +
                    "    public record Foo2(String i, String j) implements Super { }\n" +
                    "    public record Foo3(String i, String j) implements Super { }\n" +
                    "}\n",
                    "[PUBLIC, CLASS, DECLARATION], 0:13-0:20",
                    "[STATIC, PUBLIC, INTERFACE, DECLARATION], 1:21-1:26",
                    "[KEYWORD], 2:11-2:17",
                    "[STATIC, PUBLIC, RECORD, DECLARATION], 2:18-2:22",
                    "[PUBLIC, CLASS], 2:23-2:29",
                    "[PUBLIC, RECORD_COMPONENT, DECLARATION], 2:30-2:31",
                    "[PUBLIC, CLASS], 2:33-2:39",
                    "[PUBLIC, RECORD_COMPONENT, DECLARATION], 2:40-2:41",
                    "[STATIC, PUBLIC, INTERFACE], 2:54-2:59",
                    "[KEYWORD], 3:11-3:17",
                    "[STATIC, PUBLIC, RECORD, DECLARATION], 3:18-3:22",
                    "[PUBLIC, CLASS], 3:23-3:29",
                    "[PUBLIC, RECORD_COMPONENT, DECLARATION], 3:30-3:31",
                    "[PUBLIC, CLASS], 3:33-3:39",
                    "[PUBLIC, RECORD_COMPONENT, DECLARATION], 3:40-3:41",
                    "[STATIC, PUBLIC, INTERFACE], 3:54-3:59",
                    "[KEYWORD], 4:11-4:17",
                    "[STATIC, PUBLIC, RECORD, DECLARATION], 4:18-4:22",
                    "[PUBLIC, CLASS], 4:23-4:29",
                    "[PUBLIC, RECORD_COMPONENT, DECLARATION], 4:30-4:31",
                    "[PUBLIC, CLASS], 4:33-4:39",
                    "[PUBLIC, RECORD_COMPONENT, DECLARATION], 4:40-4:41",
                    "[STATIC, PUBLIC, INTERFACE], 4:54-4:59");
    }
    
    public void testSealed() throws Exception {
        setSourceLevel("17");
        performTest("SealedTest",
                "sealed class Test{}\n"
                + "non-sealed class Child extends Test{}\n",
                "[KEYWORD], 0:0-0:6",
                "[PACKAGE_PRIVATE, CLASS, DECLARATION], 0:13-0:17",
                "[KEYWORD], 1:0-1:3",
                "[KEYWORD], 1:4-1:10",
                "[PACKAGE_PRIVATE, CLASS, UNUSED, DECLARATION], 1:17-1:22",
                "[PACKAGE_PRIVATE, CLASS], 1:31-1:35");
    }

    public void testSealed2() throws Exception {
        setSourceLevel("17");
        performTest("SealedTest",
                "sealed class Test permits Child{}\n"
                + "non-sealed class Child extends Test{}\n",
                "[KEYWORD], 0:0-0:6",
                "[PACKAGE_PRIVATE, CLASS, DECLARATION], 0:13-0:17",
                "[KEYWORD], 0:18-0:25",
                "[PACKAGE_PRIVATE, CLASS], 0:26-0:31",
                "[KEYWORD], 1:0-1:3",
                "[KEYWORD], 1:4-1:10",
                "[PACKAGE_PRIVATE, CLASS, DECLARATION], 1:17-1:22",
                "[PACKAGE_PRIVATE, CLASS], 1:31-1:35");
    }

    public void testSwitchPattern() throws Exception {
        setSourceLevel("21");
        performTest("TestSwitchPattern.java",
                "public class TestSwitchPattern {\n"
                + "    String strColor = \"color\";\n"
                + "    void m1() {\n"
                + "        Object obj = \"test\";\n"
                + "        switch (obj) {\n"
                + "            case String s when s.equals(strColor) -> System.out.println(\"same\");\n"
                + "            case default -> System.out.println(\"default\");\n"
                + "        }\n"
                + "    }\n"
                + "}",
                "[PUBLIC, CLASS, DECLARATION], 0:13-0:30\n"
                + "[PUBLIC, CLASS], 1:4-1:10\n"
                + "[PACKAGE_PRIVATE, FIELD, DECLARATION], 1:11-1:19\n"
                + "[PACKAGE_PRIVATE, METHOD, UNUSED, DECLARATION], 2:9-2:11\n"
                + "[PUBLIC, CLASS], 3:8-3:14\n"
                + "[LOCAL_VARIABLE, DECLARATION], 3:15-3:18\n"
                + "[LOCAL_VARIABLE], 4:16-4:19\n"
                + "[PUBLIC, CLASS], 5:17-5:23\n"
                + "[LOCAL_VARIABLE, DECLARATION], 5:24-5:25\n"
                + "[KEYWORD], 5:26-5:30\n"
                + "[LOCAL_VARIABLE], 5:31-5:32\n"
                + "[PUBLIC, METHOD], 5:33-5:39\n"
                + "[PACKAGE_PRIVATE, FIELD], 5:40-5:48\n"
                + "[PUBLIC, CLASS], 5:53-5:59\n"
                + "[STATIC, PUBLIC, FIELD], 5:60-5:63\n"
                + "[PUBLIC, METHOD], 5:64-5:71\n"
                + "[PUBLIC, CLASS], 6:28-6:34\n"
                + "[STATIC, PUBLIC, FIELD], 6:35-6:38\n"
                + "[PUBLIC, METHOD], 6:39-6:46\n");
    }

    public void testRecordPattern() throws Exception {
        setSourceLevel("21");
        performTest("TestRecordPattern.java",
                "public class TestRecordPattern {\n"
                + "    record Person(int name, int a){}\n"
                + "    void m1() {\n"
                + "        Person obj = new Person(1,2);\n"
                + "        switch (obj) {\n"
                + "            case Person(int x, int y) when x > 0 -> System.out.println(\"x greater than 0\");\n"
                + "            case default -> System.out.println(\"default\");\n"
                + "        }\n"
                + "    }\n"
                + "}",
                "[PUBLIC, CLASS, DECLARATION], 0:13-0:30\n"
                + "[KEYWORD], 1:4-1:10\n"
                + "[STATIC, PACKAGE_PRIVATE, RECORD, DECLARATION], 1:11-1:17\n"
                + "[PUBLIC, RECORD_COMPONENT, DECLARATION], 1:22-1:26\n"
                + "[PUBLIC, RECORD_COMPONENT, DECLARATION], 1:32-1:33\n"
                + "[PACKAGE_PRIVATE, METHOD, UNUSED, DECLARATION], 2:9-2:11\n"
                + "[STATIC, PACKAGE_PRIVATE, RECORD], 3:8-3:14\n"
                + "[LOCAL_VARIABLE, DECLARATION], 3:15-3:18\n"
                + "[PACKAGE_PRIVATE, CONSTRUCTOR], 3:25-3:31\n"
                + "[LOCAL_VARIABLE], 4:16-4:19\n"
                + "[STATIC, PACKAGE_PRIVATE, RECORD], 5:17-5:23\n"
                + "[LOCAL_VARIABLE, DECLARATION], 5:28-5:29\n"
                + "[LOCAL_VARIABLE, UNUSED, DECLARATION], 5:35-5:36\n"
                + "[KEYWORD], 5:38-5:42\n"
                + "[LOCAL_VARIABLE], 5:43-5:44\n"
                + "[PUBLIC, CLASS], 5:52-5:58\n"
                + "[STATIC, PUBLIC, FIELD], 5:59-5:62\n"
                + "[PUBLIC, METHOD], 5:63-5:70\n"
                + "[PUBLIC, CLASS], 6:28-6:34\n"
                + "[STATIC, PUBLIC, FIELD], 6:35-6:38\n"
                + "[PUBLIC, METHOD], 6:39-6:46\n");
    }

    public void testYield() throws Exception {
        setSourceLevel("17");
        performTest("YieldTest.java",
                    "public class YieldTest {\n" +
                    "    private int map(int i) {\n" +
                    "        return switch (i) { default -> { yield 0; } };\n" +
                    "    }\n" +
                    "}\n",
                    "[PUBLIC, CLASS, DECLARATION], 0:13-0:22\n" +
                    "[PRIVATE, METHOD, UNUSED, DECLARATION], 1:16-1:19\n" +
                    "[PARAMETER, DECLARATION], 1:24-1:25\n" +
                    "[PARAMETER], 2:23-2:24\n" +
                    "[KEYWORD], 2:41-2:46\n");
    }

    public void testRawStringLiteral() throws Exception {
        setSourceLevel("15");
        performTest("RawStringLiteral",
                    "public class RawStringLiteral {\n" +
                    "    String s1 = \"\"\"\n" +
                    "                int i1 = 1;    \n" +
                    "                  int i2 = 2;\n" +
                    "             \"\"\";\n" +
                    "    String s2 = \"\"\"\n" +
                    "                int i1 = 1;    \n" +
                    "                  int i2 = 2;\n" +
                    "                      \"\"\";\n" +
                    "}\n",
                    "[PUBLIC, CLASS, DECLARATION], 0:13-0:29",
                    "[PUBLIC, CLASS], 1:4-1:10",
                    "[PACKAGE_PRIVATE, FIELD, UNUSED, DECLARATION], 1:11-1:13",
                    "[UNINDENTED_TEXT_BLOCK], 2:13-2:27",
                    "[UNINDENTED_TEXT_BLOCK], 3:13-3:29",
                    "[PUBLIC, CLASS], 5:4-5:10",
                    "[PACKAGE_PRIVATE, FIELD, UNUSED, DECLARATION], 5:11-5:13",
                    "[UNINDENTED_TEXT_BLOCK], 6:16-6:27",
                    "[UNINDENTED_TEXT_BLOCK], 7:16-7:29");
    }

    public void testBindingPattern() throws Exception {
        setSourceLevel("16");
        performTest("BindingPattern",
                    "public class BindingPattern {\n" +
                    "    public boolean test(Object o) {\n" +
                    "        return o instanceof String str && str.isEmpty();\n" +
                    "    }\n" +
                    "}\n",
                    "[PUBLIC, CLASS, DECLARATION], 0:13-0:27",
                    "[PUBLIC, METHOD, DECLARATION], 1:19-1:23",
                    "[PUBLIC, CLASS], 1:24-1:30",
                    "[PARAMETER, DECLARATION], 1:31-1:32",
                    "[PARAMETER], 2:15-2:16",
                    "[PUBLIC, CLASS], 2:28-2:34",
                    "[LOCAL_VARIABLE, DECLARATION], 2:35-2:38",
                    "[LOCAL_VARIABLE], 2:42-2:45",
                    "[PUBLIC, METHOD], 2:46-2:53");
    }

    public void testInvalidParameterList() throws Exception {
        setShowPrependedText(true);
        setInlineHints(true, false, false);
        performTest("Test.java",
                    "public class BugSemanticHighlighterBase {\n" +
                    "    private Object testMethod(final String arg1 final String arg2) {\n" +
                    "        return new String(\"\");\n" +
                    "    }\n" +
                    "}\n",
                    "[PUBLIC, CLASS, DECLARATION], 0:13-0:39",
                    "[PUBLIC, CLASS], 1:12-1:18",
                    "[PRIVATE, METHOD, UNUSED, DECLARATION], 1:19-1:29",
                    "[PUBLIC, CLASS], 1:36-1:42",
                    "[PARAMETER, UNUSED, DECLARATION], 1:43-1:47",
                    "[PUBLIC, CLASS], 1:54-1:60",
                    "[PACKAGE_PRIVATE, FIELD, UNUSED, DECLARATION], 1:61-1:65",
                    "[PACKAGE_PRIVATE, CONSTRUCTOR], 2:19-2:25");
    }

    public void testChainTypes() throws Exception {
        setShowPrependedText(true);
        setInlineHints(true, true, false);
        performTest("Test.java",
                    "package test;\n" +
                    "public class Test<T> {\n" +
                    "    public void test(Test<String> t) {\n" +
                    "        t.run1()\n" +
                    "         .run2()\n" +
                    "         .run3()\n" +
                    "         .run4();\n" +
                    "    }\n" +
                    "    private Test<Integer> run1() {\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "    private Test<String> run2() {\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "    private Test<Integer> run3() {\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "    private Test<String> run4() {\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "}\n",
                    "[PUBLIC, CLASS, DECLARATION], 1:13-1:17",
                    "[PUBLIC, METHOD, DECLARATION], 2:16-2:20",
                    "[PUBLIC, CLASS], 2:21-2:25",
                    "[PUBLIC, CLASS], 2:26-2:32",
                    "[PARAMETER, DECLARATION], 2:34-2:35",
                    "[PARAMETER], 3:8-3:9",
                    "[PRIVATE, METHOD], 3:10-3:14",
                    "[  Test<Integer>], 3:16-4:0",
                    "[PRIVATE, METHOD], 4:10-4:14",
                    "[  Test<String>], 4:16-5:0",
                    "[PRIVATE, METHOD], 5:10-5:14",
                    "[  Test<Integer>], 5:16-6:0",
                    "[PRIVATE, METHOD], 6:10-6:14",
                    "[  Test<String>], 6:17-7:0",
                    "[PUBLIC, CLASS], 8:12-8:16",
                    "[PUBLIC, CLASS], 8:17-8:24",
                    "[PRIVATE, METHOD, DECLARATION], 8:26-8:30",
                    "[PUBLIC, CLASS], 11:12-11:16",
                    "[PUBLIC, CLASS], 11:17-11:23",
                    "[PRIVATE, METHOD, DECLARATION], 11:25-11:29",
                    "[PUBLIC, CLASS], 14:12-14:16",
                    "[PUBLIC, CLASS], 14:17-14:24",
                    "[PRIVATE, METHOD, DECLARATION], 14:26-14:30",
                    "[PUBLIC, CLASS], 17:12-17:16",
                    "[PUBLIC, CLASS], 17:17-17:23",
                    "[PRIVATE, METHOD, DECLARATION], 17:25-17:29");
    }

    public void testChainTypes2() throws Exception {
        setShowPrependedText(true);
        setInlineHints(true, true, false);
        performTest("Test.java",
                    "package test;\n" +
                    "public class Test<T> {\n" +
                    "    public void test(Test<String> t) {\n" +
                    "        test2(t.run1()\n" +
                    "               .run2()\n" +
                    "               .run3()\n" +
                    "               .run4(),\n" +
                    "              t.run1()\n" +
                    "               .run2()\n" +
                    "               .run3()\n" +
                    "               .run4());\n" +
                    "    }\n" +
                    "    private Test<Integer> run1() {\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "    private Test<String> run2() {\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "    private Test<Integer> run3() {\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "    private Test<String> run4() {\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "    public void test2(Test<String> t1, Test<String> t2) {\n" +
                    "    }\n" +
                    "}\n",
                    "[PUBLIC, CLASS, DECLARATION], 1:13-1:17",
                    "[PUBLIC, METHOD, DECLARATION], 2:16-2:20",
                    "[PUBLIC, CLASS], 2:21-2:25",
                    "[PUBLIC, CLASS], 2:26-2:32",
                    "[PARAMETER, DECLARATION], 2:34-2:35",
                    "[PUBLIC, METHOD], 3:8-3:13",
                    "[t1:], 3:14-3:15",
                    "[PRIVATE, METHOD], 3:16-3:20",
                    "[  Test<Integer>], 3:22-4:0",
                    "[PRIVATE, METHOD], 4:16-4:20",
                    "[  Test<String>], 4:22-5:0",
                    "[PRIVATE, METHOD], 5:16-5:20",
                    "[  Test<Integer>], 5:22-6:0",
                    "[PRIVATE, METHOD], 6:16-6:20",
                    "[  Test<String>], 6:23-7:0",
                    "[t2:], 7:14-7:15",
                    "[PRIVATE, METHOD], 7:16-7:20",
                    "[  Test<Integer>], 7:22-8:0",
                    "[PRIVATE, METHOD], 8:16-8:20",
                    "[  Test<String>], 8:22-9:0",
                    "[PRIVATE, METHOD], 9:16-9:20",
                    "[  Test<Integer>], 9:22-10:0",
                    "[PRIVATE, METHOD], 10:16-10:20",
                    "[  Test<String>; ], 10:24-11:0",
                    "[PUBLIC, CLASS], 12:12-12:16",
                    "[PUBLIC, CLASS], 12:17-12:24",
                    "[PRIVATE, METHOD, DECLARATION], 12:26-12:30",
                    "[PUBLIC, CLASS], 15:12-15:16",
                    "[PUBLIC, CLASS], 15:17-15:23",
                    "[PRIVATE, METHOD, DECLARATION], 15:25-15:29",
                    "[PUBLIC, CLASS], 18:12-18:16",
                    "[PUBLIC, CLASS], 18:17-18:24",
                    "[PRIVATE, METHOD, DECLARATION], 18:26-18:30",
                    "[PUBLIC, CLASS], 21:12-21:16",
                    "[PUBLIC, CLASS], 21:17-21:23",
                    "[PRIVATE, METHOD, DECLARATION], 21:25-21:29",
                    "[PUBLIC, METHOD, DECLARATION], 24:16-24:21",
                    "[PUBLIC, CLASS], 24:22-24:26",
                    "[PUBLIC, CLASS], 24:27-24:33",
                    "[PARAMETER, DECLARATION], 24:35-24:37",
                    "[PUBLIC, CLASS], 24:39-24:43",
                    "[PUBLIC, CLASS], 24:44-24:50",
                    "[PARAMETER, DECLARATION], 24:52-24:54");
    }

    public void testChainTypes3() throws Exception {
        setShowPrependedText(true);
        setInlineHints(true, true, false);
        performTest("Test.java",
                    "package test;\n" +
                    "public class Test<T> {\n" +
                    "    public void test(Test<String> t) {\n" +
                    "        testChain3(testChain2(testChain1(t.run1()\n" +
                    "               .run2()\n" +
                    "               .run3()\n" +
                    "               .run4())));\n" +
                    "    }\n" +
                    "    private Test<Integer> run1() {\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "    private Test<String> run2() {\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "    private Test<Integer> run3() {\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "    private Test<String> run4() {\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "    public Test<Integer> testChain1(Test<String> t1) {\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "    public Test<Number> testChain2(Test<Integer> t1) {\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "    public String testChain3(Test<Number> t1) {\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "}\n",
                    "[PUBLIC, CLASS, DECLARATION], 1:13-1:17",
                    "[PUBLIC, METHOD, DECLARATION], 2:16-2:20",
                    "[PUBLIC, CLASS], 2:21-2:25",
                    "[PUBLIC, CLASS], 2:26-2:32",
                    "[PARAMETER, DECLARATION], 2:34-2:35",
                    "[PUBLIC, METHOD], 3:8-3:18",
                    "[t1:], 3:19-3:20",
                    "[PUBLIC, METHOD], 3:19-3:29",
                    "[t1:], 3:30-3:31",
                    "[PUBLIC, METHOD], 3:30-3:40",
                    "[t1:], 3:41-3:42",
                    "[PRIVATE, METHOD], 3:43-3:47",
                    "[  Test<Integer>], 3:49-4:0",
                    "[PRIVATE, METHOD], 4:16-4:20",
                    "[  Test<String>], 4:22-5:0",
                    "[PRIVATE, METHOD], 5:16-5:20",
                    "[  Test<Integer>], 5:22-6:0",
                    "[PRIVATE, METHOD], 6:16-6:20",
                    "[  Test<String>; Test<Integer>; Test<Number>; String], 6:26-7:0",
                    "[PUBLIC, CLASS], 8:12-8:16",
                    "[PUBLIC, CLASS], 8:17-8:24",
                    "[PRIVATE, METHOD, DECLARATION], 8:26-8:30",
                    "[PUBLIC, CLASS], 11:12-11:16",
                    "[PUBLIC, CLASS], 11:17-11:23",
                    "[PRIVATE, METHOD, DECLARATION], 11:25-11:29",
                    "[PUBLIC, CLASS], 14:12-14:16",
                    "[PUBLIC, CLASS], 14:17-14:24",
                    "[PRIVATE, METHOD, DECLARATION], 14:26-14:30",
                    "[PUBLIC, CLASS], 17:12-17:16",
                    "[PUBLIC, CLASS], 17:17-17:23",
                    "[PRIVATE, METHOD, DECLARATION], 17:25-17:29",
                    "[PUBLIC, CLASS], 20:11-20:15",
                    "[PUBLIC, CLASS], 20:16-20:23",
                    "[PUBLIC, METHOD, DECLARATION], 20:25-20:35",
                    "[PUBLIC, CLASS], 20:36-20:40",
                    "[PUBLIC, CLASS], 20:41-20:47",
                    "[PARAMETER, DECLARATION], 20:49-20:51",
                    "[PUBLIC, CLASS], 23:11-23:15",
                    "[ABSTRACT, PUBLIC, CLASS], 23:16-23:22",
                    "[PUBLIC, METHOD, DECLARATION], 23:24-23:34",
                    "[PUBLIC, CLASS], 23:35-23:39",
                    "[PUBLIC, CLASS], 23:40-23:47",
                    "[PARAMETER, DECLARATION], 23:49-23:51",
                    "[PUBLIC, CLASS], 26:11-26:17",
                    "[PUBLIC, METHOD, DECLARATION], 26:18-26:28",
                    "[PUBLIC, CLASS], 26:29-26:33",
                    "[ABSTRACT, PUBLIC, CLASS], 26:34-26:40",
                    "[PARAMETER, DECLARATION], 26:42-26:44");
    }

    public void testChainTypes4() throws Exception {
        setShowPrependedText(true);
        setInlineHints(true, true, false);
        performTest("Test.java",
                    "package test;\n" +
                    "public class Test<T> {\n" +
                    "    public void test(Test<String> t) {\n" +
                    "        voidMethod(t.run1()\n" +
                    "               .run2()\n" +
                    "               .run3()\n" +
                    "               .run4())));\n" +
                    "        undefinedMethod(t.run1()\n" +
                    "               .run2()\n" +
                    "               .run3()\n" +
                    "               .run4())));\n" +
                    "    }\n" +
                    "    private Test<Integer> run1() {\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "    private Test<String> run2() {\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "    private Test<Integer> run3() {\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "    private Test<String> run4() {\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "    public void voidMethod(Test<String> t1) {\n" +
                    "    }\n" +
                    "}\n",
                    "[PUBLIC, CLASS, DECLARATION], 1:13-1:17",
                    "[PUBLIC, METHOD, DECLARATION], 2:16-2:20",
                    "[PUBLIC, CLASS], 2:21-2:25",
                    "[PUBLIC, CLASS], 2:26-2:32",
                    "[PARAMETER, DECLARATION], 2:34-2:35",
                    "[PUBLIC, METHOD], 3:8-3:18",
                    "[t1:], 3:19-3:20",
                    "[PRIVATE, METHOD], 3:21-3:25",
                    "[  Test<Integer>], 3:27-4:0",
                    "[PRIVATE, METHOD], 4:16-4:20",
                    "[  Test<String>], 4:22-5:0",
                    "[PRIVATE, METHOD], 5:16-5:20",
                    "[  Test<Integer>], 5:22-6:0",
                    "[PRIVATE, METHOD], 6:16-6:20",
                    "[  Test<String>; ], 6:26-7:0",
                    "[STATIC, PUBLIC, CLASS], 7:8-7:23",
                    "[PARAMETER], 7:24-7:25",
                    "[PRIVATE, METHOD], 7:26-7:30",
                    "[  Test<Integer>], 7:32-8:0",
                    "[PRIVATE, METHOD], 8:16-8:20",
                    "[  Test<String>], 8:22-9:0",
                    "[PRIVATE, METHOD], 9:16-9:20",
                    "[  Test<Integer>], 9:22-10:0",
                    "[PRIVATE, METHOD], 10:16-10:20",
                    "[  Test<String>; ], 10:26-11:0",
                    "[PUBLIC, CLASS], 12:12-12:16",
                    "[PUBLIC, CLASS], 12:17-12:24",
                    "[PRIVATE, METHOD, DECLARATION], 12:26-12:30",
                    "[PUBLIC, CLASS], 15:12-15:16",
                    "[PUBLIC, CLASS], 15:17-15:23",
                    "[PRIVATE, METHOD, DECLARATION], 15:25-15:29",
                    "[PUBLIC, CLASS], 18:12-18:16",
                    "[PUBLIC, CLASS], 18:17-18:24",
                    "[PRIVATE, METHOD, DECLARATION], 18:26-18:30",
                    "[PUBLIC, CLASS], 21:12-21:16",
                    "[PUBLIC, CLASS], 21:17-21:23",
                    "[PRIVATE, METHOD, DECLARATION], 21:25-21:29",
                    "[PUBLIC, METHOD, DECLARATION], 24:16-24:26",
                    "[PUBLIC, CLASS], 24:27-24:31",
                    "[PUBLIC, CLASS], 24:32-24:38",
                    "[PARAMETER, DECLARATION], 24:40-24:42");
    }

    public void testRawStringLiteralNETBEANS_5118() throws Exception {
        setSourceLevel("15");
        performTest("RawStringLiteral",
                    "public class RawStringLiteral {\n" +
                    "    String s1 = \"\"\"\n" +
                    "                int i1 = 1;    \n" +
                    "\n" +
                    "     \n" +
                    "     \t\n" +
                    "                  int i2 = 2;\n" +
                    "             \"\"\";\n" +
                    "}\n",
                    "[PUBLIC, CLASS, DECLARATION], 0:13-0:29",
                    "[PUBLIC, CLASS], 1:4-1:10",
                    "[PACKAGE_PRIVATE, FIELD, UNUSED, DECLARATION], 1:11-1:13",
                    "[UNINDENTED_TEXT_BLOCK], 2:13-2:27",
                    "[UNINDENTED_TEXT_BLOCK], 6:13-6:29");
    }

    public void testVar() throws Exception {
        setSourceLevel("11");
        setShowPrependedText(true);
        setInlineHints(true, false, true);
        performTest("Var",
                    "public class Var {\n" +
                    "    private void test(java.util.List<String> l) {\n" +
                    "        var v1 = l.iterator();\n" +
                    "    }\n" +
                    "}\n",
                    "[PUBLIC, CLASS, DECLARATION], 0:13-0:16",
                    "[PRIVATE, METHOD, UNUSED, DECLARATION], 1:17-1:21",
                    "[PUBLIC, INTERFACE], 1:32-1:36",
                    "[PUBLIC, CLASS], 1:37-1:43",
                    "[PARAMETER, DECLARATION], 1:45-1:46",
                    "[LOCAL_VARIABLE, UNUSED, DECLARATION], 2:12-2:14",
                    "[ : Iterator<String>], 2:14-2:15",
                    "[PARAMETER], 2:17-2:18",
                    "[ABSTRACT, PUBLIC, METHOD], 2:19-2:27");
    }

    public void testCaseRuleBodyHighlight() throws Exception {
        performTest("CaseTest",
                """
                public class CaseTest {
                    private void t(Object o) {
                        switch (o) {
                            case Object oo -> {
                                o = null;
                            }
                        }
                    }
                }
                """,
                "[PUBLIC, CLASS, DECLARATION], 0:13-0:21",
                "[PRIVATE, METHOD, UNUSED, DECLARATION], 1:17-1:18",
                "[PUBLIC, CLASS], 1:19-1:25",
                "[PARAMETER, DECLARATION], 1:26-1:27",
                "[PARAMETER], 2:16-2:17",
                "[PUBLIC, CLASS], 3:17-3:23",
                "[LOCAL_VARIABLE, UNUSED, DECLARATION], 3:24-3:26",
                "[PARAMETER], 4:16-4:17");
    }

    private void performTest(String fileName) throws Exception {
        performTest(fileName, new Performer() {
            public void compute(CompilationController parameter, Document doc, final ErrorDescriptionSetter setter) {
                new SemanticHighlighterBase() {
                    @Override
                    protected boolean process(CompilationInfo info, Document doc) {
                        return process(info, doc, setter);
                    }
                }.process(parameter, doc);
            }
        });
    }
    
    private void performTest(String fileName, String content, String expected) throws Exception {
        performTest(fileName, content, new Performer() {
            public void compute(CompilationController parameter, Document doc, final ErrorDescriptionSetter setter) {
                new SemanticHighlighterBase() {
                    @Override
                    protected boolean process(CompilationInfo info, Document doc) {
                        return process(info, doc, setter);
                    }
                }.process(parameter, doc);
            }
        }, false, expected);
    }

    private void performTest(String fileName, String code, String... expected) throws Exception {
        performTest(fileName, code, new Performer() {
            public void compute(CompilationController parameter, Document doc, final ErrorDescriptionSetter setter) {
                new SemanticHighlighterBase() {
                    @Override
                    protected boolean process(CompilationInfo info, Document doc) {
                        return process(info, doc, setter);
                    }
                }.process(parameter, doc);
            }
        }, expected);
    }

    private FileObject testSourceFO;
    
    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
}
