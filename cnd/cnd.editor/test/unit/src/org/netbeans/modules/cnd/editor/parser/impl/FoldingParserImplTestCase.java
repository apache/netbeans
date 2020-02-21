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
package org.netbeans.modules.cnd.editor.parser.impl;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.editor.Analyzer;
import org.netbeans.modules.cnd.editor.parser.CppFoldRecord;
import org.netbeans.modules.cnd.test.CndBaseTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 */
public class FoldingParserImplTestCase extends CndBaseTestCase {

    private static final boolean TRACE = false;

    /**
     * Creates a new instance of ModelImplBaseTestCase
     */
    public FoldingParserImplTestCase(String testName) {
        super(testName);
    }

    public void testIfdefFolding() throws Exception {
        performTest("ifdef.cc");
    }

    public void testSimpleFolding() throws Exception {
        performTest("simpleFolding.cc");
    }

    public void testErrorDirective() throws Exception {
        performTest("error_directive.cc");
    }

    public void testLastIncludes() throws Exception {
        performTest("lastIncludes.cc");
    }

    public void testMixedPrepocDirectives() throws Exception {
        performTest("mixedPreprocDirectives.cc");
    }

    public void testExternC() throws Exception {
        performTest("extern_c.cc");
    }

    private void performTest(String source) throws Exception {
        if (TRACE) {
            System.out.println(getWorkDir());
        }
        File testSourceFile = getDataFile(source);
        FileObject fo = FileUtil.toFileObject(testSourceFile);
        char[] text = Analyzer.loadFile(testSourceFile.getAbsolutePath());
        FoldingParserService foldingParserService = new FoldingParserService();
        List<CppFoldRecord> folds = foldingParserService.parse(fo, text);
        Collections.sort(folds, FOLD_COMPARATOR);
        for (CppFoldRecord fold : folds) {
            ref(fold.toString());
        }
        compareReferenceFiles();
    }
    private static final Comparator<CppFoldRecord> FOLD_COMPARATOR = new Comparator<CppFoldRecord>() {

        @Override
        public int compare(CppFoldRecord o1, CppFoldRecord o2) {
            return o1.getStartOffset() - o2.getStartOffset();
        }
    };
}
