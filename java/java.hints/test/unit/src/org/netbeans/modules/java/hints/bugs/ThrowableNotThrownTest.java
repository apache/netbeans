/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

/**
 *
 * @author sdedic
 */
public class ThrowableNotThrownTest extends NbTestCase {

    public ThrowableNotThrownTest(String name) {
        super(name);
    }
    
    /**
     * New instance is assigned to a variable, then thrown -> OK
     * @throws Exception 
     */
    public void testThrowableAssignedAndThrown() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "\n" +
                "import java.io.IOException;\n" +
                "\n" +
                "public final class Test\n" +
                "{\n" +
                "    public void test() throws Throwable {\n" +
                "        Throwable t = new IOException();\n" +
                "        throw t;\n" +
                "    }\n" +
                "}"
                )
                .run(ThrowableNotThrown.class)
                .assertWarnings();
    }
    
    /**
     * Throwable is assigned to a variable, but is not thrown
     */
    public void testThrowableAssingedNotThrown() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "\n" +
                "import java.io.IOException;\n" +
                "\n" +
                "public final class Test\n" +
                "{\n" +
                "    public void test() throws Throwable {\n" +
                "        Throwable t = new IOException();\n" +
                "        throw new IllegalArgumentException();\n" +
                "    }\n" +
                "}"
                )
                .run(ThrowableNotThrown.class)
                .assertWarnings("7:22-7:39:verifier:Throwable instance not thrown");
    }
    
    /**
     * Should test the 'simple' case throw new ...
     * @throws Exception 
     */
    public void testThrowableThrown() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "\n" +
                "import java.io.IOException;\n" +
                "\n" +
                "public final class Test\n" +
                "{\n" +
                "    public void test() throws Throwable {\n" +
                "        throw new IOException();\n" +
                "    }\n" +
                "}"
                )
                .run(ThrowableNotThrown.class)
                .assertWarnings();
    }
    
    public void testThrowablePassedToMethod() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "\n" +
                "import java.io.IOException;\n" +
                "\n" +
                "public final class Test\n" +
                "{\n" +
                "    public void test() throws Throwable {\n" +
                "        Throwable t = new IOException();\n" +
                "        report(t);\n" +
                "    }\n" +
                "    \n" +
                "    public void report(Throwable t) {}\n" +
                "}"
                )
                .run(ThrowableNotThrown.class)
                .assertWarnings();
    }
    
    public void testThrowableReturned() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "\n" +
                "import java.io.IOException;\n" +
                "\n" +
                "public final class Test\n" +
                "{\n" +
                "    public Throwable test() throws Throwable {\n" +
                "        Throwable t = new IOException();\n" +
                "        return t;\n" +
                "    }\n" +
                "}"
                )
                .run(ThrowableNotThrown.class)
                .assertWarnings();
    }
    
    public void testAssignedToField() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "\n" +
                "import java.io.IOException;\n" +
                "\n" +
                "public final class Test\n" +
                "{\n" +
                "    private Throwable f;\n" +
                "    public void test() throws Throwable {\n" +
                "        Throwable t = new IOException();\n" +
                "        f = t;\n" +
                "    }\n" +
                "}"
                )
                .run(ThrowableNotThrown.class)
                .assertWarnings();
    }

    /**
     * Throwable is assigned, then reassigned in some code path, then thrown.
     * @throws Exception 
     */
    public void testThrowableReassignedAndThrown() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "\n" +
                "import java.io.IOException;\n" +
                "\n" +
                "public final class Test\n" +
                "{\n" +
                "    private Throwable f;\n" +
                "    public void test() throws Throwable {\n" +
                "        Throwable t = new IOException();\n" +
                "        Throwable e = null;\n" +
                "        if (Math.random() > 0) {\n" +
                "            e = t;\n" +
                "        }\n" +
                "        throw e;\n" +
                "    }\n" +
                "}" 
                )
                .run(ThrowableNotThrown.class)
                .assertWarnings();
    }
    
    public void testMethodCallResultNotThrown() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "public final class Test\n" +
                "{\n" +
                "    public void test() throws Throwable {\n" +
                "        t();\n" +
                "    }\n" +
                "    \n" +
                "    public Throwable t() {return null;}\n" +
                "}"
                )
                .run(ThrowableNotThrown.class)
                .assertWarnings("4:8-4:11:verifier:Throwable method result is ignored");
    }
    
    /**
     * Checks that a call to getCause that stands alone is reported
     */
    public void testGetCauseAloneReported() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "import java.lang.reflect.InvocationTargetException;\n" +
                "public final class Test\n" +
                "{\n" +
                "  public static Object bar (java.lang.reflect.Method m) {\n" +
                "    try {\n" +
                "      return m.invoke(null);\n" +
                "    }\n" +
                "    catch (InvocationTargetException t) {\n" +
                "        t.getCause(); \n" +
                "      return null;\n" +
                "    }\n" +
                "    catch (IllegalAccessException t) {\n" +
                "      return null;\n" +
                "    }\n" +
                "  } " +
                "}"
                )
                .run(ThrowableNotThrown.class)
                .assertWarnings("9:8-9:20:verifier:Throwable method result is ignored");
    }


    /**
     * Checks that a call to getCause that is passed somewhere is OK
     */
    public void testGetCausePassedOn() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "import java.lang.reflect.InvocationTargetException;\n" +
                "public final class Test\n" +
                "{\n" +
                "  public static Object bar (java.lang.reflect.Method m) {\n" +
                "    try {\n" +
                "      return m.invoke(null);\n" +
                "    }\n" +
                "    catch (InvocationTargetException t) {\n" +
                "      System.err.println(t.getCause()); // <-- No hint\n" +
                "      return null;\n" +
                "    }\n" +
                "    catch (IllegalAccessException t) {\n" +
                "      return null;\n" +
                "    }\n" +
                "  } " +
                "}"
                )
                .run(ThrowableNotThrown.class)
                .assertWarnings();
    }


    public void testMethodInvokedOnThrowable() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "import java.lang.reflect.InvocationTargetException;\n" +
                "public final class Test\n" +
                "{\n" +
                "  public static Object foo (java.lang.reflect.Method m) {\n" +
                "    try {\n" +
                "      return m.invoke(null);\n" +
                "    }\n" +
                "    catch (InvocationTargetException t) {\n" +
                "      System.err.println(t.getCause().toString());\n" +
                "      return null;\n" +
                "    }\n" +
                "    catch (IllegalAccessException t) {\n" +
                "      return null;\n" +
                "    } \n" +
                "  }" +
                "}"
                )
                .run(ThrowableNotThrown.class)
                .assertWarnings();
    }

    public void testThrowableCompared() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "import java.io.FileInputStream;\n" +
                "import java.io.IOException;\n" +
                "\n" +
                "public class Test {    \n" +
                "    void test() throws Exception {\n" +
                "        try {\n" +
                "            new FileInputStream(\"/foo\");\n" +
                "        } catch (IOException ex) {\n" +
                "            Exception foo = processException(ex);\n" +
                "            if (foo == null) {\n" +
                "                throw ex;\n" +
                "            }\n" +
                "        }\n" +
                "    } \n" +
                "    \n" +
                "    private Exception processException(Exception ex) {\n" +
                "        return null;\n" +
                "    }\n" +
                "}"
                )
                .run(ThrowableNotThrown.class)
                .assertWarnings();
    }
    
    /**
     * Checks that passing throwable to throw statement through exception parameter works - does not report a warning
     * See issue #262558
     */
    public void testThrowableAssignedToExceptionParameter() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "import java.io.FileInputStream;\n" +
                "import java.io.FileNotFoundException;\n" +
                "import java.io.IOException;\n" +
                "\n" +
                "public class Test {    \n" +
                "    void test(Exception ex2) throws Exception {\n" +
                "        try {\n" +
                "            new FileInputStream(\"/foo\");\n" +
                "        } catch (FileNotFoundException ex) {\n" +
                "            ex2 = processException(ex);\n" +
            "                throw ex2;\n" +
                "        } catch (IOException ex) {\n" +
                "            ex = (IOException)processException(ex);\n" +
            "                throw ex;\n" +
                "        }\n" +
                "    } \n" +
                "    \n" +
                "    private Exception processException(Exception ex) {\n" +
                "        return null;\n" +
                "    }\n" +
                "}"
                )
                .run(ThrowableNotThrown.class)
                .assertWarnings();
    }

    public void testEnhancedSwitch1() throws Exception {
        HintTest.create()
                .sourceLevel(25)
                .input("""
                       package test;
                       import java.io.FileInputStream;
                       import java.io.IOException;

                       public class Test {
                           void test() throws Throwable {
                               try {
                                   new FileInputStream("/foo");
                               } catch (IOException ex) {
                                   switch (ex.getCause()) {
                                       case IOException ioe -> throw ioe;
                                       case Throwable _ -> throw ex;
                                   }
                               }
                           }
                       }
                       """)
                .run(ThrowableNotThrown.class)
                .assertWarnings();
    }

    public void testEnhancedSwitch2() throws Exception {
        HintTest.create()
                .sourceLevel(25)
                .input("""
                       package test;
                       import java.io.FileInputStream;
                       import java.io.IOException;

                       public class Test {
                           void test() throws Throwable {
                               try {
                                   new FileInputStream("/foo");
                               } catch (IOException ex) {
                                   switch (ex.getCause()) {
                                       case IOException ioe -> throw ex;
                                       case Throwable _ -> throw ex;
                                   }
                               }
                           }
                       }
                       """)
                .run(ThrowableNotThrown.class)
                .assertWarnings("9:20-9:33:verifier:Throwable method result is ignored");
    }

    public void testEnhancedSwitchExpressionNoWarn1() throws Exception {
        HintTest.create()
                .sourceLevel(25)
                .input("""
                       package test;
                       import java.io.FileInputStream;
                       import java.io.IOException;

                       public class Test {
                           void test() throws Throwable {
                               try {
                                   new FileInputStream("/foo");
                               } catch (IOException ex) {
                                   throw switch (ex.getCause()) {
                                       case IOException ioe -> ioe;
                                       case Throwable _ -> ex;
                                   };
                               }
                           }
                       }
                       """)
                .run(ThrowableNotThrown.class)
                .assertWarnings();
    }

    public void testEnhancedSwitchExpressionNoWarn2() throws Exception {
        HintTest.create()
                .sourceLevel(25)
                .input("""
                       package test;
                       import java.io.FileInputStream;
                       import java.io.IOException;

                       public class Test {
                           void test() throws Throwable {
                               try {
                                   new FileInputStream("/foo");
                               } catch (IOException ex) {
                                   Throwable t = switch (ex.getCause()) {
                                       case IOException ioe -> ioe;
                                       case Throwable tt -> throw tt;
                                   };
                               }
                           }
                       }
                       """)
                .run(ThrowableNotThrown.class)
                .assertWarnings();
    }

    public void testEnhancedSwitchExpressionWarn() throws Exception {
        HintTest.create()
                .sourceLevel(25)
                .input("""
                       package test;
                       import java.io.FileInputStream;
                       import java.io.IOException;

                       public class Test {
                           void test() throws Throwable {
                               try {
                                   new FileInputStream("/foo");
                               } catch (IOException ex) {
                                   Throwable t = switch (ex.getCause()) {
                                       case IOException ioe -> ioe;
                                       case Throwable tt -> tt;
                                   };
                               }
                           }
                       }
                       """)
                .run(ThrowableNotThrown.class)
                .assertWarnings("9:34-9:47:verifier:Throwable method result is ignored");
    }

}
