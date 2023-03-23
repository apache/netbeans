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
        // phpdoc missing the parameter name, so just use actual parameter name
        // ignore phpdoc types
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

    public void testInheritdocClassWithInlineTagForPhpDocumentor() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "class GrandchildInlineTagClass extends ChildInlineTagC^lass {", false, "", true);
    }

    public void testInheritdocInterfaceOnlyTag() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "class GrandchildClass extends ChildClass implements ChildInterf^ace {", false, "");
    }

    public void testInheritdocInterfaceWithInlineTag() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "interface GrandchildInlineTagInterface extends ChildInlineTagI^nterface {", false, "");
    }

    public void testInheritdocInterfaceWithInlineTagForPhpDocumentor() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "interface GrandchildInlineTagInterface extends ChildInlineTagI^nterface {", false, "", true);
    }

    public void testInheritdocChildMethodSingleSentence() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "$childClass->testSingle^Sentence();", false, "");
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

    public void testInheritdocMethodWithInlineTagForPhpDocumentor() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "$grandchildClass->testInlin^e($param1, $param2);", false, "", true);
    }

    public void testInheritdocMethodWithMissingParam() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "$childClass->testMissing^Param($param1);", false, "");
    }

    public void testInheritdocMethodWithInvalidTag() throws Exception {
        // not {@inheritdoc} but @inheritdoc
        // the same result as the normal tag
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "$childClass->testInvalidT^ag();", false, "");
    }

    public void testInheritdocConstWithSingleSentence() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "ChildClass::CONSTANT_SINGLE_^SENTENCE;", false, "");
    }

    public void testInheritdocConstOnlyTag() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "ChildClass::CONSTANT_ONL^Y_TAG;", false, "");
    }

    public void testInheritdocConstInlineTag() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "ChildClass::CONSTANT_INLIN^E_TAG;", false, "");
    }

    public void testInheritdocConstInlineTagForPhpDocumentor() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "ChildClass::CONSTANT_INLIN^E_TAG;", false, "", true);
    }

    public void testInheritdocConstWithoutPhpDoc() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "ChildClass::CONSTANT_WITHOUT_PH^PDOC;", false, "");
    }

    public void testInheritdocFieldWithSingleSentence() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "$childClass->fieldSingle^Sentence;", false, "");
    }

    public void testInheritdocFieldOnlyTag() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "$childClass->fieldOnl^ytag;", false, "");
    }

    public void testInheritdocFieldInlineTag() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "$childClass->fieldInli^neTag;", false, "");
    }

    public void testInheritdocFieldInlineTagForPhpDocumentor() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "$childClass->fieldInlineT^ag;", false, "", true);
    }

    public void testInheritdocFieldWithoutPhpDoc() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/inheritdoc.php", "$childClass->fieldWithoutP^hpDoc;", false, "");
    }

    public void testFieldTypedWithoutPhpDoc() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/fieldWithoutPhpDoc.php", "$this->typ^ed;", false, "");
    }

    public void testFieldNullableTypeWithoutPhpDoc() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/fieldWithoutPhpDoc.php", "$this->nullabl^e;", false, "");
    }

    public void testFieldUnionTypeWithoutPhpDoc() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/fieldWithoutPhpDoc.php", "self::$union^Type", false, "");
    }

    public void testFieldIntersectionTypeWithoutPhpDoc() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/fieldWithoutPhpDoc.php", "$this->interse^ctionType;", false, "");
    }

    public void testFieldLongNameTypeWithoutPhpDoc() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/fieldWithoutPhpDoc.php", "$this->longNameT^ype;", false, "");
    }

    public void testMethodTypedWithoutPhpDoc() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/functionWithoutPhpDoc.php", "$instance->testTy^ped(1, null, null);", false, "");
    }

    public void testMethodNullableTypeWithoutPhpDoc() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/functionWithoutPhpDoc.php", "TestClass::testNullableTy^pe(null, null, null);", false, "");
    }

    public void testMethodUnionTypeWithoutPhpDoc() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/functionWithoutPhpDoc.php", "$instance->testUnionTy^pe(1, null, null);", false, "");
    }

    public void testMethodIntersectionTypeWithoutPhpDoc() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/functionWithoutPhpDoc.php", "$instance->testIntersectio^nType(null, null);", false, "");
    }

    public void testFunctionTypedWithoutPhpDoc() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/functionWithoutPhpDoc.php", "testTyp^ed(2, null, null);", false, "");
    }

    public void testFunctionNullableTypeWithoutPhpDoc() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/functionWithoutPhpDoc.php", "testNullableTy^pe(2, null, null);", false, "");
    }

    public void testFunctionUnionTypeWithoutPhpDoc() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/functionWithoutPhpDoc.php", "testUnionTy^pe(2, null, null);", false, "");
    }

    public void testFunctionIntersectionTypeWithoutPhpDoc() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/functionWithoutPhpDoc.php", "testIntersectionTy^pe(null, null); // function", false, "");
    }

    public void testIssueGH5427_01() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issueGH5427.php", "$this->test_without_d^oc", false, "");
    }

    public void testIssueGH5427_02() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issueGH5427.php", "$this->test_without_v^ar_tag", false, "");
    }

    public void testIssueGH5427_03() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issueGH5427.php", "$this->test_with_v^ar_tag", false, "");
    }

    public void testIssueGH5375_01() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issueGH5375.php", "$this->test_without_v^ar_tag", false, "");
    }

    public void testIssueGH5375_02() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issueGH5375.php", "$this->test_with_v^ar_tag", false, "");
    }

    public void testIssueGH5375_03() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issueGH5375Intersection.php", "$this->test_without_v^ar_tag", false, "");
    }

    public void testIssueGH5375_04() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issueGH5375Intersection.php", "$this->test_with_v^ar_tag", false, "");
    }

    public void testIssueGH5426_01() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issueGH5426.php", "        $this->testFi^eld;", false, "");
    }

    public void testIssueGH5426_02() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issueGH5426.php", "        $this->testMetho^d(null);", false, "");
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

    public void checkCompletionDocumentation(final String file, final String caretLine, final boolean includeModifiers, final String itemPrefix, boolean followPhpdocumentor) throws Exception {
        if (followPhpdocumentor) {
            DocRenderer.PHPDocExtractor.UNIT_TEST_INHERITDOC_FOR_PHPDOCUMENTER = true;
        }
        checkCompletionDocumentation(file, caretLine, includeModifiers, itemPrefix);
        if (followPhpdocumentor) {
            DocRenderer.PHPDocExtractor.UNIT_TEST_INHERITDOC_FOR_PHPDOCUMENTER = false;
        }
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
