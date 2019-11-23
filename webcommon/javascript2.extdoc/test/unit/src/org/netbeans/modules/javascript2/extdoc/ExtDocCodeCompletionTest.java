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
package org.netbeans.modules.javascript2.extdoc;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.netbeans.modules.javascript2.editor.JsCodeCompletionBase;
import org.netbeans.modules.parsing.api.Source;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author Martin Fousek <marfous@oracle.com>
 */
public class ExtDocCodeCompletionTest extends JsCodeCompletionBase {

    public ExtDocCodeCompletionTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Files.copy(
            new File(getDataDir(), "../../../testfiles/jsdoc-testfiles/classWithExtDoc.js").toPath(),
            new File(getDataDir(), "testfiles/extdoc/classWithExtDoc.js").toPath(),
            StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    protected File getDataFile(String relFilePath) {
        // CslTestBase loads test file and reference file from different locations
        // this breaks our assumption, that we can prepare the JS on-the-fly in the
        // build directory. This redirects the resolution of the reference files
        // to the build directory (they are also copied on test begin)
        return FileUtil.toFile(getTestFile(relFilePath));
    }

    public void testAllCompletion() throws Exception {
        checkCompletion("testfiles/extdoc/classWithExtDoc.js", " * @^param {int} One The first number to add", false);
    }

    public void testNoCompletion() throws Exception {
        checkCompletion("testfiles/extdoc/classWithExtDoc.js", " * ^@param {int} One The first number to add", false);
    }

    public void testParamCompletion2() throws Exception {
        checkCompletion("testfiles/extdoc/classWithExtDoc.js", " * @p^aram {int} One The first number to add", false);
    }

    public void testParamCompletion3() throws Exception {
        checkCompletion("testfiles/extdoc/classWithExtDoc.js", " * @pa^ram {int} One The first number to add", false);
    }

    public void testParamCompletion4() throws Exception {
        checkCompletion("testfiles/extdoc/classWithExtDoc.js", " * @par^am {int} One The first number to add", false);
    }

    public void testParamCompletion5() throws Exception {
        checkCompletion("testfiles/extdoc/classWithExtDoc.js", " * @para^m {int} One The first number to add", false);
    }

    public void testParamCompletion6() throws Exception {
        checkCompletion("testfiles/extdoc/classWithExtDoc.js", " * @param^ {int} One The first number to add", false);
    }

    public void testMethodCompletion1() throws Exception {
        checkCompletion("testfiles/extdoc/classWithExtDoc.js", " * @m^e", false);
    }

    public void testMethodCompletion2() throws Exception {
        checkCompletion("testfiles/extdoc/classWithExtDoc.js", " * @me^", false);
    }

}
