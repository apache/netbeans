/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.groovy.editor.api;

import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;

/**
 *
 * @author Martin Adamek
 */
public class GroovyDeclarationFinderTest extends GroovyTestBase {
    
    private final String TEST_BASE = "testfiles/declarationfinder/";

    public GroovyDeclarationFinderTest(String testName) {
        super(testName);
    }

    @Override
    protected Set<String> additionalSourceClassPath() {
        return Collections.singleton("/testfiles/declarationfinder");
    }

    // this test is for variables defined and used in the same CU.

    public void testField1() throws Exception {
        checkDeclaration(TEST_BASE + "Script.groovy", "        println va^r1", "    def ^var1 = 'aaa'");
    }

    // we gotta have a test for class usage:

    public void testClass1() throws Exception {
        checkDeclaration(TEST_BASE + "Consumer.groovy", "        Fin^der finder = new Finder()", "class ^Finder {");
    }

    public void testMethod1() throws Exception {
        checkDeclaration(TEST_BASE + "Methods1.groovy", "println get^Name(x)", "^def getName(a) {");
    }

    public void testMethod2() throws Exception {
        checkDeclaration(TEST_BASE + "Methods2.groovy", "println get^Name()", "^def getName() {");
    }
    
    public void testJavaMethod1() throws Exception {
        checkDeclaration(TEST_BASE + "a/Declaration4.groovy", "        def builder = new TestBuil^der()", "TestBuilder.java", 25);
    }
    
    public void testJavaMethod2() throws Exception {
        checkDeclaration(TEST_BASE + "a/Declaration4.groovy", "        def clon = builder.clo^ne(builder)", "TestBuilder.java", 63);
    }


    public void testGroovyClass1() throws Exception {
        checkDeclaration(TEST_BASE + "a/Declaration2.groovy", "    private Decla^ration1 x", "Declaration1.groovy", 17);
    }

    public void testGroovyClass2() throws Exception {
        checkDeclaration(TEST_BASE + "a/Declaration2.groovy", "    Declara^tion1 y", "Declaration1.groovy", 17);
    }

    public void testGroovyClass3() throws Exception {
        checkDeclaration(TEST_BASE + "a/Declaration2.groovy", "    a.Decla^ration1 z", "Declaration1.groovy", 17);
    }

    public void testGroovyClass4() throws Exception {
        checkDeclaration(TEST_BASE + "a/Declaration2.groovy", "    def foo1(Decla^ration1 foo) {", "Declaration1.groovy", 17);
    }

    public void testGroovyClass5() throws Exception {
        assertEquals(DeclarationLocation.NONE,
                findDeclaration(TEST_BASE + "a/Declaration2.groovy", "    private Declaration1 x^"));
    }

    public void testGroovyClass6() throws Exception {
        assertEquals(DeclarationLocation.NONE,
                findDeclaration(TEST_BASE + "a/Declaration2.groovy", "    Declaration1 y^"));
    }

    public void testGroovyClass7() throws Exception {
        assertEquals(DeclarationLocation.NONE,
                findDeclaration(TEST_BASE + "a/Declaration2.groovy", "    a.Declaration1 z^"));
    }

    public void testGroovyClass8() throws Exception {
        assertEquals(DeclarationLocation.NONE,
                findDeclaration(TEST_BASE + "a/Declaration2.groovy", "    def foo1(Declaration1 f^oo) {"));
    }

    public void testGroovyClass9() throws Exception {
        assertEquals(DeclarationLocation.NONE,
                findDeclaration(TEST_BASE + "a/Declaration2.groovy", "    def foo2(ba^r) {"));
    }

    public void testExtendsImplements1() throws Exception {
        checkDeclaration(TEST_BASE + "a/Declaration2.groovy",
                "class Declaration2 extends Declar^ation1 implements Interface1, Interface2 {", "Declaration1.groovy", 17);
    }

    public void testExtendsImplements2() throws Exception {
        checkDeclaration(TEST_BASE + "a/Declaration2.groovy",
                "class Declaration2 extends Declaration1 implements Interfa^ce1, Interface2 {", "Interface1.java", 29);
    }

    public void testExtendsImplements3() throws Exception {
        checkDeclaration(TEST_BASE + "a/Declaration2.groovy",
                "class Declaration2 extends Declaration1 implements Interface1, Int^erface2 {", "Interface2.java", 29);
    }

    public void testInnerClasses1() throws Exception {
        checkDeclaration(TEST_BASE + "a/Declaration3.groovy",
                "        return Inner^Classes.Type.DUMMY_1;", "InnerClasses.java", 25);
    }

    public void testInnerClasses2() throws Exception {
        checkDeclaration(TEST_BASE + "a/Declaration3.groovy",
                "        return InnerClasses.Ty^pe.DUMMY_1;", "InnerClasses.java", 64);
    }

    public void testInnerClasses3() throws Exception {
        checkDeclaration(TEST_BASE + "a/Declaration3.groovy",
                "        InnerClasses.Type.ca^ll()", "InnerClasses.java", 133);
    }

    public void testInnerClasses4() throws Exception {
        checkDeclaration(TEST_BASE + "a/Declaration3.groovy",
                "        return InnerClasses.Type.DU^MMY_1;", "InnerClasses.java", 79);
    }

    public void testImports1() throws Exception {
        checkDeclaration(TEST_BASE + "Imports.groovy",
                "import a.Interfa^ce1", "Interface1.java", 29);
    }

    public void testImports2() throws Exception {
        checkDeclaration(TEST_BASE + "Imports.groovy",
                "class Imports implements Interfa^ce1 {", "Interface1.java", 29);
    }

    public void testAnnotations1() throws Exception {
        checkDeclaration(TEST_BASE + "Annotations.groovy",
                "@Annot^ation class AnnotationOccurrencesTester {", "Annotation.java", 30);
    }

    public void testAnnotations2() throws Exception {
        checkDeclaration(TEST_BASE + "Annotations.groovy",
                "    @Annot^ation protected String field", "Annotation.java", 30);
    }

    public void testAnnotations3() throws Exception {
        checkDeclaration(TEST_BASE + "Annotations.groovy",
                "    @Annot^ation String property", "Annotation.java", 30);
    }

    public void testAnnotations4() throws Exception {
        checkDeclaration(TEST_BASE + "Annotations.groovy",
                "    @Annot^ation AnnotationOccurrencesTester() {}", "Annotation.java", 30);
    }

    public void testAnnotations5() throws Exception {
        checkDeclaration(TEST_BASE + "Annotations.groovy",
                "    @Annot^ation public String method() {}", "Annotation.java", 30);
    }

    public void testGroovyClassInner1() throws Exception {
        checkDeclaration(TEST_BASE + "ClassWithInner.groovy",
                "            this.^x = null", "ClassWithInner.groovy", 96);
    }

    public void testGroovyClassInner2() throws Exception {
        checkDeclaration(TEST_BASE + "ClassWithInner.groovy",
                "            ^y.isEmpty()", "ClassWithInner.groovy", 54);
    }

    public void testGroovyClassInner3() throws Exception {
        checkDeclaration(TEST_BASE + "ClassWithInner.groovy",
                "        this.^x.isNumber()", "ClassWithInner.groovy", 35);
    }
}
