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
package org.netbeans.modules.java.hints.suggestions;

import org.junit.Test;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.netbeans.modules.java.source.parsing.JavacParser;

/**
 *
 * @author lahvac
 */
public class LambdaTest {
    
    @Test
    public void testLambda2Class1() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(Runnable r) {\n" +
                       "        main(() -^> { System.err.println(\"block\"); });\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.8")
                .run(Lambda.class)
                .findWarning("3:17-3:17:verifier:ERR_lambda2Class")
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n" +
                                      "public class Test {\n" +
                                      "    public static void main(Runnable r) {\n" +
                                      "        main(new Runnable() {\n" +
                                      "            @Override\n" +
                                      "            public void run() {\n" +
                                      "                System.err.println(\"block\");\n" +
                                      "            }\n" +
                                      "        });\n" +
                                      "    }\n" +
                                      "}\n");
    }
    
    @Test
    public void testLambda2ClassThis() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void main(Runnable r) {\n" +
                       "        main(() -^> { System.err.println(this.toString()); });\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.8")
                .run(Lambda.class)
                .findWarning("3:17-3:17:verifier:ERR_lambda2Class")
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n" +
                                      "public class Test {\n" +
                                      "    public void main(Runnable r) {\n" +
                                      "        main(new Runnable() {\n" +
                                      "            @Override\n" +
                                      "            public void run() {\n" +
                                      "                System.err.println(Test.this.toString());\n" +
                                      "            }\n" +
                                      "        });\n" +
                                      "    }\n" +
                                      "}\n");
    }
    
    @Test
    public void testLambda2ClassShadowedMethod() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n"
                        + "import java.util.Objects;\n"
                        + "import java.util.function.Function;\n"
                        + "\n"
                        + "@FunctionalInterface\n"
                        + "interface Test<A, B, C, R> {\n"
                        + "    public static final int EE = 1;\n"
                        + "    public R apply(A a, B b, C c);\n"
                        + "\n"
                        + "    default <V> Test<A, B, C, V> andThen(Function<? super R, ? extends V> after) {\n"
                        + "        Objects.requireNonNull(after);\n"
                        + "        return (A a, B b, C c) -^> {\n"
                        + "            return after.apply(apply(a, b, c));\n"
                        + "        };\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel("1.8")
                .run(Lambda.class)
                .findWarning("11:32-11:32:verifier:ERR_lambda2Class")
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "import java.util.Objects;\n"
                        + "import java.util.function.Function;\n"
                        + "\n"
                        + "@FunctionalInterface\n"
                        + "interface Test<A, B, C, R> {\n"
                        + "    public static final int EE = 1;\n"
                        + "    public R apply(A a, B b, C c);\n"
                        + "\n"
                        + "    default <V> Test<A, B, C, V> andThen(Function<? super R, ? extends V> after) {\n"
                        + "        Objects.requireNonNull(after);\n"
                        + "        return new Test<A, B, C, V>() {\n"
                        + "            @Override\n"
                        + "            public V apply(A a, B b, C c) {\n"
                        + "                return after.apply(Test.this.apply(a, b, c));\n"
                        + "            }\n"
                        + "        };\n"
                        + "    }\n"
                        + "}\n");
    }
    
    @Test
    public void testLambda2ClassShadowedStaticSameField() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n"
                        + "import java.util.Objects;\n"
                        + "import java.util.function.Function;\n"
                        + "\n"
                        + "@FunctionalInterface\n"
                        + "interface Test<A, B, C, R> {\n"
                        + "    public static final int EE = 1;\n"
                        + "    public R apply(A a, B b, C c);\n"
                        + "\n"
                        + "    default <V> Test<A, B, C, V> andThen(Function<? super R, ? extends V> after) {\n"
                        + "        Objects.requireNonNull(after);\n"
                        + "        return (A a, B b, C c) -^> {\n"
                        + "            System.err.println(EE);\n"
                        + "            return after.apply(apply(a, b, c));\n"
                        + "        };\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel("1.8")
                .run(Lambda.class)
                .findWarning("11:32-11:32:verifier:ERR_lambda2Class")
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "import java.util.Objects;\n"
                        + "import java.util.function.Function;\n"
                        + "\n"
                        + "@FunctionalInterface\n"
                        + "interface Test<A, B, C, R> {\n"
                        + "    public static final int EE = 1;\n"
                        + "    public R apply(A a, B b, C c);\n"
                        + "\n"
                        + "    default <V> Test<A, B, C, V> andThen(Function<? super R, ? extends V> after) {\n"
                        + "        Objects.requireNonNull(after);\n"
                        + "        return new Test<A, B, C, V>() {\n"
                        + "            @Override\n"
                        + "            public V apply(A a, B b, C c) {\n"
                        + "                System.err.println(EE);\n"
                        + "                return after.apply(Test.this.apply(a, b, c));\n"
                        + "            }\n"
                        + "        };\n"
                        + "    }\n"
                        + "}\n");
    }
    
    @Test
    public void testLambda2ClassShadowedInheritedMethod() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n"
                        + "import java.util.Objects;\n"
                        + "import java.util.function.Function;\n"
                        + "\n"
                        + "@FunctionalInterface\n"
                        + "interface Test<A, B, C, R> {\n"
                        + "    public static final int EE = 1;\n"
                        + "    public R apply(A a, B b, C c);\n"
                        + "\n"
                        + "    default <V> Test2<A, B, C, V> andThen(Function<? super R, ? extends V> after) {\n"
                        + "        Objects.requireNonNull(after);\n"
                        + "        return (A a, B b, C c) -^> {\n"
                        + "            return after.apply(apply(a, b, c));\n"
                        + "        };\n"
                        + "    \n"
                        + "        \n"
                        + "    }\n"
                        + "}\n"
                        + "\n"
                        + "interface Test2<A, B, C, R> extends Test<A, B, C, R> {\n"
                        + "    public static final int EE = 2;\n"
                        + "}")
                .sourceLevel("1.8")
                .run(Lambda.class)
                .findWarning("11:32-11:32:verifier:ERR_lambda2Class")
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "import java.util.Objects;\n"
                        + "import java.util.function.Function;\n"
                        + "\n"
                        + "@FunctionalInterface\n"
                        + "interface Test<A, B, C, R> {\n"
                        + "    public static final int EE = 1;\n"
                        + "    public R apply(A a, B b, C c);\n"
                        + "\n"
                        + "    default <V> Test2<A, B, C, V> andThen(Function<? super R, ? extends V> after) {\n"
                        + "        Objects.requireNonNull(after);\n"
                        + "        return new Test2<A, B, C, V>() {\n"
                        + "            @Override\n"
                        + "            public V apply(A a, B b, C c) {\n"
                        + "                return after.apply(Test.this.apply(a, b, c));\n"
                        + "            }\n"
                        + "        };\n"
                        + "    \n"
                        + "        \n"
                        + "    }\n"
                        + "}\n"
                        + "\n"
                        + "interface Test2<A, B, C, R> extends Test<A, B, C, R> {\n"
                        + "    public static final int EE = 2;\n"
                        + "}"
                );
    }
    
    @Test
    public void testLambda2ClassShadowedUnrelatedField() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n"
                        + "import java.util.Objects;\n"
                        + "import java.util.function.Function;\n"
                        + "\n"
                        + "@FunctionalInterface\n"
                        + "interface Test<A, B, C, R> {\n"
                        + "    public static final int EE = 1;\n"
                        + "    public R apply(A a, B b, C c);\n"
                        + "\n"
                        + "    default <V> Test2<A, B, C, V> andThen(Function<? super R, ? extends V> after) {\n"
                        + "        Objects.requireNonNull(after);\n"
                        + "        return (A a, B b, C c) -^> {\n"
                        + "            System.err.println(EE);\n"
                        + "            return null;\n"
                        + "        };\n"
                        + "    \n"
                        + "        \n"
                        + "    }\n"
                        + "}\n"
                        + "\n"
                        + "interface Test2<A, B, C, R> extends Test<A, B, C, R> {\n"
                        + "    public static final int EE = 2;\n"
                        + "}\n")
                .sourceLevel("1.8")
                .run(Lambda.class)
                .findWarning("11:32-11:32:verifier:ERR_lambda2Class")
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "import java.util.Objects;\n"
                        + "import java.util.function.Function;\n"
                        + "\n"
                        + "@FunctionalInterface\n"
                        + "interface Test<A, B, C, R> {\n"
                        + "    public static final int EE = 1;\n"
                        + "    public R apply(A a, B b, C c);\n"
                        + "\n"
                        + "    default <V> Test2<A, B, C, V> andThen(Function<? super R, ? extends V> after) {\n"
                        + "        Objects.requireNonNull(after);\n"
                        + "        return new Test2<A, B, C, V>() {\n"
                        + "            @Override\n"
                        + "            public V apply(A a, B b, C c) {\n"
                        + "                System.err.println(Test.EE);\n"
                        + "                return null;\n"
                        + "            }\n"
                        + "        };\n"
                        + "    \n"
                        + "        \n"
                        + "    }\n"
                        + "}\n"
                        + "\n"
                        + "interface Test2<A, B, C, R> extends Test<A, B, C, R> {\n"
                        + "    public static final int EE = 2;\n"
                        + "}\n");
    }
    
    @Test
    public void testLambda2ClassExpression() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "import java.util.*;\n" +
                       "public class Test {\n" +
                       "    public void main(List<String> list) {\n" +
                       "        Collections.sort(list, (l, r) -^> l.compareTo(r));\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.8")
                .run(Lambda.class)
                .findWarning("4:39-4:39:verifier:ERR_lambda2Class")
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n" +
                                      "import java.util.*;\n" +
                                      "public class Test {\n" +
                                      "    public void main(List<String> list) {\n" +
                                      "        Collections.sort(list, new Comparator<String>() {\n" +
                                      "            @Override\n" +
                                      "            public int compare(String l, String r) {\n" +
                                      "                return l.compareTo(r);\n" +
                                      "            }\n" +
                                      "        });\n" +
                                      "    }\n" +
                                      "}\n");
    }
    
    @Test
    public void testLambda2ClassExpressionVoid233100() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "import javax.swing.*;\n" +
                       "public class Test {\n" +
                       "    public void main() {\n" +
                       "        SwingUtilities.invokeLater(() -^> System.err.println(1));\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.8")
                .run(Lambda.class)
                .findWarning("4:39-4:39:verifier:ERR_lambda2Class")
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import javax.swing.*;\n" +
                              "public class Test {\n" +
                              "    public void main() {\n" +
                              "        SwingUtilities.invokeLater(new Runnable() {\n" +
                              "            @Override\n" +
                              "            public void run() {\n" +
                              "                System.err.println(1);\n" +
                              "            }\n" +
                              "        });\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testExpression2Body() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "import java.util.*;\n" +
                       "public class Test {\n" +
                       "    public void main(List<String> list) {\n" +
                       "        Collections.sort(list, (l, r) -^> l.compareTo(r));\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.8")
                .run(Lambda.class)
                .findWarning("4:39-4:39:verifier:ERR_expression2Return")
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n" +
                                      "import java.util.*;\n" +
                                      "public class Test {\n" +
                                      "    public void main(List<String> list) {\n" +
                                      "        Collections.sort(list, (l, r) -> {\n" +
                                      "            return l.compareTo(r);\n" +
                                      "        });\n" +
                                      "    }\n" +
                                      "}\n");
    }
    
    @Test
    public void testVoidExpression2Body233100() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "import javax.swing.*;\n" +
                       "public class Test {\n" +
                       "    public void main() {\n" +
                       "        SwingUtilities.invokeLater(() -^> System.err.println(1));\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.8")
                .run(Lambda.class)
                .findWarning("4:39-4:39:verifier:ERR_expression2Return")
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n" +
                                      "import javax.swing.*;\n" +
                                      "public class Test {\n" +
                                      "    public void main() {\n" +
                                      "        SwingUtilities.invokeLater(() -> {\n" +
                                      "            System.err.println(1);\n" +
                                      "        });\n" +
                                      "    }\n" +
                                      "}\n");
    }
    
    @Test
    public void testLambda2Ref() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "import java.util.*;\n" +
                       "public class Test {\n" +
                       "    public void main(List<String> list) {\n" +
                       "        Collections.sort(list, (l, r) -^> l.compareTo(r));\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.8")
                .run(Lambda.class)
                .findWarning("4:39-4:39:verifier:" + Bundle.ERR_lambda2MemberReference())
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n" +
                                      "import java.util.*;\n" +
                                      "public class Test {\n" +
                                      "    public void main(List<String> list) {\n" +
                                      "        Collections.sort(list, String::compareTo);\n" +
                                      "    }\n" +
                                      "}\n");
    }
    
    @Test
    public void testRef2LambdaStaticStatic() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "import java.util.*;\n" +
                       "public class Test {\n" +
                       "    public void main(List<String> list) {\n" +
                       "        Collections.sort(list, Test:^:compare);\n" +
                       "    }\n" +
                       "    private static int compare(String s1, String s2) {\n" +
                       "        return s1.compareTo(s2);\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.8")
                .run(Lambda.class)
                .findWarning("4:36-4:36:verifier:" + Bundle.ERR_memberReference2Lambda())
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n" +
                                      "import java.util.*;\n" +
                                      "public class Test {\n" +
                                      "    public void main(List<String> list) {\n" +
                                      "        Collections.sort(list, (s1, s2) -> Test.compare(s1, s2));\n" +
                                      "    }\n" +
                                      "    private static int compare(String s1, String s2) {\n" +
                                      "        return s1.compareTo(s2);\n" +
                                      "    }\n" +
                                      "}\n");
    }
    
    @Test
    public void testRef2LambdaStaticInstance() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "import java.util.*;\n" +
                       "public class Test {\n" +
                       "    public void main(List<Wrapper> list) {\n" +
                       "        Collections.sort(list, Wrapper:^:compareTo);\n" +
                       "    }\n" +
                       "    public static class Wrapper {\n" +
                       "        public int compareTo(Wrapper other) {\n" +
                       "            return 0;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.8")
                .run(Lambda.class)
                .findWarning("4:39-4:39:verifier:" + Bundle.ERR_memberReference2Lambda())
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n" +
                                      "import java.util.*;\n" +
                                      "public class Test {\n" +
                                      "    public void main(List<Wrapper> list) {\n" +
                                      "        Collections.sort(list, (wrapper, other) -> wrapper.compareTo(other));\n" +
                                      "    }\n" +
                                      "    public static class Wrapper {\n" +
                                      "        public int compareTo(Wrapper other) {\n" +
                                      "            return 0;\n" +
                                      "        }\n" +
                                      "    }\n" +
                                      "}\n");
    }
    
    @Test
    public void testRef2LambdaInstance() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "import java.util.*;\n" +
                       "public class Test {\n" +
                       "    public void main(List<Wrapper> list) {\n" +
                       "        filter(list, Wrapper.INSTANCE:^:check);\n" +
                       "    }\n" +
                       "    public static void filter(List<Wrapper> list, Predicate p) {\n" +
                       "    }\n" +
                       "    public interface Predicate {\n" +
                       "        public boolean accept(Wrapper value);\n" +
                       "    }\n" +
                       "    public static class Wrapper {\n" +
                       "        public static final Wrapper INSTANCE = new Wrapper();\n" +
                       "        public boolean check(Wrapper other) { return false; }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.8")
                .run(Lambda.class)
                .findWarning("4:38-4:38:verifier:" + Bundle.ERR_memberReference2Lambda())
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n" +
                                      "import java.util.*;\n" +
                                      "public class Test {\n" +
                                      "    public void main(List<Wrapper> list) {\n" +
                                      "        filter(list, other -> Wrapper.INSTANCE.check(other));\n" +
                                      "    }\n" +
                                      "    public static void filter(List<Wrapper> list, Predicate p) {\n" +
                                      "    }\n" +
                                      "    public interface Predicate {\n" +
                                      "        public boolean accept(Wrapper value);\n" +
                                      "    }\n" +
                                      "    public static class Wrapper {\n" +
                                      "        public static final Wrapper INSTANCE = new Wrapper();\n" +
                                      "        public boolean check(Wrapper other) { return false; }\n" +
                                      "    }\n" +
                                      "}\n");
    }

    @Test
    public void testExplicitParameterTypes() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "import java.util.*;\n" +
                       "public class Test {\n" +
                       "    public void main(List<String> list) {\n" +
                       "        Collections.sort(list, (l, r) -^> l.compareTo(r));\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.8")
                .run(Lambda.class)
                .findWarning("4:39-4:39:verifier:ERR_addExplicitLambdaParameters")
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n" +
                                      "import java.util.*;\n" +
                                      "public class Test {\n" +
                                      "    public void main(List<String> list) {\n" +
                                      "        Collections.sort(list, (String l, String r) -> l.compareTo(r));\n" +
                                      "    }\n" +
                                      "}\n");
    }

    @Test
    public void testImplicitVarParameterTypes1() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "import java.util.function.IntBinaryOperator;\n" +
                       "public class Test {\n" +
                       "    public void main(String[] args) {\n" +
                       "        IntBinaryOperator calc3 = (int x, int y)^ ->  x + y;\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.11")
                .run(Lambda.class)
                .findWarning("4:48-4:48:verifier:ERR_ConvertVarLambdaParameters")
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n" +
                                      "import java.util.function.IntBinaryOperator;\n" +
                                      "public class Test {\n" +
                                      "    public void main(String[] args) {\n" +
                                      "        IntBinaryOperator calc3 = (var x, var y) ->  x + y;\n" +
                                      "    }\n" +
                                      "}\n");
    }

    @Test
    public void testImplicitVarParameterTypes2() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "import java.util.function.IntBinaryOperator;\n" +
                       "public class Test {\n" +
                       "    public void main(String[] args) {\n" +
                       "        IntBinaryOperator calc3 = (x, y)^ ->  x + y;\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.11")
                .run(Lambda.class)
                .findWarning("4:40-4:40:verifier:ERR_ConvertVarLambdaParameters")
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n" +
                                      "import java.util.function.IntBinaryOperator;\n" +
                                      "public class Test {\n" +
                                      "    public void main(String[] args) {\n" +
                                      "        IntBinaryOperator calc3 = (var x, var y) ->  x + y;\n" +
                                      "    }\n" +
                                      "}\n");
    }
    
    @Test
    public void testImplicitVarParameterTypes3() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "import java.util.function.IntBinaryOperator;\n" +
                       "public class Test {\n" +
                       "    public void main(String[] args) {\n" +
                       "        IntBinaryOperator calc3 = (var x, var y)^ ->  x + y;\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.11")
                .run(Lambda.class)
                .assertNotContainsWarnings("ERR_ConvertVarLambdaParameters");
    }

    @Test
    public void testConvertVarToExplicitParameterTypes() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "import java.util.*;\n" +
                       "public class Test {\n" +
                       "    public void main(List<String> list) {\n" +
                       "        Collections.sort(list, (var l, var r) -^> l.compareTo(r));\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.11")
                .run(Lambda.class)
                .findWarning("4:47-4:47:verifier:ERR_addExplicitLambdaParameters")
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n" +
                                      "import java.util.*;\n" +
                                      "public class Test {\n" +
                                      "    public void main(List<String> list) {\n" +
                                      "        Collections.sort(list, (String l, String r) -> l.compareTo(r));\n" +
                                      "    }\n" +
                                      "}\n");
    }

    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
}
