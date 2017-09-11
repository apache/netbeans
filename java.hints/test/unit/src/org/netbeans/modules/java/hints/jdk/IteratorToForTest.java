/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jdk;

import org.junit.Test;
import org.netbeans.modules.java.hints.test.api.HintTest;

public class IteratorToForTest {

    @Test public void whileWarning() throws Exception {
        HintTest.create().input("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    void m(List<String> strings) {\n"
                + "        Iterator it = strings.iterator();\n"
                + "        while (it.hasNext()) {\n"
                + "            String s = (String) it.next();\n"
                + "            System.out.println(s);\n"
                + "            System.err.println(s);\n"
                + "        }\n"
                + "    }\n"
                + "}\n").run(IteratorToFor.class).assertWarnings("4:8-4:13:verifier:" + Bundle.ERR_IteratorToFor());
    }

    @Test public void whileWarningSelf() throws Exception {
        HintTest.create().input("package test;\n"
                + "import java.util.*;"
                + "public class Test extends ArrayList<String> {\n"
                + "    void m() {\n"
                + "        Iterator it = iterator();\n"
                + "        while (it.hasNext()) {\n"
                + "            String s = (String) it.next();\n"
                + "            System.out.println(s);\n"
                + "            System.err.println(s);\n"
                + "        }\n"
                + "    }\n"
                + "}\n").run(IteratorToFor.class).findWarning("4:8-4:13:verifier:" + Bundle.ERR_IteratorToFor()).
                applyFix().assertOutput("package test;\n"
                + "import java.util.*;"
                + "public class Test extends ArrayList<String> {\n"
                + "    void m() {\n"
                + "        for (String s : this) {\n"
                + "            System.out.println(s);\n"
                + "            System.err.println(s);\n"
                + "        }\n"
                + "    }\n"
                + "}\n");
    }

    @Test public void whileUsedSpecially() throws Exception {
        HintTest.create().input("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    void m(List<String> strings) {\n"
                + "        Iterator it = strings.iterator();\n"
                + "        while (it.hasNext()) {\n"
                + "            String s = (String) it.next();\n"
                + "            if (s.isEmpty()) {\n"
                + "                it.remove();\n"
                + "            } else {\n"
                + "                System.out.println(s);\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}\n").run(IteratorToFor.class).assertWarnings();
    }

    @Test public void whileRaw() throws Exception {
        HintTest.create().input("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    void m(List strings) {\n"
                + "        Iterator it = strings.iterator();\n"
                + "        while (it.hasNext()) {\n"
                + "            String s = (String) it.next();\n"
                + "            System.out.println(s);\n"
                + "            System.err.println(s);\n"
                + "        }\n"
                + "    }\n"
                + "}\n").run(IteratorToFor.class).assertWarnings();
    }

    @Test public void whileWrongType() throws Exception {
        HintTest.create().input("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    void m(List<java.net.URL> strings) {\n"
                + "        Iterator it = strings.iterator();\n"
                + "        while (it.hasNext()) {\n"
                + "            String s = (String) it.next();\n"
                + "            System.out.println(s);\n"
                + "            System.err.println(s);\n"
                + "        }\n"
                + "    }\n"
                + "}\n").run(IteratorToFor.class).assertWarnings();
    }

    @Test public void whileNotRaw() throws Exception {
        HintTest.create().input("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    void m(MyList strings) {\n"
                + "        Iterator it = strings.iterator();\n"
                + "        while (it.hasNext()) {\n"
                + "            String s = (String) it.next();\n"
                + "            System.out.println(s);\n"
                + "            System.err.println(s);\n"
                + "        }\n"
                + "    }\n"
                + "    static class MyList extends ArrayList<String> {}\n"
                + "}\n").run(IteratorToFor.class).assertWarnings("4:8-4:13:verifier:" + Bundle.ERR_IteratorToFor());
    }

    @Test public void whileNotIterable() throws Exception {
        HintTest.create().input("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    void m(MyList strings) {\n"
                + "        Iterator it = strings.iterator();\n"
                + "        while (it.hasNext()) {\n"
                + "            String s = (String) it.next();\n"
                + "            System.out.println(s);\n"
                + "            System.err.println(s);\n"
                + "        }\n"
                + "    }\n"
                + "    interface MyList {\n"
                + "        Iterator<String> iterator();\n"
                + "    }\n"
                + "}\n").run(IteratorToFor.class).assertWarnings();
    }

