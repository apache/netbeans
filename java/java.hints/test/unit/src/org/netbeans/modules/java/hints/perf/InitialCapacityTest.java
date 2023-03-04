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
package org.netbeans.modules.java.hints.perf;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class InitialCapacityTest extends NbTestCase {

    public InitialCapacityTest(String name) {
        super(name);
    }

    public void testCollections1() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.util.ArrayList;\n" +
                       "import java.util.List;\n" +
                       "import java.util.HashMap;\n" +
                       "import java.util.Map;\n" +
                       "public class Test {\n" +
                       "     private void test(Map m, List l) {\n" +
                       "         new HashMap();\n" +
                       "         new HashMap(m);\n" +
                       "         new HashMap(1);\n" +
                       "         new ArrayList();\n" +
                       "         new ArrayList(l);\n" +
                       "         new ArrayList(1);\n" +
                       "     }\n" +
                       "}\n")
                .run(InitialCapacity.class)
                .assertWarnings("6:9-6:22:verifier:ERR_InitialCapacity_collections",
                                "9:9-9:24:verifier:ERR_InitialCapacity_collections");
    }

    public void testCollections2() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.util.ArrayList;\n" +
                       "import java.util.HashMap;\n" +
                       "import java.util.List;\n" +
                       "import java.util.Map;\n" +
                       "public class Test {\n" +
                       "     private void test(Map m, List l) {\n" +
                       "         new HashMap<Object, Object>();\n" +
                       "         new HashMap<Object, Object>(m);\n" +
                       "         new HashMap<Object, Object>(1);\n" +
                       "         new ArrayList<Object>();\n" +
                       "         new ArrayList<Object>(l);\n" +
                       "         new ArrayList<Object>(1);\n" +
                       "     }\n" +
                       "}\n")
                .run(InitialCapacity.class)
                .assertWarnings("6:9-6:38:verifier:ERR_InitialCapacity_collections",
                                "9:9-9:32:verifier:ERR_InitialCapacity_collections");
    }
}
