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
        checkHints(this, onLineHint, TEST_DIRECTORY + fileName, caretLine);
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
        applyHint(this, hint, TEST_DIRECTORY + fileName, caretLine, fixDesc);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), "/" + TEST_DIRECTORY))
            })
        );
    }

}
