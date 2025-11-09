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

import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class AddCastTest extends ErrorHintsTestBase {
    
    public AddCastTest(String testName) {
        super(testName, AddCast.class);
    }

    public void test117868() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {private void test() {int length = 0; byte b = |length & 0xFF00; } }",
                       "[AddCastFix:...length&0xFF00:byte]",
                       "package test; public class Test {private void test() {int length = 0; byte b = (byte) (length & 0xFF00); } }");
    }
    
    public void test118284() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {private Object[][] o; private String test() {return |o[0][0];} }",
                       "[AddCastFix:...o[][]:String]",
                       "package test; public class Test {private Object[][] o; private String test() {return (String) o[0][0];} }");
    }

    public void testMethodInvocation1() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {private void test() {Object o = null; |x(o);} private void x(String s) {}}",
                       "[AddCastFix:...o:String]",
                       "package test; public class Test {private void test() {Object o = null; x((String) o);} private void x(String s) {}}");
    }
    
    public void testMethodInvocation2() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {private void test() {java.util.List<String> l = null; Object o = null; |l.add(o);} }",
                       "[AddCastFix:...o:String]",
                       "package test; public class Test {private void test() {java.util.List<String> l = null; Object o = null; l.add((String) o);} }");
    }
    
    public void testNewClass1() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {private static void test() {Object o = null; |new Test(o);} public Test(String s) {} }",
                       "[AddCastFix:...o:String]",
                       "package test; public class Test {private static void test() {Object o = null; new Test((String) o);} public Test(String s) {} }");
    }
    
    public void test132639() throws Exception {
        performFixTest("test/Test.java",
                       "package test; import javax.swing.JComponent; public class Test {private static void test() {Class<? extends JComponent> c; c = |Class.forName(\"java.swingx.JLabel\");}}",
                       "[AddCastFix:...forName(...):Class<? extends JComponent>]",
                       "package test; import javax.swing.JComponent; public class Test {private static void test() {Class<? extends JComponent> c; c = (Class<? extends JComponent>) Class.forName(\"java.swingx.JLabel\");}}");
    }
    
    public void test133392() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; public class Test {private static void test() {Unknown u |= }}");
    }

    public void test136313() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; import java.util.ArrayList; public class Test {int[] convert(ArrayList<Integer> l) {Integer[] ex = {}; return |l.toArray(ex);}}");
    }

    public void test145220() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; public class Test {void foo() { null = new |String(\"hello\"); }}");
    }

    public void test161450() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; import java.io.File; public class Test {void foo() { File f = new |File() }}");
    }

    public void test193668() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; import java.util.List; public class Test { public List t(List l) { return t(c|()); } }");
    }
    
    public void test193625a() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test { private void t(Object o) { java.util.List l = |o; } }",
                       "[AddCastFix:...o:List]",
                       "package test; public class Test { private void t(Object o) { java.util.List l = (java.util.List) o; } }");
    }
    
    public void test219142() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test { private void t(int i) { short s = (i & 0x07); } }",
                       -1,
                       "[AddCastFix:...i&0x07:short]",
                       "package test; public class Test { private void t(int i) { short s = (short) (i & 0x07); } }");
    }
    
    public void test220031() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test<T> { private T[] f = new Object[0]; }",
                       -1,
                       "[AddCastFix:...new Object[...]:T[]]",
                       "package test; public class Test<T> { private T[] f = (T[]) new Object[0]; }");
    }
    
    public void test222344() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test { static void C(B data) { C(new Object()); } class A<T> { public void method(T data) {} } class B extends A<Test> {} }",
                       -1,
                       "[AddCastFix:...new Object(...):B]",
                       "package test; public class Test { static void C(B data) { C((B) new Object()); } class A<T> { public void method(T data) {} } class B extends A<Test> {} }");
    }
    
    public void test223022() throws Exception {
        //#223022: position set manually intentionally (to check that a NPE is not thrown):
        performAnalysisTest("test/Test.java",
                            "package test; public class Test { static int C() { retur|n ; } }");
    }

    public void test214835a() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; public class Test { private static void x(long l) { t(l); } void t(byte b) {} void t(int i) {} void t(String s) {} }",
                            -1,
                            "[AddCastFix:...l:byte]",
                            "[AddCastFix:...l:int]");
    }
    
    public void test214835b() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test { private static void x(long l) { t(l); } void t(byte b) {} void t(int i) {} void t(String s) {} }",
                       -1,
                       "[AddCastFix:...l:byte]",
                       "package test; public class Test { private static void x(long l) { t((byte) l); } void t(byte b) {} void t(int i) {} void t(String s) {} }");
    }
    
    static {
        NbBundle.setBranding("test");
    }
    
}
