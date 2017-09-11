/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.java.editor.base.semantic;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
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

    public void testRetentionPolicy() throws Exception {
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
    
    private FileObject testSourceFO;
    
    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
}
