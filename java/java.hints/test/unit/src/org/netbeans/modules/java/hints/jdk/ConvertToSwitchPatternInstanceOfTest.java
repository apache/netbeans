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
 * @author aksinsin
 */
public class ConvertToSwitchPatternInstanceOfTest extends NbTestCase {
    
    public ConvertToSwitchPatternInstanceOfTest(String name) {
        super(name);
    }
    
    public void testSimple() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_17, skip tests
            return;
        }
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    private void test(Object o) {\n"
                        + "        if (o instanceof String) {\n"
                        + "            String s = (String) o;\n"
                        + "            System.out.println(s + \" String\");\n"
                        + "        } else if (o instanceof StringBuilder) {\n"
                        + "            StringBuilder sb = (StringBuilder) o;\n"
                        + "            System.out.println(sb + \" StringBuilder\");\n"
                        + "        } else if (o instanceof CharSequence) {\n"
                        + "            CharSequence cs = (CharSequence) o;\n"
                        + "            System.out.println(cs + \" CharSequence\");\n"
                        + "        } else {\n"
                        + "            System.out.println(\"else\");\n"
                        + "        }\n"
                        + "    }\n"
                        + "}")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertToSwitchPatternInstanceOf.class)
                .findWarning("3:8-3:10:verifier:" + Bundle.ERR_ConvertToSwitchPatternInstanceOf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n"
                        + "public class Test {\n"
                        + "    private void test(Object o) {\n"
                        + "        switch (o) {\n"
                        + "            case String s -> System.out.println(s + \" String\");\n"
                        + "            case StringBuilder sb -> System.out.println(sb + \" StringBuilder\");\n"
                        + "            case CharSequence cs -> System.out.println(cs + \" CharSequence\");\n"
                        + "            case default -> System.out.println(\"else\");\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n");
    }
}
