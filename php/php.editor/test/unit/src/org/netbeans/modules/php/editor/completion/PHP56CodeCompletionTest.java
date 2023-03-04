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

    public void testClassConst_01() throws Exception {
        checkCompletion("testfiles/completion/lib/php56/classConst.php", "       echo self::INDEX^;", false);
    }

    public void testClassConst_02() throws Exception {
        checkCompletion("testfiles/completion/lib/php56/classConst.php", "        echo self::ALICE^;", false);
    }

    public void testClassConst_03() throws Exception {
        checkCompletion("testfiles/completion/lib/php56/classConst.php", "        echo self::PLANET^;", false);
    }

    public void testClassConst_04() throws Exception {
        checkCompletion("testfiles/completion/lib/php56/classConst.php", "        echo self::NO_KEYS^;", false);
    }

    public void testClassConst_05() throws Exception {
        checkCompletion("testfiles/completion/lib/php56/classConst.php", "       echo self::WITH_KEYS^;", false);
    }

    public void testClassConst_06() throws Exception {
        checkCompletion("testfiles/completion/lib/php56/classConst.php", "       echo self::LONG_ARRAY^;", false);
    }

    public void testClassConst_07() throws Exception {
        checkCompletion("testfiles/completion/lib/php56/classConst.php", "       echo self::CONST_REF^;", false);
    }

    public void testGlobalConst_01() throws Exception {
        checkCompletion("testfiles/completion/lib/php56/globalConst.php", "echo INDEX^;", false);
    }

    public void testGlobalConst_02() throws Exception {
        checkCompletion("testfiles/completion/lib/php56/globalConst.php", "echo ALICE^;", false);
    }

    public void testGlobalConst_03() throws Exception {
        checkCompletion("testfiles/completion/lib/php56/globalConst.php", "echo PLANET^;", false);
    }

    public void testGlobalConst_04() throws Exception {
        checkCompletion("testfiles/completion/lib/php56/globalConst.php", "echo NO_KEYS^;", false);
    }

    public void testGlobalConst_05() throws Exception {
        checkCompletion("testfiles/completion/lib/php56/globalConst.php", "echo WITH_KEYS^;", false);
    }

    public void testGlobalConst_06() throws Exception {
        checkCompletion("testfiles/completion/lib/php56/globalConst.php", "echo LONG_ARRAY^;", false);
    }

    public void testGlobalConst_07() throws Exception {
        checkCompletion("testfiles/completion/lib/php56/globalConst.php", "echo CONST_REF^;", false);
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
