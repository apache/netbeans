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
package org.netbeans.modules.java.hints.suggestions;

import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 * @author mbien
 */
public class ClassToRecordTest extends NbTestCase {

    public ClassToRecordTest(String name) {
        super(name);
    }

    @Test
    public void testConvertClassToRecord() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public class Test {
                            private int a;
                            private String b;
                            public static final int K = 1;
                            public int getA() {
                                return a;
                            }
                        }
                        """)
                .sourceLevel("16")
                .run(ClassToRecord.class)
                .findWarning("1:13-1:17:verifier:Class Test may be converted to a record")
                .applyFix("Convert class Test to a record")
                .assertCompilable()
                .assertOutput(
                        """
                        package test;
                        public record Test(int a, String b) {
                            public static final int K = 1;
                            public int getA() {
                                return a;
                            }
                        }
                        """);
    }

    @Test
    public void testConvertClassToRecordNoBody() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public class Test {
                            private int a;
                        }
                        """)
                .sourceLevel("16")
                .run(ClassToRecord.class)
                .findWarning("1:13-1:17:verifier:Class Test may be converted to a record")
                .applyFix("Convert class Test to a record")
                .assertCompilable()
                .assertOutput(
                        """
                        package test;
                        public record Test(int a) {}
                        """);
    }

    @Test
    public void testConvertFinalClassToRecord() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public final class Test {
                            private int a;
                        }
                        """)
                .sourceLevel("16")
                .run(ClassToRecord.class)
                .findWarning("1:19-1:23:verifier:Class Test may be converted to a record")
                .applyFix("Convert class Test to a record")
                .assertCompilable()
                .assertOutput(
                        """
                        package test;
                        public record Test(int a) {}
                        """);
    }

    @Test
    public void testConvertClassWithImplements() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public class Test implements Cloneable, java.io.Serializable {
                            private int a;
                        }
                        """)
                .sourceLevel("16")
                .run(ClassToRecord.class)
                .findWarning("1:13-1:17:verifier:Class Test may be converted to a record")
                .applyFix("Convert class Test to a record")
                .assertCompilable()
                .assertOutput(
                        """
                        package test;
                        public record Test(int a) implements Cloneable, java.io.Serializable {}
                        """);
    }

    @Test
    public void testConvertGenericClassToRecord() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public class Test<T> {
                            private T value;
                        }
                        """)
                .sourceLevel("16")
                .run(ClassToRecord.class)
                .findWarning("1:13-1:17:verifier:Class Test may be converted to a record")
                .applyFix("Convert class Test to a record")
                .assertCompilable()
                .assertOutput(
                        """
                        package test;
                        public record Test<T>(T value) {}
                        """);
    }

    @Test
    public void testPackagePrivateClass() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        class Test {
                            private int a;
                        }
                        """)
                .sourceLevel("16")
                .run(ClassToRecord.class)
                .findWarning("1:6-1:10:verifier:Class Test may be converted to a record")
                .applyFix("Convert class Test to a record")
                .assertCompilable()
                .assertOutput(
                        """
                        package test;
                        record Test(int a) {}
                        """);
    }

    @Test
    public void testClassWithConstructor() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public class Test {
                            private int a;
                            public Test(int a) {
                                this.a = a;
                            }
                        }
                        """)
                .sourceLevel("16")
                .run(ClassToRecord.class)
                .findWarning("1:13-1:17:verifier:Class Test may be converted to a record")
                .applyFix("Convert class Test to a record")
                .assertCompilable()
                .assertOutput(
                        """
                        package test;
                        public record Test(int a) {}
                        """);
    }

    @Test
    public void testRecordLikeClass() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public class Test {
                            private final int i;
                            public Test(int i) {
                                this.i = i;
                            }
                            public int i() {
                                return i;
                            }
                        }
                        """)
                .sourceLevel("16")
                .run(ClassToRecord.class)
                .findWarning("1:13-1:17:verifier:Class Test may be converted to a record")
                .applyFix("Convert class Test to a record")
                .assertCompilable()
                .assertOutput(
                        """
                        package test;
                        public record Test(int i) {}
                        """);
    }

    @Test
    public void testRecordLikeClassWithObjectMembers() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public class Test {
                            private final int i;
                            public Test(int i) {
                                this.i = i;
                            }
                            public int i() {
                                return i;
                            }
                            @Override
                            public boolean equals(Object o) {
                                if (o == this)
                                    return true;
                                if (!(o instanceof Test other))
                                    return false;
                                return i == other.i;
                            }
                            @Override
                            public int hashCode() {
                                return java.util.Objects.hash(i);
                            }
                            @Override
                            public String toString() {
                                return "Test[i=" + i + "]";
                            }
                        }
                        """)
                .sourceLevel("16")
                .run(ClassToRecord.class)
                .findWarning("1:13-1:17:verifier:Class Test may be converted to a record")
                .applyFix("Convert class Test to a record")
                .assertCompilable()
                .assertOutput(
                        """
                        package test;
                        public record Test(int i) {
                            @Override
                            public boolean equals(Object o) {
                                if (o == this)
                                    return true;
                                if (!(o instanceof Test other))
                                    return false;
                                return i == other.i;
                            }
                            @Override
                            public int hashCode() {
                                return java.util.Objects.hash(i);
                            }
                            @Override
                            public String toString() {
                                return "Test[i=" + i + "]";
                            }
                        }
                        """);
    }

    @Test
    public void testCtorWithLogic() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public class Test {
                            private int a;
                            public Test(int a) {
                                if (a < 0)
                                    throw new IllegalArgumentException();
                                this.a = a;
                            }
                        }
                        """)
                .sourceLevel("16")
                .run(ClassToRecord.class)
                .findWarning("1:13-1:17:verifier:Class Test may be converted to a record")
                .applyFix("Convert class Test to a record")
                .assertCompilable()
                .assertOutput(
                        """
                        package test;
                        public record Test(int a) {
                            public Test(int a) {
                                if (a < 0)
                                    throw new IllegalArgumentException();
                                this.a = a;
                            }
                        }
                        """);
    }

    @Test
    public void testAnnotatedCtorIsKept() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public class Test {
                            private int a;
                            @Deprecated
                            public Test(int a) {
                                this.a = a;
                            }
                        }
                        """)
                .sourceLevel("16")
                .run(ClassToRecord.class)
                .findWarning("1:13-1:17:verifier:Class Test may be converted to a record")
                .applyFix("Convert class Test to a record")
                .assertCompilable()
                .assertOutput(
                        """
                        package test;
                        public record Test(int a) {
                            @Deprecated
                            public Test(int a) {
                                this.a = a;
                            }
                        }
                        """);
    }

    @Test
    public void testCtorWithSuperCall() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public class Test {
                            private int a;
                            public Test(int a) {
                                super();
                                this.a = a;
                            }
                        }
                        """)
                .sourceLevel("16")
                .run(ClassToRecord.class)
                .assertNotContainsWarnings("Class Test may be converted to a record");
    }

    @Test
    public void testAnnotatedAccessor() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public class Test {
                            private int a;
                            @Deprecated
                            public int a() {
                                return a;
                            }
                        }
                        """)
                .sourceLevel("16")
                .run(ClassToRecord.class)
                .assertNotContainsWarnings("Class Test may be converted to a record");
    }

    @Test
    public void testClassWithExtends() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public class Test extends Object {
                            private int a;
                        }
                        """)
                .sourceLevel("16")
                .run(ClassToRecord.class)
                .assertNotContainsWarnings("Class Test may be converted to a record");
    }

    @Test
    public void testAbstractClass() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public abstract class Test {
                            private int a;
                        }
                        """)
                .sourceLevel("16")
                .run(ClassToRecord.class)
                .assertNotContainsWarnings("Class Test may be converted to a record");
    }

    @Test
    public void testFieldWithInitializer() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public class Test {
                            private int a = 1;
                        }
                        """)
                .sourceLevel("16")
                .run(ClassToRecord.class)
                .assertNotContainsWarnings("Class Test may be converted to a record");
    }

    @Test
    public void testFieldWithAnnotation() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public class Test {
                            @Deprecated private int a;
                        }
                        """)
                .sourceLevel("16")
                .run(ClassToRecord.class)
                .assertNotContainsWarnings("Class Test may be converted to a record");
    }

    @Test
    public void testRecordStyleAccessor() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public class Test {
                            private int a;
                            public int a() {
                                return a;
                            }
                        }
                        """)
                .sourceLevel("16")
                .run(ClassToRecord.class)
                .findWarning("1:13-1:17:verifier:Class Test may be converted to a record")
                .applyFix("Convert class Test to a record")
                .assertCompilable()
                .assertOutput(
                        """
                        package test;
                        public record Test(int a) {}
                        """);
    }

    @Test
    public void testNonRedundantAccessorNameCollision() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public class Test {
                            private int a;
                            public int a() {
                                return a + 1;
                            }
                        }
                        """)
                .sourceLevel("16")
                .run(ClassToRecord.class)
                .assertNotContainsWarnings("Class Test may be converted to a record");
    }

    @Test
    public void testSealedClass() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public sealed class Test permits Sub {
                            private int a;
                        }
                        final class Sub extends Test {
                            private final int b = 1;
                        }
                        """)
                .sourceLevel("17")
                .run(ClassToRecord.class)
                .assertNotContainsWarnings("Class Test may be converted to a record");
    }

    @Test
    public void testOldSourceLevel() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public class Test {
                            private int a;
                        }
                        """)
                .sourceLevel("13")
                .run(ClassToRecord.class)
                .assertNotContainsWarnings("Class Test may be converted to a record");
    }

    @Test
    public void testObjectMethodsRemoved() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public class Test {
                            private final int i;
                            public Test(int i) {
                                this.i = i;
                            }
                            @Override
                            public final boolean equals(Object o) {
                                if (o == this)
                                    return true;
                                if (!(o instanceof Test other))
                                    return false;
                                return i == other.i;
                            }
                            @Override
                            public int hashCode() {
                                return java.util.Objects.hash(i);
                            }
                            @Override
                            public String toString() {
                                return "Test[i=" + i + "]";
                            }
                        }
                        """)
                .sourceLevel("16")
                .preference(ClassToRecord.OPTION_REMOVE_OBJECT_METHODS, true)
                .run(ClassToRecord.class)
                .findWarning("1:13-1:17:verifier:Class Test may be converted to a record")
                .applyFix("Convert class Test to a record")
                .assertCompilable()
                .assertOutput(
                        """
                        package test;
                        public record Test(int i) {}
                        """);
    }

    @Test
    public void testObjectMethodsRemovedKeepsNonOverriding() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public class Test {
                            private int i;
                            @Override
                            public boolean equals(Object o) {
                                return o instanceof Test other && i == other.i;
                            }
                            public boolean equals(int other) {
                                return i == other;
                            }
                            @Deprecated
                            @Override
                            public String toString() {
                                return "Test[i=" + i + "]";
                            }
                        }
                        """)
                .sourceLevel("16")
                .preference(ClassToRecord.OPTION_REMOVE_OBJECT_METHODS, true)
                .run(ClassToRecord.class)
                .findWarning("1:13-1:17:verifier:Class Test may be converted to a record")
                .applyFix("Convert class Test to a record")
                .assertCompilable()
                .assertOutput(
                        """
                        package test;
                        public record Test(int i) {
                            public boolean equals(int other) {
                                return i == other;
                            }
                            @Deprecated
                            @Override
                            public String toString() {
                                return "Test[i=" + i + "]";
                            }
                        }
                        """);
    }
}

