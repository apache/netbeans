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

/**
 *
 * @author lahvac
 */
public class AnnotationProcessorsTest {

    @Test
    public void testOverridingGetSupportedAnnotations() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public abstract class Test extends javax.annotation.processing.AbstractProcessor {\n" +
                       "    @Override public java.util.Set<String> getSupportedAnnotationTypes() {return null;}\n" +
                       "}\n")
                .run(AnnotationProcessors.class)
                .assertWarnings("1:0-1:82:verifier:" + Bundle.HINT_AnnoProcessor_NoSupportedSource(),
                                "2:43-2:70:verifier:ERR_AnnotationProcessors.overridingGetSupportedAnnotations");
    }
}
