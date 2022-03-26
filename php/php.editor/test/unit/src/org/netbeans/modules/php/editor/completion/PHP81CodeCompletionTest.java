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

    private String getTestDirName() {
        String name = getName();
        int indexOf = name.indexOf("_");
        if (indexOf != -1) {
            name = name.substring(0, indexOf);
        }
        return name;
    }

    private String getTestPath(String fileName) {
        return String.format("testfiles/completion/lib/php81/%s/%s.php", getTestDirName(), fileName);
    }

    private void checkCompletion(String fileName, String caretPosition) throws Exception {
        checkCompletion(getTestPath(fileName), caretPosition, false);
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
}
