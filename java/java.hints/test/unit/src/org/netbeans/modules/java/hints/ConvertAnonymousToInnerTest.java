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
package org.netbeans.modules.java.hints;

import com.sun.source.tree.NewClassTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.io.IOException;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.SourceUtilsTestUtil2;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class ConvertAnonymousToInnerTest extends NbTestCase {
    
    public ConvertAnonymousToInnerTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
    }

    private static final class FindNewClassTree extends ErrorAwareTreePathScanner<TreePath, Void> {
        @Override
        public TreePath visitNewClass(NewClassTree node, Void p) {
            return getCurrentPath();
        }

        @Override
        public TreePath reduce(TreePath r1, TreePath r2) {
            if (r1 == null)
                return r2;
            return r1;
        }
        
    }
    
    public void testSimpleConvert() throws Exception {
        performTest(
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        new Runnable() {\n" +
                "            public void run() {}\n" +
                "        };\n" +
                "    }\n" +
                "}\n",
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        new RunnableImpl();\n" +
                "    }\n" +
                "    private static class RunnableImpl implements Runnable {\n" +
                "        public RunnableImpl() {\n" +
                "        }\n" +
                "        public void run() {\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
    }

    public void testDetectLocalVars() throws Exception {
        performTest(
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        final int i = 0;\n"+
                "        new Runnable() {\n" +
                "            public void run() {\n" +
                "                System.err.println(i);\n" +
                "            }\n" +
                "        };\n" +
                "    }\n" +
                "}\n",
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        final int i = 0;\n"+
                "        new RunnableImpl(i) ;\n" +
                "    }\n" +
                "    private static class RunnableImpl implements Runnable {\n" +
                "        private final int i;\n" +
                "        public RunnableImpl(int i) {\n" +
                "            this.i = i;\n" +
                "        }\n" +
                "        public void run() {\n" +
                "            System.err.println(i);\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
    }

    public void testDetectLocalVars2() throws Exception {
        performTest(
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        final int i = 0;\n"+
                "        final String s = \"\";\n"+
                "        new Runnable() {\n" +
                "            public void run() {\n" +
                "                for (int cntr = 0; cntr < i; cntr++) {\n" +
                "                    System.err.println(s);\n" +
                "                }\n" +
                "            }\n" +
                "        };\n" +
                "    }\n" +
                "}\n",
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        final int i = 0;\n"+
                "        final String s = \"\";\n"+
                "        new RunnableImpl(i, s) ;\n" +
                "    }\n" +
                "    private static class RunnableImpl implements Runnable {\n" +
                "        private final int i;\n" +
                "        private final String s;\n" +
                "        public RunnableImpl(int i, String s) {\n" +
                "            this.i = i;\n" +
                "            this.s = s;\n" +
                "        }\n" +
                "        public void run() {\n" +
                "            for (int cntr = 0; cntr < i; cntr++) {\n" +
                "                System.err.println(s);\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
    }

    public void testNonStatic() throws Exception {
        performTest(
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    private int x;\n" +
                "    public void taragui() {\n" +
                "        new Runnable() {\n" +
                "            public void run() {\n" +
                "                System.err.println(x);\n" +
                "            }\n" +
                "        };\n" +
                "    }\n" +
                "}\n",
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    private int x;\n" +
                "    public void taragui() {\n" +
                "        new RunnableImpl();\n" +
                "    }\n" +
                "    private class RunnableImpl implements Runnable {\n" +
                "        public RunnableImpl() {\n" +
                "        }\n" +
                "        public void run() {\n" +
                "            System.err.println(x);\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
    }

    public void testConstructorWithParameters1() throws Exception {
        performTest(
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        new java.util.ArrayList(3) {};\n" +
                "    }\n" +
                "}\n",
                info -> ("package hierbas.del.litoral;\n\n" +
                         "import java.util.ArrayList;\n" +
                         "public class Test {\n" +
                         "    public void taragui() {\n" +
                         "        new ArrayListImpl(3);\n" +
                         "    }\n" +
                         "    private static class ArrayListImpl extends ArrayList {\n" +
                         "        public ArrayListImpl(int ${param}) {\n" +
                         "            super(${param});\n" +
                         "        }\n" +
                         "    }\n" +
                         "}\n").replace("${param}", getParameterName(info, "int")));
    }

    public void testConstructorWithParameters2() throws Exception {
        performTest(
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        int i = 3;\n" +
                "        new java.util.ArrayList(i) {};\n" +
                "    }\n" +
                "}\n",
                info -> ("package hierbas.del.litoral;\n\n" +
                         "import java.util.ArrayList;\n" +
                         "public class Test {\n" +
                         "    public void taragui() {\n" +
                         "        int i = 3;\n" +
                         "        new ArrayListImpl(i);\n" +
                         "    }\n" +
                         "    private static class ArrayListImpl extends ArrayList {\n" +
                         "        public ArrayListImpl(int ${param}) {\n" +
                         "            super(${param});\n" +
                         "        }\n" +
                         "    }\n" +
                         "}\n").replace("${param}", getParameterName(info, "int")));
    }

    public void testConstructorWithParameters3() throws Exception {
        performTest(
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    int i = 3;\n" +
                "    public void taragui() {\n" +
                "        new java.util.ArrayList(i) {};\n" +
                "    }\n" +
                "}\n",
                info -> ("package hierbas.del.litoral;\n\n" +
                         "import java.util.ArrayList;\n" +
                         "public class Test {\n" +
                         "    int i = 3;\n" +
                         "    public void taragui() {\n" +
                         "        new ArrayListImpl(i);\n" +
                         "    }\n" +
                         "    private static class ArrayListImpl extends ArrayList {\n" +
                         "        public ArrayListImpl(int ${param}) {\n" +
                         "            super(${param});\n" +
                         "        }\n" +
                         "    }\n" +
                         "}\n").replace("${param}", getParameterName(info, "int")));
    }

    public void testConstructorWithParameters4() throws Exception {
        performTest(
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    java.util.List<? extends CharSequence> l;\n" +
                "    public void taragui() {\n" +
                "        new java.util.ArrayList<CharSequence>(l) {};\n" +
                "    }\n" +
                "}\n",
                info -> ("package hierbas.del.litoral;\n\n" +
                         "import java.util.ArrayList;\n" +
                         "import java.util.Collection;\n" +
                         "public class Test {\n" +
                         "    java.util.List<? extends CharSequence> l;\n" +
                         "    public void taragui() {\n" +
                         "        new ArrayListImpl(l);\n" +
                         "    }\n" +
                         "    private static class ArrayListImpl extends ArrayList<CharSequence> {\n" +
                         "        public ArrayListImpl(Collection<? extends CharSequence> ${param}) {\n" +
                         "            super(${param});\n" +
                         "        }\n" +
                         "    }\n" +
                         "}\n").replace("${param}", getParameterName(info, "java.util.Collection<? extends E>")));
    }

    private String getParameterName(CompilationInfo info, String expectedType) {
        TypeElement arrayList = info.getElements().getTypeElement("java.util.ArrayList");
        for (ExecutableElement el : ElementFilter.constructorsIn(arrayList.getEnclosedElements())) {
            if (el.getParameters().size() == 1 && expectedType.equals(el.getParameters().get(0).asType().toString())) {
                return el.getParameters().get(0).getSimpleName().toString();
            }
        }
        throw new IllegalStateException("Didn't find the expected constructor");
    }

    public void testInnerClass1() throws Exception {
        performTest(
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        new X() {};\n" +
                "    }\n" +
                "    class X {}\n" +
                "}\n",
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        new XImpl();\n" +
                "    }\n" +
                "    class X {}\n" +
                "    private class XImpl extends X {\n" +
                "        public XImpl() {\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
    }

    public void testInnerClass2() throws Exception {
        performTest(
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        new Test() {};\n" +
                "    }\n" +
                "}\n",
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        new TestImpl();\n" +
                "    }\n" +
                "    private static class TestImpl extends Test {\n" +
                "        public TestImpl() {\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
    }

    public void testInnerClass3() throws Exception {
        performTest(
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        new X() {};\n" +
                "    }\n" +
                "    static class X {}\n" +
                "}\n",
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        new XImpl();\n" +
                "    }\n" +
                "    static class X {}\n" +
                "    private static class XImpl extends X {\n" +
                "        public XImpl() {\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
    }

    public void testInnerClass4() throws Exception {
        performTest(
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        new X() {};\n" +
                "    }\n" +
                "    interface X {}\n" +
                "}\n",
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        new XImpl();\n" +
                "    }\n" +
                "    interface X {}\n" +
                "    private static class XImpl implements X {\n" +
                "        public XImpl() {\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
    }

    public void test114079() throws Exception {
        performTest(
        "package hierbas.del.litoral;\n\n" +
        "public class TestClass {\n" +
        "    public void test() {\n" +
        "        new SuperClass() {\n" +
        "            public void method() {\n" +
        "                new SubClass() {\n" +
        "                };\n" +
        "            }\n" +
        "        };\n" +
        "    }\n" +
        "}\n" +
        "class SuperClass {\n" +
        "}\n" +
        "\n" +
        "class SubClass extends SuperClass {\n" +
        "}\n",
                "package hierbas.del.litoral;\n\n" +
        "public class TestClass {\n" +
        "    public void test() {\n" +
        "        new SuperClassImpl();\n" +
        "    }\n" +
        "    private static class SuperClassImpl extends SuperClass {\n" +
        "        public SuperClassImpl() {\n" +
        "        }\n" +
        "            public void method() {\n" +
        "                new SubClass() {\n" +
        "                };\n" +
        "            }\n" +
        "    }\n" +
        "}\n" +
        "class SuperClass {\n" +
        "}\n" +
        "\n" +
        "class SubClass extends SuperClass {\n" +
        "}\n");
    }

    public void test114737() throws Exception {
        performTest(
        "package hierbas.del.litoral;\n\n" +
        "public class TestClass {\n" +
        "    public void test() {\n" +
        "        new Runnable() {\n" +
        "            public void run() {\n" +
        "                test();\n" +
        "            }\n" +
        "        };\n" +
        "    }\n" +
        "    private void test() {}\n" +
        "}\n",
        "package hierbas.del.litoral;\n\n" +
        "public class TestClass {\n" +
        "    public void test() {\n" +
        "    new RunnableImpl();\n" +
        "    }\n" +
        "    private void test() {}\n" +
        "    private class RunnableImpl implements Runnable {\n" +
        "        public RunnableImpl() {\n" +
        "        }\n" +
        "        public void run() {\n" +
        "            test();\n" +
        "        }\n" +
        "    }\n" +
        "}\n");
    }

    public void test106801a() throws Exception {
        performTest(
        "package hierbas.del.litoral;\n\n" +
        "public class TestClass {\n" +
        "    public void test() {\n" +
        "        new Runnable() {\n" +
        "            public void run() {\n" +
        "            }\n" +
        "        };\n" +
        "    }\n" +
        "    private class RunnableImpl implements Runnable {\n" +
        "    }\n" +
        "}\n",
        "package hierbas.del.litoral;\n\n" +
        "public class TestClass {\n" +
        "    public void test() {\n" +
        "    new RunnableImpl1();\n" +
        "    }\n" +
        "    private class RunnableImpl implements Runnable {\n" +
        "    }\n" +
        "    private static class RunnableImpl1 implements Runnable {\n" +
        "        public RunnableImpl1() {\n" +
        "        }\n" +
        "        public void run() {\n" +
        "        }\n" +
        "    }\n" +
        "}\n");
    }

    public void test106801b() throws Exception {
        performTest(
        "package hierbas.del.litoral;\n\n" +
        "public class TestClass {\n" +
        "    public void test() {\n" +
        "        new Runnable() {\n" +
        "            public void run() {\n" +
        "            }\n" +
        "        };\n" +
        "    }\n" +
        "    private class RunnableImpl implements Runnable {\n" +
        "    }\n" +
        "    private class RunnableImpl1 implements Runnable {\n" +
        "    }\n" +
        "    private class RunnableImpl2 implements Runnable {\n" +
        "    }\n" +
        "}\n",
        "package hierbas.del.litoral;\n\n" +
        "public class TestClass {\n" +
        "    public void test() {\n" +
        "    new RunnableImpl3();\n" +
        "    }\n" +
        "    private class RunnableImpl implements Runnable {\n" +
        "    }\n" +
        "    private class RunnableImpl1 implements Runnable {\n" +
        "    }\n" +
        "    private class RunnableImpl2 implements Runnable {\n" +
        "    }\n" +
        "    private static class RunnableImpl3 implements Runnable {\n" +
        "        public RunnableImpl3() {\n" +
        "        }\n" +
        "        public void run() {\n" +
        "        }\n" +
        "    }\n" +
        "}\n");
    }

    public void test129413() throws Exception {
        performTest("package hierbas.del.litoral;\n\n" +
                    "public class TestClass {\n" +
                    "    public static void method() {\n" +
                    "        new AbstractClass(\"\",26) {\n" +
                    "            public String getInfo() {\n" +
                    "                return this.name;\n" +
                    "            }\n" +
                    "        };\n" +
                    "    }\n" +
                    "    private static abstract class AbstractClass {\n" +
                    "        protected String name;\n" +
                    "        public AbstractClass(String s, int i) {\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "package hierbas.del.litoral;\n\n" +
                    "public class TestClass {\n" +
                    "    public static void method() {\n" +
                    "        new AbstractClassImpl(\"\",26);\n" +
                    "    }\n" +
                    "    private static abstract class AbstractClass {\n" +
                    "        protected String name;\n" +
                    "        public AbstractClass(String s, int i) {\n" +
                    "        }\n" +
                    "    }\n" +
                    "    private static class AbstractClassImpl extends AbstractClass {\n" +
                    "        public AbstractClassImpl(String s, int i) {\n" +
                    "            super(s, i);\n" +
                    "        }\n" +
                    "        public String getInfo() {\n" +
                    "            return this.name;\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n");
    }

    public void test171386() throws Exception {
        performTest("package hierbas.del.litoral;\n\n" +
                    "public class TestClass {\n" +
                    "    public static void method() {\n" +
                    "        new Runnable() {\n" +
                    "            //aslkdjflakjdsflkjaskldfj\n" +
                    "            public void run() { //adsfadsfaasdf\n" +
                    "                //2\n" +
                    "                return ;\n" +
                    "                //alskjdflkasjdlfkasdf\n" +
                    "            }\n" +
                    "            //dddddddddddd\n" +
                    "        };\n" +
                    "    }\n" +
                    "}\n",
                    "package hierbas.del.litoral;\n\n" +
                    "public class TestClass {\n" +
                    "    public static void method() {\n" +
                    "        new RunnableImpl();\n" +
                    "    }\n" +
                    "    private static class RunnableImpl implements Runnable {\n" +
                    "        public RunnableImpl() {\n" +
                    "        }\n" +
                    "        //aslkdjflakjdsflkjaskldfj\n" +
                    "        public void run() { //adsfadsfaasdf\n" +
                    "            //2\n" +
                    "            return ;\n" +
                    "            //alskjdflkasjdlfkasdf\n" +
                    "        }\n" +
                    "        //dddddddddddd\n" +
                    "    }\n" +
                    "}\n");
    }

    public void test177186() throws Exception {
        performTest("package hierbas.del.litoral;\n\n" +
                    "public class TestClass {\n" +
                    "    public static void method(String str) {\n" +
                    "        new TestClass(str) {};\n" +
                    "    }\n" +
                    "}\n",
                    "package hierbas.del.litoral;\n\n" +
                    "public class TestClass {\n" +
                    "    public static void method(String str) {\n" +
                    "        new TestClassImpl(str);\n" +
                    "    }\n" +
                    "    private static class TestClassImpl extends TestClass {\n" +
                    "        public TestClassImpl(String str) {\n" +
		    "            super(str);\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n");
    }

    private void performTest(String test, String golden) throws Exception {
        performTest(test, info -> golden);
    }

    private void performTest(String test, Function<CompilationInfo, String> goldenGenerator) throws Exception {
        clearWorkDir();

        FileUtil.refreshFor(getWorkDir());
        
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject src = FileUtil.createFolder(wd, "src");
        FileObject build = FileUtil.createFolder(wd, "build");
        FileObject cache = FileUtil.createFolder(wd, "cache");
        
        SourceUtilsTestUtil.prepareTest(src, build, cache);

        FileObject testFile = FileUtil.createData(src, "Test.java");

        TestUtilities.copyStringToFile(testFile, test);
        
        JavaSource testSource = JavaSource.forFileObject(testFile);
        String[] golden = new String[1];
        Task task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                
                golden[0] = goldenGenerator.apply(workingCopy);
                TreePath nct = new FindNewClassTree().scan(workingCopy.getCompilationUnit(), null);
                
                ConvertAnonymousToInner.convertAnonymousToInner(workingCopy, nct);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(FileUtil.toFile(testFile));
//        System.err.println(res);
        assertEquals(removeWhitespaces(golden[0]), removeWhitespaces(res));
    }
    
    private static String removeWhitespaces(String text) {
        return text.replace(" ", "").replace("\n", "");
    }

    public void test179766a() throws Exception {
        performEnabledTest("package hierbas.del.litoral;\n\n" +
                           "public class TestClass {\n" +
                           "    public static void method(String str) {\n" +
                           "        |new TestClass(str) {}|;\n" +
                           "    }\n" +
                           "}\n",
                           false,
                           true);
    }

    public void test179766b() throws Exception {
        performEnabledTest("package hierbas.del.litoral;\n\n" +
                           "public class TestClass {\n" +
                           "    public static void method(String str) {\n" +
                           "        |new TestClass(str) {};\n" +
                           "    }\n" +
                           "}\n",
                           false,
                           true);
    }

    public void test179766c() throws Exception {
        performEnabledTest("package hierbas.del.litoral;\n\n" +
                           "public class TestClass {\n" +
                           "    public static void method(String str) {\n" +
                           "        new TestClass(str) {}|;\n" +
                           "    }\n" +
                           "}\n",
                           false,
                           true);
    }

    public void test179766d() throws Exception {
        performEnabledTest("package hierbas.del.litoral;\n\n" +
                           "public class TestClass {\n" +
                           "    public static void method(String str) {\n" +
                           "        new TestClass(str) {|};\n" +
                           "    }\n" +
                           "}\n",
                           true,
                           false);
    }

    private void performEnabledTest(String test, final boolean onlyHeader, final boolean golden) throws Exception {
        clearWorkDir();

        String[] parts = test.split(Pattern.quote("|"));
        final int start, end;

        if (parts.length == 2) {
            start = end = parts[0].length();
            test = parts[0] + parts[1];
        } else {
            start = parts[0].length();
            end   = start + parts[1].length();
            test = parts[0] + parts[1] + parts[2];
        }

        FileUtil.refreshFor(getWorkDir());

        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject src = FileUtil.createFolder(wd, "src");
        FileObject build = FileUtil.createFolder(wd, "build");
        FileObject cache = FileUtil.createFolder(wd, "cache");

        SourceUtilsTestUtil.prepareTest(src, build, cache);

        FileObject testFile = FileUtil.createData(src, "Test.java");

        TestUtilities.copyStringToFile(testFile, test);

        JavaSource testSource = JavaSource.forFileObject(testFile);
        Task task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);

                assertEquals(golden, ConvertAnonymousToInner.computeFix(workingCopy, start, end, onlyHeader) != null);
            }

        };
        testSource.runModificationTask(task).commit();
    }
}
