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

import javax.lang.model.SourceVersion;
import org.junit.Test;
import org.netbeans.modules.java.hints.test.api.HintTest;

public class ConvertToTextBlockTest {

    @Test
    public void testFixWorking() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_15");
        } catch (IllegalArgumentException ex) {
            //OK, skip test
            return ;
        }
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        assert args[0].equals(\"{\\n\" +\n" +
                       "                              \"    int i = 0;\\n\" +\n" +
                       "                              \"}\");\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertToTextBlock.class)
                .findWarning("3:30-3:37:verifier:" + Bundle.ERR_ConvertToTextBlock())
                .applyFix()
                .assertCompilable()
                //TODO: change to match expected output
                .assertOutput("package test;\n" +
                        "public class Test {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        assert args[0].equals(\"\"\"\n" +
                        "                              {\n" +
                        "                                  int i = 0;\n" +
                        "                              }\"\"\");\n" +
                        "    }\n" +
                        "}\n");
    }

    @Test
    public void testNewLineAtEnd() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_15");
        } catch (IllegalArgumentException ex) {
            //OK, skip test
            return ;
        }
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        assert args[0].equals(\"{\\n\" +\n" +
                       "                              \"    int i = 0;\\n\" +\n" +
                       "                              \"}\\n\");\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertToTextBlock.class)
                .findWarning("3:30-3:37:verifier:" + Bundle.ERR_ConvertToTextBlock())
                .applyFix()
                .assertCompilable()
                //TODO: change to match expected output
                .assertOutput("package test;\n" +
                        "public class Test {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        assert args[0].equals(\"\"\"\n" +
                        "                              {\n" +
                        "                                  int i = 0;\n" +
                        "                              }\n" +
                        "                              \"\"\");\n" +
                        "    }\n" +
                        "}\n");
    }

    @Test
    public void testNewLinesAtEnd() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_15");
        } catch (IllegalArgumentException ex) {
            //OK, skip test
            return ;
        }
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        assert args[0].equals(\"{\\n\" +\n" +
                       "                              \"    int i = 0;\\n\" +\n" +
                       "                              \"}\\n\\n\");\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertToTextBlock.class)
                .findWarning("3:30-3:37:verifier:" + Bundle.ERR_ConvertToTextBlock())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                        "public class Test {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        assert args[0].equals(\"\"\"\n" +
                        "                              {\n" +
                        "                                  int i = 0;\n" +
                        "                              }\n" +
                        "                              \n"  +
                        "                              \"\"\");\n" +
                        "    }\n" +
                        "}\n");
    }

    @Test
    public void testOnlyLiterals() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_15");
        } catch (IllegalArgumentException ex) {
            //OK, skip test
            return ;
        }
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public int test() {\n" +
                       "        return c() + c();\n" +
                       "    }\n" +
                       "    private int c() { return 0; }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertToTextBlock.class)
                .assertWarnings();
    }
}
