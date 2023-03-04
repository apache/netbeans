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
package org.netbeans.modules.java.hints.suggestions;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class FlipOperandsTest extends NbTestCase {
    
    public FlipOperandsTest(String name) {
        super(name);
    }
    
    public void testSimple() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private boolean test(int i) {\n" +
                       "        return i =^= 0;\n" +
                       "    }\n" +
                       "}\n")
                .run(FlipOperands.class)
                .findWarning("3:18-3:18:hint:" + Bundle.DESC_FlipOperands())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private boolean test(int i) {\n" +
                              "        return 0 == i;\n" +
                              "    }\n" +
                              "}\n");
    }
}