    @Test public void whileSubtype() throws Exception {
        HintTest.create().input("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    void m(List<PropertyResourceBundle> bundles) {\n"
                + "        Iterator it = bundles.iterator();\n"
                + "        while (it.hasNext()) {\n"
                + "            ResourceBundle bundle = (ResourceBundle) it.next();\n"
                + "            System.out.println(bundle);\n"
                + "            System.err.println(bundle);\n"
                + "        }\n"
                + "    }\n"
                + "}\n").run(IteratorToFor.class).assertWarnings("4:8-4:13:verifier:" + Bundle.ERR_IteratorToFor());
    }

    @Test public void whileGenericSubtype() throws Exception {
        HintTest.create().input("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    void m(List<ArrayList<String>> lists) {\n"
                + "        Iterator it = lists.iterator();\n"
                + "        while (it.hasNext()) {\n"
                + "            List<String> list = (List<String>) it.next();\n"
                + "            System.out.println(list);\n"
                + "        }\n"
                + "    }\n"
                + "}\n").run(IteratorToFor.class).findWarning("4:8-4:13:verifier:" + Bundle.ERR_IteratorToFor()).
                applyFix().
                assertCompilable().
                assertOutput("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    void m(List<ArrayList<String>> lists) {\n"
                + "        for (List<String> list : lists) {\n"
                + "            System.out.println(list);\n"
                + "        }\n"
                + "    }\n"
                + "}\n");
    }

    @Test public void whileNotSubtype() throws Exception {
        HintTest.create().input("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    void m(List<ResourceBundle> bundles) {\n"
                + "        Iterator it = bundles.iterator();\n"
                + "        while (it.hasNext()) {\n"
                + "            PropertyResourceBundle bundle = (PropertyResourceBundle) it.next();\n"
                + "            System.out.println(bundle);\n"
                + "            System.err.println(bundle);\n"
                + "        }\n"
                + "    }\n"
                + "}\n").run(IteratorToFor.class).assertWarnings();
    }

    @Test public void whileFix() throws Exception {
        HintTest.create().input("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    void m(List<String> strings) {\n"
                + "        Collections.reverse(strings);\n"
                + "        Iterator it = strings.iterator();\n"
                + "        while (it.hasNext()) {\n"
                + "            String s = (String) it.next();\n"
                + "            System.out.println(s);\n"
                + "            // OK\n"
                + "            System.err.println(s);\n"
                + "        }\n"
                + "        System.out.println();\n"
                + "    }\n"
                + "}\n").run(IteratorToFor.class).findWarning("5:8-5:13:verifier:" + Bundle.ERR_IteratorToFor()).
                applyFix().
                assertCompilable()
                .assertOutput("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    void m(List<String> strings) {\n"
                + "        Collections.reverse(strings);\n"
                + "        for (String s : strings) {\n"
                + "            System.out.println(s);\n"
                + "            // OK\n"
                + "            System.err.println(s);\n"
                + "        }\n"
                + "        System.out.println();\n"
                + "    }\n"
                + "}\n");
    }

    @Test public void forWarning() throws Exception {
        HintTest.create().input("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    void m(List<String> strings) {\n"
                + "        for (Iterator it = strings.iterator(); it.hasNext(); ) {\n"
                + "            String s = (String) it.next();\n"
                + "            System.out.println(s);\n"
                + "            System.err.println(s);\n"
                + "        }\n"
                + "    }\n"
                + "}\n").run(IteratorToFor.class).assertWarnings("3:8-3:11:verifier:" + Bundle.ERR_IteratorToFor());
    }

    @Test public void forUsedSpecially() throws Exception {
        HintTest.create().input("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    void m(List<String> strings) {\n"
                + "        for (Iterator it = strings.iterator(); it.hasNext(); ) {\n"
                + "            String s = (String) it.next();\n"
                + "            if (s.isEmpty()) {\n"
                + "                it.remove();\n"
                + "            } else {\n"
                + "                System.out.println(s);\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}\n").run(IteratorToFor.class).assertWarnings();
    }

