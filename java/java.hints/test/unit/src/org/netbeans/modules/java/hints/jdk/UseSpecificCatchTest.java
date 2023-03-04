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
