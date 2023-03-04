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
package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.tree.Tree;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.java.test.RefactoringTestBase;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 *
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
public class IntroduceParameterActionTest extends RefactoringTestBase {

    public IntroduceParameterActionTest(String name) {
        super(name);
    }

    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public void testIPvar() throws Exception {
        String file;
        writeFilesAndWaitForScan(src,
                new RefactoringTestBase.File("t/A.java", file = "package t;\n"
                        + "\n"
                        + "public class A {\n"
                        + "\n"
                        + "  public void m1(){\n"
                        + "    m2(1);\n"
                        + "  }\n"
                        + "\n"
                        + "  public void m2(int i){\n"
                        + "    if (i > 5) {\n"
                        + "      System.out.println(\"abcd\");\n"
                        + "    }\n"
                        + "  }\n"
                        + "}\n"
                        + "class B {\n"
                        + "}\n"));
        FileObject testFile = src.getFileObject("t/A.java");
        DataObject testFileDO = DataObject.find(testFile);
        EditorCookie ec = testFileDO.getLookup().lookup(EditorCookie.class);
        ec.open();
        final AtomicInteger called = new AtomicInteger();
        int expectedCount = 0;
        
        final TreePathHandle[] handle = {null};
        ContextAnalyzer.SHOW = new ContextAnalyzer.ShowUI() {
            @Override public void show(RefactoringUI ui, TopComponent activetc) {
                assertTrue(ui instanceof IntroduceParameterUI);
                handle[0] = ui.getRefactoring().getRefactoringSource().lookup(TreePathHandle.class);
                called.incrementAndGet();
            }
        };
        
        // a. Parameter i is selected and caret is at position (i| > 5)
        ec.getOpenedPanes()[0].setCaretPosition(99);
        ec.getOpenedPanes()[0].moveCaretPosition(100);
        new JavaRefactoringActionsProvider().doIntroduceParameter(Lookups.fixed(ec));
        assertEquals(++expectedCount, called.get());
        assertEquals(Tree.Kind.IDENTIFIER, handle[0].getKind());

        // b. Parameter i is selected and caret is at position (|i > 5)
        ec.getOpenedPanes()[0].setCaretPosition(100);
        ec.getOpenedPanes()[0].moveCaretPosition(99);
        new JavaRefactoringActionsProvider().doIntroduceParameter(Lookups.fixed(ec));
        assertEquals(++expectedCount, called.get());
        assertEquals(Tree.Kind.IDENTIFIER, handle[0].getKind());
        
        // c. Nothing is selected and caret is at position (|i > 5)
        ec.getOpenedPanes()[0].setCaretPosition(99);
        new JavaRefactoringActionsProvider().doIntroduceParameter(Lookups.fixed(ec));
        assertEquals(++expectedCount, called.get());
        assertEquals(Tree.Kind.IDENTIFIER, handle[0].getKind());
        
        // d. Nothing is selected and caret is at position (i| > 5)
        ec.getOpenedPanes()[0].setCaretPosition(100);
        new JavaRefactoringActionsProvider().doIntroduceParameter(Lookups.fixed(ec));
        assertEquals(++expectedCount, called.get());
        assertEquals(Tree.Kind.IDENTIFIER, handle[0].getKind());
        
        // e. Nothing is selected and caret is at position (i > |5)
        ec.getOpenedPanes()[0].setCaretPosition(103);
        new JavaRefactoringActionsProvider().doIntroduceParameter(Lookups.fixed(ec));
        assertEquals(++expectedCount, called.get());
        assertEquals(Tree.Kind.INT_LITERAL, handle[0].getKind());
        
        // f. Nothing is selected and caret is at position (i > 5|)
        ec.getOpenedPanes()[0].setCaretPosition(104);
        new JavaRefactoringActionsProvider().doIntroduceParameter(Lookups.fixed(ec));
        assertEquals(++expectedCount, called.get());
        assertEquals(Tree.Kind.INT_LITERAL, handle[0].getKind());
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }
}
