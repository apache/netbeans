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
public class ThrowableInitCauseTest extends NbTestCase {

    public ThrowableInitCauseTest(String name) {
        super(name);
    }

    public void testSimple1() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.IOException;" +
                       "import java.util.ArrayList;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         try {\n" +
                       "             throw new IOException(\"a\");\n" +
                       "         } catch (IOException e) {\n" +
                       "             throw (IllegalStateException) new IllegalStateException(e.toString()).initCause(e);\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .run(ThrowableInitCause.class)
                .findWarning("6:19-6:95:verifier:ERR_ThrowableInitCause")
                .applyFix("FIX_ThrowableInitCause")
                .assertCompilable()
                .assertOutput("package test;" +
                              "import java.io.IOException;" +
                              "import java.util.ArrayList;\n" +
                              "public class Test {\n" +
                              "     private void test() {\n" +
                              "         try {\n" +
                              "             throw new IOException(\"a\");\n" +
                              "         } catch (IOException e) {\n" +
                              "             throw new IllegalStateException(e);\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testSimple2() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.IOException;" +
                       "import java.util.ArrayList;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         try {\n" +
                       "             throw new IOException(\"a\");\n" +
                       "         } catch (IOException e) {\n" +
                       "             throw (IllegalStateException) new IllegalStateException(\"a\").initCause(e);\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .run(ThrowableInitCause.class)
                .findWarning("6:19-6:86:verifier:ERR_ThrowableInitCause")
                .applyFix("FIX_ThrowableInitCause")
                .assertCompilable()
                .assertOutput("package test;" +
                              "import java.io.IOException;" +
                              "import java.util.ArrayList;\n" +
                              "public class Test {\n" +
                              "     private void test() {\n" +
                              "         try {\n" +
                              "             throw new IOException(\"a\");\n" +
                              "         } catch (IOException e) {\n" +
                              "             throw new IllegalStateException(\"a\", e);\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testSimple3() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.IOException;" +
                       "import java.util.ArrayList;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         try {\n" +
                       "             throw new IOException(\"a\");\n" +
                       "         } catch (IOException e) {\n" +
                       "             IllegalStateException ex = new IllegalStateException(e.toString());\n" +
                       "             ex.initCause(e);" +
                       "             throw ex;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .run(ThrowableInitCause.class)
                .findWarning("6:13-6:80:verifier:ERR_ThrowableInitCause")
                .applyFix("FIX_ThrowableInitCause")
                .assertCompilable()
                .assertOutput("package test;" +
                              "import java.io.IOException;" +
                              "import java.util.ArrayList;\n" +
                              "public class Test {\n" +
                              "     private void test() {\n" +
                              "         try {\n" +
                              "             throw new IOException(\"a\");\n" +
                              "         } catch (IOException e) {\n" +
                              "             throw new IllegalStateException(e);\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testSimpleNoStringArgNotStrict() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.IOException;" +
                       "import java.util.ArrayList;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         try {\n" +
                       "             throw new IOException(\"a\");\n" +
                       "         } catch (IOException e) {\n" +
                       "             IllegalStateException ex = new IllegalStateException();\n" +
                       "             ex.initCause(e);" +
                       "             throw ex;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .preference(ThrowableInitCause.STRICT_KEY, false)
                .run(ThrowableInitCause.class)
                .findWarning("6:13-6:68:verifier:ERR_ThrowableInitCause")
                .applyFix("FIX_ThrowableInitCause")
                .assertCompilable()
                .assertOutput("package test;" +
                              "import java.io.IOException;" +
                              "import java.util.ArrayList;\n" +
                              "public class Test {\n" +
                              "     private void test() {\n" +
                              "         try {\n" +
                              "             throw new IOException(\"a\");\n" +
                              "         } catch (IOException e) {\n" +
                              "             throw new IllegalStateException(e);\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testSimpleNoStringArgNotStrict2() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.IOException;" +
                       "import java.util.ArrayList;\n" +
                       "public class Test {\n" +
                       "     private void test() throws IOException {\n" +
                       "         Exception[] blex = null;\n" +
                       "         IOException ioe = new IOException(blex[0].getMessage());\n" +
                       "         ioe.initCause(blex[0]);\n" +
                       "         throw ioe;\n" +
                       "     }\n" +
                       "}\n")
                .preference(ThrowableInitCause.STRICT_KEY, false)
                .run(ThrowableInitCause.class)
                .findWarning("4:9-4:65:verifier:ERR_ThrowableInitCause")
                .applyFix("FIX_ThrowableInitCause")
                .assertCompilable()
                .assertOutput("package test;" +
                              "import java.io.IOException;" +
                              "import java.util.ArrayList;\n" +
                              "public class Test {\n" +
                              "     private void test() throws IOException {\n" +
                              "         Exception[] blex = null;\n" +
                              "         throw new IOException(blex[0]);\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testSimpleNoStringArgStrict() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.IOException;" +
                       "import java.util.ArrayList;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         try {\n" +
                       "             throw new IOException(\"a\");\n" +
                       "         } catch (IOException e) {\n" +
                       "             IllegalStateException ex = new IllegalStateException();\n" +
                       "             ex.initCause(e);" +
                       "             throw ex;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .preference(ThrowableInitCause.STRICT_KEY, true)
                .run(ThrowableInitCause.class)
                .findWarning("6:13-6:68:verifier:ERR_ThrowableInitCause")
                .applyFix("FIX_ThrowableInitCause")
                .assertCompilable()
                .assertOutput("package test;" +
                              "import java.io.IOException;" +
                              "import java.util.ArrayList;\n" +
                              "public class Test {\n" +
                              "     private void test() {\n" +
                              "         try {\n" +
                              "             throw new IOException(\"a\");\n" +
                              "         } catch (IOException e) {\n" +
                              "             throw new IllegalStateException(null, e);\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testFinalVariable() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.IOException;" +
                       "import java.util.ArrayList;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         try {\n" +
                       "             throw new IOException(\"a\");\n" +
                       "         } catch (IOException e) {\n" +
                       "             final IllegalStateException ex = new IllegalStateException();\n" +
                       "             ex.initCause(e);" +
                       "             throw ex;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .preference(ThrowableInitCause.STRICT_KEY, true)
                .run(ThrowableInitCause.class)
                .findWarning("6:13-6:74:verifier:ERR_ThrowableInitCause")
                .applyFix("FIX_ThrowableInitCause")
                .assertCompilable()
                .assertOutput("package test;" +
                              "import java.io.IOException;" +
                              "import java.util.ArrayList;\n" +
                              "public class Test {\n" +
                              "     private void test() {\n" +
                              "         try {\n" +
                              "             throw new IOException(\"a\");\n" +
                              "         } catch (IOException e) {\n" +
                              "             throw new IllegalStateException(null, e);\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testExpression() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.IOException;" +
                       "import java.util.ArrayList;\n" +
                       "public class Test {\n" +
                       "     private Object test() {\n" +
                       "         try {\n" +
                       "             throw new IOException(\"a\");\n" +
                       "         } catch (IOException e) {\n" +
                       "             return (IllegalStateException) new IllegalStateException(e.toString()).initCause(e);\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .preference(ThrowableInitCause.STRICT_KEY, false)
                .run(ThrowableInitCause.class)
                .findWarning("6:20-6:96:verifier:ERR_ThrowableInitCause")
                .applyFix("FIX_ThrowableInitCause")
                .assertCompilable()
                .assertOutput("package test;" +
                              "import java.io.IOException;" +
                              "import java.util.ArrayList;\n" +
                              "public class Test {\n" +
                              "     private Object test() {\n" +
                              "         try {\n" +
                              "             throw new IOException(\"a\");\n" +
                              "         } catch (IOException e) {\n" +
                              "             return new IllegalStateException(e);\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }

    //TODO:
    public void XtestMoreStatementsNoThrow() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.IOException;" +
                       "import java.util.ArrayList;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         try {\n" +
                       "             throw new IOException(\"a\");\n" +
                       "         } catch (IOException e) {\n" +
                       "             IllegalStateException ex = new IllegalStateException(e.toString());\n" +
                       "             ex.initCause(e);" +
                       "             ex.printStackTrace();\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .run(ThrowableInitCause.class)
                .findWarning("TODO")
                .applyFix("FIX_ThrowableInitCause")
                .assertCompilable()
                .assertOutput("package test;" +
                              "import java.io.IOException;" +
                              "import java.util.ArrayList;\n" +
                              "public class Test {\n" +
                              "     private void test() {\n" +
                              "         try {\n" +
                              "             throw new IOException(\"a\");\n" +
                              "         } catch (IOException e) {\n" +
                              "             IllegalStateException ex = new IllegalStateException(e);\n" +
                              "             ex.printStackTrace();\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }
}