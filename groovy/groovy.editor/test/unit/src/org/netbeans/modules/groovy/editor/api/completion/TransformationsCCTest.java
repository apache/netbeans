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

package org.netbeans.modules.groovy.editor.api.completion;

/**
 *
 * @author Petr Hejl
 */
public class TransformationsCCTest extends GroovyCCTestBase {

    public TransformationsCCTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestType() {
        return "transformations"; //NOI18N
    }

    public void testSingleton1_withInPrefix() throws Exception {
        checkCompletion(BASE + "Singleton1.groovy", "        Singleton1.in^", true);
    }

    public void testSingleton2_withGetPrefix() throws Exception {
        checkCompletion(BASE + "Singleton2.groovy", "        Singleton2.get^", true);
    }

    public void testSingleton3_withoutPrefix() throws Exception {
        checkCompletion(BASE + "Singleton3.groovy", "        Singleton3.^", true);
    }

    public void testDelegate1_interfaceDelegator_withoutPrefix() throws Exception {
        checkCompletion(BASE + "Delegate1.groovy", "showcase.^", true);
    }

    public void testDelegate2_classDelegator_withPrefix() throws Exception {
        checkCompletion(BASE + "Delegate2.groovy", "showcase.f^", true);
    }
}
