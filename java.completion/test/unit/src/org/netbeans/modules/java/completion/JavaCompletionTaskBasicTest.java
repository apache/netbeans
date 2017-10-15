/**
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

package org.netbeans.modules.java.completion;

/**
 *
 * @author Dusan Balek
 */
public class JavaCompletionTaskBasicTest extends CompletionTestBase {

    public JavaCompletionTaskBasicTest(String testName) {
        super(testName);
    }

    // file beginning tests ----------------------------------------------------

    public void testEmptyFile() throws Exception {
        performTest("Empty", 809, null, "topLevelKeywords.pass");
    }

    public void testFileBeginning() throws Exception {
        performTest("Simple", 809, null, "topLevelKeywords.pass");
    }
    
    // package declaration tests -----------------------------------------------
    
    public void testEmptyFileTypingPackageKeyword() throws Exception {
        performTest("Empty", 809, "p", "topLevelKeywordsStartingWithP.pass");
    }
    
    public void testTypingPackageKeyword() throws Exception {
        performTest("SimpleNoPackage", 809, "p", "topLevelKeywordsStartingWithP.pass");
    }
    
    public void testOnPackageKeyword() throws Exception {
        performTest("Simple", 811, null, "topLevelKeywordsStartingWithP.pass");
    }
    
    public void testEmptyFileAfterTypingPackageKeyword() throws Exception {
        performTest("Empty", 809, "package", "packageKeyword.pass");
    }
    
    public void testAfterTypingPackageKeyword() throws Exception {
        performTest("SimpleNoPackage", 809, "package", "packageKeyword.pass");
    }
    
    public void testAfterPackageKeyword() throws Exception {
        performTest("Simple", 817, null, "packageKeyword.pass");
    }
    
    public void testEmptyFileBeforeTypingPackageId() throws Exception {
        performTest("Empty", 809, "package ", "empty.pass");
    }
    
    public void testBeforeTypingPackageId() throws Exception {
        performTest("SimpleNoPackage", 809, "package ", "empty.pass");
    }
    
    public void testBeforePackageId() throws Exception {
        performTest("Simple", 818, null, "empty.pass");
    }
    
    public void testEmptyFileTypingPackageId() throws Exception {
        performTest("Empty", 809, "package t", "empty.pass");
    }
    
    public void testTypingPackageId() throws Exception {
        performTest("SimpleNoPackage", 809, "package t", "empty.pass");
    }
    
