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


public class PHP80CodeCompletionTest extends PHPCodeCompletionTestBase {

    public PHP80CodeCompletionTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        PHPCodeCompletion.PHP_VERSION = PhpVersion.getDefault();
        super.setUp();
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[]{
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/php80/" + getTestDirName()))
            })
        );
    }

    private String getTestDirName() {
        String name = getName();
        int indexOf = name.indexOf("_");
        if (indexOf != -1) {
            name = name.substring(0, indexOf);
        }
        return name;
    }

    private String getTestPath(String fileName) {
        return String.format("testfiles/completion/lib/php80/%s/%s.php", getTestDirName(), fileName);
    }

    public void testNonCapturingCatches_01() throws Exception {
        checkCompletion(getTestPath("nonCapturingCatches"), "} catch (Ex^ception) { // test1", false);
    }

    public void testNonCapturingCatches_02() throws Exception {
        checkCompletion(getTestPath("nonCapturingCatches"), "} catch (Exception^A | ExceptionB) { // test2 test3", false);
    }

    public void testNonCapturingCatches_03() throws Exception {
        checkCompletion(getTestPath("nonCapturingCatches"), "} catch (ExceptionA | Excep^tionB) { // test2 test3", false);
    }

    public void testNonCapturingCatches_04() throws Exception {
        checkCompletion(getTestPath("nonCapturingCatches"), "} catch (^) { // test4", false);
    }

}
