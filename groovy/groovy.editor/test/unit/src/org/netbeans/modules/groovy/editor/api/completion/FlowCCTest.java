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
 * @author sreimers
 */
public class FlowCCTest extends GroovyCCTestBase {

    public FlowCCTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestType() {
        return "flow"; //NOI18N
    }

    public void testCompletionReturnType1_1() throws Exception {
        checkCompletion(BASE + "CompletionReturnType1.groovy", "def fieldB = fieldA.c^", false);
    }

    public void testCompletionReturnType1_2() throws Exception {
        checkCompletion(BASE + "CompletionReturnType1.groovy", "def fieldB1 = fieldA1.c^", false);
    }

    public void testCompletionReturnType1_3() throws Exception {
        checkCompletion(BASE + "CompletionReturnType1.groovy", "fieldE.c^", false);
    }

    public void testCompletionReturnType1_4() throws Exception {
        checkCompletion(BASE + "CompletionReturnType1.groovy", "localA.c^", false);
    }

    public void testCompletionReturnType1_5() throws Exception {
        checkCompletion(BASE + "CompletionReturnType1.groovy", "localA.concat(\"b\").c^", false);
    }

    public void testCompletionReturnType1_6() throws Exception {
        checkCompletion(BASE + "CompletionReturnType1.groovy", "localA.concat(localB).c^", false);
    }

    public void testCompletionReturnType1_7() throws Exception {
        checkCompletion(BASE + "CompletionReturnType1.groovy", "localC.c^", false);
    }
    
    public void testCollectionLiterals1_1() throws Exception {
        checkCompletion(BASE + "CollectionLiterals1.groovy", "range1.a^", false);
    }

    public void testCollectionLiterals1_2() throws Exception {
        checkCompletion(BASE + "CollectionLiterals1.groovy", "list1.listIter^", false);
    }

    public void testCollectionLiterals1_3() throws Exception {
        checkCompletion(BASE + "CollectionLiterals1.groovy", "map.ent^", false);
    }
    
    public void testCollectionLiterals2_1() throws Exception {
        checkCompletion(BASE + "CollectionLiterals2.groovy", "range1.a^", false);
    }

    public void testCollectionLiterals2_2() throws Exception {
        checkCompletion(BASE + "CollectionLiterals2.groovy", "list1.listIter^", false);
    }

    public void testCollectionLiterals2_3() throws Exception {
        checkCompletion(BASE + "CollectionLiterals2.groovy", "map.ent^", false);
    }
    
    public void testReassignment_1() throws Exception {
        checkCompletion(BASE + "Reassignment.groovy", "def sub = varA.subs^", false);
    }

    public void testReassignment_2() throws Exception {
        checkCompletion(BASE + "Reassignment.groovy", "varA.lis^", false);
    }
    
}
