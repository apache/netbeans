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
/*
 * Contributor(s): Alexandru Gyori <Alexandru.Gyori at gmail.com>
 */
package org.netbeans.modules.java.hints.jdk.mapreduce;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.netbeans.modules.java.source.parsing.JavacParser;

/**
 *
 * @author alexandrugyori
 */
public class ForLoopToFunctionalHintTest extends NbTestCase {

    public ForLoopToFunctionalHintTest(String name) {
        super(name);
    }

    public void testSimpleConvert() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public void test(List<Integer> ls) {        \n"
                + "        for (Integer l : ls) \n"
                + "            System.out.println(l);\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("12:8-12:11:hint:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public void test(List<Integer> ls) {        \n"
                + "        ls.forEach(l -> { \n"
                + "            System.out.println(l);\n"
                + "        });\n"
                + "        \n"
                + "    }\n"
                + "}");
    }
    
    public void testDisableWhenNot8() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public void test(List<Integer> ls) {        \n"
                + "        for (Integer l : ls) \n"
                + "            System.out.println(l);\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.7")
                .run(ForLoopToFunctionalHint.class)
                .assertWarnings();
    }

    public void testChainingMapForEachcConvert() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public void test(List<Integer> ls) {        \n"
                + "        for (Integer l : ls) {\n"
                + "            String s = l.toString();\n"
                + "            System.out.println(s);\n"
                + "        }\n"
                + "            \n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("12:8-12:11:hint:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public void test(List<Integer> ls) {        \n"
                + "        ls.stream().map(l -> l.toString()).forEachOrdered(s -> {\n"
                + "            System.out.println(s);\n"
                + "        });\n"
                + "            \n"
                + "        \n"
                + "    }\n"
                + "}");
    }

    public void testDoubleIncrementReducer() throws Exception {
        HintTest.create()
                .input("/*\n"
                + " * To change this template, choose Tools | Templates\n"
                + " * and open the template in the editor.\n"
                + " */\n"
                + "package javaapplication4;\n"
                + "\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.List;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author alexandrugyori\n"
                + " */\n"
                + "class JavaApplication4 {\n"
                + "\n"
                + "    /**\n"
                + "     * @param args the command line arguments\n"
                + "     */\n"
                + "    public static void main( String[] args) {\n"
                + "        // TODO code application logic here\n"
                + "        List<Integer> ints=new ArrayList<>();\n"
                + "        double len=0.;        \n"
                + "        for(int i : ints)\n"
                + "            len++;\n"
                + "            \n"
                + "    }    \n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("22:8-22:11:hint:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("/*\n"
                + " * To change this template, choose Tools | Templates\n"
                + " * and open the template in the editor.\n"
                + " */\n"
                + "package javaapplication4;\n"
                + "\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.List;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author alexandrugyori\n"
                + " */\n"
                + "class JavaApplication4 {\n"
                + "\n"
                + "    /**\n"
                + "     * @param args the command line arguments\n"
                + "     */\n"
                + "    public static void main( String[] args) {\n"
                + "        // TODO code application logic here\n"
                + "        List<Integer> ints=new ArrayList<>();\n"
                + "        double len=0.;        \n"
                + "        len = ints.stream().map(_item -> 1.0).reduce(len, (accumulator, _item) -> accumulator + 1);\n"
                + "            \n"
                + "    }    \n"
                + "}");
    }

    public void testChainingFilterMapForEachConvert() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public void test(List<Integer> ls) {        \n"
                + "        for (Integer l : ls) {\n"
                + "            if(l!=null)\n"
                + "            {\n"
                + "                String s = l.toString();\n"
                + "                System.out.println(s);\n"
                + "            }\n"
                + "        }\n"
                + "            \n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("12:8-12:11:hint:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public void test(List<Integer> ls) {        \n"
                + "        ls.stream().filter(l -> (l!=null)).map(l -> l.toString()).forEachOrdered(s -> {\n"
                + "            System.out.println(s);\n"
                + "        });\n"
                + "            \n"
                + "        \n"
                + "    }\n"
                + "}");
    }

    public void testSmoothLongerChaining() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1,2,3));\n"
                + "    }\n"
                + "\n"
                + "    public void test(List<Integer> ls) {        \n"
                + "        for (Integer a : ls) {\n"
                + "            Integer l = new Integer(a.intValue());\n"
                + "            if(l!=null)\n"
                + "            {\n"
                + "                String s = l.toString();\n"
                + "                System.out.println(s);\n"
                + "            }\n"
                + "        }\n"
                + "            \n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("12:8-12:11:hint:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1,2,3));\n"
                + "    }\n"
                + "\n"
                + "    public void test(List<Integer> ls) {        \n"
                + "        ls.stream().map(a -> new Integer(a.intValue())).filter(l -> (l!=null)).map(l -> l.toString()).forEachOrdered(s -> {\n"
                + "            System.out.println(s);\n"
                + "        });\n"
                + "            \n"
                + "        \n"
                + "    }\n"
                + "}");
    }

    public void testNonFilteringIfChaining() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1,2,3));\n"
                + "    }\n"
                + "\n"
                + "    public void test(List<Integer> ls) {        \n"
                + "        for (Integer a : ls) {\n"
                + "            Integer l = new Integer(a.intValue());\n"
                + "            if(l!=null)\n"
                + "            {                \n"
                + "                String s = l.toString();\n"
                + "                if(s!=null)\n"
                + "                    System.out.println(s);\n"
                + "                System.out.println(\"cucu\");\n"
                + "            }\n"
                + "        }\n"
                + "            \n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("12:8-12:11:hint:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1,2,3));\n"
                + "    }\n"
                + "\n"
                + "    public void test(List<Integer> ls) {        \n"
                + "        ls.stream().map(a -> new Integer(a.intValue())).filter(l -> (l!=null)).map(l -> l.toString()).map(s -> {\n"
                + "            if(s!=null)\n"
                + "                System.out.println(s);\n"
                + "            return s;\n"
                + "        }).forEachOrdered(_item -> {\n"
                + "            System.out.println(\"cucu\");\n"
                + "        });\n"
                + "            \n"
                + "        \n"
                + "    }\n"
                + "}");
    }

    public void testContinuingIfFilterSingleStatement() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public void test(List<Integer> ls) {\n"
                + "        for (Integer l : ls) {            \n"
                + "            if (l == null) {\n"
                + "                continue;\n"
                + "            }\n"
                + "            String s = l.toString();\n"
                + "            if (s != null) {\n"
                + "                System.out.println(s);\n"
                + "            }     \n"
                + "\n"
                + "        }\n"
                + "\n"
                + "\n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("12:8-12:11:hint:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public void test(List<Integer> ls) {\n"
                + "        ls.stream().filter(l -> !(l == null)).map(l -> l.toString()).filter(s -> (s != null)).forEachOrdered(s -> {\n"
                + "            System.out.println(s);\n"
                + "        });\n"
                + "\n"
                + "\n"
                + "    }\n"
                + "}");
    }

    public void testChainedAnyMatch() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        for(Integer l:ls)\n"
                + "        {\n"
                + "            String s = l.toString();\n"
                + "            Object o = foo(s);\n"
                + "            if(o==null)\n"
                + "                return true;\n"
                + "        }\n"
                + "        \n"
                + "        return false;\n"
                + "\n"
                + "\n"
                + "    }\n"
                + "    \n"
                + "    Object foo(Object o)\n"
                + "    {\n"
                + "        return o;\n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("12:8-12:11:hint:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        if (ls.stream().map(l -> l.toString()).map(s -> foo(s)).anyMatch(o -> (o==null))) {\n"
                + "            return true;\n"
                + "        }\n"
                + "        \n"
                + "        return false;\n"
                + "\n"
                + "\n"
                + "    }\n"
                + "    \n"
                + "    Object foo(Object o)\n"
                + "    {\n"
                + "        return o;\n"
                + "    }\n"
                + "}");
    }

    public void testChainedNoneMatch() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        for(Integer l:ls)\n"
                + "        {\n"
                + "            String s = l.toString();\n"
                + "            Object o = foo(s);\n"
                + "            if(o==null)\n"
                + "                return false;\n"
                + "        }\n"
                + "        \n"
                + "        return true;\n"
                + "\n"
                + "\n"
                + "    }\n"
                + "    \n"
                + "    Object foo(Object o)\n"
                + "    {\n"
                + "        return o;\n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("12:8-12:11:hint:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        if (!ls.stream().map(l -> l.toString()).map(s -> foo(s)).noneMatch(o -> (o==null))) {\n"
                + "            return false;\n"
                + "        }\n"
                + "        \n"
                + "        return true;\n"
                + "\n"
                + "\n"
                + "    }\n"
                + "    \n"
                + "    Object foo(Object o)\n"
                + "    {\n"
                + "        return o;\n"
                + "    }\n"
                + "}");
    }

    public void testNoNeededVariablesMerging() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) throws Exception {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) throws Exception {\n"
                + "        Integer i=0;        \n"
                + "        for(Integer l : ls)\n"
                + "        {         \n"
                + "            System.out.println();\n"
                + "            System.out.println(\"\");\n"
                + "            \n"
                + "        }\n"
                + "        System.out.println(i);\n"
                + "        return false;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o, int i) throws Exception\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("14:8-14:11:hint:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) throws Exception {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) throws Exception {\n"
                + "        Integer i=0;        \n"
                + "        ls.stream().map(_item -> {         \n"
                + "            System.out.println();\n"
                + "            return _item;\n"
                + "        }).forEachOrdered(_item -> {\n"
                + "            System.out.println(\"\");\n"
                + "        });\n"
                + "        System.out.println(i);\n"
                + "        return false;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o, int i) throws Exception\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}");
    }

    public void testSomeChainingWithNoNeededVar() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        for(Integer a:ls)\n"
                + "        {\n"
                + "            Integer l = new Integer(a.intValue());\n"
                + "            if(l==null)\n"
                + "            {\n"
                + "                String s=l.toString();\n"
                + "                if(s!=null)\n"
                + "                {\n"
                + "                    System.out.println(s);\n"
                + "                }\n"
                + "                System.out.println(\"cucu\");\n"
                + "            }   \n"
                + "            System.out.println();\n"
                + "        }\n"
                + "        \n"
                + "        return true;\n"
                + "\n"
                + "\n"
                + "    }\n"
                + "        \n"
                + "    Object foo(Object o)\n"
                + "    {\n"
                + "        return o;\n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("12:8-12:11:hint:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        ls.stream().map(a -> new Integer(a.intValue())).map(l -> {\n"
                + "            if(l==null)\n"
                + "            {\n"
                + "                String s=l.toString();\n"
                + "                if(s!=null)\n"
                + "                {\n"
                + "                    System.out.println(s);\n"
                + "                }\n"
                + "                System.out.println(\"cucu\");\n"
                + "            }   \n"
                + "            return l;\n"
                + "        }).forEachOrdered(_item -> {\n"
                + "            System.out.println();\n"
                + "        });\n"
                + "        \n"
                + "        return true;\n"
                + "\n"
                + "\n"
                + "    }\n"
                + "        \n"
                + "    Object foo(Object o)\n"
                + "    {\n"
                + "        return o;\n"
                + "    }\n"
                + "}");
    }

    public void testSimpleReducer() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        Integer i=0;\n"
                + "        for(Integer l : ls)\n"
                + "            i++;\n"
                + "        System.out.println(i);\n"
                + "        return true;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("14:8-14:11:hint:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        Integer i=0;\n"
                + "        i = ls.stream().map(_item -> 1).reduce(i, Integer::sum);\n"
                + "        System.out.println(i);\n"
                + "        return true;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "}");
    }

    public void testChainedReducer() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        Integer i=0;\n"
                + "        for(Integer l : ls)\n"
                + "        {             \n"
                + "            if(l!=null)\n"
                + "            {\n"
                + "                foo(l);\n"
                + "                i++;\n"
                + "            }\n"
                + "            \n"
                + "        }\n"
                + "        System.out.println(i);\n"
                + "        return true;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o)\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("14:8-14:11:hint:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        Integer i=0;\n"
                + "        i = ls.stream().filter(l -> (l!=null)).map(l -> {\n"
                + "            foo(l);\n"
                + "            return l;\n"
                + "        }).map(_item -> 1).reduce(i, Integer::sum);\n"
                + "        System.out.println(i);\n"
                + "        return true;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o)\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}");
    }

    public void testChainedReducerWithMerging() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        Integer i=0;\n"
                + "        for(Integer l : ls)\n"
                + "        {        \n"
                + "            String s =l.toString();\n"
                + "            System.out.println(s);\n"
                + "            foo(l);\n"
                + "            if(l!=null)\n"
                + "            {\n"
                + "                foo(l);                \n"
                + "                i--;\n"
                + "            }\n"
                + "            \n"
                + "        }\n"
                + "        System.out.println(i);\n"
                + "        return true;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o)\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("14:8-14:11:hint:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        Integer i=0;\n"
                + "        i = ls.stream().map(l -> {        \n"
                + "            String s =l.toString();\n"
                + "            System.out.println(s);\n"
                + "            foo(l);\n"
                + "            return l;\n"
                + "        }).filter(l -> (l!=null)).map(l -> {\n"
                + "            foo(l);\n"
                + "            return l;\n"
                + "        }).map(_item -> 1).reduce(i, (accumulator, _item) -> accumulator - 1);\n"
                + "        System.out.println(i);\n"
                + "        return true;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o)\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}");
    }

    public void testIncrementReducer() throws Exception {
        HintTest.create()
                .input("package javatargettempapp;\n"
                + "\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.List;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author alexandrugyori\n"
                + " */\n"
                + "class JavaTargetTempApp {\n"
                + "\n"
                + "    /**\n"
                + "     * @param args the command line arguments\n"
                + "     */\n"
                + "    public static void main( String[] args) {\n"
                + "        List<Integer> ls = new ArrayList<>();\n"
                + "        int i =0;\n"
                + "        for ( Integer l : ls) {\n"
                + "            i+=1;        \n"
                + "        }\n"
                + "\n"
                + "    }\n"
                + "\n"
                + "    private static void foo(Integer l) {\n"
                + "        throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n"
                + "    }\n"
                + "}\n"
                + "")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("17:8-17:11:hint:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package javatargettempapp;\n"
                + "\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.List;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author alexandrugyori\n"
                + " */\n"
                + "class JavaTargetTempApp {\n"
                + "\n"
                + "    /**\n"
                + "     * @param args the command line arguments\n"
                + "     */\n"
                + "    public static void main( String[] args) {\n"
                + "        List<Integer> ls = new ArrayList<>();\n"
                + "        int i =0;\n"
                + "        i = ls.stream().map(_item -> 1).reduce(i, Integer::sum);\n"
                + "\n"
                + "    }\n"
                + "\n"
                + "    private static void foo(Integer l) {\n"
                + "        throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n"
                + "    }\n"
                + "}\n"
                + "");
    }

    public void testAccumulatingMapReduce() throws Exception {
        HintTest.create()
                .input("package javatargettempapp;\n"
                + "\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.List;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author alexandrugyori\n"
                + " */\n"
                + "class JavaTargetTempApp {\n"
                + "\n"
                + "    /**\n"
                + "     * @param args the command line arguments\n"
                + "     */\n"
                + "    public static void main( String[] args) {\n"
                + "        List<Integer> ls = new ArrayList<>();\n"
                + "        int i =0;\n"
                + "        for ( Integer l : ls) {\n"
                + "            i+=foo(l);        \n"
                + "        }\n"
                + "\n"
                + "    }\n"
                + "\n"
                + "    private static int foo(Integer l) {\n"
                + "        throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n"
                + "    }\n"
                + "}\n"
                + "")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("17:8-17:11:hint:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package javatargettempapp;\n"
                + "\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.List;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author alexandrugyori\n"
                + " */\n"
                + "class JavaTargetTempApp {\n"
                + "\n"
                + "    /**\n"
                + "     * @param args the command line arguments\n"
                + "     */\n"
                + "    public static void main( String[] args) {\n"
                + "        List<Integer> ls = new ArrayList<>();\n"
                + "        int i =0;\n"
                + "        i = ls.stream().map(l -> foo(l)).reduce(i, Integer::sum);\n"
                + "\n"
                + "    }\n"
                + "\n"
                + "    private static int foo(Integer l) {\n"
                + "        throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n"
                + "    }\n"
                + "}\n"
                + "");
    }

    public void testStringConcat() throws Exception {
        HintTest.create()
                .input("package javatargettempapp;\n"
                + "\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.List;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author alexandrugyori\n"
                + " */\n"
                + "class JavaTargetTempApp {\n"
                + "\n"
                + "    /**\n"
                + "     * @param args the command line arguments\n"
                + "     */\n"
                + "    public static void main( String[] args) {\n"
                + "        List<Integer> ls = new ArrayList<>();\n"
                + "        String i =\"\";\n"
                + "        for ( Integer l : ls) {\n"
                + "            i+=foo(l);        \n"
                + "        }\n"
                + "\n"
                + "    }\n"
                + "\n"
                + "    private static String foo(Integer l) {\n"
                + "        throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n"
                + "    }\n"
                + "}\n"
                + "")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("17:8-17:11:hint:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package javatargettempapp;\n"
                + "\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.List;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author alexandrugyori\n"
                + " */\n"
                + "class JavaTargetTempApp {\n"
                + "\n"
                + "    /**\n"
                + "     * @param args the command line arguments\n"
                + "     */\n"
                + "    public static void main( String[] args) {\n"
                + "        List<Integer> ls = new ArrayList<>();\n"
                + "        String i =\"\";\n"
                + "        i = ls.stream().map(l -> foo(l)).reduce(i, String::concat);\n"
                + "\n"
                + "    }\n"
                + "\n"
                + "    private static String foo(Integer l) {\n"
                + "        throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n"
                + "    }\n"
                + "}\n"
                + "");
    }

    public void testMergingOperations() throws Exception {
        HintTest.create()
                .input("/*\n"
                + " * To change this template, choose Tools | Templates\n"
                + " * and open the template in the editor.\n"
                + " */\n"
                + "package javaapplication1;\n"
                + "\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.List;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author alexandrugyori\n"
                + " */\n"
                + "class JavaApplication1 {\n"
                + "\n"
                + "    /**\n"
                + "     * @param args the command line arguments\n"
                + "     */\n"
                + "    public boolean b() {\n"
                + "        // TODO code application logic here\n"
                + "        List<String> strs = new ArrayList<String>();\n"
                + "        int i = 0;\n"
                + "        int j = 0;\n"
                + "        for(String str: strs)\n"
                + "        {            \n"
                + "            int len1=str.length();\n"
                + "            int len2 = str.length();\n"
                + "            if(len1%2==0){\n"
                + "                len2++;\n"
                + "                System.out.println(len2);\n"
                + "                System.out.println();\n"
                + "            }\n"
                + "            \n"
                + "        }\n"
                + "        return false;\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("23:8-23:11:hint:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("/*\n"
                + " * To change this template, choose Tools | Templates\n"
                + " * and open the template in the editor.\n"
                + " */\n"
                + "package javaapplication1;\n"
                + "\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.List;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author alexandrugyori\n"
                + " */\n"
                + "class JavaApplication1 {\n"
                + "\n"
                + "    /**\n"
                + "     * @param args the command line arguments\n"
                + "     */\n"
                + "    public boolean b() {\n"
                + "        // TODO code application logic here\n"
                + "        List<String> strs = new ArrayList<String>();\n"
                + "        int i = 0;\n"
                + "        int j = 0;\n"
                + "        strs.forEach(str -> {            \n"
                + "            int len1=str.length();\n"
                + "            int len2 = str.length();\n"
                + "            if (len1%2==0) {\n"
                + "                len2++;\n"
                + "                System.out.println(len2);\n"
                + "                System.out.println();\n"
                + "            }\n"
                + "        });\n"
                + "        return false;\n"
                + "        \n"
                + "    }\n"
                + "}");
    }

    public void testBeautificationWorks() throws Exception {
        HintTest.create()
                .input("/*\n"
                + " * To change this template, choose Tools | Templates\n"
                + " * and open the template in the editor.\n"
                + " */\n"
                + "package javaapplication1;\n"
                + "\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.List;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author alexandrugyori\n"
                + " */\n"
                + "class JavaApplication1 {\n"
                + "\n"
                + "    /**\n"
                + "     * @param args the command line arguments\n"
                + "     */\n"
                + "    public boolean b() {\n"
                + "        // TODO code application logic here\n"
                + "        List<String> strs = new ArrayList<String>();\n"
                + "        int i = 0;\n"
                + "        int j = 0;\n"
                + "        for(String str: strs)\n"
                + "        {            \n"
                + "            String s = \"foo\";\n"
                + "            s=s.toString();\n"
                + "            System.out.println(s);\n"
                + "            \n"
                + "        }\n"
                + "        return false;\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("23:8-23:11:hint:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("/*\n"
                + " * To change this template, choose Tools | Templates\n"
                + " * and open the template in the editor.\n"
                + " */\n"
                + "package javaapplication1;\n"
                + "\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.List;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author alexandrugyori\n"
                + " */\n"
                + "class JavaApplication1 {\n"
                + "\n"
                + "    /**\n"
                + "     * @param args the command line arguments\n"
                + "     */\n"
                + "    public boolean b() {\n"
                + "        // TODO code application logic here\n"
                + "        List<String> strs = new ArrayList<String>();\n"
                + "        int i = 0;\n"
                + "        int j = 0;\n"
                + "        strs.stream().map(_item -> \"foo\").map(s -> s.toString()).forEachOrdered(s -> {\n"
                + "            System.out.println(s);\n"
                + "        });\n"
                + "        return false;\n"
                + "        \n"
                + "    }\n"
                + "}");
    }

    public void testBeautificationWorks2() throws Exception {
        HintTest.create()
                .input("/*\n"
                + " * To change this template, choose Tools | Templates\n"
                + " * and open the template in the editor.\n"
                + " */\n"
                + "package javaapplication1;\n"
                + "\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.List;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author alexandrugyori\n"
                + " */\n"
                + "class JavaApplication1 {\n"
                + "\n"
                + "    /**\n"
                + "     * @param args the command line arguments\n"
                + "     */\n"
                + "    public boolean b() {\n"
                + "        // TODO code application logic here\n"
                + "        List<String> strs = new ArrayList<String>();\n"
                + "        int i = 0;\n"
                + "        int j = 0;\n"
                + "        for(String str: strs)\n"
                + "        {            \n"
                + "            String s = \"foo\";\n"
                + "            s=s.toString();\n"
                + "            System.out.println();\n"
                + "            \n"
                + "        }\n"
                + "        return false;\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("23:8-23:11:hint:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("/*\n"
                + " * To change this template, choose Tools | Templates\n"
                + " * and open the template in the editor.\n"
                + " */\n"
                + "package javaapplication1;\n"
                + "\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.List;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author alexandrugyori\n"
                + " */\n"
                + "class JavaApplication1 {\n"
                + "\n"
                + "    /**\n"
                + "     * @param args the command line arguments\n"
                + "     */\n"
                + "    public boolean b() {\n"
                + "        // TODO code application logic here\n"
                + "        List<String> strs = new ArrayList<String>();\n"
                + "        int i = 0;\n"
                + "        int j = 0;\n"
                + "        strs.stream().map(_item -> \"foo\").map(s -> s.toString()).forEachOrdered(_item -> {\n"
                + "            System.out.println();\n"
                + "        });\n"
                + "        return false;\n"
                + "        \n"
                + "    }\n"
                + "}");
    }

    public void testDecrementingReducer() throws Exception {
        HintTest.create()
                .input("/*\n"
                + " * To change this template, choose Tools | Templates\n"
                + " * and open the template in the editor.\n"
                + " */\n"
                + "package javaapplication1;\n"
                + "\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.List;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author alexandrugyori\n"
                + " */\n"
                + "class JavaApplication1 {\n"
                + "\n"
                + "    /**\n"
                + "     * @param args the command line arguments\n"
                + "     */\n"
                + "    public boolean b() {\n"
                + "        // TODO code application logic here\n"
                + "        List<String> strs = new ArrayList<String>();\n"
                + "        int i = 0;\n"
                + "        int j = 0;\n"
                + "        for(String str : strs)\n"
                + "            i-=1;\n"
                + "        return false;\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("23:8-23:11:hint:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("/*\n"
                + " * To change this template, choose Tools | Templates\n"
                + " * and open the template in the editor.\n"
                + " */\n"
                + "package javaapplication1;\n"
                + "\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.List;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author alexandrugyori\n"
                + " */\n"
                + "class JavaApplication1 {\n"
                + "\n"
                + "    /**\n"
                + "     * @param args the command line arguments\n"
                + "     */\n"
                + "    public boolean b() {\n"
                + "        // TODO code application logic here\n"
                + "        List<String> strs = new ArrayList<String>();\n"
                + "        int i = 0;\n"
                + "        int j = 0;\n"
                + "        i = strs.stream().map(_item -> 1).reduce(i, (accumulator, _item) -> accumulator - _item);\n"
                + "        return false;\n"
                + "        \n"
                + "    }\n"
                + "}");
    }

    public void testWithArrays() throws Exception {
        HintTest.create()
                .input("/*\n"
                + " * To change this template, choose Tools | Templates\n"
                + " * and open the template in the editor.\n"
                + " */\n"
                + "package javaapplication1;\n"
                + "\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.List;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author alexandrugyori\n"
                + " */\n"
                + "class JavaApplication1 {\n"
                + "\n"
                + "    /**\n"
                + "     * @param args the command line arguments\n"
                + "     */\n"
                + "    public boolean b() {\n"
                + "        // TODO code application logic here\n"
                + "        String[] strs = new String[10];\n"
                + "        int i = 0;\n"
                + "        int j = 0;\n"
                + "        for(String str:strs)\n"
                + "            i++;\n"
                + "        return false;\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .assertNotContainsWarnings("23:8-23:11:hint:" + Bundle.ERR_ForLoopToFunctionalHint());
    }

    public void testNoHintDueToNEF() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        Integer i=0;\n"
                + "        for(Integer l : ls)\n"
                + "        {        \n"
                + "            String s =l.toString();\n"
                + "            System.out.println(s);\n"
                + "            foo(l,i);            \n"
                + "            if(l!=null)\n"
                + "            {                           \n"
                + "                i++;\n"
                + "            }\n"
                + "            \n"
                + "        }\n"
                + "        System.out.println(i);\n"
                + "        return true;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o, int i)\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .assertWarnings();
    }

    public void testNoHintDueToBreak() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        Integer i=0;\n"
                + "        for(Integer l : ls)\n"
                + "        {                      \n"
                + "            if(l!=null)\n"
                + "            {                           \n"
                + "                break;\n"
                + "            }\n"
                + "            \n"
                + "        }\n"
                + "        System.out.println(i);\n"
                + "        return true;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o, int i)\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .assertWarnings();
    }

    public void testNoHintDueToReturnInt() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public int test(List<Integer> ls) {\n"
                + "        Integer i=0;\n"
                + "        for(Integer l : ls)\n"
                + "        {                      \n"
                + "            if(l!=null)\n"
                + "            {                           \n"
                + "                return 0;\n"
                + "            }\n"
                + "            \n"
                + "        }\n"
                + "        System.out.println(i);\n"
                + "        return 1;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o, int i)\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .assertWarnings();
    }

    public void testNoHintDueToMultipleReturnBoolean() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        Integer i=0;\n"
                + "        for(Integer l : ls)\n"
                + "        {                      \n"
                + "            if(l==null)\n"
                + "            {                           \n"
                + "                return true;\n"
                + "            }\n"
                + "            if(l.toString()==null)\n"
                + "                return true;\n"
                + "            \n"
                + "        }\n"
                + "        System.out.println(i);\n"
                + "        return false;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o, int i)\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .assertWarnings();
    }

    public void testNoHintDueToLabeledContinue() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        Integer i=0;\n"
                + "        label:\n"
                + "        for(Integer l : ls)\n"
                + "        {                      \n"
                + "            if(l==null)\n"
                + "            {                           \n"
                + "                continue label;\n"
                + "            }\n"
                + "            if(l.toString()==null)\n"
                + "                return true;\n"
                + "            \n"
                + "        }\n"
                + "        System.out.println(i);\n"
                + "        return false;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o, int i)\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .assertWarnings();
    }

    public void testNoHintDueToNonEliminableContinue() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        Integer i=0;\n"
                + "        \n"
                + "        for(Integer l : ls)\n"
                + "        {                      \n"
                + "            if(l==null)\n"
                + "            {                           \n"
                + "                continue;\n"
                + "            }\n"
                + "            else if(l.toString()==null)\n"
                + "                return true;\n"
                + "            \n"
                + "        }\n"
                + "        System.out.println(i);\n"
                + "        return false;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o, int i)\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .assertWarnings("15:8-15:11:hint:" + Bundle.ERR_ForLoopToFunctionalHint());
    }

    public void testNoHintDueToMethodThrowingException() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) throws Exception {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) throws Exception {\n"
                + "        Integer i=0;\n"
                + "        \n"
                + "        for(Integer l : ls)\n"
                + "        {         \n"
                + "            foo(l,1);\n"
                + "            if(l==null)\n"
                + "            {                           \n"
                + "                continue;\n"
                + "            }\n"
                + "            else if(l.toString()==null)\n"
                + "                return true;\n"
                + "            \n"
                + "        }\n"
                + "        System.out.println(i);\n"
                + "        return false;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o, int i) throws Exception\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .assertWarnings();
    }

    public void testNoHintDueToExplicitThrow() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) throws Exception {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) throws Exception {\n"
                + "        Integer i=0;\n"
                + "        \n"
                + "        for(Integer l : ls)\n"
                + "        {         \n"
                + "            throw new Exception();            \n"
                + "            \n"
                + "        }\n"
                + "        System.out.println(i);\n"
                + "        return false;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o, int i) throws Exception\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .assertWarnings();
    }
    
    public void testNPEForReturnWithExpressions() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.util.List;" +
                       "class Test {\n" +
                       "    public void test(List<Integer> ls) throws Exception {\n" +
                       "        for(Integer l : ls) {\n" +
                       "            return ;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .assertWarnings();
    }
    
    public void testNoHintDueToReducers() throws Exception {
        HintTest.create()
                .input("/*\n"
                + " * To change this template, choose Tools | Templates\n"
                + " * and open the template in the editor.\n"
                + " */\n"
                + "package javaapplication1;\n"
                + "\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.List;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author alexandrugyori\n"
                + " */\n"
                + "class JavaApplication1 {\n"
                + "\n"
                + "    /**\n"
                + "     * @param args the command line arguments\n"
                + "     */\n"
                + "    public static void main(String[] args) {\n"
                + "        // TODO code application logic here\n"
                + "        List<String> strs = new ArrayList<String>();\n"
                + "        int i = 0;\n"
                + "        int j = 0;\n"
                + "        for(String str: strs)\n"
                + "        {\n"
                + "            if(str!=null){\n"
                + "                str.toString();\n"
                + "            i++;\n"
                + "            j++;\n"
                + "            }\n"
                + "            //j++;\n"
                + "        }\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .assertNotContainsWarnings("23:8-23:11:hint:Can use functional operation");

    }

    //Check this test, this should fail.
    public void testNoHintDueToReturningIf() throws Exception {
        HintTest.create()
                .input("/*\n"
                + " * To change this template, choose Tools | Templates\n"
                + " * and open the template in the editor.\n"
                + " */\n"
                + "package javaapplication1;\n"
                + "\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.List;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author alexandrugyori\n"
                + " */\n"
                + "class JavaApplication1 {\n"
                + "\n"
                + "    /**\n"
                + "     * @param args the command line arguments\n"
                + "     */\n"
                + "    public boolean b() {\n"
                + "        // TODO code application logic here\n"
                + "        List<String> strs = new ArrayList<String>();\n"
                + "        int i = 0;\n"
                + "        int j = 0;\n"
                + "        for(String str: strs)\n"
                + "        {            \n"
                + "            if(str!=null){\n"
                + "                return true;\n"
                + "            }\n"
                + "            System.out.println(\"gugu\");\n"
                + "        }\n"
                + "        return false;\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .assertNotContainsWarnings("23:8-23:11:hint:Can use functional operation");

    }

    public void testNoHintDueToReturnNothing() throws Exception {
        HintTest.create()
                .input("/*\n"
                + " * To change this template, choose Tools | Templates\n"
                + " * and open the template in the editor.\n"
                + " */\n"
                + "package javaapplication1;\n"
                + "\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.List;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author alexandrugyori\n"
                + " */\n"
                + "class JavaApplication1 {\n"
                + "\n"
                + "    /**\n"
                + "     * @param args the command line arguments\n"
                + "     */\n"
                + "    public void b() {\n"
                + "        // TODO code application logic here\n"
                + "        List<String> strs = new ArrayList<String>();\n"
                + "        int i = 0;\n"
                + "        int j = 0;\n"
                + "        for(String str: strs)\n"
                + "        {            \n"
                + "            if(str!=null){\n"
                + "                return;\n"
                + "            }\n"
                + "        }\n"
                + "        return;\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .assertNotContainsWarnings("23:8-23:11:hint:Can use functional operation");

    }
    
    {
        //to ensure the tests can run against JDK7:
        ForLoopToFunctionalHint.DISABLE_CHECK_FOR_STREAM = true;
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
}
