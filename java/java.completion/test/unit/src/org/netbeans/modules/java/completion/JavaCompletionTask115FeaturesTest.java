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
package org.netbeans.modules.java.completion;

import javax.lang.model.SourceVersion;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.parsing.JavacParser;


/**
 *
 * @author arusinha
 */
public class JavaCompletionTask115FeaturesTest extends CompletionTestBase {

    private static String SOURCE_LEVEL = "15"; //NOI18N

    public JavaCompletionTask115FeaturesTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        try {
            SourceVersion.valueOf("RELEASE_15"); //NOI18N
            suite.addTestSuite(JavaCompletionTask115FeaturesTest.class);
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_13, skip tests
            suite.addTest(new JavaCompletionTask115FeaturesTest("noop")); //NOI18N
        }
        return suite;
    }

    public void testAfterTypeParams() throws Exception {
        performTest("Generics", 887, null, "extendsAndImplementsAndPermitsKeywords.pass",SOURCE_LEVEL);
    }


    public void testAfterTypingTypeParams() throws Exception {
        performTest("GenericsNoTypeParams", 841, "<X extends Number, Y extends RuntimeException>", "extendsAndImplementsAndPermitsKeywords.pass",SOURCE_LEVEL);
    }

    public void testEmptyFileAfterTypingTypeParams() throws Exception {
        performTest("GenericsStart", 841, "<X extends Number, Y extends RuntimeException>", "extendsAndImplementsAndPermitsKeywords.pass",SOURCE_LEVEL);
    }

    public void testEmptyFileBeforeTypingDefaultModifier() throws Exception {
        performTest("Empty", 808, "package test;\ninterface Test {", "interfaceMemberModifiersAndTypes.pass", SOURCE_LEVEL);
    }

    public void testBeforeDefaultModifier() throws Exception {
        performTest("Interface", 846, null, "interfaceMemberModifiersAndTypes.pass", SOURCE_LEVEL);
    }

    public void testEmptyFileAfterTypingPackageDecl() throws Exception {
        performTest("Empty", 808, "package test;", "topLevelKeywordsWithoutPackage.pass",SOURCE_LEVEL);
    }

    public void testEmptyFileAfterTypingIntefaceName() throws Exception {
        performTest("Empty", 808, "package test;\ninterface Test ", "extendsAndPermitsKeywords.pass",SOURCE_LEVEL);
    }

    public void testEmptyFileBeforeTypingImplementsKeyword() throws Exception {
        performTest("Empty", 808, "package test;\npublic class Test extends Object ", "implementsAndPermitsKeywords.pass",SOURCE_LEVEL);
    }

    public void testEmptyFileAfterTypingFieldDeclaration() throws Exception {
        performTest("MethodStart", 849, "public int field = hashCode() / 10;", "memberModifiersTypesAndGenElements3.pass",SOURCE_LEVEL);
    }

    public void testEmptyFileAfterTypingMethodBody() throws Exception {
        performTest("MethodStart", 849, "public void op() {\n}", "memberModifiersTypesAndGenElements.pass",SOURCE_LEVEL);
    }

    public void testAfterTypingImplementedInterfaceAndSpace() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 842, "implements Cloneable ", "permitsKeyword.pass",SOURCE_LEVEL);
    }

    public void testAfterMethodBody() throws Exception {
        performTest("Method", 940, null, "memberModifiersTypesAndGenElements.pass",SOURCE_LEVEL);
    }

    public void testBeforeTypingImplementsKeyword() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 842, "extends Object ", "implementsAndPermitsKeywords.pass",SOURCE_LEVEL);
    }

    public void testAfterClassBody() throws Exception {
        performTest("Simple", 891, null, "classModifiersWithoutPublic.pass",SOURCE_LEVEL);
    }

    public void testEmptyFile() throws Exception {
        performTest("Empty", 808, null, "topLevelKeywords.pass",SOURCE_LEVEL);
    }

    public void testEmptyFileBeforeTypingExtendsKeyword() throws Exception {
        performTest("Empty", 808, "package test;\npublic class Test ", "extendsAndImplementsAndPermitsKeywords.pass",SOURCE_LEVEL);
    }

    public void testBeforeImplementsKeyword() throws Exception {
        performTest("Simple", 857, null, "implementsAndPermitsKeywords.pass",SOURCE_LEVEL);
    }    
    public void testAfterTypingPackageDecl() throws Exception {
        performTest("SimpleNoPackage", 808, "package test;", "topLevelKeywordsWithoutPackage.pass",SOURCE_LEVEL);
    }    
 
    public void testBeforeTypingExtendsKeyword() throws Exception {
        performTest("SimpleNoExtendsAndImplements", 842, null, "extendsAndImplementsAndPermitsKeywords.pass",SOURCE_LEVEL);
    }
      public void testAfterFieldDeclaration() throws Exception {
        performTest("Field", 884, null, "memberModifiersTypesAndGenElements4.pass",SOURCE_LEVEL);
    }
      
     public void testFileBeginning() throws Exception {
        performTest("Simple", 808, null, "topLevelKeywords.pass",SOURCE_LEVEL);
    }
     
    public void testBeforeClassKeyword() throws Exception {
        performTest("Simple", 831, null, "classModifiersWithoutPublic.pass",SOURCE_LEVEL);
    }
      
    public void testAfterTypingIntefaceName() throws Exception {
        performTest("SimpleInterfaceNoExtends", 839, null, "extendsAndPermitsKeywords.pass",SOURCE_LEVEL);
    }
      public void testEmptyFileTypingPublicKeywordInMethodDecl() throws Exception {
        performTest("MethodStart", 849, "p", "memberModifiersStartingWithP.pass",SOURCE_LEVEL);
    }
    
    public void testEmptyFileAfterTypingClassBody() throws Exception {
        performTest("Empty", 808, "package test;\npublic class Test {\n}", "classModifiersWithoutPublic.pass",SOURCE_LEVEL);
    }
        public void testAfterImportStatement() throws Exception {
        performTest("Import", 845,  null, "topLevelKeywordsWithoutPackage.pass",SOURCE_LEVEL);
    }
       public void testOnPublicKeywordInMethodDecl() throws Exception {
        performTest("Method", 850, null, "memberModifiersStartingWithP.pass",SOURCE_LEVEL);
    }
       
    public void testAfterImplementedInterfaceAndSpace() throws Exception {
        performTest("Simple", 877, " ", "permitsKeyword.pass",SOURCE_LEVEL);
    }
      
    public void testEmptyFileAfterTypingImportStatement() throws Exception {
        performTest("Empty", 808, "import java.awt.List;", "topLevelKeywordsWithoutPackage.pass",SOURCE_LEVEL);
    }
      
    public void testBeforeExtendsKeyword() throws Exception {
        performTest("Simple", 842, null, "extendsAndImplementsAndPermitsKeywords.pass",SOURCE_LEVEL);
    }
      
    public void testInClassBody() throws Exception {
        performTest("Simple", 889, null, "memberModifiersTypesAndGenElements2.pass",SOURCE_LEVEL);
    }
    public void testAfterTypingImportStatement() throws Exception {
        performTest("Simple", 823, "import java.awt.List;", "topLevelKeywordsWithoutPackage.pass",SOURCE_LEVEL);
    }
      
    public void testEmptyFileAfterTypingImplementedInterfaceAndSpace() throws Exception {
        performTest("Empty", 808, "package test;\npublic class Test extends Object implements Cloneable ", "permitsKeyword.pass",SOURCE_LEVEL);
    }
     public void testEmptyFileTypingClassBody() throws Exception {
        performTest("Empty", 808, "package test;\npublic class Test {", "memberModifiersTypesAndGenElements.pass",SOURCE_LEVEL);
    }
        
    public void testAfterIntefaceName() throws Exception {
        performTest("SimpleInterface", 839, null, "extendsAndPermitsKeywords.pass",SOURCE_LEVEL);
    }
      
    public void testEmptyFileBeforeTypingClassKeyword() throws Exception {
        performTest("Empty", 808, "package test;\npublic ", "classModifiersWithoutPublic.pass",SOURCE_LEVEL);
    }
     
        public void testUnimplementedMethod() throws Exception {
        performTest("UnimplementedMethod", 894, "", "UnimplementedMethod.pass",SOURCE_LEVEL);
    }
    
    public void testOverrideAbstractList() throws Exception {
        performTest("OverrideAbstractList", 927, "", "OverrideAbstractList.pass",SOURCE_LEVEL);
    }
    
    /**
     * Checks that cc: offers just one size() for override, but offers size() for both implement AND override.
     */
    public void testOverrideAbstractListAbstract() throws Exception {
        performTest("OverrideAbstractListAbstract", 935, "", "OverrideAbstractListAbstract.pass",SOURCE_LEVEL);
    }
    
    public void testCreateConstructorTest() throws Exception {
        performTest("CreateConstructorTest", 1058, "", "CreateConstructorTest.pass",SOURCE_LEVEL);
    }

    public void testCreateConstructorTestInnerClass() throws Exception {
        performTest("CreateConstructorTest", 1243, "", "CreateConstructorTestInnerClass.pass",SOURCE_LEVEL);
    }

    public void testCreateConstructorWithConstructors() throws Exception {
        performTest("CreateConstructorWithConstructors", 1209, "", "CreateConstructorWithConstructors.pass",SOURCE_LEVEL);
    }

    public void testCreateConstructorWithConstructorsInnerClass() throws Exception {
        performTest("CreateConstructorWithConstructors", 1476, "", "CreateConstructorWithConstructorsInnerClass.pass",SOURCE_LEVEL);
    }

    public void testCreateConstructorWithDefaultConstructor() throws Exception {
        performTest("CreateConstructorWithDefaultConstructor", 1161, "", "CreateConstructorWithDefaultConstructor.pass",SOURCE_LEVEL);
    }

    public void testCreateConstructorWithDefaultConstructorInnerClass() throws Exception {
        performTest("CreateConstructorWithDefaultConstructor", 1369, "", "CreateConstructorWithDefaultConstructorInnerClass.pass",SOURCE_LEVEL);
    }

    public void testCreateConstructorNonDefaultConstructor() throws Exception {
        performTest("CreateConstructorNonDefaultConstructor", 1188, "", "CreateConstructorNonDefaultConstructor.pass",SOURCE_LEVEL);
    }

    public void testCreateConstructorNonDefaultConstructorInnerClass() throws Exception {
        performTest("CreateConstructorNonDefaultConstructor", 1433, "", "CreateConstructorNonDefaultConstructorInnerClass.pass",SOURCE_LEVEL);
    }

    public void noop() {
    }

    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
}
