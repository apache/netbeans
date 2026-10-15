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
package org.netbeans.modules.java.hints.bugs;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.netbeans.modules.java.ui.FmtOptions;

/**
 *
 * @author lahvac
 */
public class TinyTest extends NbTestCase {

    public TinyTest(String name) {
        super(name);
    }

    public void testSingleCharRegexPositive1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(String[] args) {\n" +
                       "        \"a\".replaceAll(\".\", \"/\");\n" +
                       "    }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:23-3:26:verifier:ERR_single-char-regex")
                .applyFix("FIX_single-char-regex")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public void test(String[] args) {\n" +
                              "        \"a\".replaceAll(\"\\\\.\", \"/\");\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testSingleCharRegexPositive2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(String[] args) {\n" +
                       "        \"a\".split(\"$\");\n" +
                       "    }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:18-3:21:verifier:ERR_single-char-regex")
                .applyFix("FIX_single-char-regex")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public void test(String[] args) {\n" +
                              "        \"a\".split(\"\\\\$\");\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testSingleCharRegexNegative1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(String[] args) {\n" +
                       "        \"a\".replaceAll(\",\", \"/\");\n" +
                       "        \"a\".replaceFirst(\"$$\", \"/\");\n" +
                       "        String foo = \"foo\";\n" +
                       "        \"a\".split(foo);\n" +
                       "    }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings();
    }

    public void testIgnoredNewObject1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(String[] args) {\n" +
                       "        new Object();\n" +
                       "    }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("3:8-3:21:verifier:new Object");
    }

