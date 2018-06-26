/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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

    public void testTypeNameNotOk53() throws Exception {
        checkHints(new TypeDeclarationHintStub(PhpVersion.PHP_53), "ClassName.php");
    }

    public void testTypeNameNotOk53_2() throws Exception {
        checkHints(new TypeDeclarationHintStub(PhpVersion.PHP_53), "ClassName.php");
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

    public void testTypeNameNotOneDeclaration52() throws Exception {
        checkHints(new TypeDeclarationHintStub(PhpVersion.PHP_52), "ClassName.php");
    }

    public void testTypeNameNotOneDeclaration53() throws Exception {
        checkHints(new TypeDeclarationHintStub(PhpVersion.PHP_53), "ClassName.php");
    }

    public void testConstantNameOk_1() throws Exception {
        checkHints(new PSR1Hint.ConstantDeclarationHint(), "ClassName.php");
    }

    public void testConstantNameOk_2() throws Exception {
        checkHints(new PSR1Hint.ConstantDeclarationHint(), "ClassName.php");
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

    public void testMethodOk_1() throws Exception {
        checkHints(new PSR1Hint.MethodDeclarationHint(), "ClassName.php");
    }

    public void testMethodOk_2() throws Exception {
        checkHints(new PSR1Hint.MethodDeclarationHint(), "ClassName.php");
    }

    public void testMethodOk_3() throws Exception {
        checkHints(new PSR1Hint.MethodDeclarationHint(), "ClassName.php");
    }

    public void testMethodNotOk_1() throws Exception {
        checkHints(new PSR1Hint.MethodDeclarationHint(), "ClassName.php");
    }

    public void testMethodNotOk_2() throws Exception {
        checkHints(new PSR1Hint.MethodDeclarationHint(), "ClassName.php");
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
