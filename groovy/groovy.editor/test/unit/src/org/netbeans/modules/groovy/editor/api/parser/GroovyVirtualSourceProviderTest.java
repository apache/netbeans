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
                "import groovy.lang.*;" + lineSep +
                "import java.util.*;" + lineSep +
                "import java.net.*;" + lineSep +
                "import java.lang.*;" + lineSep +
                "import groovy.util.*;" + lineSep +
                "import java.io.*;" + lineSep +
                "" + lineSep +
                "public class Foo" + lineSep +
                "  extends java.lang.Object  implements" + lineSep +
                "    groovy.lang.GroovyObject {" + lineSep +
                "public Foo() {}" + lineSep +
                "public java.lang.Object method1() { return null;}" + lineSep +
                "public groovy.lang.MetaClass getMetaClass() { return (groovy.lang.MetaClass)null;}" + lineSep +
                "public void setMetaClass(groovy.lang.MetaClass mc) { }" + lineSep +
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
                "import groovy.lang.*;" + lineSep +
                "import java.util.*;" + lineSep +
                "import java.net.*;" + lineSep +
                "import java.lang.*;" + lineSep +
                "import groovy.util.*;" + lineSep +
                "import java.io.*;" + lineSep +
                "" + lineSep +
                "public class Test" + lineSep +
                "  extends groovy.lang.Script {" + lineSep +
                "public Test() {}" + lineSep +
                "public Test(groovy.lang.Binding context) {" + lineSep +
                "super ();" + lineSep +
                "}" + lineSep +
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
                "import groovy.lang.*;" + lineSep +
                "import java.util.*;" + lineSep +
                "import java.net.*;" + lineSep +
                "import java.lang.*;" + lineSep +
                "import groovy.util.*;" + lineSep +
                "import java.io.*;" + lineSep +
                "" + lineSep +
                "public class Foo" + lineSep +
                "  extends java.lang.Object  implements" + lineSep +
                "    groovy.lang.GroovyObject {" + lineSep +
                "public Foo() {}" + lineSep +
                "public static java.util.List<java.lang.String> get() { return (java.util.List<java.lang.String>)null;}" + lineSep +
                "public groovy.lang.MetaClass getMetaClass() { return (groovy.lang.MetaClass)null;}" + lineSep +
                "public void setMetaClass(groovy.lang.MetaClass mc) { }" + lineSep +
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
                "import groovy.lang.*;" + lineSep +
                "import java.util.*;" + lineSep +
                "import java.net.*;" + lineSep +
                "import java.lang.*;" + lineSep +
                "import groovy.util.*;" + lineSep +
                "import javax.swing.*;" + lineSep +
                "import java.io.*;" + lineSep +
                "" + lineSep +
                "public class MyTest" + lineSep +
                "  extends javax.swing.JPanel  implements" + lineSep +
                "    groovy.lang.GroovyObject {" + lineSep +
                "public MyTest() {}" + lineSep +
                "public javax.swing.JPanel getPanel() { return (javax.swing.JPanel)null;}" + lineSep +
                "public groovy.lang.MetaClass getMetaClass() { return (groovy.lang.MetaClass)null;}" + lineSep +
                "public void setMetaClass(groovy.lang.MetaClass mc) { }" + lineSep +
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
                "import groovy.lang.*;" + lineSep +
                "import java.util.*;" + lineSep +
                "import java.net.*;" + lineSep +
                "import java.lang.*;" + lineSep +
                "import groovy.util.*;" + lineSep +
                "import java.io.*;" + lineSep +
                "" + lineSep +
                "public class PostService" + lineSep +
                "  extends java.lang.Object  implements" + lineSep +
                "    groovy.lang.GroovyObject {" + lineSep +
                "public PostService() {}" + lineSep +
                "public java.lang.Object serviceMethod() throws PostException { return null;}" + lineSep +
                "public groovy.lang.MetaClass getMetaClass() { return (groovy.lang.MetaClass)null;}" + lineSep +
                "public void setMetaClass(groovy.lang.MetaClass mc) { }" + lineSep +
                "public boolean getTransactional() { return (boolean)false;}" + lineSep +
                "public void setTransactional(boolean value) { }" + lineSep +
                "public boolean isTransactional() { return (boolean)false;}" + lineSep +
                "}" + lineSep, charSequence);

        charSequence = generator.generateClass(classNodes.get(1));
        assertEquals(
                "import groovy.lang.*;" + lineSep +
                "import java.util.*;" + lineSep +
                "import java.net.*;" + lineSep +
                "import java.lang.*;" + lineSep +
                "import groovy.util.*;" + lineSep +
                "import java.io.*;" + lineSep +
                "" + lineSep +
                "public class PostException" + lineSep +
                "  extends java.lang.Exception  implements" + lineSep +
                "    groovy.lang.GroovyObject {" + lineSep +
                "public PostException() {" + lineSep +
                "super ();" + lineSep +
                "}" + lineSep +
                "public groovy.lang.MetaClass getMetaClass() { return (groovy.lang.MetaClass)null;}" + lineSep +
                "public void setMetaClass(groovy.lang.MetaClass mc) { }" + lineSep +
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
                "import groovy.lang.*;" + lineSep +
                "import java.util.*;" + lineSep +
                "import java.net.*;" + lineSep +
                "import java.lang.*;" + lineSep +
                "import groovy.util.*;" + lineSep +
                "import java.io.*;" + lineSep +
                "" + lineSep +
                "public class MyTest" + lineSep +
                "  extends java.lang.Object  implements" + lineSep +
                "    groovy.lang.GroovyObject {" + lineSep +
                "public MyTest() {}" + lineSep +
                "public void test1() throws java.lang.RuntimeException { }" + lineSep +
                "public void test2() throws java.lang.Exception { }" + lineSep +
                "public java.lang.Object test3() throws java.lang.RuntimeException { return null;}" + lineSep +
                "public groovy.lang.MetaClass getMetaClass() { return (groovy.lang.MetaClass)null;}" + lineSep +
                "public void setMetaClass(groovy.lang.MetaClass mc) { }" + lineSep +
                "}" + lineSep, charSequence);
    }
}
