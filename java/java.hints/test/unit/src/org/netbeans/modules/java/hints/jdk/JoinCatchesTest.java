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