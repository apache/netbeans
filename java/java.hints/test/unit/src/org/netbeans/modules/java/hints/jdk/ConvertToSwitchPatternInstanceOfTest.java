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
import org.testng.annotations.Test;

/**
 *
 * @author sandeemi
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
                        + "   private int test(Object o){\n"
                        + "        if(o instanceof String){\n"
                        + "            String s = (String)o;\n"
                        + "            System.out.println(s);\n"
                        + "        }else if(o instanceof Integer){\n"
                        + "            Integer i = (Integer)o;\n"
                        + "            System.out.println(i);\n"
                        + "        }else{\n"
                        + "             System.out.println(\"else\");\n"
                        + "         }\n"
                        + "        return -1;\n"
                        + "    }\n"
                        + "}\n"
                )
                .sourceLevel("17")
                .options("--enable-preview")
                .run(ConvertToSwitchPatternInstanceOf.class)
                .findWarning("3:8-3:10:verifier:" + Bundle.ERR_ConvertToSwitchPatternInstanceOf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n"
                        + "public class Test {\n"
                        + "   private int test(Object o){\n"
                        + "        switch (o) {\n"
                        + "            case String s -> System.out.println(s);\n"
                        + "            case Integer i -> System.out.println(i);\n"
                        + "            case default -> System.out.println(\"else\");\n"
                        + "        }\n"
                        + "        return -1;\n"
                        + "    }\n"
                        + "}\n");
    }

    @Test
    public void testSimplePatternMatch() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17");
        } catch (IllegalArgumentException ex) {
            //OK, skip test
            return;
        }
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    static String formatter(Object o) {\n"
                        + "        String formatted = \"unknown\";\n"
                        + "        if (o instanceof Integer i) {\n"
                        + "            formatted = String.format(\"int %d\", i);\n"
                        + "        } else if (o instanceof Long l) {\n"
                        + "            formatted = String.format(\"long %d\", l);\n"
                        + "        } else if (o instanceof Double d) {\n"
                        + "            formatted = String.format(\"double %f\", d);\n"
                        + "        } else if (o instanceof String s) {\n"
                        + "            formatted = String.format(\"String %s\", s);\n"
                        + "        }\n"
                        + "        return formatted;\n"
                        + "    }"
                        + "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertToSwitchPatternInstanceOf.class)
                .findWarning("4:8-4:10:verifier:" + Bundle.ERR_ConvertToSwitchPatternInstanceOf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n"
                        + "public class Test {\n"
                        + "    static String formatter(Object o) {\n"
                        + "        String formatted = \"unknown\";\n"
                        + "        switch (o) {\n"
                        + "            case Integer i -> formatted = String.format(\"int %d\", i);\n"
                        + "            case Long l -> formatted = String.format(\"long %d\", l);\n"
                        + "            case Double d -> formatted = String.format(\"double %f\", d);\n"
                        + "            case String s -> formatted = String.format(\"String %s\", s);\n"
                        + "            case default -> {\n"
                        + "            }\n"
                        + "        }\n"
                        + "        return formatted;\n"
                        + "    }\n"
                        + "}\n");
    }

    @Test
    public void testSimpleSwitchWithNull() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17");
        } catch (IllegalArgumentException ex) {
            //OK, skip test
            return;
        }
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "     private String formatter(Object o) {\n"
                        + "        String formatted = \"unknown\";\n"
                        + "        if (o == null) {\n"
                        + "            formatted = \"null\";\n"
                        + "        }\n"
                        + "        switch (o) {\n"
                        + "            case Integer i ->\n"
                        + "                formatted = String.format(\"int %d\", i);\n"
                        + "            case Long l ->\n"
                        + "                formatted = String.format(\"long %d\", l);\n"
                        + "            case Double d ->\n"
                        + "                formatted = String.format(\"double %f\", d);\n"
                        + "            case String s ->\n"
                        + "                formatted = String.format(\"String %s\", s);\n"
                        + "            case default -> formatted = \"unknown\";\n"
                        + "        }\n"
                        + "        return formatted;\n"
                        + "    }"
                        + "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertToSwitchPatternInstanceOf.class)
                .findWarning("4:8-4:24:verifier:" + Bundle.ERR_ConvertToSwitchPatternInstanceOf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n"
                        + "public class Test {\n"
                        + "     private String formatter(Object o) {\n"
                        + "        String formatted = \"unknown\";\n"
                        + "        switch (o) {\n"
                        + "            case null -> formatted = \"null\";\n"
                        + "            case Integer i ->\n"
                        + "                formatted = String.format(\"int %d\", i);\n"
                        + "            case Long l ->\n"
                        + "                formatted = String.format(\"long %d\", l);\n"
                        + "            case Double d ->\n"
                        + "                formatted = String.format(\"double %f\", d);\n"
                        + "            case String s ->\n"
                        + "                formatted = String.format(\"String %s\", s);\n"
                        + "            case default -> \n"
                        + "                formatted = \"unknown\";\n"
                        + "        }\n"
                        + "        return formatted;\n"
                        + "    }\n"
                        + "}\n");
    }

    @Test
    public void testSingleStatements() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17");
        } catch (IllegalArgumentException ex) {
            //OK, skip test
            return;
        }
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "     private int formatter(Object o) {\n"
                        + "        String formatted = \"unknown\";\n"
                        + "        if (o instanceof String) { \n"
                        + "            String s = (String) o;\n"
                        + "            System.out.println(s);\n"
                        + "        } else if (o instanceof Integer) {\n"
                        + "            Integer i = (Integer) o;\n"
                        + "            System.out.println(i);\n"
                        + "        } else if (o instanceof Character) {\n"
                        + "            Character c = (Character) o;\n"
                        + "            return 1;\n"
                        + "        } else {\n"
                        + "            return 1;\n"
                        + "        }\n"
                        + "        return -1;\n"
                        + "    }"
                        + "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertToSwitchPatternInstanceOf.class)
                .findWarning("4:8-4:10:verifier:" + Bundle.ERR_ConvertToSwitchPatternInstanceOf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n"
                        + "public class Test {\n"
                        + "     private int formatter(Object o) {\n"
                        + "        String formatted = \"unknown\";\n"
                        + "        switch (o) {\n"
                        + "            case String s -> System.out.println(s);\n"
                        + "            case Integer i -> System.out.println(i);\n"
                        + "            case Character c -> {\n"
                        + "                return 1;\n"
                        + "            }\n"
                        + "            case default -> {\n"
                        + "                return 1;\n"
                        + "            }\n"
                        + "        }\n"
                        + "        return -1;\n"
                        + "    }\n"
                        + "}\n");
    }

    @Test
    public void testMultipleStatements() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17");
        } catch (IllegalArgumentException ex) {
            //OK, skip test
            return;
        }
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "     private int formatter(Object o) {\n"
                        + "        String formatted = \"unknown\";\n"
                        + "        if (o instanceof String) { \n"
                        + "            String s = (String) o;\n"
                        + "            formatted = \"String\";\n"
                        + "            System.out.println(s);\n"
                        + "        } else if (o instanceof Integer) {\n"
                        + "            Integer i = (Integer) o;\n"
                        + "            formatted = \"Integer\";\n"
                        + "            System.out.println(i);\n"
                        + "        } else if (o instanceof Character) {\n"
                        + "            Character c = (Character) o;\n"
                        + "            formatted = \"Character\";\n"
                        + "            return 1;\n"
                        + "        } else {\n"
                        + "            formatted = \"else\";\n"
                        + "            return 1;\n"
                        + "        }\n"
                        + "        return -1;\n"
                        + "    }"
                        + "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertToSwitchPatternInstanceOf.class)
                .findWarning("4:8-4:10:verifier:" + Bundle.ERR_ConvertToSwitchPatternInstanceOf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n"
                        + "public class Test {\n"
                        + "     private int formatter(Object o) {\n"
                        + "        String formatted = \"unknown\";\n"
                        + "        switch (o) {\n"
                        + "            case String s -> {\n"
                        + "                formatted = \"String\";\n"
                        + "                System.out.println(s);\n"
                        + "            }\n"
                        + "            case Integer i -> {\n"
                        + "                formatted = \"Integer\";\n"
                        + "                System.out.println(i);\n"
                        + "            }\n"
                        + "            case Character c -> {\n"
                        + "                formatted = \"Character\";\n"
                        + "                return 1;\n"
                        + "            }\n"
                        + "            case default -> {\n"
                        + "                formatted = \"else\";\n"
                        + "                return 1;   \n"
                        + "            }\n"
                        + "        }\n"
                        + "        return -1;\n"
                        + "    }\n"
                        + "}\n");
    }

    @Test
    public void testEmptyStatements() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17");
        } catch (IllegalArgumentException ex) {
            //OK, skip test
            return;
        }
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "     private int formatter(Object o) {\n"
                        + "        String formatted = \"unknown\";\n"
                        + "        if (o instanceof String) { \n"
                        + "            String s = (String) o;\n"
                        + "            System.out.println(s);\n"
                        + "        } else if (o instanceof Integer) {\n"
                        + "            Integer i = (Integer) o;\n"
                        + "            System.out.println(i);\n"
                        + "        } else if (o instanceof Character) {\n"
                        + "            Character c = (Character) o;\n"
                        + "        } else {\n"
                        + "        }\n"
                        + "        return -1;\n"
                        + "    }"
                        + "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertToSwitchPatternInstanceOf.class)
                .findWarning("4:8-4:10:verifier:" + Bundle.ERR_ConvertToSwitchPatternInstanceOf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n"
                        + "public class Test {\n"
                        + "     private int formatter(Object o) {\n"
                        + "        String formatted = \"unknown\";\n"
                        + "        switch (o) {\n"
                        + "            case String s -> System.out.println(s);\n"
                        + "            case Integer i -> System.out.println(i);\n"
                        + "            case Character c -> {\n"
                        + "            }\n"
                        + "            case default -> {\n"
                        + "            }\n"
                        + "        }\n"
                        + "        return -1;\n"
                        + "    }\n"
                        + "}\n");
    }
}
