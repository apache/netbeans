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
import org.netbeans.modules.csl.api.CodeCompletionHandler.QueryType;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

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

    public void testArrayReturnTypeWithTab_01() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/arrayReturnType.php", "withTabSpace01^();", false, "");
    }

    public void testArrayReturnTypeWithTab_02() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/arrayReturnType.php", "withTabSpace02^();", false, "");
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

    public void testIssueGH4494_01() throws Exception {
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH4494.php", "gh44^94_aa_bbb();");
    }

    public void testIssueGH4494_02() throws Exception {
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH4494.php", "gh4494_aa^_bbb();");
    }

    public void testIssueGH4494_03() throws Exception {
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH4494.php", "gh4494_aa_b^bb();");
    }

    public void testIssueGH4494_04() throws Exception {
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH4494.php", "gh4494^_aa_bb_cc();");
    }

    public void testIssueGH4494_05() throws Exception {
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH4494.php", "gh4494_aa^_bb_cc();");
    }

    public void testIssueGH4494_06() throws Exception {
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH4494.php", "gh4494_aa_bb^_cc();");
    }

    public void testIssueGH4494_07() throws Exception {
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH4494.php", "gh4494_aa_bb_cc^();");
    }

    public void testIssueGH5347_01() throws Exception {
        // no golden file
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH5347.php", "^// test", true);
    }

    public void testIssueGH5347_02() throws Exception {
        // no golden file
        // XXX
        // sometimes occur with CI in Windows
        // Working directory: D:\a\netbeans\netbeans\php\php.editor\build\test\\unit\work\o.n.m.p.e.c.P\testIssueGH5347_02
        if (!Utilities.isWindows()) {
            checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH5347.php", "un^defined();", true);
        }
    }

    public void testIssueGH5355_01() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issueGH5355.php", "$this->testMetho^d(null);", false, "");
    }

    public void testIssueGH5355_02() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issueGH5355.php", "testFunctio^n(null);", false, "");
    }

    public void testIssueGH5881_01a() throws Exception {
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH5881.php", "$prop1 = $test->pr^op_aa_bbb;");
    }

    public void testIssueGH5881_01b() throws Exception {
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH5881.php", "$prop1 = $test->prop_aa^_bbb;");
    }

    public void testIssueGH5881_01c() throws Exception {
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH5881.php", "$prop1 = $test->prop_aa_bb^b;");
    }

    public void testIssueGH5881_01d() throws Exception {
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH5881.php", "$prop1 = $test->^prop_aa_bbb;");
    }

    public void testIssueGH5881_02a() throws Exception {
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH5881.php", "$prop2 = $test->pr^op_aa_bb_cc;");
    }

    public void testIssueGH5881_02b() throws Exception {
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH5881.php", "$prop2 = $test->prop_aa^_bb_cc;");
    }

    public void testIssueGH5881_02c() throws Exception {
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH5881.php", "$prop2 = $test->prop_aa_bb^_cc;");
    }

    public void testIssueGH5881_02d() throws Exception {
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH5881.php", "$prop2 = $test->prop_aa_bb_cc^;");
    }

    public void testIssueGH5881_02e() throws Exception {
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH5881.php", "$prop2 = $test->^prop_aa_bb_cc;");
    }

    public void testIssueGH5881_03a() throws Exception {
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH5881.php", "$test->meth^od_aa_bbb();");
    }

    public void testIssueGH5881_03b() throws Exception {
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH5881.php", "$test->method_aa^_bbb();");
    }

    public void testIssueGH5881_03c() throws Exception {
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH5881.php", "$test->method_aa_bb^b();");
    }

    public void testIssueGH5881_03d() throws Exception {
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH5881.php", "$test->^method_aa_bbb();");
    }

    public void testIssueGH5881_04a() throws Exception {
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH5881.php", "$test->me^thod_aa_bb_cc();");
    }

    public void testIssueGH5881_04b() throws Exception {
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH5881.php", "$test->method_aa^_bb_cc();");
    }

    public void testIssueGH5881_04c() throws Exception {
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH5881.php", "$test->method_aa_bb^_cc();");
    }

    public void testIssueGH5881_04d() throws Exception {
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH5881.php", "$test->method_aa_bb_cc^();");
    }

    public void testIssueGH5881_04e() throws Exception {
        checkCompletionOnlyDocumentation("testfiles/completion/documentation/issueGH5881.php", "$test->method_aa_bb_cc^();");
    }

    public void testGuessingNullReturnType_01() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/guessingNullReturnType.php", "$this->testGuessingNullMetho^d(null);", false, "");
    }

    public void testGuessingNullReturnType_02() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/guessingNullReturnType.php", "testGuessingNullFunctio^n(null);", false, "");
    }

    public void testNullConstant_01() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/nullConstant.php", "$a = TEST_CON^ST;", false, "");
    }

    public void testNullConstant_02() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/nullConstant.php", "$b = TestConst::TEST_CLASS_CON^ST;", false, "");
    }

    public void testDeprecatedTypedFields_01() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "$this->typedFiel^d;", false, "");
    }

    public void testDeprecatedTypedFields_02() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "$this->multiField1^;", false, "");
    }

    public void testDeprecatedTypedFields_03() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "$this->multiField2^;", false, "");
    }

    public void testDeprecatedTypedFields_04() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "$this->nullableTypeFi^eld;", false, "");
    }

    public void testDeprecatedTypedFields_05() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "$this->unionType^Field;", false, "");
    }

    public void testDeprecatedTypedFields_06() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "$this->intersectionType^Field;", false, "");
    }

    // TODO: dnf types are not fixed yet
