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

package org.netbeans.modules.groovy.editor.api.parser;

import org.netbeans.modules.groovy.editor.api.parser.GroovyVirtualSourceProvider;
import java.io.IOException;
import java.util.List;
import org.codehaus.groovy.ast.ClassNode;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Adamek
 */
public class GroovyVirtualSourceProviderTest extends GroovyTestBase {

    private static final String lineSep = System.getProperty("line.separator");

    public GroovyVirtualSourceProviderTest(String testName) {
        super(testName);
    }

    public void testGeneratorWithClass() throws IOException {
        copyStringToFileObject(testFO,
                "class Foo {" + lineSep +
                "  def closure1 = {" + lineSep +
                "    println 'closure1'" + lineSep +
                "  }" + lineSep +
                "  def method1() {" + lineSep +
                "    println 'method1'" + lineSep +
                "  }" + lineSep +
                "}");
        List<ClassNode> classNodes = GroovyVirtualSourceProvider.getClassNodes(FileUtil.toFile(testFO));
        assertEquals(classNodes.size(), 1);

        GroovyVirtualSourceProvider.JavaStubGenerator generator = new GroovyVirtualSourceProvider.JavaStubGenerator();
        CharSequence charSequence = generator.generateClass(classNodes.get(0));
        assertEquals(
                "import groovy.util.*;" + lineSep +
                "import java.lang.*;" + lineSep +
                "import groovy.lang.*;" + lineSep +
                "import java.util.*;" + lineSep +
                "import java.io.*;" + lineSep +
                "import java.net.*;" + lineSep +
                "" + lineSep +
                "public class Foo" + lineSep +
                "  extends java.lang.Object  implements" + lineSep +
                "    groovy.lang.GroovyObject {" + lineSep +
                "public Foo() {}" + lineSep +
                "public java.lang.Object method1() { return null;}" + lineSep +
                "public java.lang.Object getClosure1() { return null;}" + lineSep +
                "public void setClosure1(java.lang.Object value) { }" + lineSep +
                "}" + lineSep, charSequence);
    }

    public void testGeneratorWithScript() throws IOException {
        copyStringToFileObject(testFO,
                "def closure1 = {" + lineSep +
                "  println 'closure1'" + lineSep +
                "}" + lineSep +
                "def method1() {" + lineSep +
                "  println 'method1'" + lineSep +
                "}");
        List<ClassNode> classNodes = GroovyVirtualSourceProvider.getClassNodes(FileUtil.toFile(testFO));
        assertEquals(classNodes.size(), 1);

        GroovyVirtualSourceProvider.JavaStubGenerator generator = new GroovyVirtualSourceProvider.JavaStubGenerator();
        CharSequence charSequence = generator.generateClass(classNodes.get(0));
        assertEquals(
                "import groovy.util.*;" + lineSep +
                "import java.lang.*;" + lineSep +
                "import groovy.lang.*;" + lineSep +
                "import java.util.*;" + lineSep +
                "import java.io.*;" + lineSep +
                "import java.net.*;" + lineSep +
                "" + lineSep +
                "public class Test" + lineSep +
                "  extends groovy.lang.Script {" + lineSep +
                "public Test() {}" + lineSep +
                "public Test(groovy.lang.Binding context) {}" + lineSep +
                "public static void main(java.lang.String[] args) { }" + lineSep +
                "public java.lang.Object run() { return null;}" + lineSep +
                "public java.lang.Object method1() { return null;}" + lineSep +
                "}" + lineSep, charSequence);
    }

    public void testGenerics() throws IOException {
        copyStringToFileObject(testFO,
                "class Foo {" + lineSep +
                "  static List<String> get() {" + lineSep +
                "    return new ArrayList<String>()" +
                "  }" + lineSep +
                "}");
        List<ClassNode> classNodes = GroovyVirtualSourceProvider.getClassNodes(FileUtil.toFile(testFO));
        assertEquals(classNodes.size(), 1);

        GroovyVirtualSourceProvider.JavaStubGenerator generator = new GroovyVirtualSourceProvider.JavaStubGenerator();
        CharSequence charSequence = generator.generateClass(classNodes.get(0));
        assertEquals(
                "import groovy.util.*;" + lineSep +
                "import java.lang.*;" + lineSep +
                "import groovy.lang.*;" + lineSep +
                "import java.util.*;" + lineSep +
                "import java.io.*;" + lineSep +
                "import java.net.*;" + lineSep +
                "" + lineSep +
                "public class Foo" + lineSep +
                "  extends java.lang.Object  implements" + lineSep +
                "    groovy.lang.GroovyObject {" + lineSep +
                "public Foo() {}" + lineSep +
                "public static java.util.List<java.lang.String> get() { return (java.util.List<java.lang.String>)null;}" + lineSep +
                "}" + lineSep, charSequence);
    }

