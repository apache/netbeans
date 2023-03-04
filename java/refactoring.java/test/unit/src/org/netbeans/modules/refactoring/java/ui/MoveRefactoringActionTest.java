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