    public void testOnPackageId() throws Exception {
        performTest("Simple", 819, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingPackageId() throws Exception {
        performTest("Empty", 809, "package test", "empty.pass");
    }
    
    public void testAfterTypingPackageId() throws Exception {
        performTest("SimpleNoPackage", 809, "package test", "empty.pass");
    }
    
    public void testAfterPackageId() throws Exception {
        performTest("Simple", 822, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingPackageIdAndSpace() throws Exception {
        performTest("Empty", 809, "package test ", "empty.pass");
    }
    
    public void testAfterTypingPackageIdAndSpace() throws Exception {
        performTest("SimpleNoPackage", 809, "package test ", "empty.pass");
    }
    
    public void testAfterPackageIdAndSpace() throws Exception {
        performTest("Simple", 822, " ", "empty.pass");
    }
    
    public void testEmptyFileAfterTypingPackageDecl() throws Exception {
        performTest("Empty", 809, "package test;", "topLevelKeywordsWithoutPackage.pass");
    }
    
    public void testAfterTypingPackageDecl() throws Exception {
        performTest("SimpleNoPackage", 809, "package test;", "topLevelKeywordsWithoutPackage.pass");
    }
    
    public void testAfterPackageDecl() throws Exception {
        performTest("Simple", 823,  null, "topLevelKeywordsWithoutPackage.pass");
    }
    
    // import declaration tests ------------------------------------------------
    
    public void testEmptyFileAfterTypingImportKeyword() throws Exception {
        performTest("Empty", 809, "import", "importKeyword.pass");
    }
    
    public void testAfterTypingImportKeyword() throws Exception {
        performTest("Simple", 824, "import", "importKeyword.pass");
    }
    
    public void testAfterImportKeyword() throws Exception {
        performTest("Import", 831, null, "importKeyword.pass");
    }
    
    public void testEmptyFileBeforeTypingImportedPackage() throws Exception {
        performTest("Empty", 809, "import ", "staticKeywordAndAllPackages.pass");
    }
    
    public void testBeforeTypingImportedPackage() throws Exception {
        performTest("Simple", 824, "import ", "staticKeywordAndAllPackages.pass");
    }
    
    public void testBeforeImportedPackage() throws Exception {
        performTest("Import", 832, null, "staticKeywordAndAllPackages.pass");
    }
    
    public void testEmptyFileTypingImportedPackage() throws Exception {
        performTest("Empty", 809, "import j", "packagesStartingWithJ.pass");
    }
    
    public void testTypingImportedPackage() throws Exception {
        performTest("Simple", 824, "import j", "packagesStartingWithJ.pass");
    }
    
    public void testOnImportedPackage() throws Exception {
        performTest("Import", 833, null, "packagesStartingWithJ.pass");
    }
    
    public void testEmptyFileTypingImportedPackageBeforeStar() throws Exception {
        performTest("Empty", 809, "import java.util.", "javaUtilContent.pass");
    }
    
    public void testTypingImportedPackageBeforeStar() throws Exception {
        performTest("Simple", 824, "import java.util.", "javaUtilContent.pass");
    }
    
    public void testOnImportedPackageBeforeStar() throws Exception {
        performTest("Import", 864, null, "javaUtilContent.pass");
    }
    
    public void testEmptyFileAfterTypingImportedPackage() throws Exception {
        performTest("Empty", 809, "import java.util.*", "empty.pass");
    }
    
    public void testAfterTypingImportedPackage() throws Exception {
        performTest("Simple", 824, "import java.util.*", "empty.pass");
    }
    
    public void testAfterImportedPackage() throws Exception {
        performTest("Import", 865, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingImportedClass() throws Exception {
        performTest("Empty", 809, "import java.awt.List", "list.pass");
    }
    
    public void testAfterTypingImportedClass() throws Exception {
        performTest("Simple", 824, "import java.awt.List", "list.pass");
    }
    
    public void testAfterImportedClass() throws Exception {
        performTest("Import", 845, null, "list.pass");
    }
    
    public void testEmptyFileAfterTypingImportedClassAndSpace() throws Exception {
        performTest("Empty", 809, "import java.awt.List ", "empty.pass");
    }
    
    public void testAfterTypingImportedClassAndSpace() throws Exception {
        performTest("Simple", 824, "import java.awt.List ", "empty.pass");
    }
    
    public void testAfterImportedClassAndSpace() throws Exception {
        performTest("Import", 845, " ", "empty.pass");
    }
    
    public void testEmptyFileAfterTypingImportStatement() throws Exception {
        performTest("Empty", 809, "import java.awt.List;", "topLevelKeywordsWithoutPackage.pass");
    }
    
    public void testAfterTypingImportStatement() throws Exception {
        performTest("Simple", 824, "import java.awt.List;", "topLevelKeywordsWithoutPackage.pass");
    }
    
    public void testAfterImportStatement() throws Exception {
        performTest("Import", 846,  null, "topLevelKeywordsWithoutPackage.pass");
    }
    
    public void testEmptyFileTypingStaticImportKeyword() throws Exception {
        performTest("Empty", 809, "import st", "staticKeyword.pass");
    }
    
    public void testTypingStaticImportKeyword() throws Exception {
        performTest("Simple", 824, "import st", "staticKeyword.pass");
    }
    
    public void testOnStaticImportKeyword() throws Exception {
        performTest("Import", 876, null, "staticKeyword.pass");
    }
    
    public void testEmptyFileAfterTypingStaticImportKeyword() throws Exception {
        performTest("Empty", 809, "import static", "staticKeyword.pass");
    }
    
    public void testAfterTypingStaticImportKeyword() throws Exception {
        performTest("Simple", 824, "import static", "staticKeyword.pass");
    }
    
    public void testAfterStaticImportKeyword() throws Exception {
        performTest("Import", 880, null, "staticKeyword.pass");
    }
    
    public void testEmptyFileBeforeTypingStaticallyImportedClass() throws Exception {
        performTest("Empty", 809, "import static ", "allPackages.pass");
    }
    
    public void testBeforeTypingStaticallyImportedClass() throws Exception {
        performTest("Simple", 824, "import static ", "allPackages.pass");
    }
    
    public void testBeforeStaticallyImportedClass() throws Exception {
        performTest("Import", 881, null, "allPackages.pass");
    }
    
    public void testEmptyFileTypingImportedPackageAfterErrorInPackageDeclaration() throws Exception {
        performTest("Empty", 809, "package \nimport j", "packagesStartingWithJ.pass");
    }
    
    public void testTypingStaticImportAfterErrorInPackageDeclaration() throws Exception {
        performTest("SimpleNoPackage", 809, "package \nimport ", "staticKeywordAndAllPackages.pass");
    }

    public void testTypingStaticImportAfterErrorInPreviousImportDeclaration() throws Exception {
        performTest("Simple", 824, "im\nimport ", "staticKeywordAndAllPackages.pass");
    }
    
    // class declaration tests -------------------------------------------------
    
    public void testEmptyFileAfterTypingPublicKeyword() throws Exception {
        performTest("Empty", 809, "package test;\npublic", "publicKeyword.pass");
    }
    
    public void testAfterPublicKeyword() throws Exception {
        performTest("Simple", 831, null, "publicKeyword.pass");
    }
    
    public void testTypingFinalClass() throws Exception {
        performTest("Simple", 831, " f", "finalKeyword.pass");
    }
    
    public void testAfterTypingFinalClass() throws Exception {
        performTest("Simple", 831, " final", "finalKeyword.pass");
    }
    
    public void testEmptyFileBeforeTypingClassKeyword() throws Exception {
        performTest("Empty", 809, "package test;\npublic ", "classModifiersWithoutPublic.pass");
    }
    
    public void testBeforeClassKeyword() throws Exception {
        performTest("Simple", 832, null, "classModifiersWithoutPublic.pass");
    }
    
    public void testEmptyFileTypingClassKeyword() throws Exception {
        performTest("Empty", 809, "package test;\npublic c", "classKeyword.pass");
    }
    
    public void testOnClassKeyword() throws Exception {
        performTest("Simple", 833, null, "classKeyword.pass");
    }
    
    public void testEmptyFileAfterTypingClassKeyword() throws Exception {
        performTest("Empty", 809, "package test;\npublic class", "classKeyword.pass");
    }
    
    public void testAfterClassKeyword() throws Exception {
        performTest("Simple", 837, null, "classKeyword.pass");
    }
    
    public void testEmptyFileBeforeTypingClassName() throws Exception {
        performTest("Empty", 809, "package test;\npublic class ", "empty.pass");
    }
    
    public void testBeforeClassName() throws Exception {
        performTest("Simple", 838, null, "empty.pass");
    }
    
    public void testEmptyFileTypingClassName() throws Exception {
        performTest("Empty", 809, "package test;\npublic class T", "empty.pass");
    }
    
    public void testOnClassName() throws Exception {
        performTest("Simple", 839, null, "empty.pass");
    }
    
    public void testEmptyAfterFileTypingClassName() throws Exception {
        performTest("Empty", 809, "package test;\npublic class Test", "empty.pass");
    }
    
    public void testAfterClassName() throws Exception {
        performTest("Simple", 842, null, "empty.pass");
    }
    
    public void testEmptyFileBeforeTypingExtendsKeyword() throws Exception {
        performTest("Empty", 809, "package test;\npublic class Test ", "extendsAndImplementsKeywords.pass");
    }
    
    public void testBeforeTypingExtendsKeyword() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 843, null, "extendsAndImplementsKeywords.pass");
    }
    
    public void testBeforeExtendsKeyword() throws Exception {
        performTest("Simple", 843, null, "extendsAndImplementsKeywords.pass");
    }
    
    public void testEmptyFileTypingExtendsKeyword() throws Exception {
        performTest("Empty", 809, "package test;\npublic class Test e", "extendsKeyword.pass");
    }
    
    public void testTypingExtendsKeyword() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 843, "e", "extendsKeyword.pass");
    }
    
    public void testOnExtendsKeyword() throws Exception {
        performTest("Simple", 844, null, "extendsKeyword.pass");
    }
    
    public void testEmptyFileAfterTypingExtendsKeyword() throws Exception {
        performTest("Empty", 809, "package test;\npublic class Test extends", "extendsKeyword.pass");
    }
    
    public void testAfterTypingExtendsKeyword() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 843, "extends", "extendsKeyword.pass");
    }
    
