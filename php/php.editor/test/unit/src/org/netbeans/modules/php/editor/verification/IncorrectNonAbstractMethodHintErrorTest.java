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
package org.netbeans.modules.php.editor.verification;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class IncorrectNonAbstractMethodHintErrorTest extends PHPHintsTestBase {

    public IncorrectNonAbstractMethodHintErrorTest(String testName) {
        super(testName);
    }

    public void testIncorrectNonAbstractMethod() throws Exception {
        checkHints(new IncorrectNonAbstractMethodHintError(), "IncorrectNonAbstractMethod/testIncorrectNonAbstractMethodHint.php");
    }

    public void testIncorrectNonAbstractMethodInClassFix01() throws Exception {
        applyHint(new IncorrectNonAbstractMethodHintError(), "IncorrectNonAbstractMethod/testIncorrectNonAbstractMethodHint.php", "function test^Class1();", "Add body of the method");
    }

    public void testIncorrectNonAbstractMethodInClassFix02() throws Exception {
        applyHint(new IncorrectNonAbstractMethodHintError(), "IncorrectNonAbstractMethod/testIncorrectNonAbstractMethodHint.php", "function test^Class2();", "Add body of the method");
    }

    public void testIncorrectNonAbstractMethodInClassFix03() throws Exception {
        applyHint(new IncorrectNonAbstractMethodHintError(), "IncorrectNonAbstractMethod/testIncorrectNonAbstractMethodHint.php", "function test^Class3();", "Add body of the method");
    }

    public void testIncorrectNonAbstractMethodInClassFix04() throws Exception {
        applyHint(new IncorrectNonAbstractMethodHintError(), "IncorrectNonAbstractMethod/testIncorrectNonAbstractMethodHint.php", "function test^Class4();", "Add body of the method");
    }

    public void testIncorrectNonAbstractMethodInAbstractClassFix01() throws Exception {
        applyHint(new IncorrectNonAbstractMethodHintError(), "IncorrectNonAbstractMethod/testIncorrectNonAbstractMethodHint.php", "public function testAbstra^ctClass();", "Add body of the method");
    }

    public void testIncorrectNonAbstractMethodInTraitFix01() throws Exception {
        applyHint(new IncorrectNonAbstractMethodHintError(), "IncorrectNonAbstractMethod/testIncorrectNonAbstractMethodHint.php", "public function ^testTrait();", "Add body of the method");
    }

    public void testIssue270385Fix01() throws Exception {
        applyHint(new IncorrectNonAbstractMethodHintError(), "IncorrectNonAbstractMethod/testIssue270385.php", "private function tes^tMethod1();", "Add body of the method");
    }

    public void testIssue270385Fix02() throws Exception {
        applyHint(new IncorrectNonAbstractMethodHintError(), "IncorrectNonAbstractMethod/testIssue270385.php", "private function te^stMethod2(): ?array;", "Add body of the method");
    }

    public void testIssue270385Fix03() throws Exception {
        applyHint(new IncorrectNonAbstractMethodHintError(), "IncorrectNonAbstractMethod/testIssue270385.php", "private function tes^tMethod3();", "Add body of the method");
    }

    public void testIssue270385Fix04() throws Exception {
        applyHint(new IncorrectNonAbstractMethodHintError(), "IncorrectNonAbstractMethod/testIssue270385.php", "private function tes^tMethod4(): string", "Add body of the method");
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), "testfiles/verification/IncorrectNonAbstractMethod"))
            })
        );
    }
}
