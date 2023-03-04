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

import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Jancura
 */
public class ShiftOutOfRangeTest extends NbTestCase {

    public ShiftOutOfRangeTest(String name) {
        super(name);
    }

    @Test
    public void testOk() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        i = i >> 10;\n" +
                       "        i = i >>> 10;\n" +
                       "        i = i << 10;\n" +
                       "        i = i >> 10l;\n" +
                       "        i = i >>> 10l;\n" +
                       "        i = i << 10l;\n" +
                       "        long l = 35;\n" +
                       "        l = l >> 35;\n" +
                       "        l = l >>> 35;\n" +
                       "        l = l << 35;\n" +
                       "        l = l >> 35l;\n" +
                       "        l = l >>> 35l;\n" +
                       "        l = l << 35l;\n" +
                       "        l = l >> '0';\n" +
                       "        l = l >>> '0';\n" +
                       "        l = l << '0';\n" +
                       "    }\n" +
                       "}")
                .run(ShiftOutOfRange.class)
                .assertWarnings();
    }

    @Test
    public void testOk2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static final int I = 10;\n" +
                       "    static final long L = 10l;\n" +
                       "    void test () {\n" +
                       "        int i = I;\n" +
                       "        i = i >> I;\n" +
                       "        i = i >>> I;\n" +
                       "        i = i << I;\n" +
                       "        i = i >> L;\n" +
                       "        i = i >>> L;\n" +
                       "        i = i << L;\n" +
                       "        long l = 35;\n" +
                       "        l = l >> I;\n" +
                       "        l = l >>> I;\n" +
                       "        l = l << I;\n" +
                       "        l = l >> L;\n" +
                       "        l = l >>> L;\n" +
                       "        l = l << L;\n" +
                       "    }\n" +
                       "}")
                .run(ShiftOutOfRange.class)
                .assertWarnings();
    }

    @Test
    public void testWarning() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        i = i >> 33;\n" +
                       "        i = i >>> 33;\n" +
                       "        i = i << 33;\n" +
                       "        i = i >> -1;\n" +
                       "        i = i >>> -1;\n" +
                       "        i = i << -1;\n" +
                       "        i = i >> 33l;\n" +
                       "        i = i >>> 33l;\n" +
                       "        i = i << 33l;\n" +
                       "        long l = 35;\n" +
                       "        l = l >> 65;\n" +
                       "        l = l >>> 65;\n" +
                       "        l = l << 65;\n" +
                       "        l = l >> -1;\n" +
                       "        l = l >>> -1;\n" +
                       "        l = l << -1;\n" +
                       "        l = l >> 65l;\n" +
                       "        l = l >>> 65l;\n" +
                       "        l = l << 65l;\n" +
                       "        l = l >> -1l;\n" +
                       "        l = l >>> -1l;\n" +
                       "        l = l << -1l;\n" +
                       "    }\n" +
                       "}")
                .run(ShiftOutOfRange.class)
                .assertWarnings(
                "4:12-4:19:verifier:Shift operation outside of the reasonable range 0..31",
                "5:12-5:20:verifier:Shift operation outside of the reasonable range 0..31",
                "6:12-6:19:verifier:Shift operation outside of the reasonable range 0..31",
                "7:12-7:19:verifier:Shift operation outside of the reasonable range 0..31",
                "8:12-8:20:verifier:Shift operation outside of the reasonable range 0..31",
                "9:12-9:19:verifier:Shift operation outside of the reasonable range 0..31",
                "10:12-10:20:verifier:Shift operation outside of the reasonable range 0..31",
                "11:12-11:21:verifier:Shift operation outside of the reasonable range 0..31",
                "12:12-12:20:verifier:Shift operation outside of the reasonable range 0..31",
                "14:12-14:19:verifier:Shift operation outside of the reasonable range 0..63",
                "15:12-15:20:verifier:Shift operation outside of the reasonable range 0..63",
                "16:12-16:19:verifier:Shift operation outside of the reasonable range 0..63",
                "17:12-17:19:verifier:Shift operation outside of the reasonable range 0..63",
                "18:12-18:20:verifier:Shift operation outside of the reasonable range 0..63",
                "19:12-19:19:verifier:Shift operation outside of the reasonable range 0..63",
                "20:12-20:20:verifier:Shift operation outside of the reasonable range 0..63",
                "21:12-21:21:verifier:Shift operation outside of the reasonable range 0..63",
                "22:12-22:20:verifier:Shift operation outside of the reasonable range 0..63",
                "23:12-23:20:verifier:Shift operation outside of the reasonable range 0..63",
                "24:12-24:21:verifier:Shift operation outside of the reasonable range 0..63",
                "25:12-25:20:verifier:Shift operation outside of the reasonable range 0..63");
    }

    @Test
    public void testWarning2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static final int I1 = 33;\n" +
                       "    static final int I2 = -1;\n" +
                       "    static final long L1 = 33l;\n" +
                       "    static final long I3 = 65;\n" +
                       "    static final long L2 = 65l;\n" +
                       "    static final long L3 = -1l;\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        i = i >> I1;\n" +
                       "        i = i >>> I1;\n" +
                       "        i = i << I1;\n" +
                       "        i = i >> I2;\n" +
                       "        i = i >>> I2;\n" +
                       "        i = i << I2;\n" +
                       "        i = i >> L1;\n" +
                       "        i = i >>> L1;\n" +
                       "        i = i << L1;\n" +
                       "        long l = 35;\n" +
                       "        l = l >> I3;\n" +
                       "        l = l >>> I3;\n" +
                       "        l = l << I3;\n" +
                       "        l = l >> I2;\n" +
                       "        l = l >>> I2;\n" +
                       "        l = l << I2;\n" +
                       "        l = l >> L2;\n" +
                       "        l = l >>> L2;\n" +
                       "        l = l << L2;\n" +
                       "        l = l >> L3;\n" +
                       "        l = l >>> L3;\n" +
                       "        l = l << L3;\n" +
                       "    }\n" +
                       "}")
                .run(ShiftOutOfRange.class)
                .assertWarnings(
                "10:12-10:19:verifier:Shift operation outside of the reasonable range 0..31",
                "11:12-11:20:verifier:Shift operation outside of the reasonable range 0..31",
                "12:12-12:19:verifier:Shift operation outside of the reasonable range 0..31",
                "13:12-13:19:verifier:Shift operation outside of the reasonable range 0..31",
                "14:12-14:20:verifier:Shift operation outside of the reasonable range 0..31",
                "15:12-15:19:verifier:Shift operation outside of the reasonable range 0..31",
                "16:12-16:19:verifier:Shift operation outside of the reasonable range 0..31",
                "17:12-17:20:verifier:Shift operation outside of the reasonable range 0..31",
                "18:12-18:19:verifier:Shift operation outside of the reasonable range 0..31",
                "20:12-20:19:verifier:Shift operation outside of the reasonable range 0..63",
                "21:12-21:20:verifier:Shift operation outside of the reasonable range 0..63",
                "22:12-22:19:verifier:Shift operation outside of the reasonable range 0..63",
                "23:12-23:19:verifier:Shift operation outside of the reasonable range 0..63",
                "24:12-24:20:verifier:Shift operation outside of the reasonable range 0..63",
                "25:12-25:19:verifier:Shift operation outside of the reasonable range 0..63",
                "26:12-26:19:verifier:Shift operation outside of the reasonable range 0..63",
                "27:12-27:20:verifier:Shift operation outside of the reasonable range 0..63",
                "28:12-28:19:verifier:Shift operation outside of the reasonable range 0..63",
                "29:12-29:19:verifier:Shift operation outside of the reasonable range 0..63",
                "30:12-30:20:verifier:Shift operation outside of the reasonable range 0..63",
                "31:12-31:19:verifier:Shift operation outside of the reasonable range 0..63");
    }

    static {
        NbBundle
                .setBranding("test");
    }
}