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
 * @author Petr Pisl
 */
public class PHPCCDocumentationTest extends PHPCodeCompletionTestBase {

    public PHPCCDocumentationTest(String testName) {
        super(testName);
    }

    public void test197696() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue197696.php", "$this->te^", false, "");
    }

    public void testArrayReturnType() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/arrayReturnType.php", "functionNam^e();", false, "");
    }

    public void testFieldWithDesc() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/fieldVar.php", "$c->fieldWithDes^c;", false, "");
    }

    public void testFieldWithoutDesc() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/fieldVar.php", "$c1->fieldWithoutDes^c;", false, "");
    }

    public void testFieldWithDescAndArray() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/fieldVar.php", "$c->arrayFieldWithDes^c;", false, "");
    }

    public void testFieldWithoutDescAndArray() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/fieldVar.php", "$c1->arrayFieldWithoutDes^c;", false, "");
    }

    public void testPropertyWithArray() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/propertyWithArray.php", "$this->te^", false, "");
    }

    public void testFunctionWithArrayParamWithoutDesc() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/functionWithArrayParamWithoutDesc.php", "aFunctionNam^e(null);", false, "");
    }

    public void testFunctionWithArrayReturnWithoutDesc() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/functionWithArrayReturnWithoutDesc.php", "bFunctionNam^e(null);", false, "");
    }

    public void testIssue207952_01() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue207952.php", "$my->aMagic^Method($paramName);", false, "");
    }

    public void testIssue207952_02() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue207952.php", "$my->nonMagic^Method($paramName);", false, "");
    }

    public void testIssue207952_03() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue207952_nonNs.php", "$my->aMagic^Method($paramName);", false, "");
    }

    public void testIssue207952_04() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue207952_nonNs.php", "$my->nonMagic^Method($paramName);", false, "");
    }

    public void testIssue215408() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue215408.php", "ClassName::F^OO;", false, "");
    }

    public void testIssue245158_01() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue245158.php", "$this->a_wi^th;", false, "");
    }

    public void testIssue245158_02() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue245158.php", "$this->b_with^out;", false, "");
    }

    // {@inheritdoc} tag
    public void testInheritdocClassOnlyTag() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "class GrandchildClass extends ChildC^lass implements ChildInterface {", false, "");
    }

    public void testInheritdocClassWithInlineTag() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "class GrandchildInlineTagClass extends ChildInlineTagC^lass {", false, "");
    }

    public void testInheritdocInterfaceOnlyTag() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "class GrandchildClass extends ChildClass implements ChildInterf^ace {", false, "");
    }

    public void testInheritdocInterfaceWithInlineTag() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "interface GrandchildInlineTagInterface extends ChildInlineTagI^nterface {", false, "");
    }

    public void testInheritdocChildMethodOnlyTag() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "$childClass->testOnlyT^ag($param1, $param2);", false, "");
    }

    public void testInheritdocGrandchildMethodOnlyTag_01() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "$grandchildClass->testOnlyT^ag($param1, $param2);", false, "");
    }

    public void testInheritdocGrandchildMethodOnlyTag_02() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "$grandchildClass->childInterfaceM^ethod();", false, "");
    }

    public void testInheritdocNoDocMethod() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "$childClass->testNoD^oc();", false, "");
    }

    public void testInheritdocNoTagMethod() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "$childClass->testNoI^nheritdoc();", false, "");
    }

    public void testInheritdocMethodWithInlineTag() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "$grandchildClass->testInlin^e($param1, $param2);", false, "");
    }

    public void testInheritdocMethodWithMissingParam() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "$childClass->testMissing^Param($param1);", false, "");
    }

    public void testInheritdocMethodWithInvalidTag() throws Exception {
        // not {@inheritdoc} but @inheritdoc
        // the same result as the normal tag
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "$childClass->testInvalidT^ag();", false, "");
    }

    @Override
    protected String alterDocumentationForTest(String documentation) {
        int start = documentation.indexOf("file:");
        if (start > 0) {
            int end = documentation.indexOf(".php", start);
            if (end > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(documentation.substring(0, start));
                sb.append(documentation.substring(end + 4));
                return sb.toString();
            }
        }
        return documentation;
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/documentation"))
            })
        );
    }
}
