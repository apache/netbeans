/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        performTest("Empty", 0, null, "topLevelKeywords.pass");
    }

    public void testFileBeginning() throws Exception {
        performTest("Simple", 0, null, "topLevelKeywords.pass");
    }
    
    // package declaration tests -----------------------------------------------
    
    public void testEmptyFileTypingPackageKeyword() throws Exception {
        performTest("Empty", 0, "p", "topLevelKeywordsStartingWithP.pass");
    }
    
    public void testTypingPackageKeyword() throws Exception {
        performTest("SimpleNoPackage", 0, "p", "topLevelKeywordsStartingWithP.pass");
    }
    
    public void testOnPackageKeyword() throws Exception {
        performTest("Simple", 1, null, "topLevelKeywordsStartingWithP.pass");
    }
    
    public void testEmptyFileAfterTypingPackageKeyword() throws Exception {
        performTest("Empty", 0, "package", "packageKeyword.pass");
    }
    
    public void testAfterTypingPackageKeyword() throws Exception {
        performTest("SimpleNoPackage", 0, "package", "packageKeyword.pass");
    }
    
    public void testAfterPackageKeyword() throws Exception {
        performTest("Simple", 7, null, "packageKeyword.pass");
    }
    
    public void testEmptyFileBeforeTypingPackageId() throws Exception {
        performTest("Empty", 0, "package ", "empty.pass");
    }
    
    public void testBeforeTypingPackageId() throws Exception {
        performTest("SimpleNoPackage", 0, "package ", "empty.pass");
    }
    
    public void testBeforePackageId() throws Exception {
        performTest("Simple", 8, null, "empty.pass");
    }
    
    public void testEmptyFileTypingPackageId() throws Exception {
        performTest("Empty", 0, "package t", "empty.pass");
    }
    
    public void testTypingPackageId() throws Exception {
        performTest("SimpleNoPackage", 0, "package t", "empty.pass");
    }
    
    public void testOnPackageId() throws Exception {
        performTest("Simple", 9, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingPackageId() throws Exception {
        performTest("Empty", 0, "package test", "empty.pass");
    }
    
    public void testAfterTypingPackageId() throws Exception {
        performTest("SimpleNoPackage", 0, "package test", "empty.pass");
    }
    
    public void testAfterPackageId() throws Exception {
        performTest("Simple", 12, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingPackageIdAndSpace() throws Exception {
        performTest("Empty", 0, "package test ", "empty.pass");
    }
    
    public void testAfterTypingPackageIdAndSpace() throws Exception {
        performTest("SimpleNoPackage", 0, "package test ", "empty.pass");
    }
    
    public void testAfterPackageIdAndSpace() throws Exception {
        performTest("Simple", 12, " ", "empty.pass");
    }
    
    public void testEmptyFileAfterTypingPackageDecl() throws Exception {
        performTest("Empty", 0, "package test;", "topLevelKeywordsWithoutPackage.pass");
    }
    
    public void testAfterTypingPackageDecl() throws Exception {
        performTest("SimpleNoPackage", 0, "package test;", "topLevelKeywordsWithoutPackage.pass");
    }
    
    public void testAfterPackageDecl() throws Exception {
        performTest("Simple", 13,  null, "topLevelKeywordsWithoutPackage.pass");
    }
    
    // import declaration tests ------------------------------------------------
    
    public void testEmptyFileAfterTypingImportKeyword() throws Exception {
        performTest("Empty", 0, "import", "importKeyword.pass");
    }
    
    public void testAfterTypingImportKeyword() throws Exception {
        performTest("Simple", 14, "import", "importKeyword.pass");
    }
    
    public void testAfterImportKeyword() throws Exception {
        performTest("Import", 21, null, "importKeyword.pass");
    }
    
    public void testEmptyFileBeforeTypingImportedPackage() throws Exception {
        performTest("Empty", 0, "import ", "staticKeywordAndAllPackages.pass");
    }
    
    public void testBeforeTypingImportedPackage() throws Exception {
        performTest("Simple", 14, "import ", "staticKeywordAndAllPackages.pass");
    }
    
    public void testBeforeImportedPackage() throws Exception {
        performTest("Import", 22, null, "staticKeywordAndAllPackages.pass");
    }
    
    public void testEmptyFileTypingImportedPackage() throws Exception {
        performTest("Empty", 0, "import j", "packagesStartingWithJ.pass");
    }
    
    public void testTypingImportedPackage() throws Exception {
        performTest("Simple", 14, "import j", "packagesStartingWithJ.pass");
    }
    
    public void testOnImportedPackage() throws Exception {
        performTest("Import", 23, null, "packagesStartingWithJ.pass");
    }
    
    public void testEmptyFileTypingImportedPackageBeforeStar() throws Exception {
        performTest("Empty", 0, "import java.util.", "javaUtilContent.pass");
    }
    
    public void testTypingImportedPackageBeforeStar() throws Exception {
        performTest("Simple", 14, "import java.util.", "javaUtilContent.pass");
    }
    
    public void testOnImportedPackageBeforeStar() throws Exception {
        performTest("Import", 54, null, "javaUtilContent.pass");
    }
    
    public void testEmptyFileAfterTypingImportedPackage() throws Exception {
        performTest("Empty", 0, "import java.util.*", "empty.pass");
    }
    
    public void testAfterTypingImportedPackage() throws Exception {
        performTest("Simple", 14, "import java.util.*", "empty.pass");
    }
    
    public void testAfterImportedPackage() throws Exception {
        performTest("Import", 55, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingImportedClass() throws Exception {
        performTest("Empty", 0, "import java.awt.List", "list.pass");
    }
    
    public void testAfterTypingImportedClass() throws Exception {
        performTest("Simple", 14, "import java.awt.List", "list.pass");
    }
    
    public void testAfterImportedClass() throws Exception {
        performTest("Import", 35, null, "list.pass");
    }
    
    public void testEmptyFileAfterTypingImportedClassAndSpace() throws Exception {
        performTest("Empty", 0, "import java.awt.List ", "empty.pass");
    }
    
    public void testAfterTypingImportedClassAndSpace() throws Exception {
        performTest("Simple", 14, "import java.awt.List ", "empty.pass");
    }
    
    public void testAfterImportedClassAndSpace() throws Exception {
        performTest("Import", 35, " ", "empty.pass");
    }
    
    public void testEmptyFileAfterTypingImportStatement() throws Exception {
        performTest("Empty", 0, "import java.awt.List;", "topLevelKeywordsWithoutPackage.pass");
    }
    
    public void testAfterTypingImportStatement() throws Exception {
        performTest("Simple", 14, "import java.awt.List;", "topLevelKeywordsWithoutPackage.pass");
    }
    
    public void testAfterImportStatement() throws Exception {
        performTest("Import", 36,  null, "topLevelKeywordsWithoutPackage.pass");
    }
    
    public void testEmptyFileTypingStaticImportKeyword() throws Exception {
        performTest("Empty", 0, "import st", "staticKeyword.pass");
    }
    
    public void testTypingStaticImportKeyword() throws Exception {
        performTest("Simple", 14, "import st", "staticKeyword.pass");
    }
    
    public void testOnStaticImportKeyword() throws Exception {
        performTest("Import", 66, null, "staticKeyword.pass");
    }
    
    public void testEmptyFileAfterTypingStaticImportKeyword() throws Exception {
        performTest("Empty", 0, "import static", "staticKeyword.pass");
    }
    
    public void testAfterTypingStaticImportKeyword() throws Exception {
        performTest("Simple", 14, "import static", "staticKeyword.pass");
    }
    
    public void testAfterStaticImportKeyword() throws Exception {
        performTest("Import", 70, null, "staticKeyword.pass");
    }
    
    public void testEmptyFileBeforeTypingStaticallyImportedClass() throws Exception {
        performTest("Empty", 0, "import static ", "allPackages.pass");
    }
    
    public void testBeforeTypingStaticallyImportedClass() throws Exception {
        performTest("Simple", 14, "import static ", "allPackages.pass");
    }
    
    public void testBeforeStaticallyImportedClass() throws Exception {
        performTest("Import", 71, null, "allPackages.pass");
    }
    
    public void testEmptyFileTypingImportedPackageAfterErrorInPackageDeclaration() throws Exception {
        performTest("Empty", 0, "package \nimport j", "packagesStartingWithJ.pass");
    }
    
    public void testTypingStaticImportAfterErrorInPackageDeclaration() throws Exception {
        performTest("SimpleNoPackage", 0, "package \nimport ", "staticKeywordAndAllPackages.pass");
    }

    public void testTypingStaticImportAfterErrorInPreviousImportDeclaration() throws Exception {
        performTest("Simple", 14, "im\nimport ", "staticKeywordAndAllPackages.pass");
    }
    
    // class declaration tests -------------------------------------------------
    
    public void testEmptyFileAfterTypingPublicKeyword() throws Exception {
        performTest("Empty", 0, "package test;\npublic", "publicKeyword.pass");
    }
    
    public void testAfterPublicKeyword() throws Exception {
        performTest("Simple", 21, null, "publicKeyword.pass");
    }
    
    public void testTypingFinalClass() throws Exception {
        performTest("Simple", 21, " f", "finalKeyword.pass");
    }
    
    public void testAfterTypingFinalClass() throws Exception {
        performTest("Simple", 21, " final", "finalKeyword.pass");
    }
    
    public void testEmptyFileBeforeTypingClassKeyword() throws Exception {
        performTest("Empty", 0, "package test;\npublic ", "classModifiersWithoutPublic.pass");
    }
    
    public void testBeforeClassKeyword() throws Exception {
        performTest("Simple", 22, null, "classModifiersWithoutPublic.pass");
    }
    
    public void testEmptyFileTypingClassKeyword() throws Exception {
        performTest("Empty", 0, "package test;\npublic c", "classKeyword.pass");
    }
    
    public void testOnClassKeyword() throws Exception {
        performTest("Simple", 23, null, "classKeyword.pass");
    }
    
    public void testEmptyFileAfterTypingClassKeyword() throws Exception {
        performTest("Empty", 0, "package test;\npublic class", "classKeyword.pass");
    }
    
    public void testAfterClassKeyword() throws Exception {
        performTest("Simple", 27, null, "classKeyword.pass");
    }
    
    public void testEmptyFileBeforeTypingClassName() throws Exception {
        performTest("Empty", 0, "package test;\npublic class ", "empty.pass");
    }
    
    public void testBeforeClassName() throws Exception {
        performTest("Simple", 28, null, "empty.pass");
    }
    
    public void testEmptyFileTypingClassName() throws Exception {
        performTest("Empty", 0, "package test;\npublic class T", "empty.pass");
    }
    
    public void testOnClassName() throws Exception {
        performTest("Simple", 29, null, "empty.pass");
    }
    
    public void testEmptyAfterFileTypingClassName() throws Exception {
        performTest("Empty", 0, "package test;\npublic class Test", "empty.pass");
    }
    
    public void testAfterClassName() throws Exception {
        performTest("Simple", 32, null, "empty.pass");
    }
    
    public void testEmptyFileBeforeTypingExtendsKeyword() throws Exception {
        performTest("Empty", 0, "package test;\npublic class Test ", "extendsAndImplementsKeywords.pass");
    }
    
    public void testBeforeTypingExtendsKeyword() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 33, null, "extendsAndImplementsKeywords.pass");
    }
    
    public void testBeforeExtendsKeyword() throws Exception {
        performTest("Simple", 33, null, "extendsAndImplementsKeywords.pass");
    }
    
    public void testEmptyFileTypingExtendsKeyword() throws Exception {
        performTest("Empty", 0, "package test;\npublic class Test e", "extendsKeyword.pass");
    }
    
    public void testTypingExtendsKeyword() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 33, "e", "extendsKeyword.pass");
    }
    
    public void testOnExtendsKeyword() throws Exception {
        performTest("Simple", 34, null, "extendsKeyword.pass");
    }
    
    public void testEmptyFileAfterTypingExtendsKeyword() throws Exception {
        performTest("Empty", 0, "package test;\npublic class Test extends", "extendsKeyword.pass");
    }
    
    public void testAfterTypingExtendsKeyword() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 33, "extends", "extendsKeyword.pass");
    }
    
    public void testAfterExtendsKeyword() throws Exception {
        performTest("Simple", 40, null, "extendsKeyword.pass");
    }
    
    public void testEmptyFileBeforeTypingExtendedObject() throws Exception {
        performTest("Empty", 0, "package test;\npublic class Test extends ", "javaLangClasses.pass");
    }
    
    public void testBeforeTypingExtendedObject() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 33, "extends ", "javaLangClasses.pass");
    }
    
    public void testBeforeExtendedObject() throws Exception {
        performTest("Simple", 41, null, "javaLangClasses.pass");
    }
    
    public void testEmptyFileTypingExtendedObject() throws Exception {
        performTest("Empty", 0, "package test;\npublic class Test extends O", "javaLangClassesStartingWithO.pass");
    }
    
    public void testTypingExtendedObject() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 33, "extends O", "javaLangClassesStartingWithO.pass");
    }
    
    public void testOnExtendedObject() throws Exception {
        performTest("Simple", 42, null, "javaLangClassesStartingWithO.pass");
    }
    
    public void testEmptyFileAfterTypingExtendedObject() throws Exception {
        performTest("Empty", 0, "package test;\npublic class Test extends Object", "object.pass");
    }
    
    public void testAfterTypingExtendedObject() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 33, "extends Object", "object.pass");
    }
    
    public void testAfterExtendedObject() throws Exception {
        performTest("Simple", 47, null, "object.pass");
    }
    
    public void testEmptyFileBeforeTypingImplementsKeyword() throws Exception {
        performTest("Empty", 0, "package test;\npublic class Test extends Object ", "implementsKeyword.pass");
    }
    
    public void testBeforeTypingImplementsKeyword() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 33, "extends Object ", "implementsKeyword.pass");
    }
    
    public void testBeforeImplementsKeyword() throws Exception {
        performTest("Simple", 48, null, "implementsKeyword.pass");
    }
    
    public void testEmptyFileTypingImplementsKeyword() throws Exception {
        performTest("Empty", 0, "package test;\npublic class Test extends Object i", "implementsKeyword.pass");
    }
    
    public void testTypingImplementsKeyword() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 33, "i", "implementsKeyword.pass");
    }
    
    public void testOnImplementsKeyword() throws Exception {
        performTest("Simple", 49, null, "implementsKeyword.pass");
    }
    
    public void testEmptyFileAfteTypingImplementsKeyword() throws Exception {
        performTest("Empty", 0, "package test;\npublic class Test extends Object implements", "implementsKeyword.pass");
    }
    
    public void testAfterTypingImplementsKeyword() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 33, "implements", "implementsKeyword.pass");
    }
    
    public void testAfterImplementsKeyword() throws Exception {
        performTest("Simple", 58, null, "implementsKeyword.pass");
    }
    
    public void testEmptyFileBeforeTypingImplementedInterface() throws Exception {
        performTest("Empty", 0, "package test;\npublic class Test extends Object implements ", "javaLangInterfaces.pass");
    }
    
    public void testBeforeTypingImplementedInterface() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 33, "implements ", "javaLangInterfaces.pass");
    }
    
    public void testBeforeImplementedInterface() throws Exception {
        performTest("Simple", 59, null, "javaLangInterfaces.pass");
    }
    
    public void testEmptyFileAfterTypingImplementedInterface() throws Exception {
        performTest("Empty", 0, "package test;\npublic class Test extends Object implements Cloneable", "cloneable.pass");
    }
    
    public void testAfterTypingImplementedInterface() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 33, "implements Cloneable", "cloneable.pass");
    }
    
    public void testAfterImplementedInterface() throws Exception {
        performTest("Simple", 68, null, "cloneable.pass");
    }
    
    public void testEmptyFileAfterTypingImplementedInterfaceAndSpace() throws Exception {
        performTest("Empty", 0, "package test;\npublic class Test extends Object implements Cloneable ", "empty.pass");
    }
    
    public void testAfterTypingImplementedInterfaceAndSpace() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 33, "implements Cloneable ", "empty.pass");
    }
    
    public void testAfterImplementedInterfaceAndSpace() throws Exception {
        performTest("Simple", 68, " ", "empty.pass");
    }
    
    public void testEmptyFileAfterTypingFirstImplementedInterface() throws Exception {
        performTest("Empty", 0, "package test;\npublic class Test extends Object implements Cloneable, ", "javaLangInterfaces.pass");
    }
    
    public void testAfterTypingFirstImplementedInterface() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 33, "implements Cloneable, ", "javaLangInterfaces.pass");
    }
    
    public void testAfterFirstImplementedInterface() throws Exception {
        performTest("Simple", 70, null, "javaLangInterfaces.pass");
    }
    
    public void testEmptyFileTypingSecondImplementedInterface() throws Exception {
        performTest("Empty", 0, "package test;\npublic class Test extends Object implements Cloneable, R", "javaLangInterfacesStartingWithR.pass");
    }
    
    public void testTypingSecondImplementedInterface() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 33, "implements Cloneable, R", "javaLangInterfacesStartingWithR.pass");
    }
    
    public void testOnSecondImplementedInterface() throws Exception {
        performTest("Simple", 71, null, "javaLangInterfacesStartingWithR.pass");
    }
    
    public void testEmptyFileTypingClassBody() throws Exception {
        performTest("Empty", 0, "package test;\npublic class Test {", "memberModifiersTypesAndGenElements.pass");
    }
    
    public void testInClassBody() throws Exception {
        performTest("Simple", 80, null, "memberModifiersTypesAndGenElements2.pass");
    }

    public void testEmptyFileAfterTypingClassBody() throws Exception {
        performTest("Empty", 0, "package test;\npublic class Test {\n}", "classModifiersWithoutPublic.pass");
    }
    
    public void testAfterClassBody() throws Exception {
        performTest("Simple", 82, null, "classModifiersWithoutPublic.pass");
    }
    
    public void testEmptyFileAfterTypingIncompleteClassBodyAndSecondClassKeyword() throws Exception {
        performTest("Empty", 0, "package test;\npublic class Test {\nclass", "classKeyword.pass");
    }
    
    // interface declaration tests ---------------------------------------------
    
    public void testEmptyFileAfterTypingIntefaceName() throws Exception {
        performTest("Empty", 0, "package test;\ninterface Test ", "extendsKeyword.pass");
    }
    
    public void testAfterTypingIntefaceName() throws Exception {
        performTest("SimpleInterfaceNoExtends", 30, null, "extendsKeyword.pass");
    }
    
    public void testAfterIntefaceName() throws Exception {
        performTest("SimpleInterface", 30, null, "extendsKeyword.pass");
    }
    
    public void testEmptyFileAfterTypingExtendsInInteface() throws Exception {
        performTest("Empty", 0, "package test;\ninterface Test extends ", "javaLangInterfaces.pass");
    }
    
    public void testAfterTypingExtendsInInterface() throws Exception {
        performTest("SimpleInterfaceNoExtends", 30, "extends ", "javaLangInterfaces.pass");
    }
    
    public void testAfterExtendsInInteface() throws Exception {
        performTest("SimpleInterface", 38, null, "javaLangInterfaces.pass");
    }
    
    // enum declaration tests --------------------------------------------------
    
    public void testEmptyFileAfterTypingEnumName() throws Exception {
        performTest("Empty", 0, "package test;\npublic enum Test ", "implementsKeyword.pass");
    }
    
    public void testAfterTypingEnumName() throws Exception {
        performTest("SimpleEnumNoImplements", 32, null, "implementsKeyword.pass");
    }
    
    public void testAfterEnumName() throws Exception {
        performTest("SimpleEnum", 32, null, "implementsKeyword.pass");
    }
    
    public void testEmptyFileAfterTypingImplementsInEnum() throws Exception {
        performTest("Empty", 0, "package test;\npublic enum Test implements ", "javaLangInterfaces.pass");
    }
    
    public void testAfterTypingImplementsInEnum() throws Exception {
        performTest("SimpleEnumNoImplements", 43, null, "javaLangInterfaces.pass");
    }
    
    public void testAfterImplementsInEnum() throws Exception {
        performTest("SimpleEnum", 43, null, "javaLangInterfaces.pass");
    }
    
    // method declaration tests -------------------------------------------------
    
    public void testEmptyFileTypingPublicKeywordInMethodDecl() throws Exception {
        performTest("MethodStart", 40, "p", "memberModifiersStartingWithP.pass");
    }
    
    public void testOnPublicKeywordInMethodDecl() throws Exception {
        performTest("Method", 41, null, "memberModifiersStartingWithP.pass");
    }
    
    public void testEmptyFileAfterTypingPublicKeywordInMethodDecl() throws Exception {
        performTest("MethodStart", 40, "public", "publicKeyword.pass");
    }
    
    public void testAfterPublicKeywordInMethodDecl() throws Exception {
        performTest("Method", 46, null, "publicKeyword.pass");
    }
    
    public void testTypingStaticMethodDecl() throws Exception {
        performTest("Method", 46, " sta", "staticKeyword.pass");
    }
    
    public void testAfterTypingStaticMethodDecl() throws Exception {
        performTest("Method", 46, " static", "staticKeyword.pass");
    }
    
    public void testEmptyFileBeforeTypingReturnValue() throws Exception {
        performTest("MethodStart", 40, "public ", "memberModifiersAndTypesWithoutPublic.pass");
    }
    
    public void testBeforeReturnValue() throws Exception {
        performTest("Method", 47, null, "memberModifiersAndTypesWithoutPublic.pass");
    }
    
    public void testEmptyFileTypingReturnValue() throws Exception {
        performTest("MethodStart", 40, "public voi", "voidKeyword.pass");
    }
    
    public void testOnReturnValue() throws Exception {
        performTest("Method", 50, null, "voidKeyword.pass");
    }
    
    public void testEmptyFileAfterTypingReturnValue() throws Exception {
        performTest("MethodStart", 40, "public void", "voidKeyword.pass");
    }
    
    public void testAfterReturnValue() throws Exception {
        performTest("Method", 51, null, "voidKeyword.pass");
    }
    
    public void testEmptyFileBeforeTypingMethodName() throws Exception {
        performTest("MethodStart", 40, "public void ", "empty.pass");
    }
    
    public void testBeforeMethodName() throws Exception {
        performTest("Method", 52, null, "empty.pass");
    }
    
    public void testEmptyFileTypingMethodName() throws Exception {
        performTest("MethodStart", 40, "public void o", "empty.pass");
    }
    
    public void testOnMethodName() throws Exception {
        performTest("Method", 53, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingMethodName() throws Exception {
        performTest("MethodStart", 40, "public void op", "empty.pass");
    }
    
    public void testAfterMethodName() throws Exception {
        performTest("Method", 54, null, "empty.pass");
    }
    
    public void testEmptyFileBeforeTypingFirstParameter() throws Exception {
        performTest("MethodStart", 40, "public void op(", "parameterTypes.pass");
    }
    
    public void testBeforeTypingFirstParameter() throws Exception {
        performTest("MethodNoParamsAndThrows", 55, null, "parameterTypes.pass");
    }

    public void testBeforeFirstParameter() throws Exception {
        performTest("Method", 55, null, "parameterTypes.pass");
    }
    
    public void testEmptyFileTypingFirstParameterType() throws Exception {
        performTest("MethodStart", 40, "public void op(i", "intKeyword.pass");
    }
    
    public void testTypingFirstParameterType() throws Exception {
        performTest("MethodNoParamsAndThrows", 55, "i", "intKeyword.pass");
    }

    public void testOnFirstParameterType() throws Exception {
        performTest("Method", 56, null, "intKeyword.pass");
    }
    
    public void testEmptyFileAfterTypingFirstParameterType() throws Exception {
        performTest("MethodStart", 40, "public void op(int", "intKeyword.pass");
    }
    
    public void testAfterTypingFirstParameterType() throws Exception {
        performTest("MethodNoParamsAndThrows", 55, "int", "intKeyword.pass");
    }

    public void testAfterFirstParameterType() throws Exception {
        performTest("Method", 58, null, "intKeyword.pass");
    }
    
    public void testEmptyFileBeforeTypingFirstParameterName() throws Exception {
        performTest("MethodStart", 40, "public void op(int ", "intVarName.pass");
    }
    
    public void testBeforeTypingFirstParameterName() throws Exception {
        performTest("MethodNoParamsAndThrows", 55, "int ", "intVarName.pass");
    }

    public void testBeforeFirstParameterName() throws Exception {
        performTest("Method", 59, null, "intVarName.pass");
    }
    
    public void testEmptyFileTypingFirstParameterName() throws Exception {
        performTest("MethodStart", 40, "public void op(int a", "empty.pass");
    }
    
    public void testTypingFirstParameterName() throws Exception {
        performTest("MethodNoParamsAndThrows", 55, "int a", "empty.pass");
    }

    public void testOnFirstParameterName() throws Exception {
        performTest("Method", 60, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingFirstParameterNameAndSpace() throws Exception {
        performTest("MethodStart", 40, "public void op(int a ", "empty.pass");
    }
    
    public void testAfterTypingFirstParameterNameAndSpace() throws Exception {
        performTest("MethodNoParamsAndThrows", 55, "int a ", "empty.pass");
    }

    public void testAfterFirstParameterNameAndSpace() throws Exception {
        performTest("Method", 60, " ", "empty.pass");
    }
    
    public void testEmptyFileBeforeTypingSecondParameter() throws Exception {
        performTest("MethodStart", 40, "public void op(int a,", "parameterTypes.pass");
    }
    
    public void testBeforeTypingSecondParameter() throws Exception {
        performTest("MethodNoParamsAndThrows", 55, "int a,", "parameterTypes.pass");
    }

    public void testBeforeSecondParameter() throws Exception {
        performTest("Method", 61, null, "parameterTypes.pass");
    }
    
    public void testEmptyFileTypingSecondParameterType() throws Exception {
        performTest("MethodStart", 40, "public void op(int a, bo", "booleanKeyword.pass");
    }
    
    public void testTypingSecondParameterType() throws Exception {
        performTest("MethodNoParamsAndThrows", 55, "int a, bo", "booleanKeyword.pass");
    }

    public void testOnSecondParameterType() throws Exception {
        performTest("Method", 64, null, "booleanKeyword.pass");
    }
    
    public void testEmptyFileAfterTypingSecondParameterType() throws Exception {
        performTest("MethodStart", 40, "public void op(int a, boolean", "booleanKeyword.pass");
    }
    
    public void testAfterTypingSecondParameterType() throws Exception {
        performTest("MethodNoParamsAndThrows", 55, "int a, boolean", "booleanKeyword.pass");
    }

    public void testAfterSecondParameterType() throws Exception {
        performTest("Method", 69, null, "booleanKeyword.pass");
    }
    
    public void testEmptyFileBeforeTypingSecondParameterName() throws Exception {
        performTest("MethodStart", 40, "public void op(int a, boolean ", "booleanVarName.pass");
    }
    
    public void testBeforeTypingSecondParameterName() throws Exception {
        performTest("MethodNoParamsAndThrows", 55, "int a, boolean ", "booleanVarName.pass");
    }

    public void testBeforeSecondParameterName() throws Exception {
        performTest("Method", 70, null, "booleanVarName.pass");
    }
    
    public void testEmptyFileTypingSecondParameterName() throws Exception {
        performTest("MethodStart", 40, "public void op(int a, boolean b", "booleanVarName.pass");
    }
    
    public void testTypingSecondParameterName() throws Exception {
        performTest("MethodNoParamsAndThrows", 55, "int a, boolean b", "booleanVarName.pass");
    }

    public void testOnSecondParameterName() throws Exception {
        performTest("Method", 71, null, "booleanVarName.pass");
    }

    public void testEmptyFileBeforeTypingThrowsKeyword() throws Exception {
        performTest("MethodStart", 40, "public void op() ", "throwsKeyword.pass");
    }
    
    public void testBeforeTypingThrowsKeyword() throws Exception {
        performTest("MethodNoParamsAndThrows", 56, " ", "throwsKeyword.pass");
    }

    public void testBeforeThrowsKeyword() throws Exception {
        performTest("Method", 73, null, "throwsKeyword.pass");
    }
    
    public void testEmptyFileTypingThrowsKeyword() throws Exception {
        performTest("MethodStart", 40, "public void op() t", "throwsKeyword.pass");
    }
    
    public void testTypingThrowsKeyword() throws Exception {
        performTest("MethodNoParamsAndThrows", 56, " t", "throwsKeyword.pass");
    }

    public void testOnThrowsKeyword() throws Exception {
        performTest("Method", 74, null, "throwsKeyword.pass");
    }
    
    public void testEmptyAfterFileTypingThrowsKeyword() throws Exception {
        performTest("MethodStart", 40, "public void op() throws", "throwsKeyword.pass");
    }
    
    public void testAfterTypingThrowsKeyword() throws Exception {
        performTest("MethodNoParamsAndThrows", 56, " throws", "throwsKeyword.pass");
    }

    public void testAfterThrowsKeyword() throws Exception {
        performTest("Method", 79, null, "throwsKeyword.pass");
    }
        
    public void testEmptyFileBeforeTypingThrownException() throws Exception {
        performTest("MethodStart", 40, "public void op() throws ", "javaLangThrowables.pass");
    }
    
    public void testBeforeTypingThrownException() throws Exception {
        performTest("MethodNoParamsAndThrows", 56, "throws ", "javaLangThrowables.pass");
    }
    
    public void testBeforeThrownException() throws Exception {
        performTest("Method", 80, null, "javaLangThrowables.pass");
    }
    
    public void testEmptyFileTypingThrownException() throws Exception {
        performTest("MethodStart", 40, "public void op() throws N", "javaLangThrowablesStartingWithN.pass");
    }
    
    public void testTypingThrownException() throws Exception {
        performTest("MethodNoParamsAndThrows", 56, "throws N", "javaLangThrowablesStartingWithN.pass");
    }
    
    public void testOnThrownException() throws Exception {
        performTest("Method", 81, null, "javaLangThrowablesStartingWithN.pass");
    }
    
    public void testEmptyFileAfterTypingThrownException() throws Exception {
        performTest("MethodStart", 40, "public void op() throws NullPointerException", "nullPointerException.pass");
    }
    
    public void testAfterTypingThrownException() throws Exception {
        performTest("MethodNoParamsAndThrows", 56, "throws NullPointerException", "nullPointerException.pass");
    }
    
    public void testAfterThrownException() throws Exception {
        performTest("Method", 100, null, "nullPointerException.pass");
    }
    
    public void testEmptyFileAfterTypingThrownExceptionAndSpace() throws Exception {
        performTest("MethodStart", 40, "public void op() throws NullPointerException ", "empty.pass");
    }
    
    public void testAfterTypingThrownExceptionAndSpace() throws Exception {
        performTest("MethodNoParamsAndThrows", 56, "throws NullPointerException ", "empty.pass");
    }
    
    public void testAfterThrownExceptionAndSpace() throws Exception {
        performTest("Method", 100, " ", "empty.pass");
    }
    
    public void testEmptyFileBeforeTypingSecondThrownException() throws Exception {
        performTest("MethodStart", 40, "public void op() throws NullPointerException, ", "javaLangThrowables.pass");
    }
    
    public void testBeforeTypingSecondThrownException() throws Exception {
        performTest("MethodNoParamsAndThrows", 56, "throws NullPointerException, ", "javaLangThrowables.pass");
    }
    
    public void testBeforeSecondThrownExceptionAndSpace() throws Exception {
        performTest("Method", 102, null, "javaLangThrowables.pass");
    }
    
    public void testEmptyFileTypingSecondThrownException() throws Exception {
        performTest("MethodStart", 40, "public void op() throws NullPointerException, I", "javaLangThrowablesStartingWithI.pass");
    }
    
    public void testTypingSecondThrownException() throws Exception {
        performTest("MethodNoParamsAndThrows", 56, "throws NullPointerException, I", "javaLangThrowablesStartingWithI.pass");
    }
    
    public void testOnSecondThrownException() throws Exception {
        performTest("Method", 103, null, "javaLangThrowablesStartingWithI.pass");
    }
    
    public void testEmptyFileAfterTypingMethodBody() throws Exception {
        performTest("MethodStart", 40, "public void op() {\n}", "memberModifiersTypesAndGenElements.pass");
    }
    
    public void testAfterMethodBody() throws Exception {
        performTest("Method", 131, null, "memberModifiersTypesAndGenElements.pass");
    }
    
    public void testEmptyFileAfterTypingIncompleteMethodBodyAndPublicKeyword() throws Exception {
        performTest("MethodStart", 40, "public void op() {\npublic", "empty.pass");
    }    

    public void testEmptyFileAfterTypingIncompleteMethodBodyAndPublicKeywordAndSpace() throws Exception {
        performTest("MethodStart", 40, "public void op() {\npublic ", "memberModifiersAndTypesWithoutPublic.pass");
    }    

    // field declaration tests -------------------------------------------------
    
    public void testEmptyFileAfterTypingFieldNameAndSpace() throws Exception {
        performTest("MethodStart", 40, "public int field ", "empty.pass");
    }
    
    public void testAfterTypingFieldNameAndSpace() throws Exception {
        performTest("FieldNoInit", 56, " ", "empty.pass");
    }
    
    public void testAfterFieldNameAndSpace() throws Exception {
        performTest("Field", 57, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingAssignmentInField() throws Exception {
        performTest("MethodStart", 40, "public static int staticField = 10;\npublic int field =", "typesLocalMembersAndSmartInt.pass");
    }
    
    public void testAfterTypingAssignmentInField() throws Exception {
        performTest("FieldNoInit", 56, " =", "typesLocalMembersAndSmartInt.pass");
    }
    
    public void testAfterAssignmentInField() throws Exception {
        performTest("Field", 58, null, "typesLocalMembersAndSmartInt.pass");
    }
    
    public void testEmptyFileBeforeTypingInitOfField() throws Exception {
        performTest("MethodStart", 40, "public static int staticField = 10;\npublic int field = ", "typesLocalMembersAndSmartInt.pass");
    }
    
    public void testBeforeTypingInitOfField() throws Exception {
        performTest("FieldNoInit", 56, " = ", "typesLocalMembersAndSmartInt.pass");
    }
    
    public void testBeforeInitOfField() throws Exception {
        performTest("Field", 59, null, "typesLocalMembersAndSmartInt.pass");
    }
    
    public void testEmptyFileBeforeTypingInitOfStaticField() throws Exception {
        performTest("MethodStart", 40, "public static int staticField = ", "typesAndStaticLocalMembers.pass");
    }
    
    public void testBeforeTypingInitOfStaticField() throws Exception {
        performTest("FieldNoInit", 91, " = ", "typesAndStaticLocalMembers.pass");
    }
    
    public void testBeforeInitOfStaticField() throws Exception {
        performTest("Field", 112, null, "typesAndStaticLocalMembers.pass");
    }
    
    public void testEmptyFileTypingInitOfField() throws Exception {
        performTest("MethodStart", 40, "public int field = ha", "intHashCode.pass");
    }
    
    public void testTypingInitOfField() throws Exception {
        performTest("FieldNoInit", 56, " = ha", "intHashCode.pass");
    }
    
    public void testOnInitOfField() throws Exception {
        performTest("Field", 61, null, "intHashCode.pass");
    }
    
    public void testEmptyFileTypingMethodInvocationWithinInitOfField() throws Exception {
        performTest("MethodStart", 40, "public static int staticField = 10;\npublic int field = hashCode(", "typesAndLocalMembers.pass");
    }
    
    public void testTypingMethodInvocationWithinInitOfField() throws Exception {
        performTest("FieldNoInit", 56, " = hashCode(", "typesAndLocalMembers.pass");
    }
    
    public void testOnMethodInvocationWithinInitOfField() throws Exception {
        performTest("Field", 68, null, "typesAndLocalMembers.pass");
    }
    
    public void testEmptyFileAfterTypingMethodInvocationWithinInitOfField() throws Exception {
        performTest("MethodStart", 40, "public int field = hashCode()", "empty.pass");
    }
    
    public void testAfterTypingMethodInvocationWithinInitOfField() throws Exception {
        performTest("FieldNoInit", 56, " = hashCode()", "empty.pass");
    }
    
    public void testAfterMethodInvocationWithinInitOfField() throws Exception {
        performTest("Field", 69, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingOperatorWithinInitOfField() throws Exception {
        performTest("MethodStart", 40, "public static int staticField = 10;\npublic int field = hashCode() /", "typesLocalMembersAndSmartPrimitives.pass");
    }
    
    public void testAfterTypingOperatorWithinInitOfField() throws Exception {
        performTest("FieldNoInit", 56, " = hashCode() /", "typesLocalMembersAndSmartPrimitives.pass");
    }
    
    public void testAfterOperatorWithinInitOfField() throws Exception {
        performTest("Field", 71, null, "typesLocalMembersAndSmartPrimitives.pass");
    }
    
    public void testEmptyFileAfterTypingConstantWithinInitOfField() throws Exception {
        performTest("MethodStart", 40, "public int field = hashCode() / 10", "empty.pass");
    }
    
    public void testAfterTypingConstantWithinInitOfField() throws Exception {
        performTest("FieldNoInit", 56, " = hashCode() / 10", "empty.pass");
    }
    
    public void testAfterConstantWithinInitOfField() throws Exception {
        performTest("Field", 74, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingFieldDeclaration() throws Exception {
        performTest("MethodStart", 40, "public int field = hashCode() / 10;", "memberModifiersTypesAndGenElements3.pass");
    }
    
    public void testAfterFieldDeclaration() throws Exception {
        performTest("Field", 75, null, "memberModifiersTypesAndGenElements4.pass");
    }
    
    public void testTypingStaticFieldAfterErrorInPreviousFieldDeclaration() throws Exception {
        performTest("MethodStart", 40, "public int \npublic sta", "staticKeyword.pass");
    }

    public void testTypingStaticFieldAfterErrorInPreviousFieldInitialization() throws Exception {
        performTest("MethodStart", 40, "public int field = has\npublic sta", "staticKeyword.pass");
    }
    
    // basic method body tests -------------------------------------------------
   
    public void testEmptyFileTypingMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 89, null, "methodBodyContent.pass");
    }
    
    public void testTypingMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 89, null, "methodBodyContent.pass");
    }
    
    public void testInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 89, null, "methodBodyContent.pass");
    }
    
    public void testEmptyFileTypingLocalVariableTypeInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 98, "bo", "booleanKeyword.pass");
    }
    
    public void testTypingLocalVariableTypeInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 98, "bo", "booleanKeyword.pass");
    }
    
    public void testLocalVariableTypeInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 100, null, "booleanKeyword.pass");
    }
    
    public void testEmptyFileAfterTypingLocalVariableTypeInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 98, "boolean", "booleanKeyword.pass");
    }
    
    public void testAfterTypingLocalVariableTypeInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 98, "boolean", "booleanKeyword.pass");
    }
    
    public void testAfterLocalVariableTypeInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 105, null, "booleanKeyword.pass");
    }
    
    public void testEmptyFileBeforeTypingLocalVariableNameInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 98, "boolean ", "booleanVarName.pass");
    }
    
    public void testBeforeTypingLocalVariableNameInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 98, "boolean ", "booleanVarName.pass");
    }
    
    public void testBeforeLocalVariableNameInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 106, null, "booleanVarName.pass");
    }
    
    public void testEmptyFileTypingLocalVariableNameInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 98, "boolean b", "booleanVarName.pass");
    }
    
    public void testTypingLocalVariableNameInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 98, "boolean b", "booleanVarName.pass");
    }
    
    public void testOnLocalVariableNameInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 107, null, "booleanVarName.pass");
    }
    
    public void testEmptyFileAfterTypingLocalVariableNameAndSpaceInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 98, "boolean b ", "empty.pass");
    }
    
    public void testAfterTypingLocalVariableNameAndSpaceInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 98, "boolean b ", "empty.pass");
    }
    
    public void testAfterLocalVariableNameAndSpaceInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 107, " ", "empty.pass");
    }
    
    public void testEmptyFileAfterTypingLocalVariableInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 98, "boolean b;", "methodBodyContentAndLocalVar.pass");
    }
    
    public void testAfterTypingLocalVariableInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 98, "boolean b;", "methodBodyContentAndLocalVar.pass");
    }
    
    public void testAfterLocalVariableInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 108, null, "methodBodyContentAndLocalVar.pass");
    }
 
    public void testEmptyFileTypingTypeInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 98, "Sy", "system.pass");
    }
    
    public void testTypingTypeInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 98, "Sy", "system.pass");
    }
    
    public void testTypeInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 112, null, "system.pass");
    }
    
    public void testEmptyFileAfterTypingTypeInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 98, "System", "system.pass");
    }
    
    public void testAfterTypingTypeInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 98, "System", "system.pass");
    }
    
    public void testAfterTypeInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 116, null, "system.pass");
    }
    
    public void testEmptyFileAfterTypingTypeAndDotInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 98, "System.", "systemContent.pass");
    }
    
    public void testAfterTypingTypeAndDotInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 98, "System.", "systemContent.pass");
    }
    
    public void testAfterTypeAndDotInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 117, null, "systemContent.pass");
    }

    public void testEmptyFileTypingFieldAccessInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 98, "System.o", "systemOut.pass");
    }
    
    public void testTypingFieldAccessInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 98, "System.o", "systemOut.pass");
    }
    
    public void testFieldAccessInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 118, null, "systemOut.pass");
    }
   
    public void testEmptyFileAfterTypingFieldAccessInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 98, "System.out", "systemOut.pass");
    }
    
    public void testAfterTypingFieldAccessInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 98, "System.out", "systemOut.pass");
    }
    
    public void testAfterFieldAccessInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 120, null, "systemOut.pass");
    }
    
    public void testEmptyFileAfterTypingFieldAccessAndSpaceInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 98, "System.out ", "instanceOf.pass");
    }
    
    public void testAfterTypingFieldAccessAndSpaceInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 98, "System.out ", "instanceOf.pass");
    }
    
    public void testAfterFieldAccessAndSpaceInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 120, " ", "instanceOf.pass");
    }
    
    public void testEmptyFileAfterTypingFieldAccessAndDotInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 98, "System.out.", "systemOutContent.pass");
    }
    
    public void testAfterTypingFieldAccessAndDotInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 98, "System.out.", "systemOutContent.pass");
    }
    
    public void testAfterFieldAccessAndDotInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 121, null, "systemOutContent.pass");
    }

    public void testEmptyFileTypingMethodInvocationInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 98, "boolean b;\nSystem.out.println(", "typesLocalMembersVarsAndSmarts.pass");
    }
    
    public void testTypingMethodInvocationInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 98, "boolean b;\nSystem.out.println(", "typesLocalMembersVarsAndSmarts.pass");
    }
    
    public void testMethodInvocationInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 129, null, "typesLocalMembersVarsAndSmarts.pass");
    }
    
    public void testEmptyFileTypingMethodInvocationParameterInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 98, "boolean b;\nSystem.out.println(b", "typesLocalMembersVarsAndSmartsStartingWithB.pass");
    }
    
    public void testTypingMethodInvocationParameterInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 98, "boolean b;\nSystem.out.println(b", "typesLocalMembersVarsAndSmartsStartingWithB.pass");
    }
    
    public void testOnMethodInvocationParameterInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 130, null, "typesLocalMembersVarsAndSmartsStartingWithB.pass");
    }
    
    public void testEmptyFileTypingMethodInvocationParameterAndSpaceInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 98, "System.out.println(b ", "empty.pass");
    }
    
    public void testTypingMethodInvocationParameterAndSpaceInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 98, "System.out.println(b ", "empty.pass");
    }
    
    public void testAfterMethodInvocationParameterAndSpaceInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 130, " ", "empty.pass");
    }
    
    public void testEmptyFileBeforeTypingSecondMethodInvocationParameterInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 98, "boolean b;\nSystem.out.println(b, ", "typesLocalMembersAndVars.pass");
    }
    
    public void testBeforeTypingSecondMethodInvocationParameterInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 98, "boolean b;\nSystem.out.println(b, ", "typesLocalMembersAndVars.pass");
    }
    
    public void testBeforeSecondMethodInvocationParameterInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 130, ", ", "typesLocalMembersAndVars.pass");
    }
    
    public void testEmptyFileTypingSecondMethodInvocationParameterInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 98, "boolean b;\nSystem.out.println(b, b", "typesLocalMembersAndVarsStartingWithB.pass");
    }
    
    public void testTypingSecondMethodInvocationParameterInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 98, "boolean b;\nSystem.out.println(b, b", "typesLocalMembersAndVarsStartingWithB.pass");
    }
    
    public void testOnSecondMethodInvocationParameterInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 130, ", b", "typesLocalMembersAndVarsStartingWithB.pass");
    }
    
    public void testEmptyFileAfterTypingMethodInvocationParametersInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 98, "boolean b;\nSystem.out.println(b)", "empty.pass");
    }
    
    public void testAfterTypingMethodInvocationParametersInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 98, "boolean b;\nSystem.out.println(b)", "empty.pass");
    }
    
    public void testAfteMethodInvocationParametersInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 131, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingMethodInvocationParametersAndSpaceInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 98, "boolean b;\nSystem.out.println(b) ", "empty.pass");
    }
    
    public void testAfterTypingMethodInvocationParametersAndSpaceInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 98, "boolean b;\nSystem.out.println(b) ", "empty.pass");
    }
    
    public void testAfteMethodInvocationParametersAndSpaceInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 131, " ", "empty.pass");
    }
    
    public void testEmptyFileAfterTypingMethodInvocationInMethodBody() throws Exception {
        performTest("SimpleMethodBodyStart", 98, "boolean b;\nSystem.out.println(b);", "methodBodyContentAndLocalVar.pass");
    }
    
    public void testAfterTypingMethodInvocationInMethodBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 98, "boolean b;\nSystem.out.println(b);", "methodBodyContentAndLocalVar.pass");
    }
    
    public void testAfteMethodInvocationInMethodBody() throws Exception {
        performTest("SimpleMethodBody", 132, null, "methodBodyContentAndLocalVar.pass");
    }
}
