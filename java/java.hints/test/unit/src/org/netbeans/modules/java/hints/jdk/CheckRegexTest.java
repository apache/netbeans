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
package org.netbeans.modules.java.hints.jdk;

import org.junit.Test;
import org.netbeans.modules.java.hints.test.api.HintTest;

public class CheckRegexTest {

    @Test
    public void testWarningProduced() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "import java.util.regex.Pattern;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        Pattern pattern = Pattern.compile(\"a(b|c|d)\");\n"
                        + "    }\n"
                        + "}\n")
                .run(CheckRegex.class)
                .assertWarnings("4:34-4:41:hint:" + Bundle.ERR_CheckRegex());
    }
    
    @Test
    public void testWarningProduced2() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "import java.util.regex.Pattern;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        boolean matches = Pattern.matches(\"\\\\d\", \"abc\");\n"
                        + "    }\n"
                        + "}\n")
                .run(CheckRegex.class)
                .assertWarnings("4:34-4:41:hint:" + Bundle.ERR_CheckRegex());
    }
    
}
