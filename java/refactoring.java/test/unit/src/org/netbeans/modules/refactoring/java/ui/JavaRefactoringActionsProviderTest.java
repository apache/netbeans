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
import static org.junit.Assert.*;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 *
 * @author lahvac
 */
public class JavaRefactoringActionsProviderTest extends RefactoringTestBase {

    public JavaRefactoringActionsProviderTest(String name) {
        super(name);
    }
    
    public void test211193() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public static void foo() {\n"
                + "        int someArray[] = {};\n"
                + "    }\n"
                + "}"));
        FileObject testFile = src.getFileObject("t/A.java");
        DataObject testFileDO = DataObject.find(testFile);
        EditorCookie ec = testFileDO.getLookup().lookup(EditorCookie.class);
        ec.open();
        ec.getOpenedPanes()[0].setCaretPosition(71);
        ec.getOpenedPanes()[0].moveCaretPosition(80);
        final AtomicInteger called = new AtomicInteger();
        ContextAnalyzer.SHOW = new ContextAnalyzer.ShowUI() {
            @Override
            public void show(RefactoringUI ui, TopComponent activetc) {
                assertNotNull(ui);
                called.incrementAndGet();
            }
        };
        int expectedCount = 0;
        new RefactoringActionsProvider().doRename(Lookups.fixed(ec));
        assertEquals(++expectedCount, called.get());
    }

    public void test190101() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("t/A.java", "package t;\n" +
                                                      "//public class A {\n" +
                                                      "    public static void foo() {}\n" +
                                                      "}"));
        FileObject testFile = src.getFileObject("t/A.java");
        DataObject testFileDO = DataObject.find(testFile);
        EditorCookie ec = testFileDO.getLookup().lookup(EditorCookie.class);
        ec.open();
        ec.getOpenedPanes()[0].setCaretPosition(30);
        final AtomicInteger called = new AtomicInteger();
        ContextAnalyzer.SHOW = new ContextAnalyzer.ShowUI() {
            @Override
            public void show(RefactoringUI ui, TopComponent activetc) {
                assertNull(ui);
                called.incrementAndGet();
            }
        };
        int expectedCount = 0;
        new JavaRefactoringActionsProvider().doChangeParameters(Lookups.fixed(ec));
        assertEquals(++expectedCount, called.get());
        new JavaRefactoringActionsProvider().doEncapsulateFields(Lookups.fixed(ec));
        assertEquals(++expectedCount, called.get());
        new JavaRefactoringActionsProvider().doExtractInterface(Lookups.fixed(ec));
        assertEquals(++expectedCount, called.get());
        new JavaRefactoringActionsProvider().doExtractSuperclass(Lookups.fixed(ec));
        assertEquals(++expectedCount, called.get());
        new JavaRefactoringActionsProvider().doInnerToOuter(Lookups.fixed(ec));
        assertEquals(++expectedCount, called.get());
        new JavaRefactoringActionsProvider().doPullUp(Lookups.fixed(ec));
        assertEquals(++expectedCount, called.get());
        new JavaRefactoringActionsProvider().doPushDown(Lookups.fixed(ec));
        assertEquals(++expectedCount, called.get());
        new JavaRefactoringActionsProvider().doUseSuperType(Lookups.fixed(ec));
        assertEquals(++expectedCount, called.get());
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

}