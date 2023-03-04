/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.groovy.editor.api;

import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class AstPathTest extends GroovyTestBase {

    public AstPathTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp(); 
    }

    public void testMiniClass() throws Exception {
        Iterator<ASTNode> it = getPath("testfiles/Hello.groovy", "class FourthClass {^}").iterator();
        assertEquals(ClassNode.class, it.next().getClass());
        assertEquals(ModuleNode.class, it.next().getClass());
        assertFalse(it.hasNext());
    }

    public void testMiniClass2() throws Exception {
        Iterator<ASTNode> it = getPath("testfiles/MiniClass.groovy", "class MiniClass {^}").iterator();
        assertEquals(ClassNode.class, it.next().getClass());
        assertEquals(ModuleNode.class, it.next().getClass());
        assertFalse(it.hasNext());
    }

// Disable this Test till it's fixed.
//    public void testMiniClass3() throws Exception {
//        Iterator<ASTNode> it = getPath("testfiles/MiniClass2.groovy", "class MiniClass2 { Cl^ }").iterator();
//        assertEquals(ClassNode.class, it.next().getClass());
//        assertEquals(ModuleNode.class, it.next().getClass());
//        assertFalse(it.hasNext());
//    }

    public void testScript() throws Exception {
        Iterator<ASTNode> it = getPath("testfiles/GroovyScopeTestcase.groovy", "pri^ntln \"Starting testcase\"").iterator();
        assertEquals(ConstantExpression.class, it.next().getClass());
        assertEquals(MethodCallExpression.class, it.next().getClass());
        assertEquals(ExpressionStatement.class, it.next().getClass());
        assertEquals(BlockStatement.class, it.next().getClass());
        assertEquals(MethodNode.class, it.next().getClass());
        assertEquals(ClassNode.class, it.next().getClass());
        assertEquals(ModuleNode.class, it.next().getClass());
        assertFalse(it.hasNext());
    }
    
    /**
     * Check that normal AST path termiantes as the 'parentFile' constant.
     */
    public void testNormalOnFirstSymbol() throws Exception {
        AstPath path = getPath(
                "testfiles/ASTPath1.groovy", 
                "someFile.parentFil^e.mkdirs()");
        Iterator<ASTNode> it = path.iterator();
        
        assertEquals(ConstantExpression.class, it.next().getClass());
    }

    /**
     * Check that dotted AST path termiantes as the PropertyExpression that points
     * to the 'parentFile' property.
     */
    public void testDotOnFirstSymbol() throws Exception {
        AstPath path = getPath(
                "testfiles/ASTPath1.groovy", 
                "someFile.parentFil^e.mkdirs()", true);
        Iterator<ASTNode> it = path.iterator();
        
        assertEquals(PropertyExpression.class, it.next().getClass());
        PropertyExpression pe = (PropertyExpression)path.leaf();
        
        assertEquals(ConstantExpression.class, pe.getProperty().getClass());
        assertEquals(VariableExpression.class, pe.getObjectExpression().getClass());
        
        assertEquals("parentFile", ((ConstantExpression)pe.getProperty()).getValue());
    }
    
    /**
     * Normal AST path should terminate at the property name 'constant' 
     */
    public void testAfterSecondPropertyRef() throws Exception {
        AstPath path = getPath(
                "testfiles/ASTPath1.groovy", 
                "someFile.absoluteFile.parentFil^e.mkdirs()");
        Iterator<ASTNode> it = path.iterator();
        
        assertEquals(ConstantExpression.class, it.next().getClass());
    }
    
    /**
     * Dotted AST path should end at the PropertyExpression for parentFile.
     */
    public void testDotAfterSecondPropertyRef() throws Exception {
        AstPath path = getPath(
                "testfiles/ASTPath1.groovy", 
                "someFile.absoluteFile.parentFil^e.mkdirs()", true);
        Iterator<ASTNode> it = path.iterator();
        
        assertEquals(PropertyExpression.class, it.next().getClass());
        PropertyExpression pe = (PropertyExpression)path.leaf();
        
        assertEquals(ConstantExpression.class, pe.getProperty().getClass());
        assertEquals(PropertyExpression.class, pe.getObjectExpression().getClass());
        
        assertEquals("parentFile", ((ConstantExpression)pe.getProperty()).getValue());
    }
    
    public void testFirstObjectMethod() throws Exception {
        
        AstPath path = getPath(
                "testfiles/ASTPath1.groovy", 
                "b.command(\"good\"^).inheritIO()");
        ASTNode leaf = path.leaf();
        assertEquals(MethodCallExpression.class, leaf.getClass());
        MethodCallExpression me = (MethodCallExpression)leaf;
        assertEquals(ConstantExpression.class, me.getMethod().getClass());
        assertEquals("command", ((ConstantExpression)me.getMethod()).getValue());
    }
    
    public void testDotFirstObjectMethod() throws Exception {
        
        AstPath path = getPath(
                "testfiles/ASTPath1.groovy", 
                "b.command(\"good\"^).inheritIO()", true);
        ASTNode leaf = path.leaf();
        assertEquals(MethodCallExpression.class, leaf.getClass());
        MethodCallExpression me = (MethodCallExpression)leaf;
        assertEquals(ConstantExpression.class, me.getMethod().getClass());
        assertEquals("command", ((ConstantExpression)me.getMethod()).getValue());
    }
    
    public void testSecondObjectMethod() throws Exception {
        
        AstPath path = getPath(
                "testfiles/ASTPath1.groovy", 
                "b.command(\"good\").inheritIO(^).command");
        ASTNode leaf = path.leaf();
        assertEquals(MethodCallExpression.class, leaf.getClass());
        MethodCallExpression me = (MethodCallExpression)leaf;
        assertEquals(ConstantExpression.class, me.getMethod().getClass());
        assertEquals("inheritIO", ((ConstantExpression)me.getMethod()).getValue());
    }

    public void testDotSecondObjectMethod() throws Exception {
        
        AstPath path = getPath(
                "testfiles/ASTPath1.groovy", 
                "b.command(\"good\").inheritIO(^).command", true);
        ASTNode leaf = path.leaf();
        assertEquals(MethodCallExpression.class, leaf.getClass());
        MethodCallExpression me = (MethodCallExpression)leaf;
        assertEquals(ConstantExpression.class, me.getMethod().getClass());
        assertEquals("inheritIO", ((ConstantExpression)me.getMethod()).getValue());
    }
    
    public void testDotMethodCallWithoutParenthesis() throws Exception {
        AstPath path = getPath(
                "testfiles/ASTPath1.groovy", 
                "aa.command \"bye^\".substring(1)", true);
        ASTNode leaf = path.leaf();
        assertEquals(ConstantExpression.class, leaf.getClass());
    }

    public void testDotMethodCallWithoutParenthesis2() throws Exception {
        AstPath path = getPath(
                "testfiles/ASTPath1.groovy", 
                "aa.command \"hello $dolly !^\".substring(1)", true);
        ASTNode leaf = path.leaf();
        assertEquals(GStringExpression.class, leaf.getClass());
    }

    private AstPath getPath(String relFilePath, final String caretLine) throws Exception {
        return getPath(relFilePath, caretLine, false);
    }

    private AstPath getPath(String relFilePath, final String caretLine, boolean outer) throws Exception {
        FileObject f = getTestFile(relFilePath);
        Source source = Source.create(f);

        final AstPath[] ret = new AstPath[1];
        final CountDownLatch latch = new CountDownLatch(1);
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                GroovyParserResult result = ASTUtils.getParseResult(resultIterator.getParserResult());
                String text = result.getSnapshot().getText().toString();

                int caretDelta = caretLine.indexOf('^');
                assertTrue(caretDelta != -1);
                String realCaretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
                int lineOffset = text.indexOf(realCaretLine);
                assertTrue(lineOffset != -1);

                int caretOffset = lineOffset + caretDelta;

                ModuleNode moduleNode = result.getRootElement().getModuleNode();
                ret[0] = new AstPath(moduleNode, caretOffset, (BaseDocument) result.getSnapshot().getSource().getDocument(true), outer);
                latch.countDown();
            }
        });
        
        latch.await();
        return ret[0];
    }
    
}
