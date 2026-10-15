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

import org.junit.Test;
import org.netbeans.modules.java.hints.test.api.HintTest;
import javax.lang.model.SourceVersion;

/* TODO to make this test work:
   - to ensure that the newest Java language features supported by the IDE are available,
     regardless of which JDK you build the module with:
   -- for Ant-based modules, add "requires.nb.javac=true" into nbproject/project.properties
   -- for Maven-based modules, use dependency:copy in validate phase to create
      target/endorsed/org-netbeans-libs-javacapi-*.jar and add to endorseddirs
      in maven-compiler-plugin and maven-surefire-plugin configuration
      See: http://wiki.netbeans.org/JavaHintsTestMaven
 */
public class ConvertTextBlockToStringTest {

    @Test
    public void newLineAtEnd() throws Exception {
        HintTest.create()
                .input("package helloworld;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        String a =\"\"\"\n"
                        + "                  abc\n"
                        + "                  def\n"
                        + "                  hij\n"
                        + "                  \"\"\";\n"
                        + "    }\n"
                        + "}")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertTextBlockToString.class)
                .findWarning("3:18-3:21:hint:" + Bundle.ERR_ConvertTextBlockToString())
                .applyFix()
                .assertCompilable()
                .assertOutput("package helloworld;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        String a =\"abc\\n\" + \"def\\n\" + \"hij\\n\";\n"
                        + "    }\n"
                        + "}");
    }

    @Test
    public void simpleTest() throws Exception {
        HintTest.create()
                .input("package helloworld;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        String a =\"\"\"\n"
                        + "                  abc\"\"\";\n"
                        + "    }\n"
                        + "}")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertTextBlockToString.class)
                .findWarning("3:18-3:21:hint:" + Bundle.ERR_ConvertTextBlockToString())
                .applyFix()
                .assertCompilable()
                .assertOutput("package helloworld;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        String a =\"abc\";\n"
                        + "    }\n"
                        + "}");
    }

    @Test
    public void multipleNewLine() throws Exception {
        HintTest.create()
                .input("package helloworld;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        String a =\"\"\"\n"
                        + "                  abc\n"
                        + "                  \n"
                        + "                  \n"
                        + "                  \n"
                        + "                  \n"
                        + "                  \n"
                        + "                  \"\"\";\n"
                        + "    }\n"
                        + "}")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertTextBlockToString.class)
                .findWarning("3:18-3:21:hint:" + Bundle.ERR_ConvertTextBlockToString())
                .applyFix()
                .assertCompilable()
                .assertOutput("package helloworld;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        String a =\"abc\\n\" + \"\\n\" + \"\\n\" + \"\\n\" + \"\\n\" + \"\\n\";\n"
                        + "    }\n"
                        + "}");
    }

    @Test
    public void newLineAfter() throws Exception {
        HintTest.create()
                .input("package helloworld;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        String a =\"\"\"\n"
                        + "                  abc\n"
                        + "                 def\"\"\"\n"
                        + "                 \n"
                        + "                 \n"
                        + "                 \n"
                        + "                 ;\n"
                        + "    }\n"
                        + "}")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertTextBlockToString.class)
                .findWarning("3:18-3:21:hint:" + Bundle.ERR_ConvertTextBlockToString())
                .applyFix()
                .assertCompilable()
                .assertOutput("package helloworld;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        String a =\" abc\\n\" + \"def\"\n"
                        + "                 \n"
                        + "                 \n"
                        + "                 \n"
                        + "                 ;\n"
                        + "    }\n"
                        + "}");
    }

    @Test
    public void manyLineTextBlock() throws Exception {
        HintTest.create()
                .input("package helloworld;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        String a =\"\"\"\n"
                        + "                  abc\n"
                        + "                  def\n"
                        + "                  ghi\n"
                        + "                  jkl\n"
                        + "                  mno\n"
                        + "                  pqrs\n"
                        + "                  tuv\n"
                        + "                  wxyz\"\"\";\n"
                        + "    }\n"
                        + "}")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertTextBlockToString.class)
                .findWarning("3:18-3:21:hint:" + Bundle.ERR_ConvertTextBlockToString())
                .applyFix()
                .assertCompilable()
                .assertOutput("package helloworld;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        String a =\"abc\\n\" + \"def\\n\" + \"ghi\\n\" + \"jkl\\n\" + \"mno\\n\" + \"pqrs\\n\" + \"tuv\\n\" + \"wxyz\";\n"
                        + "    }\n"
                        + "}");
    }

    @Test
    public void twoLineTextBlock() throws Exception {
        HintTest.create()
                .input("package helloworld;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        String a =\"\"\"\n"
                        + "                  abc\n"
                        + "                  def\"\"\";\n"
                        + "    }\n"
                        + "}")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertTextBlockToString.class)
                .findWarning("3:18-3:21:hint:" + Bundle.ERR_ConvertTextBlockToString())
                .applyFix()
                .assertCompilable()
                .assertOutput("package helloworld;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        String a =\"abc\\n\" + \"def\";\n"
                        + "    }\n"
                        + "}");
    }

    @Test
    public void twoNewLines() throws Exception {
        HintTest.create()
                .input("package helloworld;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        String a =\"\"\"\n"
                        + "                 \n"
                        + "                 \n"
                        + "                 \"\"\";\n"
                        + "    }\n"
                        + "}")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertTextBlockToString.class)
                .findWarning("3:18-3:21:hint:" + Bundle.ERR_ConvertTextBlockToString())
                .applyFix()
                .assertCompilable()
                .assertOutput("package helloworld;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        String a =\"\\n\" + \"\\n\";\n"
                        + "    }\n"
                        + "}");
    }

    @Test
    public void slashConvert() throws Exception {
        HintTest.create()
                .input("package helloworld;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        String a =\"\"\"\n"
                        + "                 \\\\\"\"\";\n"
                        + "    }\n"
                        + "}")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertTextBlockToString.class)
                .findWarning("3:18-3:21:hint:" + Bundle.ERR_ConvertTextBlockToString())
                .applyFix()
                .assertCompilable()
                .assertOutput("package helloworld;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        String a =\"\\\\\";\n"
                        + "    }\n"
                        + "}");
    }

    @Test
    public void escapeCharTextBlock() throws Exception {
        HintTest.create()
                .input("package helloworld;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        String a =\"\"\"\n"
                        + "                  abc\n"
                        + "                  \"def\"\"\";\n"
                        + "    }\n"
                        + "}")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertTextBlockToString.class)
                .findWarning("3:18-3:21:hint:" + Bundle.ERR_ConvertTextBlockToString())
                .applyFix()
                .assertCompilable()
                .assertOutput("package helloworld;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        String a =\"abc\\n\" + \"\\\"def\";\n"
                        + "    }\n"
                        + "}");
    }

    @Test
    public void escapeCharTextBlock2() throws Exception {
        HintTest.create()
                .input("package helloworld;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        String a =\"\"\"\n"
                        + "                  abc\n"
                        + "                  \"def\n"
                        + "                  ghi\n"
                        + "                  'lmn'\n"
                        + "                  opq\n"
                        + "                  \"\"\";\n"
                        + "    }\n"
                        + "}")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertTextBlockToString.class)
                .findWarning("3:18-3:21:hint:" + Bundle.ERR_ConvertTextBlockToString())
                .applyFix()
                .assertCompilable()
                .assertOutput("package helloworld;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        String a =\"abc\\n\" + \"\\\"def\\n\" + \"ghi\\n\" + \"'lmn'\\n\" + \"opq\\n\";\n"
                        + "    }\n"
                        + "}");
    }

    @Test
    public void textBlockAsParameter1() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "\n"
                        + "class myClass{\n"
                        + "    public static void main(String[] args) {\n"
                        + "        System.out.println(\"\"\"\n"
                        + "                           abc\"\"\");\n"
                        + "    }\n"
                        + "}")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertTextBlockToString.class)
                .findWarning("4:27-4:30:hint:" + Bundle.ERR_ConvertTextBlockToString())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n"
                        + "\n"
                        + "class myClass{\n"
                        + "    public static void main(String[] args) {\n"
                        + "        System.out.println(\"abc\");\n"
                        + "    }\n"
                        + "}");
    }

    @Test
    public void textBlockAsParameter2() throws Exception {
        HintTest.create()
                .input("class myClass{\n"
                        + "    public static void main(String[] args) {\n"
                        + "        System.out.println(\"\"\"\n"
                        + "                           abc\n"
                        + "                           def\n"
                        + "                           ghi\"\"\");\n"
                        + "    }\n"
                        + "}")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertTextBlockToString.class)
                .findWarning("2:27-2:30:hint:" + Bundle.ERR_ConvertTextBlockToString())
                .applyFix()
                .assertCompilable()
                .assertOutput("class myClass{\n"
                        + "    public static void main(String[] args) {\n"
                        + "        System.out.println(\"abc\\n\" + \"def\\n\" + \"ghi\");\n"
                        + "    }\n"
                        + "}");
    }
}
