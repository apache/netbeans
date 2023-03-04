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
public class AddOverrideAnnotationTest extends NbTestCase {
    
    public AddOverrideAnnotationTest(String testName) {
        super(testName);
    }
    
    public void testAddOverride1() throws Exception {
        HintTest.create()
                .input("package test; public class Test extends java.util.ArrayList {public int size() {return 0;}}")
                .run(AddOverrideAnnotation.class)
                .assertWarnings("0:72-0:76:verifier:Add @Override Annotation");
    }

    public void testAddOverride2() throws Exception {
        HintTest.create()
                .input("package test; public class Test implements Runnable {public void run() {}}")
                .run(AddOverrideAnnotation.class)
                .assertWarnings();
    }
    
    public void testAddOverride3() throws Exception {
        HintTest.create()
                .input("package test; public class Test implements Runnable {public void run() {}}")
                .sourceLevel("1.6")
                .run(AddOverrideAnnotation.class)
                .assertWarnings("0:65-0:68:verifier:Add @Override Annotation");
    }
    
    public void testAddOverride4() throws Exception {
        HintTest.create()
                .input("package test; public class UUUU {public void () {} private static class W extends UUUU {public void () {}}}", false)
                .run(AddOverrideAnnotation.class)
                .assertWarnings();
    }
    
}
