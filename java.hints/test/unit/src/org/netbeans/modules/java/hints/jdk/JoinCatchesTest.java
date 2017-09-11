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
public class JoinCatchesTest extends NbTestCase {

    public JoinCatchesTest(String name) {
        super(name);
    }

    public void testHint() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        try {\n" +
                       "        } catch (java.net.URISyntaxException m) {\n" +
                       "            m.printStackTrace();\n" +
                       "        } catch (java.io.IOException i) {\n" +
                       "            i.printStackTrace();\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n", false)
                .sourceLevel("1.7")
                .run(JoinCatches.class)
                .findWarning("4:26-4:44:verifier:ERR_JoinCatches")
                .applyFix("FIX_JoinCatches")
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    {\n" +
                              "        try {\n" +
                              "        } catch (java.net.URISyntaxException | java.io.IOException m) {\n" +
                              "            m.printStackTrace();\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void test192793() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        try (java.io.InputStream in = new java.io.FileInputStream(\"a\")){\n" +
                       "        } catch (final java.net.URISyntaxException m) {\n" +
                       "            m.printStackTrace();\n" +
                       "        } catch (final java.io.IOException i) {\n" + //XXX: final-ness should not ideally matter while searching for duplicates
                       "            i.printStackTrace();\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n", false)
                .sourceLevel("1.7")
                .run(JoinCatches.class)
                .findWarning("4:32-4:50:verifier:ERR_JoinCatches")
                .applyFix("FIX_JoinCatches")
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    {\n" +
                              "        try (java.io.InputStream in = new java.io.FileInputStream(\"a\")){\n" +
                              "        } catch (final java.net.URISyntaxException | java.io.IOException m) {\n" +
                              "            m.printStackTrace();\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testNeg() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        try {\n" +
                       "        } catch (java.net.URISyntaxException m) {\n" +
                       "            m.printStackTrace();\n" +
                       "            m = new java.io.IOException();\n" +
                       "        } catch (java.io.IOException i) {\n" +
                       "            i.printStackTrace();" +
                       "            i = new java.io.IOException();\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n", false)
                .sourceLevel("1.7")
                .run(JoinCatches.class)
                .assertWarnings();
    }

    public void test200707() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "import java.io.*;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        try (java.io.InputStream in = new java.io.FileInputStream(\"a\")){\n" +
                       "        } catch (UnknownHostException m) {\n" +
                       "            m.printStackTrace();\n" +
                       "        } catch (IOException i) {\n" +
                       "            i.printStackTrace();\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n", false)
                .sourceLevel("1.7")
                .run(JoinCatches.class)
                .assertWarnings();
    }

    public void test203139() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "import java.io.*;\n" +
                       "public class Test {\n" +
                       "    void t() throws Exception {\n" +
                       "        try {\n" +
                       "            throw new Exception();\n" +
                       "        } catch (InterruptedException e) {\n" +
                       "            System.err.println(e);\n" +
                       "        } catch (java.util.concurrent.ExecutionException e) {\n" +
                       "            System.err.println(e.getCause());\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(JoinCatches.class)
                .assertWarnings();
    }

    public void test205167() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "import java.util.concurrent.*;\n" +
                       "public class Test {\n" +
                       "    public void taragui() {\n" +
                       "        try {\n" +
                       "            return -1;\n" +
                       "        } catch (InterruptedException | TimeoutException e) {\n" +
                       "            return -1;\n" +
                       "        } catch (ExecutionException e) {\n" +
                       "            return -1;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n", false)
                .sourceLevel("1.7")
                .run(JoinCatches.class)
                .findWarning("6:17-6:56:verifier:ERR_JoinCatches")
                .applyFix("FIX_JoinCatches")
                .assertOutput("package test;\n" +
                              "import java.util.concurrent.*;\n" +
                              "public class Test {\n" +
                              "    public void taragui() {\n" +
                              "        try {\n" +
                              "            return -1;\n" +
                              "        } catch (InterruptedException | TimeoutException | ExecutionException e) {\n" +
                              "            return -1;\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    public void test215637() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "import java.util.concurrent.*;\n" +
                       "public class Test {\n" +
                       "    public void taragui() {\n" +
                       "        try {\n" +
                       "            throw new NullPointerException();\n" +
                       "        } catch(Error | NullPointerException  e) {\n" +
                       "        } catch(Exception e) {\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n", false)
                .sourceLevel("1.7")
                .run(JoinCatches.class)
                .assertWarnings();
    }
    public void test219636() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "import java.util.concurrent.*;\n" +
                       "public class Test {\n" +
                       "    public void taragui() {\n" +
                       "        try {\n" +
                       "            throw new Exception();\n" +
                       "        } catch (RuntimeException | IOException ex) {\n" +
                       "        } catch (Exception ex) {\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n", false)
                .sourceLevel("1.7")
                .run(JoinCatches.class)
                .assertWarnings();
    }

    public void test234085() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.util.concurrent.*;\n" +
                       "public class Test {\n" +
                       "    public void taragui() {\n" +
                       "        try {\n" +
                       "            Class.forName(\"Object\").newInstance();\n" +
                       "        } catch (IllegalAccessException ex) {\n" +
                       "            System.out.println(\"\");\n" +
                       "        } catch (ClassNotFoundException ex) {\n" +
                       "        } catch (InstantiationException ex) {\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n", false)
                .sourceLevel("1.7")
                .run(JoinCatches.class)
                .findWarning("8:17-8:39:verifier:ERR_JoinCatches")
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.concurrent.*;\n" +
                              "public class Test {\n" +
                              "    public void taragui() {\n" +
                              "        try {\n" +
                              "            Class.forName(\"Object\").newInstance();\n" +
                              "        } catch (IllegalAccessException ex) {\n" +
                              "            System.out.println(\"\");\n" +
                              "        } catch (ClassNotFoundException | InstantiationException ex) {\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
}