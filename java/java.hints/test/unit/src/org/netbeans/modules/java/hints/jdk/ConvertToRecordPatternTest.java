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
import static org.junit.Assume.assumeTrue;

/**
 *
 * @author mjayan
 */
public class ConvertToRecordPatternTest extends NbTestCase {

    public ConvertToRecordPatternTest(String name) {
        super(name);
    }

    public void testSimple() throws Exception {
        assumeTrue(isRecordClassPresent());
        assumeTrue(SourceVersion.latest().ordinal() >= 21);
        HintTest.create()
                .input("package test;\n"
                        + "record Person(String name, String place){}\n"
                        + "public class Test {\n"
                        + "    private int test(Object o) {\n"
                        + "        if (o instanceof Person p) {\n"
                        + "            String name = p.name();\n"
                        + "            return name.length();\n"
                        + "        }\n"
                        + "        return -1;\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertToRecordPattern.class)
                .findWarning("4:8-4:10:verifier:" + Bundle.ERR_ConvertToRecordPattern())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n"
                        + "record Person(String name, String place){}\n"
                        + "public class Test {\n"
                        + "    private int test(Object o) {\n"
                        + "        if (o instanceof Person(String name, String place)) {\n"
                        + "            return name.length();\n"
                        + "        }\n"
                        + "        return -1;\n"
                        + "    }\n"
                        + "}\n");
    }

    public void testDuplicateVarName() throws Exception {
        assumeTrue(isRecordClassPresent());
        assumeTrue(SourceVersion.latest().ordinal() >= 21);
        HintTest.create()
                .input("package test;\n"
                        + "record Person(String name, int s){}\n"
                        + "public class Test {\n"
                        + "    private int test(Object s) {\n"
                        + "        if (s instanceof Person p) {\n"
                        + "            String name = p.name();\n"
                        + "            return name.length();\n"
                        + "        }\n"
                        + "        return -1;\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertToRecordPattern.class)
                .findWarning("4:8-4:10:verifier:" + Bundle.ERR_ConvertToRecordPattern())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n"
                        + "record Person(String name, int s){}\n"
                        + "public class Test {\n"
                        + "    private int test(Object s) {\n"
                        + "        if (s instanceof Person(String name, int s1)) {\n"
                        + "            return name.length();\n"
                        + "        }\n"
                        + "        return -1;\n"
                        + "    }\n"
                        + "}\n");
    }

    public void testUsingUserVar() throws Exception {
        assumeTrue(isRecordClassPresent());
        assumeTrue(SourceVersion.latest().ordinal() >= 21);
        HintTest.create()
                .input("package test;\n"
                        + "record Person(String name, int s){}\n"
                        + "public class Test {\n"
                        + "    private int test(Object s) {\n"
                        + "        if (s instanceof Person p) {\n"
                        + "            String userName = p.name();\n"
                        + "            return userName.length();\n"
                        + "        }\n"
                        + "        return -1;\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertToRecordPattern.class)
                .findWarning("4:8-4:10:verifier:" + Bundle.ERR_ConvertToRecordPattern())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n"
                        + "record Person(String name, int s){}\n"
                        + "public class Test {\n"
                        + "    private int test(Object s) {\n"
                        + "        if (s instanceof Person(String userName, int s1)) {\n"
                        + "            return userName.length();\n"
                        + "        }\n"
                        + "        return -1;\n"
                        + "    }\n"
                        + "}\n");
    }

    private boolean isRecordClassPresent() {
        try {
            Class.forName("java.lang.Record");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
}
