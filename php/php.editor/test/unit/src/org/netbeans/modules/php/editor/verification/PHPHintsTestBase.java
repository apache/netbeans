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
package org.netbeans.modules.php.editor.verification;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PHPHintsTestBase extends PHPTestBase {
    protected static final String TEST_DIRECTORY = "testfiles/verification/"; //NOI18N

    public PHPHintsTestBase(String testName) {
        super(testName);
    }

    /**
     * Checks hints in a whole file.
     *
     * @param hint Instantion of hint to test.
     * @param fileName Name of the file which is in "<tt>testfiles/verification/</tt>" directory.
     * @throws Exception
     */
    protected void checkHints(Rule hint, String fileName) throws Exception {
        checkHints(hint, fileName, null);
    }

    protected void checkHints(Rule onLineHint, String fileName, String caretLine) throws Exception {
        checkHints(this, onLineHint, getTestDirectory() + fileName, caretLine);
    }

    /**
     * Apply the hint. Run tests for the HintFix. To run the tests, need two
     * files. The first one is the file to apply the hint, actually (e.g.
     * testMyHint.php). The second one is the file for expected results and it
     * has to be named [the first one's name].[test case name].fixed (e.g.
     * testMyHint.php.testFix.fixed).
     *
     * @param hint Instantion of hint to test.
     * @param fileName Name of the file which is in
     * "<tt>testfiles/verification/</tt>" directory.
     * @param caretLine The text contained in the line which has the caret. Add
     * the caret position to "^". e.g. "MyC^lass"
     * @param fixDesc The text contained in the description for the HintFix (see
     * the implementation of {@link org.netbeans.modules.csl.api.HintFix#getDescription})
     * @throws Exception
     */
    protected void applyHint(Rule hint, String fileName, String caretLine, String fixDesc) throws Exception {
        applyHint(this, hint, getTestDirectory() + fileName, caretLine, fixDesc);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), "/" + getTestDirectory()))
            })
        );
    }

    protected String getTestDirectory() {
        return TEST_DIRECTORY;
    }

}
