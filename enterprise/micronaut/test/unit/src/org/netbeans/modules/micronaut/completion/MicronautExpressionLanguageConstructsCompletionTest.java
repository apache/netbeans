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
package org.netbeans.modules.micronaut.completion;

/**
 *
 * @author Dusan Balek
 */
public class MicronautExpressionLanguageConstructsCompletionTest extends MicronautExpressionLanguageCompletionTestBase {

    public MicronautExpressionLanguageConstructsCompletionTest(String name) {
        super(name);
    }

    public void testEmpty() throws Exception {
        performTest("", 0, "base.pass");
    }

    public void testSpace() throws Exception {
        performTest("  ", 1, "base.pass");
    }

    public void testEmptyBeforePrefix() throws Exception {
        performTest("e", 0, "base.pass");
    }

    public void testSpaceBeforePrefix() throws Exception {
        performTest(" e ", 1, "base.pass");
    }

    public void testEmptyWithPrefix() throws Exception {
        performTest("e", 1, "baseStartWithE.pass");
    }

    public void testSpaceWithPrefix() throws Exception {
        performTest(" e ", 2, "baseStartWithE.pass");
    }

    public void testEmptyTypeReference() throws Exception {
        performTest("T()", 2, "allTypes.pass");
    }

    public void testSpaceInTypeReference() throws Exception {
        performTest("T( )", 3, "allTypes.pass");
    }

    public void testTypeReferenceBeforePrefix() throws Exception {
        performTest("T(j)", 2, "allTypes.pass");
    }

    public void testSpaceInTypeReferenceBeforePrefix() throws Exception {
        performTest("T( j)", 3, "allTypes.pass");
    }

    public void testTypeReferenceWithPrefix() throws Exception {
        performTest("T(j)", 3, "allTypesStartWithJ.pass");
    }

    public void testSpaceInTypeReferenceWithPrefix() throws Exception {
        performTest("T( j)", 4, "allTypesStartWithJ.pass");
    }

    public void testTypeReferenceWithPackagePrefix() throws Exception {
        performTest("T(java.)", 7, "javaPackageContent.pass");
    }

    public void testTypeReferenceWithSubPackagePrefix() throws Exception {
        performTest("T(java.l)", 8, "javaPackageContentStartWithL.pass");
    }

    public void testTypeReferenceWithPackageClasses() throws Exception {
        performTest("T(java.lang.)", 12, "javaLangPackageContent.pass");
    }

    public void testTypeReferenceWithQualifiedClass1() throws Exception {
        performTest("T(java.lang.Integer)", 2, "allTypes.pass");
    }

    public void testTypeReferenceWithQualifiedClass2() throws Exception {
        performTest("T(java.lang.Integer)", 3, "allTypesStartWithJ.pass");
    }

    public void testTypeReferenceWithQualifiedClass3() throws Exception {
        performTest("T(java.lang.Integer)", 7, "javaPackageContent.pass");
    }

    public void testTypeReferenceWithQualifiedClass4() throws Exception {
        performTest("T(java.lang.Integer)", 8, "javaPackageContentStartWithL.pass");
    }

    public void testTypeReferenceWithQualifiedClass5() throws Exception {
        performTest("T(java.lang.Integer)", 12, "javaLangPackageContent.pass");
    }

    public void testTypeReferenceWithQualifiedClass6() throws Exception {
        performTest("T(java.lang.Integer)", 19, "intClass.pass");
    }

    public void testTypeReferenceWithSimpleClassPrefix() throws Exception {
        performTest("T(Integ)", 7, "intClass.pass");
    }

    public void testTypeReferenceWithSimpleClass() throws Exception {
        performTest("T(Integer)", 9, "intClass.pass");
    }

    public void testAfterTypeReference() throws Exception {
        performTest("T(Integer)", 10, "empty.pass");
    }

    public void testAfterTypeReferenceSpace() throws Exception {
        performTest("T(Integer) ", 11, "instanceOf.pass");
    }

    public void testTypeReferenceMethods() throws Exception {
        performTest("T(Integer).", 11, "intStaticMethods.pass");
    }

    public void testSpaceBeforeTypeReferenceMethods() throws Exception {
        performTest("T(Integer). ", 12, "intStaticMethods.pass");
    }

    public void testTypeReferenceMethodsWithPrefix() throws Exception {
        performTest("T(Integer).d", 12, "intStaticMethodsStartWithD.pass");
    }

    public void testTypeReferenceMethodsWithSpaceAndPrefix() throws Exception {
        performTest("T(Integer). d", 13, "intStaticMethodsStartWithD.pass");
    }

    public void testEmptyMethodArgs() throws Exception {
        performTest("T(Integer).decode()", 18, "base.pass");
    }

    public void testSpaceInMethodArgs() throws Exception {
        performTest("T(Integer).decode( )", 19, "base.pass");
    }

    public void testEmptyBeforePrefixInMethodArgs() throws Exception {
        performTest("T(Integer).decode(e)", 18, "base.pass");
    }

    public void testSpaceBeforePrefixInMethodArgs() throws Exception {
        performTest("T(Integer).decode( e) ", 19, "base.pass");
    }