    public void testAfterExtendsKeyword() throws Exception {
        performTest("Simple", 850, null, "extendsKeyword.pass");
    }
    
    public void testEmptyFileBeforeTypingExtendedObject() throws Exception {
        performTest("Empty", 809, "package test;\npublic class Test extends ", "javaLangClasses.pass");
    }
    
    public void testBeforeTypingExtendedObject() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 843, "extends ", "javaLangClasses.pass");
    }
    
    public void testBeforeExtendedObject() throws Exception {
        performTest("Simple", 851, null, "javaLangClasses.pass");
    }
    
    public void testEmptyFileTypingExtendedObject() throws Exception {
        performTest("Empty", 809, "package test;\npublic class Test extends O", "javaLangClassesStartingWithO.pass");
    }
    
    public void testTypingExtendedObject() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 843, "extends O", "javaLangClassesStartingWithO.pass");
    }
    
    public void testOnExtendedObject() throws Exception {
        performTest("Simple", 852, null, "javaLangClassesStartingWithO.pass");
    }
    
    public void testEmptyFileAfterTypingExtendedObject() throws Exception {
        performTest("Empty", 809, "package test;\npublic class Test extends Object", "object.pass");
    }
    
    public void testAfterTypingExtendedObject() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 843, "extends Object", "object.pass");
    }
    
    public void testAfterExtendedObject() throws Exception {
        performTest("Simple", 857, null, "object.pass");
    }
    
    public void testEmptyFileBeforeTypingImplementsKeyword() throws Exception {
        performTest("Empty", 809, "package test;\npublic class Test extends Object ", "implementsKeyword.pass");
    }
    
    public void testBeforeTypingImplementsKeyword() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 843, "extends Object ", "implementsKeyword.pass");
    }
    
    public void testBeforeImplementsKeyword() throws Exception {
        performTest("Simple", 858, null, "implementsKeyword.pass");
    }
    
    public void testEmptyFileTypingImplementsKeyword() throws Exception {
        performTest("Empty", 809, "package test;\npublic class Test extends Object i", "implementsKeyword.pass");
    }
    
    public void testTypingImplementsKeyword() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 843, "i", "implementsKeyword.pass");
    }
    
    public void testOnImplementsKeyword() throws Exception {
        performTest("Simple", 859, null, "implementsKeyword.pass");
    }
    
    public void testEmptyFileAfteTypingImplementsKeyword() throws Exception {
        performTest("Empty", 809, "package test;\npublic class Test extends Object implements", "implementsKeyword.pass");
    }
    
    public void testAfterTypingImplementsKeyword() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 843, "implements", "implementsKeyword.pass");
    }
    
    public void testAfterImplementsKeyword() throws Exception {
        performTest("Simple", 868, null, "implementsKeyword.pass");
    }
    
    public void testEmptyFileBeforeTypingImplementedInterface() throws Exception {
        performTest("Empty", 809, "package test;\npublic class Test extends Object implements ", "javaLangInterfaces.pass");
    }
    
    public void testBeforeTypingImplementedInterface() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 843, "implements ", "javaLangInterfaces.pass");
    }
    
    public void testBeforeImplementedInterface() throws Exception {
        performTest("Simple", 869, null, "javaLangInterfaces.pass");
    }
    
    public void testEmptyFileAfterTypingImplementedInterface() throws Exception {
        performTest("Empty", 809, "package test;\npublic class Test extends Object implements Cloneable", "cloneable.pass");
    }
    
    public void testAfterTypingImplementedInterface() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 843, "implements Cloneable", "cloneable.pass");
    }
    
    public void testAfterImplementedInterface() throws Exception {
        performTest("Simple", 878, null, "cloneable.pass");
    }
    
    public void testEmptyFileAfterTypingImplementedInterfaceAndSpace() throws Exception {
        performTest("Empty", 809, "package test;\npublic class Test extends Object implements Cloneable ", "empty.pass");
    }
    
    public void testAfterTypingImplementedInterfaceAndSpace() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 843, "implements Cloneable ", "empty.pass");
    }
    
    public void testAfterImplementedInterfaceAndSpace() throws Exception {
        performTest("Simple", 878, " ", "empty.pass");
    }
    
    public void testEmptyFileAfterTypingFirstImplementedInterface() throws Exception {
        performTest("Empty", 809, "package test;\npublic class Test extends Object implements Cloneable, ", "javaLangInterfaces.pass");
    }
    
    public void testAfterTypingFirstImplementedInterface() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 843, "implements Cloneable, ", "javaLangInterfaces.pass");
    }
    
    public void testAfterFirstImplementedInterface() throws Exception {
        performTest("Simple", 880, null, "javaLangInterfaces.pass");
    }
    
    public void testEmptyFileTypingSecondImplementedInterface() throws Exception {
        performTest("Empty", 809, "package test;\npublic class Test extends Object implements Cloneable, R", "javaLangInterfacesStartingWithR.pass");
    }
    
    public void testTypingSecondImplementedInterface() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 843, "implements Cloneable, R", "javaLangInterfacesStartingWithR.pass");
    }
    
    public void testOnSecondImplementedInterface() throws Exception {
        performTest("Simple", 881, null, "javaLangInterfacesStartingWithR.pass");
    }
    
    public void testEmptyFileTypingClassBody() throws Exception {
        performTest("Empty", 809, "package test;\npublic class Test {", "memberModifiersTypesAndGenElements.pass");
    }
    
    public void testInClassBody() throws Exception {
        performTest("Simple", 890, null, "memberModifiersTypesAndGenElements2.pass");
    }

    public void testEmptyFileAfterTypingClassBody() throws Exception {
        performTest("Empty", 809, "package test;\npublic class Test {\n}", "classModifiersWithoutPublic.pass");
    }
    
    public void testAfterClassBody() throws Exception {
        performTest("Simple", 892, null, "classModifiersWithoutPublic.pass");
    }
    
    public void testEmptyFileAfterTypingIncompleteClassBodyAndSecondClassKeyword() throws Exception {
        performTest("Empty", 809, "package test;\npublic class Test {\nclass", "classKeyword.pass");
    }
    
    // interface declaration tests ---------------------------------------------
    
    public void testEmptyFileAfterTypingIntefaceName() throws Exception {
        performTest("Empty", 809, "package test;\ninterface Test ", "extendsKeyword.pass");
    }
    
    public void testAfterTypingIntefaceName() throws Exception {
        performTest("SimpleInterfaceNoExtends", 840, null, "extendsKeyword.pass");
    }
    
    public void testAfterIntefaceName() throws Exception {
        performTest("SimpleInterface", 840, null, "extendsKeyword.pass");
    }
    
    public void testEmptyFileAfterTypingExtendsInInteface() throws Exception {
        performTest("Empty", 809, "package test;\ninterface Test extends ", "javaLangInterfaces.pass");
    }
    
    public void testAfterTypingExtendsInInterface() throws Exception {
        performTest("SimpleInterfaceNoExtends", 840, "extends ", "javaLangInterfaces.pass");
    }
    
    public void testAfterExtendsInInteface() throws Exception {
        performTest("SimpleInterface", 848, null, "javaLangInterfaces.pass");
    }
    
    // enum declaration tests --------------------------------------------------
    
    public void testEmptyFileAfterTypingEnumName() throws Exception {
        performTest("Empty", 809, "package test;\npublic enum Test ", "implementsKeyword.pass");
    }
    
    public void testAfterTypingEnumName() throws Exception {
        performTest("SimpleEnumNoImplements", 842, null, "implementsKeyword.pass");
    }
    
    public void testAfterEnumName() throws Exception {
        performTest("SimpleEnum", 842, null, "implementsKeyword.pass");
    }
    
    public void testEmptyFileAfterTypingImplementsInEnum() throws Exception {
        performTest("Empty", 809, "package test;\npublic enum Test implements ", "javaLangInterfaces.pass");
    }
    
    public void testAfterTypingImplementsInEnum() throws Exception {
        performTest("SimpleEnumNoImplements", 853, null, "javaLangInterfaces.pass");
    }
    
    public void testAfterImplementsInEnum() throws Exception {
        performTest("SimpleEnum", 853, null, "javaLangInterfaces.pass");
    }
    
    // method declaration tests -------------------------------------------------
    
    public void testEmptyFileTypingPublicKeywordInMethodDecl() throws Exception {
        performTest("MethodStart", 850, "p", "memberModifiersStartingWithP.pass");
    }
    
    public void testOnPublicKeywordInMethodDecl() throws Exception {
        performTest("Method", 851, null, "memberModifiersStartingWithP.pass");
    }
    
    public void testEmptyFileAfterTypingPublicKeywordInMethodDecl() throws Exception {
        performTest("MethodStart", 850, "public", "publicKeyword.pass");
    }
    
    public void testAfterPublicKeywordInMethodDecl() throws Exception {
        performTest("Method", 856, null, "publicKeyword.pass");
    }
    
    public void testTypingStaticMethodDecl() throws Exception {
        performTest("Method", 856, " sta", "staticKeyword.pass");
    }
    
    public void testAfterTypingStaticMethodDecl() throws Exception {
        performTest("Method", 856, " static", "staticKeyword.pass");
    }
    
    public void testEmptyFileBeforeTypingReturnValue() throws Exception {
        performTest("MethodStart", 850, "public ", "memberModifiersAndTypesWithoutPublic.pass");
    }
    
    public void testBeforeReturnValue() throws Exception {
        performTest("Method", 857, null, "memberModifiersAndTypesWithoutPublic.pass");
    }
    
    public void testEmptyFileTypingReturnValue() throws Exception {
        performTest("MethodStart", 850, "public voi", "voidKeyword.pass");
    }
    
    public void testOnReturnValue() throws Exception {
        performTest("Method", 860, null, "voidKeyword.pass");
    }
    
    public void testEmptyFileAfterTypingReturnValue() throws Exception {
        performTest("MethodStart", 850, "public void", "voidKeyword.pass");
    }
    
    public void testAfterReturnValue() throws Exception {
        performTest("Method", 861, null, "voidKeyword.pass");
    }
    
    public void testEmptyFileBeforeTypingMethodName() throws Exception {
        performTest("MethodStart", 850, "public void ", "empty.pass");
    }
    
    public void testBeforeMethodName() throws Exception {
        performTest("Method", 862, null, "empty.pass");
    }
    
    public void testEmptyFileTypingMethodName() throws Exception {
        performTest("MethodStart", 850, "public void o", "empty.pass");
    }
    
    public void testOnMethodName() throws Exception {
        performTest("Method", 863, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingMethodName() throws Exception {
        performTest("MethodStart", 850, "public void op", "empty.pass");
    }
    
    public void testAfterMethodName() throws Exception {
        performTest("Method", 864, null, "empty.pass");
    }
    
    public void testEmptyFileBeforeTypingFirstParameter() throws Exception {
        performTest("MethodStart", 850, "public void op(", "parameterTypes.pass");
    }
    
    public void testBeforeTypingFirstParameter() throws Exception {
        performTest("MethodNoParamsAndThrows", 865, null, "parameterTypes.pass");
    }

    public void testBeforeFirstParameter() throws Exception {
        performTest("Method", 865, null, "parameterTypes.pass");
    }
    
    public void testEmptyFileTypingFirstParameterType() throws Exception {
        performTest("MethodStart", 850, "public void op(i", "intKeyword.pass");
    }
    
    public void testTypingFirstParameterType() throws Exception {
        performTest("MethodNoParamsAndThrows", 865, "i", "intKeyword.pass");
    }

    public void testOnFirstParameterType() throws Exception {
        performTest("Method", 866, null, "intKeyword.pass");
    }
    
    public void testEmptyFileAfterTypingFirstParameterType() throws Exception {
        performTest("MethodStart", 850, "public void op(int", "intKeyword.pass");
    }
    
    public void testAfterTypingFirstParameterType() throws Exception {
        performTest("MethodNoParamsAndThrows", 865, "int", "intKeyword.pass");
    }

    public void testAfterFirstParameterType() throws Exception {
        performTest("Method", 868, null, "intKeyword.pass");
    }
    
    public void testEmptyFileBeforeTypingFirstParameterName() throws Exception {
        performTest("MethodStart", 850, "public void op(int ", "intVarName.pass");
    }
    
    public void testBeforeTypingFirstParameterName() throws Exception {
        performTest("MethodNoParamsAndThrows", 865, "int ", "intVarName.pass");
    }

    public void testBeforeFirstParameterName() throws Exception {
        performTest("Method", 869, null, "intVarName.pass");
    }
    
    public void testEmptyFileTypingFirstParameterName() throws Exception {
        performTest("MethodStart", 850, "public void op(int a", "empty.pass");
    }
    
    public void testTypingFirstParameterName() throws Exception {
        performTest("MethodNoParamsAndThrows", 865, "int a", "empty.pass");
    }

    public void testOnFirstParameterName() throws Exception {
        performTest("Method", 870, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingFirstParameterNameAndSpace() throws Exception {
        performTest("MethodStart", 850, "public void op(int a ", "empty.pass");
    }
    
    public void testAfterTypingFirstParameterNameAndSpace() throws Exception {
        performTest("MethodNoParamsAndThrows", 865, "int a ", "empty.pass");
    }

    public void testAfterFirstParameterNameAndSpace() throws Exception {
        performTest("Method", 870, " ", "empty.pass");
    }
    
    public void testEmptyFileBeforeTypingSecondParameter() throws Exception {
        performTest("MethodStart", 850, "public void op(int a,", "parameterTypes.pass");
    }
    
    public void testBeforeTypingSecondParameter() throws Exception {
        performTest("MethodNoParamsAndThrows", 865, "int a,", "parameterTypes.pass");
    }

    public void testBeforeSecondParameter() throws Exception {
        performTest("Method", 871, null, "parameterTypes.pass");
    }
    
    public void testEmptyFileTypingSecondParameterType() throws Exception {
        performTest("MethodStart", 850, "public void op(int a, bo", "booleanKeyword.pass");
    }
    
    public void testTypingSecondParameterType() throws Exception {
        performTest("MethodNoParamsAndThrows", 865, "int a, bo", "booleanKeyword.pass");
    }

    public void testOnSecondParameterType() throws Exception {
        performTest("Method", 874, null, "booleanKeyword.pass");
    }
    
    public void testEmptyFileAfterTypingSecondParameterType() throws Exception {
        performTest("MethodStart", 850, "public void op(int a, boolean", "booleanKeyword.pass");
    }
    
    public void testAfterTypingSecondParameterType() throws Exception {
        performTest("MethodNoParamsAndThrows", 865, "int a, boolean", "booleanKeyword.pass");
    }

    public void testAfterSecondParameterType() throws Exception {
        performTest("Method", 879, null, "booleanKeyword.pass");
    }
    
    public void testEmptyFileBeforeTypingSecondParameterName() throws Exception {
        performTest("MethodStart", 850, "public void op(int a, boolean ", "booleanVarName.pass");
    }
    
    public void testBeforeTypingSecondParameterName() throws Exception {
        performTest("MethodNoParamsAndThrows", 865, "int a, boolean ", "booleanVarName.pass");
    }

    public void testBeforeSecondParameterName() throws Exception {
        performTest("Method", 880, null, "booleanVarName.pass");
    }
    
    public void testEmptyFileTypingSecondParameterName() throws Exception {
        performTest("MethodStart", 850, "public void op(int a, boolean b", "booleanVarName.pass");
    }
    
    public void testTypingSecondParameterName() throws Exception {
        performTest("MethodNoParamsAndThrows", 865, "int a, boolean b", "booleanVarName.pass");
    }

    public void testOnSecondParameterName() throws Exception {
        performTest("Method", 881, null, "booleanVarName.pass");
    }

    public void testEmptyFileBeforeTypingThrowsKeyword() throws Exception {
        performTest("MethodStart", 850, "public void op() ", "throwsKeyword.pass");
    }
    
    public void testBeforeTypingThrowsKeyword() throws Exception {
        performTest("MethodNoParamsAndThrows", 866, " ", "throwsKeyword.pass");
    }

    public void testBeforeThrowsKeyword() throws Exception {
        performTest("Method", 883, null, "throwsKeyword.pass");
    }
    
    public void testEmptyFileTypingThrowsKeyword() throws Exception {
        performTest("MethodStart", 850, "public void op() t", "throwsKeyword.pass");
    }
    
    public void testTypingThrowsKeyword() throws Exception {
        performTest("MethodNoParamsAndThrows", 866, " t", "throwsKeyword.pass");
    }

    public void testOnThrowsKeyword() throws Exception {
        performTest("Method", 884, null, "throwsKeyword.pass");
    }
    
    public void testEmptyAfterFileTypingThrowsKeyword() throws Exception {
        performTest("MethodStart", 850, "public void op() throws", "throwsKeyword.pass");
    }
    
    public void testAfterTypingThrowsKeyword() throws Exception {
        performTest("MethodNoParamsAndThrows", 866, " throws", "throwsKeyword.pass");
    }

    public void testAfterThrowsKeyword() throws Exception {
        performTest("Method", 889, null, "throwsKeyword.pass");
    }
        
    public void testEmptyFileBeforeTypingThrownException() throws Exception {
        performTest("MethodStart", 850, "public void op() throws ", "javaLangThrowables.pass");
    }
    
    public void testBeforeTypingThrownException() throws Exception {
        performTest("MethodNoParamsAndThrows", 866, "throws ", "javaLangThrowables.pass");
    }
    
    public void testBeforeThrownException() throws Exception {
        performTest("Method", 890, null, "javaLangThrowables.pass");
    }
    
    public void testEmptyFileTypingThrownException() throws Exception {
        performTest("MethodStart", 850, "public void op() throws N", "javaLangThrowablesStartingWithN.pass");
    }
    
    public void testTypingThrownException() throws Exception {
        performTest("MethodNoParamsAndThrows", 866, "throws N", "javaLangThrowablesStartingWithN.pass");
    }
    
    public void testOnThrownException() throws Exception {
        performTest("Method", 891, null, "javaLangThrowablesStartingWithN.pass");
    }
    
    public void testEmptyFileAfterTypingThrownException() throws Exception {
        performTest("MethodStart", 850, "public void op() throws NullPointerException", "nullPointerException.pass");
    }
    
    public void testAfterTypingThrownException() throws Exception {
        performTest("MethodNoParamsAndThrows", 866, "throws NullPointerException", "nullPointerException.pass");
    }
    
    public void testAfterThrownException() throws Exception {
        performTest("Method", 910, null, "nullPointerException.pass");
    }
    
    public void testEmptyFileAfterTypingThrownExceptionAndSpace() throws Exception {
        performTest("MethodStart", 850, "public void op() throws NullPointerException ", "empty.pass");
    }
    
    public void testAfterTypingThrownExceptionAndSpace() throws Exception {
        performTest("MethodNoParamsAndThrows", 866, "throws NullPointerException ", "empty.pass");
    }
    
    public void testAfterThrownExceptionAndSpace() throws Exception {
        performTest("Method", 910, " ", "empty.pass");
    }
    
    public void testEmptyFileBeforeTypingSecondThrownException() throws Exception {
        performTest("MethodStart", 850, "public void op() throws NullPointerException, ", "javaLangThrowables.pass");
    }
    
    public void testBeforeTypingSecondThrownException() throws Exception {
        performTest("MethodNoParamsAndThrows", 866, "throws NullPointerException, ", "javaLangThrowables.pass");
    }
    
    public void testBeforeSecondThrownExceptionAndSpace() throws Exception {
        performTest("Method", 912, null, "javaLangThrowables.pass");
    }
    
    public void testEmptyFileTypingSecondThrownException() throws Exception {
        performTest("MethodStart", 850, "public void op() throws NullPointerException, I", "javaLangThrowablesStartingWithI.pass");
    }
    
    public void testTypingSecondThrownException() throws Exception {
        performTest("MethodNoParamsAndThrows", 866, "throws NullPointerException, I", "javaLangThrowablesStartingWithI.pass");
    }
    
    public void testOnSecondThrownException() throws Exception {
        performTest("Method", 913, null, "javaLangThrowablesStartingWithI.pass");
    }
    
    public void testEmptyFileAfterTypingMethodBody() throws Exception {
        performTest("MethodStart", 850, "public void op() {\n}", "memberModifiersTypesAndGenElements.pass");
    }
    
    public void testAfterMethodBody() throws Exception {
        performTest("Method", 941, null, "memberModifiersTypesAndGenElements.pass");
    }
    
    public void testEmptyFileAfterTypingIncompleteMethodBodyAndPublicKeyword() throws Exception {
        performTest("MethodStart", 850, "public void op() {\npublic", "empty.pass");
    }    

    public void testEmptyFileAfterTypingIncompleteMethodBodyAndPublicKeywordAndSpace() throws Exception {
        performTest("MethodStart", 850, "public void op() {\npublic ", "memberModifiersAndTypesWithoutPublic.pass");
    }    

    // field declaration tests -------------------------------------------------
    
    public void testEmptyFileAfterTypingFieldNameAndSpace() throws Exception {
        performTest("MethodStart", 850, "public int field ", "empty.pass");
    }
    
    public void testAfterTypingFieldNameAndSpace() throws Exception {
        performTest("FieldNoInit", 866, " ", "empty.pass");
    }
    
    public void testAfterFieldNameAndSpace() throws Exception {
        performTest("Field", 867, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingAssignmentInField() throws Exception {
        performTest("MethodStart", 850, "public static int staticField = 10;\npublic int field =", "typesLocalMembersAndSmartInt.pass");
    }
    
    public void testAfterTypingAssignmentInField() throws Exception {
        performTest("FieldNoInit", 866, " =", "typesLocalMembersAndSmartInt.pass");
    }
    
    public void testAfterAssignmentInField() throws Exception {
        performTest("Field", 868, null, "typesLocalMembersAndSmartInt.pass");
    }
    
    public void testEmptyFileBeforeTypingInitOfField() throws Exception {
        performTest("MethodStart", 850, "public static int staticField = 10;\npublic int field = ", "typesLocalMembersAndSmartInt.pass");
    }
    
    public void testBeforeTypingInitOfField() throws Exception {
        performTest("FieldNoInit", 866, " = ", "typesLocalMembersAndSmartInt.pass");
    }
    
    public void testBeforeInitOfField() throws Exception {
        performTest("Field", 869, null, "typesLocalMembersAndSmartInt.pass");
    }
    
    public void testEmptyFileBeforeTypingInitOfStaticField() throws Exception {
        performTest("MethodStart", 850, "public static int staticField = ", "typesAndStaticLocalMembers.pass");
    }
    
    public void testBeforeTypingInitOfStaticField() throws Exception {
        performTest("FieldNoInit", 901, " = ", "typesAndStaticLocalMembers.pass");
    }
    
    public void testBeforeInitOfStaticField() throws Exception {
        performTest("Field", 922, null, "typesAndStaticLocalMembers.pass");
    }
    
    public void testEmptyFileTypingInitOfField() throws Exception {
        performTest("MethodStart", 850, "public int field = ha", "intHashCode.pass");
    }
    
    public void testTypingInitOfField() throws Exception {
        performTest("FieldNoInit", 866, " = ha", "intHashCode.pass");
    }
    
    public void testOnInitOfField() throws Exception {
        performTest("Field", 871, null, "intHashCode.pass");
    }
    
    public void testEmptyFileTypingMethodInvocationWithinInitOfField() throws Exception {
        performTest("MethodStart", 850, "public static int staticField = 10;\npublic int field = hashCode(", "typesAndLocalMembers.pass");
    }
    
    public void testTypingMethodInvocationWithinInitOfField() throws Exception {
        performTest("FieldNoInit", 866, " = hashCode(", "typesAndLocalMembers.pass");
    }
    
    public void testOnMethodInvocationWithinInitOfField() throws Exception {
        performTest("Field", 878, null, "typesAndLocalMembers.pass");
    }
    
    public void testEmptyFileAfterTypingMethodInvocationWithinInitOfField() throws Exception {
        performTest("MethodStart", 850, "public int field = hashCode()", "empty.pass");
    }
    
    public void testAfterTypingMethodInvocationWithinInitOfField() throws Exception {
        performTest("FieldNoInit", 866, " = hashCode()", "empty.pass");
    }
    
    public void testAfterMethodInvocationWithinInitOfField() throws Exception {
        performTest("Field", 879, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingOperatorWithinInitOfField() throws Exception {
        performTest("MethodStart", 850, "public static int staticField = 10;\npublic int field = hashCode() /", "typesLocalMembersAndSmartPrimitives.pass");
    }
    
    public void testAfterTypingOperatorWithinInitOfField() throws Exception {
        performTest("FieldNoInit", 866, " = hashCode() /", "typesLocalMembersAndSmartPrimitives.pass");
    }
    
    public void testAfterOperatorWithinInitOfField() throws Exception {
        performTest("Field", 881, null, "typesLocalMembersAndSmartPrimitives.pass");
    }
    
    public void testEmptyFileAfterTypingConstantWithinInitOfField() throws Exception {
        performTest("MethodStart", 850, "public int field = hashCode() / 10", "empty.pass");
    }
    
    public void testAfterTypingConstantWithinInitOfField() throws Exception {
        performTest("FieldNoInit", 866, " = hashCode() / 10", "empty.pass");
    }
    
    public void testAfterConstantWithinInitOfField() throws Exception {
        performTest("Field", 884, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingFieldDeclaration() throws Exception {
        performTest("MethodStart", 850, "public int field = hashCode() / 10;", "memberModifiersTypesAndGenElements3.pass");
    }
    
    public void testAfterFieldDeclaration() throws Exception {
        performTest("Field", 885, null, "memberModifiersTypesAndGenElements4.pass");
    }
    
    public void testTypingStaticFieldAfterErrorInPreviousFieldDeclaration() throws Exception {
        performTest("MethodStart", 850, "public int \npublic sta", "staticKeyword.pass");
    }

    public void testTypingStaticFieldAfterErrorInPreviousFieldInitialization() throws Exception {
        performTest("MethodStart", 850, "public int field = has\npublic sta", "staticKeyword.pass");
    }
    
    // basic method body tests -------------------------------------------------
   
    public void testEmptyFileTypingMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 899, null, "methodBodyContent.pass");
    }
    
    public void testTypingMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 899, null, "methodBodyContent.pass");
    }
    
    public void testInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 899, null, "methodBodyContent.pass");
    }
    
    public void testEmptyFileTypingLocalVariableTypeInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 908, "bo", "booleanKeyword.pass");
    }
    
    public void testTypingLocalVariableTypeInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 908, "bo", "booleanKeyword.pass");
    }
    
    public void testLocalVariableTypeInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 910, null, "booleanKeyword.pass");
    }
    
    public void testEmptyFileAfterTypingLocalVariableTypeInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 908, "boolean", "booleanKeyword.pass");
    }
    
    public void testAfterTypingLocalVariableTypeInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 908, "boolean", "booleanKeyword.pass");
    }
    
    public void testAfterLocalVariableTypeInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 915, null, "booleanKeyword.pass");
    }
    
    public void testEmptyFileBeforeTypingLocalVariableNameInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 908, "boolean ", "booleanVarName.pass");
    }
    
    public void testBeforeTypingLocalVariableNameInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 908, "boolean ", "booleanVarName.pass");
    }
    
    public void testBeforeLocalVariableNameInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 916, null, "booleanVarName.pass");
    }
    
    public void testEmptyFileTypingLocalVariableNameInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 908, "boolean b", "booleanVarName.pass");
    }
    
    public void testTypingLocalVariableNameInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 908, "boolean b", "booleanVarName.pass");
    }
    
    public void testOnLocalVariableNameInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 917, null, "booleanVarName.pass");
    }
    
    public void testEmptyFileAfterTypingLocalVariableNameAndSpaceInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 908, "boolean b ", "empty.pass");
    }
    
    public void testAfterTypingLocalVariableNameAndSpaceInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 908, "boolean b ", "empty.pass");
    }
    
    public void testAfterLocalVariableNameAndSpaceInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 917, " ", "empty.pass");
    }
    
    public void testEmptyFileAfterTypingLocalVariableInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 908, "boolean b;", "methodBodyContentAndLocalVar.pass");
    }
    
    public void testAfterTypingLocalVariableInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 908, "boolean b;", "methodBodyContentAndLocalVar.pass");
    }
    
    public void testAfterLocalVariableInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 918, null, "methodBodyContentAndLocalVar.pass");
    }
 
    public void testEmptyFileTypingTypeInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 908, "Sy", "system.pass");
    }
    
    public void testTypingTypeInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 908, "Sy", "system.pass");
    }
    
    public void testTypeInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 922, null, "system.pass");
    }
    
    public void testEmptyFileAfterTypingTypeInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 908, "System", "system.pass");
    }
    
    public void testAfterTypingTypeInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 908, "System", "system.pass");
    }
    
    public void testAfterTypeInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 926, null, "system.pass");
    }
    
    public void testEmptyFileAfterTypingTypeAndDotInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 908, "System.", "systemContent.pass");
    }
    
    public void testAfterTypingTypeAndDotInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 908, "System.", "systemContent.pass");
    }
    
    public void testAfterTypeAndDotInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 927, null, "systemContent.pass");
    }

    public void testEmptyFileTypingFieldAccessInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 908, "System.o", "systemOut.pass");
    }
    
    public void testTypingFieldAccessInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 908, "System.o", "systemOut.pass");
    }
    
    public void testFieldAccessInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 928, null, "systemOut.pass");
    }
   
    public void testEmptyFileAfterTypingFieldAccessInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 908, "System.out", "systemOut.pass");
    }
    
    public void testAfterTypingFieldAccessInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 908, "System.out", "systemOut.pass");
    }
    
    public void testAfterFieldAccessInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 930, null, "systemOut.pass");
    }
    
    public void testEmptyFileAfterTypingFieldAccessAndSpaceInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 908, "System.out ", "instanceOf.pass");
    }
    
    public void testAfterTypingFieldAccessAndSpaceInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 908, "System.out ", "instanceOf.pass");
    }
    
    public void testAfterFieldAccessAndSpaceInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 930, " ", "instanceOf.pass");
    }
    
    public void testEmptyFileAfterTypingFieldAccessAndDotInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 908, "System.out.", "systemOutContent.pass");
    }
    
    public void testAfterTypingFieldAccessAndDotInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 908, "System.out.", "systemOutContent.pass");
    }
    
    public void testAfterFieldAccessAndDotInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 931, null, "systemOutContent.pass");
    }

    public void testEmptyFileTypingMethodInvocationInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 908, "boolean b;\nSystem.out.println(", "typesLocalMembersVarsAndSmarts.pass");
    }
    
    public void testTypingMethodInvocationInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 908, "boolean b;\nSystem.out.println(", "typesLocalMembersVarsAndSmarts.pass");
    }
    
    public void testMethodInvocationInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 939, null, "typesLocalMembersVarsAndSmarts.pass");
    }
    
    public void testEmptyFileTypingMethodInvocationParameterInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 908, "boolean b;\nSystem.out.println(b", "typesLocalMembersVarsAndSmartsStartingWithB.pass");
    }
    
    public void testTypingMethodInvocationParameterInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 908, "boolean b;\nSystem.out.println(b", "typesLocalMembersVarsAndSmartsStartingWithB.pass");
    }
    
    public void testOnMethodInvocationParameterInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 940, null, "typesLocalMembersVarsAndSmartsStartingWithB.pass");
    }
    
    public void testEmptyFileTypingMethodInvocationParameterAndSpaceInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 908, "System.out.println(b ", "empty.pass");
    }
    
    public void testTypingMethodInvocationParameterAndSpaceInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 908, "System.out.println(b ", "empty.pass");
    }
    
    public void testAfterMethodInvocationParameterAndSpaceInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 940, " ", "empty.pass");
    }
    
    public void testEmptyFileBeforeTypingSecondMethodInvocationParameterInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 908, "boolean b;\nSystem.out.println(b, ", "typesLocalMembersAndVars.pass");
    }
    
    public void testBeforeTypingSecondMethodInvocationParameterInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 908, "boolean b;\nSystem.out.println(b, ", "typesLocalMembersAndVars.pass");
    }
    
    public void testBeforeSecondMethodInvocationParameterInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 940, ", ", "typesLocalMembersAndVars.pass");
    }
    
    public void testEmptyFileTypingSecondMethodInvocationParameterInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 908, "boolean b;\nSystem.out.println(b, b", "typesLocalMembersAndVarsStartingWithB.pass");
    }
    
    public void testTypingSecondMethodInvocationParameterInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 908, "boolean b;\nSystem.out.println(b, b", "typesLocalMembersAndVarsStartingWithB.pass");
    }
    
    public void testOnSecondMethodInvocationParameterInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 940, ", b", "typesLocalMembersAndVarsStartingWithB.pass");
    }
    
    public void testEmptyFileAfterTypingMethodInvocationParametersInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 908, "boolean b;\nSystem.out.println(b)", "empty.pass");
    }
    
    public void testAfterTypingMethodInvocationParametersInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 908, "boolean b;\nSystem.out.println(b)", "empty.pass");
    }
    
    public void testAfteMethodInvocationParametersInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 941, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingMethodInvocationParametersAndSpaceInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 908, "boolean b;\nSystem.out.println(b) ", "empty.pass");
    }
    
    public void testAfterTypingMethodInvocationParametersAndSpaceInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 908, "boolean b;\nSystem.out.println(b) ", "empty.pass");
    }
    
    public void testAfteMethodInvocationParametersAndSpaceInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 941, " ", "empty.pass");
    }
    
    public void testEmptyFileAfterTypingMethodInvocationInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 908, "boolean b;\nSystem.out.println(b);", "methodBodyContentAndLocalVar.pass");
    }
    
    public void testAfterTypingMethodInvocationInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 908, "boolean b;\nSystem.out.println(b);", "methodBodyContentAndLocalVar.pass");
    }
    
    public void testAfteMethodInvocationInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 942, null, "methodBodyContentAndLocalVar.pass");
    }
}
