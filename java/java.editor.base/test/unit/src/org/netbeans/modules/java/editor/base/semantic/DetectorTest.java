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
        performTest("Test.java",
                    "package test;" +
                    "public class Test {" +
                    "    public Test(String param) {" +
                    "    }" +
                    "    public void api(String param1, int param2, int param3, float param4, Object... param5) {" +
                    "    }" +
                    "    private int getValue() {" +
                    "        return -1;" +
                    "    }" +
                    "    private void test() {" +
                    "        new Test(\"\")" +
                    "        api(\"\", 2, getValue(), 1.0f);" +
                    "        api(\"\", 2, getValue(), 1.0f, null);" +
                    "        api(\"\", 2, getValue(), 1.0f, null, null);" +
                    "    }" +
                    "}",
                    "[PUBLIC, CLASS, DECLARATION], 0:26-0:30\n" +
                    "[PUBLIC, CONSTRUCTOR, DECLARATION], 0:43-0:47\n" +
                    "[PUBLIC, CLASS], 0:48-0:54\n" +
                    "[PARAMETER, DECLARATION], 0:55-0:60\n" +
                    "[PUBLIC, METHOD, DECLARATION], 0:84-0:87\n" +
                    "[PUBLIC, CLASS], 0:88-0:94\n" +
                    "[PARAMETER, DECLARATION], 0:95-0:101\n" +
                    "[PARAMETER, DECLARATION], 0:107-0:113\n" +
                    "[PARAMETER, DECLARATION], 0:119-0:125\n" +
                    "[PARAMETER, DECLARATION], 0:133-0:139\n" +
                    "[PUBLIC, CLASS], 0:141-0:147\n" +
                    "[PARAMETER, DECLARATION], 0:151-0:157\n" +
                    "[PRIVATE, METHOD, DECLARATION], 0:181-0:189\n" +
                    "[PRIVATE, METHOD, UNUSED, DECLARATION], 0:233-0:237\n" +
                    "[PUBLIC, CONSTRUCTOR], 0:253-0:257\n" +
                    "[param:], 0:258-0:259\n" +
                    "[PUBLIC, METHOD], 0:269-0:272\n" +
                    "[param1:], 0:273-0:274\n" +
                    "[param2:], 0:277-0:278\n" +
                    "[PRIVATE, METHOD], 0:280-0:288\n" +
                    "[param4:], 0:292-0:293\n" +
                    "[PUBLIC, METHOD], 0:306-0:309\n" +
                    "[param1:], 0:310-0:311\n" +
                    "[param2:], 0:314-0:315\n" +
                    "[PRIVATE, METHOD], 0:317-0:325\n" +
                    "[param4:], 0:329-0:330\n" +
                    "[param5:], 0:335-0:336\n" +
                    "[PUBLIC, METHOD], 0:349-0:352\n" +
                    "[param1:], 0:353-0:354\n" +
                    "[param2:], 0:357-0:358\n" +
                    "[PRIVATE, METHOD], 0:360-0:368\n" +
                    "[param4:], 0:372-0:373\n" +
                    "[param5:], 0:378-0:379\n" +
                    "[param5:], 0:384-0:385\n");
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

    public void testYield() throws Exception {
        enablePreview();
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
        try {
            SourceVersion.valueOf("RELEASE_13");
        } catch (IllegalArgumentException iae) {
            //OK, presumably no support for raw string literals
            return ;
        }
        setSourceLevel("13");
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
                    "[PACKAGE_PRIVATE, FIELD, DECLARATION], 1:11-1:13",
                    "[UNINDENTED_TEXT_BLOCK], 2:13-2:27",
                    "[UNINDENTED_TEXT_BLOCK], 3:13-3:29",
                    "[PUBLIC, CLASS], 5:4-5:10",
                    "[PACKAGE_PRIVATE, FIELD, DECLARATION], 5:11-5:13",
                    "[UNINDENTED_TEXT_BLOCK], 6:16-6:27",
                    "[UNINDENTED_TEXT_BLOCK], 7:16-7:29");
    }

    public void testBindingPattern() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_14");
        } catch (IllegalArgumentException iae) {
            //OK, presumably no support for raw string literals
            return ;
        }
        setSourceLevel("14");
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