    public void testEmptyWithPrefixInMethodArgs() throws Exception {
        performTest("T(Integer).decode(e)", 19, "baseStartWithE.pass");
    }

    public void testSpaceWithPrefixInMethodArgs() throws Exception {
        performTest("T(Integer).decode( e) ", 20, "baseStartWithE.pass");
    }

    public void testAfterMethodArg() throws Exception {
        performTest("T(Integer).decode('100') ", 23, "empty.pass");
    }

    public void testSpaceAfterMethodArg() throws Exception {
        performTest("T(Integer).decode('100' ) ", 24, "empty.pass");
    }

    public void testBeforeCommaInMethodArgs() throws Exception {
        performTest("T(Integer).decode('100',) ", 23, "empty.pass");
    }

    public void testAfterCommaInMethodArgs() throws Exception {
        performTest("T(Integer).decode('100',) ", 24, "base.pass");
    }

    public void testAfterCommaAndSpaceInMethodArgs() throws Exception {
        performTest("T(Integer).decode('100', ) ", 25, "base.pass");
    }

    public void testChainedMethods() throws Exception {
        performTest("T(Integer).decode('100').", 25, "intMethods.pass");
    }

    public void testChainedMethodsWithPrefix() throws Exception {
        performTest("T(Integer).decode('100').i", 26, "intMethodsStartWithI.pass");
    }

    public void testAfterChainedMethods() throws Exception {
        performTest("T(Integer).decode('100').intValue()", 35, "empty.pass");
    }

    public void testAfterChainedMethodsAndSpace() throws Exception {
        performTest("T(Integer).decode('100').intValue() ", 36, "numOperators.pass");
    }

    public void testTypeReferenceWithChainedMethods1() throws Exception {
        performTest("T(Integer).decode('100').intValue()", 1, "baseStartWithT.pass");
    }

    public void testTypeReferenceWithChainedMethods2() throws Exception {
        performTest("T(Integer).decode('100').intValue()", 2, "allTypes.pass");
    }

    public void testTypeReferenceWithChainedMethods3() throws Exception {
        performTest("T(Integer).decode('100').intValue()", 10, "empty.pass");
    }

    public void testTypeReferenceWithChainedMethods4() throws Exception {
        performTest("T(Integer).decode('100').intValue()", 11, "intStaticMethods.pass");
    }

    public void testTypeReferenceWithChainedMethods5() throws Exception {
        performTest("T(Integer).decode('100').intValue()", 18, "base.pass");
    }

    public void testTypeReferenceWithChainedMethods6() throws Exception {
        performTest("T(Integer).decode('100').intValue()", 23, "empty.pass");
    }

    public void testTypeReferenceWithChainedMethods7() throws Exception {
        performTest("T(Integer).decode('100').intValue()", 24, "empty.pass");
    }

    public void testTypeReferenceWithChainedMethods8() throws Exception {
        performTest("T(Integer).decode('100').intValue()", 25, "intMethods.pass");
    }

    public void testTypeReferenceWithChainedMethods9() throws Exception {
        performTest("T(Integer).decode('100').intValue()", 34, "base.pass");
    }

    public void testAfterHash() throws Exception {
        performTest("#", 1, "contextMethods.pass");
    }

    public void testAfterHashAndSpace() throws Exception {
        performTest("# ", 2, "contextMethods.pass");
    }

    public void testPropertyPrefix() throws Exception {
        performTest("#u", 2, "user.pass");
    }

    public void testPropertyPrefixWithSpace() throws Exception {
        performTest("# u", 3, "user.pass");
    }

    public void testPropertyChainedMethods() throws Exception {
        performTest("#user.", 6, "stringMethods.pass");
    }

    public void testPropertyChainedMethodPrefix() throws Exception {
        performTest("#user.l", 7, "stringMethodsStartWithL.pass");
    }

    public void testPropertyChainedMethods1() throws Exception {
        performTest("#user.length()", 1, "contextMethods.pass");
    }

    public void testPropertyChainedMethods2() throws Exception {
        performTest("#user.length()", 2, "user.pass");
    }

    public void testPropertyChainedMethods3() throws Exception {
        performTest("#user.length()", 6, "stringMethods.pass");
    }

    public void testChainedMethods1() throws Exception {
        performTest("#getUser().length()", 1, "contextMethods.pass");
    }

    public void testChainedMethods2() throws Exception {
        performTest("#getUser().length()", 2, "getUser.pass");
    }

    public void testChainedMethods3() throws Exception {
        performTest("#getUser().length()", 10, "empty.pass");
    }

    public void testChainedMethods4() throws Exception {
        performTest("#getUser().length()", 11, "stringMethods.pass");
    }

    public void testEmptyBeanCtx() throws Exception {
        performTest("ctx[]", 4, "ctxTypes.pass");
    }

    public void testSpaceInBeanCtx() throws Exception {
        performTest("ctx[ ]", 5, "ctxTypes.pass");
    }

    public void testBeforeTypeReferenceInBeanCtx() throws Exception {
        performTest("ctx[T()]", 4, "ctxTypes.pass");
    }

