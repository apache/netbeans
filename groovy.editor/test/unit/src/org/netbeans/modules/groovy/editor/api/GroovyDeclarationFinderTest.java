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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
