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
package org.netbeans.modules.cnd.highlight.semantic;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.highlight.semantic.debug.InterrupterImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 */
public class MarkOccurrencesTest extends SemanticHighlightingTestBase {

    public MarkOccurrencesTest(String testName) {
        super(testName);
    }
    private static final String SOURCE = "markocc.cc"; // NOI18N

    @Override
    protected void setUp() throws Exception {
        try {
            super.setUp();
        } catch (IOException e) {
            final String message = e.getMessage();
            if (message != null && message.startsWith("Cannot delete file")) { // NOI18N
                Logger.getInstance().log(Level.INFO, "MarkOccurrencesTest {0}", e);
            } else {
                throw e;
            }
        }
    }
    
    public void testMacro() throws Exception {
        // MOO 
        performTest(SOURCE, 22, 15);
    }

    public void testLocalVariable() throws Exception {
        performTest(SOURCE, 23, 18);
    }

    public void testGlobalVariable() throws Exception {
        // int bar
         performTest(SOURCE, 25, 8);
    }

    public void testField() throws Exception {
        //boo 
        performTest(SOURCE, 14, 14);
    }

    public void testCtor() throws Exception {
        // Foo() 
        performTest(SOURCE, 14, 7);
    }

    public void testCtor2() throws Exception {
        // Foo(int) 
        performTest(SOURCE, 17, 6);
    }

    public void testClassName() throws Exception {
        // class Foo 
        performTest(SOURCE, 14, 2);
    }

    public void testPreproc1() throws Exception {
        performTest(SOURCE, 29, 1);
        clearWorkDir();
        performTest(SOURCE, 31, 3);
        clearWorkDir();
        performTest(SOURCE, 37, 3);
        clearWorkDir();
        performTest(SOURCE, 43, 7);
    }

    public void testPreproc5() throws Exception {
        performTest(SOURCE, 33, 3);
    }

    public void testPreproc6() throws Exception {
        performTest(SOURCE, 41, 5);
    }

    public void testSeveralDeclarations() throws Exception {
        performTest(SOURCE, 47, 10);
    }

    public void testConstAndNonConstMethods() throws Exception {
        performTest(SOURCE, 57, 15);
    }

    public void testIZ175700() throws Exception {
        // IZ#175700 : [code model] Parser does not recognized inline initialization in constructor
        performTest(SOURCE, 79, 5);
    }
    
    public void testStringLiterals() throws Exception {
        performTest(SOURCE, 83, 16);
    }
    
    public void testCharLiterals() throws Exception {
        performTest(SOURCE, 93, 30);
    }

    public void testAddSymbolZeroParams() throws Exception {
        performTest(SOURCE, 106, 15);
        clearWorkDir();
        performTest(SOURCE, 115, 25);
        clearWorkDir();
        performTest(SOURCE, 138, 34);
    }
    
    public void testAddSymbolOneParam() throws Exception {
        performTest(SOURCE, 107, 15);
        clearWorkDir();
        performTest(SOURCE, 120, 25);
        clearWorkDir();
        performTest(SOURCE, 137, 34);
    }
    
    public void testAddSymbolTwoParams() throws Exception {
        performTest(SOURCE, 108, 15);
        clearWorkDir();
        performTest(SOURCE, 125, 25);
        clearWorkDir();
        performTest(SOURCE, 140, 35);
        clearWorkDir();
    }
    
    public void test206416_1() throws Exception {
        if (!Utilities.isWindows()) { // somehow it is failing on windows due to not removed file
            // #206416 - Renaming a local variable in C/C++ code changes the name of other variables with the same name in other scopes
            performTest(SOURCE, 147, 14);
            clearWorkDir();
            performTest(SOURCE, 151, 10);
        }
    }
    
    public void test206416_2() throws Exception {
        // #206416 - Renaming a local variable in C/C++ code changes the name of other variables with the same name in other scopes
        performTest(SOURCE, 148, 19);
        clearWorkDir();
        performTest(SOURCE, 149, 20);
    }
    
    public void test206416_3() throws Exception {
        // #206416 - Renaming a local variable in C/C++ code changes the name of other variables with the same name in other scopes
        performTest(SOURCE, 154, 14);
        clearWorkDir();
        performTest(SOURCE, 155, 10);
        clearWorkDir();
    }
    
    public void testAddSymbolMoreParams() throws Exception {
        performTest(SOURCE, 110, 15);
        clearWorkDir();
        performTest(SOURCE, 131, 25);
        clearWorkDir();
        performTest(SOURCE, 139, 35);
        clearWorkDir();
    }
    
    public void test231272_1() throws Exception {
        performTest(SOURCE, 165, 15);
        clearWorkDir();
        performTest(SOURCE, 182, 20);
    }
    
    public void test231272_2() throws Exception {
        performTest(SOURCE, 172, 15);
        clearWorkDir();
        performTest(SOURCE, 182, 10);
    }

    @Override
    protected Collection<? extends CsmOffsetable> getBlocks(FileImpl testFile, int offset) {
        BaseDocument doc;
        try {
            doc = getBaseDocument(FileUtil.toFile(testFile.getFileObject()));
        } catch (Exception e) {
            e.printStackTrace(System.err);
            doc = null;
        }
        return MarkOccurrencesHighlighter.getOccurrences(doc, testFile, offset, new InterrupterImpl());
    }
}
