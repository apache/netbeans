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
package org.netbeans.modules.java.hints.jdk;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;
import javax.lang.model.SourceVersion;

/**
 *
 * @author lahvac
 */
public class ConvertToPatternInstanceOfTest extends NbTestCase {
    
    public ConvertToPatternInstanceOfTest(String name) {
        super(name);
    }
    
    public void testSimple() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private int test(Object o) {\n" +
                       "        if (o instanceof String) {\n" +
                       "            String s = (String) o;\n" +
                       "            return s.length();\n" +
                       "        }\n" +
                       "        return -1;\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("17")
                .run(ConvertToPatternInstanceOf.class)
                .findWarning("3:8-3:10:verifier:" + Bundle.ERR_ConvertToPatternInstanceOf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private int test(Object o) {\n" +
                              "        if (o instanceof String s) {\n" +
                              "            return s.length();\n" +
                              "        }\n" +
                              "        return -1;\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testWithElse() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private int test(Object o) {\n" +
                       "        if (o instanceof String) {\n" +
                       "            String s = (String) o;\n" +
                       "            return s.length();\n" +
                       "        } else {\n" +
                       "            return -1;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("17")
                .run(ConvertToPatternInstanceOf.class)
                .findWarning("3:8-3:10:verifier:" + Bundle.ERR_ConvertToPatternInstanceOf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private int test(Object o) {\n" +
                              "        if (o instanceof String s) {\n" +
                              "            return s.length();\n" +
                              "        } else {\n" +
                              "            return -1;\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testNoSoSimple() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private int test(Object o) {\n" +
                       "        if (o instanceof String) {\n" +
                       "            return ((String) o).length();\n" +
                       "        }\n" +
                       "        return -1;\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("17")
                .run(ConvertToPatternInstanceOf.class)
                .findWarning("3:8-3:10:verifier:" + Bundle.ERR_ConvertToPatternInstanceOf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private int test(Object o) {\n" +
                              "        if (o instanceof String string) {\n" +
                              "            return string.length();\n" +
                              "        }\n" +
                              "        return -1;\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testNoSoSimpleNameClash() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private int test(Object o, String string) {\n" +
                       "        if (o instanceof String) {\n" +
                       "            return ((String) o).length();\n" +
                       "        }\n" +
                       "        return -1;\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("17")
                .run(ConvertToPatternInstanceOf.class)
                .findWarning("3:8-3:10:verifier:" + Bundle.ERR_ConvertToPatternInstanceOf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private int test(Object o, String string) {\n" +
                              "        if (o instanceof String string1) {\n" +
                              "            return string1.length();\n" +
                              "        }\n" +
                              "        return -1;\n" +
                              "    }\n" +
                              "}\n");
    }

}