    @Test public void forRaw() throws Exception {
        HintTest.create().input("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    void m(List strings) {\n"
                + "        for (Iterator it = strings.iterator(); it.hasNext(); ) {\n"
                + "            String s = (String) it.next();\n"
                + "            System.out.println(s);\n"
                + "            System.err.println(s);\n"
                + "        }\n"
                + "    }\n"
                + "}\n").run(IteratorToFor.class).assertWarnings();
    }

    @Test public void forFix() throws Exception {
        HintTest.create().input("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    void m(List<String> strings) {\n"
                + "        Collections.reverse(strings);\n"
                + "        for (Iterator it = strings.iterator(); it.hasNext(); ) {\n"
                + "            String s = (String) it.next();\n"
                + "            System.out.println(s);\n"
                + "            // OK\n"
                + "            System.err.println(s);\n"
                + "        }\n"
                + "        System.out.println();\n"
                + "    }\n"
                + "}\n").run(IteratorToFor.class).findWarning("4:8-4:11:verifier:" + Bundle.ERR_IteratorToFor()).
                applyFix().
                assertCompilable()
                .assertOutput("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    void m(List<String> strings) {\n"
                + "        Collections.reverse(strings);\n"
                + "        for (String s : strings) {\n"
                + "            System.out.println(s);\n"
                + "            // OK\n"
                + "            System.err.println(s);\n"
                + "        }\n"
                + "        System.out.println();\n"
                + "    }\n"
                + "}\n");
    }
    
    @Test public void forForArray225914a() throws Exception {
        HintTest.create().input("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        for (int i = 0; i < args.length; i++) {\n"
                + "            System.err.println(args[i]);\n"
                + "        }\n"
                + "    }\n"
                + "}\n")
                .run(IteratorToFor.class)
                .findWarning("3:8-3:11:verifier:" + Bundle.ERR_IteratorToForArray())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        for (String arg : args) {\n"
                + "            System.err.println(arg);\n"
                + "        }\n"
                + "    }\n"
                + "}\n");
    }
    
    @Test public void forForArray225914b() throws Exception {
        HintTest.create().input("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        for (int i = 0; i < args.length; i++) {\n"
                + "            System.err.println(args[i] + args[i-1]);\n"
                + "        }\n"
                + "    }\n"
                + "}\n")
                .run(IteratorToFor.class)
                .assertWarnings();
    }
    
