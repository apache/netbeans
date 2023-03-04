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
package org.netbeans.modules.java.hints.errors;

import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;

/**
 *
 * @author lahvac
 */
public class ArrayAccessTest extends ErrorHintsTestBase {
    
    public ArrayAccessTest(String name) {
        super(name, ArrayAccess.class);
    }

    public void testReadList() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.util.List;\n" +
                       "public class Test {\n" +
                       "    private String g(List<String> l, int i) {\n" +
                       "        return l[i];\n" +
                       "    }\n" +
                       "}",
                       -1,
                       Bundle.FIX_UseListGet(),
                       ("package test;\n" +
                        "import java.util.List;\n" +
                        "public class Test {\n" +
                        "    private String g(List<String> l, int i) {\n" +
                        "        return l.get(i);\n" +
                        "    }\n" +
                        "}").replaceAll("[ \t\r\n]+", " "));
    }
    
    public void testWriteList() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.util.List;\n" +
                       "public class Test {\n" +
                       "    private void g(List<String> l, int i) {\n" +
                       "        l[i] = null;\n" +
                       "    }\n" +
                       "}",
                       -1,
                       Bundle.FIX_UseListSet(),
                       ("package test;\n" +
                        "import java.util.List;\n" +
                        "public class Test {\n" +
                        "    private void g(List<String> l, int i) {\n" +
                        "        l.set(i, null);\n" +
                        "    }\n" +
                        "}").replaceAll("[ \t\r\n]+", " "));
    }
    
    public void testReadMap() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.util.Map;\n" +
                       "public class Test {\n" +
                       "    private String g(Map<String, String> m, String key) {\n" +
                       "        return m[key];\n" +
                       "    }\n" +
                       "}",
                       -1,
                       Bundle.FIX_UseMapGet(),
                       ("package test;\n" +
                        "import java.util.Map;\n" +
                        "public class Test {\n" +
                        "    private String g(Map<String, String> m, String key) {\n" +
                        "        return m.get(key);\n" +
                        "    }\n" +
                        "}").replaceAll("[ \t\r\n]+", " "));
    }
    
    public void testWriteMap() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.util.Map;\n" +
                       "public class Test {\n" +
                       "    private void g(Map<String, String> m, String key, String value) {\n" +
                       "        m[key] = value;\n" +
                       "    }\n" +
                       "}",
                       -1,
                       Bundle.FIX_UseMapPut(),
                       ("package test;\n" +
                        "import java.util.Map;\n" +
                        "public class Test {\n" +
                        "    private void g(Map<String, String> m, String key, String value) {\n" +
                        "        m." + /*XXX:*/" " + "put(key, value);\n" +
                        "    }\n" +
                        "}").replaceAll("[ \t\r\n]+", " "));
    }
}