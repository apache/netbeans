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
package org.netbeans.modules.php.editor.verification;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.csl.api.Rule;
import static org.netbeans.modules.php.editor.verification.PHPHintsTestBase.TEST_DIRECTORY;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PSR1HintTest extends PHPHintsTestBase {

    public PSR1HintTest(String testName) {
        super(testName);
    }

    public void testTypeNameOk53() throws Exception {
        checkHints(new TypeDeclarationHintStub(PhpVersion.PHP_53), "ClassName.php");
    }

    public void testEnumNameOk_01() throws Exception {
        checkHints(new TypeDeclarationHintStub(PhpVersion.PHP_53), "EnumName.php");
    }

    public void testTypeNameNotOk53() throws Exception {
        checkHints(new TypeDeclarationHintStub(PhpVersion.PHP_53), "ClassName.php");
    }

    public void testTypeNameNotOk53_2() throws Exception {
        checkHints(new TypeDeclarationHintStub(PhpVersion.PHP_53), "ClassName.php");
    }

    public void testEnumNameNotOk_01() throws Exception {
        checkHints(new TypeDeclarationHintStub(PhpVersion.PHP_53), "EnumName.php");
    }

    public void testEnumNameNotOk_02() throws Exception {
        checkHints(new TypeDeclarationHintStub(PhpVersion.PHP_53), "EnumName.php");
    }

    public void testTypeNameOk52() throws Exception {
        checkHints(new TypeDeclarationHintStub(PhpVersion.PHP_52), "ClassName.php");
    }

    public void testTypeNameNotOk52() throws Exception {
        checkHints(new TypeDeclarationHintStub(PhpVersion.PHP_52), "ClassName.php");
    }

    public void testTypeNameNotOkNamespace52() throws Exception {
        checkHints(new TypeDeclarationHintStub(PhpVersion.PHP_52), "ClassName.php");
    }

    public void testTypeNameNotOkNamespace53() throws Exception {
        checkHints(new TypeDeclarationHintStub(PhpVersion.PHP_53), "ClassName.php");
    }

    public void testTypeNameNotOkNamespace53_2() throws Exception {
        checkHints(new TypeDeclarationHintStub(PhpVersion.PHP_53), "ClassName.php");
    }

    public void testEnumNameNotOkNamespace_01() throws Exception {
        checkHints(new TypeDeclarationHintStub(PhpVersion.PHP_53), "EnumName.php");
    }

    public void testEnumNameNotOkNamespace_02() throws Exception {
        checkHints(new TypeDeclarationHintStub(PhpVersion.PHP_53), "EnumName.php");
    }

    public void testTypeNameNotOneDeclaration52() throws Exception {
        checkHints(new TypeDeclarationHintStub(PhpVersion.PHP_52), "ClassName.php");
    }

    public void testTypeNameNotOneDeclaration53() throws Exception {
        checkHints(new TypeDeclarationHintStub(PhpVersion.PHP_53), "ClassName.php");
    }

    public void testEnumNameNotOneDeclaration_01() throws Exception {
        checkHints(new TypeDeclarationHintStub(PhpVersion.PHP_53), "EnumName.php");
    }

    public void testConstantNameOk_1() throws Exception {
        checkHints(new PSR1Hint.ConstantDeclarationHint(), "ClassName.php");
    }

    public void testConstantNameOk_2() throws Exception {
        checkHints(new PSR1Hint.ConstantDeclarationHint(), "ClassName.php");
    }

    public void testConstantNameEnumOk_01() throws Exception {
        checkHints(new PSR1Hint.ConstantDeclarationHint(), "EnumName.php");
    }

    public void testConstantNameEnumOk_02() throws Exception {
        checkHints(new PSR1Hint.ConstantDeclarationHint(), "EnumName.php");
    }

    public void testConstantNameNotOk_1() throws Exception {
        checkHints(new PSR1Hint.ConstantDeclarationHint(), "ClassName.php");
    }

    public void testConstantNameNotOk_2() throws Exception {
        checkHints(new PSR1Hint.ConstantDeclarationHint(), "ClassName.php");
    }

    public void testConstantNameNotOk_3() throws Exception {
        checkHints(new PSR1Hint.ConstantDeclarationHint(), "ClassName.php");
    }

    public void testConstantNameEnumNotOk_01() throws Exception {
        checkHints(new PSR1Hint.ConstantDeclarationHint(), "EnumName.php");
    }

    public void testConstantNameEnumNotOk_02() throws Exception {
        checkHints(new PSR1Hint.ConstantDeclarationHint(), "EnumName.php");
    }

    public void testConstantNameEnumNotOk_03() throws Exception {
        checkHints(new PSR1Hint.ConstantDeclarationHint(), "EnumName.php");
    }

    public void testMethodOk_1() throws Exception {
        checkHints(new PSR1Hint.MethodDeclarationHint(), "ClassName.php");
    }

    public void testMethodOk_2() throws Exception {
        checkHints(new PSR1Hint.MethodDeclarationHint(), "ClassName.php");
    }

    public void testMethodOk_3() throws Exception {
        checkHints(new PSR1Hint.MethodDeclarationHint(), "ClassName.php");
    }

    public void testMethodEnumOk_01() throws Exception {
        checkHints(new PSR1Hint.MethodDeclarationHint(), "EnumName.php");
    }

    public void testMethodEnumOk_02() throws Exception {
        checkHints(new PSR1Hint.MethodDeclarationHint(), "EnumName.php");
    }

    public void testMethodNotOk_1() throws Exception {
        checkHints(new PSR1Hint.MethodDeclarationHint(), "ClassName.php");
    }

    public void testMethodNotOk_2() throws Exception {
        checkHints(new PSR1Hint.MethodDeclarationHint(), "ClassName.php");
    }

    public void testMethodEnumNotOk_01() throws Exception {
        checkHints(new PSR1Hint.MethodDeclarationHint(), "EnumName.php");
    }

    public void testMethodEnumNotOk_02() throws Exception {
        checkHints(new PSR1Hint.MethodDeclarationHint(), "EnumName.php");
    }

    public void testMethodMagicOk() throws Exception {
        checkHints(new PSR1Hint.MethodDeclarationHint(), "ClassName.php");
    }

    public void testPropertyName_01() throws Exception {
        checkHints(new PSR1Hint.PropertyNameHint(), "ClassName.php");
    }

    public void testPropertyName_02() throws Exception {
        checkHints(new PSR1Hint.PropertyNameHint(), "ClassName.php");
    }

    public void testPropertyName_03() throws Exception {
        checkHints(new PSR1Hint.PropertyNameHint(), "ClassName.php");
    }

    public void testPropertyName_04() throws Exception {
        checkHints(new PSR1Hint.PropertyNameHint(), "ClassName.php");
    }

    public void testPropertyName_05() throws Exception {
        checkHints(new PSR1Hint.PropertyNameHint(), "ClassName.php");
    }

    public void testPropertyName_06() throws Exception {
        checkHints(new PSR1Hint.PropertyNameHint(), "ClassName.php");
    }

    public void testSideEffect_01() throws Exception {
        checkHints(new PSR1Hint.SideEffectHint(), "ClassName.php");
    }

    public void testSideEffect_02() throws Exception {
        checkHints(new PSR1Hint.SideEffectHint(), "ClassName.php");
    }

    public void testSideEffect_03() throws Exception {
        checkHints(new PSR1Hint.SideEffectHint(), "ClassName.php");
    }

    public void testSideEffect_04() throws Exception {
        checkHints(new PSR1Hint.SideEffectHint(), "ClassName.php");
    }

    public void testSideEffect_05() throws Exception {
        checkHints(new PSR1Hint.SideEffectHint(), "ClassName.php");
    }

    public void testSideEffect_06() throws Exception {
        checkHints(new PSR1Hint.SideEffectHint(), "ClassName.php");
    }

    public void testSideEffect_07() throws Exception {
        checkHints(new PSR1Hint.SideEffectHint(), "ClassName.php");
    }

    public void testSideEffect_08() throws Exception {
        checkHints(new PSR1Hint.SideEffectHint(), "ClassName.php");
    }

    public void testSideEffect_09() throws Exception {
        checkHints(new PSR1Hint.SideEffectHint(), "ClassName.php");
    }

    public void testSideEffect_10() throws Exception {
        checkHints(new PSR1Hint.SideEffectHint(), "ClassName.php");
    }

    public void testSideEffect_11() throws Exception {
        checkHints(new PSR1Hint.SideEffectHint(), "EnumName.php");
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
        return "PSR1/" + getName();
    }

    private static final class TypeDeclarationHintStub extends PSR1Hint.TypeDeclarationHint {
        private final PhpVersion phpVersion;

        public TypeDeclarationHintStub(PhpVersion phpVersion) {
            this.phpVersion = phpVersion;
        }

        @Override
        protected boolean isPhp52() {
            return phpVersion.isPhp52();
        }

    }

    private enum PhpVersion {
        PHP_53 {
            @Override
            boolean isPhp52() {
                return false;
            }
        },
        PHP_52 {
            @Override
            boolean isPhp52() {
                return true;
            }
        };

        abstract boolean isPhp52();
    }

}
