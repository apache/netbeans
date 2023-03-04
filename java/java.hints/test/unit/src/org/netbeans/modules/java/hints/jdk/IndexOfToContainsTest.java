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
package org.netbeans.modules.java.hints.jdk;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class IndexOfToContainsTest extends NbTestCase {
    
    public IndexOfToContainsTest(String name) {
        super(name);
    }
    
    public void testContainsForIndexOf1() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private boolean test(String str) {\n" +
                       "        return str.indexOf(\"sub\") == (-1);\n" +
                       "    }\n" +
                       "}\n")
                .run(IndexOfToContains.class)
                .findWarning("3:15-3:41:verifier:" + Bundle.FIX_containsForIndexOf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private boolean test(String str) {\n" +
                              "        return !str.contains(\"sub\");\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testContainsForIndexOfChar() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private boolean test(String str) {\n" +
                       "        return str.indexOf('s') == (-1);\n" +
                       "    }\n" +
                       "}\n")
                .run(IndexOfToContains.class)
                .assertWarnings();
    }
}
