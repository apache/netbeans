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
 * @author Tomas Zezula
 */
public class ConvertToARMTest extends NbTestCase {

    public ConvertToARMTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super
                .setUp();
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
    }

    public void testSimpleTryFinally() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         System.out.println(\"Start\");" +
                       "         final InputStream in = new FileInputStream(new File(\"a\"));" +
                       "         try {" +
                       "            in.read();" +
                       "         } finally {" +
                       "            in.close();" +
                       "         }" +
                       "         System.out.println(\"Done\");" +
                       "     }" +
                       "}")
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:216-0:218:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\"); try (InputStream in = new FileInputStream(new File(\"a\"))) { in.read(); } System.out.println(\"Done\"); }}");
    }

    public void testEFSimpleTryFinally() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "public class Test {" +
                       "     public void test(InputStream in) throws Exception {" +
                       "         System.out.println(\"Start\");" +
                       "         try {" +
                       "            in.read();" +
                       "         } finally {" +
                       "            in.close();" +
                       "         }" +
                       "         System.out.println(\"Done\");" +
                       "     }" +
                       "}")
                .sourceLevel("9")
                .run(ConvertToARM.class)
                .findWarning("0:178-0:180:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;public class Test { public void test(InputStream in) throws Exception { System.out.println(\"Start\"); try (in) { in.read(); } System.out.println(\"Done\"); }}");
    }

    public void testTryFinally() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         System.out.println(\"Start\");" +
                       "         InputStream in = new FileInputStream(new File(\"a\"));" +
                       "         try {" +
                       "            in.read();" +
                       "         } catch (Exception e) {" +
                       "            System.out.println(\"Ex\");" +
                       "         } finally {" +
                       "            in.close();" +
                       "            System.out.println(\"Fin\");" +
                       "         }" +
                       "         System.out.println(\"Done\");" +
                       "     }" +
                       "}")
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:210-0:212:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\"); try (InputStream in = new FileInputStream(new File(\"a\"))) { in.read(); } catch (Exception e) { System.out.println(\"Ex\"); } finally { System.out.println(\"Fin\"); } System.out.println(\"Done\"); }}");
    }

    public void testEFTryFinally() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "public class Test {" +
                       "     public void test(InputStream in) throws Exception {" +
                       "         System.out.println(\"Start\");" +
                       "         try {" +
                       "            in.read();" +
                       "         } catch (Exception e) {" +
                       "            System.out.println(\"Ex\");" +
                       "         } finally {" +
                       "            in.close();" +
                       "            System.out.println(\"Fin\");" +
                       "         }" +
                       "         System.out.println(\"Done\");" +
                       "     }" +
                       "}")
                .sourceLevel("9")
                .run(ConvertToARM.class)
                .findWarning("0:178-0:180:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;public class Test { public void test(InputStream in) throws Exception { System.out.println(\"Start\"); try (in) { in.read(); } catch (Exception e) { System.out.println(\"Ex\"); } finally { System.out.println(\"Fin\"); } System.out.println(\"Done\"); }}");
    }

    public void testTryFinallyWithFinal() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         System.out.println(\"Start\");" +
                       "         final InputStream in = new FileInputStream(new File(\"a\"));" +
                       "         try {" +
                       "            in.read();" +
                       "         } catch (Exception e) {" +
                       "            System.out.println(\"Ex\");" +
                       "         } finally {" +
                       "            in.close();" +
                       "            System.out.println(\"Fin\");" +
                       "         }" +
                       "         System.out.println(\"Done\");" +
                       "     }" +
                       "}")
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:216-0:218:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\"); try (InputStream in = new FileInputStream(new File(\"a\"))) { in.read(); } catch (Exception e) { System.out.println(\"Ex\"); } finally { System.out.println(\"Fin\"); } System.out.println(\"Done\"); }}");
    }

    public void testLazyTryFinally() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         System.out.println(\"Start\");" +
                       "         InputStream in = null;" +
                       "         try {" +
                       "            in = new FileInputStream(new File(\"a\"));\n" +
                       "            in.read();" +
                       "         } finally {" +
                       "            if (in != null)" +
                       "                in.close();" +
                       "         }" +
                       "         System.out.println(\"Done\");" +
                       "     }" +
                       "}")
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:210-0:212:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\"); try (InputStream in = new FileInputStream(new File(\"a\"))) { in.read(); } System.out.println(\"Done\"); }}");
    }

    public void testNoTry0Stms() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         System.out.println(\"Start\");" +
                       "         InputStream in = new FileInputStream(new File(\"a\"));" +
                       "         in.read();" +
                       "         in.close();" +
                       "         System.out.println(\"Done\");" +
                       "     }" +
                       "}")
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:210-0:212:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\"); try (InputStream in = new FileInputStream(new File(\"a\"))) { in.read(); } System.out.println(\"Done\"); }}");
    }

    public void testEFNoTry0Stms() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "public class Test {" +
                       "     public void test(InputStream in) throws Exception {" +
                       "         System.out.println(\"Start\");" +
                       "         in.close();" +
                       "         System.out.println(\"Done\");" +
                       "     }" +
                       "}")
                .sourceLevel("9")
                .run(ConvertToARM.class)
                .findWarning("0:161-0:163:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;public class Test { public void test(InputStream in) throws Exception { try (in) { System.out.println(\"Start\"); } System.out.println(\"Done\"); }}");
    }

    public void testNoTry1Stm() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         System.out.println(\"Start\");" +
                       "         InputStream in = new FileInputStream(new File(\"a\"));" +
                       "         in.read();" +
                       "         in.close();" +
                       "         System.out.println(\"Done\");" +
                       "     }" +
                       "}")
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:210-0:212:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\"); try (InputStream in = new FileInputStream(new File(\"a\"))) { in.read(); } System.out.println(\"Done\"); }}");
    }

    public void testEFNoTry1Stm() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "public class Test {" +
                       "     public void test(InputStream in) throws Exception {" +
                       "         System.out.println(\"Start\");" +
                       "         in.read();" +
                       "         in.close();" +
                       "         System.out.println(\"Done\");" +
                       "     }" +
                       "}")
                .sourceLevel("9")
                .run(ConvertToARM.class)
                .findWarning("0:161-0:163:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;public class Test { public void test(InputStream in) throws Exception { try (in) { System.out.println(\"Start\"); in.read(); } System.out.println(\"Done\"); }}");
    }

    public void testNoTryMoreStms() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         System.out.println(\"Start\");" +
                       "         InputStream in = new FileInputStream(new File(\"a\"));" +
                       "         in.read();" +
                       "         in.read();" +
                       "         in.close();" +
                       "         System.out.println(\"Done\");" +
                       "     }" +
                       "}")
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:210-0:212:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\"); try (InputStream in = new FileInputStream(new File(\"a\"))) { in.read(); in.read(); } System.out.println(\"Done\"); }}");
    }

    public void testEFNoTryMoreStms() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "public class Test {" +
                       "     public void test(InputStream in) throws Exception {" +
                       "         System.out.println(\"Start\");" +
                       "         in.read();" +
                       "         in.read();" +
                       "         in.close();" +
                       "         System.out.println(\"Done\");" +
                       "     }" +
                       "}")
                .sourceLevel("9")
                .run(ConvertToARM.class)
                .findWarning("0:161-0:163:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;public class Test { public void test(InputStream in) throws Exception { try (in) { System.out.println(\"Start\"); in.read(); in.read(); } System.out.println(\"Done\"); }}");
    }

    public void testNoTry1StmFinal() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         System.out.println(\"Start\");" +
                       "         final InputStream in = new FileInputStream(new File(\"a\"));" +
                       "         in.read();" +
                       "         in.close();" +
                       "         System.out.println(\"Done\");" +
                       "     }" +
                       "}")
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:216-0:218:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\"); try (InputStream in = new FileInputStream(new File(\"a\"))) { in.read(); } System.out.println(\"Done\"); }}");
    }

    public void testNestedInFinally() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         InputStream in = new FileInputStream(new File(\"a\"));" +
                       "         try {" +
                       "             try (InputStream in2 = new FileInputStream(new File(\"a\"))){" +
                       "                 in.read();" +
                       "             }" +
                       "         } finally {" +
                       "             in.close();" +
                       "         }" +
                       "     }" +
                       "}", false)
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:173-0:175:verifier:TXT_ConvertToARM")
                .applyFix("FIX_MergeTryResources")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { try (InputStream in = new FileInputStream(new File(\"a\")); InputStream in2 = new FileInputStream(new File(\"a\"))) { in.read(); } }}");
    }

    public void testEFNestedInFinally() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test(InputStream in) throws Exception {" +
                       "         try {" +
                       "             try (InputStream in2 = new FileInputStream(new File(\"a\"))){" +
                       "                 in.read();" +
                       "             }" +
                       "         } finally {" +
                       "             in.close();" +
                       "         }" +
                       "     }" +
                       "}", false)
                .sourceLevel("9")
                .run(ConvertToARM.class)
                .findWarning("0:269-0:271:verifier:TXT_ConvertToARM")
                .applyFix("FIX_MergeTryResources")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test(InputStream in) throws Exception { try (in; InputStream in2 = new FileInputStream(new File(\"a\"))) { in.read(); } }}");
    }

    public void testNestedInFinal() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         final InputStream in = new FileInputStream(new File(\"a\"));" +
                       "         try {" +
                       "             try (InputStream in2 = new FileInputStream(new File(\"a\"))){" +
                       "                 in.read();" +
                       "             }" +
                       "         } finally {" +
                       "             in.close();" +
                       "         }" +
                       "     }" +
                       "}", false)
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:179-0:181:verifier:TXT_ConvertToARM")
                .applyFix("FIX_MergeTryResources")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { try (InputStream in = new FileInputStream(new File(\"a\")); InputStream in2 = new FileInputStream(new File(\"a\"))) { in.read(); } }}");
    }

    public void testNestedInCatchFinally() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         InputStream in = new FileInputStream(new File(\"a\"));" +
                       "         try {" +
                       "             try (InputStream in2 = new FileInputStream(new File(\"a\"))){" +
                       "                 in.read();" +
                       "             }" +
                       "         } catch (Exception e) {" +
                       "             throw e;" +
                       "         }finally {" +
                       "             in.close();" +
                       "             System.gc();" +
                       "         }" +
                       "     }" +
                       "}", false)
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:173-0:175:verifier:TXT_ConvertToARM")
                .applyFix("FIX_MergeTryResources")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { try (InputStream in = new FileInputStream(new File(\"a\")); InputStream in2 = new FileInputStream(new File(\"a\"))) { in.read(); } catch (Exception e) { throw e; }finally { System.gc(); } }}");
    }

    public void testEFNestedInCatchFinally() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test(InputStream in) throws Exception {" +
                       "         try {" +
                       "             try (InputStream in2 = new FileInputStream(new File(\"a\"))){" +
                       "                 in.read();" +
                       "             }" +
                       "         } catch (Exception e) {" +
                       "             throw e;" +
                       "         }finally {" +
                       "             in.close();" +
                       "             System.gc();" +
                       "         }" +
                       "     }" +
                       "}", false)
                .sourceLevel("9")
                .run(ConvertToARM.class)
                .findWarning("0:269-0:271:verifier:TXT_ConvertToARM")
                .applyFix("FIX_MergeTryResources")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test(InputStream in) throws Exception { try (in; InputStream in2 = new FileInputStream(new File(\"a\"))) { in.read(); } catch (Exception e) { throw e; }finally { System.gc(); } }}");
    }

    public void testNestedInLazyCatchFinally() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         InputStream in = null;" +
                       "         try {" +
                       "             in = new FileInputStream(new File(\"a\"));" +
                       "             try (InputStream in2 = new FileInputStream(new File(\"a\"))){" +
                       "                 in.read();" +
                       "             }" +
                       "         } catch (Exception e) {" +
                       "             throw e;" +
                       "         }finally {" +
                       "             if (in != null)" +
                       "             in.close();" +
                       "             System.gc();" +
                       "         }" +
                       "     }" +
                       "}", false)
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:173-0:175:verifier:TXT_ConvertToARM")
                .applyFix("FIX_MergeTryResources")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { try (InputStream in = new FileInputStream(new File(\"a\")); InputStream in2 = new FileInputStream(new File(\"a\"))) { in.read(); } catch (Exception e) { throw e; }finally { System.gc(); } }}");
    }

    public void testNestedInStms() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         InputStream in = new FileInputStream(new File(\"a\"));" +
                       "         try (InputStream in2 = new FileInputStream(new File(\"a\"))){" +
                       "             in.read();" +
                       "         }" +
                       "         in.close();" +
                       "     }" +
                       "}", false)
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:173-0:175:verifier:TXT_ConvertToARM")
                .applyFix("FIX_MergeTryResources")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { try (InputStream in = new FileInputStream(new File(\"a\")); InputStream in2 = new FileInputStream(new File(\"a\"))){ in.read(); } }}");
    }

    public void testEFNestedInStms() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test(InputStream in) throws Exception {" +
                       "         try (InputStream in2 = new FileInputStream(new File(\"a\"))){" +
                       "             in.read();" +
                       "         }" +
                       "         in.close();" +
                       "     }" +
                       "}", false)
                .sourceLevel("9")
                .run(ConvertToARM.class)
                .findWarning("0:247-0:249:verifier:TXT_ConvertToARM")
                .applyFix("FIX_MergeTryResources")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test(InputStream in) throws Exception { try (in; InputStream in2 = new FileInputStream(new File(\"a\"))){ in.read(); } }}");
    }

    public void testEnclosedFinally() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         try (InputStream in2 = new FileInputStream(new File(\"a\"))){" +
                       "             InputStream in = new FileInputStream(new File(\"a\"));" +
                       "             try {" +
                       "                 in.read();" +
                       "             } finally {" +
                       "                 in.close();" +
                       "             }" +
                       "        }" +
                       "     }" +
                       "}", false)
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:245-0:247:verifier:TXT_ConvertToARM")
                .applyFix("FIX_MergeTryResources")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { try (InputStream in2 = new FileInputStream(new File(\"a\")); InputStream in = new FileInputStream(new File(\"a\"))){ in.read(); } }}");
    }

    public void testEFEnclosedFinally() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test(InputStream in) throws Exception {" +
                       "         try (InputStream in2 = new FileInputStream(new File(\"a\"))){" +
                       "             try {" +
                       "                 in.read();" +
                       "             } finally {" +
                       "                 in.close();" +
                       "             }" +
                       "        }" +
                       "     }" +
                       "}", false)
                .sourceLevel("9")
                .run(ConvertToARM.class)
                .findWarning("0:269-0:271:verifier:TXT_ConvertToARM")
                .applyFix("FIX_MergeTryResources")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test(InputStream in) throws Exception { try (InputStream in2 = new FileInputStream(new File(\"a\")); in){ in.read(); } }}");
    }

    public void testEnclosedFinallyInCatchFinally() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         try (InputStream in2 = new FileInputStream(new File(\"a\"))){" +
                       "             InputStream in = new FileInputStream(new File(\"a\"));" +
                       "             try {" +
                       "                 in.read();" +
                       "             } finally {" +
                       "                 in.close();" +
                       "             }" +
                       "        } catch (Exception e) {" +
                       "            throw e;" +
                       "        } finally {" +
                       "            System.gc();" +
                       "        }" +
                       "     }" +
                       "}", false)
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:245-0:247:verifier:TXT_ConvertToARM")
                .applyFix("FIX_MergeTryResources")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { try (InputStream in2 = new FileInputStream(new File(\"a\")); InputStream in = new FileInputStream(new File(\"a\"))){ in.read(); } catch (Exception e) { throw e; } finally { System.gc(); } }}");
    }

    public void testEFEnclosedFinallyInCatchFinally() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test(InputStream in) throws Exception {" +
                       "         try (InputStream in2 = new FileInputStream(new File(\"a\"))){" +
                       "             try {" +
                       "                 in.read();" +
                       "             } finally {" +
                       "                 in.close();" +
                       "             }" +
                       "        } catch (Exception e) {" +
                       "            throw e;" +
                       "        } finally {" +
                       "            System.gc();" +
                       "        }" +
                       "     }" +
                       "}", false)
                .sourceLevel("9")
                .run(ConvertToARM.class)
                .findWarning("0:269-0:271:verifier:TXT_ConvertToARM")
                .applyFix("FIX_MergeTryResources")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test(InputStream in) throws Exception { try (InputStream in2 = new FileInputStream(new File(\"a\")); in){ in.read(); } catch (Exception e) { throw e; } finally { System.gc(); } }}");
    }

    public void testEnclosedFinallyFinal() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         try (InputStream in2 = new FileInputStream(new File(\"a\"))){" +
                       "             final InputStream in = new FileInputStream(new File(\"a\"));" +
                       "             try {" +
                       "                 in.read();" +
                       "             } finally {" +
                       "                 in.close();" +
                       "             }" +
                       "        }" +
                       "     }" +
                       "}", false)
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:251-0:253:verifier:TXT_ConvertToARM")
                .applyFix("FIX_MergeTryResources")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { try (InputStream in2 = new FileInputStream(new File(\"a\")); InputStream in = new FileInputStream(new File(\"a\"))){ in.read(); } }}");
    }

    public void testEnclosedStms() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         try (InputStream in2 = new FileInputStream(new File(\"a\"))){" +
                       "             InputStream in = new FileInputStream(new File(\"a\"));" +
                       "             in.read();" +
                       "             in.close();" +
                       "         }" +
                       "    }" +
                       "}", false)
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:245-0:247:verifier:TXT_ConvertToARM")
                .applyFix("FIX_MergeTryResources")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { try (InputStream in2 = new FileInputStream(new File(\"a\")); InputStream in = new FileInputStream(new File(\"a\"))){ in.read(); } }}");
    }

    public void testEFEnclosedStms() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test(InputStream in) throws Exception {" +
                       "         try (InputStream in2 = new FileInputStream(new File(\"a\"))){" +
                       "             in.read();" +
                       "             in.close();" +
                       "         }" +
                       "    }" +
                       "}", false)
                .sourceLevel("9")
                .run(ConvertToARM.class)
                .findWarning("0:247-0:249:verifier:TXT_ConvertToARM")
                .applyFix("FIX_MergeTryResources")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test(InputStream in) throws Exception { try (InputStream in2 = new FileInputStream(new File(\"a\")); in){ in.read(); } }}");
    }

    public void testEnclosedStmsInCatchFinally() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         try (InputStream in2 = new FileInputStream(new File(\"a\"))){" +
                       "             InputStream in = new FileInputStream(new File(\"a\"));" +
                       "             in.read();" +
                       "             in.close();" +
                       "         } catch (Exception e) { throw e;} finally { System.gc(); }" +
                       "    }" +
                       "}", false)
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:245-0:247:verifier:TXT_ConvertToARM")
                .applyFix("FIX_MergeTryResources")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { try (InputStream in2 = new FileInputStream(new File(\"a\")); InputStream in = new FileInputStream(new File(\"a\"))){ in.read(); } catch (Exception e) { throw e;} finally { System.gc(); } }}");
    }

    public void testEFEnclosedStmsInCatchFinally() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test(InputStream in) throws Exception {" +
                       "         try (InputStream in2 = new FileInputStream(new File(\"a\"))){" +
                       "             in.read();" +
                       "             in.close();" +
                       "         } catch (Exception e) { throw e;} finally { System.gc(); }" +
                       "    }" +
                       "}", false)
                .sourceLevel("9")
                .run(ConvertToARM.class)
                .findWarning("0:247-0:249:verifier:TXT_ConvertToARM")
                .applyFix("FIX_MergeTryResources")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test(InputStream in) throws Exception { try (InputStream in2 = new FileInputStream(new File(\"a\")); in){ in.read(); } catch (Exception e) { throw e;} finally { System.gc(); } }}");
    }

    public void testEnclosedStmsFinal() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         try (InputStream in2 = new FileInputStream(new File(\"a\"))){" +
                       "             final InputStream in = new FileInputStream(new File(\"a\"));" +
                       "             in.read();" +
                       "             in.close();" +
                       "         }" +
                       "    }" +
                       "}", false)
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:251-0:253:verifier:TXT_ConvertToARM")
                .applyFix("FIX_MergeTryResources")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { try (InputStream in2 = new FileInputStream(new File(\"a\")); InputStream in = new FileInputStream(new File(\"a\"))){ in.read(); } }}");
    }

    public void testEnclosedLazy() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         try (InputStream in2 = new FileInputStream(new File(\"a\"))) {" +
                       "             InputStream in = null;" +
                       "             try {" +
                       "                in = new FileInputStream(new File(\"b\"));" +
                       "                in.read();" +
                       "" +
                       "             } finally { if (in != null) in.close(); }" +
                       "         }" +
                       "     }" +
                       "}", false)
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:246-0:248:verifier:TXT_ConvertToARM")
                .applyFix("FIX_MergeTryResources")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { try (InputStream in2 = new FileInputStream(new File(\"a\")); InputStream in = new FileInputStream(new File(\"b\"))) { in.read(); } }}");
    }

    public void testEnclosedLazyInCatchFinally() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         try (InputStream in2 = new FileInputStream(new File(\"a\"))) {" +
                       "             InputStream in = null;" +
                       "             try {" +
                       "                in = new FileInputStream(new File(\"b\"));" +
                       "                in.read();" +
                       "" +
                       "             } finally { if (in != null) in.close(); }" +
                       "         }catch(Exception e) { throw e;} finally {System.exit(1);}" +
                       "     }" +
                       "}", false)
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:246-0:248:verifier:TXT_ConvertToARM")
                .applyFix("FIX_MergeTryResources")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { try (InputStream in2 = new FileInputStream(new File(\"a\")); InputStream in = new FileInputStream(new File(\"b\"))) { in.read(); }catch(Exception e) { throw e;} finally {System.exit(1);} }}");
    }

    public void testTryTry() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.OutputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.FileOutputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test(File from, File to1) throws Exception {" +
                       "         final byte[] data = new byte[512];" +
                       "         final InputStream in = new FileInputStream(from);" +
                       "         try {" +
                       "            final OutputStream out1 = new FileOutputStream(to1);" +
                       "            try {" +
                       "                int len;" +
                       "                while ((len = in.read(data)) > 0) {" +
                       "                    out1.write(data, 0, len);" +
                       "                }" +
                       "            } finally {" +
                       "                out1.close();" +
                       "            }" +
                       "        } finally {" +
                       "            in.close();" +
                       "        }" +
                       "     }" +
                       "}")
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:301-0:303:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1) throws Exception { final byte[] data = new byte[512]; try (InputStream in = new FileInputStream(from)) { final OutputStream out1 = new FileOutputStream(to1); try { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); } } finally { out1.close(); } } }}");
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.OutputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.FileOutputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test(File from, File to1) throws Exception {" +
                       "         final byte[] data = new byte[512];" +
                       "         final InputStream in = new FileInputStream(from);" +
                       "         try {" +
                       "            final OutputStream out1 = new FileOutputStream(to1);" +
                       "            try {" +
                       "                int len;" +
                       "                while ((len = in.read(data)) > 0) {" +
                       "                    out1.write(data, 0, len);" +
                       "                }" +
                       "            } finally {" +
                       "                out1.close();" +
                       "            }" +
                       "        } finally {" +
                       "            in.close();" +
                       "        }" +
                       "     }" +
                       "}")
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:377-0:381:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1) throws Exception { final byte[] data = new byte[512]; final InputStream in = new FileInputStream(from); try { try (OutputStream out1 = new FileOutputStream(to1)) { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); } } } finally { in.close(); } }}");
    }

    public void testTryTryTry() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.OutputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.FileOutputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test(File from, File to1, File to2) throws Exception {" +
                       "         final byte[] data = new byte[512];" +
                       "         final InputStream in = new FileInputStream(from);" +
                       "         try {" +
                       "            final OutputStream out1 = new FileOutputStream(to1);" +
                       "            try {" +
                       "                final OutputStream out2 = new FileOutputStream(to2);" +
                       "                try {" +
                       "                    int len;" +
                       "                    while ((len = in.read(data)) > 0) {" +
                       "                        out1.write(data, 0, len);" +
                       "                        out2.write(data, 0, len);" +
                       "                    }" +
                       "                } finally {" +
                       "                    out2.close();" +
                       "                }" +
                       "            } finally {" +
                       "                out1.close();" +
                       "            }" +
                       "        } finally {" +
                       "            in.close();" +
                       "        }" +
                       "    }" +
                       "}")
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:311-0:313:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1, File to2) throws Exception { final byte[] data = new byte[512]; try (InputStream in = new FileInputStream(from)) { final OutputStream out1 = new FileOutputStream(to1); try { final OutputStream out2 = new FileOutputStream(to2); try { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); out2.write(data, 0, len); } } finally { out2.close(); } } finally { out1.close(); } } }}");
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.OutputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.FileOutputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test(File from, File to1, File to2) throws Exception {" +
                       "         final byte[] data = new byte[512];" +
                       "         final InputStream in = new FileInputStream(from);" +
                       "         try {" +
                       "            final OutputStream out1 = new FileOutputStream(to1);" +
                       "            try {" +
                       "                final OutputStream out2 = new FileOutputStream(to2);" +
                       "                try {" +
                       "                    int len;" +
                       "                    while ((len = in.read(data)) > 0) {" +
                       "                        out1.write(data, 0, len);" +
                       "                        out2.write(data, 0, len);" +
                       "                    }" +
                       "                } finally {" +
                       "                    out2.close();" +
                       "                }" +
                       "            } finally {" +
                       "                out1.close();" +
                       "            }" +
                       "        } finally {" +
                       "            in.close();" +
                       "        }" +
                       "    }" +
                       "}")
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:387-0:391:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1, File to2) throws Exception { final byte[] data = new byte[512]; final InputStream in = new FileInputStream(from); try { try (OutputStream out1 = new FileOutputStream(to1)) { final OutputStream out2 = new FileOutputStream(to2); try { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); out2.write(data, 0, len); } } finally { out2.close(); } } } finally { in.close(); } }}");
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.OutputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.FileOutputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test(File from, File to1, File to2) throws Exception {" +
                       "         final byte[] data = new byte[512];" +
                       "         final InputStream in = new FileInputStream(from);" +
                       "         try {" +
                       "            final OutputStream out1 = new FileOutputStream(to1);" +
                       "            try {" +
                       "                final OutputStream out2 = new FileOutputStream(to2);" +
                       "                try {" +
                       "                    int len;" +
                       "                    while ((len = in.read(data)) > 0) {" +
                       "                        out1.write(data, 0, len);" +
                       "                        out2.write(data, 0, len);" +
                       "                    }" +
                       "                } finally {" +
                       "                    out2.close();" +
                       "                }" +
                       "            } finally {" +
                       "                out1.close();" +
                       "            }" +
                       "        } finally {" +
                       "            in.close();" +
                       "        }" +
                       "    }" +
                       "}")
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:472-0:476:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1, File to2) throws Exception { final byte[] data = new byte[512]; final InputStream in = new FileInputStream(from); try { final OutputStream out1 = new FileOutputStream(to1); try { try (OutputStream out2 = new FileOutputStream(to2)) { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); out2.write(data, 0, len); } } } finally { out1.close(); } } finally { in.close(); } }}");
    }

    public void testTryTryTryPathUp() throws Exception {
        /*
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.OutputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.FileOutputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test(File from, File to1, File to2) throws Exception {" +
                       "         final byte[] data = new byte[512];" +
                       "         final InputStream in = new FileInputStream(from);" +
                       "         try {" +
                       "            final OutputStream out1 = new FileOutputStream(to1);" +
                       "            try {" +
                       "                final OutputStream out2 = new FileOutputStream(to2);" +
                       "                try {" +
                       "                    int len;" +
                       "                    while ((len = in.read(data)) > 0) {" +
                       "                        out1.write(data, 0, len);" +
                       "                        out2.write(data, 0, len);" +
                       "                    }" +
                       "                } finally {" +
                       "                    out2.close();" +
                       "                }" +
                       "            } finally {" +
                       "                out1.close();" +
                       "            }" +
                       "        } finally {" +
                       "            in.close();" +
                       "        }" +
                       "    }" +
                       "}")
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:472-0:476:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1, File to2) throws Exception { final byte[] data = new byte[512]; final InputStream in = new FileInputStream(from); try { final OutputStream out1 = new FileOutputStream(to1); try { try (OutputStream out2 = new FileOutputStream(to2)) { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); out2.write(data, 0, len); } } } finally { out1.close(); } } finally { in.close(); } }}");
                */
        HintTest
                .create()
                .input("package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1, File to2) throws Exception { final byte[] data = new byte[512]; final InputStream in = new FileInputStream(from); try { final OutputStream out1 = new FileOutputStream(to1); try { try (OutputStream out2 = new FileOutputStream(to2)) { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); out2.write(data, 0, len); } } } finally { out1.close(); } } finally { in.close(); } }}", false)
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:348-0:352:verifier:TXT_ConvertToARM")
                .applyFix("FIX_MergeTryResources")
                .assertOutput("package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1, File to2) throws Exception { final byte[] data = new byte[512]; final InputStream in = new FileInputStream(from); try { try (OutputStream out1 = new FileOutputStream(to1); OutputStream out2 = new FileOutputStream(to2)) { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); out2.write(data, 0, len); } } } finally { in.close(); } }}");
        HintTest
                .create()
                .input("package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1, File to2) throws Exception { final byte[] data = new byte[512]; final InputStream in = new FileInputStream(from); try { try (OutputStream out1 = new FileOutputStream(to1); OutputStream out2 = new FileOutputStream(to2)) { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); out2.write(data, 0, len); } } } finally { in.close(); } }}", false)
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:291-0:293:verifier:TXT_ConvertToARM")
                .applyFix("FIX_MergeTryResources")
                .assertOutput("package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1, File to2) throws Exception { final byte[] data = new byte[512]; try (InputStream in = new FileInputStream(from); OutputStream out1 = new FileOutputStream(to1); OutputStream out2 = new FileOutputStream(to2)) { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); out2.write(data, 0, len); } } }}");
    }

    public void testTryTryTryPathDown() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.OutputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.FileOutputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test(File from, File to1, File to2) throws Exception {" +
                       "         final byte[] data = new byte[512];" +
                       "         final InputStream in = new FileInputStream(from);" +
                       "         try {" +
                       "            final OutputStream out1 = new FileOutputStream(to1);" +
                       "            try {" +
                       "                final OutputStream out2 = new FileOutputStream(to2);" +
                       "                try {" +
                       "                    int len;" +
                       "                    while ((len = in.read(data)) > 0) {" +
                       "                        out1.write(data, 0, len);" +
                       "                        out2.write(data, 0, len);" +
                       "                    }" +
                       "                } finally {" +
                       "                    out2.close();" +
                       "                }" +
                       "            } finally {" +
                       "                out1.close();" +
                       "            }" +
                       "        } finally {" +
                       "            in.close();" +
                       "        }" +
                       "    }" +
                       "}")
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:311-0:313:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1, File to2) throws Exception { final byte[] data = new byte[512]; try (InputStream in = new FileInputStream(from)) { final OutputStream out1 = new FileOutputStream(to1); try { final OutputStream out2 = new FileOutputStream(to2); try { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); out2.write(data, 0, len); } } finally { out2.close(); } } finally { out1.close(); } } }}");
        HintTest
                .create()
                .input("package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1, File to2) throws Exception { final byte[] data = new byte[512]; try (InputStream in = new FileInputStream(from)) { final OutputStream out1 = new FileOutputStream(to1); try { final OutputStream out2 = new FileOutputStream(to2); try { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); out2.write(data, 0, len); } } finally { out2.close(); } } finally { out1.close(); } } }}", false)
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:343-0:347:verifier:TXT_ConvertToARM")
                .applyFix("FIX_MergeTryResources")
                .assertOutput("package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1, File to2) throws Exception { final byte[] data = new byte[512]; try (InputStream in = new FileInputStream(from); OutputStream out1 = new FileOutputStream(to1)) { final OutputStream out2 = new FileOutputStream(to2); try { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); out2.write(data, 0, len); } } finally { out2.close(); } } }}");
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.OutputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.FileOutputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "public void test(File from, File to1, File to2) throws Exception {" +
                       "   final byte[] data = new byte[512];" +
                       "   try ( InputStream in = new FileInputStream(from);  OutputStream out1 = new FileOutputStream(to1)) {" +
                       "       final OutputStream out2 = new FileOutputStream(to2);" +
                       "       try {" +
                       "           int len;" +
                       "           while ((len = in.read(data)) > 0) {" +
                       "               out1.write(data, 0, len);" +
                       "               out2.write(data, 0, len);" +
                       "           }" +
                       "       } finally {" +
                       "           out2.close();" +
                       "       }" +
                       "   }" +
                       "}" +
                       "}", false)
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:401-0:405:verifier:TXT_ConvertToARM")
                .applyFix("FIX_MergeTryResources")
                .assertOutput("package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test {public void test(File from, File to1, File to2) throws Exception { final byte[] data = new byte[512]; try ( InputStream in = new FileInputStream(from); OutputStream out1 = new FileOutputStream(to1); OutputStream out2 = new FileOutputStream(to2)) { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); out2.write(data, 0, len); } }}}");
    }

    public void testSimpleVarDeclUsedAfterClose() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         System.out.println(\"Start\");" +
                       "         InputStream ins = new FileInputStream(\"\");" +
                       "         int r = ins.read();" +
                       "         ins.close();" +
                       "         System.out.println(r);" +
                       "     }" +
                       "}")
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:210-0:213:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\"); int r; try (InputStream ins = new FileInputStream(\"\")) { r = ins.read(); } System.out.println(r); }}");
    }

    public void testSimpleVarDeclUsedAfterClose2() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         System.out.println(\"Start\");" +
                       "         InputStream ins = new FileInputStream(\"\");" +
                       "         int r;" +
                       "         r = ins.read();" +
                       "         ins.close();" +
                       "         System.out.println(r);" +
                       "     }" +
                       "}")
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:210-0:213:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\"); int r; try (InputStream ins = new FileInputStream(\"\")) { r = ins.read(); } System.out.println(r); }}");
    }

    public void testComplexVarDeclUsedAfterClose() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         System.out.println(\"Start\");" +
                       "         InputStream ins = new FileInputStream(\"\");" +
                       "         int r1 = ins.read();" +
                       "         int r2 = ins.read();" +
                       "         int sum = r1 + r2;" +
                       "         ins.close();" +
                       "         System.out.println(r1);" +
                       "         System.out.println(sum);" +
                       "     }" +
                       "}")
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:210-0:213:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\"); int r1; int sum; try (InputStream ins = new FileInputStream(\"\")) { r1 = ins.read(); int r2 = ins.read(); sum = r1 + r2; } System.out.println(r1); System.out.println(sum); }}");
    }

    public void testResourceUsedAfterClose1() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "        System.out.println(\"Start\");" +
                       "	InputStream ins = new FileInputStream(\"\");" +
                       "        ins.read();" +
                       "        ins.close();" +
                       "        ins = null;" +
                       "        System.out.println(r);" +
                       "     }" +
                       "}", false)
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:201-0:204:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\"); try (InputStream ins = new FileInputStream(\"\")) { ins.read(); } System.out.println(r); }}");
    }

    public void testResourceUsedAfterClose2() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "        System.out.println(\"Start\");" +
                       "	InputStream ins = new FileInputStream(\"\");" +
                       "        ins.read();" +
                       "        ins.close();" +
                       "        ins.available();" +
                       "        System.out.println(r);" +
                       "     }" +
                       "}", false)
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .assertWarnings();
    }

    public void testResourceUsedAfterClose3() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "        System.out.println(\"Start\");" +
                       "	InputStream ins = new FileInputStream(\"\");" +
                       "        ins.read();" +
                       "        ins.close();" +
                       "        if (true) {" +
                       "           ins = null;" +
                       "           System.out.println(r);" +
                       "        }" +
                       "     }" +
                       "}", false)
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:201-0:204:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\"); try (InputStream ins = new FileInputStream(\"\")) { ins.read(); } if (true) { System.out.println(r); } }}");
    }

    public void testNullResourceNoIfCheck() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "        System.out.println(\"Start\");" +
                       "        InputStream ins = null;" +
                       "        try {" +
                       "            ins = new FileInputStream(\"\");" +
                       "            ins.read();" +
                       "        } finally {" +
                       "             ins.close();" +
                       "        }" +
                       "        System.out.println(\"Done\");" +
                       "     }" +
                       "}")
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("0:208-0:211:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\"); try (InputStream ins = new FileInputStream(\"\")) { ins.read(); } System.out.println(\"Done\"); }}");
    }

    public void testNoStatements() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "        System.out.println(\"Start\");" +
                       "        InputStream ins = new FileInputStream(\"\");" +
                       "        ins.close();" +
                       "        System.out.println(\"Done\");" +
                       "     }" +
                       "}")
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .assertWarnings();
    }

    public void testNoARMHintForSourceLevelLessThen17() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         System.out.println(\"Start\");" +
                       "         final InputStream in = new FileInputStream(new File(\"a\"));" +
                       "         try {" +
                       "            in.read();" +
                       "         } finally {" +
                       "            in.close();" +
                       "         }" +
                       "         System.out.println(\"Done\");" +
                       "     }" +
                       "}", false)
                .sourceLevel("1.6")
                .run(ConvertToARM.class)
                .assertWarnings();
    }

    public void testAssignmentToResource() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public void test(boolean b) throws Exception {" +
                       "        System.out.println(\"Start\");" +
                       "        InputStream ins = null;" +
                       "        if (b) {" +
                       "            ins = new FileInputStream(\"\");" +
                       "        }" +
                       "        if (ins == null) {" +
                       "            ins = new FileInputStream(\"\");" +
                       "        }" +
                       "        ins.read();" +
                       "        ins.close();" +
                       "     }" +
                       "}")
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .assertWarnings();
    }

    public void testCannotSplitVariable() throws Exception {
        HintTest
                .create()
                .input("package test;" +
                       "import java.io.InputStream;" +
                       "import java.io.FileInputStream;" +
                       "import java.io.File;" +
                       "public class Test {" +
                       "     public int test(boolean b) throws Exception {" +
                       "        System.out.println(\"Start\");" +
                       "        InputStream ins = new FileInputStream(\"\");" +
                       "        if (b) {" +
                       "            int r = 0;" +
                       "            System.err.println(r);" +
                       "        }" +
                       "        int r = ins.read();" +
                       "        ins.close();" +
                       "        return r;" +
                       "     }" +
                       "}")
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .assertWarnings();
    }

    public void test225686() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "import java.io.*;\n" +
                       "public class Test {\n" +
                       "     public String test() throws Exception {\n" +
                       "        InputStream in = new FileInputStream(\"\");\n" +
                       "        System.err.println(\"interfering\");\n" +
                       "        String str;\n" +
                       "        str = \"\";\n" +
                       "        in.close();\n" +
                       "        return str;\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("4:20-4:22:verifier:TXT_ConvertToARM")
                .applyFix("TXT_ConvertToARM")
                .assertOutput("package test;\n" +
                              "import java.io.*;\n" +
                              "public class Test {\n" +
                              "     public String test() throws Exception {\n" +
                              "        String str;\n" +
                              "        try (InputStream in = new FileInputStream(\"\")) {\n" +
                              "            System.err.println(\"interfering\");\n" +
                              "            str = \"\";\n" +
                              "        }\n" +
                              "        return str;\n" +
                              "     }\n" +
                              "}\n");
    }

    public void test227787() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.io.*;\n" +
                       "public class Test {\n" +
                       "    public void test() throws Exception {\n" +
                       "         try (InputStream in = new FileInputStream(\"\")) {\n" +
                       "              in.read();\n" +
                       "              OutputStream out = new FileOutputStream(\"\");\n" +
                       "              int r;\n" +
                       "              while ((r = in.read()) != (-1)) {\n" +
                       "                    out.write(r);\n" +
                       "              }\n" +
                       "              out.close();\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .assertWarnings("6:27-6:30:verifier:TXT_ConvertToARM");
    }
    
    /**
     * All comments except possibly for the removed .close(); statement
     * should be somehow preserved, although they might be moved around.
     * 
     * @throws Exception 
     */
    public void testPreserveComments228141() throws Exception {
        String s =
            HintTest.create()
                .input("package test;\n" +
                       "import java.io.*;\n" +
                       "public class Test {\n" +
                        "    public void arm2() throws Exception {\n" +
                        "         System.err.println(\"start\");\n" +
                        "\n" +         
                        "        // c1\n" +
                        "        InputStream in = new FileInputStream(\"boo\"); // c2\n" +
                        "        // c3\n" +
                        "        try { // c4\n" +
                        "            // c5\n" +
                        "            in.read(); // c6\n" +
                        "            // c7\n" +
                        "        } catch (Exception e) { // c8\n" +
                        "            // c12\n" +
                        "            System.err.println(\"ex\"); // c9\n" +
                        "            // c10\n" +
                        "        } finally { \n" +
                        "            in.close();\n" +
                        "            // c11\n" +
                        "            System.err.println(\"fin\"); // c12\n" +
                        "            // c13\n" +
                        "        } // c14\n" +
                        "        // c15\n" +
                        "        System.err.println(\"done\");\n" +
                        "   }\n" +
                        "}\n")
                .sourceLevel("1.7")
                .run(ConvertToARM.class)
                .findWarning("7:20-7:22:verifier:TXT_ConvertToARM").applyFix().getOutput();
        
        for (int i = 1 ; i < 15; i++) {
            String comment = "// c" + i + "\n";
            if (!s.contains(comment)) {
                fail("Comment #" + i + " is missing");
            }
        }
    }
}
