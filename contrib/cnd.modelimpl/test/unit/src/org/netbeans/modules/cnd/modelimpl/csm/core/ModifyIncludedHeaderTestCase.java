/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.File;

/**
 *
 */
public class ModifyIncludedHeaderTestCase extends ModifyDocumentTestCaseBase {
    public ModifyIncludedHeaderTestCase(String testName) {
        super(testName);
    }

    public void test207091() throws Exception {
        // #207091: Definitions in include file not reflected in .c file
        final File sourceFile = getDataFile("headerForModification.h");
        final File checkedFile = getDataFile("fileToBeChecked.cc");
        super.insertTextThenSaveAndCheck(sourceFile, 1, "#define ABC\n", 
                checkedFile, new DeadBlocksNumberChecker(1, 0), false);
    }

    public void testOwnIncludedStorageInvalidation() throws Exception {
        // 
        final File sourceFile = getDataFile("headerForModification.h");
        final File checkedFile = getDataFile("fileToBeChecked.cc");
        super.insertTextThenSaveAndCheck(sourceFile, 1, "#define ABC\n",
                checkedFile, new DeadBlocksNumberChecker(1, 0), false);
    }

}
