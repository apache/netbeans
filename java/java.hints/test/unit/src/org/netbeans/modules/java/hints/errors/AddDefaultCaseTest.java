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

package org.netbeans.modules.java.hints.errors;

import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.openide.util.NbBundle;

public class AddDefaultCaseTest extends ErrorHintsTestBase {

    static {
        NbBundle.setBranding("test");
    }
    
    public AddDefaultCaseTest(String testName) {
        super(testName, AddDefaultCase.class);
    }

    @Override
    protected void setUp() throws Exception {
        sourceLevel = "21";
        super.setUp();
    }

    public void testSwitch() throws Exception {
        performFixTest( 
            "test/Test.java",
            """
            package test;
            public class Test {
                private void test(String o) {
                    switch (o) {
                        case null: break;
                        case "1": break;
                        case "2": break;
                    }
                }
            }
            """,
            -1,
            "Add Default Case",
            """
            package test;
            public class Test {
                private void test(String o) {
                    switch (o) {
                        case null: break;
                        case "1": break;
                        case "2": break;
                        default: throw new IllegalStateException("Unexpected value: " + o);
                    }
                }
            }
            """.replaceAll("\\s+", " ")
        );
    }

    public void testRuleSwitch() throws Exception {
        performFixTest( 
            "test/Test.java",
            """
            package test;
            public class Test {
                private void test(Object o) {
                    switch (o) {
                        case null -> System.out.println("null case");
                        case Integer i when i > 0 -> System.out.println("2nd case");
                        case Integer i -> System.out.println("3rd case");
                    }
                }
            }
            """,
            -1,
            "Add Default Case",
            """
            package test;
            public class Test {
                private void test(Object o) {
                    switch (o) {
                        case null -> System.out.println("null case");
                        case Integer i when i > 0 -> System.out.println("2nd case");
                        case Integer i -> System.out.println("3rd case");
                        default -> throw new IllegalStateException("Unexpected value: " + o);
                    }
                }
            }
            """.replaceAll("\\s+", " ")
        );
    }

    public void testRuleSwitchExpression() throws Exception {
        performFixTest( 
            "test/Test.java",
            """
            package test;
            public class Test {
                private Object test(Object o) {
                    return switch (o) {
                        case null -> 1;
                        case Integer i when i > 0 -> 2;
                        case Integer i -> 3;
                    };
                }
            }
            """,
            -1,
            "Add Default Case",
            """
            package test;
            public class Test {
                private Object test(Object o) {
                    return switch (o) {
                        case null -> 1;
                        case Integer i when i > 0 -> 2;
                        case Integer i -> 3;
                        default -> throw new IllegalStateException("Unexpected value: " + (o));
                    };
                }
            }
            """.replaceAll("\\s+", " ")
        );
    }

    public void testSwitchExpression() throws Exception {
        performFixTest( 
            "test/Test.java",
            """
            package test;
            public class Test {
                private Object test(String o) {
                    return switch (o) {
                        case null: yield 1;
                        case "1": yield 2;
                        case "2": yield 3;
                    };
                }
            }
            """,
            -1,
            "Add Default Case",
            """
            package test;
            public class Test {
                private Object test(String o) {
                    return switch (o) {
                        case null: yield 1;
                        case "1": yield 2;
                        case "2": yield 3;
                        default: throw new IllegalStateException("Unexpected value: " + (o));
                    };
                }
            }
            """.replaceAll("\\s+", " ")
        );
    }
    
}
