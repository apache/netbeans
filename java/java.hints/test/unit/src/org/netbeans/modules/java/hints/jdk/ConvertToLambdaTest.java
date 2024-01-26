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
/*
 * Contributor(s): Lyle Franklin <lylejfranklin@gmail.com>
 */

package org.netbeans.modules.java.hints.jdk;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.spiimpl.TestCompilerSettings;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.netbeans.modules.java.source.parsing.JavacParser;

/**
 *
 * @author lahvac
 */
public class ConvertToLambdaTest extends NbTestCase {
    
    private static final String lambdaConvDesc = "MSG_AnonymousConvertibleToLambda";
    private static final String lambdaConvWarning = "verifier:" + lambdaConvDesc;
    private static final String lambdaFix = "FIX_ConvertToLambda";
    private static final String memberReferenceFix = "FIX_ConvertToMemberReference";

    public ConvertToLambdaTest(String name) {
        super(name);
    }

    public void testConversionForStatementLambda() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "import javax.swing.SwingUtilities;\n" +
                       "public class Test {\n" +
                       "    {\n" + 
                       "        SwingUtilities.invokeLater(new Runnable() {\n" +
                       "           public void run() { System.err.println(1); }\n" +
                       "        });\n" +
                       "    }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("4:39-4:47:" + lambdaConvWarning)
                .applyFix()
                .assertOutput("package test;\n" +
                       "import javax.swing.SwingUtilities;\n" +
                       "public class Test {\n" +
                       "    {\n" + 
                       "        SwingUtilities.invokeLater(() -> {\n" +
                       "           System.err.println(1);\n" +
                       "        });\n" +
                       "    }\n" +
                       "}\n");
    }
    
    public void testConversionForExpressionLambda() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "import java.util.*;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        Comparator<String> comp = new Comparator<String>() {\n" +
                       "            public int compare(String s0, String s1) { return s0.compareTo(s1); }\n" +
                       "        };\n" +
                       "    }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("4:38-4:56:" + lambdaConvWarning)
                .applyFix(lambdaFix)
                .assertOutput("package test;\n" +
                       "import java.util.*;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        Comparator<String> comp = (String s0, String s1) -> s0.compareTo(s1);\n" +
                       "    }\n" +
                       "}\n");
    }
    
    public void testConversionForMemberReference() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "import java.util.*;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        Comparator<String> comp = new Comparator<String>() {\n" +
                       "            public int compare(String s0, String s1) { return s0.compareTo(s1); }\n" +
                       "        };\n" +
                       "    }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("4:38-4:56:" + lambdaConvWarning)
                .applyFix(memberReferenceFix)
                .assertOutput("package test;\n" +
                       "import java.util.*;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        Comparator<String> comp = String::compareTo;\n" +
                       "    }\n" +
                       "}\n");
    }
    
    public void testConversionForStaticMemberReference() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "import java.util.*;\n" +
                       "public class Test {\n" +
                       "    public static int doCompare(String s1, String s2) { return 0; }\n" +
                       "    {\n" +
                       "        Comparator<String> comp = new Comparator<String>() {\n" +
                       "            public int compare(String s0, String s1) { return doCompare(s0, s1); }\n" +
                       "        };\n" +
                       "    }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("5:38-5:56:" + lambdaConvWarning)
                .applyFix(memberReferenceFix)
                .assertOutput("package test;\n" +
                       "import java.util.*;\n" +
                       "public class Test {\n" +
                       "    public static int doCompare(String s1, String s2) { return 0; }\n" +
                       "    {\n" +
                       "        Comparator<String> comp = Test::doCompare;\n" +
                       "    }\n" +
                       "}\n");
    }
    
    public void testConvertToConstructorReference() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                        "import java.util.function.Supplier;\n" +
                        "public class Test {\n" +
                        "    static class R implements Runnable { public void run() {}}\n" +
                        "    public void test() {\n" +
                        "        Supplier<Runnable> sr = new Supplier<Runnable>() {\n" +
                        "            @Override\n" +
                        "            public Runnable get() {\n" +
                        "                return new R();\n" +
                        "            }\n" +
                        "        };\n" +
                        "    }\n" +
                        "}")
                .run(ConvertToLambda.class)
                .findWarning("5:36-5:54:" + lambdaConvWarning)
                .applyFix(memberReferenceFix)
                .assertOutput("package test;\n"
                        + "import java.util.function.Supplier;\n"
                        + "public class Test {\n"
                        + "    static class R implements Runnable { public void run() {}}\n"
                        + "    public void test() {\n"
                        + "        Supplier<Runnable> sr = R::new;\n"
                        + "    }\n"
                        + "}");
    }
    
    public void testNoMemberReferenceForExplicitInstance() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n"
                        + "import java.util.function.Supplier;\n"
                        + "public class Test {\n"
                        + "    class R implements Runnable { public void run() {}}\n"
                        + "    public static void test(Test inst) {\n"
                        + "        Supplier<Runnable> sr = new Supplier<Runnable>() {\n"
                        + "            @Override\n"
                        + "            public Runnable get() {\n"
                        + "                return inst.new R();\n"
                        + "            }\n"
                        + "        };\n"
                        + "    }\n"
                        + "}")
                .run(ConvertToLambda.class)
                .findWarning("5:36-5:54:" + lambdaConvWarning)
                .assertFixesNotPresent(memberReferenceFix)
                .applyFix(lambdaFix)
                .assertOutput("package test;\n"
                        + "import java.util.function.Supplier;\n"
                        + "public class Test {\n"
                        + "    class R implements Runnable { public void run() {}}\n"
                        + "    public static void test(Test inst) {\n"
                        + "        Supplier<Runnable> sr = () -> inst.new R();\n"
                        + "    }\n"
                        + "}");
    }
    
    public void testThatSiteIsIgnoredWhenNoTypeIsFound() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        new Runnable() {\n" +
                       "           public void run() { System.err.println(1); }\n" +
                       "        }.run();\n" +
                       "    }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .assertWarnings();
    }

    public void testThatCastIsAddedWhenAssigningToTypeObject() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        Object obj = new Runnable() { public void run() { System.err.println(1); } };" +
                       "    }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("3:25-3:33:" + lambdaConvWarning)
                .applyFix()
                .assertOutput("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        Object obj = (Runnable) () -> { System.err.println(1); };" +
                       "    }\n" +
                       "}\n");
    }
    
    public void testThatCastIsAddedWhenReturningTypeObject() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private Object getObjectLambda() {\n" +
                       "        return new Runnable() {\n" +
                       "            public void run() { System.err.println(1); }\n" +
                       "        };\n" +
                       "    }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("3:19-3:27:" + lambdaConvWarning)
                .applyFix()
                .assertOutput("package test;\n" +
                       "public class Test {\n" +
                       "    private Object getObjectLambda() {\n" +
                       "        return (Runnable) () ->\n" +
                       "            { System.err.println(1); };\n" +
                       "    }\n" +
                       "}\n");
    }
    
    public void testThatCastIsAddedWhenPassingTypeObject() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        passObjectLambda(new Runnable() {\n" +
                       "            public void run() { System.err.println(1); }\n" +
                       "        });\n" +
                       "    }\n" +
                       "    private void passObjectLambda(Object obj) { }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("3:29-3:37:" + lambdaConvWarning)
                .applyFix()
                .assertOutput("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        passObjectLambda((Runnable) () ->\n" +
                       "            { System.err.println(1); });\n" +
                       "    }\n" +
                       "    private void passObjectLambda(Object obj) { }\n" +
                       "}\n");
    }
    
    public void testThatCastIsIfAssignedToRaw() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        Comparable c = new Comparable<String>(){            \n" +
                       "            @Override\n" +
                       "            public int compareTo(String o) {\n" +
                       "                return 1;\n" +
                       "            }            \n" +
                       "        };" +
                       "    }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("3:27-3:45:" + lambdaConvWarning)
                .applyFix()
                .assertOutput("package test;\n" +
                    "public class Test {\n" +
                       "    {\n" +
                       "        Comparable c = (Comparable<String>) (String o) -> 1;\n" +
                       "    }\n" +
                       "}\n");
       }

    public void testThatSiteWithinConstrIsConverted() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        new Thread(new Runnable() {\n" +
                       "          public void run() { System.err.println(1); }\n" +
                       "        }).start();\n" +
                       "    }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("3:23-3:31:" + lambdaConvWarning)
                .applyFix()
                .assertOutput("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        new Thread(() -> {\n" +
                       "           System.err.println(1);\n" +
                       "        }).start();\n" +
                       "    }\n" +
                       "}\n");
    }
    
    public void testThatSiteIsIgnoredWhenThisIsFound() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        Runnable r = new Runnable() {\n" +
                       "           public void run() {\n" + 
                       "              System.err.println(this.toString());\n" +
                       "           }\n" + 
                       "        };\n" +
                       "    }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .assertWarnings();
    }
    
    public void testThatSiteIsIgnoredWhenSuperIsFound() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        Runnable r = new Runnable() {\n" +
                       "           public void run() {\n" + 
                       "              System.err.println(super.toString());\n" +
                       "           }\n" + 
                       "        };\n" +
                       "    }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .assertWarnings();
    }
    
    public void testThatSiteContainingBasicRecursionIsIgnored() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        VoidInterface recursive = new VoidInterface() {\n" +
                       "            public void print() { print(); }\n" +
                       "        };\n" +
                       "    }\n" +
                       "    private interface VoidInterface { public void print(); }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .assertWarnings();
    }
    
    public void testThatAicFromNonInterfaceIsIgnored() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        AbstractClass ac = new AbstractClass() {\n" +
                       "            public void print() { System.err.println(1); }\n" +
                       "        };\n" +
                       "    }\n" +
                       "    private abstract class AbstractClass { public abstract void print(); }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .assertWarnings();
    }
    
    public void testThatSiteContainingSameMethodNameIsConverted() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        VoidInterface notRecursive = new VoidInterface() {\n" +
                       "            public void print() { System.out.print(\"\"); }\n" +
                       "        };\n" +
                       "    }\n" +
                       "    private interface VoidInterface { public void print(); }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("3:41-3:54:" + lambdaConvWarning)
                .applyFix()
                .assertOutput("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        VoidInterface notRecursive = () -> {\n" +
                       "            System.out.print(\"\");\n" +
                       "        };\n" +
                       "    }\n" +
                       "    private interface VoidInterface { public void print(); }\n" +
                       "}\n");
    }
    
    public void testThatCastIsAddedWhenAssigningToParentInterface() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        ParentInterface parent = new SubInterface() {\n" +
                       "            public void print() { System.out.print(\"\"); }\n" +
                       "        };\n" +
                       "    }\n" +
                       "    private interface ParentInterface { }\n" +
                       "    private interface SubInterface extends ParentInterface {\n" +
                       "        public void print();\n" +
                       "    }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("3:37-3:49:" + lambdaConvWarning)
                .applyFix()
                .assertOutput("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        ParentInterface parent = (SubInterface) () -> {\n" +
                       "            System.out.print(\"\");\n" +
                       "        };\n" +
                       "    }\n" +
                       "    private interface ParentInterface { }\n" +
                       "    private interface SubInterface extends ParentInterface {\n" +
                       "        public void print();\n" +
                       "    }\n" +
                       "}\n");
    }
    
    public void testThatShadowedVariableIsRenamed() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        int z = 0;\n" +
                       "        Runnable shadowed = new Runnable() {\n" +
                       "            public void run() {\n" +
                       "               int z = 1;\n" +
                       "               System.out.print(z);\n" +
                       "            }\n" +
                       "        };\n" +
                       "    }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("4:32-4:40:" + lambdaConvWarning)
                .applyFix()
                .assertOutput("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        int z = 0;\n" +
                       "        Runnable shadowed = () -> {\n" +
                       "           int z1 = 1;\n" +
                       "           System.out.print(z1);\n" +
                       "        };\n" +
                       "    }\n" +
                       "}\n");
    }
    
    public void testThatShadowedVariableIsRenamedInMethodInvoke() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        int z = 0;\n" +
                       "        runRunnable(new Runnable() {\n" +
                       "            public void run() {\n" +
                       "               int z = 1;\n" +
                       "               System.out.print(z);\n" +
                       "            }\n" +
                       "        });\n" +
                       "    }\n" +
                       "    public void runRunnable(Runnable runnable) { runnable.run(); }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("4:24-4:32:" + lambdaConvWarning)
                .applyFix()
                .assertOutput("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        int z = 0;\n" +
                       "        runRunnable(() -> {\n" +
                       "            int z1 = 1;\n" +
                       "            System.out.print(z1);\n" +
                       "        });\n" +
                       "    }\n" +
                       "    public void runRunnable(Runnable runnable) { runnable.run(); }\n" +
                       "}\n");
    }
    
    public void testThatShadowedVariableIsRenamedInNestedScope() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        int z = 0;\n" +
                       "        Runnable shadowed = new Runnable() {\n" +
                       "            public void run() {\n" +
                       "               for (int z = 0; z < 1; z++) {\n" +
                       "                  System.out.print(z);\n" + 
                       "               }\n" +
                       "               System.out.print(z);\n" +
                       "            }\n" +
                       "        };\n" +
                       "    }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("4:32-4:40:" + lambdaConvWarning)
                .applyFix()
                .assertOutput("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        int z = 0;\n" +
                       "        Runnable shadowed = () -> {\n" +
                       "           for (int z1 = 0; z1 < 1; z1++) {\n" +
                       "              System.out.print(z1);\n" + 
                       "           }\n" +
                       "           System.out.print(z);\n" +
                       "        };\n" +
                       "    }\n" +
                       "}\n");
    }
    
    public void testThatShadowedVariableIsRenamedInParams() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        int z = 0;\n" +
                       "        PassShadow shadowed = new PassShadow() {\n" +
                       "            public void pass(int z) {\n" +
                       "               System.out.print(z);\n" +
                       "            }\n" +
                       "        };\n" +
                       "    }\n" +
                       "    private interface PassShadow { public void pass(int z); }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("4:34-4:44:" + lambdaConvWarning)
                .applyFix(lambdaFix)
                .assertOutput("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        int z = 0;\n" +
                       "        PassShadow shadowed = (int z1) -> {\n" +
                       "           System.out.print(z1);\n" +
                       "        };\n" +
                       "    }\n" +
                       "    private interface PassShadow { public void pass(int z); }\n" +
                       "}\n");
    }
    
    public void testThatNamingCollisionsWithShadowedVariableAreHandled() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        int z = 0;\n" +
                       "        Runnable shadowed = new Runnable() {\n" +
                       "            public void run() {\n" +
                       "               int z = 1;\n" +
                       "               int z1 = 0;\n" +
                       "               System.out.print(z + \"\" + z1);\n" +
                       "            }\n" +
                       "        };\n" +
                       "    }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("4:32-4:40:" + lambdaConvWarning)
                .applyFix()
                .assertOutput("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        int z = 0;\n" +
                       "        Runnable shadowed = () -> {\n" +
                       "           int z1 = 1;\n" +
                       "           int z2 = 0;\n" +
                       "           System.out.print(z1 + \"\" + z2);\n" +
                       "        };\n" +
                       "    }\n" +
                       "}\n");
    }
    
    public void testThatShadowedVariableInEnclMethodArgsIsDetected() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(int z) {\n" +
                       "        Runnable shadowed = new Runnable() {\n" +
                       "            public void run() {\n" +
                       "               int z = 1;\n" +
                       "               System.out.print(z);\n" +
                       "            }\n" +
                       "        };\n" +
                       "    }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("3:32-3:40:" + lambdaConvWarning)
                .applyFix()
                .assertOutput("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(int z) {\n" +
                       "        Runnable shadowed = () -> {\n" +
                       "           int z1 = 1;\n" +
                       "           System.out.print(z1);\n" +
                       "        };\n" +
                       "    }\n" +
                       "}\n");
    }
    
    public void testThatCastIsAddedWithAmbiguousLambdaInMethodInvokeSingleArg() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        passOverload(new FuncInterface() {\n" +
                       "            public void nothing() { System.err.println(1); }\n" +
                       "        });\n" +
                       "    }\n" +
                       "    private void passOverload(FuncInterface fi) { }\n" +
                       "    private void passOverload(AnotherFuncInterface fi) { }\n" +
                       "    private interface FuncInterface { public void nothing(); }\n" +
                       "    private interface AnotherFuncInterface { public void nothing(); }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("3:25-3:38:" + lambdaConvWarning)
                .applyFix()
                .assertOutput("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        passOverload((FuncInterface) () -> {\n" +
                       "            System.err.println(1);\n" +
                       "        });\n" +
                       "    }\n" +
                       "    private void passOverload(FuncInterface fi) { }\n" +
                       "    private void passOverload(AnotherFuncInterface fi) { }\n" +
                       "    private interface FuncInterface { public void nothing(); }\n" +
                       "    private interface AnotherFuncInterface { public void nothing(); }\n" +
                       "}\n");
    }
    
    public void testThatCastIsAddedWithAmbiguousLambdaInMethodInvokeMultiArgs() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        passOverload(0, new FuncInterface() {\n" +
                       "            public void nothing() { System.err.println(1); }\n" +
                       "        });\n" +
                       "    }\n" +
                       "    private void passOverload(int x, FuncInterface fi) { }\n" +
                       "    private void passOverload(int x, AnotherFuncInterface fi) { }\n" +
                       "    private interface FuncInterface { public void nothing(); }\n" +
                       "    private interface AnotherFuncInterface { public void nothing(); }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("3:28-3:41:" + lambdaConvWarning)
                .applyFix()
                .assertOutput("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        passOverload(0, (FuncInterface) () -> {\n" +
                       "            System.err.println(1);\n" +
                       "        });\n" +
                       "    }\n" +
                       "    private void passOverload(int x, FuncInterface fi) { }\n" +
                       "    private void passOverload(int x, AnotherFuncInterface fi) { }\n" +
                       "    private interface FuncInterface { public void nothing(); }\n" +
                       "    private interface AnotherFuncInterface { public void nothing(); }\n" +
                       "}\n");
    }
    
    public void testThatCastIsAddedWithAmbiguousLambdaDueToOverloadInSupertype() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        Child.passOverload(new FuncInterface() {\n" +
                       "            public void nothing() { System.err.println(1); }\n" +
                       "        });\n" +
                       "    }\n" +
                       "    private static class Child extends Parent {\n" +
                       "       public static void passOverload(FuncInterface fi) { }\n" +
                       "    }\n" +
                       "    private static class Parent {\n" +
                       "       public static void passOverload(AnotherFuncInterface fi) { }\n" +
                       "    }\n" +
                       "    private interface FuncInterface { public void nothing(); }\n" +
                       "    private interface AnotherFuncInterface { public void nothing(); }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("3:31-3:44:" + lambdaConvWarning)
                .applyFix()
                .assertOutput("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        Child.passOverload((FuncInterface) () -> {\n" +
                       "            System.err.println(1);\n" +
                       "        });\n" +
                       "    }\n" +
                       "    private static class Child extends Parent {\n" +
                       "       public static void passOverload(FuncInterface fi) { }\n" +
                       "    }\n" +
                       "    private static class Parent {\n" +
                       "       public static void passOverload(AnotherFuncInterface fi) { }\n" +
                       "    }\n" +
                       "    private interface FuncInterface { public void nothing(); }\n" +
                       "    private interface AnotherFuncInterface { public void nothing(); }\n" +
                       "}\n");
    }
    
    public void testThatCastIsNotAddedWithBasicLambdaInMethodInvoke() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        passNotOverload(new FuncInterface() {\n" +
                       "            public void nothing() { System.err.println(1); }\n" +
                       "        });\n" +
                       "    }\n" +
                       "    private void passNotOverload(FuncInterface fi) { }\n" +
                       "    private interface FuncInterface { public void nothing(); }\n" +
                       "    private interface AnotherFuncInterface { public void nothing(); }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("3:28-3:41:" + lambdaConvWarning)
                .applyFix()
                .assertOutput("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        passNotOverload(() -> {\n" +
                       "            System.err.println(1);\n" +
                       "        });\n" +
                       "    }\n" +
                       "    private void passNotOverload(FuncInterface fi) { }\n" +
                       "    private interface FuncInterface { public void nothing(); }\n" +
                       "    private interface AnotherFuncInterface { public void nothing(); }\n" +
                       "}\n");
    }
    
    public void testThatCastIsAddedWithAmbiguousLambdaInNewClassSingleArg() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        ConstrOverload newClass = new ConstrOverload(new FuncInterface() {\n" +
                       "            public void nothing() { System.err.println(1); }\n" +
                       "        });\n" +
                       "    }\n" +
                       "    private class ConstrOverload {\n" +
                       "       public ConstrOverload(FuncInterface fi) { }\n" +
                       "       public ConstrOverload(AnotherFuncInterface fi) { }\n" +
                       "    }\n" +
                       "    private interface FuncInterface { public void nothing(); }\n" +
                       "    private interface AnotherFuncInterface { public void nothing(); }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("3:57-3:70:" + lambdaConvWarning)
                .applyFix()
                .assertOutput("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        ConstrOverload newClass = new ConstrOverload((FuncInterface) () -> {\n" +
                       "            System.err.println(1);\n" +
                       "        });\n" +
                       "    }\n" +
                       "    private class ConstrOverload {\n" +
                       "       public ConstrOverload(FuncInterface fi) { }\n" +
                       "       public ConstrOverload(AnotherFuncInterface fi) { }\n" +
                       "    }\n" +
                       "    private interface FuncInterface { public void nothing(); }\n" +
                       "    private interface AnotherFuncInterface { public void nothing(); }\n" +
                       "}\n");
    }
    
    public void testThatCastIsAddedWithAmbiguousLambdaInNewClassMultiArgs() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        ConstrOverload newClass = new ConstrOverload(0, new FuncInterface() {\n" +
                       "            public void nothing() { System.err.println(1); }\n" +
                       "        });\n" +
                       "    }\n" +
                       "    private class ConstrOverload {\n" +
                       "       public ConstrOverload(int x, FuncInterface fi) { }\n" +
                       "       public ConstrOverload(int x, AnotherFuncInterface fi) { }\n" +
                       "    }\n" +
                       "    private interface FuncInterface { public void nothing(); }\n" +
                       "    private interface AnotherFuncInterface { public void nothing(); }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("3:60-3:73:" + lambdaConvWarning)
                .applyFix()
                .assertOutput("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        ConstrOverload newClass = new ConstrOverload(0, (FuncInterface) () -> {\n" +
                       "            System.err.println(1);\n" +
                       "        });\n" +
                       "    }\n" +
                       "    private class ConstrOverload {\n" +
                       "       public ConstrOverload(int x, FuncInterface fi) { }\n" +
                       "       public ConstrOverload(int x, AnotherFuncInterface fi) { }\n" +
                       "    }\n" +
                       "    private interface FuncInterface { public void nothing(); }\n" +
                       "    private interface AnotherFuncInterface { public void nothing(); }\n" +
                       "}\n");
    }
    
    public void testThatCastIsNotAddedWithBasicLambdaInNewClass() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        NotConstrOverload newClass = new NotConstrOverload(new FuncInterface() {\n" +
                       "            public void nothing() { System.err.println(1); }\n" +
                       "        });\n" +
                       "    }\n" +
                       "    private class NotConstrOverload {\n" +
                       "       public NotConstrOverload(FuncInterface fi) { }\n" +
                       "    }\n" +
                       "    private interface FuncInterface { public void nothing(); }\n" +
                       "    private interface AnotherFuncInterface { public void nothing(); }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("3:63-3:76:" + lambdaConvWarning)
                .applyFix()
                .assertOutput("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        NotConstrOverload newClass = new NotConstrOverload(() -> {\n" +
                       "            System.err.println(1);\n" +
                       "        });\n" +
                       "    }\n" +
                       "    private class NotConstrOverload {\n" +
                       "       public NotConstrOverload(FuncInterface fi) { }\n" +
                       "    }\n" +
                       "    private interface FuncInterface { public void nothing(); }\n" +
                       "    private interface AnotherFuncInterface { public void nothing(); }\n" +
                       "}\n");
    }
    
    public void testThatGenericsAreConverted() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "import java.util.*;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        List<String> l = null;\n" +
                       "        Collections.sort(l, new Comparator<String>() {\n" +
                       "            @Override public int compare(String o1, String o2) {\n" +
                       "                return o1.compareToIgnoreCase(o2);\n" +
                       "            }\n" +
                       "        });\n" +
                       "    }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("5:32-5:50:" + lambdaConvWarning)
                .applyFix(lambdaFix)
                .assertOutput("package test;\n" +
                       "import java.util.*;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        List<String> l = null;\n" +
                       "        Collections.sort(l, (String o1, String o2) -> o1.compareToIgnoreCase(o2));\n" +
                       "    }\n" +
                       "}\n");
    }
    
    public void testThatCastIsAddedWithAmbiguousLambdaWithGenerics() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        doPrivileged(new PrivilegedAction<String>() {\n" +
                       "            public String run() { return new String(); }\n" +
                       "        });\n" +
                       "    }\n" +
                       "    private <T> void doPrivileged(PrivilegedAction<T> pa) { }\n" +
                       "    private <T> void doPrivileged(PrivilegedExceptionAction<T> pa) { }\n" +
                       "    private interface PrivilegedAction<T> { T run(); }\n" +
                       "    private interface PrivilegedExceptionAction<T> { T run() throws Exception; }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("3:25-3:49:" + lambdaConvWarning)
                .applyFix(lambdaFix)
                .assertOutput("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        doPrivileged((PrivilegedAction<String>) () -> new String());\n" +
                       "    }\n" +
                       "    private <T> void doPrivileged(PrivilegedAction<T> pa) { }\n" +
                       "    private <T> void doPrivileged(PrivilegedExceptionAction<T> pa) { }\n" +
                       "    private interface PrivilegedAction<T> { T run(); }\n" +
                       "    private interface PrivilegedExceptionAction<T> { T run() throws Exception; }\n" +
                       "}\n");
    }
    
    public void testThatCastIsAddedWithAmbiguousLambdaWithGenericsReference() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        doPrivileged(new PrivilegedAction<String>() {\n" +
                       "            public String run() { return new String(); }\n" +
                       "        });\n" +
                       "    }\n" +
                       "    private <T> void doPrivileged(PrivilegedAction<T> pa) { }\n" +
                       "    private <T> void doPrivileged(PrivilegedExceptionAction<T> pa) { }\n" +
                       "    private interface PrivilegedAction<T> { T run(); }\n" +
                       "    private interface PrivilegedExceptionAction<T> { T run() throws Exception; }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("3:25-3:49:" + lambdaConvWarning)
                .applyFix(memberReferenceFix)
                .assertOutput("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        doPrivileged((PrivilegedAction<String>) String::new);\n" +
                       "    }\n" +
                       "    private <T> void doPrivileged(PrivilegedAction<T> pa) { }\n" +
                       "    private <T> void doPrivileged(PrivilegedExceptionAction<T> pa) { }\n" +
                       "    private interface PrivilegedAction<T> { T run(); }\n" +
                       "    private interface PrivilegedExceptionAction<T> { T run() throws Exception; }\n" +
                       "}\n");
    }
    
    public void testThatNoExceptionIsThrownWhenCheckingAicAgainstPrimitive() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Test() {\n" +
                       "        this(\"Name\", new EdgeChooser() {\n" +
                       "            public boolean choose(Edge edge) {\n" +
                       "                if (edge.isExceptionEdge())\n" +
                       "                    return false;\n" +
                       "                else\n" +
                       "                    return true;\n" +
                       "            }\n" +
                       "        });\n" +
                       "    }\n" +
                       "    public Test (String name, EdgeChooser edgeChooser) { }\n" +
                       "    public Test (String name, boolean ignore) { }\n" +
                       "    private interface EdgeChooser { public boolean choose(Edge edge); }\n" +
                       "    private interface Edge { public boolean isExceptionEdge(); }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("3:25-3:36:" + lambdaConvWarning)
                .applyFix()
                .assertOutput("package test;\n" +
                       "public class Test {\n" +
                       "    public Test() {\n" +
                       "        this(\"Name\", (Edge edge) -> {\n" +
                       "            if (edge.isExceptionEdge())\n" +
                       "                return false;\n" +
                       "            else\n" +
                       "                return true;\n" +
                       "        });\n" +
                       "    }\n" +
                       "    public Test (String name, EdgeChooser edgeChooser) { }\n" +
                       "    public Test (String name, boolean ignore) { }\n" +
                       "    private interface EdgeChooser { public boolean choose(Edge edge); }\n" +
                       "    private interface Edge { public boolean isExceptionEdge(); }\n" +
                       "}\n");
    }
    
    public void testThatOuterLambdaIsConvertedInNestedContext() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        FuncInterface newClass = new FuncInterface() {\n" +
                       "            public void nothing() {\n" +
                       "                NestedFuncInterface nested = new NestedFuncInterface() {\n" +
                       "                   public void nothing() { System.err.println(1); }\n" +
                       "                };\n" +
                       "            }\n" +
                       "        };\n" +
                       "    }\n" +
                       "    private interface FuncInterface { public void nothing(); }\n" +
                       "    private interface NestedFuncInterface { public void nothing(); }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("3:37-3:50:" + lambdaConvWarning)
                .applyFix()
                .assertOutput("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        FuncInterface newClass = () -> {\n" +
                       "            NestedFuncInterface nested = new NestedFuncInterface() {\n" +
                       "                public void nothing() { System.err.println(1); }\n" +
                       "            };\n" +
                       "        };\n" +
                       "    }\n" +
                       "    private interface FuncInterface { public void nothing(); }\n" +
                       "    private interface NestedFuncInterface { public void nothing(); }\n" +
                       "}\n");
    }
    
    public void testThatInnerLambdaIsConvertedInNestedContext() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        FuncInterface newClass = new FuncInterface() {\n" +
                       "            public void nothing() {\n" +
                       "                NestedFuncInterface nested = new NestedFuncInterface() {\n" +
                       "                   public void nothing() { System.err.println(1); }\n" +
                       "                };\n" +
                       "            }\n" +
                       "        };\n" +
                       "    }\n" +
                       "    private interface FuncInterface { public void nothing(); }\n" +
                       "    private interface NestedFuncInterface { public void nothing(); }\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("5:49-5:68:" + lambdaConvWarning)
                .applyFix()
                .assertOutput("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        FuncInterface newClass = new FuncInterface() {\n" +
                       "            public void nothing() {\n" +
                       "                NestedFuncInterface nested = () -> {\n" +
                       "                   System.err.println(1);\n" +
                       "                };\n" +
                       "            }\n" +
                       "        };\n" + 
                       "    }\n" +
                       "    private interface FuncInterface { public void nothing(); }\n" +
                       "    private interface NestedFuncInterface { public void nothing(); }\n" +
                       "}\n");
                       
    }
    
    public void test234686() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    void foo() {\n" +
                       "        print(0, new Runnable() {\n" +
                       "            @ Override public void run() { }\n" +
                       "        });\n" +
                       "    }\n" +
                       "    void print(String str, Runnable r) {\n" +
                       "    }\n" +
                       "}\n",
                       false)
                .run(ConvertToLambda.class)
                .assertWarnings();

    }

    // default methods don't qualify for functional interfaces
    public void testThatDefaultMethodsAreIgnored1() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private interface NotFunctional {\n" +
                       "        public default void a(int i) {};\n" +
                       "    }\n" +
                       "    NotFunctional nf = new NotFunctional() {\n" +
                       "        @Override public void a(int i) { System.err.println(i); }\n" +
                       "    };\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .assertWarnings();
    }

    public void testThatDefaultMethodsAreIgnored2() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private interface DefaultRunnableNotFunctional1 extends Runnable {\n" +
                       "        @Override public default void run() {};\n" +
                       "    }\n" +
                       "    private interface DefaultRunnableNotFunctional2 extends DefaultRunnableNotFunctional1 {}\n" +
                       "    DefaultRunnableNotFunctional2 nf = new DefaultRunnableNotFunctional2() {\n" +
                       "        @Override public void run() { System.err.println(); }\n" +
                       "    };\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .assertWarnings();
    }

    public void testThatDefaultMethodsAreIgnored3() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private interface DefaultRunnableFunctional1 extends Runnable {\n" +
                       "        public void walk();\n" +
                       "        @Override public default void run() {};\n" +
                       "    }\n" +
                       "    private interface DefaultRunnableFunctional2 extends DefaultRunnableFunctional1 {}\n" +
                       "    DefaultRunnableFunctional2 f = new DefaultRunnableFunctional2() {\n" +
                       "        @Override public void walk() { System.err.println(5); }\n" +
                       "    };\n" +
                       "}\n")
                .run(ConvertToLambda.class)
                .findWarning("7:39-7:65:" + lambdaConvWarning)
                .applyFix()
                .assertOutput("package test;\n" +
                       "public class Test {\n" +
                       "    private interface DefaultRunnableFunctional1 extends Runnable {\n" +
                       "        public void walk();\n" +
                       "        @Override public default void run() {};\n" +
                       "    }\n" +
                       "    private interface DefaultRunnableFunctional2 extends DefaultRunnableFunctional1 {}\n" +
                       "    DefaultRunnableFunctional2 f = () -> {\n" +
                       "        System.err.println(5);\n" +
                       "    };\n" +
                       "}\n");
    }

    static {
        TestCompilerSettings.commandLine = "-XDfind=lambda";
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
}