//    public void testDeprecatedTypedFields_07() throws Exception {
//        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "$this->dnfTypeFi^eld;", false, "");
//    }

    public void testDeprecatedStaticTypedFields_01() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "self::$typedStaticFiel^d;", false, "");
    }

    public void testDeprecatedStaticTypedFields_02() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "self::$multiStaticField1^;", false, "");
    }

    public void testDeprecatedStaticTypedFields_03() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "self::$multiStaticField2^;", false, "");
    }

    public void testDeprecatedStaticTypedFields_04() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "self::$nullableTypeStatic^Field;", false, "");
    }

    public void testDeprecatedStaticTypedFields_05() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "self::$unionTypeStaticFiel^d;", false, "");
    }

    public void testDeprecatedStaticTypedFields_06() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "self::$intersectionTypeStat^icField;", false, "");
    }

    // TODO: dnf types are not fixed yet
//    public void testDeprecatedStaticTypedFields_07() throws Exception {
//        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "self::$dnfTypeStaticFi^eld;", false, "");
//    }

    public void testDeprecatedTypedTraitFields_01() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "$this->typedFiel^d; // trait", false, "");
    }

    public void testDeprecatedTypedTraitFields_02() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "$this->multiField1^; // trait", false, "");
    }

    public void testDeprecatedTypedTraitFields_03() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "$this->multiField2^; // trait", false, "");
    }

    public void testDeprecatedTypedTraitFields_04() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "$this->nullableTypeFi^eld; // trait", false, "");
    }

    public void testDeprecatedTypedTraitFields_05() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "$this->unionType^Field; // trait", false, "");
    }

    public void testDeprecatedTypedTraitFields_06() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "$this->intersectionType^Field; // trait", false, "");
    }

    public void testDeprecatedStaticTypedTraitFields_01() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "self::$typedStaticFiel^d; // trait", false, "");
    }

    public void testDeprecatedStaticTypedTraitFields_02() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "self::$multiStaticField1^; // trait", false, "");
    }

    public void testDeprecatedStaticTypedTraitFields_03() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "self::$multiStaticField2^; // trait", false, "");
    }

    public void testDeprecatedStaticTypedTraitFields_04() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "self::$nullableTypeStatic^Field; // trait", false, "");
    }

    public void testDeprecatedStaticTypedTraitFields_05() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "self::$unionTypeStaticFiel^d; // trait", false, "");
    }

    public void testDeprecatedStaticTypedTraitFields_06() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/deprecatedTypedFields/deprecatedTypedFields.php", "self::$intersectionTypeStat^icField; // trait", false, "");
    }

    public void testDNFTypesPropertyTag() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/php82/dnfTypes.php", "$this->prop^erty;", false, "");
    }

    public void testDNFTypesMethodTag() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/php82/dnfTypes.php", "$this->methodT^ag($param1, $param2);", false, "");
    }

    public void testDNFTypesStaticMethodTag() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/php82/dnfTypes.php", "self::staticMethodTa^g($param1, $param2);", false, "");
    }

    public void testDNFTypesMethod() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/php82/dnfTypes.php", "$this->methodTe^st(null);", false, "");
    }

    public void testDNFTypesPhpdocMethod() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/php82/dnfTypes.php", "$this->phpdocMethodTe^st(null);", false, "");
    }

    public void testDNFTypesInheritdocMethod() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/php82/dnfTypes.php", "$this->inheritdocMetho^d(null);", false, "");
    }

    public void testDNFTypesStaticMethod() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/php82/dnfTypes.php", "self::staticMethodTes^t(null);", false, "");
    }

    public void testDNFTypesPhpdocStaticMethod() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/php82/dnfTypes.php", "self::phpdocStaticMethodTe^st(null);", false, "");
    }

    public void testDNFTypesField() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/php82/dnfTypes.php", "$this->privateFiel^d;", false, "");
    }

    public void testDNFTypesPhpdocField() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/php82/dnfTypes.php", "$this->phpdocFie^ld;", false, "");
    }

    public void testDNFTypesStaticField() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/php82/dnfTypes.php", "static::$privateStaticFi^eld;", false, "");
    }

    public void testDNFTypesPhpdocStaticField() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/php82/dnfTypes.php", "static::$phpdocStaticFiel^d;", false, "");
    }

    public void testFunctionGuessingArrayReturnType_01() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/functionGuessingArrayReturnType.php", "testArrayReturn^Type();", false, "");
    }

    public void testFunctionGuessingArrayReturnType_02() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/functionGuessingArrayReturnType.php", "testArrayReturnTypeWithUnion^Type();", false, "");
    }

    public void testEnumCase_01() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/php81/enumCases.php", "EnumCase::CASE_A^;", false, "");
    }

    public void testEnumCase_02() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/php81/enumCases.php", "EnumCase::CASE_B^;", false, "");
    }

    public void testBackedEnumCaseString_01() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/php81/enumCases.php", "BackeEnumCaseString::CASE_A^;", false, "");
    }

    public void testBackedEnumCaseString_02() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/php81/enumCases.php", "BackeEnumCaseString::CASE_B^;", false, "");
    }

    public void testBackedEnumCaseString_03() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/php81/enumCases.php", "BackeEnumCaseString::CASE_C^;", false, "");
    }

    public void testBackedEnumCaseInt_01() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/php81/enumCases.php", "BackeEnumCaseInt::CASE_A^;", false, "");
    }

    public void testBackedEnumCaseInt_02() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/php81/enumCases.php", "BackeEnumCaseInt::CASE_B^;", false, "");
    }

    public void testBackedEnumCaseInt_03() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/php81/enumCases.php", "BackeEnumCaseInt::CASE_C^;", false, "");
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

    private void checkCompletionOnlyDocumentation(String filePath, String caretLine) throws Exception {
        checkCompletionOnlyDocumentation(filePath, caretLine, false);
    }

    private void checkCompletionOnlyDocumentation(String filePath, String caretLine, boolean noDocument) throws Exception {
        if (!noDocument) {
            checkCompletionDocumentation(filePath, caretLine, false, "", QueryType.DOCUMENTATION);
        } else {
            try {
                checkCompletionDocumentation(filePath, caretLine, false, "", QueryType.DOCUMENTATION);
            } catch (AssertionError ex) {
                // there is no completion item
                return;
            }
            fail("Must not have documentation");
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
