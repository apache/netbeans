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

public class PHPCodeCompletionNb4503Test extends PHPCodeCompletionTestBase {

    public PHPCodeCompletionNb4503Test(String testName) {
        super(testName);
    }

    public void testNb4503_01() throws Exception {
        testNb4503("            $e->^test(); // test1");
    }

    public void testNb4503_02() throws Exception {
        testNb4503("                $ex->^test(); // test2");
    }

    public void testNb4503_03() throws Exception {
        testNb4503("            testNes^tedFunction2(); // test3");
    }

    public void testNb4503_04() throws Exception {
        testNb4503("        test^NestedFunction(); // test4");
    }

    public void testNb4503_05() throws Exception {
        testNb4503("        $example->^test(); // test5");
    }

    public void testNb4503_06() throws Exception {
        testNb4503("te^stNestedFunction2(); // test6");
    }

    private void testNb4503(String caretLine) throws Exception {
        checkCompletion("testfiles/completion/lib/nb4503/nb4503.php", caretLine, false);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/nb4503"))
            })
        );
    }
}
