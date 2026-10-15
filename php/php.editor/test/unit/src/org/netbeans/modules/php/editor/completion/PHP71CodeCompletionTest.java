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

public class PHP71CodeCompletionTest extends PHPCodeCompletionTestBase {

    public PHP71CodeCompletionTest(String testName) {
        super(testName);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[]{
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/php71/" + getTestDirName()))
            })
        );
    }

    private String getTestPath(String fileName) {
        return String.format("testfiles/completion/lib/php71/%s/%s.php", getTestDirName(), fileName);
    }

    // after nullable type prefix
    // "void" is invalid
    public void testNullableTypes_ReturnType01() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "public function classReturnType(): ?^Foo {", false);
    }

    public void testNullableTypes_ReturnType02() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "public function classReturnType(): ?F^oo {", false);
    }

    public void testNullableTypes_ReturnType03() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "public static function classReturnTypeStatic(): ?^Foo {", false);
    }

    public void testNullableTypes_ReturnType04() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "public static function classReturnTypeStatic(): ?F^oo {", false);
    }

    public void testNullableTypes_ReturnType05() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "public function traitReturnType(): ?^Foo {", false);
    }

    public void testNullableTypes_ReturnType06() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "public function traitReturnType(): ?Fo^o {", false);
    }

    public void testNullableTypes_ReturnType07() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "public static function traitReturnTypeStatic(): ?\\My\\S^ub\\Foo {", false);
    }

    public void testNullableTypes_ReturnType08() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "public static function traitReturnTypeStatic(): ?\\My\\Sub\\F^oo {", false);
    }

    public void testNullableTypes_ReturnType09() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "public function interfaceReturnType(?string $string): ?^Foo;", false);
    }

    public void testNullableTypes_ReturnType10() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "public function interfaceReturnType(?string $string): ?Fo^o;", false);
    }

    public void testNullableTypes_ReturnType11() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "public static function interfaceReturnTypeStatic(): ?^Foo;", false);
    }

    public void testNullableTypes_ReturnType12() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "public static function interfaceReturnTypeStatic(): ?F^oo;", false);
    }

    public void testNullableTypes_ReturnType13() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "function returnType(?Foo $foo): ?^Foo {", false);
    }

    public void testNullableTypes_ReturnType14() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "function returnType(?Foo $foo): ?Fo^o {", false);
    }

    public void testNullableTypes_ReturnType15() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "function returnType2(?Foo $foo): ?^iterable { // CC", false);
    }

    public void testNullableTypes_ReturnType16() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "function returnType2(?Foo $foo): ?i^terable { // CC", false);
    }

    public void testNullableTypes_ReturnTypeDispatch01() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "$nullableType->classReturnType()->^publicFooMethod();", false);
    }

    public void testNullableTypes_ReturnTypeDispatch02() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "NullableType::classReturnTypeStatic()->p^ublicFooMethod();", false);
    }

    public void testNullableTypes_ReturnTypeDispatch03() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "NullableTypeTrait::traitReturnTypeStatic()->^publicFooMethod();", false);
    }

    public void testNullableTypes_ReturnTypeDispatch04() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "NullableTypeInterface::interfaceReturnTypeStatic()->^publicFooMethod();", false);
    }

    public void testNullableTypes_ReturnTypeDispatch05() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "returnType()->^publicFooMethod();", false);
    }

    // after nullable type prefix
    public void testNullableTypes_ParameterType01() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "public function classParameterType(?^Foo $foo, ?string $stirng) {", false);
    }

    public void testNullableTypes_ParameterType02() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "public function classParameterType(?F^oo $foo, ?string $stirng) {", false);
    }

    public void testNullableTypes_ParameterType03() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "public static function classParameterTypeStatic(?Foo $foo, ?^string $stirng) {", false);
    }

    public void testNullableTypes_ParameterType04() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "public static function classParameterTypeStatic(?Foo $foo, ?st^ring $stirng) {", false);
    }

    public void testNullableTypes_ParameterType05() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "public function traitParameterType(?\\M^y\\Sub\\Foo $foo, ?string $stirng): Foo {", false);
    }

    public void testNullableTypes_ParameterType06() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "public function traitParameterType(?\\My\\Sub\\F^oo $foo, ?string $stirng): Foo {", false);
    }

    public void testNullableTypes_ParameterType07() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "public static function traitParameterTypeStatic(?Foo $foo, ?^string $stirng) {", false);
    }

    public void testNullableTypes_ParameterType08() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "public static function traitParameterTypeStatic(?Foo $foo, ?st^ring $stirng) {", false);
    }

    public void testNullableTypes_ParameterType09() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "public function interfaceParameterType(?^Foo $foo, ?string $stirng);", false);
    }

    public void testNullableTypes_ParameterType10() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "public function interfaceParameterType(?F^oo $foo, ?string $stirng);", false);
    }

    public void testNullableTypes_ParameterType11() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "public static function interfaceParameterTypeStatic(?Foo $foo, ?^string $stirng): ?string;", false);
    }

    public void testNullableTypes_ParameterType12() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "public static function interfaceParameterTypeStatic(?Foo $foo, ?st^ring $stirng): ?string;", false);
    }

    public void testNullableTypes_ParameterType13() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "function parameterType(?^Foo $foo): Foo {", false);
    }

    public void testNullableTypes_ParameterType14() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "function parameterType(?Fo^o $foo): Foo {", false);
    }

    public void testNullableTypes_ParameterTypeDispatch01() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "$foo->^publicFooMethod(); // CC class", false);
    }

    public void testNullableTypes_ParameterTypeDispatch02() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "$foo->^publicFooMethod(); // CC class static", false);
    }

    public void testNullableTypes_ParameterTypeDispatch03() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "$foo->^publicFooMethod(); // CC trait", false);
    }

    public void testNullableTypes_ParameterTypeDispatch04() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "$foo->^publicFooMethod(); // CC trait static", false);
    }

    public void testNullableTypes_ParameterTypeDispatch05() throws Exception {
        checkCompletion(getTestPath("nullableTypes"), "$foo->^publicFooMethod(); // CC function", false);
    }

    public void testNullableTypes_TypingReturnType01() throws Exception {
        checkCompletion(getTestPath("typingReturnType01"), "function mytest():?^", false);
    }

    public void testNullableTypes_TypingReturnType02() throws Exception {
        checkCompletion(getTestPath("typingReturnType02"), "function mytest(): ?^", false);
    }

    public void testNullableTypes_TypingReturnType03() throws Exception {
        checkCompletion(getTestPath("typingReturnType03"), "function mytest():?^{", false);
    }

    public void testNullableTypes_TypingReturnType04() throws Exception {
        checkCompletion(getTestPath("typingReturnType04"), "function mytest(): ?^{", false);
    }

    public void testNullableTypes_TypingReturnType05() throws Exception {
        checkCompletion(getTestPath("typingReturnType05"), "function test():?^", false);
    }

    public void testNullableTypes_TypingReturnType06() throws Exception {
        checkCompletion(getTestPath("typingReturnType06"), "function test(): ?^", false);
    }

    public void testNullableTypes_TypingReturnType07() throws Exception {
        checkCompletion(getTestPath("typingReturnType07"), "function test():?^;", false);
    }

    public void testNullableTypes_TypingReturnType08() throws Exception {
        checkCompletion(getTestPath("typingReturnType08"), "function test(): ?^;", false);
    }

    public void testNullableTypes_TypingReturnType09() throws Exception {
        checkCompletion(getTestPath("typingReturnType09"), "function test():?^", false);
    }

    public void testNullableTypes_TypingReturnType10() throws Exception {
        checkCompletion(getTestPath("typingReturnType10"), "function test(): ?^", false);
    }

    public void testNullableTypes_TypingReturnType11() throws Exception {
        checkCompletion(getTestPath("typingReturnType11"), "function test():?^{", false);
    }

    public void testNullableTypes_TypingReturnType12() throws Exception {
        checkCompletion(getTestPath("typingReturnType12"), "function test(): ?^;", false);
    }

    public void testNullableTypes_TypingParameterType01() throws Exception {
        checkCompletion(getTestPath("typingParameterType01"), "function mytest(?^", false);
    }

    public void testNullableTypes_TypingParameterType02() throws Exception {
        checkCompletion(getTestPath("typingParameterType02"), "function mytest(?^)", false);
    }

    public void testNullableTypes_TypingParameterType03() throws Exception {
        checkCompletion(getTestPath("typingParameterType03"), "function mytest(?^) {", false);
    }

    public void testNullableTypes_TypingParameterType04() throws Exception {
        checkCompletion(getTestPath("typingParameterType04"), "function mytest(?string $test, ?^) {", false);
    }

    public void testNullableTypes_TypingParameterType05() throws Exception {
        checkCompletion(getTestPath("typingParameterType05"), "public function mytest(?^", false);
    }

    public void testNullableTypes_TypingParameterType06() throws Exception {
        checkCompletion(getTestPath("typingParameterType06"), "public function mytest(?^)", false);
    }

    public void testNullableTypes_TypingParameterType07() throws Exception {
        checkCompletion(getTestPath("typingParameterType07"), "public function mytest(?^) {", false);
    }

    public void testNullableTypes_TypingParameterType08() throws Exception {
        checkCompletion(getTestPath("typingParameterType08"), "public function mytest(?string $test, ?^) {", false);
    }

    public void testNullableTypes_TypingParameterType09() throws Exception {
        checkCompletion(getTestPath("typingParameterType09"), "public function mytest(?^", false);
    }

    public void testNullableTypes_TypingParameterType10() throws Exception {
        checkCompletion(getTestPath("typingParameterType10"), "public function mytest(?^)", false);
    }

    public void testNullableTypes_TypingParameterType11() throws Exception {
        checkCompletion(getTestPath("typingParameterType11"), "public function mytest(?^);", false);
    }

    public void testNullableTypes_TypingParameterType12() throws Exception {
        checkCompletion(getTestPath("typingParameterType12"), "public function mytest(?string $test, ?^);", false);
    }

    public void testSanitizedNullableTypes_TypingParameterType01() throws Exception {
        checkCompletion(getTestPath("typingParameterType01"), "public function mytest(?ASaniti^);", false);
    }

    public void testSanitizedNullableTypes_TypingParameterType02() throws Exception {
        checkCompletion(getTestPath("typingParameterType02"), "public function mytest(?BSanitizing $sanitizing, ?BSanit^);", false);
    }

    public void testSanitizedNullableTypes_TypingParameterType03() throws Exception {
        checkCompletion(getTestPath("typingParameterType03"), "public function mytest(?CSaniti^ , ?CSanitizing $sanitizing);", false);
    }

    public void testSanitizedNullableTypes_TypingParameterType04() throws Exception {
        checkCompletion(getTestPath("typingParameterType04"), "public function mytest(?DSanitizing $sanitizing1,?DSan^ , ?DSanitizing $sanitizing3);", false);
    }

    // Multi Catch
    public void testMultiCatch_UnqualifiedName01() throws Exception {
        checkCompletion(getTestPath("unqualifiedName"), "} catch (^ExceptionType1 | ExceptionType2 $e) {", false);
    }

    public void testMultiCatch_UnqualifiedName02() throws Exception {
        checkCompletion(getTestPath("unqualifiedName"), "} catch (ExceptionType1 | ^ExceptionType2 $e) {", false);
    }

    public void testMultiCatch_UnqualifiedName03() throws Exception {
        checkCompletion(getTestPath("unqualifiedName"), "} catch (ExceptionType1 | ExceptionType2 ^$e) {", false);
    }

    public void testMultiCatch_UnqualifiedNameWithoutWS01() throws Exception {
        checkCompletion(getTestPath("unqualifiedNameWithoutWS"), "} catch(^ExceptionType1|ExceptionType2 $e) {", false);
    }

    public void testMultiCatch_UnqualifiedNameWithoutWS02() throws Exception {
        checkCompletion(getTestPath("unqualifiedNameWithoutWS"), "} catch(ExceptionType1|^ExceptionType2 $e) {", false);
    }

    public void testMultiCatch_UnqualifiedNameWithoutWS03() throws Exception {
        checkCompletion(getTestPath("unqualifiedNameWithoutWS"), "} catch(ExceptionType1|ExceptionType2 ^$e) {", false);
    }

    public void testMultiCatch_UnqualifiedNameDispatch01() throws Exception {
        checkCompletion(getTestPath("unqualifiedName"), "echo $e->^getTraceAsString(); // multi", false);
    }

    public void testMultiCatch_UnqualifiedNameDispatch02() throws Exception {
        checkCompletion(getTestPath("unqualifiedName"), "echo $e->^getTraceAsString(); // single", false);
    }

    public void testMultiCatch_FullyQualifiedName01() throws Exception {
        checkCompletion(getTestPath("fullyQualifiedName"), "} catch (^\\Test\\Sub\\ExceptionType1 | \\Test\\Sub\\ExceptionType2 $e) {", false);
    }

    public void testMultiCatch_FullyQualifiedName02() throws Exception {
        checkCompletion(getTestPath("fullyQualifiedName"), "} catch (\\Test\\Sub\\E^xceptionType1 | \\Test\\Sub\\ExceptionType2 $e) {", false);
    }

    public void testMultiCatch_FullyQualifiedName03() throws Exception {
        checkCompletion(getTestPath("fullyQualifiedName"), "} catch (\\Test\\Sub\\ExceptionType1 | ^\\Test\\Sub\\ExceptionType2 $e) {", false);
    }

    public void testMultiCatch_FullyQualifiedName04() throws Exception {
        checkCompletion(getTestPath("fullyQualifiedName"), "} catch (\\Test\\Sub\\ExceptionType1 | \\Test\\Sub\\Except^ionType2 $e) {", false);
    }

    public void testMultiCatch_FullyQualifiedName05() throws Exception {
        checkCompletion(getTestPath("fullyQualifiedName"), "} catch (\\Test\\Sub\\ExceptionType1 | \\Test\\Sub\\ExceptionType2 ^$e) {", false);
    }

    public void testMultiCatch_FullyQualifiedNameWithoutWS01() throws Exception {
        checkCompletion(getTestPath("fullyQualifiedNameWithoutWS"), "} catch(^\\Test\\Sub\\ExceptionType1|\\Test\\Sub\\ExceptionType2 $e) {", false);
    }

    public void testMultiCatch_FullyQualifiedNameWithoutWS02() throws Exception {
        checkCompletion(getTestPath("fullyQualifiedNameWithoutWS"), "} catch(\\Test\\Sub\\E^xceptionType1|\\Test\\Sub\\ExceptionType2 $e) {", false);
    }

    public void testMultiCatch_FullyQualifiedNameWithoutWS03() throws Exception {
        checkCompletion(getTestPath("fullyQualifiedNameWithoutWS"), "} catch(\\Test\\Sub\\ExceptionType1|^\\Test\\Sub\\ExceptionType2 $e) {", false);
    }

    public void testMultiCatch_FullyQualifiedNameWithoutWS04() throws Exception {
        checkCompletion(getTestPath("fullyQualifiedNameWithoutWS"), "} catch(\\Test\\Sub\\ExceptionType1|\\Test\\Sub\\Except^ionType2 $e) {", false);
    }

    public void testMultiCatch_FullyQualifiedNameWithoutWS05() throws Exception {
        checkCompletion(getTestPath("fullyQualifiedNameWithoutWS"), "} catch(\\Test\\Sub\\ExceptionType1|\\Test\\Sub\\ExceptionType2 ^$e) {", false);
    }

    public void testMultiCatch_FullyQualifiedNameDispatch01() throws Exception {
        checkCompletion(getTestPath("fullyQualifiedName"), "echo $e->^getTraceAsString(); // multi", false);
    }

    public void testMultiCatch_FullyQualifiedNameDispatch02() throws Exception {
        checkCompletion(getTestPath("fullyQualifiedName"), "echo $e->^getTraceAsString(); // single", false);
    }

    public void testMultiCatch_TypingType01a() throws Exception {
        checkCompletion(getTestPath("typingType01"), "} catch (ExceptionType2 |^ ", false);
    }

    public void testMultiCatch_TypingType01b() throws Exception {
        checkCompletion(getTestPath("typingType01"), "} catch (ExceptionType2 | ^", false);
    }

    public void testMultiCatch_TypingType02a() throws Exception {
        checkCompletion(getTestPath("typingType02"), "} catch (ExceptionType2 |^ )", false);
    }

    public void testMultiCatch_TypingType02b() throws Exception {
        checkCompletion(getTestPath("typingType02"), "} catch (ExceptionType2 | ^)", false);
    }

    public void testMultiCatch_TypingType03a() throws Exception {
        checkCompletion(getTestPath("typingType03"), "} catch (ExceptionType2 |^ ) {", false);
    }

    public void testMultiCatch_TypingType03b() throws Exception {
        checkCompletion(getTestPath("typingType03"), "} catch (ExceptionType2 | ^) {", false);
    }

    // Class Constant Visibility
    public void testClassConstantVisibility_InClass01() throws Exception {
        checkCompletion(getTestPath("classConstantVisibility"), "ParentClass::^IMPLICIT_PUBLIC_PARENT_CONST; // CC in class", false);
    }

    public void testClassConstantVisibility_InClass02() throws Exception {
        checkCompletion(getTestPath("classConstantVisibility"), "self::^IMPLICIT_PUBLIC_PARENT_CONST; // CC in class", false);
    }

    public void testClassConstantVisibility_InClass03() throws Exception {
        checkCompletion(getTestPath("classConstantVisibility"), "static::^PRIVATE_PARENT_CONST; // CC in class", false);
    }

    public void testClassConstantVisibility_InExtendingClass01() throws Exception {
        checkCompletion(getTestPath("classConstantVisibility"), "ChildClass::^IMPLICIT_PUBLIC_CHILD_CONST; // CC in ex", false);
    }

    public void testClassConstantVisibility_InExtendingClass02() throws Exception {
        checkCompletion(getTestPath("classConstantVisibility"), "self::^IMPLICIT_PUBLIC_CHILD_CONST; // CC in ex", false);
    }

    public void testClassConstantVisibility_InExtendingClass03() throws Exception {
        checkCompletion(getTestPath("classConstantVisibility"), "static::^IMPLICIT_PUBLIC_CHILD_CONST; // CC in ex", false);
    }

    public void testClassConstantVisibility_InExtendingClass04() throws Exception {
        checkCompletion(getTestPath("classConstantVisibility"), "TestInterfaceImpl::^IMPLICIT_INTERFACE_PUBLIC_CONST; // CC in impl", false);
    }

    public void testClassConstantVisibility_InExtendingClass05() throws Exception {
        checkCompletion(getTestPath("classConstantVisibility"), "self::^IMPLICIT_INTERFACE_PUBLIC_CONST; // CC in impl", false);
    }

    public void testClassConstantVisibility_InExtendingClass06() throws Exception {
        checkCompletion(getTestPath("classConstantVisibility"), "static::^IMPLICIT_INTERFACE_PUBLIC_CONST; // CC in impl", false);
    }

    public void testClassConstantVisibility_InExtendingClass07() throws Exception {
        checkCompletion(getTestPath("classConstantVisibility"), "ChildClass2::^IMPLICIT_PUBLIC_PARENT_CONST; // CC in ex and impl", false);
    }

    public void testClassConstantVisibility_InExtendingClass08() throws Exception {
        checkCompletion(getTestPath("classConstantVisibility"), "self::^IMPLICIT_PUBLIC_PARENT_CONST; // CC in ex and impl", false);
    }

    public void testClassConstantVisibility_InExtendingClass09() throws Exception {
        checkCompletion(getTestPath("classConstantVisibility"), "static::^IMPLICIT_PUBLIC_PARENT_CONST; // CC in ex and impl", false);
    }

    public void testClassConstantVisibility_InExtendingClass10() throws Exception {
        checkCompletion(getTestPath("classConstantVisibility"), "static::IM^PLICIT_PUBLIC_PARENT_CONST; // CC in ex and impl", false);
    }

    public void testClassConstantVisibility_InGlobal01() throws Exception {
        checkCompletion(getTestPath("classConstantVisibility"), "ParentClass::^IMPLICIT_PUBLIC_PARENT_CONST; // CC global", false);
    }

    public void testClassConstantVisibility_InGlobal02() throws Exception {
        checkCompletion(getTestPath("classConstantVisibility"), "ChildClass::^IMPLICIT_PUBLIC_CHILD_CONST; // CC global", false);
    }

    public void testClassConstantVisibility_InGlobal03() throws Exception {
        checkCompletion(getTestPath("classConstantVisibility"), "TestInterfaceImpl::^IMPLICIT_PUBLIC_INTERFACE_IMPL_CONST; // CC global", false);
    }

    public void testClassConstantVisibility_InGlobal04() throws Exception {
        checkCompletion(getTestPath("classConstantVisibility"), "ChildClass2::^PUBLIC_CHILD2_CONST; // CC global", false);
    }

    public void testVoidReturnType_Function01() throws Exception {
        checkCompletion(getTestPath("voidReturnType"), "function voidReturnType(): ^void { // func", false);
    }

    public void testVoidReturnType_Function02() throws Exception {
        checkCompletion(getTestPath("voidReturnType"), "function voidReturnType(): vo^id { // func", false);
    }

    public void testVoidReturnType_Function03() throws Exception {
        checkCompletion(getTestPath("voidReturnType"), "function invalidInParameter(v^oid $void): void { // func", false);
    }

    public void testVoidReturnType_Class01() throws Exception {
        checkCompletion(getTestPath("voidReturnType"), "public function returnType(): ^void { // class", false);
    }

    public void testVoidReturnType_Class02() throws Exception {
        checkCompletion(getTestPath("voidReturnType"), "public function returnType(): voi^d { // class", false);
    }

    public void testVoidReturnType_Class03() throws Exception {
        checkCompletion(getTestPath("voidReturnType"), "public function invalidInParameter(v^oid $void): void { // class", false);
    }

    public void testVoidReturnType_Interface01() throws Exception {
        checkCompletion(getTestPath("voidReturnType"), "public function returnType(): ^void; // interface", false);
    }

    public void testVoidReturnType_Interface02() throws Exception {
        checkCompletion(getTestPath("voidReturnType"), "public function returnType(): voi^d; // interface", false);
    }

    public void testVoidReturnType_Interface03() throws Exception {
        checkCompletion(getTestPath("voidReturnType"), "public function invalidInParameter(vo^id $void): void; // interface", false);
    }

    public void testIterableKeyword_ReturnType01() throws Exception {
        checkCompletion(getTestPath("iterable"), "function iteratorReturnType(): iter^able {", false);
    }

    public void testIterableKeyword_ParameterType01() throws Exception {
        checkCompletion(getTestPath("iterable"), "function iteratorParameterType(itera^ble $iterator) {", false);
    }

    public void testNullableTypesInPHPDoc_NullableType01() throws Exception {
        checkCompletion(getTestPath("nullableTypesInPHPDoc"), " * @method ?\\^", false);
    }

    public void testNullableTypesInPHPDoc_NullableType02() throws Exception {
        checkCompletion(getTestPath("nullableTypesInPHPDoc"), " * @property ?PHP^", false);
    }

    public void testNullableTypesInPHPDoc_NullableType03() throws Exception {
        checkCompletion(getTestPath("nullableTypesInPHPDoc"), "@param ?PHPDo^", false);
    }

    public void testNullableTypesInPHPDoc_NullableType04() throws Exception {
        checkCompletion(getTestPath("nullableTypesInPHPDoc"), "@return ?^", false);
    }

    public void testNullableTypesInPHPDoc_NullableType05() throws Exception {
        checkCompletion(getTestPath("nullableTypesInPHPDoc"), "@return ^?", false);
    }

    public void testNullableTypesInPHPDoc_NullableType06() throws Exception {
        checkCompletion(getTestPath("nullableTypesInPHPDoc"), " * @method PHPDocTags|?^", false);
    }

}
