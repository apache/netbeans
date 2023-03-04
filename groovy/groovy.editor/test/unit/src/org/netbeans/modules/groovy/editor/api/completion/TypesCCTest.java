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
public class TypesCCTest extends GroovyCCTestBase {

    public TypesCCTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestType() {
        return "types";
    }

    public void testFqnTypeCompletion1() throws Exception {
        checkCompletion(BASE + "FqnTypeCompletion1.groovy", "groovy.time.^", false);
    }

    // we don't get proper AST for this mini-class, disable it for now.
//    public void testTypeCompletion1() throws Exception {
//        checkCompletion(getTestFolderPath() + "" + "TypeCompletion1.groovy", "class Bar { ^}", false);
//        // assertTrue(false);
//    }

    public void testTypeCompletion2() throws Exception {
        checkCompletion(BASE + "TypeCompletion2.groovy", "class Pre { Cl^ }", false);
    }

    public void testTypeCompletion3() throws Exception {
        checkCompletion(BASE + "TypeCompletion3.groovy", "    Cl^ }", false);
    }

    public void testTypeCompletion4() throws Exception {
        checkCompletion(BASE + "TypeCompletion4.groovy", "class Pre { Cl^", false);
    }

    public void testTypeCompletion5() throws Exception {
        checkCompletion(BASE + "TypeCompletion5.groovy", "    No^", false);
    }

    public void testManualImport1() throws Exception {
        checkCompletion(BASE + "ManualImport1.groovy", "println Sign^", false);
    }  

    public void testManualImport2() throws Exception {
        checkCompletion(BASE + "ManualImport2.groovy", "println Can^", false);
    }

    public void testDefaultImport1_1() throws Exception {
        checkCompletion(BASE + "DefaultImport1.groovy", "FileRea^", false);
    }

    public void testDefaultImport1_2() throws Exception {
        checkCompletion(BASE + "DefaultImport1.groovy", "ClassCastExc^", false);
    }

    public void testDefaultImport1_3() throws Exception {
        checkCompletion(BASE + "DefaultImport1.groovy", "BigDec^", false);
    }

    public void testDefaultImport1_4() throws Exception {
        checkCompletion(BASE + "DefaultImport1.groovy", "BigInte^", false);
    }

    public void testDefaultImport1_5() throws Exception {
        checkCompletion(BASE + "DefaultImport1.groovy", "HttpU^", false);
    }

    public void testDefaultImport1_6() throws Exception {
        checkCompletion(BASE + "DefaultImport1.groovy", "Scan^", false);
    }

    // make sure, we don't complete in comments.
    // not in block comments.
    public void testNotInComments1_1() throws Exception {
        checkCompletion(BASE + "NotInComments1.groovy", "Groovy def^", false);
    }

    // ... and also not in line comments.
    public void testNotInComments1_2() throws Exception {
        checkCompletion(BASE + "NotInComments1.groovy", "java.lang.ClassCastException^", false);
    }

    // test for types defined in the very same file
    public void testSamePackage1() throws Exception {
        checkCompletion(BASE + "SamePackage1.groovy", "println TestSamePack^", false);
    }

    // testing wildcard-imports
//    public void testWildCardImport1() throws Exception {
//        checkCompletion(getTestFolderPath() + "" + "WildCardImport1.groovy", "new Mark^", false);
//    }
}
