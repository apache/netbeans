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
}
