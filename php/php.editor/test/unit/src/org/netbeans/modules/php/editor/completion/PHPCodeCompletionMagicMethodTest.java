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
package org.netbeans.modules.php.editor.completion;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class PHPCodeCompletionMagicMethodTest extends PHPCodeCompletionTestBase {

    public PHPCodeCompletionMagicMethodTest(String testName) {
        super(testName);
    }

    public void testMagicMethodsCustomTemplate_PHP56() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("magicMethods"), "    __^",
                new DefaultFilter(PhpVersion.PHP_56, "__"), true);
    }

    public void testMagicMethodsCustomTemplate_PHP70() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("magicMethods"), "    __^",
                new DefaultFilter(PhpVersion.PHP_70, "__"), true);
    }

    public void testMagicMethodsCustomTemplate_PHP71() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("magicMethods"), "    __^",
                new DefaultFilter(PhpVersion.PHP_71, "__"), true);
    }

    public void testMagicMethodsCustomTemplate_PHP72() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("magicMethods"), "    __^",
                new DefaultFilter(PhpVersion.PHP_72, "__"), true);
    }

    public void testMagicMethodsCustomTemplate_PHP73() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("magicMethods"), "    __^",
                new DefaultFilter(PhpVersion.PHP_73, "__"), true);
    }

    public void testMagicMethodsCustomTemplate_PHP74() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("magicMethods"), "    __^",
                new DefaultFilter(PhpVersion.PHP_74, "__"), true);
    }

    public void testMagicMethodsCustomTemplate_PHP80() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("magicMethods"), "    __^",
                new DefaultFilter(PhpVersion.PHP_80, "__"), true);
    }

    public void testMagicMethodsCustomTemplate_PHP81() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("magicMethods"), "    __^",
                new DefaultFilter(PhpVersion.PHP_81, "__"), true);
    }

    public void testMagicMethodsCustomTemplate_PHP82() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("magicMethods"), "    __^",
                new DefaultFilter(PhpVersion.PHP_82, "__"), true);
    }

    public void testMagicMethodsCustomTemplate_PHP83() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("magicMethods"), "    __^",
                new DefaultFilter(PhpVersion.PHP_83, "__"), true);
    }

    public void testToStringCustomTemplate_01_PHP83() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("toString01"), "    __^",
                new DefaultFilter(PhpVersion.PHP_83, "__toString"), true);
    }

    public void testToStringCustomTemplate_02_PHP83() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("toString02"), "    __^",
                new DefaultFilter(PhpVersion.PHP_83, "__toString"), true);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[]{
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/magicMethods/" + getTestDirName()))
            })
        );
    }

    private String getTestPath(String fileName) {
        return String.format("testfiles/completion/lib/magicMethods/%s/%s.php", getTestDirName(), fileName);
    }
}
