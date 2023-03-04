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

/**
 *
 * @author lahvac
 */
public class CastVSInstanceOfTest extends NbTestCase {

    public CastVSInstanceOfTest(String name) {
        super(name);
    }

    public void testSimple1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "import java.util.List;\n" +
                       "public class Test {\n" +
                       "    private void test(Object o) {\n" +
                       "        if (o instanceof List) {\n" +
                       "            String str = (String) o;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(CastVSInstanceOf.class)
                .assertWarnings("5:26-5:32:verifier:CastVSInstanceOf");
    }

    public void testSimple2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "import java.util.Collection;\n" +
                       "import java.util.List;\n" +
                       "public class Test {\n" +
                       "    private void test(Object o) {\n" +
                       "        if (o instanceof List) {\n" +
                       "            Collection<String> str = (Collection<String>) o;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(CastVSInstanceOf.class)
                .assertWarnings();
    }

    public void testSimple3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "import java.util.List;\n" +
                       "public class Test {\n" +
                       "    private void test(Object o) {\n" +
                       "        if (o instanceof List) {\n" +
                       "            final String str = (String) o;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(CastVSInstanceOf.class)
                .assertWarnings("5:32-5:38:verifier:CastVSInstanceOf");
    }

    public void testSimple4() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "import java.util.List;\n" +
                       "public class Test {\n" +
                       "    private void test(String str, Object o) {\n" +
                       "        if (o instanceof List) {\n" +
                       "            str = (String) o;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(CastVSInstanceOf.class)
                .assertWarnings("5:19-5:25:verifier:CastVSInstanceOf");
    }
}