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
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class PHP81CodeCompletionTest extends PHPCodeCompletionTestBase {

    public PHP81CodeCompletionTest(String testName) {
        super(testName);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[]{
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/php81/" + getTestDirName()))
            })
        );
    }

    private String getTestPath(String fileName) {
        return String.format("testfiles/completion/lib/php81/%s/%s.php", getTestDirName(), fileName);
    }

    private void checkCompletion(String fileName, String caretPosition) throws Exception {
        checkCompletion(getTestPath(fileName), caretPosition, false);
    }

    private void checkCompletionForFirstClassCallable(String fileName, String caretPosition) throws Exception {
        try {
            PHPCompletionItem.setAddFirstClassCallable(true);
            checkCompletion(getTestPath(fileName), caretPosition, false);
        } finally {
            PHPCompletionItem.setAddFirstClassCallable(null);
        }
    }

    public void testNeverReturnType_Function01() throws Exception {
        checkCompletion("neverReturnType", "function returnType(): ^never { // func");
    }

    public void testNeverReturnType_Function02() throws Exception {
        checkCompletion("neverReturnType", "function returnType(): ne^ver { // func");
    }

    public void testNeverReturnType_Function03() throws Exception {
        checkCompletion("neverReturnType", "function invalidInParameter(ne^ver $never): never { // func");
    }

    public void testNeverReturnType_Class01() throws Exception {
        checkCompletion("neverReturnType", "public function returnType(): ^never { // class");
    }

    public void testNeverReturnType_Class02() throws Exception {
        checkCompletion("neverReturnType", "public function returnType(): neve^r { // class");
    }

    public void testNeverReturnType_Class03() throws Exception {
        checkCompletion("neverReturnType", "public function invalidInParameter(ne^ver $never): never { // class");
    }

    public void testNeverReturnType_Trait01() throws Exception {
        checkCompletion("neverReturnType", "public function returnType(): ^never { // trait");
    }

    public void testNeverReturnType_Trait02() throws Exception {
        checkCompletion("neverReturnType", "public function returnType(): neve^r { // trait");
    }

    public void testNeverReturnType_Trait03() throws Exception {
        checkCompletion("neverReturnType", "public function invalidInParameter(ne^ver $never): never { // trait");
    }

    public void testNeverReturnType_Interface01() throws Exception {
        checkCompletion("neverReturnType", "public function returnType(): ^never; // interface");
    }

    public void testNeverReturnType_Interface02() throws Exception {
        checkCompletion("neverReturnType", "public function returnType(): ne^ver; // interface");
    }

    public void testNeverReturnType_Interface03() throws Exception {
        checkCompletion("neverReturnType", "public function invalidInParameter(ne^ver $never): never; // interface");
    }

    public void testReadonlyPropertiesTyping01() throws Exception {
        checkCompletion("readonlyPropertiesTyping01", "    ^// test");
    }

    public void testReadonlyPropertiesTyping02() throws Exception {
        checkCompletion("readonlyPropertiesTyping02", "    read^");
    }

    public void testReadonlyPropertiesTyping03() throws Exception {
        checkCompletion("readonlyPropertiesTyping03", "readonly ^");
    }

    public void testReadonlyPropertiesTyping04() throws Exception {
        checkCompletion("readonlyPropertiesTyping04", "    readonly p^");
    }

    public void testReadonlyPropertiesTyping05() throws Exception {
        checkCompletion("readonlyPropertiesTyping05", "readonly public ^");
    }

    public void testReadonlyPropertiesTyping06() throws Exception {
        checkCompletion("readonlyPropertiesTyping06", "    p^");
    }

    public void testReadonlyPropertiesTyping07() throws Exception {
        checkCompletion("readonlyPropertiesTyping07", "    public ^");
    }

    public void testReadonlyPropertiesTyping08() throws Exception {
        checkCompletion("readonlyPropertiesTyping08", "    public read^");
    }

    public void testReadonlyPropertiesTyping09() throws Exception {
        checkCompletion("readonlyPropertiesTyping09", "    public readonly ^");
    }

    public void testReadonlyPropertiesTyping10() throws Exception {
        checkCompletion("readonlyPropertiesTyping10", "    public readonly ?^");
    }

    public void testReadonlyPromotedPropertiesTyping01() throws Exception {
        checkCompletion("readonlyPropertiesTyping01", "        ^// test");
    }

    public void testReadonlyPromotedPropertiesTyping02() throws Exception {
        checkCompletion("readonlyPropertiesTyping02", "        read^");
    }

    public void testReadonlyPromotedPropertiesTyping03() throws Exception {
        checkCompletion("readonlyPropertiesTyping03", "            readonly ^");
    }

    public void testReadonlyPromotedPropertiesTyping04() throws Exception {
        checkCompletion("readonlyPropertiesTyping04", "            readonly p^");
    }

    public void testReadonlyPromotedPropertiesTyping05() throws Exception {
        checkCompletion("readonlyPropertiesTyping05", "            readonly public ^");
    }

    public void testReadonlyPromotedPropertiesTyping06() throws Exception {
        checkCompletion("readonlyPropertiesTyping06", "            ^// test");
    }

    public void testReadonlyPromotedPropertiesTyping07() throws Exception {
        checkCompletion("readonlyPropertiesTyping07", "            read^//test");
    }

    public void testReadonlyPromotedPropertiesTyping08() throws Exception {
        checkCompletion("readonlyPropertiesTyping08", "            readonly public int|^//test");
    }

    public void testReadonlyPromotedPropertiesTyping09() throws Exception {
        checkCompletion("readonlyPropertiesTyping09", "            p^");
    }

    public void testReadonlyPromotedPropertiesTyping10() throws Exception {
        checkCompletion("readonlyPropertiesTyping10", "            private ^");
    }

    public void testReadonlyPromotedPropertiesTyping11() throws Exception {
        checkCompletion("readonlyPropertiesTyping11", "            private readon^");
    }

    public void testReadonlyPromotedPropertiesTyping12() throws Exception {
        checkCompletion("readonlyPropertiesTyping12", "            private readonly ^");
    }

    public void testReadonlyPromotedPropertiesTyping13() throws Exception {
        checkCompletion("readonlyPropertiesTyping13", "            private readonly str^");
    }

    public void testReadonlyProperties_01() throws Exception {
        checkCompletion("readonlyProperties", "    publ^ic readonly int $publicReadonly;");
    }

    public void testReadonlyProperties_02() throws Exception {
        checkCompletion("readonlyProperties", "    public reado^nly int $publicReadonly;");
    }

    public void testReadonlyProperties_03() throws Exception {
        checkCompletion("readonlyProperties", "    public readonly in^t $publicReadonly;");
    }

    public void testReadonlyProperties_04() throws Exception {
        checkCompletion("readonlyProperties", "    private readonly ?strin^g $privateReadonly;");
    }

    public void testReadonlyProperties_05() throws Exception {
        checkCompletion("readonlyProperties", "    protected readonly stri^ng|int $protectedReadonly;");
    }

    public void testReadonlyProperties_06() throws Exception {
        checkCompletion("readonlyProperties", "    protected readonly string|in^t $protectedReadonly;");
    }

    public void testReadonlyProperties_07() throws Exception {
        checkCompletion("readonlyProperties", "    readon^ly public string $readonlyPublic;");
    }

    public void testReadonlyProperties_08() throws Exception {
        checkCompletion("readonlyProperties", "    readonly publi^c string $readonlyPublic;");
    }

    public void testReadonlyProperties_09() throws Exception {
        checkCompletion("readonlyProperties", "    readonly public str^ing $readonlyPublic;");
    }

    public void testReadonlyProperties_10() throws Exception {
        checkCompletion("readonlyProperties", "    readonly private ?stri^ng $readonlyPrivate;");
    }

    public void testReadonlyProperties_11() throws Exception {
        checkCompletion("readonlyProperties", "    readonly protected in^t|string $readonlyProtected;");
    }

    public void testReadonlyProperties_12() throws Exception {
        checkCompletion("readonlyProperties", "    readonly protected int|str^ing $readonlyProtected;");
    }

    public void testReadonlyProperties_13() throws Exception {
        checkCompletion("readonlyProperties", "        publ^ic readonly int|string $promotedPublicReadonly = 0,");
    }

    public void testReadonlyProperties_14() throws Exception {
        checkCompletion("readonlyProperties", "        public reado^nly int|string $promotedPublicReadonly = 0,");
    }

    public void testReadonlyProperties_15() throws Exception {
        checkCompletion("readonlyProperties", "        public readonly i^nt|string $promotedPublicReadonly = 0,");
    }

    public void testReadonlyProperties_16() throws Exception {
        checkCompletion("readonlyProperties", "        public readonly int|str^ing $promotedPublicReadonly = 0,");
    }

    public void testReadonlyProperties_17() throws Exception {
        checkCompletion("readonlyProperties", "        private readonly arr^ay $promotedPrivateReadonly = [],");
    }

    public void testReadonlyProperties_18() throws Exception {
        checkCompletion("readonlyProperties", "        protected readonly ?str^ing $promotedProtectedReadonly = \"test\",");
    }

    public void testReadonlyProperties_19() throws Exception {
        checkCompletion("readonlyProperties", "        readonly public int|st^ring $promotedReadonlyPublic = 0,");
    }

    public void testReadonlyProperties_20() throws Exception {
        checkCompletion("readonlyProperties", "        readonly private arra^y $promotedReadonlyPrivate = [],");
    }

    public void testReadonlyProperties_21() throws Exception {
        checkCompletion("readonlyProperties", "        readonly protected ?stri^ng $promotedReadonlyProtected = \"test\",");
    }

    public void testNewInInitializers_01() throws Exception {
        checkCompletion("newInInitializers", "static $staticVariable = ne^w StaticVariable;");
    }

    public void testNewInInitializers_02() throws Exception {
        checkCompletion("newInInitializers", "static $staticVariable = new StaticVa^riable;");
    }

    public void testNewInInitializers_03() throws Exception {
        checkCompletion("newInInitializers", "const CONSTANT = n^ew Constant();");
    }

    public void testNewInInitializers_04() throws Exception {
        checkCompletion("newInInitializers", "const CONSTANT = new Constan^t();");
    }

    public void testNewInInitializers_05() throws Exception {
        checkCompletion("newInInitializers", "function func1($param = ^new Func) {}");
    }

    public void testNewInInitializers_06() throws Exception {
        checkCompletion("newInInitializers", "function func1($param = ne^w Func) {}");
    }

    public void testNewInInitializers_07() throws Exception {
        checkCompletion("newInInitializers", "function func1($param = new Fun^c) {}");
    }

    public void testNewInInitializers_08() throws Exception {
        checkCompletion("newInInitializers", "#[AnAttribute(n^ew Foo(x: 1))]");
    }

    public void testNewInInitializers_09() throws Exception {
        checkCompletion("newInInitializers", "#[AnAttribute(new Fo^o(x: 1))]");
    }

    public void testNewInInitializers_10() throws Exception {
        checkCompletion("newInInitializers", "        public $prop = ^new Foo(\"test\"),");
    }

    public void testNewInInitializers_11() throws Exception {
        checkCompletion("newInInitializers", "        public $prop = ne^w Foo(\"test\"),");
    }

    public void testNewInInitializers_12() throws Exception {
        checkCompletion("newInInitializers", "        public $prop = new Fo^o(\"test\"),");
    }

    public void testNewInInitializers_13() throws Exception {
        checkCompletion("newInInitializers", "        $param = ne^w Foo(test: \"test\"),");
    }

    public void testNewInInitializers_14() throws Exception {
        checkCompletion("newInInitializers", "        $param = new Fo^o(test: \"test\"),");
    }

    public void testNewInInitializersStaticVariableTyping01() throws Exception {
        checkCompletion("newInInitializersStaticVariableTyping01", "static $staticVariable = ^");
    }

    public void testNewInInitializersStaticVariableTyping02() throws Exception {
        checkCompletion("newInInitializersStaticVariableTyping02", "static $staticVariable = ne^");
    }

    public void testNewInInitializersStaticVariableTyping03() throws Exception {
        checkCompletion("newInInitializersStaticVariableTyping03", "static $staticVariable = new Stati^");
    }

    public void testNewInInitializersConstantTyping01() throws Exception {
        checkCompletion("newInInitializersConstantTyping01", "const CONSTANT = ^");
    }

    public void testNewInInitializersConstantTyping02() throws Exception {
        checkCompletion("newInInitializersConstantTyping02", "const CONSTANT = ne^");
    }

    public void testNewInInitializersConstantTyping03() throws Exception {
        checkCompletion("newInInitializersConstantTyping03", "const CONSTANT = new Consta^");
    }

    public void testNewInInitializersFuncTyping01() throws Exception {
        checkCompletion("newInInitializersFuncTyping01", "function func1($param = ^) {}");
    }

    public void testNewInInitializersFuncTyping02() throws Exception {
        checkCompletion("newInInitializersFuncTyping02", "function func1($param = ne^) {}");
    }

    public void testNewInInitializersFuncTyping03() throws Exception {
        checkCompletion("newInInitializersFuncTyping03", "function func1($param = new Fun^) {}");
    }

    public void testNewInInitializersMethodTyping01() throws Exception {
        checkCompletion("newInInitializersMethodTyping01", "public $prop = ^,");
    }

    public void testNewInInitializersMethodTyping02() throws Exception {
        checkCompletion("newInInitializersMethodTyping02", "public $prop = ne^,");
    }

    public void testNewInInitializersMethodTyping03() throws Exception {
        checkCompletion("newInInitializersMethodTyping03", "public $prop = new Fo^,");
    }

    public void testNewInInitializersMethodTyping04() throws Exception {
        checkCompletion("newInInitializersMethodTyping04", "$param = ^");
    }

    public void testNewInInitializersMethodTyping05() throws Exception {
        checkCompletion("newInInitializersMethodTyping05", "$param = ne^");
    }

    public void testNewInInitializersMethodTyping06() throws Exception {
        checkCompletion("newInInitializersMethodTyping06", "$param = new Fo^");
    }

    public void testNewInInitializersAttributeTyping01() throws Exception {
        checkCompletion("newInInitializersAttributeTyping01", "#[AnAttribute(^)]");
    }

    public void testNewInInitializersAttributeTyping02() throws Exception {
        checkCompletion("newInInitializersAttributeTyping02", "#[AnAttribute(ne^)]");
    }

    public void testNewInInitializersAttributeTyping03() throws Exception {
        checkCompletion("newInInitializersAttributeTyping03", "#[AnAttribute(new Fo^)]");
    }

    public void testIntersectionTypes_01() throws Exception {
        checkCompletion("intersectionTypes", "        $this->^publicFieldInterfaceImpl->publicMethodClass1();");
    }

    public void testIntersectionTypes_02() throws Exception {
        checkCompletion("intersectionTypes", "        $this->publicFieldInterfaceImpl->^publicMethodClass1();");
    }

    public void testIntersectionTypes_03() throws Exception {
        checkCompletion("intersectionTypes", "        $this->publicFieldTrait1->^publicMethodClass1();");
    }

    public void testIntersectionTypes_04() throws Exception {
        checkCompletion("intersectionTypes", "        self::^$publicStaticFieldInterfaceImpl->publicMethodClass1();");
    }

    public void testIntersectionTypes_05() throws Exception {
        checkCompletion("intersectionTypes", "        self::$publicStaticFieldInterfaceImpl->^publicMethodClass1();");
    }

    public void testIntersectionTypes_06() throws Exception {
        checkCompletion("intersectionTypes", "        self::$publicStaticFieldInterfaceImpl::^CONST_CLASS1;");
    }

    public void testIntersectionTypes_07() throws Exception {
        checkCompletion("intersectionTypes", "        $object->^publicMethodClass1()->publicMethodClass2();");
    }

    public void testIntersectionTypes_08() throws Exception {
        checkCompletion("intersectionTypes", "        $object->publicMethodClass1()->^publicMethodClass2();");
    }

    public void testIntersectionTypes_09() throws Exception {
        checkCompletion("intersectionTypes", "        $object->publicMethodClass1()::^CONST_CLASS2;");
    }

    public void testIntersectionTypes_10() throws Exception {
        checkCompletion("intersectionTypes", "        $object::^$publicFieldClass1->publicMethodClass1();");
    }

    public void testIntersectionTypes_11() throws Exception {
        checkCompletion("intersectionTypes", "        $object::$publicFieldClass1->^publicMethodClass1();");
    }

    public void testIntersectionTypes_12() throws Exception {
        checkCompletion("intersectionTypes", "        $object::$publicFieldClass1::^publicStaticMethodClass1();");
    }

    public void testIntersectionTypes_13() throws Exception {
        checkCompletion("intersectionTypes", "        $object->^publicMethodClass2(); // phpdoc");
    }

    public void testIntersectionTypes_14() throws Exception {
        checkCompletion("intersectionTypes", "        $object::^publicStaticMethodClass2(); // phpdoc");
    }

    public void testIntersectionTypes_15() throws Exception {
        checkCompletion("intersectionTypes", "        $this::^$publicStaticFieldTrait1->publicMethodClass1();");
    }

    public void testIntersectionTypes_16() throws Exception {
        checkCompletion("intersectionTypes", "        $this::$publicStaticFieldTrait1->^publicMethodClass1();");
    }

    public void testIntersectionTypes_17() throws Exception {
        checkCompletion("intersectionTypes", "        $this::publicStaticMethodTrait1()::^publicStaticMethodClass1();");
    }

    public void testIntersectionTypes_18() throws Exception {
        checkCompletion("intersectionTypes", "        $object->^publicMethodClass1(); // with whitespaces");
    }

    public void testIntersectionTypes_19() throws Exception {
        checkCompletion("intersectionTypes", "        $object::^publicStaticMethodClass1(); // with whitespaces");
    }

    public void testIntersectionTypesFields01() throws Exception {
        checkCompletion("intersectionTypesFields01", "    private IntersectionTypes1&^");
    }

    public void testIntersectionTypesFields02() throws Exception {
        checkCompletion("intersectionTypesFields02", "    private IntersectionTypes1&\\Test1\\^");
    }

    public void testIntersectionTypesFields03() throws Exception {
        checkCompletion("intersectionTypesFields03", "    private Test&\\Test1\\IntersectionTypes2&^");
    }

    public void testIntersectionTypesFields04() throws Exception {
        checkCompletion("intersectionTypesFields04", "    private Test&\\Test1\\IntersectionTypes2&\\Test1\\IntersectionT^ypes2 $test;");
    }

    public void testIntersectionTypesFields05() throws Exception {
        checkCompletion("intersectionTypesFields05", "    private Test&\\Test1\\IntersectionTypes2&^&\\Test1\\IntersectionTypes1 $test;");
    }

    public void testIntersectionTypesFields06() throws Exception {
        checkCompletion("intersectionTypesFields06", "    private static Test&\\Test1\\IntersectionTypes2&^\\Test1\\IntersectionTypes1 $test;");
    }

    public void testIntersectionTypesFunctionParameterType01() throws Exception {
        checkCompletion("intersectionTypesFunctionParameterType01", "function intersection_types(IntersectionTypes1&^)");
    }

    public void testIntersectionTypesFunctionParameterType02() throws Exception {
        checkCompletion("intersectionTypesFunctionParameterType02", "function intersection_types(IntersectionTypes1&\\Test\\I^)");
    }

    public void testIntersectionTypesFunctionParameterType03() throws Exception {
        checkCompletion("intersectionTypesFunctionParameterType03", "function intersection_types(IntersectionTypes1&\\Test\\IntersectionTypes2&^)");
    }

    public void testIntersectionTypesFunctionParameterType04() throws Exception {
        checkCompletion("intersectionTypesFunctionParameterType04", "function intersection_types(IntersectionTypes1&^&\\Test\\IntersectionTypes2)");
    }

    public void testIntersectionTypesFunctionParameterType05() throws Exception {
        checkCompletion("intersectionTypesFunctionParameterType05", "function intersection_types(IntersectionTypes1&^) {");
    }

    public void testIntersectionTypesFunctionParameterType06() throws Exception {
        checkCompletion("intersectionTypesFunctionParameterType06", "function intersection_types(IntersectionTypes1&Inte^) {");
    }

    public void testIntersectionTypesFunctionParameterType07() throws Exception {
        checkCompletion("intersectionTypesFunctionParameterType07", "function intersection_types(IntersectionTypes1&IntersectionTypes2 $param, IntersectionTypes1&IntersectionTypes2&^) {");
    }

    public void testIntersectionTypesFunctionParameterType08() throws Exception {
        checkCompletion("intersectionTypesFunctionParameterType08", "function intersection_types(IntersectionTypes1&IntersectionTypes2 $param, IntersectionTypes1&IntersectionTypes2&\\Test\\^) {");
    }

    public void testIntersectionTypesFunctionParameterType09() throws Exception {
        checkCompletion("intersectionTypesFunctionParameterType09", "function intersection_types(IntersectionTypes1&IntersectionTypes2 $param, IntersectionTypes1&IntersectionTypes2&\\Test\\Inter^) {");
    }

    public void testIntersectionTypesFunctionReturnType01() throws Exception {
        checkCompletion("intersectionTypesFunctionReturnType01", "function intersection_types(IntersectionTypes1&\\Test\\IntersectionTypes2 $param): IntersectionTypes1&^");
    }

    public void testIntersectionTypesFunctionReturnType02() throws Exception {
        checkCompletion("intersectionTypesFunctionReturnType02", "function intersection_types(IntersectionTypes1&\\Test\\IntersectionTypes2 $param): IntersectionTypes1&\\Test\\^");
    }

    public void testIntersectionTypesFunctionReturnType03() throws Exception {
        checkCompletion("intersectionTypesFunctionReturnType03", "function intersection_types(IntersectionTypes1&\\Test\\IntersectionTypes2 $param): IntersectionTypes1&\\Test\\IntersectionTypes2&In^");
    }

    public void testIntersectionTypesFunctionReturnType04() throws Exception {
        checkCompletion("intersectionTypesFunctionReturnType04", "function intersection_types(IntersectionTypes1&\\Test\\IntersectionTypes2 $param): IntersectionTypes1&^&\\Test\\IntersectionTypes2");
    }

    public void testIntersectionTypesFunctions_01() throws Exception {
        checkCompletion("intersectionTypesFunctions", "function intersection_types(Intersect^ion1&Intersection2&\\Test\\Intersection3 $param1, Intersection1&Intersection2 $param): Intersection1& Intersection3 {");
    }

    public void testIntersectionTypesFunctions_02() throws Exception {
        checkCompletion("intersectionTypesFunctions", "function intersection_types(Intersection1&Intersection2&\\Test\\Intersection3 $param1, Intersection1&^Intersection2 $param): Intersection1& Intersection3 {");
    }

    public void testIntersectionTypesFunctions_03() throws Exception {
        checkCompletion("intersectionTypesFunctions", "function intersection_types(Intersection1&Intersection2&\\Test\\Intersection3 $param1, Intersection1&Intersect^ion2 $param): Intersection1& Intersection3 {");
    }

    public void testIntersectionTypesFunctions_04() throws Exception {
        checkCompletion("intersectionTypesFunctions", "function intersection_types(Intersection1&Intersection2&\\Test\\Intersection3 $param1, Intersection1&Intersection2 $param): ^Intersection1& Intersection3 {");
    }

    public void testIntersectionTypesFunctions_05() throws Exception {
        checkCompletion("intersectionTypesFunctions", "function intersection_types(Intersection1&Intersection2&\\Test\\Intersection3 $param1, Intersection1&Intersection2 $param): Intersection1& ^Intersection3 {");
    }

    public void testIntersectionTypesFunctions_06() throws Exception {
        checkCompletion("intersectionTypesFunctions", "function intersection_types(Intersection1&Intersection2&\\Test\\Intersection3 $param1, Intersection1&Intersection2 $param): Intersection1& Intersec^tion3 {");
    }

    public void testIntersectionTypesFunctions_07() throws Exception {
        checkCompletion("intersectionTypesFunctions", "$closure = function(Intersection1&Intersection2&\\Test\\Intersection3 $param1, Intersecti^on1& Intersection2 $param): Intersection1&Intersection2 {");
    }

    public void testIntersectionTypesFunctions_08() throws Exception {
        checkCompletion("intersectionTypesFunctions", "$closure = function(Intersection1&Intersection2&\\Test\\Intersection3 $param1, Intersection1& ^Intersection2 $param): Intersection1&Intersection2 {");
    }

    public void testIntersectionTypesFunctions_09() throws Exception {
        checkCompletion("intersectionTypesFunctions", "$closure = function(Intersection1&Intersection2&\\Test\\Intersection3 $param1, Intersection1& Intersection2 $param): ^Intersection1&Intersection2 {");
    }

    public void testIntersectionTypesFunctions_10() throws Exception {
        checkCompletion("intersectionTypesFunctions", "$closure = function(Intersection1&Intersection2&\\Test\\Intersection3 $param1, Intersection1& Intersection2 $param): Intersection1&^Intersection2 {");
    }

    public void testIntersectionTypesFunctions_11() throws Exception {
        checkCompletion("intersectionTypesFunctions", "$closure = function(Intersection1&Intersection2&\\Test\\Intersection3 $param1, Intersection1& Intersection2 $param): Intersection1&Intersec^tion2 {");
    }

    public void testIntersectionTypesFunctions_12() throws Exception {
        checkCompletion("intersectionTypesFunctions", "$arrow = fn(Intersection1&Intersection2&^\\Test\\Intersection3 $param1, Intersection1&Intersection2 $param): \\Test\\Intersection1&\\Test\\Intersection2&Intersection3 {");
    }

    public void testIntersectionTypesFunctions_13() throws Exception {
        checkCompletion("intersectionTypesFunctions", "$arrow = fn(Intersection1&Intersection2&\\Test\\Intersection3 $param1, Intersection1&^Intersection2 $param): \\Test\\Intersection1&\\Test\\Intersection2&Intersection3 {");
    }

    public void testIntersectionTypesFunctions_14() throws Exception {
        checkCompletion("intersectionTypesFunctions", "$arrow = fn(Intersection1&Intersection2&\\Test\\Intersection3 $param1, Intersection1&Intersection2^ $param): \\Test\\Intersection1&\\Test\\Intersection2&Intersection3 {");
    }

    public void testIntersectionTypesFunctions_15() throws Exception {
        checkCompletion("intersectionTypesFunctions", "$arrow = fn(Intersection1&Intersection2&\\Test\\Intersection3 $param1, Intersection1&Intersection2 $param): ^\\Test\\Intersection1&\\Test\\Intersection2&Intersection3 {");
    }

    public void testIntersectionTypesFunctions_16() throws Exception {
        checkCompletion("intersectionTypesFunctions", "$arrow = fn(Intersection1&Intersection2&\\Test\\Intersection3 $param1, Intersection1&Intersection2 $param): \\Test\\Inte^rsection1&\\Test\\Intersection2&Intersection3 {");
    }

    public void testIntersectionTypesFunctions_17() throws Exception {
        checkCompletion("intersectionTypesFunctions", "$arrow = fn(Intersection1&Intersection2&\\Test\\Intersection3 $param1, Intersection1&Intersection2 $param): \\Test\\Intersection1&^\\Test\\Intersection2&Intersection3 {");
    }

    public void testIntersectionTypesFunctions_18() throws Exception {
        checkCompletion("intersectionTypesFunctions", "$arrow = fn(Intersection1&Intersection2&\\Test\\Intersection3 $param1, Intersection1&Intersection2 $param): \\Test\\Intersection1&\\Test\\^Intersection2&Intersection3 {");
    }

    public void testIntersectionTypesFunctions_19() throws Exception {
        checkCompletion("intersectionTypesFunctions", "$arrow = fn(Intersection1&Intersection2&\\Test\\Intersection3 $param1, Intersection1&Intersection2 $param): \\Test\\Intersection1&\\Test\\Intersection2&^Intersection3 {");
    }

    public void testIntersectionTypesFunctions_20() throws Exception {
        checkCompletion("intersectionTypesFunctions", "$arrow = fn(Intersection1&Intersection2&\\Test\\Intersection3 $param1, Intersection1&Intersection2 $param): \\Test\\Intersection1&\\Test\\Intersection2&Intersection3^ {");
    }

    public void testIntersectionTypesImplementMethod01() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testIntersectionTypesImplementMethod01"), "    test^",
                new DefaultFilter(PhpVersion.PHP_81, "test"), true);
    }

    public void testIntersectionTypesImplementMethod02() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testIntersectionTypesImplementMethod02"), "    test^",
                new DefaultFilter(PhpVersion.PHP_81, "test"), true);
    }

    public void testIntersectionTypesImplementMethod03() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testIntersectionTypesImplementMethod03"), "    test^",
                new DefaultFilter(PhpVersion.PHP_81, "test"), true);
    }

    public void testIntersectionTypesImplementMethod04() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testIntersectionTypesImplementMethod04"), "    test^",
                new DefaultFilter(PhpVersion.PHP_81, "test"), true);
    }

    public void testIntersectionTypesMethods_01() throws Exception {
        checkCompletion("intersectionTypesMethods", "           ^\\Test1\\Intersection1&TestClass $object,");
    }

    public void testIntersectionTypesMethods_02() throws Exception {
        checkCompletion("intersectionTypesMethods", "            \\Test1\\^Intersection1&TestClass $object,");
    }

    public void testIntersectionTypesMethods_03() throws Exception {
        checkCompletion("intersectionTypesMethods", "            \\Test1\\Intersection1&^TestClass $object,");
    }

    public void testIntersectionTypesMethods_04() throws Exception {
        checkCompletion("intersectionTypesMethods", "            \\Test1\\Intersection1&Test^Class $object,");
    }

    public void testIntersectionTypesMethods_05() throws Exception {
        checkCompletion("intersectionTypesMethods", "            ^TestClass&\\Test1\\Intersection1 $param,");
    }

    public void testIntersectionTypesMethods_06() throws Exception {
        checkCompletion("intersectionTypesMethods", "            TestClass&\\Test1\\Inters^ection1 $param,");
    }

    public void testIntersectionTypesMethods_07() throws Exception {
        checkCompletion("intersectionTypesMethods", "    ): ^\\Test1\\Intersection2&\\Test1\\Intersection1 {");
    }

    public void testIntersectionTypesMethods_08() throws Exception {
        checkCompletion("intersectionTypesMethods", "    ): \\Test1\\Intersection2&^\\Test1\\Intersection1 {");
    }

    public void testIntersectionTypesMethods_09() throws Exception {
        checkCompletion("intersectionTypesMethods", "    ): \\Test1\\Intersection2&\\Test1\\Intersect^ion1 {");
    }

    public void testIntersectionTypesMethods_10() throws Exception {
        checkCompletion("intersectionTypesMethods", "            \\Test1\\Intersection1&TestC^lass $object, // static");
    }

    public void testIntersectionTypesMethods_11() throws Exception {
        checkCompletion("intersectionTypesMethods", "TestClass&\\Test1\\Intersec^tion1 $param, // static");
    }

    public void testIntersectionTypesMethods_12() throws Exception {
        checkCompletion("intersectionTypesMethods", "    ): \\Test1\\Intersection2&^TestClass {");
    }

    public void testIntersectionTypesMethods_13() throws Exception {
        checkCompletion("intersectionTypesMethods", "    ): \\Test1\\Intersection2&TestC^lass {");
    }

    public void testIntersectionTypesMethods_14() throws Exception {
        checkCompletion("intersectionTypesMethods", "    public function childClassMethod(TestClass&\\Test1\\Intersection1 $object, TestClass&^\\Test1\\Intersection1 $object2): TestClass&\\Test1\\Intersection1 {");
    }

    public void testIntersectionTypesMethods_15() throws Exception {
        checkCompletion("intersectionTypesMethods", "    public function childClassMethod(TestClass&\\Test1\\Intersection1 $object, TestClass&\\Test1\\In^tersection1 $object2): TestClass&\\Test1\\Intersection1 {");
    }

    public void testIntersectionTypesMethods_16() throws Exception {
        checkCompletion("intersectionTypesMethods", "    public function childClassMethod(TestClass&\\Test1\\Intersection1 $object, TestClass&\\Test1\\Intersection1 $object2): TestClass&\\Test1\\Intersec^tion1 {");
    }

    public void testIntersectionTypesMethods_17() throws Exception {
        checkCompletion("intersectionTypesMethods", "    public function interfaceMethod(\\Test1\\Intersection1&TestClass $object, TestClass&\\Test1\\Intersectio^n1 $param, ): TestClass&\\Test1\\Intersection1;");
    }

    public void testIntersectionTypesMethods_18() throws Exception {
        checkCompletion("intersectionTypesMethods", "    public function interfaceMethod(\\Test1\\Intersection1&TestClass $object, TestClass&\\Test1\\Intersection1 $param, ): TestClass&\\Test1\\Intersec^tion1;");
    }

    public void testIntersectionTypesMethods_19() throws Exception {
        checkCompletion("intersectionTypesMethods", "    abstract protected function abstractClassMethod(TestClass&\\Test1\\Intersection1 $param, TestInerface&TestC^lass $object,): TestClass&\\Test1\\Intersection1;");
    }

    public void testIntersectionTypesMethods_20() throws Exception {
        checkCompletion("intersectionTypesMethods", "    abstract protected function abstractClassMethod(TestClass&\\Test1\\Intersection1 $param, TestInerface&TestClass $object,): TestClass&\\Test1\\Intersect^ion1;");
    }

    public void testIntersectionTypesMethods_21() throws Exception {
        checkCompletion("intersectionTypesMethods", "    private function traitMethod(TestInterface&\\Test1\\Intersection2 $object, TestClass&\\Test1\\Intersec^tion1 $param): TestInterface&TestClass {");
    }

    public void testIntersectionTypesMethods_22() throws Exception {
        checkCompletion("intersectionTypesMethods", "    private function traitMethod(TestInterface&\\Test1\\Intersection2 $object, TestClass&\\Test1\\Intersection1 $param): TestInterface&TestC^lass {");
    }

    public void testIntersectionTypesOverrideMethod01() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testIntersectionTypesOverrideMethod01"), "    test^",
                new DefaultFilter(PhpVersion.PHP_81, "test"), true);
    }

    public void testIntersectionTypesOverrideMethod02() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testIntersectionTypesOverrideMethod02"), "    test^",
                new DefaultFilter(PhpVersion.PHP_81, "test"), true);
    }

    public void testIntersectionTypesOverrideMethod03() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testIntersectionTypesOverrideMethod03"), "    test^",
                new DefaultFilter(PhpVersion.PHP_81, "test"), true);
    }

    public void testEnums_01() throws Exception {
        checkCompletion("enums", "const CONSTANT2 = self::^CASE2;");
    }

    public void testEnums_02() throws Exception {
        checkCompletion("enums", "const CONSTANT2 = self::CASE^2;");
    }

    public void testEnums_03() throws Exception {
        checkCompletion("enums", "static::CAS^E1 => 'Case1',");
    }

    public void testEnums_04() throws Exception {
        checkCompletion("enums", "        Simple::^CASE2;");
    }

    public void testEnums_05() throws Exception {
        checkCompletion("enums", "        Simple::C^ASE2;");
    }

    public void testEnums_06() throws Exception {
        checkCompletion("enums", "        Simple::PRIV^ATE_CONST;");
    }

    public void testEnums_07() throws Exception {
        checkCompletion("enums", "        Simple::protected^StaticEnumMethod();");
    }

    public void testEnums_08() throws Exception {
        checkCompletion("enums", "        Simple::CASE1->^publicEnumMethod();");
    }

    public void testEnums_09() throws Exception {
        checkCompletion("enums", "        Simple::CASE1->publicEnum^Method();");
    }

    public void testEnums_10() throws Exception {
        checkCompletion("enums", "Simple::CASE1::^publicStaticEnumMethod();");
    }

    public void testEnums_11() throws Exception {
        checkCompletion("enums", "Simple::CASE1::publicStatic^EnumMethod();");
    }

    public void testEnums_12() throws Exception {
        checkCompletion("enums", "        self::^CASE1;");
    }

    public void testEnums_13() throws Exception {
        checkCompletion("enums", "        self::CASE^1;");
    }

    public void testEnums_14() throws Exception {
        checkCompletion("enums", "        self::privateStatic^EnumMethod();");
    }

    public void testEnums_15() throws Exception {
        checkCompletion("enums", "        self::CASE^1->privateEnumMethod();");
    }

    public void testEnums_16() throws Exception {
        checkCompletion("enums", "        self::CASE1->priv^ateEnumMethod();");
    }

    public void testEnums_17() throws Exception {
        checkCompletion("enums", "        self::CASE1^::protectedStaticEnumMethod();");
    }

    public void testEnums_18() throws Exception {
        checkCompletion("enums", "        self::CASE1::protectedStatic^EnumMethod();");
    }

    public void testEnums_19() throws Exception {
        checkCompletion("enums", "        static::p^rivateStaticEnumMethod();");
    }

    public void testEnums_20() throws Exception {
        checkCompletion("enums", "        static::CASE1->p^ublicEnumMethod();");
    }

    public void testEnums_21() throws Exception {
        checkCompletion("enums", "        static::CASE1::p^ublicEnumMethod();");
    }

    public void testEnums_22() throws Exception {
        checkCompletion("enums", "Simple::CASE1::^CONSTANT1;");
    }

    public void testEnums_23() throws Exception {
        checkCompletion("enums", "Simple::CASE1::CON^STANT1;");
    }

    public void testEnums_24() throws Exception {
        checkCompletion("enums", "Simple::CASE2->^publicEnumMethod();");
    }

    public void testEnums_25() throws Exception {
        checkCompletion("enums", "Simple::CASE2->public^EnumMethod();");
    }

    public void testEnums_26() throws Exception {
        checkCompletion("enums", "Simple::^publicStaticEnumMethod();");
    }

    public void testEnums_27() throws Exception {
        checkCompletion("enums", "Simple::publicStatic^EnumMethod();");
    }

    public void testEnums_28() throws Exception {
        checkCompletion("enums", "$i::^CASE1;");
    }

    public void testEnums_29() throws Exception {
        checkCompletion("enums", "$i::CASE^1;");
    }

    public void testEnums_30() throws Exception {
        checkCompletion("enums", "$i->^publicEnumMethod();");
    }

    public void testEnums_31() throws Exception {
        checkCompletion("enums", "$i->public^EnumMethod();");
    }

    public void testEnumsTyping_01() throws Exception {
        checkCompletion("enumsTyping_01", "/*test */ Si^");
    }

    public void testEnumsTyping_02() throws Exception {
        checkCompletion("enumsTyping_02", "/*test */ Simple::^");
    }

    public void testEnumsTyping_03() throws Exception {
        checkCompletion("enumsTyping_03", "/*test */ Simple::CA^");
    }

    public void testEnumsTyping_04() throws Exception {
        checkCompletion("enumsTyping_04", "/*test */ Simple::CASE1::^");
    }

    public void testEnumsTyping_05() throws Exception {
        checkCompletion("enumsTyping_05", "/*test */ Simple::CASE1::pub^");
    }

    public void testEnumsTyping_06() throws Exception {
        checkCompletion("enumsTyping_06", "/*test */ Simple::CASE1->^");
    }

    public void testEnumsTyping_07() throws Exception {
        checkCompletion("enumsTyping_07", "/*test */ Simple::CASE1->publicE^");
    }

    public void testEnumsTyping_use_01() throws Exception {
        checkCompletion("enumsTyping_use_01", "use Enum1\\^");
    }

    public void testEnumsTyping_use_02() throws Exception {
        checkCompletion("enumsTyping_use_02", "use Enum1\\Sim^");
    }

    public void testEnumsTyping_fqn_01() throws Exception {
        checkCompletion("enumsTyping_fqn_01", "\\Enum1\\^");
    }

    public void testEnumsTyping_fqn_02() throws Exception {
        checkCompletion("enumsTyping_fqn_02", "\\Enum1\\Sim^");
    }

    public void testEnumsInConstExpr01() throws Exception {
        checkCompletion("enumsInConstExpr01", "    public const PUBLIC_CONST = ^;");
    }

    public void testEnumsInConstExpr02() throws Exception {
        checkCompletion("enumsInConstExpr02", "    public const PUBLIC_CONST = Enum^;");
    }

    public void testEnumsReturnType_01() throws Exception {
        checkCompletion("enumsReturnType", "function returnType(): ^Enum1 {");
    }

    public void testEnumsReturnType_02() throws Exception {
        checkCompletion("enumsReturnType", "function returnType(): Enum^1 {");
    }

    public void testEnumsReturnType_03() throws Exception {
        checkCompletion("enumsReturnType", "    public function publicMethod1(): ^Enum1 {");
    }

    public void testEnumsReturnType_04() throws Exception {
        checkCompletion("enumsReturnType", "    public function publicMethod1(): Enu^m1 {");
    }

    public void testEnumsReturnType_05() throws Exception {
        checkCompletion("enumsReturnType", "    public function publicMethod2(): Enum1|Enum^2 {");
    }

    public void testEnumsReturnType_06() throws Exception {
        checkCompletion("enumsReturnType", "    public static function publicMethod3(): Enu^m1&Enum2 {");
    }

    public void testEnumsReturnType_07() throws Exception {
        checkCompletion("enumsReturnType", "    public static function publicMethod3(): Enum1&Enum2^ {");
    }

    public void testEnumsReturnType_08() throws Exception {
        checkCompletion("enumsReturnType", "    public function publicMethod4(): ?^Enum1 {");
    }

    public void testEnumsReturnType_09() throws Exception {
        checkCompletion("enumsReturnType", "    public function publicMethod4(): ?Enum^1 {");
    }

    public void testEnumsParamType_01() throws Exception {
        checkCompletion("enumsParamType", "function paramType(^EnumTest $param): void {");
    }

    public void testEnumsParamType_02() throws Exception {
        checkCompletion("enumsParamType", "function paramType(Enum^Test $param): void {");
    }

    public void testEnumsParamType_03() throws Exception {
        checkCompletion("enumsParamType", "    public function publicMethod1(En^um1 $enum1, Enum2 $enum2): void {");
    }

    public void testEnumsParamType_04() throws Exception {
        checkCompletion("enumsParamType", "    public function publicMethod1(Enum1 $enum1, Enum2^ $enum2): void {");
    }

    public void testEnumsParamType_05() throws Exception {
        checkCompletion("enumsParamType", "    public static function publicMethod2(Enu^m1|Enum2 $enum1): void {");
    }

    public void testEnumsParamType_06() throws Exception {
        checkCompletion("enumsParamType", "    public static function publicMethod2(Enum1|Enum^2 $enum1): void {");
    }

    public void testEnumsParamType_07() throws Exception {
        checkCompletion("enumsParamType", "    public function publicMethod3(Enum1&Enum2 $enum1, En^um1&Enum2 $enum2): void {");
    }

    public void testEnumsParamType_08() throws Exception {
        checkCompletion("enumsParamType", "    public function publicMethod3(Enum1&Enum2 $enum1, Enum1&^Enum2 $enum2): void {");
    }

    public void testEnumsParamType_09() throws Exception {
        checkCompletion("enumsParamType", "    public function publicMethod4(?^Enum1 $enum1): void {");
    }

    public void testEnumsParamType_10() throws Exception {
        checkCompletion("enumsParamType", "    public function publicMethod4(?Enum^1 $enum1): void {");
    }

    public void testEnumsFieldType_01a() throws Exception {
        checkCompletion("enumsFieldType", "    private ^Enum1 $field;");
    }

    public void testEnumsFieldType_01b() throws Exception {
        checkCompletion("enumsFieldType", "    private En^um1 $field;");
    }

    public void testEnumsFieldType_02a() throws Exception {
        checkCompletion("enumsFieldType", "    public static ^Enum1 $staticField;");
    }

    public void testEnumsFieldType_02b() throws Exception {
        checkCompletion("enumsFieldType", "    public static Enum^1 $staticField;");
    }

    public void testEnumsImplementTypesTyping_01() throws Exception {
        checkCompletion("enumsImplementTypesTyping_01", "enum EnumTest implements ^ {");
    }

    public void testEnumsImplementTypesTyping_02() throws Exception {
        checkCompletion("enumsImplementTypesTyping_02", "enum EnumTest implements EnumInter^ {");
    }

    public void testEnumsImplementTypesTyping_03() throws Exception {
        checkCompletion("enumsImplementTypesTyping_03", "enum EnumTest implements EnumInterface1, EnumIn^ {");
    }

    public void testEnumsImplementTypesTyping_01b() throws Exception {
        checkCompletion("enumsImplementTypesTyping_01b", "enum EnumTest:int implements ^ {");
    }

    public void testEnumsImplementTypesTyping_02b() throws Exception {
        checkCompletion("enumsImplementTypesTyping_02b", "enum EnumTest:int implements EnumInter^ {");
    }

    public void testEnumsImplementTypesTyping_03b() throws Exception {
        checkCompletion("enumsImplementTypesTyping_03b", "enum EnumTest:int implements EnumInterface1, EnumIn^ {");
    }

    public void testEnumsBackingTypesTyping_01() throws Exception {
        checkCompletion("enumsBackingTypesTyping_01", "enum EnumTest: ^");
    }

    public void testEnumsBackingTypesTyping_02() throws Exception {
        checkCompletion("enumsBackingTypesTyping_02", "enum EnumTest: in^");
    }

    public void testEnumsBackingTypesTyping_03() throws Exception {
        checkCompletion("enumsBackingTypesTyping_03", "enum EnumTest: int i^");
    }

    public void testEnumsFieldTypeTyping01() throws Exception {
        checkCompletion("enumsFieldTypeTyping01", "    private ^");
    }

    public void testEnumsFieldTypeTyping02() throws Exception {
        checkCompletion("enumsFieldTypeTyping02", "    private En^");
    }

    public void testEnumsFieldTypeTyping03() throws Exception {
        checkCompletion("enumsFieldTypeTyping03", "    private static ^");
    }

    public void testEnumsFieldTypeTyping04() throws Exception {
        checkCompletion("enumsFieldTypeTyping04", "    private static Enum^");
    }

    public void testEnumsUnionAndBackedMembers_01() throws Exception {
        checkCompletion("enumsUnionAndBackedMembers", "        self::^from(\"apple\");");
    }

    public void testEnumsUnionAndBackedMembers_02() throws Exception {
        checkCompletion("enumsUnionAndBackedMembers", "            self::APPLE => \"apple\" === self::APPLE->^value,");
    }

    public void testEnumsUnionAndBackedMembers_03() throws Exception {
        checkCompletion("enumsUnionAndBackedMembers", "            self::BANANA => \"banana\" === self::BANANA?->^value,");
    }

    public void testEnumsUnionAndBackedMembers_04() throws Exception {
        checkCompletion("enumsUnionAndBackedMembers", "        self::TEST1->^value;");
    }

    public void testEnumsUnionAndBackedMembers_05() throws Exception {
        checkCompletion("enumsUnionAndBackedMembers", "Union::^cases();");
    }

    public void testEnumCasesTyping01() throws Exception {
        checkCompletion("enumCasesTyping01", "    case CASE_C = self::^");
    }

    public void testEnumCasesTyping02() throws Exception {
        checkCompletion("enumCasesTyping02", "    case CASE_C = self::CASE_A^");
    }

    public void testEnumCasesTyping03() throws Exception {
        checkCompletion("enumCasesTyping03", "    case CASE_C = self::^;");
    }

    // GH-5100
    public void testEnumsSpecialVariablesWithinInstanceContextGH5100_01() throws Exception {
        checkCompletion("enumsSpecialVariablesWithinInstanceContextGH5100", "        $this->^publicEnumMethod();");
    }

    public void testEnumsSpecialVariablesWithinInstanceContextGH5100_02() throws Exception {
        checkCompletion("enumsSpecialVariablesWithinInstanceContextGH5100", "        $^ // test keywords");
    }

    public void testEnumsSpecialVariablesWithinInstanceContextGH5100_03() throws Exception {
        checkCompletion("enumsSpecialVariablesWithinInstanceContextGH5100", "        sel^f::class;");
    }

    public void testEnumsSpecialVariablesWithinInstanceContextGH5100_04() throws Exception {
        checkCompletion("enumsSpecialVariablesWithinInstanceContextGH5100", "        stat^ic::class;");
    }

    public void testEnumsAliasedName_01() throws Exception {
        checkCompletion("enumsAliasedName", "Aliased^ // test");
    }

    public void testFirstClassCallableSyntax_01() throws Exception {
        checkCompletionForFirstClassCallable("firstClassCallableSyntax", "tes^t(...);");
    }

    public void testFirstClassCallableSyntax_02() throws Exception {
        // can't use it with new expression
        checkCompletionForFirstClassCallable("firstClassCallableSyntax", "$test = new Te^st();");
    }

    public void testFirstClassCallableSyntax_03() throws Exception {
        checkCompletionForFirstClassCallable("firstClassCallableSyntax", "$fn = $test->met^hod(...);");
    }

    public void testFirstClassCallableSyntax_04() throws Exception {
        checkCompletionForFirstClassCallable("firstClassCallableSyntax", "$fn = Test::staticM^ethod(...);");
    }

    public void testFirstClassCallableSyntax_05() throws Exception {
        checkCompletionForFirstClassCallable("firstClassCallableSyntax", "$fn = $test::staticMeth^od(...);");
    }

    public void testFirstClassCallableSyntax_06() throws Exception {
        checkCompletionForFirstClassCallable("firstClassCallableSyntax", "$fn = $test->staticMet^hod(...);");
    }

    public void testFirstClassCallableSyntax_07() throws Exception {
        // can't use it with null safe operator(?->), but show it atm...
        checkCompletionForFirstClassCallable("firstClassCallableSyntax", "$test?->meth^od($test->method(...));");
    }

    public void testFirstClassCallableSyntax_08() throws Exception {
        checkCompletionForFirstClassCallable("firstClassCallableSyntax", "$test?->method($test->met^hod(...));");
    }

    public void testFirstClassCallableSyntax_09() throws Exception {
        checkCompletionForFirstClassCallable("firstClassCallableSyntax", "$fn = (new Test)->meth^od(...);");
    }

}
