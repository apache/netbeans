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
import org.netbeans.modules.php.editor.options.CodeCompletionPanel.CodeCompletionType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class PHPCodeCompletionAutoImportTest extends PHPCodeCompletionTestBase {

    public PHPCodeCompletionAutoImportTest(String testName) {
        super(testName);
    }

    @Override
    protected FileObject[] createSourceClassPathsForTest() {
        return new FileObject[]{FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/autoImport/" + getTestDirName()))};
    }

    private String getTestPath() {
        return String.format("testfiles/completion/lib/autoImport/%s/%s.php", getTestDirName(), getTestDirName());
    }

    private void checkAutoImportCustomTemplateResult(String caretLine, AutoImportOptions autoImportOptions) throws Exception {
        checkCompletionCustomTemplateResult(getTestPath(), caretLine, null, true, autoImportOptions);
    }

    public void testType01_Smart01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("Example^ // test", options);
    }

    public void testType01_Smart02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("Example^ // test", options);
    }

    public void testType01_Unqualified01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("Example^ // test", options);
    }

    public void testType01_Unqualified02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("Example^ // test", options);
    }

    public void testType01_FQN01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("Example^ // test", options);
    }

    public void testType01_FQN02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("Example^ // test", options);
    }

    public void testType02_Smart01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("new Example^ // test", options);
    }

    public void testType02_Smart02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("new Example^ // test", options);
    }

    public void testType02_Unqualified01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("new Example^ // test", options);
    }

    public void testType02_Unqualified02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("new Example^ // test", options);
    }

    public void testType02_FQN01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("new Example^ // test", options);
    }

    public void testType02_FQN02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("new Example^ // test", options);
    }

    public void testType03_Smart01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("    use ExampleTrait^;", options);
    }

    public void testType03_Smart02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("    use ExampleTrait^;", options);
    }

    public void testType03_Unqualified01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("    use ExampleTrait^;", options);
    }

    public void testType03_Unqualified02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("    use ExampleTrait^;", options);
    }

    public void testType03_FQN01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("    use ExampleTrait^;", options);
    }

    public void testType03_FQN02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("    use ExampleTrait^;", options);
    }

    public void testType04_Smart01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("    private Example^ // test", options);
    }

    public void testType04_Smart02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("    private Example^ // test", options);
    }

    public void testType04_Unqualified01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("    private Example^ // test", options);
    }

    public void testType04_Unqualified02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("    private Example^ // test", options);
    }

    public void testType04_FQN01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("    private Example^ // test", options);
    }

    public void testType04_FQN02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("    private Example^ // test", options);
    }

    public void testType05_Smart01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("    public function paramTest(Example^): void {", options);
    }

    public void testType05_Smart02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("    public function paramTest(Example^): void {", options);
    }

    public void testType05_Unqualified01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("    public function paramTest(Example^): void {", options);
    }

    public void testType05_Unqualified02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("    public function paramTest(Example^): void {", options);
    }

    public void testType05_FQN01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("    public function paramTest(Example^): void {", options);
    }

    public void testType05_FQN02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("    public function paramTest(Example^): void {", options);
    }

    public void testType06_Smart01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("    public function returnTypeTest(): Example^ {", options);
    }

    public void testType06_Smart02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("    public function returnTypeTest(): Example^ {", options);
    }

    public void testType06_Unqualified01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("    public function returnTypeTest(): Example^ {", options);
    }

    public void testType06_Unqualified02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("    public function returnTypeTest(): Example^ {", options);
    }

    public void testType06_FQN01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("    public function returnTypeTest(): Example^ {", options);
    }

    public void testType06_FQN02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("    public function returnTypeTest(): Example^ {", options);
    }

    public void testTypeAlias01_Smart01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("Alias^ // test", options);
    }

    public void testTypeAlias01_Smart02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("Alias^ // test", options);
    }

    public void testTypeAlias01_Unqualified01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("Alias^ // test", options);
    }

    public void testTypeAlias01_Unqualified02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("Alias^ // test", options);
    }

    public void testTypeAlias01_FQN01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("Alias^ // test", options);
    }

    public void testTypeAlias01_FQN02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("Alias^ // test", options);
    }

    public void testTypeAlias02_Smart01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("    use AliasTrait^; // test", options);
    }

    public void testTypeAlias02_Smart02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("    use AliasTrait^; // test", options);
    }

    public void testTypeAlias02_Unqualified01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("    use AliasTrait^; // test", options);
    }

    public void testTypeAlias02_Unqualified02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("    use AliasTrait^; // test", options);
    }

    public void testTypeAlias02_FQN01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("    use AliasTrait^; // test", options);
    }

    public void testTypeAlias02_FQN02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("    use AliasTrait^; // test", options);
    }

    public void testFunction01_Smart01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("example^ // test", options);
    }

    public void testFunction01_Smart02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("example^ // test", options);
    }

    public void testFunction01_Unqualified01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("example^ // test", options);
    }

    public void testFunction01_Unqualified02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("example^ // test", options);
    }

    public void testFunction01_FQN01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("example^ // test", options);
    }

    public void testFunction01_FQN02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("example^ // test", options);
    }

    public void testConst01_Smart01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("example^ // test", options);
    }

    public void testConst01_Smart02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("example^ // test", options);
    }

    public void testConst01_Unqualified01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("example^ // test", options);
    }

    public void testConst01_Unqualified02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("example^ // test", options);
    }

    public void testConst01_FQN01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("example^ // test", options);
    }

    public void testConst01_FQN02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("example^ // test", options);
    }

    public void testTypeWithSameName01_Smart01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("Example^ // test", options);
    }

    public void testTypeWithSameName01_Smart02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("Example^ // test", options);
    }

    public void testTypeWithSameName01_Unqualified01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("Example^ // test", options);
    }

    public void testTypeWithSameName01_Unqualified02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("Example^ // test", options);
    }

    public void testTypeWithSameName01_FQN01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("Example^ // test", options);
    }

    public void testTypeWithSameName01_FQN02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("Example^ // test", options);
    }

    public void testTypeWithSameName02_Smart01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("    use ExampleTrait^;", options);
    }

    public void testTypeWithSameName02_Smart02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("    use ExampleTrait^;", options);
    }

    public void testTypeWithSameName02_Unqualified01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("    use ExampleTrait^;", options);
    }

    public void testTypeWithSameName02_Unqualified02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("    use ExampleTrait^;", options);
    }

    public void testTypeWithSameName02_FQN01() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("    use ExampleTrait^;", options);
    }

    public void testTypeWithSameName02_FQN02() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(false)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("    use ExampleTrait^;", options);
    }

    public void testGlobalNamespaceItem01_Smart01a() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("    Globa^l // test", options);
    }

    public void testGlobalNamespaceItem01_Unqualified01a() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("    Globa^l // test", options);
    }

    public void testGlobalNamespaceItem01_FQN01a() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("    Globa^l // test", options);
    }

    public void testGlobalNamespaceItem01_Smart01b() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.SMART)
                .globalItemImportable(true);
        checkAutoImportCustomTemplateResult("    Globa^l // test", options);
    }

    public void testGlobalNamespaceItem01_Unqualified01b() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED)
                .globalItemImportable(true);
        checkAutoImportCustomTemplateResult("    Globa^l // test", options);
    }

    public void testGlobalNamespaceItem01_FQN01b() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED)
                .globalItemImportable(true);
        checkAutoImportCustomTemplateResult("    Globa^l // test", options);
    }

    public void testGlobalNamespaceItem02_Smart01a() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.SMART);
        checkAutoImportCustomTemplateResult("        use GlobalNSTrai^t;", options);
    }

    public void testGlobalNamespaceItem02_Unqualified01a() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED);
        checkAutoImportCustomTemplateResult("        use GlobalNSTrai^t;", options);
    }

    public void testGlobalNamespaceItem02_FQN01a() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED);
        checkAutoImportCustomTemplateResult("        use GlobalNSTrai^t;", options);
    }

    public void testGlobalNamespaceItem02_Smart01b() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.SMART)
                .globalItemImportable(true);
        checkAutoImportCustomTemplateResult("        use GlobalNSTrai^t;", options);
    }

    public void testGlobalNamespaceItem02_Unqualified01b() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.UNQUALIFIED)
                .globalItemImportable(true);
        checkAutoImportCustomTemplateResult("        use GlobalNSTrai^t;", options);
    }

    public void testGlobalNamespaceItem02_FQN01b() throws Exception {
        AutoImportOptions options = new AutoImportOptions()
                .autoImport(true)
                .codeCompletionType(CodeCompletionType.FULLY_QUALIFIED)
                .globalItemImportable(true);
        checkAutoImportCustomTemplateResult("        use GlobalNSTrai^t;", options);
    }

}
