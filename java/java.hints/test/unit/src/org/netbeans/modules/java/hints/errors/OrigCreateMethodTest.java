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

import org.netbeans.modules.java.hints.infrastructure.HintsTestBase;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class OrigCreateMethodTest extends HintsTestBase {

    public OrigCreateMethodTest(String name) {
        super(name);
    }
    
    public void testCreateElement1() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement1", "Method", 23, 16);
    }

    public void testCreateElement2() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement2", "Method", 23, 16);
    }

    public void testCreateElement3() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement3", "Method", 24, 16);
    }

    public void testCreateElement4() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement4", "Method", 23, 16);
    }

    public void testCreateElement5() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement5", "Method", 23, 16);
    }

    public void testCreateElement6() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement6", "Method", 23, 16);
    }

    public void testCreateElement7() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement7", "Method", 23, 16);
    }

    public void testCreateElement8() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement8", "Method", 24, 16);
    }

    public void testCreateElement9() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement9", "Method", 23, 16);
    }

    public void testCreateElementa() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElementa", "Method", 23, 16);
    }
    
    public void testCreateConstructor1() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateConstructor1", "Create Constructor", 9, 16);
    }

    @Override
    protected String testDataExtension() {
        return "org/netbeans/test/java/hints/OrigCreateMethodTest/";
    }

    static {
        NbBundle.setBranding("test");
    }
}
