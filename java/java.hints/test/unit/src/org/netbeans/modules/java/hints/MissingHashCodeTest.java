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
package org.netbeans.modules.java.hints;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author Jaroslav Tulach
 */
public class MissingHashCodeTest extends NbTestCase {

    public MissingHashCodeTest(String testName) {
        super(testName);
    }

    public void testMissingHashCode() throws Exception {
        String before = "package test; public class Test extends Object {" + " public boolean ";
        String after = " equals(Object snd) {" + "  return snd != null && getClass().equals(snd.getClass());" + " }" + "}";

        HintTest
                .create()
                .input(before + after)
                .run(MissingHashCode.class)
                .assertWarnings("0:65-0:71:verifier:MSG_GenHashCode");
    }

    public void testMissingEquals() throws Exception {
        String before = "package test; public class Test extends Object {" + " public int ";
        String after = " hashCode() {" + "  return 1;" + " }" + "}";

        HintTest
                .create()
                .input(before + after)
                .run(MissingHashCode.class)
                .assertWarnings("0:61-0:69:verifier:MSG_GenEquals");
    }

    public void testWhenNoFieldsGenerateHashCode() throws Exception {
        String before = "package test; public class Test extends Object {" + " public boolean equa";
        String after = "ls(Object snd) { return snd == this; } }";

        String res = HintTest
                .create()
                .input(before + after)
                .run(MissingHashCode.class)
                .findWarning("0:64-0:70:verifier:MSG_GenHashCode")
                .applyFix("MSG_GenHashCode")
                .getOutput()
                .replaceAll("[ \t\n]+", " ");

        if (!res
                .matches(".*equals.*hashCode.*")) {
            fail("We want equals and hashCode:\n" + res);
        }
    }

    public void testWhenNoFieldsGenerateEquals() throws Exception {
        String before = "package test; public class Test extends Object {" + " public int hash";
        String after = "Code() { return 1; } }";

        String res = HintTest
                .create()
                .input(before + after)
                .run(MissingHashCode.class)
                .findWarning("0:60-0:68:verifier:MSG_GenEquals")
                .applyFix("MSG_GenEquals")
                .getOutput()
                .replaceAll("[ \t\n]+", " ");

        if (!res
                .matches(".*hashCode.*equals.*")) {
            fail("We want equals and hashCode:\n" + res);
        }
    }
}
