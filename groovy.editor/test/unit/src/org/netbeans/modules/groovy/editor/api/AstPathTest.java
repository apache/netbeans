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

package org.netbeans.modules.groovy.editor.api;

import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
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

    private AstPath getPath(String relFilePath, final String caretLine) throws Exception {
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
                ret[0] = new AstPath(moduleNode, caretOffset, (BaseDocument) result.getSnapshot().getSource().getDocument(true));
                latch.countDown();
            }
        });
        
        latch.await();
        return ret[0];
    }
    
}
