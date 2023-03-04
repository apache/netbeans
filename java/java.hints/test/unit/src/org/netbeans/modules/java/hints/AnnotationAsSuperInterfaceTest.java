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
 * @author lahvac
 */
public class AnnotationAsSuperInterfaceTest extends NbTestCase {

    public AnnotationAsSuperInterfaceTest(String name) {
        super(name);
    }

    public void testAnalysis1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test implements Deprecated {\n" +
                       "}\n", false)
                .run(AnnotationAsSuperInterface.class)
                .assertWarnings("1:29-1:39:verifier:HNT_AnnotationAsSuperInterface (Deprecated)");
    }

    public void testAnalysis2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test implements Runnable {\n" +
                       "}\n", false)
                .run(AnnotationAsSuperInterface.class)
                .assertWarnings();
    }

    public void testAnalysis3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test implements Deprecated, SuppressWarnings {\n" +
                       "}\n", false)
                .run(AnnotationAsSuperInterface.class)
                .assertWarnings("1:29-1:39:verifier:HNT_AnnotationAsSuperInterface (Deprecated)",
                                "1:41-1:57:verifier:HNT_AnnotationAsSuperInterface (SuppressWarnings)");
    }
}