    @Test public void forForWithGenerics225914() throws Exception {
        HintTest.create().input("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        List<String> argsList = Arrays.asList(args);\n"
                + "        for (Iterator<String> it = argsList.iterator(); it.hasNext();) {\n"
                + "            String arg = it.next();\n"
                + "            System.err.println(arg);\n"
                + "        }\n"
                + "    }\n"
                + "}\n")
                .run(IteratorToFor.class)
                .findWarning("4:8-4:11:verifier:" + Bundle.ERR_IteratorToFor())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        List<String> argsList = Arrays.asList(args);\n"
                + "        for (String arg : argsList) {\n"
                + "            System.err.println(arg);\n"
                + "        }\n"
                + "    }\n"
                + "}\n");
    }
    
    @Test public void for232298a() throws Exception {
        HintTest.create().input("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        List<String> argsList = Arrays.asList(args);\n"
                + "        for (Iterator<String> it = argsList.iterator(); it.hasNext();) {\n"
                + "            String arg = it.next();\n"
                + "\n"
                + "            //a\n"
                + "            System.err.println(arg); //b\n"
                + "            //c\n"
                + "            System.err.println(arg); //d\n"
                + "            //e\n"
                + "        }\n"
                + "    }\n"
                + "}\n")
                .run(IteratorToFor.class)
                .findWarning("4:8-4:11:verifier:" + Bundle.ERR_IteratorToFor())
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        List<String> argsList = Arrays.asList(args);\n"
                + "        for (String arg : argsList) {\n"
                + "            //a\n"
                + "            System.err.println(arg); //b\n"
                + "            //c\n"
                + "            System.err.println(arg); //d\n"
                + "            //e\n"
                + "        }\n"
                + "    }\n"
                + "}\n");
    }
    
    @Test public void for232298b() throws Exception {
        HintTest.create().input("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        for (int i = 0; i < args.length; i++) {\n"
                + "            //a\n"
                + "            System.err.println(args[i]); //b\n"
                + "            //c\n"
                + "            System.err.println(args[i]); //d\n"
                + "            //e\n"
                + "        }\n"
                + "    }\n"
                + "}\n")
                .run(IteratorToFor.class)
                .findWarning("3:8-3:11:verifier:" + Bundle.ERR_IteratorToForArray())
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        for (String arg : args) {\n"
                + "            //a\n"
                + "            System.err.println(arg); //b\n"
                + "            //c\n"
                + "            System.err.println(arg); //d\n"
                + "            //e\n"
                + "        }\n"
                + "    }\n"
                + "}\n");
    }

    @Test public void forSingularName232718() throws Exception {
        HintTest.create().input("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    public static void main(String[] interfaces) {\n"
                + "        for (int i = 0; i < interfaces.length; i++) {\n"
                + "            System.err.println(interfaces[i]);\n"
                + "        }\n"
                + "    }\n"
                + "}\n")
                .run(IteratorToFor.class)
                .findWarning("3:8-3:11:verifier:" + Bundle.ERR_IteratorToForArray())
                .applyFix()
                .assertCompilable();//intentionally not testing the exact name
    }
    
    @Test public void forWriteToArray233017a() throws Exception {
        HintTest.create().input("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    public static void main(String[] interfaces) {\n"
                + "        for (int i = 0; i < interfaces.length; i++) {\n"
                + "            interfaces[i] = interfaces[i].trim();\n"
                + "        }\n"
                + "    }\n"
                + "}\n")
                .run(IteratorToFor.class)
                .assertWarnings();
    }
    
    @Test public void forWriteToArray233017b() throws Exception {
        HintTest.create().input("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    public static void main(int[] n) {\n"
                + "        for (int i = 0; i < n.length; i++) {\n"
                + "            n[i] = n[i] * 2;\n"
                + "        }\n"
                + "    }\n"
                + "}\n")
                .run(IteratorToFor.class)
                .assertWarnings();
    }
    
    @Test public void for234091a() throws Exception {
        HintTest.create()
                .input("package test;\n"
                     + "import java.util.*;\n"
                     + "public class Test {\n"
                     + "    public void method(String[] strings) {\n"
                     + "        for (int i = 0; i < strings.length; i++) {\n"
                     + "            String string = strings[i];\n"
                     + "            System.out.println(string);\n"
                     + "        }\n"
                     + "    }\n"
                     + "}\n")
                .run(IteratorToFor.class)
                .findWarning("4:8-4:11:verifier:" + Bundle.ERR_IteratorToForArray())
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                                    + "import java.util.*;\n"
                                    + "public class Test {\n"
                                    + "    public void method(String[] strings) {\n"
                                    + "        for (String string : strings) {\n"
                                    + "            System.out.println(string);\n"
                                    + "        }\n"
                                    + "    }\n"
                                    + "}\n");
    }

    @Test public void for234091b() throws Exception {
        HintTest.create()
                .input("package test;\n"
                     + "import java.util.*;\n"
                     + "public class Test {\n"
                     + "    public void method(String[] strings) {\n"
                     + "        for (int i = 0; i < strings.length; i++) {\n"
                     + "            System.out.println(strings[i]);\n"
                     + "            String string = strings[i];\n"
                     + "            System.out.println(string);\n"
                     + "        }\n"
                     + "    }\n"
                     + "}\n")
                .run(IteratorToFor.class)
                .findWarning("4:8-4:11:verifier:" + Bundle.ERR_IteratorToForArray())
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                                    + "import java.util.*;\n"
                                    + "public class Test {\n"
                                    + "    public void method(String[] strings) {\n"
                                    + "        for (String string1 : strings) {\n"
                                    + "            System.out.println(string1);\n"
                                    + "            String string = string1;\n"
                                    + "            System.out.println(string);\n"
                                    + "        }\n"
                                    + "    }\n"
                                    + "}\n");
    }

    // XXX also ought to match: for (Iterator i = coll.iterator(); i.hasNext(); ) {use((Type) i.next());}
    // XXX match final modifiers on iterator and/or element vars
    // XXX remove import of java.util.Iterator if present

}
