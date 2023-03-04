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

import org.netbeans.modules.java.hints.jdk.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 * Note: many of the cases are already tested in StringToSwitchTest. String
 * conversion works for equals only, so null-ity may not be tested fully.
 *
 * @author Svata
 */
public class ConvertIfToSwitchTest extends NbTestCase {

    public ConvertIfToSwitchTest(String name) {
        super(name);
    }

    public void testPrimitiveWithDefault() throws Exception {
        HintTest
                .create()
                .input("package test;\n"
                        + "\n"
                        + "public abstract class Test {\n"
                        + "    public String toString(int t) {\n"
                        + "	String s;\n"
                        + "	if (t == 1) {\n"
                        + "		s = \"1\";\n"
                        + "	} else if (t == 2) {\n"
                        + "		s = \"2\";\n"
                        + "	} else {\n"
                        + "		s = \"3\";\n"
                        + "	}\n"
                        + "	return \"number is \" + s;\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.7")
                .run(ConvertIfToSwitch.class)
                .findWarning("5:1-5:14:verifier:HINT_ConvertIfToSwitch")
                .applyFix("FIX_ConvertIfsToSwitch")
                .assertCompilable()
                .assertOutput("package test;\n"
                        + "\n"
                        + "public abstract class Test {\n"
                        + "    public String toString(int t) {\n"
                        + "	String s;\n"
                        + "        switch (t) {\n"
                        + "            case 1:\n"
                        + "                s = \"1\";\n"
                        + "                break;\n"
                        + "            case 2:\n"
                        + "                s = \"2\";\n"
                        + "                break;\n"
                        + "            default:\n"
                        + "                s = \"3\";\n"
                        + "                break;\n"
                        + "        }\n"
                        + "	return \"number is \" + s;\n"
                        + "    }\n"
                        + "}\n"
                        + "");
    }

    public void testUsedValueFromElseBranch259071() throws Exception {
        HintTest
                .create()
                .input("package test;\n"
                        + "\n"
                        + "public abstract class Test {\n"
                        + "    public enum testEnum{\n"
                        + "            ONE,\n"
                        + "            TWO,\n"
                        + "            THREE;\n"
                        + "    }\n"
                        + "    \n"
                        + "    public String toString(testEnum t) {\n"
                        + "	String s;\n"
                        + "	if (t == testEnum.ONE) {\n"
                        + "		s = \"1\";\n"
                        + "	} else if (t == testEnum.TWO) {\n"
                        + "		s = \"2\";\n"
                        + "	} else {\n"
                        + "		s = \"3\";\n"
                        + "	}\n"
                        + "	return \"number is \" + s;\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.7")
                .run(ConvertIfToSwitch.class)
                .findWarning("11:1-11:25:verifier:HINT_ConvertIfToSwitch")
                .applyFix("FIX_ConvertIfsToSwitch")
                .assertCompilable()
                .assertOutput("package test;\n"
                        + "\n"
                        + "public abstract class Test {\n"
                        + "    public enum testEnum{\n"
                        + "            ONE,\n"
                        + "            TWO,\n"
                        + "            THREE;\n"
                        + "    }\n"
                        + "    \n"
                        + "    public String toString(testEnum t) {\n"
                        + "	String s;\n"
                        + "	if (null == t) {\n"
                        + "            s = \"3\";\n"
                        + "        } else switch (t) {\n"
                        + "            case ONE:\n"
                        + "                s = \"1\";\n"
                        + "                break;\n"
                        + "            case TWO:\n"
                        + "                s = \"2\";\n"
                        + "                break;\n"
                        + "            default:\n"
                        + "                s = \"3\";\n"
                        + "                break;\n"
                        + "        }\n"
                        + "	return \"number is \" + s;\n"
                        + "    }\n"
                        + "}");
    }
}
