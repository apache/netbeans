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

public class PSR4HintTest extends PHPHintsTestBase {

    public PSR4HintTest(String testName) {
        super(testName);
    }

    public void testNamespaceOk() throws Exception {
        checkHints(new PSR4Hint.NamespaceDeclarationHint(), "Vendor/App/ClassName.php");
    }

    public void testNamespaceDifferent() throws Exception {
        checkHints(new PSR4Hint.NamespaceDeclarationHint(), "Vendor/App/ClassName.php");
    }

    public void testNamespaceDifferentCase() throws Exception {
        checkHints(new PSR4Hint.NamespaceDeclarationHint(), "Vendor/App/ClassName.php");
    }

    public void testSubNamespaceOk() throws Exception {
        checkHints(new PSR4Hint.NamespaceDeclarationHint(), "App/ClassName.php");
    }

    public void testClassNameOk() throws Exception {
        checkHints(new PSR4Hint.TypeDeclarationHint(), "Vendor/App/ClassName.php");
    }

    public void testClassNameDifferent() throws Exception {
        checkHints(new PSR4Hint.TypeDeclarationHint(), "Vendor/App/ClassName.php");
    }

    public void testClassNameDifferentCase() throws Exception {
        checkHints(new PSR4Hint.TypeDeclarationHint(), "Vendor/App/ClassName.php");
    }

    public void testInterfaceNameOk() throws Exception {
        checkHints(new PSR4Hint.TypeDeclarationHint(), "Vendor/App/InterfaceName.php");
    }

    public void testInterfaceNameDifferent() throws Exception {
        checkHints(new PSR4Hint.TypeDeclarationHint(), "Vendor/App/InterfaceName.php");
    }

    public void testInterfaceNameDifferentCase() throws Exception {
        checkHints(new PSR4Hint.TypeDeclarationHint(), "Vendor/App/InterfaceName.php");
    }

    public void testTraitNameOk() throws Exception {
        checkHints(new PSR4Hint.TypeDeclarationHint(), "Vendor/App/TraitName.php");
    }

    public void testTraitNameDifferent() throws Exception {
        checkHints(new PSR4Hint.TypeDeclarationHint(), "Vendor/App/TraitName.php");
    }

    public void testTraitNameDifferentCase() throws Exception {
        checkHints(new PSR4Hint.TypeDeclarationHint(), "Vendor/App/TraitName.php");
    }

    public void testEnumNameOk() throws Exception {
        checkHints(new PSR4Hint.TypeDeclarationHint(), "Vendor/App/EnumName.php");
    }

    public void testEnumNameDifferent() throws Exception {
        checkHints(new PSR4Hint.TypeDeclarationHint(), "Vendor/App/EnumName.php");
    }

    public void testEnumNameDifferentCase() throws Exception {
        checkHints(new PSR4Hint.TypeDeclarationHint(), "Vendor/App/EnumName.php");
    }

    @Override
    protected void checkHints(Rule hint, String fileName) throws Exception {
        super.checkHints(hint, getTestDir() + "/" + fileName);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
                PhpSourcePath.SOURCE_CP,
                ClassPathSupport.createClassPath(new FileObject[]{
            FileUtil.toFileObject(new File(getDataDir(), "/" + TEST_DIRECTORY + getTestDir()))
        })
        );
    }

    private String getTestDir() {
        return "PSR4/" + getName();
    }

}