    public void testImports() throws IOException {
        copyStringToFileObject(testFO,
            "import javax.swing.JPanel" + lineSep +
            "class MyTest extends JPanel {" + lineSep +
            "    JPanel getPanel() {" + lineSep +
            "        return null;" + lineSep +
            "    }" + lineSep +
            "}");

        List<ClassNode> classNodes = GroovyVirtualSourceProvider.getClassNodes(FileUtil.toFile(testFO));
        assertEquals(classNodes.size(), 1);

        GroovyVirtualSourceProvider.JavaStubGenerator generator = new GroovyVirtualSourceProvider.JavaStubGenerator();
        CharSequence charSequence = generator.generateClass(classNodes.get(0));
        assertEquals(
                "import groovy.util.*;" + lineSep +
                "import java.lang.*;" + lineSep +
                "import groovy.lang.*;" + lineSep +
                "import java.util.*;" + lineSep +
                "import javax.swing.*;" + lineSep +
                "import java.io.*;" + lineSep +
                "import java.net.*;" + lineSep +
                "" + lineSep +
                "public class MyTest" + lineSep +
                "  extends javax.swing.JPanel  implements" + lineSep +
                "    groovy.lang.GroovyObject {" + lineSep +
                "public MyTest() {}" + lineSep +
                "public javax.swing.JPanel getPanel() { return (javax.swing.JPanel)null;}" + lineSep +
                "}" + lineSep, charSequence);
    }

    public void testMultipleClasses() throws IOException {
        copyStringToFileObject(testFO,
                "class PostService {" + lineSep +
                "    boolean transactional = true" + lineSep +
                "    def serviceMethod() throws PostException {" + lineSep +
                "        throw new PostException();" + lineSep +
                "    }" + lineSep +
                "}" + lineSep +
                "" + lineSep +
                "class PostException extends Exception {" + lineSep +
                "    public PostException() {" + lineSep +
                "        super();" + lineSep +
                "    }" + lineSep +
                "}");
        List<ClassNode> classNodes = GroovyVirtualSourceProvider.getClassNodes(FileUtil.toFile(testFO));
        assertEquals(classNodes.size(), 2);

        GroovyVirtualSourceProvider.JavaStubGenerator generator = new GroovyVirtualSourceProvider.JavaStubGenerator();
        CharSequence charSequence = generator.generateClass(classNodes.get(0));
        assertEquals(
                "import groovy.util.*;" + lineSep +
                "import java.lang.*;" + lineSep +
                "import groovy.lang.*;" + lineSep +
                "import java.util.*;" + lineSep +
                "import java.io.*;" + lineSep +
                "import java.net.*;" + lineSep +
                "" + lineSep +
                "public class PostService" + lineSep +
                "  extends java.lang.Object  implements" + lineSep +
                "    groovy.lang.GroovyObject {" + lineSep +
                "public PostService() {}" + lineSep +
                "public java.lang.Object serviceMethod() throws PostException { return null;}" + lineSep +
                "public boolean getTransactional() { return (boolean)false;}" + lineSep +
                "public void setTransactional(boolean value) { }" + lineSep +
                "public boolean isTransactional() { return (boolean)false;}" + lineSep +
                "}" + lineSep, charSequence);

        charSequence = generator.generateClass(classNodes.get(1));
        assertEquals(
                "import groovy.util.*;" + lineSep +
                "import java.lang.*;" + lineSep +
                "import groovy.lang.*;" + lineSep +
                "import java.util.*;" + lineSep +
                "import java.io.*;" + lineSep +
                "import java.net.*;" + lineSep +
                "" + lineSep +
                "public class PostException" + lineSep +
                "  extends java.lang.Exception  implements" + lineSep +
                "    groovy.lang.GroovyObject {" + lineSep +
                "public PostException() {" + lineSep +
                "super ();" + lineSep +
                "}" + lineSep +
                "}" + lineSep, charSequence);
    }

    public void testThrowsClause() throws IOException {
        copyStringToFileObject(testFO,
            "class MyTest {" + lineSep +
            "    void test1() throws RuntimeException {" + lineSep +
            "        return null;" + lineSep +
            "    }" + lineSep +
            "    void test2() throws Exception {" + lineSep +
            "        return null;" + lineSep +
            "    }" + lineSep +
            "    def test3() throws RuntimeException {" + lineSep +
            "        return null;" + lineSep +
            "    }" + lineSep +
            "}");

        List<ClassNode> classNodes = GroovyVirtualSourceProvider.getClassNodes(FileUtil.toFile(testFO));
        assertEquals(classNodes.size(), 1);

        GroovyVirtualSourceProvider.JavaStubGenerator generator = new GroovyVirtualSourceProvider.JavaStubGenerator();
        CharSequence charSequence = generator.generateClass(classNodes.get(0));
        assertEquals(
                "import groovy.util.*;" + lineSep +
                "import java.lang.*;" + lineSep +
                "import groovy.lang.*;" + lineSep +
                "import java.util.*;" + lineSep +
                "import java.io.*;" + lineSep +
                "import java.net.*;" + lineSep +
                "" + lineSep +
                "public class MyTest" + lineSep +
                "  extends java.lang.Object  implements" + lineSep +
                "    groovy.lang.GroovyObject {" + lineSep +
                "public MyTest() {}" + lineSep +
                "public void test1() throws java.lang.RuntimeException { }" + lineSep +
                "public void test2() throws java.lang.Exception { }" + lineSep +
                "public java.lang.Object test3() throws java.lang.RuntimeException { return null;}" + lineSep +
                "}" + lineSep, charSequence);
    }
}
