/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PHP54CodeCompletionTest extends PHPCodeCompletionTestBase {

    public PHP54CodeCompletionTest(String testName) {
        super(testName);
    }

    public void testTraits1() throws Exception {
        checkCompletion("testfiles/completion/lib/php54/traits.php", "$bc->^baseTraitField;", false);
    }

    public void testTraits2() throws Exception {
        checkCompletion("testfiles/completion/lib/php54/traits.php", "$tc->^traitedTraitField;", false);
    }

    public void testTraits3() throws Exception {
        checkCompletion("testfiles/completion/lib/php54/traits.php", "$this->^baseTraitField;", false);
    }

    public void testTraits4() throws Exception {
        checkCompletion("testfiles/completion/lib/php54/traits.php", "$this->^traitedTraitField;", false);
    }

    public void testTraitsMultiUse1() throws Exception {
        checkCompletion("testfiles/completion/lib/php54/traitsMultiUse.php", "$this->^publicFirstField;", false);
    }

    public void testTraitsMultiUse2() throws Exception {
        checkCompletion("testfiles/completion/lib/php54/traitsMultiUse.php", "$wmu->^publicFirstField;", false);
    }

    public void testShortArrays() throws Exception {
        checkCompletion("testfiles/completion/lib/php54/shortArrays.php", "$xxxAr^r;", false);
    }

    public void testAnonymousObjectVariables_01() throws Exception {
        checkCompletion("testfiles/completion/lib/php54/anonymousObjectVariables.php", "(new AnonymousObject)->^bar;", false);
    }

    public void testAnonymousObjectVariables_02() throws Exception {
        checkCompletion("testfiles/completion/lib/php54/anonymousObjectVariables.php", "(new AnonymousObject(new Bar($baz, $bat)))->^baz();", false);
    }

    public void testCallableTypeHint_01() throws Exception {
        checkCompletion("testfiles/completion/lib/php54/callableTypeHint.php", "function callableTypeHint(^callable $arg) {", false);
    }

    public void testCallableTypeHint_02() throws Exception {
        checkCompletion("testfiles/completion/lib/php54/callableTypeHint.php", "function callableTypeHint(call^able $arg) {", false);
    }

    public void testCallableTypeHint_03() throws Exception {
        checkCompletion("testfiles/completion/lib/php54/callableTypeHint.php", "function __construct(^callable $arg) {", false);
    }

    public void testCallableTypeHint_04() throws Exception {
        checkCompletion("testfiles/completion/lib/php54/callableTypeHint.php", "function __construct(call^able $arg) {", false);
    }

    public void testAnonymousObjectVariablesNs_01() throws Exception {
        checkCompletion("testfiles/completion/lib/php54/anonymousObjectVariablesNs.php", "(new Omg\\AliasedClassName())->^objFoo();", false);
    }

    public void testAnonymousObjectVariablesNs_02() throws Exception {
        checkCompletion("testfiles/completion/lib/php54/anonymousObjectVariablesNs.php", "(new \\Foo\\Bar\\AliasedClassName())->^objFoo();", false);
    }

    public void testAnonymousObjectVariablesNs_03() throws Exception {
        checkCompletion("testfiles/completion/lib/php54/anonymousObjectVariablesNs.php", "(new Cls())->^objFoo();", false);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/php54/"))
            })
        );
    }

}
