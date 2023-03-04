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
import static org.netbeans.modules.java.hints.bugs.Bundle.*;

/**
 *
 * @author
 * sdedic
 */
public class TryCatchFinallyTest extends NbTestCase {

    public TryCatchFinallyTest(String name) {
        super(name);
    }
    
    /**
     * Checks that the pattern matches try - finally without catch.
     * All break, continue and return should be reported
     */
    public void testTryFinallyReturn() throws Exception {
        HintTest.create().input(
            "package test;\n" +
            "public class Test {\n" +
            "    public void testTryFinallyReturn() {\n" +
            "        while (true) {\n" +
            "            try {\n" +
            "            } finally {\n" +
            "                if (Boolean.getBoolean(\"e\")) {\n" +
            "                    break;\n" +
            "                } else if (Boolean.getBoolean(\"e\")) {\n" +
            "                    continue;\n" +
            "                }\n" +
            "                return;\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}"
        ).run(TryCatchFinally.class).assertWarnings(
            "7:20-7:26:verifier:" + TEXT_returnBreakContinueInFinallyBlock("break"),
            "9:20-9:29:verifier:" + TEXT_returnBreakContinueInFinallyBlock("continue"),
            "11:16-11:23:verifier:" + TEXT_returnBreakContinueInFinallyBlock("return")
        );
    }
    
    /**
     * Checks that the pattern matches try - catch - finally.
     * break and continue escape from finally block so they must be reported.
     */
    public void testTryCatch1FinallyReturn() throws Exception {
        HintTest.create().input(
            "package test;\n" +
            "public class Test {\n" +
            "public void testTryCatch1FinallyReturn() {\n" +
            "        L: while (true) {\n" +
            "            try {\n" +
            "            } catch (IllegalArgumentException ex) {\n" +
            "            } finally {\n" +
            "                do {\n" +
            "                    if (Boolean.getBoolean(\"e\")) {\n" +
            "                        break L;\n" +
            "                    } else if (Boolean.getBoolean(\"e\")) {\n" +
            "                        continue L;\n" +
            "                    }\n" +
            "                } while (false);\n" +
            "                return;\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}"
        ).run(TryCatchFinally.class).assertWarnings(
            "9:24-9:32:verifier:" + TEXT_returnBreakContinueInFinallyBlock("break"),
            "11:24-11:35:verifier:" + TEXT_returnBreakContinueInFinallyBlock("continue"),
            "14:16-14:23:verifier:" + TEXT_returnBreakContinueInFinallyBlock("return")
        );
    }
    
    /**
     * Just checks that the pattern matches even multiple catch clauses.
     */
    public void testTryCatch2FinallyReturn() throws Exception {
        HintTest.create().input(
            "package test;\n" +
            "public class Test {\n" +
            "    public void testTryCatch2FinallyReturn() {\n" +
            "        try {\n" +
            "        } catch (IllegalArgumentException ex) {\n" +
            "        } catch (IllegalStateException ex) {\n" +
            "        } finally {\n" +
            "            return;\n" +
            "        }\n" +
            "    }\n" +
            "}"
        ).run(TryCatchFinally.class).assertWarnings(
            "7:12-7:19:verifier:" + TEXT_returnBreakContinueInFinallyBlock("return")
        );
    }

    /**
     * Break and continue break just the inner loop, do not escape outside of finally.
     * They must not be reported.
     */
    public void testBreakContinueInFinallyOK() throws Exception {
        HintTest.create().input(
            "package test;\n" +
            "public class Test {\n" +
            "    public void testBreakContinueInFinallyOK() {\n" +
            "        L: while (true) {\n" +
            "            try {\n" +
            "            } catch (IllegalArgumentException ex) {\n" +
            "            } finally {\n" +
            "                L2: while (true) {\n" +
            "                    do {\n" +
            "                        if (Boolean.getBoolean(\"e\")) {\n" +
            "                            break L2;\n" +
            "                        } else if (Boolean.getBoolean(\"e\")) {\n" +
            "                            continue L2;\n" +
            "                        }\n" +
            "                    } while (false);\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}"
        ).run(TryCatchFinally.class).assertWarnings();
    }

    /**
     * Checks that throw in a finally block is reported.
     */
    public void testThrowInFinally() throws Exception {
        HintTest.create().input(
            "package test;\n" +
            "public class Test {\n" +
            "    public void testThrowInFinally() {\n" +
            "        try {\n" +
            "            \n" +
            "        } finally {\n" +
            "            throw new IllegalStateException();\n" +
            "        }\n" +
            "    }\n" +
            "}"
        ).run(TryCatchFinally.class).assertWarnings(
            "6:12-6:46:verifier:" + TEXT_throwsInFinallyBlock()
        );
    }

    /**
     * Checks that throw ISE which is catched within finally block is not reported.
     * The throw IAE must be reported - it's not catched and completes the finally
     * block abruptly.
     */
    public void testThrowInFinallyWithCatch() throws Exception {
        HintTest.create().input(
            "package test;\n" +
            "public class Test {\n" +
            "    public void testThrowInFinallyWithCatch() {\n" +
            "        try {\n" +
            "            \n" +
            "        } finally {\n" +
            "            try {\n" +
            "                if (Boolean.getBoolean(\"e\")) {\n" +
            "                    throw new IllegalArgumentException();\n" +
            "                }\n" +
            "                throw new IllegalStateException();\n" +
            "            } catch (IllegalStateException ex) {\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}"
        ).run(TryCatchFinally.class).assertWarnings(
            "8:20-8:57:verifier:" + TEXT_throwsInFinallyBlock()
        );
    }

}
