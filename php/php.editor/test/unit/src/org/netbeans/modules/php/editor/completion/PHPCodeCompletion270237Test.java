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

public class PHPCodeCompletion270237Test extends PHPCodeCompletionTestBase {

    public PHPCodeCompletion270237Test(String testName) {
        super(testName);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[]{
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/tests270237/"))
            })
        );
    }

    public void testIssue270237_01a() throws Exception {
        checkCompletionCustomTemplateResult("testfiles/completion/lib/tests270237/issue270237.php", "test^",
                new DefaultFilter(PhpVersion.PHP_71, "test71"), true);
    }

    public void testIssue270237_01b() throws Exception {
        checkCompletionCustomTemplateResult("testfiles/completion/lib/tests270237/issue270237.php", "test^",
                new DefaultFilter(PhpVersion.PHP_55, "test71"), true);
    }

    public void testIssue270237_02a() throws Exception {
        checkCompletionCustomTemplateResult("testfiles/completion/lib/tests270237/issue270237.php", "test^",
                new DefaultFilter(PhpVersion.PHP_70, "test70"), true);
    }

    public void testIssue270237_02b() throws Exception {
        checkCompletionCustomTemplateResult("testfiles/completion/lib/tests270237/issue270237.php", "test^",
                new DefaultFilter(PhpVersion.PHP_56, "test70"), true);
    }

}
