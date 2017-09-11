/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