    public void testTypeReferenceInBeanCtx() throws Exception {
        performTest("ctx[T()]", 6, "allTypes.pass");
    }

    public void testSpaceInTypeReferenceInBeanCtx() throws Exception {
        performTest("ctx[T( )]", 7, "allTypes.pass");
    }

    public void testTypeReferenceInBeanCtxBeforePrefix() throws Exception {
        performTest("ctx[T(j)]", 6, "allTypes.pass");
    }

    public void testTypeReferenceInBeanCtxWithPrefix() throws Exception {
        performTest("ctx[T(j)]", 7, "allTypesStartWithJ.pass");
    }

    public void testAtertTypeReferenceInBeanCtx() throws Exception {
        performTest("ctx[T(java.lang.Integer)]", 24, "empty.pass");
    }

    public void testBeanCtxWithTypeReferenceMethods() throws Exception {
        performTest("ctx[T(java.lang.Integer)].", 26, "intMethods.pass");
    }

    public void testBeanCtxBeforePrefix() throws Exception {
        performTest("ctx[j]", 4, "ctxTypes.pass");
    }

    public void testSpaceInBeanCtxBeforePrefix() throws Exception {
        performTest("ctx[ j]", 5, "ctxTypes.pass");
    }

    public void testBeanCtxWithPrefix() throws Exception {
        performTest("ctx[j]", 5, "allTypesStartWithJ.pass");
    }

    public void testSpaceInBeanCtxWithPrefix() throws Exception {
        performTest("ctx[ J]", 6, "allTypesStartWithJ.pass");
    }

    public void testBeanCtxWithPackagePrefix() throws Exception {
        performTest("ctx[java.]", 9, "javaPackageContent.pass");
    }

    public void testBeanCtxWithSubPackagePrefix() throws Exception {
        performTest("ctx[java.l]", 10, "javaPackageContentStartWithL.pass");
    }

    public void testBeanCtxWithSimpleClassPrefix() throws Exception {
        performTest("ctx[Integ]", 9, "intClass.pass");
    }

    public void testBeanCtxWithSimpleClass() throws Exception {
        performTest("ctx[Integer]", 11, "intClass.pass");
    }

    public void testAfterBeanCtx() throws Exception {
        performTest("ctx[java.lang.Integer]", 22, "empty.pass");
    }

    public void testAfterBeanCtxSpace() throws Exception {
        performTest("ctx[java.lang.Integer] ", 23, "instanceOf.pass");
    }

    public void testBeanCtxMethods() throws Exception {
        performTest("ctx[java.lang.Integer].", 23, "intMethods.pass");
    }

    public void testBeanCtxWithQualifiedClass1() throws Exception {
        performTest("ctx[java.lang.Integer]", 4, "ctxTypes.pass");
    }

    public void testBeanCtxWithQualifiedClass2() throws Exception {
        performTest("ctx[java.lang.Integer]", 5, "allTypesStartWithJ.pass");
    }

    public void testBeanCtxWithQualifiedClass3() throws Exception {
        performTest("ctx[java.lang.Integer]", 9, "javaPackageContent.pass");
    }

    public void testBeanCtxWithQualifiedClass4() throws Exception {
        performTest("ctx[java.lang.Integer]", 10, "javaPackageContentStartWithL.pass");
    }

    public void testEmptyEnvProperties() throws Exception {
        performTest("env[]", 4, "allEnvProperties.pass");
    }

    public void testSpaceInEnvProperties() throws Exception {
        performTest("env[ ]", 5, "allEnvProperties.pass");
    }

    public void testEnvPropertiesBeforePrefix() throws Exception {
        performTest("env['micronaut']", 4, "allEnvProperties.pass");
    }

    public void testSpaceInEnvPropertiesBeforePrefix() throws Exception {
        performTest("env[ 'micronaut']", 5, "allEnvProperties.pass");
    }

    public void testEnvPropertiesWithPrefix() throws Exception {
        performTest("env['micronaut']", 14, "envPropertiesStartWithMicronaut.pass");
    }

    public void testEnvPropertiesWithDotPrefix() throws Exception {
        performTest("env['micronaut.']", 15, "envPropertiesStartWithMicronaut.pass");
    }

    public void testEnvPropertiesWithCompoundPrefix() throws Exception {
        performTest("env['micronaut.a']", 16, "envPropertiesStartWithMicronautA.pass");
    }

    public void testEnvProperties1() throws Exception {
        performTest("env['micronaut.applacation.name']", 1, "baseStartWithE.pass");
    }

    public void testEnvProperties2() throws Exception {
        performTest("env['micronaut.applacation.name']", 4, "allEnvProperties.pass");
    }

    public void testEnvProperties3() throws Exception {
        performTest("env['micronaut.applacation.name']", 16, "envPropertiesStartWithMicronautA.pass");
    }

    public void testEnvProperties4() throws Exception {
        performTest("env['micronaut.applacation.name']", 32, "empty.pass");
    }

    public void testEnvProperties5() throws Exception {
        performTest("env['micronaut.applacation.name']", 33, "empty.pass");
    }
}
