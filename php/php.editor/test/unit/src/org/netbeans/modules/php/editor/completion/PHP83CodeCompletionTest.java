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

public class PHP83CodeCompletionTest extends PHPCodeCompletionTestBase {

    public PHP83CodeCompletionTest(String testName) {
        super(testName);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[]{
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/php83/" + getTestDirName()))
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
        return String.format("testfiles/completion/lib/php83/%s/%s.php", getTestDirName(), fileName);
    }

    private void checkCompletion(String fileName, String caretPosition) throws Exception {
        checkCompletion(getTestPath(fileName), caretPosition, false);
    }

    public void testDynamicClassConstantFetchInConstDeclTyping_01() throws Exception {
        checkCompletion("inConstantDeclarationTyping", "    public const TEST1 = self::{^};");
    }

    public void testDynamicClassConstantFetchInEnumCaseTyping_01() throws Exception {
        checkCompletion("inEnumCaseTyping", "    case TEST1 = self::{^};");
    }

    public void testDynamicClassConstantFetchTyping_01() throws Exception {
        checkCompletion("dynamicClassConstantFetchTyping", "Test::{^};");
    }

    public void testDynamicClassConstantFetch_01() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "    public const TEST3 = self::{self::T^ES . self::T};");
    }

    public void testDynamicClassConstantFetch_02() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "Test::{$^variable};");
    }

    public void testDynamicClassConstantFetch_03() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "Test::{$var^iable};");
    }

    public void testDynamicClassConstantFetch_04() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "$test::{$variable . $^e};");
    }

    public void testDynamicClassConstantFetch_05() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "$test::{$variable . $e^};");
    }

    public void testDynamicClassConstantFetch_06a() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "Test::{Tes^t::method()}::{test($variable)};");
    }

    public void testDynamicClassConstantFetch_06b() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "Test::{Test::met^hod()}::{test($variable)};");
    }

    public void testDynamicClassConstantFetch_06c() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "Test::{Test::method()}::{te^st($variable)};");
    }

    public void testDynamicClassConstantFetch_06d() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "Test::{Test::method()}::{test($var^iable)};");
    }

    public void testDynamicClassConstantFetch_07a() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "    case TEST3 = self::^{self::TES . self::T};");
    }

    public void testDynamicClassConstantFetch_07b() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "    case TEST3 = self::{sel^f::TES . self::T};");
    }

    public void testDynamicClassConstantFetch_07c() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "    case TEST3 = self::{self::TE^S . self::T};");
    }

    public void testDynamicClassConstantFetch_08a() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "    case TEST4 = EnumTe^st::{self::{self::TES} . self::T};");
    }

    public void testDynamicClassConstantFetch_08b() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "    case TEST4 = EnumTest::{se^lf::{self::TES} . self::T};");
    }

    public void testDynamicClassConstantFetch_08c() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "    case TEST4 = EnumTest::{self::{sel^f::TES} . self::T};");
    }

    public void testDynamicClassConstantFetch_08d() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "    case TEST4 = EnumTest::{self::{self::TE^S} . self::T};");
    }

    public void testDynamicClassConstantFetch_08e() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "    case TEST4 = EnumTest::{self::{self::TES} . self::T^};");
    }
}
