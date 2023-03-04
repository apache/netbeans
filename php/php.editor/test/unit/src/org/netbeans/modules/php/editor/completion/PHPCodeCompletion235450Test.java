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

public class PHPCodeCompletion235450Test extends PHPCodeCompletionTestBase {

    public PHPCodeCompletion235450Test(String testName) {
        super(testName);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[]{
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/tests235450/"))
            })
        );
    }

    // check in not the platform i.e. names are not converted to lowercase letters
    // XXX how to check platform constatns?
    public void testLowercase_01() throws Exception {
        checkCompletion("testfiles/completion/lib/tests235450/issue235450.php", "$a = ^true;", false);
    }

    public void testLowercase_02() throws Exception {
        checkCompletion("testfiles/completion/lib/tests235450/issue235450.php", "$a = tru^e;", false);
    }

    public void testLowercase_03() throws Exception {
        checkCompletion("testfiles/completion/lib/tests235450/issue235450.php", "$b = fal^se;", false);
    }

    public void testLowercase_04() throws Exception {
        checkCompletion("testfiles/completion/lib/tests235450/issue235450.php", "$c = nul^l;", false);
    }

    public void testLowercase_05() throws Exception {
        checkCompletion("testfiles/completion/lib/tests235450/issue235450.php", "ConstantClass::T^RUE;", false);
    }

    public void testLowercase_06() throws Exception {
        checkCompletion("testfiles/completion/lib/tests235450/issue235450.php", "ConstantClass::FAL^SE;", false);
    }

    public void testLowercase_07() throws Exception {
        checkCompletion("testfiles/completion/lib/tests235450/issue235450.php", "ConstantClass::NU^LL;", false);
    }

}
