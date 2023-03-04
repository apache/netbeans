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
package org.netbeans.modules.java.hints.encapsulation;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author Tomas Zezula
 */
public class ReturnEncapsulationTest extends NbTestCase {

    public ReturnEncapsulationTest(final String name) {
        super(name);
    }

    public void testReturnCollectionField() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.List l;\n" +
                       "    public java.util.List getList() {\n" +
                       "        return l;\n" +
                       "    }\n" +
                       "}")
                .run(ReturnEncapsulation.class)
                .assertWarnings("4:8-4:17:verifier:Return of Collection Field");
    }

    public void testReturnCollectionLocal() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public java.util.List getList() {\n" +
                       "        java.util.List l = null;\n" +
                       "        return l;\n" +
                       "    }\n" +
                       "}")
                .run(ReturnEncapsulation.class)
                .assertWarnings();
    }

    public void testReturnCollectionFromOtherClass() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public java.util.List getList() {\n" +
                       "        return java.util.Collections.EMPTY_LIST;\n" +
                       "    }\n" +
                       "}")
                .run(ReturnEncapsulation.class)
                .assertWarnings();
    }

    public void testReturnUnmodifiableCollectionField195557() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private final java.util.List l = java.util.Arrays.asList(1);\n" +
                       "    public java.util.List getList() {\n" +
                       "        return l;\n" +
                       "    }\n" +
                       "}")
                .run(ReturnEncapsulation.class)
                .assertWarnings();
    }

    public void testReturnArrayField() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private int[] l;\n" +
                       "    public int[] getArry() {\n" +
                       "        return l;\n" +
                       "    }\n" +
                       "}")
                .run(ReturnEncapsulation.class)
                .assertWarnings("4:8-4:17:verifier:Return of Array Field");
    }

    public void testReturnArrayLocal() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public int[] getArry() {\n" +
                       "        int[] l = null;\n" +
                       "        return l;\n" +
                       "    }\n" +
                       "}")
                .run(ReturnEncapsulation.class)
                .assertWarnings();
    }

    public void testReturnDateField() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.Date d;\n" +
                       "    public java.util.Date getDate() {\n" +
                       "        return d;\n" +
                       "    }\n" +
                       "}")
                .run(ReturnEncapsulation.class)
                .assertWarnings("4:8-4:17:verifier:Return of Date or Calendar Field");
    }

    public void testReturnDateLocal() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public java.util.Date getDate() {\n" +
                       "        java.util.Date d = null;\n" +
                       "        return d;\n" +
                       "    }\n" +
                       "}")
                .run(ReturnEncapsulation.class)
                .assertWarnings();
    }

    public void testReturnCalendarField() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.Calendar d;\n" +
                       "    public java.util.Calendar getDate() {\n" +
                       "        return d;\n" +
                       "    }\n" +
                       "}")
                .run(ReturnEncapsulation.class)
                .assertWarnings("4:8-4:17:verifier:Return of Date or Calendar Field");
    }

    public void testReturnCalendarLocal() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public java.util.Calendar getDate() {\n" +
                       "        java.util.Calendar d = null;\n" +
                       "        return d;\n" +
                       "    }\n" +
                       "}")
                .run(ReturnEncapsulation.class)
                .assertWarnings();
    }

    public void testReturnError() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private Foo d;\n" +
                       "    public java.util.Collection getCollection() {\n" +
                       "        return d;\n" +
                       "    }\n" +
                       "}", false)
                .run(ReturnEncapsulation.class)
                .assertWarnings();
    }

    public void testCollectionFix() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.Collection l;\n" +
                       "    public java.util.Collection getCollection() {\n" +
                       "        return l;\n" +
                       "    }\n" +
                       "}")
                .run(ReturnEncapsulation.class)
                .findWarning("4:8-4:17:verifier:Return of Collection Field")
                .applyFix("Replace with java.util.Collections.unmodifiableCollection(l)")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.Collections;\n" +
                              "public class Test {\n" +
                              "    private java.util.Collection l;\n" +
                              "    public java.util.Collection getCollection() {\n" +
                              "        return Collections.unmodifiableCollection(l);\n" +
                              "    }\n" +
                              "}");
    }

    public void testListFix() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.List l;\n" +
                       "    public java.util.List getCollection() {\n" +
                       "        return l;\n" +
                       "    }\n" +
                       "}")
                .run(ReturnEncapsulation.class)
                .findWarning("4:8-4:17:verifier:Return of Collection Field")
                .applyFix("Replace with java.util.Collections.unmodifiableList(l)")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.Collections;\n" +
                              "public class Test {\n" +
                              "    private java.util.List l;\n" +
                              "    public java.util.List getCollection() {\n" +
                              "        return Collections.unmodifiableList(l);\n" +
                              "    }\n" +
                              "}");
    }

    public void testSetFix() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.Set l;\n" +
                       "    public java.util.Set getCollection() {\n" +
                       "        return l;\n" +
                       "    }\n" +
                       "}")
                .run(ReturnEncapsulation.class)
                .findWarning("4:8-4:17:verifier:Return of Collection Field")
                .applyFix("Replace with java.util.Collections.unmodifiableSet(l)")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.Collections;\n" +
                              "public class Test {\n" +
                              "    private java.util.Set l;\n" +
                              "    public java.util.Set getCollection() {\n" +
                              "        return Collections.unmodifiableSet(l);\n" +
                              "    }\n" +
                              "}");
    }

    public void testMapFix() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.Map l;\n" +
                       "    public java.util.Map getCollection() {\n" +
                       "        return l;\n" +
                       "    }\n" +
                       "}")
                .run(ReturnEncapsulation.class)
                .findWarning("4:8-4:17:verifier:Return of Collection Field")
                .applyFix("Replace with java.util.Collections.unmodifiableMap(l)")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.Collections;\n" +
                              "public class Test {\n" +
                              "    private java.util.Map l;\n" +
                              "    public java.util.Map getCollection() {\n" +
                              "        return Collections.unmodifiableMap(l);\n" +
                              "    }\n" +
                              "}");
    }

    public void testSortedSetFix192445() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.SortedSet l;\n" +
                       "    public java.util.SortedSet getCollection() {\n" +
                       "        return l;\n" +
                       "    }\n" +
                       "}")
                .run(ReturnEncapsulation.class)
                .findWarning("4:8-4:17:verifier:Return of Collection Field")
                .applyFix("Replace with java.util.Collections.unmodifiableSortedSet(l)")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.Collections;\n" +
                              "public class Test {\n" +
                              "    private java.util.SortedSet l;\n" +
                              "    public java.util.SortedSet getCollection() {\n" +
                              "        return Collections.unmodifiableSortedSet(l);\n" +
                              "    }\n" +
                              "}");
    }
}