    public void testIgnoredNewObject2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "        public static void test() {\n" +
                       "            new Test().new T(1, 3);\n" +
                       "        }\n" +
                       "        private class T {\n" +
                       "            public T(int i, int j) {}" +
                       "        }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("3:12-3:35:verifier:new Object");
    }

    public void testSystemArrayCopy() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "        public static void test(Object o1, Object[] o2, Object o3) {\n" +
                       "            System.arraycopy(o1, 0, o2, 0, 1);\n" +
                       "            System.arraycopy(o2, 0, o3, 0, 1);\n" +
                       "            System.arraycopy(o2, 0 - 1, o2, 0 + 2 - 4, -1);\n" +
                       "        }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("3:29-3:31:verifier:...o1 not an instance of an array type",
                                "4:36-4:38:verifier:...o3 not an instance of an array type",
                                "5:33-5:38:verifier:0-1 is negative",
                                "5:44-5:53:verifier:0+2-4 is negative",
                                "5:55-5:57:verifier:-1 is negative");
    }

    public void testEqualsNull() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public boolean test(String arg) {\n" +
                       "        return arg.equals(null);\n" +
                       "    }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:15-3:31:verifier:ERR_equalsNull")
                .applyFix("FIX_equalsNull")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public boolean test(String arg) {\n" +
                              "        return arg == null;\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testResultSet1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Object test(java.sql.ResultSet set) throws java.sql.SQLException{\n" +
                       "        return set.getBoolean(0);\n" +
                       "    }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("3:30-3:31:verifier:ERR_ResultSetZero");
    }

    public void testResultSet2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Object test(R set) {\n" +
                       "        return set.getBoolean(0);\n" +
                       "    }" +
                       "    private interface R extends java.sql.ResultSet {" +
                       "        public boolean getBoolean(int i);" +
                       "    }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("3:30-3:31:verifier:ERR_ResultSetZero");
    }

    public void testResultSet180027() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Object test(R set, int i) {\n" +
                       "        set.getBoolean(0);\n" +
                       "        return set.getBoolean(i + 1);\n" +
                       "    }" +
                       "    private interface R extends java.sql.ResultSet {" +
                       "        public boolean getBoolean(int i);" +
                       "    }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("3:23-3:24:verifier:ERR_ResultSetZero");
    }

    public void testInconsistentIndentationIf() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(boolean b) {\n" +
                       "        if (b)\n" +
                       "            System.err.println(1);\n" +
                       "            System.err.println(1);\n" +
                       "    }" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("5:12-5:34:verifier:" + Bundle.ERR_indentation());
    }
    
    public void testInconsistentIndentationNeg() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(boolean b) {\n" +
                       "        if (b)\n" +
                       "            System.err.println(1);\n" +
                       "        System.err.println(1);\n" +
                       "    }" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings();
    }

    public void testInconsistentIndentationIfElse() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(boolean b) {\n" +
                       "        if (b)\n" +
                       "            System.err.println(1);\n" +
                       "        else\n" +
                       "            System.err.println(2);\n" +
                       "            System.err.println(2);\n" +
                       "    }" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("7:12-7:34:verifier:" + Bundle.ERR_indentation());
    }

    public void testInconsistentIndentationWhile() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(boolean b) {\n" +
                       "        while (b = !b)\n" +
                       "            System.err.println(1);\n" +
                       "            System.err.println(1);\n" +
                       "    }" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("5:12-5:34:verifier:" + Bundle.ERR_indentation());
    }

    public void testInconsistentIndentationFor() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(boolean b) {\n" +
                       "        for (; b = !b; )\n" +
                       "            System.err.println(1);\n" +
                       "            System.err.println(1);\n" +
                       "    }" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("5:12-5:34:verifier:" + Bundle.ERR_indentation());
    }

    public void testInconsistentIndentationEnhancedFor() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(Iterable<String> inp) {\n" +
                       "        for (String s : inp)\n" +
                       "            System.err.println(1);\n" +
                       "            System.err.println(1);\n" +
                       "    }" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("5:12-5:34:verifier:" + Bundle.ERR_indentation());
    }

    public void testInconsistentIndentationTabHandling1() throws Exception {
        Map<String, String> original = alterSettings(FmtOptions.tabSize, "8");
        
        try {
            HintTest.create()
                    .input("package test;\n" +
                           "public class Test {\n" +
                           "    public void test(boolean b) {\n" +
                           "        if (b)\n" +
                           "            System.err.println(1);\n" +
                           "    \tSystem.err.println(1);\n" +
                           "    }" +
                           "}\n")
                    .run(Tiny.class)
                    .assertWarnings("5:5-5:27:verifier:" + Bundle.ERR_indentation());
        } finally {
            reset(original);
        }
    }
    
    public void testInconsistentIndentationTabHandling2() throws Exception {
        Map<String, String> original = alterSettings(FmtOptions.tabSize, "8");
        
        try {
            HintTest.create()
                    .input("package test;\n" +
                           "public class Test {\n" +
                           "    public void test(boolean b) {\n" +
                           "        if (b)\n" +
                           "            System.err.println(1);\n" +
                           "\t    System.err.println(1);\n" +
                           "    }" +
                           "}\n")
                    .run(Tiny.class)
                    .assertWarnings("5:5-5:27:verifier:" + Bundle.ERR_indentation());
        } finally {
            reset(original);
        }
    }
    
    public void testInconsistentIndentationLast() throws Exception {
        Map<String, String> original = alterSettings(FmtOptions.tabSize, "8");
        
        try {
            HintTest.create()
                    .input("package test;\n" +
                           "public class Test {\n" +
                           "    public void test(boolean b) {\n" +
                           "        if (b)\n" +
                           "            System.err.println(1);\n" +
                           "    }" +
                           "}\n")
                    .run(Tiny.class)
                    .assertWarnings();
        } finally {
            reset(original);
        }
    }

    public void testVarUsageWithoutExplicitType() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        var v = new java.util.ArrayList<>();\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("11")
                .run(Tiny.class)
                .assertWarnings("3:8-3:44:verifier:ERR_varTypeDiamondOperator");
    }
    
    public void testVarUsageWithoutExplicitType2() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        var v = new java.util.HashSet<>();\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("11")
                .run(Tiny.class)
                .assertWarnings("3:8-3:42:verifier:ERR_varTypeDiamondOperator");
    }
    
    public void testVarUsageWithoutExplicitType3() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        var v = new java.util.HashMap<>();\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("11")
                .run(Tiny.class)
                .assertWarnings("3:8-3:42:verifier:ERR_varTypeDiamondOperator");
    }
    
    public void testVarUsageWithExplicitType() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        var v = new java.util.ArrayList<Integer>();\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("11")
                .run(Tiny.class)
                .assertWarnings();
    }

    public void testVarUsageWithExplicitType2() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        var v = new String[2];\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("11")
                .run(Tiny.class)
                .assertWarnings();
    }
    
    public void testWithoutVarUsageWithExplicitType() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        java.util.List<Integer> v = new java.util.ArrayList<Integer>();\n" +
                       "    }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings();
    }
    
    public void testWithoutVarUsageWithExplicitType2() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        java.util.List<Integer> v = new java.util.ArrayList<>();\n" +
                       "    }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings();
    }
    
    public void testVarUsageSensibleTypeInferred1() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(java.util.Set<String> input) {\n" +
                       "        var v = new java.util.HashSet<>(input);\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("11")
                .run(Tiny.class)
                .assertWarnings();
    }

    public void testVarUsageSensibleTypeInferred2() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(java.util.Map<String, String> input) {\n" +
                       "        var v = new java.util.HashMap<>(input);\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("11")
                .run(Tiny.class)
                .assertWarnings();
    }

    private static Map<String, String> alterSettings(String... settings) throws Exception {
        //XXX: hack, need to initialize the HintTest's lookup before setting the
        //formatting preferences
        HintTest.create();
        
        Map<String, String> adjustPreferences = new HashMap<String, String>();
        for (int i = 0; i < settings.length; i += 2) {
            adjustPreferences.put(settings[i], settings[i + 1]);
        }
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        Map<String, String> origValues = new HashMap<String, String>();
        for (String key : adjustPreferences.keySet()) {
            origValues.put(key, preferences.get(key, null));
        }
        setValues(preferences, adjustPreferences);
        return origValues;
    }
    
    private static void reset(Map<String, String> values) {
        setValues(MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class), values);
    }
    
    private static void setValues(Preferences p, Map<String, String> values) {
        for (Entry<String, String> e : values.entrySet()) {
            if (e.getValue() != null) {
                p.put(e.getKey(), e.getValue());
            } else {
                p.remove(e.getKey());
            }
        }
    }
}
