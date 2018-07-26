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

package org.netbeans.modules.groovy.editor.api;

import java.util.Collections;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Adamek
 */
public class GroovyDeclarationFinderTest extends GroovyTestBase {
    
    private final String TEST_BASE = "testfiles/declarationfinder/";

    public GroovyDeclarationFinderTest(String testName) {
        super(testName);
    }

    protected @Override Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            ClassPath.SOURCE,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(getDataFile("/testfiles/declarationfinder"))
            })
        );
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
                "class Declaration2 extends Declaration1 implements Interfa^ce1, Interface2 {", "Interface1.java", 12);
    }

    public void testExtendsImplements3() throws Exception {
        checkDeclaration(TEST_BASE + "a/Declaration2.groovy",
                "class Declaration2 extends Declaration1 implements Interface1, Int^erface2 {", "Interface2.java", 12);
    }

    public void testInnerClasses1() throws Exception {
        checkDeclaration(TEST_BASE + "a/Declaration3.groovy",
                "        return Inner^Classes.Type.DUMMY_1;", "InnerClasses.java", 12);
    }

    // TESTFAIL wrongly parsed source by groovy
//    public void testInnerClasses2() throws Exception {
//        checkDeclaration(TEST_BASE + "a/Declaration3.groovy",
//                "        return InnerClasses.Ty^pe.DUMMY_1;", "InnerClasses.java", 45);
//    }
//
//    public void testInnerClasses3() throws Exception {
//        checkDeclaration(TEST_BASE + "a/Declaration3.groovy",
//                "        InnerClasses.Type.ca^ll()", "InnerClasses.java", 45);
//    }
//
//    public void testInnerClasses4() throws Exception {
//        checkDeclaration(TEST_BASE + "a/Declaration3.groovy",
//                "        return InnerClasses.Type.DU^MMY_1;", "InnerClasses.java", 80);
//    }
}
