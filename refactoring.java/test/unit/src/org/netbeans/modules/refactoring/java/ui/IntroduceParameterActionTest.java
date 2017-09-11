/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.tree.Tree;
import java.util.concurrent.atomic.AtomicInteger;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
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
