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

    @Test public void whileWithGenericIterator() throws Exception {
        HintTest.create().input("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    void m(List<String> strings) {\n"
                + "        Iterator<String> it = strings.iterator();\n"
                + "        while (it.hasNext()) {\n"
                + "            String str = it.next();\n"
                + "            System.out.println(str);\n"
                + "        }\n"
                + "    }\n"
                + "}\n").run(IteratorToFor.class).findWarning("4:8-4:13:verifier:" + Bundle.ERR_IteratorToFor()).
                applyFix().
                assertCompilable().
                assertOutput("package test;\n"
                + "import java.util.*;"
                + "public class Test {\n"
                + "    void m(List<String> strings) {\n"
                + "        for (String str : strings) {\n"
                + "            System.out.println(str);\n"
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
