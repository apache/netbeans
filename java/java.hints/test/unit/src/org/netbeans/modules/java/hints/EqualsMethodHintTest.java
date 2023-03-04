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

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author Jan Lahoda
 */
public class EqualsMethodHintTest extends NbTestCase {

    public EqualsMethodHintTest(String testName) {
        super(testName);
    }

    public void testSimple1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public boolean equals(Object o) {\n" +
                       "        return true;" +
                       "    }" +
                       "}\n")
                .run(EqualsMethodHint.class)
                .assertWarnings("2:19-2:25:verifier:ENC");

    }

    public void testSimple2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public boolean equals(String s) {\n" +
                       "        return true;" +
                       "    }" +
                       "}\n")
                .run(EqualsMethodHint.class)
                .assertWarnings();

    }

    public void testSimple3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public boolean equals(Object o) {\n" +
                       "        return o instanceof Test;" +
                       "    }" +
                       "}\n")
                .run(EqualsMethodHint.class)
                .assertWarnings();

    }

    public void testSimple4() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public boolean equals(Object o) {\n" +
                       "        return o.getClass() == Test.class;" +
                       "    }" +
                       "}\n")
                .run(EqualsMethodHint.class)
                .assertWarnings();

    }

    public void test134255() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public boolean equals(Object o);\n" +
                       "}\n", false)
                .run(EqualsMethodHint.class)
                .assertWarnings();

    }

    public void testAnnotations() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    @SuppressWarnings(\"a\") public boolean equals(Object o) {\n" +
                       "        return true;" +
                       "    }" +
                       "}\n")
                .run(EqualsMethodHint.class)
                .assertWarnings("2:42-2:48:verifier:ENC");

    }
    
    public void testClassIsInstance216498() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public boolean equals(Object o) {\n" +
                       "        if (!getClass().isInstance(o)) return false;" +
                       "        return true;" +
                       "    }" +
                       "}\n")
                .run(EqualsMethodHint.class)
                .assertWarnings();

    }
}
