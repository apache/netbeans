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


public class PHPCodeCompletionNb5062Test extends PHPCodeCompletionTestBase {

    public PHPCodeCompletionNb5062Test(String testName) {
        super(testName);
    }

    public void testNb5062Static_01() throws Exception {
        testNb5062("$static1->^testMethod();");
    }

    public void testNb5062Static_02() throws Exception {
        testNb5062("$static2->^testMethod();");
    }

    public void testNb5062Static_03() throws Exception {
        testNb5062("$static3->^testMethod();");
    }

    public void testNb5062Self_01() throws Exception {
        testNb5062("$self1->^testMethod();");
    }

    public void testNb5062Self_02() throws Exception {
        testNb5062("$self2->^testMethod();");
    }

    public void testNb5062Self_03() throws Exception {
        testNb5062("$self3->^testMethod();");
    }

    private void testNb5062(String caretLine) throws Exception {
        checkCompletion("testfiles/completion/lib/nb5062/nb5062.php", caretLine, false);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/nb5062"))
            })
        );
    }

}
