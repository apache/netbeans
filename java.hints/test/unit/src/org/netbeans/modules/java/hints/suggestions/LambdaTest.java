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
                       "    public void main(List<String> list) {\n" +
                       "        Collections.sort(list, String:^:compareTo);\n" +
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
                                      "    public void main(List<String> list) {\n" +
                                      "        Collections.sort(list, (string, string1) -> string.compareTo(string1));\n" +
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
                       "    public void main(List<String> list) {\n" +
                       "        filter(list, \"a\":^:equalsIgnoreCase);\n" +
                       "    }\n" +
                       "    public static void filter(List<String> list, Predicate p) {\n" +
                       "    }\n" +
                       "    public interface Predicate {\n" +
                       "        public boolean accept(String str);\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.8")
                .run(Lambda.class)
                .findWarning("4:25-4:25:verifier:" + Bundle.ERR_memberReference2Lambda())
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n" +
                                      "import java.util.*;\n" +
                                      "public class Test {\n" +
                                      "    public void main(List<String> list) {\n" +
                                      "        filter(list, (string) -> \"a\".equalsIgnoreCase(string));\n" +
                                      "    }\n" +
                                      "    public static void filter(List<String> list, Predicate p) {\n" +
                                      "    }\n" +
                                      "    public interface Predicate {\n" +
                                      "        public boolean accept(String str);\n" +
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

    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
}
