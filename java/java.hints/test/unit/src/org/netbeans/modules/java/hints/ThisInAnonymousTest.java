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
 * @author lahvac
 */
public class ThisInAnonymousTest extends NbTestCase {

    public ThisInAnonymousTest(String name) {
        super(name);
    }

    public void testSynchronized() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void m() {\n" +
                       "         new Runnable() {\n" +
                       "             public void run() {\n" +
                       "                 synchronized(this) {}\n" +
                       "             }\n" +
                       "         };\n" +
                       "     }\n" +
                       "}\n")
                .run(ThisInAnonymous.class)
                .findWarning("5:30-5:34:verifier:ERR_ThisInAnonymous")
                .applyFix("FIX_ThisInAnonymous")
                .assertCompilable()
                .assertOutput("package test; public class Test { private void m() { new Runnable() { public void run() { synchronized(Test.this) {} } }; } } ");
    }

    public void testSynchronized184382() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void m() {\n" +
                       "         new Runnable() {\n" +
                       "             public void run() {\n" +
                       "                 javax.swing.SwingUtilities.invokeLater(this);\n" +
                       "             }\n" +
                       "         };\n" +
                       "     }\n" +
                       "}\n")
                .run(ThisInAnonymous.class)
                .assertWarnings();
    }

    public void testLocalClass() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void m() {\n" +
                       "         class L implements Runnable {\n" +
                       "             public void run() {\n" +
                       "                 synchronized(this) {}\n" +
                       "             }\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .run(ThisInAnonymous.class)
                .findWarning("5:30-5:34:verifier:ERR_ThisInAnonymousLocal")
                .applyFix("FIX_ThisInAnonymous")
                .assertCompilable()
                .assertOutput("package test; public class Test { private void m() { class L implements Runnable { public void run() { synchronized(Test.this) {} } } } } ");
    }
}