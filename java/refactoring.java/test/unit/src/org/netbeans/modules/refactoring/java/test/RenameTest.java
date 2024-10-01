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
package org.netbeans.modules.refactoring.java.test;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TestUtilities.TestInput;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.java.ui.JavaRenameProperties;
import org.netbeans.modules.refactoring.spi.impl.UndoManager;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Becicka
 */
public class RenameTest extends RefactoringTestBase {

    public RenameTest(String name) {
        super(name);
    }
    
    public void testStaticImportDoubled() throws Exception {
        writeFilesAndWaitForScan(src, new File("Test.java", "import static java.util.Objects.requireNonNull;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "final int number;\n"
                + "\n"
                + "public Test(final Integer number) {\n"
                + "    this.number = requireNonNull(number);\n"
                + "}\n"
                + "\n"
                + "}"));
        JavaRenameProperties props = new JavaRenameProperties();
        performRename(src.getFileObject("Test.java"), 0, -1, "getal", props, true);
        verifyContent(src, new File("Test.java", "import static java.util.Objects.requireNonNull;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "final int getal;\n"
                + "\n"
                + "public Test(final Integer number) {\n"
                + "    this.getal = requireNonNull(number);\n"
                + "}\n"
                + "\n"
                + "}"));
    }
    
    public void testMethodChainInTest() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/SampleClass.java", "package t;\n"
                        + "\n"
                        + "import java.math.BigDecimal;\n"
                        + "\n"
                        + "public class SampleClass {\n"
                        + "\n"
                        + "  public static final class Builder {\n"
                        + "\n"
                        + "    private Builder() {\n"
                        + "    }\n"
                        + "\n"
                        + "    public static Builder create() {\n"
                        + "      return new Builder();\n"
                        + "    }\n"
                        + "\n"
                        + "    public Builder sampleMethod1(BigDecimal bd) {\n"
                        + "      return this;\n"
                        + "    }\n"
                        + "\n"
                        + "    public Builder sampleMethod2() {\n"
                        + "      return this;\n"
                        + "    }\n"
                        + "  }\n"
                        + "\n"
                        + "}"));
        writeFilesAndWaitForScan(test,
                new File("t/SampleClassTest.java", "package t;\n"
                        + "\n"
                        + "import static java.math.BigDecimal.*;\n"
                        + "import static t.BigDecimalConstants.*;\n"
                        + "\n"
                        + "public class SampleClassTest {\n"
                        + "\n"
                        + "  public void testSomeMethod() {\n"
                        + "    SampleClass.Builder.create()\n"
                        + "            .sampleMethod1(ONE)\n"
                        + "            .sampleMethod1(FIVE)\n"
                        + "            .sampleMethod2();\n"
                        + "  }\n"
                        + "\n"
                        + "}"),
                new File("t/BigDecimalConstants.java", "package t;\n"
                        + "\n"
                        + "import java.math.BigDecimal;\n"
                        + "\n"
                        + "public interface BigDecimalConstants {\n"
                        + "\n"
                        + "  public static final BigDecimal FIVE = new BigDecimal(\"5.00\");\n"
                        + "\n"
                        + "}\n"
                        + ""));
        JavaRenameProperties props = new JavaRenameProperties();
        performRename(src.getFileObject("t/SampleClass.java"), 1, 3, "sampleMethod3", props, true);
        verifyContent(src,
                new File("t/SampleClass.java", "package t;\n"
                        + "\n"
                        + "import java.math.BigDecimal;\n"
                        + "\n"
                        + "public class SampleClass {\n"
                        + "\n"
                        + "  public static final class Builder {\n"
                        + "\n"
                        + "    private Builder() {\n"
                        + "    }\n"
                        + "\n"
                        + "    public static Builder create() {\n"
                        + "      return new Builder();\n"
                        + "    }\n"
                        + "\n"
                        + "    public Builder sampleMethod1(BigDecimal bd) {\n"
                        + "      return this;\n"
                        + "    }\n"
                        + "\n"
                        + "    public Builder sampleMethod3() {\n"
                        + "      return this;\n"
                        + "    }\n"
                        + "  }\n"
                        + "\n"
                        + "}"));
        verifyContent(test,
                new File("t/SampleClassTest.java", "package t;\n"
                        + "\n"
                        + "import static java.math.BigDecimal.*;\n"
                        + "import static t.BigDecimalConstants.*;\n"
                        + "\n"
                        + "public class SampleClassTest {\n"
                        + "\n"
                        + "  public void testSomeMethod() {\n"
                        + "    SampleClass.Builder.create()\n"
                        + "            .sampleMethod1(ONE)\n"
                        + "            .sampleMethod1(FIVE)\n"
                        + "            .sampleMethod3();\n"
                        + "  }\n"
                        + "\n"
                        + "}"),
                new File("t/BigDecimalConstants.java", "package t;\n"
                        + "\n"
                        + "import java.math.BigDecimal;\n"
                        + "\n"
                        + "public interface BigDecimalConstants {\n"
                        + "\n"
                        + "  public static final BigDecimal FIVE = new BigDecimal(\"5.00\");\n"
                        + "\n"
                        + "}\n"
                        + ""));
    }
    
    public void testStaticImport() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "\n"
                + "import java.util.Collection;\n"
                + "import java.util.Arrays;\n"
                + "import static t.A.Length.of;\n"
                + "\n"
                + "public class A {\n"
                + "    static class Length {\n"
                + "        static int of(String in) {\n"
                + "            return in.length();\n"
                + "        }\n"
                + "\n"
                + "        static int of(Collection<?> collection) {\n"
                + "            return collection.size();\n"
                + "        }\n"
                + "    }\n"
                + "\n"
                + "    public static void main(final String[] args) {\n"
                + "        int i = of(\"jfgdksjgfkds\");\n"
                + "        int j = of(Arrays.asList(\"a\", \"b\"));\n"
                + "    }"
                + "}"));
        JavaRenameProperties props = new JavaRenameProperties();
        performRename(src.getFileObject("t/A.java"), 1, 2, "fooBar", props, false);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "\n"
                + "import java.util.Collection;\n"
                + "import java.util.Arrays;\n"
                + "import static t.A.Length.fooBar;\n"
                + "import static t.A.Length.of;\n"
                + "\n"
                + "public class A {\n"
                + "    static class Length {\n"
                + "        static int of(String in) {\n"
                + "            return in.length();\n"
                + "        }\n"
                + "\n"
                + "        static int fooBar(Collection<?> collection) {\n"
                + "            return collection.size();\n"
                + "        }\n"
                + "    }\n"
                + "\n"
                + "    public static void main(final String[] args) {\n"
                + "        int i = of(\"jfgdksjgfkds\");\n"
                + "        int j = fooBar(Arrays.asList(\"a\", \"b\"));\n"
                + "    }"
                + "}"));
    }
    
    public void testStaticImport2() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "\n"
                + "import java.util.Collection;\n"
                + "import java.util.Arrays;\n"
                + "import static t.A.Length.of;\n"
                + "\n"
                + "public class A {\n"
                + "    static class Length {\n"
                + "        static int of(Collection<?> collection) {\n"
                + "            return collection.size();\n"
                + "        }\n"
                + "    }\n"
                + "\n"
                + "    public static void main(final String[] args) {\n"
                + "        int j = of(Arrays.asList(\"a\", \"b\"));\n"
                + "    }"
                + "}"));
        JavaRenameProperties props = new JavaRenameProperties();
        performRename(src.getFileObject("t/A.java"), 1, 1, "fooBar", props, false);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "\n"
                + "import java.util.Collection;\n"
                + "import java.util.Arrays;\n"
                + "import static t.A.Length.fooBar;\n"
                + "\n"
                + "public class A {\n"
                + "    static class Length {\n"
                + "        static int fooBar(Collection<?> collection) {\n"
                + "            return collection.size();\n"
                + "        }\n"
                + "    }\n"
                + "\n"
                + "    public static void main(final String[] args) {\n"
                + "        int j = fooBar(Arrays.asList(\"a\", \"b\"));\n"
                + "    }"
                + "}"));
    }
    
    public void testJavadocMethodRef() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void foo() {\n"
                + "    }\n"
                + "    \n"
                + "    /**\n"
                + "     * Not {@link #bar}.\n"
                + "     * @see #foo() we just call method foo()\n"
                + "     */\n"
                + "    public static void main() {\n"
                + "        new A().foo();\n"
                + "    }\n"
                + "}"));
        performRename(src.getFileObject("t/A.java"), 1, -1, "fooBar", null, false);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void fooBar() {\n"
                + "    }\n"
                + "    \n"
                + "    /**\n"
                + "     * Not {@link #bar}.\n"
                + "     * @see #fooBar() we just call method foo()\n"
                + "     */\n"
                + "    public static void main() {\n"
                + "        new A().fooBar();\n"
                + "    }\n"
                + "}"));
    }
    
    public void testJavadocClassRef() throws Exception { // #250415 - Refactor Rename ... Class should rename Class in @see or @link Class#member javadoc
        writeFilesAndWaitForScan(src,
                new File("t/B.java", "package t;\n"
                        + "/**\n"
                        + " * Extends {@link A} with own main method\n"
                        + " * implemented using the BaseClass {@link A#main main} method.\n"
                        + " * \n"
                        + " * @see A\n"
                        + " */\n"
                        + "public class B extends A {\n"
                        + "  /**\n"
                        + "   * Calls {@link A#main}.\n"
                        + "   * @see A#main\n"
                        + "   */\n"
                        + "  public static void main(String[] parameters) {\n"
                        + "    A.main(new B(), parameters);\n"
                        + "  }\n"
                        + "}"),
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    public static void main(A base, String[] parameters) {\n"
                        + "    }\n"
                        + "}"));
        performRename(src.getFileObject("t/A.java"), -1, -1, "C", null, false);
        verifyContent(src,
                new File("t/B.java", "package t;\n"
                        + "/**\n"
                        + " * Extends {@link C} with own main method\n"
                        + " * implemented using the BaseClass {@link C#main main} method.\n"
                        + " * \n"
                        + " * @see C\n"
                        + " */\n"
                        + "public class B extends C {\n"
                        + "  /**\n"
                        + "   * Calls {@link C#main}.\n"
                        + "   * @see C#main\n"
                        + "   */\n"
                        + "  public static void main(String[] parameters) {\n"
                        + "    C.main(new B(), parameters);\n"
                        + "  }\n"
                        + "}"),
                new File("t/A.java", "package t;\n"
                        + "public class C {\n"
                        + "    public static void main(C base, String[] parameters) {\n"
                        + "    }\n"
                        + "}"));
    }
    
    public void test236868() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t;\n"
                        + "public class A {\n"
                        + "    public int x;\n"
                        + "        public void method(int x) {\n"
                        + "        LABEL:\n"
                        + "        while (x > 0) {\n"
                        + "            LABEL2:\n"
                        + "            for (int i = 0; i < 10; i++) {\n"
                        + "                if (x == 1) {\n"
                        + "                    break LABEL;\n"
                        + "                }\n"
                        + "                if (x == 2) {\n"
                        + "                    continue LABEL;\n"
                        + "                }\n"
                        + "                if (x == 3) {\n"
                        + "                    break LABEL2;\n"
                        + "                }\n"
                        + "            }\n"
                        + "        }\n"
                        + "    }"
                        + "}"));
        performRename(src.getFileObject("t/A.java"), source.indexOf("LABEL") + 1, "LABEL1", null, false);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    public int x;\n"
                        + "        public void method(int x) {\n"
                        + "        LABEL1:\n"
                        + "        while (x > 0) {\n"
                        + "            LABEL2:\n"
                        + "            for (int i = 0; i < 10; i++) {\n"
                        + "                if (x == 1) {\n"
                        + "                    break LABEL1;\n"
                        + "                }\n"
                        + "                if (x == 2) {\n"
                        + "                    continue LABEL1;\n"
                        + "                }\n"
                        + "                if (x == 3) {\n"
                        + "                    break LABEL2;\n"
                        + "                }\n"
                        + "            }\n"
                        + "        }\n"
                        + "    }"
                        + "}"));
    }
    
    public void test238268() throws Exception {
        String source;
        writeFilesAndWaitForScan(src, new File("t/A.java", source = "package t;\n"
                + "public class Calculator {\n"
                + "\n"
                + "    interface IntegerMath {\n"
                + "\n"
                + "        int operation(int a, int b);\n"
                + "    }\n"
                + "\n"
                + "    public int operateBinary(int a, int b, IntegerMath op) {\n"
                + "        return op.operation(a, b);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(String... args) {\n"
                + "\n"
                + "        Calculator myApp = new Calculator();\n"
                + "        IntegerMath addition = (a, b) -> a + b;\n"
                + "        IntegerMath subtraction = (x, y) -> x - y; // Try to rename 'x' to 'a'\n"
                + "        System.out.println(\"40 + 2 = \"\n"
                + "                + myApp.operateBinary(40, 2, addition));\n"
                + "        System.out.println(\"20 - 10 = \"\n"
                + "                + myApp.operateBinary(20, 10, subtraction));\n"
                + "    }\n"
                + "}"));
        performRename(src.getFileObject("t/A.java"), source.indexOf('x') + 1, "a", null, true);
        verifyContent(src, new File("t/A.java", "package t;\n"
                + "public class Calculator {\n"
                + "\n"
                + "    interface IntegerMath {\n"
                + "\n"
                + "        int operation(int a, int b);\n"
                + "    }\n"
                + "\n"
                + "    public int operateBinary(int a, int b, IntegerMath op) {\n"
                + "        return op.operation(a, b);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(String... args) {\n"
                + "\n"
                + "        Calculator myApp = new Calculator();\n"
                + "        IntegerMath addition = (a, b) -> a + b;\n"
                + "        IntegerMath subtraction = (a, y) -> a - y; // Try to rename 'x' to 'a'\n"
                + "        System.out.println(\"40 + 2 = \"\n"
                + "                + myApp.operateBinary(40, 2, addition));\n"
                + "        System.out.println(\"20 - 10 = \"\n"
                + "                + myApp.operateBinary(20, 10, subtraction));\n"
                + "    }\n"
                + "}"));
    }
    
    public void test235564a() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    public int x;\n"
                        + "    \n"
                        + "    public class Inner {\n"
                        + "        public int y;\n"
                        + "        \n"
                        + "        public void foo() { \n"
                        + "            y = x + 1;\n"
                        + "        }\n"
                        + "    }\n"
                        + "    public void foo() { \n"
                        + "        x = x + 1;\n"
                        + "    }\n"
                        + "}"));
        performRename(src.getFileObject("t/A.java"), 2, 1, "x", null, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    public int x;\n"
                        + "    \n"
                        + "    public class Inner {\n"
                        + "        public int x;\n"
                        + "        \n"
                        + "        public void foo() {\n"
                        + "            x = A.this.x + 1;\n"
                        + "        }\n"
                        + "    }\n"
                        + "    public void foo() { \n"
                        + "        x = x + 1;\n"
                        + "    }\n"
                        + "}"));
    }
    
    public void test235564b() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    public int x() {\n"
                        + "        return 1;\n"
                        + "    }\n"
                        + "    public class Inner {\n"
                        + "        public void foo() { \n"
                        + "            foo();\n"
                        + "            x();\n"
                        + "        }\n"
                        + "    }\n"
                        + "}"));
        performRename(src.getFileObject("t/A.java"), 2, 1, "x", null, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    public int x() {\n"
                        + "        return 1;\n"
                        + "    }\n"
                        + "    public class Inner {\n"
                        + "        public void x() { \n"
                        + "            x();\n"
                        + "            A.this.x();\n"
                        + "        }\n"
                        + "    }\n"
                        + "}"));
    }
    
    public void testJavadocEnum2() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public enum A {\n"
                + "    \n"
                + "    /**\n"
                + "     * This is ONE test\n"
                + "     * @see www.netbeans.org\n"
                + "     */\n"
                + "    ONE\n"
                + "}"));
        performRename(src.getFileObject("t/A.java"), 1, -1, "TWO", null, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public enum A {\n"
                + "    \n"
                + "    /**\n"
                + "     * This is TWO test\n"
                + "     * @see www.netbeans.org\n"
                + "     */\n"
                + "    TWO\n"
                + "}"));
    }
    
        public void testJavadocFieldgroup() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    \n"
                + "    /**\n"
                + "     * This is a test\n"
                + "     */\n"
                + "    private String s, t;\n"
                + "}"));
        performRename(src.getFileObject("t/A.java"), 1, -1, "v", null, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    \n"
                + "    /**\n"
                + "     * This is a test\n"
                + "     */\n"
                + "    private String v, t;\n"
                + "}"));
    }
    
    public void testJavadocEnum() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public enum A {\n"
                + "    \n"
                + "    /**\n"
                + "     * This is a test\n"
                + "     * @see www.netbeans.org\n"
                + "     */\n"
                + "    ONE\n"
                + "}"));
        performRename(src.getFileObject("t/A.java"), 1, -1, "TWO", null, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public enum A {\n"
                + "    \n"
                + "    /**\n"
                + "     * This is a test\n"
                + "     * @see www.netbeans.org\n"
                + "     */\n"
                + "    TWO\n"
                + "}"));
    }

    public void testRenamePropa() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int property;\n"
                + "    public void setProperty(int property) {\n"
                + "        this.property = property;\n"
                + "    }\n"
                + "    public int getProperty() {\n"
                + "        return property;\n"
                + "    }\n"
                + "    public int foo() {\n"
                + "        A a = new A();\n"
                + "        a.setProperty(1);\n"
                + "        return a.getProperty();\n"
                + "    }\n"
                + "}"));
        JavaRenameProperties props = new JavaRenameProperties();
        props.setIsRenameGettersSetters(true);
        performRename(src.getFileObject("t/A.java"), 1, -1, "renamed", props, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int renamed;\n"
                + "    public void setRenamed(int renamed) {\n"
                + "        this.renamed = renamed;\n"
                + "    }\n"
                + "    public int getRenamed() {\n"
                + "        return renamed;\n"
                + "    }\n"
                + "    public int foo() {\n"
                + "        A a = new A();\n"
                + "        a.setRenamed(1);\n"
                + "        return a.getRenamed();\n"
                + "    }\n"
                + "}"));

    }
    
    public void testRenamePropb() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    private String status;\n"
                        + "\n"
                        + "    /**\n"
                        + "     * @return the status\n"
                        + "     */\n"
                        + "    public String getStatus() {\n"
                        + "        return status;\n"
                        + "    }\n"
                        + "\n"
                        + "    /**\n"
                        + "     * @param status the status to set\n"
                        + "     */\n"
                        + "    public void setStatus(String status) {\n"
                        + "        this.status = status;\n"
                        + "    }"
                        + "}"));
        JavaRenameProperties props = new JavaRenameProperties();
        props.setIsRenameGettersSetters(true);
        performRename(src.getFileObject("t/A.java"), 1, -1, "message", props, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    private String message;\n"
                        + "\n"
                        + "    /**\n"
                        + "     * @return the message\n"
                        + "     */\n"
                        + "    public String getMessage() {\n"
                        + "        return message;\n"
                        + "    }\n"
                        + "\n"
                        + "    /**\n"
                        + "     * @param message the message to set\n"
                        + "     */\n"
                        + "    public void setMessage(String message) {\n"
                        + "        this.message = message;\n"
                        + "    }"
                        + "}"));

    }
    
    public void testRenamePropJavaDoc() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int property;\n"
                + "    /**\n"
                + "     * Update the value of property.\n"
                + "     * @param property the new value of property\n"
                + "     */\n"
                + "    public void setProperty(int property) {\n"
                + "        this.property = property;\n"
                + "    }\n"
                + "    public int getProperty() {\n"
                + "        return property;\n"
                + "    }\n"
                + "    public int foo() {\n"
                + "        A a = new A();\n"
                + "        a.setProperty(1);\n"
                + "        return a.getProperty();\n"
                + "    }\n"
                + "}"));
        JavaRenameProperties props = new JavaRenameProperties();
        props.setIsRenameGettersSetters(true);
        performRename(src.getFileObject("t/A.java"), 1, -1, "renamed", props, false);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int renamed;\n"
                + "    /**\n"
                + "     * Update the value of property.\n"
                + "     * @param renamed the new value of property\n"
                + "     */\n"
                + "    public void setRenamed(int renamed) {\n"
                + "        this.renamed = renamed;\n"
                + "    }\n"
                + "    public int getRenamed() {\n"
                + "        return renamed;\n"
                + "    }\n"
                + "    public int foo() {\n"
                + "        A a = new A();\n"
                + "        a.setRenamed(1);\n"
                + "        return a.getRenamed();\n"
                + "    }\n"
                + "}"));
    }
    
    public void testRenamePropJavaDoc2() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int property;\n"
                + "    /**\n"
                + "     * Update the value of property.\n"
                + "     * @param property the new value of property\n"
                + "     */\n"
                + "    public void setProperty(int property) {\n"
                + "        this.property = property;\n"
                + "    }\n"
                + "    public int getProperty() {\n"
                + "        return property;\n"
                + "    }\n"
                + "    public int foo() {\n"
                + "        A a = new A();\n"
                + "        a.setProperty(1);\n"
                + "        return a.getProperty();\n"
                + "    }\n"
                + "}"));
        JavaRenameProperties props = new JavaRenameProperties();
        props.setIsRenameGettersSetters(true);
        performRename(src.getFileObject("t/A.java"), 1, -1, "renamed", props, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int renamed;\n"
                + "    /**\n"
                + "     * Update the value of renamed.\n"
                + "     * @param renamed the new value of renamed\n"
                + "     */\n"
                + "    public void setRenamed(int renamed) {\n"
                + "        this.renamed = renamed;\n"
                + "    }\n"
                + "    public int getRenamed() {\n"
                + "        return renamed;\n"
                + "    }\n"
                + "    public int foo() {\n"
                + "        A a = new A();\n"
                + "        a.setRenamed(1);\n"
                + "        return a.getRenamed();\n"
                + "    }\n"
                + "}"));
    }

    public void testRenamePropUndoRedo() throws Exception { // #220547
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int property;\n"
                + "    public void setProperty(int property) {\n"
                + "        this.property = property;\n"
                + "    }\n"
                + "    public int getProperty() {\n"
                + "        return property;\n"
                + "    }\n"
                + "    public int foo() {\n"
                + "        A a = new A();\n"
                + "        a.setProperty(1);\n"
                + "        return a.getProperty();\n"
                + "    }\n"
                + "}"));
        JavaRenameProperties props = new JavaRenameProperties();
        props.setIsRenameGettersSetters(true);
        performRename(src.getFileObject("t/A.java"), 1, -1, "renamed", props, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int renamed;\n"
                + "    public void setRenamed(int renamed) {\n"
                + "        this.renamed = renamed;\n"
                + "    }\n"
                + "    public int getRenamed() {\n"
                + "        return renamed;\n"
                + "    }\n"
                + "    public int foo() {\n"
                + "        A a = new A();\n"
                + "        a.setRenamed(1);\n"
                + "        return a.getRenamed();\n"
                + "    }\n"
                + "}"));
        UndoManager undoManager = UndoManager.getDefault();
        undoManager.setAutoConfirm(true);
        undoManager.undo(null);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int property;\n"
                + "    public void setProperty(int property) {\n"
                + "        this.property = property;\n"
                + "    }\n"
                + "    public int getProperty() {\n"
                + "        return property;\n"
                + "    }\n"
                + "    public int foo() {\n"
                + "        A a = new A();\n"
                + "        a.setProperty(1);\n"
                + "        return a.getProperty();\n"
                + "    }\n"
                + "}"));
        undoManager.redo(null);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int renamed;\n"
                + "    public void setRenamed(int renamed) {\n"
                + "        this.renamed = renamed;\n"
                + "    }\n"
                + "    public int getRenamed() {\n"
                + "        return renamed;\n"
                + "    }\n"
                + "    public int foo() {\n"
                + "        A a = new A();\n"
                + "        a.setRenamed(1);\n"
                + "        return a.getRenamed();\n"
                + "    }\n"
                + "}"));

    }
    
    public void testRenameLocalVariable_1() throws Exception { // see NETBEANS-4274 
        writeFilesAndWaitForScan(src,
                new File("t/X.java", "package t;\n"
                + "public class X {\n"
                + "    private static int i;\n"
                + "    public static void main(String[] args) {\n"
                + "        X x = new X();\n"
                + "        String newName = Integer.toString(x.i);\n"
                + "    }\n"
                + "}"));
        JavaRenameProperties props = new JavaRenameProperties();
        performRename(src.getFileObject("t/X.java"), 1, -1, "newName", props, true);
        verifyContent(src,
                new File("t/X.java", "package t;\n"
                + "public class X {\n"
                + "    private static int newName;\n"
                + "    public static void main(String[] args) {\n"
                + "        X x = new X();\n"
                + "        String newName = Integer.toString(x.newName);\n"
                + "    }\n"
                + "}"));
    }       
    
    public void testRenameLocalVariable_2() throws Exception { // see NETBEANS-4274 
        writeFilesAndWaitForScan(src,
                new File("t/X.java", "package t;\n"
                + "public class X {\n"
                + "    private int i;\n"
                + "    public static void main(String[] args) {\n"
                + "        X x = new X();\n"
                + "        String newName = Integer.toString(x.i);\n"
                + "    }\n"
                + "}"));
        JavaRenameProperties props = new JavaRenameProperties();
        performRename(src.getFileObject("t/X.java"), 1, -1, "newName", props, true);
        verifyContent(src,
                new File("t/X.java", "package t;\n"
                + "public class X {\n"
                + "    private int newName;\n"
                + "    public static void main(String[] args) {\n"
                + "        X x = new X();\n"
                + "        String newName = Integer.toString(x.newName);\n"
                + "    }\n"
                + "}"));
    }      
    
    public void testRenameLocalVariable_3() throws Exception { // see NETBEANS-4274 
        writeFilesAndWaitForScan(src,
                new File("t/X.java", "package t;\n"
                + "public class X {\n"
                + "    private static int i;\n"
                + "    public static void main(String[] args) {\n"
                + "        X x = new X();\n"
                + "        String newName = Integer.toString(i);\n"
                + "    }\n"
                + "}"));
        JavaRenameProperties props = new JavaRenameProperties();
        performRename(src.getFileObject("t/X.java"), 1, -1, "newName", props, true);
        verifyContent(src,
                new File("t/X.java", "package t;\n"
                + "public class X {\n"
                + "    private static int newName;\n"
                + "    public static void main(String[] args) {\n"
                + "        X x = new X();\n"
                + "        String newName = Integer.toString(X.newName);\n"
                + "    }\n"
                + "}"));
    }      
    
    public void test253063() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public enum A implements ATest {\n"
                + "    AAA { void foo() { } }\n"
                + "}"),
                new File("t/ATest.java", "package t;\n"
                + "\n"
                + "public interface ATest {\n"
                + "    void foo();\n"
                + "}"));
        JavaRenameProperties props = new JavaRenameProperties();
        performRename(src.getFileObject("t/ATest.java"), 0, -1, "foos", props, false, new Problem(false, "ERR_IsOverridden"));
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public enum A implements ATest {\n"
                + "    AAA { void foos() { } }\n"
                + "}"),
                new File("t/ATest.java", "package t;\n"
                + "\n"
                + "public interface ATest {\n"
                + "    void foos();\n"
                + "}"));
    }
    
    public void test234094() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public enum A {\n"
                + "}"));
        writeFilesAndWaitForScan(test,
                new File("t/ATest.java", "package t;\n"
                + "import junit.framework.TestCase;\n"
                + "\n"
                + "public class ATest extends TestCase {\n"
                + "}"));
        JavaRenameProperties props = new JavaRenameProperties();
        props.setIsRenameTestClass(true);
        performRename(src.getFileObject("t/A.java"), -1, -1, "B", props, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n" // XXX: Why use old filename, is it not renamed?
                + "public enum B {\n"
                + "}"));
        verifyContent(test,
                new File("t/BTest.java", "package t;\n"
                + "import junit.framework.TestCase;\n"
                + "\n"
                + "public class BTest extends TestCase {\n"
                + "}"));
    }
    
    public void test200224() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "}"));
        writeFilesAndWaitForScan(test,
                new File("t/ATest.java", "package t;\n"
                + "import junit.framework.TestCase;\n"
                + "\n"
                + "public class ATest extends TestCase {\n"
                + "}"));
        JavaRenameProperties props = new JavaRenameProperties();
        props.setIsRenameTestClass(true);
        performRename(src.getFileObject("t/A.java"), -1, -1, "B", props, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n" // XXX: Why use old filename, is it not renamed?
                + "public class B {\n"
                + "}"));
        verifyContent(test,
                new File("t/BTest.java", "package t;\n"
                + "import junit.framework.TestCase;\n"
                + "\n"
                + "public class BTest extends TestCase {\n"
                + "}"));
    }

    public void test62897() throws Exception { // #62897 rename class method renames test method as well
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void foo() {\n"
                + "    }\n"
                + "}"));
        writeFilesAndWaitForScan(test,
                new File("t/ATest.java", "package t;\n"
                + "import junit.framework.TestCase;\n"
                + "\n"
                + "public class ATest extends TestCase {\n"
                + "    public void testFoo() {\n"
                + "    }\n"
                + "}"));
        JavaRenameProperties props = new JavaRenameProperties();
        props.setIsRenameTestClassMethod(true);
        performRename(src.getFileObject("t/A.java"), 1, -1, "fooBar", props, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n" // XXX: Why use old filename, is it not renamed?
                + "public class A {\n"
                + "    public void fooBar() {\n"
                + "    }\n"
                + "}"));
        verifyContent(test,
                new File("t/ATest.java", "package t;\n"
                + "import junit.framework.TestCase;\n"
                + "\n"
                + "public class ATest extends TestCase {\n"
                + "    public void testFooBar() {\n"
                + "    }\n"
                + "}"));
    }

    public void test243970() throws Exception { // #62897 rename class method renames correct test method as well
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void foo() {\n"
                + "    }\n"
                + "}"));
        writeFilesAndWaitForScan(test,
                new File("t/ATest.java", "package t;\n"
                + "import org.testng.annotations.DataProvider;\n"
                + "import org.testng.annotations.Test;\n"
                + "\n"
                + "public class ATest {\n"
                + "    @DataProvider\n"
                + "    Object[][] testFoo() {\n"
                + "        return new Object[][] {\n"
                + "            { \"Value 1\" },\n"
                + "            { \"Value 2\" }\n"
                + "        };\n"
                + "    }"
                + "    @Test(dataProvider = \"testFoo\")"
                + "    public void testFoo(final String value) {\n"
                + "    }\n"
                + "}"));
        JavaRenameProperties props = new JavaRenameProperties();
        props.setIsRenameTestClassMethod(true);
        performRename(src.getFileObject("t/A.java"), 1, -1, "fooBar", props, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n" // XXX: Why use old filename, is it not renamed?
                + "public class A {\n"
                + "    public void fooBar() {\n"
                + "    }\n"
                + "}"));
        verifyContent(test,
                new File("t/ATest.java", "package t;\n"
                + "import org.testng.annotations.DataProvider;\n"
                + "import org.testng.annotations.Test;\n"
                + "\n"
                + "public class ATest {\n"
                + "    @DataProvider\n"
                + "    Object[][] testFoo() {\n"
                + "        return new Object[][] {\n"
                + "            { \"Value 1\" },\n"
                + "            { \"Value 2\" }\n"
                + "        };\n"
                + "    }"
                + "    @Test(dataProvider = \"testFoo\")"
                + "    public void testFooBar(final String value) {\n"
                + "    }\n"
                + "}"));
    }
    
    public void test111953() throws Exception {
        writeFilesAndWaitForScan(src, new File("t/B.java", "class B { public void m(){};}"),
                new File("t/A.java", "class A extends B implements I{ public void m(){};}"),
                new File("t/I.java", "interface I { void m();}"),
                new File("t/J.java", "interface J { void m();}"),
                new File("t/C.java", "class C extends D implements I, J{ public void m(){};}"),
                new File("t/D.java", "class D { public void m(){};}"));
        performRename(src.getFileObject("t/B.java"), 1, -1, "k", null, true, new Problem(false, "ERR_IsOverridden"), new Problem(false, "ERR_IsOverriddenOverrides"));
        verifyContent(src, new File("t/B.java", "class B { public void k(){};}"),
                new File("t/A.java", "class A extends B implements I{ public void k(){};}"),
                new File("t/I.java", "interface I { void m();}"),
                new File("t/J.java", "interface J { void m();}"),
                new File("t/C.java", "class C extends D implements I, J{ public void m(){};}"),
                new File("t/D.java", "class D { public void m(){};}"));
    }
    
    public void test195070() throws Exception { // #195070 - refactor/rename works wrong with override
        writeFilesAndWaitForScan(src, new File("t/A.java", "class A { public void bindSuper(){}}"),
                new File("t/B.java", "class B extends A { public void bind(){ bindSuper();}}"));
        performRename(src.getFileObject("t/A.java"), 1, -1, "bind", null, true);
        verifyContent(src, new File("t/A.java", "class A { public void bind(){}}"),
                new File("t/B.java", "class B extends A { public void bind(){ super.bind();}}"));
        
        writeFilesAndWaitForScan(src, new File("t/A.java", "class A { public void bindSuper(){}}"),
                new File("t/B.java", "class B extends A { public void bind(){ bindSuper();}}"));
        performRename(src.getFileObject("t/A.java"), 1, -1, "binding", null, true);
        verifyContent(src, new File("t/A.java", "class A { public void binding(){}}"),
                new File("t/B.java", "class B extends A { public void bind(){ binding();}}"));
    }
    
    public void test215139() throws Exception { // #215139 - [Rename] Method and Field rename incorrectly adds Type.super 
        writeFilesAndWaitForScan(src, new File("t/A.java", "class A { public void bindSuper(){}}"),
                new File("t/B.java", "class B { private A a = new A(); public void bind(){ a.bindSuper();}}"));
        performRename(src.getFileObject("t/A.java"), 1, -1, "bind", null, true);
        verifyContent(src, new File("t/A.java", "class A { public void bind(){}}"),
                new File("t/B.java", "class B { private A a = new A(); public void bind(){ a.bind();}}"));
    }
    
    public void test202251() throws Exception { // #202251 - Refactoring code might lead to uncompilable code
        writeFilesAndWaitForScan(src, new File("test/Tool.java", "package test;\n"
                + "\n"
                + "import java.util.StringTokenizer;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>\n"
                + " */\n"
                + "public class Tool {\n"
                + "\n"
                + "    private Tool() {\n"
                + "    }\n"
                + "\n"
                + "    public static boolean compareNumberStrings(String first, String second) {\n"
                + "        return conpareNumberStrings(first, second, \".\");\n"
                + "    }\n"
                + "\n"
                + "    public static boolean conpareNumberStrings(String first, String second,\n"
                + "            String separator) {\n"
                + "        return true;\n"
                + "    }\n"
                + "}"));
        performRename(src.getFileObject("test/Tool.java"), 2, -1, "compareNumberStrings", null, true);
        verifyContent(src, new File("test/Tool.java", "package test;\n"
                + "\n"
                + "import java.util.StringTokenizer;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>\n"
                + " */\n"
                + "public class Tool {\n"
                + "\n"
                + "    private Tool() {\n"
                + "    }\n"
                + "\n"
                + "    public static boolean compareNumberStrings(String first, String second) {\n"
                + "        return compareNumberStrings(first, second, \".\");\n"
                + "    }\n"
                + "\n"
                + "    public static boolean compareNumberStrings(String first, String second,\n"
                + "            String separator) {\n"
                + "        return true;\n"
                + "    }\n"
                + "}"));
    }
    
    public void test104819() throws Exception{ // #104819 [Rename] Cannot rename inner class to same name as class in same package
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public int foo() {\n"
                + "        return C.c;\n"
                + "    }\n"
                + "    public static class C {\n"
                + "        public static int c = 5;\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "import t.A.C;"
                + "public class B {\n"
                + "    public int foo() {\n"
                + "        return C.c;\n"
                + "    }\n"
                + "}"));
        performRename(src.getFileObject("t/A.java"), 2, -1, "B", null, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public int foo() {\n"
                + "        return B.c;\n"
                + "    }\n"
                + "    public static class B {\n"
                + "        public static int c = 5;\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public int foo() {\n"
                + "        return A.B.c;\n"
                + "    }\n"
                + "}"));
    }
    
    public void test200985() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    static int a;\n"
                + "    static void m(int b){\n"
                + "        System.out.println(a);\n"
                + "    }\n"
                + "}"));
        performRename(src.getFileObject("t/A.java"), 1, -1, "b", null, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    static int b;\n"
                + "    static void m(int b){\n"
                + "        System.out.println(A.b);\n"
                + "    }\n"
                + "}"));
    }
    
    public void test200987() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    int a;\n"
                + "}\n"
                + "class B extends A {\n"
                + "    void m(int b){\n"
                + "        System.out.println(a);\n"
                + "    }\n"
                + "}"));
        performRename(src.getFileObject("t/A.java"), 1, -1, "b", null, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    int b;\n"
                + "}\n"
                + "class B extends A {\n"
                + "    void m(int b){\n"
                + "        System.out.println(this.b);\n"
                + "    }\n"
                + "}"));
    }
    
    public void test202675() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("a/A.java", "package a;\n"
                + "import b.B;\n"
                + "public class A {\n"
                + "    B b;\n"
                + "}"),
                new File("b/B.java", "package b;\n"
                + "public class B {\n"
                + "}"));
        
        RefactoringSession rs = RefactoringSession.create("Rename");
        RenameRefactoring rr = new RenameRefactoring(Lookups.singleton(src.getFileObject("b/B.java")));
        rr.setNewName("C");
        rr.setSearchInComments(true);
        rr.prepare(rs);
        rs.doRefactoring(true);

        verifyContent(src,
                new File("a/A.java", "package a;\n"
                + "import b.C;\n"
                + "public class A {\n"
                + "    C b;\n"
                + "}"),
                new File("b/C.java", "package b;\n"
                + "public class C {\n"
                + "}"));
    }
    
    public void test104819_2() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public int foo() {\n"
                + "        return C.c;\n"
                + "    }\n"
                + "    public static class C {\n"
                + "        public static int c = 5;\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public int foo() {\n"
                + "        return A.C.c;\n"
                + "    }\n"
                + "}"));
        performRename(src.getFileObject("t/A.java"), 2, -1, "B", null, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public int foo() {\n"
                + "        return B.c;\n"
                + "    }\n"
                + "    public static class B {\n"
                + "        public static int c = 5;\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public int foo() {\n"
                + "        return A.B.c;\n"
                + "    }\n"
                + "}"));
    }
    
    public void test201610() throws Exception { // #201610 [rename class] introduces behavioral change
        writeFilesAndWaitForScan(src, new File("p1/B.java", "package p1;\n"
                + "import p2.*;\n"
                + "public class B extends A {\n"
                + "  public long k(){\n"
                + "    return 0;\n"
                + "  }\n"
                + "}"),
                new File("p2/C.java", "package p2;\n"
                + "import p1.*;\n"
                + "public class C extends A {\n"
                + "  public long m(){\n"
                + "    return new B().k();\n"
                + "  }\n"
                + "}"),
                new File("p2/A.java", "package p2;\n"
                + "public class A {\n"
                + "  protected long k(){\n"
                + "    return 1;\n"
                + "  }\n"
                + "}"));
        performRename(src.getFileObject("p1/B.java"), -1, -1, "C", null, true);
        verifyContent(src, new File("p1/B.java", "package p1;\n"
                + "import p2.*;\n"
                + "public class C extends A {\n"
                + "  public long k(){\n"
                + "    return 0;\n"
                + "  }\n"
                + "}"),
                new File("p2/C.java", "package p2;\n"
                + "import p1.*;\n"
                + "public class C extends A {\n"
                + "  public long m(){\n"
                + "    return new p1.C().k();\n"
                + "  }\n"
                + "}"),
                new File("p2/A.java", "package p2;\n"
                + "public class A {\n"
                + "  protected long k(){\n"
                + "    return 1;\n"
                + "  }\n"
                + "}"));
    }
    
    public void test201608() throws Exception { // #201608 [rename class] introduces compilation error: Cycle detected: the type cannot extend/implement itself or one of its own member types
        writeFilesAndWaitForScan(src,
                new File("p2/C.java", "package p2;\n"
                + "import p1.*;\n"
                + "public class C extends B {\n"
                + "}"),
                new File("p1/B.java", "package p1;\n"
                + "import p2.*;\n"
                + "public class B extends A {\n"
                + "  long k(  long a){\n"
                + "    return 1;\n"
                + "  }\n"
                + "  protected long k(  int a){\n"
                + "    return 0;\n"
                + "  }\n"
                + "  public long m(){\n"
                + "    return new B().k(2);\n"
                + "  }\n"
                + "}"),
                new File("p2/A.java", "package p2;\n"
                + "public class A {\n"
                + "}"));
        performRename(src.getFileObject("p1/B.java"), -1, -1, "C", null, true);
        verifyContent(src,
                new File("p2/C.java", "package p2;\n"
                + "import p1.*;\n"
                + "public class C extends p1.C {\n"
                + "}"),
                new File("p1/B.java", "package p1;\n"
                + "import p2.*;\n"
                + "public class C extends A {\n"
                + "  long k(  long a){\n"
                + "    return 1;\n"
                + "  }\n"
                + "  protected long k(  int a){\n"
                + "    return 0;\n"
                + "  }\n"
                + "  public long m(){\n"
                + "    return new C().k(2);\n"
                + "  }\n"
                + "}"),
                new File("p2/A.java", "package p2;\n"
                + "public class A {\n"
                + "}"));
    }
    
    public void testJavadocClass() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("p2/C.java", "package p2;\n"
                + "public class C {\n"
                + "}"),
                new File("p2/A.java", "package p2;\n"
                + "/**\n"
                + " * @see C\n"
                + " */\n"
                + "public class A {\n"
                + "    private C b;\n"
                + "}"));
        performRename(src.getFileObject("p2/C.java"), -1, -1, "B", null, false);
        verifyContent(src,
                new File("p2/C.java", "package p2;\n"
                + "public class B {\n"
                + "}"),
                new File("p2/A.java", "package p2;\n"
                + "/**\n"
                + " * @see B\n"
                + " */\n"
                + "public class A {\n"
                + "    private B b;\n"
                + "}"));
    }
    
    public void testJavadocMethod() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void foo() {\n"
                + "    }\n"
                + "    \n"
                + "    /**\n"
                + "     * @see #foo() we just call method foo()\n"
                + "     */\n"
                + "    public static void main() {\n"
                + "        new A().foo();\n"
                + "    }\n"
                + "}"));
        performRename(src.getFileObject("t/A.java"), 1, -1, "fooBar", null, false);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void fooBar() {\n"
                + "    }\n"
                + "    \n"
                + "    /**\n"
                + "     * @see #fooBar() we just call method foo()\n"
                + "     */\n"
                + "    public static void main() {\n"
                + "        new A().fooBar();\n"
                + "    }\n"
                + "}"));
    }
    
    public void testComments() throws Exception{
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    /**\n"
                + "     * @see A.C\n"
                + "     */\n"
                + "    public int foo() {\n"
                + "        return C.c;\n"
                + "    }\n"
                + "    public static class C {\n"
                + "        public static int c = 5;\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "import t.A.C;"
                + "public class B {\n"
                + "    public int foo() {\n"
                + "        return C.c;\n"
                + "    }\n"
                + "}"));

        performRename(src.getFileObject("t/A.java"), 2, -1, "B", null, false);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    /**\n"
                + "     * @see A.B\n"
                + "     */\n"
                + "    public int foo() {\n"
                + "        return B.c;\n"
                + "    }\n"
                + "    public static class B {\n"
                + "        public static int c = 5;\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public int foo() {\n"
                + "        return A.B.c;\n"
                + "    }\n"
                + "}"));
    }
    
    public void testJavadocRecord() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("p2/C.java", "package p2;\n"
                + "public record C() {\n"
                + "}"),
                new File("p2/A.java", "package p2;\n"
                + "/**\n"
                + " * @see C\n"
                + " */\n"
                + "public class A {\n"
                + "    private C b;\n"
                + "}"));
        performRename(src.getFileObject("p2/C.java"), -1, -1, "B", null, false);
        verifyContent(src,
                new File("p2/C.java", "package p2;\n"
                + "public record B() {\n"
                + "}"),
                new File("p2/A.java", "package p2;\n"
                + "/**\n"
                + " * @see B\n"
                + " */\n"
                + "public class A {\n"
                + "    private B b;\n"
                + "}"));
    }
    
    public void testRenameRecordPropa() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public record A() {\n"
                + "    private static int property;\n"
                + "    public static int getProperty() {\n"
                + "        return property;\n"
                + "    }\n"
                + "    public int foo() {\n"
                + "        return property;\n"
                + "    }\n"
                + "}"));
        JavaRenameProperties props = new JavaRenameProperties();
        props.setIsRenameGettersSetters(true);
        performRename(src.getFileObject("t/A.java"), 1, -1, "renamed", props, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public record A() {\n"
                + "    private static int renamed;\n"
                + "    public static int getRenamed() {\n"
                + "        return renamed;\n"
                + "    }\n"
                + "    public int foo() {\n"
                + "        return renamed;\n"
                + "    }\n"
                + "}"));

    }

    public void testRenameBindingVariableType() throws Exception {
        if (!typeTestPatternAvailable()) return; //only run the test when javac supports it
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public boolean taragui(Object o) {\n"
                + "        return o instanceof A a && a.toString() != null;\n"
                + "    }\n"
                + "}"));
        JavaRenameProperties props = new JavaRenameProperties();
        performRename(src.getFileObject("t/A.java"), 25, "B", props, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class B {\n"
                + "    public boolean taragui(Object o) {\n"
                + "        return o instanceof B a && a.toString() != null;\n"
                + "    }\n"
                + "}"));

    }

    public void testRenameClassInAnnotation() throws Exception {
        TestInput input = TestUtilities.splitCodeAndPos("""
                                                        package t;
                                                        public class T|est {
                                                        }
                                                        """);

        writeFilesAndWaitForScan(src,
                new File("t/Test.java", input.code()),
                new File("t/Ann.java",
                         """
                         package t;
                         @interface Ann {
                             public Class<?> value();
                         }
                         """),
                new File("t/Use.java",
                         """
                         package t;
                         public class Use {
                             @Ann(Test.class)
                             void t1() {}
                             @Ann({Test.class})
                             void t2() {}
                         }
                         """));
        JavaRenameProperties props = new JavaRenameProperties();
        performRename(src.getFileObject("t/Test.java"), input.pos(), "NewName", props, true);
        verifyContent(src,
                new File("t/Test.java",
                         """
                         package t;
                         public class NewName {
                         }
                         """),
                new File("t/Ann.java",
                         """
                         package t;
                         @interface Ann {
                             public Class<?> value();
                         }
                         """),
                new File("t/Use.java",
                         """
                         package t;
                         public class Use {
                             @Ann(NewName.class)
                             void t1() {}
                             @Ann({NewName.class})
                             void t2() {}
                         }
                         """));
    }

    private void performRename(FileObject source, final int position, final int position2, final String newname, final JavaRenameProperties props, final boolean searchInComments, Problem... expectedProblems) throws Exception {
        final RenameRefactoring[] r = new RenameRefactoring[1];
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController javac) throws Exception {
                javac.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = javac.getCompilationUnit();

                Tree method = cut.getTypeDecls().get(0);
                if (position >= 0) {
                    method = ((ClassTree) method).getMembers().get(position);
                    if(position2 > 0) {
                        method = ((ClassTree) method).getMembers().get(position2);
                    }
                }

                TreePath tp = TreePath.getPath(cut, method);
                r[0] = new RenameRefactoring(Lookups.singleton(TreePathHandle.create(tp, javac)));
                r[0].setNewName(newname);
                r[0].setSearchInComments(searchInComments);
                if(props != null) {
                    r[0].getContext().add(props);
                }
            }
        }, true);
        
        RefactoringSession rs = RefactoringSession.create("Rename");
        List<Problem> problems = new LinkedList<Problem>();

        addAllProblems(problems, r[0].preCheck());
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r[0].prepare(rs));
        }
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, rs.doRefactoring(true));
        }

        assertProblems(Arrays.asList(expectedProblems), problems);
    }
    
    private void performRename(FileObject source, final int absPos, final String newname, final JavaRenameProperties props, final boolean searchInComments, Problem... expectedProblems) throws Exception {
        final RenameRefactoring[] r = new RenameRefactoring[1];
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController javac) throws Exception {
                javac.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = javac.getCompilationUnit();

                TreePath tp = javac.getTreeUtilities().pathFor(absPos);

                r[0] = new RenameRefactoring(Lookups.singleton(TreePathHandle.create(tp, javac)));
                r[0].setNewName(newname);
                r[0].setSearchInComments(searchInComments);
                if(props != null) {
                    r[0].getContext().add(props);
                }
            }
        }, true);
        
        RefactoringSession rs = RefactoringSession.create("Rename");
        List<Problem> problems = new LinkedList<>();

        addAllProblems(problems, r[0].preCheck());
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r[0].prepare(rs));
        }
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, rs.doRefactoring(true));
        }

        assertProblems(Arrays.asList(expectedProblems), problems);
    }

    private boolean typeTestPatternAvailable() {
        try {
            Class.forName("com.sun.source.tree.BindingPatternTree", false, Tree.class.getClassLoader()).getDeclaredMethod("getVariable");
            return true;
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException ex) {
            //OK
            return false;
        }
    }
}
