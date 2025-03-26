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

    public void testTraitsAliasedName01() throws Exception {
        checkCompletion("testfiles/completion/lib/php54/traitsAliasedName01.php", "    use Alias^", false);
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
