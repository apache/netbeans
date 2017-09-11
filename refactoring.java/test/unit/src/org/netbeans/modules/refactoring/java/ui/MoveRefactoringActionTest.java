/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.ui;

import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.refactoring.java.test.RefactoringTestBase;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 *
 * @author Ralph Ruijs
 */
public class MoveRefactoringActionTest extends RefactoringTestBase {

    public MoveRefactoringActionTest(String name) {
        super(name);
    }

    public void testMoveMemberAction() throws Exception {
        String file;
        writeFilesAndWaitForScan(src,
                new RefactoringTestBase.File("t/A.java", file = "package t;\n"
                + "public class A {\n"
                + "    public static void foo() {}\n"
                + "    static class C {\n"
                + "    }\n"
                + "}\n"
                + "class B {\n"
                + "}\n"));
        FileObject testFile = src.getFileObject("t/A.java");
        DataObject testFileDO = DataObject.find(testFile);
        EditorCookie ec = testFileDO.getLookup().lookup(EditorCookie.class);
        ec.open();
        final AtomicInteger called = new AtomicInteger();
        int expectedCount = 0;
        
        ec.getOpenedPanes()[0].setCaretPosition(file.indexOf("ass B"));
        ContextAnalyzer.SHOW = new ContextAnalyzer.ShowUI() {
            @Override public void show(RefactoringUI ui, TopComponent activetc) {
                assertTrue(ui instanceof MoveClassUI);
                called.incrementAndGet();
            }
        };
        new RefactoringActionsProvider().doMove(Lookups.fixed(ec));
        assertEquals(++expectedCount, called.get());
        
        ec.getOpenedPanes()[0].setCaretPosition(file.indexOf("ass C"));
        ContextAnalyzer.SHOW = new ContextAnalyzer.ShowUI() {
            @Override public void show(RefactoringUI ui, TopComponent activetc) {
                assertTrue(ui instanceof MoveMembersUI);
                called.incrementAndGet();
            }
        };
        new RefactoringActionsProvider().doMove(Lookups.fixed(ec));
        assertEquals(++expectedCount, called.get());

        ec.getOpenedPanes()[0].setCaretPosition(file.indexOf("oo()"));
        ContextAnalyzer.SHOW = new ContextAnalyzer.ShowUI() {
            @Override public void show(RefactoringUI ui, TopComponent activetc) {
                assertTrue(ui instanceof MoveMembersUI);
                called.incrementAndGet();
            }
        };
        new RefactoringActionsProvider().doMove(Lookups.fixed(ec));
        assertEquals(++expectedCount, called.get());
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }
}
