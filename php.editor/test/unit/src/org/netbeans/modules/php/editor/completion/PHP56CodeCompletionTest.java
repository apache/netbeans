/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.completion;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PHP56CodeCompletionTest extends PHPCodeCompletionTestBase {

    public PHP56CodeCompletionTest(String testName) {
        super(testName);
    }

    public void testUseFuncAndConst_01() throws Exception {
        checkCompletion("testfiles/completion/lib/php56/useFuncAndConst.php", "echo FO^O;", false);
    }

    public void testUseFuncAndConst_02() throws Exception {
        checkCompletion("testfiles/completion/lib/php56/useFuncAndConst.php", "echo FOO^2;", false);
    }

    public void testUseFuncAndConst_03() throws Exception {
        checkCompletion("testfiles/completion/lib/php56/useFuncAndConst.php", "fn^c();", false);
    }

    public void testUseFuncAndConst_04() throws Exception {
        checkCompletion("testfiles/completion/lib/php56/useFuncAndConst.php", "fnc^2();", false);
    }

    public void testUseFuncAndConst_05() throws Exception {
        checkCompletion("testfiles/completion/lib/php56/useFuncAndConst.php", "use const Name\\Space\\^FOO;", false);
    }

    public void testUseFuncAndConst_06() throws Exception {
        checkCompletion("testfiles/completion/lib/php56/useFuncAndConst.php", "use const Name\\Space\\F^OO as FOO2;", false);
    }

    public void testUseFuncAndConst_07() throws Exception {
        checkCompletion("testfiles/completion/lib/php56/useFuncAndConst.php", "use function Name\\Space\\^fnc;", false);
    }

    public void testUseFuncAndConst_08() throws Exception {
        checkCompletion("testfiles/completion/lib/php56/useFuncAndConst.php", "use function Name\\Space\\f^nc as fnc2;", false);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/php56/"))
            })
        );
    }

}
