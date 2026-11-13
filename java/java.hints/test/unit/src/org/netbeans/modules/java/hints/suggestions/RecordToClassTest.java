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
public class RecordToClassTest extends NbTestCase {

    public RecordToClassTest(String name) {
        super(name);
    }

    @Test
    public void testConvertRecordToClass() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public record Test(int a, String b) {
                            public static final int K = 1;
                            public int getA() {
                                return a;
                            }
                        }
                        """)
                .sourceLevel("16")
                .run(RecordToClass.class)
                .findWarning("1:14-1:18:hint:Record Test can be converted to a class")
                .applyFix("Convert record Test to a class")
                .assertCompilable()
                .assertOutput(
                        """
                        package test;
                        public final class Test {

                            private final int a;
                            private final String b;

                            public Test(int a, String b) {
                                this.a = a;
                                this.b = b;
                            }
                            public static final int K = 1;
                            public int getA() {
                                return a;
                            }
                        }
                        """);
    }

    @Test
    public void testConvertRecordToClassWithConstructor() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public record Test(int a, String b) {
                            public Test(int a, String b) {
                                this.a = a;
                                this.b = b;
                            }
                        }
                        """)
                .sourceLevel("16")
                .run(RecordToClass.class)
                .findWarning("1:14-1:18:hint:Record Test can be converted to a class")
                .applyFix("Convert record Test to a class")
                .assertCompilable()
                .assertOutput(
                        """
                        package test;
                        public final class Test {

                            private final int a;
                            private final String b;
                            public Test(int a, String b) {
                                this.a = a;
                                this.b = b;
                            }
                        }
                        """);
    }

    @Test
    public void testConvertRecordToClassNoBody() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public record Test(int a) {}
                        """)
                .sourceLevel("16")
                .run(RecordToClass.class)
                .findWarning("1:14-1:18:hint:Record Test can be converted to a class")
                .applyFix("Convert record Test to a class")
                .assertCompilable()
                .assertOutput(
                        """
                        package test;
                        public final class Test {

                            private final int a;
                            public Test(int a) {
                                this.a = a;
                            }
                        }
                        """);
    }

    @Test
    public void testNoHintForClass() throws Exception {
        HintTest.create()
                .input( """
                        package test;
                        public final class Test {
                            private int a;
                        }
                        """)
                .sourceLevel("16")
                .run(RecordToClass.class)
                .assertWarnings();
    }
}

