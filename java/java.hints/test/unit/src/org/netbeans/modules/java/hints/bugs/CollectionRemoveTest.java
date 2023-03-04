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
package org.netbeans.modules.java.hints.bugs;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class CollectionRemoveTest extends NbTestCase {

    public CollectionRemoveTest(String name) {
        super(name);
    }
    
    @Override
    protected int timeOut() {
        return 30000;
    }

    public void testSimple1() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.List<String> l = null;" +
                       "        l.remove(new Object());" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings("0:106-0:128:verifier:SC: java.util.Collection.remove, Object, String");
    }

    public void testSimple2() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.List<String> l = null;" +
                       "        l.contains(new Object());" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings("0:106-0:130:verifier:SC: java.util.Collection.contains, Object, String");
    }

    public void testSimple3() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.List<String> l = null;" +
                       "        l.remove(Integer.valueOf(1));" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings("0:106-0:134:verifier:SCIT: java.util.Collection.remove, Integer, String");
    }

    public void testSimple4() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.List<String> l = null;" +
                       "        l.contains(Integer.valueOf(1));" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings("0:106-0:136:verifier:SCIT: java.util.Collection.contains, Integer, String");
    }

    public void testSimple5() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.Map<String, Number> l = null;" +
                       "        l.containsKey(Integer.valueOf(1));" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings("0:113-0:146:verifier:SCIT: java.util.Map.containsKey, Integer, String");
    }

    public void testSimple6() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.Map<String, Number> l = null;" +
                       "        l.containsValue(\"\");" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings("0:113-0:132:verifier:SCIT: java.util.Map.containsValue, String, Number");
    }

    public void testExtends1() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test extends java.util.LinkedList<String> {" +
                       "    private void test () {" +
                       "        remove(new Object());" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings("0:103-0:123:verifier:SC: java.util.Collection.remove, Object, String");
    }

    public void testExtends2() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test extends java.util.LinkedList<String> {" +
                       "    private void test () {" +
                       "        new Runnable() {" +
                       "            public void run() {" +
                       "                remove(new Object());" +
                       "            }" +
                       "        };" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings("0:166-0:186:verifier:SC: java.util.Collection.remove, Object, String");
    }

    public void testBoxing1() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.List<Integer> l = null;" +
                       "        l.contains(1);" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings();
    }

    public void testBoxing2() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.List<String> l = null;" +
                       "        l.contains(1);" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings("0:106-0:119:verifier:SCIT: java.util.Collection.contains, int, String");
    }

    public void testExtendsWildcard() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.List<? extends String> l = null;" +
                       "        l.contains(\"\");" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings();
    }

    public void testExtendsWildcard2() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test (boolean b) {" +
                       "        test(get().contains(\"\"));\n" +
                       "    }\n" +
                       "    private java.util.List<? extends String> get() {return null;}\n" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings();
    }

    public void testSuperWildcard() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.List<? super String> l = null;" +
                       "        l.contains(\"\");" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings();
    }

    public void testWildcard() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.List<?> l = null;" +
                       "        l.contains(\"\");" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings();
    }

    public void testCollectionRemoveInteger() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.List<String> l = null;" +
                       "        l.remove(0);" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings();
    }

    public void testCollectionRemoveInteger2() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test {" +
                       "    private void test () {" +
                       "        java.util.List<String> l = null;" +
                       "        l.subList(0, 0).remove(0);" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings();
    }

    public void testInsideItsOverride196606() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "public class Test extends java.util.ArrayList<String> {" +
                       "    @Override public boolean remove(Object o) {" +
                       "        return super.remove(o);\n" +
                       "    }" +
                       "}")
                .run(CollectionRemove.class)
                .assertWarnings();
    }

    static {
        NbBundle
                .setBranding("test");
    }
}
