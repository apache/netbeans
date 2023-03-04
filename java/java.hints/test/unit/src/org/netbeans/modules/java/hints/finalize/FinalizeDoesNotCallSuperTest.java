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
package org.netbeans.modules.java.hints.finalize;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author Tomas Zezula
 */
public class FinalizeDoesNotCallSuperTest extends NbTestCase {

    public FinalizeDoesNotCallSuperTest(final String name) {
        super(name);
    }

    public void testFinalizeWithNoSuper() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    protected final void finalize() {\n" +
                       "    }\n" +
                       "}")
                .run(FinalizeDoesNotCallSuper.class)
                .assertWarnings("2:25-2:33:verifier:finalize() does not call super.finalize()");
    }

    public void testFinalizeWithSuper() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    protected final void finalize() throws Throwable {\n" +
                       "        super.finalize();\n" +
                       "    }\n" +
                       "}")
                .run(FinalizeDoesNotCallSuper.class)
                .assertWarnings();
    }

    public void testSuppressed() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "@SuppressWarnings(\"FinalizeDoesntCallSuperFinalize\")\n" +
                       "public class Test {\n" +
                       "    protected final void finalize() {\n" +
                       "    }\n" +
                       "}")
                .run(FinalizeDoesNotCallSuper.class)
                .assertWarnings();
    }

    public void testFix() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    protected final void finalize() throws Throwable {\n" +
                       "        int a = 10;" +
                       "    }\n" +
                       "}")
                .run(FinalizeDoesNotCallSuper.class)
                .findWarning("2:25-2:33:verifier:finalize() does not call super.finalize()")
                .applyFix("Add super.finalize()")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    protected final void finalize() throws Throwable {\n" +
                              "        try {" +
                              "            int a = 10;" +
                              "        } finally {" +
                              "            super.finalize();" +
                              "        }" +
                              "    }\n" +
                              "}");
    }

    public void testBroken185456() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    protected abstract void finalize();\n" +
                       "}", false)
                .run(FinalizeDoesNotCallSuper.class)
                .assertWarnings();
    }

    public void testUseTryBlockIfAvailable() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    protected final void finalize() throws Throwable {\n" +
                       "        try {" +
                       "            throw new IllegalStateException(\"\");" +
                       "        } catch (Exception ex) {" +
                       "        }" +
                       "    }\n" +
                       "}")
                .run(FinalizeDoesNotCallSuper.class)
                .findWarning("2:25-2:33:verifier:finalize() does not call super.finalize()")
                .applyFix("Add super.finalize()")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    protected final void finalize() throws Throwable {\n" +
                              "        try {" +
                              "            throw new IllegalStateException(\"\");" +
                              "        } catch (Exception ex) {" +
                              "        } finally {" +
                              "            super.finalize();" +
                              "        }" +
                              "    }\n" +
                              "}");
    }
    
    public void testDoNotChangeExistingFinally() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    protected final void finalize() throws Throwable {\n" +
                       "        try {\n" +
                       "            throw new IllegalStateException(\"\");\n" +
                       "        } catch (Exception ex) {\n" +
                       "        } finally {\n" +
                       "            System.err.println(\"\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}")
                .run(FinalizeDoesNotCallSuper.class)
                .findWarning("2:25-2:33:verifier:finalize() does not call super.finalize()")
                .applyFix("Add super.finalize()")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    protected final void finalize() throws Throwable {\n" +
                              "        try {" +
                              "            try {" +
                              "                throw new IllegalStateException(\"\");" +
                              "            } catch (Exception ex) {" +
                              "            } finally {" +
                              "                System.err.println(\"\");" +
                              "            }" +
                              "        } finally {" +
                              "            super.finalize();" +
                              "        }" +
                              "    }\n" +
                              "}");
    }
    
}
