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
 * @author schmidtm
 */
public class CollectionsCCTest extends GroovyCCTestBase {

    public CollectionsCCTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestType() {
        return "collections";
    }

    // testing proper creation of constructor-call proposals

    //     * groovy.lang.*
    //     * groovy.util.*

    public void testCollections1_1() throws Exception {
        checkCompletion(BASE + "" + "Collections1.groovy", "[\"one\",\"two\"].listIter^", false);
    }

    public void testCollections1_2() throws Exception {
        checkCompletion(BASE + "" + "Collections1.groovy", "[1:\"one\", 2:\"two\"].ent^", false);
    }

    public void testCollections1_3() throws Exception {
        checkCompletion(BASE + "" + "Collections1.groovy", "    (1..10).a^", false);
    }

    public void testCollections1_4() throws Exception {
        checkCompletion(BASE + "" + "Collections1.groovy", "    1..10.d^", false);
    }

    public void testCollections1_5() throws Exception {
        checkCompletion(BASE + "" + "Collections1.groovy", "    (1..10).^", false);
    }

    public void testCollections1_6() throws Exception {
        checkCompletion(BASE + "" + "Collections1.groovy", "[\"one\",\"two\"].it^", false);
    }
}
