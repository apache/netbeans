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
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PSR0HintTest extends PHPHintsTestBase {

    public PSR0HintTest(String testName) {
        super(testName);
    }

    public void testNsOkClassOk() throws Exception {
        checkHints(new PSR0Hint.NamespaceDeclarationHint(), "FirstNs/SecondNs/ClassName.php");
    }

    public void testNsOkClassOk_01() throws Exception {
        checkHints(new PSR0Hint.NamespaceDeclarationHint(), "FirstNs/SecondNs/Some/Class.php");
    }

    public void testNsNotOkClassNotOk() throws Exception {
        checkHints(new PSR0Hint.NamespaceDeclarationHint(), "FirstNs/SecondNs/WrongName.php");
    }

    public void testNsOkClassNotOk() throws Exception {
        checkHints(new PSR0Hint.NamespaceDeclarationHint(), "FirstNs/SecondNs/WrongName.php");
    }

    public void testNsOkClassNotOk_01() throws Exception {
        checkHints(new PSR0Hint.NamespaceDeclarationHint(), "FirstNs/SecondNs/SomeClass.php");
    }

    public void testIssue246997() throws Exception {
        checkHints(new PSR0Hint.NamespaceDeclarationHint(), "SomeClass.php");
    }

    public void testNsOkClassOk_type() throws Exception {
        checkHints(new PSR0Hint.TypeDeclarationHint(), "FirstNs/SecondNs/ClassName.php");
    }

    public void testNsOkClassOk_01_type() throws Exception {
        checkHints(new PSR0Hint.TypeDeclarationHint(), "FirstNs/SecondNs/Some/Class.php");
    }

    public void testNsNotOkClassNotOk_type() throws Exception {
        checkHints(new PSR0Hint.TypeDeclarationHint(), "FirstNs/SecondNs/WrongName.php");
    }

    public void testNsOkClassNotOk_type() throws Exception {
        checkHints(new PSR0Hint.TypeDeclarationHint(), "FirstNs/SecondNs/WrongName.php");
    }

    public void testNsOkClassNotOk_01_type() throws Exception {
        checkHints(new PSR0Hint.TypeDeclarationHint(), "FirstNs/SecondNs/SomeClass.php");
    }

    @Override
    protected void checkHints(Rule hint, String fileName) throws Exception {
        super.checkHints(hint, getTestDir() + "/" + fileName);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), "/" + TEST_DIRECTORY + getTestDir()))
            })
        );
    }

    private String getTestDir() {
        return "PSR0/" + getName();
    }

}
