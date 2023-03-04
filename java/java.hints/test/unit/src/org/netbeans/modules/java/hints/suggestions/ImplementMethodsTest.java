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

import org.junit.Test;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class ImplementMethodsTest {
    
    public ImplementMethodsTest() {
    }
    
    @Test
    public void testSimple1() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public abstract class Test implements Runn|able {\n" +
                       "}\n")
                .run(ImplementMethods.class)
                .findWarning("1:42-1:42:verifier:" + Bundle.ERR_ImplementMethods("java.lang.Runnable"))
                .applyFix(Bundle.FIX_ImplementSuperTypeMethods("java.lang.Runnable"))
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public abstract class Test implements Runnable {\n" +
                              "    public void run() {\n" +
                              "        throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody\n" +
                              "    }\n" +
                              "}\n");
    }
}
