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
 * @author Jan Lahoda
 */
public class SyncOnNonFinalTest extends NbTestCase {

    public SyncOnNonFinalTest(String testName) {
        super(testName);
    }

    public void testSimple1() throws Exception {
        HintTest
                .create()
                .input("package test; public class Test {private Object o; private void t() {synchronized(o) {}}}")
                .run(SyncOnNonFinal.class)
                .assertWarnings("0:81-0:84:verifier:Synchronization on non-final field");
    }

    public void testSimple2() throws Exception {
        HintTest
                .create()
                .input("package test; public class Test {private final Object o = new Object(); private void t() {synchronized(o) {}}}")
                .run(SyncOnNonFinal.class)
                .assertWarnings();
    }

    public void testSimple3() throws Exception {
        HintTest
                .create()
                .input("package test; public class Test {private void t() {Object o = null; synchronized(o) {}}}")
                .run(SyncOnNonFinal.class)
                .assertWarnings();
    }
}
