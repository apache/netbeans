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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Jan Lahoda
 */
public class CreateMethodTest extends ErrorHintsTestBase {
    
    /** Creates a new instance of CreateElementTest */
    public CreateMethodTest(String name) {
        super(name, CreateElement.class);
    }

    @Override
    protected int timeOut() {
        return 30000;
    }

    public void testMoreMethods() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public void test() {test(1);}}", 103 - 48, "CreateMethodFix:test(int i)void:test.Test");
    }

    public void testConstructor() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public static void test() {new Test(1);}}", 114 - 48, "CreateConstructorFix:(int i):test.Test");
    }

    public void testNoCreateConstructorForNonExistingClass() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public static void test() {new NonExisting(1);}}", 114 - 48);
    }

    public void testFieldLike() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public void test() {Collections.emptyList();}}", 107 - 48);
    }

    public void testMemberSelect1() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public void test() {emptyList().doSomething();}}", 107 - 48, "CreateMethodFix:emptyList()java.lang.Object:test.Test");
    }

    public void testMemberSelect2() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public Test test() {test().doSomething();}}", 112 - 48, "CreateMethodFix:doSomething()void:test.Test");
    }

    public void testAssignment() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public void test() {int i = fff();}}", 110 - 48, "CreateMethodFix:fff()int:test.Test");
    }

    public void testNewInAnnonymousInnerclass() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public Test(){} public void test() {new Runnable() {public void run() {new Test(1);}}}}", 158 - 48, "CreateConstructorFix:(int i):test.Test");
    }

    public void testCreateMethodInInterface() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() {Int i = null; i.test(1);} public static interface Int{}}", 96 - 24,
                       "CreateMethodFix:test(int i)void:test.Test.Int",
                       "package test; public class Test {public void test() {Int i = null; i.test(1);} public static interface Int{ public void test(int i); }}");
    }

    public void testCreateMethod106255() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() {test2(null);}}", 82 - 25,
                       "CreateMethodFix:test2(java.lang.Object object)void:test.Test",
                       "package test; public class Test {public void test() {test2(null);} private void test2(Object object) { throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody } }");
    }

    public void testCreateMethod77038() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() {b(test2() ? true : false);} void t(boolean b){}}", 82 - 25,
                       "CreateMethodFix:test2()boolean:test.Test",
                       "package test; public class Test {public void test() {b(test2() ? true : false);} void t(boolean b){} private boolean test2() { throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody } }");
    }

    public void testCreateMethod82923() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public void test() {int i = 0; switch (i) {case 1: fff(); break;}}}", 134 - 48, "CreateMethodFix:fff()void:test.Test");
    }

    public void testCreateMethod82931() throws Exception {
        performFixTest("test/Test.java",
                       "package test; import java.util.Collection; public class Test {public static void test() {fff(getStrings());} private static Collection<String> getStrings() {return null;}}",
                       116 - 25,
                       "CreateMethodFix:fff(java.util.Collection<java.lang.String> strings)void:test.Test",
                       "package test; import java.util.Collection; public class Test {public static void test() {fff(getStrings());} private static Collection<String> getStrings() {return null;} private static void fff(Collection<String> strings) { throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody } }");
    }

    public void testCreateMethod74129() throws Exception {
        doRunIndexing = true;
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() {TopLevel.f|ff();}} class TopLevel {}",
                       "CreateMethodFix:fff()void:test.TopLevel",
                       "package test; public class Test {public void test() {TopLevel.fff();}} class TopLevel { static void fff() { throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody } }");
    }

    public void testCreateMethod76498() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public static class T extends Test {public void test() {super.fff();}}}",
                       122 - 25,
                       "CreateMethodFix:fff()void:test.Test",
                       "package test; public class Test { private void fff() { throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody } public static class T extends Test {public void test() {super.fff();}}}");
    }

    public void testCreateMethod75069() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test<T> {public void test() {this.fff();}}",
                       88 - 25,
                       "CreateMethodFix:fff()void:test.Test",
                       "package test; public class Test<T> {public void test() {this.fff();} private void fff() { throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody } }");
    }

    public void testCreateMethod119037() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {static {f|ff();}}",
                       "CreateMethodFix:fff()void:test.Test",
                       "package test; public class Test {static {fff();} private static void fff() { throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody } }");
    }

    public void testCreateMethodWithAnonymousParameter104820() throws Exception {
        performFixTest("test/Test.java",
                       "package test;public class Test {public static void method() {final Test ac = new Test();new Runnable() {public void run() {ac.a|ction(this);}};}}",
                       "CreateMethodFix:action(java.lang.Runnable aThis)void:test.Test",
                       "package test;public class Test {public static void method() {final Test ac = new Test();new Runnable() {public void run() {ac.action(this);}};} private void action(Runnable aThis) { throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody } }");
    }

    public void testCreateMethodWithEnumParam() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test { enum Paddle{UP, DOWN} public void foo() {f|ff(Paddle.UP);}}",
                       "CreateMethodFix:fff(test.Test.Paddle paddle)void:test.Test",
                       "package test; public class Test { private void fff(Paddle paddle) { throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody } enum Paddle{UP, DOWN} public void foo() {fff(Paddle.UP);}}");
    }

    public void testCreateMethodWithParamOfEnumType199793() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test { enum Paddle{UP, DOWN} public void foo(Paddle test) {f|ff(test);}}",
                       "CreateMethodFix:fff(test.Test.Paddle test)void:test.Test",
                       "package test; public class Test { private void fff(Paddle test) { throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody } enum Paddle{UP, DOWN} public void foo(Paddle test) {fff(test);}}");
    }

    public void test220582() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public void foo() {\n" +
                       "        String name = null, description = null;\n" +
                       "        if(!is|New(name) && isNew(description)){ // 1\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n",
                       "CreateMethodFix:isNew(java.lang.String name)boolean:test.Test",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public void foo() {\n" +
                        "        String name = null, description = null;\n" +
                        "        if(!isNew(name) && isNew(description)){ // 1\n" +
                        "        }\n" +
                        "    }\n" +
                        "    private boolean isNew(String name) {\n" +
                        "        throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \n\t\r]+", " "));
    }

    public void test223011a() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.lang.reflect.*;\n" +
                       "public class Test {\n" +
                       "    public void foo(Class<?> c) throws Exception {\n" +
                       "        Field f = m|ethod(c);\n" +
                       "    }\n" +
                       "}\n",
                       "CreateMethodFix:method(java.lang.Class<?> c)java.lang.reflect.Field:test.Test",
                       ("package test;\n" +
                        "import java.lang.reflect.*;\n" +
                        "public class Test {\n" +
                        "    public void foo(Class<?> c) throws Exception {\n" +
                        "        Field f = method(c);\n" +
                        "    }\n" +
                        "    private Field method(Class<?> c) {\n" +
                        "        throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \n\t\r]+", " "));
    }

    public void test223011b() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.lang.reflect.*;\n" +
                       "public class Test {\n" +
                       "    public <T extends Number&CharSequence, E extends Integer> void foo(Class<E> c1, Class<T> c2) throws Exception {\n" +
                       "        Field f = m|ethod(c1, c2);\n" +
                       "    }\n" +
                       "}\n",
                       "CreateMethodFix:method(java.lang.Class<E> c1,java.lang.Class<T> c2)java.lang.reflect.Field:test.Test",
                       ("package test;\n" +
                        "import java.lang.reflect.*;\n" +
                        "public class Test {\n" +
                        "    public <T extends Number&CharSequence, E extends Integer> void foo(Class<E> c1, Class<T> c2) throws Exception {\n" +
                        "        Field f = method(c1, c2);\n" +
                        "    }\n" +
                        "    private <T extends Number & CharSequence, E extends Integer> Field method(Class<E> c1, Class<T> c2) {\n" +
                        "        throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \n\t\r]+", " "));
    }

    public void test223011c() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.lang.reflect.*;\n" +
                       "public class Test {\n" +
                       "    public <T extends Number&CharSequence, E> void foo(Class<T> c) throws Exception {\n" +
                       "        Class<E> cr = m|ethod(c);\n" +
                       "    }\n" +
                       "}\n",
                       "CreateMethodFix:method(java.lang.Class<T> c)java.lang.Class<E>:test.Test",
                       ("package test;\n" +
                        "import java.lang.reflect.*;\n" +
                        "public class Test {\n" +
                        "    public <T extends Number&CharSequence, E> void foo(Class<T> c) throws Exception {\n" +
                        "        Class<E> cr = method(c);\n" +
                        "    }\n" +
                        "    private <T extends Number & CharSequence, E> Class<E> method(Class<T> c) {\n" +
                        "        throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \n\t\r]+", " "));
    }

    public void test203476() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import static test.Aux.getName;\n" +
                       "public class Test {\n" +
                       "    public void foo() {\n" +
                       "        getName(undefined());\n" +
                       "    }\n" +
                       "}\n" +
                       "class Aux {\n" +
                       "    public static void getName(String param) { }\n" +
                       "}\n",
                       -1,
                       "CreateMethodFix:undefined()java.lang.String:test.Test",
                       ("package test;\n" +
                        "import static test.Aux.getName;\n" +
                        "public class Test {\n" +
                        "    public void foo() {\n" +
                        "        getName(undefined());\n" +
                        "    }\n" +
                        "    private String undefined() {\n" +
                        "         throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody \n" +
                        "    }\n" +
                        "}\n" +
                        "class Aux {\n" +
                        "    public static void getName(String param) { }\n" +
                        "}\n").replaceAll("[ \n\t\r]+", " "));
    }

    public void test233502() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public static void foo(Number str) {\n" +
                       "        m(str);\n" +
                       "    }\n" +
                       "    public static void m(String str) {}\n" +
                       "}\n",
                       -1,
                       "CreateMethodFix:m(java.lang.Number str)void:test.Test",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public static void foo(Number str) {\n" +
                        "        m(str);\n" +
                        "    }\n" +
                        "    public static void m(String str) {}\n" +
                        "    private static void m(Number str) {\n" +
                        "         throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody \n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \n\t\r]+", " "));
    }

    public void testMethodRefInstanceRefToInstance() throws Exception {
        sourceLevel = "1.8";
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public void test(Runnable r) {\n" +
                       "        test(this::undef);\n" +
                       "    }\n" +
                       "}\n",
                       -1,
                       "CreateMethodFix:undef()void:test.Test",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public void test(Runnable r) {\n" +
                        "        test(this::undef);\n" +
                        "    }\n" +
                        "    private void undef() {\n" +
                        "         throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody \n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \n\t\r]+", " "));
    }

    public void testMethodRefStaticRefToStatic() throws Exception {
        sourceLevel = "1.8";
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public void test(Runnable r) {\n" +
                       "        test(Test::undef);\n" +
                       "    }\n" +
                       "}\n",
                       -1,
                       "CreateMethodFix:undef()void:test.Test",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    private static void undef() {\n" +
                        "         throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody \n" +
                        "    }\n" +
                        "    public void test(Runnable r) {\n" +
                        "        test(Test::undef);\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \n\t\r]+", " "));
    }

    public void testMethodRefStaticRefToInstance() throws Exception {
        sourceLevel = "1.8";
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public void test(I<Test> r) {\n" +
                       "        test(Test::undef);\n" +
                       "    }\n" +
                       "    public interface I<T> {\n" +
                       "        public void run(T t);\n" +
                       "        public default void run2() {}\n" +
                       "    }\n" +
                       "}\n",
                       -1,
                       "CreateMethodFix:undef()void:test.Test",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public void test(I<Test> r) {\n" +
                        "        test(Test::undef);\n" +
                        "    }\n" +
                        "    private void undef() {\n" +
                        "         throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody \n" +
                        "    }\n" +
                        "    public interface I<T> {\n" +
                        "        public void run(T t);\n" +
                        "        public default void run2() {}\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \n\t\r]+", " "));
    }

    public void testMethodRefStaticRefToStatic2() throws Exception {
        sourceLevel = "1.8";
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public void test(I<Test> r) {\n" +
                       "        test(Test::undef);\n" +
                       "    }\n" +
                       "    public interface I<T> {\n" +
                       "        public void run(T t);\n" +
                       "        public default void run2() {}\n" +
                       "    }\n" +
                       "}\n",
                       -1,
                       "CreateMethodFix:undef(test.Test t)void:test.Test",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    private static void undef(Test t) {\n" +
                        "         throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody \n" +
                        "    }\n" +
                        "    public void test(I<Test> r) {\n" +
                        "        test(Test::undef);\n" +
                        "    }\n" +
                        "    public interface I<T> {\n" +
                        "        public void run(T t);\n" +
                        "        public default void run2() {}\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \n\t\r]+", " "));
    }

    public void testMethodRefInstanceRefToInstance2() throws Exception {
        sourceLevel = "1.8";
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public void test(I<Test> r) {\n" +
                       "        test(this::undef);\n" +
                       "    }\n" +
                       "    public interface I<T> {\n" +
                       "        public void run(T t);\n" +
                       "        public default void run2() {}\n" +
                       "    }\n" +
                       "}\n",
                       -1,
                       "CreateMethodFix:undef(test.Test t)void:test.Test",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public void test(I<Test> r) {\n" +
                        "        test(this::undef);\n" +
                        "    }\n" +
                        "    private void undef(Test t) {\n" +
                        "         throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody \n" +
                        "    }\n" +
                        "    public interface I<T> {\n" +
                        "        public void run(T t);\n" +
                        "        public default void run2() {}\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \n\t\r]+", " "));
    }

    public void testErroneousMethodRef() throws Exception {
        sourceLevel = "1.8";
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public void test(I<Test> r) {\n" +
                            "        test(undef::undef);\n" +
                            "    }\n" +
                            "    public interface I<T> {\n" +
                            "        public void run(T t);\n" +
                            "        public default void run2() {}\n" +
                            "    }\n" +
                            "}\n",
                            -1);
    }

    @Override
    protected List<Fix> computeFixes(CompilationInfo info, String diagnosticCode, int pos, TreePath path) throws Exception {
        List<Fix> fixes = new CreateElement().analyze(info, diagnosticCode, pos);
        List<Fix> result=  new LinkedList<>();
        
        for (Fix f : fixes) {
            if (f instanceof CreateMethodFix)
                result.add(f);
        }
        
        return result;
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return ((CreateMethodFix) f).toDebugString(info);
    }
    
}
