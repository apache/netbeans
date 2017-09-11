/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.jdk;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class UseSpecificCatchTest extends NbTestCase {

    public UseSpecificCatchTest(String name) {
        super(name);
    }

    public void testHintPos() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        try {\n" +
                       "            if (true) throw new java.io.FileNotFoundException();\n" +
                       "            else      throw new java.net.MalformedURLException();\n" +
                       "        } catch (Throwable e) {\n" +
                       "            e.printStackTrace();\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(UseSpecificCatch.class)
                .findWarning("6:17-6:26:verifier:ERR_UseSpecificCatch")
                .applyFix("FIX_UseSpecificCatch")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.io.FileNotFoundException;\n" +
                              "import java.net.MalformedURLException;\n" +
                              "public class Test {\n" +
                              "    {\n" +
                              "        try {\n" +
                              "            if (true) throw new java.io.FileNotFoundException();\n" +
                              "            else      throw new java.net.MalformedURLException();\n" +
                              "        } catch (FileNotFoundException | MalformedURLException e) {\n" +
                              "            e.printStackTrace();\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testHintPosFinally() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        try {\n" +
                       "            if (true) throw new java.io.FileNotFoundException();\n" +
                       "            else      throw new java.net.MalformedURLException();\n" +
                       "        } catch (final Throwable e) {\n" +
                       "            e.printStackTrace();\n" +
                       "        } finally {\n" +
                       "            System.err.println(1);\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(UseSpecificCatch.class)
                .findWarning("6:23-6:32:verifier:ERR_UseSpecificCatch")
                .applyFix("FIX_UseSpecificCatch")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.io.FileNotFoundException;\n" +
                              "import java.net.MalformedURLException;\n" +
                              "public class Test {\n" +
                              "    {\n" +
                              "        try {\n" +
                              "            if (true) throw new java.io.FileNotFoundException();\n" +
                              "            else      throw new java.net.MalformedURLException();\n" +
                              "        } catch (final FileNotFoundException | MalformedURLException e) {\n" +
                              "            e.printStackTrace();\n" +
                              "        } finally {\n" +
                              "            System.err.println(1);\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testHintNeg() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        try {\n" +
                       "            if (true) throw new java.io.FileNotFoundException();\n" +
                       "            else      throw new Throwable();\n" +
                       "        } catch (Throwable e) {\n" +
                       "            e.printStackTrace();\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(UseSpecificCatch.class)
                .assertWarnings();
    }

    public void testNeg2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        try {\n" +
                       "            if (true) throw new java.io.FileNotFoundException();\n" +
                       "            else      throw new java.net.MalformedURLException();\n" +
                       "        } catch (Throwable e) {\n" +
                       "            e = new java.io.FileNotFoundException();\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(UseSpecificCatch.class)
                .assertWarnings();
    }

    public void testParametrizedTypeException() throws Exception {
        HintTest
                .create()
                .input("package test;\n"
                        + "\n"
                        + "public abstract class Test<X extends SecurityException> {\n"
                        + "\n"
                        + "    public abstract void foo() throws X, java.io.IOException;\n"
                        + "\n"
                        + "    public void example() {\n"
                        + "        try {\n"
                        + "            foo();\n"
                        + "        } catch (Exception ex) {\n"
                        + "            // do something\n"
                        + "        }\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.7")
                .run(UseSpecificCatch.class)
                .findWarning("9:17-9:26:verifier:ERR_UseSpecificCatch")
                .applyFix("FIX_UseSpecificCatch")
                .assertCompilable()
                .assertOutput("package test;\n"
                        + "\n"
                        + "import java.io.IOException;\n"
                        + "\n"
                        + "public abstract class Test<X extends SecurityException> {\n"
                        + "\n"
                        + "    public abstract void foo() throws X, java.io.IOException;\n"
                        + "\n"
                        + "    public void example() {\n"
                        + "        try {\n"
                        + "            foo();\n"
                        + "        } catch (IOException | SecurityException ex) {\n"
                        + "            // do something\n"
                        + "        }\n"
                        + "    }\n"
                        + "}");
    }

    public void testParametrizedTypeExceptionJDK6() throws Exception {
        HintTest
                .create()
                .input("package test;\n"
                        + "\n"
                        + "public abstract class Test<X extends SecurityException> {\n"
                        + "\n"
                        + "    public abstract void foo() throws X, java.io.IOException;\n"
                        + "\n"
                        + "    public void example() {\n"
                        + "        try {\n"
                        + "            foo();\n"
                        + "        } catch (Exception ex) {\n"
                        + "            // do something\n"
                        + "        }\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.6")
                .run(UseSpecificCatch.class)
                .findWarning("9:17-9:26:verifier:ERR_UseSpecificCatch")
                .applyFix("FIX_UseSpecificCatchSplit")
                .assertCompilable()
                .assertOutput("package test;\n"
                        + "\n"
                        + "import java.io.IOException;\n"
                        + "\n"
                        + "public abstract class Test<X extends SecurityException> {\n"
                        + "\n"
                        + "    public abstract void foo() throws X, java.io.IOException;\n"
                        + "\n"
                        + "    public void example() {\n"
                        + "        try {\n"
                        + "            foo();\n"
                        + "        } catch (IOException ex) { \n"
                        + "            // do something\n"
                        + "        } catch (SecurityException ex) {\n"
                        + "            // do something\n"
                        + "        }\n"
                        + "    }\n"
                        + "}");
    }
    
    public void testSuppressWarningsOnCatchVariable229740() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        try {\n" +
                       "            if (true) throw new java.io.FileNotFoundException();\n" +
                       "            else      throw new java.net.MalformedURLException();\n" +
                       "        } catch (@SuppressWarnings(\"" + UseSpecificCatch.SW_KEY + "\") Throwable e) {\n" +
                       "            e.printStackTrace();\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(UseSpecificCatch.class)
                .assertWarnings();
    }
}
