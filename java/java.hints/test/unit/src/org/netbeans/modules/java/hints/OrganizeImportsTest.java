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
package org.netbeans.modules.java.hints;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class OrganizeImportsTest extends NbTestCase {
    
    public OrganizeImportsTest(String name) {
        super(name);
    }
    
    public void testSimple() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.util.List;\n" +
                       "import java.util.ArrayList;\n" +
                       "public class Test {\n" +
                       "     List l = new ArrayList();\n" +
                       "}\n")
                .run(OrganizeImports.class)
                .findWarning("1:0-1:22:verifier:MSG_OragnizeImports")
                .applyFix()
                .assertOutput("package test;\n" +
                              "import java.util.ArrayList;\n" +
                              "import java.util.List;\n" +
                              "public class Test {\n" +
                              "     List l = new ArrayList();\n" +
                              "}\n");
    }

    public void testClashing() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.awt.*;\n" +
                       "import java.util.List;\n" +
                       "import java.util.*;\n" +
                       "public class Test {\n" +
                       "     List l = new ArrayList();\n" +
                       "     Button b;\n" +
                       "}\n")
                .run(OrganizeImports.class)
                .findWarning("2:0-2:22:verifier:MSG_OragnizeImports")
                .applyFix()
                .assertOutput("package test;\n" +
                              "import java.awt.*;\n" +
                              "import java.util.*;\n" +
                              "import java.util.List;\n" +
                              "public class Test {\n" +
                              "     List l = new ArrayList();\n" +
                              "     Button b;\n" +
                              "}\n");
    }
}
